/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
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

import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.web.toolkit.utils.RuntimeJarLoader;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

public class RuntimePersistenceModule extends PersistenceModule {
	// CLASS SCOPE =============================================================
	// -------------------------------------------------------------------------
	public static final String KEY_JDBC_DRIVER_FILENAME = PersistenceModule.SETTINGS_GROUP_NAME + "." + RuntimePersistenceModule.class.getSimpleName() + ".driverFile";

	public static final String KEY_JDBC_DRIVER_CLASS    = "javax.persistence.jdbc.driver";
	public static final String KEY_JDBC_URL             = "javax.persistence.jdbc.url";
	public static final String KEY_JDBC_USER            = "javax.persistence.jdbc.user";
	public static final String KEY_JDBC_PASSWORD        = "javax.persistence.jdbc.password";
	// -------------------------------------------------------------------------

	public static final String DEFAULT_JDBC_DRIVER_FILENAME = "h2.jar";
	public static final String DEFAULT_JDBC_DRIVER_CLASS    = "org.h2.Driver";
	public static final String DEFAULT_JDBC_URL             = "jdbc:h2:mem:";
	public static final String DEFAULT_JDBC_USER            = "sa";
	public static final String DEFAULT_JDBC_PASSWORD        = "sa";

	// INSTANCE SCOPE ==========================================================
	private EntityManagerFactory emf = null;
	private File jdbcDriverFile;
	private String jdbcDriverClass;
	private String jdbcUrl;
	private String jdbcUser;
	private char[] jdbcPassword;

	private void reset() {
		jdbcDriverFile = null;
		jdbcDriverClass = null;
		jdbcUrl = null;
		jdbcUser = null;
		jdbcPassword = null;
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

	protected char[] getJdbcPassword() {
		return jdbcPassword;
	}

	@Override
	public Properties getDefaultProperties() {
		Properties defaultProperties = super.getDefaultProperties();

		defaultProperties.setProperty(KEY_JDBC_DRIVER_FILENAME, DEFAULT_JDBC_DRIVER_FILENAME);
		defaultProperties.setProperty(KEY_JDBC_DRIVER_CLASS,    DEFAULT_JDBC_DRIVER_CLASS);
		defaultProperties.setProperty(KEY_JDBC_URL,             DEFAULT_JDBC_URL);
		defaultProperties.setProperty(KEY_JDBC_USER,            DEFAULT_JDBC_USER);
		defaultProperties.setProperty(KEY_JDBC_PASSWORD,        DEFAULT_JDBC_PASSWORD);

		return defaultProperties;
	}

	@Override
	protected void onModuleInit(AbstractWebApplication webApp) {
		// super.onModuleInit should be skipped!

		reset();

		Properties props = getProperties();

		jdbcDriverClass = getMandatoryProperty(props, KEY_JDBC_DRIVER_CLASS);
		jdbcUrl         = getMandatoryProperty(props, KEY_JDBC_URL);
		jdbcUser        = getMandatoryProperty(props, KEY_JDBC_USER);
		jdbcPassword    = getMandatoryProperty(props, KEY_JDBC_PASSWORD).toCharArray();

		String jdbcFilename = getProperty(props, KEY_JDBC_DRIVER_FILENAME);

		if (jdbcFilename != null && !jdbcFilename.isEmpty()) {
			jdbcDriverFile = new File(webApp.getDirectory(), jdbcFilename);
			RuntimeJarLoader.loadJar(jdbcDriverFile);
		}

		Map<Object, Object> propertyMap = new LinkedHashMap<>(getProperties());
		propertyMap.put(KEY_JDBC_PASSWORD, jdbcPassword);

		emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), propertyMap);
	}

	@Override
	protected void onModuleStop() {
		// super.onModuleStop() sould be skipped!
		
		emf.close();
		emf = null;
	}

	@Override
	protected EntityManager getCustomEntityManager() {
		EntityManager em = emf.createEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		return em;
	}
	// =========================================================================
}
