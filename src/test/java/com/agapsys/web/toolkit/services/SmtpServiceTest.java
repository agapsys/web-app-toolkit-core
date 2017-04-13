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
import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.MockedWebApplication;
import com.sun.mail.util.MailConnectException;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SmtpServiceTest {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    private static class TestSmtpService extends SmtpService {
        private boolean methodCalled = false;

        @Override
        public void sendMessage(Message message) throws MessagingException {
            methodCalled = true;
            super.sendMessage(message);
        }
        
    }
    // =========================================================================
    // </editor-fold>

    private TestSmtpService service;
    private AbstractApplication app;
    private final Message testMessage;
    

    public SmtpServiceTest() throws AddressException {
        this.testMessage = new MessageBuilder("sender@localhost", "recipient@localhost").build();
    }

    @Before
    public void before() {
        service = new TestSmtpService();
    }
    
    @After
    public void after() {
        if (app != null && app.isRunning())
            app.stop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendNullMessage() throws MessagingException {
        service.sendMessage(null);
    }

    @Test(expected = IllegalStateException.class)
    public void sendMessageWhileNotRunning() throws MessagingException {
        Assert.assertFalse(service.isRunning());
        service.sendMessage(testMessage);
    }

    @Test
    public void sendMessageWhileRunning() throws MessagingException {
        app = new MockedWebApplication() {
            @Override
            protected void beforeStart() {
                super.beforeStart();

                registerService(service);
            }
        };
        app.start();
        try {
            app.getServiceOnDemand(SmtpService.class).sendMessage(testMessage);
        } catch (MailConnectException ignored) {}
        Assert.assertTrue(service.methodCalled);
        app.stop();
        Assert.assertNull(AbstractApplication.getRunningInstance());
    }
}
