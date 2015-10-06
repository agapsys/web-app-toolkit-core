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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public abstract class WebApplication extends AbstractApplication implements ServletContextListener {
	// CLASS SCOPE =============================================================
	private static WebApplication singleton = null;
	
	public static WebApplication getInstance() {
		if (singleton == null)
			throw new IllegalStateException("Web application is not running");
		
		return singleton;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	protected void beforeApplicationStart() {
		super.beforeApplicationStart();
		
		// Register used modules
		Class<? extends AbstractModule> persistenceModuleClass       = getPersistenceModuleClass();
		Class<? extends AbstractModule> exceptionReporterModuleClass = getExceptionReporterModuleClass();
		Class<? extends AbstractModule> smtpModuleClass              = getSmtpModuleClass();
		
		if (persistenceModuleClass != null)
			registerModule(persistenceModuleClass);
		
		if (exceptionReporterModuleClass != null)
			registerModule(exceptionReporterModuleClass);
		
		if (smtpModuleClass != null)
			registerModule(smtpModuleClass);
	}
	
	/**
	 * Return the persistence module used by application
	 * @return Persistence module class. Default implementation returns {@linkplain PersistenceModule} class.
	 */
	protected Class<? extends AbstractPersistenceModule> getPersistenceModuleClass() {
		return PersistenceModule.class;
	}
	
	/**
	 * Return the exception reporter module used by application.
	 * @return Exception reporter module class. Default implementation returns {@linkplain SmtpExceptionReporterModule} class
	 */
	protected Class<? extends AbstractExceptionReporterModule> getExceptionReporterModuleClass() {
		return SmtpExceptionReporterModule.class;
	}

	/**
	 * Returns the SMTP module used by application.
	 * @return SMTP module class. Default implementation returns {@linkplain SmtpModule} class
	 */
	protected Class<? extends AbstractSmtpModule> getSmtpModuleClass() {
		return SmtpModule.class;
	}
	
	/** @return The persistence module used by this application. If there is no such module, returns null. */
	public final AbstractPersistenceModule getPersistenceModule() {
		return (AbstractPersistenceModule) getModuleInstance(getPersistenceModuleClass());
	}

	/** @return The exception reporter module used by this application. If there is no such module, returns null. */
	public final AbstractExceptionReporterModule getExceptionReporterModule() {
		return (AbstractExceptionReporterModule) getModuleInstance(getExceptionReporterModuleClass());
	}

	/** @return The SMTP module used by this application. If there is no such module, returns null. */
	public final AbstractSmtpModule getSmtpModule() {
		return (AbstractSmtpModule) getModuleInstance(getSmtpModuleClass());
	}
	
	@Override
	public final void contextInitialized(ServletContextEvent sce) {
		start();
		singleton = this;
	}

	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		stop();
		singleton = null;
	}
	// =========================================================================

	
}
