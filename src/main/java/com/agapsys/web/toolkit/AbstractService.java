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

/** Basic service implementation. */
public abstract class AbstractService implements Service {
	private AbstractWebApplication webApp;
	
	@Override
	public boolean isActive() {
		return webApp != null;
	}
	
	@Override
	public final void init(AbstractWebApplication webApp) {
		if (isActive())
			throw new IllegalStateException("Service was already initialized");
			
		if (webApp == null)
			throw new IllegalArgumentException("Null web application");
		
		this.webApp = webApp;
		onInit(webApp);
	}
	
	/** Called upon module initialization. Default implementation does nothing. */
	protected void onInit(AbstractWebApplication webApp) {}

	@Override
	public final AbstractWebApplication getWebApplication() {
		return webApp;
	}

	@Override
	public final <T extends Module> T getModule(Class<T> moduleClass) {
		return webApp.getModule(moduleClass);
	}
	
	@Override
	public final <T extends Service> T getService(Class<T> serviceClass) {
		return webApp.getService(serviceClass);
	}
}
