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

package com.agapsys;

public class Utils {
	// CLASS SCOPE =============================================================
	public static void printCurrentMethod() {
		StackTraceElement ste = Thread.currentThread().getStackTrace()[2];
		String[] classNameTokens = ste.getClassName().split("\\.");
		System.out.println(String.format("[%s::%s]", classNameTokens[classNameTokens.length - 1], ste.getMethodName()));
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private Utils() {}
	// =========================================================================
}
