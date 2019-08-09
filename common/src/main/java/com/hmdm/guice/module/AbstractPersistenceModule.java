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

package com.hmdm.guice.module;

import com.google.inject.name.Names;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.builtin.PooledDataSourceProvider;

import javax.servlet.ServletContext;
import java.util.Enumeration;

/**
 * <p>A base for module used for configuring the {@link org.apache.ibatis.session.SqlSessionFactory} to be used by the
 * persistence layer of the application.</p>
 *
 * @author isv
 */
public abstract class AbstractPersistenceModule extends MyBatisModule {

    /**
     * <p>A context for module usage.</p>
     */
    protected final ServletContext context;

    /**
     * <p>Constructs new <code>PersistenceModule</code> instance for use in specified context.</p>
     *
     * @param context a context for module usage.
     */
    public AbstractPersistenceModule(ServletContext context) {
        this.context = context;
    }

    /**
     * <p>Initializes this module.</p>
     *
     * <ul>
     *     <li>Binds the constants with names starting with "JDBC" to values set in context</li>
     *     <li>Configures the SQL session factory to use conection pool and JDBC transaction strategy</li>
     *     <li>Sets database connection poll size to 30</li>
     *     <li>Register the mapper classes and aliases for domain objects</li>
     * </ul>
     *
     * <p>The plugin MUST override the {@link #getMapperPackageName()} and {@link #getDomainObjectsPackageName()} to
     * return the package names specific to plugins.</p>
     *
     * @see PooledDataSourceProvider
     * @see JdbcTransactionFactory
     */
    protected final void initialize() {
        Enumeration params = this.context.getInitParameterNames();

        while(params.hasMoreElements()) {
            String paramName = params.nextElement().toString();
            if (paramName.startsWith("JDBC") && this.context.getInitParameter(paramName) != null) {
                this.bindConstant().annotatedWith(Names.named(paramName)).to(this.context.getInitParameter(paramName));
            }
        }

        this.bindConstant().annotatedWith(Names.named("mybatis.pooled.maximumActiveConnections")).to(30);
        this.environmentId("production");
        this.bindDataSourceProviderType(PooledDataSourceProvider.class);
        this.bindTransactionFactoryType(JdbcTransactionFactory.class);
        this.addMapperClasses(getMapperPackageName());
        this.addSimpleAliases(getDomainObjectsPackageName());
    }

    /**
     * <p>Gets the name of Java package containing the MyBatis mapper classes to be used by the persistence layer of the
     * application.</p>
     *
     * @return a fully-qualified name of package with MyBatis mapper classes.
     */
    protected abstract String getMapperPackageName();

    /**
     * <p>Gets the name of Java package containing the Domain Object classes to be used by the persistence layer of the
     * application.</p>
     *
     * @return a fully-qualified name of package with Domain Object classes.
     */
    protected abstract String getDomainObjectsPackageName();

}
