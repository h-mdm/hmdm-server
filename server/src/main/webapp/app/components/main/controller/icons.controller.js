// Localization completed
angular.module('headwind-kiosk')
    .controller('IconsTabController', function ($scope, $rootScope, $state, $modal, alertService, confirmModal,
                                                 iconService, $window, localization) {
        $scope.search = {};

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.$watch('paging.currentPage', function () {
            $window.scrollTo(0, 0);
        });

        $scope.init = function () {
            $rootScope.settingsTabActive = true;
            $rootScope.pluginsTabActive = false;
            $scope.paging.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            iconService.getAllIcons({value: $scope.search.searchValue},
                function (response) {
                    $scope.icons = response.data;
                });
        };

        $scope.editIcon = function (icon) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/addIcon.html',
                controller: 'IconModalController',
                resolve: {
                    icon: function () {
                        return icon;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.removeIcon = function (icon) {
            let localizedText = localization.localize('question.delete.icon').replace('${iconName}', icon.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                iconService.removeIcon({id: icon.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else {
                        alertService.showAlertMessage(localization.localize('error.internal.server'));
                    }
                });
            });
        };

        $scope.init();
    })
    .controller('IconModalController', function ($scope, $modalInstance, iconService, fileService, icon, localization) {
        $scope.icon = {};
        for (var prop in icon) {
            if (icon.hasOwnProperty(prop)) {
                $scope.icon[prop] = icon[prop];
            }
        }

        $scope.files = [];
        fileService.getAllFiles({},
            function (response) {
                if (response.status === 'OK') {
                    // Exclude non-image files
                    response.data = response.data.filter(f => f.filePath != null &&
                        (f.filePath.endsWith(".jpg") || f.filePath.endsWith(".png") || f.filePath.endsWith(".jpeg")));

                    response.data.forEach(function (file) {
                        file.name = file.description ? file.description :
                            file.external ? file.url : file.filePath;
                        if (file.external) {
                            file.externalUrl = file.url;
                        }
                        if (file.id === $scope.icon.fileId) {
                            $scope.file = file;
                        }
                    });
                    $scope.files = response.data;
                } else {
                    $scope.errorMessage = localization.localizeServerResponse(response);
                }
            });

        $scope.save = function () {
            $scope.successMessage = '';
            $scope.errorMessage = '';

            $scope.icon.fileId = $scope.file.id;
            if (!$scope.icon.name) {
                $scope.errorMessage = localization.localize('error.icon.empty.name');
            } else if (!$scope.icon.fileId) {
                    $scope.errorMessage = localization.localize('error.icon.empty.file');
            } else {
                var request = {};
                for (var prop in $scope.icon) {
                    if ($scope.icon.hasOwnProperty(prop)) {
                        request[prop] = $scope.icon[prop];
                    }
                }

                iconService.createIcon(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localize('error.duplicate.icon.name');
                    }
                });
            }
        };

        $scope.cancel = function () {
            $modalInstance.dismiss();
        }
    });