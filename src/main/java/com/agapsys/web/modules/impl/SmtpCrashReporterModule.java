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

package com.agapsys.web.modules.impl;

import com.agapsys.mail.Message;
import com.agapsys.mail.SecurityType;
import com.agapsys.mail.SmtpSender;
import com.agapsys.mail.SmtpSettings;
import com.agapsys.web.WebApplication;
import com.agapsys.web.utils.Properties;
import com.agapsys.web.utils.Utils;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default crash reporter module with SMTP sending capabilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SmtpCrashReporterModule extends DefaultCrashReporterModule {
	// CLASS SCOPE =============================================================
	public static final String HEADER_STATUS             = "com.agapsys.web.status";
	public static final String HEADER_STATUS_VALUE_OK    = "ok";
	public static final String HEADER_STATUS_VALUE_ERROR = "error";
		
	public static final String KEY_ERR_MAIL_RECIPIENTS   = "com.agapsys.web.errMailRecipients";
	public static final String KEY_ERR_MAIL_SENDER       = "com.agapsys.web.errMailSender";
	public static final String KEY_ERR_MAIL_SUBJECT      = "com.agapsys.web.errSubject";
	
	public static final String RECIPIENT_DELIMITER = ",";
	
	public static final String DEFAULT_SMTP_SERVER   = "smtp.server.com";
	public static final String DEFAULT_SMTP_AUTH     = "true";
	public static final String DEFAULT_SMTP_USERNAME = "user";
	public static final String DEFAULT_SMTP_PASSWORD = "password";
	public static final String DEFAULT_SMTP_SECURITY = SecurityType.NONE.name();
	public static final String DEFAULT_SMTP_PORT     = "25";
			
	public static final String DEFAULT_ERR_SUBJECT    = "[System report] Error report";
	public static final String DEFAULT_ERR_RECIPIENTS = "user@email.com";
	public static final String DEFAULT_ERR_SENDER     = "no-reply@email.com";
	
	private static final Properties DEFAULT_PROPERTIES;
	
	static {
		DEFAULT_PROPERTIES = new Properties();
		DEFAULT_PROPERTIES.setProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
		
		DEFAULT_PROPERTIES.setProperty(KEY_ERR_MAIL_RECIPIENTS, DEFAULT_ERR_RECIPIENTS);
		DEFAULT_PROPERTIES.setProperty(KEY_ERR_MAIL_SENDER,     DEFAULT_ERR_SENDER);
		DEFAULT_PROPERTIES.setProperty(KEY_ERR_MAIL_SUBJECT,    DEFAULT_ERR_SUBJECT);
		
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_SERVER,   DEFAULT_SMTP_SERVER);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_AUTH,     DEFAULT_SMTP_AUTH);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_USERNAME, DEFAULT_SMTP_USERNAME);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_PASSWORD, DEFAULT_SMTP_PASSWORD);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_SECURITY, DEFAULT_SMTP_SECURITY);
		DEFAULT_PROPERTIES.setProperty(SmtpSettings.KEY_PORT,     DEFAULT_SMTP_PORT);
	}
	
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final SmtpSender smtpSender;
	private final String[]   msgRecipients;
	private final String     msgSender;
	private final String     msgSubject;
	
	public SmtpCrashReporterModule() {
		java.util.Properties props = new java.util.Properties();
		props.putAll(WebApplication.getProperties().getEntries());
		
		SmtpSettings settings = new SmtpSettings(props);
		smtpSender = new SmtpSender(settings);
		
		msgRecipients = props.getProperty(KEY_ERR_MAIL_RECIPIENTS, DEFAULT_ERR_RECIPIENTS).split(RECIPIENT_DELIMITER);
		msgSender     = props.getProperty(KEY_ERR_MAIL_SENDER,     DEFAULT_ERR_SENDER);
		msgSubject    = props.getProperty(KEY_ERR_MAIL_SUBJECT,    DEFAULT_ERR_SUBJECT);

		for (int i = 0; i < msgRecipients.length; i++)
			msgRecipients[i] = msgRecipients[i].trim();
	}
	
	@Override
	public Properties getDefaultSettings() {
		return DEFAULT_PROPERTIES;
	}

	@Override
	protected void logError(String message) {
		super.logError(message);
		
		try {
			Message msg = new Message();
			msg.setSenderAddress(msgSender);
			msg.setRecipients(msgRecipients);
			msg.setSubject(msgSubject);
			msg.setText(message);
			smtpSender.sendMessage(msg);
		} catch (MessagingException ex) {
			WebApplication.log(WebApplication.LOG_TYPE_ERROR, String.format("Error sending error report:\n----\n%s\n----", Utils.getStackTrace(ex)));
			throw new RuntimeException(ex);
		}
	}

	/** This method is used for test-only */
	private void addStatusHeader(HttpServletResponse resp, String status) {
		resp.addHeader(HEADER_STATUS, status);
	}
	
	@Override
	public void reportError(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			super.reportError(req, resp);
			addStatusHeader(resp, HEADER_STATUS_VALUE_OK);
		} catch (RuntimeException ex) {
			addStatusHeader(resp, HEADER_STATUS_VALUE_ERROR);
			throw ex;
		}
	}
	// =========================================================================
}
