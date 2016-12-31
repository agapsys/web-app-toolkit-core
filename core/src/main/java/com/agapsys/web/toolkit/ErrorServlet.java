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

package com.agapsys.web.toolkit;

import com.agapsys.web.toolkit.modules.ExceptionReporterModule;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Standard Servlet for handling error requests
 */
public class ErrorServlet extends HttpServlet {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    private static final String ATTR_EXCEPTION = "javax.servlet.error.exception";

    private static Throwable getException(HttpServletRequest req) {
        return (Throwable) req.getAttribute(ATTR_EXCEPTION);
    }
    // =========================================================================
    // </editor-fold>

    private <T extends Module> T getModule(Class<T> moduleClass) {
        AbstractApplication app = AbstractApplication.getRunningInstance();

        if (app == null)
            return null;

        return app.getModule(moduleClass);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Throwable t = getException(req);

        ExceptionReporterModule exceptionReporterModule = getModule(ExceptionReporterModule.class);

        if (t != null && exceptionReporterModule != null) {
            exceptionReporterModule.reportException(t, req);
        } else {
            AbstractApplication app = AbstractApplication.getRunningInstance();

            if (app != null)
                app.log(LogType.WARNING, "There is no exception reporter module registered with the application");
        }
    }

}
