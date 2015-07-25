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
import javax.servlet.http.HttpServletRequest;

public class RequestUtils {
	// CLASS SCOPE =============================================================
	private static final String JSON_CONTENT_TYPE = "application/json";
	private static final Gson   JSON              = new Gson();	
	
	public static class BadRequestException extends Exception {
		public BadRequestException(String message) {
			super(message);
		}
	}
	
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
	
	public static String getClientIp(HttpServletRequest req) {
		String clientId = req.getHeader("X-FORWARDED-FOR");
		if (clientId == null || clientId.isEmpty()) {
			clientId = req.getRemoteAddr();
		}
		return clientId;
	}
	
	public static String getClientUserAgent(HttpServletRequest req) {
		return req.getHeader("user-agent");
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private RequestUtils() {}
	// =========================================================================
}
