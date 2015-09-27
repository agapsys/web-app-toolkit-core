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
import com.agapsys.web.toolkit.utils.Properties;
import com.agapsys.web.toolkit.utils.HttpUtils;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
public class DefaultExceptionReporterModule extends ExceptionReporterModule {
	// CLASS SCOPE =============================================================
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
	private final List<String> stacktraceHistory = new LinkedList<>();
	private final Set<String> mandatoryDependencies = new LinkedHashSet<>();

	public DefaultExceptionReporterModule(WebApplication application) {
		super(application);
		mandatoryDependencies.add(com.agapsys.web.toolkit.application.WebApplication.LOGGING_MODULE_ID);
	}

	@Override
	protected Set<String> getMandatoryDependencies() {
		return mandatoryDependencies;
	}
	
	protected int getStacktraceHistorySize() {
		return DEFAULT_STACKTRACE_HISTORY_SIZE;
	}

	@Override
	protected void onStart() {
		nodeName = WebApplication.getInstance().getProperties().getProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
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
		String stacktrace = ExceptionReporterModule.getStackTrace(throwable);

		String msg =
			"An error was detected"
			+ "\n\n"
			+ "Application: " + WebApplication.getInstance().getName() + "\n"
			+ "Application version: " + WebApplication.getInstance().getVersion() + "\n"
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
	
	protected LoggingModule getLoggingModule() {
		return (LoggingModule) getApplication().getModuleInstance(com.agapsys.web.toolkit.application.WebApplication.LOGGING_MODULE_ID);
	}
	
	/** 
	 * Logs the error.
	 * @param message complete error message
	 */
	protected void reportError(String message) {
		getLoggingModule().log(LoggingModule.LOG_TYPE_ERROR, String.format("Application error:\n----\n%s\n----", message));
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
			if (stacktraceHistory.size() == getStacktraceHistorySize()) {
				stacktraceHistory.remove(0); // Remove oldest
			}
			stacktraceHistory.add(stacktrace);
			return false;
		}
	}
	
	@Override
	protected void onReportErroneousRequest(HttpServletRequest req, HttpServletResponse resp) {
		if (isRunning()) {
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
					getLoggingModule().log(LoggingModule.LOG_TYPE_WARNING, "Application error (already reported): " + throwable.getMessage());
			} else {
				String extraInfo = String.format("User-agent: %s\nClient IP:%s, Request URL: %s", userAgent, clientIp, requestUri);
				getLoggingModule().log(LoggingModule.LOG_TYPE_ERROR, String.format("Bad request for maintenance module:\n----\n%s\n----", extraInfo));
				resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		}
	}
	// =========================================================================
}
