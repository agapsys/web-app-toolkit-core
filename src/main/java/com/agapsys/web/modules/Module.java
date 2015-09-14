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
package com.agapsys.web.modules;

import com.agapsys.web.utils.Properties;

/**
 * Basic module interface
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class Module {
	private boolean running = false;
	
	/** 
	 * Return the default settings used by this module.
	 * @return default settings for this module. Default implementation returns
	 * null
	 */
	public Properties getDefaultSettings() {
		return null;
	}

	/** 
	 * Actual module initialization code.
	 * Default implementation does nothing.
	 */
	protected void onStart() {}
	
	/**
	 * Actual module shutdown code.
	 * Default implementation does nothing.
	 */
	protected void onStop() {}
	
	/** @return a boolean indicating if this module is running. */
	public final boolean isRunning() {
		return running;
	}
	
	/** Starts the module. If module is already running, nothing happens. */
	public final void start() {
		if (!running) {
			onStart();
			running = true;
		}
	}
	
	/** Stops the module. If module is not running, nothing happens. */
	public final void stop() {
		if (running) {
			onStop();
			running = false;
		}
	}
}
