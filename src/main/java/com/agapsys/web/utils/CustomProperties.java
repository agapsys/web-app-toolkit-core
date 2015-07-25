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
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class CustomProperties extends Properties {
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
		
		public Entry (String key, String value) {
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
	private final List<Object> items = new LinkedList<>();

	public CustomProperties() {
	}

	public CustomProperties(Properties defaults) {
		super(defaults);
	}
	
	public synchronized void addComment(String comment) {
		if(comment == null || comment.trim().isEmpty())
			comment = "#";
		
		comment = "# " + comment.trim();
		items.add(new Comment(comment));
	}

	public synchronized void addEmptyLine() {
		items.add(null);
	}
	
	@Override
	public synchronized Object put(Object key, Object value) {
		Object obj = super.put(key, value);
		items.add(new Entry((String)key, (String)value));
		return obj;
	}
	
	@Override
	public synchronized Object setProperty(String key, String value) {
		return setProperty(key, value, false);
	}
	
	public synchronized Object setProperty(String key, String value, boolean keepExisting) {
		Object obj = null;
		
		if (!super.containsKey(key) || !keepExisting) {
			obj = super.setProperty(key, value);
		}
		return obj;
	}
	
	public void load(File file) throws IOException {
		try (FileInputStream fis = new FileInputStream(file)) {
			load(fis);
			Set<Map.Entry<Object, Object>> entries = entrySet();
			
			for (Map.Entry entry: entries) {
				items.add(new Entry((String)entry.getKey(), (String)entry.getValue()));
			}
		}
	}
	
	public void store(Writer writer) throws IOException {
		StringBuilder sb = new StringBuilder();
		
		
		
		for (Object item : items) {
			if (item == null) {
				sb.append("\n");
			} else {
				sb.append(item.toString()).append("\n");
			}
		}
		
		writer.write(sb.toString());
	}

	// Unsupported methods -----------------------------------------------------
	/** DO NOT USE THIS METHOD! */
	@Override
	public void store(Writer writer, String comments) throws IOException {
		throw new UnsupportedOperationException();
	}
	
	/** DO NOT USE THIS METHOD! */
	@Override
	public void store(OutputStream out, String comments) throws IOException {
		throw new UnsupportedOperationException();
	}
	// -------------------------------------------------------------------------
	// =========================================================================
}
