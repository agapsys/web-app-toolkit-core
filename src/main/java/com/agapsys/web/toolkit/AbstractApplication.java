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

import com.agapsys.web.toolkit.services.LogService;
import com.agapsys.web.toolkit.utils.FileUtils;
import com.agapsys.web.toolkit.utils.FileUtils.AccessError;
import com.agapsys.web.toolkit.utils.SingletonManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Represents an application.
 */
public abstract class AbstractApplication {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static interface StringConverter<T> {
        public T fromString(String str);

        public String toString(T t);
    }

    private static final Map<Class, StringConverter> STRING_CONVERTER_MAP;

    static {
        Map<Class, StringConverter> stringConverterMap = new LinkedHashMap<>();
        STRING_CONVERTER_MAP = Collections.unmodifiableMap(stringConverterMap);

        stringConverterMap.put(String.class, new StringConverter<String>() {
            @Override
            public String fromString(String str) {
                return str;
            }

            @Override
            public String toString(String str) {
                return str;
            }
        });
        stringConverterMap.put(Byte.class, new StringConverter<Byte>() {
            @Override
            public Byte fromString(String str) {
                return Byte.parseByte(str);
            }

            @Override
            public String toString(Byte b) {
                return Byte.toString(b);
            }

        });
        stringConverterMap.put(Short.class, new StringConverter<Short>() {
            @Override
            public Short fromString(String str) {
                return Short.parseShort(str);
            }

            @Override
            public String toString(Short s) {
                return Short.toString(s);
            }
        });
        stringConverterMap.put(Integer.class, new StringConverter<Integer>() {
            @Override
            public Integer fromString(String str) {
                return Integer.parseInt(str);
            }

            @Override
            public String toString(Integer i) {
                return Integer.toString(i);
            }
        });
        stringConverterMap.put(Long.class, new StringConverter<Long>() {
            @Override
            public Long fromString(String str) {
                return Long.parseLong(str);
            }

            @Override
            public String toString(Long l) {
                return Long.toString(l);
            }
        });
        stringConverterMap.put(Float.class, new StringConverter<Float>() {
            @Override
            public Float fromString(String str) {
                return Float.parseFloat(str);
            }

            @Override
            public String toString(Float f) {
                return Float.toString(f);
            }
        });
        stringConverterMap.put(Double.class, new StringConverter<Double>() {
            @Override
            public Double fromString(String str) {
                return Double.parseDouble(str);
            }

            @Override
            public String toString(Double d) {
                return Double.toString(d);
            }
        });
        stringConverterMap.put(BigDecimal.class, new StringConverter<BigDecimal>() {
            @Override
            public BigDecimal fromString(String str) {
                return new BigDecimal(str);
            }

            @Override
            public String toString(BigDecimal bd) {
                return bd.toPlainString();
            }
        });
        stringConverterMap.put(Date.class, new StringConverter<Date>() {
            @Override
            public Date fromString(String str) {
                return new Date(Long.parseLong(str));
            }

            @Override
            public String toString(Date d) {
                return Long.toString(d.getTime());
            }
        });
        stringConverterMap.put(Boolean.class, new StringConverter<Boolean>() {
            @Override
            public Boolean fromString(String str) {
                return Boolean.parseBoolean(str);
            }

            @Override
            public String toString(Boolean t) {
                return Boolean.toString(t);
            }
        });
    }

    private static final String   APP_NAME_PATTERN    = "^[a-zA-Z][a-zA-Z0-9\\-_]*$";
    protected static final String PROPERTIES_FILENAME = "application.properties";
    protected static final String LOG_DIR             = "log";

    private static AbstractApplication runningInstance = null;

    private static void __setRunningInstance(AbstractApplication app) {
        synchronized(AbstractApplication.class) {
            runningInstance = app;
        }
    }

    public static AbstractApplication getRunningInstance() {
        synchronized(AbstractApplication.class) {
            return runningInstance;
        }
    }
    // =========================================================================
    // </editor-fold>

    private final SingletonManager<Service>       serviceManager              = new SingletonManager<>(Service.class);
    private final List<Service>                   initializedServiceList      = new LinkedList<>();
    private final List<Class<? extends Service>>  serviceCircularRefCheckList = new LinkedList<>();
    private final Properties                      properties                  = new Properties();

    private File             appDirectory;
    private volatile boolean running;

    /** Resets instance. */
    private synchronized void __reset() {
        properties.clear();
        serviceManager.clear();
        initializedServiceList.clear();
        serviceCircularRefCheckList.clear();

        appDirectory = null;
        running      = false;
    }

    /** Returns the circular reference path ending in given class. */
    private synchronized String __getCicularReferencePath(Class<? extends Service> clazz) {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        for (Class<? extends Service> serviceClass : serviceCircularRefCheckList) {
            if (i > 0)
                sb.append(" --> ");

            sb.append(serviceClass.getName());

            i++;
        }

        sb.append(" --> ").append(clazz.getName());
        return sb.toString();
    }

    /** Stops initialized services in appropriate sequence. */
    private synchronized  void __stopServices() {
        for (int i = initializedServiceList.size() - 1; i >= 0; i--) {
            Service service = initializedServiceList.get(i);
            service._stop();
        }
    }

    /** Always returns a non-null instance. */
    private synchronized Properties __getDefaultProperties() {
        Properties mProperties = getDefaultProperties();

        if (mProperties == null)
            mProperties = new Properties();

        return mProperties;
    }

    /**
     * Loads application properties.
     *
     * @param createFile define if a default properties file should be created when there is default properties.
     * @throws IOException if there was an I/O error while reading/creating properties file.
     */
    private synchronized void __loadProperties(boolean createFile) throws IOException {
        properties.clear();

        File propertiesFile = new File(getDirectory(), PROPERTIES_FILENAME);

        // Loads properties from file...
        if (propertiesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                properties.load(fis);
            }
        }

        // Applies default properties...
        for (Map.Entry<Object, Object> entry : __getDefaultProperties().entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();

            if (!properties.containsKey(key))
                properties.setProperty(key, value);
        }

        if (!propertiesFile.exists() && !properties.isEmpty() && createFile)
            saveProperties();
    }


    public AbstractApplication() {
        __reset();
    }

    /**
     * Returns application name.
     *
     * @return application name.
     */
    public abstract String getName();

    /**
     * Returns application version.
     *
     * @return application version.
     */
    public abstract String getVersion();

    /**
     * Logs application messages.
     *
     * If a log service is not registered, nothing happens.
     * @param timestamp log message timestamp.
     * @param logType log message type.
     * @param message message to be logged.
     * @param msgArgs message parameters (see {@linkplain String#format(String, Object...)}).
     */
    public void log(Date timestamp, LogType logType, String message, Object...msgArgs) {
        synchronized (this) {
            if (!isRunning())
                throw new IllegalStateException("Application is not running");

            LogService logService = getService(LogService.class, false);

            if (logService != null)
                logService.log(timestamp, logType, message, msgArgs);
        }
    }

    /** Convenience method for log(new Date(), logType, message, msgArgs). */
    public final void log(LogType logType, String message, Object...msgArgs) {
        log(new Date(), logType, message, msgArgs);
    }

    /**
     * Return a boolean indicating if application is running.
     *
     * @return a boolean indicating if application is running.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns a file representing application directory.
     *
     * @return the directory where application stores resources.
     */
    public final File getDirectory() {
        synchronized(this) {
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
        }

        if (!appDirectory.canWrite())
            throw new RuntimeException("Cannot write into application directory: " + appDirectory.getAbsolutePath());

        return appDirectory;
    }

    /**
     * Returns the parent directory where application directory will be placed.
     *
     * @return the parent directory where application directory will be placed.
     * Default implementation returns user's home directory.
     */
    protected File getParentDir() {
        return FileUtils.USER_HOME;
    }

    /**
     * Register a service instance.
     *
     * @param service service instance to be registered.
     * @param overrideClassHierarchy defines if class hierarchy should be overridden during registration.
     */
    public void registerService(Service service, boolean overrideClassHierarchy) {
        synchronized(this) {
            serviceManager.registerInstance(service, overrideClassHierarchy);
        }
    }

    /** This is a convenience method for registerService(service, true). */
    public final void registerService(Service service) {
        registerService(service, true);
    }

    /**
     * Returns a service instance.
     *
     * @param <S> Service type.
     * @param serviceClass expected service class. Service class must be a concrete class.
     * @param autoRegistration defines if service instance should be registered. An attempt to get a service which is not registered will return null.
     * @return service instance.
     */
    public <S extends Service> S getService(Class<S> serviceClass, boolean autoRegistration) {
        synchronized(this) {
            if (!isRunning())
                throw new IllegalStateException("Application is not running");

            S service = serviceManager.getInstance(serviceClass, autoRegistration, false);

            if (service != null && !service.isRunning()) {
                if (serviceCircularRefCheckList.contains(serviceClass))
                    throw new RuntimeException("Circular service reference: " + __getCicularReferencePath(serviceClass));

                serviceCircularRefCheckList.add(serviceClass);
                service._start(this);
                initializedServiceList.add(service);
                serviceCircularRefCheckList.remove(serviceClass);
            }

            return service;
        }
    }

    public final <S extends Service> S getRegisteredService(Class<S> serviceClass) throws NoSuchElementException {
        S service = getService(serviceClass, false);

        if (service == null)
            throw new NoSuchElementException(serviceClass.getName());

        return service;
    }

    public final <S extends Service> S getServiceOnDemand(Class<S> serviceClass) {
        return getService(serviceClass, true);
    }

    /**
     * Return an application property
     * @param <T> returned type
     * @param targetClass expected returned type class
     * @param key property key
     * @param defaultValue property default value.
     * @return property value.
     */
    public <T> T getProperty(Class<T> targetClass, String key, T defaultValue) {
        synchronized(this) {
            if (!isRunning())
                throw new IllegalStateException("Application is not running");

            StringConverter<T> converter = STRING_CONVERTER_MAP.get(targetClass);
            if (converter == null)
                throw new UnsupportedOperationException("No converter for " + targetClass.getName());

            String strVal = properties.getProperty(key, converter.toString(defaultValue));
            return converter.fromString(strVal);
        }
    }

    public final String getProperty(String key, String defaultValue) {
        return getProperty(String.class, key, defaultValue);
    }

    public final <T> T getMandatoryProperty(Class<T> targetClass, String key) throws NoSuchElementException {
        T value = getProperty(targetClass, key, null);

        if (value == null || ((value instanceof String) && ((String)value).trim().isEmpty()))
            throw new NoSuchElementException("No such property: " + key);

        return value;
    }

    public final String getMandatoryProperty(String key) throws NoSuchElementException {
        return getMandatoryProperty(String.class, key);
    }

    /**
     * Sets an application property.
     *
     * @param key property key.
     * @param value property value.
     * @param overrideExisting defines if existing properties should be overridden.
     */
    public void setProperty(String key, String value, boolean overrideExisting) {
        synchronized(this) {
            if (!isRunning())
                throw new IllegalStateException("Application is not running");

            if (overrideExisting || !properties.containsKey(key)) {
                properties.setProperty(key, value);
            }
        }
    }

    /** Convenience method for setProperty(key, value, true). */
    public final void setProperty(String key, String value) {
        setProperty(key, value, true);
    }

    /** Convenience method for setProperty(key, value, false). */
    public final void setPropertyIfAbsent(String key, String value) {
        setProperty(key, value, false);
    }

    /**
     * Loads properties from application properties file.
     *
     * If properties files does not exists, only default properties are loaded.
     *
     * @throws IOException if an error happened during process.
     */
    public void loadProperties() throws IOException {
        __loadProperties(false);
    }

    /**
     * Saves current application properties into properties file.
     *
     * @throws IOException If an error happened during the process.
     */
    public void saveProperties() throws IOException {
        synchronized(this) {
            File propertiesFile = new File(getDirectory(), PROPERTIES_FILENAME);

            try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
                properties.store(fos, getName());
            }
        }
    }

    /**
     * Returns application default properties.
     *
     * @return application default properties. Default implementation returns null.
     */
    protected Properties getDefaultProperties() {
        return null;
    }

    /** Starts this application. */
    public void start() {
        synchronized(this) {
            if (isRunning())
                throw new IllegalStateException("Application is already running");

            if (runningInstance != null)
                throw new IllegalStateException(String.format("Another application instance is already running (class: %s, name: %s, version: %s)", runningInstance.getClass().getName(), runningInstance.getName(), runningInstance.getVersion()));

            try {
                String name = getName();

                if (name != null)
                    name = name.trim();

                if (name == null || name.isEmpty())
                    throw new RuntimeException("Missing application name");

                if (!name.matches(APP_NAME_PATTERN))
                    throw new RuntimeException("Invalid application name: " + name);

                String version = getVersion();

                if (version != null)
                    version = version.trim();

                if (version == null || version.isEmpty())
                    throw new RuntimeException("Missing application version");

                __reset();

                __loadProperties(true);

                beforeStart();

                running = true; // <-- from this point, services can be started.
                __setRunningInstance(this);
                log(LogType.INFO, "Starting application (%s - v. %s)", name, version);
                onStart();
                log(LogType.INFO, "Application is ready: (%s - v. %s)", name, version);
            } catch (Throwable ex) {
                __setRunningInstance(null);
                running = true;

                onStartError(ex);

                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                } else {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    /**
     * Called before application is initialized.
     *
     * Default implementation does nothing.
     * This is the place to register services with the application.
     */
    protected void beforeStart() {}

    /**
     * Called during application start.
     *
     * During this phase the service management is available.
     * Default implementation does nothing.
     */
    protected void onStart() {}

    /** Stops this application. */
    public void stop() {
        synchronized(this) {
            if (!isRunning())
                throw new RuntimeException("Application is not running");

            try {
                log(LogType.INFO, "Stopping aplication (%s - v. %s)", getName(), getVersion());
                beforeStop();
                onStop();
                __stopServices();
                __setRunningInstance(null);
                running = false;
                afterStop();
            } catch (Throwable ex) {
                __setRunningInstance(null);
                running = false;
                onStopError(ex);
                throw ex;
            }
        }
    }

    /**
     * Called before application stop.
     *
     * Default implementation does nothing.
     */
    protected void beforeStop() {}

    /**
     * Called during application stop.
     *
     * Default implementation does nothing.
     */
    protected void onStop() {}

    /**
     * Called after application is stopped.
     *
     * During this phase, settings and services are unavailable.
     * Default implementation does nothing.
     */
    protected void afterStop() {}

    /**
     * Called when there was an error while starting application.
     * Default implementation does nothing.
     * After this callback is invoked, given exception is thrown.
     *
     * @param ex error
     */
    protected void onStartError(Throwable ex) {}

    /**
     * Called when there was an error while stopping the application.
     * Default implementation does nothing.
     * After this callback is invoked, given exception is thrown.
     *
     * @param ex error
     */
    protected void onStopError(Throwable ex) {}

    /** Restarts this application. */
    public void restart() {
        synchronized(this) {
            log(LogType.INFO, "Restarting application: %s", getName());

            stop();
            start();
        }
    }

}