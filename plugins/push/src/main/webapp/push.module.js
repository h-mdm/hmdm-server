// Localization completed
angular.module('plugin-push', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-push', {
                url: "/" + 'plugin-push',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.push.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-push';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-push', e);
        }

        try {
            $stateProvider.state('plugin-settings-push', {
                url: "/" + 'plugin-settings-push',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.push.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-push'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-push', e);
        }
    })
    .factory('pluginPushService', function ($resource) {
        return $resource('', {}, {
            purgeOldMessages: {url: 'rest/plugins/push/private/purge/:days', method: 'GET'},
            getMessages: {url: 'rest/plugins/push/private/search', method: 'POST'},
            sendMessage: {url: 'rest/plugins/push/private/send', method: 'POST'},
            deleteMessage: {url: 'rest/plugins/push/:id', method: 'DELETE'},
            lookupDevices: {url: 'rest/private/devices/autocomplete', method: 'POST'},
        });
    })
    .factory('getDevicesService', ['pluginPushService', function(pluginPushService) {
        var getDeviceInfo = function( device ) {
            if ( device.info ) {
                try {
                    return JSON.parse( device.info );
                } catch ( e ) {}
            }

            return undefined;
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

        return {
            getDevices: function(val) {
                return pluginPushService.lookupDevices(val).$promise.then(function(response) {
                    if (response.status === 'OK') {
                        return response.data.map(function (device) {
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
            },
            deviceLookupFormatter: function(v) {
                if (v) {
                    var pos = v.indexOf('/');
                    if (pos > -1) {
                        return v.substr(0, pos).trim();
                    }
                }
                return v;
            }
        }
    }])
    .controller('PluginPushTabController', function ($scope, $rootScope, $window, $location, $modal, $timeout, $interval,
                                                          pluginPushService, getDevicesService, confirmModal,
                                                          authService, localization) {

        $scope.hasPermission = authService.hasPermission;

        $rootScope.settingsTabActive = false;
        $rootScope.pluginsTabActive = true;

        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            totalItems: 0,
            deviceFilter: '',
            messageFilter: '',
            dateFrom: null,
            dateTo: null,
            sortValue: 'createTime'
        };

        $scope.$watch('paging.pageNum', function() {
            $window.scrollTo(0, 0);
        });

        var deviceNumber = ($location.search()).deviceNumber;
        if (deviceNumber) {
            $scope.paging.deviceFilter = deviceNumber;
        }
        $scope.dateFormat = localization.localize('format.date.plugin.push.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.push.createTime');
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
        };

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.getDevices = getDevicesService.getDevices;
        $scope.deviceLookupFormatter = getDevicesService.deviceLookupFormatter;

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
                    $scope.errorMessage = localization.localize('error.plugin.push.date.range.invalid');
                    return;
                }
            }

            $scope.paging.pageNum = 1;
            loadData();
        };

        $scope.$watch('paging.pageNum', function () {
            loadData();
        });

        $scope.newMessage = function (message) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/push/views/push.modal.html',
                controller: 'NewPushMessageController',
                resolve: {
                    message: function () {
                        return message;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.successMessage = localization.localize('plugin.push.send.success');
                $timeout(function() { $scope.successMessage = undefined;}, 5000);
                $scope.search();
            });
        };

        var loading = false;
        var loadData = function () {
            $scope.errorMessage = undefined;
            
            if (loading) {
                console.log("Skipping query for message list since a previous request is pending");
                return;
            }

            loading = true;

            var request = {};
            for (var p in $scope.paging) {
                if ($scope.paging.hasOwnProperty(p)) {
                    request[p] = $scope.paging[p];
                }
            }

            request.deviceFilter = getDevicesService.deviceLookupFormatter(request.deviceFilter);

            pluginPushService.getMessages(request, function (response) {
                loading = false;
                if (response.status === 'OK') {
                    $scope.messages = response.data.items;
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
    })
    .controller('PluginPushSettingsController', function ($scope, $rootScope, $modal,
                                                               confirmModal, localization, pluginPushService) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.settings = {
            "pushPurgePeriod": 7
        };

        $scope.purge = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;

            if (isNaN($scope.settings.pushPurgePeriod)) {
                $scope.errorMessage = localization.localize('plugin.push.settings.enter.number');
            }

            pluginPushService.purgeOldMessages({"days": $scope.settings.pushPurgePeriod}, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('plugin.push.settings.message.purge.success');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };
    })
    .controller('NewPushMessageController', function ($scope, $rootScope, $modalInstance, configurationService, groupService,
                                                  confirmModal, localization, pluginPushService, getDevicesService) {

        $scope.sending = false;

        $scope.getDevices = getDevicesService.getDevices;
        $scope.deviceLookupFormatter = getDevicesService.deviceLookupFormatter;

        groupService.getAllGroups(function (response) {
            $scope.groups = response.data;
        });

        configurationService.getAllConfigurations(function (response) {
            $scope.configurations = response.data;
        });

        $scope.message = {
            scope: "device",
            deviceNumber: "",
            groupId: "",
            configurationId: "",
            messageType: "configUpdated",
            customMessageType: "",
            payload: ""
        };

        var samplePayloads = {
            configUpdated: "",
            runApp: "{pkg: \"app.package.id\"}",
            uninstallApp: "{pkg: \"app.package.id\"}",
            deleteFile: "{path: \"/path/to/file\"}",
            deleteDir: "{path: \"/path/to/dir\"}",
            purgeDir: "{path: \"/path/to/dir\", recursive: \"1\"}",
            permissiveMode: "",
            "(custom)": ""
        };

        $scope.typeChanged = function() {
            $scope.message.payload = samplePayloads[$scope.message.messageType];
        };

        $scope.send = function () {
            $scope.errorMessage = undefined;

            if ($scope.message.scope === 'device' && $scope.message.deviceNumber.trim() === '') {
                $scope.errorMessage = localization.localize('plugin.push.error.empty.device');
                return;
            }

            if ($scope.message.scope === 'group' && !$scope.message.groupId) {
                $scope.errorMessage = localization.localize('plugin.push.error.empty.group');
                return;
            }

            if ($scope.message.scope === 'configuration' && !$scope.message.configurationId) {
                $scope.errorMessage = localization.localize('plugin.push.error.empty.configuration');
                return;
            }

            $scope.message.deviceNumber = getDevicesService.deviceLookupFormatter($scope.message.deviceNumber)

            $scope.sending = true;

            if ($scope.message.messageType == '(custom)') {
                if (!$scope.message.customMessageType) {
                    $scope.errorMessage = localization.localize('plugin.push.error.empty.messageType');
                    return;
                }
                $scope.message.messageType = $scope.message.customMessageType;
            }

            pluginPushService.sendMessage($scope.message).$promise.then(function(response) {
                $scope.sending = false;
                if (response.status === 'OK') {
                    $modalInstance.close();
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            }, function () {
                $scope.sending = false;
                $scope.errorMessage = localization.localizeServerResponse('error.request.failure');
            });
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };
    })
    .run(function ($rootScope, $location, localization) {
        $rootScope.$on('plugin-push-device-selected', function (event, device) {
            $location.url('/plugin-push?deviceNumber=' + device.number);
        })
        localization.loadPluginResourceBundles("push");
    });


