// Localization completed
angular.module('headwind-kiosk')
    .controller('CustomersTabController', function ($scope, $rootScope, $state, $modal, alertService, confirmModal,
                                                    customerService, authService, $window, localization) {
        if (!authService.isSuperAdmin()) {
            $state.transitionTo( 'main' );
        }

        $scope.sort = {
            by: 'name'
        };

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.$watch('paging.currentPage', function() {
            $window.scrollTo(0, 0);
        });

        $scope.init = function () {
            $scope.paging.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            customerService.getAllCustomers(
                {value: $scope.search.searchValue},
                function (response) {
                    $scope.customers = response.data;
                });
        };

        $scope.loginAs = function (customer) {
            let localizedText = localization.localize('question.impersonate.user').replace('${customerName}', customer.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                customerService.loginAs({id: customer.id}, function (response) {
                    if (response.status === 'OK') {
                        var user = response.data;
                        authService.update(user);
                        $state.transitionTo( 'main' );
                    } else {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                });
            });
        };

        $scope.editCustomer = function (customer) {

            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/customer.html',
                controller: 'CustomerModalController',
                resolve: {
                    customer: function () {
                        return customer;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        };

        $scope.removeCustomer = function (customer) {
            let localizedText = localization.localize('question.delete.customer').replace('${customerName}', customer.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                customerService.removeCustomer({id: customer.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    }
                });
            });
        };

        $scope.changePassword = function (customer) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/control-panel/view/modal/password.html',
                controller: 'PasswordModalController',
                resolve: {
                    customer: function () {
                        return customer;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        }
    })
    .controller("PasswordModalController", function ($scope, customer, alertService, userService, $modalInstance,
                                                     localization) {
        var resetMessages = function () {
            $scope.errorMessage = '';
            $scope.completeMessage = '';
        };

        $scope.errorMessage = '';
        $scope.completeMessage = '';

        $scope.user = {};
        $scope.users = [];

        userService.getAllBySuperAdmin({customerId: customer.id}, function (response) {
            if (response.status === 'OK') {
                $scope.users = response.data;
            } else if (response.status === 'ERROR') {
                $scope.errorMessage = localization.localizeServerResponse(response);
            }
        });

        $scope.cancel = function () {
            $modalInstance.close();
        };

        $scope.save = function () {
            resetMessages();

            if (!$scope.user.id) {
                $scope.errorMessage = localization.localize('error.empty.user');
            } else if (!$scope.user.newPassword || $scope.user.newPassword.length === 0) {
                $scope.errorMessage = localization.localize('error.empty.password');
            } else if (!$scope.user.confirm || $scope.user.confirm.length === 0) {
                $scope.errorMessage = localization.localize('error.empty.password.confirm');
            } else if ($scope.user.newPassword !== $scope.user.confirm) {
                $scope.errorMessage = localization.localize('error.mismatch.password');
            } else {
                var user = {};
                for (var p in $scope.user) {
                    if ($scope.user.hasOwnProperty(p)) {
                        user[p] = $scope.user[p];
                    }
                }

                user.newPassword = user.newPassword ? md5(user.newPassword).toUpperCase() : undefined;
                user.oldPassword = undefined;

                userService.updatePasswordBySuperAdmin(user, function (response) {
                    resetMessages();

                    if (response.status === 'OK') {
                        $scope.completeMessage = localization.localizeServerResponse(response);
                        $modalInstance.close();
                    } else if (response.status === 'ERROR') {
                        $scope.errorMessage = localization.localizeServerResponse(response);
                    }
                });
            }

        };
    })
    // *****************************************************************************************************************
    .controller('CustomerModalController',
        function ($scope, $modalInstance, customerService, customer, alertService, configurationService, localization) {

            $scope.configurationsList = [];

            $scope.configurationsSelection = [];

            $scope.tableFilteringTexts = {
                'buttonDefaultText': localization.localize('table.filtering.no.selected.configuration'),
                'checkAll': localization.localize('table.filtering.check.all'),
                'uncheckAll': localization.localize('table.filtering.uncheck.all'),
                'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.configuration')
            };

            configurationService.getAllConfigurations(function (response) {
                $scope.configurations = response.data;
                $scope.configurationsList = response.data.map(function (config) {
                    return {id: config.id, label: config.name};
                });
            });

            $scope.customer = {};
            for (var prop in customer) {
                if (customer.hasOwnProperty(prop)) {
                    $scope.customer[prop] = customer[prop];
                }
            }

            $scope.save = function () {
                $scope.saveInternal();
            };

            $scope.saveInternal = function () {
                $scope.errorMessage = '';

                if (!$scope.customer.name) {
                    $scope.errorMessage = localization.localize('error.empty.customer.name');
                } else {
                    var request = {};
                    for (var prop in $scope.customer) {
                        if ($scope.customer.hasOwnProperty(prop)) {
                            request[prop] = $scope.customer[prop];
                        }
                    }

                    request.configurationIds = $scope.configurationsSelection.map(function (selection) {
                        return selection.id;
                    });

                    customerService.updateCustomer(request, function (response) {
                        if (response.status === 'OK') {
                            $modalInstance.close();
                            if (response.data['adminCredentials']) {
                                let localizedText = localization.localize('success.admin.created').replace('${adminCredentials}', response.data['adminCredentials']);
                                alertService.showAlertMessage(localizedText);
                            }
                        } else {
                            $scope.errorMessage = localization.localize('error.duplicate.customer.name');
                        }
                    });
                }
            };

            $scope.closeModal = function () {
                $modalInstance.dismiss();
            }
        })
    // *****************************************************************************************************************
    .controller('ControlPanelController', function ($scope, $rootScope, $state, $modal, $interval, confirmModal, deviceService,
                                                    groupService, settingsService, localization) {
        $scope.localization = localization;
    })
    // *****************************************************************************************************************
    .controller('ControlPanelApplicationsTabController', function ($scope, $rootScope, $modal, confirmModal, applicationService,
                                                                   alertService, $window, localization) {
        $scope.search = {};

        $scope.paging = {
            currentPage: 1,
            pageSize: 50
        };

        $scope.$watch('paging.currentPage', function() {
            $window.scrollTo(0, 0);
        });

        $scope.init = function () {
            $scope.paging.currentPage = 1;
            $scope.search();
        };

        $scope.search = function () {
            applicationService.getAllAdminApplications({value: $scope.search.searchValue},
                function (response) {
                    $scope.applications = response.data;
                });
        };

        $scope.removeApplication = function (application) {
            let localizedText = localization.localize('question.delete.application').replace('${applicationName}', application.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                applicationService.removeApplication({id: application.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else if (response.status === 'ERROR') {
                        alertService.showAlertMessage(localization.localize(response.message));
                    }
                });
            });
        };

        $scope.turnIntoCommonApplication = function (application) {
            let localizedText = localization.localize('question.turn2common.application').replace('${applicationName}', application.name);
            confirmModal.getUserConfirmation(localizedText, function () {
                applicationService.turnIntoCommonApplication({id: application.id}, function (response) {
                    if (response.status === 'OK') {
                        $scope.search();
                    } else {
                        alertService.showAlertMessage( localization.localize(response.message) );
                    }
                });
            });
        };

        $scope.editApplication = function (application) {
            var modalInstance = $modal.open({
                templateUrl: 'app/components/main/view/modal/application.html',
                controller: 'ApplicationModalController',
                resolve: {
                    application: function () {
                        return application;
                    },
                    isControlPanel: function () {
                        return true;
                    },
                    closeOnSave: function () {
                        return false;
                    }
                }
            });

            modalInstance.result.then(function () {
                $scope.search();
            });
        }

    });

