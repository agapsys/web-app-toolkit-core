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
import javax.servlet.ServletContextListener;

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
	private static final String SETTINGS_FILENAME_ENVIRONMENT_DELIMITER = "-";
	
	public static WebApplication singleton = null;
	
	public static boolean isRunning() {
		return singleton != null;
	}
	
	public static WebApplication getInstance() throws IllegalStateException {
		if (singleton == null)
			throw new IllegalStateException("Application is not running");
		
		return singleton;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final Map<String, Module> moduleMap          = new LinkedHashMap<>();
	private final List<Module>        loadedModules      = new LinkedList<>();
	private final Properties          properties         = new Properties();
	
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
	 * @see String#format(String, Object...)
	 * @see WebApplication#start()
	 */
	protected void debug(String message, Object...args) {
		if (isDebugEnabled())
			Console.printlnf(message, args);
	}
	
	/** @return the application name */
	public abstract String getName();
	
	/** @return the application version **/
	public abstract String getVersion();

	/** @return the folder where application stores resources outside application context in servlet container. Default implementation will create folder if it not exists */
	public File getFolder() {
		return FileUtils.getOrCreateFolder(new File(FileUtils.USER_HOME, "." + getName()).getAbsolutePath());
	}
	
	/** @return the name of the currently running environment. Default implementation return {@linkplain WebApplication#DEFAULT_ENVIRONMENT} */
	public String getEnvironment() {
		return DEFAULT_ENVIRONMENT;
	}
	
	/** @return the module classes used by this application. Default implementation returns null (no modules) */
	protected Map<String, Class<? extends Module>> getModuleClassMap() {
		return null;
	}
	
	/** 
	 * Returns a module registered with this application.
	 * @param moduleId module ID
	 * @return registered module instance or null if there is no such module.
	 */
	public final Module getModuleInstance(String moduleId) {
		return moduleMap.get(moduleId);
	}
	
	/** @return application properties. */
	public final Properties getProperties() {
		return properties;
	}
	
	/** 
	 * Load application settings.
	 * @throws IOException if there is an error reading settings file.
	 */
	private void loadSettings() throws IOException {
		properties.clear();
		
		String environment = getEnvironment();
		
		String strDelimiter   = environment.equals(DEFAULT_ENVIRONMENT) ? "" : getSettingsFilenameEnvironmentDelimiter();
		String strEnvironment = environment.equals(DEFAULT_ENVIRONMENT) ? "" : environment;
		
		File settingsFile = new File(getFolder(), getSettingsFilenamePrefix() + strDelimiter + strEnvironment + getSettingsFilenameSuffix());

		if (settingsFile.exists()) {
			debug("\tLoading settings file...");

			try (FileInputStream fis = new FileInputStream(settingsFile)) {
				properties.load(fis);
			}
		}

		List<PropertyGroup> propertyGroups = new LinkedList<>();
		for (Map.Entry<String, Module> entry : moduleMap.entrySet()) {
			Module moduleInstance = entry.getValue();
			
			Properties defaultModuleProperties = moduleInstance.getDefaultSettings();
			
			if (defaultModuleProperties != null && !defaultModuleProperties.isEmpty()) {
				propertyGroups.add(new PropertyGroup(defaultModuleProperties, moduleInstance.getDescription()));
				
				for (Map.Entry<Object, Object> defaultEntry : defaultModuleProperties.entrySet()) {
					properties.putIfAbsent(defaultEntry.getKey(), defaultEntry.getValue());
				}
			}
		}
		
		if (!settingsFile.exists()) {
			debug("\tCreating default settings file...");
			PropertyGroup.writeToFile(settingsFile, propertyGroups);
		}
	}	

	/** 
	 * Creates a instance of given module
	 * @param moduleClass module class
	 * @return module instance.
	 */
	private Module instantiateModule(Class<? extends Module> moduleClass)  {
		try {
			return moduleClass.getConstructor(WebApplication.class).newInstance(this);
		} catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	/**
	 * Starts a module and all the dependencies
	 * @param moduleId ID of the module to be initialized
	 * @param mandatory defines if given module is mandatory
	 * @param callerModules list of recursive callers (initial call may be null)
	 */
	private void startModule(String moduleId, boolean mandatory, List<String> callerModules)  {
		if (callerModules == null)
			callerModules = new LinkedList<>();
		
		Module moduleInstance = getModuleInstance(moduleId);
		
		if (moduleInstance == null && mandatory)
			throw new RuntimeException("Mandatory module not registered: " + moduleId);
		
		if (moduleInstance == null && !mandatory)
			debug("\tOptional module not found: %s", moduleId);
		
		if (moduleInstance != null && !moduleInstance.isRunning()) {
			
			if (callerModules.contains(moduleId))
				throw new RuntimeException("Cyclic dependency on module: " + moduleId);

			callerModules.add(moduleId);
			
			Set<String> mandatoryDependencies = moduleInstance.getMandatoryDependencies();
			Set<String> optionalDependencies = moduleInstance.getOptionalDependencies();

			if (mandatoryDependencies == null)
				mandatoryDependencies = new LinkedHashSet<>();

			if (optionalDependencies == null)
				optionalDependencies = new LinkedHashSet<>();

			String moduleErrString = "Module is mandatory and optional at the same time: %s (%s)";

			for (String mandatoryModuleId : mandatoryDependencies) {
				if (optionalDependencies.contains(mandatoryModuleId))
					throw new RuntimeException(String.format(moduleErrString, mandatoryModuleId, moduleId));
			}

			for (String optionalModuleId : optionalDependencies) {
				if (mandatoryDependencies.contains(optionalModuleId))
					throw new RuntimeException(String.format(moduleErrString, optionalModuleId, moduleId));
			}
			
			// Load mandatory modules
			for (String mandatoryModuleId : mandatoryDependencies) {
				startModule(mandatoryModuleId, true, callerModules);
			}
			
			// Load optional modules
			for (String optionalModuleId : optionalDependencies) {
				startModule(optionalModuleId, false, callerModules);
			}

			moduleInstance.start();
			debug("\tModule initialized: %s", moduleInstance.getDescription());

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
	 * In a web environment, this method is intended to be called by 
	 * 'contextInitialized' in application's {@linkplain ServletContextListener context listener}. 
	 * @throws IllegalStateException if application is already running;
	 */
	public final void start() throws IllegalStateException {
		if (singleton == null) {
			String name = getName();
			if (name != null)
				name = name.trim();
			if (name == null || name.isEmpty())
				throw new IllegalStateException("Missing application name");
			
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
			
			Map<String, Class<? extends Module>> moduleClassMap = getModuleClassMap();
			
			// Instantiate all modules...
			for (Map.Entry<String, Class<? extends Module>> entry : moduleClassMap.entrySet()) {
				moduleMap.put(entry.getKey(), instantiateModule(entry.getValue()));
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
				for (Map.Entry<String, Module> entry : moduleMap.entrySet()) {
					if (!entry.getValue().isRunning()) {
						startModule(entry.getKey(), true, null);
					}
				}
			}
			
			singleton = this;
			afterApplicationStart();
			debug("====== AGAPSYS WEB TOOLKIT IS READY! ======");
		} else {
			throw new IllegalStateException("Application is already running: " + singleton.getName());
		}
	}

	@Override
	public final void contextInitialized(ServletContextEvent sce) {
		start();
	}
	
	/** 
	 * Called before application is initialized.
	 * Default implementation does nothing.
	 */
	protected void beforeApplicationStart() {}
	
	/**
	 * Called after application is initialized.
	 * Default implementation does nothing.
	 */
	protected void afterApplicationStart() {}
	
	/**
	 * Forces application shutdown.
	 * In a web environment, this method is intended to be called by 
	 * 'contextDestroyed' in application's {@linkplain ServletContextListener context listener}.
	 */
	public final void stop() {
		if (singleton != null) {
			debug("====== AGAPSYS WEB TOOLKIT SHUTDOWN ======");
			beforeApplicationStop();
			
			shutdownModules();
			loadedModules.clear();
			moduleMap.clear();
			properties.clear();
			singleton = null;
			
			afterApplicationStop();
			debug("====== AGAPSYS WEB TOOLKIT WAS SHUTTED DOWN! ======");
		}
	}
	
	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		stop();
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