(function () {
    angular
        .module('headwind-kiosk')
        .directive('sarshaSpinner', sarshaSpinner)
        .service('spinnerService', spinnerService)
        .factory('spinnerHttpInterceptor', spinnerHttpInterceptor);

    sarshaSpinner.$inject = ['spinnerService'];
    function sarshaSpinner(spinnerService) {
        return {
            restrict: 'E',
            scope: {
                name: '@'
            },
            transclude: true,
            template: [
                '<div class="sarsha-spinner-container" ng-if="active">',
                '<div class="sarsha-spinner">',
                '<div ng-transclude>',
                '<div class="spinner">',
                '<div class="rect1"></div>',
                '<div class="rect2"></div>',
                '<div class="rect3"></div>',
                '<div class="rect4"></div>',
                '<div class="rect5"></div>',
                '</div>',
                '</div>',
                '</div>',
                '</div>'
            ].join(" "),
            link: function (scope, elm, attrs) {
                scope.active = false;

                var parent = elm.parent();
                var parentPosition = parent.position;

                var spinnerContext = {
                    show: show,
                    close: close
                }

                if (!parentPosition || parentPosition === 'static' || parentPosition === '')
                    parent.css('position', 'relative');

                function show() {
                    scope.active = true;
                }

                function close() {
                    scope.active = false;
                }

                spinnerService.register(scope.name, spinnerContext);
            }
        }
    }

    function spinnerService() {
        var service = this;
        var spinners = {};

        service.show = show;
        service.close = close;
        service.showAll = showAll;
        service.closeAll = closeAll;
        service.register = register;
        service.unregister = unregister;

        return service;

        function show(name) {
            if (spinners[name])
                spinners[name].show()
        }

        function close(name) {
            if (spinners[name])
                spinners[name].close();
        }

        function showAll() {
            for (var name in spinners) {
                if (spinners[name])
                    spinners[name].show();
            }
        }

        function closeAll() {
            for (var name in spinners) {
                if (spinners[name])
                    spinners[name].close();
            }
        }

        function register(name, spinnerContext) {
            spinners[name] = spinnerContext;
        }

        function unregister(name) {
            spinners[name] = null;
        }
    }

    spinnerHttpInterceptor.$inject = ['spinnerService', '$q'];
    function spinnerHttpInterceptor(spinnerService, $q) {

        var activeSpinners = {};

        return {
            'request': function (config) {
                handleRequest(config);
                return config;
            },
            'requestError': function (rejection) {
                handleResponse(rejection.config);
                return $q.reject(rejection);
            },
            'response': function (response) {
                handleResponse(response.config);
                return response;
            },
            'responseError': function (rejection) {
                handleResponse(rejection.config);
                return $q.reject(rejection);
            }
        }

        function handleRequest(config) {
            var spinner = config.spinner,
                url = config.url;

            if (!spinner) {
                activeSpinners[url] = 'all';
                spinnerService.showAll();
                return;
            }

            activeSpinners[url] = spinner;

            if (Array.isArray(spinner)) {
                spinner.forEach(function (name) {
                    spinnerService.show(name);
                })
            } else {
                spinnerService.show(spinner);
            }

        }

        function handleResponse(config) {
            var url = config.url;
            var spinner = activeSpinners[url];

            if (spinner === 'all') {
                spinnerService.closeAll();
            } else if (Array.isArray(spinner)) {
                spinner.forEach(function (name) {
                    spinnerService.close(name);
                })
            } else {
                spinnerService.close(spinner);
            }

            activeSpinners[url] = null;
        }
    }
})();