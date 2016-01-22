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

package com.agapsys.web.toolkit.utils;

import org.junit.Assert;
import org.junit.Test;

public class SingletonManagerTest {
	// CLASS SCOPE =============================================================
	public static class TestSingleton {}
	
	public static class CustomTestSingleton extends TestSingleton {}
	
	private static class OntherSingleton {}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Test
	public void testSingleton() {
		SingletonManager sm = new SingletonManager();
		
		Object instance1 = sm.getSingleton(TestSingleton.class);
		Object instance2 = sm.getSingleton(TestSingleton.class);
		
		Assert.assertTrue(instance1 == instance2);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSingletonReplace() {
		SingletonManager sm = new SingletonManager();
		
		sm.replaceSingleton(TestSingleton.class, OntherSingleton.class);
	}
	
	@Test
	public void testSingletonReplace() {
		SingletonManager sm = new SingletonManager();
		Object obj1 = sm.getSingleton(TestSingleton.class);
		Assert.assertTrue(obj1.getClass() == TestSingleton.class);
		sm.clear();
		
		sm.replaceSingleton(TestSingleton.class, CustomTestSingleton.class);
		Object obj2 = sm.getSingleton(TestSingleton.class);
		Assert.assertTrue(obj2.getClass() == CustomTestSingleton.class);
		
		Object obj3 = sm.getSingleton(CustomTestSingleton.class);
		Assert.assertTrue(obj3 == obj2);
		
		sm.clearReplacement();
		Object obj4 = sm.getSingleton(TestSingleton.class);
		Assert.assertTrue(obj4.getClass() == TestSingleton.class);
		Assert.assertFalse(obj4 == obj1); // Because of clear
	}
	
	@Test
	public void testSingletonReplace1() {
		SingletonManager sm = new SingletonManager();
		Object obj1 = sm.getSingleton(TestSingleton.class);
		Assert.assertTrue(obj1.getClass() == TestSingleton.class);
		
		sm.replaceSingleton(TestSingleton.class, CustomTestSingleton.class); // Previous instance associated to TestSingleton.class (before replacement) will be lost
		Object obj2 = sm.getSingleton(TestSingleton.class);
		Assert.assertTrue(obj2.getClass() == CustomTestSingleton.class);
		
		Object obj3 = sm.getSingleton(CustomTestSingleton.class);
		Assert.assertTrue(obj3 == obj2);
		
		sm.clearReplacement();
		Object obj4 = sm.getSingleton(TestSingleton.class);
		Assert.assertTrue(obj4.getClass() == TestSingleton.class);
		Assert.assertTrue(obj4 != obj1);
	}
	// =========================================================================
}
