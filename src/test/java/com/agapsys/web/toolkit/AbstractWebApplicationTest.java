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
import com.agapsys.web.toolkit.mock.MockedApplication;
import java.io.File;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

public class AbstractWebApplicationTest  {
	// CLASS SCOPE =============================================================
	// Custom modules ----------------------------------------------------------
	public static class CustomPersistenceModule extends PersistenceModule {
		public static final String MODULE_ID = "persistence";
		
		@Override
		protected void onStart(AbstractWebApplication webApp) {
			super.onStart(webApp);
			((WebApplicationBase)webApp).onPersistenceModuleStartCalled = true;
		}

		@Override
		protected void onStop() {
			super.onStop();
			((WebApplicationBase)getWebApplication()).onPersistenceModuleStopCalled = true;
		}
	}
	
	public static class CustomExceptionReporterModule extends ExceptionReporterModule {
		public static final String MODULE_ID = "exceptionReporter";

		@Override
		protected void onStart(AbstractWebApplication webApp) {
			super.onStart(webApp);
			((WebApplicationBase)webApp).onExceptionReporterModuleStartCalled = true;
		}

		@Override
		protected void onStop() {
			super.onStop();
			((WebApplicationBase)getWebApplication()).onExceptionReporterModuleStopCalled = true;
		}
	}
		
	public static class CustomSmtpModule extends SmtpModule {
		public static final String MODULE_ID = "smtp";

		@Override
		protected void onStart(AbstractWebApplication webApp) {
			super.onStart(webApp);
			((WebApplicationBase)webApp).onSmtpModuleStartCalled = true;
		}

		@Override
		protected void onStop() {
			super.onStop();
			((WebApplicationBase)getWebApplication()).onSmtpModuleStopCalled = true;
		}
	}
	// -------------------------------------------------------------------------
	
	// Custom applications -----------------------------------------------------	
	private static class WebApplicationBase extends AbstractWebApplication {
		private boolean beforeApplicationStopCalled = false;
		private boolean beforeApplicationStartCalled = false;
		
		private boolean afterApplicationStartCalled = false;
		private boolean afterApplicationStopCalled = false;

		public boolean isBeforeApplicationStartCalled() {
			return beforeApplicationStartCalled;
		}
		public boolean isAfterApplicationStartCalled() {
			return afterApplicationStartCalled;
		}
		
		public boolean isBeforeApplicationStopCalled() {
			return beforeApplicationStopCalled;
		}
		public boolean isAfterApplicationStopCalled() {
			return afterApplicationStopCalled;
		}
		
		private boolean onPersistenceModuleStartCalled = false;
		private boolean onPersistenceModuleStopCalled = false;

		public boolean isOnPersistenceModuleStartCalled() {
			return onPersistenceModuleStartCalled;
		}
		public boolean isOnPersistenceModuleStopCalled() {
			return onPersistenceModuleStopCalled;
		}
		
		private boolean onExceptionReporterModuleStartCalled = false;
		private boolean onExceptionReporterModuleStopCalled = false;

		public boolean isOnExceptionReporterModuleStartCalled() {
			return onExceptionReporterModuleStartCalled;
		}
		public boolean isOnExceptionReporterModuleStopCalled() {
			return onExceptionReporterModuleStopCalled;
		}
		
		private boolean onSmtpModuleStartCalled = false;
		private boolean onSmtpModuleStopCalled = false;

		public boolean isOnSmtpModuleStartCalled() {
			return onSmtpModuleStartCalled;
		}
		public boolean isOnSmtpModuleStopCalled() {
			return onSmtpModuleStopCalled;
		}

		@Override
		protected void beforeApplicationStart() {
			this.beforeApplicationStartCalled = true;
			super.beforeApplicationStart();
			java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF); // Disable Hibernate log output
		}
		@Override
		protected void afterApplicationStart() {
			this.afterApplicationStartCalled = true;
			super.afterApplicationStart();
		}

		@Override
		protected void beforeApplicationStop() {
			this.beforeApplicationStopCalled = true;
			super.beforeApplicationStop();
		}
		@Override
		protected void afterApplicationStop() {
			this.afterApplicationStopCalled = true;
			super.afterApplicationStop();
		}	
		
		@Override
		public String getName() {
			return TestDefs.APP_NAME;
		}

		@Override
		public String getVersion() {
			return TestDefs.APP_VERSION;
		}
	}
	
	private static class WebApplicationWithPersistence extends WebApplicationBase {

		@Override
		protected void beforeApplicationStart() {
			super.beforeApplicationStart();
			registerModule(CustomPersistenceModule.class);
		}
	}
	
	private static class WebApplicationWithErrorReport extends WebApplicationBase {

		@Override
		protected void beforeApplicationStart() {
			super.beforeApplicationStart();
			registerModule(CustomExceptionReporterModule.class);
		}
	}
	
	private static class WebApplicationWithSmtpSender extends WebApplicationBase {

		@Override
		protected void beforeApplicationStart() {
			super.beforeApplicationStart();
			registerModule(CustomSmtpModule.class);
		}
	}
	
	private static class FullFledgedApplication extends WebApplicationBase {

		@Override
		protected void beforeApplicationStart() {
			super.beforeApplicationStart();
			registerModule(CustomPersistenceModule.class);
			registerModule(CustomSmtpModule.class);
			registerModule(CustomExceptionReporterModule.class);
		}
	}
	// -------------------------------------------------------------------------
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Test
	public void Simple_web_application_start_stop_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationBase();
		webApp.contextInitialized(null);
		Assert.assertTrue(webApp.isBeforeApplicationStartCalled());
		Assert.assertTrue(webApp.isRunning());
		Assert.assertFalse(webApp.isOnPersistenceModuleStartCalled());
		Assert.assertFalse(webApp.isOnExceptionReporterModuleStartCalled());
		Assert.assertFalse(webApp.isOnSmtpModuleStartCalled());
		Assert.assertTrue(webApp.isAfterApplicationStartCalled());
		Assert.assertFalse(webApp.isBeforeApplicationStopCalled());
		Assert.assertFalse(webApp.isAfterApplicationStopCalled());
		
		webApp.contextDestroyed(null);
		Assert.assertTrue(webApp.isBeforeApplicationStopCalled());
		Assert.assertFalse(webApp.isOnPersistenceModuleStopCalled());
		Assert.assertFalse(webApp.isOnExceptionReporterModuleStopCalled());
		Assert.assertFalse(webApp.isOnSmtpModuleStopCalled());
		Assert.assertFalse(webApp.isRunning());
		Assert.assertTrue(webApp.isAfterApplicationStopCalled());
		Assert.assertNull(AbstractWebApplication.getRunningInstance());
	}
	
	@Test
	public void Simple_web_application_getName_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.contextInitialized(null);
		Assert.assertEquals(TestDefs.APP_NAME, AbstractWebApplication.getRunningInstance().getName());
		
		webApp.contextDestroyed(null);
	}
	
	@Test
	public void Simple_web_application_getVersion_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.contextInitialized(null);
		Assert.assertEquals(TestDefs.APP_VERSION, AbstractWebApplication.getRunningInstance().getVersion());
		
		webApp.contextDestroyed(null);
	}
	
	@Test
	public void Simple_web_application_getAppFolder_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.contextInitialized(null);
		File appFolder = new File(System.getProperty("user.home"), String.format(".%s", AbstractWebApplication.getRunningInstance().getName()));
		Assert.assertEquals(appFolder.getAbsolutePath(), AbstractWebApplication.getRunningInstance().getDirectory().getAbsolutePath());
		
		webApp.contextDestroyed(null);
	}
	
	@Test
	public void Web_application_with_persistence_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationWithPersistence();
		webApp.contextInitialized(null);
		Assert.assertTrue(webApp.isOnPersistenceModuleStartCalled());
		
		AbstractPersistenceModule persistenceModule = (AbstractPersistenceModule) webApp.getModule(CustomPersistenceModule.class);
		Assert.assertNotNull(persistenceModule.getEntityManager());
		
		webApp.contextDestroyed(null);
	}
	
	@Test
	public void Web_application_with_error_report_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationWithErrorReport();
		webApp.contextInitialized(null);
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStartCalled());
		
		webApp.contextDestroyed(null);
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStopCalled());
	}
	
	@Test
	public void Web_application_with_smtp_sender_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationWithSmtpSender();
		webApp.contextInitialized(null);
		Assert.assertTrue(webApp.isOnSmtpModuleStartCalled());
		
		webApp.contextDestroyed(null);
		Assert.assertTrue(webApp.isOnSmtpModuleStopCalled());
	}
	
	@Test
	public void Full_fledged_application_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new FullFledgedApplication();
		webApp.contextInitialized(null);
		
		Assert.assertTrue(webApp.isOnPersistenceModuleStartCalled());
		Assert.assertTrue(webApp.isOnSmtpModuleStartCalled());
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStartCalled());
		
		webApp.contextDestroyed(null);
		Assert.assertTrue(webApp.isOnPersistenceModuleStopCalled());
		Assert.assertTrue(webApp.isOnSmtpModuleStopCalled());
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStopCalled());
	}
	
	@Test
	public void Full_fledged_application_with_standard_modules_test() {
		Utils.printCurrentMethod();

		MockedApplication app = new MockedApplication();
		app.contextInitialized(null);
		app.contextDestroyed(null);
	}
	// =========================================================================
}
