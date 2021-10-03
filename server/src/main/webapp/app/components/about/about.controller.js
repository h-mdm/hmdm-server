// Localization completed
angular.module('headwind-kiosk')
    .controller('AboutController', function ($scope, $rootScope, $modalInstance, APP_VERSION, localization,
                                             pluginService, rebranding) {

        rebranding.query(function(value) {
            $scope.line1Text = localization.localize('about.line.1').replace('${appName}', value.appName);
        });
        $scope.line3Text = localization.localize('about.line.3').replace('${versionNumber}', APP_VERSION);

        pluginService.getAvailablePlugins(function (response) {
            if (response.status === 'OK') {
                $scope.plugins = response.data.map(function (plugin) {
                    return localization.localize(plugin.nameLocalizationKey)
                }).sort();
                $scope.pluginList = $scope.plugins.join(', ');
            }
        });

        var listener = $scope.$on('aero_USER_LOGOUT', $modalInstance.dismiss);
        $scope.$on("$destroy", listener);
        
        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    });
