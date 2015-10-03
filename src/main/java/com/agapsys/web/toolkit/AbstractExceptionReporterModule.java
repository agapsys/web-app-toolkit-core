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

import javax.servlet.http.HttpServletRequest;

/**
 * Represents an error reporter
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class AbstractExceptionReporterModule extends AbstractModule {
	// CLASS SCOPE =============================================================
	private static final String DESCRIPTION = "Exception reporter module";
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	public AbstractExceptionReporterModule(AbstractWebApplication application) {
		super(application);
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
	
	/**
	 * Actual error report code.
	 * This method will be called only when module is running.
	 * @param t exception to be reported
	 * @param req HTTP request which thrown the exception
	 */
	protected abstract void onExceptionReport(Throwable t, HttpServletRequest req);
	
	/**
	 * Handles an erroneous request.
	 * If module is not running, nothing happens.
	 * @param t exception to be reported
	 * @param req HTTP request which thrown the exception
	 */
	public final void reportException(Throwable t, HttpServletRequest req) throws IllegalArgumentException {
		if (t == null)
			throw new IllegalArgumentException("null throwable");
		
		if (req == null)
			throw new IllegalArgumentException("Null request");
		
		if (isRunning()) {
			onExceptionReport(t, req);
		}
	}
	// =========================================================================
}
