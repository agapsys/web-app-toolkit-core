/*
 * Copyright 2016 Agapsys Tecnologia Ltda-ME.
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
import com.agapsys.web.toolkit.LogType;
import com.agapsys.web.toolkit.Module;
import com.agapsys.web.toolkit.utils.DateUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public final class LogModule extends Module {
	// STATIC SCOPE ============================================================
	public static String SETTINGS_GROUP_NAME = LogModule.class.getName();

	/**
	 * Represents a log stream.
	 */
	public static abstract class LogStream {
		static final LogStream[] EMPTY_ARRAY = new LogStream[]{};

		private boolean active = false;
		private LogModule logModule;

		/**
		 * Associates this instance with a log module.
		 *
		 * @param logModule associated log module
		 * @param allowNull defines if logModule parameter may be null
		 */
		private void setLogModule(LogModule logModule, boolean allowNull) {
			synchronized(this) {
				if (logModule == null && !allowNull)
					throw new IllegalArgumentException("Log module cannot be null");

				if (this.logModule != null && !allowNull)
					throw new IllegalStateException("This instance is already associated with a module");

				this.logModule = logModule;
			}
		}

		/**
		 * Returns associated log module.
		 *
		 * @return associated log module. If this instance was not registered with a module (via {@linkplain LogModule#addStream(com.agapsys.web.toolkit.modules.LogModule.LogStream) }) returns null.
		 */
		public LogModule getLogModule() {
			return logModule;
		}

		/**
		 * Prints a line into log stream.
		 *
		 * @param timestamp log timestamp.
		 * @param logType log type.
		 * @param message message to be print.
		 */
		public final void println(Date timestamp, LogType logType, String message) {
			synchronized(this) {
				onPrintln(timestamp, logType, message);
			}
		}

		/**
		 * Called upon log print.
		 *
		 * @param timestamp log timestamp.
		 * @param logType log type.
		 * @param message message to be print.
		 */
		protected abstract void onPrintln(Date timestamp, LogType logType, String message);

		/**
		 * Returns a boolean indicating if this stream is active.
		 *
		 * @return a boolean indicating if this stream is active.
		 */
		public final boolean isActive() {
			synchronized(this) {
				return active;
			}
		}

		/**
		 * Stops the stream.
		 *
		 * If the stream is not active, nothing happens.
		 */
		public final void stop() {
			synchronized(this) {
				if (isActive()) {
					onStop();
					active = false;
				}
			}
		}

		/**
		 * Called during stop. Default implementation does nothing.
		 */
		protected void onStop() {}

		/**
		 * Initializes the module.
		 *
		 * If the stream is already active, nothing happens.
		 * @param logModule associated log module.
		 */
		@Deprecated
		public final void init(LogModule logModule) {
			synchronized(this) {
				if (!isActive()) {
					onInit(logModule);
					active = true;
				}
			}
		}

		/**
		 * Initializes this stream.
		 */
		private final void init() {
			synchronized(this) {
				if (!isActive()) {
					onInit(getLogModule());
					active = true;
				}
			}
		}

		/**
		 * Called during initialization.
		 *
		 * Default implementation does nothing.
		 * @param logModule associated log module.
		 */
		protected void onInit(LogModule logModule) {}
	}

	/**
	 * Stream which prints messages into console.
	 */
	public static class ConsoleLogStream extends LogStream {

		public ConsoleLogStream() {
			super.init();
		}

		protected String getMessage(Date timestamp, LogType logType, String message) {
			return String.format("%s [%s] %s", DateUtils.getInstance().getIso8601Date(), logType.name(), message);
		}

		@Override
		public void onPrintln(Date timestamp, LogType logType, String message) {
			System.out.println(getMessage(timestamp, logType, message));
		}

		@Override
		protected final void onInit(LogModule logModule) {
			super.onInit(logModule);
		}
	}

	/**
	 * Stream which prints messages in a daily log file
	 */
	public static class DailyLogFileStream extends LogStream {
		private static final String DEFAULT_PATTERN = "application-%s.log";

		private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyMMdd");

		private final File   logDir;
		private final String filenamePattern;

		private File currentFile;
		private PrintStream currentPs;

		public DailyLogFileStream(File logDir) {
			this(logDir, DEFAULT_PATTERN);
		}

		public DailyLogFileStream(File logDir, String filenamePattern) {
			if (logDir == null)
				throw new IllegalArgumentException("Log directory cannot be null");

			this.logDir = logDir;

			if (filenamePattern == null || filenamePattern.trim().isEmpty())
				throw new IllegalArgumentException("Null/Empty file pattern");

			this.filenamePattern = filenamePattern;

			currentFile = _getLogFile(logDir, filenamePattern);
			super.init();
		}

		private File _getLogFile(File logDir, String filenamePattern) {
			String logFilename = String.format(filenamePattern, SDF.format(new Date()));
			return new File(logDir, logFilename);
		}

		public File getLogDir() {
			return logDir;
		}

		public String getFilenamePattern() {
			return filenamePattern;
		}

		protected String getMessage(Date timestamp, LogType logType, String message) {
			return String.format("%s [%s] %s", DateUtils.getInstance().getIso8601Date(), logType.name(), message);
		}

		@Override
		protected void onPrintln(Date timestamp, LogType logType, String message) {
			File logFile = _getLogFile(getLogDir(), getFilenamePattern());

			if (!logFile.equals(currentFile)) {
				currentPs.close();

				currentFile = logFile;
				try {
					currentPs = new PrintStream(new FileOutputStream(logFile, true));
				} catch (FileNotFoundException ex) {
					throw new RuntimeException(ex);
				}
			}

			currentPs.println(getMessage(timestamp, logType, message));
			currentPs.flush();
		}

		@Override
		protected final void onInit(LogModule logModule) {
			super.onInit(logModule);

			try {
				currentPs = new PrintStream(new FileOutputStream(currentFile, true));
			} catch (FileNotFoundException ex) {
				throw new RuntimeException(ex);
			}

		}

		@Override
		public void onStop() {
			currentPs.close();
		}

	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private final Set<LogStream> streamSet = new LinkedHashSet<>();
	private final Set<LogStream> readOnlyStreamSet = Collections.unmodifiableSet(streamSet);

	public LogModule(LogStream...streams) {
		for (LogStream stream : streams) {
			if (stream != null)
				addStream(stream);
		}
	}

	public Set<LogStream> getStreams() {
		synchronized(streamSet) {
			return readOnlyStreamSet;
		}
	}

	public void removeAllStreams() {
		synchronized(streamSet) {
			streamSet.clear();
		}
	}

	public void addStream(LogStream logStream) {
		synchronized(streamSet) {
			if (logStream == null)
				throw new IllegalArgumentException("Log stream cannot be null");

			if (streamSet.contains(logStream))
				throw new IllegalArgumentException("Stream already registerd: " + logStream.toString());

			logStream.setLogModule(this, true);

			streamSet.add(logStream);
		}
	}

	public void removeStream(LogStream logStream) {
		synchronized(streamSet) {
			streamSet.remove(logStream);
			logStream.setLogModule(null, true);
		}
	}

	@Override
	protected final String getSettingsGroupName() {
		return SETTINGS_GROUP_NAME;
	}

	@Override
	protected void onInit(AbstractApplication app) {
		super.onInit(app);

		for (LogStream logStream : getStreams()) {
			logStream.init();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		for (LogStream logStream : getStreams()) {
			logStream.stop();
		}

	}

	/**
	 * Logs a message.
	 *
	 * @param logType log type.
	 * @param message message to be logged.
	 * @param msgArgs message arguments.
	 */
	public void log(LogType logType, String message, Object...msgArgs) {
		log(new Date(), logType, message, msgArgs);
	}

	/**
	 * Logs a message.
	 *
	 * @param timestamp log timestamp.
	 * @param logType log type.
	 * @param message message to be logged.
	 * @param msgArgs message arguments.
	 */
	public void log(Date timestamp, LogType logType, String message, Object...msgArgs) {
		synchronized(streamSet) {
			if (msgArgs.length > 0)
				message = String.format(message, msgArgs);

			for (LogStream logStream : getStreams()) {
				logStream.println(timestamp, logType, message);
			}
		}
	}

}
