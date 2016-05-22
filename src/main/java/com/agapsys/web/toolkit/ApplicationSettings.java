/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class ApplicationSettings {
	// CLASS SCOPE =============================================================
	/**
	 * Parses a string properties.
	 *
	 * @param s string to be parsed.
	 * @return parsed properties.
	 */
	private static Properties parseProperties(String s) {
		try {
			final Properties p = new Properties();
			p.load(new StringReader(s));
			return p;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Converts a properties instance to string.
	 *
	 * @param prop properties instance.
	 * @return properties string representation.
	 */
	private static String toString(Properties prop) {
		try {
			StringWriter writer = new StringWriter();
			prop.store(writer, null);
			String out = writer.getBuffer().toString();
			String firstLine = out.substring(0, out.indexOf("\n"));
			return out.replace(firstLine + "\n", "");
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private static final String GROUP_NAME_PATTERN = "^[a-zA-Z]+[a-zA-Z0-9\\-_\\.]+$";

	// Utility methods ---------------------------------------------------------
	/**
	 * Returns a property.
	 *
	 * @param map properties to be processed.
	 * @param key property key.
	 * @return property value or null if there is not such property.
	 */
	public static Object getProperty(Map map, Object key) {
		if (key == null)
			throw new IllegalArgumentException("Key cannot be null");

		Object val = map.get(key);

		if (val != null && val instanceof String) {
			String strVal = (String) val;
			strVal = strVal.trim();
			if (strVal.isEmpty())
				strVal = null;

			val = strVal;
		}

		return val;
	}

	/**
	 * Returns a mandatory property.
	 *
	 * @param map properties to be processed.
	 * @param key property key.
	 * @return application property.
	 * @throws RuntimeException if such property isn't defined.
	 */
	public static Object getMandatoryProperty(Map map, Object key) {
		Object val = getProperty(map, key);

		if (val == null)
			throw new RuntimeException(String.format("Missing property: %s", key.toString()));

		return val;
	}

	/**
	 * @see ApplicationSettings#getProperty(java.util.Map, java.lang.Object)
	 */
	public static String getProperty(Properties properties, String key) {
		return (String) getProperty((Map)properties, (Object)key);
	}

	/**
	 * @see ApplicationSettings#getMandatoryProperty(java.util.Map, java.lang.Object)
	 */
	public static String getMandatoryProperty(Properties properties, String key) {
		return (String) getMandatoryProperty((Map)properties, (Object)key);
	}

	/**
	 * Returns a new properties merging two instances
	 * @param properties main property instance
	 * @param defaults contains default properties to be merged if containing entries are not defined in main instance.
	 * @return merged instance.
	 */
	public static Properties mergeProperties(Properties properties, Properties defaults) {
		properties = new Properties(properties);

		for (Map.Entry defaultEntry : defaults.entrySet()) {
			properties.putIfAbsent(defaultEntry.getKey(), defaultEntry.getValue());
		}

		return properties;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final Map<String, Properties> propertyMap;
	private final boolean locked;

	private ApplicationSettings readOnlyInstance = null;

	public ApplicationSettings() {
		this(false, new LinkedHashMap<String, Properties>());
	}

	private ApplicationSettings(boolean locked, Map<String, Properties> propertyMap) {
		this.locked = locked;
		this.propertyMap = propertyMap;
	}

	private void throwIfLocked() throws UnsupportedOperationException {
		if (locked) throw new UnsupportedOperationException("Read-only instance");
	}

	/**
	 * Adds a property group.
	 *
	 * @param groupName group name.
	 * @param properties properties.
	 */
	public void addProperties(String groupName, Properties properties) {
		synchronized(propertyMap) {
			throwIfLocked();

			if (groupName == null || groupName.trim().isEmpty())
				throw new IllegalArgumentException("Null/Empty group name");

			if (properties == null)
				throw new IllegalArgumentException("Properties cannot be null");

			if (!groupName.matches(GROUP_NAME_PATTERN))
				throw new IllegalArgumentException("Invalid group: " + groupName);

			Properties existingProperties = getProperties(groupName);

			if (existingProperties == null) {
				propertyMap.put(groupName, properties);
			} else {
				for (Map.Entry entry : properties.entrySet()) {
					existingProperties.put(entry.getKey(), entry.getValue());
				}
			}
		}
	}

	/**
	 * Removes a property group.
	 *
	 * @param groupName group name.
	 */
	public void removeProperties(String groupName) {
		synchronized(propertyMap) {
			throwIfLocked();

			propertyMap.remove(groupName);
		}
	}

	/**
	 * Returns properties instance associated with given group.
	 *
	 * @param groupName property group
	 * @return properties associated with given group
	 */
	public Properties getProperties(String groupName) {
		synchronized(propertyMap) {
			if (groupName == null)
				throw new IllegalArgumentException("Group cannot be null");

			return propertyMap.get(groupName);
		}
	}

	/**
	 * Clear settings
	 */
	public void clear() {
		synchronized(propertyMap) {
			throwIfLocked();

			propertyMap.clear();
		}
	}

	/**
	 * Write settings to a file.
	 *
	 * @param output output file.
	 * @throws IOException if an error happened during the operation.
	 */
	public void writeToFile(File output) throws IOException {
		synchronized(propertyMap) {
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(output))) {

				for (Map.Entry<String, Properties> entry : propertyMap.entrySet()) {
					String group = entry.getKey();
					Properties props = entry.getValue();

					writer.println(String.format("[%s]", group));
					writer.println(toString(props));
				}
			}
		}
	}

	/**
	 * Reads settings from a file.
	 *
	 * @param input input file.
	 * @throws IOException if an error happened during the operation.
	 */
	public void read(File input) throws IOException {
		synchronized (propertyMap) {
			throwIfLocked();

			try (BufferedReader br = new BufferedReader(new FileReader(input))) {
				String line;

				String currentGroup = null;
				StringBuilder propString = new StringBuilder();

				while ((line = br.readLine()) != null) {
					line = line.trim();

					if (line.isEmpty() || line.startsWith("#"))
						continue;

					if (line.startsWith("[") && line.endsWith("]")) { // <-- group begin
						String group = line.substring(1, line.length() - 1);

						if (!group.matches(GROUP_NAME_PATTERN))
							throw new IOException("Invalid group name: " + group);

						if (currentGroup != null) {
							Properties props = parseProperties(propString.toString());
							addProperties(currentGroup, props);
							propString = new StringBuilder();
						}

						currentGroup = group;

					} else {
						if (currentGroup == null)
							throw new IOException("Root entries are not allowed: " + line);

						propString.append(line);
					}
				}

				if (currentGroup != null) {
					Properties props = parseProperties(propString.toString());
					addProperties(currentGroup, props);
				}
			}
		}
	}

	public ApplicationSettings getReadOnlyInstance() {
		synchronized(propertyMap) {
			if (readOnlyInstance == null)
				readOnlyInstance = new ApplicationSettings(true, this.propertyMap);

			return readOnlyInstance;
		}
	}
	// =========================================================================
}
