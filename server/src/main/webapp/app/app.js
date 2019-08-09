angular.module('headwind-kiosk',
    ['ngResource', 'ngCookies', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ngAnimate',
        'lr.upload', 'colorpicker.module',
        'ui.mask', 'ncy-angular-breadcrumb', 'oc.lazyLoad', 'angularjs-dropdown-multiselect'])
    .constant("SUPPORTED_LANGUAGES", {
        'en': 'en_US',
        'en_US': 'en_US',
        'en_UK': 'en_US',
        'en_GB': 'en_US',
        'en_IN': 'en_US',
        'ru': "ru_RU",
        'ru_RU': "ru_RU",
    })
    .constant("APP_VERSION", "3.05.0006") // Update this value on each commit
    .constant("ENGLISH", "en_US")
    .provider('getBrowserLanguage', function (ENGLISH, SUPPORTED_LANGUAGES) {
        this.f = function () {
            var userLang = window.navigator.language || window.navigator.userLanguage;
            if (userLang) {
                userLang = userLang.replace('-', '_');
                if (SUPPORTED_LANGUAGES[userLang]) {
                    userLang = SUPPORTED_LANGUAGES[userLang];
                } else {
                    userLang = ENGLISH;
                }
            } else {
                userLang = ENGLISH;
            }

            return userLang;
        };

        this.$get = function() {
            return this.f;
        };

    })
    .constant("localizeText", function (locale, key) {
        var value = document.localization[locale][key];
        if (!value) {
            console.error('Message key ', key, ' is missing from I18N resource bundle for locale ', locale);
        }
        return value ? value : key;
    })
    .config(['$provide', function ($provide) {
        $provide.decorator('$state', ['$delegate', '$window',
            function ($delegate, $window) {
                var extended = {
                    goNewTab: function (stateName, params) {
                        $window.open(
                            document.location.origin + document.location.pathname + $delegate.href(stateName, params, {absolute: false}), '_blank');
                    }
                };
                angular.extend($delegate, extended);
                return $delegate;
            }]);
    }])
    .config(function ($stateProvider, $urlRouterProvider, getBrowserLanguageProvider, localizeText) {
        $urlRouterProvider.otherwise('/');

        let browserLanguage = getBrowserLanguageProvider.f();

        $stateProvider
            .state('qr', {
                url: '/qr/{code}',
                templateUrl: 'app/components/main/view/qr.html',
                controller: 'QRController'
            })
            .state('main', {
                url: '/',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.devices') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "DEVICES"}
                }

            })
            .state('applications', {
                url: '/applications',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.applications') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "APPS"}
                }
            })
            .state('appVersionsEditor', {
                url: '/application/{id}/versions',
                templateUrl: 'app/components/main/view/applicationVersions.html',
                controller: 'ApplicationVersionEditor',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.application.versions'), //label to show in breadcrumbs,
                    parent: 'applications',

                }
            })
            .state('configurations', {
                url: '/configurations',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.configurations') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "CONFS"}
                }
            })
            .state('files', {
                url: '/files',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.files') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "FILES"}
                }
            })
            .state('designSettings', {
                url: '/designSettings',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.default.design') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "DESIGN"}
                }
            })
            .state('commonSettings', {
                url: '/commonSettings',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.common.settings') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "COMMON"}
                }
            })
            .state('langSettings', {
                url: '/langSettings',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.language.settings') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "LANG"}
                }
            })
            .state('users', {
                url: '/users',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.users') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "USERS"}
                }
            })
            .state('groups', {
                url: '/groups',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.groups') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "GROUPS"}
                }
            })
            .state('configEditor', {
                url: '/configuration/{id}/{typical}',
                templateUrl: 'app/components/main/view/configuration.html',
                controller: 'ConfigurationEditorController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.config.details'), //label to show in breadcrumbs,
                    parent: 'configurations',

                }
            })
            .state('login', {
                url: '/login',
                templateUrl: 'app/components/main/view/login.html',
                controller: 'LoginController'
            })
            .state('profile', {
                url: '/profile',
                templateUrl: 'app/components/main/view/profile.html',
                controller: 'ProfileController'
            })
            .state('control-panel', {
                url: '/control-panel',
                templateUrl: 'app/components/control-panel/view/panel.html',
                controller: 'ControlPanelController'
            })
            // Plugins
            .state('plugin-photo', {
                url: '/plugin-photo',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: localizeText(browserLanguage, 'breadcrumb.plugin.photo.main') //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {return "plugin-photo"}
                }
            })
    })

    .config(function ($httpProvider) {
        $httpProvider.interceptors.push(function ($q, $injector) {
            return {
                'responseError': function (rejection) {
                    if (rejection.status === 403) {
                        $injector.get('authService').logout();
                        $injector.get('$state').transitionTo('login');

                        return new Promise(function () {
                        });
                    } else if (rejection.status !== 200 && rejection.status !== 204) {
                        var $body = angular.element(document.body);
                        var $rootScope = $body.scope().$root;
                        $rootScope.$broadcast('RELOAD_MESSAGE');
                    }

                    return $q.reject(rejection);
                }
            };
        });

        $httpProvider.defaults.cache = false;
        if (!$httpProvider.defaults.headers.get)
            $httpProvider.defaults.headers.get = {};

        $httpProvider.defaults.headers.get['If-Modified-Since'] = 'Thu, 01 Jan 1970 00:00:00 GMT';
        $httpProvider.defaults.headers.get['Cache-Control'] = 'no-cache';
    })

    .run(function ($rootScope, $state, $stateParams, authService, pluginService, $ocLazyLoad, localization, $window) {
        $rootScope.$state = $state;
        $rootScope.$stateParams = $stateParams;

        pluginService.getRegisteredPlugins(function (response) {
            if (response.status === 'OK') {
                if (response.data) {
                    response.data.forEach(function (plugin) {
                        try {
                            $ocLazyLoad.load(plugin.javascriptModuleFile);
                        } catch (e) {
                            console.error('Failed to load plugin module', e);
                        }
                    });
                }
            }
        });

        $window.document.title = localization.localize('app.title');

        $rootScope.$on('aero_LANGUAGE_SETTINGS_UPDATED', function (event, newSettings) {
            localization.onLangSettingsChange(newSettings, $rootScope);
        });

        $rootScope.$on('aero_LOCALE_CHANGED', function () {
            $window.document.title = localization.localize('app.title');
        });

        $rootScope.$on('aero_USER_AUTHENTICATED', function () {
            localization.onLogin($rootScope);
        });

        $rootScope.$on('$stateChangeStart',
            function (event, toState, toParams, fromState, fromParams) {
                if (toState.name !== 'password_recovery' && toState.name !== 'qr') {
                    if (!authService.isLoggedIn() && toState.name !== 'login') {
                        event.preventDefault();
                        $state.transitionTo('login');
                    }
                }

                if (authService.isLoggedIn() &&
                    (toState.name === 'login' || toState.name === 'password_recovery')) {
                    event.preventDefault();
                    $state.transitionTo('main');
                }
            });
    });
