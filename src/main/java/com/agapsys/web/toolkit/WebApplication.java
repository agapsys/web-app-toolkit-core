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

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class WebApplication extends AbstractWebApplication {
	// CLASS SCOPE =============================================================
	public static final String PERSISTENCE_MODULE_ID        = "agapsys.webtoolkit.persistence";
	public static final String EXCEPTION_REPORTER_MODULE_ID = "agapsys.webtoolkit.exceptionReporter";
	public static final String SMTP_MODULE_ID               = "agapsys.webtoolkit.smtp";
	
	public static WebApplication getInstance() {
		return (WebApplication) AbstractWebApplication.getInstance();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	protected Map<String, Class<? extends AbstractModule>> getModuleClassMap() {
		Map<String, Class<? extends AbstractModule>> moduleMap = new LinkedHashMap<>();

		Class<? extends AbstractModule> persistenceModuleClass       = getPersistenceModuleClass();
		Class<? extends AbstractModule> exceptionReporterModuleClass = getExceptionReporterModuleClass();
		Class<? extends AbstractModule> smtpModuleClass              = getSmtpModuleClass();
		
		if (persistenceModuleClass != null)
			moduleMap.put(PERSISTENCE_MODULE_ID, getPersistenceModuleClass());
		
		if (exceptionReporterModuleClass != null)
			moduleMap.put(EXCEPTION_REPORTER_MODULE_ID, getExceptionReporterModuleClass());
		
		if (smtpModuleClass != null)
			moduleMap.put(SMTP_MODULE_ID, getSmtpModuleClass());
		
		return moduleMap;
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
	// =========================================================================
}
