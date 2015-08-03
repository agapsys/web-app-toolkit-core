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

package com.agapsys.web;

import com.agapsys.web.WebApplication.LogType;
import com.agapsys.logger.ConsoleLoggerStream;
import com.agapsys.logger.FileLoggerStream;
import com.agapsys.logger.Logger;
import com.agapsys.web.utils.RequestUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

/**
 * Logging module.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
class LoggingModule {
	// CLASS SCOPE =============================================================
	
	// Inner classes -----------------------------------------------------------
	private static class AppLogger extends Logger {

		@Override
		public void writLog(String message) {
			writeLog(LogType.INFO, message);
		}

		@Override
		public void writeLog(String message, boolean includeTimeStamp) {
			writeLog(LogType.INFO, message, includeTimeStamp);
		}
		
		public void writeLog(LogType type, String message) {
			writeLog(type, message, true);
		}
		
		public void writeLog(LogType type, String message, boolean includeTimeStamp) {
			message = String.format("[%s] %s", type.name(), message);
			super.writeLog(message, includeTimeStamp); 
		}
	}
	// -------------------------------------------------------------------------
	
	private static final String LOG_FILENAME_PREFIX    = "log";
	private static final String LOG_FILENAME_SUFFIX    = ".log";
	private static final String LOG_FILENAME_DELIMITER = "-";
	
	private static AppLogger logger = null;
	private static File logFile = null;

	private static File getLogFile() {
		if (logFile == null)
			logFile = new File(WebApplication.getAppFolder(), LOG_FILENAME_PREFIX + LOG_FILENAME_DELIMITER + WebApplication.getEnvironment() + LOG_FILENAME_SUFFIX);
		
		return logFile;
	}
	
	/** @return boolean indicating if module is running. */
	public static boolean isRunning() {
		return logger != null;
	}
	
	/** 
	 * Starts the module module.
	 * If the module is running, nothing happens.
	 */
	public static void start() {
		if (!isRunning()) {
			try {
				logger = new AppLogger();
				logger.addStream(ConsoleLoggerStream.getSingletonInstance());
				logger.addStream(new FileLoggerStream(new FileOutputStream(getLogFile(), true)));
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}
	}
	
	/** 
	 * Stops the module.
	 * If module is not running, nothing happens.
	 */	
	public static void stop() {
		if (isRunning()) {
			logger.closeAllStreams();
			logger = null;
		}
	}
	
	/**
	 * Prints a log message
	 * @param type message type
	 * @param message log message
	 * @throws IllegalStateException if module is not running
	 */
	public static void log(LogType type, String message) throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		logger.writeLog(type, message);
	}
	
	/**
	 * Prints a log related to a request
	 * @param type message type
	 * @param request related request
	 * @param message log message
	 * @throws IllegalStateException if module is not running
	 */
	public static void log (LogType type, HttpServletRequest request, String message) throws IllegalStateException {
		if (!isRunning())
			throw new IllegalStateException("Module is not running");
		
		message = String.format("[ip: %s, user-agent: %s] %s", RequestUtils.getClientIp(request), RequestUtils.getClientUserAgent(request), message);
		log(type, message);
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private LoggingModule() {}
	// =========================================================================
}
