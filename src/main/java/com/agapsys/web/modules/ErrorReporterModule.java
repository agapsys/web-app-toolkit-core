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

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Represents an error reporter
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public interface ErrorReporterModule extends Module {
	/**
	 * Handles an erroneous request
	 * @param req HTTP request
	 * @param resp HTTP response
	 * @throws ServletException when there is an error processing request
	 * @throws IOException when there is an I/O error processing request
	 */
	public void reportError(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException ;
}
