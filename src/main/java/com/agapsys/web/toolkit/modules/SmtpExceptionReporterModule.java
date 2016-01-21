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

package com.agapsys.web.toolkit.modules;

import com.agapsys.mail.Message;
import com.agapsys.mail.MessageBuilder;
import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.web.toolkit.Module;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Default exception reporter module with SMTP sending capabilities.
 * The module requires {@linkplain SmtpModule}.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SmtpExceptionReporterModule extends ExceptionReporterModule {
	// CLASS SCOPE =============================================================
	// SETTINGS ----------------------------------------------------------------
	public static final String KEY_RECIPIENTS   = "agapsys.webtoolkit.smtpExceptionReporter.recipients";
	public static final String KEY_SUBJECT      = "agapsys.webtoolkit.smtpExceptionReporter.subject";
	// -------------------------------------------------------------------------
	
	public static final String APP_NAME_TOKEN = "${appName}";
	
	public static final String DEFAULT_SUBJECT    = String.format("[%s][System report] Error report", APP_NAME_TOKEN);
	public static final String DEFAULT_RECIPIENTS = "user@email.com";
	
	public static final String RECIPIENT_DELIMITER = ",";
	
	private static final Class<? extends Module>[] DEPENDENCIES = new Class[] {SmtpModule.class};
	
	/**
	 * Returns an array of recipient addresses from a delimited string
	 * @param recipients delimited string
	 * @param delimiter delimiter
	 * @return array of {@linkplain InternetAddress} instances
	 */
	private static InternetAddress[] getRecipientsFromString(String recipients, String delimiter) {
		if (recipients == null || recipients.trim().isEmpty())
			throw new RuntimeException("Null/empty recipients");
		
		if (delimiter == null || delimiter.trim().isEmpty())
			throw new RuntimeException("Null/empty delimiter");
		
		String[] recipientArray = recipients.split(Pattern.quote(RECIPIENT_DELIMITER));
		InternetAddress[] result = new InternetAddress[recipientArray.length];
		
		for (int i = 0; i < recipientArray.length; i++) {
			try {
				result[i] = new InternetAddress(recipientArray[i].trim());
			} catch (AddressException ex) {
				throw new RuntimeException("Invalid address: " + recipientArray[i].trim(), ex);
			}
		}
		
		return result;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private InternetAddress[] recipients = null;
	private String            subject    = null;

	@Override
	public Class<? extends Module>[] getDependencies() {
		return DEPENDENCIES;
	}
	
	@Override
	public Properties getDefaultProperties() {
		Properties properties = super.getDefaultProperties();
		
		if (properties == null)
			properties = new Properties();
		
		properties.setProperty(KEY_SUBJECT,    DEFAULT_SUBJECT);
		properties.setProperty(KEY_RECIPIENTS, DEFAULT_RECIPIENTS);
		
		return properties;
	}
		
	@Override
	protected void onStart(AbstractWebApplication webApp) {
		super.onStart(webApp);
		Properties properties = webApp.getProperties();
		
		String val;
		
		// Recipients
		val = getMandatoryProperty(properties, KEY_RECIPIENTS);
		recipients = getRecipientsFromString(val, RECIPIENT_DELIMITER);
		
		// Subject
		val = getMandatoryProperty(properties, KEY_SUBJECT);
		subject = val;
	}

	@Override
	protected void onStop() {
		recipients = null;
		subject = null;
	}

	/**
	 * Returns message recipients defined in application settings
	 * @return message recipients defined in application settings
	 */
	public InternetAddress[] getRecipients() {
		return recipients;
	}
	
	/**
	 * Returns message subject defined in application settings
	 * @return message subject defined in application settings
	 */
	public String getSubject() {
		return subject;
	}

	@Override
	protected void reportErrorMessage(String message) {
		super.reportErrorMessage(message);
		
		SmtpModule smtpModule =  getModule(SmtpModule.class);
		
		String finalSubject = getSubject().replaceAll(Pattern.quote(APP_NAME_TOKEN), getWebApplication().getName());

		Message msg = new MessageBuilder(smtpModule.getSender(), getRecipients())
			.setSubject(finalSubject).setText(message).build();

		smtpModule.sendMessage(msg);
	}
	// =========================================================================
}
