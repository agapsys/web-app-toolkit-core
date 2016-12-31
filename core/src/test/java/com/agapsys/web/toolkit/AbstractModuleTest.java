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

public class AbstractModuleTest {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    private static class TestModule extends Module {
        private boolean isStartCalled = false;
        private boolean isStopCalled = false;

        @Override
        protected void onInit(AbstractApplication app) {
            isStartCalled = true;
        }

        @Override
        protected void onStop() {
            isStopCalled = true;
        }

        @Override
        protected String getSettingsSection() {
            return "";
        }
    }
    // =========================================================================
    // </editor-fold>

    private final AbstractWebApplication app = new MockedWebApplication();
    private TestModule module = null;

    @Before
    public void before() {
        module = new TestModule();
    }

    @Test
    public void testDefaults() {
        Assert.assertFalse(module.isStartCalled);
        Assert.assertFalse(module.isStopCalled);
    }

    @Test
    public void testRunning() {
        Assert.assertFalse(module.isActive());

        module._init(app);
        Assert.assertTrue(module.isStartCalled);
        Assert.assertFalse(module.isStopCalled);
        Assert.assertTrue(module.isStartCalled);

        module._stop();
        Assert.assertTrue(module.isStopCalled);
        Assert.assertFalse(module.isActive());
    }
}
