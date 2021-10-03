// Localization completed
angular.module('headwind-kiosk')
    .factory('rebranding', function ($cookies, localization, serverRebrandingService) {

        var defaultValue = {
            appName: localization.localize('app.name'),
            vendorName: localization.localize('app.vendor.name'),
            vendorLink: localization.localize('app.vendor.link')
        };

        return {
            query: function(callback) {
                var data;
                var cookieData = $cookies.get('rebranding');
                if (cookieData) {
                    data = JSON.parse(cookieData);
                    if (data.appName === "") {
                        // Empty strings are replaced by default values
                        data.appName = localization.localize('app.name');
                    }
                    if (data.vendorName === "") {
                        data.vendorName = localization.localize('app.vendor.name');
                    }
                    if (data.vendorLink === "") {
                        data.vendorLink = localization.localize('app.vendor.link');
                    }
                    callback(data);

                } else {
                    // No cookie data, need to request values from the server
                    serverRebrandingService.query({}, function (response) {
                        if (response.status === "OK") {
                            var value = response.data;
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
