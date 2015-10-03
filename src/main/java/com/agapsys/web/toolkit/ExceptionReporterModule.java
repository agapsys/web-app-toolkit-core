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

import com.agapsys.web.toolkit.utils.HttpUtils;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

/**
 * Default implementation of an exception reporter module.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ExceptionReporterModule extends AbstractExceptionReporterModule {
	// CLASS SCOPE =============================================================
	// SETTINGS ----------------------------------------------------------------
	public static final String KEY_MODULE_ENABLED          = "agapsys.webtoolkit.exceptionReporter.enabled";
	public static final String KEY_NODE_NAME               = "agapsys.webtoolkit.exceptionReporter.nodeName";
	public static final String KEY_STACKTRACE_HISTORY_SIZE = "agapsys.webtoolkit.exceptionReporter.stacktraceHistorySize";
	// -------------------------------------------------------------------------
	
	public static final int     DEFAULT_STACKTRACE_HISTORY_SIZE = 5;
	public static final String  DEFAULT_NODE_NAME               = "node-01";
	public static final boolean DEFAULT_MODULE_ENABLED          = true;
	
	/** 
	 * Return a string representation of a stack trace for given error
	 * @return a string representation of a stack trace for given error
	 * @param throwable error
	 */
	protected static String getStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	// Fields --------------------------------------------------------------
	private String nodeName = null;
	private int stacktraceHistorySize = 0;
	private boolean enabled = DEFAULT_MODULE_ENABLED;
	
	private final List<String> stacktraceHistory = new LinkedList<>();
	// -------------------------------------------------------------------------
	public ExceptionReporterModule(AbstractWebApplication application) {
		super(application);
	}

	/**
	 * Returns the default stacktrace history size
	 * @return default stacktrace history size
	 */
	protected int getDefaultStacktraceHistorySize() {
		return DEFAULT_STACKTRACE_HISTORY_SIZE;
	}

	/**
	 * Returns the default node name
	 * @return default node name
	 */
	protected String getDefaultNodeName() {
		return DEFAULT_NODE_NAME;
	}
	
	/**
	 * Returns the default enabling status of the module
	 * @return default enabling status
	 */
	protected boolean getDefaultModuleEnableStatus() {
		return DEFAULT_MODULE_ENABLED;
	}
	
	@Override
	public Properties getDefaultSettings() {
		Properties properties = new Properties();
		
		String defaultNodeName = getDefaultNodeName();
		if (defaultNodeName == null || defaultNodeName.trim().isEmpty())
			defaultNodeName = DEFAULT_NODE_NAME;
		
		int defaultStacktraceHistorySize = getDefaultStacktraceHistorySize();
		if (defaultStacktraceHistorySize < 0)
			defaultStacktraceHistorySize = 0;
		
		boolean defaultModuleEnabled = getDefaultModuleEnableStatus();
		
		properties.setProperty(KEY_NODE_NAME, defaultNodeName);
		properties.setProperty(KEY_STACKTRACE_HISTORY_SIZE, "" + defaultStacktraceHistorySize);
		properties.setProperty(KEY_MODULE_ENABLED, "" + defaultModuleEnabled);
		
		return properties;
	}

	@Override
	protected void onStart() {
		Properties appProperties = getApplication().getProperties();
		
		String val;
		
		// isEnabled?
		val = appProperties.getProperty(KEY_MODULE_ENABLED);
		if (val == null || val.trim().isEmpty()) {
			val = "" + getDefaultModuleEnableStatus();
		}
		enabled = Boolean.parseBoolean(val);
		
		// nodeName
		val = appProperties.getProperty(KEY_NODE_NAME);
		if (val == null || val.trim().isEmpty()) {
			nodeName = getDefaultNodeName();
		} else {
			nodeName = val;
		}
		
		// stacktraceHistorySize
		val = appProperties.getProperty(KEY_STACKTRACE_HISTORY_SIZE);
		if (val == null || val.trim().isEmpty()) {
			val = "" + getDefaultStacktraceHistorySize();
		}
		stacktraceHistorySize = Integer.parseInt(val);
	}

	@Override
	protected void onStop() {
		nodeName = null;
		stacktraceHistorySize = 0;
		stacktraceHistory.clear();
		enabled = DEFAULT_MODULE_ENABLED;
	}	
	
	/**
	 * Returns the stacktrace history size defined in application settings
	 * @return stacktrace history size defined in application settings
	 */
	public int getStacktraceHistorySize() {
		return stacktraceHistorySize;
	}
	
	/**
	 * Returns the node name defined in application settings.
	 * @return the node name defined in application settings.
	 */
	public String getNodeName() {
		return nodeName;
	}
	
	/**
	 * Returns a boolean status indicating if module is enabled
	 * @return a boolean status indicating if module is enabled (this property is defined in application settings).
	 */
	public boolean isModuleEnabled() {
		return enabled;
	}
	
	/** 
	 * Returns the message generated for error report.
	 * @param throwable exception instance
	 * @param req HTTP request which thrown the exception
	 * @return error message
	 */
	protected String getErrorMessage(Throwable throwable, HttpServletRequest req) {
		String stacktrace = getStackTrace(throwable);
		
		String originalRequestUrl = (String) req.getAttribute(RequestFilter.ATTR_ORIGINAL_REQUEST_URL);
		if (originalRequestUrl == null) {
			originalRequestUrl = HttpUtils.getRequestUrl(req);
		}
		
		String msg =
			"An error was detected"
			+ "\n\n"
			+ "Application: "         + getApplication().getName() + "\n"
			+ "Application version: " + getApplication().getVersion() + "\n"
			+ "Node name: "           + getNodeName() + "\n\n"
			+ "Server timestamp: "    + DateUtils.getLocalTimestamp() + "\n"
			+ "Error message: "       + throwable.getMessage() + "\n"
			+ "Request URI: "         + originalRequestUrl + "\n"
			+ "User-agent: "          + HttpUtils.getOriginUserAgent(req) + "\n"
			+ "Client id: "           + HttpUtils.getOriginIp(req) + "\n"
			+ "Stacktrace:\n"         + stacktrace;
		
		return msg;
	}
		
	/**
	 * @param t error to test
	 * @return a boolean indicating if report shall be skipped for given error
	 */
	protected boolean skipErrorReport(Throwable t) {
		String stacktrace = getStackTrace(t);
		
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
	protected void onExceptionReport(Throwable t, HttpServletRequest req) {
		if (enabled) {
			if (!skipErrorReport(t)) {
				reportErrorMessage(getErrorMessage(t, req));
			} else {
				AbstractWebApplication.logToConsole(
					AbstractWebApplication.LOG_TYPE_WARNING, 
					String.format("Application error (already reported): " + t.getMessage())
				);
			}
		}
	}
	
	/** 
	 * Report the error message.
	 * @param message complete error message
	 */
	protected void reportErrorMessage(String message) {
		AbstractWebApplication.logToConsole(AbstractWebApplication.LOG_TYPE_ERROR, String.format("Application error:\n----\n%s\n----", message));
	}
	// =========================================================================
}
