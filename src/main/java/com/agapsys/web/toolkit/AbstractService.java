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

package com.agapsys.web.toolkit;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a service.
 * A service is a class responsible by a specific business logic inside application and has a singleton scope
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class AbstractService {
	// CLASS SCOPE =============================================================
	private static final Map<Class<? extends AbstractService>, AbstractService> SERVICE_MAP = new LinkedHashMap<>();
	
	/** 
	 * Returns a singleton instance of a service.
	 * @param serviceClass service class
	 * @return service instance
	 */
	public static synchronized <T extends AbstractService> T getService(Class<T> serviceClass) {
		T service = (T) SERVICE_MAP.get(serviceClass);
		
		if (service == null) {
			try {
				service = serviceClass.getConstructor().newInstance();
				SERVICE_MAP.put(serviceClass, service);
			} catch (
					NoSuchMethodException | 
					SecurityException | 
					InstantiationException | 
					IllegalAccessException | 
					IllegalArgumentException |
					InvocationTargetException ex
				) {
				throw new RuntimeException(ex);
			}
		}
		
		return service;
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================	
	protected AbstractService() {}
	// =========================================================================
}
