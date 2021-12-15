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

package com.hmdm.persistence.mapper;

import com.hmdm.persistence.domain.DeviceSearchRequest;
import com.hmdm.rest.json.CustomerSearchRequest;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;
import com.hmdm.persistence.domain.Customer;

import java.util.List;

/**
 * <p>An ORM mapper for {@link Customer} domain object.</p>
 *
 * @author isv
 */
public interface CustomerMapper {

    String SELECT_BASE = "SELECT customers.* FROM customers ";

    @Select({SELECT_BASE})
    List<Customer> findAll();

    @Select({SELECT_BASE + " WHERE master = FALSE ORDER BY name"})
    List<Customer> findAllExceptMaster();

    @Insert({"INSERT INTO customers (name, email, description, filesDir, master, prefix, registrationTime, " +
             "accountType, expiryTime, deviceLimit, customerStatus) " +
             "VALUES (#{name}, #{email}, #{description}, #{filesDir}, FALSE, #{prefix}, #{registrationTime}, " +
             "#{accountType}, #{expiryTime}, #{deviceLimit}, #{customerStatus})"})
    @SelectKey( statement = "SELECT currval('customers_id_seq')", keyColumn = "id",
            keyProperty = "id", before = false, resultType = int.class )
    void insert(Customer customer);

    @Update({"UPDATE customers SET name=#{name}, email=#{email}, description=#{description}, " +
            "accountType=#{accountType}, expiryTime=#{expiryTime}, deviceLimit=#{deviceLimit}, " +
            "customerStatus=#{customerStatus} WHERE id=#{id} AND master = FALSE"})
    void update(Customer customer);

    @Delete({"DELETE FROM customers WHERE id=#{id} AND master = FALSE"})
    void delete(@Param("id") Integer id);

    @Select({SELECT_BASE +
            "WHERE master = FALSE " +
            "AND (LOWER(name) LIKE #{filter} OR LOWER(description) LIKE #{filter}) " +
            "ORDER BY name"})
    List<Customer> findAllByValue(@Param("filter") String value);

    @Select({SELECT_BASE + " WHERE id=#{id}"})
    Customer findCustomerById(@Param("id") Integer id);

    @Select({"SELECT customers.* " +
            "FROM customers " +
            "WHERE id = #{id}"})
    Customer findCustomerByIdForUpdate(@Param("id") Integer id);

    @Select({SELECT_BASE + " WHERE LOWER(name) = LOWER(#{name}) LIMIT 1"})
    Customer findCustomerByName(@Param("name") String name);

    @Select({SELECT_BASE + " WHERE LOWER(email) = LOWER(#{email}) LIMIT 1"})
    Customer findCustomerByEmail(@Param("email") String email);

    @Select("SELECT EXISTS (SELECT 1 FROM customers WHERE LOWER(prefix) = LOWER(#{prefix}))")
    boolean isPrefixUsed(@Param("prefix") String prefix);

    @Select("SELECT EXISTS (SELECT 1 FROM customers WHERE id > 1 LIMIT 1)")
    boolean isMultiTenant();

    @Update("UPDATE customers SET lastLoginTime = #{time} WHERE id = #{id}")
    int recordLastLoginTime(@Param("id") int customerId, @Param("time") long time);

    List<Customer> searchCustomers(CustomerSearchRequest request);

    Long countAllCustomers(CustomerSearchRequest request);
}
