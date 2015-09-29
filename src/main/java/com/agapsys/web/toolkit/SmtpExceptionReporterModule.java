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
import com.agapsys.mail.MessageBuilder;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import javax.mail.MessagingException;

/**
 * Default crash reporter module with SMTP sending capabilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SmtpExceptionReporterModule extends ExceptionReporterModule {
	// CLASS SCOPE =============================================================
	private static final String SMTP_MODULE_ID = WebApplication.SMTP_MODULE_ID;
	
	// SETTINGS ----------------------------------------------------------------
	public static final String KEY_RECIPIENTS   = "agapsys.webtoolkit.smtpExceptionReporter.recipients";
	public static final String KEY_SUBJECT      = "agapsys.webtoolkit.smtpExceptionReporter.subject";
	// -------------------------------------------------------------------------
	
	public static final String APP_NAME_TOKEN = "@appName";
	
	public static final String DEFAULT_SUBJECT    = String.format("[%s][System report] Error report", APP_NAME_TOKEN);
	public static final String DEFAULT_RECIPIENTS = "user@email.com";
	
	public static final String RECIPIENT_DELIMITER = ",";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private String[] recipients = null;
	private String   subject    = null;

	public SmtpExceptionReporterModule(AbstractWebApplication application) {
		super(application);
	}
	
	protected String getDefaultSubject() {
		return DEFAULT_SUBJECT;
	}
	
	protected String getDefaultRecipients() {
		return DEFAULT_RECIPIENTS;
	}
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = super.getDefaultSettings();
		
		String defaultSubject = getDefaultSubject();
		if (defaultSubject == null || defaultSubject.trim().isEmpty())
			defaultSubject = DEFAULT_SUBJECT;
		
		String defaultRecipients = getDefaultRecipients();
		if (defaultSubject == null || defaultRecipients.trim().isEmpty())
			defaultRecipients = DEFAULT_RECIPIENTS;
		
		properties.setProperty(KEY_SUBJECT,    defaultSubject);
		properties.setProperty(KEY_RECIPIENTS, defaultRecipients);
		
		return properties;
	}
	
	protected String getSmtpModuleId() {
		return SMTP_MODULE_ID;
	}
	
	@Override
	protected Set<String> getMandatoryDependencies() {
		Set<String> superMandatoryDeps = super.getMandatoryDependencies();
		
		Set<String> deps = new LinkedHashSet<>();
		if (superMandatoryDeps != null)
			deps.addAll(superMandatoryDeps);
		
		deps.add(getSmtpModuleId());
		return deps;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Properties properties = getApplication().getProperties();
		
		String val;
		
		// Recipients
		val = properties.getProperty(KEY_RECIPIENTS);
		if (val == null || val.trim().isEmpty())
			val = getDefaultRecipients();
		
		recipients = val.split(RECIPIENT_DELIMITER);
		for (int i = 0; i < recipients.length; i++) {
			recipients[i] = recipients[i].trim();
		}
		
		// Subject
		val = properties.getProperty(KEY_SUBJECT);
		if (val == null || val.trim().isEmpty())
			val = getDefaultSubject();
		
		val = val.replaceAll(APP_NAME_TOKEN, getApplication().getName());
		subject = val;
	}

	@Override
	protected void onStop() {
		recipients = null;
		subject = null;
	}

	
	public String[] getRecipients() {
		return recipients;
	}
	
	public String getSubject() {
		return subject;
	}
	
	private AbstractSmtpModule getSmtpModule() {
		return (AbstractSmtpModule) getApplication().getModuleInstance(getSmtpModuleId());
	}

	@Override
	protected void reportError(String message) {
		super.reportError(message);

		try {
			Message msg = new MessageBuilder(getApplication().getProperties().getProperty(SmtpModule.KEY_SENDER), getRecipients())
				.setSubject(getSubject())
				.setText(message)
				.build();
			
			getSmtpModule().sendMessage(msg);
		} catch (MessagingException ex) {
			log(AbstractLoggingModule.LOG_TYPE_ERROR, String.format("Error sending error report:\n----\n%s\n----", AbstractExceptionReporterModule.getStackTrace(ex)));
			throw new RuntimeException(ex);
		}
	}
	// =========================================================================
}