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
package com.agapsys.web.modules;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents an error reporter
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class ErrorReporterModule extends Module {
	// CLASS SCOPE =============================================================
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
