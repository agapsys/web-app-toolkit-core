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

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * Loads a JAR file at runtime.
 * 
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class RuntimeJarLoader {
	// CLASS SCOPE =============================================================
	private static final List<File> LOADED_JARS = new LinkedList<>();
	private static final Class[] PARAMS = new Class[]{URL.class};
	
	/** 
	 * Loads a jar file.
	 * If file was already loaded, nothing happens.
	 * @param jarFile file to be loaded
	 */
	public static synchronized void loadJar(File jarFile) {
		if (!LOADED_JARS.contains(jarFile)) {
			try {
				if (!jarFile.exists())
					throw new FileNotFoundException(String.format("File not found: '%s'", jarFile.getAbsolutePath()));

				URL url = new URL(String.format("jar:file:%s!/", jarFile.getAbsolutePath()));

				URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
				Class urlClassLoaderClass = URLClassLoader.class;

				Method addUrlMethod = urlClassLoaderClass.getDeclaredMethod("addURL", PARAMS);
				addUrlMethod.setAccessible(true);
				addUrlMethod.invoke(sysloader, new Object[]{url});
				LOADED_JARS.add(jarFile);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private RuntimeJarLoader() {}
	// =========================================================================
}
