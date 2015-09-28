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

package com.agapsys.web.toolkit.application;

import com.agapsys.mail.Message;
import com.agapsys.web.toolkit.ExceptionReporterModule;
import com.agapsys.web.toolkit.LoggingModule;
import com.agapsys.web.toolkit.Module;
import com.agapsys.web.toolkit.PersistenceModule;
import com.agapsys.web.toolkit.SmtpModule;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class WebApplication extends com.agapsys.web.toolkit.WebApplication {
	// CLASS SCOPE =============================================================
	static final String PERSISTENCE_MODULE_ID        = WebApplication.class.getPackage().getName() + ".persistence";
	static final String EXCEPTION_REPORTER_MODULE_ID = WebApplication.class.getPackage().getName() + ".exceptionReporter";
	static final String SMTP_MODULE_ID               = WebApplication.class.getPackage().getName() + ".smtp";
	static final String LOGGING_MODULE_ID            = WebApplication.class.getPackage().getName() + ".logging";
	
	public static WebApplication getInstance() {
		return (WebApplication) com.agapsys.web.toolkit.WebApplication.getInstance();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	protected Map<String, Class<? extends Module>> getModuleClassMap() {
		Map<String, Class<? extends Module>> moduleMap = new LinkedHashMap<>();

		Class<? extends Module> persistenceModuleClass       = getPersistenceModuleClass();
		Class<? extends Module> exceptionReporterModuleClass = getExceptionReporterModuleClass();
		Class<? extends Module> smtpModuleClass              = getSmtpModuleClass();
		Class<? extends Module> loggingModuleClass           = getLoggingModuleClass();
		
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
	
	protected Class<? extends PersistenceModule> getPersistenceModuleClass() {
		return DefaultPersistenceModule.class;
	}
	
	protected Class<? extends ExceptionReporterModule> getExceptionReporterModuleClass() {
		return DefaultSmtpExceptionReporterModule.class;
	}

	protected Class<? extends SmtpModule> getSmtpModuleClass() {
		return DefaultSmtpModule.class;
	}
	
	protected Class<? extends LoggingModule> getLoggingModuleClass() {
		return DefaultLoggingModule.class;
	}

	public EntityManager getEntityManager() throws IllegalStateException {
		PersistenceModule persistenceModule = (PersistenceModule) getModuleInstance(PERSISTENCE_MODULE_ID);
		
		if (persistenceModule != null)
			return persistenceModule.getEntityManager();
		else
			return null;
	}
	
	public void reportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) {
		ExceptionReporterModule exceptionReporterModule = (ExceptionReporterModule) getModuleInstance(EXCEPTION_REPORTER_MODULE_ID);
		
		if (exceptionReporterModule != null)
			exceptionReporterModule.reportErroneousRequest(req, resp);
	}
	
	public void sendMessage(Message message) {
		SmtpModule smtpModule = (SmtpModule) getModuleInstance(SMTP_MODULE_ID);
		
		if (smtpModule != null)
			smtpModule.sendMessage(message);
	}
	
	public void log(String logType, String message) {
		LoggingModule loggingModule = (LoggingModule) getModuleInstance(LOGGING_MODULE_ID);
		
		if (loggingModule != null)
			loggingModule.log(logType, message);
	}
	// =========================================================================
}
