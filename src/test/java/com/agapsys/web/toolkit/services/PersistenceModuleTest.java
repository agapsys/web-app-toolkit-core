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

package com.agapsys.web.toolkit.services;

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.MockedWebApplication;
import com.agapsys.web.toolkit.services.PersistenceService;
import javax.persistence.EntityManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PersistenceModuleTest {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static class TestPersistenceService extends PersistenceService {
        private boolean methodCalled = false;

        @Override
        protected void onStart() {} // <-- does not init entity manager factory

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

    private TestPersistenceService service;

    @Before
    public void before() {
        service = new TestPersistenceService();
    }

    @Test
    public void sanityCheck() {
        Assert.assertFalse(service.methodCalled);
        Assert.assertFalse(service.isRunning());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetEntityManagerWhileNotRunning() {
        service.getEntityManager();
    }

    @Test
    public void testGetEntityManagerWhileRunning() {
        MockedWebApplication app = new MockedWebApplication() {
            @Override
            protected void beforeStart() {
                super.beforeStart();

                registerService(service);
            }
        };
        app.start();
        Assert.assertNull(app.getService(PersistenceService.class).getEntityManager());
        Assert.assertTrue(service.methodCalled);
        app.stop();
        Assert.assertNull(AbstractApplication.getRunningInstance());
    }
}
