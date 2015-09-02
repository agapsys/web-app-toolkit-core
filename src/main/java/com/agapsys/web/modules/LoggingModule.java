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
package com.agapsys.web.modules;

/**
 * Represents a logging module
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class LoggingModule extends Module {
	
	/** 
	 * Actual log code. 
	 * This method will be called only when module is running.
	 */
	protected abstract void processLog(String logType, String message);
	
	/**
	 * Logs a message.
	 * If module is not running, nothing happens.
	 * @param logType message type
	 * @param message message to be logged
	 * @throws IllegalArgumentException if either logType is null/empty of message is null/empty
	 */
	public final void log(String logType, String message) throws IllegalArgumentException {
		if (logType == null)
			throw new IllegalArgumentException("logType == null");
		
		if (message == null || message.trim().isEmpty())
			throw new IllegalArgumentException("Null/Empty message");
						
		if (isRunning()) {
			processLog(logType, message);
		}
	}
}
