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

/** Represents a singleton object. */
public class SingletonManager {
	private final Map<Class<?>, Object>   INSTANCE_MAP = new LinkedHashMap<>();
	private final Map<Class<?>, Class<?>> ALIAS_MAP    = new LinkedHashMap<>();
	
	/**
	 * Replaces a singleton class by a subclass.
	 * @param baseClass singleton base class
	 * @param subclass singleton subclass which will replace given base class
	 */
	public synchronized void replaceSingleton(Class<?> baseClass, Class<?> subclass) {
		if (baseClass == null)
			throw new IllegalArgumentException("Null alias class");
		
		if (subclass == null)
			throw new IllegalArgumentException("Null singleton class");
		
		if (!baseClass.isAssignableFrom(subclass))
			throw new IllegalArgumentException(String.format("%s cannot be cast to %s", subclass.getName(), baseClass.getName()));
		
		ALIAS_MAP.put(baseClass, subclass);
	}
	
	/**
	 * Returns a singleton instance associated with given class.
	 * If given class was replaced (via {@linkplain SingletonManager#replaceSingleton(Class, Class)}) returned
	 * instance will be an instance of associated subclass.
	 * @param <T> returned singleton class
	 * @param singletonClass singleton class
	 * @return singleton instance.
	 */
	public synchronized <T> T getSingleton(Class<T> singletonClass) {
		try {
			Class<?> targetClass = ALIAS_MAP.get(singletonClass);
			
			if (targetClass == null) {
				// First attempt to get a singleton without an alias...
				ALIAS_MAP.put(singletonClass, singletonClass);
				targetClass = singletonClass;
			}
			
			Object targetSingleton = INSTANCE_MAP.get(singletonClass);
			if (targetSingleton != null && targetSingleton.getClass() != targetClass) {
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
			
			Throwable cause;
			if (ex instanceof InvocationTargetException) {
				cause = ((InvocationTargetException) ex).getTargetException();
			} else {
				cause = ex;
			}
			
			throw new RuntimeException(cause);
		}
	}
	
	/** Removes any singleton replacement associated with this manager. */
	public void clearReplacement() {
		ALIAS_MAP.clear();
	}
	
	/**
	 * Removes any singleton replacement associated with given class..
	 * @param baseClass base class which was replaced by a subclass.
	 */
	public void clearReplacement(Class<?> baseClass) {
		ALIAS_MAP.remove(baseClass);
	}
	
	/** Removes all singletons and replacements registered with this manager. */
	public void clear() {
		INSTANCE_MAP.clear();
		ALIAS_MAP.clear();
	}
}
