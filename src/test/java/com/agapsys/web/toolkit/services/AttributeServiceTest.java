/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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

import com.agapsys.web.toolkit.services.AttributeService.DestroyListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class AttributeServiceTest {

    // <editor-fold desc="STATIC SCOPE" defaultstate="collapsed">
    // =========================================================================
    private static class ErrorWrapper {
        private Throwable error = null;

        public synchronized Throwable getError() {
            return error;
        }

        public synchronized void setError(Throwable error) {
            this.error = error;
        }
    }
    
    private static class ObjectWrapper<T> {
        private T wrappedObj;
        
        public ObjectWrapper() {
            this(null);
        }
        
        public ObjectWrapper(T wrappedObj) {
            this.wrappedObj = wrappedObj;
        }
        
        public synchronized T getWrappedObj() {
            return wrappedObj;
        }
        
        public synchronized void setWrappedObj(T wrappedObject) {
            this.wrappedObj = wrappedObject;
        }
    }
    // =========================================================================
    // </editor-fold>

    private final AttributeService attributeService = new AttributeService();

    @After
    public void after() {
        attributeService.destroyAttributes();
    }

    @Test
    public void test() throws InterruptedException {
        final ErrorWrapper errorWrapper = new ErrorWrapper();
        
        attributeService.setAttribute("val", "mainThread");
        Assert.assertEquals("mainThread", attributeService.getAttribute("val"));

        Thread anotherThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Assert.assertNull(attributeService.getAttribute("val"));
                    attributeService.setAttribute("val", "anotherThread");
                    Assert.assertEquals("anotherThread", attributeService.getAttribute("val"));
                } catch (Throwable t) {
                    errorWrapper.setError(t);
                }
            }
        });

        anotherThread.start();
        anotherThread.join();
        Assert.assertEquals("mainThread", attributeService.getAttribute("val"));
        Assert.assertNull(errorWrapper.getError());
    }
    
    @Test
    public void testDestroyListener() throws InterruptedException {
        final String COMMON_ATTR_NAME = "val";
        final ErrorWrapper errorWrapper = new ErrorWrapper();

        final ObjectWrapper<Boolean> mainThreadListenerCheker = new ObjectWrapper<>(false);
        final ObjectWrapper<Boolean> anotherThreadListenerChecker = new ObjectWrapper<>(false);
        
        attributeService.setAttribute(COMMON_ATTR_NAME, "mainThread", new DestroyListener() {
            @Override
            public void onDestroy(Object obj) {
                mainThreadListenerCheker.setWrappedObj(true);
            }
        });
        Assert.assertEquals("mainThread", attributeService.getAttribute(COMMON_ATTR_NAME));
        Assert.assertFalse(mainThreadListenerCheker.getWrappedObj());
        
        Thread anotherThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Assert.assertNull(attributeService.getAttribute("val"));
                    attributeService.setAttribute(COMMON_ATTR_NAME, "anotherThread", new DestroyListener() {
                        @Override
                        public void onDestroy(Object obj) {
                            anotherThreadListenerChecker.setWrappedObj(true);
                        }
                    });
                    Assert.assertEquals("anotherThread", attributeService.getAttribute(COMMON_ATTR_NAME));
                    Assert.assertFalse(mainThreadListenerCheker.getWrappedObj());
                    Assert.assertFalse(anotherThreadListenerChecker.getWrappedObj());
                    
                    attributeService.destroyAttributes();
                    Assert.assertNull(attributeService.getAttribute(COMMON_ATTR_NAME));
                    Assert.assertFalse(mainThreadListenerCheker.getWrappedObj());
                    Assert.assertTrue(anotherThreadListenerChecker.getWrappedObj());
                } catch (Throwable t) {
                    errorWrapper.setError(t);
                }
            }
        });

        anotherThread.start();
        anotherThread.join();
        Assert.assertEquals("mainThread", attributeService.getAttribute(COMMON_ATTR_NAME));
        
        attributeService.destroyAttributes();
        Assert.assertNull(attributeService.getAttribute(COMMON_ATTR_NAME));
        Assert.assertTrue(mainThreadListenerCheker.getWrappedObj());
        
        Assert.assertNull(errorWrapper.getError());
    }
    
    @Test
    public void testDestroyListenerIndividual() throws InterruptedException {
        final String COMMON_ATTR_NAME = "val";
        final ErrorWrapper errorWrapper = new ErrorWrapper();

        final ObjectWrapper<Boolean> mainThreadListenerCheker = new ObjectWrapper<>(false);
        final ObjectWrapper<Boolean> anotherThreadListenerChecker = new ObjectWrapper<>(false);
        
        attributeService.setAttribute(COMMON_ATTR_NAME, "mainThread", new DestroyListener() {
            @Override
            public void onDestroy(Object obj) {
                mainThreadListenerCheker.setWrappedObj(true);
            }
        });
        Assert.assertEquals("mainThread", attributeService.getAttribute(COMMON_ATTR_NAME));
        Assert.assertFalse(mainThreadListenerCheker.getWrappedObj());
        
        Thread anotherThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Assert.assertNull(attributeService.getAttribute("val"));
                    attributeService.setAttribute(COMMON_ATTR_NAME, "anotherThread", new DestroyListener() {
                        @Override
                        public void onDestroy(Object obj) {
                            anotherThreadListenerChecker.setWrappedObj(true);
                        }
                    });
                    Assert.assertEquals("anotherThread", attributeService.getAttribute(COMMON_ATTR_NAME));
                    Assert.assertFalse(mainThreadListenerCheker.getWrappedObj());
                    Assert.assertFalse(anotherThreadListenerChecker.getWrappedObj());
                    
                    attributeService.destroyAttribute(COMMON_ATTR_NAME);
                    Assert.assertNull(attributeService.getAttribute(COMMON_ATTR_NAME));
                    Assert.assertFalse(mainThreadListenerCheker.getWrappedObj());
                    Assert.assertTrue(anotherThreadListenerChecker.getWrappedObj());
                } catch (Throwable t) {
                    errorWrapper.setError(t);
                }
            }
        });

        anotherThread.start();
        anotherThread.join();
        Assert.assertEquals("mainThread", attributeService.getAttribute(COMMON_ATTR_NAME));
        
        attributeService.destroyAttribute(COMMON_ATTR_NAME);
        Assert.assertNull(attributeService.getAttribute(COMMON_ATTR_NAME));
        Assert.assertTrue(mainThreadListenerCheker.getWrappedObj());
        
        Assert.assertNull(errorWrapper.getError());
    }
}
