// Localization completed
angular.module('headwind-kiosk')
    .controller('GroupsTabController', function ($scope, $rootScope, $state, $modal, alertService, confirmModal,
                                                 groupService, $window, localization) {
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
            groupService.getAllGroups({value: $scope.search.searchValue},
                function (response) {
                    $scope.groups = response.data;
                });
        };

        $scope.editGroup = function (group) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/group.html',
                controller: 'GroupModalController',
                resolve: {
                    group: function () {
                        return group;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.removeGroup = function (group) {
            let localizedText = localization.localize('question.delete.group').replace('${groupName}', group.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                groupService.removeGroup({id: group.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else {
                        alertService.showAlertMessage(localization.localize('error.notempty.group'));
                    }
                });
            });
        };

        $scope.init();
    })
    .controller('GroupModalController', function ($scope, $modalInstance, groupService, group, localization) {
        $scope.group = {};
        for (var prop in group) {
            if (group.hasOwnProperty(prop)) {
                $scope.group[prop] = group[prop];
            }
        }

        $scope.save = function () {
            $scope.errorMessage = '';

            if (!$scope.group.name) {
                $scope.errorMessage = localization.localize('error.empty.group.name');
            } else {
                var request = {};
                for (var prop in $scope.group) {
                    if ($scope.group.hasOwnProperty(prop)) {
                        request[prop] = $scope.group[prop];
                    }
                }

                groupService.updateGroup(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localize('error.duplicate.group.name');
                    }
                });
            }
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    });