// Localization completed
angular.module('headwind-kiosk')
    .factory('utils', function () {
        return {
            compareVersions: function(v1, v2) {
                // Versions are numbers separated by a dot
                var v1d = (v1 || "").replace(/[^\d.]/g, "");
                var v2d = (v2 || "").replace(/[^\d.]/g, "");

                var v1n = v1d.split(".");
                var v2n = v2d.split(".");

                // One version could contain more digits than another
                var count = v1n.length < v2n.length ? v1n.length : v2n.length;

                for (var n = 0; n < count; n++) {
                    var n1 = Number(v1n[n]);
                    var n2 = Number(v2n[n]);
                    if (n1 < n2) {
                        return -1;
                    } else if (n1 > n2) {
                        return 1;
                    }
                    // If major version numbers are equals, continue to compare minor version numbers
                }

                // Here we are if common parts are equal
                // Now we decide that if a version has more parts, it is considered as greater
                if (v1n.length < v2n.length) {
                    return -1;
                } else if (v1n.length > v2n.length) {
                    return 1;
                }
                return 0;            }
        }
    });
