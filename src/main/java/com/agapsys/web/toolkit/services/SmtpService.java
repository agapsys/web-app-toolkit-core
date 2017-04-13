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

package com.agapsys.web.toolkit.services;

import com.agapsys.mail.Message;
import com.agapsys.mail.MessageBuilder;
import com.agapsys.mail.SecurityType;
import com.agapsys.mail.SmtpSender;
import com.agapsys.mail.SmtpSettings;
import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.Service;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public class SmtpService extends Service {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    // SETTINGS ----------------------------------------------------------------
    public static final String PROPERTY_PREFIX = SmtpService.class.getName();

    public static final String KEY_SENDER        = PROPERTY_PREFIX + ".sender";
    public static final String KEY_SERVER        = PROPERTY_PREFIX + ".server";
    public static final String KEY_AUTH_ENABLED  = PROPERTY_PREFIX + ".authEnabled";
    public static final String KEY_USERNAME      = PROPERTY_PREFIX + ".username";
    public static final String KEY_PASSWORD      = PROPERTY_PREFIX + ".password";
    public static final String KEY_SECURITY_TYPE = PROPERTY_PREFIX + ".security";
    public static final String KEY_PORT          = PROPERTY_PREFIX + ".port";
    // -------------------------------------------------------------------------

    public static final String       DEFAULT_SENDER        = "no-reply@localhost";
    public static final String       DEFAULT_SERVER        = "smtp.server.com";
    public static final boolean      DEFAULT_AUTH_ENABLED  = true;
    public static final String       DEFAULT_USERNAME      = "user";
    public static final String       DEFAULT_PASSWORD      = "password";
    public static final SecurityType DEFAULT_SECURITY_TYPE = SecurityType.NONE;
    public static final int          DEFAULT_PORT          = 25;

    /** Convenience method to create InternetAddress instance which throws RuntimeException instead of checked one. */
    private static InternetAddress __getSenderFromString(String senderAddrStr) {
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

    private String          server;
    private boolean         authEnabled;
    private String          username;
    private SecurityType    securityType;
    private int             port;
    private SmtpSender      smtpSender = null;
    private InternetAddress sender     = null;

    public SmtpService() {
        __reset();
    }

    private void __reset() {
        server = null;
        authEnabled = false;
        username = null;
        securityType = null;
        port = -1;
        smtpSender = null;
        sender = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        synchronized(this) {
            __reset();

            AbstractApplication app = getApplication();

            server       = app.getProperty(KEY_SERVER,    DEFAULT_SERVER);
            authEnabled  = app.getProperty(Boolean.class, KEY_AUTH_ENABLED, DEFAULT_AUTH_ENABLED);
            username     = app.getProperty(KEY_USERNAME,  DEFAULT_USERNAME);
            port         = app.getProperty(Integer.class, KEY_PORT, DEFAULT_PORT);

            securityType = SecurityType.valueOf(app.getProperty(KEY_SECURITY_TYPE, DEFAULT_SECURITY_TYPE.name()));

            SmtpSettings smtpSettings = new SmtpSettings();
            smtpSettings.setServer(server);
            smtpSettings.setAuthenticationEnabled(authEnabled);
            smtpSettings.setUsername(username);
            smtpSettings.setSecurityType(securityType);
            smtpSettings.setPassword(app.getProperty(KEY_PASSWORD, DEFAULT_PASSWORD));
            smtpSettings.setPort(port);

            smtpSender = new SmtpSender(smtpSettings);
            sender = __getSenderFromString(app.getProperty(KEY_SENDER, DEFAULT_SENDER));
        }
    }

    /**
     * Returns the sender address defined in application settings.
     *
     * @return sender address.
     */
    public InternetAddress getSender() {
        synchronized(this) {
            return sender;
        }
    }

    public String getServer() {
        synchronized(this) {
            return server;
        }
    }

    public boolean isAuthEnabled() {
        synchronized(this) {
            return authEnabled;
        }
    }

    public String getUsername() {
        synchronized(this) {
            return username;
        }
    }

    public SecurityType getSecurityType() {
        synchronized(this) {
            return securityType;
        }
    }

    public int getPort() {
        synchronized(this) {
            return port;
        }
    }

    /**
     * Sends an email message.
     *
     * @param message message to be sent.
     * @throws MessagingException if an error happened during the process
     */
    public void sendMessage(Message message) throws MessagingException {
        synchronized(this) {
            if (message == null)
                throw new IllegalArgumentException("null message");

            if (!isRunning())
                throw new IllegalStateException("Service is not running");

            // Forces sender address if message's address not equals to application default sender.
            if (!message.getSenderAddress().equals(getSender())) {
                message = new MessageBuilder(getSender(), message.getRecipients().toArray(new InternetAddress[message.getRecipients().size()]))
                    .setCharset(message.getCharset())
                    .setMimeSubtype(message.getMimeSubtype())
                    .setSubject(message.getSubject())
                    .setText(message.getText()).build();
            }

            smtpSender.sendMessage(message);
        }
    }
}
