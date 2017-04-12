/*
 * Copyright 2015-2016 Agapsys Tecnologia Ltda-ME.
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

import com.agapsys.web.toolkit.services.ExceptionReporterService;
import com.agapsys.web.toolkit.services.PersistenceService;
import com.agapsys.web.toolkit.services.SmtpService;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

public class AbstractWebApplicationTest  {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static final String APP_NAME   = "test-app";
    private static final String APP_VERSION = "0.1.0";

    // Custom services ----------------------------------------------------------
    public static class CustomPersistenceService extends PersistenceService {

        @Override
        protected void onStart() {
            super.onStart();
            ((WebApplicationBase)getApplication()).onPersistenceServiceStartCalled = true;
        }

        @Override
        protected void onStop() {
            super.onStop();
            ((WebApplicationBase)getApplication()).onPersistenceServiceStopCalled = true;
        }

    }

    public static class CustomExceptionReporterService extends ExceptionReporterService {

        @Override
        protected void onStart() {
            super.onStart();
            ((WebApplicationBase)getApplication()).onExceptionReporterServiceStartCalled = true;
        }

        @Override
        protected void onStop() {
            super.onStop();
            ((WebApplicationBase)getApplication()).onExceptionReporterServiceStopCalled = true;
        }

    }

    public static class CustomSmtpService extends SmtpService {
        @Override
        protected void onStart() {
            super.onStart();
            ((WebApplicationBase)getApplication()).onSmtpServiceStartCalled = true;
        }

        @Override
        protected void onStop() {
            super.onStop();
            ((WebApplicationBase)getApplication()).onSmtpServiceStopCalled = true;
        }
    }
    // -------------------------------------------------------------------------

    // Custom applications -----------------------------------------------------
    private static class WebApplicationBase extends MockedWebApplication {
        private boolean beforeStopCalled = false;
        private boolean beforeStartCalled = false;

        private boolean onStartCalled = false;
        private boolean afterStopCalled = false;

        public boolean isBeforeApplicationStartCalled() {
            return beforeStartCalled;
        }
        public boolean isAfterApplicationStartCalled() {
            return onStartCalled;
        }

        public boolean isBeforeApplicationStopCalled() {
            return beforeStopCalled;
        }
        public boolean isAfterApplicationStopCalled() {
            return afterStopCalled;
        }

        private boolean onPersistenceServiceStartCalled = false;
        private boolean onPersistenceServiceStopCalled = false;

        public boolean isOnPersistenceServiceStartCalled() {
            return onPersistenceServiceStartCalled;
        }
        public boolean isOnPersistenceServiceStopCalled() {
            return onPersistenceServiceStopCalled;
        }

        private boolean onExceptionReporterServiceStartCalled = false;
        private boolean onExceptionReporterServiceStopCalled = false;

        public boolean isOnExceptionReporterServiceStartCalled() {
            return onExceptionReporterServiceStartCalled;
        }
        public boolean isOnExceptionReporterServiceStopCalled() {
            return onExceptionReporterServiceStopCalled;
        }

        private boolean onSmtpServiceStartCalled = false;
        private boolean onSmtpServiceStopCalled = false;

        public boolean isOnSmtpServiceStartCalled() {
            return onSmtpServiceStartCalled;
        }
        public boolean isOnSmtpServiceStopCalled() {
            return onSmtpServiceStopCalled;
        }

        @Override
        protected void beforeStart() {
            this.beforeStartCalled = true;
            super.beforeStart();
            java.util.logging.Logger.getLogger("org.hibernate").setLevel(Level.OFF); // Disable Hibernate log output
        }
        @Override
        protected void onStart() {
            this.onStartCalled = true;
        }

        @Override
        protected void beforeStop() {
            this.beforeStopCalled = true;
            super.beforeStop();
        }
        @Override
        protected void afterStop() {
            this.afterStopCalled = true;
            super.afterStop();
        }

        @Override
        public String getRootName() {
            return APP_NAME;
        }

        @Override
        public String getVersion() {
            return APP_VERSION;
        }
    }

    private static class WebApplicationWithPersistence extends WebApplicationBase {

        @Override
        protected void beforeStart() {
            super.beforeStart();
            registerService(new CustomPersistenceService());
        }
    }

    private static class WebApplicationWithErrorReport extends WebApplicationBase {

        @Override
        protected void beforeStart() {
            super.beforeStart();
            registerService(new CustomExceptionReporterService());
        }
    }

    private static class WebApplicationWithSmtpSender extends WebApplicationBase {

        @Override
        protected void beforeStart() {
            super.beforeStart();
            registerService(new CustomSmtpService());
        }
    }

    private static class FullFledgedApplication extends WebApplicationBase {

        @Override
        protected void beforeStart() {
            super.beforeStart();
            registerService(new CustomPersistenceService());
            registerService(new CustomSmtpService());
            registerService(new CustomExceptionReporterService());
        }
    }
    // -------------------------------------------------------------------------
    // =========================================================================
    // </editor-fold>

    @Test
    public void Simple_web_application_start_stop_test() {
        WebApplicationBase webApp = new WebApplicationBase();
        webApp.start();
        Assert.assertTrue(webApp.isBeforeApplicationStartCalled());
        Assert.assertTrue(webApp.isRunning());
        Assert.assertFalse(webApp.isOnPersistenceServiceStartCalled());
        Assert.assertFalse(webApp.isOnExceptionReporterServiceStartCalled());
        Assert.assertFalse(webApp.isOnSmtpServiceStartCalled());
        Assert.assertTrue(webApp.isAfterApplicationStartCalled());
        Assert.assertFalse(webApp.isBeforeApplicationStopCalled());
        Assert.assertFalse(webApp.isAfterApplicationStopCalled());

        webApp.stop();
        Assert.assertTrue(webApp.isBeforeApplicationStopCalled());
        Assert.assertFalse(webApp.isOnPersistenceServiceStopCalled());
        Assert.assertFalse(webApp.isOnExceptionReporterServiceStopCalled());
        Assert.assertFalse(webApp.isOnSmtpServiceStopCalled());
        Assert.assertFalse(webApp.isRunning());
        Assert.assertTrue(webApp.isAfterApplicationStopCalled());
        Assert.assertNull(AbstractWebApplication.getRunningInstance());
    }

    @Test
    public void Simple_web_application_getName_test() {
        AbstractWebApplication webApp = new WebApplicationBase();
        webApp.contextInitialized(null);
        Assert.assertEquals(APP_NAME, AbstractWebApplication.getRunningInstance().getName());
        webApp.contextDestroyed(null);
    }

    @Test
    public void Simple_web_application_getVersion_test() {
        AbstractWebApplication webApp = new WebApplicationBase();
        webApp.contextInitialized(null);
        Assert.assertEquals(APP_VERSION, AbstractWebApplication.getRunningInstance().getVersion());
        webApp.contextDestroyed(null);
    }

    @Test
    public void Web_application_with_persistence_test() {
        WebApplicationBase webApp = new WebApplicationWithPersistence();
        webApp.start();
        Assert.assertFalse(webApp.isOnPersistenceServiceStartCalled());
        webApp.getService(PersistenceService.class); // <-- Forces service initialization
        Assert.assertTrue(webApp.isOnPersistenceServiceStartCalled());

        PersistenceService persistenceService = (PersistenceService) webApp.getService(CustomPersistenceService.class);
        Assert.assertNotNull(persistenceService.getEntityManager());

        webApp.stop();
    }

    @Test
    public void Web_application_with_error_report_test() {
        WebApplicationBase webApp = new WebApplicationWithErrorReport();
        webApp.start();
        Assert.assertFalse(webApp.isOnExceptionReporterServiceStartCalled());
        webApp.getService(ExceptionReporterService.class); // <-- Forces service initialization
        Assert.assertTrue(webApp.isOnExceptionReporterServiceStartCalled());

        webApp.stop();
        Assert.assertTrue(webApp.isOnExceptionReporterServiceStopCalled());
    }

    @Test
    public void Web_application_with_smtp_sender_test() {
        WebApplicationBase webApp = new WebApplicationWithSmtpSender();
        webApp.start();
        Assert.assertFalse(webApp.isOnSmtpServiceStartCalled());
        webApp.getService(SmtpService.class); // <-- Forces service initialization
        Assert.assertTrue(webApp.isOnSmtpServiceStartCalled());

        webApp.stop();
        Assert.assertTrue(webApp.isOnSmtpServiceStopCalled());
    }

    @Test
    public void Full_fledged_application_test() {
        WebApplicationBase webApp = new FullFledgedApplication();
        webApp.start();

        Assert.assertFalse(webApp.isOnPersistenceServiceStartCalled());
        Assert.assertFalse(webApp.isOnSmtpServiceStartCalled());
        Assert.assertFalse(webApp.isOnExceptionReporterServiceStartCalled());

        webApp.getService(PersistenceService.class);       // <-- Forces service initialization
        webApp.getService(ExceptionReporterService.class); // <-- Forces service initialization
        webApp.getService(SmtpService.class);              // <-- Forces service initialization

        Assert.assertTrue(webApp.isOnPersistenceServiceStartCalled());
        Assert.assertTrue(webApp.isOnSmtpServiceStartCalled());
        Assert.assertTrue(webApp.isOnExceptionReporterServiceStartCalled());

        webApp.stop();
        Assert.assertTrue(webApp.isOnPersistenceServiceStopCalled());
        Assert.assertTrue(webApp.isOnSmtpServiceStopCalled());
        Assert.assertTrue(webApp.isOnExceptionReporterServiceStopCalled());
    }

    @Test
    public void Full_fledged_application_with_standard_services_test() {
        MockedWebApplication app = new MockedWebApplication();
        app.start();
        app.stop();
    }

}
