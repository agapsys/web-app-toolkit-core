/*
 * Copyright 2015-2016 Agapsys Tecnologia Ltda-ME.
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

package com.agapsys.web.toolkit.services;

import com.agapsys.web.toolkit.Service;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.Persistence;

/**
 * Represents a JPA persistence service.
 */
public class PersistenceService extends Service {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static final String PROPERTIES_FILE = "persistence.properties";

    public static final String DEFAULT_PERSISTENCE_UNIT_NAME = "default";

    public static interface EmFactory {
        public EntityManager getInstance();
    }
    // =========================================================================
    // </editor-fold>

    private final String persistenceUnitName;

    private final EmFactory defaultFactory = new EmFactory() {

        @Override
        public EntityManager getInstance() {
            EntityManager em = emf.createEntityManager();
            em.setFlushMode(FlushModeType.COMMIT);
            return em;
        }

    };

    private EntityManagerFactory emf = null;
    private EmFactory emFactory = null;

    /**
     * Default constructor.
     *
     * Default persistence name equals to {@linkplain PersistenceService#DEFAULT_PERSISTENCE_UNIT_NAME}.
     */
    public PersistenceService() {
        this(DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Constructor.
     *
     * Allows a custom persistence unit name.
     *
     * @param persistenceUnitName persistence unit name used by this service.
     */
    public PersistenceService(String persistenceUnitName) {
        if (persistenceUnitName == null || persistenceUnitName.trim().isEmpty())
            throw new IllegalArgumentException("Null/Empty name");

        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * Return the name of persistence unit associated with this instance.
     *
     * @return the name of persistence unit associated with this instance.
     */
    public final String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    protected File getPropertiesFile() {
        return new File(getApplication().getDirectory(), PROPERTIES_FILE);
    }

    /**
     * Returns default entity manager factory properties.
     *
     * @return Default entity manager factory properties. Default implementation returns null.
     */
    protected Properties getDefaultEmfProperties() {
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        synchronized(this) {
            Properties properties = new Properties();
            File propertiesFile = getPropertiesFile();

            if (propertiesFile.exists()) {
                try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                    properties.load(fis);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            Properties defaultEmfProperties = getDefaultEmfProperties();
            if (defaultEmfProperties == null)
                defaultEmfProperties = new Properties();

            for (Map.Entry entry : defaultEmfProperties.entrySet()) {
                String key = (String) entry.getKey();
                String value = (String) entry.getValue();

                if (!properties.containsKey(key)) {
                    properties.setProperty(key, value);
                }
            }

            Map emfProperties = new LinkedHashMap();

            for (Map.Entry entry : properties.entrySet()) {
                String key = (String) entry.getKey();
                Object value = entry.getValue();

                emfProperties.put(key, value);
            }

            emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), emfProperties);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        synchronized(this) {
            emf.close();
            emf = null;
        }
    }

    /**
     * Returns the EmFactory instance to be used by this service.
     *
     * @return EmFactory instance associated with this service instance.
     */
    protected EmFactory getEmFactory() {
        return defaultFactory;
    }

    private EmFactory __getEmFactory() {
        synchronized(this) {
            if (emFactory == null) {
                emFactory = getEmFactory();
            }

            return emFactory;
        }
    }

    /**
     * Returns an entity manager to be used by application.
     *
     * @return an entity manager to be used by application.
     */
    public final EntityManager getEntityManager() {
        synchronized(this) {
            if (!isRunning())
                throw new IllegalStateException("Service is not running");

            return __getEmFactory().getInstance();
        }
    }

}