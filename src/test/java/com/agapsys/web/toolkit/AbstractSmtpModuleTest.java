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

import com.agapsys.Utils;
import com.agapsys.mail.Message;
import com.agapsys.mail.MessageBuilder;
import com.agapsys.web.toolkit.mock.MockedApplication;
import javax.mail.internet.AddressException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AbstractSmtpModuleTest {
	// CLASS SCOPE =============================================================
	private static class TestSmtpModule extends AbstractSmtpModule {
		private boolean methodCalled = false;
		
		@Override
		protected void onSendMessage(Message message) {
			methodCalled = true;
		}

		@Override
		protected void onStart(AbstractWebApplication webApp) {}

		@Override
		protected void onStop() {}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private TestSmtpModule module;
	private final Message testMessage;
	private final AbstractWebApplication app = new MockedApplication();


	public AbstractSmtpModuleTest() throws AddressException {
		this.testMessage = new MessageBuilder("sender@host.com", "recipient@host.com").build();
	}	
	
	@Before
	public void before() {
		module = new TestSmtpModule();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void sendNullMessage() {
		Utils.printCurrentMethod();
		
		module.sendMessage(null);
	}
	
	@Test(expected = IllegalStateException.class)
	public void sendMessageWhileNotRunning() {
		Utils.printCurrentMethod();
		
		Assert.assertFalse(module.isRunning());
		module.sendMessage(testMessage);
	}
	
	@Test
	public void sendMessageWhileRunning() {
		Utils.printCurrentMethod();
		
		module.start(app);
		module.sendMessage(testMessage);
		Assert.assertTrue(module.methodCalled);
	}
	// =========================================================================
}
