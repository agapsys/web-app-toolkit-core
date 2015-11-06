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

/** 
 * Represents a service in a {@linkplain AbstractWebApplication web application}.
 * A service has a singleton scope managed by associated application and are
 * lazy loaded.
 */
public interface Service {
	/**
	 * Initializes this module
	 * @param webApp associated web application.
	 */
	public void init(AbstractWebApplication webApp);
	
	/**
	 * Returns a boolean indicating if this module was initialized.
	 * @return a boolean indicating if this module was initialized.
	 */
	public boolean isActive();
	
	/**
	 * Return the application managing this module instance
	 * @return the application managing this module instance.
	 */
	public AbstractWebApplication getWebApplication();
	
	/**
	 * Returns another module registered in the same application as this module
	 * is registered with.
	 * @param <T> Module type
	 * @param moduleClass module class
	 * @return module class or null if given module class was not registered with
	 * associated application.
	 */
	public <T extends Module> T getModule(Class<T> moduleClass);
	
	/**
	 * Returns a service instance
	 * @param <T> Module type
	 * @param serviceClass service class
	 * @return service instance.
	 */
	public <T extends Service> T getService(Class<T> serviceClass);
}
