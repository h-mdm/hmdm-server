// Localization completed
angular.module('headwind-kiosk')
    .factory('authService', function ($cookies, serverAuthService) {
        var user;
        if ($cookies.get('user')) {
            user = JSON.parse($cookies.get('user'));
        }

        return {
            login: function (login, password, successCallback) {
                serverAuthService.login({login: login, password: password}, function (response) {
                    if (response.status === "OK") {
                        user = response.data;
                        $cookies.put('user', JSON.stringify(user));
                    }

                    successCallback(response);
                });
            },

            hasPermission: function (permission) {
                if (user) {
                    if (user.userRole) {
                        if (user.userRole.superAdmin) {
                            return true;
                        } else {
                            if (user.userRole.permissions) {
                                return user.userRole.permissions.find(function (p) {
                                    return p.name === permission;
                                }) !== undefined;
                            }
                        }
                    }
                }

                return false;
            },

            mobileLogin: function (email, successCallback) {
                serverAuthService.mobileLogin({'email': email}, function (response) {
                    if (response.status === "OK") {
                        user = response.data;
                        $cookies.put('user', JSON.stringify(user));
                    }

                    successCallback(response);
                });
            },

            logout: function () {
                serverAuthService.logout();

                user = undefined;
                $cookies.remove('user');
            },

            update: function (newUser) {
                user = newUser;
                $cookies.put('user', JSON.stringify(newUser))
            },

            isLoggedIn: function () {
                return user !== undefined;
            },

            isSuperAdmin: function () {
                return (user && user.userRole.superAdmin);
            },

            getUserName: function () {
                return user ? user.name : undefined;
            },
            getUserLogin: function () {
                return user ? user.login : undefined;
            },
            getId: function () {
                return user ? user.id : undefined;
            },
            getUser: function () {
                var result = {};
                for (var p in user) {
                    if (user.hasOwnProperty(p)) {
                        result[p] = user[p];
                    }
                }

                return result;
            }
        }
    })
    .factory('serverAuthService', function ($resource) {
        return $resource('rest/public/auth/', {}, {
            login: {url: 'rest/public/auth/login', method: 'POST'},
            mobileLogin: {url: 'rest/public/auth/login/mobile', method: 'POST'},
            logout: {url: 'rest/public/auth/logout', method: 'POST'}
        });
    });