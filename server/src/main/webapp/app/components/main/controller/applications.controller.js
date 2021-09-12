// Localization completed
angular.module('headwind-kiosk')
    .controller('ApplicationsTabController', function ($scope, $rootScope, $modal, confirmModal, applicationService,
                                                       authService, $window, localization, alertService, $state) {

        $scope.authService = authService;

        $scope.search = {};
        $scope.loading = false;

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.showMyAppsOnly = {
            on: ($window.localStorage.getItem('HMDM_showMyAppsOnly') === 'true'),
            system: true,
        };
        let item = $window.localStorage.getItem('HMDM_showSystemApps');
        if (item !== null && item !== undefined) {
            $scope.showMyAppsOnly.system = (item === 'true');
        }

        $scope.myAppsButtonVisible = false;

        $scope.showMyAppsOnlyToggled = function () {
            $window.localStorage.setItem('HMDM_showMyAppsOnly', $scope.showMyAppsOnly.on);
            $scope.init();
        };

        $scope.showSystemAppsOnlyToggled = function () {
            $window.localStorage.setItem('HMDM_showSystemApps', $scope.showMyAppsOnly.system);
            $scope.init();
        };

        $scope.$watch('paging.currentPage', function () {
            $window.scrollTo(0, 0);
        });

        $scope.hasPermission = authService.hasPermission;

        $scope.init = function () {
            $rootScope.settingsTabActive = false;
            $rootScope.pluginsTabActive = false;
            $scope.paging.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            $scope.loading = true;
            applicationService.getAllApplications({value: $scope.search.searchValue},
                function (response) {
                    $scope.loading = false;
                    $scope.applications = response.data.filter(function (app) {
                        return ($scope.showMyAppsOnly.on && !app.common || !$scope.showMyAppsOnly.on) &&
                            ($scope.showMyAppsOnly.system && app.system || !app.system);
                    });

                    $scope.myAppsButtonVisible = (response.data.find(function (app) {return app.common;}) !== undefined);
                    
                }, function () {
                    $scope.loading = false;
                });
        };

        $scope.removeApplication = function (application) {
            let localizedText = localization.localize('question.delete.application').replace('${applicationName}', application.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                applicationService.removeApplication({id: application.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else if (response.status === 'ERROR') {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                });
            });
        };

        $scope.editApplication = function (application) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/application.html',
                controller: 'ApplicationModalController',
                resolve: {
                    application: function () {
                        return application;
                    },
                    isControlPanel: function () {
                        return false;
                    },
                    closeOnSave: function () {
                        return false;
                    }
                }
            });

            modalInstance.result.then($scope.search, $scope.search);
        };

        $scope.clarifyOnCommon = function () {
            alertService.showAlertMessage(localization.localize('common.app.clarification'));
        };

        $scope.editConfiguration = function (application) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationConfigurations.html',
                controller: 'ApplicationConfigurationsModalController',
                resolve: {
                    application: function () {
                        return application;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });

        };

        $scope.pkgInfoVisible = function (application) {
            return application.type !== 'web';
        };

        $scope.editVersions = function (application) {
            $state.transitionTo('appVersionsEditor', {"id": application.id});
        };

        $scope.init();
    })
    .controller('ApplicationModalController', function ($scope, $modalInstance, applicationService, iconService,
                                                        application,
                                                        $modal, isControlPanel, localization, closeOnSave) {
        $scope.isControlPanel = isControlPanel;

        $scope.localization = localization;

        $scope.isNewApp = application.id === null || application.id === undefined;

        $scope.application = angular.copy(application, {});
        if ($scope.application.iconId === null || $scope.application.iconId === undefined) {
            $scope.application.iconId = -1;
        }

        $scope.appdesc = {};

        $scope.icons = [{id: -1, name: localization.localize("form.application.icon.default")}];

        const loadIcons = function (callback) {
            iconService.getAllIcons(function (response) {
                if (response.status === 'OK') {
                    $scope.icons = $scope.icons.concat(response.data);
                }
            });
        };

        loadIcons();

        if ($scope.isNewApp) {
            $scope.loading = false;
            $scope.file = {};
            $scope.fileName = null;
            $scope.invalidFile = false;
            $scope.fileSelected = false;
            
            $scope.application.type = 'app';
        }

        $scope.onStartedUpload = function (files) {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
            $scope.invalidFile = false;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.fileName = files[0].name;
                if ($scope.fileName.endsWith(".apk") || $scope.fileName.endsWith(".xapk")) {
                    $scope.loading = true;
                    $scope.successMessage = localization.localize('success.uploading.file');
                } else {
                    $scope.errorMessage = localization.localize('error.apk.file.required');
                    $scope.invalidFile = true;
                }
            }
        };

        $scope.onUploadProgress = function(progress) {
            var loadedMb = (progress.loaded / 1048576).toFixed(1);
            var totalMb = (progress.total / 1048576).toFixed(1);
            $scope.successMessage = localization.localize('success.uploading.file') +
                " " + loadedMb + " / " + totalMb + " Mb";
        };

        $scope.fileUploaded = function (response) {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
            $scope.fileSelected = false;

            $scope.loading = false;

            if (!$scope.invalidFile) {
                if (response.data.status === 'OK') {
                    $scope.file.path = response.data.data.serverPath;
                    if (response.data.data.application) {
                        var app = response.data.data.application;
                        $scope.application.name = app.name;
                        $scope.application.showIcon = app.showIcon;
                        $scope.application.useKiosk = app.useKiosk;
                        $scope.application.runAfterInstall = app.runAfterInstall;
                        $scope.application.runAtBoot = app.runAtBoot;
                        $scope.application.system = app.system;
                    }
                    if (response.data.data.fileDetails) {
                        var fileDetails = response.data.data.fileDetails;
                        $scope.application.pkg = fileDetails.pkg;
                        $scope.appdesc.pkg = fileDetails.pkg + " - " + localization.localize("form.application.from.file");
                        $scope.application.version = fileDetails.version;
                        $scope.appdesc.version = fileDetails.version + " - " + localization.localize("form.application.from.file");
                        $scope.application.arch = fileDetails.arch;

                        $scope.appTypeWarning = null;
                        $scope.appTypeSuccess = null;
                        $scope.complete = null;
                        if (response.data.data.exists) {
                            $scope.appTypeWarning = localization.localize('form.application.version.exists');
                        } else if (response.data.data.complete) {
                            $scope.appTypeSuccess = localization.localize('form.application.arch.success');
                            $scope.complete = true;
                        } else if (fileDetails.arch) {
                            $scope.appTypeWarning = localization.localize('form.application.arch.warning').replace('${arch}', fileDetails.arch);
                        }
                    }
                    $scope.successMessage = localization.localize('success.file.uploaded');
                    $scope.fileSelected = true;
                } else {
                    $scope.errorMessage = localization.localize('error.apk.parse');
                }
            } else {
                $scope.errorMessage = localization.localize('error.apk.file.required');
            }
        };

        $scope.clearFile = function () {
            $scope.file = {};
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
            $scope.fileSelected = false;
            $scope.invalidFile = false;
            $scope.loading = false;
        };

        const doSaveWebApplication = function (request) {
            applicationService.updateWebApplication(request, function (response) {
                if (response.status === 'OK') {
                    if (!closeOnSave) {
                        if ($scope.isNewApp) {
                            $scope.application = response.data;
                            $scope.isNewApp = false;
                            $scope.file = {};
                            $scope.loading = false;
                            $scope.fileName = null;
                            $scope.invalidFile = false;
                            $scope.fileSelected = false;
                            $scope.manageConfigurations(true);
                        } else {
                            $modalInstance.close();
                        }
                    } else {
                        $modalInstance.close(response.data);
                    }
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        var doSaveAndroidApplication = function (request) {
            applicationService.updateApplication(request, function (response) {
                if (response.status === 'OK') {
                    if (!closeOnSave) {
                        if ($scope.isNewApp) {
                            $scope.application = response.data;
                            $scope.isNewApp = false;
                            $scope.file = {};
                            $scope.loading = false;
                            $scope.fileName = null;
                            $scope.invalidFile = false;
                            $scope.fileSelected = false;
                            $scope.manageConfigurations(true);
                        } else {
                            $modalInstance.close();
                        }
                    } else {
                        $modalInstance.close(response.data);
                    }
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        var doSaveApplicationVersion = function (request, app) {
            applicationService.updateApplicationVersion(request, function (response) {
                if (response.status === 'OK') {
                    if (!closeOnSave) {
                        if ($scope.isNewApp) {
                            $scope.application = undefined;
                            $scope.isNewApp = false;
                            $scope.file = {};
                            $scope.loading = false;
                            $scope.fileName = null;
                            $scope.invalidFile = false;
                            $scope.fileSelected = false;
                            $scope.manageAppVersionConfigurations(response.data, true);
                        } else {
                            $modalInstance.close();
                        }
                    } else {
                        app.version = response.data.version;
                        app.usedVersionId = response.data.id;
                        $modalInstance.close(app);
                    }
                } else {
                    if (response.message === 'error.duplicate.file') {
                        response.message = 'error.version.exists';
                    }
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        const webAppValidator = function() {
            const iconRequired = $scope.application.showIcon;

            if (!$scope.application.name || $scope.application.name.trim().length === 0) {
                return 'error.empty.app.name';
            } else if (!$scope.application.url || $scope.application.url.trim().length === 0) {
                return 'error.empty.app.url';
            } else if (iconRequired && (!$scope.application.iconText || $scope.application.iconText.trim().length === 0)) {
                return 'error.empty.app.iconText';
            }

            return null;
        };

        const webAppPersistor = function () {
            var request = angular.copy($scope.application, {});
            if (request.iconId == -1) {
                delete request.iconId;
            }
            doSaveWebApplication(request);
        };

        const androidAppValidator = function () {
            if (!$scope.application.name || $scope.application.name.trim().length === 0) {
                return 'error.empty.app.name';
            } else if (!$scope.application.pkg && !$scope.fileSelected) {
                return 'error.empty.app.pkg';
            } else if (!$scope.application.version && !$scope.fileSelected) {
                return 'error.empty.app.version';
            }

            return null;
        };

        const androidAppPersistor = function () {
            var request = angular.copy($scope.application, {});
            if (request.iconId == -1) {
                delete request.iconId;
            }

            if ($scope.isNewApp) {
                if ($scope.fileSelected) {
                    request.filePath = $scope.file.path;
                }
            }

            applicationService.validateApplicationPackage(request, function (response) {
                if (response.status === 'OK') {
                    var existingAppsForPkg = response.data;
                    if (existingAppsForPkg.length > 0 && (!request.id || request.pkg !== $scope.application.pkg)) {
                        if (existingAppsForPkg.length != 1 ||
                            existingAppsForPkg[0].version > request.version ||
                            (existingAppsForPkg[0].version == request.version && !$scope.complete) ||       // Do not confirm if upload different architectures
                            existingAppsForPkg[0].name != request.name) {
                            // If the user change the name, he may decide to create a new app
                            // Also, let him choose the proper option if there are multiple apps with the same package ID
                            startDuplicatePkgResolutionDialog(request, existingAppsForPkg);
                        } else {
                            // Apparently a new version, no need to confirm
                            request.applicationId = existingAppsForPkg[0].id;
                            doSaveApplicationVersion(request, existingAppsForPkg[0]);
                        }
                        return;
                    }
                    doSaveAndroidApplication(request);
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.save = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;

            var validator;
            var persistor;
            if ($scope.application.type === 'app') {
                validator = androidAppValidator;
                persistor = androidAppPersistor;
            } else {
                validator = webAppValidator;
                persistor = webAppPersistor;
            }

            const err = validator();

            if (err) {
                $scope.errorMessage = localization.localize(err);
            } else {
                persistor();
            }
        };

        $scope.addNewIcon = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/addIcon.html',
                controller: 'AddIconController'
            });

            modalInstance.result.then(function (newIcon) {
                if (newIcon) {
                    $scope.application.iconId = newIcon.id;
                    loadIcons();
                }
            });
        };

        var startDuplicatePkgResolutionDialog = function (request, existingAppsForPkg) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/duplicatePkgResolution.html',
                controller: 'DuplicatePkgResolutionController',
                resolve: {
                    application: function () {
                        return request;
                    },
                    existingApps: function () {
                        return existingAppsForPkg;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                if (result.changePkg) {
                    doSaveAndroidApplication(request);
                } else if (result.newApp) {
                    doSaveAndroidApplication(request);
                } else if (result.newAppVersion) {
                    request.applicationId = result.targetAppId;
                    doSaveApplicationVersion(request, result.targetApp);
                }
            });
        };

        $scope.manageConfigurations = function (closeOnExit) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationConfigurations.html',
                controller: 'ApplicationConfigurationsModalController',
                resolve: {
                    application: function () {
                        return $scope.application;
                    }
                }
            });

            modalInstance.result.then(function () {
                if (closeOnExit) {
                    $scope.closeModal();
                }
            }, function () {
                if (closeOnExit) {
                    $scope.closeModal();
                }
            });
        };

        $scope.manageAppVersionConfigurations = function (applicationVersion, closeOnExit) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationVersionConfigurations.html',
                controller: 'ApplicationVersionConfigurationsModalController',
                resolve: {
                    applicationVersion: function () {
                        return applicationVersion;
                    }
                }
            });

            modalInstance.result.then(function () {
                if (closeOnExit) {
                    $scope.closeModal();
                }
            }, function () {
                if (closeOnExit) {
                    $scope.closeModal();
                }
            });
        };


        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    })
    .controller('ApplicationConfigurationsModalController',
        function ($scope, $modalInstance, applicationService, application, localization, confirmModal, configurationService,
                  alertService) {

            $scope.localizeRenewVersionTitle = function (appConfigurationLink) {
                let localizedText = localization.localize('configuration.app.version.upgrade.message')
                    .replace('${installedVersion}', appConfigurationLink.currentVersionText)
                    .replace('${latestVersion}', appConfigurationLink.latestVersionText);

                return localizedText;
            };

            $scope.upgradeApp = function (appConfigurationLink) {
                let localizedText = localization.localize('question.app.upgrade')
                    .replace('${v1}', appConfigurationLink.applicationName)
                    .replace('${v2}', appConfigurationLink.configurationName);
                confirmModal.getUserConfirmation(localizedText, function () {
                    configurationService.upgradeConfigurationApplication(
                        {configurationId: appConfigurationLink.configurationId, applicationId: appConfigurationLink.applicationId}, function (response) {
                            if (response.status === 'OK') {
                                loadData();
                            } else {
                                alertService.showAlertMessage(localization.localize(response.message));
                            }
                        }, alertService.onRequestFailure);
                });
            };


            $scope.actionChanged = function (configuration) {
                configuration.remove = (configuration.action == '2');
            };
            $scope.isInstallOptionAvailable = function (application) {
                return !application.system && (application.url || application.urlArm64 || application.urlArmeabi);
            };
            $scope.isRemoveOptionAvailable = function (application) {
                return !application.system;
            };

            $scope.application = {"id": application.id};
            for (var prop in application) {
                if (application.hasOwnProperty(prop)) {
                    $scope.application[prop] = application[prop];
                }
            }

            var loadData = function () {
                applicationService.getConfigurations({"id": application.id}, function (response) {
                    if (response.data) {
                        $scope.configurations = response.data;
                    }
                });
            };

            $scope.configurations = [];
            loadData();

            $scope.save = function () {
                $scope.errorMessage = '';

                var request = {"applicationId": application.id};

                var configurations = [];
                for (var i = 0; i < $scope.configurations.length; i++) {
                    // if ($scope.configurations[i].action != '0') {
                    //     configurations.push($scope.configurations[i]);
                    // }
                    configurations.push($scope.configurations[i]);
                }

                request.configurations = configurations;

                applicationService.updateApplicationConfigurations(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                });
            };

            $scope.closeModal = function () {
                $modalInstance.dismiss();
            }
        })
    .controller('ApplicationVersionEditor', function ($rootScope, $scope, $stateParams, applicationService,
                                                      localization, $window, confirmModal, $modal, authService,
                                                      alertService) {
        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };
        $scope.loading = false;
        $scope.authService = authService;

        $scope.init = function () {
            $rootScope.settingsTabActive = false;
            $rootScope.pluginsTabActive = false;
            $scope.paging.currentPage = 1;
            $scope.search();

            applicationService.getApplication({id: applicationId},
                function (response) {
                    $scope.application = response.data;
                });
        };

        $scope.search = function () {
            $scope.loading = true;
            applicationService.getApplicationVersions({id: applicationId},
                function (response) {
                    $scope.loading = false;
                    $scope.applications = response.data;
                }, function () {
                    $scope.loading = false;
                });
        };

        $scope.$watch('paging.currentPage', function () {
            $window.scrollTo(0, 0);
        });

        $scope.removeApplicationVersion = function (applicationVersion) {
            let localizedText = localization.localize('question.delete.application.version').replace('${applicationVersion}', applicationVersion.version);
            confirmModal.getUserConfirmation(localizedText, function () {
                applicationService.removeApplicationVersion({id: applicationVersion.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else if (response.status === 'ERROR') {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                });
            });
        };

        $scope.clarifyOnCommon = function () {
            alertService.showAlertMessage(localization.localize('common.app.clarification'));
        };

        $scope.addApplicationVersion = function (applicationVersion) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationVersionAdd.html',
                controller: 'ApplicationVersionModalController',
                resolve: {
                    applicationVersion: function () {
                        return applicationVersion;
                    },
                    isControlPanel: function () {
                        return false;
                    }
                }
            });

            modalInstance.result.then($scope.search, $scope.search);
        };

        $scope.editApplicationVersion = function (applicationVersion) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationVersionEdit.html',
                controller: 'ApplicationVersionModalController',
                resolve: {
                    applicationVersion: function () {
                        return applicationVersion;
                    },
                    isControlPanel: function () {
                        return false;
                    }
                }
            });

            modalInstance.result.then($scope.search, $scope.search);
        };

        $scope.manageConfigurations = function (applicationVersion) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationVersionConfigurations.html',
                controller: 'ApplicationVersionConfigurationsModalController',
                resolve: {
                    applicationVersion: function () {
                        return applicationVersion;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.init();
            });
        };

        $scope.applications = [];
        $scope.application = {};
        var applicationId = $stateParams.id;
        $scope.init();

    })
    .controller('ApplicationVersionModalController', function ($scope, $modalInstance, applicationService,
                                                               applicationVersion,
                                                               $modal, isControlPanel, localization) {
        $scope.isControlPanel = isControlPanel;

        $scope.isWebType = applicationVersion.type === 'web';

        $scope.isNewApp = applicationVersion.id === null || applicationVersion.id === undefined;

        if ($scope.isNewApp) {
            $scope.file = {};
            $scope.loading = false;
            $scope.fileName = null;
            $scope.invalidFile = false;
            $scope.fileSelected = false;
        }

        $scope.application = angular.copy(applicationVersion, {});

        $scope.onStartedUpload = function (files) {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
            $scope.invalidFile = false;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.fileName = files[0].name;
                if ($scope.fileName.endsWith(".apk") || $scope.fileName.endsWith(".xapk")) {
                    $scope.loading = true;
                    $scope.successMessage = localization.localize('success.uploading.file');
                } else {
                    $scope.errorMessage = localization.localize('error.apk.file.required');
                    $scope.invalidFile = true;
                }
            }
        };

        $scope.onUploadProgress = function(progress) {
            var loadedMb = (progress.loaded / 1048576).toFixed(1);
            var totalMb = (progress.total / 1048576).toFixed(1);
            $scope.successMessage = localization.localize('success.uploading.file') +
                " " + loadedMb + " / " + totalMb + " Mb";
        };

        $scope.fileUploaded = function (response) {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
            $scope.fileSelected = false;
            $scope.appdesc = {};

            $scope.loading = false;

            if (!$scope.invalidFile) {
                if (response.data.status === 'OK') {
                    $scope.file.path = response.data.data.serverPath;
                    if (response.data.data.application) {
                        var app = response.data.data.application;
                        $scope.application.name = app.name;
                        $scope.application.showIcon = app.showIcon;
                        $scope.application.useKiosk = app.useKiosk;
                        $scope.application.runAfterInstall = app.runAfterInstall;
                        $scope.application.runAtBoot = app.runAtBoot;
                        $scope.application.system = app.system;
                    }
                    if (response.data.data.fileDetails) {
                        var fileDetails = response.data.data.fileDetails;
                        $scope.application.pkg = fileDetails.pkg;
                        $scope.application.version = fileDetails.version;
                        $scope.application.arch = fileDetails.arch;
                        $scope.appdesc.version = fileDetails.version + " - " + localization.localize("form.application.from.file");

                        $scope.appTypeWarning = null;
                        $scope.appTypeSuccess = null;
                        $scope.complete = null;
                        if (response.data.data.exists) {
                            $scope.appTypeWarning = localization.localize('form.application.version.exists');
                        } else if (response.data.data.complete) {
                            $scope.appTypeSuccess = localization.localize('form.application.arch.success');
                            $scope.complete = true;
                        } else if (fileDetails.arch) {
                            $scope.appTypeWarning = localization.localize('form.application.arch.warning').replace('${arch}', fileDetails.arch);
                        }
                    }
                    $scope.successMessage = localization.localize('success.file.uploaded');
                    $scope.fileSelected = true;
                }
            } else {
                $scope.errorMessage = localization.localize('error.apk.file.required');
            }
        };

        $scope.clearFile = function () {
            $scope.file = {};
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
            $scope.fileSelected = false;
            $scope.invalidFile = false;
            $scope.loading = false;
        };

        $scope.save = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;

            if (!$scope.application.version && !$scope.fileSelected) {
                $scope.errorMessage = localization.localize('error.empty.app.version');
            } else {
                var request = {};
                for (var prop in $scope.application) {
                    if ($scope.application.hasOwnProperty(prop)) {
                        request[prop] = $scope.application[prop];
                    }
                }

                if ($scope.isNewApp) {
                    if ($scope.fileSelected) {
                        request.filePath = $scope.file.path;
                    }
                }

                applicationService.updateApplicationVersion(request, function (response) {
                    if (response.status === 'OK') {
                        if ($scope.isNewApp) {
                            $scope.application = response.data;
                            $scope.isNewApp = false;
                            $scope.file = {};
                            $scope.loading = false;
                            $scope.fileName = null;
                            $scope.invalidFile = false;
                            $scope.fileSelected = false;
                            $scope.manageConfigurations(true);
                        } else {
                            $modalInstance.close();
                        }
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                });
            }
        };

        $scope.manageConfigurations = function (closeOnExit) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationVersionConfigurations.html',
                controller: 'ApplicationVersionConfigurationsModalController',
                resolve: {
                    applicationVersion: function () {
                        return $scope.application;
                    }
                }
            });

            modalInstance.result.then(function () {
                if (closeOnExit) {
                    $scope.closeModal();
                }
            }, function () {
                if (closeOnExit) {
                    $scope.closeModal();
                }
            });
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    })
    .controller('ApplicationVersionConfigurationsModalController',
        function ($scope, $modalInstance, applicationService, applicationVersion, localization, confirmModal, configurationService,
                  alertService) {

            $scope.localizeRenewVersionTitle = function (appConfigurationLink) {
                let localizedText = localization.localize('configuration.app.version.upgrade.message')
                    .replace('${installedVersion}', appConfigurationLink.currentVersionText)
                    .replace('${latestVersion}', appConfigurationLink.latestVersionText);

                return localizedText;
            };

            $scope.actionChanged = function (configuration) {
                configuration.remove = (configuration.action == '2');
            };
            $scope.isInstallOptionAvailable = function (application) {
                return !application.system && (application.url || application.urlArm64 || application.urlArmeabi);
            };
            $scope.isRemoveOptionAvailable = function (application) {
                return !application.system;
            };

            $scope.applicationVersion = {"id": applicationVersion.id};
            for (var prop in applicationVersion) {
                if (applicationVersion.hasOwnProperty(prop)) {
                    $scope.applicationVersion[prop] = applicationVersion[prop];
                }
            }

            var loadData = function () {
                applicationService.getVersionConfigurations({"id": applicationVersion.id}, function (response) {
                    if (response.data) {
                        $scope.configurations = response.data;
                    }
                });

                applicationService.getApplication({id: applicationVersion.applicationId},
                    function (response) {
                        $scope.application = response.data;
                    });

            };

            $scope.save = function () {
                $scope.errorMessage = '';

                var request = {"applicationVersionId": applicationVersion.id};

                var configurations = [];
                for (var i = 0; i < $scope.configurations.length; i++) {
                    if ($scope.configurations[i].action != '0') {
                        configurations.push($scope.configurations[i]);
                    }
                }

                request.configurations = configurations;

                applicationService.updateApplicationVersionConfigurations(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, alertService.onRequestFailure);
            };

            $scope.closeModal = function () {
                $modalInstance.dismiss();
            };

            $scope.configurations = [];
            $scope.application = {};

            loadData();



        })
    .controller('DuplicatePkgResolutionController', function ($scope, $modalInstance, localization, application, existingApps) {

        $scope.isNewApp = (application.id === null || application.id === undefined);
        $scope.application = application;

        if ($scope.isNewApp) {

            $scope.duplicateApps = existingApps;

            $scope.textLine1 = localization.localize('form.resolved.duplicate.pkg.text1')
                .replace('${pkg}', application.pkg);

            $scope.formData = {
                targetAppId: existingApps[0].id
            };

            $scope.newApp = function () {
                $modalInstance.close({
                    newApp: true
                });
            };

            $scope.newAppVersion = function () {
                var selectedApp = existingApps.filter(function (app) {
                    return app.id === $scope.formData.targetAppId;
                })[0];

                $modalInstance.close({
                    newAppVersion: true,
                    targetAppId: $scope.formData.targetAppId,
                    targetApp: selectedApp
                });
            };
        } else {

            var appNames = existingApps.map(function (item, index) {
                return item.name;
            }).join(', ');

            $scope.textLine4 = localization.localize('form.resolved.duplicate.pkg.text4')
                .replace('${pkg}', application.pkg)
                .replace('${apps}', appNames);

            $scope.changePkg = function () {
                $modalInstance.close({
                    changePkg: true
                });
            };
        }

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };
    })
    .controller('AddIconController', function ($scope, $modalInstance, iconService, localization) {
        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.icon = {
            name: undefined,
            fileId: undefined
        };

        $scope.newIconFile = undefined;

        $scope.loading = false;

        $scope.save = function () {
            clearMessages();

            if (!$scope.icon.name || $scope.icon.name.trim().length === 0) {
                $scope.errorMessage = localization.localize('error.icon.empty.name');
            } else if (!$scope.icon.fileId) {
                $scope.errorMessage = localization.localize('error.icon.empty.file');
            } else {
                $scope.loading = true;

                const request = angular.copy($scope.icon, {});
                iconService.createIcon(request, function (response) {
                    $scope.loading = false;
                    if (response.status === 'OK') {
                        $modalInstance.close(response.data);
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, function () {
                    $scope.loading = false;
                    $scope.errorMessage = localization.localize('error.request.failure');
                });
            }
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

        $scope.onStartedUploadIcon = function (files) {
            clearMessages();

            if (files.length > 0) {
                $scope.loading = true;
                $scope.successMessage = localization.localize('success.uploading.file');
            }
        };

        $scope.fileUploadedIcon = function (response) {
            clearMessages();

            $scope.loading = false;

            if (response.data.status === 'OK') {
                $scope.newIconFile = response.data.data;
                $scope.icon.fileId = response.data.data.id;
                $scope.successMessage = localization.localize('success.file.uploaded');
            } else {
                $scope.errorMessage = localization.localize(response.data.message);
            }
        };

        $scope.clearFileIcon = function () {
            $scope.newIconFile = undefined;
            $scope.icon.fileId = undefined;

            $scope.loading = false;
            clearMessages();
        };
        
        const clearMessages = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
        };
    })
;

