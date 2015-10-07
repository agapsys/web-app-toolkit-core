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
import com.agapsys.web.toolkit.AbstractWebApplication;
import com.agapsys.web.toolkit.DefaultFilter;
import com.agapsys.web.toolkit.TestApplication;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationDisableTest {
	// CLASS SCOPE =============================================================
	@WebListener
	public static class DisabledApplication extends TestApplication {

		@Override
		protected Properties getDefaultProperties() {
			Properties properties = super.getDefaultProperties();
			if (properties == null)
				properties = new Properties();
			
			properties.setProperty(AbstractWebApplication.KEY_APP_DISABLE, "" + true);
			
			return properties;
		}
	}
	
	public static class EnabledApplication extends TestApplication {}
	
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
		ApplicationContext enabledContext = new ApplicationContext();
		enabledContext.registerEventListener(new EnabledApplication());
		enabledContext.registerServlet(TestServlet.class);
		enabledContext.registerFilter(DefaultFilter.class);
		
		sc = new ServletContainer();
		sc.registerContext(enabledContext, "/enabled");
		sc.startServer();
		
		HttpResponse resp = sc.doGet("/enabled/test");
		Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
	}
	
	@Test
	public void testDisabledApplication() {
		ApplicationContext disabledContext = new ApplicationContext();
		disabledContext.registerEventListener(new DisabledApplication());
		disabledContext.registerServlet(TestServlet.class);
		disabledContext.registerFilter(DefaultFilter.class);
		
		sc = new ServletContainer();
		sc.registerContext(disabledContext, "/disabled");
		sc.startServer();
		
		HttpResponse resp = sc.doGet("/disabled/test");
		Assert.assertEquals(HttpServletResponse.SC_SERVICE_UNAVAILABLE, resp.getStatusCode());
	}
	// =========================================================================
}
