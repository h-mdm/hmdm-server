// Localization completed
angular.module('headwind-kiosk')
    .controller('SettingsTabController', function ($scope, $rootScope, $timeout, $modal, hintService, settingsService,
                                                   localization, authService, userService,
                                                   groupService, configurationService) {
        $scope.settings = {};
        $scope.userRoleSettings = {};
        $scope.loading = false;

        var userRoleSettings = {};

        $scope.formData = {
            userRoleId: authService.getUser().userRole.id
        };

        var onRequestFailure = function () {
            $scope.loading = false;
            $scope.errorMessage = localization.localize('error.request.failure');
        };

        var clearMessages = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
        };

        $scope.init = function () {
            $rootScope.settingsTabActive = true;
            $rootScope.pluginsTabActive = false;

            clearMessages();

            $scope.loading = true;

            groupService.getAllGroups(function (response) {
                $scope.groups = response.data;

                configurationService.getAllConfigurations(function (response) {
                    $scope.configurations = response.data;

                    settingsService.getSettings(function (response) {
                        if (response.data) {
                            $scope.settings = response.data;
                        }
                        $scope.loading = false;
                    }, onRequestFailure);
                }, onRequestFailure);
            }, onRequestFailure);

        };

        $scope.desktopHeaderTemplatePlaceholder = localization.localize('form.configuration.settings.design.desktop.header.template.placeholder') + ' deviceId, description, custom1, custom2, custom3';

        $scope.initCommonSettings = function () {
            clearMessages();

            var roleId = authService.getUser().userRole.id;
            $scope.loading = true;
            settingsService.getUserRoleSettings({roleId: roleId}, function (response) {
                if (response.status === 'OK') {
                    $scope.userRoleSettings = response.data;
                    userRoleSettings[roleId] = response.data;

                    userService.getUserRoles(function (response) {
                        if (response.status === 'OK') {
                            $scope.userRoles = response.data;
                        } else {
                            $scope.errorMessage = localization.localizeServerResponse(response);
                        }
                        $scope.loading = false;
                    }, onRequestFailure);
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, onRequestFailure);
        };

        $scope.userRoleChanged = function () {
            clearMessages();

            var roleId = $scope.formData.userRoleId;
            if (!userRoleSettings[roleId]) {
                $scope.loading = true;
                settingsService.getUserRoleSettings({roleId: roleId}, function (response) {
                    if (response.status === 'OK') {
                        $scope.userRoleSettings = response.data;
                        userRoleSettings[roleId] = response.data;
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                    $scope.loading = false;
                }, onRequestFailure);
            } else {
                $scope.userRoleSettings = userRoleSettings[roleId];
            }
        };

        $scope.uploadBackground = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/file.html',
                // Defined in files.controller.js
                controller: 'FileModalController'
            });

            modalInstance.result.then(function (data) {
                if (data) {
                    $scope.settings.backgroundImageUrl = data.url;
                }
            });
        };

        $scope.saveDefaultDesignSettings = function () {
            clearMessages();
            settingsService.updateDefaultDesignSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.settings.design.saved');
                    $timeout(function () {
                        $scope.successMessage = '';
                    }, 2000);
                }
            });
        };

        $scope.saveCommonSettings = function () {
            clearMessages();
            var settings = [];
            for (var p in userRoleSettings) {
                if (userRoleSettings.hasOwnProperty(p)) {
                    settings.push(userRoleSettings[p]);
                }
            }

            settingsService.updateUserRolesCommonSettings(settings, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.settings.common.saved');
                    $timeout(function () {
                        $scope.successMessage = '';
                    }, 2000);
                    $rootScope.$broadcast('aero_COMMON_SETTINGS_UPDATED', settings);
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.saveLanguageSettings = function () {
            clearMessages();

            if ($scope.settings.createNewDevices && !$scope.settings.newDeviceConfigurationId) {
                $scope.errorMessage = localization.localize('error.empty.configuration');
                return;
            }

            settingsService.updateMiscSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    settingsService.updateLanguageSettings($scope.settings, function (response) {
                        if (response.status === 'OK') {
                            $rootScope.$broadcast('aero_LANGUAGE_SETTINGS_UPDATED', $scope.settings);
                            $scope.successMessage = localization.localize('success.settings.saved');
                            $timeout(function () {
                                $scope.successMessage = '';
                            }, 2000);
                        }
                    });
                }
            });
        };

        $scope.enableHints = function () {
            clearMessages();
            hintService.enableHints(function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.settings.hints.enabled');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.errorMessage = localization.localize('error.request.failure');
            });
        };

        $scope.disableHints = function () {
            clearMessages();
            hintService.disableHints(function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.settings.hints.disabled');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.errorMessage = localization.localize('error.request.failure');
            });
        };

        $scope.init();

    });