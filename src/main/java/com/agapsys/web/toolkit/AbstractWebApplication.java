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

import com.agapsys.web.toolkit.utils.HttpUtils;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents a web application.
 */
public abstract class AbstractWebApplication extends AbstractApplication implements ServletContextListener {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    // Global settings ---------------------------------------------------------
    /** Defines if application is disabled. When an application is disabled, all requests are ignored and a {@linkplain HttpServletResponse#SC_SERVICE_UNAVAILABLE} is sent to the client. */
    public static final String KEY_APP_DISABLE = "com.agapsys.webtoolkit.appDisable";

    /** Defines a comma-delimited list of allowed origins for this application or '*' for any origin. If an origin is not accepted a {@linkplain HttpServletResponse#SC_FORBIDDEN} is sent to the client. */
    public static final String KEY_APP_ALLOWED_ORIGINS = "com.agapsys.webtoolkit.allowedOrigins";

    public static final boolean DEFAULT_APP_DISABLED        = false;
    public static final String  DEFAULT_APP_ALLOWED_ORIGINS = "*";
    public static final String  ORIGIN_DELIMITER            = ",";

    private static AbstractWebApplication singleton;
    public static AbstractWebApplication getRunningInstance() {
        return singleton;
    }
    // -------------------------------------------------------------------------
    // =========================================================================
    // </editor-fold>

    private boolean  disabled;
    private String[] allowedOrigins;

    public AbstractWebApplication() {
        super();
        reset();
    }

    /** Resets application state. */
    private void reset() {
        disabled       = DEFAULT_APP_DISABLED;
        allowedOrigins = new String[] {DEFAULT_APP_ALLOWED_ORIGINS};
    }

    /**
     * Returns a boolean indicating if application is disabled.
     *
     * @return a boolean indicating if application is disabled.
     */
    public boolean isDisabled() {
        if (!isActive())
            throw new RuntimeException("Application is not running");

        return disabled;
    }

    /**
     * Returns a boolean indicating if given request is allowed to proceed.
     *
     * @param req HTTP request.
     * @return boolean indicating if given request is allowed to proceed.
     */
    final boolean _isOriginAllowed(HttpServletRequest req) {
        return isOriginAllowed(req);
    }

    /**
     * Returns a boolean indicating if given request is allowed to proceed.
     *
     * @param req HTTP request.
     * @return boolean indicating if given request is allowed to proceed.
     */
    protected boolean isOriginAllowed(HttpServletRequest req) {
        if (!isActive())
            throw new RuntimeException("Application is not running");

        boolean isOriginAllowed = allowedOrigins.length == 1 && allowedOrigins[0].equals(DEFAULT_APP_ALLOWED_ORIGINS);

        if (isOriginAllowed)
            return true;

        String originIp = HttpUtils.getOriginIp(req);

        for (String allowedOrigin : allowedOrigins) {
            if (allowedOrigin.equals(originIp))
                return true;
        }

        return false;
    }

    @Override
    protected void beforeApplicationStart() {
        reset();
        super.beforeApplicationStart();
    }

    @Override
    protected Properties getDefaultProperties() {
        Properties defaultProperties = super.getDefaultProperties();

        defaultProperties.setProperty(KEY_APP_DISABLE,         "" + DEFAULT_APP_DISABLED);
        defaultProperties.setProperty(KEY_APP_ALLOWED_ORIGINS, DEFAULT_APP_ALLOWED_ORIGINS);

        return defaultProperties;
    }

    @Override
    protected void afterApplicationStart() {
        disabled       = Boolean.parseBoolean(getProperties().getProperty(KEY_APP_DISABLE, "" + DEFAULT_APP_DISABLED));
        allowedOrigins = getProperties().getProperty(KEY_APP_ALLOWED_ORIGINS, AbstractWebApplication.DEFAULT_APP_ALLOWED_ORIGINS).split(Pattern.quote(ORIGIN_DELIMITER));

        for (int i = 0; i < allowedOrigins.length; i++) {
            allowedOrigins[i] = allowedOrigins[i].trim();
        }
    }

    /**
     * Called during context initialization.
     *
     * @param sce Servlet context event
     */
    protected void onContextInitialized(ServletContextEvent sce) {}

    /**
     * Called during context initialization.
     *
     * @param sce Servlet context event
     */
    protected void onContextDestroyed(ServletContextEvent sce) {}

    @Override
    public final void contextInitialized(ServletContextEvent sce) {
        start();
        singleton = this;
        onContextInitialized(sce);
    }

    @Override
    public final void contextDestroyed(ServletContextEvent sce) {
        stop();
        singleton = null;
        onContextDestroyed(sce);
    }

}