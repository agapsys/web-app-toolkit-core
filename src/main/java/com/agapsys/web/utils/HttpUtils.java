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

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpUtils {
	// CLASS SCOPE =============================================================
	private static final String JSON_CONTENT_TYPE = "application/json";
	private static final Gson   JSON              = new Gson();	
	
	/** Represents a bad request detected in application. */
	public static class BadRequestException extends Exception {
		private BadRequestException(String message) {
			super(message);
		}
	}
	
	/**
	 * @return an object stored in given request. It's expected that request has a content-type "application/json".
	 * @param request HTTP request
	 * @param clazz desired output object class
	 * @throws IllegalArgumentException if request == null || clazz == null
	 * @throws com.agapsys.web.utils.RequestUtils.BadRequestException if given request content-type does not match with expected
	 * @throws IOException if there is an I/O error while processing the request
	 */
	public static <T> T getJsonRequestData(HttpServletRequest request, Class<T> clazz) throws IllegalArgumentException, BadRequestException, IOException {
		if (request == null)
			throw new IllegalArgumentException("Null request");
		
		if (clazz == null)
			throw new IllegalArgumentException("Null clazz");
		
		String reqContentType = request.getContentType();
		if(!reqContentType.equals(JSON_CONTENT_TYPE))
			throw new BadRequestException("Invalid content-type: " + reqContentType);
				
		try {
			return JSON.fromJson(request.getReader(), clazz);
		} catch (JsonIOException ex) {
			throw new IOException(ex);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed request");
		}
	}
	
	/**
	 * Sends an object as a json
	 * @param response HTTP response
	 * @param object object to be sent
	 * @throws IllegalArgumentException
	 * @throws IOException 
	 */
	public static void sendJsonData(HttpServletResponse response, Object object) throws IllegalArgumentException, IOException {
		if (response == null)
			throw new IllegalArgumentException("Null response");
		
		//TODO check null object
		response.setContentType(JSON_CONTENT_TYPE);
		PrintWriter out = response.getWriter();
		String json = JSON.toJson(object);
		out.write(json);
	}
	
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
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private HttpUtils() {}
	// =========================================================================
}
