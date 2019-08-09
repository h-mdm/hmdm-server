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
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * <p>A DTO to carry the data for a single page for a paginated view.</p>
 *
 * @author isv
 */
@ApiModel(description = "Paginated data")
public class PaginatedData<T> {

    /**
     * <p>A list of collection items for a single page.</p>
     */
    @ApiModelProperty("A list of collection items for a single page")
    private List<T> items;

    /**
     * <p>A total number of items in collection.</p>
     */
    @ApiModelProperty("A total number of items in collection")
    private long totalItemsCount;

    /**
     * <p>Constructs new <code>PaginatedData</code> instance. This implementation does nothing.</p>
     */
    public PaginatedData() {
    }

    public PaginatedData(List<T> items, long totalItemsCount) {
        this.items = items;
        this.totalItemsCount = totalItemsCount;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getTotalItemsCount() {
        return totalItemsCount;
    }

    public void setTotalItemsCount(long totalItemsCount) {
        this.totalItemsCount = totalItemsCount;
    }
}
