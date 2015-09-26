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

import com.agapsys.mail.Message;
import com.agapsys.utils.console.Console;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.Properties;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.LinkedList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Web application.
 * This class is not thread-safe
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
	private static boolean running           = false;
	private static boolean settingsLoaded    = false;
	private static boolean persistenceLoaded = false;
	private static boolean fullyLoaded       = false;
	
	private static boolean enableDebug = false;
	private static String  appName     = null;
	private static String  appVersion  = null;
	private static File    appFolder   = null;
	private static String  environment = null;
	
	private static final Properties properties = new Properties();
	private static Properties readOnlyProperties = null;
	
	private static PersistenceModule   persistenceModule   = null;
	private static ExceptionReporterModule errorReporterModule = null;
	private static LoggingModule       loggingModule       = null;
	private static SmtpModule          smtpModule          = null;
	
	private static final List<Runnable> eventQueue = new LinkedList<>();
	// -------------------------------------------------------------------------
	
	// Application management methods ------------------------------------------
	/**
	 * Return  a boolean indicating if application is running. 
	 * @return  a boolean indicating if application is running.
	 */
	public static boolean isRunning() {
		return running;
	}
	
	/** Throws an exception if application is not running. */
	private static void throwIfNotRunning() throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Application is not running");
	}
	
	/** Queues an runnable to be executed after application is fully loaded. */
	private static void invokeLater(Runnable runnable) {
		eventQueue.add(runnable);
	}
	
	/** Process event queue. */
	private static void processEventQueue() {
		for (Runnable runnable : eventQueue) {
			runnable.run();
		}
		eventQueue.clear();
	}
	
	/** 
	 * Prints debug messages if debug is enabled.
	 * @param message message to be printed
	 * @param args arguments if message is a formatted string
	 * @see String#format(String, Object...)
	 * @see WebApplication#start()
	 */
	public static void debug(String message, Object...args) throws IllegalStateException  {
		if (enableDebug)
			Console.printlnf(message, args);
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
	/**
	 * Return application settings.
	 * @return application properties
	 * @throws IllegalStateException if settings were not loaded yet
	 */
	public static Properties getProperties() throws IllegalStateException {
		if (!settingsLoaded)
			throw new IllegalStateException("Settings were not loaded yet");
		
		if (readOnlyProperties == null)
			readOnlyProperties = properties.getUnmodifiableProperties();
		
		return readOnlyProperties;
	}
	
	/**
	 * Returns an entity manager to be used by application.
	 * @return an entity manger to be used by application. If there is no persistence module, returns null
	 * @throws IllegalStateException if application is not running or application is running but persistence module were no loaded yet.
	 * @see WebApplication#getPersistenceModule() 
	 */
	public static EntityManager getEntityManager() throws IllegalStateException {
		throwIfNotRunning();
		
		if (persistenceModule != null) {
			if (persistenceLoaded) {
				return persistenceModule.getEntityManager();
			} else {
				throw new IllegalStateException("Persistence module were not loaded yet");
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Reports an error in the application. 
	 * If there is no error reporter, nothing happens. 
	 * 
	 * Calling this method before application fully-loaded will queue the
	 * operation and return immediately.
	 * 
	 * @param req erroneous HTTP request
	 * @param resp HTTP response
	 * @throws IllegalStateException if application is not running.
	 * @see WebApplication#getExceptionReporterModule() 
	 */
	public static void reportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) throws IllegalStateException {
		throwIfNotRunning();
		
		final HttpServletRequest _req = req;
		final HttpServletResponse _resp = resp;
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (errorReporterModule != null)
					errorReporterModule.reportErroneousRequest(_req, _resp);
			}
		};
		
		if (fullyLoaded)
			runnable.run();
		else
			invokeLater(runnable);
	}
	
	/**
	 * Logs a message in the application. 
	 * If there is no logging module, nothing happens.
	 * 
	 * Calling this method before application fully-loaded will queue the
	 * operation and return immediately.
	 * 
	 * @param logType message type
	 * @param message message to be logged
	 * @throws IllegalStateException if application is not running.
	 * @see WebApplication#getLoggingModule() 
	 */
	public static void log(String logType, String message) throws IllegalStateException {
		throwIfNotRunning();
		
		final String _logType = logType;
		final String _message = message;
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (loggingModule != null)
					loggingModule.log(_logType, _message);
			}
		};
		
		if (fullyLoaded) {
			runnable.run();
		} else {
			invokeLater(runnable);
		}
	}
	
	/**
	 * Sends a message using SMTP module.
	 * If there is no SMTP module, nothing happens.
	 * 
	 * Calling this method before application fully-loaded will queue the
	 * operation and return immediately.
	 * 
	 * @param message message to be sent
	 * @throws IllegalStateException if application is not running.
	 * @see WebApplication#getSmtpModule()
	 */
	public static void sendMessage(Message message) throws IllegalStateException {
		throwIfNotRunning();
		
		final Message _message = message;
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				if (smtpModule != null)
					smtpModule.sendMessage(_message);
			}
		};
		
		if (fullyLoaded) {
			runnable.run();
		} else {
			invokeLater(runnable);
		}
	}
	// -------------------------------------------------------------------------
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	/** 
	 * Returns a boolean indicating if debug is enabled.
	 * @return a boolean indicating if debug messages shall be printed.
	 */
	protected boolean isDebugEnabled() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
	}
	
	/** @return the application name */
	protected abstract String getAppName();
	
	/** @return the application version **/
	protected abstract String getAppVersion();
	
	/** @return the defaultEnvironment used by application. Default implementation returns {@linkplain WebApplication#DEFAULT_ENVIRONMENT}. */
	protected String getDefaultEnvironment() {
		return DEFAULT_ENVIRONMENT;
	}
	
	
	// Persistence module ----------------------------------------------------------
	/** 
	 * Return the persistence module used by application. 
	 * Default implementation returns an instance of {@linkplain DefaultPersistenceModule}.
	 * @return the persistence module used by application.
	 */
	protected PersistenceModule getPersistenceModule() {
		return new DefaultPersistenceModule();
	}
	
	/** 
	 * Called after persistence module is initialized.
	 * Default implementation does nothing.
	 * This method will be called only if {@linkplain WebApplication#getPersistenceModule()} returns a non-null value.
	 */
	protected void onPersistenceModuleStart() {}
	
	/** 
	 * Called before persistence module shutdown.
	 * Default implementation does nothing.
	 * This method will be called only if {@linkplain WebApplication#getPersistenceModule()} returns a non-null value.
	 */
	protected void beforePersistenceModuleStop() {}
	// -------------------------------------------------------------------------

	// SMTP module ----------------------------------------------------------
	/** 
	 * Returns the SMTP module used by application. 
	 * Default implementation returns an instance of {@linkplain DefaultSmtpModule}.
	 * @return the SMTP module used by application.
	 */
	protected SmtpModule getSmtpModule() {
		return new DefaultSmtpModule();
	}
	
	/** 
	 * Called after SMTP module is initialized.
	 * Default implementation does nothing.
	 * This method will be called only if {@linkplain WebApplication#getSmtpModule()} returns a non-null value.
	 */
	protected void onSmtpModuleStart() {}
	
	/**
	 * Called before SMTP module shutdown.
	 * Default implementation does nothing.
	 * This method will be called only if {@linkplain WebApplication#getSmtpModule()} returns a non-null value.
	 */
	protected void beforeSmtpModuleStop() {}
	// -------------------------------------------------------------------------
	
	// Error reporter module ---------------------------------------------------
	/**
	 * Return the error reporter module used by application. 
	 * Default implementation returns an instance off {@linkplain DefaultExceptionReporterModule}.
	 * @return the error reporter module used by application.
	 */
	protected ExceptionReporterModule getExceptionReporterModule() {
		return new DefaultExceptionReporterModule();
	}
	
	/**
	 * Called after error reporter module is initialized.
	 * Default implementation does nothing.
	 * This method will be called only if {@link WebApplication#getExceptionReporterModule()} returns a non-null value.
	 */
	protected void onErrorReporterModuleStart() {}
	
	/** 
	 * Called before error reporter module shutdown.
	 * Default implementation does nothing.
	 * This method will be called only if {@link WebApplication#getExceptionReporterModule()} returns a non-null value.
	 */
	protected void beforeErrorReporterModuleStop() {}
	// -------------------------------------------------------------------------
	
	// Logging module ----------------------------------------------------------
	/** 
	 * Returns the logging module used by application. 
	 * Default implementation returns an instance of {@linkplain DefaultLoggingModule}.
	 * @return the logging module used by application.
	 */
	protected LoggingModule getLoggingModule() {
		return new DefaultLoggingModule();
	}
	
	/** 
	 * Called after logging module is initialized.
	 * Default implementation does nothing.
	 * This method will be called only if {@linkplain WebApplication#getLoggingModule()} returns a non-null value.
	 */
	protected void onLogginModuleStart() {}
	
	/**
	 * Called before logging module shutdown.
	 * Default implementation does nothing.
	 * This method will be called only if {@linkplain WebApplication#getLoggingModule()} returns a non-null value.
	 */
	protected void beforeLoggingModuleStop() {}
	// -------------------------------------------------------------------------
	
	private void loadSettings() throws IOException {
		String strDelimiter = environment.equals(DEFAULT_ENVIRONMENT) ? "" : SETTINGS_FILENAME_DELIMITER;
		String strEnvironment = environment.equals(DEFAULT_ENVIRONMENT) ? "" : environment;
		
		File settingsFile = new File(appFolder, SETTINGS_FILENAME_PREFIX + strDelimiter + strEnvironment + SETTINGS_FILENAME_SUFFIX);
		Properties tmpProperties;

		if (settingsFile.exists()) {
			debug("Loading settings file...");

			// Load settings from file...
			properties.load(settingsFile);

			// Persistence module: put default settings when there is no definition...
			if (persistenceModule != null) {
				tmpProperties = persistenceModule.getDefaultSettings();

				if (tmpProperties != null)
					properties.append(tmpProperties, true);
			}
			
			// SMTP module: put default settings when there is no definition...
			if (smtpModule != null) {
				tmpProperties = smtpModule.getDefaultSettings();

				if (tmpProperties != null)
					properties.append(tmpProperties, true);
			}

			// Error reporter module: put default settings when there is no definition...
			if (errorReporterModule != null) {
				tmpProperties = errorReporterModule.getDefaultSettings();

				if (tmpProperties != null)
					properties.append(tmpProperties, true);
			}

		} else {
			boolean addedSettings = false;
			
			// Persistence module: Loading defaults...
			if (persistenceModule != null) {
				tmpProperties = persistenceModule.getDefaultSettings();

				if (tmpProperties != null) {
					properties.addComment("Persistence settings==========================================================");
					properties.append(tmpProperties);
					properties.addComment("==============================================================================");
					addedSettings = true;
				}
			}
			
			// From now, non-getter modules can be loaded in any sequence...
			
			// SMTP module: Loading defaults...
			if (smtpModule != null) {
				tmpProperties = smtpModule.getDefaultSettings();

				if (tmpProperties != null) {
					if (addedSettings)
						properties.addEmptyLine();
					
					properties.addComment("SMTP settings=================================================================");
					properties.append(tmpProperties);
					properties.addComment("==============================================================================");
					addedSettings = true;
				}
			}

			// Error reporter module: Loading defaults...
			if (errorReporterModule != null) {
				tmpProperties = errorReporterModule.getDefaultSettings();

				if (tmpProperties != null) {
					if (addedSettings)
						properties.addEmptyLine();
					
					properties.addComment("Error reporter settings ======================================================");
					properties.append(tmpProperties);
					properties.addComment("==============================================================================");
					addedSettings = true;
				}
			}

			// Storing in settings file...
			if (!properties.isEmpty()) {
				debug("Creating default settings file...");
				properties.store(settingsFile);
			}
		}
	}
	
	/** 
	 * Puts application into running state.
	 * In a web environment, this method is intended to be called by 
	 * 'contextInitialized' in application's {@linkplain ServletContextListener context listener}. 
	 */
	public final void start() {
		if (!isRunning()) {
			running = true;
			WebApplication.enableDebug = isDebugEnabled();
			
			debug("====== AGAPSYS WEB TOOLKIT INITIALIZATION ======");
			appName = getAppName();
			if (appName == null || appName.trim().isEmpty())
				throw new IllegalStateException("Missing application name");
			
			appName = appName.trim();
			
			appVersion = getAppVersion();
			if (appVersion == null || appVersion.trim().isEmpty())
				throw new IllegalStateException("Missing application version");
			
			appVersion = appVersion.trim();
			
			environment = getDefaultEnvironment();
			if (environment == null || environment.trim().isEmpty())
				throw new IllegalStateException("Missing environment");
			
			environment = environment.trim();
			
			debug("Environment set: %s", environment);
			
			appFolder = FileUtils.getOrCreateFolder(new File(FileUtils.USER_HOME, "." + appName).getAbsolutePath());

			persistenceModule   = getPersistenceModule();
			smtpModule          = getSmtpModule();
			errorReporterModule = getExceptionReporterModule();
			loggingModule       = getLoggingModule();
			
			try {
				loadSettings();
				settingsLoaded = true;
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			
			if (persistenceModule != null) {
				debug("Starting persistence module...");
				persistenceModule.start();
				onPersistenceModuleStart();
				persistenceLoaded = true;
			}
			
			if (smtpModule != null) {
				debug("Starting SMTP module...");
				smtpModule.start();
				onSmtpModuleStart();
			}
			
			if (errorReporterModule != null) {
				debug("Starting error reporter module...");
				errorReporterModule.start();
				onErrorReporterModuleStart();
			}
			
			if (loggingModule != null) {
				debug("Starting logging module...");
				loggingModule.start();
				onLogginModuleStart();
			}
			
			processEventQueue(); // <-- Processes all pending events genereted due to cross-module calls using an unloaded-module
			onApplicationStart();
			fullyLoaded = true;
			debug("====== AGAPSYS WEB TOOLKIT IS READY! ======");
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		start();
	}
	
	/**
	 * Called after application is initialized.
	 * Default implementation does nothing.
	 */
	protected void onApplicationStart() {}
	
	/**
	 * Forces application shutdown.
	 * In a web environment, this method is intended to be called by 
	 * 'contextDestroyed' in application's {@linkplain ServletContextListener context listener}.
	 */
	public final void stop() {
		if (isRunning()) {
			debug("====== AGAPSYS WEB TOOLKIT SHUTDOWN ======");
			beforeApplicationShutdown();
			
			// 1) Module pre-shutdown. Sequence is irrelevant. Cross-module calls are allowed
			if (loggingModule != null) {
				debug("Shutting down logging module...");
				beforeLoggingModuleStop();
			}
						
			if (errorReporterModule != null) {
				debug("Shutting down error reporter module...");
				beforeErrorReporterModuleStop();
			}
			
			if (smtpModule != null) {
				debug("Shutting down SMTP module");
				beforeSmtpModuleStop();
			}
			
			if (persistenceModule != null) {
				debug("Shutting down persistence module...");
				beforePersistenceModuleStop();
			}
			
			// 2) Actual module shutdown...
			if (loggingModule != null)
				loggingModule.stop();
			
			if (errorReporterModule != null)
				errorReporterModule.stop();
			
			if (smtpModule != null)
				smtpModule.stop();
			
			if (persistenceModule != null)
				persistenceModule.stop(); // persistence module must be the last module to be shutted down
			
			fullyLoaded = false;
			persistenceLoaded = false;
			settingsLoaded = false;
			running = false;
			
			debug("====== AGAPSYS WEB TOOLKIT WAS SHUTTED DOWN! ======");
		}
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		stop();
	}
	
	/**
	 * Called before application shutdown.
	 * Default implementation does nothing.
	 */
	protected void beforeApplicationShutdown() {}
	// =========================================================================
}