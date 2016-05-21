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

import com.agapsys.web.toolkit.modules.LogModule;
import com.agapsys.web.toolkit.modules.LogModule.DailyLogFileStream;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.HttpUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
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
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractWebApplication implements ServletContextListener {
	// CLASS SCOPE =============================================================

	// Global settings ---------------------------------------------------------
	private sta

	/** Defines if application is disabled. When an application is disabled, all requests are ignored and a {@linkplain HttpServletResponse#SC_SERVICE_UNAVAILABLE} is sent to the client. */
	public static final String KEY_APP_DISABLE = "com.agapsys.webtoolkit.appDisable";

	/** Defines a comma-delimited list of allowed origins for this application or '*' for any origin. If an origin is not accepted a {@linkplain HttpServletResponse#SC_FORBIDDEN} is sent to the client. */
	public static final String KEY_APP_ALLOWED_ORIGINS = "com.agapsys.webtoolkit.allowedOrigins";

	/** Defines the environment used by application. */
	public static final String KEY_ENVIRONMENT = "com.agapsys.webtoolkit.environment";

	public static final boolean DEFAULT_APP_DISABLED        = false;
	public static final String  DEFAULT_APP_ALLOWED_ORIGINS = "*";
	public static final String  ORIGIN_DELIMITER            = ",";

	private static final String APP_NAME_PATTERN = "^[a-zA-Z0-9\\-_]+$";
	// -------------------------------------------------------------------------

	private static final String SETTINGS_FILENAME = "application.properties";
	private static final String LOG_DIR_NAME           = "log";

	private static AbstractWebApplication singleton = null;

	/**
	 * Return application running instance.
	 *
	 * @return application singleton instance. If an application is not running returns null
	 */
	public static AbstractWebApplication getRunningInstance() {
		return singleton;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final Map<Class<? extends Module>, Module>   moduleMap = new LinkedHashMap<>();
	private final Map<Class<? extends Service>, Service> serviceMap = new LinkedHashMap<>();
	private final List<Module> initializedModules  = new LinkedList<>();
	private final ApplicationSettings settings = new ApplicationSettings();
	private final Properties defaultProperties = new Properties();

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
		initializedModules.clear();
		settings.clear();
		defaultProperties.clear();

		appDirectory   = null;
		running        = false;
		disabled       = DEFAULT_APP_DISABLED;
		allowedOrigins = new String[] {DEFAULT_APP_ALLOWED_ORIGINS};
	}

	/**
	 * Logs application messages.
	 *
	 * Default implementation just prints to console.
	 * @param logType log message type.
	 * @param message message to be logged.
	 * @param args message parameters (see {@linkplain String#format(String, Object...)}).
	 */
	public void log(LogType logType, String message, Object...args) {
		getModule(LogModule.class).log(logType, message, args);
	}

	/**
	 * Return a boolean indicating if application is running.
	 *
	 * @return a boolean indicating if application is running.
	 */
	public final boolean isActive() {
		return running;
	}

	/**
	 * Returns a boolean indicating if application is disabled.
	 *
	 * @return a boolean indicating if application is disabled.
	 */
	public boolean isDisabled() {
		if (!isActive())
			throw new RuntimeException("Application is not running");

		return disabled;
	}

	/**
	 * Returns a boolean indicating if given request is allowed to proceed.
	 *
	 * @param req HTTP request.
	 * @return boolean indicating if given request is allowed to proceed.
	 */
	protected boolean isOriginAllowed(HttpServletRequest req) {
		if (!isActive())
			throw new RuntimeException("Application is not running");

		boolean isOriginAllowed = allowedOrigins.length == 1 && allowedOrigins[0].equals(DEFAULT_APP_ALLOWED_ORIGINS);

		if (isOriginAllowed)
			return true;

		String originIp = HttpUtils.getInstance().getOriginIp(req);

		for (String allowedOrigin : allowedOrigins) {
			if (allowedOrigin.equals(originIp))
				return true;
		}

		return false;
	}

	/**
	 * Returns application name.
	 *
	 * @return the application name.
	 */
	public abstract String getName();

	/**
	 * Returns application version.
	 *
	 * @return the application version.
	 */
	public abstract String getVersion();

	/**
	 * Returns application settings filename.
	 *
	 * @return application settings filename.
	 */
	protected String getSettingsFilename() {
		return SETTINGS_FILENAME;
	}

	/**
	 * Returns the absolute path of application folder.
	 *
	 * @return the path of application directory.
	 */
	protected String getDirectoryAbsolutePath() {
		File d = new File(FileUtils.USER_HOME, "." + getName());

		return d.getAbsolutePath();
	}

	/**
	 * Returns a file representing application directory.
	 *
	 * @return the directory where application stores resources outside application context in servlet container.
	 */
	public final File getDirectory() {
		if (appDirectory == null) {
			String directoryPath = getDirectoryAbsolutePath();

			appDirectory = new File(directoryPath);

			if (!appDirectory.exists())
				appDirectory = FileUtils.getInstance().getOrCreateDirectory(directoryPath);
		}

		return appDirectory;
	}

	/**
	 * Returns the name of log directory.
	 *
	 * @return the name of log directory.
	 */
	protected String getLogDirName() {
		return LOG_DIR_NAME;
	}

	/**
	 * Returns an object instance of a given class using its default constructor.
	 *
	 * @param <T> Object type
	 * @param clazz Object class.
	 * @return an object instance of a given class using its default constructor.
	 */
	private <T> T getDefaultObjInstance(Class<T> clazz) {
		try {
			T obj = clazz.getConstructor().newInstance();
			return obj;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new RuntimeException(String.format("Error instantiating class: '%s'", clazz), ex);
		}
	}

	/**
	 * Register a service instance with the application.
	 *
	 * @param <S> Service type.
	 * @param serviceClass service class.
	 * @param serviceInstance service instance to be registered.
	 * @param warnRuntime defines if service registering during application runtime should issue a warning log.
	 */
	private <S extends Service> void registerService(Class<S> serviceClass, S serviceInstance, boolean warnRuntime) {
		synchronized(serviceMap) {
			if (serviceClass == null)
				throw new IllegalArgumentException("Service class cannot be null");

			if (serviceInstance == null)
				throw new IllegalArgumentException("Service instance cannot be null");

			if (isActive() && warnRuntime)
				log(LogType.WARNING, "Registering a service during runtime: '%s'", serviceInstance.getClass().getName());

			serviceMap.put(serviceClass, serviceInstance);
		}
	}

	/**
	 * Register a service instance with the application.
	 *
	 * @param <S> Service type.
	 * @param serviceClass service class.
	 * @param serviceInstance service instance to be registered.
	 */
	public final <S extends Service> void registerService(Class<S> serviceClass, S serviceInstance) {
		registerService(serviceClass, serviceInstance, true);
	}

	/**
	 * Returns a service instance.
	 *
	 * @param <S> Service type.
	 * @param serviceClass expected service class. Service class must have an accessible default constructor or must be previously registered via {@linkplain AbstractWebApplication#registerService(java.lang.Class, com.agapsys.web.toolkit.Service)}
	 * @return service instance.
	 */
	public final <S extends Service> S getService(Class<S> serviceClass) {
		synchronized(serviceMap) {
			if (serviceClass == null)
				throw new IllegalArgumentException("Service class cannot be null");

			S service = (S) serviceMap.get(serviceClass);

			if (service == null) {
				service = getDefaultObjInstance(serviceClass);
				registerService(serviceClass, service, false);
			}

			if (!service.isActive())
				service.init(this);

			return service;
		}
	}

	/**
	 * Registers a module to be initialized with the application.
	 *
	 * @param <M> module type.
	 * @param moduleClass module class to be registered. Given class must have an accessible default constructor.
	 */
	public final <M extends Module> void registerModule(Class<M> moduleClass) {
		synchronized(moduleMap) {
			if (moduleClass == null)
				throw new IllegalArgumentException("Module class cannot be null");

			M module = getDefaultObjInstance(moduleClass);
			registerModule(moduleClass, module);
		}
	}

	/**
	 * Registers a module to be initialized with the application.
	 *
	 * @param <M> module type.
	 * @param moduleClass module class to be registered.
	 * @param moduleInstance associated module instance.
	 */
	public final <M extends Module> void registerModule(Class<M> moduleClass, M moduleInstance) {
		synchronized (moduleMap) {
			if (isActive())
				throw new RuntimeException("Cannot register a module with a running application");

			if (moduleClass == null)
				throw new IllegalArgumentException("Module class cannot be null");

			if (moduleInstance == null)
				throw new IllegalArgumentException("Module instance cannot be null");

			moduleMap.put(moduleClass, moduleInstance);
		}
	}

	/**
	 * Returns a module instance registered with this application.
	 *
	 * @param <M> module type.
	 * @param moduleClass module class.
	 * @return module instance or null if a module is not registered.
	 */
	public final <M extends Module> M getModule(Class<M> moduleClass) {
		synchronized(moduleMap) {
			return (M) moduleMap.get(moduleClass);
		}
	}

	/**
	 * Resolves a module.
	 *
	 * @param moduleClass module class to be resolved.
	 * @param callerModules recursive caller list. Used to detect cyclic dependencies.
	 * @param transientModules used to store transient dependencies which are not registered by application.
	 * @param init defines if given module shall be initialized.
	 */
	private <M extends Module> void resolveModule(Class<M> moduleClass, List<Class<? extends Module>> callerModules, boolean init) {
		if (callerModules == null)
			callerModules = new LinkedList<>();

		M moduleInstance = getModule(moduleClass);

		if (moduleInstance == null) {
			moduleInstance = getDefaultObjInstance(moduleClass);
			registerModule(moduleClass, moduleInstance);
		}

		if (!moduleInstance.isActive()) {
			if (callerModules.contains(moduleClass))
				throw new RuntimeException("Cyclic dependency on module: " + moduleClass);

			callerModules.add(moduleClass);

			Set<Class<? extends Module>> dependencies = moduleInstance.getDependencies();

			for (Class<? extends Module> dep : dependencies) {
				resolveModule(dep, callerModules, init);
			}

			if (init) {
				try {
					log(LogType.INFO, "Initializing module: %s", moduleInstance.getClass().getName());
					moduleInstance.init(this);
				} catch (Throwable t) {
					log(LogType.ERROR, "Error initializing module: %s (%s)", moduleInstance.getClass().getName(), t.getMessage());

					if (t instanceof RuntimeException)
						throw (RuntimeException) t;

					throw new RuntimeException(t);
				}

				initializedModules.add(moduleInstance);
			}
		}
	}

	/**
	 * Resolves all modules registered with the application.
	 *
	 * @param init defines if modules should be initialized (pass false to register transient modules).
	 */
	private void resolveModules(boolean init) {
		if (!moduleMap.isEmpty() && init)
			log(LogType.INFO, "Starting modules...");

		for (Class<? extends Module> moduleClass : moduleMap.keySet()) {
			resolveModule(moduleClass, null, init);
		}
	}

	/**
	 * Shutdown initialized modules in appropriate sequence.
	 */
	private void stopModules() {
		if (initializedModules.size() > 0)
			log(LogType.INFO, "Stopping modules...");

		for (int i = initializedModules.size() - 1; i >= 0; i--) {
			Module module = initializedModules.get(i);
			log(LogType.INFO, "Shutting down module: %s", module.getClass().getName());
			module.stop();
		}
	}

	/**
	 * @return application global settings
	 */
	public ApplicationSettings getSettings() {
		return settings.getReadOnlyInstance();
	}

	/**
	 * Returns application default properties.
	 *
	 * @return application default properties.
	 */
	protected Properties getDefaultProperties() {
		defaultProperties.setProperty(KEY_APP_DISABLE,         "" + DEFAULT_APP_DISABLED);
		defaultProperties.setProperty(KEY_APP_ALLOWED_ORIGINS, DEFAULT_APP_ALLOWED_ORIGINS);

		return defaultProperties;
	}

	/**
	 * Load application settings.
	 *
	 * @throws IOException if there is an error reading settings file.
	 */
	private void loadSettings() throws IOException {
		File settingsFile = new File(getDirectory(), getSettingsFilename());
		Map<String, Properties> propertyGroups = new LinkedHashMap<>();

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
		for (Module module : moduleMap.values()) {
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

	/**
	 * Starts this application.
	 */
	public void init() {
		if (isActive())
			throw new RuntimeException("Application is already running");

		if (singleton != null)
			throw new RuntimeException("Another application instance is already running: " + singleton.getClass().getName());

		String name = getName();

		if (name != null)
			name = name.trim();

		if (name == null || name.isEmpty())
			throw new IllegalStateException("Missing application name");

		if (!name.matches(APP_NAME_PATTERN))
			throw new IllegalArgumentException("Invalid application name: " + name);

		String version = getVersion();

		if (version != null)
			version = version.trim();

		if (version == null || version.isEmpty())
			throw new IllegalStateException("Missing application version");

		log(LogType.INFO, "Starting application: %s", name);

		_beforeApplicationStart();

		try {
			resolveModules(false); // <-- required in order to retrieve transient modules default settings...
			log(LogType.INFO, "Loading settings...");
			loadSettings();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		// Starts all modules
		resolveModules(true);
		singleton = this;
		running = true;
		log(LogType.INFO, "Application is ready: %s", name);

		_afterApplicationStart();
	}


	private void _beforeApplicationStart() {
		String logDirPath = new File(getDirectory(), getLogDirName()).getAbsolutePath();
		File logDir = FileUtils.getInstance().getOrCreateDirectory(logDirPath);

		registerModule(LogModule.class, new LogModule(new DailyLogFileStream(logDir)));
		beforeApplicationStart();
	}

	/**
	 * Called before application is initialized.
	 *
	 * Default implementation does nothing.
	 * This is the place to register modules and/or services with the application.
	 */
	protected void beforeApplicationStart() {}

	/**
	 * Required functionality to be called after application starts.
	 */
	private void _afterApplicationStart() {
		disabled       = Boolean.parseBoolean(getProperties().getProperty(KEY_APP_DISABLE, "" + DEFAULT_APP_DISABLED));
		allowedOrigins = getProperties().getProperty(KEY_APP_ALLOWED_ORIGINS, AbstractWebApplication.DEFAULT_APP_ALLOWED_ORIGINS).split(Pattern.quote(ORIGIN_DELIMITER));

		for (int i = 0; i < allowedOrigins.length; i++) {
			allowedOrigins[i] = allowedOrigins[i].trim();
		}

		afterApplicationStart();
	}

	/**
	 * Called after application is initialized.
	 *
	 * During this phase all modules associated with this application are running.
	 * Default implementation does nothing.
	 */
	protected void afterApplicationStart() {}

	/**
	 * Stops this application.
	 */
	public void stop() {
		if (!isActive())
			throw new RuntimeException("Application is not running");

		log(LogType.INFO, "Shutting shutdown application: %s", getName());
		beforeApplicationStop();

		stopModules();
		reset();
		singleton = null;

		afterApplicationStop();
		log(LogType.INFO, "Application was stopped: %s", getName());
	}

	/**
	 * Called before application shutdown.
	 *
	 * During this phase all modules are accessible.
	 * Default implementation does nothing.
	 */
	protected void beforeApplicationStop() {}

	/**
	 * Called after application is stopped.
	 *
	 * During this phase there is no active modules associated with this application.
	 * Default implementation does nothing.
	 */
	protected void afterApplicationStop() {}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		init();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		stop();
	}
	// =========================================================================
}