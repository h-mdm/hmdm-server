// Localization completed
angular.module('headwind-kiosk')
    .factory('paginationService', function () {
        let paginationConfig = {};
        let array;
        let outScope;

        let initAjaxPaginationTable = function (scope, data, itemsOnPage) {
            paginationConfig.itemsOnPage = itemsOnPage;

            paginationConfig.totalItems = data.total;
            paginationConfig.currentPage = 1;
            paginationConfig.ajax = true;
            outScope = scope;

            updatePaginationLabel();
            return paginationConfig;
        };

        let initPaginationTable = function (scope, outArray, itemsOnPage) {
            if (itemsOnPage)
                paginationConfig.itemsOnPage = itemsOnPage;
            else
                paginationConfig.itemsOnPage = 100;

            paginationConfig.totalItems = outArray.length;
            paginationConfig.currentPage = 1;
            array = outArray;

            outScope = scope;

            return paginationConfig;
        };

        let updatePaginationLabel = function () {
            outScope.totalRecord = paginationConfig.totalItems;
            outScope.firstRecord = 1 + (paginationConfig.currentPage - 1) * paginationConfig.itemsOnPage;
            outScope.lastRecord = paginationConfig.currentPage * paginationConfig.itemsOnPage;

            if (outScope.lastRecord > outScope.totalRecord)
                outScope.lastRecord = outScope.totalRecord;
        };

        let getPageContent = function (currentPage) {
            let left = (currentPage - 1) * paginationConfig.itemsOnPage;
            let right = currentPage * paginationConfig.itemsOnPage;

            updatePaginationLabel();
            return array.slice(left, right);
        };

        let setPage = function () {
            updatePaginationLabel();
        };

        let setData = function (data) {
            array = data;
        };

        return {
            initPaginationTable: initPaginationTable,
            initAjaxPaginationTable: initAjaxPaginationTable,
            getPageContent: getPageContent,
            setData: setData,
            setPage: setPage
        }
    });