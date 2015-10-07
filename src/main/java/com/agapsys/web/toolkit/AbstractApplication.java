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
import com.agapsys.web.toolkit.utils.PropertyGroup;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletContextEvent;

/** 
 * Web application.
 * This class is not thread-safe
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractApplication  {
	// CLASS SCOPE =============================================================
	public static final String DEFAULT_ENVIRONMENT = "production";
	
	private static final String SETTINGS_FILENAME_PREFIX    = "application";
	private static final String SETTINGS_FILENAME_SUFFIX    = ".properties";
	private static final String SETTINGS_FILENAME_ENVIRONMENT_DELIMITER = "-";
	
	public static final String LOG_TYPE_ERROR   = "error";
	public static final String LOG_TYPE_INFO    = "info";
	public static final String LOG_TYPE_WARNING = "warning";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final Map<Class<? extends AbstractModule>, AbstractModule> moduleMap     = new LinkedHashMap<>();
	private final List<AbstractModule>                                 loadedModules = new LinkedList<>();
	private final Properties                                           properties    = new Properties();
	private final Set<Class<? extends AbstractModule>>                 moduleSet     = new LinkedHashSet<>();
	
	private File    appDirectory = null;
	private boolean running      = false;
	
	/** @return a boolean indicating if application is running. */
	public final boolean isRunning() {
		return running;
	}
	
	/**
	 * Log application messages.
	 * Default implementation just prints to console.
	 * @param logType log type
	 * @param message message to be logged
	 */
	public void log(String logType, String message) {
		Console.println(String.format("[%s] [%s] %s", DateUtils.getLocalTimestamp(), logType, message));
	}
	
	/** @return a boolean indicating if debug messages shall be printed. */
	protected boolean isDebugEnabled() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("-agentlib:jdwp") > 0;
	}
	
	/** @return settings filename prefix. */
	protected String getSettingsFilenamePrefix() {
		return SETTINGS_FILENAME_PREFIX;
	}
	
	/** @return settings filename suffix. */
	protected String getSettingsFilenameSuffix() {
		return SETTINGS_FILENAME_SUFFIX;
	}
	
	/** @return settings filename environment delimiter. */
	protected String getSettingsFilenameEnvironmentDelimiter() {
		return SETTINGS_FILENAME_ENVIRONMENT_DELIMITER;
	}
	
	/** 
	 * Prints debug messages if debug is enabled.
	 * @param message message to be printed
	 * @param args arguments if message is a formatted string
	 * @see AbstractApplication#isDebugEnabled()
	 * @see String#format(String, Object...)
	 */
	protected final void debug(String message, Object...args) {
		if (isDebugEnabled())
			Console.printlnf(message, args);
	}
	
	/** @return the application name */
	public abstract String getName();
	
	/** @return the application version **/
	public abstract String getVersion();
	
	/** @return a boolean indicating if application folder shall be created if it does not exist. Default implementation returns true. */
	protected boolean isDirectoryCreationEnabled() {
		return true;
	}
	
	/** @return the path of application directory. */
	protected String getDirectoryPath() {
		File d = new File(FileUtils.USER_HOME, "." + getName());
		return d.getAbsolutePath();
	}
	
	/** @return the directory where application stores resources outside application context in servlet container.*/
	public final File getDirectory() {
		if (appDirectory == null) {
			String directoryPath = getDirectoryPath();
			
			appDirectory = new File(directoryPath);

			if (!appDirectory.exists() && isDirectoryCreationEnabled())
				appDirectory = FileUtils.getOrCreateDirectory(directoryPath);
		}
		
		return appDirectory;
	}
	
	/** @return the name of the currently running environment. Default implementation return {@linkplain AbstractApplication#DEFAULT_ENVIRONMENT} */
	public String getEnvironment() {
		return DEFAULT_ENVIRONMENT;
	}
	
	/**
	 * Registers a module with this application.
	 * This method shall be called before application is running
	 * @see AbstractApplication#beforeApplicationStart()
	 * @param moduleClass module class to be registered
	 */
	public final void registerModule(Class<? extends AbstractModule> moduleClass) {
		if (isRunning())
			throw new IllegalStateException("Cannot add a module to a running application");
		
		if (moduleClass == null)
			throw new IllegalArgumentException("Null module class");
		
		if (!moduleSet.add(moduleClass))
			throw new IllegalArgumentException("Module already registered: " + moduleClass.getName());
	}
	
	/** Unregisters all modules associated with this application. Application cannot be running! */
	public final void unregisterModules() {
		if (isRunning())
			throw new IllegalStateException("Cannot unregister modules in a running application");
		
		moduleSet.clear();
	}
	
	/** 
	 * Returns a module registered with this application.
	 * @param moduleClass module class
	 * @return registered module instance or null if there is no such module.
	 */
	public final AbstractModule getModuleInstance(Class<? extends AbstractModule> moduleClass) {
		return moduleMap.get(moduleClass);
	}
	
	/** @return application properties. */
	public final Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns application default settings.
	 * @return application default settings. Default implementation returns null
	 */
	protected Properties getDefaultProperties() {
		return null;
	}
	
	
	/** @return a boolean indicating if a default settings file shall be created if it does not exist. Default implementation returns true. */
	protected boolean isPropertiesFileCreationEnabled() {
		return true;
	}
	
	/** @return a boolean indicating if properties shall be loaded from a settings file. Default implementation returns true. */
	protected boolean isPropertiesFileLoadingEnabled() {
		return true;
	}
	
	/** 
	 * Load application settings.
	 * @throws IOException if there is an error reading settings file.
	 */
	private void loadSettings() throws IOException {
		// Priority: (1) Loaded from file, (2) getDefaultProperties, (3) modules default properties
		
		properties.clear();

		File settingsFile = null;
		if (isPropertiesFileLoadingEnabled()) {
			String environment = getEnvironment();

			String strDelimiter   = environment.equals(DEFAULT_ENVIRONMENT) ? "" : getSettingsFilenameEnvironmentDelimiter();
			String strEnvironment = environment.equals(DEFAULT_ENVIRONMENT) ? "" : environment;

			settingsFile = new File(getDirectory(), getSettingsFilenamePrefix() + strDelimiter + strEnvironment + getSettingsFilenameSuffix());

			if (settingsFile.exists()) {
				debug("\tLoading settings file...");

				try (FileInputStream fis = new FileInputStream(settingsFile)) {
					properties.load(fis);
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
		for (Map.Entry<Class<? extends AbstractModule>, AbstractModule> entry : moduleMap.entrySet()) {
			
			Class<? extends AbstractModule> moduleClass    = entry.getKey();
			AbstractModule                  moduleInstance = entry.getValue();
			
			Properties defaultModuleProperties = moduleInstance.getDefaultSettings();
			
			if (defaultModuleProperties != null && !defaultModuleProperties.isEmpty()) {
				propertyGroups.add(new PropertyGroup(defaultModuleProperties, moduleClass.getName()));
				
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
			debug("\tCreating default settings file...");
			PropertyGroup.writeToFile(settingsFile, propertyGroups);
		}
	}	

	/** 
	 * Creates a instance of given module
	 * @param moduleClass module class
	 * @return module instance.
	 */
	private AbstractModule instantiateModule(Class<? extends AbstractModule> moduleClass)  {
		try {
			return moduleClass.getConstructor(AbstractApplication.class).newInstance(this);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Starts a module and all the dependencies
	 * @param moduleClass module class to be initialized
	 * @param mandatory defines if given module is mandatory
	 * @param callerModules list of recursive callers (initial call may be null)
	 */
	private void startModule(Class<? extends AbstractModule> moduleClass, boolean mandatory, List<Class<? extends AbstractModule>> callerModules)  {
		if (callerModules == null)
			callerModules = new LinkedList<>();
		
		AbstractModule moduleInstance = getModuleInstance(moduleClass);
		
		if (moduleInstance == null && mandatory)
			throw new RuntimeException("Mandatory module not registered: " + moduleClass);
		
		if (moduleInstance == null && !mandatory)
			debug("\tOptional module not found: %s", moduleClass);
		
		if (moduleInstance != null && !moduleInstance.isRunning()) {
			
			if (callerModules.contains(moduleClass))
				throw new RuntimeException("Cyclic dependency on module: " + moduleClass);

			callerModules.add(moduleClass);
			
			Set<Class<? extends AbstractModule>> mandatoryDependencies = moduleInstance.getMandatoryDependencies();
			Set<Class<? extends AbstractModule>> optionalDependencies  = moduleInstance.getOptionalDependencies();

			if (mandatoryDependencies == null)
				mandatoryDependencies = new LinkedHashSet<>();

			if (optionalDependencies == null)
				optionalDependencies = new LinkedHashSet<>();

			String moduleErrString = "Module is mandatory and optional at the same time: %s (%s)";

			for (Class<? extends AbstractModule> mandatoryModuleClass : mandatoryDependencies) {
				if (optionalDependencies.contains(mandatoryModuleClass))
					throw new RuntimeException(String.format(moduleErrString, mandatoryModuleClass.getName(), moduleClass.getName()));
			}

			for (Class<? extends AbstractModule> optionalModuleClass : optionalDependencies) {
				if (mandatoryDependencies.contains(optionalModuleClass))
					throw new RuntimeException(String.format(moduleErrString, optionalModuleClass.getName(), moduleClass.getName()));
			}
			
			// Load mandatory modules
			for (Class<? extends AbstractModule> mandatoryModuleClass : mandatoryDependencies) {
				startModule(mandatoryModuleClass, true, callerModules);
			}
			
			// Load optional modules
			for (Class<? extends AbstractModule> optionalModuleClass : optionalDependencies) {
				startModule(optionalModuleClass, false, callerModules);
			}

			moduleInstance.start();
			debug("\tModule initialized: %s", moduleClass.getName());

			loadedModules.add(moduleInstance);
		}
	}
	
	/** Shutdown initialized modules in appropriate sequence. */
	private void shutdownModules() {
		for (int i = loadedModules.size() - 1; i >= 0; i--) {
			loadedModules.get(i).stop();
		}
	}
	
	/** 
	 * Puts application into running state.
	 * If application is already running, nothing happens
	 * In a web environment, this method will be called by {@linkplain AbstractWebApplication#contextInitialized(ServletContextEvent)}
	 */
	public final void start() {
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
			if (environment != null)
				environment = environment.trim();
			if (environment == null || environment.isEmpty())
				throw new IllegalStateException("Missing environment");
			
			debug("====== AGAPSYS WEB TOOLKIT INITIALIZATION: %s (%s) ======", name, environment);
			beforeApplicationStart();
			
			// Instantiate all modules...
			for (Class<? extends AbstractModule> moduleClass : moduleSet) {
				moduleMap.put(moduleClass, instantiateModule(moduleClass));
			}

			try {
				debug("Loading settings...");
				loadSettings();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			// Starts all modules
			if (moduleMap.size() > 0) {
				debug("Starting modules...");
				for (Map.Entry<Class<? extends AbstractModule>, AbstractModule> entry : moduleMap.entrySet()) {
					if (!entry.getValue().isRunning()) {
						startModule(entry.getKey(), true, null);
					}
				}
			}
			
			afterApplicationStart();
			running = true;
			debug("====== AGAPSYS WEB TOOLKIT IS READY! ======");
		}
	}
	
	/** 
	 * Called before application is initialized.
	 * Default implementation does nothing. This is the place to register modules with the application.
	 */
	protected void beforeApplicationStart() {}
	
	/**
	 * Called after application is initialized.
	 * Default implementation does nothing.
	 */
	protected void afterApplicationStart() {}
	
	/**
	 * Forces application shutdown.
	 * If application is not running, nothing happens.
	 * In a web environment, this method will be called by {@linkplain AbstractWebApplication#contextDestroyed(ServletContextEvent)}
	 */
	public final void stop() {
		if (isRunning()) {
			debug("====== AGAPSYS WEB TOOLKIT SHUTDOWN ======");
			beforeApplicationStop();
			
			shutdownModules();
			
			// Final members...
			moduleMap.clear();
			loadedModules.clear();
			properties.clear();
			moduleSet.clear();
			
			//Non-final members
			appDirectory = null;
			running = false;
			
			afterApplicationStop();
			debug("====== AGAPSYS WEB TOOLKIT WAS SHUTTED DOWN! ======");
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
	// =========================================================================
}