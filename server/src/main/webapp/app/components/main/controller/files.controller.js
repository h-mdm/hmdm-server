// Localization completed
angular.module('headwind-kiosk')
    .controller('FilesTabController', function ($scope, $rootScope, $state, $modal, alertService, confirmModal, fileService,
                                                authService, $window, localization, storageService) {
        $scope.search = {};

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.$watch('paging.currentPage', function () {
            $window.scrollTo(0, 0);
        });

        $scope.hasPermission = authService.hasPermission;

        $scope.url = document.URL.replace("/#/login", "").replace("/#/", "");

        $scope.readableSizeMb = function(size) {
            if (size != -1) {
                return storageService.readableSize(size) + " Mb";
            } else {
                // -1 means the file doesn't exist
                return localization.localize('form.file.deleted')
            }
        };

        $scope.availableSpace = null;
        var updateLimit = function() {
            fileService.getLimit(function(response) {
                if (response.status === 'OK' &&
                    response.data.sizeLimit > 0) {
                    var availableSpace = response.data.sizeLimit - response.data.sizeUsed;
                    if (availableSpace < 0) {
                        availableSpace = 0;
                    }
                    if (availableSpace < 20) {
                        $scope.availableSpace = localization.localize('form.file.available')
                            .replaceAll('${space}', availableSpace);
                    }
                }
            });
        };

        $scope.init = function () {
            updateLimit();
            $rootScope.settingsTabActive = false;
            $rootScope.pluginsTabActive = false;
            $scope.paging.currentPage = 1;
            $scope.search();
        };

        $scope.updateTimeFormat = localization.localize('format.devices.date.createTime');
        $scope.search = function () {
            fileService.getAllFiles({value: $scope.search.searchValue},
                function (response) {
                    response.data.forEach(function (file) {
                        file.removeButtonTooltip = '';
                        if (file.usedByConfigurations && file.usedByConfigurations.length > 0) {
                            file.removalDisabled = true;
                            var s = localization.localize("tooltip.usage.byconfigurations");
                            file.usedByConfigurations.forEach(function (item) {
                                s += "\n";
                                s += item;
                            });
                            file.removeButtonTooltip += s;
                        }
                        if (file.usedByIcons && file.usedByIcons.length > 0) {
                            file.removalDisabled = true;
                            var s = localization.localize("tooltip.usage.byicons");
                            file.usedByIcons.forEach(function (item) {
                                s += "\n";
                                s += item;
                            });
                            file.removeButtonTooltip += s;
                        }
                        file.copyLinkTooltip = localization.localize("form.file.copy.link")
                            .replaceAll('${link}', file.url);
                    });

                    $scope.files = response.data;
                });
        };

        $scope.copyLink = function(file) {
            navigator.clipboard.writeText(file.url);
        };

        $scope.editFile = function(file) {
            if (file && file.external) {
                file.externalUrl = file.url;
            }
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/file.html',
                controller: 'FileModalController',
                resolve: {
                    file: function () {
                        return file;
                    }
                }
            });

            modalInstance.result.then(function (response) {
                updateLimit();
                if (angular.equals(file, {})) {
                    $scope.editConfiguration(response);
                } else {
                    $scope.search();
                }
            });

        };

        $scope.removeFile = function (file) {
            var fileName = file.description ? file.description :
                (file.external ? file.url : file.filePath);
            confirmModal.getUserConfirmation(localization.localize('question.delete.file').replace('${fileName}', fileName), function () {
                fileService.removeFile(file, function (response) {
                    if (response.status === 'OK') {
                        updateLimit();
                        $scope.search();
                    } else {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                });
            });
        };

        $scope.editConfiguration = function(file) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/fileConfigurations.html',
                controller: 'FileConfigurationsModalController',
                resolve: {
                    file: function () {
                        return file;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.showApps = function (file) {
            fileService.getApps({value: encodeURIComponent(file.url)}, function (response) {
                if (response.status === 'OK') {
                    var modalInstance = $modal.open({
                        templateUrl: 'app/components/main/view/modal/fileApps.html',
                        controller: 'FileAppsModalController',
                        resolve: {
                            apps: function () {
                                return response.data;
                            }
                        }
                    });
                } else {
                    alertService.showAlertMessage(localization.localize(response.message));
                }
            })
        };

        $scope.init();
    })
    .controller('FileAppsModalController', function ($scope, $modalInstance, apps) {
        $scope.apps = apps;

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    })
    .controller('FileModalController', function ($scope, $modalInstance, fileService, file, localization) {
        $scope.file = file !== null ? angular.copy(file, {}) : {};

        fileService.getLimit(function(response) {
            if (response.status === 'OK' &&
                response.data.sizeLimit > 0) {
                var availableSpace = response.data.sizeLimit - response.data.sizeUsed;
                if (availableSpace < 0) {
                    availableSpace = 0;
                }
                if (availableSpace < 20) {
                    $scope.availableSpace = localization.localize('form.file.available')
                        .replaceAll('${space}', availableSpace);
                }
            }
        });

        $scope.save = function () {
            $scope.errorMessage = '';
            $scope.successMessage = '';

            if (!$scope.file.id && !$scope.file.tmpPath && !$scope.file.external) {
                $scope.errorMessage = localization.localize('error.file.empty');
            } else {
                var request = {};
                for (var prop in $scope.file) {
                    if ($scope.file.hasOwnProperty(prop)) {
                        request[prop] = $scope.file[prop];
                    }
                }

                fileService.updateFile(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close(response.data);
                    } else {
                        $scope.errorMessage = localization.localize(response.message);
                    }
                });
            }
        };

        $scope.onStartedUpload = function () {
            $scope.loading = true;
        };

        $scope.onUploadProgress = function(progress) {
            var loadedMb = (progress.loaded / 1048576).toFixed(1);
            var totalMb = (progress.total / 1048576).toFixed(1);
            $scope.successMessage = localization.localize('success.uploading.file') +
                " " + loadedMb + " / " + totalMb + " Mb";
        };

        $scope.fileUploaded = function (response) {
            $scope.errorMessage = '';
            $scope.successMessage = '';

            $scope.loading = false;

            if (response.data.status === 'OK') {
                $scope.file.filePath = response.data.data.name;
                $scope.file.tmpPath = response.data.data.serverPath;
                $scope.successMessage = localization.localize('success.file.uploaded');
            } else if (response.data.message == 'error.size.limit.exceeded') {
                $scope.errorMessage = localization.localize(response.data.message) + ' (' + response.data.data + ' Mb)';
            } else {
                $scope.errorMessage = localization.localize(response.data.message);
            }
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    })
    .controller('FileConfigurationsModalController', function ($scope, $modalInstance, fileService, file, localization) {
        $scope.file = angular.copy(file);

        var loadData = function () {
            fileService.getConfigurations({"id": file.id}, function (response) {
                if (response.data) {
                    $scope.configurations = response.data;
                }
            });
        };
        $scope.file.fileName = file.description ? file.description :
            (file.external ? file.url : file.filePath);

        $scope.configurations = [];
        loadData();

        $scope.selectionChanged = function(configuration) {
            configuration.notify = true;
        };

        $scope.save = function () {
            $scope.errorMessage = '';

            var request = {"fileId": file.id};

            var configurations = [];
            for (var i = 0; i < $scope.configurations.length; i++) {
                configurations.push($scope.configurations[i]);
            }

            request.configurations = configurations;

            fileService.updateConfigurations(request, function (response) {
                if (response.status === 'OK') {
                    $modalInstance.close();
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }

    });