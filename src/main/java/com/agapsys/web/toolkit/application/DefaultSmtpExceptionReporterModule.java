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
import com.agapsys.mail.MessageBuilder;
import com.agapsys.web.toolkit.LoggingModule;
import com.agapsys.web.toolkit.SmtpModule;
import com.agapsys.web.toolkit.WebApplication;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import javax.mail.MessagingException;

/**
 * Default crash reporter module with SMTP sending capabilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class DefaultSmtpExceptionReporterModule extends DefaultExceptionReporterModule {
	// CLASS SCOPE =============================================================
	private static final String SMTP_MODULE_ID = com.agapsys.web.toolkit.application.WebApplication.SMTP_MODULE_ID;
	
	public static final String KEY_ERR_MAIL_RECIPIENTS   = "com.agapsys.web.errMailRecipients";
	public static final String KEY_ERR_MAIL_SUBJECT      = "com.agapsys.web.errSubject";
	
	public static final String DEFAULT_ERR_SUBJECT    = "[%s][System report] Error report";
	public static final String DEFAULT_ERR_RECIPIENTS = "user@email.com";
	
	public static final String RECIPIENT_DELIMITER = ",";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private String[] msgRecipients = null;
	private String   msgSubject    = null;

	public DefaultSmtpExceptionReporterModule(WebApplication application) {
		super(application);
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
	
	protected String getDefaultErrMailSubject() {
		return DEFAULT_ERR_SUBJECT;
	}
	
	protected String getDefaultErrRecipients() {
		return DEFAULT_ERR_RECIPIENTS;
	}
		
	@Override
	public Properties getDefaultSettings() {
		Properties properties = new Properties();
		
		String defaultErrMailSubject = getDefaultErrMailSubject();
		if (defaultErrMailSubject == null || defaultErrMailSubject.trim().isEmpty())
			defaultErrMailSubject = DEFAULT_ERR_SUBJECT;
		
		String defaultErrRecipients = getDefaultErrRecipients();
		if (defaultErrMailSubject == null || defaultErrRecipients.trim().isEmpty())
			defaultErrRecipients = DEFAULT_ERR_RECIPIENTS;
		
		
		properties.setProperty(KEY_ERR_MAIL_SUBJECT,    defaultErrMailSubject);
		properties.setProperty(KEY_ERR_MAIL_RECIPIENTS, defaultErrRecipients);
		
		return properties;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		Properties props = getApplication().getProperties();
		
		msgRecipients = props.getProperty(KEY_ERR_MAIL_RECIPIENTS, DEFAULT_ERR_RECIPIENTS).split(RECIPIENT_DELIMITER);
		
		String tmpErrSubject = props.getProperty(KEY_ERR_MAIL_SUBJECT, DEFAULT_ERR_SUBJECT);
		if (tmpErrSubject.equals(DEFAULT_ERR_SUBJECT))
			tmpErrSubject = String.format(tmpErrSubject, getApplication().getName());
		
		msgSubject = tmpErrSubject;

		for (int i = 0; i < msgRecipients.length; i++)
			msgRecipients[i] = msgRecipients[i].trim();
	}

	@Override
	protected void onStop() {
		msgRecipients = null;
		msgSubject = null;
	}

	public String[] getMsgRecipients() {
		return msgRecipients;
	}
	
	public String getMsgSubject() {
		return msgSubject;
	}
	
	private SmtpModule getSmtpModule() {
		return (SmtpModule) getApplication().getModuleInstance(getSmtpModuleId());
	}

	@Override
	protected void reportError(String message) {
		super.reportError(message);

		try {
			Message msg = new MessageBuilder(getApplication().getProperties().getProperty(DefaultSmtpModule.KEY_SMTP_MAIL_SENDER), getMsgRecipients())
				.setSubject(getMsgSubject())
				.setText(message)
				.build();
			getSmtpModule().sendMessage(msg);
		} catch (MessagingException ex) {
			log(LoggingModule.LOG_TYPE_ERROR, String.format("Error sending error report:\n----\n%s\n----", DefaultExceptionReporterModule.getStackTrace(ex)));
			throw new RuntimeException(ex);
		}
	}
	// =========================================================================
}
