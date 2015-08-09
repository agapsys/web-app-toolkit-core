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

package com.agapsys.web;

import com.agapsys.web.utils.Properties;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;

public class DefaultPersistenceModule implements PersistenceModule {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
	
	public static final String DEFAULT_JDBC_DRIVER       = "org.h2.Driver";
	public static final String DEFAULT_JDBC_URL          = "jdbc:h2:mem:";
	public static final String DEFAULT_JDBC_USER         = "sa";
	public static final String DEFAULT_JDBC_PASSWORD     = "sa";
	public static final String DEFAULT_SCHEMA_GENERATION = "create";
	
	private static final Properties DEFAULT_PROPERTIES;
	
	static {
		DEFAULT_PROPERTIES = new Properties();
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.driver",                       DEFAULT_JDBC_DRIVER);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.url",                          DEFAULT_JDBC_URL);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.user",                         DEFAULT_JDBC_USER);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.jdbc.password",                     DEFAULT_JDBC_PASSWORD);
		DEFAULT_PROPERTIES.setProperty("javax.persistence.schema-generation.database.action", DEFAULT_SCHEMA_GENERATION);
	}

	// INSTANCE SCOPE ==========================================================
	private PersistenceUnit persistenceUnit = null;
	
	@Override
	public Properties getDefaultSettings() {
		return DEFAULT_PROPERTIES;
	}
	
	protected String getDefaultPersistenceUnitName() {
		return DEFAULT_PERSISTENCE_UNIT_NAME;
	}
	
	protected void init(EntityManager entityManager) {}
	
	public boolean isRunning() {
		return persistenceUnit != null;
	}
	
	public void start() {
		if (!isRunning()) {
			java.util.Properties props = new java.util.Properties();
			props.putAll(WebApplication.getProperties().getEntries());
			persistenceUnit = new PersistenceUnit(Persistence.createEntityManagerFactory(getDefaultPersistenceUnitName(), props));
			
			EntityManager entityManager = persistenceUnit.getEntityManager();
			init(entityManager);
			entityManager.close();
		}
	}
	
	public void stop() {
		if (isRunning()) {
			persistenceUnit.close();
			persistenceUnit = null;
		}
	}
	
	@Override
	public EntityManager getEntityManager() {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		return persistenceUnit.getEntityManager();
	}
	// =========================================================================
}
