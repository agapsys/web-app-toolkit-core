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

import com.agapsys.web.toolkit.utils.Settings.PropertyNotFoundException;
import static com.agapsys.web.toolkit.utils.Settings._KEY_PATTERN;
import static com.agapsys.web.toolkit.utils.Settings._KEY_REGEX;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationSettings {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static final Pattern SECTION_PATTERN = Pattern.compile(String.format("^\\[(%s)\\]$", _KEY_REGEX));
    private static final Pattern ENTRY_PATTERN   = Pattern.compile(String.format("^%s=.+", _KEY_REGEX));

    public static ApplicationSettings load(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"))) {
            ApplicationSettings settings = new ApplicationSettings();

            String currentGroupName = null;

            String line;
            int i = 1;

            while ((line = br.readLine()) != null) {
                line = line.trim();

                Matcher m = SECTION_PATTERN.matcher(line);
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

    private final Map<String, Settings> sectionMap = new LinkedHashMap<>();

    public synchronized boolean containsSection(String name) {
        return sectionMap.containsKey(name);
    }

    public synchronized Settings getSection(String name) {
        return sectionMap.get(name);
    }

    public synchronized boolean removeSection(String name) {
        return sectionMap.remove(name) != null;
    }


    public synchronized boolean clear() {
        if (sectionMap.isEmpty())
            return false;

        for (Settings settings : sectionMap.values())
            settings.clear();

        sectionMap.clear();
        return true;
    }


    public synchronized final boolean containsProperty(String key) {
        return containsProperty(null, key);
    }

    public synchronized boolean containsProperty(String section, String key) {
        return containsSection(section) && getSection(section).containsKey(key);
    }


    public synchronized final boolean removeProperty(String key) {
        return removeProperty(null, key);
    }

    public synchronized boolean removeProperty(String section, String key) {
        if (containsSection(section)) {
            Settings mSection = getSection(section);
            boolean removed = mSection.removeProperty(key);

            if (mSection.isEmpty())
                removeSection(section);

            return removed;
        }

        return false;
    }


    public synchronized final boolean setProperty(String key, String value) {
        return setProperty(null, key, value);
    }

    public synchronized boolean setProperty(String section, String key, String value) {
        if (section != null && !section.matches(_KEY_PATTERN.pattern()))
            throw new IllegalArgumentException("Invalid section name: " + section);

        Settings mSection = getSection(section);
        if (mSection == null) {
            mSection = new Settings();
            mSection.setProperty(key, value);
            sectionMap.put(section, mSection);
            return true;
        } else {
            return mSection.setProperty(key, value);
        }
    }


    public synchronized final boolean setPropertyIfAbsent(String key, String value) {
        return setPropertyIfAbsent(null, key, value);
    }

    public synchronized boolean setPropertyIfAbsent(String section, String key, String value) {
        if (containsProperty(section, key))
            return false;

        return setProperty(section, key, value);
    }


    public synchronized final String getProperty(String key, String defaultValue) {
        return getProperty(null, key, defaultValue);
    }

    public synchronized String getProperty(String section, String key, String defaultValue) {
        if (!containsProperty(section, key))
            return defaultValue;

        return getSection(section).getProperty(key, defaultValue);
    }


    public synchronized final String getMandatoryProperty(String key) {
        return getMandatoryProperty(null, key);
    }

    public synchronized String getMandatoryProperty(String section, String key) {
        if (!containsProperty(section, key))
            throw new PropertyNotFoundException(String.format("%s/%s", section, key));

        return getSection(section).getMandatoryProperty(key);
    }


    public synchronized final void store(File outputFile) throws IOException {
        store(outputFile, true);
    }

    public synchronized void store(File outputFile, boolean overwrite) throws IOException {
        if (outputFile.exists() && !overwrite)
            throw new IOException("File already exists: " + outputFile.getAbsolutePath());

        int i = 0;
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            
            // Root section must be stored first
            Settings rootSettings = sectionMap.get(null);
            if (rootSettings != null) {
                rootSettings._store(fos);
                i++;
            }
            
            for (Map.Entry<String, Settings> entry : sectionMap.entrySet()) {
                String sectionName = entry.getKey();
                
                if (sectionName == null)
                    continue;

                fos.write(String.format("%s[%s]\n", (i == 0 ? "" : "\n"), sectionName).getBytes("utf-8"));
                Settings mSettings = entry.getValue();
                mSettings._store(fos);
                i++;
            }
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.sectionMap);
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
        final ApplicationSettings other = (ApplicationSettings) obj;
        if (!Objects.equals(this.sectionMap, other.sectionMap)) {
            return false;
        }
        return true;
    }

    public Set<Entry<String, Settings>> entrySet() {
        return sectionMap.entrySet();
    }

}
