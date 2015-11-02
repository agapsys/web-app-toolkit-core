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

import com.agapsys.web.toolkit.SingletonManager.Singleton;
import java.util.Properties;

/**
 * Basic module
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractModule implements Singleton{
	private boolean running = false;
	
	/**
	 * Convenience method to get application singleton.
	 * @return application singleton
	 */
	protected AbstractWebApplication getApplication() {
		return AbstractWebApplication.getInstance();
	}
	
	/**
	 * Returns a user-friendly name associated with module instance.
	 * @return a user-friendly name associated with module instance.
	 */
	public abstract String getTitle();
	
	/** 
	 * Return the default settings used by this module.
	 * @return default settings for this module. Default implementation returns null.
	 */
	public Properties getDefaultSettings() {
		return null;
	}

	/** 
	 * Actual module initialization code.
	 * Default implementation does nothing.
	 */
	protected void onStart(AbstractWebApplication webApp) {}
	
	/**
	 * Actual module shutdown code.
	 * Default implementation does nothing.
	 */
	protected void onStop() {}
	
	/**
	 * Returns a boolean indicating if this module is running.
	 * @return a boolean indicating if this module is running.
	 */
	public boolean isRunning() {
		return running;
	}
		
	/** Starts the module. If module is already running, nothing happens. */
	public void start(AbstractWebApplication webApp) {
		if (!running)  {
			onStart(webApp);
			running = true;
		}
	}
	
	/** Stops the module. If module is not running, nothing happens. */
	public void stop() {
		if (running) {
			onStop();
			running = false;
		}
	}
}
