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

import com.agapsys.web.modules.impl.DefaultCrashReporterModule;
import com.agapsys.sevlet.test.ApplicationContext;
import com.agapsys.sevlet.test.HttpResponse;
import com.agapsys.sevlet.test.ServletContainer;
import com.agapsys.web.modules.CrashReporterModule;
import com.agapsys.web.modules.impl.SmtpCrashReporterModule;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

public class MaintenanceModuleTest {
	// CLASS SCOPE =============================================================
	@WebServlet(ExceptionServlet.URL)
	public static class ExceptionServlet extends HttpServlet {
		public static final String URL = "/exception";

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
			throw new RuntimeException();
		}
	}
	
	@WebListener
	public static class TestApplication extends WebApplication {
		private CrashReporterModule crm = null;
		
		@Override
		protected String getAppName() {
			return Defs.APP_NAME;
		}

		@Override
		protected String getAppVersion() {
			return Defs.APP_VERSION;
		}

		@Override
		protected String getDefaultEnvironment() {
			return Defs.ENVIRONMENT;
		}

		@Override
		protected CrashReporterModule getCrashReporterModule() {
			if (crm == null)
				crm = new SmtpCrashReporterModule();
			
			return crm;
		}		
	}
	
	private static ServletContainer sc;
	
	@BeforeClass
	public static void beforeClass() {
		
		sc = new ServletContainer();
		
		ApplicationContext context = new ApplicationContext();
		context.registerEventListener(new TestApplication());
		context.registerServlet(ErrorServlet.class);
		context.registerServlet(ExceptionServlet.class);
		
		context.registerErrorPage(500, ErrorServlet.URL);
		
		sc.registerContext(context, "/");
		sc.startServer();
	}
	
	@AfterClass
	public static void afterClass() {
		sc.stopServer();
	}
// =========================================================================
	
	// INSTANCE SCOPE ==========================================================
	private HttpResponse sendErrorRequest() {
		return sc.doGet(ExceptionServlet.URL);
	}
	
	@Test
	public void testErrorPage() {
		HttpResponse resp = sendErrorRequest();
		if (!resp.getFirstHeader(SmtpCrashReporterModule.HEADER_STATUS).getValue().equals(SmtpCrashReporterModule.HEADER_STATUS_VALUE_OK)) {
			System.out.println(String.format("Check smtp settings"));
		}
		assertEquals(SmtpCrashReporterModule.HEADER_STATUS_VALUE_OK, resp.getFirstHeader(SmtpCrashReporterModule.HEADER_STATUS).getValue());
	}
	// =========================================================================
}
