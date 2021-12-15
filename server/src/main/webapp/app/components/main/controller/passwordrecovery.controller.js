// Localization completed
angular.module('headwind-kiosk')
    .controller('PasswordRecoveryController', function ($scope, passwordResetService, localization) {
        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.username = '';
        $scope.recoverySubmitted = false;

        resetMessages();

        $scope.recoverPassword = function () {

            passwordResetService.recoverPassword({username: $scope.username}, function (response) {
                resetMessages();

                if (response.status === 'OK') {
                    $scope.recoverySubmitted = true;
                    $scope.successMessage = localization.localize('recovery.password.success');
                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

    });



