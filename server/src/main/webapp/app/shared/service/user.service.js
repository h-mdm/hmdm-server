// Localization completed
angular.module('headwind-kiosk')
    .factory('userService', function ($resource) {
        return $resource('rest/private/users', {}, {
            updatePassword: {url: 'rest/private/users/current', method: 'PUT'},
            updateDetails: {url: 'rest/private/users/details', method: 'PUT'},
            update: {url: 'rest/private/users', method: 'PUT'},
            getCurrent: {url: 'rest/private/users/current', method: 'GET'},
            create: {url: 'rest/private/users/other', method: 'POST'},
            change: {url: 'rest/private/users/other', method: 'PUT'},
            remove: {url: 'rest/private/users/other/:id', method: 'DELETE'},
            getAll: {url: 'rest/private/users/all', method: 'GET'},
            getAllBySuperAdmin: {url: 'rest/private/users/superadmin/all/:customerId', method: 'GET'},
            updatePasswordBySuperAdmin: {url: 'rest/private/users/superadmin/password', method: 'PUT'},
            getAllSummaries: {url: 'rest/private/users/all/summaries', method: 'GET'},
            getUserDetails: {url: 'rest/private/users/:id', method: 'GET'},
            getTypes: {url: 'rest/private/users/types', method: 'GET'},
            validate: {url: 'rest/private/users/validate', method: 'POST'},
            loginAs: {url: 'rest/private/users/impersonate/:id', method: 'GET'},

            getOrganizations: {url: 'rest/private/users/organizations', method: 'GET'},
            createOrganization: {url: 'rest/private/users/organizations', method: 'POST'},
            updateOrganization: {url: 'rest/private/users/organizations', method: 'PUT'},
            removeOrganization: {url: 'rest/private/users/organizations/:id', method: 'DELETE'},
            validateOrganization: {url: 'rest/private/users/organizations/validate', method: 'POST'},

            getPagedAllSummaries: {url: 'rest/private/users/all/summaries', method: 'POST'},
            getPagedOrganizations: {url: 'rest/private/users/organizations/paged', method: 'POST'},

            getUserRoles: {url: 'rest/private/users/roles', method: 'GET'}
        });
    });