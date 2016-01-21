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

import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.agreste.utils.HttpUtils;
import com.agapsys.web.toolkit.LogType;
import com.agapsys.web.toolkit.WebApplicationFilter;
import com.agapsys.web.toolkit.modules.AbstractExceptionReporterModule;
import com.agapsys.web.toolkit.utils.DateUtils;
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
	@Override
	public Properties getDefaultProperties() {
		Properties properties = new Properties();
		
		properties.setProperty(KEY_NODE_NAME,                DEFAULT_NODE_NAME);
		properties.setProperty(KEY_STACK_TRACE_HISTORY_SIZE, "" + DEFAULT_STACK_TRACE_HISTORY_SIZE);
		properties.setProperty(KEY_MODULE_ENABLED,           "" + DEFAULT_MODULE_ENABLED);
		
		return properties;
	}


	@Override
	protected void onStart(AbstractWebApplication webApp) {
		Properties appProperties = webApp.getProperties();
		
		String val;
		
		// isEnabled
		val = getMandatoryProperty(appProperties, KEY_MODULE_ENABLED);
		enabled = Boolean.parseBoolean(val);
		
		// nodeName
		val = getMandatoryProperty(appProperties, KEY_NODE_NAME);
		nodeName = val;
		
		// stackTraceHistorySize
		val = getMandatoryProperty(appProperties, KEY_STACK_TRACE_HISTORY_SIZE);
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
			+ "Application: "          + getWebApplication().getName() + "\n"
			+ "Application version: "  + getWebApplication().getVersion() + "\n"
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
				String originalRequestStr = (String) req.getAttribute(WebApplicationFilter.ATTR_ORIGINAL_REQUEST_URI);
				reportErrorMessage(getErrorMessage(t, req, originalRequestStr));
			} else {
				getWebApplication().log(LogType.ERROR, "Application error (already reported): %s", t.getMessage());
			}
		}
	}
	
	/** 
	 * Report the error message.
	 * @param message complete error message
	 */
	protected void reportErrorMessage(String message) {
		getWebApplication().log(LogType.ERROR, "Application error:\n----\n%s\n----", message);
	}
	// =========================================================================
}