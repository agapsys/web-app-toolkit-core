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
import org.junit.Test;

public class DefaultExceptionReporterModuleTest {
	// CLASS SCOPE =============================================================
	private static final int STACK_TRACE_HISTORY_SIZE = 2;
	
	private static class TestModule extends ExceptionReporterModule {
		
		public TestModule(AbstractApplication application) {
			super(application);
		}

		@Override
		public int getStacktraceHistorySize() {
			return STACK_TRACE_HISTORY_SIZE;
		}
		
		@Override
		public boolean skipErrorReport(Throwable t) {
			return super.skipErrorReport(t);
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Test
	public void skipReportTest() {
		Utils.printCurrentMethod();
		
		TestModule module = new TestModule(new TestApplication());
		
		RuntimeException re1 = new RuntimeException();
		RuntimeException re2 = new RuntimeException();
		RuntimeException re3 = new RuntimeException();

		Assert.assertFalse(module.skipErrorReport(re1)); // history: re1
		Assert.assertFalse(module.skipErrorReport(re2)); // history: re1, re2
		
		Assert.assertTrue(module.skipErrorReport(re1));
		Assert.assertTrue(module.skipErrorReport(re2));
		
		Assert.assertFalse(module.skipErrorReport(re3)); // history: re2, re3
		
		Assert.assertTrue(module.skipErrorReport(re2));
		Assert.assertTrue(module.skipErrorReport(re3));
		
		Assert.assertFalse(module.skipErrorReport(re1)); // history: re3, re1
		Assert.assertFalse(module.skipErrorReport(re2)); // history: re1, re2
		
		Assert.assertTrue(module.skipErrorReport(re1));
		Assert.assertTrue(module.skipErrorReport(re2));
	}
	// =========================================================================
}
