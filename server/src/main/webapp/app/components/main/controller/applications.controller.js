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
            on: ($window.sessionStorage.getItem('HMDM_showMyAppsOnly') === 'true'),
            system: true,
        };
        let item = $window.sessionStorage.getItem('HMDM_showSystemApps');
        if (item !== null && item !== undefined) {
            $scope.showMyAppsOnly.system = (item === 'true');
        }

        $scope.myAppsButtonVisible = false;

        $scope.showMyAppsOnlyToggled = function () {
            $window.sessionStorage.setItem('HMDM_showMyAppsOnly', $scope.showMyAppsOnly.on);
            $scope.init();
        };

        $scope.showSystemAppsOnlyToggled = function () {
            $window.sessionStorage.setItem('HMDM_showSystemApps', $scope.showMyAppsOnly.system);
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

        $scope.editVersions = function (application) {
            $state.transitionTo('appVersionsEditor', {"id": application.id});
        };

        $scope.init();
    })
    .controller('ApplicationModalController', function ($scope, $modalInstance, applicationService, application,
                                                        $modal, isControlPanel, localization, closeOnSave) {
        $scope.application = {};
        $scope.isControlPanel = isControlPanel;

        $scope.isNewApp = application.id === null || application.id === undefined;

        if ($scope.isNewApp) {
            $scope.file = {};
            $scope.loading = false;
            $scope.fileName = null;
            $scope.invalidFile = false;
            $scope.fileSelected = false;
        }

        for (var prop in application) {
            if (application.hasOwnProperty(prop)) {
                $scope.application[prop] = application[prop];
            }
        }

        $scope.onStartedUpload = function (files) {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
            $scope.invalidFile = false;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.fileName = files[0].name;
                if ($scope.fileName.endsWith(".apk")) {
                    $scope.loading = true;
                    $scope.successMessage = localization.localize('success.uploading.file');
                } else {
                    $scope.errorMessage = localization.localize('error.apk.file.required');
                    $scope.invalidFile = true;
                }
            }
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
                        $scope.application.runAfterInstall = app.runAfterInstall;
                        $scope.application.system = app.system;
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

            if (!$scope.application.name) {
                $scope.errorMessage = localization.localize('error.empty.app.name');
            } else if (!$scope.application.pkg && !$scope.fileSelected) {
                $scope.errorMessage = localization.localize('error.empty.app.pkg');
            } else if (!$scope.application.version && !$scope.fileSelected) {
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
            }
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
            $scope.isPermitOptionAvailable = function (application) {
                return application.system || !application.url || application.url.length === 0;
            };
            $scope.isProhibitOptionAvailable = function (application) {
                return true;
            };
            $scope.isInstallOptionAvailable = function (application) {
                return !(application.system || !application.url || application.url.length === 0);
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

        $scope.editApplicationVersion = function (applicationVersion) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationVersion.html',
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
        $scope.application = {};
        $scope.isControlPanel = isControlPanel;

        $scope.isNewApp = applicationVersion.id === null || applicationVersion.id === undefined;

        if ($scope.isNewApp) {
            $scope.file = {};
            $scope.loading = false;
            $scope.fileName = null;
            $scope.invalidFile = false;
            $scope.fileSelected = false;
        }

        for (var prop in applicationVersion) {
            if (applicationVersion.hasOwnProperty(prop)) {
                $scope.application[prop] = applicationVersion[prop];
            }
        }

        $scope.onStartedUpload = function (files) {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
            $scope.invalidFile = false;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.fileName = files[0].name;
                if ($scope.fileName.endsWith(".apk")) {
                    $scope.loading = true;
                    $scope.successMessage = localization.localize('success.uploading.file');
                } else {
                    $scope.errorMessage = localization.localize('error.apk.file.required');
                    $scope.invalidFile = true;
                }
            }
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
                        $scope.application.runAfterInstall = app.runAfterInstall;
                        $scope.application.system = app.system;
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
            $scope.isPermitOptionAvailable = function (application) {
                return application.system || !application.url || application.url.length === 0;
            };
            $scope.isProhibitOptionAvailable = function (application) {
                return true;
            };
            $scope.isInstallOptionAvailable = function (application) {
                return !(application.system || !application.url || application.url.length === 0);
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
;

