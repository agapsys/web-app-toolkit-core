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

package com.agapsys.web.toolkit;

import com.agapsys.agreste.utils.HttpUtils;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebApplicationFilter implements Filter {
	// CLASS SCOPE =============================================================
	public static final String ATTR_ORIGINAL_REQUEST_URI = "com.agapsys.web.toolkit.originalRequestUri";
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		AbstractWebApplication webApp = AbstractWebApplication.getRunningInstance();
		
		if (webApp != null) {
			if (webApp.isDisabled()) {
				resp.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				resp.flushBuffer();
				return;
			} 

			if (!webApp.isOriginAllowed(req)) {
				resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				resp.flushBuffer();
				return;
			}
		}
		
		request.setAttribute(ATTR_ORIGINAL_REQUEST_URI, HttpUtils.getRequestUri((HttpServletRequest) request));
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {}
	// =========================================================================
}
