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
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GsonSerializer implements ObjectSerializer {

	
	// CLASS SCOPE =============================================================
	public static final String JSON_CONTENT_TYPE = "application/json";
	public static final String JSON_DATE_FORMAT = "yyyy-MM-dd";
	public static final String JSON_ENCODING = "UTF-8";
	
	private static final Gson DEFAULT_GSON = new GsonBuilder().setDateFormat(JSON_DATE_FORMAT).create();
	
	// Check if given request is valid for GSON parsing
	private static void checkJsonContentType(HttpServletRequest req) throws BadRequestException {
		String reqContentType = req.getContentType();

		if (!reqContentType.startsWith(JSON_CONTENT_TYPE)) {
			throw new BadRequestException("Invalid content-type: " + reqContentType);
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Gson gson = null;
	
	private synchronized Gson _getGson() {
		if (gson == null) {
			gson = getGson();
		}
		
		return gson;
	}
	
	protected Gson getGson() {
		return DEFAULT_GSON;
	}
	
	@Override
	public <T> T readObject(HttpServletRequest req, Class<T> targetClass) throws BadRequestException {
		if (targetClass == null)  throw new IllegalArgumentException("Null targetClass");

		checkJsonContentType(req);

		try {
			return _getGson().fromJson(req.getReader(), targetClass);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON", ex);
		} catch (Throwable t) {
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}

	@Override
	public void writeObject(HttpServletResponse resp, Object object) {
		resp.setContentType(JSON_CONTENT_TYPE);
		resp.setCharacterEncoding(JSON_ENCODING);

		PrintWriter out;
		try {
			out = resp.getWriter();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		String json = _getGson().toJson(object);
		out.write(json);
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
	 * Returns a list of objects from given request Request must have
	 * 'application/json' content-type.
	 *
	 * @param <T> generic type
	 * @param req HTTP request
	 * @param elementType type of the list elements
	 * @return list stored in request content body
	 * @throws BadRequestException if given request content-type does not match
	 * with expected
	 */
	public <T> List<T> getJsonList(HttpServletRequest req, Class<T> elementType) throws BadRequestException {
		checkJsonContentType(req);

		try {
			ListType lt = new ListType(elementType);
			return getGson().fromJson(req.getReader(), lt);
		} catch (JsonSyntaxException ex) {
			throw new BadRequestException("Malformed JSON", ex);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	// =========================================================================
}
