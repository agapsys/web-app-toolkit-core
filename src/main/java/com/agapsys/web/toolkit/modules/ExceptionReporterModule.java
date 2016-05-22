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

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.ApplicationSettings;
import com.agapsys.web.toolkit.LogType;
import com.agapsys.web.toolkit.WebApplicationFilter;
import com.agapsys.web.toolkit.services.AttributeService;
import com.agapsys.web.toolkit.utils.DateUtils;
import com.agapsys.web.toolkit.utils.HttpUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;

/**
 * Represents an exception reporter.
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ExceptionReporterModule extends WebModule {
	// CLASS SCOPE =============================================================

	public static final String SETTINGS_GROUP_NAME = ExceptionReporterModule.class.getName();

	// -------------------------------------------------------------------------
	public static final String KEY_MODULE_ENABLED           = SETTINGS_GROUP_NAME + ".enabled";
	public static final String KEY_NODE_NAME                = SETTINGS_GROUP_NAME + ".nodeName";
	public static final String KEY_STACK_TRACE_HISTORY_SIZE = SETTINGS_GROUP_NAME + ".stackTraceHistorySize";
	// -------------------------------------------------------------------------

	// -------------------------------------------------------------------------
	public static final int     DEFAULT_STACK_TRACE_HISTORY_SIZE = 5;
	public static final String  DEFAULT_NODE_NAME                = "node-01";
	public static final boolean DEFAULT_MODULE_ENABLED           = true;
	// -------------------------------------------------------------------------

	/**
	 * Return a string representation of a stack trace for given error.
	 *
	 * @return a string representation of a stack trace for given error.
	 * @param throwable error.
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));

		return stringWriter.toString();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	// -------------------------------------------------------------------------
	private final List<String> stackTraceHistory = new LinkedList<>();

	private String  nodeName              = DEFAULT_NODE_NAME;
	private int     stackTraceHistorySize = DEFAULT_STACK_TRACE_HISTORY_SIZE;
	private boolean enabled               = DEFAULT_MODULE_ENABLED;

	private AttributeService attributeService;
	// -------------------------------------------------------------------------

	public ExceptionReporterModule() {
		reset();
	}

	private void reset() {
		nodeName = DEFAULT_NODE_NAME;
		stackTraceHistorySize = DEFAULT_STACK_TRACE_HISTORY_SIZE;
		stackTraceHistory.clear();
		enabled = DEFAULT_MODULE_ENABLED;
	}

	@Override
	protected final String getSettingsGroupName() {
		return SETTINGS_GROUP_NAME;
	}

	@Override
	protected Properties getDefaultProperties() {
		Properties properties = super.getDefaultProperties();

		properties.setProperty(KEY_NODE_NAME,                DEFAULT_NODE_NAME);
		properties.setProperty(KEY_STACK_TRACE_HISTORY_SIZE, "" + DEFAULT_STACK_TRACE_HISTORY_SIZE);
		properties.setProperty(KEY_MODULE_ENABLED,           "" + DEFAULT_MODULE_ENABLED);

		return properties;
	}

	@Override
	protected void onInit(AbstractApplication app) {
		super.onInit(app);

		reset();

		Properties props = getProperties();

		attributeService = getService(AttributeService.class);

		String val;

		// isEnabled
		val = ApplicationSettings.getMandatoryProperty(props, KEY_MODULE_ENABLED);
		enabled = Boolean.parseBoolean(val);

		// nodeName
		val = ApplicationSettings.getMandatoryProperty(props, KEY_NODE_NAME);
		nodeName = val;

		// stackTraceHistorySize
		val = ApplicationSettings.getMandatoryProperty(props, KEY_STACK_TRACE_HISTORY_SIZE);
		stackTraceHistorySize = Integer.parseInt(val);
	}

	/**
	 * Returns the stack trace history size defined in application settings.
	 *
	 * @return stack trace history size defined in application settings.
	 */
	public int getStacktraceHistorySize() {
		return stackTraceHistorySize;
	}

	/**
	 * Returns the node name defined in application settings.
	 *
	 * @return the node name defined in application settings.
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * Returns a boolean status indicating if module is enabled.
	 *
	 * @return a boolean status indicating if module is enabled (this property is defined in application settings).
	 */
	public boolean isModuleEnabled() {
		return enabled;
	}

	/**
	 * Returns the message generated for exception report.
	 *
	 * @param throwable exception instance.
	 * @param req HTTP request which thrown the exception.
	 * @param originalRequestUri original request URI.
	 * @return error message.
	 */
	protected String getErrorMessage(Throwable throwable, HttpServletRequest req, String originalRequestUri) {
		String stackTrace = getStackTrace(throwable);

		AbstractApplication app = getApplication();
		HttpUtils httpUtils = HttpUtils.getInstance();

		String msg =
			"An error was detected"
			+ "\n\n"
			+ "Application: "          + app.getName() + "\n"
			+ "Application version: "  + app.getVersion() + "\n"
			+ "Node name: "            + getNodeName() + "\n\n"
			+ "Server timestamp: "     + DateUtils.getInstance().getIso8601Date() + "\n"
			+ "Error message: "        + throwable.getMessage() + "\n"
			+ "Original request URI: " + originalRequestUri + "\n"
			+ "Request URI: "          + httpUtils.getRequestUri(req) + "\n"
			+ "User-agent: "           + httpUtils.getOriginUserAgent(req) + "\n"
			+ "Client id: "            + httpUtils.getOriginIp(req) + "\n"
			+ "Stacktrace:\n"          + stackTrace;

		return msg;
	}

	/**
	 * @param t error to test.
	 *
	 * @return a boolean indicating if report shall be skipped for given error.
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

	/**
	 * Actual exception report code.
	 *
	 * This method will be called only when module is running.
	 * @param t exception to be reported.
	 * @param req HTTP request which thrown the exception.
	 */
	protected void onExceptionReport(Throwable t, HttpServletRequest req) {
		if (isModuleEnabled()) {
			if (!skipErrorReport(t)) {
				String originalRequestStr = (String) attributeService.getAttribute(WebApplicationFilter.ATTR_ORIGINAL_REQUEST_URI);
				reportErrorMessage(getErrorMessage(t, req, originalRequestStr));
			} else {
				getApplication().log(LogType.ERROR, "Application error (already reported): %s", t.getMessage());
			}
		}
	}

	/**
	 * Report the error message.
	 *
	 * @param message complete error message.
	 */
	protected void reportErrorMessage(String message) {
		getApplication().log(LogType.ERROR, "Application error:\n----\n%s\n----", message);
	}

	/**
	 * Reports an error in the application.
	 *
	 * @param t exception to be reported.
	 * @param req HTTP request which thrown the exception.
	 */
	public final void reportException(Throwable t, HttpServletRequest req) {
		synchronized(this) {
			if (t == null)
				throw new IllegalArgumentException("null throwable");

			if (req == null)
				throw new IllegalArgumentException("Null request");

			if (!isActive())
				throw new IllegalStateException("Module is not running");

			onExceptionReport(t, req);
		}
	}
	// =========================================================================
}
