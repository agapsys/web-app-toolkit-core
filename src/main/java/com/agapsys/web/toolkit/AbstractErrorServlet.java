/*
 * Copyright 2015 Agapsys Tecnologia Ltda-ME.
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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Standard Servlet for handling error requests
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractErrorServlet extends HttpServlet {
	// CLASS SCOPE =============================================================
	private static final String ATTR_EXCEPTION = "javax.servlet.error.exception";
		
	private static Throwable getException(HttpServletRequest req) {
		return (Throwable) req.getAttribute(ATTR_EXCEPTION);
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	protected abstract Class<? extends AbstractExceptionReporterModule> getExceptionReporterModuleClass();
	
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Throwable t = getException(req);
		
		AbstractExceptionReporterModule exceptionReporterModule = AbstractWebApplication.getInstance().getModuleInstance(getExceptionReporterModuleClass());
		
		if (t != null && exceptionReporterModule != null) {
			exceptionReporterModule.reportException(t, req);
		}
	}
	// =========================================================================
}
