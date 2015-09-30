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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpUtils {
	// CLASS SCOPE =============================================================
	public static final String JSON_CONTENT_TYPE = "application/json";
	public static final String JSON_DATE_FORMAT  = "yyyy-MM-dd";
	public static final String JSON_ENCODING     = "UTF-8";
	
	private static final Gson DEFAULT_GSON;
	
	private static Gson gson;
	
	static {
		DEFAULT_GSON = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
	}
	
	/** Represents a bad request detected in application. */
	public static class BadRequestException extends Exception {
		private BadRequestException(String message) {
			super(message);
		}
		
		private BadRequestException(String message, Throwable cause) {
			super(message, cause);
		}
	}
	
	public static void setGson(Gson gson) {
		HttpUtils.gson = gson;
	}
	
	private static Gson getGson() {
		if (gson == null)
			gson = DEFAULT_GSON;
		
		return gson;
	}
	
	// Check if given request is valid for GSON parsing
	private static void checkJsonContent(HttpServletRequest req) throws BadRequestException {
		String reqContentType = req.getContentType();
		
		if(!reqContentType.startsWith(JSON_CONTENT_TYPE))
			throw new BadRequestException("Invalid content-type: " + reqContentType);
	}
	
	/**
	 * Returns an object from given request.
	 * Request must have 'application/json' content-type.
	 * @param req HTTP request
	 * @param clazz desired output object class
	 * @throws BadRequestException if given request content-type does not match with expected
	 * @throws IOException if there is an I/O error while processing the request
	 * @param <T> generic type
	 * @return an object stored in given request.
	 */
	public static <T> T getJsonData(HttpServletRequest req, Class<T> clazz) throws BadRequestException, IOException {
		if (clazz == null)
			throw new IllegalArgumentException("Null clazz");
		
		checkJsonContent(req);
				
		try {
			return getGson().fromJson(req.getReader(), clazz);
		} catch (JsonIOException ex) {
			throw new IOException(ex);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON", ex);
		}
	}
	
	private static class ListType implements ParameterizedType {
		private final Type[] typeArguments = new Type[1];
		public ListType(Class<?> clazz) {
			typeArguments[0] = clazz;
		}

		@Override
		public String getTypeName() {
			return String.format("java.util.List<%s>", typeArguments[0].getTypeName());
		}

		@Override
		public Type[] getActualTypeArguments() {
			return typeArguments;
		}

		@Override
		public Type getRawType() {
			return List.class;
		}

		@Override
		public Type getOwnerType() {
			return List.class;
		}
	}
	
	/**
	 * Returns a list of objects from given request
	 * Request must have 'application/json' content-type.
	 * @param <T> generic type
	 * @param req HTTP request
	 * @param elementType type of the list elements
	 * @return list stored in request content body
	 * @throws BadRequestException if given request content-type does not match with expected
	 * @throws IOException if there is an I/O error while processing the request
	 */	
	public static <T> List<T> getJsonList(HttpServletRequest req, Class<T> elementType) throws BadRequestException, IOException {
		checkJsonContent(req);
		
		try {
			ListType lt = new ListType(elementType);
			return getGson().fromJson(req.getReader(), lt);
		} catch (JsonIOException ex) {
			throw new IOException(ex);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON", ex);
		}
	}
	
	/**
	 * Sends an object as a json
	 * @param resp HTTP response
	 * @param object object to be sent
	 * @throws IOException if there is an I/O error while processing the request
	 */
	public static void sendJsonData(HttpServletResponse resp, Object object) throws IOException {
		resp.setContentType(JSON_CONTENT_TYPE);
		resp.setCharacterEncoding(JSON_ENCODING);
		
		PrintWriter out = resp.getWriter();
		String json = getGson().toJson(object);
		out.write(json);
	}
	
	/**
	 * Sends an object as a json
	 * @param resp HTTP response
	 * @param object object to be sent
	 * @param type type of given object
	 * @throws IOException if there is an I/O error while processing the request
	 */
	public static void sendJsonData(HttpServletResponse resp, Object object, Type type) throws IOException {
		resp.setContentType(JSON_CONTENT_TYPE);
		resp.setCharacterEncoding(JSON_ENCODING);
		PrintWriter out = resp.getWriter();
		String json = getGson().toJson(object, type);
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
