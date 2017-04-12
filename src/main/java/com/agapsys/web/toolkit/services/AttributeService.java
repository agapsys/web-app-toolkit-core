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

import com.agapsys.web.toolkit.Service;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global attribute service for thread-safe access.
 */
public class AttributeService extends Service {

    public static interface DestroyListener<T> {

        /**
         * Called just before given object is being destroyed.
         *
         * @param t object being destroyed.
         */
        public void onDestroy(T t);
    }

    private static class Attribute {
        private final Object obj;
        private final DestroyListener destroyListener;

        public Attribute(Object obj) {
            this(obj, null);
        }

        public Attribute(Object obj, DestroyListener destroyListener) {
            this.obj = obj;
            this.destroyListener = destroyListener;
        }

        public final Object getObject() {
            return obj;
        }

        public final DestroyListener getDestroyListener() {
            return destroyListener;
        }
    }

    private final Map<Thread, Map<String, Attribute>> threadMap = new ConcurrentHashMap<>();

    private Map<String, Attribute> __getAttributeMap() {
        Thread currentThread = Thread.currentThread();
        Map<String, Attribute> attributeMap = threadMap.get(currentThread);
        if (attributeMap == null) {
            attributeMap = new LinkedHashMap<>();
            threadMap.put(currentThread, attributeMap);
        }

        return attributeMap;
    }

    public final Object getAttribute(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Null/Empty name");

        Attribute attribute = __getAttributeMap().get(name);
        return attribute != null ? attribute.getObject() : null;
    }
    public final void setAttribute(String name, Object attribute) {
        setAttribute(name, attribute, null);
    }
    public final <T> void setAttribute(String name, T attribute, DestroyListener<T> destroyListener) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Null/Empty name");

        Attribute mAttribute = new Attribute(attribute, destroyListener);

        __getAttributeMap().put(name, mAttribute);
    }

    private void __destroyAttribute(Attribute attribute) {
        if (attribute != null && attribute.destroyListener != null) {
            attribute.destroyListener.onDestroy(attribute.obj);
        }
    }

    public final void destroyAttribute(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Null/Empty name");

        Map<String, Attribute> attributeMap = threadMap.get(Thread.currentThread());
        if (attributeMap != null) {
            __destroyAttribute(attributeMap.get(name));
            attributeMap.remove(name);
        }
    }

    public final void destroyAttributes() {
        Map<String, Attribute> attributeMap = threadMap.get(Thread.currentThread());

        if (attributeMap != null) {
            for (Map.Entry<String, Attribute> entry : attributeMap.entrySet()) {
                __destroyAttribute(entry.getValue());
            }
            attributeMap.clear();
        }
        threadMap.remove(Thread.currentThread());
    }
}
