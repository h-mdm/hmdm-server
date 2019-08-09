// Localization completed
angular.module('headwind-kiosk')
    .factory('alertService', function ($modal, localization) {

        var showAlert = function (message) {
            var modalInstance = $modal.open({
                templateUrl: 'app/shared/view/alert.html',
                controller: 'AlertController',
                resolve: {
                    message: function () {
                        return message;
                    }
                }
            });

            return modalInstance;
        };

        return {
            showAlertMessage: function (message) {
                showAlert(message);
            },
            onRequestFailure: function (response) {
                console.error("Error when sending request to server", response);
                showAlert(localization.localize('error.request.failure'));
            }
        }
    })
    .controller('AlertController', function ($scope, $modalInstance, message) {
        $scope.message = message;
        $scope.OK = function () {
            $modalInstance.close();
        }
    });
