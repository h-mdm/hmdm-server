// Localization completed
angular.module('headwind-kiosk')
    .controller('ConfigurationsTabController', function ($scope, $rootScope, $state, $modal, confirmModal,
                                                         configurationService, authService, $window, localization,
                                                         alertService, hintService, $timeout) {
        $scope.isTypical = false;

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.searchObj = {
            searchValue: null
        };

        $scope.$watch('paging.currentPage', function () {
            $window.scrollTo(0, 0);
        });

        $scope.hasPermission = authService.hasPermission;

        $scope.qrCodeAvailable = function (configuration) {
            return configuration.qrCodeKey && configuration.mainAppId > 0 && configuration.eventReceivingComponent &&
                configuration.eventReceivingComponent.length > 0;
        };

        $scope.showQrCode = function (configuration) {
            var url = configuration.baseUrl + "/#/qr/" + configuration.qrCodeKey + "/";
            $window.open(url, "_blank");
        };

        $scope.init = function (isTypical) {
            $rootScope.settingsTabActive = false;
            $rootScope.pluginsTabActive = false;
            $scope.paging.currentPage = 1;
            $scope.isTypical = isTypical;
            $scope.search(function () {
                $timeout(function () {
                    hintService.onStateChangeSuccess();
                }, 100);
            });
        };

        $scope.search = function (callback) {
            if ($scope.isTypical) {
                configurationService.getAllTypicalConfigurations(
                    {value: $scope.searchObj.searchValue},
                    function (response) {
                        $scope.configurations = response.data;
                        if (callback) {
                            callback();
                        }
                    });
            } else {
                configurationService.getAllConfigurations(
                    {value: $scope.searchObj.searchValue},
                    function (response) {
                        $scope.configurations = response.data;
                        if (callback) {
                            callback();
                        }
                    });
            }
        };

        $scope.addConfiguration = function() {
            confirmModal.getUserConfirmation(localization.localize('configuration.add.warning'), function () {
                $scope.editConfiguration({});
            });
        };

        $scope.editConfiguration = function (configuration) {

            // $state.goNewTab('configEditor', {"id": configuration.id, "typical": $scope.isTypical});
            $state.transitionTo('configEditor', {"id": configuration.id, "typical": $scope.isTypical});

        };

        $scope.copyConfiguration = function (configuration) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/copyConfiguration.html',
                controller: 'CopyConfigurationModalController',
                resolve: {
                    configuration: function () {
                        return configuration;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.removeConfiguration = function (configuration) {
            let localizedText = $scope.configurations.length > 1 ?
                localization.localize('question.delete.configuration').replace('${configurationName}', configuration.name) :
                localization.localize('configuration.remove.warning');
            confirmModal.getUserConfirmation(localizedText, function () {
                configurationService.removeConfiguration({id: configuration.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                }, alertService.onRequestFailure);
            });
        };

        $scope.init(false);
    })
    .controller('CopyConfigurationModalController',
        function ($scope, $modalInstance, configurationService, configuration, localization) {

            $scope.configuration = {"id": configuration.id, "name": ""};

            $scope.save = function () {
                $scope.saveInternal();
            };

            $scope.saveInternal = function () {
                $scope.errorMessage = '';

                if (!$scope.configuration.name) {
                    $scope.errorMessage = localization.localize('error.empty.configuration.name');
                } else {
                    var request = {"id": $scope.configuration.id, "name": $scope.configuration.name};
                    configurationService.copyConfiguration(request, function (response) {
                        if (response.status === 'OK') {
                            $modalInstance.close();
                        } else {
                            $scope.errorMessage = localization.localize('error.duplicate.configuration.name');
                        }
                    });
                }
            };

            $scope.closeModal = function () {
                $modalInstance.dismiss();
            }
        })
    .controller('ApplicationSettingEditorController', function ($scope, $modalInstance, localization,
                                                                applicationSetting, getApps) {
        var copy = {};
        for (var p in applicationSetting) {
            if (applicationSetting.hasOwnProperty(p)) {
                copy[p] = applicationSetting[p];
            }
        }

        $scope.applicationSetting = copy;
        $scope.mainApp = null;
        $scope.errorMessage = undefined;

        if (applicationSetting.id || applicationSetting.tempId) {
            $scope.mainApp = {
                id: applicationSetting.applicationId,
                name: applicationSetting.applicationName,
                pkg: applicationSetting.applicationPkg
            };
        }

        $scope.appLookupFormatter = function (val) {
            if (val) {
                return val.pkg;
            } else {
                return null;
            }
        };

        $scope.onMainAppSelected = function ($item) {
            $scope.mainApp = $item;
        };

        $scope.getApps = getApps;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            $scope.errorMessage = undefined;

            if (!$scope.applicationSetting.name) {
                $scope.errorMessage = localization.localize('error.application.setting.empty.name');
                // } else if (!$scope.applicationSetting.value) {
                //     $scope.errorMessage = localization.localize('error.application.setting.empty.value');
            } else if (!$scope.mainApp || !$scope.mainApp.id) {
                $scope.errorMessage = localization.localize('error.application.setting.empty.app');
            } else {

                $scope.applicationSetting.applicationPkg = $scope.mainApp.pkg;
                $scope.applicationSetting.applicationName = $scope.mainApp.name;
                $scope.applicationSetting.applicationId = $scope.mainApp.id;
                $scope.applicationSetting.lastUpdate = new Date().getTime();

                $modalInstance.close($scope.applicationSetting);
            }
        };
    })
    .controller('AddConfigurationAppModalController', function ($scope, localization, configurationService,
                                                                applications, configuration, $modalInstance, $modal) {

        // TODO : ISV : Update this controller
        // $scope.mainAppSelected = false;
        $scope.mainApp = {id: -1, name: ""};

        $scope.appLookupFormatter = function (val) {
            return val.name + (val.version && val.version !== '0' ? " " + val.version : "");
        };

        // $scope.trackMainApp = function (val) {
        //     $scope.mainAppSelected = val;
        // };

        $scope.onMainAppSelected = function ($item) {
            $scope.mainApp = $item;
            $scope.mainApp.action = 1;
        };

        $scope.getApps = function (filter) {
            var lower = filter.toLowerCase();

            var apps = $scope.availableApplications.filter(function (app) {
                // Intentionally using app.action == 1 but not app.action === 1
                return (app.name.toLowerCase().indexOf(lower) > -1
                    || app.pkg && app.pkg.toLowerCase().indexOf(lower) > -1
                    || app.version && app.version.toLowerCase().indexOf(lower) > -1);
            });

            apps.sort(function (a, b) {
                let n1 = a.name.toLowerCase();
                let n2 = b.name.toLowerCase();

                if (n1 === n2) {
                    return 0;
                } else if (n1 < n2) {
                    return -1;
                } else {
                    return 1;
                }
            });

            return apps;
        };

        $scope.availableApplications = applications.filter(function (app) {
            return app.action == '0' && !app.actionChanged;
        });

        $scope.showIconSelectOptions = [
            {id: true, label: localization.localize('form.configuration.apps.label.show')},
            {id: false, label: localization.localize('form.configuration.apps.label.not.show')},
        ];

        $scope.isInstallOptionAvailable = function (application) {
            return !application.system && (application.url || application.urlArm64 || application.urlArmeabi);
        };
        $scope.isRemoveOptionAvailable = function (application) {
            return !application.system;
        };
        $scope.actionChanged = function (application) {
            application.remove = (application.action == '2');
        };

        $scope.configuration = configuration;

        $scope.save = function () {
            $modalInstance.close($scope.mainApp);
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.newApp = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/application.html',
                controller: 'ApplicationModalController',
                resolve: {
                    application: function () {
                        return {};
                    },
                    isControlPanel: function () {
                        return false;
                    },
                    closeOnSave: function () {
                        return true;
                    }
                }
            });

            modalInstance.result.then(function (addedApp) {
                addedApp.isNew = true;
                $scope.mainApp = addedApp;
                $scope.mainApp.action = 1;
            });
        };
    })
    .controller('ConfigurationEditorController',
        function ($scope, configurationService, settingsService, $stateParams, $state, $rootScope, $window, $timeout,
                  localization, confirmModal, alertService, $modal, appVersionComparisonService) {

            $scope.successMessage = null;

            let sortItem = $window.localStorage.getItem('HMDM_configAppsSortBy');
            $scope.sort = {
                by: ((sortItem !== null && sortItem !== undefined) ? sortItem : 'name')
            };

            $scope.pkgInfoVisible = function (application) {
                return application.type !== 'web';
            };

            $scope.saveButtonClass = function () {
                return $scope.configurationForm.$dirty ? 'btn-attention' : '';
            }

            $scope.uploadBackground = function () {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/file.html',
                    // Defined in files.controller.js
                    controller: 'FileModalController'
                });

                modalInstance.result.then(function (data) {
                    if (data) {
                        $scope.configuration.backgroundImageUrl = data.url;
                    }
                });
            };

            $scope.localizeRenewVersionTitle = function (application) {
                let localizedText = localization.localize('configuration.app.version.upgrade.message')
                    .replace('${installedVersion}', application.version)
                    .replace('${latestVersion}', application.latestVersionText);

                return localizedText;
            };

            $scope.filterApps = function (item) {
                var filter = ($scope.paging.filterText || '').toLowerCase();

                return (item.name && item.name.toLowerCase().indexOf(filter) >= 0)
                    || (item.type === 'app' && item.pkg && item.pkg.toLowerCase().indexOf(filter) >= 0);
            };

            $scope.splitApkWarning = function(application) {
                if (application.type != 'app') {
                    return null;
                }
                if (application.split && application.urlArmeabi && !application.urlArm64) {
                    return localization.localize('form.application.arch.warning').replace('${arch}', "armeabi-v7a");
                }
                if (application.split && !application.urlArmeabi && application.urlArm64) {
                    return localization.localize('form.application.arch.warning').replace('${arch}', "arm64-v8a");
                }
                return null;
            };

            $scope.checkNetworkState = function() {
                if ($scope.configuration.wifi === false && $scope.configuration.mobileData === false) {
                    alertService.showAlertMessage(localization.localize('form.configuration.settings.common.no.network.warning'));
                }
            };

            $scope.addApp = function () {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/addConfigurationApplication.html',
                    controller: 'AddConfigurationAppModalController',
                    resolve: {
                        applications: function () {
                            return allApplications;
                        },
                        configuration: function () {
                            return $scope.configuration;
                        }
                    }
                });

                // TODO : ISV : need to re-caclculate mainappid, contentappid if necessary
                modalInstance.result.then(function (addedApp) {
                    if (addedApp) {
                        addedApp.actionChanged = true;
                        var addedAppIsToBeUsed = addedApp.action == 1;
                        if (addedAppIsToBeUsed) {
                            $scope.applications.filter(function (app) {
                                return app.pkg === addedApp.pkg && (app.action == 1);
                            }).forEach(function (app) {
                                app.action = 0;
                            });
                        }
                        if (addedApp.isNew) {
                            allApplications.push(addedApp);
                        }
                        $scope.applications.push(addedApp);
                        if (!addedApp.usedVersionId) {
                            addedApp.usedVersionId = addedApp.latestVersion;
                        }

                        if (addedAppIsToBeUsed) {
                            syncMainApp();
                            syncContentApp();
                        }
                    }
                });
            };

            $scope.actionChanged = function (updatedApp) {
                updatedApp.actionChanged = true;
                if (updatedApp.action == 1) {
                    $scope.applications.filter(function (app) {
                        return updatedApp != app && app.pkg === updatedApp.pkg && (app.action == 1);
                    }).forEach(function (app) {
                        app.action = 0;
                    });
                }
            };

            $scope.pushOptionsChanged = function() {
                updateMqttHint();
            };

            var updateMqttHint = function() {
                if ($scope.configuration.pushOptions === 'mqttWorker') {
                    $scope.pushHint = localization.localize('form.configuration.settings.push.options.mqtt.worker.hint');
                } else if ($scope.configuration.pushOptions === 'mqttAlarm') {
                    $scope.pushHint = localization.localize('form.configuration.settings.push.options.mqtt.alarm.hint');
                } else if ($scope.configuration.pushOptions === 'polling') {
                    $scope.pushHint = localization.localize('form.configuration.settings.push.options.polling.hint');
                } else {
                    $scope.pushHint = '';
                }
            };

            $scope.upgrading = false;
            $scope.upgradeApp = function (application) {
                if ($scope.upgrading) {
                    // Prevent multiple clicks, because this somehow clears the app being upgraded
                    return;
                }
                let localizedText = localization.localize('question.app.upgrade')
                    .replace('${v1}', application.name)
                    .replace('${v2}', $scope.configuration.name);
                confirmModal.getUserConfirmation(localizedText, function () {
                    $scope.upgrading = true;
                    configurationService.upgradeConfigurationApplication(
                        {configurationId: $scope.configuration.id, applicationId: application.id}, function (response) {
                            $scope.upgrading = false;
                            if (response.status === 'OK') {
                                $scope.configuration.mainAppId = response.data.mainAppId;
                                $scope.configuration.contentAppId = response.data.contentAppId;

                                $scope.loadApps($scope.configuration.id);
                            } else {
                                alertService.showAlertMessage(localization.localize(response.message));
                            }
                        }, function (response) {
                            $scope.upgrading = false;
                            console.error("Error when sending request to server", response);
                            showAlert(localization.localize('error.request.failure'));
                        });
                });
            };

            $scope.showIconSelectOptions = [
                {id: true, label: localization.localize('form.configuration.apps.label.show')},
                {id: false, label: localization.localize('form.configuration.apps.label.not.show')},
            ];

            $scope.isInstallOptionAvailable = function (application) {
                return !application.system && (application.url || application.urlArm64 || application.urlArmeabi);
            };
            $scope.isRemoveOptionAvailable = function (application) {
                return !application.system;
            };

            $scope.$on('$stateChangeStart',
                function (event, toState, toParams, fromState, fromParams) {
                    if ($scope.configurationForm.$dirty) {
                        if (!$scope.saved) {
                            var confirmed = confirm(localization.localize('question.exit.without.saving'));
                            if (!confirmed) {
                                event.preventDefault();
                            }
                        }
                    }
                });

            $scope.sortByChanged = function () {
                $window.localStorage.setItem('HMDM_configAppsSortBy', $scope.sort.by);
                $scope.paging.currentPage = 1;
            };

            $scope.getApps = function (filter) {
                var lower = filter.toLowerCase();
                var apps = allApplications.filter(function (app) {
                    // Intentionally using app.action == 1 but not app.action === 1
                    return (app.action == 1) && (app.name.toLowerCase().indexOf(lower) > -1
                        || app.pkg && app.pkg.toLowerCase().indexOf(lower) > -1
                        || app.version && app.version.toLowerCase().indexOf(lower) > -1);
                });

                apps.sort(function (a, b) {
                    let n1 = a.name.toLowerCase();
                    let n2 = b.name.toLowerCase();

                    if (n1 === n2) {
                        return 0;
                    } else if (n1 < n2) {
                        return -1;
                    } else {
                        return 1;
                    }
                });

                return apps;
            };

            $scope.onMainAppSelected = function ($item) {
                $scope.mainApp = $item;
            };
            $scope.onContentAppSelected = function ($item) {
                $scope.contentApp = $item;
            };
            $scope.trackMainApp = function (val) {
                mainAppSelected = val;
            };
            $scope.trackContentApp = function (val) {
                contentAppSelected = val;
            };

            $scope.appLookupFormatter = function (val) {
                return val.name + (val.version && val.version !== '0' ? " " + val.version : "");
            };

            var getAppSettingsApps = function (filter) {
                var lower = filter.toLowerCase();
                var apps = allApplications.filter(function (app) {
                    // Intentionally using app.action == 1 but not app.action === 1
                    return (app.name.toLowerCase().indexOf(lower) > -1
                        || app.pkg && app.pkg.toLowerCase().indexOf(lower) > -1
                        || app.version && app.version.toLowerCase().indexOf(lower) > -1);
                });

                apps.sort(function (a, b) {
                    let n1 = a.name.toLowerCase();
                    let n2 = b.name.toLowerCase();

                    if (n1 === n2) {
                        return 0;
                    } else if (n1 < n2) {
                        return -1;
                    } else {
                        return 1;
                    }
                });

                return apps;
            };

            $scope.getAppSettingsApps = getAppSettingsApps;

            $scope.onAppSettingsFilterAppSelected = function ($item) {
                $scope.settingsPaging.appSettingsFilterApp = $item;
                $scope.settingsPaging.appSettingsAppFilterText = $item.pkg;
                filterApplicationSettings();
            };

            $scope.appSettingsAppLookupFormatter = function (val) {
                if (val) {
                    return val.pkg;
                } else {
                    return null;
                }
            };

            $scope.appSettingsFilterChanged = function () {
                filterApplicationSettings();
            };

            $scope.filesFilterChanged = function () {
                filterFiles();
            };

            $scope.systemAppsToggled = function () {
                $scope.showSystemApps = !$scope.showSystemApps;
                $window.localStorage.setItem('HMDM_configShowSystemApps', $scope.showSystemApps);
                $scope.applications = allApplications.filter(function (app) {
                    return (app.actionChanged || app.action != '0') && (!app.system || $scope.showSystemApps);
                });
            };

            $scope.loadApps = function (configId) {
                configurationService.getApplications({"id": configId}, function (response) {
                    if (response.status === 'OK') {
                        response.data.forEach(function (app) {
                            app.actionChanged = false;
                        });

                        allApplications = response.data.map(function (app) {
                            return app;
                        });
                        $scope.applications = response.data.filter(function (app) {
                            // Application com.hmdm.launcher is made available by default when creating new configuration
                            return app.action != '0' && (!app.system || $scope.showSystemApps) || (!configId && app.pkg === 'com.hmdm.launcher' && app.action != '2');
                        });

                        // For new configuration use default app for main app and content receiver
                        if (!configId) {
                            let mainAppCandidates = response.data.filter(function (app) {
                                return app.pkg === 'com.hmdm.launcher' && app.action != '2';
                            });

                            if (mainAppCandidates.length > 0) {
                                $scope.configuration.mainAppId = mainAppCandidates[0].usedVersionId;
                                mainAppCandidates[0].action = 1; // Install
                            }
                        }

                        if ($scope.configuration.mainAppId) {
                            let mainApps = response.data.filter(function (app) {
                                return app.usedVersionId === $scope.configuration.mainAppId;
                            });

                            if (mainApps.length > 0) {
                                $scope.mainApp = mainApps[0];
                                mainAppSelected = true;
                            }
                        }

                        if ($scope.configuration.contentAppId) {
                            let contentApps = response.data.filter(function (app) {
                                return app.usedVersionId === $scope.configuration.contentAppId;
                            });

                            if (contentApps.length > 0) {
                                $scope.contentApp = contentApps[0];
                                contentAppSelected = true;
                            }
                        }
                    } else {
                        $scope.errorMessage = localization.localize(response.message);
                    }
                });
            };

            $scope.save = function (doClose) {
                $scope.errorMessage = '';
                $scope.saved = false;

                if (!$scope.configuration.pushOptions) {
                    $scope.errorMessage = localization.localize('error.empty.push.options');
                } else if (!$scope.configuration.name) {
                    $scope.errorMessage = localization.localize('error.empty.configuration.name');
                } else if (!$scope.configuration.password) {
                    $scope.errorMessage = localization.localize('error.empty.configuration.password');
                } else if ($scope.configuration.kioskMode && (!contentAppSelected)) {
                    $scope.errorMessage = localization.localize('error.empty.configuration.contentApp');
                } else {
                    var request = {};

                    for (var prop in $scope.configuration) {
                        if ($scope.configuration.hasOwnProperty(prop)) {
                            request[prop] = $scope.configuration[prop];
                        }
                    }

                    if (mainAppSelected) {
                        var apps = allApplications.filter(function (app) {
                            // Intentionally using app.action == 1 but not app.action === 1
                            return (app.action == 1) && (app.usedVersionId === $scope.mainApp.usedVersionId);
                        });

                        if (apps.length === 0) {
                            $scope.errorMessage = localization.localize('error.invalid.configuration.mainApp');
                            return;
                        }

                        request["mainAppId"] = $scope.mainApp.usedVersionId;
                    } else {
                        request["mainAppId"] = null;
                    }

                    if (contentAppSelected) {
                        var apps = allApplications.filter(function (app) {
                            // Intentionally using app.action == 1 but not app.action === 1
                            return (app.action == 1) && (app.usedVersionId === $scope.contentApp.usedVersionId);
                        });

                        if (apps.length === 0) {
                            $scope.errorMessage = localization.localize('error.invalid.configuration.contentApp');
                            return;
                        }

                        request["contentAppId"] = $scope.contentApp.usedVersionId;
                    } else {
                        request["contentAppId"] = null;
                    }

                    var applications = allApplications.filter(function (app) {
                        // Intentionally using app.action != 0 but not app.action !== 0
                        return app.action != 0;
                    });

                    request.applications = applications;
                    request.type = $scope.isTypical ? 1 : 0;

                    if ($scope.configuration.systemUpdateType === 2) {
                        request.systemUpdateFrom = pad($scope.dates.systemUpdateFrom.getHours(), 2) + ':' + pad($scope.dates.systemUpdateFrom.getMinutes(), 2);
                        request.systemUpdateTo = pad($scope.dates.systemUpdateTo.getHours(), 2) + ':' + pad($scope.dates.systemUpdateTo.getMinutes(), 2);
                    }

                    if ($scope.configuration.passwordMode == 'any') {
                        request.passwordMode = null;
                    }

                    if ($scope.configuration.timeZoneMode == 'default') {
                        request.timeZone = null;
                    } else if ($scope.configuration.timeZoneMode == 'auto') {
                        request.timeZone = 'auto';
                    }

                    if ($scope.configuration.allowedClasses == '') {
                        request.allowedClasses = null;
                    }

                    if ($scope.configuration.newServerUrl == '') {
                        request.newServerUrl = null;
                    }

                    if ($scope.configuration.orientation == 0) {
                        request.orientation = null;
                    }

                    configurationService.updateConfiguration(request, function (response) {
                        if (response.status === 'OK') {
                            $scope.saved = true;
                            if (doClose) {
                                $rootScope["configurationsMessage"] = localization.localize('success.configuration.saved');
                                $scope.close();
                            } else {
                                $scope.successMessage = localization.localize('success.configuration.saved');
                                $scope.configuration = response.data;

                                if ($scope.configuration.timeZone === null) {
                                    $scope.configuration.timeZoneMode = 'default';
                                } else if ($scope.configuration.timeZone === 'auto') {
                                    $scope.configuration.timeZoneMode = 'auto';
                                } else {
                                    $scope.configuration.timeZoneMode = 'manual';
                                }

                                $scope.loadApps($scope.configuration.id);
                                $scope.configurationForm.$dirty = false;
                                let $timeout1 = $timeout(function () {
                                    $scope.successMessage = null;
                                }, 5000);
                                $scope.$on('$destroy', function () {
                                    $timeout.cancel($timeout1);
                                });

                                filterApplicationSettings();
                            }
                        } else {
                            $scope.errorMessage = localization.localize(response.message);
                        }
                    }, function () {
                        $scope.errorMessage = localization.localize('error.request.failure');
                    });
                }
            };

            $scope.close = function () {
                $state.transitionTo('configurations');
                // $window.close();
            };

            $scope.addFile = function () {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/configurationFile.html',
                    controller: 'FileEditorController',
                    resolve: {
                        file: function () {
                            return {remove: false};
                        },
                        defaultFilePath: function() {
                            return $scope.configuration.defaultFilePath;
                        }
                    }
                });

                modalInstance.result.then(function (file) {
                    $scope.configurationForm.$dirty = true;
                    if (!file.id) {
                        file.tempId = new Date().getTime();
                        $scope.configuration.files.push(file);
                        filterFiles();
                    }
                });
            };

            $scope.addApplicationSetting = function () {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/applicationSetting.html',
                    controller: 'ApplicationSettingEditorController',
                    resolve: {
                        applicationSetting: function () {
                            return {type: "STRING"};
                        },
                        getApps: function () {
                            return getAppSettingsApps;
                        }
                    }
                });

                modalInstance.result.then(function (applicationSetting) {
                    if (!applicationSetting.id) {
                        applicationSetting.tempId = new Date().getTime();
                        $scope.configuration.applicationSettings.push(applicationSetting);
                        filterApplicationSettings();
                    }
                });
            };

            var mergeApplicationUsageParameters = function (newApplicationUsageParameters) {
                var appParametersIndex = $scope.configuration.applicationUsageParameters.findIndex(function (item) {
                    return item.applicationId === newApplicationUsageParameters.applicationId;
                });

                if (appParametersIndex < 0) {
                    $scope.configuration.applicationUsageParameters.push(newApplicationUsageParameters);
                } else {
                    $scope.configuration.applicationUsageParameters[appParametersIndex] = newApplicationUsageParameters;
                }
            };

            $scope.selectVersion = function (application) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/configurationAppVersionSelection.html',
                    controller: 'ConfigurationAppVersionSelectController',
                    resolve: {
                        application: function () {
                            return application;
                        },
                        applicationParameters: function () {
                            return $scope.configuration.applicationUsageParameters.find(function (item) {
                                return item.applicationId === application.id;
                            });
                        },
                    }
                });

                modalInstance.result.then(function (data) {
                    var selectedAppVersion = data.selectedVersion;
                    var applicationVersions = data.availableVersions;



                    var newAppVersion = applicationVersions.filter(function (item) {
                        return item.id === selectedAppVersion.applicationVersionId;
                    })[0];

                    var currentAppVersion = applicationVersions.filter(function (item) {
                        return item.id === application.usedVersionId;
                    })[0];

                    // Compare new version and existing one
                    var comparisonResult = appVersionComparisonService.compare(newAppVersion.version, currentAppVersion.version);

                    if (comparisonResult > 0) { // Upgrade
                        let localizedText = localization.localize('form.configuration.app.version.select.upgrade.warning')
                            .replace('${v1}', application.name)
                            .replace('${v3}', newAppVersion.version)
                            .replace('${v2}', $scope.configuration.name);
                        
                        confirmModal.getUserConfirmation(localizedText, function () {
                            mergeApplicationUsageParameters(data.applicationParameters);

                            allApplications.filter(function (app) {
                                return app.id === newAppVersion.applicationId && (app.action == 1);
                            }).forEach(function (app) {
                                app.usedVersionId = newAppVersion.id;
                                app.version = newAppVersion.version;
                                app.url = newAppVersion.url;
                                app.outdated = newAppVersion.id !== app.latestVersion;
                            });

                            allApplications = allApplications.filter(function (app) {
                                return app.id !== newAppVersion.applicationId  || app.action == 1 || app.usedVersionId !== newAppVersion.id;
                            });

                            allApplications.sort(function (a, b) {
                                return appVersionComparisonService.compare(a.version, b.version)
                            });

                            $scope.applications = allApplications.filter(function (app) {
                                return (app.actionChanged || app.action != '0') && (!app.system || $scope.showSystemApps);
                            });

                            syncMainApp();
                            syncContentApp();
                        });
                    } else if (comparisonResult < 0) { // Downgrade
                        let localizedText = localization.localize('form.configuration.app.version.select.downgrade.warning')
                            .replace('${v1}', application.name)
                            .replace('${v2}', newAppVersion.version);
                        
                        confirmModal.getUserConfirmation(localizedText, function () {
                            mergeApplicationUsageParameters(data.applicationParameters);
                            applicationVersions.forEach(function (availableAppVersion) {
                                var result1 = appVersionComparisonService.compare(
                                    newAppVersion.version, availableAppVersion.version
                                );
                                if (result1 < 0) {
                                    var result2 = appVersionComparisonService.compare(
                                        availableAppVersion.version, currentAppVersion.version
                                    );
                                    if (result2 <= 0) {
                                        var alreadyListed = false;
                                        allApplications.filter(function (app) {
                                            return app.id === newAppVersion.applicationId && (app.usedVersionId === availableAppVersion.id);
                                        }).forEach(function (app) {
                                            alreadyListed = true;
                                            app.action = 2;
                                        });

                                        if (!alreadyListed) {
                                            var copy = {};
                                            for (var p in application) {
                                                if (application.hasOwnProperty(p)) {
                                                    copy[p] = application[p];
                                                }
                                            }

                                            copy.version = availableAppVersion.version;
                                            copy.usedVersionId = availableAppVersion.id;
                                            copy.action = 2;
                                            delete copy.$$hashKey;

                                            allApplications.push(copy);
                                        }
                                    }
                                }
                            });

                            var copy = {};
                            for (var p in application) {
                                if (application.hasOwnProperty(p)) {
                                    copy[p] = application[p];
                                }
                            }

                            copy.version = newAppVersion.version;
                            copy.usedVersionId = newAppVersion.id;
                            copy.action = 1;
                            delete copy.$$hashKey;

                            allApplications.push(copy);

                            allApplications = allApplications.filter(function (app) {
                                return app.id !== newAppVersion.applicationId  || app.action == 1 || app.usedVersionId !== newAppVersion.id;
                            });

                            allApplications.sort(function (a, b) {
                                return appVersionComparisonService.compare(a.version, b.version)
                            });

                            $scope.applications = allApplications.filter(function (app) {
                                return (app.actionChanged || app.action != '0') && (!app.system || $scope.showSystemApps);
                            });

                            syncMainApp();
                            syncContentApp();

                        });
                    } else {
                        mergeApplicationUsageParameters(data.applicationParameters);
                    }
                });
            };

            $scope.editDetails = function (application) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/configurationAppDetails.html',
                    controller: 'ConfigurationAppDetailsController',
                    resolve: {
                        application: function () {
                            return application;
                        }
                    }
                });
            };

            var syncMainApp = function () {
                if ($scope.configuration.mainAppId) {
                    let mainAppInstalledVersion = $scope.applications.find(function (app) {
                        // return app.id === $scope.mainApp.id && app.action == 1;
                        return app.pkg === $scope.mainApp.pkg && app.action == 1;
                    });

                    if (mainAppInstalledVersion) {
                        var copy = {};
                        for (var p in mainAppInstalledVersion) {
                            if (mainAppInstalledVersion.hasOwnProperty(p)) {
                                copy[p] = mainAppInstalledVersion[p];
                            }
                        }
                        $scope.mainApp = copy;
                        mainAppSelected = true;
                    }
                }
            };

            var syncContentApp = function () {
                if ($scope.configuration.contentAppId) {
                    let contentAppInstalledVersion = $scope.applications.find(function (app) {
                        // return app.id === $scope.contentApp.id && app.action == 1;
                        return app.pkg === $scope.contentApp.pkg && app.action == 1;
                    });

                    if (contentAppInstalledVersion) {
                        var copy = {};
                        for (var p in contentAppInstalledVersion) {
                            if (contentAppInstalledVersion.hasOwnProperty(p)) {
                                copy[p] = contentAppInstalledVersion[p];
                            }
                        }
                        $scope.contentApp = copy;
                        contentAppSelected = true;
                    }
                }
            };

            $scope.editApplicationSetting = function (setting) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/applicationSetting.html',
                    controller: 'ApplicationSettingEditorController',
                    resolve: {
                        applicationSetting: function () {
                            return setting;
                        },
                        getApps: function () {
                            return getAppSettingsApps;
                        }
                    }
                });

                modalInstance.result.then(function (applicationSetting) {
                    var index = $scope.configuration.applicationSettings.findIndex(function (item) {
                        if (item.id) {
                            return item.id === applicationSetting.id;
                        } else if (item.tempId) {
                            return item.tempId === applicationSetting.tempId;
                        } else {
                            return false;
                        }
                    });

                    if (index >= 0) {
                        $scope.configuration.applicationSettings[index] = applicationSetting;
                        filterApplicationSettings();
                    }
                });
            };

            $scope.removeApplicationSetting = function (applicationSetting) {
                var index = $scope.configuration.applicationSettings.findIndex(function (item) {
                    if (item.id) {
                        return item.id === applicationSetting.id;
                    } else if (item.tempId) {
                        return item.tempId === applicationSetting.tempId;
                    } else {
                        return false;
                    }
                });

                if (index >= 0) {
                    $scope.configuration.applicationSettings.splice(index, 1);
                    filterApplicationSettings();
                }
            };

            $scope.editFile = function (file) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/configurationFile.html',
                    controller: 'FileEditorController',
                    resolve: {
                        file: function () {
                            return file;
                        },
                        defaultFilePath: function() {
                            return $scope.configuration.defaultFilePath;
                        }
                    }
                });

                modalInstance.result.then(function (updatedFile) {
                    $scope.configurationForm.$dirty = true;
                    var index = $scope.configuration.files.findIndex(function (item) {
                        if (item.id) {
                            return item.id === updatedFile.id;
                        } else if (item.tempId) {
                            return item.tempId === updatedFile.tempId;
                        } else {
                            return false;
                        }
                    });

                    if (index >= 0) {
                        $scope.configuration.files[index] = updatedFile;
                        filterFiles();
                    }
                });
            };

            $scope.removeFile = function (file) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/components/main/view/modal/removeFileConfirmation.html',
                    controller: 'RemoveConfigurationFileModalController',
                    resolve: {
                        file: function () {
                            return file;
                        }
                    }
                });

                modalInstance.result.then(function (removeFileFromDisk) {
                    var index = $scope.configuration.files.findIndex(function (item) {
                        if (item.id) {
                            return item.id === file.id;
                        } else if (item.tempId) {
                            return item.tempId === file.tempId;
                        } else {
                            return false;
                        }
                    });

                    if (removeFileFromDisk) {
                        if (!$scope.configuration.filesToRemove) {
                            $scope.configuration.filesToRemove = [];
                        }

                        $scope.configuration.filesToRemove.push(file.fileId);
                    }

                    if (index >= 0) {
                        $scope.configurationForm.$dirty = true;
                        $scope.configuration.files.splice(index, 1);
                        filterFiles();
                    }
                });
            };

            var turnWifiOn = function () {
                $scope.configuration.wifi = true;
            };

            var turnGpsOn = function () {
                $scope.configuration.gps = true;
            };

            $scope.requestUpdatesChanged = function () {
                var networkStatus = true;
                var alertText;
                var alertCallback;
                var alertButtonText;
                if ($scope.configuration.requestUpdates === 'WIFI') {
                    networkStatus = $scope.configuration.wifi;
                    alertText = 'form.configuration.settings.request.updates.prompt.wifi';
                    alertButtonText = 'button.wifi.on';
                    alertCallback = turnWifiOn;
                } else if ($scope.configuration.requestUpdates === 'GPS') {
                    networkStatus = $scope.configuration.gps;
                    alertText = 'form.configuration.settings.request.updates.prompt.gps';
                    alertButtonText = 'button.gps.on';
                    alertCallback = turnGpsOn;
                }

                if (networkStatus === false) {
                    confirmModal.getUserConfirmation(localization.localize(alertText), alertCallback, alertButtonText);
                }
            };

            var filterApplicationSettings = function () {
                $scope.applicationSettings = $scope.configuration.applicationSettings.filter(function (item) {
                    var valid = true;
                    if ($scope.settingsPaging.appSettingsFilterText && $scope.settingsPaging.appSettingsFilterText.length > 0) {
                        var lower = $scope.settingsPaging.appSettingsFilterText.toLowerCase();

                        valid = (item.name !== null) && (item.name !== undefined) && item.name.toLowerCase().indexOf(lower) > -1
                            || (item.value !== null) && (item.value !== undefined) && item.value.toLowerCase().indexOf(lower) > -1
                            || (item.comment !== null) && ((item.comment !== undefined)) && item.comment.toLowerCase().indexOf(lower) > -1
                    }

                    if (valid) {
                        if ($scope.settingsPaging.appSettingsFilterApp && $scope.settingsPaging.appSettingsFilterApp.id) {
                            valid = item.applicationId === $scope.settingsPaging.appSettingsFilterApp.id;
                        } else if (typeof $scope.settingsPaging.appSettingsFilterApp === "string") {
                            valid = item.applicationPkg.toLowerCase().indexOf($scope.settingsPaging.appSettingsFilterApp.toLowerCase(0)) > -1;
                        }
                    }

                    return valid;
                });
            };

            var filterFiles = function () {
                $scope.files = $scope.configuration.files.filter(function (item) {
                    var valid = true;
                    if ($scope.filesPaging.filesFilterText && $scope.filesPaging.filesFilterText.length > 0) {
                        var lower = $scope.filesPaging.filesFilterText.toLowerCase();

                        valid = (item.description !== null) && (item.description !== undefined) && item.description.toLowerCase().indexOf(lower) > -1
                            || (item.path !== null) && (item.path !== undefined) && item.path.toLowerCase().indexOf(lower) > -1
                            || (item.url !== null) && ((item.url !== undefined)) && item.url.toLowerCase().indexOf(lower) > -1
                    }

                    return valid;
                });
            };

            function pad(num, size) {
                var s = num + "";
                while (s.length < size) s = "0" + s;
                return s;
            }

            // Entry point
            var configId = $stateParams.id;
            setTimeout(function () {
                angular.element(document.querySelector('#password-c')).attr('type', 'password');
            }, 300);

            $scope.togglePassword = function() {
                var passwordElement = angular.element(document.querySelector('#password-c'));
                var passwordButton = angular.element(document.querySelector('#button-show-password'));
                var passwordIcon = angular.element(document.querySelector('#span-show-password'));
                var type = passwordElement.attr('type');
                if (type == 'text') {
                    passwordElement.attr('type', 'password');
                    passwordButton.attr('title', localization.localize('button.show.password'));
                    passwordIcon.removeClass('glyphicon-eye-close');
                    passwordIcon.addClass('glyphicon-eye-open');
                } else {
                    passwordElement.attr('type', 'text');
                    passwordButton.attr('title', localization.localize('button.hide.password'));
                    passwordIcon.removeClass('glyphicon-eye-open');
                    passwordIcon.addClass('glyphicon-eye-close');
                }
            };

            var mainAppSelected = false;
            var contentAppSelected = false;
            var allApplications;

            $scope.configuration = {
                defaultFilePath: "/"
            };
            $scope.isTypical = ($stateParams.typical === 'true');
            $scope.saved = false;
            let item = $window.localStorage.getItem('HMDM_configShowSystemApps');
            if (item !== null && item !== undefined) {
                $scope.showSystemApps = (item === 'true');
            } else {
                $scope.showSystemApps = true;
            }

            var d1 = new Date();
            d1.setHours(0);
            d1.setMinutes(0);

            var d2 = new Date();
            d2.setHours(23);
            d2.setMinutes(59);

            $scope.dates = {};

            if (configId) {
                configurationService.getById({"id": configId}, function (response) {
                    if (response.data) {
                        $scope.configuration = response.data;

                        filterApplicationSettings();
                        filterFiles();
                        updateMqttHint();

                        if (response.data.systemUpdateType === 2) {
                            try {
                                if (response.data.systemUpdateFrom) {
                                    var time = response.data.systemUpdateFrom;
                                    var pos = time.indexOf(':');
                                    if (pos > -1) {
                                        d1.setHours(parseInt(time.substring(0, pos)));
                                        d1.setMinutes(parseInt(time.substring(pos + 1)));
                                    }
                                }
                                if (response.data.systemUpdateTo) {
                                    var time = response.data.systemUpdateTo;
                                    var pos = time.indexOf(':');
                                    if (pos > -1) {
                                        d2.setHours(parseInt(time.substring(0, pos)));
                                        d2.setMinutes(parseInt(time.substring(pos + 1)));
                                    }
                                }
                            } catch (e) {
                                console.error('Failed to parse system update times from server', e);
                            }
                        }

                        $scope.dates.systemUpdateFrom = d1;
                        $scope.dates.systemUpdateTo = d2;

                        if ($scope.configuration.timeZone === null) {
                            $scope.configuration.timeZoneMode = 'default';
                        } else if ($scope.configuration.timeZone === 'auto') {
                            $scope.configuration.timeZoneMode = 'auto';
                        } else {
                            $scope.configuration.timeZoneMode = 'manual';
                        }
                    }
                });
            } else {
                $scope.dates.systemUpdateFrom = d1;
                $scope.dates.systemUpdateTo = d2;
                $scope.configuration.eventReceivingComponent = 'com.hmdm.launcher.AdminReceiver';
                $scope.configuration.systemUpdateType = 0;
            }

            $scope.selected = {id: ''};

            $scope.paging = {
                currentPage: 1,
                pageSize: 50,
                filterText: ''
            };

            $scope.settingsPaging = {
                currentPage: 1,
                pageSize: 50,
                appSettingsAppFilterText: '',
                appSettingsFilterText: '',
                appSettingsFilterApp: null
            };

            $scope.filesPaging = {
                currentPage: 1,
                pageSize: 50,
                filesFilterText: '',
            };

            $scope.$watch('paging.currentPage', function () {
                $window.scrollTo(0, 0);
            });
            $scope.$watch('settingsPaging.currentPage', function () {
                $window.scrollTo(0, 0);
            });
            $scope.$watch('filesPaging.currentPage', function () {
                $window.scrollTo(0, 0);
            });

            $scope.mainApp = {id: -1, name: ""};
            $scope.contentApp = {id: -1, name: ""};

            if (!configId) {
                $scope.configuration.useDefaultDesignSettings = true;
                // settingsService.getSettings(function (response) {
                //     if (response.data) {
                //         $scope.configuration.backgroundColor = response.data.backgroundColor;
                //         $scope.configuration.textColor = response.data.textColor;
                //         $scope.configuration.backgroundImageUrl = response.data.backgroundImageUrl;
                //         $scope.configuration.iconSize = response.data.iconSize;
                //         $scope.configuration.desktopHeader = response.data.desktopHeader;
                //     }
                // });
            }

            $scope.loadApps(configId);
        })
    .controller('ConfigurationAppVersionSelectController', function ($scope, $modalInstance, applicationService,
                                                                     localization, application, applicationParameters) {

        $scope.errorMessage = undefined;
        $scope.application = application;
        $scope.versions = [];

        var applicationParametersCopy = {
            applicationId: application.id,
            skipVersionCheck: false
        };
        if (applicationParameters) {
            for (var p in applicationParameters) {
                if (applicationParameters.hasOwnProperty(p)) {
                    applicationParametersCopy[p] = applicationParameters[p];
                }
            }
        }

        $scope.applicationParameters = applicationParametersCopy;

        $scope.usedVersion = {
            applicationVersionId: application.usedVersionId || application.latestVersion
        };

        applicationService.getApplicationVersions({id: application.id}, function (response) {
            if (response.status === 'OK') {
                $scope.versions = response.data;
            } else {
                $scope.errorMessage = localization.localize(response.message);
            }
        }, function () {
            $scope.errorMessage = localization.localize('error.request.failure')
        });

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            $modalInstance.close({
                selectedVersion: $scope.usedVersion,
                availableVersions: $scope.versions,
                applicationParameters: $scope.applicationParameters
            });
        };
    })
    .controller('ConfigurationAppDetailsController', function ($scope, $modalInstance, applicationService,
                                                                     localization, application) {

        $scope.errorMessage = undefined;
        $scope.application = application;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };
    })
    .controller('FileEditorController', function ($scope, $modalInstance, localization, file, defaultFilePath) {

        $scope.file = angular.copy(file, {});
        $scope.errorMessage = undefined;
        $scope.fileSelected = false;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            $scope.errorMessage = undefined;
            if (!$scope.file.lastUpdate) {
                $scope.file.lastUpdate = Date.now();
            }
            $scope.file.remove = ($scope.file.remove === 'true' || $scope.file.remove === true);
            if ($scope.file.externalUrl && $scope.file.externalUrl.trim().length === 0) {
                $scope.file.externalUrl = null;
            }
            if ($scope.file.filePath && $scope.file.filePath.trim().length === 0) {
                $scope.file.filePath = null;
            }

            if (!$scope.file.path || $scope.file.path.trim().length === 0) {
                $scope.errorMessage = localization.localize('error.configuration.file.empty.path');
            } else if (!$scope.file.remove && (!$scope.file.externalUrl || $scope.file.externalUrl.trim().length === 0)
                && (!$scope.file.filePath || $scope.file.filePath.trim().length === 0)) {
                $scope.errorMessage = localization.localize('error.configuration.file.empty.file');
            } else {
                if ($scope.file.externalUrl && $scope.file.externalUrl.trim().length > 0) {
                    $scope.file.url = $scope.file.externalUrl;
                    $scope.file.filePath = undefined;
                    $scope.file.fileId = undefined;
                } else {
                    // $scope.file.url = $scope.file.filePath;
                    $scope.file.externalUrl = undefined;
                }
                $modalInstance.close($scope.file);
            }
        };

        $scope.onStartedUpload = function (files) {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
            $scope.fileSelected = false;

            if (files.length > 0) {
                $scope.loading = true;
                $scope.successMessage = localization.localize('success.uploading.file');
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

            if (response.data.status === 'OK') {
                if (!defaultFilePath.endsWith("/")) {
                    defaultFilePath += "/";
                }
                $scope.file.path = defaultFilePath + response.data.data.filePath;
                $scope.file.filePath = response.data.data.filePath;
                $scope.file.fileId = response.data.data.id;
                $scope.file.lastUpdate = response.data.data.uploadTime;
                $scope.file.checksum = response.data.data.checksum;
                $scope.file.url = response.data.data.url;
                $scope.fileSelected = true;
                $scope.successMessage = localization.localize('success.file.uploaded');
            } else {
                $scope.errorMessage = localization.localize(response.data.message);
            }
        };

        $scope.clearFile = function () {
            $scope.file.filePath = undefined;
            $scope.file.url = undefined;
            $scope.file.fileId = undefined;
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
            $scope.fileSelected = false;
            $scope.loading = false;
        };
    })
    .controller('RemoveConfigurationFileModalController',
        function ($scope, $modalInstance, file) {

            $scope.obj = {
                deleteFileFromDisk: false
            };

            $scope.deleteOptionEnabled = !file.externalUrl || file.externalUrl.trim().length === 0;

            $scope.save = function () {
                $modalInstance.close($scope.deleteOptionEnabled && $scope.obj.deleteFileFromDisk);
            };

            $scope.closeModal = function () {
                $modalInstance.dismiss();
            }
        })
;