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

package com.agapsys.web.toolkit.modules;

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.MockedWebApplication;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistenceModuleTest {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static class TestPersistenceModule extends PersistenceModule {
        private boolean methodCalled = false;

        @Override
        protected void onInit(AbstractApplication app) {} // <-- does not init entity manager factory

        @Override
        protected EmFactory getEmFactory() {
            return new EmFactory() {
                @Override
                public EntityManager getInstance() {
                    methodCalled = true;
                    return null;
                }
            };
        }

        @Override
        protected void onStop() {}
    }
    // =========================================================================
    // </editor-fold>

    private TestPersistenceModule module;

    @Before
    public void before() {
        module = new TestPersistenceModule();
    }

    @Test
    public void sanityCheck() {
        Assert.assertFalse(module.methodCalled);
        Assert.assertFalse(module.isActive());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEntityManagerWhileNotRunning() {
        module.getEntityManager();
    }

    @Test
    public void testGetEntityManagerWhileRunning() {
        MockedWebApplication app = new MockedWebApplication() {
            @Override
            protected void beforeApplicationStart() {
                super.beforeApplicationStart();

                registerModule(module);
            }
        };
        app.start();
        Assert.assertNull(module.getEntityManager());
        Assert.assertTrue(module.methodCalled);
        app.stop();
        Assert.assertNull(AbstractApplication.getRunningInstance());
    }
}
