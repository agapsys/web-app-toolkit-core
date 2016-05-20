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
public class Service {
	private AbstractWebApplication webApp;
	
	/**
	 * Returns a boolean indicating if this instance was initialized.
	 * 
	 * @return a boolean indicating if this instance was initialized.
	 */
	public final boolean isActive() {
		synchronized(this) {
			return webApp != null;
		}
	}
	
	/**
	 * Initializes this instance.
	 * 
	 * @param webApp associated web application.
	 */
	public final void init(AbstractWebApplication webApp) {
		synchronized(this) {
			if (isActive())
				throw new IllegalStateException("Instance was already initialized");

			if (webApp == null)
				throw new IllegalArgumentException("webApp cannot be null");

			this.webApp = webApp;
			onInit(webApp);
		}
	}
	
	/**
	 * Called upon instance initialization. Default implementation does nothing.
	 * 
	 * @param webApp associated web application
	 */
	protected void onInit(AbstractWebApplication webApp) {}
	
	/**
	 * Stops the instance.
	 */
	public final void stop() {
		synchronized(this) {
			if (!isActive())
				throw new IllegalStateException("Instance is not active");

			onStop();

			this.webApp = null;
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
	public final AbstractWebApplication getWebApplication() {
		synchronized(this) {
			return webApp;
		}
	}

	/**
	 * Returns a module registered in the same application as this instance is registered with.
	 * 
	 * @param <M> Module type
	 * @param moduleClass module class
	 * @return module instance or null if given module class was not registered with associated application.
	 */
	public final <M extends Module> M getModule(Class<M> moduleClass) {
		synchronized(this) {
			if (!isActive())
				throw new IllegalStateException("Instance is not active");
			
			return webApp.getModule(moduleClass);
		}
	}
	
	/**
	 * Returns a service instance.
	 * 
	 * @param <S> Service type
	 * @param serviceClass service class
	 * @return service instance.
	 */
	public final <S extends Service> S getService(Class<S> serviceClass) {
		synchronized(this) {
			if (!isActive())
				throw new IllegalStateException("Instance is not active");
			
			return webApp.getService(serviceClass);
		}
	}

}
