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
 * Represents a JPA persistence module.
 */
public class PersistenceService extends Service {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static final String PROPERTIES_FILE = "persistence.properties";

    public static final String KEY_JDBC_PASSWORD = "javax.persistence.jdbc.password";

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
     * Default persistence name equals to {@linkplain PersistenceModule#DEFAULT_PERSISTENCE_UNIT_NAME}.
     */
    public PersistenceService() {
        this(DEFAULT_PERSISTENCE_UNIT_NAME);
    }

    /**
     * Constructor.
     *
     * Allows a custom persistence unit name.
     *
     * @param persistenceUnitName persistence unit name used by this module.
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

    @Override
    protected void onStart() {
        super.onStart();

        Properties properties = new Properties();
        File propertiesFile = getPropertiesFile();

        if (propertiesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propertiesFile)) {
                properties.load(fis);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        Map additionalProperties = new LinkedHashMap();

        for (Map.Entry entry : properties.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();

            if (key.equals(KEY_JDBC_PASSWORD)) {
                value = ((String) value).toCharArray();
            }

            additionalProperties.put(key, value);
        }

        emf = Persistence.createEntityManagerFactory(getPersistenceUnitName(), additionalProperties);
    }

    @Override
    protected void onStop() {
        super.onStop();

        emf.close();
        emf = null;
    }

    /**
     * Returns the EmFactory instance to be used by this module.
     *
     * @return EmFactory instance associated with this module instance.
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

    /** This method exists just for testing purposes. */
    EntityManager _getEntityManager() {
         if (!isRunning())
            throw new IllegalStateException("Service is not active");

        return __getEmFactory().getInstance();
     }

    /**
     * Returns an entity manager to be used by application.
     *
     * @return an entity manager to be used by application.
     */
    public final EntityManager getEntityManager() {
        synchronized(this) {
            return _getEntityManager();
        }
    }

}