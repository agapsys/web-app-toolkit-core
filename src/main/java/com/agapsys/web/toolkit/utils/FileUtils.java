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
	 * @return a file representing a given path. If directory hierarchy does not exist, they will be created.
	 * @param path directory path
	 * @throws AccessError if directory hierarchy does not exist and it was not possible to create it.
	 * @throws  IllegalArgumentException if given path points to a file instead of a directory.
	 */
	public static File getOrCreateDirectory(String path) throws AccessError, IllegalArgumentException {
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
	
	/**
	 * Returns a generated non-existent file
	 * @param parentDirectory parent directory
	 * @param maxAttempts maximum number of attempts trying to get a random non-existent file before an exception is thrown
	 * @return non-existent file with random name
	 * @throws FileNotFoundException if a non-existent file could not be found.
	 */
	public static File getRandomNonExistentFile(File parentDirectory, int nameLength, int maxAttempts) throws FileNotFoundException {
		char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

		if (parentDirectory == null)
			throw new IllegalArgumentException("Parent directory cannot be null");
		
		if (nameLength < 1)
			throw new IllegalArgumentException("Name length must be greater than 1");
		
		if (maxAttempts < 1)
			throw new IllegalArgumentException("Maximum attempts must be greater than 1");
		
		File file;
		int attempts = 0;
		
		while(true) {
			if (attempts >= maxAttempts)
				throw new FileNotFoundException(String.format("It was not possible to generate a randon non-existent file after %d attempts", maxAttempts));
			
			file = new File(parentDirectory, StringUtils.getRandomString(nameLength, chars));
			
			if (!file.exists())
				return file;
			
			attempts++;
		}
	}
	// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private FileUtils() {} 
	// =========================================================================
}
