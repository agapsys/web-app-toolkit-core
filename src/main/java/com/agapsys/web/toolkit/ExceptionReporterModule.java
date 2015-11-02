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

import com.agapsys.web.toolkit.AbstractWebApplication.LogType;
import com.agapsys.web.toolkit.utils.DateUtils;
import com.agapsys.web.toolkit.utils.HttpUtils;
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
	public static final String KEY_MODULE_ENABLED           = "agapsys.webtoolkit.exceptionReporter.enabled";
	public static final String KEY_NODE_NAME                = "agapsys.webtoolkit.exceptionReporter.nodeName";
	public static final String KEY_STACK_TRACE_HISTORY_SIZE = "agapsys.webtoolkit.exceptionReporter.stackTraceHistorySize";
	// -------------------------------------------------------------------------
	
	public static final int     DEFAULT_STACK_TRACE_HISTORY_SIZE = 5;
	public static final String  DEFAULT_NODE_NAME                = "node-01";
	public static final boolean DEFAULT_MODULE_ENABLED           = true;
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	// Fields --------------------------------------------------------------
	private String  nodeName              = null;
	private int     stackTraceHistorySize = 0;
	private boolean enabled               = DEFAULT_MODULE_ENABLED;
	
	private final List<String> stackTraceHistory = new LinkedList<>();
	// -------------------------------------------------------------------------
	
	/**
	 * Returns the default stack trace history size when there is no definition.
	 * @return default stack trace history size
	 */
	protected int getDefaultStacktraceHistorySize() {
		return DEFAULT_STACK_TRACE_HISTORY_SIZE;
	}

	/**
	 * Returns the default node name when there is no definition.
	 * @return default node name
	 */
	protected String getDefaultNodeName() {
		return DEFAULT_NODE_NAME;
	}
	
	/**
	 * Returns the default enabling status of the module when there is no definition
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
			throw new RuntimeException("Null/Empty default node name");
		
		int defaultStackTraceHistorySize = getDefaultStacktraceHistorySize();
		if (defaultStackTraceHistorySize < 0)
			throw new RuntimeException("Invalid default stack trace history size: " + defaultStackTraceHistorySize);
		
		boolean defaultModuleEnabled = getDefaultModuleEnableStatus();
		
		properties.setProperty(KEY_NODE_NAME,               defaultNodeName);
		properties.setProperty(KEY_STACK_TRACE_HISTORY_SIZE, "" + defaultStackTraceHistorySize);
		properties.setProperty(KEY_MODULE_ENABLED,          "" + defaultModuleEnabled);
		
		return properties;
	}

	@Override
	protected void onStart(AbstractWebApplication webApp) {
		Properties appProperties = webApp.getProperties();
		
		String val;
		
		// isEnabled
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
		
		// stackTraceHistorySize
		val = appProperties.getProperty(KEY_STACK_TRACE_HISTORY_SIZE);
		if (val == null || val.trim().isEmpty()) {
			val = "" + getDefaultStacktraceHistorySize();
		}
		stackTraceHistorySize = Integer.parseInt(val);
	}

	@Override
	protected void onStop() {
		nodeName = null;
		stackTraceHistorySize = 0;
		stackTraceHistory.clear();
		enabled = DEFAULT_MODULE_ENABLED;
	}	
	
	/**
	 * Returns the stack trace history size defined in application settings
	 * @return stack trace history size defined in application settings
	 */
	public int getStacktraceHistorySize() {
		return stackTraceHistorySize;
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
	 * Returns the message generated for exception report.
	 * @param throwable exception instance
	 * @param req HTTP request which thrown the exception
	 * @param originalRequestUri original request URI
	 * @return error message
	 */
	protected String getErrorMessage(Throwable throwable, HttpServletRequest req, String originalRequestUri) {
		String stackTrace = getStackTrace(throwable);
		
		
		String msg =
			"An error was detected"
			+ "\n\n"
			+ "Application: "          + getApplication().getName() + "\n"
			+ "Application version: "  + getApplication().getVersion() + "\n"
			+ "Node name: "            + getNodeName() + "\n\n"
			+ "Server timestamp: "     + DateUtils.getLocalTimestamp() + "\n"
			+ "Error message: "        + throwable.getMessage() + "\n"
			+ "Original request URI: " + originalRequestUri + "\n"
			+ "Request URI: "          + HttpUtils.getRequestUri(req) + "\n"
			+ "User-agent: "           + HttpUtils.getOriginUserAgent(req) + "\n"
			+ "Client id: "            + HttpUtils.getOriginIp(req) + "\n"
			+ "Stacktrace:\n"          + stackTrace;
		
		return msg;
	}
		
	/**
	 * @param t error to test
	 * @return a boolean indicating if report shall be skipped for given error
	 */
	protected boolean skipErrorReport(Throwable t) {
		String stackTrace = getStackTrace(t);
		
		if (stackTraceHistory.contains(stackTrace)) {
			return true;
		} else {
			if (stackTraceHistory.size() == getStacktraceHistorySize())
				stackTraceHistory.remove(0); // Remove oldest
			
			stackTraceHistory.add(stackTrace);
			return false;
		}
	}
	
	@Override
	protected void onExceptionReport(Throwable t, HttpServletRequest req) {
		if (isModuleEnabled()) {
			if (!skipErrorReport(t)) {
				String originalRequestStr = (String) req.getAttribute(DefaultFilter.ATTR_ORIGINAL_REQUEST_URI);
				reportErrorMessage(getErrorMessage(t, req, originalRequestStr));
			} else {
				getApplication().log(LogType.ERROR, "Application error (already reported): %s", t.getMessage());
			}
		}
	}
	
	/** 
	 * Report the error message.
	 * @param message complete error message
	 */
	protected void reportErrorMessage(String message) {
		getApplication().log(LogType.ERROR, "Application error:\n----\n%s\n----", message);
	}
	// =========================================================================
}
