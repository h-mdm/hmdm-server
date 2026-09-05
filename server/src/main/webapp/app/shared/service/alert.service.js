// Localization completed
angular.module('headwind-kiosk')
    .factory('alertService', function (
        $modal,
        $rootScope,
        $timeout,
        $compile,
        localization
    ) {

        var toastId = 0;
        var container = null;


        /*
         * ---------------------------------------------------------
         * Create notification container dynamically
         * ---------------------------------------------------------
         */
        var createContainer = function () {

            if (container && container.length) {
                return container;
            }

            var element = angular.element(
                '<div class="notification-container"></div>'
            );

            angular.element(document.body).append(element);

            /*
             * Compile the dynamically created HTML so Angular
             * directives such as ng-repeat/ng-click work.
             */
            $compile(element)($rootScope);

            container = element;

            return container;
        };


        /*
         * ---------------------------------------------------------
         * Show old-style modal alert
         * ---------------------------------------------------------
         */
        var showAlert = function (
            message,
            callback,
            okButtonTextKey
        ) {

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
                        }

                        return 'button.close';
                    }
                }
            });


            modalInstance.result.then(function () {

                if (callback) {
                    callback();
                }

            });


            return modalInstance;
        };


        /*
         * ---------------------------------------------------------
         * Remove toast
         * ---------------------------------------------------------
         */
        var removeToast = function (toast) {

            if (!toast) {
                return;
            }

            toast.visible = false;

            $timeout(function () {

                var index =
                    $rootScope.notifications.indexOf(toast);

                if (index !== -1) {

                    $rootScope.notifications.splice(
                        index,
                        1
                    );
                }

            }, 250);
        };


        /*
         * ---------------------------------------------------------
         * Show toast
         * ---------------------------------------------------------
         */
        var showToast = function (
            message,
            type,
            duration
        ) {

            createContainer();

            if (!$rootScope.notifications) {
                $rootScope.notifications = [];
            }


            var toast = {

                id: ++toastId,

                message: message,

                type: type || 'info',

                visible: true
            };


            $rootScope.notifications.push(toast);


            /*
             * Default: 4 seconds
             *
             * duration = 0 means don't automatically dismiss.
             */
            if (duration === undefined || duration === null) {
                duration = 4000;
            }


            if (duration > 0) {

                toast.timeout = $timeout(
                    function () {

                        removeToast(toast);

                    },
                    duration
                );
            }


            return toast;
        };


        /*
         * ---------------------------------------------------------
         * Public service
         * ---------------------------------------------------------
         */
        return {

            /*
             * Existing modal API
             */
            showAlertMessage: function (
                message,
                callback,
                okButtonTextKey
            ) {

                return showAlert(
                    message,
                    callback,
                    okButtonTextKey
                );
            },


            /*
             * Existing request failure API
             */
            onRequestFailure: function (response) {

                console.error(
                    "Error when sending request to server",
                    response
                );

                return showAlert(
                    localization.localize(
                        'error.request.failure'
                    )
                );
            },


            /*
             * Modern notifications
             */
            success: function (
                message,
                duration
            ) {

                return showToast(
                    message,
                    'success',
                    duration
                );
            },


            error: function (
                message,
                duration
            ) {

                return showToast(
                    message,
                    'error',
                    duration
                );
            },


            warning: function (
                message,
                duration
            ) {

                return showToast(
                    message,
                    'warning',
                    duration
                );
            },


            info: function (
                message,
                duration
            ) {

                return showToast(
                    message,
                    'info',
                    duration
                );
            },


            /*
             * Manually dismiss one notification
             */
            dismiss: function (toast) {

                if (toast && toast.timeout) {

                    $timeout.cancel(
                        toast.timeout
                    );
                }

                removeToast(toast);
            },


            /*
             * Remove all notifications
             */
            clear: function () {

                if (!$rootScope.notifications) {
                    return;
                }


                angular.forEach(
                    $rootScope.notifications,
                    function (toast) {

                        if (toast.timeout) {

                            $timeout.cancel(
                                toast.timeout
                            );
                        }

                        toast.visible = false;
                    }
                );


                $timeout(function () {

                    $rootScope.notifications.length = 0;

                }, 250);
            }
        };
    })


    /*
     * -------------------------------------------------------------
     * Existing modal AlertController
     * -------------------------------------------------------------
     */
    .controller(
        'AlertController',
        function (
            $scope,
            $modalInstance,
            message,
            okButtonTextKey
        ) {

            $scope.message = message;

            $scope.okButtonTextKey =
                okButtonTextKey;


            $scope.OK = function () {

                $modalInstance.close();

            };
        }
    );
// angular.module('headwind-kiosk')
//     .factory('alertService', function ($modal, localization) {

//         var showAlert = function (message, callback, okButtonTextKey) {
//             var modalInstance = $modal.open({
//                 templateUrl: 'app/shared/view/alert.html',
//                 controller: 'AlertController',
//                 resolve: {
//                     message: function () {
//                         return message;
//                     },
//                     okButtonTextKey: function () {
//                         if (okButtonTextKey) {
//                             return okButtonTextKey;
//                         } else {
//                             return 'button.close';
//                         }
//                     }
//                 }
//             });

//             modalInstance.result.then(function () {
//                 if (callback) callback();
//             });

//             return modalInstance;
//         };

//         return {
//             showAlertMessage: function (message, callback, okButtonTextKey) {
//                 return showAlert(message, callback, okButtonTextKey);
//             },
//             onRequestFailure: function (response) {
//                 console.error("Error when sending request to server", response);
//                 return showAlert(localization.localize('error.request.failure'));
//             }
//         }
//     })
//     .controller('AlertController', function ($scope, $modalInstance, message, okButtonTextKey) {
//         $scope.message = message;
//         $scope.okButtonTextKey = okButtonTextKey;
//         $scope.OK = function () {
//             $modalInstance.close();
//         }
//     });
