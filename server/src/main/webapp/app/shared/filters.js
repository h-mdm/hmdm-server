// Localization completed
angular.module( 'headwind-kiosk' )
.filter( 'deviceFilter', function() {
    return function( input, value ) {
        if ( !value )
            return input;

        var result = [];
        value = value.toLowerCase();
        for ( var i = 0; i < input.length; i++ ) {
            if ( ( input[ i ].deviceName && input[ i ].deviceName.toLowerCase().indexOf( value ) !== -1 ) ||
                 ( input[ i ].id && input[ i ].id.toLowerCase().indexOf( value ) !== -1 ) )
                 result.push( input[ i ] );
        }

        return result;
    }
} );
