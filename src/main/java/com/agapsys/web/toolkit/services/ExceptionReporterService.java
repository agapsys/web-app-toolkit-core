/*
 * Copyright 2015-2016 Agapsys Tecnologia Ltda-ME.
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

import com.agapsys.mail.Message;
import com.agapsys.mail.MessageBuilder;
import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.LogType;
import com.agapsys.web.toolkit.Service;
import com.agapsys.web.toolkit.utils.DateUtils;
import com.agapsys.web.toolkit.utils.HttpUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

/**
 * Represents an exception reporter.
 */
public class ExceptionReporterService extends Service {

    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    public static interface ExceptionReporter {

        public void reportException(Throwable ex, HttpServletRequest req, String nodeName);

        public void start(AbstractApplication app);

        public void stop();
    }

    public static class ExceptionReporterAdapter implements ExceptionReporter {

        private AbstractApplication app;

        public final AbstractApplication getApplication() {
            return app;
        }

        @Override
        public void reportException(Throwable ex, HttpServletRequest req, String nodeName) {}

        @Override
        public final void start(AbstractApplication app) {
            this.app = app;
            onStart();
        }

        protected void onStart() {}

        @Override
        public final void stop() {
            onStop();
            this.app = null;
        }

        protected void onStop() {}

    }

    public static class LogReporter extends ExceptionReporterAdapter {

        protected String getReportMessage(Throwable throwable, HttpServletRequest req, String nodeName) {
            StringBuilder sb = new StringBuilder("Application error").append("\n")
                    .append("---------------------------")
                    .append("URI: ").append(req.getRequestURI()).append("\n")
                    .append("Node: ").append(nodeName).append("\n")
                    .append("Stacktrace:").append("\n")
                    .append(getStackTrace(throwable)).append("\n")
                    .append("---------------------------");

            return sb.toString();
        }

        @Override
        public void reportException(Throwable ex, HttpServletRequest req, String nodeName) {
            LogService logService = getApplication().getRegisteredService(LogService.class);
            logService.log(LogType.ERROR, getReportMessage(ex, req, nodeName));
        }

    }

    public static class SmtpReporter extends ExceptionReporterAdapter {

        // <editor-fold desc="STATIC SCOPE">
        private static final String PROPERTY_PREFIX = SmtpReporter.class.getName();

        public static final String KEY_MSG_RECIPIENTS = PROPERTY_PREFIX + ".recipients";
        public static final String KEY_MSG_SUBJECT = PROPERTY_PREFIX + ".subject";

        public static final String DEFAULT_MSG_SUBJECT = "Exception Report";
        public static final String DEFAULT_MSG_RECIPIENTS = "user@localhost";

        private static InternetAddress[] __getRecipientsFromString(String recipients, String delimiter) {
            if (recipients == null || recipients.trim().isEmpty()) {
                throw new IllegalArgumentException("Null/empty recipients");
            }

            if (delimiter == null || delimiter.trim().isEmpty()) {
                throw new IllegalArgumentException("Null/empty delimiter");
            }

            String[] recipientArray = recipients.split(Pattern.quote(delimiter));
            InternetAddress[] result = new InternetAddress[recipientArray.length];

            for (int i = 0; i < recipientArray.length; i++) {
                try {
                    result[i] = new InternetAddress(recipientArray[i].trim());
                } catch (AddressException ex) {
                    throw new IllegalArgumentException("Invalid address: " + recipientArray[i].trim(), ex);
                }
            }

            return result;
        }
        // </editor-fold>

        private String msgSubject = DEFAULT_MSG_SUBJECT;
        private InternetAddress[] recipients = null;

        protected String getReportMessage(Throwable throwable, HttpServletRequest req, String nodeName) {
            String stackTrace = getStackTrace(throwable);

            AbstractApplication app = AbstractApplication.getRunningInstance();

            String msg
                = "An error was detected"
                + "\n\n"
                + "Application: " + app.getName() + "\n"
                + "Application version: " + app.getVersion() + "\n"
                + "Node name: " + nodeName + "\n\n"
                + "Server timestamp: " + DateUtils.getIso8601Date() + "\n"
                + "Error message: " + throwable.getMessage() + "\n"
                + "Request URI: " + HttpUtils.getRequestUri(req) + "\n"
                + "User-agent: " + HttpUtils.getOriginUserAgent(req) + "\n"
                + "Client id: " + HttpUtils.getOriginIp(req) + "\n"
                + "Stacktrace:\n" + stackTrace;

            return msg;
        }

        @Override
        protected void onStart() {
            super.onStart();

            AbstractApplication app = getApplication();
            msgSubject = app.getProperty(KEY_MSG_SUBJECT, DEFAULT_MSG_SUBJECT);
            recipients = __getRecipientsFromString(app.getProperty(KEY_MSG_RECIPIENTS, DEFAULT_MSG_RECIPIENTS), ",");
        }

        public String getMsgSubject() {
            return msgSubject;
        }

        public InternetAddress[] getRecipients() {
            return recipients;
        }

        @Override
        public void reportException(Throwable ex, HttpServletRequest req, String nodeName) {
            SmtpService smtpService = getApplication().getRegisteredService(SmtpService.class);

            Message message = new MessageBuilder(smtpService.getSender(), getRecipients())
                .setText(getReportMessage(ex, req, nodeName)).build();

            try {
                smtpService.sendMessage(message);
            } catch (MessagingException ex1) {
                throw new RuntimeException(ex1);
            }
        }

    }

    public static final String PROPERTY_PREFIX = ExceptionReporterService.class.getName();

    public static final String KEY_SERVICE_ENABLED = PROPERTY_PREFIX + ".enabled";
    public static final String KEY_NODE_NAME = PROPERTY_PREFIX + ".nodeName";
    public static final String KEY_STACK_TRACE_HISTORY_SIZE = PROPERTY_PREFIX + ".stackTraceHistorySize";

    public static final boolean DEFAULT_SERVICE_ENABLED = true;
    public static final String DEFAULT_NODE_NAME = "node-01";
    public static final int DEFAULT_STACK_TRACE_HISTORY_SIZE = 5;

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
    // </editor-fold>

    private final List<String> stackTraceHistory = new LinkedList<>();

    private final Set<ExceptionReporter> reporters = new LinkedHashSet<>();
    private final Set<ExceptionReporter> roReporters = Collections.unmodifiableSet(reporters);

    private String nodeName = DEFAULT_NODE_NAME;
    private int stackTraceHistorySize = DEFAULT_STACK_TRACE_HISTORY_SIZE;
    private boolean enabled = DEFAULT_SERVICE_ENABLED;

    public ExceptionReporterService(ExceptionReporter... reporters) {
        __reset();

        for (ExceptionReporter reporter : reporters) {
            if (reporter != null) {
                addReporter(reporter);
            }
        }
    }

    private void __reset() {
        nodeName = DEFAULT_NODE_NAME;
        stackTraceHistorySize = DEFAULT_STACK_TRACE_HISTORY_SIZE;
        enabled = DEFAULT_SERVICE_ENABLED;
        stackTraceHistory.clear();
    }

    public final Set<ExceptionReporter> getReporters() {
        synchronized (this) {
            return roReporters;
        }
    }

    public void clearReporters() {
        synchronized (this) {
            if (isRunning())
                throw new IllegalStateException("Cannot remove a reporter from a running service");

            // Reporters don't need to be stopped since service is not running.

            reporters.clear();
        }
    }

    public void addReporter(ExceptionReporter reporter) {
        synchronized (this) {
            if (reporter == null)
                throw new IllegalArgumentException("Reporter cannot be null");

            if (isRunning())
                throw new IllegalStateException("Cannot add a reporter to a running service");

            if (!reporters.contains(reporter)) {
                reporters.add(reporter);
            }
        }
    }

    public void removeReporter(ExceptionReporter reporter) {
        synchronized (this) {
            if (isRunning())
                throw new IllegalStateException("Cannot remove a reporter from a running service");

            reporters.remove(reporter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        synchronized(this) {
            __reset();

            AbstractApplication app = getApplication();

            enabled = app.getProperty(Boolean.class, KEY_SERVICE_ENABLED, DEFAULT_SERVICE_ENABLED);
            nodeName = app.getProperty(KEY_NODE_NAME, DEFAULT_NODE_NAME);
            stackTraceHistorySize = app.getProperty(Integer.class, KEY_STACK_TRACE_HISTORY_SIZE, DEFAULT_STACK_TRACE_HISTORY_SIZE);

            for (ExceptionReporter reporter : getReporters()) {
                reporter.start(app);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        synchronized(this) {
            for (ExceptionReporter reporter : getReporters()) {
                reporter.stop();
            }
        }
    }

    /**
     * Returns the stack trace history size defined in application settings.
     *
     * @return stack trace history size defined in application settings.
     */
    public int getStackTraceHistorySize() {
        synchronized(this) {
            return stackTraceHistorySize;
        }
    }

    /**
     * Returns the node name defined in application settings.
     *
     * @return the node name defined in application settings.
     */
    public String getNodeName() {
        synchronized(this) {
            return nodeName;
        }
    }

    /**
     * Returns a boolean status indicating if service is enabled.
     *
     * @return a boolean status indicating if service is enabled (this property
     * is defined in application settings).
     */
    public final boolean isServiceEnabled() {
        synchronized(this) {
            return enabled;
        }
    }

    /**
     * Return a boolean indicating if report shall be skipped for given error.
     *
     * @param t error to test.
     *
     * @return a boolean indicating if report shall be skipped for given error.
     */
    protected boolean skipErrorReport(Throwable t) {
        String stackTrace = getStackTrace(t);

        if (stackTraceHistory.contains(stackTrace)) {
            return true;
        } else {
            if (stackTraceHistory.size() == getStackTraceHistorySize()) {
                stackTraceHistory.remove(0); // Remove oldest
            }
            stackTraceHistory.add(stackTrace);
            return false;
        }
    }

    /**
     * Reports an error in the application.
     *
     * @param exception exception to be reported.
     * @param req HTTP request which thrown the exception.
     */
    public void reportException(Throwable exception, HttpServletRequest req) {
        synchronized (this) {
            if (exception == null)
                throw new IllegalArgumentException("null throwable");

            if (req == null)
                throw new IllegalArgumentException("Null request");

            if (!isRunning())
                throw new IllegalStateException("Service is not running");

             if (isServiceEnabled()) {
                if (!skipErrorReport(exception)) {
                    for (ExceptionReporter reporter : getReporters()) {
                        reporter.reportException(exception, req, getNodeName());
                    }
                } else {
                    getApplication().log(LogType.ERROR, "Application error (already reported): %s", exception.getMessage());
                }
            }
        }
    }

}
