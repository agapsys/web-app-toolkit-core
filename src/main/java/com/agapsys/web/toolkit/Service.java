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

import java.util.NoSuchElementException;

/** Basic service implementation. */
public abstract class Service {

    private AbstractApplication app;
    private boolean running = false;

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
            try {
                if (app == null)
                    throw new IllegalArgumentException("Missing application");

                this.app = app;
                onStart();
                this.running = true;
                app.log(LogType.INFO, "Started service: %s", this.getClass().getName());
            } catch (RuntimeException ex) {
                this.app = null;
                this.running = false;
                throw ex;
            }
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
            if (!isRunning())
                return;

            try {
                onStop();
                getApplication().log(LogType.INFO, "Stopped service: %s", this.getClass().getName());
            } finally {
                this.running = false;
                this.app = null;
            }
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

    /** See {@linkplain AbstractApplication#getService(java.lang.Class, boolean)}. */
    public <S extends Service> S getService(Class<S> serviceClass, boolean autoRegistration) {
        if (app == null)
            throw new IllegalStateException("Service is not associated with an application");
        
        return app.getService(serviceClass, autoRegistration);
    }

    public final <S extends Service> S getRegisteredService(Class<S> serviceClass) throws NoSuchElementException {
        S service = getService(serviceClass, false);
        
        if (service == null)
            throw new NoSuchElementException(serviceClass.getName());
        
        return service;
    }
    
    public final <S extends Service> S getOnDemandService(Class<S> serviceClass) {
        return getService(serviceClass, true);
    }

}
