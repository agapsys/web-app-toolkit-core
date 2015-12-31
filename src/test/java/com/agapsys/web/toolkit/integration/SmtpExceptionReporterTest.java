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
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.sevlet.test.ServletContainerBuilder;
import com.agapsys.web.toolkit.ErrorServlet;
import com.agapsys.web.toolkit.ExceptionReporterModule;
import com.agapsys.web.toolkit.SmtpExceptionReporterModule;
import com.agapsys.web.toolkit.WebApplicationFilter;
import com.agapsys.web.toolkit.mock.MockedApplication;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SmtpExceptionReporterTest {
	// CLASS SCOPE =============================================================
	@WebListener
	public static class Application extends MockedApplication {
		@Override
		protected String getPropertiesFilename() {
			return "smtp-exception-test.properties";
		}

		@Override
		protected void beforeApplicationStart() {
			super.beforeApplicationStart();
			registerModuleReplacement(ExceptionReporterModule.class, SmtpExceptionReporterModule.class);
		}
	}
	
	@WebServlet(CustomErrorServlet.URL)
	public static class CustomErrorServlet extends ErrorServlet {
		public static final String URL = "/error";
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private ServletContainer sc;
	
	@Before
	public void before() {
		sc = new ServletContainerBuilder()
			.addRootContext()
				.registerEventListener(new Application())
				.registerServlet(CustomErrorServlet.class)
				.registerServlet(ExceptionServlet.class)
				.registerFilter(WebApplicationFilter.class, "/*")
				.registerErrorPage(500, CustomErrorServlet.URL)
			.endContext()
			.build();
		
		sc.startServer();
	}
	
	@After
	public void after() {
		sc.stopServer();
	}
	
	@Test
	public void callErrorUrl() {
		String url = ExceptionServlet.URL + "?a=1&b=2";
		HttpResponse resp = sc.doRequest(new HttpGet(url));
		Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp.getStatusCode());
	}
	// =========================================================================
}
