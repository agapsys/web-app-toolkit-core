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

import com.agapsys.web.modules.LoggingModule;
import com.agapsys.web.modules.CrashReporterModule;
import com.agapsys.web.modules.PersistenceModule;
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

/** 
 * Default application listener
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class WebApplication implements ServletContextListener {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_ENVIRONMENT = "production";
	
	private static final String SETTINGS_FILENAME_PREFIX    = "settings";
	private static final String SETTINGS_FILENAME_SUFFIX    = ".conf";
	private static final String SETTINGS_FILENAME_DELIMITER = "-";
	
	public static final String LOG_TYPE_ERROR   = "ERROR";
	public static final String LOG_TYPE_WARNING = "WARNING";
	public static final String LOG_TYPE_INFO    = "INFO";
	
	// State -------------------------------------------------------------------
	private static boolean           running           = false;
	
	private static String            appName           = null;
	private static String            appVersion        = null;
	private static File              appFolder         = null;
	private static String            environment       = null;
	
	private static final Properties properties = new Properties();
	private static Properties readOnlyProperties = null;
	
	private static PersistenceModule   persistenceModule = null;
	private static CrashReporterModule crashReporter     = null;
	private static LoggingModule       loggingModule     = null;
	// -------------------------------------------------------------------------
	
	// Application management methods ------------------------------------------
	
	/** @return  a boolean indicating if application is running. */
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
	
	/**
	 * @return application name. 
	 * @see WebApplication#getAppName() 
	 * @throws IllegalStateException if application is not running
	 */
	public static String getName() throws IllegalStateException {
		throwIfNotRunning();
		return appName;
	}
	
	/**
	 * @return application version.
	 * @see WebApplication#getAppVersion() 
	 * @throws IllegalStateException if application is not running.
	 */
	public static String getVersion() throws IllegalStateException {
		throwIfNotRunning();
		
		return appVersion;
	}
	
	/** 
	 * @return the folder where application stores resources outside application context in servlet container. 
	 * @throws IllegalStateException if application is not running.
	 */
	public static File getAppFolder() throws IllegalStateException {
		throwIfNotRunning();
		
		return appFolder;
	}
	
	/**
	 * @return the name of the currently running environment.
	 * @throws IllegalStateException if application is not running.
	 * @see WebApplication#getDefaultEnvironment() 
	 */
	public static String getEnvironment() throws IllegalStateException {
		throwIfNotRunning();
		
		return environment;
	}
	// -------------------------------------------------------------------------
	
	// Global application methods ----------------------------------------------
	/** @return application properties */
	public static Properties getProperties() {
		if (readOnlyProperties == null)
			readOnlyProperties = properties.getUnmodifiableProperties();
		
		return readOnlyProperties;
	}
	
	/**
	 * @return an entity manger to be used by application. If there is no persistence module, returns null
	 * @throws IllegalStateException if application is not running.
	 * @see WebApplication#getPersistenceModule() 
	 */
	public static EntityManager getEntityManager() throws IllegalStateException {
		throwIfNotRunning();
		
		if (persistenceModule != null)
			return persistenceModule.getEntityManager();
		else
			return null;
	}
	
	/**
	 * Reports an error in the application. If there is no crash report module, nothing happens;
	 * @param req erroneous HTTP request
	 * @param resp HTTP response
	 * @throws IllegalStateException if application is not running.
	 * @throws ServletException if there is an error processing the request
	 * @throws IOException  if there is an I/O error processing the request
	 * @see WebApplication#getCrashReporterModule() 
	 */
	public static void reportError(HttpServletRequest req, HttpServletResponse resp) throws IllegalStateException, ServletException, IOException {
		throwIfNotRunning();
		
		if (crashReporter != null)
			crashReporter.reportError(req, resp);
	}
	
	/**
	 * Logs a message in the application. If there is no logging module, nothing happens.
	 * @param logType message type
	 * @param message message to be logged
	 * @throws IllegalStateException if application is not running.
	 * @see WebApplication#getLoggingModule() 
	 */
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
	
	
	// Persistence module ----------------------------------------------------------
	/** @return the persistence module used by application. Default implementation just returns null (there is no persistence module). */
	protected PersistenceModule getPersistenceModule() {
		return null;
	}
	
	/** Performs persistence module initialization. Default implementation does nothing. */
	protected void startPersistenceModule() {}
	
	/** Performs persistence module shutdown. Default implementation does nothing. */
	protected void stopPersistenceModule() {}
	// -------------------------------------------------------------------------
	
	// Crash reporter ----------------------------------------------------------
	/** @return the crash report module used by application. Default implementation just returns null (there is no crash report module). */
	protected CrashReporterModule getCrashReporterModule() {
		return null;
	}
	
	/** Performs crash report module initialization. Default implementation does nothing. */
	protected void startCrashReporter() {}
	
	/** Performs crash report module shutdown. Default implementation does nothing. */
	protected void stopCrashReporter() {}
	// -------------------------------------------------------------------------
	
	// Logging module ----------------------------------------------------------
	/** @return the logging module used by application. Default implementation just returns null (there is no logging module). */
	protected LoggingModule getLoggingModule() {
		return null;
	}
	
	/** Performs logging module initialization. Default implementation does nothing. */
	protected void startLoggingModule() {}
	
	/** Performs logging module shutdown. Default implementation does nothing. */
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
	
	/** Forces application initialization. This method is intended to be used for testing purposes. */
	public final void start() {
		if (!isRunning()) {
			printToConsole("====== AGAPSYS WEB CORE FRAMEWORK INITIALIZATION ======");
			appName = getAppName().trim();
			if (appName == null || appName.trim().isEmpty())
				throw new IllegalStateException("Missing application name");
			
			appVersion = getAppVersion().trim();
			if (appVersion == null || appVersion.trim().isEmpty())
				throw new IllegalStateException("Missing application version");
			
			environment = getDefaultEnvironment().trim();
			if (environment == null || environment.trim().isEmpty())
				throw new IllegalStateException("Missing environment");
			
			printToConsole("Environment set: %s", environment);
			
			appFolder = FileUtils.getOrCreateFolder(new File(FileUtils.USER_HOME, "." + appName).getAbsolutePath());

			persistenceModule = getPersistenceModule();
			crashReporter     = getCrashReporterModule();
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
	
	/** Forces application shutdown. This method is intended to be used for testing purposes. */
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
