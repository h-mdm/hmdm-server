angular.module('headwind-kiosk')
    .factory('passwordService', function (localization) {

        return {
            checkQuality: function(password, length, strength) {
                if (password.length < length) {
                    return false;
                }

                if (strength === 0) {
                    return true;
                }

                var hasDigits = /\d/g.test(password);
                var hasLower = /[a-z]/g.test(password);
                var hasCaps = /[A-Z]/g.test(password);


                if (strength === 1) {
                    return hasDigits && hasLower && hasCaps;
                }

                if (strength === 2) {
                    var hasSpecial = /[_\-.,!#$%()=+;*\/]/g.test(password);
                    return hasDigits && hasLower && hasCaps && hasSpecial;
                }
                // Reserved
                return false;

            },
            qualityMessage: function(length, strength) {
                var message = '';
                if (length > 0) {
                    message += localization.localize('form.password.length').replace('${length}', length);
                }
                if (strength == 1) {
                    message += localization.localize('form.settings.misc.password.alphanumeric');
                } else if (strength == 2) {
                    message += localization.localize('form.settings.misc.password.specialchar');
                }
                return message;
            }

        }
    });
