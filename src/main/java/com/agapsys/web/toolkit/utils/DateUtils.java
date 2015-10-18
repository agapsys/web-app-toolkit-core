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

package com.agapsys.web.toolkit.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date utilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class DateUtils {
	// CLASS SCOPE =============================================================
	private static final DateFormat SIMPLE_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	/** @return system time stamp formated as yyyy-MM-dd HH:mm:ss:SSS */
	public static String getLocalTimestamp() {
		return SIMPLE_DATE_FORMATTER.format(new Date());
	}
	
	/** 
	 * @return given date formated as a local timestamp.
	 * @param date date to be formated
	 */
	public static String getLocalTimestamp(Date date) {
		if (date == null)
			throw new IllegalArgumentException("Null date");
		
		return SIMPLE_DATE_FORMATTER.format(date);
	}
	
	private static final DateFormat ISO_8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
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
	 * Returns a future date
	 * @param millisFromNow milliseconds from now
	 * @return future date
	 */
	public static Date getDate(long millisFromNow) {
		return new Date(new Date().getTime() + millisFromNow);
	}
	
	/**
	 * Returns a boolean indicating if given date is in the past
	 * @param date date to be evaluated
	 * @return a boolean indicating if given date is in the past
	 */
	public static boolean isBeforeNow(Date date) {
		long now = new Date().getTime();
		return date.getTime() < now;
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private DateUtils() {}
	// =========================================================================
}