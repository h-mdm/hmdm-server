// Localization completed
angular.module( 'headwind-kiosk' )
.controller( 'HeaderController', function( $scope, $rootScope, $state, $modal, $timeout, $interval, $filter, $window,
                                           authService, localization, hintService, rebranding ) {
    $scope.isControlPanel = false;
    $scope.authService = authService;
    $scope.showExitReportMode = false;
    $scope.$on( 'START_REPORT_MODE', function() {
        $scope.showExitReportMode = true;
    } );

    $scope.$on( 'HIDE_REPORT_MODE', function() {
        $scope.showExitReportMode = false;
    } );

    $scope.$on( 'HIDE_ADDRESS', function() {
        $scope.mapToolsConfig.showDeviceAddress = false;
    } );

    $scope.$on( 'SHOW_CHECKLIST_INFO', function( event, checklistId ) {
        showWorkResultsContent( checklistId );
    } );

    $scope.$on( 'SHOW_DATA_LOADING_MODAL', function() {
        $scope.dataLoadingWait = true;
    } );

    $scope.$on( 'HIDE_DATA_LOADING_MODAL', function() {
        $scope.dataLoadingWait = false;
    } );

    $rootScope.$on( 'SHOW_EXPIRY_WARNING', function() {
        $scope.expiryWarning = true;
    } );

    rebranding.query(function(value) {
        $scope.appName = value.appName;
    });

    updateDateTime = function() {
        $scope.dateTime = $filter( 'date' )( new Date(), localization.localize('format.date.header') );
    };
    updateDateTime();

    var interval = $interval( updateDateTime, 10000 );
    $scope.$on('$destroy', function() { $interval.cancel( interval ) } );

    $scope.getUserName = function() { return authService.getUserName(); };
    $scope.isAuth = function() { return authService.isLoggedIn() && document.URL.indexOf( 'invoice' ) === -1; };
    $scope.isHidden = function() {
        return $state.current.name === 'qr';
    };

    $scope.isSuperAdmin = function() {
        return authService.isSuperAdmin();
    };

    $scope.logout = function() {
        authService.logout();
        hintService.onLogout();
        $state.transitionTo( 'login' );
        $rootScope.$emit('aero_USER_LOGOUT');
    };

    $scope.isActive = function( state ) {
        return $state.$current.self.name === state;
    };

    $scope.controlPanel = function() {
        $state.transitionTo( 'control-panel' );
        $scope.isControlPanel = true;
    };

    $scope.mainPanel = function() {
        $state.transitionTo( 'main' );
        $scope.isControlPanel = false;
    };

    $scope.about = function () {
        $modal.open({
            templateUrl: 'app/components/about/about.html',
            controller: 'AboutController'
        });
    };
} );
