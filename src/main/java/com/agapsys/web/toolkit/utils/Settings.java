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
package com.agapsys.web.toolkit.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class Settings {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    static final String  _KEY_REGEX   = "[a-z][a-zA-Z\\.0-9_]*";
    static final Pattern _KEY_PATTERN = Pattern.compile(String.format("^%s$", _KEY_REGEX));

    public static class PropertyNotFoundException extends RuntimeException {
        private final String key;

        PropertyNotFoundException(String key) {
            super("Property not found: " + key);
            this.key = key;
        }

        public final String getKey() {
            return key;
        }

    }
    // =========================================================================
    // </editor-fold>

    private final Map<String, String> propertyMap = new LinkedHashMap<>();

    public synchronized boolean containsKey(String key) {
        return propertyMap.containsKey(key);
    }

    public synchronized boolean removeProperty(String key) {
        return propertyMap.remove(key) != null;
    }

    public synchronized boolean setProperty(String key, String value) {
        if (key == null || key.isEmpty())
            throw new IllegalArgumentException("Null/Empty key");

        if (!key.matches(_KEY_PATTERN.pattern()))
            throw new IllegalArgumentException("Invalid key: " + key);

        if (value == null)
            throw new IllegalArgumentException("Null value");

        if (Objects.equals(value, propertyMap.get(key)))
            return false;

        propertyMap.put(key, value);

        return true;
    }

    public synchronized boolean setPropertyIfAbsent(String key, String value) {
        if (containsKey(key))
            return false;

        return setProperty(key, value);
    }

    public synchronized boolean setProperties(Settings other) {
        boolean result = false;
        for (Map.Entry<String, String> entry : other.entrySet()) {
            result = result || propertyMap.containsKey(entry.getKey());

            propertyMap.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public synchronized boolean setPropertiesIfAbsent(Settings other) {
        boolean result = false;
        for (Map.Entry<String, String> entry : other.entrySet()) {
            if (!propertyMap.containsKey(entry.getKey())) {
                result = true;
                propertyMap.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public synchronized String getProperty(String key, String defaultValue) {
        if (!propertyMap.containsKey(key))
            return defaultValue;

        return propertyMap.get(key);
    }

    public synchronized String getMandatoryProperty(String key) throws PropertyNotFoundException {
        String value = getProperty(key, null);

        if (value == null)
            throw new PropertyNotFoundException(key);

        return value;
    }

    public synchronized boolean clear() {
        if (propertyMap.isEmpty())
            return false;

        propertyMap.clear();
        return true;
    }

    public synchronized boolean isEmpty() {
        return propertyMap.isEmpty();
    }

    void _store(OutputStream os) throws IOException {
        for(Map.Entry<String, String> entry : propertyMap.entrySet()) {
            String line = String.format("%s=%s\n", entry.getKey(), entry.getValue());
            os.write(line.getBytes("utf-8"));
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.propertyMap);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Settings other = (Settings) obj;
        if (!Objects.equals(this.propertyMap, other.propertyMap)) {
            return false;
        }
        return true;
    }

    public synchronized Set<Entry<String, String>> entrySet() {
        return propertyMap.entrySet();
    }
}
