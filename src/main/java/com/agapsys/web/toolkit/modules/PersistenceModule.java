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
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

public class PersistenceModule extends AbstractPersistenceModule {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private EntityManagerFactory emf = null;
	
	private final String persistenceUnitName;
	
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
	
	/**
	 * Return the name of persistence unit associated with this instance.
	 * 
	 * @return the name of persistence unit associated with this instance.
	 */
	protected String getPersistenceUnitName() {
		return persistenceUnitName;
	}
	
	@Override
	protected void onInit(AbstractWebApplication webapplication) {
		emf = Persistence.createEntityManagerFactory(getPersistenceUnitName());
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