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

package com.hmdm.rest.json;

import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@ApiModel(description = "A request to search the customers account")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerSearchRequest implements Serializable {

    private static final long serialVersionUID = 2687185533062827989L;
    private int currentPage;
    private int pageSize;
    private String searchValue;
    private String sortValue;
    private String sortDirection;
    private Integer accountType;
    private String customerStatus;

    /**
     * <p>Constructs new <code>CustomerSearchRequest</code> instance. This implementation does nothing.</p>
     */
    public CustomerSearchRequest() {
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getSortValue() {
        return sortValue;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

    public String getCustomerStatus() {
        return customerStatus;
    }

    public void setCustomerStatus(String customerStatus) {
        this.customerStatus = customerStatus;
    }

    @Override
    public String toString() {
        return "CustomerSearchRequest{" +
                "currentPage=" + currentPage +
                ", pageSize=" + pageSize +
                ", searchValue='" + searchValue + '\'' +
                ", sortValue='" + sortValue + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", accountType=" + accountType +
                ", customerStatus='" + customerStatus + '\'' +
                '}';
    }

    
}
