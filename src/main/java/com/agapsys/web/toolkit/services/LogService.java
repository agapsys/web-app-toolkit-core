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
package com.agapsys.web.toolkit.services;

import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.LogType;
import com.agapsys.web.toolkit.Service;
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

public final class LogService extends Service {
    // STATIC SCOPE ============================================================

    /** Logger interface. */
    public static interface Logger {
        /**
         * Starts the logger.
         *
         * @param application associated application.
         */
        public void start(AbstractApplication application);

        /**
         * Stops the logger.
         *
         * @param application associated application.
         */
        public void stop();

        /**
         * Logs a message.
         *
         * @param timestamp message timestamp.
         * @param logType log message type.
         * @param message log message.
         */
        public void log(Date timestamp, LogType logType, String message);
    }

    /** Logger adapter. */
    public static class LoggerAdapter implements Logger {
        private AbstractApplication app;

        public final AbstractApplication getApplication() {
            return app;
        }

        @Override
        public final void start(AbstractApplication application) {
            this.app = application;
            onStart();
        }

        protected void onStart() {}

        @Override
        public final void stop() {
            onStop();
            this.app = null;
        }

        protected void onStop() {}

        @Override
        public void log(Date timestamp, LogType logType, String message) {}

    }

    /** Logger which prints messages into console. */
    public static class ConsoleLogger extends LoggerAdapter {
        private final boolean useLogTypeColors;

        public ConsoleLogger() {
            this(false);
        }

        public ConsoleLogger(boolean useLogTypeColors) {
            this.useLogTypeColors = useLogTypeColors;
        }

        public final boolean usesLogTypeColors() {
            return useLogTypeColors;
        }

        protected Integer getFgColor(LogType logType) {
            switch(logType) {
                case ERROR:
                    return 31;

                case WARNING:
                    return 33;

                case INFO:
                    return 32;

                default:
                    return null;
            }
        }

        private String __getColoredLogType(int color, LogType logType) {
            return String.format("\u001B[%sm%s\u001B[0m", getFgColor(logType), logType.name());
        }

        protected String getMessage(Date timestamp, LogType logType, String message) {
            String logTypeStr = usesLogTypeColors() ? __getColoredLogType(0, logType) : logType.name();

            return String.format("%s [%s] %s", DateUtils.getIso8601Date(),logTypeStr, message);
        }

        @Override
        public void log(Date timestamp, LogType logType, String message) {
            System.out.println(getMessage(timestamp, logType, message));
        }

    }

    /** Logger which prints messages in a daily log file. */
    public static class DailyFileLogger extends LoggerAdapter {
        private static final String DEFAULT_PATTERN = "application-%s.log";

        private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyMMdd");

        private final File   logDir;
        private final String filenamePattern;

        private File currentFile;
        private PrintStream currentPs;

        public DailyFileLogger(File logDir) {
            this(logDir, DEFAULT_PATTERN);
        }

        public DailyFileLogger(File logDir, String filenamePattern) {
            if (logDir == null)
                throw new IllegalArgumentException("Log directory cannot be null");

            if (!logDir.exists()) {
                if (!logDir.mkdirs())
                    throw new RuntimeException("Cannot create log directory: " + logDir.getAbsolutePath());
            } else {
                if (!logDir.isDirectory())
                    throw new RuntimeException("Log directory path already exists and is a file: " + logDir.getAbsolutePath());
            }

            this.logDir = logDir;

            if (filenamePattern == null || filenamePattern.trim().isEmpty())
                throw new IllegalArgumentException("Null/Empty file pattern");

            this.filenamePattern = filenamePattern;

            currentFile = __getLogFile(logDir, filenamePattern);
        }

        private File __getLogFile(File logDir, String filenamePattern) {
            String logFilename = String.format(filenamePattern, SDF.format(new Date()));
            return new File(logDir, logFilename);
        }

        public final File getLogDir() {
            return logDir;
        }

        public final String getFilenamePattern() {
            return filenamePattern;
        }

        protected String getMessage(Date timestamp, LogType logType, String message) {
            return String.format("%s [%s] %s", DateUtils.getIso8601Date(), logType.name(), message);
        }

        @Override
        public void log(Date timestamp, LogType logType, String message) {
            File logFile = __getLogFile(getLogDir(), getFilenamePattern());

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
        protected void onStart() {
            super.onStart();

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

    private final Set<Logger> loggers   = new LinkedHashSet<>();
    private final Set<Logger> roLoggers = Collections.unmodifiableSet(loggers);

    public LogService(Logger...loggers) {
        for (Logger stream : loggers) {
            if (stream != null)
                addLogger(stream);
        }
    }

    public final Set<Logger> getLoggers() {
        synchronized(this) {
            return roLoggers;
        }
    }

    public void clearLoggers() {
        synchronized(this) {
            if (isRunning())
                throw new IllegalStateException("Cannot remove a logger from a running service");

            loggers.clear();
        }
    }

    public void addLogger(Logger logger) {
        synchronized(this) {
            if (logger == null)
                throw new IllegalArgumentException("Logger cannot be null");

            if (isRunning())
                throw new IllegalStateException("Cannot add a logger to a running service");

            if (!loggers.contains(logger))
                loggers.add(logger);
        }
    }

    public void removeLogger(Logger logger) {
        synchronized(this) {
            if (isRunning())
                throw new IllegalStateException("Cannot remove a logger from a running service");

            loggers.remove(logger);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        synchronized(this) {
            for (Logger logger : getLoggers()) {
                logger.start(getApplication());
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        synchronized(this) {
            for (Logger logger : getLoggers()) {
                logger.stop();
            }
        }

    }

    /** Convenience method for log(new Date(), logType, message, msgArgs). */
    public final void log(LogType logType, String message, Object...msgArgs) {
        log(new Date(), logType, message, msgArgs);
    }

    /**
     * Logs a message.
     *
     * @param timestamp log timestamp.
     * @param logType log type.
     * @param message message to be logged.
     * @param msgArgs message arguments (see {@linkplain String#format(String, Object...)}).
     */
    public void log(Date timestamp, LogType logType, String message, Object...msgArgs) {
        synchronized(this) {
            message = msgArgs.length > 0 ? String.format(message, msgArgs) : message;

            for (Logger logger : getLoggers()) {
                logger.log(timestamp, logType, message);
            }
        }
    }

}
