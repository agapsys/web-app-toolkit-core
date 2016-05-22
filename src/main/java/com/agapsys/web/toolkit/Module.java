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
 * Represents a module in a {@linkplain AbstractApplication application}.
 * A module must be registered with an application and will be initialized upon
 * application initialization. A module share settings with the application and
 * will have a singleton scope controlled by associated application.
 */

public abstract class Module extends Service {
	// STATIC SCOPE ============================================================
	/**
	 * Returns a property.
	 *
	 * @param map properties to be processed.
	 * @param key property key.
	 * @return property value or null if there is not such property.
	 */
	protected static Object getProperty(Map map, Object key) {
		synchronized(Module.class) {
			if (key == null)
				throw new IllegalArgumentException("Key cannot be null");

			Object val = map.get(key);

			if (val != null && val instanceof String) {
				String strVal = (String) val;
				strVal = strVal.trim();
				if (strVal.isEmpty())
					strVal = null;

				val = strVal;
			}

			return val;
		}
	}

	/**
	 * Returns a mandatory property.
	 *
	 * @param map properties to be processed.
	 * @param key property key.
	 * @return application property.
	 * @throws RuntimeException if such property isn't defined.
	 */
	protected static Object getMandatoryProperty(Map map, Object key) {
		synchronized(Module.class) {
			Object val = getProperty(map, key);

			if (val == null)
				throw new RuntimeException(String.format("Missing property: %s", key.toString()));

			return val;
		}
	}

	/**
	 * @see Module#getProperty(java.util.Map, java.lang.Object)
	 */
	protected static String getProperty(Properties properties, String key) {
		return (String) getProperty((Map)properties, (Object)key);
	}

	/**
	 * @see Module#getMandatoryProperty(java.util.Map, java.lang.Object)
	 */
	protected static String getMandatoryProperty(Properties properties, String key) {
		return (String) getMandatoryProperty((Map)properties, (Object)key);
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	// -------------------------------------------------------------------------
	private final Properties defaultProperties = new Properties();
	private final Set<Class<? extends Module>> defaultDependencies = new LinkedHashSet<>();
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	/**
	 * Returns the name of settings group associated with this module.
	 *
	 * @return the name of settings group associated with this module.
	 */
	protected abstract String getSettingsGroupName();

	/**
	 * @see Module#getSettingsGroupName()
	 */
	final String _getSettingsGroupName() {
		return getSettingsGroupName();
	}
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
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
	final Properties _getDefaultProperties() {
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
			Properties properties = getApplication().getSettings().getProperties(getSettingsGroupName());

			if (properties == null)
				properties = new Properties();

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
	final Set<Class<? extends Module>> _getDependencies() {
		return getDependencies();
	}
	// -------------------------------------------------------------------------
	// =========================================================================
}
