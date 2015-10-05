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

import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.web.toolkit.Defs;
import com.agapsys.web.toolkit.ErrorServlet;
import com.agapsys.web.toolkit.OriginalRequestKeepFilter;
import com.agapsys.web.toolkit.WebApplication;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SmtpExceptionReporterTest {
	// CLASS SCOPE =============================================================
	@WebListener
	public static class Application extends WebApplication {

		@Override
		protected boolean isDebugEnabled() {
			return true;
		}
		
		@Override
		public String getName() {
			return Defs.APP_NAME;
		}

		@Override
		public String getVersion() {
			return Defs.APP_VERSION;
		}

		@Override
		protected String getSettingsFilenamePrefix() {
			return "smtp-exception-test";
		}

		@Override
		protected String getSettingsFilenameSuffix() {
			return ".conf";
		}
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private ServletContainer sc;
	
	@Before
	public void before() {
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerServlet(ErrorServlet.class);
		context.registerServlet(ExceptionServlet.class);
		context.registerEventListener(new Application());
		context.registerFilter(OriginalRequestKeepFilter.class);
		context.registerErrorPage(500, ErrorServlet.URL);
		
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
		HttpResponse resp = sc.doGet(url);
		Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resp.getStatusCode());
	}
	// =========================================================================
}
