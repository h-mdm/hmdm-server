// Localization completed
angular.module('headwind-kiosk')
    .controller('QRController', function ($state, $scope, $window, $stateParams, hintService, $timeout) {
        $scope.size = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);
        $scope.deviceId = $stateParams.deviceId;

        $scope.formData = {
            deviceIdNew: $stateParams.deviceId
        };

        $scope.qrCodeKey = $stateParams.code;
        $scope.devices = [];
        $scope.device = {};
        $scope.showQR = true;
        $scope.showHelp = false;
        $scope.helpSize = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);

        $scope.renew = function () {
            $scope.showQR = false;
            $scope.size = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);
            $scope.deviceId = $scope.formData.deviceIdNew;
            $scope.showQR = true;
        };

        // angular.element($window).bind('resize', function(){
        //     $scope.helpSize = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);
        // });

        $timeout(function () {
            hintService.onStateChangeSuccess();
        }, 100);

        $scope.helpClicked = function () {
            $scope.showHelp = !$scope.showHelp;
        };
    });


