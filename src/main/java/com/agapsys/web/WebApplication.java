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

import com.agapsys.web.PersistenceUnit.DbInitializer;
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
import com.agapsys.web.utils.Utils;

public abstract class WebApplication implements ServletContextListener {
	// CLASS SCOPE =============================================================
	public static enum LogType {
		INFO,
		WARNING,
		ERROR
	}
	
	public static final String DEFAULT_ENVIRONMENT = "production";
	public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";
		
	// State -------------------------------------------------------------------
	private static boolean       running             = false;
	private static String        environment         = null;
	private static String        appName             = null;
	private static File          appFolder           = null;
	private static String        appVersion          = null;
	private static String        persistenceUnitName = null;
	private static DbInitializer dbInitializer       = null;
	// -------------------------------------------------------------------------
	
	private static void throwIfRunning() throws IllegalStateException {
		if (isRunning())
			throw new IllegalStateException("Application is running");
	}
	
	// Application management methods ------------------------------------------
	/** @return a boolean indicating if environment is running. */
	public static boolean isRunning() {
		return running;
	}
	
	/** Starts the application. If application is already running, nothing happens. */
	public static void start() {
		if (!isRunning()) {
			Utils.printConsoleLog(String.format("========== Environment set: '%s' ==========", environment));
			Utils.printConsoleLog("Starting settings module...");
			SettingsModule.start();
			Utils.printConsoleLog("Starting logging module...");
			LoggingModule.start();
			Utils.printConsoleLog("Starting persistence module...");
			PersistenceModule.start(dbInitializer);
			Utils.printConsoleLog("Starting maintenance module...");
			MaintenanceModule.start();
			running = true;
		}
	}
	
	/** Stops the application. If application is not running, nothing happens. */
	public static void stop() {
		if (isRunning()) {
			Utils.printConsoleLog("Stopping logging module...");
			LoggingModule.stop();
			Utils.printConsoleLog("Stopping persistence module...");
			PersistenceModule.stop();
		}
	}
	// -------------------------------------------------------------------------
	
	// State management methods ------------------------------------------------
	public static String getName() throws IllegalStateException{
		return appName;
	}
	public static void setName(String appName) throws IllegalArgumentException, IllegalStateException {
		throwIfRunning();
		
		if (appName == null || appName.isEmpty())
			throw new IllegalArgumentException("Null/Empty name");
		
		WebApplication.appName = appName;
	}
	
	public static String getVersion() throws IllegalStateException {
		return appVersion;
	}
	public static void setVersion(String appVersion) throws IllegalArgumentException, IllegalStateException {
		throwIfRunning();
		
		if (appVersion == null || appVersion.isEmpty())
			throw new IllegalArgumentException("Null/Empty version");
		
		WebApplication.appVersion = appVersion;
	}
	
	public static String getPersistenceUnitName() throws IllegalStateException {
		return persistenceUnitName;
	}
	public static void setPersistenceUnitName(String peristenceUnitName) throws IllegalArgumentException, IllegalStateException {
		throwIfRunning();
		
		if (peristenceUnitName == null || peristenceUnitName.isEmpty()) 
			throw new IllegalArgumentException("Null/Empty peristenceUnitName");
		
		WebApplication.persistenceUnitName = peristenceUnitName;
	}
	
	/** @return the db initializer for the application. */
	public static DbInitializer getDbInitializer() {
		return dbInitializer;
	}
	
	/**
	 * Sets the db initializer for the application 
	 * @param dbInitializer db initializer or null if initialization is not required
	 * @throws IllegalStateException if application is running
	 */
	public static void setDbInitializer(DbInitializer dbInitializer) throws IllegalStateException {
		throwIfRunning();
		
		WebApplication.dbInitializer = dbInitializer;
	}
	
	/**
	 * Returns the folder where application stores external resources.
	 * @return the folder where application stores external resources.
	 * @throws IllegalStateException if application name is not set
	 */
	public static File getAppFolder() throws IllegalStateException {
		if (appName == null)
			throw new IllegalStateException("Missing application name");
		
		if (appFolder == null) {
			appFolder = FileUtils.getOrCreateFolder(new File(FileUtils.USER_HOME, "." + appName).getAbsolutePath());
		}
		
		return appFolder;
	}
	
	/** @return the environment set to the application. */
	public static String getEnvironment() {
		return environment;
	}
	
	/** 
	 * Sets the environment used by application.
	 * @param environment environment name
	 * @throws IllegalArgumentException if (environment == null || environment.isEmpty())
	 * @throws IllegalStateException if application is running
	 */
	public static void setEnvironment(String environment) throws IllegalArgumentException, IllegalStateException {
		throwIfRunning();

		if (environment == null || environment.isEmpty())
			throw new IllegalArgumentException("Null/Empty environment");
		
		WebApplication.environment = environment;
	}
	// -------------------------------------------------------------------------
	
	// Global application methods --------------------------------------------------
	public static void log(LogType type, String message) {
		LoggingModule.log(type, message);
	}
	
	public static void log (LogType type, HttpServletRequest request, String message) {
		LoggingModule.log(type, request, message);
	}
	
	public static void handleErrorRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		MaintenanceModule.handleErrorRequest(req, resp);
	}
	
	public static EntityManager getEntityManager() {
		return PersistenceModule.getEntityManager();
	}
	
	static Properties getProperties() {
		return SettingsModule.getProperties();
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
	
	protected String getDefaultPersistenceUnitName() {
		return DEFAULT_PERSISTENCE_UNIT_NAME;
	}
	
	/** 
	 * Returns the database initializer used by application.
	 * @return the database initializer used by application. 
	 * If an initialization is not required returns null. 
	 * Default implementation returns null.
	 */
	protected DbInitializer getDefaultDbInitializer() {
		return null;
	}
	
	@Override
	public final void contextInitialized(ServletContextEvent sce) {
		setName(getAppName());
		setVersion(getAppVersion());
		setPersistenceUnitName(getDefaultPersistenceUnitName());
		setDbInitializer(getDefaultDbInitializer());
		setEnvironment(getDefaultEnvironment());
		start();
	}

	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		stop();
	}
	// =========================================================================
}
