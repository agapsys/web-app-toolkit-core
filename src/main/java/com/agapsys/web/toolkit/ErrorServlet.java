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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(ErrorServlet.URL)
/**
 * Standard Servlet for handling error requests
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ErrorServlet extends HttpServlet {
	// CLASS SCOPE =============================================================
	public static final String URL = "/error";
	
	private static final String ATTR_STATUS_CODE    = "javax.servlet.error.status_code";
	private static final String ATTR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";
	private static final String ATTR_MESSAGE        = "javax.servlet.error.message";
	private static final String ATTR_REQUEST_URI    = "javax.servlet.error.request_uri";
	private static final String ATTR_EXCEPTION      = "javax.servlet.error.exception";
	
	private static int getStatusCode(HttpServletRequest req) {
		return (Integer) req.getAttribute(ATTR_STATUS_CODE);
	}
	
	private static Class<?> getExceptionType(HttpServletRequest req) {
		return (Class) req.getAttribute(ATTR_EXCEPTION_TYPE);
	}
	
	private static String getExceptionMessage(HttpServletRequest req) {
		return (String) req.getAttribute(ATTR_MESSAGE);
	}
	
	private static String getRequestUri(HttpServletRequest req) {
		return (String) req.getAttribute(ATTR_REQUEST_URI);
	}
	
	private static Throwable getException(HttpServletRequest req) {
		return (Throwable) req.getAttribute(ATTR_EXCEPTION);
	}
	

	
	
	
	
	
	
	
	
	
	
	// =========================================================================

	// INSTANCE SCOPE ==========================================================		
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Throwable t = getException(req);
		AbstractExceptionReporterModule exceptionReporterModule = (AbstractExceptionReporterModule) WebApplication.getInstance().getModuleInstance(WebApplication.EXCEPTION_REPORTER_MODULE_ID);
		if (t != null && exceptionReporterModule != null) {
			exceptionReporterModule.reportException(t, req);
		}
	}
	// =========================================================================
}
