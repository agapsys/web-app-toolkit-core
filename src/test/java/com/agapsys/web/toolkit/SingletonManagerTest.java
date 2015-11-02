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

import com.agapsys.web.toolkit.SingletonManager.Singleton;
import org.junit.Assert;
import org.junit.Test;

public class SingletonManagerTest {
	// CLASS SCOPE =============================================================
	public static class TestSingleton implements Singleton {}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Test
	public void testSingleton() {
		SingletonManager sm = new SingletonManager();
		
		Singleton instance1 = sm.getSingleton(TestSingleton.class);
		Singleton instance2 = sm.getSingleton(TestSingleton.class);
		
		Assert.assertTrue(instance1 == instance2);
	}
	
	@Test
	public void testSingletonWithId() {
		SingletonManager sm = new SingletonManager();
		
		sm.registerSingleton("id1", TestSingleton.class);
		sm.registerSingleton("id2", TestSingleton.class);
		
		Singleton instance1 = sm.getSingleton(TestSingleton.class);
		Singleton instance2 = sm.getSingleton("id1");
		Singleton instance3 = sm.getSingleton("id2");
		
		Assert.assertTrue(instance1 == instance2);
		Assert.assertTrue(instance1 == instance3);
	}
	// =========================================================================
}
