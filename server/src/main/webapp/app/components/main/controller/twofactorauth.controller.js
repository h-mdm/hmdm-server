// Localization completed
angular.module('headwind-kiosk')
    .controller('TwoFactorAuthController', function ($scope, $rootScope, $state, $stateParams, authService,
                                                     twoFactorAuthService, localization) {
        var token = $stateParams.token;

        $scope.user = authService.getUser();

        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.auth = {
            qrCodeUrl: 'rest/private/twofactor/qr/' + $scope.user.id,
            code: ''
        };

        $scope.errorMessage = '';
        $scope.completeMessage = '';

        $scope.verify = function () {

            if ($scope.auth.code.length != 6 || !/^\d+$/.test($scope.auth.code)) {
                resetMessages();
                $scope.errorMessage = localization.localize('form.two.factor.auth.code.error');
                return;
            }

            var data = {
                user: $scope.user.id,
                code: $scope.auth.code
            };
            twoFactorAuthService.verify(data, function (response) {
                resetMessages();

                if (response.status === 'OK') {
                    $state.transitionTo('main');
                    $rootScope.$emit('aero_USER_AUTHENTICATED');
                } else if (response.status === 'ERROR') {
                    if (response.message === 'error.permission.denied') {
                        $scope.errorMessage = localization.localize('form.two.factor.auth.code.invalid');
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }
            });
        };

    });



