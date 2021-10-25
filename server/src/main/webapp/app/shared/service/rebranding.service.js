// Localization completed
angular.module('headwind-kiosk')
    .factory('rebranding', function ($cookies, localization, serverRebrandingService) {

        var defaultValue = {
            appName: localization.localize('app.name'),
            vendorName: localization.localize('app.vendor.name'),
            vendorLink: localization.localize('app.vendor.link')
        };

        var fixEmptyValue = function(value) {
            if (value.appName === "") {
                // Empty strings are replaced by default values
                value.appName = localization.localize('app.name');
            }
            if (value.vendorName === "") {
                value.vendorName = localization.localize('app.vendor.name');
            }
            if (value.vendorLink === "") {
                value.vendorLink = localization.localize('app.vendor.link');
            }
            return value;
        };

        return {
            query: function(callback) {
                var data;
                var cookieData = $cookies.get('rebranding');
                if (cookieData) {
                    data = JSON.parse(cookieData);
                    data = fixEmptyValue(data);
                    callback(data);

                } else {
                    // No cookie data, need to request values from the server
                    serverRebrandingService.query({}, function (response) {
                        if (response.status === "OK") {
                            var value = response.data;
                            value = fixEmptyValue(value);
                            $cookies.put('rebranding', JSON.stringify(value));
                            callback(value);
                        } else {
                            callback(defaultValue);
                        }
                    }, function(error) {
                        callback(defaultValue);
                    });
                }
            }
        }
    })
    .factory('serverRebrandingService', function ($resource) {
        return $resource('', {}, {
            query: {url: 'rest/public/name', method: 'GET'}
        });
    });
