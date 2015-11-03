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
import com.agapsys.web.toolkit.WebToolkit;
import com.agapsys.web.toolkit.ErrorServlet;
import com.agapsys.web.toolkit.SmtpExceptionReporterModule;
import com.agapsys.web.toolkit.SmtpModule;
import com.agapsys.web.toolkit.TestApplication;
import com.agapsys.web.toolkit.WebApplicationFilter;
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
	public static class Application extends TestApplication {
		@Override
		protected String getPropertiesFilename() {
			return "smtp-exception-test.properties";
		}

		@Override
		protected boolean isPropertiesFileCreationEnabled() {
			return true;
		}

		@Override
		protected boolean isPropertiesFileLoadingEnabled() {
			return true;
		}

		@Override
		protected void beforeApplicationStart() {
			super.beforeApplicationStart();
			registerModule(WebToolkit.SMTP_MODULE_ID, SmtpModule.class);
			registerModule(WebToolkit.EXCEPTION_REPORTER_MODULE_ID, SmtpExceptionReporterModule.class);
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
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(CustomErrorServlet.class);
		context.registerServlet(ExceptionServlet.class);
		context.registerEventListener(new Application());
		context.registerFilter(WebApplicationFilter.class, "/*");
		context.registerErrorPage(500, CustomErrorServlet.URL);
		
		sc.registerContext(context);
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
