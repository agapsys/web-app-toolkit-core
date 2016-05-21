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
	private static final DateFormat ISO8601_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS'Z'");
	private static final DateUtils  SINGLETON = new DateUtils();
	
	public static DateUtils getInstance() {
		return SINGLETON;
	}
	
	static {
		ISO8601_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	public final String getIso8601Date() {
		return getIso8601Date(new Date());
	}
	
	/**
	 * @param date date to be formatted.
	 * 
	 * @return ISO representation of given date.
	 */
	public String getIso8601Date(Date date) {
		return ISO8601_FORMATTER.format(date);
	}
	
	/**
	 * @param isoDate date in ISO-8601 format.
	 * 
	 * @return Date equivalent Date instance.
	 * @throws ParseException if given string is an invalid date.
	 */
	public Date getDateFromIso(String isoDate) throws ParseException {
		return ISO8601_FORMATTER.parse(isoDate);
	}
	
	/** 
	 * Returns a future date.
	 * 
	 * @param millisFromNow milliseconds from now.
	 * @return future date.
	 */
	public Date getDate(long millisFromNow) {
		return new Date(new Date().getTime() + millisFromNow);
	}
	
	/**
	 * Returns a boolean indicating if given date is in the past.
	 * 
	 * @param date date to be evaluated.
	 * @return a boolean indicating if given date is in the past.
	 */
	public boolean isBeforeNow(Date date) {
		long now = new Date().getTime();
		return date.getTime() < now;
	}
	// =========================================================================
}