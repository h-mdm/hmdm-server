// Localization completed
angular.module('headwind-kiosk')
    .controller('ApplicationsTabController', function ($scope, $rootScope, $modal, confirmModal, applicationService,
                                                       authService, $window, localization, alertService, $state,
                                                       fileService, storageService) {

        $scope.authService = authService;

        $scope.user = authService.getUser();

        $scope.search = {};
        $scope.loading = false;

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.availableSpace = null;
        var updateLimit = function() {
            fileService.getLimit(function(response) {
                if (response.status === 'OK' &&
                    response.data.sizeLimit > 0) {
                    var availableSpace = response.data.sizeLimit - response.data.sizeUsed;
                    if (availableSpace < 0) {
                        availableSpace = 0;
                    }
                    if (availableSpace < 20) {
                        $scope.availableSpace = localization.localize('form.file.available')
                            .replaceAll('${space}', availableSpace);
                    }
                }
            });
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
            updateLimit();
            $scope.loading = true;
            applicationService.getAllApplications({value: $scope.search.searchValue},
                function (response) {
                    $scope.loading = false;
                    $scope.applications = response.data.filter(function (app) {
                        return ($scope.showMyAppsOnly.on && (!app.common || app.customerId == $scope.user.customerId) ||
                            !$scope.showMyAppsOnly.on) &&
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
            return application.type === 'app';
        };

        $scope.editVersions = function (application) {
            $state.transitionTo('appVersionsEditor', {"id": application.id});
        };

        $scope.init();
    })
    .controller('ApplicationModalController', function ($scope, $modalInstance, applicationService, iconService,
                                                        application, $modal, $q, isControlPanel, localization, closeOnSave,
                                                        fileService) {
        $scope.isControlPanel = isControlPanel;

        $scope.localization = localization;

        $scope.isNewApp = application.id === null || application.id === undefined;

        $scope.application = angular.copy(application, {});
        if ($scope.application.iconId === null || $scope.application.iconId === undefined) {
            $scope.application.iconId = -1;
        }

        $scope.appdesc = {};

        fileService.getLimit(function(response) {
            if (response.status === 'OK' &&
                response.data.sizeLimit > 0) {
                var availableSpace = response.data.sizeLimit - response.data.sizeUsed;
                if (availableSpace < 0) {
                    availableSpace = 0;
                }
                if (availableSpace < 20) {
                    $scope.availableSpace = localization.localize('form.file.available')
                        .replaceAll('${space}', availableSpace);
                }
            }
        });

        $scope.icons = [{id: -1, name: localization.localize("form.application.icon.default")}];

        $scope.intentPlaceholder = localization.localize("form.application.intent.placeholder");
        $scope.intentOptions = [
            'android.intent.action.DIAL',
            'android.settings.ACCESSIBILITY_SETTINGS',
            'android.settings.AIRPLANE_MODE_SETTINGS',
            'android.settings.ALL_APPS_NOTIFICATION_SETTINGS',
            'android.settings.APN_SETTINGS',
            'android.settings.APPLICATION_DEVELOPMENT_SETTINGS',
            'android.settings.APPLICATION_SETTINGS',
            'android.settings.APP_SEARCH_SETTINGS',
            'android.settings.AUTO_ROTATE_SETTINGS',
            'android.settings.BATTERY_SAVER_SETTINGS',
            'android.settings.BIOMETRIC_ENROLL',
            'android.settings.BLUETOOTH_SETTINGS',
            'android.settings.CAPTIONING_SETTINGS',
            'android.settings.CAST_SETTINGS',
            'android.settings.ACTION_CONDITION_PROVIDER_SETTINGS',
            'android.settings.DATA_ROAMING_SETTINGS',
            'android.settings.DATA_USAGE_SETTINGS',
            'android.settings.DATE_SETTINGS',
            'android.settings.DEVICE_INFO_SETTINGS',
            'android.settings.DISPLAY_SETTINGS',
            'android.settings.DREAM_SETTINGS',
            'android.settings.FINGERPRINT_ENROLL',
            'android.settings.HARD_KEYBOARD_SETTINGS',
            'android.settings.HOME_SETTINGS',
            'android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS',
            'android.settings.INPUT_METHOD_SETTINGS',
            'android.settings.INPUT_METHOD_SUBTYPE_SETTINGS',
            'android.settings.INTERNAL_STORAGE_SETTINGS',
            'android.settings.LOCALE_SETTINGS',
            'android.settings.LOCATION_SOURCE_SETTINGS',
            'android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS',
            'android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION',
            'android.settings.MANAGE_ALL_SIM_PROFILES_SETTINGS',
            'android.settings.MANAGE_APPLICATIONS_SETTINGS',
            'android.settings.MANAGE_DEFAULT_APPS_SETTINGS',
            'android.settings.action.MANAGE_OVERLAY_PERMISSION',
            'android.settings.MANAGE_UNKNOWN_APP_SOURCES',
            'android.settings.action.MANAGE_WRITE_SETTINGS',
            'android.settings.MEMORY_CARD_SETTINGS',
            'android.settings.NETWORK_OPERATOR_SETTINGS',
            'android.settings.NFCSHARING_SETTINGS',
            'android.settings.NFC_PAYMENT_SETTINGS',
            'android.settings.NFC_SETTINGS',
            'android.settings.NIGHT_DISPLAY_SETTINGS',
            'android.settings.NOTIFICATION_ASSISTANT_SETTINGS',
            'android.settings.ACTION_NOTIFICATION_LISTENER_SETTING',
            'android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS',
            'android.settings.ACTION_PRINT_SETTINGS',
            'android.settings.PRIVACY_SETTINGS',
            'android.settings.QUICK_ACCESS_WALLET_SETTINGS',
            'android.settings.QUICK_LAUNCH_SETTINGS',
            'android.settings.REGIONAL_PREFERENCES_SETTINGS',
            'android.search.action.SEARCH_SETTINGS',
            'android.settings.SECURITY_SETTINGS',
            'android.settings.SETTINGS',
            'android.settings.SHOW_REGULATORY_INFO',
            'android.settings.SHOW_WORK_POLICY_INFO',
            'android.settings.SOUND_SETTINGS',
            'android.settings.STORAGE_VOLUME_ACCESS_SETTINGS',
            'android.settings.SYNC_SETTINGS',
            'android.settings.USAGE_ACCESS_SETTINGS',
            'android.settings.USER_DICTIONARY_SETTINGS',
            'android.settings.VOICE_INPUT_SETTINGS',
            'android.settings.VPN_SETTINGS',
            'android.settings.VR_LISTENER_SETTINGS',
            'android.settings.WEBVIEW_SETTINGS',
            'android.settings.WIFI_IP_SETTINGS',
            'android.settings.WIFI_SETTINGS',
            'android.settings.WIRELESS_SETTINGS',
            'android.settings.ZEN_MODE_PRIORITY_SETTINGS'
        ];

        $scope.filterIntents = function(userInput) {
            var filtered = $scope.intentOptions.filter(function(intent) {
                return intent
                    .toLowerCase()
                    .indexOf(userInput.toLowerCase()) !== -1;
            });
            // wrap in a resolved promise
            return $q.when(filtered);
        };

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
                        $scope.application.autoUpdateDisplayed = true;
                    }
                    if (response.data.data.fileDetails) {
                        var fileDetails = response.data.data.fileDetails;
                        $scope.application.pkg = fileDetails.pkg;
                        $scope.appdesc.pkg = fileDetails.pkg + " - " + localization.localize("form.application.from.file");
                        if (!$scope.application.name) {
                            $scope.application.name = fileDetails.name;
                        }
                        $scope.application.version = fileDetails.version;
                        $scope.application.versionCode = fileDetails.versionCode;
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
                    if (response.data.message == 'form.application.version.code.exists') {
                        $scope.errorMessage = localization.localize(response.data.message);
                    } else if (response.data.message == 'error.size.limit.exceeded') {
                        $scope.errorMessage = localization.localize(response.data.message) +
                            ' (' + response.data.data + ' Mb)';
                    } else {
                        $scope.errorMessage = localization.localize('error.apk.parse');
                    }
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

        const doSave = function (request, updateService) {
            updateService(request, function (response) {
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

        const doSaveWebApplication = function (request) {
            doSave(request,  applicationService.updateWebApplication);
        };

        var doSaveAndroidApplication = function (request) {
            doSave(request, applicationService.updateApplication);
        };

        var doSaveApplicationVersion = function (request, app) {
            applicationService.updateApplicationVersion(request, function (response) {
                if (response.status === 'OK') {
                    if (!closeOnSave) {
                        if ($scope.isNewApp) {
                            response.data.autoUpdate = $scope.application.autoUpdate;
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

        const intentAppValidator = function() {
            const iconRequired = $scope.application.showIcon;

            if (!$scope.application.name || $scope.application.name.trim().length === 0) {
                return 'error.empty.app.name';
            } else if (iconRequired && (!$scope.application.iconText || $scope.application.iconText.trim().length === 0)) {
                return 'error.empty.app.iconText';
            }

            return null;
        };

        const intentAppPersistor = webAppPersistor;

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
                    if (existingAppsForPkg.length > 0 && request.id) {
                        // If the user updates an existing application, check the name
                        for (var i in existingAppsForPkg) {
                            if (request.name == existingAppsForPkg[i].name) {
                                $scope.errorMessage = localization.localize("error.app.name.exists");
                                return;
                            }
                        }
                    }
                    if (existingAppsForPkg.length > 0 && (!request.id || request.pkg !== $scope.application.pkg)) {
                        if (existingAppsForPkg.length != 1 ||
                            existingAppsForPkg[0].versionCode > request.versionCode ||
                            (existingAppsForPkg[0].versionCode == request.versionCode && !$scope.complete) ||       // Do not confirm if upload different architectures
                            existingAppsForPkg[0].name != request.name) {
                            // If the user changes the name, he may decide to create a new app
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
            } else if ($scope.application.type === 'web') {
                validator = webAppValidator;
                persistor = webAppPersistor;
            } else {
                validator = intentAppValidator;
                persistor = intentAppPersistor;
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
                    var uniqueName = true;
                    for (var key in existingAppsForPkg) {
                        if (existingAppsForPkg[key].name == request.name) {
                            uniqueName = false;
                        }
                    }
                    if (uniqueName) {
                        doSaveAndroidApplication(request);
                    } else {
                        $scope.errorMessage = localization.localize("error.app.name.exists");
                    }
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

            // This method is never used!
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
                configuration.notify = true;
                $scope.actionGroup = -1;
            };
            $scope.isInstallOptionAvailable = function (application) {
                return !application.system && application.type === 'app' &&
                    (application.url || application.urlArm64 || application.urlArmeabi);
            };
            $scope.isRemoveOptionAvailable = function (application) {
                return !application.system && application.type === 'app';
            };

            $scope.application = {"id": application.id};
            for (var prop in application) {
                if (application.hasOwnProperty(prop)) {
                    $scope.application[prop] = application[prop];
                }
            }

            $scope.actionGroup = -1;
            $scope.toggleSelectAll = false;
            $scope.selectAllChanged = function() {
                for (var i in $scope.configurations) {
                    $scope.configurations[i].selected = $scope.toggleSelectAll;
                }
            };

            $scope.selectionChanged = function(configuration) {
                $scope.toggleSelectAll = false;
            };

            $scope.actionGroupChanged = function() {
                if ($scope.actionGroup == -1) {
                    return;
                }
                for (var i in $scope.configurations) {
                    if ($scope.configurations[i].selected && $scope.configurations[i].action != $scope.actionGroup) {
                        $scope.configurations[i].action = $scope.actionGroup;
                        $scope.configurations[i].notify = true;
                    }
                }
            };

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
        $scope.user = authService.getUser();
        $scope.hasPermission = authService.hasPermission;

        $scope.init = function () {
            $rootScope.settingsTabActive = false;
            $rootScope.pluginsTabActive = false;
            $scope.paging.currentPage = 1;
            $scope.search();

            applicationService.getApplication({id: applicationId},
                function (response) {
                    $scope.parentApp = response.data;
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

        $scope.appType = applicationVersion.type;

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
                    if (!response.data.data.application ||
                        response.data.data.application.pkg != response.data.data.fileDetails.pkg) {
                        $scope.errorMessage = localization.localize('error.package.not.match');
                        return;
                    }
                    if (response.data.data.fileDetails) {
                        var fileDetails = response.data.data.fileDetails;
                        $scope.application.pkg = fileDetails.pkg;
                        if (!$scope.application.name) {
                            $scope.application.name = fileDetails.name;
                        }
                        $scope.application.version = fileDetails.version;
                        $scope.application.versionCode = fileDetails.versionCode;
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
                } else {
                    if (response.data.message == 'form.application.version.code.exists') {
                        $scope.errorMessage = localization.localize(response.data.message);
                    } else {
                        $scope.errorMessage = localization.localize('error.apk.parse');
                    }
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
                            response.data.autoUpdate = $scope.application.autoUpdate;
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
                configuration.notify = true;
                $scope.actionGroup = -1;
            };
            $scope.isInstallOptionAvailable = function (application) {
                return !application.system && application.type === 'app' &&
                    (application.intent || application.url || application.urlArm64 || application.urlArmeabi);
            };
            $scope.isRemoveOptionAvailable = function (application) {
                return !application.system && application.type === 'app';
            };

            $scope.applicationVersion = {"id": applicationVersion.id};
            for (var prop in applicationVersion) {
                if (applicationVersion.hasOwnProperty(prop)) {
                    $scope.applicationVersion[prop] = applicationVersion[prop];
                }
            }

            $scope.actionGroup = -1;
            $scope.toggleSelectAll = false;
            $scope.selectAllChanged = function() {
                for (var i in $scope.configurations) {
                    $scope.configurations[i].selected = $scope.toggleSelectAll;
                }
            };

            $scope.selectionChanged = function(configuration) {
                $scope.toggleSelectAll = false;
            };

            $scope.actionGroupChanged = function() {
                if ($scope.actionGroup == -1) {
                    return;
                }
                for (var i in $scope.configurations) {
                    if ($scope.configurations[i].selected && $scope.configurations[i].action != $scope.actionGroup) {
                        $scope.configurations[i].action = $scope.actionGroup;
                        $scope.configurations[i].notify = true;
                    }
                }
            };

            var loadData = function () {
                applicationService.getVersionConfigurations({"id": applicationVersion.id}, function (response) {
                    if (response.data) {
                        $scope.configurations = response.data;
                        // For new version, this will always return "Do not install" for all configurations
                        // The autoUpdate flag updates the default actions
                        if (applicationVersion.autoUpdate) {
                            updateActions();
                        }
                    }
                });

                applicationService.getApplication({id: applicationVersion.applicationId},
                    function (response) {
                        $scope.application = response.data;
                    });

            };

            var updateActions = function() {
                applicationService.getConfigurations({"id": applicationVersion.applicationId}, function (response) {
                    if (response.data) {
                        response.data.forEach(function(item) {
                            var matchingConfig = $scope.configurations.find(obj => obj.configurationId == item.configurationId);
                            if (matchingConfig) {
                                matchingConfig.action = item.action;
                                if (item.action !== 0) {
                                    matchingConfig.notify = true;
                                }
                            }
                        });
                    }
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
            $scope.application = null;

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
    .controller('AddIconController', function ($scope, $modalInstance, iconService, fileService, localization) {
        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.icon = {
            name: undefined,
            fileId: undefined
        };

        $scope.files = [];
        fileService.getAllFiles({},
            function (response) {
                if (response.status === 'OK') {
                    // Exclude non-image files
                    response.data = response.data.filter(f => f.filePath != null &&
                        (f.filePath.endsWith(".jpg") || f.filePath.endsWith(".png") || f.filePath.endsWith(".jpeg")));

                    response.data.forEach(function (file) {
                        file.name = file.description ? file.description :
                            file.external ? file.url : file.filePath;
                        if (file.external) {
                            file.externalUrl = file.url;
                        }
                    });
                    $scope.files = response.data;
                    if ($scope.files.length > 0) {
                        $scope.file = $scope.files[0];
                    }
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });

        $scope.save = function () {
            clearMessages();

            $scope.icon.fileId = $scope.file.id;
            if (!$scope.icon.name || $scope.icon.name.trim().length === 0) {
                $scope.errorMessage = localization.localize('error.icon.empty.name');
            } else if (!$scope.icon.fileId) {
                $scope.errorMessage = localization.localize('error.icon.empty.file');
            } else {
                const request = angular.copy($scope.icon, {});
                iconService.createIcon(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close(response.data);
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, function () {
                    $scope.errorMessage = localization.localize('error.request.failure');
                });
            }
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        };

        const clearMessages = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
        };
    })
;

