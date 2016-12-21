/*
 * Copyright 2015-2016 Agapsys Tecnologia Ltda-ME.
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
import com.agapsys.mail.SecurityType;
import com.agapsys.mail.SmtpSender;
import com.agapsys.mail.SmtpSettings;
import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.WebModule;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SmtpModule extends WebModule {
    
    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    // SETTINGS ----------------------------------------------------------------
    public static final String SETTINGS_GROUP_NAME = SmtpModule.class.getName();

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
     *
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
    // =========================================================================
    // </editor-fold>

    private SmtpSender      smtpSender = null;
    private InternetAddress sender     = null;

    public SmtpModule() {
        reset();
    }

    private void reset() {
        smtpSender = null;
        sender = null;
    }

    @Override
    protected final String getSettingsGroupName() {
        return SETTINGS_GROUP_NAME;
    }

    /**
     * Returns the name of the log file used to store errors in SMTP module.
     *
     * @return log error filename. Default implementation returns {@linkplain SmtpModule#SMTP_ERR_LOG_FILENAME}.
     */
    protected String getSmtpErrorLogFilename() {
        return SMTP_ERR_LOG_FILENAME;
    }

    @Override
    public Properties getDefaultProperties() {
        Properties properties = super.getDefaultProperties();

        properties.setProperty(KEY_SENDER,       DEFAULT_SENDER);
        properties.setProperty(KEY_SERVER,       DEFAULT_SERVER);
        properties.setProperty(KEY_AUTH_ENABLED,  "" + DEFAULT_AUTH_ENABLED);
        properties.setProperty(KEY_USERNAME,      DEFAULT_USERNAME);
        properties.setProperty(KEY_PASSWORD,      DEFAULT_PASSWORD);
        properties.setProperty(KEY_SECURITY_TYPE, DEFAULT_SECURITY_TYPE.name());
        properties.setProperty(KEY_PORT,          "" + DEFAULT_PORT);

        return properties;
    }


    @Override
    protected void onInit(AbstractApplication webApp) {
        super.onInit(webApp);

        reset();

        Properties moduleProperties = getProperties();

        SmtpSettings settings = new SmtpSettings(moduleProperties);

        smtpSender = new SmtpSender(settings);
        sender = getSenderFromString(moduleProperties.getProperty(KEY_SENDER));
    }

    /**
     * Returns the sender address defined in application settings.
     *
     * @return sender address.
     */
    public InternetAddress getSender() {
        return sender;
    }

    /**
     * Called during message sending.
     *
     * @param message Message to be sent.
     */
    protected void onSendMessage(Message message) {
        try {
            // Forces sender address if message's address not equals to application default sender.
            if (!message.getSenderAddress().equals(getSender())) {
                message = new MessageBuilder(getSender(), message.getRecipients().toArray(new InternetAddress[message.getRecipients().size()]))
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

    /**
     * Called when there is an error while sending a message.
     *
     * Default implementation writes the error in a log file (see {@linkplain SmtpModule#getSmtpErrorLogFilename()}).
     * @param ex error.
     * @param message message.
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

        errMsg = String.format(errMsg, DateUtils.getIso8601Date(), this.getClass().getName());

        return errMsg;
    }

    /**
     * Sends an email message.
     *
     * @param message message to be sent.
     */
    public final void sendMessage(Message message) {
        synchronized(this) {
            if (message == null)
                throw new IllegalArgumentException("null message");

            if (!isActive())
                throw new IllegalStateException("Module is not active");

            onSendMessage(message);
        }
    }
}
