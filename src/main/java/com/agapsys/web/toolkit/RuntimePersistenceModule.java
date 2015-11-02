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

import com.agapsys.web.toolkit.utils.RuntimeJarLoader;
import java.io.File;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class RuntimePersistenceModule extends PersistenceModule {
	// CLASS SCOPE =============================================================
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

	@Override
	public String getTitle() {
		return "Runtime persistence Module";
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
		if (defaultJdbcDriverFilename != null && defaultJdbcDriverFilename.trim().isEmpty())
			defaultJdbcDriverFilename = null;
		
		String defaultJdbcDriverClass = getDefaultJdbcDriverClass();
		if (defaultJdbcDriverClass == null || defaultJdbcDriverClass.trim().isEmpty())
			throw new RuntimeException("Null/empty default JDBC driver class");
		
		String defaultJdbcUrl = getDefaultJdbcUrl();
		if (defaultJdbcUrl == null || defaultJdbcUrl.trim().isEmpty())
			throw new RuntimeException("Null/Empty default JDBC URL");
		
		String defaultJdbcUser = getDefaultJdbcUser();
		if (defaultJdbcUser == null)
			throw new RuntimeException("Null default JDBC user");
		
		String defaultJdbcPassword = getDefaultJdbcPassword();
		if (defaultJdbcPassword == null)
			throw new RuntimeException("Null default JDBC password");
			
		if (defaultJdbcDriverFilename != null)
			properties.setProperty(KEY_JDBC_DRIVER_FILENAME, defaultJdbcDriverFilename);
		
		properties.setProperty(KEY_JDBC_DRIVER_CLASS,    defaultJdbcDriverClass);
		properties.setProperty(KEY_JDBC_URL,             defaultJdbcUrl);
		properties.setProperty(KEY_JDBC_USER,            defaultJdbcUser);
		properties.setProperty(KEY_JDBC_PASSWORD,        defaultJdbcPassword);
		
		return properties;
	}	
	
	@Override
	protected void onStart(AbstractWebApplication webApp) {
		Properties properties = webApp.getProperties();
		
		String jdbcFilename = properties.getProperty(KEY_JDBC_DRIVER_FILENAME);
		
		if (jdbcFilename != null && !jdbcFilename.trim().isEmpty()) {
			File jdbcDriverFile = new File(webApp.getDirectory(), jdbcFilename);
			RuntimeJarLoader.loadJar(jdbcDriverFile);
		}
		
		emf = Persistence.createEntityManagerFactory(getDefaultPersistenceUnitName(), properties);
	}
	
	@Override
	protected void onStop() {
		emf.close();
		emf = null;
	}
	
	@Override
	protected EntityManager getAppEntityManager() {
		return emf.createEntityManager();
	}
	// =========================================================================
}
