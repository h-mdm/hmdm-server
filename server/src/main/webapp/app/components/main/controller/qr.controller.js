// Localization completed
angular.module('headwind-kiosk')
    .controller('QRController', function ($state, $scope, $window, $stateParams, $http,
                                          localization, hintService, groupService, $timeout) {
        $scope.size = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);
        $scope.deviceId = $stateParams.deviceId;

        $scope.formData = {
            deviceIdNew: $stateParams.deviceId,
            groups: null
        };

        $scope.qrCodeKey = $stateParams.qrCode;
        $scope.devices = [];
        $scope.device = {};
        $scope.showQR = true;
        $scope.showHelp = false;
        $scope.helpSize = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);

        $scope.renew = function () {
            $scope.showQR = false;
            $scope.size = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);
            $scope.deviceId = $scope.formData.deviceIdNew;
            generateQrUrl();
            if ($scope.jsonData) {
                $scope.generateJson();
            }
            $scope.showQR = true;
        };

        $scope.groupsList = [];

        $scope.groupsSelection = ($scope.formData.groups || []).map(function (group) {
            return {id: group.id};
        });

        groupService.getAllGroups(function (response) {
            $scope.groups = response.data;
            $scope.groupsList = response.data.map(function (group) {
                return {id: group.id, label: group.name};
            });
        });

        $scope.groupsSelectionEvents = {
            onItemSelect: function(item) { $scope.renew(); },
            onItemDeselect: function(item) { $scope.renew(); },
            onSelectAll: function() { $scope.renew(); },
            onDeselectAll: function() { $scope.renew(); }
        };

        var urlPart = function() {
            var res = "";
            if ($scope.formData.create) {
                res += "&create=1";
                if ($scope.formData.useId) {
                    res += "&useId=" + $scope.formData.useId;
                }
                for (var i = 0; i < $scope.groupsSelection.length; i++) {
                    var group = $scope.groupsSelection[i];
                    res += "&group=" + encodeURI(group.id);
                }
            }
            return res;
        };

        var generateQrUrl = function() {
            $scope.qrCodeUrl = "rest/public/qr/" + $scope.qrCodeKey + "?size=" + $scope.size;
            if ($scope.deviceId !== null) {
                $scope.qrCodeUrl += "&deviceId=" + $scope.deviceId;
            }
            $scope.qrCodeUrl += urlPart();
        };
        generateQrUrl();

        $scope.generateJson = function() {
            var url = "rest/public/qr/json/" + $scope.qrCodeKey + "?" + urlPart().substring(1);
            $http.get(url)
                .then(function (response) {
                    if (response.status === 200) {
                        $scope.jsonData = response.data;
                    }
                });
        };

        $scope.tableFilteringTexts = {
            'buttonDefaultText': localization.localize('table.filtering.no.selected.group'),
            'checkAll': localization.localize('table.filtering.check.all'),
            'uncheckAll': localization.localize('table.filtering.uncheck.all'),
            'dynamicButtonTextSuffix': localization.localize('table.filtering.suffix.group')
        };

        // angular.element($window).bind('resize', function(){
        //     $scope.helpSize = (Math.min($window.innerWidth, $window.innerHeight) * 0.80).toFixed(0);
        // });

        $timeout(function () {
            hintService.onStateChangeSuccess();
        }, 100);

        $scope.helpClicked = function () {
            $scope.showHelp = !$scope.showHelp;
        };
    });


