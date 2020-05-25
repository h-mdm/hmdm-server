// Localization completed
angular.module('plugin-devicelog', ['ngResource', 'ui.bootstrap', 'ui.router', 'ngTagsInput', 'ncy-angular-breadcrumb'])
    .config(function ($stateProvider) {
        // TODO : #5937 : Localization : localize ncyBreadcrumb.label
        try {
            $stateProvider.state('plugin-devicelog', {
                url: "/" + 'plugin-devicelog',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.devicelog.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-devicelog';
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-devicelog', e);
        }

        try {
            $stateProvider.state('plugin-settings-devicelog', {
                url: "/" + 'plugin-settings-devicelog',
                templateUrl: 'app/components/main/view/content.html',
                controller: 'TabController',
                ncyBreadcrumb: {
                    label: '{{"breadcrumb.plugin.devicelog.main" | localize}}', //label to show in breadcrumbs
                },
                resolve: {
                    openTab: function () {
                        return 'plugin-settings-devicelog'
                    }
                },
            });
        } catch (e) {
            console.log('An error when adding state ' + 'plugin-settings-devicelog', e);
        }
    })
    .factory('pluginDeviceLogService', function ($resource) {
        return $resource('', {}, {
            getSettings: {url: 'rest/plugins/devicelog/devicelog-plugin-settings/private', method: 'GET'},
            saveSettings: {url: 'rest/plugins/devicelog/devicelog-plugin-settings/private', method: 'PUT'},
            saveSettingsRule: {url: 'rest/plugins/devicelog/devicelog-plugin-settings/private/rule', method: 'PUT'},
            deleteSettingsRule: {url: 'rest/plugins/devicelog/devicelog-plugin-settings/private/rule/:id', method: 'DELETE'},
            getLogs: {url: 'rest/plugins/devicelog/log/private/search', method: 'POST'},
            exportLogs: {
                url: 'rest/plugins/devicelog/log/private/search/export',
                method: 'POST',
                responseType: 'arraybuffer',
                cache: false,
                transformResponse: function (data) {
                    return {
                        response: new Blob([data], {
                            type: "text/plain"
                        })
                    };
                }
            },
            lookupDevices: {url: 'rest/private/devices/autocomplete', method: 'POST'},
            lookupApplications: {url: 'rest/private/applications/autocomplete', method: 'POST'},
            lookupGroups: {url: 'rest/private/groups/autocomplete', method: 'POST'},
            lookupConfigurations: {url: 'rest/private/configurations/autocomplete', method: 'POST'},
        });
    })
    .controller('PluginDeviceLogTabController', function ($scope, $rootScope, $window, $location, $interval, $http,
                                                          pluginDeviceLogService, confirmModal,
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
            applicationFilter: null,
            severity: -1,
            dateFrom: null,
            dateTo: null,
            sortValue: 'createTime'
        };

        $scope.$watch('paging.pageNum', function() {
            $window.scrollTo(0, 0);
        });

        let deviceNumber = ($location.search()).deviceNumber;
        if (deviceNumber) {
            $scope.paging.deviceFilter = deviceNumber;
        }
        $scope.dateFormat = localization.localize('format.date.plugin.devicelog.datePicker');
        $scope.createTimeFormat = localization.localize('format.date.plugin.devicelog.createTime');
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
        };

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

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

        $scope.getDevices = function(val) {
            return pluginDeviceLogService.lookupDevices(val).$promise.then(function(response){
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
        };

        $scope.deviceLookupFormatter = function (v) {
            if (v) {
                var pos = v.indexOf('/');
                if (pos > -1) {
                    return v.substr(0, pos);
                }
            }
            return v;
        };

        $scope.getApplications = function(val) {
            return pluginDeviceLogService.lookupApplications(val).$promise.then(function(response){
                if (response.status === 'OK') {
                    return response.data.map(function (app) {
                        return app.name;
                    });
                } else {
                    return [];
                }
            });
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
            $scope.errorMessage = undefined;

            if ($scope.paging.dateFrom && $scope.paging.dateTo) {
                if ($scope.paging.dateFrom > $scope.paging.dateTo) {
                    $scope.errorMessage = localization.localize('error.plugin.devicelog.date.range.invalid');
                    return;
                }
            }

            $scope.paging.pageNum = 1;
            loadData();
        };

        $scope.exportFile = function () {
            $scope.errorMessage = undefined;

            if ($scope.paging.dateFrom && $scope.paging.dateTo) {
                if ($scope.paging.dateFrom > $scope.paging.dateTo) {
                    $scope.errorMessage = localization.localize('error.plugin.devicelog.date.range.invalid');
                    return;
                }
            }

            var request = {};
            for (var p in $scope.paging) {
                if ($scope.paging.hasOwnProperty(p)) {
                    request[p] = $scope.paging[p];
                }
            }

            request.deviceFilter = $scope.deviceLookupFormatter(request.deviceFilter);

            pluginDeviceLogService.exportLogs(request, function (data) {
                var downloadableBlob = URL.createObjectURL(data.response);

                var link = document.createElement('a');
                link.href = downloadableBlob;
                link.download = 'logs.txt';

                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }, function () {
                $scope.errorMessage = localization.localize('error.request.failure');
            });
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

            request.deviceFilter = $scope.deviceLookupFormatter(request.deviceFilter);

            pluginDeviceLogService.getLogs(request, function (response) {
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

        var reloadData = function() {
            if ($scope.paging.pageNum == 1) {
                loadData();
            }
        }

        var autoUpdateInterval = $interval(reloadData, 15000);
        $scope.$on('$destroy', function () {
            if (autoUpdateInterval) $interval.cancel(autoUpdateInterval);
        });

    })
    .controller('PluginDeviceLogSettingsController', function ($scope, $rootScope, $modal,
                                                               confirmModal, localization, pluginDeviceLogService) {
        $scope.successMessage = undefined;
        $scope.errorMessage = undefined;

        $rootScope.settingsTabActive = true;
        $rootScope.pluginsTabActive = false;

        $scope.settings = {};

        pluginDeviceLogService.getSettings(function (response) {
            if (response.status === 'OK') {
                $scope.settings = response.data;
                if (!$scope.settings.rules) {
                    $scope.settings.rules = [];
                }
            } else {
                $scope.errorMessage = localization.localize(response.message);
            }
        });

        $scope.save = function () {
            $scope.successMessage = undefined;
            $scope.errorMessage = undefined;

            var copy = {};
            for (var p in $scope.settings) {
                if ($scope.settings.hasOwnProperty(p)) {
                    copy[p] = $scope.settings[p];
                }
            }
            delete copy.rules;


            pluginDeviceLogService.saveSettings(copy, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.plugin.devicelog.settings.saved');
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.removeRule = function (rule) {
            let localizedText = localization.localize('plugin.devicelog.settings.question.delete.rule').replace('${rulename}', rule.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                pluginDeviceLogService.deleteSettingsRule({id: rule.id}, function (response) {
                    if (response.status === 'OK') {
                        refreshRules();
                    } else {
                        alertService.showAlertMessage(localization.localize('error.internal.server'));
                    }
                });
            });
        };

        $scope.editRule = function (rule) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/devicelog/views/rule.modal.html',
                controller: 'PluginDeviceLogEditRuleController',
                resolve: {
                    rule: function () {
                        return rule;
                    }
                }
            });

            modalInstance.result.then(function (saved) {
                if (saved) {
                    refreshRules();
                }
            });
        };

        var refreshRules = function () {
            pluginDeviceLogService.getSettings(function (response) {
                if (response.status === 'OK') {
                    $scope.settings.rules = response.data.rules;
                } else {
                    $scope.errorMessage = localization.localize(response.message);
                }
            });
        };
    })
    .controller('PluginDeviceLogEditRuleController', function ($scope, $modal, $modalInstance, $http,
                                                               localization, pluginDeviceLogService, rule) {

        var ruleCopy = {};
        for (var p in rule) {
            if (rule.hasOwnProperty(p)) {
                ruleCopy[p] = rule[p];
            }
        }

        $scope.rule = ruleCopy;
        $scope.saving = false;

        var appCandidates = [];
        var groupCandidates = [];
        var configurationCandidates = [];

        $scope.getApplications = function(val) {
            return pluginDeviceLogService.lookupApplications(val).$promise.then(function(response){
                if (response.status === 'OK') {
                    appCandidates = response.data;
                    return response.data.map(function (item) {
                        return item.name;
                    });
                } else {
                    appCandidates = [];
                    return [];
                }
            });
        };

        $scope.getGroups = function(val) {
            return pluginDeviceLogService.lookupGroups(val).$promise.then(function(response){
                if (response.status === 'OK') {
                    groupCandidates = response.data;
                    return response.data.map(function (item) {
                        return item.name;
                    });
                } else {
                    groupCandidates = [];
                    return [];
                }
            });
        };

        $scope.getConfigurations = function(val) {
            return pluginDeviceLogService.lookupConfigurations(val).$promise.then(function(response){
                if (response.status === 'OK') {
                    configurationCandidates = response.data;
                    return response.data.map(function (item) {
                        return item.name;
                    });
                } else {
                    configurationCandidates = [];
                    return [];
                }
            });
        };

        $scope.editRuleDevices = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/plugins/devicelog/views/ruleDevices.modal.html',
                controller: 'PluginDeviceLogEditRuleDevicesController',
                resolve: {
                    rule: function () {
                        return $scope.rule;
                    }
                }
            });

            modalInstance.result.then(function (ruleDevicesToUse) {
                $scope.rule.devices = ruleDevicesToUse;
            });
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            // Validate form
            if (!$scope.rule.name || $scope.rule.name.length === 0) {
                $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.empty.rule.name');
            } else if (!$scope.rule.applicationPkg || $scope.rule.applicationPkg.length === 0) {
                $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.empty.rule.app.pkg');
            } else if (!$scope.rule.severity || $scope.rule.severity.length === 0) {
                $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.empty.rule.severity');
            } else if (!validateApplication()) {
                $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.invalid.app');
            } else if (!validateGroup()) {
                $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.invalid.group');
            } else if (!validateConfiguration()) {
                $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.invalid.configuration');
            } else {
                $scope.saving = true;
                pluginDeviceLogService.saveSettingsRule($scope.rule, function (response) {
                    $scope.saving = false;
                    if (response.status === 'OK') {
                        $modalInstance.close(true);
                    } else {
                        $scope.errorMessage = localization.localize(response.message);
                    }
                }, function () {
                    $scope.saving = false;
                    $scope.errorMessage = localization.localize('error.request.failure');
                });
            }
        };

        var validateApplication = function () {
            if ($scope.rule.applicationPkg) {
                let foundItems = appCandidates.filter(function (item) {
                    return item.name === $scope.rule.applicationPkg;
                });

                if (foundItems.length > 0) {
                    $scope.rule.applicationId = foundItems[0].id;
                    return true;
                } else {
                    return false;
                }
            } else {
                $scope.rule.applicationId = null;
            }

            return true;
        };

        var validateGroup = function () {
            if ($scope.rule.groupName) {
                let foundItems = groupCandidates.filter(function (item) {
                    return item.name === $scope.rule.groupName;
                });

                if (foundItems.length > 0) {
                    $scope.rule.groupId = foundItems[0].id;
                    return true;
                } else {
                    return false;
                }
            } else {
                $scope.rule.groupId = null;
            }

            return true;
        };

        var validateConfiguration = function () {
            if ($scope.rule.configurationName) {
                let foundItems = configurationCandidates.filter(function (item) {
                    return item.name === $scope.rule.configurationName;
                });

                if (foundItems.length > 0) {
                    $scope.rule.configurationId = foundItems[0].id;
                    return true;
                } else {
                    return false;
                }
            } else {
                $scope.rule.configurationId = null;
            }

            return true;
        };

        if ($scope.rule.applicationPkg) {
            $scope.getApplications($scope.rule.applicationPkg);
        }
        if ($scope.rule.groupName) {
            $scope.getGroups($scope.rule.groupName);
        }
        if ($scope.rule.configurationName) {
            $scope.getConfigurations($scope.rule.configurationName);
        }

    })
    .controller('PluginDeviceLogEditRuleDevicesController', function ($scope, $modalInstance, $http,
                              localization, pluginDeviceLogService, rule) {

        var ruleCopy = {};
        for (var p in rule) {
            if (rule.hasOwnProperty(p)) {
                ruleCopy[p] = rule[p];
            }
        }

        ruleCopy.devices = [];
        if (rule.devices) {
            ruleCopy.devices = ruleCopy.devices.concat(rule.devices);
        }

        $scope.rule = ruleCopy;

        $scope.saving = false;
        $scope.newDevice = null;

        var deviceCandidates = [];


        $scope.addNewDevice = function () {
            $scope.errorMessage = undefined;

            if ($scope.newDevice) {
                let foundItems = deviceCandidates.filter(function (item) {
                    return item.name === $scope.newDevice;
                });

                if (foundItems.length > 0) {
                     $scope.rule.devices.push(foundItems[0]);
                     $scope.newDevice = null;
                     deviceCandidates = [];
                } else {
                    $scope.errorMessage = localization.localize('plugin.devicelog.settings.error.invalid.device');
                }
            }
        };

        $scope.getDevices = function(val) {
            return pluginDeviceLogService.lookupDevices(val).$promise.then(function(response){
                if (response.status === 'OK') {
                    deviceCandidates = response.data.filter(function (device) {
                        return $scope.rule.devices.findIndex(function (ruleDevice) {
                            return ruleDevice.id === device.id;
                        }) === -1;
                    });
                    return deviceCandidates.map(function (item) {
                        return item.name;
                    });
                } else {
                    deviceCandidates = [];
                    return [];
                }
            });
        };

        $scope.removeDevice = function (device) {
            var index = $scope.rule.devices.indexOf(device);
            if (index !== -1) {
                $scope.rule.devices.splice(index, 1);
            }
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            $modalInstance.close($scope.rule.devices);
        };
    })
    .run(function ($rootScope, $location, localization) {
        $rootScope.$on('plugin-devicelog-device-selected', function (event, device) {
            $location.url('/plugin-devicelog?deviceNumber=' + device.number);
        });
        localization.loadPluginResourceBundles("devicelog");
    });


