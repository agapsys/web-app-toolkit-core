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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ModuleTest {
	// CLASS SCOPE =============================================================
	private static class TestModule extends AbstractModule {
		private boolean isStartCalled = false;
		private boolean isStopCalled = false;

		public TestModule(AbstractApplication application) {
			super(application);
		}

		@Override
		protected void onStart() {
			isStartCalled = true;
		}

		@Override
		protected void onStop() {
			isStopCalled = true;
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private TestModule module = null;
	
	@Before
	public void before() {
		module = new TestModule(new TestApplication());
	}
	
	@Test
	public void testDefaults() {
		Utils.printCurrentMethod();
		
		Assert.assertNull(module.getDefaultSettings());
		Assert.assertFalse(module.isStartCalled);
		Assert.assertFalse(module.isStopCalled);
	}
	
	@Test
	public void testRunning() {
		Utils.printCurrentMethod();
		
		Assert.assertFalse(module.isRunning());
		
		module.start();
		Assert.assertTrue(module.isStartCalled);
		Assert.assertFalse(module.isStopCalled);
		Assert.assertTrue(module.isStartCalled);
		
		module.stop();
		Assert.assertTrue(module.isStopCalled);
		Assert.assertFalse(module.isRunning());
	}
	// =========================================================================
}
