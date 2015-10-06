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
import java.io.File;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

public class AbstractWebApplicationTest  {
	// CLASS SCOPE =============================================================
	// Custom modules ----------------------------------------------------------
	private static class CustomPersistenceModule extends PersistenceModule {
		public CustomPersistenceModule(AbstractApplication application) {
			super(application);
		}

		@Override
		protected void onStart() {
			super.onStart();
			((WebApplicationBase)getApplication()).onPersistenceModuleStartCalled = true;
		}

		@Override
		protected void onStop() {
			super.onStop();
			((WebApplicationBase)getApplication()).onPersistenceModuleStopCalled = true;
		}
	}
	
	private static class CustomExceptionReporterModule extends ExceptionReporterModule {

		public CustomExceptionReporterModule(AbstractApplication application) {
			super(application);
		}

		@Override
		protected void onStart() {
			super.onStart();
			((WebApplicationBase)getApplication()).onExceptionReporterModuleStartCalled = true;
		}

		@Override
		protected void onStop() {
			super.onStop();
			((WebApplicationBase)getApplication()).onExceptionReporterModuleStopCalled = true;
		}
	}
		
	private static class CustomSmtpModule extends SmtpModule {

		public CustomSmtpModule(AbstractApplication application) {
			super(application);
		}

		@Override
		protected void onStart() {
			super.onStart();
			((WebApplicationBase)getApplication()).onSmtpModuleStartCalled = true;
		}

		@Override
		protected void onStop() {
			super.onStop();
			((WebApplicationBase)getApplication()).onSmtpModuleStopCalled = true;
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
		protected Class<? extends AbstractPersistenceModule> getPersistenceModuleClass() {
			return null;
		}

		@Override
		protected Class<? extends ExceptionReporterModule> getExceptionReporterModuleClass() {
			return null;
		}
		
		@Override
		protected Class<? extends AbstractSmtpModule> getSmtpModuleClass() {
			return null;
		}		
		
		@Override
		public String getName() {
			return Defs.APP_NAME;
		}

		@Override
		public String getVersion() {
			return Defs.APP_VERSION;
		}

		@Override
		public String getEnvironment() {
			return Defs.ENVIRONMENT;
		}

		@Override
		protected boolean isDebugEnabled() {
			return true;
		}
	}
	
	private static class WebApplicationWithPersistence extends WebApplicationBase {

		@Override
		protected Class<? extends AbstractPersistenceModule> getPersistenceModuleClass() {
			return CustomPersistenceModule.class;
		}
	}
	
	private static class WebApplicationWithErrorReport extends WebApplicationBase {

		@Override
		protected Class<? extends ExceptionReporterModule> getExceptionReporterModuleClass() {
			return CustomExceptionReporterModule.class;
		}
	}
	
	private static class WebApplicationWithSmtpSender extends WebApplicationBase {

		@Override
		protected Class<? extends AbstractSmtpModule> getSmtpModuleClass() {
			return CustomSmtpModule.class;
		}
	}
	
	private static class FullFledgedApplication extends WebApplicationBase {

		@Override
		protected Class<? extends AbstractPersistenceModule> getPersistenceModuleClass() {
			return CustomPersistenceModule.class;
		}

		@Override
		protected Class<? extends AbstractSmtpModule> getSmtpModuleClass() {
			return CustomSmtpModule.class;
		}

		@Override
		protected Class<? extends ExceptionReporterModule> getExceptionReporterModuleClass() {
			return CustomExceptionReporterModule.class;
		}
	}
	// -------------------------------------------------------------------------
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Test
	public void Simple_web_application_start_stop_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertTrue(webApp.isBeforeApplicationStartCalled());
		Assert.assertTrue(webApp.isRunning());
		Assert.assertFalse(webApp.isOnPersistenceModuleStartCalled());
		Assert.assertFalse(webApp.isOnExceptionReporterModuleStartCalled());
		Assert.assertFalse(webApp.isOnSmtpModuleStartCalled());
		Assert.assertTrue(webApp.isAfterApplicationStartCalled());
		Assert.assertFalse(webApp.isBeforeApplicationStopCalled());
		Assert.assertFalse(webApp.isAfterApplicationStopCalled());
		
		webApp.stop();
		Assert.assertTrue(webApp.isBeforeApplicationStopCalled());
		Assert.assertFalse(webApp.isOnPersistenceModuleStopCalled());
		Assert.assertFalse(webApp.isOnExceptionReporterModuleStopCalled());
		Assert.assertFalse(webApp.isOnSmtpModuleStopCalled());
		Assert.assertFalse(webApp.isRunning());
		Assert.assertTrue(webApp.isAfterApplicationStopCalled());
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getName_test() {
		Utils.printCurrentMethod();
		
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertEquals(Defs.APP_NAME, AbstractWebApplication.getInstance().getName());
		webApp.stop();
		AbstractWebApplication.getInstance().getName();
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getVersion_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertEquals(Defs.APP_VERSION, AbstractWebApplication.getInstance().getVersion());
		
		webApp.stop();
		AbstractWebApplication.getInstance().getVersion();
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getEnvironment_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertEquals(Defs.ENVIRONMENT, AbstractWebApplication.getInstance().getEnvironment());
		
		webApp.stop();
		AbstractWebApplication.getInstance().getEnvironment();
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getAppFolder_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.start();
		
		File appFolder = new File(System.getProperty("user.home"), String.format(".%s", AbstractWebApplication.getInstance().getName()));
		Assert.assertEquals(appFolder.getAbsolutePath(), AbstractWebApplication.getInstance().getDirectory().getAbsolutePath());
		
		webApp.stop();
		AbstractWebApplication.getInstance().getDirectory();
	}
	
	@Test
	public void Simple_web_application_getEntityManager_test() {
		Utils.printCurrentMethod();
		
		AbstractWebApplication webApp = new WebApplicationBase();
		webApp.start();
		
		AbstractPersistenceModule persistenceModule = webApp.getPersistenceModule();
		Assert.assertNull(persistenceModule);
		webApp.stop();
	}
	
	
	@Test
	public void Web_application_with_persistence_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationWithPersistence();
		webApp.start();
		Assert.assertTrue(webApp.isOnPersistenceModuleStartCalled());
		
		
		AbstractPersistenceModule persistenceModule = webApp.getPersistenceModule();
		Assert.assertNotNull(persistenceModule.getEntityManager());
		
		webApp.stop();
		Assert.assertTrue(webApp.isOnPersistenceModuleStopCalled());
	}
	
	@Test
	public void Web_application_with_error_report_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationWithErrorReport();
		webApp.start();
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStartCalled());
		
		webApp.stop();
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStopCalled());
	}
	
	@Test
	public void Web_application_with_smtp_sender_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new WebApplicationWithSmtpSender();
		webApp.start();
		Assert.assertTrue(webApp.isOnSmtpModuleStartCalled());
		
		webApp.stop();
		Assert.assertTrue(webApp.isOnSmtpModuleStopCalled());
	}
	
	@Test
	public void Full_fledged_application_test() {
		Utils.printCurrentMethod();
		
		WebApplicationBase webApp = new FullFledgedApplication();
		webApp.start();
		
		Assert.assertTrue(webApp.isOnPersistenceModuleStartCalled());
		Assert.assertTrue(webApp.isOnSmtpModuleStartCalled());
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStartCalled());
		
		webApp.stop();
		Assert.assertTrue(webApp.isOnPersistenceModuleStopCalled());
		Assert.assertTrue(webApp.isOnSmtpModuleStopCalled());
		Assert.assertTrue(webApp.isOnExceptionReporterModuleStopCalled());
	}
	
	@Test
	public void Full_fledged_application_with_standard_modules_test() {
		Utils.printCurrentMethod();

		TestApplication app = new TestApplication();
		app.start();
		app.stop();
	}
	// =========================================================================
}
