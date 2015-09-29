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

package com.agapsys.web.toolkit;

import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.web.toolkit.AbstractPersistenceModule;
import com.agapsys.web.toolkit.utils.RuntimeJarLoader;
import java.io.File;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceModule extends AbstractPersistenceModule {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
	
	// SETTINGS -------------------------------------------------------
	public static final String KEY_JDBC_DRIVER_FILENAME = "agapsys.webtoolkit.persistence.driverFile";
	
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

	public PersistenceModule(AbstractWebApplication application) {
		super(application);
	}

	/**
	 * Return the name of default persistence unit name. 
	 * @return the name of default persistence unit name. Default implementation 
	 * returns {@linkplain PersistenceModule#DEFAULT_PERSISTENCE_UNIT_NAME} 
	 */
	protected String getDefaultPersistenceUnitName() {
		return DEFAULT_PERSISTENCE_UNIT_NAME;
	}
	
	protected String getDefaultJdbcDriverFilename() {
		return DEFAULT_JDBC_DRIVER_FILENAME;
	}
	
	protected String getDefaultJdbcDriverClass() {
		return DEFAULT_JDBC_DRIVER_CLASS;
	}
	
	protected String getDefaultJdbcUrl() {
		return DEFAULT_JDBC_URL;
	}
	
	protected String getDefaultJdbcUser() {
		return DEFAULT_JDBC_USER;
	}
	
	protected String getDefaultJdbcPassword() {
		return DEFAULT_JDBC_PASSWORD;
	}
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = new Properties();
		
		String defaultJdbcDriverFilename = getDefaultJdbcDriverFilename();
		if (defaultJdbcDriverFilename == null || defaultJdbcDriverFilename.trim().isEmpty())
			defaultJdbcDriverFilename = DEFAULT_JDBC_DRIVER_FILENAME;
		
		String defaultJdbcDriverClass = getDefaultJdbcDriverClass();
		if (defaultJdbcDriverClass == null || defaultJdbcDriverClass.trim().isEmpty())
			defaultJdbcDriverClass = DEFAULT_JDBC_DRIVER_CLASS;
		
		String defaultJdbcUrl = getDefaultJdbcUrl();
		if (defaultJdbcUrl == null || defaultJdbcUrl.trim().isEmpty())
			defaultJdbcUrl = DEFAULT_JDBC_URL;
		
		String defaultJdbcUser = getDefaultJdbcUser();
		if (defaultJdbcUser == null || defaultJdbcUser.trim().isEmpty())
			defaultJdbcUser = DEFAULT_JDBC_USER;
		
		String defaultJdbcPassword = getDefaultJdbcPassword();
		if (defaultJdbcPassword == null || defaultJdbcPassword.trim().isEmpty())
			defaultJdbcPassword = DEFAULT_JDBC_PASSWORD;
			
		properties.setProperty(KEY_JDBC_DRIVER_FILENAME, defaultJdbcDriverFilename);
		properties.setProperty(KEY_JDBC_DRIVER_CLASS,    defaultJdbcDriverClass);
		properties.setProperty(KEY_JDBC_URL,             defaultJdbcUrl);
		properties.setProperty(KEY_JDBC_USER,            defaultJdbcUser);
		properties.setProperty(KEY_JDBC_PASSWORD,        defaultJdbcPassword);
		
		return properties;
	}
	
	@Override
	protected void onStart() {
		AbstractWebApplication application = getApplication();
		Properties properties = application.getProperties();
		
		File jdbcDriverFile = new File(application.getFolder(), properties.getProperty(KEY_JDBC_DRIVER_FILENAME));
		RuntimeJarLoader.loadJar(jdbcDriverFile);
		
		emf = Persistence.createEntityManagerFactory(getDefaultPersistenceUnitName(), properties);
	}
	
	@Override
	protected void onStop() {
		emf.close();
		emf = null;
	}
	
	@Override
	protected EntityManager getAppEntityManager() throws IllegalStateException {
		return emf.createEntityManager();
	}
	// =========================================================================
}
