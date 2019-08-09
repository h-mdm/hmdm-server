// Localization completed
angular.module('headwind-kiosk')
    .controller('SettingsTabController', function ($scope, $rootScope, $timeout, settingsService, localization) {
        $scope.settings = {};

        $scope.init = function () {
            $rootScope.settingsTabActive = true;
            $rootScope.pluginsTabActive = false;

            settingsService.getSettings(function (response) {
                if (response.data) {
                    $scope.settings = response.data;
                }
            });
        };

        $scope.saveDefaultDesignSettings = function () {
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
            settingsService.updateCommonSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.settings.common.saved');
                    $timeout(function () {
                        $scope.successMessage = '';
                    }, 2000);
                    $rootScope.$broadcast('aero_COMMON_SETTINGS_UPDATED', $scope.settings);
                }
            });
        };

        $scope.saveLanguageSettings = function () {
            settingsService.updateLanguageSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    $rootScope.$broadcast('aero_LANGUAGE_SETTINGS_UPDATED', $scope.settings);
                    $scope.successMessage = localization.localize('success.settings.language.saved');
                    $timeout(function () {
                        $scope.successMessage = '';
                    }, 2000);
                }
            });
        };

        $scope.init();

    });