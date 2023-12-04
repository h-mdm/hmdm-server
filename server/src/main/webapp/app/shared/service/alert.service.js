// Localization completed
angular.module('headwind-kiosk')
    .factory('alertService', function ($modal, localization) {

        var showAlert = function (message, callback, okButtonTextKey) {
            var modalInstance = $modal.open({
                templateUrl: 'app/shared/view/alert.html',
                controller: 'AlertController',
                resolve: {
                    message: function () {
                        return message;
                    },
                    okButtonTextKey: function () {
                        if (okButtonTextKey) {
                            return okButtonTextKey;
                        } else {
                            return 'button.close';
                        }
                    }
                }
            });

            modalInstance.result.then(function () {
                if (callback) callback();
            });

            return modalInstance;
        };

        return {
            showAlertMessage: function (message, callback, okButtonTextKey) {
                return showAlert(message, callback, okButtonTextKey);
            },
            onRequestFailure: function (response) {
                console.error("Error when sending request to server", response);
                return showAlert(localization.localize('error.request.failure'));
            }
        }
    })
    .controller('AlertController', function ($scope, $modalInstance, message, okButtonTextKey) {
        $scope.message = message;
        $scope.okButtonTextKey = okButtonTextKey;
        $scope.OK = function () {
            $modalInstance.close();
        }
    });
