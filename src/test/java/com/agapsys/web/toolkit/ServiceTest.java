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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceTest {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    private static class TestService extends Service {
        private boolean isStartCalled = false;
        private boolean isStopCalled = false;

        @Override
        protected void onStart() {
            isStartCalled = true;
        }

        @Override
        protected void onStop() {
            isStopCalled = true;
        }
    }
    // =========================================================================
    // </editor-fold>

    private final AbstractWebApplication app = new MockedWebApplication();
    private TestService service = null;

    @Before
    public void before() {
        service = new TestService();
    }

    @Test
    public void testDefaults() {
        Assert.assertFalse(service.isStartCalled);
        Assert.assertFalse(service.isStopCalled);
    }

    @Test
    public void testRunning() {
        Assert.assertFalse(service.isRunning());

        app.start();
        
        service._start(app);
        Assert.assertTrue(service.isStartCalled);
        Assert.assertFalse(service.isStopCalled);
        Assert.assertTrue(service.isStartCalled);

        service._stop();
        Assert.assertTrue(service.isStopCalled);
        Assert.assertFalse(service.isRunning());
        
        app.stop();
    }
}
