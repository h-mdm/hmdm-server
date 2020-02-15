// Localization completed
angular.module('plugin-messaging', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        try {
            $stateProvider.state('plugin-messaging', {
                url: "/" + 'plugin-messaging',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.messaging.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-messaging';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-messaging', e);
        }

        try {
            $stateProvider.state('plugin-settings-messaging', {
                url: "/" + 'plugin-settings-messaging',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.messaging.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-messaging'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-messaging', e);
        }
    })
    .factory('pluginMessagingService', function ($resource) {
        return $resource('', {}, {
            purgeOldMessages: {url: 'rest/plugins/messaging/private/purge/:days', method: 'GET'},
            getMessages: {url: 'rest/plugins/messaging/private/search', method: 'POST'},
            sendMessage: {url: 'rest/plugins/messaging/private/send', method: 'POST'},
            deleteMessage: {url: 'rest/plugins/messaging/:id', method: 'DELETE'},
            lookupDevices: {url: 'rest/private/devices/autocomplete', method: 'POST'},
        });
    })
    .factory('getDevicesService', ['pluginMessagingService', function(pluginMessagingService) {
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
                return pluginMessagingService.lookupDevices(val).$promise.then(function(response) {
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
    .filter('status', function() {
        return function(input) {
            switch(input) {
                case 0:
                    return 'plugin.messaging.status.sent';
                case 1:
                    return 'plugin.messaging.status.delivered';
                case 2:
                    return 'plugin.messaging.status.read';
            }
        };
    })
    .controller('PluginMessagingTabController', function ($scope, $rootScope, $window, $location, $modal, $timeout, $interval,
                                                          pluginMessagingService, getDevicesService, confirmModal,
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
            status: -1,
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
        $scope.dateFormat = localization.localize('format.date.plugin.messaging.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.messaging.createTime');
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
                    $scope.errorMessage = localization.localize('error.plugin.messaging.date.range.invalid');
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
                templateUrl: 'app/components/plugins/messaging/views/message.modal.html',
                controller: 'NewMessageController',
                resolve: {
                    message: function () {
                        return message;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.successMessage = localization.localize('plugin.messaging.send.success');
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

            pluginMessagingService.getMessages(request, function (response) {
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

        var autoUpdateInterval = $interval(loadData, 15000);
        $scope.$on('$destroy', function () {
            if (autoUpdateInterval) $interval.cancel(autoUpdateInterval);
        });
    })
    .controller('PluginMessagingSettingsController', function ($scope, $rootScope, $modal,
                                                               confirmModal, localization, pluginMessagingService) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.settings = {
            "messagingPurgePeriod": 7
        };

        $scope.purge = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;

            if (isNaN($scope.settings.messagingPurgePeriod)) {
                $scope.errorMessage = localization.localize('plugin.messaging.settings.enter.number');
            }

            pluginMessagingService.purgeOldMessages({"days": $scope.settings.messagingPurgePeriod}, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('plugin.messaging.settings.message.purge.success');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };
    })
    .controller('NewMessageController', function ($scope, $rootScope, $modalInstance, configurationService, groupService,
                                                  confirmModal, localization, pluginMessagingService, getDevicesService) {

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
            message: ""
        }

        $scope.send = function () {
            $scope.errorMessage = undefined;

            if ($scope.message.scope === 'device' && $scope.message.deviceNumber.trim() === '') {
                $scope.errorMessage = localization.localize('plugin.messaging.error.empty.device');
                return;
            }

            if ($scope.message.scope === 'group' && !$scope.message.groupId) {
                $scope.errorMessage = localization.localize('plugin.messaging.error.empty.group');
                return;
            }

            if ($scope.message.scope === 'configuration' && !$scope.message.configurationId) {
                $scope.errorMessage = localization.localize('plugin.messaging.error.empty.configuration');
                return;
            }

            if ($scope.message.message.trim() === '') {
                $scope.errorMessage = localization.localize('plugin.messaging.error.empty.text');
                return;
            }

            $scope.message.deviceNumber = getDevicesService.deviceLookupFormatter($scope.message.deviceNumber)

            $scope.sending = true;

            pluginMessagingService.sendMessage($scope.message).$promise.then(function(response) {
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
        $rootScope.$on('plugin-messaging-device-selected', function (event, device) {
            $location.url('/plugin-messaging?deviceNumber=' + device.number);
        })
        localization.loadPluginResourceBundles("messaging");
    });


