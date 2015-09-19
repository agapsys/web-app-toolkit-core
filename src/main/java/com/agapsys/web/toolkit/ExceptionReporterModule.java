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

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents an error reporter
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class ExceptionReporterModule extends Module {
	// CLASS SCOPE =============================================================
	private static final String ATTR_STATUS_CODE    = "javax.servlet.error.status_code";
	private static final String ATTR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";
	private static final String ATTR_MESSAGE        = "javax.servlet.error.message";
	private static final String ATTR_REQUEST_URI    = "javax.servlet.error.request_uri";
	private static final String ATTR_EXCEPTION      = "javax.servlet.error.exception";
	
	static int getStatusCode(HttpServletRequest req) {
		return (Integer) req.getAttribute(ATTR_STATUS_CODE);
	}
	
	static Class<?> getExceptionType(HttpServletRequest req) {
		return (Class) req.getAttribute(ATTR_EXCEPTION_TYPE);
	}
	
	static String getExceptionMessage(HttpServletRequest req) {
		return (String) req.getAttribute(ATTR_MESSAGE);
	}
	
	static String getRequestUri(HttpServletRequest req) {
		return (String) req.getAttribute(ATTR_REQUEST_URI);
	}
	
	static Throwable getException(HttpServletRequest req) {
		return (Throwable) req.getAttribute(ATTR_EXCEPTION);
	}
	
	/** 
	 * Return a string representation of a stack trace for given error
	 * @return a string representation of a stack trace for given error
	 * @param throwable error
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	/** 
	 * Actual error report code. 
	 * This method will be called only when module is running.
	 * @param req HTTP request
	 * @param resp HTTP response
	 */
	protected abstract void onReportErroneousRequest(HttpServletRequest req, HttpServletResponse resp);
	
	/**
	 * Handles an erroneous request.
	 * If module is not running, nothing happens.
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws IllegalArgumentException if either req == null or resp == null.
	 */
	public final void reportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) throws IllegalArgumentException {
		if (req == null)
			throw new IllegalArgumentException("req == null");
		
		if (resp == null)
			throw new IllegalArgumentException("resp == null");
		
		if (isRunning()) {
			onReportErroneousRequest(req, resp);
		}
	}
	// =========================================================================
}
