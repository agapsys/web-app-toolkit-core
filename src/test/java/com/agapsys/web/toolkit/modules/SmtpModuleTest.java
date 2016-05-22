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
import com.agapsys.web.toolkit.MockedWebApplication;
import javax.mail.internet.AddressException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SmtpModuleTest {
	// CLASS SCOPE =============================================================
	private static class TestSmtpModule extends SmtpModule {
		private boolean methodCalled = false;

		@Override
		protected void onSendMessage(Message message) {
			methodCalled = true;
		}

		@Override
		protected void onInit(AbstractWebApplication webApp) {} // <-- does not load implementation logic

		@Override
		protected void onStop() {}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private TestSmtpModule module;
	private final Message testMessage;
	private final AbstractWebApplication app = new MockedWebApplication();

	public SmtpModuleTest() throws AddressException {
		this.testMessage = new MessageBuilder("sender@host.com", "recipient@host.com").build();
	}

	@Before
	public void before() {
		module = new TestSmtpModule();
	}

	@Test(expected = IllegalArgumentException.class)
	public void sendNullMessage() {
		module.sendMessage(null);
	}

	@Test(expected = IllegalStateException.class)
	public void sendMessageWhileNotRunning() {
		Assert.assertFalse(module.isActive());
		module.sendMessage(testMessage);
	}

	@Test
	public void sendMessageWhileRunning() {
		module.init(app);
		module.sendMessage(testMessage);
		Assert.assertTrue(module.methodCalled);
	}
	// =========================================================================
}
