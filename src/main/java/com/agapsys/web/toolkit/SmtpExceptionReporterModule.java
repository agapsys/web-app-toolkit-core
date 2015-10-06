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
	
	/**
	 * Return the appropriate string representation of given array of recipients
	 * @param recipients array of recipients
	 * @param delimiter delimiter
	 * @return string representation of given array of recipients
	 */
	private static String getRecipientsString(InternetAddress[] recipients, String delimiter) {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < recipients.length; i++) {
			if (i > 0) {
				sb.append(delimiter);
			}
			
			sb.append(recipients[i].toString());
		}
		
		return sb.toString();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private InternetAddress[] recipients = null;
	private String            subject    = null;

	public SmtpExceptionReporterModule(AbstractApplication application) {
		super(application);
	}
	
	/**
	 * Returns the default subject for messaged sent by the module
	 * @return the default subject for messaged sent by the module
	 */
	protected String getDefaultSubject() {
		return DEFAULT_SUBJECT;
	}
	
	/**
	 * Returns the default recipients for messages sent by the module
	 * @return the default recipients for messages sent by the module
	 */
	protected InternetAddress[] getDefaultRecipients() {
		return getRecipientsFromString(DEFAULT_RECIPIENTS, RECIPIENT_DELIMITER);
	}
	
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = super.getDefaultSettings();
		
		String defaultSubject = getDefaultSubject();
		if (defaultSubject == null || defaultSubject.trim().isEmpty())
			throw new RuntimeException("Null/Empty default subject");
		
		InternetAddress[] defaultRecipients = getDefaultRecipients();
		if (defaultRecipients == null || defaultRecipients.length == 0)
			throw new RuntimeException("Null/Empty default recipients");
		
		properties.setProperty(KEY_SUBJECT,    defaultSubject);
		properties.setProperty(KEY_RECIPIENTS, getRecipientsString(defaultRecipients, RECIPIENT_DELIMITER));
		
		return properties;
	}
	
	/** @return the SMTP module class used by this module. */
	protected Class<? extends SmtpModule> getSmtpModuleClass() {
		return SmtpModule.class;
	}
	
	@Override
	protected Set<Class<? extends AbstractModule>> getMandatoryDependencies() {
		Set<Class<? extends AbstractModule>> superMandatoryDeps = super.getMandatoryDependencies();
		
		Set<Class<? extends AbstractModule>> deps = new LinkedHashSet<>();
		if (superMandatoryDeps != null)
			deps.addAll(superMandatoryDeps);
		
		deps.add(getSmtpModuleClass());
		return deps;
	}
		
	@Override
	protected void onStart() {
		super.onStart();
		Properties properties = getApplication().getProperties();
		
		String val;
		
		// Recipients
		val = properties.getProperty(KEY_RECIPIENTS);
		if (val == null || val.trim().isEmpty()) {
			recipients = getDefaultRecipients();
		} else {
			recipients = getRecipientsFromString(val, RECIPIENT_DELIMITER);
		}
		
		// Subject
		val = properties.getProperty(KEY_SUBJECT);
		if (val == null || val.trim().isEmpty())
			val = getDefaultSubject();
		
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
	
	private SmtpModule getSmtpModule() {
		// Since SMTP module is a mandatory dependency there is no need to check if it is null
		return (SmtpModule) getApplication().getModuleInstance(getSmtpModuleClass());
	}

	@Override
	protected void reportErrorMessage(String message) {
		super.reportErrorMessage(message);
		
		SmtpModule smtpModule = getSmtpModule();
		
		String finalSubject = getSubject().replaceAll(Pattern.quote(APP_NAME_TOKEN), getApplication().getName());

		Message msg = new MessageBuilder(smtpModule.getSender(), getRecipients())
			.setSubject(finalSubject).setText(message).build();

		smtpModule.sendMessage(msg);
	}
	// =========================================================================
}
