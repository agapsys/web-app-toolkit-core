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

import com.agapsys.web.toolkit.utils.DateUtils;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.HttpUtils;
import com.agapsys.web.toolkit.utils.PropertyGroup;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
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
	
	// Global settings ---------------------------------------------------------
	
	/** Defines if application is disabled. When an application is disabled, all requests are ignored and a {@linkplain HttpServletResponse#SC_SERVICE_UNAVAILABLE} is sent to the client. */
	public static final String KEY_APP_DISABLE = "com.agapsys.webtoolkit.appDisable";
	
	/** Defines a comma-delimited list of allowed origins for this application or '*' for any origin. If an origin is not accepted a {@linkplain HttpServletResponse#SC_FORBIDDEN} is sent to the client. */
	public static final String KEY_APP_ALLOWED_ORIGINS = "com.agapsys.webtoolkit.allowedOrigins";
	
	/** Defines the environment used by application. */
	public static final String KEY_ENVIRONMENT = "com.agapsys.webtoolkit.environment";
	
	public static final boolean DEFAULT_APP_DISABLED        = false;
	public static final String  DEFAULT_APP_ALLOWED_ORIGINS = "*";
	
	private static final String ORIGIN_DELIMITER = ",";
	// -------------------------------------------------------------------------
	
	private static final String PROPERTIES_FILENAME = "application.properties";
		
	private static AbstractWebApplication singleton = null;
	
	/**
	 * Return application running instance.
	 * @return application singleton instance. If an application is not running returns null
	 */
	public static AbstractWebApplication getRunningInstance() {
		return singleton;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final Map<Class<? extends Module>, Module>   moduleMap = new LinkedHashMap<>();
	private final Map<Class<? extends Service>, Service> serviceMap = new LinkedHashMap<>();
	private final List<Module> startedModules  = new LinkedList<>();
	private final Properties properties = new Properties();
	
	private File     appDirectory;
	private boolean  running;
	private boolean  disabled;
	private String[] allowedOrigins;


	public AbstractWebApplication() {
		reset();
	}
	
	/** Resets application state. */
	private void reset() {
		moduleMap.clear();
		serviceMap.clear();
		
		startedModules.clear();
		properties.clear();
		
		appDirectory = null;
		running = false;
		disabled = DEFAULT_APP_DISABLED;
		allowedOrigins = new String[] {DEFAULT_APP_ALLOWED_ORIGINS};
	}
	
	/**
	 * Logs application messages.
	 * Default implementation just prints to console.
	 * @param logType log message type
	 * @param message message to be logged
	 * @param args message parameters (see {@linkplain String#format(String, Object...)})
	 */
	public void log(LogType logType, String message, Object...args) {
		if (args.length > 0)
			message = String.format(message, args);
		
		System.out.println(String.format("%s [%s] %s", DateUtils.getLocalTimestamp(), logType.name(), message));
	}
	
	
	/**
	 * Return a boolean indicating if application is running.
	 * @return a boolean indicating if application is running.
	 */
	public final boolean isRunning() {
		return running;
	}
	
	/**
	 * Returns a boolean indicating if application is disabled.
	 * @return a boolean indicating if application is disabled.
	 */
	public boolean isDisabled() {
		if (!isRunning())
			throw new RuntimeException("Application is not running");
		
		return disabled;
	}
	
	/**
	 * Returns a boolean indicating if given request is allowed to proceed.
	 * @param req HTTP request
	 * @return boolean indicating if given request is allowed to proceed.
	 */
	public boolean isOriginAllowed(HttpServletRequest req) {
		if (!isRunning())
			throw new RuntimeException("Application is not running");
		
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
	public final File getDirectory() {
		if (appDirectory == null) {
			String directoryPath = getDirectoryAbsolutePath();
			
			appDirectory = new File(directoryPath);

			if (!appDirectory.exists())
				appDirectory = FileUtils.getOrCreateDirectory(directoryPath);
		}
		
		return appDirectory;
	}
	
		
	/**
	 * Registers a module to be initialized with the application.
	 * @param <M> module type
	 * @param moduleClass module class to be registered
	 * @param moduleInstance associated module instance
	 */
	public final <M extends Module> void registerModule(Class<M> moduleClass, M moduleInstance) {
		if (isRunning())
			throw new RuntimeException("Cannot register a module in a running application");
		
		if (moduleClass == null)
			throw new IllegalArgumentException("Module class cannot be null");
		
		if (moduleInstance == null)
			throw new IllegalArgumentException("Module instance cannot be null");
		
		moduleMap.put(moduleClass, moduleInstance);
	}
	
	/**
	 * Returns a module instance registered with this application.
	 * @param moduleClass module class
	 * @return module instance or null if a module is not registered
	 * @param <M> module type
	 */
	public final <M extends Module> M getModule(Class<M> moduleClass) {
		return (M) moduleMap.get(moduleClass);
	}
	
	/** 
	 * Starts a module.
	 * @param moduleClass module class to be started
	 * @param callerModules recursive caller list. Used to detect cyclic dependencies
	 * @param transientModules used to store transient dependencies which are not registered by application
	 * @param start defines if given module shall be started
	 */
	private void startModule(Class<? extends Module> moduleClass, List<Class<? extends Module>> callerModules, Set<Class<? extends Module>> transientModules, boolean start) {
		if (callerModules == null)
			callerModules = new LinkedList<>();
		
		if (transientModules == null)
			transientModules = new LinkedHashSet<>();
		
		Module moduleInstance = moduleMap.get(moduleClass);
		
		if (!moduleInstance.isRunning()) {
			if (callerModules.contains(moduleClass))
				throw new RuntimeException("Cyclic dependency on module: " + moduleClass);

			callerModules.add(moduleClass);
			
			Class<? extends Module>[] dependencies = moduleInstance.getDependencies();
			if (dependencies == null)
				dependencies = new Class[] {};
			
			for (Class<? extends Module> dep : dependencies) {
				if (!moduleMap.containsKey(dep))
					transientModules.add(dep); // <-- Probably transient dependencies are not explicitly registered, so register them here (required for settings loading of not-registered modules).
				
				startModule(dep, callerModules, transientModules, start);
			}

			if (start) {
				try {
					moduleInstance.start(this);
					log(LogType.INFO, "Initialized module: %s", moduleInstance.getClass().getName());
				} catch (Throwable t) {
					log(LogType.ERROR, "Error starting module: %s (%s)", moduleInstance.getClass().getName(), t.getMessage());
					if (t instanceof RuntimeException)
						throw (RuntimeException) t;
					else
						throw new RuntimeException(t);
				}

				startedModules.add(moduleInstance);
			}
		}
	}
	
	/** Starts modules. */
	private void startModules(boolean start) {
		if (!moduleMap.isEmpty() && start)
			log(LogType.INFO, "Starting modules...");
		
		Set<Class<? extends Module>> transientModules = new LinkedHashSet<>();
		
		for (Class<? extends Module> moduleClass : moduleMap.keySet()) {
			startModule(moduleClass, null, transientModules, start);
		}
		
		for (Class<? extends Module> transientDep : transientModules) {
			moduleClassSet.add(transientDep); // <-- required for settings loading of not-registered modules.
		}
	}
	
	/** Shutdown initialized modules in appropriate sequence. */
	private void stopModules() {
		if (startedModules.size() > 0)
			log(LogType.INFO, "Stopping modules...");

		for (int i = startedModules.size() - 1; i >= 0; i--) {
			Module module = startedModules.get(i);
			module.stop();
			log(LogType.INFO, "Shutted down module: %s", module.getClass().getName());
		}
	}
	
	
	/**
	 * Returns a service instance.
	 * @param <T> returned type
	 * @param serviceClass expected service class
	 * @return service instance.
	 */
	public final <T extends Service> T getService(Class<T> serviceClass) {
		T service = singletonManager.getSingleton(serviceClass);
		
		if (!service.isActive())
			service.init(this);
		
		return service;
	}
	
	/**
	 * Replaces a service by a subclass of it.
	 * @param <T> base service type
	 * @param baseclass service base class
	 * @param subclass service subclass.
	 */
	public final <T extends Service> void registerServiceReplacement(Class<T> baseclass, Class<? extends T> subclass) {
		if (isRunning())
			throw new RuntimeException("Cannot register a service replacement in a running application");
		
		singletonManager.registerClassReplacement(baseclass, subclass);
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
	 * @return application default properties.
	 */
	protected Properties getDefaultProperties() {
		Properties defaultProperties = new Properties();
		defaultProperties.setProperty(KEY_APP_DISABLE,         "" + DEFAULT_APP_DISABLED);
		defaultProperties.setProperty(KEY_APP_ALLOWED_ORIGINS, DEFAULT_APP_ALLOWED_ORIGINS);
		return defaultProperties;
	}
	
	/** 
	 * Load application settings.
	 * @throws IOException if there is an error reading settings file.
	 */
	private void loadSettings() throws IOException {
		File settingsFile = new File(getDirectory(), getPropertiesFilename());
		List<PropertyGroup> propertyGroups = new LinkedList<>();
		
		// Apply application default properties...
		Properties defaultProperties = getDefaultProperties();
		if (defaultProperties != null) {
			for (Map.Entry<Object, Object> entry : defaultProperties.entrySet()) {
				properties.put(entry.getKey(), entry.getValue());
			}
		}

		// Apply settings file properties if file exists...
		if (settingsFile.exists()) {
			try (FileInputStream fis = new FileInputStream(settingsFile)) {
				Properties tmpProperties = new Properties();
				tmpProperties.load(fis);
				properties.putAll(tmpProperties);
			}
		} else {
			propertyGroups.add(new PropertyGroup(properties, "Application settings"));
		}
		
		// Apply modules default properties (keeping existing)...
		for (Class<? extends Module> moduleClass : moduleClassSet) {
			Module module = singletonManager.getSingleton(moduleClass);
			Properties defaultModuleProperties = module.getDefaultProperties();
			
			if (defaultModuleProperties != null && !defaultModuleProperties.isEmpty()) {
				propertyGroups.add(new PropertyGroup(defaultModuleProperties, module.getClass().getName()));
				
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
		if (!settingsFile.exists()) {
			log(LogType.INFO, "Creating default settings file...");
			PropertyGroup.writeToFile(settingsFile, propertyGroups);
		}
	}	

	
	/** Starts this application. */
	public void start() {
		if (!isRunning()) {
			if (singleton != null)
				throw new RuntimeException("Another application instance is already running: " + singleton.getClass().getName());
			
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
			
			log(LogType.INFO, "Starting application: %s", name);
			beforeApplicationStart();
			
			try {
				startModules(false); // <-- required in order to retrieve transient modules default settings...
				log(LogType.INFO, "Loading settings...");
				loadSettings();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}

			// Starts all modules
			startModules(true);
			singleton = this;
			running = true;
			log(LogType.INFO, "Application is ready: %s", name);
			
			afterApplicationStart();
		} else {
			throw new RuntimeException("Application is already running");
		}
	}
	
	/** 
	 * Called before application is initialized.
	 * Default implementation does nothing. 
	 * This is the place to register modules with the application.
	 */
	protected void beforeApplicationStart() {}
	
	/** 
	 * Called after application is initialized.
	 * During this phase all modules associated with this application are running.
	 * Always call super implementation.
	 */
	protected void afterApplicationStart() {
		disabled       = Boolean.parseBoolean(getProperties().getProperty(KEY_APP_DISABLE, "" + DEFAULT_APP_DISABLED));
		allowedOrigins = getProperties().getProperty(KEY_APP_ALLOWED_ORIGINS, AbstractWebApplication.DEFAULT_APP_ALLOWED_ORIGINS).split(Pattern.quote(ORIGIN_DELIMITER));

		for (int i = 0; i < allowedOrigins.length; i++) {
			allowedOrigins[i] = allowedOrigins[i].trim();
		}
	}
	
	
	/** Stops this application. */
	public void stop() {
		if (isRunning()) {
			log(LogType.INFO, "Application shutdown: %s", getName());
			beforeApplicationStop();
			
			stopModules();
			reset();
			singleton = null;
			log(LogType.INFO, "Application was shutted down: %s", getName());
			
			afterApplicationStop();

		} else {
			throw new RuntimeException("Application is not running");
		}
	}
	
	/**
	 * Called before application shutdown.
	 * During this phase all modules are accessible.
	 * Default implementation does nothing.
	 */
	protected void beforeApplicationStop() {}
	
	/** 
	 * Called after application is stopped.
	 * During this phase there is no active modules associated with this application.
	 * Default implementation does nothing.
	 */
	protected void afterApplicationStop() {}
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		start();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		stop();
	}
	// =========================================================================
}