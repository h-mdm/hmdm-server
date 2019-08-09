// Localization completed
angular.module('headwind-kiosk')
    .controller('TabController', function ($scope, $rootScope, $timeout, userService, authService, openTab, $state,
                                           pluginService, localization) {

        $scope.localization = localization;

        pluginService.getAvailablePlugins(function (response) {
            if (response.status === 'OK') {
                if (response.data) {
                    // Plugins available for Functions tab
                    $scope.functionsPlugins = response.data.filter(function (plugin) {
                        return plugin.functionsViewTemplate !== undefined && plugin.functionsViewTemplate !== null;
                    });
                    $scope.functionsPlugins.forEach(function (plugin) {
                        let ID = 'plugin-' + plugin.identifier;
                        routes[ID] = ID;
                    });

                    // Plugins available for Setings tab
                    $scope.settingsPlugins = response.data.filter(function (plugin) {
                        return plugin.settingsViewTemplate !== undefined && plugin.settingsViewTemplate !== null;
                    });
                    $scope.settingsPlugins.forEach(function (plugin) {
                        let ID = 'plugin-settings-' + plugin.identifier;
                        routes[ID] = ID;
                    });
                }
            } else {
                $scope.functionsPlugins = [];
                $scope.settingsPlugins = [];
            }
        });

        $scope.currentUser = {};

        $scope.hasPermission = authService.hasPermission;

        $scope.activeTab = openTab;

        $scope.act = {};
        $scope.act[openTab] = true;

        $scope.functionsPlugins = [];
        $scope.settingsPlugins = [];

        var routes = {
            DEVICES: 'main',
            APPS: 'applications',
            CONFS: 'configurations',
            FILES: 'files',
            DESIGN: 'designSettings',
            COMMON: 'commonSettings',
            USERS: 'users',
            GROUPS: 'groups',
            LANG: 'langSettings'

        };

        $scope.openTab = function (tabName) {
            if (tabName === $scope.activeTab) {
                return;
            }
            if (routes[tabName]) {
                $state.transitionTo(routes[tabName]);
            }
        };

        userService.getCurrent(function (response) {
            if (response.data) {
                $scope.currentUser = response.data;
            }
        });
    });
