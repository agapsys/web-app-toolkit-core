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

package com.agapsys.web.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * General utilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class Utils {

	// CLASS SCOPE =============================================================
	/**
	 * Prints a string into application console
	 * @param str string to be printed
	 */
	public static synchronized void printConsoleLog(String str) {
		String logMsg = String.format("[web-core][%s]: %s ", getLocalTimestamp(), str);
		System.out.println(logMsg);
	}
	
	private static final DateFormat SIMPLE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	public static String getLocalTimestamp() {
		return SIMPLE_DATE_FORMATTER.format(new Date());
	}
	
	
	private static final DateFormat ISO_8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'");
	static {
		ISO_8601_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	/**
	 * @param date date to be formatted
	 * @return ISO representation of given date
	 */
	public static String getIso8601Date(Date date) {
		return ISO_8601_FORMATTER.format(date);
	}
	
	/**
	 * @param isoDate date to be parsed
	 * @return Date
	 * @throws ParseException if given string is an invalid date
	 */
	public static Date getDateFromIso(String isoDate) throws ParseException {
		return ISO_8601_FORMATTER.parse(isoDate);
	}

	/** 
	 * @return A string representation of a stacktrace for given error
	 * @param throwable error
	 */
	public static String getStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Utils() {} // Private constructor prevents external instantiation
	// =========================================================================
}