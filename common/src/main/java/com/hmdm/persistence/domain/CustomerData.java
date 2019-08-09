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

package com.hmdm.persistence.domain;

import io.swagger.annotations.ApiModelProperty;

/**
 * <p>An interface for domain objects linked to some customer record.</p>
 *
 * @author isv
 */
public interface CustomerData {
    
    /**
     * <p>Gets the ID of this object.</p>
     *
     * @return an ID of this object.
     */
    Integer getId();

    /**
     * <p>Gets the ID of a customer account record this object is linked to.</p>
     *
     * @return an ID of a customer account record.
     */
    int getCustomerId();

    /**
     * <p>Sets the ID of a customer account record this object is linked to.</p>
     *
     * @param customerId an ID of a customer account record.
     */
    void setCustomerId(int customerId);

    /**
     * <p>Checks if this record is accessible to all customers or not.</p>
     *
     * @return <code>true</code> if this record is accessible to all customers; <code>false</code> otherwise.
     */
    @ApiModelProperty(hidden = true)
    default boolean isCommon() {
        return false;
    }
}
