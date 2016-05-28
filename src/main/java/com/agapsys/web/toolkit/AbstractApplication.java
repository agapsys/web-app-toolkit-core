/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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
import com.agapsys.web.toolkit.utils.SingletonManager;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Represents an application.
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractApplication {
	// CLASS SCOPE =============================================================

	// Global settings ---------------------------------------------------------
	public static final String DEFAULT_SETTINGS_GROUP_NAME = AbstractApplication.class.getPackage().getName() + ".Application";

	private static final String APP_NAME_PATTERN = "^[a-zA-Z0-9\\-_]+$";
	// -------------------------------------------------------------------------
	private static final String SETTINGS_FILENAME = "application.properties";
	private static final String LOG_DIR_NAME      = "log";

	private static AbstractApplication singleton = null;

	/**
	 * Return application running instance.
	 *
	 * @return application singleton instance. If an application is not running returns null
	 */
	public static AbstractApplication getRunningInstance() {
		return singleton;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final SingletonManager<Module>  moduleManager  = new SingletonManager<>(Module.class);
	private final SingletonManager<Service> serviceManager = new SingletonManager<>(Service.class);

	private final List<Module> initializedModules  = new LinkedList<>();
	private final ApplicationSettings settings = new ApplicationSettings();
	private final Properties defaultProperties = new Properties();

	private File     appDirectory;
	private boolean  running;

	public AbstractApplication() {
		reset();
	}

	/** Resets application state. */
	private void reset() {
		moduleManager.clear();
		serviceManager.clear();
		initializedModules.clear();
		settings.clear();
		defaultProperties.clear();

		appDirectory   = null;
		running        = false;
	}

	/**
	 * Logs application messages.
	 *
	 * Default implementation just prints to console.
	 * @param logType log message type.
	 * @param message message to be logged.
	 * @param args message parameters (see {@linkplain String#format(String, Object...)}).
	 */
	public final void log(LogType logType, String message, Object...args) {
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
	 * @return the directory where application stores resources.
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
	 * Register a service instance.
	 *
	 * Usually, services do not need to be registered, since they are
	 * automatically registered upon demand. Use this method to replace a
	 * service instance by a customized one.
	 * @param service service instance to be registered.
	 */
	public final void registerService(Service service) {
		serviceManager.registerInstance(service);
	}

	/**
	 * Returns a service instance.
	 *
	 * @param <S> Service type.
	 * @param serviceClass expected service class.
	 * Service class must be a concrete class. If an instance is not already
	 * registered, service class must have an accessible default constructor.
	 * @return service instance.
	 */
	public final <S extends Service> S getService(Class<S> serviceClass) {
		return serviceManager.getInstance(serviceClass, true);
	}

	/**
	 * Registers a module to be initialized with the application.
	 *
	 * @param <M> module type.
	 * @param moduleClass module class to be registered.
	 * Given class must be a concrete class and must have an accessible default
	 * constructor.
	 * @return registered instance
	 */
	public final <M extends Module> M registerModule(Class<M> moduleClass) {
		if (isActive()) throw new RuntimeException("Cannot register a module with a running application");
		return moduleManager.registerClass(moduleClass);
	}

	/**
	 * Registers a module to be initialized with the application.
	 *
	 * @param moduleInstance associated module instance.
	 */
	public final void registerModule(Module moduleInstance) {
		if (isActive()) throw new RuntimeException("Cannot register a module with a running application");
		moduleManager.registerInstance(moduleInstance);
	}

	/**
	 * Returns a module instance registered with this application.
	 *
	 * @param <M> module type.
	 * @param moduleClass module class.
	 * @return module instance or null if a module is not registered.
	 */
	public final <M extends Module> M getModule(Class<M> moduleClass) {
		return moduleManager.getInstance(moduleClass);
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

		if (moduleInstance == null)
			moduleInstance = registerModule(moduleClass);

		if (!moduleInstance.isActive()) {
			if (callerModules.contains(moduleClass))
				throw new RuntimeException("Cyclic dependency on module: " + moduleClass);

			callerModules.add(moduleClass);

			Set<Class<? extends Module>> dependencies = moduleInstance._getDependencies();

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

				if (!(moduleInstance instanceof LogModule)) // <-- Log module is registered automatically via _beforeApplicationStart()
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
		if (!moduleManager.isEmpty() && init)
			log(LogType.INFO, "Starting modules...");

		Set<Class<? extends Module>> declaredClasses = new LinkedHashSet<>(); // <-- required to avoid concurrent modification: moduleManager may be modified on resolveModule()
		for (Class<? extends Module> moduleClass : moduleManager.getClasses()) {
			declaredClasses.add(moduleClass);
		}

		for (Class<? extends Module> moduleClass : declaredClasses) {
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
	ApplicationSettings getSettings() {
		return settings.getReadOnlyInstance();
	}

	/**
	 * Returns the properties associated to this application.
	 *
	 * @return the properties associated to this application.
	 */
	protected Properties getProperties() {
		synchronized(this) {
			Properties mainProperties = getSettings().getProperties(getAppSettingsGroupName());
			Properties defaults = getDefaultProperties();
			return ApplicationSettings.mergeProperties(mainProperties, defaults);
		}
	}

	/**
	 * Returns the name of the settings group used by application.
	 *
	 * @return the name of the settings group used by application.
	 */
	protected final String getAppSettingsGroupName() {
		return DEFAULT_SETTINGS_GROUP_NAME;
	}

	/**
	 * Returns application default properties.
	 *
	 * @return application default properties.
	 */
	protected Properties getDefaultProperties() {
		return defaultProperties;
	}

	/**
	 * Load application settings.
	 *
	 * @throws IOException if there is an error reading settings file.
	 */
	private void loadSettings() throws IOException {
		File settingsFile = new File(getDirectory(), getSettingsFilename());

		// Apply application default properties...
		settings.addProperties(getAppSettingsGroupName(), getDefaultProperties());

		// Apply modules default properties...
		Set<Module> moduleInstanceSet = new LinkedHashSet<>();
		for (Module module : moduleManager.getInstances()) {
			moduleInstanceSet.add(module);
		}
		for (Module module : moduleInstanceSet) {
			settings.addProperties(module._getSettingsGroupName(), module._getDefaultProperties());
		}

		if (settingsFile.exists()) {
			// Apply settings file properties if file exists...
			settings.read(settingsFile);
		} else {
			// Write properties to disk if file doesn't exist...
			log(LogType.INFO, "Creating default settings file...");
			settings.writeToFile(settingsFile);
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

		_beforeApplicationStart();

		log(LogType.INFO, "Starting application: %s", name);

		resolveModules(false); // <-- required in order to retrieve transient modules default settings...

		try {
			log(LogType.INFO, "Loading settings...");
			loadSettings();
		} catch (IOException ex) {
			log(LogType.ERROR, "Error loading settings: %s", ex.getMessage());
			throw new RuntimeException(ex);
		}

		// Starts all modules
		resolveModules(true);
		singleton = this;
		running = true;
		log(LogType.INFO, "Application is ready: %s", name);

		afterApplicationStart();

		settings.clear(); // <-- Settings should not be kept in memory since it may contains sensitive-data
	}

	private void _beforeApplicationStart() {
		reset();

		String logDirPath = new File(getDirectory(), getLogDirName()).getAbsolutePath();
		File logDir = FileUtils.getInstance().getOrCreateDirectory(logDirPath);

		LogModule logModule = new LogModule(new DailyLogFileStream(logDir));
		registerModule(logModule);
		initializedModules.add(logModule); // <-- forces the log module to be the last stopped module.

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
		singleton = null;

		afterApplicationStop();
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
	// =========================================================================
}