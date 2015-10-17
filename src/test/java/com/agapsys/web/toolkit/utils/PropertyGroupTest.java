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

import java.util.Properties;
import org.junit.Assert;
import org.junit.Test;

public class PropertyGroupTest {
	@Test
	public void testSubProperties() {
		Properties props = new Properties();
		// Default properties
		props.setProperty("prop1", "prop1Val");
		props.setProperty("prop2", "prop2Val");
		props.setProperty("prop3", "prop3Val");
		
		// dev propeties
		props.setProperty("[dev]prop1", "devProp1Val");
		props.setProperty("[dev]prop3", "devProp3Val");
		
		// test propeties
		props.setProperty("[test]prop1", "testProp1Val");
		props.setProperty("[test]prop3", "testProp3Val");
		
		Properties devProperties = PropertyGroup.getSubProperties(props, "dev", "[]");
		Properties testProperties = PropertyGroup.getSubProperties(props, "test", "[]");
		Properties defaultProperties = PropertyGroup.getSubProperties(props, null, "[]");
		
		Assert.assertEquals(3, defaultProperties.size());
		Assert.assertEquals(3, devProperties.size());
		Assert.assertEquals(3, testProperties.size());
		
		Assert.assertEquals("prop1Val", defaultProperties.getProperty("prop1"));
		Assert.assertEquals("prop2Val", defaultProperties.getProperty("prop2"));
		Assert.assertEquals("prop3Val", defaultProperties.getProperty("prop3"));
		
		Assert.assertEquals("devProp1Val", devProperties.getProperty("prop1"));
		Assert.assertEquals("prop2Val",    devProperties.getProperty("prop2"));
		Assert.assertEquals("devProp3Val", devProperties.getProperty("prop3"));
		
		Assert.assertEquals("testProp1Val", testProperties.getProperty("prop1"));
		Assert.assertEquals("prop2Val",     testProperties.getProperty("prop2"));
		Assert.assertEquals("testProp3Val", testProperties.getProperty("prop3"));
	}
}
