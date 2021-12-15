// Localization completed
angular.module('headwind-kiosk')
    .controller('ProfileController', function ($scope, authService, userService, settingsService, localization, passwordService) {
        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.user = authService.getUser();
        $scope.user.newPassword = '';
        $scope.user.confirm = '';

        $scope.errorMessage = '';
        $scope.completeMessage = '';

        $scope.now = (new Date()) * 1;
        $scope.accountType = function(type) {
            switch (type) {
                case 0:
                    return localization.localize('customer.type.demo');
                case 1:
                    return localization.localize('customer.type.small');
                case 2:
                    return localization.localize('customer.type.corporate');
            }
            return '';
        };

        $scope.saveDetails = function() {
            var user = {};
            for (var p in $scope.user) {
                if ($scope.user.hasOwnProperty(p)) {
                    user[p] = $scope.user[p];
                }
            }
            userService.updateDetails(user, function (response) {
                resetMessages();

                if (response.status === 'OK') {
                    authService.update(response.data);
                    $scope.user = authService.getUser();
                    $scope.completeMessage = localization.localizeServerResponse(response);
                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.saveProfile = function() {
            if ($scope.user.newPassword !== $scope.user.confirm) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.mismatch.password');
                return;
            }

            if ($scope.user.newPassword == '') {
                resetMessages();
                $scope.errorMessage = localization.localize('error.password.empty');
                return;
            }

            if (!passwordService.checkQuality($scope.user.newPassword, $scope.settings.passwordLength, $scope.settings.passwordStrength)) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.password.weak');
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
        };

        $scope.saveEnabled = false;
        settingsService.getSettings(function (response) {
            if (response.data) {
                $scope.settings = response.data;
                $scope.qualityMessage = passwordService.qualityMessage($scope.settings.passwordLength, $scope.settings.passwordStrength);
                $scope.saveEnabled = true;
            }
        });

    });



