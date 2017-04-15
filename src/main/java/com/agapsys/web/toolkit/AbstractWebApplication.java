/*
 * Copyright 2017 Agapsys Tecnologia Ltda-ME.
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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Represents a web application
 */
public abstract class AbstractWebApplication extends AbstractApplication implements ServletContextListener {

    private String contextPath;

    @Override
    public final String getName() {
        String rootName = getRootName();

        if (contextPath == null || contextPath.equals("/") || contextPath.isEmpty()) {
            return rootName;
        } else {
            String contextName = contextPath.substring(1);
            if (contextName.equals(rootName)) {
                return rootName;
            } else {
                return rootName + "-" + contextPath.substring(1);
            }
        }
    }

    /**
     * Returns the name of the application when used in root context.
     *
     * @return the name of the application when used in root context.
     */
    public abstract String getRootName();

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
        this.contextPath = sce == null ? null : sce.getServletContext().getContextPath();

        start();
        onContextInitialized(sce);
    }

    @Override
    public final void contextDestroyed(ServletContextEvent sce) {
        stop();
        onContextDestroyed(sce);
    }

}
