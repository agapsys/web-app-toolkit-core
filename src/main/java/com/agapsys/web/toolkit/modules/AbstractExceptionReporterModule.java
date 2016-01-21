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
package com.agapsys.web.toolkit.modules;

import com.agapsys.web.toolkit.AbstractModule;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;

/** Represents an exception reporter. */
public abstract class AbstractExceptionReporterModule extends AbstractModule {
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
	 * Actual exception report code.
	 * This method will be called only when module is running.
	 * @param t exception to be reported
	 * @param req HTTP request which thrown the exception
	 */
	protected abstract void onExceptionReport(Throwable t, HttpServletRequest req);
	
	/**
	 * Reports an error in the application.
	 * @param t exception to be reported
	 * @param req HTTP request which thrown the exception
	 */
	public final void reportException(Throwable t, HttpServletRequest req) {
		if (t == null)
			throw new IllegalArgumentException("null throwable");
		
		if (req == null)
			throw new IllegalArgumentException("Null request");
		
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		onExceptionReport(t, req);
	}
	// =========================================================================
}
