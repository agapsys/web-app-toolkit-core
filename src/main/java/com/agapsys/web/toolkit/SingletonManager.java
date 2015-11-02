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
 * Represents a singleton object.
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class SingletonManager {

	// CLASS SCOPE =============================================================
	public static interface Singleton {}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private final Map<Class<? extends Singleton>, Singleton> CLASS_INSTANCE_MAP = new LinkedHashMap<>();
	private final Map<String, Class<? extends Singleton>>    ID_CLASS_MAP       = new LinkedHashMap<>();
	private final Map<String, Singleton>                     ID_INSTANCE_MAP    = new LinkedHashMap<>();
	
	public void registerSingleton(String id, Class<? extends Singleton> singletonClass) {
		if (id == null || id.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty ID");
	
		if (singletonClass == null)
			throw new IllegalArgumentException("Null singleton class");
		
		if (ID_CLASS_MAP.put(id, singletonClass) != null)
			throw new IllegalArgumentException("ID is already assigned: " + id);
	}

	public Class<? extends Singleton> getSingletonClass(String id) {
		return ID_CLASS_MAP.get(id);
	}
	
	public void clear() {
		ID_CLASS_MAP.clear();
		ID_INSTANCE_MAP.clear();
		CLASS_INSTANCE_MAP.clear();
	}

	public synchronized Singleton getSingleton(Class<? extends Singleton> singletonClass) {
		try {
			Singleton singleton = CLASS_INSTANCE_MAP.get(singletonClass);
		
			if (singleton == null)
				singleton = singletonClass.getConstructor().newInstance();
			
			CLASS_INSTANCE_MAP.put(singletonClass, singleton);
			return singleton;
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
	
	public synchronized Singleton getSingleton(String id) {
		if (id == null || id.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty ID");
		
		Singleton singleton = ID_INSTANCE_MAP.get(id);
		
		if (singleton == null) {
			Class<? extends Singleton> singletonClass = ID_CLASS_MAP.get(id);

			if (singletonClass == null)
				throw new IllegalArgumentException("ID is not registered: " + id);

			singleton = getSingleton(singletonClass);

			ID_INSTANCE_MAP.put(id, singleton);
		}
		
		return singleton;
	}
	// =========================================================================
}
