module.exports = function( grunt ) {
    grunt.initConfig( {
        pkg: grunt.file.readJSON( 'package.json' ),

        // install bower dependencies
        // options: https://github.com/yatskevich/grunt-bower-task
        bower: {
            install: {}
        },

        // grunt clean task
        // options: https://github.com/gruntjs/grunt-contrib-clean
        clean: {
            dist: {
                options: { force: true },
                build: [ 'bower_components', 'lib', '../src/main/webapp/lib' ]
            }
        },

        // grunt copy task
        // options: https://github.com/gruntjs/grunt-contrib-copy
        copy : {
            index : {
                files: [ { expand: true, cwd: 'lib/', src: [ '**/*',  '!**/bootstrap-css-only/**' ], dest: '../src/main/webapp/lib' },
                         { expand: true, cwd: 'lib/bootstrap-css-only/', src: [ '*.css' ], dest: '../src/main/webapp/lib/bootstrap-css-only/css/' },
                         { expand: true, cwd: 'lib/bootstrap-css-only/', src: [ 'glyphicons*' ], dest: '../src/main/webapp/lib/bootstrap-css-only/fonts/' } ]
            }
        }
    });

    grunt.loadNpmTasks( 'grunt-bower-task' );
    grunt.loadNpmTasks( 'grunt-contrib-clean' );
    grunt.loadNpmTasks( 'grunt-contrib-copy' );

    grunt.registerTask( 'resolve', [ 'clean', 'bower:install', 'copy' ] );
}
