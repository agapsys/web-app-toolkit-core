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

import com.agapsys.web.PersistenceUnit.DbInitializer;
import com.agapsys.web.utils.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.hibernate.Session;

/**
 * Persistence module
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
class PersistenceModule {
	// CLASS SCOPE =============================================================
	public static String DEFAULT_JDBC_DRIVER    = "org.h2.Driver";
	public static String DEFAULT_JDBC_URL       = "jdbc:h2:mem:";
	public static String DEFAULT_JDBC_USER      = "sa";
	public static String DEFAULT_JDBC_PASSWORD  = "sa";
	public static String DEFAULT_DDL_GENERATION = "update";
	public static String DEFAULT_FLUSH_MODE     = "COMMIT";
	
	static void loadDefaults(Properties properties, boolean keepExisting) {
		properties.setProperty("javax.persistence.jdbc.driver",   DEFAULT_JDBC_DRIVER,    keepExisting);
		properties.setProperty("javax.persistence.jdbc.url",      DEFAULT_JDBC_URL,       keepExisting);
		properties.setProperty("javax.persistence.jdbc.user",     DEFAULT_JDBC_USER,      keepExisting);
		properties.setProperty("javax.persistence.jdbc.password", DEFAULT_JDBC_PASSWORD,  keepExisting);
		properties.setProperty("hibernate.hbm2ddl.auto",          DEFAULT_DDL_GENERATION, keepExisting);
		properties.setProperty("org.hibernate.flushMode",         DEFAULT_FLUSH_MODE,     keepExisting);
	}
		
	private static PersistenceUnit persistenceUnit = null;

	/** @return boolean indicating if module is running. */
	public static boolean isRunning() {
		return persistenceUnit != null;
	}
	
	/** 
	 * Starts the module module.
	 * If module is already running, nothing happens.
	 * @param dbInitializer initializer for persistence or null if 
	 * an initialization is not required.
	 */
	public static void start(DbInitializer dbInitializer) {
		if (!isRunning()) {
			try {
				persistenceUnit = PersistenceUnit.getInstance();
				
				if (dbInitializer != null) {
					dbInitializer.init(persistenceUnit);
				}
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	/** 
	 * Stops the module.
	 * If module is not running, nothing happens.
	 */
	public static void stop() {
		if (isRunning()) {
			persistenceUnit.close();
			persistenceUnit = null;
		}
	}
	
	/**
	 * @return an entity manager to be used by application
	 * @throws IllegalStateException if module is not running
	 */
	public static EntityManager getEntityManager() throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		return persistenceUnit.getEntityManager();
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private PersistenceModule() {}
	// =========================================================================


}
