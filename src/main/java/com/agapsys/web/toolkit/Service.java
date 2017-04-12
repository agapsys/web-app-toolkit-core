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

/** Basic service implementation. */
public abstract class Service {

    private AbstractApplication app;
    private boolean running = false;

    final void __throwIfNotRunning() {
        if (!isRunning())
            throw new RuntimeException("Service is not running");
    }

    /**
     * Returns a boolean indicating if this instance is running.
     *
     * @return a boolean indicating if this instance is running.
     */
    public final boolean isRunning() {
        synchronized(this) {
            return running;
        }
    }

    /**
     * Starts this service instance.
     *
     * @param app associated application.
     */
    final void _start(AbstractApplication app) {
        synchronized(this) {
            if (isRunning())
                throw new IllegalStateException("Instance is already running");

            if (app == null)
                throw new IllegalArgumentException("Missing application");

            this.app = app;
            onStart();
            this.running = true;
            app.log(LogType.INFO, "Started service: %s", this.getClass().getName());

        }
    }

    /**
     * Called upon service start. Default implementation does nothing.
     */
    protected void onStart() {}

    /**
     * Stops this service instance.
     */
    final void _stop() {
        synchronized(this) {
            __throwIfNotRunning();

            onStop();
            getApplication().log(LogType.INFO, "Stopped service: %s", this.getClass().getName());
            this.running = false;
            this.app = null;
        }
    }

    /**
     * Called during service stop. Default implementation does nothing.
     */
    protected void onStop() {}

    /**
     * Return the application managing this instance.
     *
     * @return the application managing this instance.
     */
    public final AbstractApplication getApplication() {
        synchronized(this) {
            return app;
        }
    }

    /**
     * Restarts this service.
     */
    public final void restart() {
        synchronized(this) {
            AbstractApplication mApp = getApplication();
            _stop();
            _start(mApp);
        }
    }

    /**
     * Returns a service instance using the same application this service is
     * registered with.
     *
     * @param <S> Service type.
     * @param serviceClass service class.
     * @param autoRegistration defines if service should be auto-registered.
     * @return service instance.
     */
    public final <S extends Service> S getService(Class<S> serviceClass, boolean autoRegistration) {
        synchronized(this) {
            return app.getService(serviceClass, autoRegistration);
        }
    }

    /** Convenience method for getService(serviceClass, true). */
    public final <S extends Service> S getService(Class<S> serviceClass) {
        return getService(serviceClass, true);
    }

}
