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

import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistenceModuleTest {
	// CLASS SCOPE =============================================================
	private static class TestPersistenceModule extends PersistenceModule {
		private boolean methodCalled = false;

		public TestPersistenceModule(WebApplication application) {
			super(application);
		}
		
		@Override
		protected EntityManager getAppEntityManager() {
			methodCalled = true;
			return null;
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private TestPersistenceModule module;
	
	@Before
	public void before() {
		module = new TestPersistenceModule(new TestApplication());
	}
	
	@Test
	public void sanityCheck() {
		Assert.assertFalse(module.methodCalled);
		Assert.assertFalse(module.isRunning());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testGetEntityManagerWhileNotRunning() {
		module.getEntityManager();
	}
	
	@Test
	public void testGetEntityManagerWhileRunning() {
		module.start();
		Assert.assertNull(module.getEntityManager());
		Assert.assertTrue(module.methodCalled);
	}
	// =========================================================================
}
