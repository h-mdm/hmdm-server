// Localization completed
angular.module('headwind-kiosk')
    .factory('localization', function (settingsService, authService, getBrowserLanguage, ENGLISH, localizeText) {

        var loadUserLangSettings = function (scope) {
            settingsService.getSettings(function (response) {
                if (response.status === 'OK') {
                    if (response.data) {
                        var settings = response.data;
                        if (settings.useDefaultLanguage) {
                            locale = getBrowserLanguage();
                        } else if (settings.language) {
                            locale = settings.language;
                        } else {
                            locale = ENGLISH;
                        }
                        if (scope) {
                            scope.$emit('aero_LOCALE_CHANGED');
                        }
                    }
                }
            });
        };

        // Determine
        var locale = getBrowserLanguage();
        if (authService.isLoggedIn()) {
            loadUserLangSettings();
        }

        // Find the translations missing in EN bundle
        // localizationObject = document.localization;
        // for ( var prop in localizationObject[ 'ru_RU' ] ) {
        //     if ( !localizationObject[ 'en_US' ] ) {
        //         console.log( prop, ' is missing in en_US' );
        //     }
        // }

        return {
            localize: function (key) {
                return localizeText(locale, key);
            },
            localizeServerResponse: function (response) {
                var key = response.message;
                var value = document.localization[locale][key];
                if (value) {
                    if (response.data) {
                        for (var p in response.data) {
                            if (response.data.hasOwnProperty(p)) {
                                value = value.replace('${' + p + '}', response.data[p]);
                            }
                        }
                    }

                    return value;
                } else {
                    console.error('Message key ', key, ' is missing from I18N resource bundle for locale ', locale);
                    return document.localization[locale]['error.internal.server'];
                }
            },
            getLocale: function () {
                return locale;
            },
            onLangSettingsChange: function (newSettings, scope) {
                if (newSettings.useDefaultLanguage) {
                    locale = getBrowserLanguage();
                } else {
                    locale = newSettings.language;
                }
                scope.$emit('aero_LOCALE_CHANGED');
            },
            onLogin: function (scope) {
                loadUserLangSettings(scope);
            }
        }
    })
    .directive('localized', function (localization) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.html(localization.localize(element.html()));
            }
        }
    })
    .directive('localizedChangeTracking', function (localization) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                var html = element.html();
                var destroyScopeHandler = $scope.$root.$on('aero_LOCALE_CHANGED', function () {
                    element.html(localization.localize(html));
                });
                $scope.$on('$destroy', function () {
                    destroyScopeHandler();
                });
                element.html(localization.localize(html));
            }
        }
    })
    .directive('localizedPlaceholder', function (localization) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.attr('placeholder', localization.localize(element.attr('localized-placeholder')));
            }
        }
    })
    .directive('localizedTitle', function (localization) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.attr('title', localization.localize(element.attr('localized-title')));
            }
        }
    })
    .directive('localizedAlt', function (localization) {
        return {
            restrict: 'A',
            link: function ($scope, element, attrs) {
                element.attr('alt', localization.localize(element.attr('localized-alt')));
            }
        }
    })
    .filter('localize', function (localization) {
        return function (key) {
            return localization.localize(key);
        };
    })
;

