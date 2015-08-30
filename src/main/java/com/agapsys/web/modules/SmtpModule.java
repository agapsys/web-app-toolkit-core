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

import com.agapsys.mail.Message;

/**
 * E-mail sender module
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public abstract class SmtpModule extends Module {
	
	/** 
	 * Actual message sending code. 
	 * This method will be called only when module is running.
	 */
	protected abstract void processMessage(Message message);
	
	/** 
	 * Sends a email message.
	 * If module is not running, nothing happens.
	 * @param message message to be sent
	 */
	public final void sendMessage(Message message) {
		if (isRunning()) {
			processMessage(message);
		}
	}
}