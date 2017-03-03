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

import com.agapsys.web.toolkit.modules.ExceptionReporterModule;
import com.agapsys.web.toolkit.modules.LogModule;
import com.agapsys.web.toolkit.modules.LogModule.DailyLogFileStream;
import com.agapsys.web.toolkit.utils.ApplicationSettings;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.FileUtils.AccessError;
import com.agapsys.web.toolkit.utils.Settings;
import com.agapsys.web.toolkit.utils.SingletonManager;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Represents an application.
 */
public abstract class AbstractApplication {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static final String APP_NAME_PATTERN  = "^[a-zA-Z][a-zA-Z0-9\\-_]*$";
    protected static final String SETTINGS_FILENAME = "settings.ini";
    protected static final String LOG_DIR           = "log";

    private static AbstractApplication singleton = null;
    public static AbstractApplication getRunningInstance() {
        return singleton;
    }
    // =========================================================================
    // </editor-fold>

    private final SingletonManager<Module>  moduleManager      = new SingletonManager<>(Module.class);
    private final SingletonManager<Service> serviceManager     = new SingletonManager<>(Service.class);
    private final List<Module>              initializedModules = new LinkedList<>();

    private File                appDirectory;
    private boolean             running;
    private ApplicationSettings applicationSettings;


    public AbstractApplication() {
        reset();
    }

    /** Resets application state. */
    private void reset() {
        moduleManager.clear();
        serviceManager.clear();
        initializedModules.clear();

        appDirectory = null;
        running      = false;
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
    public final boolean isRunning() {
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
     * Returns a file representing application directory.
     *
     * @return the directory where application stores resources.
     */
    public final File getDirectory() {
        if (appDirectory == null) {
            String directoryPath = new File(getParentDir(), "." + getName()).getAbsolutePath();

            appDirectory = new File(directoryPath);

            if (!appDirectory.exists()) {
                try {
                    appDirectory = FileUtils.getOrCreateDirectory(directoryPath);
                } catch (AccessError ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        
        if (!appDirectory.canWrite())
            throw new RuntimeException("Cannot write into application directory: " + appDirectory.getAbsolutePath());

        return appDirectory;
    }

    /**
     * Returns the parent dir where application directory will be placed.
     * @return the parent dir where application directory will be placed. Default implementation returns user's home dir.
     */
    protected File getParentDir() {
        return FileUtils.USER_HOME;
    }

    /**
     * Register a service instance.
     *
     * Usually, services do not need to be registered, since they are
     * automatically registered on demand. Use this method to replace a
     * service instance by a customized one.
     * @param service service instance to be registered.
     * @param overrideClassHierarchy defines if class hierarchy should be overriden.
     */
    public final void registerService(Service service, boolean overrideClassHierarchy) {
        serviceManager.registerInstance(service, overrideClassHierarchy);
    }
    
    /**
     * Register a service instance.
     *
     * This is a convenience method for registerService(service, true).
     * 
     * Usually, services do not need to be registered, since they are
     * automatically registered on demand. Use this method to replace a
     * service instance by a customized one.
     * @param service service instance to be registered.
     */
    public final void registerService(Service service) {
        registerService(service, true);
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
        S service = serviceManager.getInstance(serviceClass, true, false);
        if (!service.isActive())
            service._init(this);

        return service;
    }

    /**
     * Registers a module to be initialized with the application.
     *
     * @param <M> module type.
     * @param moduleClass module class to be registered.
     * @param overrideClassHierarchy defines if class hierarchy shall be overriden.
     * Given class must be a concrete class and must have an accessible default
     * constructor.
     * @return registered instance
     */
    public final <M extends Module> M registerModule(Class<M> moduleClass, boolean overrideClassHierarchy) {
        if (isRunning())
            throw new RuntimeException("Cannot register a module with a running application");
        
        return moduleManager.registerClass(moduleClass, overrideClassHierarchy);
    }
    
    /**
     * Registers a module to be initialized with the application.
     * 
     * This is a convenience method for registerModule(moduleClass, true).
     *
     * @param <M> module type.
     * @param moduleClass module class to be registered.
     * Given class must be a concrete class and must have an accessible default
     * constructor.
     * @return registered instance
     */
    public final <M extends Module> M registerModule(Class<M> moduleClass) {
        return registerModule(moduleClass, true);
    }

    /**
     * Registers a module to be initialized with the application.
     *
     * @param moduleInstance associated module instance.
     * @param overrideClassHierarchy defines if class hierarchy shall be overriden.
     */
    public final void registerModule(Module moduleInstance, boolean overrideClassHierarchy) {
        if (isRunning())
            throw new RuntimeException("Cannot register a module with a running application");

        moduleManager.registerInstance(moduleInstance, overrideClassHierarchy);
    }
    
    /**
     * Registers a module to be initialized with the application.
     * 
     * This is a convenience method for registerModule(moduleInstance, true).
     *
     * @param moduleInstance associated module instance.
     */
    public final void registerModule(Module moduleInstance) {
        registerModule(moduleInstance, true);
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
    private <M extends Module> void __resolveModule(Class<M> moduleClass, List<Class<? extends Module>> callerModules, boolean init) {
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
                __resolveModule(dep, callerModules, init);
            }

            if (init) {
                try {
                    log(LogType.INFO, "Initializing module: %s", moduleInstance.getClass().getName());
                    moduleInstance._init(this);
                } catch (Throwable t) {
                    log(LogType.ERROR, "Error initializing module: %s (%s)\n----\n%s----", moduleInstance.getClass().getName(), t.getMessage(), ExceptionReporterModule.getStackTrace(t));

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
    private void __resolveModules(boolean init) {
        if (!moduleManager.isEmpty() && init)
            log(LogType.INFO, "Starting modules...");

        Set<Class<? extends Module>> declaredClasses = new LinkedHashSet<>(); // <-- required to avoid concurrent modification: moduleManager may be modified on resolveModule()
        for (Class<? extends Module> moduleClass : moduleManager.getClasses()) {
            declaredClasses.add(moduleClass);
        }

        for (Class<? extends Module> moduleClass : declaredClasses) {
            __resolveModule(moduleClass, null, init);
        }
    }

    /**
     * Shutdown initialized modules in appropriate sequence.
     */
    private void __stopModules() {
        if (initializedModules.size() > 0)
            log(LogType.INFO, "Stopping modules...");

        for (int i = initializedModules.size() - 1; i >= 0; i--) {
            Module module = initializedModules.get(i);

            log(LogType.INFO, "Shutting down module: %s", module.getClass().getName());
            module._stop();
        }
    }

    /**
     * @return application global settings
     */
    public ApplicationSettings getApplicationSettings() {
        synchronized(this) {
            return applicationSettings;
        }
    }

    /**
     * Returns application default settings.
     *
     * @return application sections properties. Default implementation returns null.
     */
    protected Settings getDefaultSettings() {
        return null;
    }

    private Settings __getDefaultSettings() {
        Settings settings = getDefaultSettings();
        if (settings == null)
            return new Settings();

        return settings;
    }

    private ApplicationSettings __loadSettings(File settingsFile, boolean writeIfNotExist) throws IOException {
        ApplicationSettings applicationSettings = settingsFile.exists() ? ApplicationSettings.load(settingsFile) : new ApplicationSettings();
        
        ApplicationSettings mDefaultApplicationSettings = new ApplicationSettings();

        // Consolidate default settings (APPLICATION)...
        Settings mDefaultSettings = __getDefaultSettings();
        for (Entry<String, String> entry : mDefaultSettings.entrySet()) {
            mDefaultApplicationSettings.setProperty(entry.getKey(), entry.getValue());
        }

        // Consolidate default settings (MODULES)...
        for (Module module : moduleManager.getInstances()) {
            String section = module._getSettingsSection();
            Settings defaults = module._getDefaultSettings();

            for (Entry<String, String> entry : defaults.entrySet()) {
                mDefaultApplicationSettings.setProperty(section, entry.getKey(), entry.getValue());
            }
        }
        

        for(Entry<String, Settings> entry : mDefaultApplicationSettings.entrySet()) {
            for (Entry<String, String> sectionEntry : entry.getValue().entrySet()) {
                applicationSettings.setPropertyIfAbsent(entry.getKey(), sectionEntry.getKey(), sectionEntry.getValue());
            }
        }
        
        if (!settingsFile.exists() && writeIfNotExist) {
            // Write properties to disk if file doesn't exist...
            log(LogType.INFO, "Creating default settings file...");
            applicationSettings.store(settingsFile);
        }
        
        return applicationSettings;
    }
    
    private void __loadSettings() throws IOException {
        if (applicationSettings != null) {
            applicationSettings.clear();
            applicationSettings = null;
        }
        
        File settingsFile = new File(getDirectory(), SETTINGS_FILENAME);
        applicationSettings = __loadSettings(settingsFile, true);
        
        onSettingsLoaded();
    }
    
    /** 
     * Called after application settings were loaded.
     * 
     * Default implementation does nothing.
     */
    protected void onSettingsLoaded() {}
    
    /**
     * Starts this application.
     */
    public void start() {
        if (isRunning())
            throw new RuntimeException("Application is already running");

        if (singleton != null)
            throw new IllegalStateException("Another application instance is already running");

        try {
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

            __beforeApplicationStart();

            log(LogType.INFO, "Starting application: %s", name);

            __resolveModules(false); // <-- required in order to retrieve transient modules default settings...

            try {
                log(LogType.INFO, "Loading settings...");
                __loadSettings();
            } catch (IOException ex) {
                log(LogType.ERROR, "Error loading settings: %s", ex.getMessage());
                throw new RuntimeException(ex);
            }

            // Starts all modules
            __resolveModules(true);
            running = true;
            log(LogType.INFO, "Application is ready: %s", name);

            afterApplicationStart();

            singleton = this;
        } catch (RuntimeException ex) {
            singleton = null;
            onStartError(ex);
            throw ex;
        }
    }

    private void __beforeApplicationStart() {
        reset();

        String logDirPath = new File(getDirectory(), LOG_DIR).getAbsolutePath();
        File logDir;
        try {
            logDir = FileUtils.getOrCreateDirectory(logDirPath);
        } catch (AccessError ex) {
            throw new RuntimeException(ex);
        }

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
        if (!isRunning())
            throw new RuntimeException("Application is not running");

        try {
            log(LogType.INFO, "Shutting shutdown application: %s", getName());
            beforeApplicationStop();

            __stopModules();

            afterApplicationStop();
            singleton = null;
            running = false;
        } catch (RuntimeException ex) {
            singleton = null;
            running = false;
            onStopError(ex);
            throw ex;
        }
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

    /**
     * Called when there was an error while starting application.
     * @param ex error
     */
    protected void onStartError(RuntimeException ex) {}

    /**
     * Called when there was an error while stoping the application.
     * @param ex error
     */
    protected void onStopError(RuntimeException ex) {}
    
    /**
     * Restarts this application;
     */
    public void restart() {
        stop();
        start();
    }

}