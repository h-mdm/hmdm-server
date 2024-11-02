// Localization completed
angular.module('headwind-kiosk')
    .controller('RolesTabController', function ($scope, $rootScope, $state, $modal, alertService, confirmModal,
                                                 roleService, $window, localization) {
        $scope.init = function () {
            $rootScope.settingsTabActive = true;
            $rootScope.pluginsTabActive = false;
            $scope.search();
        };

        $scope.search = function () {
            roleService.getPermissions(
                function (response) {
                    $scope.permissions = response.data;
                });
            roleService.getRoles(
                function (response) {
                    $scope.roles = response.data;
                });
        };

        $scope.editRole = function (role) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/role.html',
                controller: 'RoleModalController',
                resolve: {
                    role: function () {
                        return role;
                    },
                    permissions: function() {
                        return $scope.permissions;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.removeRole = function (role) {
            let localizedText = localization.localize('question.delete.role').replace('${roleName}', role.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                roleService.removeRole({id: role.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else {
                        alertService.showAlertMessage(localization.localize('error.request.failure'));
                    }
                });
            });
        };

        $scope.init();
    })
    .controller('RoleModalController', function ($scope, $modalInstance, roleService, role, permissions, localization) {
        $scope.role = {};
        for (var prop in role) {
            if (role.hasOwnProperty(prop)) {
                $scope.role[prop] = role[prop];
            }
        }
        $scope.permissionList = permissions.map(function (permission) {
            return {
                id: permission.id,
                label: localization.localize('permission.' + permission.name)
            };
        });

        $scope.permissionSelection = (role.permissions || []).map(function (permission) {
            return {id: permission.id};
        });

        $scope.tablePermissionTexts = {
            'buttonDefaultText': localization.localize('table.filtering.no.selected.permission'),
            'checkAll': localization.localize('table.filtering.check.all'),
            'uncheckAll': localization.localize('table.filtering.uncheck.all'),
            'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.permission')
        };

        $scope.save = function () {
            $scope.errorMessage = '';

            if (!$scope.role.name) {
                $scope.errorMessage = localization.localize('error.empty.role.name');
            } else {
                var request = {};
                for (var prop in $scope.role) {
                    if ($scope.role.hasOwnProperty(prop)) {
                        request[prop] = $scope.role[prop];
                    }
                }
                request.permissions = $scope.permissionSelection;

                roleService.updateRole(request, function (response) {
                    if (response.status === 'OK') {
                        $modalInstance.close();
                    } else {
                        $scope.errorMessage = localization.localize('error.duplicate.role.name');
                    }
                });
            }
        };

        $scope.closeModal = function () {
            $modalInstance.dismiss();
        }
    });