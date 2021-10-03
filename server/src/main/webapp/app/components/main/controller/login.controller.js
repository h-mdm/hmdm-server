<!-- Localization completed -->
angular.module('headwind-kiosk')
    .controller('LoginController', function ($scope, $state, $rootScope, $timeout, authService, localization, rebranding) {
        $scope.login = {};

        $scope.rebranding = null;
        rebranding.query(function(value) {
            $scope.rebranding = value;
            $scope.ieBrowserNotice2 = localization.localize('ie.browser.notice.2').replace('${appName}', $scope.rebranding.appName);
        });

        $scope.isIE = detectIE();

        var loginHandler = function (response) {
            if (response.status === 'OK') {
                $state.transitionTo('main');
                $rootScope.$emit('aero_USER_AUTHENTICATED');
            } else if (response.status === 'ERROR') {
                $scope.errorMessage = localization.localize('login.password.incorrect');
            }
        };

        $scope.onLogin = function () {
            authService.login($scope.login.username, md5($scope.login.password).toUpperCase(), loginHandler);
        };

        /**
         * detect IE
         * returns version of IE or false, if browser is not Internet Explorer
         */
        function detectIE() {
            var ua = window.navigator.userAgent;

            var msie = ua.indexOf('MSIE ');
            if (msie > 0) {
                // IE 10 or older => return version number
                return parseInt(ua.substring(msie + 5, ua.indexOf('.', msie)), 10);
            }

            var trident = ua.indexOf('Trident/');
            if (trident > 0) {
                // IE 11 => return version number
                var rv = ua.indexOf('rv:');
                return parseInt(ua.substring(rv + 3, ua.indexOf('.', rv)), 10);
            }

            var edge = ua.indexOf('Edge/');
            if (edge > 0) {
                // Edge (IE 12+) => return version number
                return parseInt(ua.substring(edge + 5, ua.indexOf('.', edge)), 10);
            }

            // other browser
            return false;
        }
    });
