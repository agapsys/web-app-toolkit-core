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

import com.agapsys.utils.console.Console;
import com.agapsys.web.toolkit.utils.DateUtils;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.HttpUtils;
import com.agapsys.web.toolkit.utils.PropertyGroup;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * Represents a web application.
 * This class is not thread-safe
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractWebApplication implements ServletContextListener {
	// CLASS SCOPE =============================================================
	public static enum LogType {
		INFO,
		WARNING,
		ERROR;
	}

	// Global settings ---------------------------------------------------------
	/** Defines if application is disabled. When an application is disabled, all requests are ignored and a {@linkplain HttpServletResponse#SC_SERVICE_UNAVAILABLE} is sent to the client. */
	public static final String KEY_APP_DISABLE = "com.agapsys.webtoolkit.appDisable";
	
	/** Defines a comma-delimited list of allowed origins for this application or '*' for any origin. If an origin is not accepted a {@linkplain HttpServletResponse#SC_FORBIDDEN} is sent to the client. */
	public static final String KEY_APP_ALLOWED_ORIGINS = "com.agapsys.webtoolkit.allowedOrigins";
	
	public static final boolean DEFAULT_APP_DISABLED        = false;
	public static final String  DEFAULT_APP_ALLOWED_ORIGINS = "*";
	
	private static final String ORIGIN_DELIMITER = ",";
	// -------------------------------------------------------------------------
	
	private static final String PROPERTIES_FILENAME = "application.properties";
	
	private static final String PROPERTIES_ENV_ENCLOSING = "[]";
		
	private static AbstractWebApplication singleton = null;
	
	public static AbstractWebApplication getInstance() {
		if (singleton == null)
			throw new IllegalStateException("Web application is not running");
		
		return singleton;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final Set<Class<? extends AbstractModule>> moduleClassSet = new LinkedHashSet<>();
	
	private final Properties       properties       = new Properties();
	private final SingletonManager singletonManager = new SingletonManager();
	
	private File     appDirectory;
	private boolean  running;
	private boolean  disabled;
	private String[] allowedOrigins;

	public AbstractWebApplication() {
		reset();
	}
	
	/** Resets application state. */
	private void reset() {
		moduleClassSet.clear();
		properties.clear();
		singletonManager.clear();
		
		appDirectory = null;
		running = false;
		disabled = DEFAULT_APP_DISABLED;
		allowedOrigins = new String[] {DEFAULT_APP_ALLOWED_ORIGINS};
	}
	
	/**
	 * Logs application messages.
	 * Default implementation just prints to console.
	 * @param message message to be logged
	 * @param args message parameters (see {@linkplain String#format(String, Object...)})
	 */
	public void log(LogType logType, String message, Object...args) {
		Console.printlnf("%s [%s] %s", DateUtils.getLocalTimestamp(), logType.name(), String.format(message, args));
	}
	
	
	/**
	 * Return a boolean indicating if application is running.
	 * @return a boolean indicating if application is running.
	 */
	public boolean isRunning() {
		return running;
	}
	
	/**
	 * Returns a boolean indicating if application is disabled.
	 * @return a boolean indicating if application is disabled.
	 */
	public boolean isDisabled() {
		return disabled;
	}
	
	/**
	 * Returns a boolean indicating if given request is allowed to proceed.
	 * @param req HTTP request
	 * @return boolean indicating if given request is allowed to proceed.
	 */
	public boolean isOriginAllowed(HttpServletRequest req) {
		boolean isOriginAllowed = allowedOrigins.length == 1 && allowedOrigins[0].equals(DEFAULT_APP_ALLOWED_ORIGINS);
		
		if (isOriginAllowed)
			return true;
		
		String originIp = HttpUtils.getOriginIp(req);
			
		for (String allowedOrigin : allowedOrigins) {
			if (allowedOrigin.equals(originIp))
				return true;
		}
		
		return false;
	}
	
	
	/**
	 * Returns application name.
	 * @return the application name 
	 */
	public abstract String getName();
	
	/** 
	 * Returns application version.
	 * @return the application version 
	 */
	public abstract String getVersion();
	
	/**
	 * Returns the running environment.
	 * @return the name of the currently running environment. Default implementation return null
	 */
	public String getEnvironment() {
		return null;
	}
	
	
	/**
	 * Returns application properties filename.
	 * @return application properties.
	 */
	protected String getPropertiesFilename() {
		return PROPERTIES_FILENAME;
	}
	
	/**
	 * Returns the absolute path of application folder.
	 * @return the path of application directory.
	 */
	protected String getDirectoryAbsolutePath() {
		File d = new File(FileUtils.USER_HOME, "." + getName());
		return d.getAbsolutePath();
	}
	
	/**
	 * Returns a file representing application directory
	 * @return the directory where application stores resources outside application context in servlet container.
	 */
	public File getDirectory() {
		if (appDirectory == null) {
			String directoryPath = getDirectoryAbsolutePath();
			
			appDirectory = new File(directoryPath);

			if (!appDirectory.exists())
				appDirectory = FileUtils.getOrCreateDirectory(directoryPath);
		}
		
		return appDirectory;
	}
		
	/**
	 * Registers a singleton with application.
	 * @param moduleClass module class to be registered
	 */
	public void registerModule(Class<? extends AbstractModule> moduleClass) {
		if (isRunning())
			throw new IllegalStateException("Cannot add a module to a running application");
		
		if (!moduleClassSet.add(moduleClass)) {
			throw new IllegalArgumentException("Module class already registered: " + moduleClass.getName());
		}
	}
	
	public void registerModuleAlias(Class<? extends AbstractModule> moduleAlias, Class<? extends AbstractModule> targetClass) {
		singletonManager.registerSingletonAlias(moduleAlias, targetClass);
		
		if (!moduleClassSet.contains(moduleAlias)) {
			moduleClassSet.add(moduleAlias);
		}
	}
	
	/**
	 * Returns a singleton registered with this application.
	 * @param id singleton ID
	 * @return singleton instance
	 */
	public AbstractModule getModule(String id) {
		return (AbstractModule) singletonManager.getSingleton(id);
	}
	
	/** 
	 * Returns application properties.
	 * @return application properties. 
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns application default properties.
	 * @return application default properties. Default implementation returns null
	 */
	protected Properties getDefaultProperties() {
		Properties props = new Properties();
		props.setProperty(KEY_APP_DISABLE,         "" + DEFAULT_APP_DISABLED);
		props.setProperty(KEY_APP_ALLOWED_ORIGINS, DEFAULT_APP_ALLOWED_ORIGINS);
		return props;
	}
	
	/** 
	 * Returns a boolean indicating if a default properties file shall be created if there is none.
	 * @return a boolean indicating if a default settings file shall be created if it does not exist. Default implementation returns true.
	 */
	protected boolean isPropertiesFileCreationEnabled() {
		return true;
	}
	
	/**
	 * Returns a boolean if application shall read properties from its properties file.
	 * @return a boolean indicating if properties shall be loaded from a settings file. Default implementation returns true.
	 */
	protected boolean isPropertiesFileLoadingEnabled() {
		return true;
	}

	/** 
	 * Load application settings.
	 * @throws IOException if there is an error reading settings file.
	 */
	private void loadSettings() throws IOException {
		// Priority: (1) Loaded from file, (2) getDefaultProperties, (3) modules default properties
		File settingsFile = null;
		if (isPropertiesFileLoadingEnabled()) {
			String environment = getEnvironment();

			settingsFile = new File(getDirectory(), getPropertiesFilename());

			if (settingsFile.exists()) {
				log(LogType.INFO, "Loading settings file...");

				try (FileInputStream fis = new FileInputStream(settingsFile)) {
					Properties tmpProperties = new Properties();
					tmpProperties.load(fis);
					tmpProperties = PropertyGroup.getSubProperties(tmpProperties, environment, PROPERTIES_ENV_ENCLOSING);
					properties.putAll(tmpProperties);
				}
			}
		}
		
		// Apply application default properties (keeping existing)...
		Properties defaultProperties = getDefaultProperties();
		if (defaultProperties != null) {
			for (Map.Entry<Object, Object> entry : defaultProperties.entrySet()) {
				properties.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}

		// Apply modules default properties (keeping existing)...
		List<PropertyGroup> propertyGroups = new LinkedList<>();
		
		for (Class<? extends AbstractModule> moduleClass : moduleClassSet) {
			AbstractModule module = (AbstractModule) singletonManager.getSingleton(moduleClass);
			Properties defaultModuleProperties = module.getDefaultSettings();
			
			if (defaultModuleProperties != null && !defaultModuleProperties.isEmpty()) {
				propertyGroups.add(new PropertyGroup(defaultModuleProperties, module.getTitle()));
				
				for (Map.Entry<Object, Object> defaultEntry : defaultModuleProperties.entrySet()) {
					Object appPropertyValue = properties.putIfAbsent(defaultEntry.getKey(), defaultEntry.getValue());
					
					if (appPropertyValue != null) {
						// Property already contains given entry
						defaultModuleProperties.put(defaultEntry.getKey(), appPropertyValue);
					}
				}
			}
		}
		
		// Write properties to disk...
		if (settingsFile != null && !settingsFile.exists() && isPropertiesFileCreationEnabled()) {
			log(LogType.INFO, "Creating default settings file...");
			PropertyGroup.writeToFile(settingsFile, propertyGroups);
		}
	}	

	/** Starts modules. */
	private void startModules() {
		if (!moduleClassSet.isEmpty())
			log(LogType.INFO, "Starting modules...");
		
		for (Class<? extends AbstractModule> moduleClass : moduleClassSet) {
			AbstractModule module = (AbstractModule) singletonManager.getSingleton(moduleClass);
			module.start(this);
			log(LogType.INFO, "Module started: %s", module.getTitle());
		}
	}
	
	/** Shutdown loaded modules. */
	private void shutdownModules() {
		if (!moduleClassSet.isEmpty())
			log(LogType.INFO, "Stopping modules...");
		
		for (Class<? extends AbstractModule> moduleClass : moduleClassSet) {
			AbstractModule module = (AbstractModule) singletonManager.getSingleton(moduleClass);
			module.stop();
			log(LogType.INFO, "Shutted down module: %s", module.getTitle());
		}
	}
	
	/** 
	 * Puts application into running state.
	 * If application is already running, nothing happens
	 * In a web environment, this method will be called by {@linkplain AbstractWebApplication#contextInitialized(ServletContextEvent)}
	 */
	private void start() {
		if (!isRunning()) {
			String name = getName();
			if (name != null)
				name = name.trim();
			if (name == null || name.isEmpty())
				throw new IllegalStateException("Missing application name");
			
			if (!name.matches("^[a-zA-Z0-9\\-_]*$")) {
				throw new IllegalArgumentException("Invalid application name: " + name);
			}
			
			String version = getVersion();
			if (version != null)
				version = version.trim();
			if (version == null || version.isEmpty())
				throw new IllegalStateException("Missing application version");
			
			String environment = getEnvironment();
			if (environment != null) {
				environment = environment.trim();
				if (environment.isEmpty())
					environment = null;
			}
			
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT INITIALIZATION: %s%s ======", name, environment == null ? "" : String.format(" (%s)", environment));
			beforeApplicationStart();
			
			try {
				log(LogType.INFO, "Loading settings...");
				loadSettings();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			// Starts all modules
			startModules();
			
			afterApplicationStart();
			running = true;
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT IS READY! ======");
		}
	}
	
	/** 
	 * Called before application is initialized.
	 * Default implementation does nothing. This is the place to register modules and singletons with the application.
	 */
	protected void beforeApplicationStart() {}
	
	/** Called after application is initialized. */
	protected void afterApplicationStart() {
		disabled = Boolean.parseBoolean(getProperties().getProperty(KEY_APP_DISABLE, "" + DEFAULT_APP_DISABLED));
		allowedOrigins = getProperties().getProperty(KEY_APP_ALLOWED_ORIGINS, AbstractWebApplication.DEFAULT_APP_ALLOWED_ORIGINS).split(Pattern.quote(ORIGIN_DELIMITER));
		
		for (int i = 0; i < allowedOrigins.length; i++) {
			allowedOrigins[i] = allowedOrigins[i].trim();
		}
	}
	
	/**
	 * Forces application shutdown.
	 * If application is not running, nothing happens.
	 * In a web environment, this method will be called by {@linkplain AbstractWebApplication#contextDestroyed(ServletContextEvent)}
	 */
	private void stop() {
		if (isRunning()) {
			String environment = getEnvironment();
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT SHUTDOWN: %s%s ======", getName(), environment == null ? "" : String.format(" (%s)", environment));
			beforeApplicationStop();
			
			shutdownModules();
			reset();
			
			afterApplicationStop();
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT WAS SHUTTED DOWN! ======");
		}
	}
	
	/**
	 * Called before application shutdown.
	 * Default implementation does nothing.
	 */
	protected void beforeApplicationStop() {}
	
	/** 
	 * Called after application is stopped.
	 * Default implementation does nothing.
	 */
	protected void afterApplicationStop() {}
	
	@Override
	public final void contextInitialized(ServletContextEvent sce) {
		start();
		singleton = this;
	}

	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		stop();
		singleton = null;
	}
	// =========================================================================
}