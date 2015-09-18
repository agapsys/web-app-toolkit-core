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

import com.agapsys.web.toolkit.WebApplication;
import java.io.File;
import org.junit.Assert;
import org.junit.Test;

public class WebApplicationTest  {
	// CLASS SCOPE =============================================================
	private static class WebApplicationBase extends WebApplication {
		private boolean beforeApplicationShutdownCalled = false;
		private boolean onApplicationStartCalled = false;

		public boolean isBeforeApplicationShutdownCalled() {
			return beforeApplicationShutdownCalled;
		}
		public boolean isOnApplicationStartCalled() {
			return onApplicationStartCalled;
		}
		
		private boolean onPersistenceModuleStartCalled = false;
		private boolean beforePersistenceModuleStopCalled = false;

		public boolean isOnPersistenceModuleStartCalled() {
			return onPersistenceModuleStartCalled;
		}
		public boolean isBeforePersistenceModuleStopCalled() {
			return beforePersistenceModuleStopCalled;
		}
		
		private boolean onLogginModuleStartCalled = false;
		private boolean beforeLoggingModuleStopCalled = false;

		public boolean isOnLogginModuleStartCalled() {
			return onLogginModuleStartCalled;
		}
		public boolean isBeforeLoggingModuleStopCalled() {
			return beforeLoggingModuleStopCalled;
		}
		
		private boolean onErrorReporterModuleStartCalled = false;
		private boolean beforeErrorReporterModuleStopCalled = false;

		public boolean isOnErrorReporterModuleStartCalled() {
			return onErrorReporterModuleStartCalled;
		}
		public boolean isBeforeErrorReporterModuleStopCalled() {
			return beforeErrorReporterModuleStopCalled;
		}
		
		private boolean onSmtpModuleStartCalled = false;
		private boolean beforeSmtpModuleStopCalled = false;

		public boolean isOnSmtpModuleStartCalled() {
			return onSmtpModuleStartCalled;
		}
		public boolean isBeforeSmtpModuleStopCalled() {
			return beforeSmtpModuleStopCalled;
		}
		
		
		@Override
		protected void onApplicationStart() {
			this.onApplicationStartCalled = true;
		}
		@Override
		protected void beforeApplicationShutdown() {
			this.beforeApplicationShutdownCalled = true;
		}
		
		@Override
		protected void onPersistenceModuleStart() {
			this.onPersistenceModuleStartCalled = true;
		}
		@Override
		protected void beforePersistenceModuleStop() {
			this.beforePersistenceModuleStopCalled = true;
		}	
		
		@Override
		protected void onLogginModuleStart() {
			this.onLogginModuleStartCalled = true;
		}
		@Override
		protected void beforeLoggingModuleStop() {
			this.beforeLoggingModuleStopCalled = true;
		}

		@Override
		protected void onErrorReporterModuleStart() {
			this.onErrorReporterModuleStartCalled = true;
		}
		@Override
		protected void beforeErrorReporterModuleStop() {
			this.beforeErrorReporterModuleStopCalled = true;
		}

		@Override
		protected void onSmtpModuleStart() {
			this.onSmtpModuleStartCalled = true;
		}
		@Override
		protected void beforeSmtpModuleStop() {
			this.beforeSmtpModuleStopCalled = true;
		}
		
		
		@Override
		protected String getAppName() {
			return Defs.APP_NAME;
		}

		@Override
		protected String getAppVersion() {
			return Defs.APP_VERSION;
		}

		@Override
		protected String getDefaultEnvironment() {
			return Defs.ENVIRONMENT;
		}

		@Override
		protected boolean isDebugEnabled() {
			return true;
		}
	}
	
	private static class WebApplicationWithPersistence extends WebApplicationBase {
		@Override
		protected PersistenceModule getPersistenceModule() {
			return new DefaultPersistenceModule();
		}
	}
	
	private static class WebApplicationWithLogging extends WebApplicationBase {

		@Override
		protected LoggingModule getLoggingModule() {
			return new DefaultLoggingModule();
		}
	}
	
	private static class WebApplicationWithErrorReport extends WebApplicationBase {

		@Override
		protected ErrorReporterModule getErrorReporterModule() {
			return new DefaultErrorReporterModule();
		}
	}
	
	private static class WebApplicationWithSmtpSender extends WebApplicationBase {

		@Override
		protected SmtpModule getSmtpModule() {
			return new DefaultSmtpModule();
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Test
	public void Simple_web_application_start_stop_test() {
		WebApplicationBase webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertTrue(webApp.isOnApplicationStartCalled());
		Assert.assertTrue(WebApplicationBase.isRunning());
		Assert.assertFalse(webApp.isOnPersistenceModuleStartCalled());
		Assert.assertFalse(webApp.isOnLogginModuleStartCalled());
		Assert.assertFalse(webApp.isOnErrorReporterModuleStartCalled());
		Assert.assertFalse(webApp.isOnSmtpModuleStartCalled());
		webApp.stop();
		Assert.assertTrue(webApp.isBeforeApplicationShutdownCalled());
		Assert.assertFalse(webApp.isBeforePersistenceModuleStopCalled());
		Assert.assertFalse(webApp.isBeforeLoggingModuleStopCalled());
		Assert.assertFalse(webApp.isBeforeErrorReporterModuleStopCalled());
		Assert.assertFalse(webApp.isBeforeSmtpModuleStopCalled());
		Assert.assertFalse(WebApplicationBase.isRunning());
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getName_test() {
		WebApplication webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertEquals(Defs.APP_NAME, WebApplication.getName());
		
		webApp.stop();
		WebApplication.getName();
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getVersion_test() {
		WebApplication webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertEquals(Defs.APP_VERSION, WebApplication.getVersion());
		
		webApp.stop();
		WebApplication.getVersion();
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getEnvironment_test() {
		WebApplication webApp = new WebApplicationBase();
		webApp.start();
		Assert.assertEquals(Defs.ENVIRONMENT, WebApplication.getEnvironment());
		
		webApp.stop();
		WebApplication.getEnvironment();
	}
	
	@Test(expected = IllegalStateException.class)
	public void Simple_web_application_getAppFolder_test() {
		WebApplication webApp = new WebApplicationBase();
		webApp.start();
		
		File appFolder = new File(System.getProperty("user.home"), String.format(".%s", WebApplication.getName()));
		Assert.assertEquals(appFolder.getAbsolutePath(), WebApplication.getAppFolder().getAbsolutePath());
		
		webApp.stop();
		WebApplication.getAppFolder();
	}
	
	@Test (expected = IllegalStateException.class)
	public void Simple_web_application_getEntityManager_test() {
		WebApplication webApp = new WebApplicationBase();
		webApp.start();
		
		Assert.assertNull(WebApplication.getEntityManager());
		
		webApp.stop();
		WebApplication.getEntityManager();
	}
	
	
	@Test
	public void Web_application_with_persistence_test() {
		WebApplicationBase webApp = new WebApplicationWithPersistence();
		webApp.start();
		Assert.assertTrue(webApp.isOnPersistenceModuleStartCalled());
		
		Assert.assertNotNull(WebApplication.getEntityManager());
		
		webApp.stop();
		Assert.assertTrue(webApp.isBeforePersistenceModuleStopCalled());
	}
	
	@Test
	public void Web_application_with_logging_test() {
		WebApplicationBase webApp = new WebApplicationWithLogging();
		webApp.start();
		Assert.assertTrue(webApp.isOnLogginModuleStartCalled());
				
		webApp.stop();
		Assert.assertTrue(webApp.isBeforeLoggingModuleStopCalled());
	}
	
	@Test
	public void Web_application_with_error_report_test() {
		WebApplicationBase webApp = new WebApplicationWithErrorReport();
		webApp.start();
		Assert.assertTrue(webApp.isOnErrorReporterModuleStartCalled());
		
		webApp.stop();
		Assert.assertTrue(webApp.isBeforeErrorReporterModuleStopCalled());
	}
	
	@Test
	public void Web_application_with_smtp_sender_test() {
		WebApplicationBase webApp = new WebApplicationWithSmtpSender();
		webApp.start();
		Assert.assertTrue(webApp.isOnSmtpModuleStartCalled());
		
		webApp.stop();
		Assert.assertTrue(webApp.isBeforeSmtpModuleStopCalled());
	}
	// =========================================================================
}
