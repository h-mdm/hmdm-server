// Localization completed
angular.module('headwind-kiosk')
    .controller('DevicesTabController', function ($scope, $rootScope, $state, $modal, $interval, confirmModal, deviceService,
                                                  groupService, settingsService, authService, pluginService, $window,
                                                  configurationService, alertService,
                                                  spinnerService, localization) {
        $scope.searchParams = {};
        $scope.selection = {
            all: false,
            groupId: -1,
            configurationId: -1
        };
        $scope.localization = localization;
        $scope.dateFormat = localization.localize('devices.date.format');

        $scope.additionalParams = {
            enabled: false,
            dateFrom: null,
            dateTo: null,
            launcherVersion: '',
            installationStatus: null
        };

        $scope.toggleAdditionalParams = function () {
            $scope.additionalParams.enabled = !$scope.additionalParams.enabled;
            if (!$scope.additionalParams.enabled) {
                $scope.additionalParams.dateFrom = null;
                $scope.additionalParams.dateTo = null;
                $scope.additionalParams.launcherVersion = '';
                $scope.additionalParams.installationStatus = '';
                $scope.search();
            }
        };

        $scope.createTimeFormat = localization.localize('format.devices.date.createTime');
        $scope.datePickerOptions = { 'show-weeks': false };
        $scope.openDatePickers = {
            'dateFrom': false,
            'dateTo': false
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

        $scope.installStatusOptions = [
            {id: 'ALL', name: localization.localize('form.devices.selection.install.status.all')},
            {id: 'SUCCESS', name: localization.localize('form.devices.selection.install.status.success')},
            {id: 'VERSION_MISMATCH', name: localization.localize('form.devices.selection.install.status.version.mismatch')},
            {id: 'FAILURE', name: localization.localize('form.devices.selection.install.status.failure')}
        ];

        $scope.paging = {
            pageNum: 1,
            pageSize: 50,
            totalItems: 0,
            sortBy: null,
            sortAsc: true
        };

        $scope.firstRecord = function() {
            if ($scope.paging.totalItems == 0) {
                return 0;
            }
            return ($scope.paging.pageNum - 1) * $scope.paging.pageSize + 1;
        };

        $scope.lastRecord = function() {
            var l = $scope.paging.pageNum * $scope.paging.pageSize;
            if (l > $scope.paging.totalItems) {
                return $scope.paging.totalItems;
            }
            return l;
        };

        $scope.sortData = function (sortBy) {
            if ($scope.paging.sortBy !== sortBy) {
                $scope.paging.sortBy = sortBy;
                $scope.paging.sortAsc = true;
            } else {
                $scope.paging.sortAsc = !$scope.paging.sortAsc;
            }
            $scope.search();
        };

        $scope.$watch('paging.pageNum', function () {
            $scope.search();
            $window.scrollTo(0, 0);
        });

        $scope.hasPermission = authService.hasPermission;

        $scope.plugins = [];

        $scope.status = {
            isopen: false
        };

        $scope.formatMultiLine = function (text) {
            if (!text) {
                return text;
            } else {
                return text.replace(/\n/g, "<br/>");
            }
        };

        $scope.initSearch = function () {
            $scope.paging.pageNum = 1;
            $scope.search();
        };

        $scope.toggled = function (open) {
            $log.log('Dropdown is now: ', open);
        };

        $scope.toggleDropdown = function ($event) {
            $event.preventDefault();
            $event.stopPropagation();
            $scope.status.isopen = !$scope.status.isopen;
        };

        groupService.getAllGroups(function (response) {
            $scope.groups = response.data;
            $scope.groups.unshift({id: -1, name: localization.localize('devices.group.options.all')});
        });

        configurationService.getAllConfigurations(function (response) {
            $scope.configurations = response.data;
            $scope.configurations.unshift({id: -1, name: localization.localize('devices.configuration.options.all')});
        });

        var loadCommonSettings = function(completion) {
            settingsService.getSettings({}, function(response) {
                if (response.data) {
                    // Common settings
                    $scope.commonSettings = response.data;
                    if (completion) {
                        completion();
                    }
                }
            })
        };

        var loadSettings = function (completion) {
            if (authService.getUser().userRole) {
                settingsService.getUserRoleSettings({roleId: authService.getUser().userRole.id}, function (response) {
                    if (response.data) {
                        // Display settings
                        $scope.settings = response.data;
                    }
                });
            }
            loadCommonSettings(completion);
        };

        var resolveDeviceField = function (serverData, deviceInfoData) {
            if (serverData === deviceInfoData) {
                return [serverData, '', ''];
            } else if (serverData.length === 0 && deviceInfoData.length > 0) {
                return [deviceInfoData, '', ''];
            } else if (serverData.length > 0 && deviceInfoData.length === 0) {
                return [serverData, localization.localize('devices.no.data'), 'no-device-data'];
            } else {
                let localizedText = localization.localize('devices.settings.conformance.broken').replace('${serverData}', serverData);
                return [deviceInfoData, localizedText, 'device-data-mismatch'];
            }
        };

        var checkExpiryTime = function() {
            if ($scope.commonSettings.expiryTime) {
                var expiryDays = ($scope.commonSettings.expiryTime - new Date()) / 86400000;
                var expiryWarningAttrName = 'hmdm-expiry-warning-time';
                if (expiryDays <= 3) {
                    var expirymessage = expiryDays <= 0 ? 'account.expired' : 'account.expiring';
                    var expiryWarningTime = $window.localStorage.getItem(expiryWarningAttrName);
                    if (!expiryWarningTime || expiryWarningTime < (new Date()) - 86400000) {
                        alertService.showAlertMessage(localization.localize(expirymessage).replace('${days}', Math.ceil(expiryDays)));
                        $window.localStorage.setItem(expiryWarningAttrName, (new Date()) * 1);
                    }
                }
                if (expiryDays <= 0) {
                    $rootScope.$emit('SHOW_EXPIRY_WARNING');
                    $scope.accountExpired = true;
                    $scope.search();
                }
            }
        };

        loadSettings(checkExpiryTime);

        var sub = $rootScope.$on('aero_COMMON_SETTINGS_UPDATED', function () {
            loadSettings();
        });
        $scope.$on('$destroy', sub);

        $scope.init = function () {
            $rootScope.settingsTabActive = false;
            $rootScope.pluginsTabActive = false;
            $scope.paging.pageNum = 1;
            $scope.search();
        };

        $scope.showSpinner = false;
        var searchIsRunning = false;
        $scope.search = function (spinnerHidden) {
            if (searchIsRunning) {
                console.log("Skipping device search since a previous search is pending", new Error());
                return;
            }

            $scope.errorMessage = undefined;

            if ($scope.additionalParams.enabled) {
                if ($scope.additionalParams.dateFrom && $scope.additionalParams.dateTo) {
                    if ($scope.additionalParams.dateFrom > $scope.additionalParams.dateTo) {
                        $scope.errorMessage = localization.localize('error.date.range.invalid');
                        return;
                    }
                }
            }

            searchIsRunning = true;
            $scope.showSpinner = !spinnerHidden;
            if ($scope.showSpinner) {
                spinnerService.show('spinner2');
            }

            var request = {
                value: $scope.searchParams.searchValue,
                groupId: $scope.selection.groupId,
                configurationId: $scope.selection.configurationId,
                pageNum: $scope.paging.pageNum,
                pageSize: $scope.paging.pageSize,
                sortBy: $scope.paging.sortBy,
                sortDir: $scope.paging.sortAsc ? "ASC" : "DESC"
            };

            if ($scope.additionalParams.enabled) {
                request["dateFrom"] = $scope.additionalParams.dateFrom;
                request["dateTo"] = $scope.additionalParams.dateTo;
                if ($scope.additionalParams.launcherVersion && $scope.additionalParams.launcherVersion.trim().length > 0) {
                    request["launcherVersion"] = $scope.additionalParams.launcherVersion;
                }
                if ($scope.additionalParams.installationStatus !== 'ALL'
                    && $scope.additionalParams.installationStatus
                    && $scope.additionalParams.installationStatus.length > 0) {
                    request["installationStatus"] = $scope.additionalParams.installationStatus;
                }
                if ($scope.additionalParams.imeiChanged) {
                    request["imeiChanged"] = true;
                }
            }

            deviceService.getAllDevices(request, function (response) {
                $scope.selection.all = false;
                searchIsRunning = false;
                if ($scope.showSpinner) {
                    spinnerService.close('spinner2');
                }
                $scope.showSpinner = false;

                if (response.data && response.data.devices.items) {

                    var configurations = response.data.configurations;

                    var counter = 0;
                    response.data.devices.items.forEach(function (device) {
                        var deviceInfo = $scope.getDeviceInfo(device);
                        var serverIMEI = device.imei || '';
                        var deviceInfoIMEI = deviceInfo ? (deviceInfo.imei || '') : '';
                        var resolvedIMEI = resolveDeviceField(serverIMEI, deviceInfoIMEI);
                        device.displayedIMEI = resolvedIMEI[0];
                        device.imeiTooltip = resolvedIMEI[1];
                        device.imeiTooltipClass = resolvedIMEI[2];
                        device.configuration = configurations[device.configurationId];

                        var serverPhone = device.phone || '';
                        var deviceInfoPhone = deviceInfo ? (deviceInfo.phone || '') : '';
                        var resolvedPhone = resolveDeviceField(serverPhone, deviceInfoPhone);
                        device.displayedPhone = resolvedPhone[0];
                        device.phoneTooltip = resolvedPhone[1];
                        device.phoneTooltipClass = resolvedPhone[2];

                        if ($scope.accountExpired) {
                            if (counter == 3) {
                                device.class='expired-device-opacity1';
                            } else if (counter == 4) {
                                device.class='expired-device-opacity2';
                            } else if (counter > 4) {
                                device.class='expired-device-hidden';
                            }
                            counter++;
                        }

                    });

                    $scope.devices = response.data.devices.items;
                    for (var i = 0; i < $scope.devices.length; i++) {
                        $scope.devices[i].lastUpdateDate = new Date($scope.devices[i].lastUpdate);
                    }

                    $scope.paging.totalItems = response.data.devices.totalItemsCount;
                }
            }, function () {
                searchIsRunning = false;
                if ($scope.showSpinner) {
                    spinnerService.close('spinner2');
                }
                $scope.showSpinner = false;
            });
        };

        $scope.showQrCode = function (device) {
            // Workaround against AngularJS bug!
            var number = device.number.replace(/\//g, "~2F");
            var url = device.configuration.baseUrl + "/#/qr/" + device.configuration.qrCodeKey + "/" + number;
            $window.open(url, "_blank");
        };

        $scope.interval = $interval(function () {
            $scope.search(true);
        }, 60 * 1000);
        $scope.$on('$destroy', function () {
            if ($scope.interval) $interval.cancel($scope.interval);
        });

        $scope.selectAll = function () {
            if ($scope.devices) {
                for (var i = 0; i < $scope.devices.length; i++) {
                    $scope.devices[i].selected = $scope.selection.all;
                }
            }
        };

        $scope.isNotSelected = function () {
            if ($scope.devices) {
                for (var i = 0; i < $scope.devices.length; i++) {
                    if ($scope.devices[i].selected) {
                        return false;
                    }
                }
            }

            return true;
        };

        const updateTime = 2 * 60 * 60 * 1000;
        $scope.getDeviceIndicatorImage = function (device) {
            if (device.statusCode) {
                return "images/circle-" + device.statusCode + ".png";
            } else {
                // This is an old approach but it is left for now just in case
                if ((new Date().getTime() - device.lastUpdate) < updateTime) {
                    return 'images/online.png';
                } else if ((new Date().getTime() - device.lastUpdate) < (2 * updateTime)) {
                    return 'images/away.png';
                } else {
                    return 'images/offline.png';
                }
            }
        };

        // Gets the info on the device parsed from the JSON-string taken from "info" attribute of the device
        $scope.getDeviceInfo = function (device) {
            return device.info;
        };

        $scope.getDeviceModel = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                return info.model;
            } else {
                return localization.localize("devices.model.unknown");
            }
        };

        $scope.getDevicePermissionIndicatorImage = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                var permissions = info.permissions[0] + info.permissions[1] + info.permissions[2];
                if (permissions === 0) {
                    return 'images/offline.png';
                } else if (permissions < 3) {
                    return 'images/away.png';
                } else {
                    return 'images/online.png';
                }
            } else {
                return 'images/offline.png';
            }
        };

        $scope.getDevicePermissionTitle = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                var permissions = info.permissions[0] + info.permissions[1] + info.permissions[2];
                if (permissions === 3) {
                    return localization.localize('devices.permissions.all');
                } else {
                    var title = '';
                    for (var i = 0; i < info.permissions.length; i++) {
                        if (info.permissions[i] !== 1) {
                            if (i === 0) {
                                title = title + localization.localize('devices.permissions.not.as.device.admin');
                            } else if (i === 1) {
                                title = title + localization.localize('devices.permissions.window.overlap.prohibited');
                            } else {
                                title = title + localization.localize('devices.permissions.history.access.prohibited');
                            }
                            title += '\n';
                        }
                    }

                    if (title.lastIndexOf('\n') === title.length - 1) {
                        title = title.substring(0, title.lastIndexOf('\n'));
                    }

                    return title;
                }
            } else {
                return localization.localize('devices.unknown');
            }
        };

        $scope.getDeviceApplicationsIndicatorImage = function (device) {
            var applications = $scope.getDeviceApplicationsStatus(device);
            if (applications) {

                var correctCount = 0;
                var incorrectCount = 0;
                var notInstalledCount = 0;
                var removedCount = 0;
                var length = 0;
                for (var i = 0; i < applications.length; i++) {
                    if (applications[i].status !== undefined) {
                        length++;
                        if (applications[i].status === 2) {
                            incorrectCount++;
                        }
                        if (applications[i].status === 3) {
                            correctCount++;
                        }
                        if (applications[i].status === 1) {
                            notInstalledCount++;
                        }
                        if (applications[i].status === 4) {
                            removedCount++;
                        }
                    }
                }

                if (correctCount === length) {
                    return 'images/online.png';
                } else if (notInstalledCount > 0) {
                    return 'images/offline.png';
                } else {
                    return 'images/away.png';
                }
            } else {
                return 'images/offline.png';
            }
        };

        $scope.getDeviceApplicationsTitle = function (device) {
            var applications = $scope.getDeviceApplicationsStatus(device);
            if (applications) {
                var title = '';

                for (var j = 0; j < applications.length; j++) {
                    if (applications[j].status === 1) {
                        let localizedText = localization.localize('devices.app.not.installed').replace('${applicationName}', applications[j].name);
                        title = title + localizedText;
                        if (applications[j].version !== '0') {
                            let localizedText = localization.localize('devices.app.version.available').replace('${applicationVersion}', applications[j].version);
                            title += localizedText;
                        }
                        title += '\n';
                    } else if (applications[j].status === 4) {
                        let localizedText = localization.localize('devices.app.installed').replace('${applicationName}', applications[j].name);
                        let localizedText2 = localization.localize('devices.app.needs.removal').replace('${applicationVersion}', (applications[j].installedVersion ? ' ' + applications[j].installedVersion : ""));
                        title = title + localizedText + localizedText2;
                        title += '\n';
                    } else if (applications[j].status === 2) {
                        let localizedText = localization.localize('devices.app.installed.and.version.available')
                            .replace('${applicationName}', applications[j].name)
                            .replace('${applicationInstalledVersion}', applications[j].installedVersion)
                            .replace('${applicationVersionAvailable}', applications[j].version);
                        title = title + localizedText;
                        title += '\n';
                    }
                }

                if (title.lastIndexOf('\n') === title.length - 1) {
                    title = title.substring(0, title.lastIndexOf('\n'));
                }

                return title;
            } else {
                return localization.localize('devices.unknown');
            }

        };

        function areVersionsEqual(v1, v2) {
            var v1d = (v1 || "").replace(/[^\d.]/g, "");
            var v2d = (v2 || "").replace(/[^\d.]/g, "");
            return v1d === v2d;
        }

        function isVersionUpToDate(installed, required) {
            return compareVersions(installed, required) >= 0;
        }

        // Returns -1 if v1 < v2, 0 if v1 == v2 and 1 if v1 > v2
        function compareVersions(v1, v2) {
            // Versions are numbers separated by a dot
            var v1d = (v1 || "").replace(/[^\d.]/g, "");
            var v2d = (v2 || "").replace(/[^\d.]/g, "");

            var v1n = v1d.split(".");
            var v2n = v2d.split(".");

            // One version could contain more digits than another
            var count = v1n.length < v2n.length ? v1n.length : v2n.length;

            for (var n = 0; n < count; n++) {
                var n1 = v1n[n];
                var n2 = v2n[n];
                if (n1 < n2) {
                    return -1;
                } else if (n1 > n2) {
                    return 1;
                }
                // If major version numbers are equals, continue to compare minor version numbers
            }

            // Here we are if common parts are equal
            // Now we decide that if a version has more parts, it is considered as greater
            if (v1n.length < v2n.length) {
                return -1;
            } else if (v1n.length > v2n.length) {
                return 1;
            }
            return 0;
        }

        $scope.getDeviceLauncherVersion = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                if (device.launcherPkg) {
                    var deviceLauncherApp = info.applications.find(function (deviceApp) {
                        return deviceApp.pkg === device.launcherPkg;
                    });
                    if (deviceLauncherApp) {
                        return deviceLauncherApp.version;
                    }
                }
            }

            return null;
        };

        $scope.getDeviceBatteryLevel = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info && info.batteryLevel) {
                return info.batteryLevel + '%';
            }

            return null;
        };

        $scope.getIsDefaultLauncher = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                if (info.defaultLauncher === true) {
                    return localization.localize('yes');
                } else if (info.defaultLauncher === false) {
                    return localization.localize('no');
                }
            }

            return null;
        };

        $scope.getDeviceLauncherVersionColor = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                if (device.launcherPkg && device.launcherVersion !== '0') {
                    var deviceLauncherApp = info.applications.find(function (deviceApp) {
                        return deviceApp.pkg === device.launcherPkg;
                    });
                    if (deviceLauncherApp && (deviceLauncherApp.version !== device.launcherVersion)) {
                        return 'red';
                    }
                }
            }

            return 'inherit';
        };

        $scope.getDeviceFilesStatus = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                var configFiles = device.configuration.files;

                for (var j = 0; j < configFiles.length; j++) {
                    configFiles[j].status = 3; // Good

                    let deviceFiles = (info.files || []);
                    let foundOnDevice = false;
                    for (var i = 0; i < deviceFiles.length; i++) {
                        if (deviceFiles[i].path === configFiles[j].path) {
                            foundOnDevice = true;
                            // if (configFiles[j].remove !== deviceFiles[i].remove) {
                            //     configFiles[j].status = 4; // Remove flag mismatches
                            // } else
                            if (configFiles[j].lastUpdate !== deviceFiles[i].lastUpdate
                                && Math.abs(configFiles[j].lastUpdate - deviceFiles[i].lastUpdate) > 1 * 60 * 60 * 1000) {
                                configFiles[j].status = 2; // lastUpdate mismatches
                                configFiles[j].lastUpdateDiff = Math.abs(configFiles[j].lastUpdate - deviceFiles[i].lastUpdate);
                            }
                            break;
                        }
                    }
                    if (!foundOnDevice && configFiles[j].remove === false) {
                        configFiles[j].status = 1; // Not installed
                    }
                }

                return configFiles;
            } else {
                return null;
            }
        };

        $scope.getDeviceFilesIndicatorImage = function (device) {
            var files = $scope.getDeviceFilesStatus(device);
            if (files) {

                var correctCount = 0;
                var incorrectCount = 0;
                var notInstalledCount = 0;
                var removedCount = 0;
                var length = 0;
                for (var i = 0; i < files.length; i++) {
                    if (files[i].status !== undefined) {
                        length++;
                        if (files[i].status === 2) {
                            incorrectCount++;
                        }
                        if (files[i].status === 3) {
                            correctCount++;
                        }
                        if (files[i].status === 1) {
                            notInstalledCount++;
                        }
                        if (files[i].status === 4) {
                            removedCount++;
                        }
                    }
                }

                if (correctCount === length) {
                    return 'images/online.png';
                } else if (notInstalledCount > 0) {
                    return 'images/offline.png';
                } else {
                    return 'images/away.png';
                }
            } else {
                return 'images/offline.png';
            }
        };

        $scope.getDeviceFilesTitle = function (device) {
            var files = $scope.getDeviceFilesStatus(device);
            if (files) {
                var title = '';

                for (var j = 0; j < files.length; j++) {
                    if (files[j].status === 1) {
                        let localizedText = localization.localize('devices.file.not.installed').replace('${file}', files[j].path);
                        title = title + localizedText;
                        title += '\n';
                    // } else if (files[j].status === 4) {
                    //     let localizedText = localization.localize('devices.app.installed').replace('${applicationName}', files[j].name);
                    //     let localizedText2 = localization.localize('devices.app.needs.removal').replace('${applicationVersion}', (files[j].installedVersion ? ' ' + files[j].installedVersion : ""));
                    //     title = title + localizedText + localizedText2;
                    //     title += '\n';
                    } else if (files[j].status === 2) {
                        let localizedText = localization.localize('devices.file.lastUpdate.differs')
                            .replace('${file}', files[j].path)
                            .replace('${diff}', parseInt(files[j].lastUpdateDiff / 1000 / 60));
                        title = title + localizedText;
                        title += '\n';
                    }
                }

                if (title.lastIndexOf('\n') === title.length - 1) {
                    title = title.substring(0, title.lastIndexOf('\n'));
                }

                return title;
            } else {
                return localization.localize('devices.unknown');
            }

        };


        // Gets the status of the configuration applications for the device. Checks which applications are not installed
        // on device (sets status = 1), which are installed but have their version mismatching (sets status = 2) and
        // which are installed and have their versions matching (sets status = 3). If application is installed on device
        // but is marked as removed in configuration then sets status = 4
        $scope.getDeviceApplicationsStatus = function (device) {
            var info = $scope.getDeviceInfo(device);
            if (info) {
                var configApplications = device.configuration.applications;

                for (var j = 0; j < configApplications.length; j++) {
                    // Applications without URL are system apps and they are not checked
                    if (configApplications[j].selected && configApplications[j].url) {
                        configApplications[j].status = 3; // Good

                        let deviceApplications = info.applications;
                        let foundOnDevice = false;
                        for (var i = 0; i < deviceApplications.length; i++) {
                            if (deviceApplications[i].pkg === configApplications[j].pkg) {
                                foundOnDevice = true;
                                if (configApplications[j].action == '2') {
                                    if (configApplications[j].version === deviceApplications[i].version) {
                                        configApplications[j].installedVersion = deviceApplications[i].version;
                                        configApplications[j].status = 4; // Needs to be removed
                                    }
                                } else if (configApplications[j].version !== '0' && !configApplications[j].skipVersion
                                    && !isVersionUpToDate(deviceApplications[i].version, configApplications[j].version)) {
                                    configApplications[j].installedVersion = deviceApplications[i].version;
                                    configApplications[j].status = 2; // Version mismatch
                                }
                                break;
                            }
                        }
                        if (!foundOnDevice && configApplications[j].action != '2') {
                            configApplications[j].status = 1; // Not installed
                        }
                    }
                }

                return configApplications;
            } else {
                return null;
            }
        };

        $scope.openBulkUpdateModal = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/device.update.html',
                controller: 'DeviceUpdateModalController',
                resolve: {
                    devices: function () {
                        return $scope.devices;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.editDevice = function (device) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/device.html',
                controller: 'DeviceModalController',
                resolve: {
                    device: function () {
                        return device;
                    },
                    settings: function() {
                        return $scope.commonSettings;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
                // Reload settings because the device amount may be changed
                loadCommonSettings();
            });
        };

        $scope.removeDevice = function (device) {
            let localizedText = localization.localize('question.delete.device').replace('${deviceNumber}', device.number);
            confirmModal.getUserConfirmation(localizedText, function () {
                deviceService.removeDevice({id: device.id}, function () {
                    $scope.search();
                    // Reload settings because the device amount may be changed
                    loadCommonSettings();
                });
            });
        };

        $scope.notifyPluginOnDevice = function (plugin, device) {
            $rootScope.$emit('plugin-' + plugin.identifier + '-device-selected', device);
        };

        $scope.editConfiguration = function (configuration) {
            $state.transitionTo('configEditor', {"id": configuration.id});
        };

        $scope.manageApplicationSettings = function (device) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/device.applicationSettings.html',
                controller: 'DeviceApplicationSettingsModalController',
                size: 'lg',
                resolve: {
                    device: function () {
                        return device;
                    }
                }
            });

            modalInstance.result.then(function () {
            });
        };

        pluginService.getAvailablePlugins(function (response) {
            if (response.status === 'OK') {
                if (response.data) {
                    $scope.plugins = response.data.filter(function (plugin) {
                        return plugin.enabledForDevice
                            && plugin.functionsViewTemplate
                            && (!plugin.deviceFunctionsPermission
                                || authService.hasPermission(plugin.deviceFunctionsPermission));
                    });
                }
            }
        });

        $scope.init();
    })
    .controller('DeviceUpdateModalController', function ($scope, $modalInstance, configurationService, deviceService, devices) {
        $scope.device = {};

        configurationService.getAllConfigurations(function (response) {
            $scope.device.configurationId = response.data[0].id;
            $scope.configurations = response.data;
        });

        $scope.save = function () {
            var ids = [];
            for (var i = 0; i < devices.length; i++) {
                if (devices[i].selected) {
                    ids.push(devices[i].id);
                }
            }

            var device = {'ids': ids, configurationId: $scope.device.configurationId};
            deviceService.updateDevice(device, function () {
                $modalInstance.close();
            });
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    })
    .controller('DeviceModalController',
        function ($scope, $modalInstance, deviceService, configurationService, groupService, device, settings,
                  localization, authService, confirmModal) {

            $scope.canEditDevice = authService.hasPermission('edit_devices');

            $scope.migratingDevice = device.hasOwnProperty('oldNumber') && device.oldNumber !== null;
            $scope.migrationHint = $scope.migratingDevice ? localization.localize('form.device.number.locked') : null;

            $scope.groupsList = [];

            groupService.getAllGroups(function (response) {
                $scope.groups = response.data;
                $scope.groupsList = response.data.map(function (group) {
                    return {id: group.id, label: group.name};
                });
            });

            $scope.groupsSelection = (device.groups || []).map(function (group) {
                return {id: group.id};
            });

            $scope.tableFilteringTexts = {
                'buttonDefaultText': localization.localize('table.filtering.no.selected.group'),
                'checkAll': localization.localize('table.filtering.check.all'),
                'uncheckAll': localization.localize('table.filtering.uncheck.all'),
                'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.group')
            };

            var deviceFields = ["id", "number", "description", "configurationId", "imei", "phone", "groups", "custom1", "custom2", "custom3", "oldNumber"];
            $scope.device = {};
            for (var prop in device) {
                if (device.hasOwnProperty(prop)) {
                    if (deviceFields.indexOf(prop) >= 0) {
                        $scope.device[prop] = device[prop];
                    }
                }
            }

            $scope.settings = settings;

            $scope.loading = false;

            var saveCompletion = function(targetService, pathParams, request) {
                targetService(pathParams, request, function (response) {
                    $scope.loading = false;
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                }, function () {
                    $scope.loading = false;
                    $scope.errorMessage = localization.localizeServerResponse('error.request.failure');
                });
            };

            $scope.save = function () {
                $scope.errorMessage = undefined;

                if (!$scope.device.configurationId) {
                    $scope.errorMessage = localization.localize('error.empty.configuration');
                } else {
                    $scope.device.groups = $scope.groupsSelection;

                    $scope.loading = true;

                    var targetService;
                    var pathParams = {};
                    var request;

                    if ($scope.canEditDevice) {
                        targetService = deviceService.updateDevice;
                        request = {};
                        for (var prop in $scope.device) {
                            if ($scope.device.hasOwnProperty(prop)) {
                                request[prop] = $scope.device[prop]
                            }
                        }

                        if ($scope.device.number !== device.number &&
                            device.lastUpdate > 0) {
                            // Confirm the migration
                            $scope.loading = false;
                            var localizedText = localization.localize('form.device.migration.warning');
                            confirmModal.getUserConfirmation(localizedText, function () {
                                $scope.loading = true;
                                request.oldNumber = device.number;
                                saveCompletion(targetService, pathParams, request);
                            });
                            return;
                        }
                    } else {
                        targetService = deviceService.updateDeviceDesc;
                        pathParams.id = $scope.device.id;
                        request = $scope.device.description;
                    }

                    saveCompletion(targetService, pathParams, request);
                }
            };

            $scope.closeModal = function () {
                $modalInstance.dismiss();
            };

            configurationService.getAllConfigurations(function (response) {
                $scope.configurations = response.data;
            });

            groupService.getAllGroups(function (response) {
                $scope.groups = response.data;
            });
        })
    .controller('DeviceApplicationSettingsModalController', function ($scope, $modal, $modalInstance,
                                                                      localization, deviceService,
                                                                      applicationService, alertService,
                                                                      device) {

        $scope.device = device;
        $scope.applicationSettings = [];
        $scope.saving = false;

        var applications = [];
        var allApplicationSettings = [];

        $scope.errorMessage = undefined;
        $scope.successMessage = undefined;

        $scope.settingsPaging = {
            currentPage: 1,
            pageSize: 50,
            appSettingsAppFilterText: '',
            appSettingsFilterText: '',
            appSettingsFilterApp: null
        };

        var getAppSettingsApps = function (filter) {
            var lower = filter.toLowerCase();
            var apps = applications.filter(function (app) {
                // Intentionally using app.action == 1 but not app.action === 1
                return (app.name.toLowerCase().indexOf(lower) > -1
                    || app.pkg && app.pkg.toLowerCase().indexOf(lower) > -1
                    || app.version && app.version.toLowerCase().indexOf(lower) > -1);
            });

            apps.sort(function (a, b) {
                let n1 = a.name.toLowerCase();
                let n2 = b.name.toLowerCase();

                if (n1 === n2) {
                    return 0;
                } else if (n1 < n2) {
                    return -1;
                } else {
                    return 1;
                }
            });

            return apps;
        };

        $scope.getAppSettingsApps = getAppSettingsApps;
        
        $scope.onAppSettingsFilterAppSelected = function ($item) {
            $scope.settingsPaging.appSettingsFilterApp = $item;
            $scope.settingsPaging.appSettingsAppFilterText = $item.pkg;
            filterApplicationSettings();
        };

        $scope.appSettingsAppLookupFormatter = function (val) {
            if (val) {
                return val.pkg;
            } else {
                return null;
            }
        };

        $scope.appSettingsFilterChanged = function () {
            filterApplicationSettings();
        };

        $scope.addApplicationSetting = function () {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationSetting.html',
                controller: 'ApplicationSettingEditorController',
                resolve: {
                    applicationSetting: function () {
                        return {type: "STRING"};
                    },
                    getApps: function () {
                        return getAppSettingsApps;
                    }
                }
            });

            modalInstance.result.then(function (applicationSetting) {
                if (!applicationSetting.id) {
                    applicationSetting.tempId = new Date().getTime();
                    allApplicationSettings.push(applicationSetting);
                    filterApplicationSettings();
                }
            });
        };

        $scope.editApplicationSetting = function (setting) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/applicationSetting.html',
                controller: 'ApplicationSettingEditorController',
                resolve: {
                    applicationSetting: function () {
                        return setting;
                    },
                    getApps: function () {
                        return getAppSettingsApps;
                    }
                }
            });

            modalInstance.result.then(function (applicationSetting) {
                var index = $scope.applicationSettings.findIndex(function (item) {
                    if (item.id) {
                        return item.id === applicationSetting.id;
                    } else if (item.tempId) {
                        return item.tempId === applicationSetting.tempId;
                    } else {
                        return false;
                    }
                });

                if (index >= 0) {
                    allApplicationSettings[index] = applicationSetting;
                    filterApplicationSettings();
                }
            });
        };

        $scope.removeApplicationSetting = function (applicationSetting) {
            var index = $scope.applicationSettings.findIndex(function (item) {
                if (item.id) {
                    return item.id === applicationSetting.id;
                } else if (item.tempId) {
                    return item.tempId === applicationSetting.tempId;
                } else {
                    return false;
                }
            });

            if (index >= 0) {
                allApplicationSettings.splice(index, 1);
                filterApplicationSettings();
            }
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        };

        $scope.save = function () {
            $scope.saving = true;
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;

            deviceService.saveDeviceApplicationSettings({id: device.id}, allApplicationSettings, function (response) {
                if (response.status === 'OK') {
                    $modalInstance.close();
                } else {
                    $scope.errorMessage = localization.localize(response.message);
                }
                $scope.saving = false;
            }, function () {
                $scope.saving = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            });
        };

        $scope.notifyDevice = function () {
            $scope.saving = true;
            $scope.errorMessage = undefined;
            $scope.successMessage = undefined;

            deviceService.notifyDeviceOnAppSettingsUpdate({id: device.id}, {}, function (response) {
                if (response.status === 'OK') {
                    $scope.successMessage = localization.localize('success.config.update.device.app.settings.notification');
                } else {
                    $scope.errorMessage = localization.localize(response.message);
                }
                $scope.saving = false;
            }, function () {
                $scope.saving = false;
                $scope.errorMessage = localization.localize('error.request.failure');
            });
        };

        var filterApplicationSettings = function () {
            $scope.applicationSettings = allApplicationSettings.filter(function (item) {
                var valid = true;
                if ($scope.settingsPaging.appSettingsFilterText && $scope.settingsPaging.appSettingsFilterText.length > 0) {
                    var lower = $scope.settingsPaging.appSettingsFilterText.toLowerCase();

                    valid = (item.name !== null) && (item.name !== undefined) && item.name.toLowerCase().indexOf(lower) > -1
                        || (item.value !== null) && (item.value !== undefined) && item.value.toLowerCase().indexOf(lower) > -1
                        || (item.comment !== null) && ((item.comment !== undefined)) && item.comment.toLowerCase().indexOf(lower) > -1
                }

                if (valid) {
                    if ($scope.settingsPaging.appSettingsFilterApp && $scope.settingsPaging.appSettingsFilterApp.id) {
                        valid = item.applicationId === $scope.settingsPaging.appSettingsFilterApp.id;
                    } else if (typeof $scope.settingsPaging.appSettingsFilterApp === "string") {
                        valid = item.applicationPkg.toLowerCase().indexOf($scope.settingsPaging.appSettingsFilterApp.toLowerCase(0)) > -1;
                    }
                }

                return valid;
            });
        };

        var loadData = function () {
            deviceService.getDeviceApplicationSettings({id: device.id}, function (response) {
                if (response.status === 'OK') {
                    allApplicationSettings = response.data;
                    filterApplicationSettings();
                } else {
                    $scope.errorMessage = localization.localize(response.message);
                }
            }, alertService.onRequestFailure);

            applicationService.getAllApplications({}, function (response) {
                if (response.status === 'OK') {
                    applications = response.data;
                } else {
                    console.error("Failed to load the list of applications: ", localization.localize(response.message));
                }
            });
        };

        loadData();

    });
