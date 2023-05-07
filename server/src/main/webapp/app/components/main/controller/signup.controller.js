<!-- Localization completed -->
angular.module('headwind-kiosk')
    .controller('SignupController', function ($scope, $state, $rootScope, $timeout, authService, localization, rebranding,
                                             signupService, getBrowserLanguage) {
        $scope.signup = {};
        $scope.signupComplete = false;

        $scope.rebranding = null;
        rebranding.query(function(value) {
            $scope.rebranding = value;
            $scope.rebranding.year = new Date().getFullYear();
        });

        $scope.onSignup = function () {
            $scope.signup.language = getBrowserLanguage().substr(0, 2);
            $scope.loading = true;
            $scope.errorMessage = '';
            signupService.verifyEmail($scope.signup, function (response) {
                $scope.loading = false;
                if (response.status === 'OK') {
                    $scope.signupComplete = true;
                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localize('signup.email.used');
                }
            });
        };
    });
