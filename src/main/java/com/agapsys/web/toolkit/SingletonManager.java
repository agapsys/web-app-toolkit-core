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
	private final Map<Class<? extends Singleton>, Singleton>                  INSTANCE_MAP = new LinkedHashMap<>();
	private final Map<Class<? extends Singleton>, Class<? extends Singleton>> ALIAS_MAP    = new LinkedHashMap<>();
	
	public synchronized void registerSingletonAlias(Class<? extends Singleton> singletonAlias, Class<? extends Singleton> targetSingleton) {
		if (singletonAlias == null)
			throw new IllegalArgumentException("Null alias class");
		
		if (targetSingleton == null)
			throw new IllegalArgumentException("Null singleton class");
		
		if (!targetSingleton.isAssignableFrom(singletonAlias))
			throw new IllegalArgumentException(String.format("%s cannot be cast to %s", targetSingleton.getName(), singletonAlias.getName()));
		
		ALIAS_MAP.put(singletonAlias, targetSingleton);
	}
	
	public synchronized <T extends Singleton> T getSingleton(Class<T> singletonClass) {
		try {
			Class<? extends Singleton> targetClass = ALIAS_MAP.get(singletonClass);
			
			if (targetClass == null) {
				// First attempt to get a singleton without an alias...
				ALIAS_MAP.put(singletonClass, singletonClass);
				targetClass = singletonClass;
			}
			
			Singleton targetSingleton = INSTANCE_MAP.get(singletonClass);
			if (targetSingleton != null && !targetSingleton.getClass().equals(targetClass)) {
				targetSingleton = null;
			}
			
			if (targetSingleton == null) {
				targetSingleton = targetClass.getConstructor().newInstance();
				INSTANCE_MAP.put(singletonClass, targetSingleton);
			}
			
			return (T) targetSingleton;
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
		
	public void clear() {
		ALIAS_MAP.clear();
		INSTANCE_MAP.clear();
	}
	// =========================================================================
}
