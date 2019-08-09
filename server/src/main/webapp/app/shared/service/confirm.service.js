// Localization completed
angular.module('headwind-kiosk')
    .factory('confirmModal', function ($modal) {
        return {
            getUserConfirmation: function (message, callback) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/shared/view/confirm.html',
                    controller: 'ConfirmController',
                    resolve: {
                        message: function () {
                            return message;
                        }
                    }
                });

                modalInstance.result.then(function () {
                    if (callback) callback();
                });
            }
        }
    })
    .controller('ConfirmController', function ($scope, $modalInstance, message) {
        $scope.message = message;

        $scope.OK = function () {
            $modalInstance.close();
        }
        $scope.cancel = function () {
            $modalInstance.dismiss()
        }
    });
