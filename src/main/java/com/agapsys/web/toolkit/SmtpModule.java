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
import com.agapsys.mail.SecurityType;
import com.agapsys.mail.SmtpSender;
import com.agapsys.mail.SmtpSettings;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SmtpModule extends AbstractSmtpModule {
	// CLASS SCOPE =============================================================
	private static final String LOGGING_MODULE_ID = WebApplication.LOGGING_MODULE_ID;
	
	// SETTINGS ----------------------------------------------------------------
	public static final String KEY_SENDER = "agapsys.webtoolkit.smtp.sender";
	
	public static final String KEY_SERVER        = SmtpSettings.KEY_SERVER;
	public static final String KEY_AUTH_ENABLED  = SmtpSettings.KEY_AUTH;
	public static final String KEY_USERNAME      = SmtpSettings.KEY_USERNAME;
	public static final String KEY_PASSWORD      = SmtpSettings.KEY_PASSWORD;
	public static final String KEY_SECURITY_TYPE = SmtpSettings.KEY_SECURITY;
	public static final String KEY_PORT          = SmtpSettings.KEY_PORT;
	// -------------------------------------------------------------------------
	
	public static final String       DEFAULT_SENDER        = "no-reply@email.com";
	public static final String       DEFAULT_SERVER        = "smtp.server.com";
	public static final boolean      DEFAULT_AUTH_ENABLED  = true;
	public static final String       DEFAULT_USERNAME      = "user";
	public static final String       DEFAULT_PASSWORD      = "password";
	public static final SecurityType DEFAULT_SECURITY_TYPE = SecurityType.NONE;
	public static final int          DEFAULT_PORT          = 25;
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private SmtpSender      smtpSender = null;
	private InternetAddress sender     = null;
	
	public SmtpModule(AbstractWebApplication application) {
		super(application);
	}

	protected String getDefaultSender() {
		return DEFAULT_SENDER;
	}
	
	protected String getDefaultServer() {
		return DEFAULT_SERVER;
	}
	
	protected boolean getDefaultAuthEnabled() {
		return DEFAULT_AUTH_ENABLED;
	}
	
	protected String getDefaultUsername() {
		return DEFAULT_USERNAME;
	}
	
	protected String getDefaultPassword() {
		return DEFAULT_PASSWORD;
	}
	
	protected SecurityType getDefaultSecurityType() {
		return DEFAULT_SECURITY_TYPE;
	}
	
	protected int getDefaultPort() {
		return DEFAULT_PORT;
	}
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = new Properties();
		
		String defaultSender = getDefaultSender();
		if (defaultSender == null || defaultSender.trim().isEmpty())
			defaultSender = DEFAULT_SENDER;
		
		String defaultServer = getDefaultServer();
		if (defaultServer == null || defaultServer.trim().isEmpty())
			defaultServer = DEFAULT_SERVER;
		
		boolean defaultAuthEnabled = getDefaultAuthEnabled();
		
		String defaultUsername = getDefaultUsername();
		if (defaultUsername == null || defaultUsername.trim().isEmpty())
			defaultUsername = DEFAULT_USERNAME;
		
		String defaultPassword = getDefaultPassword();
		if (defaultPassword == null || defaultPassword.trim().isEmpty())
			defaultPassword = DEFAULT_PASSWORD;
		
		SecurityType defaultSecurityType = getDefaultSecurityType();
		if (defaultSecurityType == null)
			defaultSecurityType = DEFAULT_SECURITY_TYPE;
		
		int defaultPort = getDefaultPort();
		if (defaultPort < 1)
			throw new RuntimeException("Invalid port: " + defaultPort);
		
		properties.put(KEY_SENDER, defaultSender);
		properties.put(KEY_SERVER, defaultServer);
		properties.put(KEY_AUTH_ENABLED, "" + defaultAuthEnabled);
		properties.put(KEY_USERNAME, defaultUsername);
		properties.put(KEY_PASSWORD, defaultPassword);
		properties.put(KEY_SECURITY_TYPE, defaultSecurityType.name());
		properties.put(KEY_PORT, "" + defaultPort);
		return properties;
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
	
	private AbstractLoggingModule getLoggingModule() {
		return (AbstractLoggingModule) getApplication().getModuleInstance(getLoggingModuleId());
	}
	
	protected void log(String logType, String message) {
		AbstractLoggingModule loggingModule = getLoggingModule();
		
		if (loggingModule != null) {
			loggingModule.log(logType, message);
		} else {
			AbstractLoggingModule.logToConsole(logType, message);
		}
	}
	
	@Override
	protected void onStart() {
		Properties properties = getApplication().getProperties();
		
		SmtpSettings settings = new SmtpSettings(properties);
		smtpSender = new SmtpSender(settings);
		
		try {
			String val;
			val = properties.getProperty(KEY_SENDER);
			if (val == null || val.trim().isEmpty())
				val = getDefaultSender();
			
			sender = new InternetAddress(val);
			
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
				log(AbstractLoggingModule.LOG_TYPE_ERROR, "Error sending message: " + ex.getMessage());
				throw new RuntimeException(ex);
			}
		}
	}
}
