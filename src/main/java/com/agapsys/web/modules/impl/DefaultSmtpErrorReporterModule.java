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
import com.agapsys.web.WebApplication;
import com.agapsys.web.utils.Properties;
import javax.mail.MessagingException;

/**
 * Default crash reporter module with SMTP sending capabilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class DefaultSmtpErrorReporterModule extends DefaultErrorReporterModule {
	// CLASS SCOPE =============================================================
	public static final String KEY_ERR_MAIL_RECIPIENTS   = "com.agapsys.web.errMailRecipients";
	public static final String KEY_ERR_MAIL_SENDER       = "com.agapsys.web.errMailSender";
	public static final String KEY_ERR_MAIL_SUBJECT      = "com.agapsys.web.errSubject";
	public static final String RECIPIENT_DELIMITER = ",";
	
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
	}
	
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private String[] msgRecipients = null;
	private String   msgSender     = null;
	private String   msgSubject    = null;
	
	@Override
	protected void onStart() {
		Properties props = WebApplication.getProperties();
		
		msgRecipients = props.getProperty(KEY_ERR_MAIL_RECIPIENTS, DEFAULT_ERR_RECIPIENTS).split(RECIPIENT_DELIMITER);
		msgSender     = props.getProperty(KEY_ERR_MAIL_SENDER,     DEFAULT_ERR_SENDER);
		msgSubject    = props.getProperty(KEY_ERR_MAIL_SUBJECT,    DEFAULT_ERR_SUBJECT);

		for (int i = 0; i < msgRecipients.length; i++)
			msgRecipients[i] = msgRecipients[i].trim();
	}

	@Override
	protected void onStop() {
		msgRecipients = null;
		msgSender = null;
		msgSubject = null;
	}
	
	@Override
	public Properties getDefaultSettings() {
		return DEFAULT_PROPERTIES;
	}

	@Override
	protected void logError(String message) {
		if (isRunning()) {
			super.logError(message);

			try {
				Message msg = new Message();
				msg.setSenderAddress(msgSender);
				msg.setRecipients(msgRecipients);
				msg.setSubject(msgSubject);
				msg.setText(message);
				WebApplication.sendMessage(msg);
			} catch (MessagingException ex) {
				WebApplication.log(WebApplication.LOG_TYPE_ERROR, String.format("Error sending error report:\n----\n%s\n----", DefaultErrorReporterModule.getStackTrace(ex)));
				throw new RuntimeException(ex);
			}
		}
	}
	// =========================================================================
}
