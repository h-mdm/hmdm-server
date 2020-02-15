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
angular.module('plugin-audit', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-audit', {
                url: "/" + 'plugin-audit',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.audit.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-audit';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-audit', e);
        }
    })
    .factory('pluginAuditService', function ($resource) {
        return $resource('', {}, {
            getLogs: {url: 'rest/plugins/audit/private/log/search', method: 'POST'},
        });
    })
    .controller('PluginAuditTabController', function ($scope, $rootScope, $window, $location, $interval, $http,
                                                      pluginAuditService, confirmModal, authService, localization) {

        $scope.hasPermission = authService.hasPermission;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            totalItems: 0,
            messageFilter: '',
            dateFrom: null,
            dateTo: null,
        };

        $scope.$watch('paging.pageNum', function() {
            $window.scrollTo(0, 0);
        });

        $scope.dateFormat = localization.localize('format.date.plugin.audit.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.audit.createTime');
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
        };

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.openDateCalendar = function( $event, isStartDate ) {
            $event.preventDefault();
            $event.stopPropagation();

            if ( isStartDate ) {
                $scope.openDatePickers.dateFrom = true;
            } else {
                $scope.openDatePickers.dateTo = true;
            }
        };

        $scope.search = function () {
            $scope.errorMessage = undefined;

            if ($scope.paging.dateFrom && $scope.paging.dateTo) {
                if ($scope.paging.dateFrom > $scope.paging.dateTo) {
                    $scope.errorMessage = localization.localize('error.plugin.audit.date.range.invalid');
                    return;
                }
            }

            $scope.paging.pageNum = 1;
            loadData();
        };

        $scope.$watch('paging.pageNum', function () {
            loadData();
        });

        var loading = false;
        var loadData = function () {
            $scope.errorMessage = undefined;

            if (loading) {
                console.log("Skipping to query for list of log record since a previous request is pending");
                return;
            }

            loading = true;

            var request = {};
            for (var p in $scope.paging) {
                if ($scope.paging.hasOwnProperty(p)) {
                    request[p] = $scope.paging[p];
                }
            }

            pluginAuditService.getLogs(request, function (response) {
                loading = false;
                if (response.status === 'OK') {
                    $scope.logs = response.data.items;
                    $scope.paging.totalItems = response.data.totalItemsCount;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                loading = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            })
        };

        loadData();

        //
        // var autoUpdateInterval = $interval(loadData, 15000);
        // $scope.$on('$destroy', function () {
        //     if (autoUpdateInterval) $interval.cancel(autoUpdateInterval);
        // });

    })
    .run(function ($rootScope, $location, localization) {
        // $rootScope.$on('plugin-devicelog-device-selected', function (event, device) {
        //     $location.url('/plugin-devicelog?deviceNumber=' + device.number);
        // })
        localization.loadPluginResourceBundles("audit");
    });


