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
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
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

	public RuntimePersistenceModule() {}

	public RuntimePersistenceModule(String persistenceUnitName) {
		super(persistenceUnitName);
	}
	
	@Override
	public Properties getDefaultProperties() {
		Properties properties = new Properties();
		
		properties.setProperty(KEY_JDBC_DRIVER_FILENAME, DEFAULT_JDBC_DRIVER_FILENAME);
		properties.setProperty(KEY_JDBC_DRIVER_CLASS,    DEFAULT_JDBC_DRIVER_CLASS);
		properties.setProperty(KEY_JDBC_URL,             DEFAULT_JDBC_URL);
		properties.setProperty(KEY_JDBC_USER,            DEFAULT_JDBC_USER);
		properties.setProperty(KEY_JDBC_PASSWORD,        DEFAULT_JDBC_PASSWORD);
		
		return properties;
	}	
	
	@Override
	protected void onInit(AbstractWebApplication webApp) {
		Properties appProperties = webApp.getProperties();
		
		getMandatoryProperty(KEY_JDBC_DRIVER_CLASS);
		getMandatoryProperty(KEY_JDBC_URL);
		getMandatoryProperty(KEY_JDBC_USER);
		getMandatoryProperty(KEY_JDBC_PASSWORD);
		
		String jdbcFilename = appProperties.getProperty(KEY_JDBC_DRIVER_FILENAME);
		
		if (jdbcFilename != null && !jdbcFilename.trim().isEmpty()) {
			File jdbcDriverFile = new File(webApp.getDirectory(), jdbcFilename);
			RuntimeJarLoader.loadJar(jdbcDriverFile);
		}
		
		emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), appProperties);
	}
	
	@Override
	protected void onStop() {
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
