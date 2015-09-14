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

package com.agapsys.web.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Properties {
	// CLASS SCOPE =============================================================
	private static class Comment {
		public String comment;
		
		public Comment(String comment) {
			this.comment = comment;
		}

		@Override
		public String toString() {
			return comment.replace("\n", "\n# ");
		}
	}
	
	private static class Entry {
		public String key;
		public String value;
		
		public Entry(String key, String value) {
			this.key = key;
			this.value = value;
		}

		@Override
		public String toString() {
			String adjustedKey = key.replace(":", "\\:").replace("=", "\\=");
			String adjustedValue = value.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t");
			
			return adjustedKey+"="+adjustedValue;
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private List<Object> items = new LinkedList<>();
	private Map<String, String> entries = new LinkedHashMap<>();

	public Properties() {}
	
	public synchronized void addComment(String comment) {
		if(comment == null || comment.trim().isEmpty())
			comment = "#";
		
		comment = "# " + comment.trim();
		items.add(new Comment(comment));
	}

	public synchronized void addEmptyLine() {
		items.add(null);
	}

	public synchronized String getProperty(String key) {
		return getProperty(key, null);
	}
	
	public synchronized String getProperty(String key, String defaultValue) {
		if (entries.containsKey(key)) {
			return entries.get(key);
		} else {
			return defaultValue;
		}
	}
	
	public synchronized void setProperty(String key, String value) {
		setProperty(key, value, false);
	}
	
	public synchronized void setProperty(String key, String value, boolean keepExisting) {
		if (key == null || key.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty key");
		
		if (!entries.containsKey(key) || !keepExisting) {
			entries.put(key, value);
			items.add(new Entry(key, value));
		}
	}
	
	public synchronized void append(Properties other) {
		append(other, false);
	}
	
	public synchronized void append(Properties other, boolean keepExisting) {
		if (other == null)
			throw new IllegalArgumentException("Given properties is null");
		
		for (Map.Entry<String, String> entry : other.entries.entrySet()) {
			setProperty(entry.getKey(), entry.getValue(), keepExisting);
		}
	}
	
	public synchronized void load(File file) throws IOException {
		clear();
		
		try (FileInputStream fis = new FileInputStream(file)) {
			java.util.Properties props = new java.util.Properties();
			props.load(fis);
			
			Set<Map.Entry<Object, Object>> entrySet = props.entrySet();
			
			for (Map.Entry entry: entrySet) {
				items.add(new Entry((String)entry.getKey(), (String)entry.getValue()));
			}
		}
	}
	
	public synchronized void store(File file) throws IOException {
		try (PrintWriter writer = new PrintWriter(new FileOutputStream(file))) {
			StringBuilder sb = new StringBuilder();
			
			for (Object item : items) {
				if (item == null) {
					sb.append("\n");
				} else {
					sb.append(item.toString()).append("\n");
				}
			}
			
			writer.println(sb.toString());
		}
	}

	public synchronized void clear() {
		items.clear();
		entries.clear();
	}
	
	public synchronized boolean isEmpty() {
		return items.isEmpty();
	}
	
	public Map<String, String> getEntries() {
		return Collections.unmodifiableMap(entries);
	}
	
	public Properties getUnmodifiableProperties() {
		List<Object> tmpItems = Collections.unmodifiableList(items);
		Map<String, String> tmpEntries = getEntries();
		Properties clone = new Properties();
		clone.entries = tmpEntries;
		clone.items = tmpItems;
		return clone;
	}
	
	// =========================================================================
}
