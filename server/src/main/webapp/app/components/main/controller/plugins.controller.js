/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// Localization completed
angular.module('headwind-kiosk')
    .controller('PluginsTabController', function ($scope, $rootScope, $timeout,
                                                   localization, pluginService) {
        $scope.loading = false;

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        var clearMessages = function () {
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;
        };

        var loadData = function () {
            clearMessages();

            $scope.loading = true;
            pluginService.getActivePlugins(function (response) {
                if (response.status === 'OK') {
                    var plugins = response.data;
                    var pluginSelection = {};

                    plugins.forEach(function (plugin) {
                        plugin.localizedName = localization.localize(plugin.nameLocalizationKey);
                        pluginSelection[plugin.id] = false;
                    });

                    plugins.sort(function (a, b) {
                        var t1 = a.localizedName;
                        var t2 = b.localizedName;

                        if (t1 === t2) {
                            return 0;
                        } else if (t1 < t2) {
                            return -1;
                        } else {
                            return 1;
                        }
                    });

                    pluginService.getAvailablePlugins(function (response) {
                        $scope.loading = false;

                        if (response.status === 'OK') {
                            response.data.forEach(function (plugin) {
                                pluginSelection[plugin.id] = true;
                            });

                            $scope.plugins = plugins;
                            $scope.pluginSelection = pluginSelection;

                        } else {
                            $scope.errorMessage = localization.localizeServerResponse(response);
                        }
                    }, function () {
                        $scope.loading = false;
                        $scope.errorMessage = localization.localize("error.request.failure");
                    });
                } else {
                    $scope.loading = false;
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.loading = false;
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };

        $scope.save = function () {
            clearMessages();

            var request = [];
            for (var p in $scope.pluginSelection) {
                if ($scope.pluginSelection.hasOwnProperty(p)) {
                    if ($scope.pluginSelection[p] === false) {
                        request.push(p);
                    }
                }
            }

            $scope.loading = true;
            pluginService.disablePlugins(request, function (response) {
                $scope.loading = false;
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.plugins.disabled');
                    $rootScope.$broadcast('aero_PLUGINS_UPDATED');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.loading = false;
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };

        loadData();
    });