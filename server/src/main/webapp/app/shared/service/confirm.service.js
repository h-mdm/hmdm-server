// Localization completed
angular.module('headwind-kiosk')
    .factory('confirmModal', function ($modal) {
        return {
            getUserConfirmation: function (message, callback, okButtonTextKey) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/shared/view/confirm.html',
                    controller: 'ConfirmController',
                    resolve: {
                        message: function () {
                            return message;
                        },
                        okButtonTextKey: function () {
                            if (okButtonTextKey) {
                                return okButtonTextKey;
                            } else {
                                return 'button.yes';
                            }
                        }
                    }
                });

                modalInstance.result.then(function () {
                    if (callback) callback();
                });
            }
        }
    })
    .controller('ConfirmController', function ($scope, $modalInstance, message, okButtonTextKey) {
        $scope.message = message;
        $scope.okButtonTextKey = okButtonTextKey;

        $scope.OK = function () {
            $modalInstance.close();
        }
        $scope.cancel = function () {
            $modalInstance.dismiss()
        }
    });
