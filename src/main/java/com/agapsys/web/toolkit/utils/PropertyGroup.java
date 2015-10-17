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

package com.agapsys.web.toolkit.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

public class PropertyGroup {
	// CLASS SCOPE =============================================================
	private static String getPropertyAsString(Properties prop, String comments) {    
		try {
			StringWriter writer = new StringWriter();
			prop.store(writer, null);
			String out = writer.getBuffer().toString();
			String firstLine = out.substring(0, out.indexOf("\n"));
			out = out.replace(firstLine + "\n", "");
			return String.format("# %s\n%s", comments, out);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public static void writeToFile(File file, Collection<PropertyGroup> propertyGroups) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			for (PropertyGroup propertyGroup : propertyGroups) {
				sb.append(getPropertyAsString(propertyGroup.getProperties(), propertyGroup.getName())).append("\n");
			}

			writer.println(sb.toString().trim());
		}
	}
	
	/** 
	 * Extracts properties associated with given keyPrefix
	 * @param properties properties to be parsed
	 * @param keyPrefix key prefix used to extract a subset of properties. Passing null or an empty string will extract only standard properties
	 * @param enclosing enclosing string (usually "()" or "[]")
	 * @return properties subset
	 */
	public static Properties getSubProperties(Properties properties, String keyPrefix, String enclosing) {
		Properties subProperties = new Properties();
		if (enclosing == null || enclosing.trim().isEmpty()) throw new IllegalArgumentException("Null/Empty enclosing");
		if (enclosing.length() % 2 != 0) throw new IllegalArgumentException("Enclosing string must have an even length");
		
		if (keyPrefix == null)
			keyPrefix = "";
		
		keyPrefix = keyPrefix.trim();
		
		String enclosingOpen = enclosing.substring(0, enclosing.length() / 2);
		String enclosingClose = enclosing.substring(enclosing.length() / 2);
		
		keyPrefix = String.format("%s%s%s", enclosingOpen, keyPrefix, enclosingClose);
		
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			
			if (!key.startsWith(enclosingOpen)) {
				// It's a standard property...
				subProperties.putIfAbsent(key, value);
			} else {	
				if (key.startsWith(keyPrefix)) {
					subProperties.setProperty(key.replaceFirst(Pattern.quote(keyPrefix), ""), value);
				}
			}
		}
		
		return subProperties;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final Properties properties;
	private final String name;

	public PropertyGroup(Properties properties, String name) {
		if (properties == null)
			throw new IllegalArgumentException("Null properties");
		
		if (name == null || name.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty name");
		
		name = name.trim();
		
		this.properties = properties;
		this.name = name;
	}

	public Properties getProperties() {
		return properties;
	}
	
	public String getName() {
		return name;
	}
	// =========================================================================
}
