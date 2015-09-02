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

package com.agapsys.web.modules;

import com.agapsys.mail.Message;
import com.agapsys.mail.MessageBuilder;
import javax.mail.internet.AddressException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SmtpModuleTest {
	// CLASS SCOPE =============================================================
	private static class TestSmtpModule extends SmtpModule {
		private boolean methodCalled = false;
		
		@Override
		protected void processMessage(Message message) {
			methodCalled = true;
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private TestSmtpModule module;
	private final Message testMessage;

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
	
	@Test
	public void sendMessageWhileNotRunning() {
		Assert.assertFalse(module.isRunning());
		module.sendMessage(testMessage);
		Assert.assertFalse(module.methodCalled);
	}
	
	@Test
	public void sendMessageWhileRunning() {
		module.start();
		module.sendMessage(testMessage);
		Assert.assertTrue(module.methodCalled);
	}
	// =========================================================================
}
