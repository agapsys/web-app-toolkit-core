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

package com.agapsys.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(ErrorServlet.URL)
public class ErrorServlet extends HttpServlet {
	// CLASS SCOPE =============================================================
	public static final String URL = "/error";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (resp.getStatus() != HttpServletResponse.SC_NOT_FOUND) {
			if (!req.getMethod().equals("GET")) {
				resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
			} else {
				WebApplication.handleErrorRequest(req, resp);
			}
		}
	}
	// =========================================================================
}
