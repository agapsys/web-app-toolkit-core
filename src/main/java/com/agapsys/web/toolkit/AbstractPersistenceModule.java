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

/** Persistence module. */
public abstract class AbstractPersistenceModule extends AbstractModule {
	/**
	 * Returns an entity manager to be used by application.
	 * This method will be called only when module is running.
	 * @return an entity manager to be used by application.
	 */
	protected abstract EntityManager _getEntityManager();
	
	/**
	 * Returns an entity manager to be used by application.
	 * @return an entity manager to be used by application.
	 */
	public final EntityManager getEntityManager() {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		return _getEntityManager();
	}
}
