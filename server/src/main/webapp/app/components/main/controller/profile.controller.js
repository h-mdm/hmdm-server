// Localization completed
angular.module('headwind-kiosk')
    .controller('ProfileController', function ($scope, authService, userService, localization) {
        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.user = authService.getUser();

        $scope.errorMessage = '';
        $scope.completeMessage = '';

        $scope.saveProfile = function () {
            if ($scope.user.newPassword !== $scope.user.confirm) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.mismatch.password');
                return;
            }

            var user = {};
            for (var p in $scope.user) {
                if ($scope.user.hasOwnProperty(p)) {
                    user[p] = $scope.user[p];
                }
            }

            user.newPassword = user.newPassword ? md5(user.newPassword).toUpperCase() : undefined;
            user.oldPassword = user.oldPassword ? md5(user.oldPassword).toUpperCase() : undefined;

            userService.updatePassword(user, function (response) {
                resetMessages();

                if (response.status === 'OK') {
                    authService.update(response.data);
                    $scope.user = authService.getUser();
                    $scope.completeMessage = localization.localizeServerResponse(response);
                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        }
    });



