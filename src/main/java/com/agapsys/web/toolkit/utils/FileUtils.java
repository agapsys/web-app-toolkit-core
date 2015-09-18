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
import java.io.IOException;

/**
 * File Handling utilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class FileUtils {
	// CLASS SCOPE =============================================================
	public static class AccessError extends RuntimeException {

		public AccessError() {}

		public AccessError(String message) {
			super(message);
		}

		public AccessError(String message, Throwable cause) {
			super(message, cause);
		}

		public AccessError(Throwable cause) {
			super(cause);
		}

		public AccessError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
			super(message, cause, enableSuppression, writableStackTrace);
		}
	}
	
	/** Default folder delimiter. */
	public static final String FOLDER_DELIMITER;
	
	/** Current user folder. */
	public static final File USER_HOME;
	
	/** Default temporary folder. */
	public static final File DEFAULT_TEMPORARY_FOLDER;
	
	/** Current Operating System. */
	public static final String OS_NAME;
	
	static {
		FOLDER_DELIMITER = System.getProperty("file.separator");
		USER_HOME = new File(System.getProperty("user.home"));
		DEFAULT_TEMPORARY_FOLDER = new File(System.getProperty("java.io.tmpdir"));
		OS_NAME = System.getProperty("os.name");
	}
	
	/**
	 * @return a file representing a given path. If folder hierarchy does not exist, they will be created.
	 * @param path folder path
	 * @throws AccessError if folder hierarchy does not exist and it was not possible to create it.
	 * @throws  IllegalArgumentException if given path points to a file instead of a directory.
	 */
	public static File getOrCreateFolder(String path) throws AccessError, IllegalArgumentException {
		File folder = new File(path);
		if (!folder.exists()) {
			if (!folder.mkdirs())
				throw new AccessError(String.format("cannot create/access '%s'", path));
		} else {
			if (!folder.isDirectory())
				throw new IllegalArgumentException(String.format("Path '%s' is a file", path));
		}
		
		if(OS_NAME.toLowerCase().contains("win") && folder.getName().startsWith(".")) {
			try {
				Runtime.getRuntime().exec("attrib +H "+folder.getAbsolutePath());
			} catch (IOException ignore) {}
		}
		return folder;
	}
	
	/** 
	 * Deletes a file.
	 * If given file is a folder, delete its contents also.
	 * @param file file to delete
	 * @throws FileNotFoundException if given file does not exist
	 */
	public static void deleteFile(File file) throws FileNotFoundException {
		if (file == null)
			throw new IllegalArgumentException("Null file");
		
		if (!file.exists())
			throw new FileNotFoundException(String.format("File not found: %s", file.getAbsolutePath()));
		
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			
			for (File tmpFile : files) {
				deleteFile(tmpFile); // recursive
			}
			
			file.delete();
		} else {
			file.delete();
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private FileUtils() {} 
	// =========================================================================
}
