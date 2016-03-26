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

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Represents a singleton object. */
public class SingletonManager {
	private final Map<Class<?>, Object>   INSTANCE_MAP    = new LinkedHashMap<>();
	private final Map<Class<?>, Class<?>> REPLACEMENT_MAP = new LinkedHashMap<>();
	
	/**
	 * Replaces a singleton class by a subclass.
	 * @param <T> base Class type
	 * @param clazz singleton base class
	 * @param subclass singleton subclass which will replace given base class
	 * @throws IllegalStateException if there is an instance associated with base class
	 * 
	 */
	public synchronized <T> void replaceSingleton(Class<T> clazz, Class<? extends T> subclass) throws IllegalStateException {
		if (clazz == null)
			throw new IllegalArgumentException("clazz == null");
		
		if (subclass == null)
			throw new IllegalArgumentException("subclass == null");
		
		if (clazz == subclass)
			throw new IllegalArgumentException("clazz = subClass");
		
		if (INSTANCE_MAP.get(clazz) != null)
			throw new IllegalStateException("There is an instance associated with clazz");
		
		REPLACEMENT_MAP.put(clazz, subclass);
	}
	
	/**
	 * Returns a singleton instance associated with given class.
	 * If given class was replaced (via {@linkplain SingletonManager#replaceSingleton(Class, Class)}) returned
	 * instance will be an instance of associated subclass.
	 * @param <T> returned singleton type
	 * @param clazz singleton class
	 * @return singleton instance.
	 */
	public synchronized <T> T getSingleton(Class<T> clazz) {
		if (clazz == null)
			throw new IllegalArgumentException("Null singletonClass");
		
		try {
			T targetInstance = (T) INSTANCE_MAP.get(clazz);
			
			if (targetInstance != null)
				return targetInstance;
			
			Class<? extends T> targetClass = (Class<? extends T>) REPLACEMENT_MAP.get(clazz);
			
			if (targetClass == null) {
				REPLACEMENT_MAP.put(clazz, clazz);
				targetClass = clazz;
			}
			
			boolean hasReplacement = clazz != targetClass;
			
			targetInstance = (T) targetClass.getConstructor().newInstance();
			INSTANCE_MAP.put(clazz, targetInstance);
			
			if (hasReplacement)
				INSTANCE_MAP.put(targetClass, targetInstance);
			
			return targetInstance;
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
	
	/**
	 * Removes any singleton/replacement associated with given class..
	 * @param clazz base class which was replaced by a subclass.
	 */
	public void clear(Class<?> clazz) {
		REPLACEMENT_MAP.remove(clazz);
		INSTANCE_MAP.remove(clazz);
	}
	
	/** Removes all singletons and replacements registered with this manager. */
	public void clear() {
		INSTANCE_MAP.clear();
		REPLACEMENT_MAP.clear();
	}
}
