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

import java.util.Properties;

/** 
 * Represents a module in a {@linkplain AbstractWebApplication web application}.
 * A module must be registered with an application and will be initialized upon
 * application initialization. A module share settings with the application and
 * will have a singleton scope controlled by associated application.
 */
public interface Module extends Singleton {
	
	/** 
	 * Starts the module.
	 * @param webApplication application initializing this module.
	 */
	public void start(AbstractWebApplication webApp);
	
	/** Stops the module. */
	public void stop();
	
	/** 
	 * Returns a boolean indicating if this module is running.
	 * @return a boolean indicating if this module is running.
	 */
	public boolean isRunning();
	
	/**
	 * Return default properties associated with this module.
	 * @return default properties associated with this module. Returning null or
	 * an empty Properties instance have the same meaning: There is no default 
	 * properties associated with the module.
	 */
	public Properties getDefaultProperties();
	
	/**
	 * Return required modules used by this module.
	 * @return required modules used by this module. Returning either null or
	 * an empty array has the same effect: Module has no dependency.
	 */
	public Module[] getDependencies();
	
	/**
	 * Return the application managing this module instance
	 * @return the application managing this module instance.
	 */
	public AbstractWebApplication getWebApplication();
	
	/**
	 * Returns another module registered in the same application as this module
	 * is registered with.
	 * @param <T> Module type
	 * @param module module class
	 * @return module class or null if given module class was not registered with
	 * associated application.
	 */
	public <T extends Module> T getModule(Class<T> module);
	
	/**
	 * Returns a service instance
	 * @param <T> Module type
	 * @param service service class
	 * @return service instance.
	 */
	public <T extends Service> T getService(Class<T> service);
}
