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

package com.agapsys.web;

import com.agapsys.web.utils.Properties;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class SettingsModule {
	// CLASS SCOPE =============================================================
	private static final String SETTINGS_FILENAME_PREFIX    = "settings";
	private static final String SETTINGS_FILENAME_SUFFIX    = ".conf";
	private static final String SETTINGS_FILENAME_DELIMITER = "-";
	
	private static File       settingsFile = null;
	private static Properties properties = null;
	
	public static File getSettingsFile() {
		if (settingsFile == null)
			settingsFile = new File(WebApplication.getAppFolder(true), SETTINGS_FILENAME_PREFIX + SETTINGS_FILENAME_DELIMITER + WebApplication.getEnvironment() + SETTINGS_FILENAME_SUFFIX);
		
		return settingsFile;
	}
	
	/** @return boolean indicating if module is running. */
	public static boolean isRunning() {
		return properties != null;
	}
	
	/** 
	 * Starts the module module.
	 * If the module is running, nothing happens.
	 */
	public static void start() {
		if (!isRunning()) {
			if (!getSettingsFile().exists()) {
				try {
					createDefaultSettings();
				} catch(IOException ex) {
					throw new RuntimeException(ex);
				}
			} 
			
			try {
				properties = new Properties();
				properties.load(getSettingsFile());
				PersistenceModule.loadDefaults(properties, true);
				MaintenanceModule.loadDefaults(properties, true);
				
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	/** 
	 * @return the properties used by the application. 
	 * @throws IllegalStateException if module is not running
	 */
	public static Properties getProperties() throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		return properties;
	}
	
	private static void createDefaultSettings() throws IOException {
		Properties props = new Properties();
		
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(getSettingsFile()))) {
			props.addComment("Persistence settings==========================================================");
			PersistenceModule.loadDefaults(props, false);
			props.addComment("==============================================================================");
			props.addEmptyLine();
			props.addComment("Maintenance module settings ==================================================");
			MaintenanceModule.loadDefaults(props, false);
			props.addComment("==============================================================================");
			props.store(writer);
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private SettingsModule() {}
	// =========================================================================
}
