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

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.Module;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/**
 * Represents a JPA persistence module.
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class PersistenceModule extends Module {
	// CLASS SCOPE =============================================================
	public static final String SETTINGS_GROUP_NAME = PersistenceModule.class.getName();

	public static final String KEY_JDBC_PASSWORD = "javax.persistence.jdbc.password";

	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final String persistenceUnitName;

	private EntityManagerFactory emf = null;
	private char[] jdbcPassword = null;

	/**
	 * Default constructor. Default persistence name equals to {@linkplain PersistenceModule#DEFAULT_PERSISTENCE_UNIT_NAME}.
	 */
	public PersistenceModule() {
		this(DEFAULT_PERSISTENCE_UNIT_NAME);
	}

	/**
	 * Constructor. Allows a custom persistence unit name.
	 *
	 * @param persistenceUnitName persistence unit name used by this module.
	 */
	public PersistenceModule(String persistenceUnitName) {
		if (persistenceUnitName == null || persistenceUnitName.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty name");

		this.persistenceUnitName = persistenceUnitName;
	}

	@Override
	protected final String getSettingsGroupName() {
		return SETTINGS_GROUP_NAME;
	}

	/**
	 * Returns JDBC password.
	 *
	 * @return JDBC password.
	 */
	protected char[] getJdbcPassword() {
		return jdbcPassword;
	}

	/**
	 * Return the name of persistence unit associated with this instance.
	 *
	 * @return the name of persistence unit associated with this instance.
	 */
	protected String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	/**
	 * Returns additional properties to be used when creating the internal
	 * entity manager factory.
	 *
	 * @param app associated application.
	 * @return additional properties to be used.
	 */
	protected Map getAdittionalProperties(AbstractApplication app) {
		Properties props = getProperties();

		Map propertyMap = new LinkedHashMap(props);

		String strJdbcPassword = getProperty(props, KEY_JDBC_PASSWORD);

		if (strJdbcPassword == null) {
			jdbcPassword = null;
		} else {
			jdbcPassword = strJdbcPassword.toCharArray();
			propertyMap.put(KEY_JDBC_PASSWORD, jdbcPassword);
		}

		return propertyMap;
	}

	@Override
	protected void onInit(AbstractApplication app) {
		super.onInit(app);
		emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), getAdittionalProperties(app));
	}

	@Override
	protected void onStop() {
		super.onStop();

		emf.close();
		emf = null;
	}

	/**
	 * Returns an entity manager to be used by application.
	 *
	 * @param emf Entity manager factory.
	 * @return an entity manager to be used by application.
	 */
	protected EntityManager getCustomEntityManager(EntityManagerFactory emf) {
		EntityManager em = emf.createEntityManager();
		em.setFlushMode(FlushModeType.COMMIT);
		return em;
	}

	/**
	 * Returns an entity manager to be used by application.
	 *
	 * @return an entity manager to be used by application.
	 */
	public final EntityManager getEntityManager() {
		synchronized(this) {
			if (!isActive())
				throw new IllegalStateException("Module is not active");

			return getCustomEntityManager(emf);
		}
	}
	// =========================================================================
}