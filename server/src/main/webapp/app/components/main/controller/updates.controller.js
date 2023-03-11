// Localization completed
angular.module('headwind-kiosk')
    .controller('UpdatesController', function ($scope, updatesService, localization, utils, APP_VERSION) {
        $scope.errorMessage = '';
        $scope.completeMessage = localization.localize('updates.checking');
        $scope.isChecking = true;
        $scope.isUpdating = false;
        $scope.isError = false;
        $scope.updateForm = {
            update: false,
            sendStats: true
        };

        var checkUpdates = function() {
            updatesService.checkUpdates(function (response) {
                $scope.isChecking = false;
                $scope.completeMessage = '';

                if (response.status === 'OK') {
                    response.data.forEach(function(app) {
                        if (app.pkg === 'web') {
                            app.currentVersion = APP_VERSION;
                        }
                        app.outdated = utils.compareVersions(app.currentVersion, app.version) < 0;
                        if (app.updateDisabled) {
                            app.updateDisableReasonLocalized = localization.localize('updates.disabled.' + app.updateDisableReason);
                        }
                    });

                    $scope.updates = response.data;
                } else if (response.status === 'ERROR') {
                        $scope.isError = true;
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.getUpdates = function() {
            $scope.completeMessage = localization.localize('updates.getting');
            $scope.isUpdating = true;
            $scope.updateForm.updates = $scope.updates;

            updatesService.getUpdates($scope.updateForm, function (response) {
                $scope.isUpdating = false;
                $scope.completeMessage = localization.localize('updates.success');

                if (response.status === 'OK') {
                    response.data.forEach(function(app) {
                        app.outdated = utils.compareVersions(app.currentVersion, app.version) < 0;
                        if (app.updateDisabled) {
                            app.updateDisableReasonLocalized = localization.localize('updates.disabled.' + app.updateDisableReason);
                        }
                    });

                    $scope.updates = response.data;

                } else if (response.status === 'ERROR') {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        checkUpdates();
    });



