<!-- Localization completed -->
angular.module('headwind-kiosk')
    .controller('SignupCompleteController', function ($scope, $state, $rootScope, $timeout, authService, localization, rebranding,
                                             signupService, $stateParams) {
        $scope.rebranding = null;
        rebranding.query(function(value) {
            $scope.rebranding = value;
            $scope.rebranding.year = new Date().getFullYear();
            $scope.signupTitle = localization.localize('signup.complete.title').replace('${appName}', $scope.rebranding.appName);
        });

        signupService.verifyToken({token: $stateParams.token}, function (response) {
            $scope.tokenValid = (response.status === 'OK');
        });

        $scope.customer = {
            token: $stateParams.token
        };
        $scope.passwds = {
            passwd1: '',
            passwd2: ''
        };
        $scope.signupComplete = false;

        $scope.checkPassword = function() {
            if (typeof $scope.passwds.passwd1 === 'undefined' || $scope.passwds.passwd1.length < 6) {
                $scope.passwordError = localization.localize('signup.password.short');
                return false;
            }
            if (typeof $scope.passwds.passwd2 === 'undefined' || $scope.passwds.passwd1 != $scope.passwds.passwd2) {
                $scope.passwordError = localization.localize('signup.password.not.match');
                return false;
            }
            $scope.passwordError = '';
            return true;
        };

        $scope.onSignup = function () {
            if (!$scope.checkPassword()) {
                return;
            }
            $scope.loading = true;
            $scope.errorMessage = '';
            $scope.customer.passwd = md5($scope.passwds.passwd1).toUpperCase();
            signupService.complete($scope.customer, function (response) {
                $scope.loading = false;
                if (response.status === 'OK') {
                    $scope.signupTitle = localization.localize('signup.ready.title').replace('${appName}', $scope.rebranding.appName);
                    $scope.signupComplete = true;
                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localize('signup.name.used');
                }
            });
        };

        $scope.login = function() {
            $state.transitionTo('login');
        };

        $scope.signup = function() {
            $state.transitionTo('signup');
        };
    });
