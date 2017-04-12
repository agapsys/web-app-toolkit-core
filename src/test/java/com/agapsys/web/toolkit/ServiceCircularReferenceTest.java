/*
 * Copyright 2017 Agapsys Tecnologia Ltda-ME.
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
import org.junit.Test;

public class ServiceCircularReferenceTest {

    public static class Service1 extends Service {
        public boolean loaded = false;

        @Override
        protected void onStart() {
            super.onStart();
            getService(Service2.class);
            loaded = true;
        }

    }

    public static class Service2 extends Service {
        public boolean loaded = false;

        @Override
        protected void onStart() {
            super.onStart();
            getService(Service3.class);
            loaded = true;
        }

    }

    public static class Service3 extends Service {
        public boolean loaded = false;
        private final boolean enableCircularReference;

        public Service3() {
            this(false);
        }

        public Service3(boolean enableCircularReference) {
            this.enableCircularReference = enableCircularReference;
        }

        @Override
        protected void onStart() {
            super.onStart();
            if (enableCircularReference) {
                getService(Service4.class);
            }
            loaded = true;
        }

    }

    public static class Service4 extends Service {
        public boolean loaded = false;

        @Override
        protected void onStart() {
            super.onStart();
            getService(Service1.class);
            loaded = true;
        }

    }

    @Test
    public void testNonCircularReference1() {
        final Service1 service1 = new Service1();
        final Service2 service2 = new Service2();
        final Service3 service3 = new Service3();
        final Service4 service4 = new Service4();

        AbstractApplication app = new MockedWebApplication() {
            @Override
            public String getRootName() {
                return "non-circular-ref";
            }


            @Override
            protected void beforeStart() {
                super.beforeStart();

                // Custom services should be declared in beforeStart callback due to application internal reset during start
                registerService(service1);
                registerService(service2);
                registerService(service3);
                registerService(service4);
            }

        };

        app.start();

        Assert.assertFalse(service1.loaded);
        Assert.assertFalse(service2.loaded);
        Assert.assertFalse(service3.loaded);
        Assert.assertFalse(service4.loaded);

        app.getService(Service1.class); // <-- dependency path: service1 --> service2 --> service3

        Assert.assertTrue(service1.loaded);
        Assert.assertTrue(service2.loaded);
        Assert.assertTrue(service3.loaded);
        Assert.assertFalse(service4.loaded);

        app.getService(Service4.class); // <-- dependency path: service4 --> service1
        Assert.assertTrue(service1.loaded);
        Assert.assertTrue(service2.loaded);
        Assert.assertTrue(service3.loaded);
        Assert.assertTrue(service4.loaded);

        app.stop();
    }

    @Test
    public void testCircularReference1() {
        final Service1 service1 = new Service1();
        final Service2 service2 = new Service2();
        final Service3 service3 = new Service3(true);
        final Service4 service4 = new Service4();

        AbstractApplication app = new MockedWebApplication() {

            @Override
            public String getRootName() {
                return "circular-ref";
            }

            @Override
            protected void beforeStart() {
                super.beforeStart();

                // Custom services should be declared in beforeStart callback due to application internal reset during start
                registerService(service1);
                registerService(service2);
                registerService(service3);
                registerService(service4);
            }

        };

        app.start();

        Assert.assertFalse(service1.loaded);
        Assert.assertFalse(service2.loaded);
        Assert.assertFalse(service3.loaded);
        Assert.assertFalse(service4.loaded);

        Throwable t = null;
        try {
            app.getService(Service1.class); // <-- dependency path: service1 --> service2 --> service3 --> service4 --> service1
        } catch (RuntimeException ex) {
            t = ex;
        }

        Assert.assertNotNull(t);
        Assert.assertTrue(t.getMessage().startsWith("Circular service reference"));
        Assert.assertTrue(t.getMessage().endsWith("$Service1"));

        Assert.assertFalse(service1.loaded);
        Assert.assertFalse(service2.loaded);
        Assert.assertFalse(service3.loaded);
        Assert.assertFalse(service4.loaded);

        app.stop();
    }
}
