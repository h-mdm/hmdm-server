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
    .factory('hmdmMap', function () {

        const mapInstance = function () {
            // A reference to a Leaflet map object
            var map;
            // var markersLayer;
            // var selectedMarkerLayer;

            /**
             * Initializes the map and binds it to specified document element.
             *
             * @param scope a current scope
             * @param mapId an ID of a document element to bind map to.
             * @param tileServerUrl an URL referencing the tile server.
             * @returns {*}
             */
            var initMap = function (scope, mapId, tileServerUrl) {
                // markersLayer = undefined;
                // selectedMarkerLayer = undefined;

                var southWest = L.latLng(-90, -180);
                var northEast = L.latLng(90, 180);
                var bounds = L.latLngBounds(southWest, northEast);

                initMapAndLayers(mapId, bounds, tileServerUrl);
                // initMapClickAction(addMarkerPossibility);
                initOnZoomHandler();
                // initOnDragMarkerHandler();
                // initFullScreenHandler(hideFullScreen, mapId);

                L.control.scale().addTo(map);

                scope.$on('$destroy', disposeMap);

                return map;
            };

            /**
             * Initializes map instance by binding it to document element referenced by the specified ID.
             *
             * @param mapId a document element ID to bind map to.
             * @param bounds a bounds of the map to be displayed.
             * @param tileServerUrl an URL referencing the tile server.
             */
            var initMapAndLayers = function (mapId, bounds, tileServerUrl) {
                var osm = L.tileLayer(tileServerUrl, {
                    attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors',
                    noWrap: false,
                    minZoom: 2
                });

                // map = L.map(mapId, {center: [0, 0], zoom: 4, maxBounds: bounds});
                map = L.map(mapId, {center: [0, 0], zoom: 2});
                osm.addTo(map);

            };

            /**
             * Initializes a handler for "zoom" related events for map.
             */
            var initOnZoomHandler = function () {
                map.on('zoomend', onZoomEndHandler);
            };

            /**
             * A handler for "zoomend" event from map. Adjusts the dimensions of the marker icons based on zoom level.
             */
            var onZoomEndHandler = function () {
                map.eachLayer(function (layer) {
                    if (layer.options && layer.options.icon) {
                        layer.setIcon(zoomMarkerIcon(layer.options.icon));
                    }

                    if (layer._layers) {
                        for (var prop in layer._layers) {
                            if (layer._layers.hasOwnProperty(prop)) {
                                var innerLayer = layer._layers[prop];
                                if (innerLayer.options && innerLayer.options.icon) {
                                    innerLayer.setIcon(zoomMarkerIcon(innerLayer.options.icon));
                                }
                            }
                        }
                    }
                });
            };

            /**
             * Evaluates new dimensions and parameters for specified marker icon based on map zoom level and returns the
             * new marker icon to be displayed instead of provided one.
             *
             * @param icon a marker icon to be handled..
             * @returns {*} an updated icons with dimensions adjusted based on map's zoom level.
             */
            var zoomMarkerIcon = function (icon) {
                var multiplier = Math.pow(map.getZoom() / 8, 0.5);
                if (multiplier < 0.5) {
                    multiplier = 0.5;
                }

                if (multiplier > 1) {
                    multiplier = 1;
                }

                updateIconParam(icon, 'iconSize', multiplier);
                updateIconParam(icon, 'shadowSize', multiplier);
                updateIconParam(icon, 'iconAnchor', multiplier);
                updateIconParam(icon, 'popupAnchor', multiplier);

                return icon;
            };

            /**
             * Applies the specified multiplier to specified property of marker icon.
             *
             * @param icon a marker icon to be handled..
             * @param param a name of icon property.
             * @param multiplier a multiplier to be applied to icon option value.
             */
            var updateIconParam = function (icon, param, multiplier) {
                if (!icon.options[param]) {
                    return;
                }

                var value;
                if (!icon[param]) {
                    value = icon.options[param];
                    icon[param] = value;
                } else {
                    value = icon[param];
                }

                icon.options[param] = [value[0] * multiplier, value[1] * multiplier];
            };

            // The markers added to map so far. Maps marker identifier to marker object
            const markers = {};

            // Polylines added to map so far.
            const polylines = {};

            /**
             * Creates a new icon with specified properties.
             *
             * @param markerIconOptions a marker icon options to use for icon customization.
             * @returns {*} a new icon instance.
             */
            var getMarkerIcon = function (markerIconOptions) {
                return zoomMarkerIcon(L.icon(markerIconOptions));
            };

            /**
             * Adds new marker to map.
             *
             * @param identifier a marker identifier.
             * @param {number} lat a latitude coordinate for marker location.
             * @param {number} lon a longitude coordinate for marker location.
             * @param {object} markerIconOptions a marker icon options to use for icon customization.
             * @param {string} title an optional title of the marker.
             * @param {string} popupTemplate an optional popup template for marker.
             * @param {Function} popupOpenHandler an optional handler for 'popupopen' event.
             * @param {Function} popupCloseHandler an optional handler for 'popupclose' event.
             * @param {Function} clickHandler an optional handler for 'click' event.
             * @returns {object} a marker created.
             */
            var addMarker = function (identifier, lat, lon, markerIconOptions, title, popupTemplate,
                                      popupOpenHandler, popupCloseHandler, clickHandler) {
                const markerIcon = getMarkerIcon(markerIconOptions);
                const marker = L.marker([lat, lon], {icon: markerIcon, "title": title || ""});

                // If need to show a label near marker
                // marker.bindTooltip("" + new Date(), {permanent: true});

                if (popupTemplate) {
                    marker.bindPopup(popupTemplate);
                }

                if (clickHandler) {
                    marker.on('click', clickHandler);
                }

                if (popupOpenHandler) {
                    marker.on('popupopen', popupOpenHandler);
                }
                if (popupCloseHandler) {
                    marker.on('popupclose', popupCloseHandler);
                }

                marker.hmdmProperties = {identifier: identifier};

                marker.addTo(map);

                markers[identifier] = marker;

                return marker;
            };

            /**
             * Removes specified marker from map.
             *
             * @param identifier a marker identifier.
             */
            var removeMarker = function (identifier) {
                if (isMarkerDisplayed(identifier)) {
                    const marker = markers[identifier];
                    delete markers[identifier];
                    map.removeLayer(marker);
                }
            };

            /**
             * Centers the map on specified coordinates.
             *
             * @param {number} lat a latitude coordinate.
             * @param {number} lon a longitude coordinate.
             */
            var centerMap = function (lat, lon) {
                map.panTo(new L.LatLng(lat, lon));
            };

            /**
             * Checks if specified marker is currently present on map.
             *
             * @param identifier a marker identifier.
             * @returns {boolean} true if requested marker is currently displayed on map; false if there is no such marker.
             */
            var isMarkerDisplayed = function (identifier) {
                return markers.hasOwnProperty(identifier);
            };

            /**
             * Centers the map on location of specified marker.
             *
             * @param identifier a marker identifier.
             */
            var centerMapOnMarker = function (identifier) {
                if (isMarkerDisplayed(identifier)) {
                    const marker = markers[identifier];

                    const location = marker.getLatLng();
                    centerMap(location.lat, location.lng);
                }
            };

            var addPolyline = function (identifier, points, options) {
                const polyline = L.polyline(points, options);

                polyline.hmdmProperties = {identifier: identifier};

                polyline.addTo(map);

                polylines[identifier] = polyline;

                return polyline;
            };

            /**
             * Checks if specified polyline is currently present on map.
             *
             * @param identifier a polyline identifier.
             * @returns {boolean} true if requested polyline is currently displayed on map; false if there is no such polyline.
             */
            var isPolylineDisplayed = function (identifier) {
                return polylines.hasOwnProperty(identifier);
            };

            /**
             * Removes specified polyline from map.
             *
             * @param identifier a polyline identifier.
             */
            var removePolyline = function (identifier) {
                if (isPolylineDisplayed(identifier)) {
                    const polyline = polylines[identifier];
                    delete polylines[identifier];
                    map.removeLayer(polyline);
                }
            };

            const removeAllPolylines = function () {
                angular.forEach(polylines, function (polyline, identifier) {
                    map.removeLayer(polyline);
                    delete polylines[identifier];
                });
            };

            /**
             * Locates the specified marker and passes it to specified handler for further manipulations.
             *
             * @param identifier a marker identifier.
             * @param {Function} markerHandler a callback function to be provided with selected marker.
             */
            const selectMarker = function(identifier, markerHandler) {
                const marker = markers[identifier];
                markerHandler(marker);
            };

            const fitBounds = function( points ) {
                map.fitBounds( points, { padding: [ 30, 30 ] } );
            };

            const fitBoundsToMarkers = function() {
                const points = [];
                angular.forEach(markers, function (marker) {
                    points.push(marker.getLatLng());
                });

                fitBounds(points);
            };

            const disposeMap = function () {
                map.remove();
            };

            const removeAllMarkers = function () {
                angular.forEach(markers, function (marker, identifier) {
                    map.removeLayer(marker);
                    delete markers[identifier];
                });
            };

            return {
                fitBoundsToMarkers: fitBoundsToMarkers,
                initMap: initMap,
                addMarker: addMarker,
                removeMarker: removeMarker,
                centerMap: centerMap,
                isMarkerDisplayed: isMarkerDisplayed,
                centerMapOnMarker: centerMapOnMarker,
                selectMarker: selectMarker,
                createMarkerIcon: getMarkerIcon,
                dispose: disposeMap,
                removeAllMarkers: removeAllMarkers,
                addPolyline: addPolyline,
                removePolyline: removePolyline,
                isPolylineDisplayed: isPolylineDisplayed,
                removeAllPolylines: removeAllPolylines,
            }
        };

        return {
            get: function () {
                return mapInstance();
            }
        }
    })
;

