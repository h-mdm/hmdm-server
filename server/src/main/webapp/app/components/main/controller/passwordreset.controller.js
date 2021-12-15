// Localization completed
angular.module('headwind-kiosk')
    .controller('PasswordResetController', function ($scope, $state, $stateParams, passwordResetService, localization, passwordService) {
        var token = $stateParams.token;

        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.reset = {
            newPassword: '',
            confirmPassword: ''
        };

        $scope.errorMessage = '';
        $scope.completeMessage = '';

        $scope.resetPassword = function () {
            if ($scope.reset.newPassword !== $scope.reset.confirmPassword) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.mismatch.password');
                return;
            }

            if ($scope.reset.newPassword == '') {
                resetMessages();
                $scope.errorMessage = localization.localize('error.password.empty');
                return;
            }

            if (!passwordService.checkQuality($scope.reset.newPassword, $scope.settings.passwordLength, $scope.settings.passwordStrength)) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.password.weak');
                return;
            }

            var data = {
                passwordResetToken: token,
                newPassword: md5($scope.reset.newPassword).toUpperCase()
            };

            passwordResetService.resetPassword(data, function (response) {
                resetMessages();

                if (response.status === 'OK') {
                    $state.transitionTo('main');
                    $rootScope.$emit('aero_USER_AUTHENTICATED');
                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.linkValid = false;
        passwordResetService.getSettings({token: token}, function (response) {
            if (response.status === 'OK' && response.data) {
                $scope.settings = response.data;
                $scope.qualityMessage = passwordService.qualityMessage($scope.settings.passwordLength, $scope.settings.passwordStrength);
                $scope.linkValid = true;
            } else {
                $scope.errorMessage = localization.localize('form.password.reset.invalid');
            }
        });

    });



