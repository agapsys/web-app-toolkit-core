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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Represents a module in a {@linkplain AbstractWebApplication web application}.
 * A module must be registered with an application and will be initialized upon
 * application initialization. A module share settings with the application and
 * will have a singleton scope controlled by associated application.
 */

public abstract class Module extends Service {

	// -------------------------------------------------------------------------
	private final Properties defaultProperties = new Properties();
	private final Set<Class<? extends Module>> defaultDependencies = new LinkedHashSet<>();
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Called upon module initialization.
	 *
	 * Default implementation does nothing.
	 * @param webApp associated web application.
	 */
	protected void onModuleInit(AbstractWebApplication webApp) {}

	@Override
	protected final void onInit(AbstractWebApplication webApp) {
		super.onInit(webApp);
		
		onModuleInit(webApp);
	}
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Called upon module stop.
	 *
	 * Default implementation does nothing.
	 */
	protected void onModuleStop() {}

	@Override
	protected final void onStop() {
		super.onStop();
		onModuleStop();
	}
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Returns the name of settings group associated with this module.
	 *
	 * @return the name of settings group associated with this module.
	 */
	protected abstract String getSettingsGroupName();

	/**
	 * Returns the name of settings group associated with this module.
	 *
	 * @return the name of settings group associated with this module.
	 */
	String _getSettingsGroupName() {
		return getSettingsGroupName();
	}

	/**
	 * Returns an application property.
	 *
	 * @param properties properties to be processed.
	 * @param key property key.
	 * @return property value or null if there is not such property.
	 */
	protected final String getProperty(Properties properties, String key) {
		synchronized(this) {
			throwIfNotActive();

			String property = getProperties().getProperty(key, null);

			if (property != null)
				property = property.trim();

			return property;
		}
	}

	/**
	 * Returns a mandatory property.
	 *
	 * @param properties properties to be processed.
	 * @param key property key.
	 * @return application property.
	 * @throws RuntimeException if such property isn't defined.
	 */
	protected final String getMandatoryProperty(Properties properties, String key) throws RuntimeException {
		String prop = getProperty(properties, key);

		if (prop == null || prop.isEmpty())
			throw new RuntimeException(String.format("Missing property: %s", key));

		return prop;
	}

	/**
	 * Return default properties associated with this module.
	 *
	 * @return default properties associated with this module.
	 */
	protected Properties getDefaultProperties() {
		return defaultProperties;
	}

	/**
	 * @see Module#getDefaultProperties()
	 */
	Properties _getDefaultProperties() {
		return getDefaultProperties();
	}

	/**
	 * Returns the properties associated with this module.
	 *
	 * @return properties instance associated with this module.
	 */
	protected final Properties getProperties() {
		synchronized(this) {
			throwIfNotActive();

			// Properties should always be processed in order to avoid leak of confidential data.
			Properties properties = getWebApplication().getSettings().getProperties(getSettingsGroupName());
			Properties _defaultProperties = getDefaultProperties();

			for (Map.Entry defaultEntry : _defaultProperties.entrySet()) {
				properties.putIfAbsent(defaultEntry.getKey(), defaultEntry.getValue());
			}

			return properties;
		}
	}
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Return required modules used by this module.
	 *
	 * @return required modules used by this module.
	 */
	protected Set<Class<? extends Module>> getDependencies() {
		return defaultDependencies;
	}

	/**
	 * @see Module#getDependencies()
	 */
	Set<Class<? extends Module>> _getDependencies() {
		return getDependencies();
	}
	// -------------------------------------------------------------------------
}
