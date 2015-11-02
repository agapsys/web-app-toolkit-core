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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceModule extends AbstractPersistenceModule {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
	// INSTANCE SCOPE ==========================================================
	private EntityManagerFactory emf = null;

	/**
	 * Return the name of default persistence unit name. 
	 * @return the name of default persistence unit name. Default implementation 
	 * returns {@linkplain PersistenceModule#DEFAULT_PERSISTENCE_UNIT_NAME} 
	 */
	protected String getDefaultPersistenceUnitName() {
		return DEFAULT_PERSISTENCE_UNIT_NAME;
	}
	
	@Override
	protected void onStart(AbstractWebApplication webapplication) {
		emf = Persistence.createEntityManagerFactory(getDefaultPersistenceUnitName());
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