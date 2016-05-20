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

import java.util.Properties;

/** 
 * Represents a module in a {@linkplain AbstractWebApplication web application}.
 * A module must be registered with an application and will be initialized upon
 * application initialization. A module share settings with the application and
 * will have a singleton scope controlled by associated application.
 */

public abstract class Module extends Service {
	
	/**
	 * Returns an application property.
	 * 
	 * @param key property key.
	 * @return property value or null if there is not such property.
	 */
	protected final String getAppProperty(String key) {
		synchronized(this) {
			if (!isActive())
				throw new IllegalStateException("Instance is not active");
			
			Properties defaultProperties = getDefaultProperties();

			String defaultValue = defaultProperties != null ? defaultProperties.getProperty(key, null) : null;
			String value = getWebApplication().getProperties().getProperty(key, defaultValue);

			if (value != null)
				value = value.trim();
			
			return value;
		}
	}
	
	/**
	 * Returns a mandatory property
	 * @param key property key
	 * @return application property
	 * @throws RuntimeException if such property isn't defined.
	 */
	protected final String getMandatoryProperty(String key) throws RuntimeException {
		String prop = getAppProperty(key);
		
		if (prop == null || prop.isEmpty())
			throw new RuntimeException(String.format("Missing property: %s", key));
		
		return prop;
	}
	
	/**
	 * Return default properties associated with this module.
	 * 
	 * @return default properties associated with this module. Returning null or
	 * an empty Properties instance have the same meaning (There is no default 
	 * properties associated with the module).
	 */
	public Properties getDefaultProperties() {
		return null;
	}
	
	/**
	 * Return required modules used by this module.
	 * 
	 * @return required modules used by this module. Returning either null or
	 * an empty array has the same effect (module has no dependency).
	 */
	public Class<? extends Module>[] getDependencies() {
		return null;
	}
	
}
