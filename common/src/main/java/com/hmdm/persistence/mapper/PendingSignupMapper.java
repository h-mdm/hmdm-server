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

import com.hmdm.persistence.domain.PendingSignup;
import com.hmdm.persistence.domain.UsageStats;
import org.apache.ibatis.annotations.*;

/**
 * <p>An ORM mapper for {@link PendingSignup} domain object.</p>
 *
 * @author seva
 */
public interface PendingSignupMapper {

    @Insert({"INSERT INTO pendingSignup (email, signupTime, language, token) " +
             "VALUES ( #{email}, #{signupTime}, #{language}, #{token}) ON CONFLICT ON CONSTRAINT pendingSignup_email_key DO " +
             "UPDATE SET signupTime = EXCLUDED.signupTime, language = EXCLUDED.language, token = EXCLUDED.token"})
    void insert(PendingSignup data);

    @Select({"SELECT * FROM pendingSignup WHERE email=#{email}"})
    PendingSignup getByEmail(@Param("email") String email);

    @Select({"SELECT * FROM pendingSignup WHERE token=#{token}"})
    PendingSignup getByToken(@Param("token") String token);

    @Delete({"DELETE FROM pendingSignup WHERE email=#{email}"})
    void remove(@Param("email") String email);
}
