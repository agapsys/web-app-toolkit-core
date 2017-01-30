/*
 * Copyright 2015-2016 Agapsys Tecnologia Ltda-ME.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.agapsys.web.toolkit.modules;

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.utils.RuntimeJarLoader;
import com.agapsys.web.toolkit.utils.Settings;
import java.io.File;
import java.util.Map;

public class RuntimePersistenceModule extends PersistenceModule {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    // -------------------------------------------------------------------------
    public static final String KEY_JDBC_DRIVER_FILENAME = RuntimePersistenceModule.class.getName()+ ".driverFile";

    public static final String KEY_JDBC_DRIVER_CLASS    = "javax.persistence.jdbc.driver";
    public static final String KEY_JDBC_URL             = "javax.persistence.jdbc.url";
    public static final String KEY_JDBC_USER            = "javax.persistence.jdbc.user";
    // -------------------------------------------------------------------------

    public static final String DEFAULT_JDBC_DRIVER_FILENAME = "h2.jar";
    public static final String DEFAULT_JDBC_DRIVER_CLASS    = "org.h2.Driver";
    public static final String DEFAULT_JDBC_URL             = "jdbc:h2:mem:";
    public static final String DEFAULT_JDBC_USER            = "sa";
    public static final String DEFAULT_JDBC_PASSWORD        = "sa";
    // =========================================================================
    // </editor-fold>

    private File jdbcDriverFile;
    private String jdbcDriverClass;
    private String jdbcUrl;
    private String jdbcUser;

    private void reset() {
        jdbcDriverFile = null;
        jdbcDriverClass = null;
        jdbcUrl = null;
        jdbcUser = null;
    }

    public RuntimePersistenceModule() {
        this(DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    public RuntimePersistenceModule(String persistenceUnitName) {
        super(persistenceUnitName);
        reset();
    }

    protected File getJdbcDriverFile() {
        return jdbcDriverFile;
    }

    protected String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    protected String getJdbcUrl() {
        return jdbcUrl;
    }

    protected String getJdbcUser() {
        return jdbcUser;
    }

    @Override
    public Settings getDefaultSettings() {
        Settings defaultSettings = super.getDefaultSettings();

        if (defaultSettings == null)
            defaultSettings = new Settings();

        defaultSettings.setProperty(KEY_JDBC_DRIVER_FILENAME, DEFAULT_JDBC_DRIVER_FILENAME);
        defaultSettings.setProperty(KEY_JDBC_DRIVER_CLASS,    DEFAULT_JDBC_DRIVER_CLASS);
        defaultSettings.setProperty(KEY_JDBC_URL,             DEFAULT_JDBC_URL);
        defaultSettings.setProperty(KEY_JDBC_USER,            DEFAULT_JDBC_USER);
        defaultSettings.setProperty(KEY_JDBC_PASSWORD,        DEFAULT_JDBC_PASSWORD);

        return defaultSettings;
    }

    @Override
    protected Map getAdditionalProperties(AbstractApplication app) {
        reset();

        Map props = super.getAdditionalProperties(app);
        Settings settings = getSettings();
        jdbcDriverClass = settings.getMandatoryProperty(KEY_JDBC_DRIVER_CLASS);
        jdbcUrl         = settings.getMandatoryProperty(KEY_JDBC_URL);
        jdbcUser        = settings.getMandatoryProperty(KEY_JDBC_USER);

        settings.getMandatoryProperty(KEY_JDBC_PASSWORD); // <-- in PersistenceModule this attribute is optional.

        String jdbcFilename = settings.getProperty(KEY_JDBC_DRIVER_FILENAME, DEFAULT_JDBC_URL);

        if (jdbcFilename != null && !jdbcFilename.isEmpty()) {
            jdbcDriverFile = new File(app.getDirectory(), jdbcFilename);
            RuntimeJarLoader.loadJar(jdbcDriverFile);
        }

        return props;
    }

}
