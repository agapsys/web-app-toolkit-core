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
import java.util.Properties;
import java.util.Set;

/**
 * Represents a module in a {@linkplain AbstractApplication application}.
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

			Properties mainProperties = getApplication().getSettings().getProperties(getSettingsGroupName());
			if (mainProperties == null)
				mainProperties = new Properties();
			
			Properties defaults = getDefaultProperties();

			return ApplicationSettings.mergeProperties(mainProperties, defaults);
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
}
