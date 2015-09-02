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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LoggingModuleTest {
	// CLASS SCOPE =============================================================
	private static class TestLoggingModule extends LoggingModule {
		private boolean methodCalled = false;

		@Override
		protected void processLog(String logType, String message) {
			methodCalled = true;
		}
	} 
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private TestLoggingModule module;
	
	@Before
	public void before() {
		module = new TestLoggingModule();
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullLogType() {
		module.log(null, "msg");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void nullMessage() {
		module.log("info", null);
	}
	
	@Test
	public void logWhileNotRunning() {
		Assert.assertFalse(module.isRunning());
		module.log("info", "test");
		Assert.assertFalse(module.methodCalled);
	}
	
	@Test
	public void logWhileRunning() {
		module.start();
		module.log("info", "test");
		Assert.assertTrue(module.methodCalled);
	}
	// =========================================================================
}
