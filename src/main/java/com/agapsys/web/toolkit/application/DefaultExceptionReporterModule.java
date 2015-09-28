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

package com.agapsys.web.toolkit.application;

import com.agapsys.web.toolkit.ExceptionReporterModule;
import com.agapsys.web.toolkit.LoggingModule;
import com.agapsys.web.toolkit.WebApplication;
import com.agapsys.web.toolkit.utils.HttpUtils;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default implementation of an exception reporter module.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class DefaultExceptionReporterModule extends ExceptionReporterModule {
	// CLASS SCOPE =============================================================
	private static final String LOGGING_MODULE_ID = com.agapsys.web.toolkit.application.WebApplication.LOGGING_MODULE_ID;

	public static final String KEY_NODE_NAME               = "com.agapsys.web.nodeName";
	public static final String KEY_STACKTRACE_HISTORY_SIZE = "com.agapsys.web.stacktraceHistorySize";
	
	public static final int    DEFAULT_STACKTRACE_HISTORY_SIZE = 5;
	public static final String DEFAULT_NODE_NAME = "node-01";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private String nodeName = null;
	private int stacktraceHistorySize = 0;
	
	private final List<String> stacktraceHistory = new LinkedList<>();

	public DefaultExceptionReporterModule(WebApplication application) {
		super(application);
	}

	protected int getDefaultStacktraceHistorySize() {
		return DEFAULT_STACKTRACE_HISTORY_SIZE;
	}

	protected String getDefaultNodeName() {
		return DEFAULT_NODE_NAME;
	}
	
	protected String getLoggingModuleId() {
		return LOGGING_MODULE_ID;
	}
	
	public String getNodeName() {
		return nodeName;
	}
	
	public int getStacktraceHistorySize() {
		return stacktraceHistorySize;
	}
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = new Properties();
		
		String defaultNodeName = getDefaultNodeName();
		if (defaultNodeName == null)
			defaultNodeName = DEFAULT_NODE_NAME;
		
		int defaultStacktraceHistorySize = getDefaultStacktraceHistorySize();
		if (defaultStacktraceHistorySize < 0)
			defaultStacktraceHistorySize = 0;
		
		properties.setProperty(KEY_NODE_NAME, defaultNodeName);
		properties.setProperty(KEY_STACKTRACE_HISTORY_SIZE, "" + defaultStacktraceHistorySize);
		
		return properties;
	}
	
	@Override
	protected Set<String> getOptionalDependencies() {
		Set<String> deps = new LinkedHashSet<>();
		deps.add(getLoggingModuleId());
		return deps;
	}
	
	@Override
	protected void onStart() {
		WebApplication app = getApplication();
		nodeName = app.getProperties().getProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
		stacktraceHistorySize = Integer.parseInt(app.getProperties().getProperty(KEY_STACKTRACE_HISTORY_SIZE, "" + DEFAULT_STACKTRACE_HISTORY_SIZE));
	}

	@Override
	protected void onStop() {
		nodeName = null;
		stacktraceHistorySize = 0;
		stacktraceHistory.clear();
	}
	
	private LoggingModule getLoggingModule() {
		return (LoggingModule) getApplication().getModuleInstance(getLoggingModuleId());
	}
	
	/** 
	 * Logs messages in this module.
	 * If there is no logging module, just prints given message to console.
	 * @param logType log type
	 * @param message message to be logged.
	 */
	protected void log(String logType, String message) {
		LoggingModule loggingModule = getLoggingModule();
		
		if (loggingModule != null)
			loggingModule.log(logType, message);
		else		
			DefaultLoggingModule.logToConsole(logType, message);
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
		String stacktrace = ExceptionReporterModule.getStackTrace(throwable);

		String msg =
			"An error was detected"
			+ "\n\n"
			+ "Application: " + getApplication().getName() + "\n"
			+ "Application version: " + getApplication().getVersion() + "\n"
			+ "Node name: " + getNodeName() + "\n\n"
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
		log(LoggingModule.LOG_TYPE_ERROR, String.format("Application error:\n----\n%s\n----", message));
	}
	
	/**
	 * @param t error to test
	 * @return a boolean indicating if report shall be skipped for given error
	 */
	protected boolean skipErrorReport(Throwable t) {
		String stacktrace = ExceptionReporterModule.getStackTrace(t);
		
		if (stacktraceHistory.contains(stacktrace)) {
			return true;
		} else {
			if (stacktraceHistory.size() == getStacktraceHistorySize())
				stacktraceHistory.remove(0); // Remove oldest
			
			stacktraceHistory.add(stacktrace);
			return false;
		}
	}
	
	@Override
	protected void onReportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) {
		Integer statusCode      = ExceptionReporterModule.getStatusCode(req);
		Class exceptionType     = ExceptionReporterModule.getExceptionType(req);
		String exceptionMessage = ExceptionReporterModule.getExceptionMessage(req);
		String requestUri       = ExceptionReporterModule.getRequestUri(req);
		Throwable throwable     = ExceptionReporterModule.getException(req);

		String userAgent = HttpUtils.getOriginUserAgent(req);
		String clientIp = HttpUtils.getOriginIp(req);

		if (throwable != null) {
			if (!skipErrorReport(throwable))
				reportError(getErrorMessage(statusCode, throwable, exceptionType, exceptionMessage, requestUri, userAgent, clientIp));
			else
				log(LoggingModule.LOG_TYPE_WARNING, "Application error (already reported): " + throwable.getMessage());
		} else {
			String extraInfo = String.format("User-agent: %s\nClient IP:%s, Request URL: %s", userAgent, clientIp, requestUri);
			log(LoggingModule.LOG_TYPE_ERROR, String.format("Bad request for exception reporter module:\n----\n%s\n----", extraInfo));
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
	}
	// =========================================================================
}
