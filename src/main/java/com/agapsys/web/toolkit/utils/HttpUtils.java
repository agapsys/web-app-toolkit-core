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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpUtils {

	// CLASS SCOPE =============================================================
	/**
	 * @return request's origin IP
	 * @param req HTTP request
	 */
	public static String getOriginIp(HttpServletRequest req) {
		String clientId = req.getHeader("X-FORWARDED-FOR");
		if (clientId == null || clientId.isEmpty()) {
			clientId = req.getRemoteAddr();
		}
		return clientId;
	}

	/**
	 * @param req HTTP request
	 * @return orgin user-agent
	 */
	public static String getOriginUserAgent(HttpServletRequest req) {
		return req.getHeader("user-agent");
	}

	/**
	 * @return request URI.
	 * @param req HTTP request
	 */
	public static String getRequestUri(HttpServletRequest req) {
		StringBuffer requestUrl = req.getRequestURL();
		if (req.getQueryString() != null) {
			requestUrl.append("?").append(req.getQueryString());
		}

		return String.format("%s %s %s", req.getMethod(), requestUrl.toString(), req.getProtocol());
	}

	/**
	 * @return cookie value. If there is no such cookie, returns null
	 * @param request HTTP request
	 * @param name cookie name
	 */
	public static String getCookieValue(HttpServletRequest request, String name) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (name.equals(cookie.getName())) {
					return cookie.getValue();
				}
			}
		}
		return null;
	}
	
	/**
	 * Adds a cookie.
	 * @param resp HTTP response
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
	 * @param path cookie path (usually {@linkplain HttpServletRequest#getContextPath()})
	 */
	public static void addCookie(HttpServletResponse resp, String name, String value, int maxAge, String path) {
		if (path == null || !path.startsWith("/"))
			throw new IllegalArgumentException("Invalid path: " + path);
		
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		resp.addCookie(cookie);
	}
	
	/**
	 * Adds a cookie for request context path
	 * @param req  HTTP request
	 * @param resp HTTP response
	 * @param name cookie name
	 * @param value cookie value
	 * @param maxAge an integer specifying the maximum age of the cookie in seconds; if negative, means the cookie is not stored; if zero, deletes the cookie
	 */
	public static void addCookie(HttpServletRequest req, HttpServletResponse resp, String name, String value, int maxAge) {
		addCookie(resp, name, value, maxAge, req.getContextPath());
	}
	
	/**
	 * Removes a cookie.
	 * @param resp HTTP response
	 * @param name name of the cookie to be removed
	 * @param path cookie path ((usually {@linkplain HttpServletRequest#getContextPath()})
	 */
	public static void removeCookie(HttpServletResponse resp, String name, String path) {
		addCookie(resp, name, null, 0, path);
	}
	
	/**
	 * Removes a cookie for request context path.
	 * @param req  HTTP request
	 * @param resp HTTP response
	 * @param name name of the cookie to be removed
	 */
	public static void removeCookie(HttpServletRequest req, HttpServletResponse resp, String name) {
		removeCookie(resp, name, req.getContextPath());
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private HttpUtils() {}
	// =========================================================================
}
