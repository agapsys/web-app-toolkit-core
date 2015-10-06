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

public class TestApplication extends WebApplication {
	
	@Override
	public String getName() {
		return Defs.APP_NAME;
	}

	@Override
	public String getVersion() {
		return Defs.APP_VERSION;
	}

	@Override
	public String getEnvironment() {
		return Defs.ENVIRONMENT;
	}

	@Override
	protected boolean isDirectoryCreationEnabled() {
		return false;
	}

	@Override
	protected boolean isPropertiesFileCreationEnabled() {
		return false;
	}

	@Override
	protected boolean isPropertiesFileLoadingEnabled() {
		return false;
	}

	@Override
	protected boolean isDebugEnabled() {
		return true;
	}

	@Override
	protected void beforeApplicationStart() {}
}
