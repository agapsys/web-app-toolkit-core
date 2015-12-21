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

package com.agapsys.web.toolkit.integration;

import com.agapsys.http.HttpGet;
import com.agapsys.http.HttpResponse;
import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.web.toolkit.WebApplicationFilter;
import com.agapsys.web.toolkit.mock.MockedApplication;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class RestrictOriginTest {
	// CLASS SCOPE =============================================================
	@WebListener
	public static class ForbiddenLocalHostApp extends MockedApplication {

		@Override
		public boolean isOriginAllowed(HttpServletRequest req) {
			return false;
		}
	}
	
	public static class AnyOriginApp extends MockedApplication {}
	
	@WebServlet("/*")
	public static class TestServlet extends HttpServlet {

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			resp.getWriter().print("OK");
		}
	}
	// =========================================================================
// INSTANCE SCOPE ==========================================================
	private ServletContainer sc;
	
	@After
	public void after() {
		sc.stopServer();
	}
	
	@Test
	public void testEnabledApplication() {
		ApplicationContext anyOriginApp = new ApplicationContext();
		anyOriginApp.registerEventListener(new AnyOriginApp());
		anyOriginApp.registerServlet(TestServlet.class);
		anyOriginApp.registerFilter(WebApplicationFilter.class, "/*");
		
		sc = new ServletContainer();
		sc.registerContext(anyOriginApp, "/enabled");
		sc.startServer();
		
		HttpResponse resp = sc.doRequest(new HttpGet("/enabled/test"));
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
	}
	
	@Test
	public void testDisabledApplication() {
		ApplicationContext forbiddenLocalHostApp = new ApplicationContext();
		forbiddenLocalHostApp.registerEventListener(new ForbiddenLocalHostApp());
		forbiddenLocalHostApp.registerServlet(TestServlet.class);
		forbiddenLocalHostApp.registerFilter(WebApplicationFilter.class, "/*");
		
		sc = new ServletContainer();
		sc.registerContext(forbiddenLocalHostApp, "/disabled");
		sc.startServer();
		
		HttpResponse resp =sc.doRequest(new HttpGet("/disabled/test"));
		Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
	}
	// =========================================================================
}
