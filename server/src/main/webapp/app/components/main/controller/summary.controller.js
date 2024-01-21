// Localization completed
angular.module('headwind-kiosk')
    .controller('SummaryTabController', function ($scope, localization, summaryService) {
        $scope.stat = undefined;
        $scope.errorMessage = undefined;

        $scope.enrollmentLabels = [
            localization.localize('summary.devices.enrolled.earlier'),
            localization.localize('summary.devices.enrolled.monthly')
        ];
        $scope.enrollmentColors = [
            '#DCDCDC',
            '#97BBCD'
        ];

        $scope.statusLabels = [
            localization.localize('summary.devices.offline'),
            localization.localize('summary.devices.idle'),
            localization.localize('summary.devices.active')
        ];
        $scope.statusColors = [
            '#F7464A',
            '#FDB45C',
            '#46BFBD'
        ];

        $scope.installLabels = [
            localization.localize('summary.devices.installation.failed'),
            localization.localize('summary.devices.version.mismatch'),
            localization.localize('summary.devices.installation.completed')
        ];
        $scope.installColors = $scope.statusColors;

        $scope.monthlyEnrollColors = [];
        for (var i = 0; i < 12; i++) {
            $scope.monthlyEnrollColors.push('#97BBCD');
        }

        $scope.statusByConfigSeries = [
            localization.localize('summary.devices.offline'),
            localization.localize('summary.devices.idle'),
            localization.localize('summary.devices.active')
        ];
        $scope.statusByConfigColors = [
            '#F7464A', '#FDB45C', '#46BFBD'
        ];

        $scope.installByConfigSeries = [
            localization.localize('summary.devices.installation.failed'),
            localization.localize('summary.devices.version.mismatch'),
            localization.localize('summary.devices.installation.completed')
        ];
        $scope.installByConfigColors = $scope.statusByConfigColors;

        summaryService.getDeviceStat(function (response) {
            var devicesEnrolledEarlier = response.data.devicesEnrolled - response.data.devicesEnrolledLastMonth;
            if (devicesEnrolledEarlier < 0) {
                devicesEnrolledEarlier = 0;
            }
            $scope.enrollmentData = [devicesEnrolledEarlier, response.data.devicesEnrolledLastMonth];
            $scope.statusData = [0, 0, 0];
            $scope.installData = [0, 0, 0];

            response.data.statusSummary.forEach(function (item, index) {
                if (item.stringAttr === 'red') {
                    $scope.statusData[0] = item.number;
                } else if (item.stringAttr === 'yellow') {
                    $scope.statusData[1] = item.number;
                } else if (item.stringAttr === 'green') {
                    $scope.statusData[2] = item.number;
                }
            });

            response.data.installSummary.forEach(function (item, index) {
                if (item.stringAttr === 'FAILURE') {
                    $scope.installData[0] = item.number;
                } else if (item.stringAttr === 'VERSION_MISMATCH') {
                    $scope.installData[1] = item.number;
                } else if (item.stringAttr === 'SUCCESS') {
                    $scope.installData[2] = item.number;
                }
            });

            $scope.statusByConfigLabels = response.data.topConfigs;
            $scope.statusByConfigData = [];
            $scope.statusByConfigData.push(response.data.statusOfflineByConfig);
            $scope.statusByConfigData.push(response.data.statusIdleByConfig);
            $scope.statusByConfigData.push(response.data.statusOnlineByConfig);

            $scope.installByConfigLabels = $scope.statusByConfigLabels;
            $scope.installByConfigData = [];
            $scope.installByConfigData.push(response.data.appFailureByConfig);
            $scope.installByConfigData.push(response.data.appMismatchByConfig);
            $scope.installByConfigData.push(response.data.appSuccessByConfig);

            $scope.monthlyEnrollLabels = [];
            $scope.monthlyEnrollData = [];
            response.data.devicesEnrolledMonthly.forEach(function (item, index) {
                $scope.monthlyEnrollLabels.push(item.stringAttr);
                $scope.monthlyEnrollData.push(item.number);
            });

        }, function () {
            $scope.errorMessage = localization.localize('error.internal.server');
        });

    });