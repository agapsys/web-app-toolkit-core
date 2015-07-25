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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public abstract class PersistenceUnit {
	// CLASS SCOPE =============================================================
	/** Database initializer */
	public static interface DbInitializer {
		public void init(PersistenceUnit persistenceUnit);
	}
	
	private static class DefaultPersistenceUnit extends PersistenceUnit {
		// CLASS SCOPE =========================================================
		private static final String PU_NAME = "default";
		// =====================================================================

		// INSTANCE SCOPE ======================================================
		public DefaultPersistenceUnit() {
			super(Persistence.createEntityManagerFactory(PU_NAME, WebApplication.getProperties()));
		}
		// =====================================================================
	}
	
	private static class SingletonHolder {
		private static final PersistenceUnit SINGLETON = new DefaultPersistenceUnit();
	}
		
	public static PersistenceUnit getInstance() {
		return SingletonHolder.SINGLETON;
	}
	// CLASS SCOPE =============================================================
	
	// INSTANCE SCOPE ==========================================================	
	private final EntityManagerFactory emf;
	
	private PersistenceUnit(EntityManagerFactory emf) {
		this.emf = emf;
	}
	
	public EntityManager getEntityManager() {
		return emf.createEntityManager();
	}
	
	public void close() {
		emf.close();
	}
	// =========================================================================
}
