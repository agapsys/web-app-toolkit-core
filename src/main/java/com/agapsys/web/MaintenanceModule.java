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

package com.agapsys.web;

import com.agapsys.web.WebApplication.LogType;
import com.agapsys.mail.Message;
import com.agapsys.mail.SecurityType;
import com.agapsys.mail.SmtpSender;
import com.agapsys.mail.SmtpSettings;
import com.agapsys.web.utils.Properties;
import com.agapsys.web.utils.RequestUtils;
import com.agapsys.web.utils.Utils;
import java.io.IOException;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Maintenance module.
 * This is module is responsible by:
 *     1) Sending email for developers when errors happens in the application
 *     2) Shutdown application when error count reaches a limit
 * 
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
class MaintenanceModule {
	// CLASS SCOPE =============================================================
	private static final String ATTR_STATUS_CODE    = "javax.servlet.error.status_code";
	private static final String ATTR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";
	private static final String ATTR_MESSAGE        = "javax.servlet.error.message";
	private static final String ATTR_REQUEST_URI    = "javax.servlet.error.request_uri";
	private static final String ATTR_EXCEPTION      = "javax.servlet.error.exception";
	
	public static final String HEADER_STATUS             = "com.agapsys.web.status";
	public static final String HEADER_STATUS_VALUE_OK    = "ok";
	public static final String HEADER_STATUS_VALUE_ERROR = "error";
		
	public static final String KEY_NODE_NAME  = "com.agapsys.web.nodeName";
	
	public static final String KEY_ERR_MAIL_RECIPIENTS   = "com.agapsys.web.errMailRecipients";
	public static final String KEY_ERR_MAIL_SENDER       = "com.agapsys.web.errMailSender";
	public static final String KEY_ERR_MAIL_SUBJECT      = "com.agapsys.web.errSubject";
	
	public static final String RECIPIENT_DELIMITER = ",";
	
	public static final String DEFAULT_NODE_NAME     = "node-01";
	public static final String DEFAULT_SMTP_SERVER   = "smtp.server.com";
	public static final String DEFAULT_SMTP_AUTH     = "true";
	public static final String DEFAULT_SMTP_USERNAME = "user";
	public static final String DEFAULT_SMTP_PASSWORD = "password";
	public static final String DEFAULT_SMTP_SECURITY = SecurityType.NONE.name();
	public static final String DEFAULT_SMTP_PORT     = "25";
			
	public static final String DEFAULT_ERR_SUBJECT    = "[System report] Error report";
	public static final String DEFAULT_ERR_RECIPIENTS = "user@email.com";
	public static final String DEFAULT_ERR_SENDER     = "no-reply@email.com";
	
	private static String     nodeName;
	private static SmtpSender smtpSender;
	private static String[]   msgRecipients;
	private static String     msgSender;
	private static String     msgSubject;
	
	static void loadDefaults(Properties properties, boolean keepExisting) {
		properties.setProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME, keepExisting);
		
		properties.setProperty(KEY_ERR_MAIL_RECIPIENTS, DEFAULT_ERR_RECIPIENTS, keepExisting);
		properties.setProperty(KEY_ERR_MAIL_SENDER,     DEFAULT_ERR_SENDER,     keepExisting);
		properties.setProperty(KEY_ERR_MAIL_SUBJECT,    DEFAULT_ERR_SUBJECT,    keepExisting);
		
		properties.addEmptyLine();
		
		properties.setProperty(SmtpSettings.KEY_SERVER,   DEFAULT_SMTP_SERVER,   keepExisting);
		properties.setProperty(SmtpSettings.KEY_AUTH,     DEFAULT_SMTP_AUTH,     keepExisting);
		properties.setProperty(SmtpSettings.KEY_USERNAME, DEFAULT_SMTP_USERNAME, keepExisting);
		properties.setProperty(SmtpSettings.KEY_PASSWORD, DEFAULT_SMTP_PASSWORD, keepExisting);
		properties.setProperty(SmtpSettings.KEY_SECURITY, DEFAULT_SMTP_SECURITY, keepExisting);
		properties.setProperty(SmtpSettings.KEY_PORT,     DEFAULT_SMTP_PORT,     keepExisting);
	}
	
	public static boolean isRunning() {
		return smtpSender != null;
	}
	
	public static void start() {	
		Properties properties = WebApplication.getProperties();
		
		SmtpSettings settings = new SmtpSettings(properties);
		smtpSender = new SmtpSender(settings);
		
		nodeName = properties.getProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
		msgRecipients = properties.getProperty(KEY_ERR_MAIL_RECIPIENTS, DEFAULT_ERR_RECIPIENTS).split(RECIPIENT_DELIMITER);
		msgSender = properties.getProperty(KEY_ERR_MAIL_SENDER, DEFAULT_ERR_SENDER);
		msgSubject = properties.getProperty(KEY_ERR_MAIL_SUBJECT, DEFAULT_ERR_SUBJECT);

		for (int i = 0; i < msgRecipients.length; i++)
			msgRecipients[i] = msgRecipients[i].trim();
	}
	
	/** This method is used for test-only */
	private static void addStatusHeader(HttpServletResponse resp, String status) {
		resp.addHeader(HEADER_STATUS, status);
	}
	
	public static void handleErrorRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			Integer statusCode = (Integer) req.getAttribute(ATTR_STATUS_CODE);
			Class exceptionType = (Class) req.getAttribute(ATTR_EXCEPTION_TYPE);
			String errorMessage = (String) req.getAttribute(ATTR_MESSAGE);
			String requestUri = (String) req.getAttribute(ATTR_REQUEST_URI);
			String userAgent = RequestUtils.getClientUserAgent(req);

			String clientId = RequestUtils.getClientIp(req);

			Throwable throwable = (Throwable) req.getAttribute(ATTR_EXCEPTION);

			if (throwable != null) {
				String stacktrace = Utils.getStackTrace(throwable);

				Message msg = new Message();
				msg.setSenderAddress(msgSender);
				msg.setRecipients(msgRecipients);
				msg.setSubject(msgSubject);
				msg.setText(
					"An error was detected"
					+ "\n\n"
					+ "Application: " + WebApplication.getName() + "\n"
					+ "Application version: " + WebApplication.getVersion() + "\n"
					+ "Node name: " + nodeName  + "\n\n"
					+ "Server timestamp: " + Utils.getLocalTimestamp() + "\n"
					+ "Status code: " + statusCode + "\n"
					+ "Exception type: " +(exceptionType != null ? exceptionType.getName() : "null") + "\n"
					+ "Error message: " + errorMessage + "\n"
					+ "Request URI: " + requestUri + "\n"
					+ "User-agent: " + userAgent + "\n"
					+ "Client id: " + clientId + "\n"
					+ "Stacktrace:\n" + stacktrace
				);
				
				WebApplication.log(LogType.ERROR, String.format("Application error:\n----\n%s\n----", msg.getText()));
				
				smtpSender.sendMessage(msg);
				addStatusHeader(resp, HEADER_STATUS_VALUE_OK);
			} else {
				String extraInfo =
					"User-agent: " + userAgent + "\n"
					+ "Client id: " + clientId;
				
				WebApplication.log(LogType.ERROR, String.format("Bad request for maintenance module:\n----\n%s\n----", extraInfo));
				addStatusHeader(resp, HEADER_STATUS_VALUE_ERROR);
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				
			}
		} catch (MessagingException ex) {
			WebApplication.log(LogType.ERROR, String.format("Error sending error report:\n----\n%s\n----", Utils.getStackTrace(ex)));
			addStatusHeader(resp, HEADER_STATUS_VALUE_ERROR);
			throw new RuntimeException(ex);
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private MaintenanceModule() {}
	// =========================================================================
}
