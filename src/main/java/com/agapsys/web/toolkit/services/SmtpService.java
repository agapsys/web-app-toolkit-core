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
import java.util.Objects;
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

    private SmtpSender __getSmtpSender() {
        SmtpSettings smtpSettings = new SmtpSettings();
        smtpSettings.setServer(server);
        smtpSettings.setAuthenticationEnabled(authEnabled);
        smtpSettings.setUsername(username);
        smtpSettings.setSecurityType(securityType);
        smtpSettings.setPassword(getApplication().getProperty(KEY_PASSWORD, DEFAULT_PASSWORD));
        smtpSettings.setPort(port);

        return new SmtpSender(smtpSettings);
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
            sender = __getSenderFromString(app.getProperty(KEY_SENDER, DEFAULT_SENDER));
        }
    }


    public InternetAddress getSender() {
        synchronized(this) {
            return sender;
        }
    }
    public void setSender(InternetAddress sender) {
        synchronized(this) {
            if (sender == null)
                throw new IllegalArgumentException("Null sender");

            if (!Objects.equals(this.sender, sender)) {
                this.sender = sender;
                getApplication().setProperty(KEY_SENDER, sender.toString());
                this.smtpSender = null;
            }
        }
    }
    public void setSender(String sender) throws AddressException {
        setSender(new InternetAddress(sender));
    }

    public String getServer() {
        synchronized(this) {
            return server;
        }
    }
    public void setServer(String server) {
        synchronized(this) {
            if (server == null || server.isEmpty())
                throw new IllegalArgumentException("Null/Empty server");

            if (!Objects.equals(this.server, server)) {
                this.server = server;
                getApplication().setProperty(KEY_SERVER, server);
                this.smtpSender = null;
            }
        }
    }

    public boolean isAuthEnabled() {
        synchronized(this) {
            return authEnabled;
        }
    }
    public void setAuthEnabled(boolean authEnabled) {
        synchronized(this) {
            if (this.authEnabled != authEnabled) {
                this.authEnabled = authEnabled;
                getApplication().setProperty(KEY_AUTH_ENABLED, authEnabled);
                this.smtpSender = null;
            }
        }
    }

    public String getUsername() {
        synchronized(this) {
            return username;
        }
    }
    public void setUsername(String username) {
        synchronized(this) {
            if (username == null)
                username = "";

            if (!Objects.equals(this.username, username)) {
                this.username = username;
                getApplication().setProperty(KEY_USERNAME, username);
                this.smtpSender = null;
            }
        }
    }

    public void setPassword(String password) {
        synchronized(this) {
            getApplication().setProperty(KEY_PASSWORD, password);
            this.smtpSender = null;
        }
    }

    public SecurityType getSecurityType() {
        synchronized(this) {
            return securityType;
        }
    }
    public void setSecurityType(SecurityType securityType) {
        synchronized(this) {
            if (securityType == null)
                throw new IllegalArgumentException("Null security type");

            if (this.securityType != securityType) {
                this.securityType = securityType;
                getApplication().setProperty(KEY_SECURITY_TYPE, securityType.name());
                this.smtpSender = null;
            }
        }
    }

    public int getPort() {
        synchronized(this) {
            return port;
        }
    }
    public void setPort(int port) {
        synchronized(this) {
            if (this.port != port) {
                this.port = port;
                getApplication().setProperty(KEY_PORT, port);
                this.smtpSender = null;
            }
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

            if (smtpSender == null)
                smtpSender = __getSmtpSender();

            smtpSender.sendMessage(message);
        }
    }
}
