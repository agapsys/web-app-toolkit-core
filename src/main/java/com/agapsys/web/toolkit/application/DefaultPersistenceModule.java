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

package com.agapsys.web.toolkit.application;

import com.agapsys.web.toolkit.WebApplication;
import com.agapsys.web.toolkit.PersistenceModule;
import com.agapsys.web.toolkit.utils.Properties;
import com.agapsys.web.toolkit.utils.RuntimeJarLoader;
import java.io.File;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class DefaultPersistenceModule extends PersistenceModule {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
	
	public static final String DEFAULT_JDBC_DRIVER       = "org.h2.Driver";
	public static final String DEFAULT_JDBC_URL          = "jdbc:h2:mem:";
	public static final String DEFAULT_JDBC_USER         = "sa";
	public static final String DEFAULT_JDBC_PASSWORD     = "sa";
	
	public static final String KEY_JDBC_DRIVER_FILE = "com.agapsys.jdbc.driverFile";
	
	private static final Properties DEFAULT_PROPERTIES;
	
	static {
		DEFAULT_PROPERTIES = new Properties();
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.driver",   DEFAULT_JDBC_DRIVER);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.url",      DEFAULT_JDBC_URL);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.user",     DEFAULT_JDBC_USER);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.password", DEFAULT_JDBC_PASSWORD);
	}

	// INSTANCE SCOPE ==========================================================
	private EntityManagerFactory emf = null;

	public DefaultPersistenceModule(WebApplication application) {
		super(application);
	}
	
	@Override
	public Properties getDefaultSettings() {
		return DEFAULT_PROPERTIES;
	}
	
	@Override
	protected void onStart() {
		WebApplication application = getApplication();
		Properties appProperties = application.getProperties();
		
		// If a JDBC driver file was set load it
		String jdbcDriverFilename = appProperties.getProperty(KEY_JDBC_DRIVER_FILE);
		String jdbcDriverClass = appProperties.getProperty("javax.persistence.jdbc.driver");
		
		if (jdbcDriverFilename != null) {
			if (jdbcDriverFilename.trim().isEmpty())
				throw new RuntimeException("Empty JDBC driver file name in application settings");
			
			if (jdbcDriverClass == null || jdbcDriverClass.trim().isEmpty())
				throw new RuntimeException("Missing jdbc driver class definition in application settings");
			
			File jdbcDriverFile = new File(application.getFolder(), jdbcDriverFilename);
			RuntimeJarLoader.loadJar(jdbcDriverFile);
		}
		
		java.util.Properties props = new java.util.Properties();
		props.putAll(appProperties.getEntries());
		emf = Persistence.createEntityManagerFactory(getDefaultPersistenceUnitName(), props);
	}
	
	@Override
	protected void onStop() {
		emf.close();
		emf = null;
	}
	
	@Override
	protected EntityManager getAppEntityManager() throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		return emf.createEntityManager();
	}
	
	/**
	 * Return the name of default persistence unit name. 
	 * @return the name of default persistence unit name. Default implementation 
	 * returns {@linkplain DefaultPersistenceModule#DEFAULT_PERSISTENCE_UNIT_NAME} 
	 */
	protected String getDefaultPersistenceUnitName() {
		return DEFAULT_PERSISTENCE_UNIT_NAME;
	}
	// =========================================================================
}
