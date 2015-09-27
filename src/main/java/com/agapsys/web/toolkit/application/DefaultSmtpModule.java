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

import com.agapsys.web.toolkit.WebApplication;
import com.agapsys.mail.Message;
import com.agapsys.mail.MessageBuilder;
import com.agapsys.mail.SecurityType;
import com.agapsys.mail.SmtpSender;
import com.agapsys.mail.SmtpSettings;
import com.agapsys.web.toolkit.LoggingModule;
import com.agapsys.web.toolkit.SmtpModule;
import com.agapsys.web.toolkit.utils.Properties;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class DefaultSmtpModule extends SmtpModule {
	// CLASS SCOPE =============================================================
	private static final String LOGGING_MODULE_ID = com.agapsys.web.toolkit.application.WebApplication.LOGGING_MODULE_ID;
	
	public static final String KEY_SMTP_MAIL_SENDER = "com.agapsys.web.smtp.sender";
	
	public static final String DEFAULT_SMTP_SENDER   = "no-reply@email.com";
	public static final String DEFAULT_SMTP_SERVER   = "smtp.server.com";
	public static final String DEFAULT_SMTP_AUTH     = "true";
	public static final String DEFAULT_SMTP_USERNAME = "user";
	public static final String DEFAULT_SMTP_PASSWORD = "password";
	public static final String DEFAULT_SMTP_SECURITY = SecurityType.NONE.name();
	public static final String DEFAULT_SMTP_PORT     = "25";
	
	private static final Properties DEFAULT_PROPERTIES;
	
	static {
		DEFAULT_PROPERTIES = new Properties();
		
		DEFAULT_PROPERTIES.setProperty(KEY_SMTP_MAIL_SENDER,      DEFAULT_SMTP_SENDER);
		
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_SERVER,   DEFAULT_SMTP_SERVER);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_AUTH,     DEFAULT_SMTP_AUTH);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_USERNAME, DEFAULT_SMTP_USERNAME);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_PASSWORD, DEFAULT_SMTP_PASSWORD);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_SECURITY, DEFAULT_SMTP_SECURITY);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_PORT,     DEFAULT_SMTP_PORT);
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private SmtpSender      smtpSender = null;
	private InternetAddress sender     = null;

	public DefaultSmtpModule(WebApplication application) {
		super(application);
	}

	protected String getLoggingModuleId() {
		return LOGGING_MODULE_ID;
	}
	
	@Override
	protected Set<String> getOptionalDependencies() {
		Set<String> deps = new LinkedHashSet<>();
		deps.add(getLoggingModuleId());
		return deps;
	}
	
	private LoggingModule getLoggingModule() {
		return (LoggingModule) getApplication().getModuleInstance(getLoggingModuleId());
	}
	
	protected void log(String logType, String message) {
		LoggingModule loggingModule = getLoggingModule();
		
		if (loggingModule != null) {
			loggingModule.log(logType, message);
		} else {
			DefaultLoggingModule.defaultLog(logType, message);
		}
	}
	
	@Override
	protected void onStart() {
		java.util.Properties props = new java.util.Properties();
		props.putAll(getApplication().getProperties().getEntries());
		
		SmtpSettings settings = new SmtpSettings(props);
		smtpSender = new SmtpSender(settings);
		
		try {
			sender = new InternetAddress(props.getProperty(KEY_SMTP_MAIL_SENDER, DEFAULT_SMTP_SENDER));
		} catch (AddressException ex) {
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected void onStop() {
		smtpSender = null;
		sender = null;
	}
	
	@Override
	public Properties getDefaultSettings() {
		return DEFAULT_PROPERTIES;
	}

	@Override
	protected void onSendMessage(Message message) {
		if (isRunning()) {
			try {
				// Forces sender address if message's address not equals to application default sender.
				if (!message.getSenderAddress().equals(sender)) {
					message = new MessageBuilder(sender, message.getRecipients().toArray(new InternetAddress[message.getRecipients().size()]))
						.setCharset(message.getCharset())
						.setMimeSubtype(message.getMimeSubtype())
						.setSubject(message.getSubject())
						.setText(message.getText()).build();
				}
				smtpSender.sendMessage(message);
			} catch (MessagingException ex) {
				log(LoggingModule.LOG_TYPE_ERROR, "Error sending message: " + ex.getMessage());
				throw new RuntimeException(ex);
			}
		}
	}
}
