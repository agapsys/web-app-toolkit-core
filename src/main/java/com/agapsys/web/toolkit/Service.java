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

	final void throwIfNotActive() {
		if (!isActive())
			throw new RuntimeException("Instance is not active");
	}

	/**
	 * Returns a boolean indicating if this instance was initialized.
	 *
	 * @return a boolean indicating if this instance was initialized.
	 */
	public final boolean isActive() {
		synchronized(this) {
			return app != null;
		}
	}

	/**
	 * Initializes this instance.
	 *
	 * @param app associated application.
	 */
	public final void init(AbstractApplication app) {
		synchronized(this) {
			if (isActive())
				throw new IllegalStateException("Instance was already initialized");

			if (app == null)
				throw new IllegalArgumentException("Application cannot be null");

			this.app = app;
			onInit(app);
		}
	}

	/**
	 * Called upon instance initialization. Default implementation does nothing.
	 *
	 * @param app associated application.
	 */
	protected void onInit(AbstractApplication app) {}

	/**
	 * Stops the instance.
	 */
	public final void stop() {
		synchronized(this) {
			throwIfNotActive();

			onStop();

			this.app = null;
		}
	}

	/**
	 * Actual instance shutdown code.
	 *
	 * Default implementation does nothing.
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
	 * Returns a module registered in the same application as this instance is registered with.
	 *
	 * @param <M> Module type.
	 * @param moduleClass module class.
	 * @return module instance or null if given module class was not registered with associated application..
	 */
	public final <M extends Module> M getModule(Class<M> moduleClass) {
		synchronized(this) {
			throwIfNotActive();

			return app.getModule(moduleClass);
		}
	}

	/**
	 * Returns a service instance.
	 *
	 * @param <S> Service type.
	 * @param serviceClass service class.
	 * @return service instance.
	 */
	public final <S extends Service> S getService(Class<S> serviceClass) {
		synchronized(this) {
			throwIfNotActive();

			return app.getService(serviceClass);
		}
	}

}
