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

package com.agapsys.web.toolkit.filters;

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.services.ExceptionReporterService;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ExceptionReporterFilter implements Filter {


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch(RuntimeException ex) {

            AbstractApplication app = AbstractApplication.getRunningInstance();
            ExceptionReporterService exceptionReporterService = (app != null ? app.getService(ExceptionReporterService.class, false) : null);

            if (exceptionReporterService != null) {
                exceptionReporterService.reportException(ex, (HttpServletRequest) request);
                HttpServletResponse resp = (HttpServletResponse) response;
                resp.setStatus(500);
                resp.getWriter().print("An uncaught error was detected (this error was reported)");
            } else {
                throw ex;
            }
        }
    }

    @Override
    public void destroy() {}

}
