// Localization completed
angular.module('headwind-kiosk')
    .factory('progressDialog', function ($modal) {
        return {
            show: function (message) {
                var modalInstance = $modal.open({
                    templateUrl: 'app/shared/view/progress.html',
                    controller: 'ProgressDialogController',
                    resolve: {
                        message: function () {
                            return message;
                        }
                    }
                });

                return modalInstance;
            }
        }
    })
    .controller('ProgressDialogController', function ($scope, $modalInstance, message) {
        $scope.message = message;
        $scope.OK = function () {
            $modalInstance.close();
        }
    });
