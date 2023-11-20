// Localization completed
angular.module('headwind-kiosk')
    .controller('SettingsTabController', function ($scope, $rootScope, $timeout, $modal, hintService, settingsService,
                                                   localization, authService, userService, confirmModal, Idle,
                                                   groupService, configurationService, twoFactorAuthService) {
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
                            $scope.initTwoFactor($scope.settings);
                        }
                        $scope.loading = false;
                    }, onRequestFailure);
                }, onRequestFailure);
            }, onRequestFailure);

        };

        var user = authService.getUser();
        $scope.twoFactor = {
            success: null,
            error: null,
            accepted: user.twoFactorAccepted,
            qrCodeUrl: 'rest/private/twofactor/qr/' + user.id,
            code: ''
        };

        $scope.initTwoFactor = function(settings) {
            $scope.twoFactor.use = settings.twoFactor;
        };

        $scope.twoFactorToggled = function() {
            if (!$scope.twoFactor.use) {
                $scope.twoFactor.use = true;
                confirmModal.getUserConfirmation(localization.localize('form.two.factor.auth.off.confirm'), function () {
                    twoFactorAuthService.reset(function(response) {
                        if (response.status === 'OK') {
                            var user = authService.getUser();
                            user.twoFactorSecret = null;
                            user.twoFactorAccepted = false;
                            authService.update(user);
                            $scope.settings.twoFactor = false;
                            $scope.twoFactor.accepted = false;
                            $scope.twoFactor.code = '';
                            $scope.twoFactor.error = '';
                            $scope.twoFactor.success = localization.localize('form.two.factor.auth.reset');
                            $scope.twoFactor.use = false;
                            $timeout(function () {
                                $scope.twoFactor.success = null;
                            }, 5000);
                        } else {
                            $scope.twoFactor.error = localization.localizeServerResponse(response);
                        }
                    });
                });
            } else {
                // Force QR code to reload and re-generate the secret
                $scope.twoFactor.qrCodeUrl = 'rest/private/twofactor/qr/' + authService.getUser().id +
                    '?' + new Date().getTime();
            }
        };

        $scope.verifyTwoFactor = function() {
            if ($scope.twoFactor.code.length != 6 || !/^\d+$/.test($scope.twoFactor.code)) {
                $scope.twoFactor.error = localization.localize('form.two.factor.auth.code.error');
                return;
            }

            var data = {
                user: authService.getUser().id,
                code: $scope.twoFactor.code
            };
            twoFactorAuthService.verify(data, function (response) {
                if (response.status === 'OK') {
                    var user = authService.getUser();
                    user.twoFactorAccepted = true;
                    authService.update(user);
                    twoFactorAuthService.set(function(response) {
                        if (response.status === 'OK') {
                            $scope.settings.twoFactor = true;
                            $scope.twoFactor.accepted = true;
                            $scope.twoFactor.code = '';
                            $scope.twoFactor.error = '';
                            $scope.twoFactor.success = localization.localize('form.two.factor.auth.set');
                            $timeout(function () {
                                $scope.twoFactor.success = null;
                            }, 5000);
                        } else {
                            $scope.twoFactor.error = localization.localizeServerResponse(response);
                        }
                    });
                } else if (response.status === 'ERROR') {
                    if (response.message === 'error.permission.denied') {
                        $scope.twoFactor.error = localization.localize('form.two.factor.auth.code.invalid');
                    } else {
                        $scope.twoFactor.error = localization.localizeServerResponse(response);
                    }
                }
            });
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

            if ($scope.settings.idleLogout) {
                Idle.setIdle($scope.settings.idleLogout);
                Idle.setTimeout(10);
                Idle.watch();
            } else {
                $scope.settings.idleLogout = null;  // Change 0 to null
                Idle.unwatch();
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