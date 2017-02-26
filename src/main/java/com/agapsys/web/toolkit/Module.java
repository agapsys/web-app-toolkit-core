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

import com.agapsys.web.toolkit.utils.Settings;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents a module in a {@linkplain AbstractApplication application}.
 *
 * A module must be registered with an application and will be initialized upon
 * application initialization. A module share settings with the application and
 * will have a singleton scope controlled by associated application.
 */

public abstract class Module extends Service {

    /**
     * Returns the name of settings section associated with this module.
     *
     * @return the name of settings section associated with this module.
     */
    protected abstract String getSettingsSection();

    /**
     * @see Module#getSettingsSection()
     */
    final String _getSettingsSection() {
        return getSettingsSection();
    }


    /**
     * Return default settings associated with this module.
     *
     * @return default settings associated with this module. Default implementation just returns null
     */
    protected Settings getDefaultSettings() {
        return null;
    }

    /**
     * @see Module#getDefaultSettings()
     */
    final Settings _getDefaultSettings() {
        Settings settings = getDefaultSettings();
        if (settings == null)
            return new Settings();

        return settings;
    }

    /**
     * Returns the settings associated with this module.
     *
     * @return settings associated with this module.
     */
    protected final Settings getSettings() {
        synchronized(this) {
            throwIfNotActive();
            Settings settings = getApplication().getApplicationSettings().getSection(getSettingsSection());
            if (settings == null)
                settings = new Settings();

            return settings;
        }
    }

    /**
     * Return required modules used by this module.
     *
     * @return required modules used by this module. Default implementation returns null.
     */
    protected Set<Class<? extends Module>> getDependencies() {
        return null;
    }

    /**
     * @see Module#getDependencies()
     */
    final Set<Class<? extends Module>> _getDependencies() {
        Set<Class<? extends Module>> dependencies = getDependencies();
        if (dependencies == null)
            return new LinkedHashSet<>();

        return dependencies;
    }
    // -------------------------------------------------------------------------
}
