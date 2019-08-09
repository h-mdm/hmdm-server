// Localization completed
angular.module('headwind-kiosk')
    .controller('AboutController', function ($scope, $rootScope, $modalInstance, APP_VERSION, localization) {

        $scope.line3Text = localization.localize('about.line.3').replace('${versionNumber}', APP_VERSION);
        
        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    });
