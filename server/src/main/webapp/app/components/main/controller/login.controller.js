<!-- Localization completed -->
angular.module('headwind-kiosk')
    .controller('LoginController', function ($scope, $state, $rootScope, $timeout, authService, localization) {
        $scope.login = {};

        var loginHandler = function (response) {
            if (response.status === 'OK') {
                $state.transitionTo('main');
                $rootScope.$emit('aero_USER_AUTHENTICATED');
            } else if (response.status === 'ERROR') {
                $scope.errorMessage = localization.localize('login.password.incorrect');
            }
        };

        $scope.onLogin = function () {
            authService.login($scope.login.username, md5($scope.login.password).toUpperCase(), loginHandler);
        }
    });


