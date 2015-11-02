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
import com.agapsys.web.toolkit.utils.DateUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SmtpModule extends AbstractSmtpModule {
	// CLASS SCOPE =============================================================
	public static final String MODULE_ID = SmtpModule.class.getName();
	
	// SETTINGS ----------------------------------------------------------------
	public static final String KEY_SENDER = "agapsys.webtoolkit.smtp.sender";
	
	public static final String KEY_SERVER        = SmtpSettings.KEY_SERVER;
	public static final String KEY_AUTH_ENABLED  = SmtpSettings.KEY_AUTH;
	public static final String KEY_USERNAME      = SmtpSettings.KEY_USERNAME;
	public static final String KEY_PASSWORD      = SmtpSettings.KEY_PASSWORD;
	public static final String KEY_SECURITY_TYPE = SmtpSettings.KEY_SECURITY;
	public static final String KEY_PORT          = SmtpSettings.KEY_PORT;
	// -------------------------------------------------------------------------
	
	public static final String SMTP_ERR_LOG_FILENAME = "smtp-errors.log";
	
	public static final String       DEFAULT_SENDER        = "no-reply@email.com";
	public static final String       DEFAULT_SERVER        = "smtp.server.com";
	public static final boolean      DEFAULT_AUTH_ENABLED  = true;
	public static final String       DEFAULT_USERNAME      = "user";
	public static final String       DEFAULT_PASSWORD      = "password";
	public static final SecurityType DEFAULT_SECURITY_TYPE = SecurityType.NONE;
	public static final int          DEFAULT_PORT          = 25;
	
	/** 
	 * Returns appropriate instance of sender address.
	 * @param senderAddrStr string representing sender address
	 * @return instance of {@linkplain InternetAddress} representing sender address
	 */
	private static InternetAddress getSenderFromString(String senderAddrStr) {
		if (senderAddrStr == null || senderAddrStr.trim().isEmpty())
			throw new RuntimeException("Null/Empty sender address");
		
		try {
			return new InternetAddress(senderAddrStr);
		} catch (AddressException ex) {
			throw new RuntimeException("Invalid address: " + senderAddrStr, ex);
		}
	}
	
	/**
	 * Returns appropriate string representation of given sender address
	 * @param senderAddress sender address
	 * @return string representation of given sender address
	 */
	private static String getSenderString(InternetAddress senderAddress) {
		return senderAddress.toString();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private SmtpSender      smtpSender = null;
	private InternetAddress sender     = null;
	
	/**
	 * Returns the name of the log file used to store errors in SMTP module
	 * @return log error filename. Default implementation returns {@linkplain SmtpModule#SMTP_ERR_LOG_FILENAME}.
	 */
	protected String getSmtpErrorLogFilename() {
		return SMTP_ERR_LOG_FILENAME;
	}
	
	/**
	 * Return the default sender address when there is no definition.
	 * @return default sender address
	 */
	protected InternetAddress getDefaultSender() {
		return getSenderFromString(DEFAULT_SENDER);
	}
	
	/**
	 * Return the default SMTP server address when there is no definition.
	 * @return default SMTP server address
	 */
	protected String getDefaultServer() {
		return DEFAULT_SERVER;
	}
	
	/**
	 * Return the default property defining if authentication is required.
	 * @return default property defining if authentication is required.
	 */
	protected boolean getDefaultAuthEnabled() {
		return DEFAULT_AUTH_ENABLED;
	}
	
	/**
	 * Return the default SMTP server username credential if there is no definition.
	 * @return default SMTP server username
	 */
	protected String getDefaultUsername() {
		return DEFAULT_USERNAME;
	}
	
	/**
	 * Return the default SMTP server password credential if there is no definition.
	 * @return default SMTP server password
	 */
	protected String getDefaultPassword() {
		return DEFAULT_PASSWORD;
	}
	
	/**
	 * Return the default SMTP server security type when there is no definition.
	 * @return default SMTP server security type when there is no definition.
	 */
	protected SecurityType getDefaultSecurityType() {
		return DEFAULT_SECURITY_TYPE;
	}
	
	/**
	 * Return the default SMTP server port when there is no definition.
	 * @return default SMTP server port when there is no definition.
	 */
	protected int getDefaultPort() {
		return DEFAULT_PORT;
	}
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = new Properties();
		
		InternetAddress defaultSender = getDefaultSender();
		if (defaultSender == null)
			throw new RuntimeException("Null default sender address");
		
		String defaultServer = getDefaultServer();
		if (defaultServer == null || defaultServer.trim().isEmpty())
			throw new RuntimeException("Null/Empty default server address/host");
		
		boolean defaultAuthEnabled = getDefaultAuthEnabled();
		
		String defaultUsername = getDefaultUsername();
		if (defaultUsername == null || defaultUsername.trim().isEmpty())
			throw new RuntimeException("Null/Empty default username");
		
		String defaultPassword = getDefaultPassword();
		if (defaultPassword == null || defaultPassword.trim().isEmpty())
			throw new RuntimeException("Null/Empty default password");
		
		SecurityType defaultSecurityType = getDefaultSecurityType();
		if (defaultSecurityType == null)
			throw new RuntimeException("Null/Empty default security type");

		int defaultPort = getDefaultPort();
		if (defaultPort < 1)
			throw new RuntimeException("Invalid port: " + defaultPort);
		
		properties.setProperty(KEY_SENDER,       getSenderString(defaultSender));
		properties.setProperty(KEY_SERVER,       defaultServer);
		properties.setProperty(KEY_AUTH_ENABLED,  "" + defaultAuthEnabled);
		properties.setProperty(KEY_USERNAME,      defaultUsername);
		properties.setProperty(KEY_PASSWORD,      defaultPassword);
		properties.setProperty(KEY_SECURITY_TYPE, defaultSecurityType.name());
		properties.setProperty(KEY_PORT,          "" + defaultPort);
		
		return properties;
	}
	
	/**
	 * Returns the sender address defined in application settings
	 * @return sender address
	 */
	public InternetAddress getSender() {
		return sender;
	}
	
	@Override
	protected void onStart(AbstractWebApplication webApp) {
		Properties properties = webApp.getProperties();
		
		SmtpSettings settings = new SmtpSettings(properties);
		smtpSender = new SmtpSender(settings);
		sender = getSenderFromString(properties.getProperty(KEY_SENDER));
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
				onError(ex, message);
			}
		}
	}
	
	/**
	 * Called when there is an error while sending a message.
	 * Default implementation writes the error in a log file (see {@linkplain SmtpModule#getSmtpErrorLogFilename()})
	 * @param ex error
	 * @param message message
	 */
	protected void onError(MessagingException ex, Message message) {
		try (PrintStream ps = new PrintStream(new FileOutputStream(new File(getApplication().getDirectory(), getSmtpErrorLogFilename()), true))) {
			ps.print(getErrorString(ex, message));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the error message which will be used by {@linkplain SmtpModule#onError(MessagingException, Message)}
	 * @param ex error
	 * @param message message
	 * @return error message string
	 */
	protected String getErrorString(MessagingException ex, Message message) {
		String errMsg =
			"[%s] [%s] Error sending message:\n" +
			"--------- Error ---------\n" +
			ExceptionReporterModule.getStackTrace(ex) + "\n" +
			"-------------------------\n\n" +
			"-------- Message --------\n" +
			message.toString() +
			"-------------------------\n";
		
		errMsg = String.format(errMsg, DateUtils.getLocalTimestamp(), this.getClass().getName());
		
		return errMsg;
	}
}
