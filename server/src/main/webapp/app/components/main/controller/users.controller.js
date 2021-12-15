// Localization completed
angular.module('headwind-kiosk')
    .controller('UsersTabController', function ($scope, $rootScope, $timeout, $state, userService, $modal, confirmModal,
                                                alertService, authService, $window, localization) {

        $scope.search = {};

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.$watch('paging.currentPage', function () {
            $window.scrollTo(0, 0);
        });

        $scope.users = [];
        $scope.currentUser = {};
        userService.getCurrent(function (response) {
            if (response.data) {
                $scope.currentUser = response.data;
            }
        });

        $scope.init = function () {
            $rootScope.settingsTabActive = true;
            $rootScope.pluginsTabActive = false;
            userService.getAll(function (response) {
                if (response.data) {
                    $scope.users = response.data;
                }
            });

        };

        $scope.search = function () {
            userService.getAll({filter: $scope.search.searchValue},
                function (response) {
                    $scope.users = response.data;
                });
        };

        $scope.editUser = function (user) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/user.html',
                controller: 'UserModalController',
                resolve: {
                    user: function () {
                        return user;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.removeUser = function (user) {
            let localizedText = localization.localize('question.delete.user').replace('${username}', user.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                userService.remove({id: user.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else {
                        alertService.showAlertMessage(localization.localize('error.internal.server'));
                    }
                });
            });
        };

        $scope.loginAs = function (user) {
            let localizedText = localization.localize('question.change.user').replace('${userName}', user.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                userService.loginAs({id: user.id}, function (response) {
                    if (response.status === 'OK') {
                        var user = response.data;
                        authService.update(user);
                        $state.transitionTo( 'main' );
                        $rootScope.$emit('aero_USER_AUTHENTICATED');
                    } else {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                });
            });
        };

        $scope.init();
    })
    //*******************************************************************************************************************
    .controller('UserModalController', function ($scope, $modalInstance, userService, user, groupService,
                                                 localization, settingsService, passwordService) {
        $scope.groupsList = [];

        groupService.getAllGroups(function (response) {
            $scope.groups = response.data;
            $scope.groupsList = response.data.map(function (group) {
                return {id: group.id, label: group.name};
            });
        });

        settingsService.getSettings(function (response) {
            if (response.data) {
                $scope.settings = response.data;
                $scope.qualityMessage = passwordService.qualityMessage($scope.settings.passwordLength, $scope.settings.passwordStrength);
            }
        });

        $scope.groupsSelection = (user.groups || []).map(function (group) {
            return {id: group.id};
        });

        $scope.tableFilteringTexts = {
            'buttonDefaultText': localization.localize('table.filtering.no.selected.group'),
            'checkAll': localization.localize('table.filtering.check.all'),
            'uncheckAll': localization.localize('table.filtering.uncheck.all'),
            'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.group')
        };

        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.user = {
            userRole: {}
        };

        $scope.userRoles = [];

        userService.getUserRoles(function (response) {
            if (response.status === 'OK') {
                $scope.userRoles = response.data;
            } else {
                console.error('Failed to get the list of user roles: ', response.message);
            }
        });

        for (var prop in user) {
            if (user.hasOwnProperty(prop)) {
                $scope.user[prop] = user[prop];
            }
        }

        $scope.save = function () {
            $scope.errorMessage = '';

            if (($scope.user.newPassword || $scope.user.confirm) && $scope.user.newPassword !== $scope.user.confirm) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.mismatch.password');
            } else if (($scope.user.newPassword || $scope.user.confirm) &&
                !passwordService.checkQuality($scope.user.newPassword, $scope.settings.passwordLength, $scope.settings.passwordStrength)) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.password.weak');
            } else if (!$scope.user.login) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.empty.user.login');
            } else if (!$scope.user.name) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.empty.user.name');
            } else if (!$scope.user.userRole.id) {
                resetMessages();
                $scope.errorMessage = localization.localize('error.empty.user.role');
            } else {
                var request = {};
                for (var prop in $scope.user) {
                    if ($scope.user.hasOwnProperty(prop)) {
                        request[prop] = $scope.user[prop];
                    }
                }
                if ($scope.user.newPassword && $scope.user.confirm) {
                    request["newPassword"] = md5($scope.user.newPassword).toUpperCase();
                    request["confirmModal"] = md5($scope.user.confirm).toUpperCase();
                } else {
                    request["newPassword"] = undefined;
                    request["confirmModal"] = undefined;
                }

                if ($scope.user.allDevicesAvailable) {
                    request.groups = null;
                } else {
                    request["allDevicesAvailable"] = false;
                    request.groups = $scope.groupsSelection;
                }

                userService.update(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                });
            }
        };


        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    });