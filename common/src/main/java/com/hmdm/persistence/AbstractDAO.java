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

package com.hmdm.persistence;

import com.hmdm.persistence.domain.CustomerData;
import com.hmdm.persistence.domain.User;
import com.hmdm.security.SecurityContext;
import com.hmdm.security.SecurityException;
import org.apache.ibatis.cursor.Cursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>A base class for DAO implementations. Provides the convenience methods for operating in security context.</p>
 *
 * @param <T> - a type of the domain object maintained by the DAO implementation.
 * @author isv
 */
public abstract class AbstractDAO<T extends CustomerData> {

    /**
     * <p>Constructs new <code>AbstractDAO</code> instance. This implementation does nothing.</p>
     */
    protected AbstractDAO() {
    }

    /**
     * <p>Gets the list of records corresponding to customer account set for current user. Uses specified list retrieval
     * logic for getting the records data.</p>
     *
     * @param listRetrievalLogic a logic to be used for records data retrieval.
     * @return a list of records.
     */
    protected List<T> getList(Function<Integer, List<T>> listRetrievalLogic) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(u -> listRetrievalLogic.apply(u.getCustomerId()))
                .orElse(new ArrayList<>());
    }

    /**
     * <p>Gets the list of records corresponding to customer account set for current user. Uses specified list retrieval
     * logic for getting the records data.</p>
     *
     * @param listRetrievalLogic a logic to be used for records data retrieval.
     * @return a list of records.
     */
    protected List<T> getListWithCurrentUser(Function<User, List<T>> listRetrievalLogic) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(listRetrievalLogic)
                .orElse(new ArrayList<>());
    }

    /**
     * <p>Gets the single record corresponding to current user account. Uses specified data retrieval logic for getting
     * the record data.</p>
     *
     * @param retrievalLogic a logic to be used for record data retrieval.
     * @return a list of records.
     */
    protected T getSingleRecordWithCurrentUser(Function<User, T> retrievalLogic) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(retrievalLogic)
                .orElse(null);
    }

    /**
     * <p>Gets the list of records corresponding to customer account set for current user. Uses specified list retrieval
     * logic for getting the records data.</p>
     *
     * @param listRetrievalLogic a logic to be used for records data retrieval.
     * @return a list of records.
     */
    protected Cursor<T> getIteratorWithCurrentUser(Function<User, Cursor<T>> listRetrievalLogic) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(listRetrievalLogic)
                .orElse(null);
    }

    /**
     * <p>Gets the single record using the provided search logic and verifies it in current security context.</p>
     *
     * @param searchLogic a logic to be used for searching the record.
     * @param exceptionProvider a provider for exception to be thrown in case the access to specified record is denied.
     * @return a record returned by search logic.
     */
    protected T getSingleRecord(Supplier<T> searchLogic, Function<T, SecurityException> exceptionProvider) {
        Optional<T> recordOpt = Optional.ofNullable(searchLogic.get());

        if (recordOpt.isPresent()) {
            T record = recordOpt.get();

            return SecurityContext.get()
                    .getCurrentUser()
                    .filter(u -> u.getCustomerId() == record.getCustomerId())
                    .map(u -> record)
                    .orElseThrow(() -> exceptionProvider.apply(record));
        } else {
            return null;
        }
    }

    /**
     * <p>Gets the single record using the provided search logic and matching the customer associated with current user.
     * </p>
     *
     * @param searchLogic a logic to be used for searching the record.
     * @return a record returned by search logic.
     */
    protected T getSingleRecord(Function<Integer, T> searchLogic) {
        return SecurityContext.get()
                .getCurrentUser()
                .map(u -> searchLogic.apply(u.getCustomerId()))
                .orElse(null);
    }

    /**
     * <p>Inserts the specified record. Links the specified record to customer account record set for current user and
     * uses specified record insertion logic for saving the record into persistent store.</p>
     *
     * @param record a record to be inserted.
     * @param recordInsertionLogic a logic to be used for inserting the specified record.
     */
    protected void insertRecord(T record, Consumer<T> recordInsertionLogic) {
        SecurityContext.get()
                .getCurrentUser()
                .ifPresent(u -> {
                    record.setCustomerId(u.getCustomerId());
                    recordInsertionLogic.accept(record);
                });
    }

    /**
     * <p>Updates the specified record. Verifies that current user is granted access to specified record. If so then
     * uses the specified logic for updating or deleting the specified record in persistent store. Otherwise a security
     * exception is thrown.</p>
     * 
     * @param record a record to be updated.
     * @param recordUpdateLogic a logic to be used for updating the record.
     * @param exceptionProvider a provider for exception to be thrown in case the access to specified record is denied.
     */
    protected void updateRecord(T record,
                                Consumer<T> recordUpdateLogic,
                                Function<T, SecurityException> exceptionProvider) {
        SecurityContext.get().getCurrentUser()
                .filter(u -> u.isSuperAdmin() || u.getCustomerId() == record.getCustomerId())
                .map(u -> {
                    recordUpdateLogic.accept(record);
                    return 1;
                }).orElseThrow(() -> exceptionProvider.apply(record));
    }

    /**
     * <p>Updates the specified record referenced by the ID. Verifies that specified record exists. If so then updates
     * the record within current security context.</p>
     *
     * @param id an ID of a record to be updated.
     * @param findByIdLogic a logic to be used for finding the required record by ID.
     * @param recordUpdateLogic a logic to be used for updating the record.
     * @param exceptionProvider a provider for exception to be thrown in case the access to specified record is denied.
     * @see #updateRecord(CustomerData, Consumer, Function)
     */
    public void updateById(Integer id,
                           Function<Integer, T> findByIdLogic,
                           Consumer<T> recordUpdateLogic,
                           Function<T, SecurityException> exceptionProvider) {
        
        Optional<T> byId = Optional.ofNullable(findByIdLogic.apply(id));
        byId.ifPresent(record -> {
            updateRecord(record, recordUpdateLogic, exceptionProvider);
        });
    }
}
