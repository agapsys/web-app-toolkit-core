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

import com.agapsys.logger.Logger;
import com.agapsys.web.utils.FileUtils;
import com.agapsys.web.utils.Properties;
import java.io.File;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class WebApplication implements ServletContextListener {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_ENVIRONMENT = "production";
	
	private static final String SETTINGS_FILENAME_PREFIX    = "settings";
	private static final String SETTINGS_FILENAME_SUFFIX    = ".conf";
	private static final String SETTINGS_FILENAME_DELIMITER = "-";
	
	public static final String LOG_TYPE_ERROR   = Logger.ERROR;
	public static final String LOG_TYPE_WARNING = Logger.WARNING;
	public static final String LOG_TYPE_INFO    = Logger.INFO;
	
	// State -------------------------------------------------------------------
	private static boolean           running           = false;
	
	private static String            appName           = null;
	private static String            appVersion        = null;
	private static File              appFolder         = null;
	private static String            environment       = null;
	
	private static final Properties properties = new Properties();
	private static Properties readOnlyProperties = null;
	
	private static PersistenceModule persistenceModule = null;
	private static CrashReporter     crashReporter     = null;
	private static LoggingModule     loggingModule         = null;
	// -------------------------------------------------------------------------
	
	// Application management methods ------------------------------------------
	public static boolean isRunning() {
		return running;
	}
	
	private static void throwIfNotRunning() throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Application is not running");
	}
	
	private static void printToConsole(String msg, Object...args) {
		System.out.println(String.format(msg, args));
	}
	// -------------------------------------------------------------------------
	
	// State management methods ------------------------------------------------
	public static String getName() throws IllegalStateException {
		throwIfNotRunning();
		return appName;
	}
	
	public static String getVersion() throws IllegalStateException {
		throwIfNotRunning();
		
		return appVersion;
	}
	
	public static File getAppFolder() throws IllegalStateException {
		throwIfNotRunning();
		
		return appFolder;
	}
	
	public static String getEnvironment() throws IllegalStateException {
		throwIfNotRunning();
		
		return environment;
	}
	// -------------------------------------------------------------------------
	
	// Global application methods ----------------------------------------------
	public static Properties getProperties() {
		if (readOnlyProperties == null)
			readOnlyProperties = properties.getUnmodifiableProperties();
		
		return readOnlyProperties;
	}
		
	public static EntityManager getEntityManager() throws IllegalStateException {
		throwIfNotRunning();
		
		if (persistenceModule != null)
			return persistenceModule.getEntityManager();
		else
			return null;
	}
	
	public static void reportError(HttpServletRequest req, HttpServletResponse resp) throws IllegalStateException, ServletException, IOException {
		throwIfNotRunning();
		
		if (crashReporter != null)
			crashReporter.reportError(req, resp);
	}
	
	public static void log(String logType, String message) throws IllegalStateException {
		throwIfNotRunning();
		
		if (loggingModule != null)
			loggingModule.writeLog(logType, message);
	}
	// -------------------------------------------------------------------------
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================

	/** @return the application name */
	protected abstract String getAppName();
	
	/** @return the application version **/
	protected abstract String getAppVersion();
	
	/** @return the defaultEnvironment used by application. Default implementation returns {@linkplain WebApplication#DEFAULT_ENVIRONMENT}. */
	protected String getDefaultEnvironment() {
		return DEFAULT_ENVIRONMENT;
	}
	
	
	// Persistence module ------------------------------------------------------
	protected PersistenceModule getPersistenceModule() {
		return null;
	}
	
	protected void startPersistenceModule() {}
	
	protected void stopPersistenceModule() {}
	// -------------------------------------------------------------------------
	
	// Crash reporter ----------------------------------------------------------
	protected CrashReporter getCrashReporter() {
		return null;
	}
	
	protected void startCrashReporter() {}
	
	protected void stopCrashReporter() {}
	// -------------------------------------------------------------------------
	
	// Logging module --------------------------------------------------------------
	protected LoggingModule getLoggingModule() {
		return null;
	}
	
	protected void startLoggingModule() {}
	
	protected void stopLoggingModule() {}
	// -------------------------------------------------------------------------
	
	private void loadSettings() throws IOException {
		File settingsFile = new File(appFolder, SETTINGS_FILENAME_PREFIX + SETTINGS_FILENAME_DELIMITER + environment + SETTINGS_FILENAME_SUFFIX);
		Properties tmpProperties;

		if (settingsFile.exists()) {
			printToConsole("Loading settings file...");

			// Load settings from file...
			properties.load(settingsFile);

			// Persistence: put default settings when there is no definition...
			if (persistenceModule != null) {
				tmpProperties = persistenceModule.getDefaultSettings();

				if (tmpProperties != null)
					properties.append(tmpProperties, true);
			}

			// Crash reporter: put default settings when there is no definition...
			if (crashReporter != null) {
				tmpProperties = crashReporter.getDefaultSettings();

				if (tmpProperties != null)
					properties.append(tmpProperties, true);
			}

		} else {
			printToConsole("Creating default settings file...");

			// Persistence: Loading defaults...
			if (persistenceModule != null) {
				tmpProperties = persistenceModule.getDefaultSettings();

				if (tmpProperties != null) {
					properties.addComment("Persistence settings==========================================================");
					properties.append(tmpProperties);
					properties.addComment("==============================================================================");
				}
			}

			// Crash reporter: Loading defaults...
			if (crashReporter != null) {
				tmpProperties = crashReporter.getDefaultSettings();

				if (tmpProperties != null) {
					properties.addComment("Crash reporter settings ======================================================");
					properties.append(tmpProperties);
					properties.addComment("==============================================================================");
				}
			}

			// Storing in settings file...
			if (!properties.isEmpty()) {
				properties.store(settingsFile);
			}
		}
	}
	
	public final void start() {
		if (!isRunning()) {
			printToConsole("====== AGAPSYS WEB CORE FRAMEWORK INITIALIZATION ======");
			appName = getAppName();
			if (appName == null || appName.trim().isEmpty())
				throw new IllegalStateException("Missing application name");
			
			appVersion = getAppVersion();
			if (appVersion == null || appVersion.trim().isEmpty())
				throw new IllegalStateException("Missing application version");
			
			environment = getDefaultEnvironment();
			if (environment == null || environment.trim().isEmpty())
				throw new IllegalStateException("Missing environment");
			
			printToConsole("Environment set: %s", environment);
			
			appFolder = FileUtils.getOrCreateFolder(new File(FileUtils.USER_HOME, "." + appName).getAbsolutePath());

			persistenceModule = getPersistenceModule();
			crashReporter     = getCrashReporter();
			loggingModule     = getLoggingModule();
			
			try {
				loadSettings();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			if (persistenceModule != null) {
				printToConsole("Starting persistence module...");
				startPersistenceModule();
			}
			
			if (crashReporter != null) {
				printToConsole("Starting crash reporter...");
				startCrashReporter();
			}
			
			if (loggingModule != null) {
				printToConsole("Starting logging module...");
				startLoggingModule();
			}
			
			running = true;
			printToConsole("====== AGAPSYS WEB CORE FRAMEWORK IS READY! ======");
		}
	}
	
	public final void stop() {
		if (isRunning()) {
			printToConsole("====== AGAPSYS WEB CORE FRAMEWORK SHUTDOWN ======");
			if (loggingModule != null) {
				printToConsole("Shutting down logging module...");
				stopLoggingModule();
			}
			
			if (crashReporter != null) {
				printToConsole("Shutting down crash reporter...");
				stopCrashReporter();
			}
			
			if (persistenceModule != null) {
				printToConsole("Shutting down persistence module...");
				stopPersistenceModule();
			}

			running = false;
			printToConsole("====== AGAPSYS WEB CORE FRAMEWORK WAS SHUTTED DOWN! ======");
		}
	}
	
	@Override
	public final void contextInitialized(ServletContextEvent sce) {
		start();
	}

	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		stop();
	}
	// =========================================================================
}
