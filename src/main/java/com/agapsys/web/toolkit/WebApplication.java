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

import com.agapsys.mail.Message;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class WebApplication extends AbstractWebApplication {
	// CLASS SCOPE =============================================================
	public static final String PERSISTENCE_MODULE_ID        = "agapsys.webtoolkit.persistence";
	public static final String EXCEPTION_REPORTER_MODULE_ID = "agapsys.webtoolkit.exceptionReporter";
	public static final String SMTP_MODULE_ID               = "agapsys.webtoolkit.smtp";
	public static final String LOGGING_MODULE_ID            = "agapsys.webtoolkit.logging";
	
	public static WebApplication getInstance() {
		return (WebApplication) com.agapsys.web.toolkit.AbstractWebApplication.getInstance();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	protected Map<String, Class<? extends AbstractModule>> getModuleClassMap() {
		Map<String, Class<? extends AbstractModule>> moduleMap = new LinkedHashMap<>();

		Class<? extends AbstractModule> persistenceModuleClass       = getPersistenceModuleClass();
		Class<? extends AbstractModule> exceptionReporterModuleClass = getExceptionReporterModuleClass();
		Class<? extends AbstractModule> smtpModuleClass              = getSmtpModuleClass();
		Class<? extends AbstractModule> loggingModuleClass           = getLoggingModuleClass();
		
		if (persistenceModuleClass != null)
			moduleMap.put(PERSISTENCE_MODULE_ID, getPersistenceModuleClass());
		
		if (exceptionReporterModuleClass != null)
			moduleMap.put(EXCEPTION_REPORTER_MODULE_ID, getExceptionReporterModuleClass());
		
		if (smtpModuleClass != null)
			moduleMap.put(SMTP_MODULE_ID, getSmtpModuleClass());
		
		if (loggingModuleClass != null)
			moduleMap.put(LOGGING_MODULE_ID, getLoggingModuleClass());
		
		return moduleMap;
	}
	
	protected Class<? extends AbstractPersistenceModule> getPersistenceModuleClass() {
		return PersistenceModule.class;
	}
	
	protected Class<? extends AbstractExceptionReporterModule> getExceptionReporterModuleClass() {
		return SmtpExceptionReporterModule.class;
	}

	protected Class<? extends AbstractSmtpModule> getSmtpModuleClass() {
		return SmtpModule.class;
	}
	
	protected Class<? extends AbstractLoggingModule> getLoggingModuleClass() {
		return LoggingModule.class;
	}

	public EntityManager getEntityManager() throws IllegalStateException {
		AbstractPersistenceModule persistenceModule = (AbstractPersistenceModule) getModuleInstance(PERSISTENCE_MODULE_ID);
		
		if (persistenceModule == null)
			throw new RuntimeException("There is no persistence module");
		
		return persistenceModule.getEntityManager();
	}
	
	public void reportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) {
		AbstractExceptionReporterModule exceptionReporterModule = (AbstractExceptionReporterModule) getModuleInstance(EXCEPTION_REPORTER_MODULE_ID);
		
		if (exceptionReporterModule == null)
			throw new RuntimeException("There is no exception reporter module");
		
		exceptionReporterModule.reportErroneousRequest(req, resp);
	}
	
	public void sendMessage(Message message) {
		AbstractSmtpModule smtpModule = (AbstractSmtpModule) getModuleInstance(SMTP_MODULE_ID);
		
		if (smtpModule == null)
			throw new RuntimeException("There is no SMTP module");
		
		smtpModule.sendMessage(message);
	}
	
	public void log(String logType, String message) {
		AbstractLoggingModule loggingModule = (AbstractLoggingModule) getModuleInstance(LOGGING_MODULE_ID);
		
		if (loggingModule == null)
			throw new RuntimeException("There is no logging module");
		
		loggingModule.log(logType, message);
	}
	// =========================================================================
}
