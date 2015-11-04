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

/** Basic module implementation. */
public abstract class AbstractModule implements Module {
	private AbstractWebApplication webApp;
	
	@Override
	public final boolean isRunning() {
		return webApp != null;
	}
	
	@Override
	public final void start(AbstractWebApplication webApp) {
		if (isRunning())
			throw new IllegalStateException("Module is already running");
		
		if (webApp == null)
			throw new IllegalArgumentException("Null web application");
		
		this.webApp = webApp;
		onStart(webApp);
	}
	
	/** 
	 * Called upon module initialization.
	 * @param webApp application managing this module
	 */
	protected abstract void onStart(AbstractWebApplication webApp);
	
	@Override
	public final void stop() {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		onStop();
		
		this.webApp = null;
	}
	
	/**
	 * Actual module shutdown code.
	 * Default implementation does nothing.
	 */
	protected abstract void onStop();
	
	@Override
	public Properties getDefaultProperties() {
		return null;
	}
	
	/**
	 * Return required modules used by this module.
	 * @return required modules used by this module. Default implementation
	 * returns null.
	 */
	@Override
	public Class<? extends Module>[] getDependencies() {
		return null;
	}
	
	@Override
	public final AbstractWebApplication getWebApplication() {
		return webApp;
	}
	
	
	@Override
	public final <T extends Module> T getModule(Class<T> module) {
		return webApp.getModule(module);
	}
	
	@Override
	public final <T extends Service> T getService(Class<T> service) {
		return webApp.getService(service);
	}
}
