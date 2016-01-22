/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AttributeServiceTest {
	// CLASS SCOPE =============================================================
	private static class ErrorWrapper {
		private Throwable error = null;
		
		public synchronized Throwable getError() {
			return error;
		}
		
		public synchronized void setError(Throwable error) {
			this.error = error;
		}
	}
	// =========================================================================
	
	private final AttributeService attributeService = new AttributeService();
	
	
	@After
	public void after() {
		attributeService.destroyAttributes();
	}
	
	@Test
	public void test() throws InterruptedException {
		final ErrorWrapper errorWrapper = new ErrorWrapper();
		
		attributeService.setAttribute("val", "mainThread");
		Assert.assertEquals("mainThread", attributeService.getAttribute("val"));
		
		Thread anotherThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Assert.assertNull(attributeService.getAttribute("val"));
					attributeService.setAttribute("val", "anotherThread");
					Assert.assertEquals("anotherThread", attributeService.getAttribute("val"));
				} catch (Throwable t) {
					errorWrapper.setError(t);
				}
			}
		});
		
		anotherThread.start();
		anotherThread.join();
		Assert.assertEquals("mainThread", attributeService.getAttribute("val"));
		Assert.assertNull(errorWrapper.getError());
	}
}
