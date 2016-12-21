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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class Settings {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static final String  KEY_REGEX = "[a-z][a-zA-Z\\.0-9_]*";

    private static final Pattern KEY_PATTERN   = Pattern.compile(String.format("^%s$", KEY_REGEX));
    private static final Pattern GROUP_PATTERN = Pattern.compile(String.format("^\\[(%s)\\]$", KEY_REGEX));
    private static final Pattern ENTRY_PATTERN = Pattern.compile(String.format("^%s=.+", KEY_REGEX));

    public static class PropertyNotFoundException extends RuntimeException {
            private final String key;

            private PropertyNotFoundException(String key) {
                super("Property not found: " + key);
                this.key = key;
            }

            public final String getKey() {
                return key;
            }

        }

    public static class SettingsGroup {

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

            if (!key.matches(KEY_PATTERN.pattern()))
                throw new IllegalArgumentException("Invalid key: " + key);

            if (value == null || value.isEmpty())
                throw new IllegalArgumentException("Null/Empty value");

            return propertyMap.put(key, value) != null;
        }

        public synchronized boolean setIfAbsent(String key, String value) {
            if (containsKey(key))
                return false;

            return setProperty(key, value);
        }

        public final String getProperty(String key) {
            return getProperty(key, null);
        }

        public synchronized String getProperty(String key, String defaultValue) {
            if (!propertyMap.containsKey(key))
                return defaultValue;

            return propertyMap.get(key);
        }

        public synchronized String getMandatoryProperty(String key) throws PropertyNotFoundException {
            String value = getProperty(key);
            if (value == null || value.isEmpty())
                throw new PropertyNotFoundException(key);

            return value;
        }

        public synchronized boolean clear() {
            if (propertyMap.isEmpty())
                return false;

            propertyMap.clear();
            return true;
        }

    }

    public static Settings load(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
            Settings settings = new Settings();

            String currentGroupName = null;

            String line;
            int i = 1;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                Matcher m = GROUP_PATTERN.matcher(line);
                if (m.find()) {
                    // It's a group...
                    currentGroupName = m.group(1);
                } else if (line.matches(ENTRY_PATTERN.pattern())) {
                    // It's an entry...
                    String[] tokens = line.split("=", 2);
                    settings.setProperty(currentGroupName, tokens[0], tokens[1]);
                } else if (!line.isEmpty()) {
                    throw new IOException("Invalid entry at line " + i);
                }
                i++;
            }

            return settings;
        }

    }
    // =========================================================================
    // </editor-fold>

    private final Map<String, SettingsGroup> groupMap = new LinkedHashMap<>();

    public Settings() {
        groupMap.put(null, new SettingsGroup());
    }

    public boolean containsGroup(String groupName) {
        return groupMap.containsKey(groupName);
    }

    public SettingsGroup getGroup(String groupName) {
        return groupMap.get(groupName);
    }

    public void removeGroup(String groupName) {
        groupMap.remove(groupName);
    }


    public void clear() {
        groupMap.clear();
    }


    public final boolean containsProperty(String key) {
        return containsProperty(null, key);
    }

    public boolean containsProperty(String groupName, String key) {
        return containsGroup(groupName) && getGroup(groupName).containsKey(key);
    }


    public final boolean removeProperty(String key) {
        return removeProperty(null, key);
    }

    public boolean removeProperty(String groupName, String key) {
        if (containsGroup(groupName))
            return getGroup(groupName).removeProperty(key);

        return false;
    }


    public final boolean setProperty(String key, String value) {
        return setProperty(null, key, value);
    }

    public boolean setProperty(String groupName, String key, String value) {
        if (groupName != null && !groupName.matches(KEY_PATTERN.pattern()))
            throw new IllegalArgumentException("Invalid group name: " + groupName);

        SettingsGroup group = getGroup(groupName);
        if (group == null) {
            group = new SettingsGroup();
            boolean result = group.setProperty(key, value);
            groupMap.put(groupName, group);
            return result;
        } else {
            return group.setProperty(key, value);
        }
    }


    public boolean setIfAbsent(String key, String value) {

    }

    public boolean setIfAbsent(String groupName, String key, String value) {

    }

    public String getProperty(String key, String defaultValue) {

    }

    public String getPropery(String groupName, String key, String defaultValue) {

    }

    public String getMandatoryProperty(String key) {

    }

    public String getMandatoryProperty(String groupName, String key) {
        
    }


}
