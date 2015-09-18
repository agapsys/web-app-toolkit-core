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

import com.agapsys.web.toolkit.utils.Properties;
import com.agapsys.web.toolkit.utils.HttpUtils;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default implementation of a crash reporter module.
 * 
 * When an error happens in the application and erroneous request is handled by
 * this module, application logs the error.
 * 
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class DefaultErrorReporterModule extends ErrorReporterModule {
	// CLASS SCOPE =============================================================
	private static final String ATTR_STATUS_CODE    = "javax.servlet.error.status_code";
	private static final String ATTR_EXCEPTION_TYPE = "javax.servlet.error.exception_type";
	private static final String ATTR_MESSAGE        = "javax.servlet.error.message";
	private static final String ATTR_REQUEST_URI    = "javax.servlet.error.request_uri";
	private static final String ATTR_EXCEPTION      = "javax.servlet.error.exception";
	
	public static final int DEFAULT_STACKTRACE_HISTORY_SIZE = 5;
	
	public static final String KEY_NODE_NAME = "com.agapsys.web.nodeName";
	
	public static final String DEFAULT_NODE_NAME = "node-01";
	
	private static final Properties DEFAULT_PROPERTIES;
	
	static {
		DEFAULT_PROPERTIES = new Properties();
		DEFAULT_PROPERTIES.setProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private String nodeName = null;
	private final int stacktraceHistorySize;
	private final List<String> stacktraceHistory = new LinkedList<>();
	
	public DefaultErrorReporterModule(int stacktraceHistorySize) {
		if (stacktraceHistorySize < 0)
			throw new IllegalArgumentException("Invalid stacktrace history size: " + stacktraceHistorySize);
		
		this.stacktraceHistorySize = stacktraceHistorySize;
	}
	
	public DefaultErrorReporterModule() {
		this(DEFAULT_STACKTRACE_HISTORY_SIZE);
	}

	@Override
	protected void onStart() {
		nodeName = WebApplication.getProperties().getProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
	}

	@Override
	protected void onStop() {
		nodeName = null;
	}

	@Override
	public Properties getDefaultSettings() {
		return DEFAULT_PROPERTIES;
	}
	
	/** 
	 * Returns the message generated for error report.
	 * @param statusCode HTTP status code of the error
	 * @param throwable exception instance
	 * @param exceptionType class of the exception
	 * @param exceptionMessage exception message
	 * @param requestUri related URL
	 * @param userAgent client user-agent
	 * @param clientIp client IP address
	 * @return error message
	 */
	protected String getErrorMessage(Integer statusCode, Throwable throwable, Class<?> exceptionType, String exceptionMessage, String requestUri, String userAgent, String clientIp) {
		String stacktrace = ErrorReporterModule.getStackTrace(throwable);

		String msg =
			"An error was detected"
			+ "\n\n"
			+ "Application: " + WebApplication.getName() + "\n"
			+ "Application version: " + WebApplication.getVersion() + "\n"
			+ "Node name: " + nodeName + "\n\n"
			+ "Server timestamp: " + DateUtils.getLocalTimestamp() + "\n"
			+ "Status code: " + statusCode + "\n"
			+ "Exception type: " +(exceptionType != null ? exceptionType.getName() : "null") + "\n"
			+ "Error message: " + exceptionMessage + "\n"
			+ "Request URI: " + requestUri + "\n"
			+ "User-agent: " + userAgent + "\n"
			+ "Client id: " + clientIp + "\n"
			+ "Stacktrace:\n" + stacktrace;
		
		return msg;
	}
	
	/** 
	 * Logs the error.
	 * @param message complete error message
	 */
	protected void reportError(String message) {
		WebApplication.log(WebApplication.LOG_TYPE_ERROR, String.format("Application error:\n----\n%s\n----", message));
	}
	
	/**
	 * @param t error to test
	 * @return a boolean indicating if report shall be skipped for given error
	 */
	protected boolean skipErrorReport(Throwable t) {
		String stacktrace = ErrorReporterModule.getStackTrace(t);
		
		if (stacktraceHistory.contains(stacktrace)) {
			return true;
		} else {
			if (stacktraceHistory.size() == stacktraceHistorySize) {
				stacktraceHistory.remove(0); // Remove oldest
			}
			stacktraceHistory.add(stacktrace);
			return false;
		}
	}
	
	@Override
	protected void onReportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) {
		if (isRunning()) {
			Integer statusCode = (Integer) req.getAttribute(ATTR_STATUS_CODE);
			Class exceptionType = (Class) req.getAttribute(ATTR_EXCEPTION_TYPE);
			String exceptionMessage = (String) req.getAttribute(ATTR_MESSAGE);
			String requestUri = (String) req.getAttribute(ATTR_REQUEST_URI);
			String userAgent = HttpUtils.getOriginUserAgent(req);

			String clientIp = HttpUtils.getOriginIp(req);

			Throwable throwable = (Throwable) req.getAttribute(ATTR_EXCEPTION);

			if (throwable != null) {
				if (!skipErrorReport(throwable))
					reportError(getErrorMessage(statusCode, throwable, exceptionType, exceptionMessage, requestUri, userAgent, clientIp));
				else
					WebApplication.log(WebApplication.LOG_TYPE_WARNING, "Application error (already reported): " + throwable.getMessage());
			} else {
				String extraInfo =
					"User-agent: " + userAgent + "\n"
					+ "Client id: " + clientIp;

				WebApplication.log(WebApplication.LOG_TYPE_ERROR, String.format("Bad request for maintenance module:\n----\n%s\n----", extraInfo));
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}
	// =========================================================================
}
