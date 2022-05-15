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
angular.module('plugin-deviceinfo', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .constant('DEVICE_PARAMS', [
        'deviceBatteryLevel',
        'deviceBatteryCharging',
        'deviceIpAddress',
        'deviceKeyguard',
        'deviceRingVolume',
        'deviceWifiEnabled',
        'deviceMobileDataEnabled',
        'deviceGpsEnabled',
        'deviceBluetoothEnabled',
        'deviceUsbEnabled',
        'deviceMemoryTotal',
        'deviceMemoryAvailable'
    ])
    .constant('WIFI_PARAMS', [
        'wifiRssi',
        'wifiSsid',
        'wifiSecurity',
        'wifiState',
        'wifiIpAddress',
        'wifiTx',
        'wifiRx',
    ])
    .constant('GPS_PARAMS', [
        'gpsState',
        'gpsLat',
        'gpsLon',
        'gpsAlt',
        'gpsSpeed',
        'gpsCourse',
    ])
    .constant('MOBILE1_PARAMS', [
        'mobile1Rssi',
        'mobile1Carrier',
        'mobile1DataEnabled',
        'mobile1IpAddress',
        'mobile1State',
        'mobile1SimState',
        'mobile1Tx',
        'mobile1Rx',
    ])
    .constant('MOBILE2_PARAMS', [
        'mobile2Rssi',
        'mobile2Carrier',
        'mobile2DataEnabled',
        'mobile2IpAddress',
        'mobile2State',
        'mobile2SimState',
        'mobile2Tx',
        'mobile2Rx',
    ])
    .constant('splitDynamicInfoRecord', function (record) {
        var deviceData = {};
        var wifiData = {};
        var gpsData = {};
        var mobile1Data = {};
        var mobile2Data = {};
        for (var p in record) {
            if (record.hasOwnProperty(p)) {
                var target = undefined;

                if (p.startsWith("device")) {
                    target = deviceData;
                } else if (p.startsWith("wifi")) {
                    target = wifiData;
                } else if (p.startsWith("gps")) {
                    target = gpsData;
                } else if (p.startsWith("mobile1")) {
                    target = mobile1Data;
                } else if (p.startsWith("mobile2")) {
                    target = mobile2Data;
                }

                if (target) {
                    target[p] = {
                        value: record[p],
                        isBoolean: typeof record[p] === 'boolean',
                        displayed: !p.endsWith('DataIncluded'),
                        name: p,
                        isEnumerated: p.endsWith("State")
                    };
                }
            }
        }

        return {
            "deviceData": deviceData,
            "wifiData": wifiData,
            "gpsData": gpsData,
            "mobile1Data": mobile1Data,
            "mobile2Data": mobile2Data,
        }
    })
    .constant('parseDynamicInfoRecord', function (record) {
        var data = {};
        for (var p in record) {
            if (record.hasOwnProperty(p)) {
                data[p] = {
                    value: record[p],
                    isBoolean: typeof record[p] === 'boolean',
                    displayed: !p.endsWith('DataIncluded'),
                    name: p,
                    isEnumerated: p.endsWith("State")
                };
            }
        }

        return data;
    })
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-deviceinfo', {
                url: "/" + 'plugin-deviceinfo/{deviceNumber}',
                params:  {
                    deviceNumber: {
                        value: null,
                        squash: true
                    }
                },
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    // label: '{{"breadcrumb.plugin.deviceinfo.main" | localize}}', //label to show in breadcrumbs
                    label: '{{formData.deviceNumber}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-deviceinfo'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-deviceinfo', e);
        }

        try {
            $stateProvider.state('plugin-deviceinfo-dynamic', {
                url: "/" + 'plugin-deviceinfo-dynamic/{deviceNumber}',
                templateUrl: 'app/components/plugins/deviceinfo/views/dynamic.html',
                controller: 'PluginDeviceDynamicInfoController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.deviceinfo.dynamic.main" | localize}}', //label to show in breadcrumbs
                    parent: 'plugin-deviceinfo'
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-deviceinfo-dynamic', e);
        }

        try {
            $stateProvider.state('plugin-settings-deviceinfo', {
                url: "/" + 'plugin-settings-deviceinfo',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.deviceinfo.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-deviceinfo'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-deviceinfo', e);
        }
    })
    .factory('pluginDeviceInfoService', function ($resource) {
        return $resource('', {}, {
            getSettings: {url: 'rest/plugins/deviceinfo/deviceinfo-plugin-settings/private', method: 'GET'},
            saveSettings: {url: 'rest/plugins/deviceinfo/deviceinfo-plugin-settings/private', method: 'PUT'},
            getDeviceInfo: {url: 'rest/plugins/deviceinfo/deviceinfo/private/:deviceNumber', method: 'GET'},
            searchDynamicData: {url: 'rest/plugins/deviceinfo/deviceinfo/private/search/dynamic', method: 'POST'},
        });
    })
    .factory('pluginDeviceInfoExportService', function ($resource) {
        return $resource('', {}, {
            exportDynamicInfo: {
                url: 'rest/plugins/deviceinfo/deviceinfo/private/export',
                method: 'POST',
                responseType: 'arraybuffer',
                cache: false,
                transformResponse: function (data) {
                    return {
                        response: new Blob([data], {
                            // type: "text/plain"
                        })
                    };
                }
            },
        });
    })
    .controller('PluginDeviceInfoSettingsController', function ($scope, $rootScope, pluginDeviceInfoService, localization) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.settings = {};

        var intervalOptionValues = [15, 30, 60, 120, 360, 720, 1440];
        $scope.intervalOptions = intervalOptionValues.map(function (value, index) {
            return {value: value, label: localization.localize('plugin.deviceinfo.intervalMins.option.' + (index + 1))};
        });

        pluginDeviceInfoService.getSettings(function (response) {
            if (response.status === 'OK') {
                $scope.settings = response.data;
            } else {
                $scope.errorMessage = localization.localize('error.internal.server');
            }
        });

        $scope.save = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;

            pluginDeviceInfoService.saveSettings($scope.settings, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.plugin.deviceinfo.settings.saved');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        }
    })
    .controller('PluginDeviceInfoController', function ($scope, $rootScope, $location, $http, $state, $stateParams,
                                                        $interval,
                                                        pluginDeviceInfoService, localization, splitDynamicInfoRecord,
                                                        DEVICE_PARAMS, WIFI_PARAMS, GPS_PARAMS,
                                                        MOBILE1_PARAMS, MOBILE2_PARAMS) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        // var deviceNumber = ($location.search()).deviceNumber;
        var deviceNumber = $stateParams.deviceNumber;
        $scope.formData = {
            deviceNumber: deviceNumber
        };

        var clearMessages = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
        };

        $scope.dynamicDataDeviceFieldsOrder = [].concat(DEVICE_PARAMS);
        $scope.dynamicDataWifiFieldsOrder = [].concat(WIFI_PARAMS);
        $scope.dynamicDataGpsFieldsOrder = [].concat(GPS_PARAMS);
        $scope.dynamicDataMobile1FieldsOrder = [].concat(MOBILE1_PARAMS);
        $scope.dynamicDataMobile2FieldsOrder = [].concat(MOBILE2_PARAMS);

        var loadData = function () {
            pluginDeviceInfoService.getDeviceInfo({"deviceNumber": deviceLookupFormatter($scope.formData.deviceNumber)}, function (response) {
                if (response.status === 'OK') {
                    $scope.deviceInfo = response.data;
                    $scope.latestDynamicData = response.data.latestDynamicData;
                    if (response.data.latestDynamicData) {
                        var data = splitDynamicInfoRecord(response.data.latestDynamicData);

                        $scope.dynamicDeviceData = data.deviceData;
                        $scope.dynamicWifiData = data.wifiData;
                        $scope.dynamicGpsData = data.gpsData;
                        $scope.dynamicMobile1Data = data.mobile1Data;
                        $scope.dynamicMobile2Data = data.mobile2Data;
                    }
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };

        var resolveDeviceField = function (serverData, deviceInfoData) {
            if (serverData === deviceInfoData) {
                return serverData;
            } else if (serverData.length === 0 && deviceInfoData.length > 0) {
                return deviceInfoData;
            } else if (serverData.length > 0 && deviceInfoData.length === 0) {
                return serverData;
            } else {
                return deviceInfoData;
            }
        };

        var getDeviceInfo = function( device ) {
            if ( device.info ) {
                try {
                    return JSON.parse( device.info );
                } catch ( e ) {}
            }

            return undefined;
        };

        var deviceLookupFormatter = function (v) {
            if (v) {
                var pos = v.indexOf('/');
                if (pos > -1) {
                    return v.substr(0, pos).trim();
                }
            }
            return v;
        };

        $scope.deviceLookupFormatter = deviceLookupFormatter;

        $scope.searchDevices = function (val) {
            return $http.get('rest/plugins/deviceinfo/deviceinfo/private/search/device?limit=10&filter=' + val)
                .then(function (response) {
                    if (response.data.status === 'OK') {
                        return response.data.data.map(function (device) {
                            var deviceInfo = getDeviceInfo(device);
                            var serverIMEI = device.imei || '';
                            var deviceInfoIMEI = deviceInfo ? (deviceInfo.imei || '') : '';
                            var resolvedIMEI = resolveDeviceField(serverIMEI, deviceInfoIMEI);

                            return device.name + (resolvedIMEI.length > 0 ? " / " + resolvedIMEI : "");
                        });
                    } else {
                        return [];
                    }
                });
        };

        $scope.search = function () {
            clearMessages();
            loadData();
        };

        $scope.viewDynamicData = function () {
            $state.transitionTo('plugin-deviceinfo-dynamic', {deviceNumber: $scope.deviceInfo.deviceNumber});
        };

        $scope.formatMultiLine = function (text) {
            if (!text) {
                return text;
            } else {
                return text.replace(/\n/g, "<br/>");
            }
        };

        if (deviceNumber) {
            loadData();
        }

        const updateInterval = $interval(function () {
            if ($scope.formData.deviceNumber) {
                loadData();
            }
        }, 60 * 1000);
        $scope.$on('$destroy', function () {
            $interval.cancel(updateInterval);
        });
    })
    .controller('PluginDeviceDynamicInfoController', function ( $scope, $stateParams, $window, $interval,
                                                                pluginDeviceInfoService,
                                                                localization, parseDynamicInfoRecord, spinnerService,
                                                                alertService, pluginDeviceInfoExportService,
                                                                DEVICE_PARAMS, WIFI_PARAMS, GPS_PARAMS,
                                                                MOBILE1_PARAMS, MOBILE2_PARAMS) {
        var clearMessages = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;
        };

        var prepareRequestToServer = function () {
            var request = copyFormData();

            delete request.totalItems;

            if (request.useFixedInterval) {
                delete request.timeFrom;
                delete request.timeTo;
                delete request.dateFrom;
                delete request.dateTo;
            } else {
                var from = new Date(request.dateFrom.getTime());
                from.setHours(request.timeFrom.getHours());
                from.setMinutes(request.timeFrom.getMinutes());
                from.setSeconds(0, 0);

                request.dateFrom = from;
                delete request.timeFrom;

                var to = new Date(request.dateTo.getTime());
                to.setHours(request.timeTo.getHours());
                to.setMinutes(request.timeTo.getMinutes());
                to.setSeconds(59, 999);

                request.dateTo = to;
                delete request.timeTo;
            }

            return request;
        };

        var loading = false;

        var loadData = function () {
            if (loading) {
                console.log("Skipping to query for list of device dynamic info dynamic since a previous request is pending");
                return;
            }

            clearMessages();

            loading = true;

            spinnerService.show('spinner2');

            var request = prepareRequestToServer();

            pluginDeviceInfoService.searchDynamicData(request, function (response) {
                loading = false;
                spinnerService.close('spinner2');
                if (response.status === 'OK') {
                    $scope.data = response.data.items;
                    $scope.formData.totalItems = response.data.totalItemsCount;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                loading = false;
                spinnerService.close('spinner2');
                $scope.errorMessage = localization.localize("error.request.failure");
            });
        };

        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $scope.parseDynamicInfoRecord = parseDynamicInfoRecord;

        $scope.deviceFields = [].concat(DEVICE_PARAMS);
        $scope.wifiFields = [].concat(WIFI_PARAMS);
        $scope.gpsFields = [].concat(GPS_PARAMS);
        $scope.mobile1Fields = [].concat(MOBILE1_PARAMS);
        $scope.mobile2Fields = [].concat(MOBILE2_PARAMS);
        $scope.allFields = [].concat(DEVICE_PARAMS).concat(WIFI_PARAMS).concat(GPS_PARAMS).concat(MOBILE1_PARAMS).concat(MOBILE2_PARAMS);

        $scope.dateFormat = localization.localize('format.date.plugin.deviceinfo.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.deviceinfo.createTime');
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
        };

        var defaultFormData = {
            deviceNumber: $stateParams.deviceNumber,
            useFixedInterval: true,
            fixedInterval: 24 * 3600,
            dateFrom: new Date(),
            dateTo: new Date(),
            timeFrom: new Date(),
            timeTo: new Date(),
            pageSize: 50,
            pageNum: 1
        };

        var storageFormDataAttrName = 'hmdm-plugin-deviceinfo-formData';
        var storedFormData = $window.localStorage.getItem(storageFormDataAttrName);
        if (storedFormData) {
            try {
                var formData = JSON.parse(storedFormData);
                formData.deviceNumber = $stateParams.deviceNumber;
                formData.fixedInterval = parseInt(formData.fixedInterval);
                if (formData.dateFrom) {
                    formData.dateFrom = new Date(formData.dateFrom);
                } else {
                    formData.dateFrom = new Date();
                }
                if (formData.dateTo) {
                    formData.dateTo = new Date(formData.dateTo);
                } else {
                    formData.dateTo = new Date();
                }
                if (formData.timeFrom) {
                    formData.timeFrom = new Date(formData.timeFrom);
                } else {
                    formData.timeFrom = new Date();
                }
                if (formData.timeTo) {
                    formData.timeTo = new Date(formData.timeTo);
                } else {
                    formData.timeTo = new Date();
                }
                formData.pageSize = 50;
                formData.pageNum = 1;

                $scope.formData = formData;
            } catch (e) {
                $scope.formData = defaultFormData;
            }
        } else {
            $scope.formData = defaultFormData;
        }

        var defaultFieldsSelection = {
            "deviceBatteryCharging": true,
            "wifiSsid": true,
            "wifiState": true,
            "wifiRssi": true,
            "mobile1State": true,
            "mobile1Rssi": true,
        };
        var storageSelectionAttrName = 'hmdm-plugin-deviceinfo-fieldsSelection';
        var storedSelection = $window.localStorage.getItem(storageSelectionAttrName);
        if (storedSelection) {
            try {
                $scope.fieldsSelection = JSON.parse(storedSelection);
            } catch (e) {
                $scope.fieldsSelection = defaultFieldsSelection;
            }
        } else {
            $scope.fieldsSelection = defaultFieldsSelection;
        }

        var defaultCollapseState = {
            main: true,
            device: false,
            wifi: false,
            gps: false,
            mobile1: false,
            mobile2: false,
        };
        var storageCollapseAttrName = 'hmdm-plugin-deviceinfo-collapseState';
        var storedCollapseState = $window.localStorage.getItem(storageCollapseAttrName);
        if (storedCollapseState) {
            try {
                $scope.collapseState = JSON.parse(storedCollapseState);
            } catch (e) {
                $scope.collapseState = defaultCollapseState;
            }
        } else {
            $scope.collapseState = defaultCollapseState;
        }

        var copyFormData = function () {
            var request = {};
            for (var p in $scope.formData) {
                if ($scope.formData.hasOwnProperty(p)) {
                    request[p] = $scope.formData[p];
                }
            }

            return request;
        };

        var saveFormData = function () {
            var copy = copyFormData();
            delete copy.pageSize;
            delete copy.pageNum;
            delete copy.totalItems;
            delete copy.deviceNumber;

            $window.localStorage.setItem(storageFormDataAttrName, JSON.stringify(copy));
        };

        $scope.fieldsSelectionChanged = function () {
            $window.localStorage.setItem(storageSelectionAttrName, JSON.stringify($scope.fieldsSelection));
        };

        $scope.fixedIntervalSelected = function () {
            $scope.formData.useFixedInterval = $scope.formData.fixedInterval > 0;
            if ($scope.formData.useFixedInterval) {
                $scope.formData.dateFrom = new Date();
                $scope.formData.dateTo = new Date();
                $scope.formData.timeFrom = new Date();
                $scope.formData.timeTo = new Date();
            }
            saveFormData();
        };

        $scope.timeParamsChanged = function () {
            saveFormData();
        };

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
            clearMessages();

            if ($scope.formData.dateFrom && $scope.formData.dateTo) {
                if ($scope.formData.dateFrom > $scope.formData.dateTo) {
                    $scope.errorMessage = localization.localize('error.plugin.deviceinfo.date.range.invalid');
                    return;
                }
            }

            $scope.formData.pageNum = 1;
            loadData();
        };

        $scope.toggleParamsVisibility = function (type) {
            $scope.collapseState[type] = !$scope.collapseState[type];
            $window.localStorage.setItem(storageCollapseAttrName, JSON.stringify($scope.collapseState));
        };

        $scope.$watch('formData.pageNum', function () {
            $window.scrollTo(0, 0);
            loadData();
        });

        $scope.doExport = function () {
            clearMessages();
            $scope.loading = true;
            $scope.successMessage = localization.localize('plugin.deviceinfo.exporting');

            var exportRequest = prepareRequestToServer();
            exportRequest.locale = localization.getLocale();
            exportRequest.fields = [];
            for (var p in $scope.fieldsSelection) {
                if ($scope.fieldsSelection.hasOwnProperty(p)) {
                    if ($scope.fieldsSelection[p] === true) {
                        exportRequest.fields.push(p);
                    }
                }
            }

            pluginDeviceInfoExportService.exportDynamicInfo(exportRequest, function (data) {
                $scope.loading = false;
                clearMessages();

                var downloadableBlob = URL.createObjectURL(data.response);

                var link = document.createElement('a');
                link.href = downloadableBlob;
                link.download = $scope.formData.deviceNumber + '.csv';

                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }, function (response) {
                $scope.loading = false;
                clearMessages();
                alertService.onRequestFailure(response);
            });
        };


        loadData();


        const updateInterval = $interval(function () {
            loadData();
        }, 60 * 1000);
        $scope.$on('$destroy', function () {
            $interval.cancel(updateInterval);
        });

    })
    .run(function ($rootScope, $location, localization) {
        $rootScope.$on('plugin-deviceinfo-device-selected', function (event, device) {
            $location.url('/plugin-deviceinfo/' + device.number);
        });
        localization.loadPluginResourceBundles("deviceinfo");
    })
;


