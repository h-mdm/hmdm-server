/*
 *
 * Headwind MDM: Open Source Android MDM Software
 * https://h-mdm.com
 *
 * Copyright (C) 2019 Headwind Solutions LLC (http://h-sms.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// Localization completed
angular.module('headwind-kiosk')
    .factory('hintService', function (localization, httpHintService, $timeout) {
        var hints = [];
        var hintKeys = {};

        var shownHints = {
        };

        var intro = introJs();
        intro.setOptions({
            "hidePrev": true,
            "hideNext": true,
            "prevLabel": " << ",
            "nextLabel": " >> ",
            "skipLabel": 'OK',
            "doneLabel": 'OK',
            "showButtons": true,
            "showBullets": false,
            "showProgress": false,
            "scrollToElement": true,
            "exitOnEsc": true,
            "exitOnOverlayClick": true,
            "disableInteraction": true,
            "showStepNumbers": false,
        });

        var clear = function () {
            hints  = [];
            hintKeys = {};
            nonAddedHints = {};
        };

        intro.onchange(function (element) {
            var hintKey = angular.element(element).attr("data-hint-key");

            if (!shownHints[hintKey]) {
                addShownHint(hintKey);
                httpHintService.add(hintKey, function (response) {
                    if (response.status !== 'OK') {
                        console.error("Failed to mark hint as shown for current user",
                            localization.localizeServerResponse(response));
                    }
                }, function (response) {
                    console.error("Error when sending request to server", response);
                })
            }
        });

        intro.oncomplete(clear);

        // Tells if specified hint should be presented to user
        var isHintAvailable = function (hintKey) {
            return !shownHints[hintKey] && !hintKeys[hintKey];
        };

        var addShownHint = function (hintKey) {
            shownHints[hintKey] = true;
        };

        var initialized = false;
        var nonAddedHints = {};

        var init =  function () {
            clear();
            shownHints = {};
            initialized = false;

            httpHintService.list({}, function (response) {
                if (response.status === 'OK') {
                    response.data.forEach(addShownHint) ;
                } else {
                    console.error("Failed to get list of shown hints", localization.localizeServerResponse(response));
                }
                initialized = true;
            }, function (response) {
                console.error("Error when sending request to server", response);
                initialized = true;
            });

        };

        var enableHints = function (callback, errorCallback) {
            httpHintService.enable({}, function (response) {
                init();
                callback(response);
            }, errorCallback);
        };

        var disableHints = function (callback, errorCallback) {
            httpHintService.disable({}, function (response) {
                init();
                callback(response);
            }, errorCallback);
        };

        var addHintFunc = function (hintKey) {
            if (isHintAvailable(hintKey)) {
                var element = document.querySelectorAll('[data-hint-key="' + hintKey + '"]')[0];
                var hintOrder = angular.element(element).attr("data-hint-order") || 1000;

                hintKeys[hintKey] = true;
                hints.push({
                    element: element,
                    intro: localization.localize(hintKey),
                    position: 'bottom',
                    hintOrder: hintOrder
                });
            }
        };

        var onStateChangeSuccessFunc = function () {
            intro.exit(true);

            hints.sort(function (o1, o2) {
                return o1.hintOrder - o2.hintOrder;
            });

            intro.setOptions({
                steps: hints
            });

            if (hints.length > 0) {
                intro.start();
            }
        };

        var b = {
            init: init,
            onLogin: init,
            onLogout: function () {
                intro.exit(true);
                clear();
                shownHints = {};
                initialized = false;
            },
            enableHints: enableHints,
            disableHints: disableHints,
            onStateChangeStart: function () {
                clear();
            },
            onStateChangeSuccess: function () {
                var nonAddedHintsExist = false;
                for (var p in nonAddedHints) {
                    if (nonAddedHints.hasOwnProperty(p)) {
                        nonAddedHintsExist = true;
                        break;
                    }
                }
                
                if (!initialized || nonAddedHintsExist) {
                    $timeout(onStateChangeSuccessFunc, 50);
                } else {
                    onStateChangeSuccessFunc();
                }
            },
            addHint: function (hintKey) {
                if (!initialized) {
                    nonAddedHints[hintKey] = true;
                    $timeout(function () {
                        b.addHint(hintKey);
                    }, 100);
                } else {
                    delete nonAddedHints[hintKey];
                    addHintFunc(hintKey);
                }
            }
        };

        return b;
    })
    .factory('httpHintService', function ($resource) {
        return $resource('rest/private/users', {}, {
            list: {url: 'rest/private/hints/history', method: 'GET'},
            enable: {url: 'rest/private/hints/enable', method: 'POST'},
            disable: {url: 'rest/private/hints/disable', method: 'POST'},
            add: {url: 'rest/private/hints/history', method: 'POST'},
        });
    })
    .directive('hintKey', function (hintService) {
        return {
            restrict: 'A',
            scope: true,
            link: function (scope, element, attrs) {
                var hintKey = attrs.hintKey;
                hintService.addHint(hintKey);
            }
        };
    });

