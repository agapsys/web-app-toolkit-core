/*
 * Copyright 2015-2016 Agapsys Tecnologia Ltda-ME.
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
import com.agapsys.jee.TestingServletContainer;
import com.agapsys.web.toolkit.AbstractApplication;
import com.agapsys.web.toolkit.MockedWebApplication;
import com.agapsys.web.toolkit.WebApplicationFilter;
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
    // <editor-fold desc="STATIC SCOPE">
    // =========================================================================
    @WebListener
    public static class ForbiddenLocalHostApp extends MockedWebApplication {

        @Override
        protected boolean isOriginAllowed(HttpServletRequest req) {
            return false;
        }
    }

    public static class AnyOriginApp extends MockedWebApplication {}

    @WebServlet("/*")
    public static class TestServlet extends HttpServlet {

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            resp.getWriter().print("OK");
        }
    }
    // =========================================================================
    // </editor-fold>

    private TestingServletContainer tc;

    @After
    public void after() {
        tc.stop();
        Assert.assertNull(AbstractApplication.getRunningInstance());
    }

    @Test
    public void testEnabledApplication() {
        tc = TestingServletContainer.newInstance()
            .registerServletContextListener(AnyOriginApp.class)
            .registerServlet(TestServlet.class)
            .registerFilter(WebApplicationFilter.class, "/*");

        tc.start();

        HttpResponse resp = tc.doRequest(new HttpGet("/test"));
        Assert.assertEquals(HttpServletResponse.SC_OK, resp.getStatusCode());
    }

    @Test
    public void testDisabledApplication() {
        tc = TestingServletContainer.newInstance()
            .registerServletContextListener(ForbiddenLocalHostApp.class)
            .registerServlet(TestServlet.class)
            .registerFilter(WebApplicationFilter.class, "/*");

        tc.start();

        HttpResponse resp =tc.doRequest(new HttpGet("/test"));
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, resp.getStatusCode());
    }
}
