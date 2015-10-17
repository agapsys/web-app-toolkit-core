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
	private static final String PROPERTIES_FILENAME = "application.properties";
	
	private static final String PROPERTIES_ENV_ENCLOSING = "[]";
	
	public static enum LogType {
		INFO,
		WARNING,
		ERROR;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final Map<Class<? extends AbstractModule>, AbstractModule> moduleMap      = new LinkedHashMap<>();
	private final List<AbstractModule>                                 loadedModules  = new LinkedList<>();
	private final Properties                                           properties     = new Properties();
	private final Set<Class<? extends AbstractModule>>                 moduleClassSet = new LinkedHashSet<>();
	
	private File    appDirectory = null;
	private boolean running      = false;

	
	/**
	 * Return a boolean indicating if application is running.
	 * @return a boolean indicating if application is running.
	 */
	public final boolean isRunning() {
		return running;
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
	 * Returns application properties filename.
	 * @return application properties.
	 */
	protected String getPropertiesFilename() {
		return PROPERTIES_FILENAME;
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
	 * Returns a boolean indicating if application folder creation is enabled.
	 * @return a boolean indicating if application folder shall be created if it does not exist. Default implementation returns true.
	 */
	protected boolean isDirectoryCreationEnabled() {
		return true;
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
	public final File getDirectory() {
		if (appDirectory == null) {
			String directoryPath = getDirectoryAbsolutePath();
			
			appDirectory = new File(directoryPath);

			if (!appDirectory.exists() && isDirectoryCreationEnabled())
				appDirectory = FileUtils.getOrCreateDirectory(directoryPath);
		}
		
		return appDirectory;
	}
	
	/**
	 * Returns the running environment.
	 * @return the name of the currently running environment. Default implementation return null
	 */
	public String getEnvironment() {
		return null;
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
		
		if (!moduleClassSet.add(moduleClass))
			throw new IllegalArgumentException("Module already registered: " + moduleClass.getName());
	}
	
	/** Unregisters all modules associated with this application. Application cannot be running! */
	public final void unregisterModules() {
		if (isRunning())
			throw new IllegalStateException("Cannot unregister modules in a running application");
		
		moduleClassSet.clear();
	}
	
	/** 
	 * Returns a module registered with this application.
	 * @param moduleClass module class
	 * @return registered module instance or null if there is no such module.
	 */
	public final AbstractModule getModuleInstance(Class<? extends AbstractModule> moduleClass) {
		return moduleMap.get(moduleClass);
	}
	
	/** 
	 * Returns application properties.
	 * @return application properties. 
	 */
	public final Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns application default properties.
	 * @return application default properties. Default implementation returns null
	 */
	protected Properties getDefaultProperties() {
		return null;
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
		
		properties.clear();

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
			log(LogType.INFO, "Creating default settings file...");
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
			log(LogType.WARNING, "Optional module not found: %s", moduleClass);
		
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
			log(LogType.INFO, "Initialized module: %s", moduleClass.getName());

			loadedModules.add(moduleInstance);
		}
	}
	
	/** Shutdown initialized modules in appropriate sequence. */
	private void shutdownModules() {
		if (loadedModules.size() > 0)
			log(LogType.INFO, "Stopping modules...");

		for (int i = loadedModules.size() - 1; i >= 0; i--) {
			AbstractModule module = loadedModules.get(i);
			module.stop();
			log(LogType.INFO, "Shutted down module: %s", module.getClass().getName());
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
			if (environment != null) {
				environment = environment.trim();
				if (environment.isEmpty())
					environment = null;
			}
			
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT INITIALIZATION: %s%s ======", name, environment == null ? "" : String.format(" (%s)", environment));
			beforeApplicationStart();
			
			// Instantiate all modules...
			for (Class<? extends AbstractModule> moduleClass : moduleClassSet) {
				moduleMap.put(moduleClass, instantiateModule(moduleClass));
			}

			try {
				log(LogType.INFO, "Loading settings...");
				loadSettings();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			// Starts all modules
			if (moduleMap.size() > 0) {
				log(LogType.INFO, "Starting modules...");
				for (Map.Entry<Class<? extends AbstractModule>, AbstractModule> entry : moduleMap.entrySet()) {
					if (!entry.getValue().isRunning()) {
						startModule(entry.getKey(), true, null);
					}
				}
			}
			
			afterApplicationStart();
			running = true;
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT IS READY! ======");
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
			
			log(LogType.INFO, "====== AGAPSYS WEB TOOLKIT SHUTDOWN: %s (%s) ======", getName(), getEnvironment());
			beforeApplicationStop();
			
			shutdownModules();
			
			// Final members...
			moduleMap.clear();
			loadedModules.clear();
			properties.clear();
			moduleClassSet.clear();
			
			//Non-final members
			appDirectory = null;
			running = false;
			
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
	// =========================================================================
}