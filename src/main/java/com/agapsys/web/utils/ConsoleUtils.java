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

package com.agapsys.web.utils;

/**
 * Console utilities
 * @author Leandro Oliveira (leandro@agapsys.com)
 */
public class ConsoleUtils {
	// CLASS SCOPE =============================================================
	
	/** Console colors. */
	public static enum ConsoleColor {
		BLACK        ("0;30"),
		DARK_GREY    ("1;30"),
		BLUE         ("0;34"),
		LIGHT_BLUE   ("1;34"),
		GREEN        ("0;32"),
		LIGHT_GREEN  ("1;32"),
		CYAN         ("0;36"),
		LIGHT_CYAN   ("1;36"),
		RED          ("0;31"),
		LIGHT_RED    ("1;31"),
		PURPLE       ("0;35"),
		LIGHT_PURPLE ("1;35"),
		BROWN        ("0;33"),
		ORANGE       ("0;33"),
		YELLOW       ("1;33"),
		LIGHT_GREY   ("0;37"),
		WHITE        ("1;37");
		
		private final String code;
		
		private ConsoleColor(String val){
			this.code = val;
		}

		private String getCode() {
			return code;
		}
		
		@Override
		public String toString() {
			return name();
		}
	}
	
	private static String getColorMessage(ConsoleColor color, String msg) {
		return String.format("\033[%sm%s\033[0m", color.getCode(), msg);
	}
	
	private static String getColorMessage(String msg, String colorToken, ConsoleColor...colors) {
		int i = 0;
	
		String msg2;
		do {
			msg2 = msg.replaceFirst(colorToken, String.format("\033[%sm", colors[i].getCode()));
			i++;
		} while (!msg2.equals(msg));
		
		if (i != colors.length)
			throw new IllegalArgumentException("color[].length does not match with given message token count");
		
		return String.format("%s\033[0m", msg2);
	}
	
	/** 
	 * Prints a message in system console. A line break will be appended at the end of the message.
	 * @param msg message to be printed.
	 */
	public static void println(String msg) {
		System.out.println(msg);
	}
	
	/**
	 * Prints a formatted message in system console. A line break will be appended at the end of the message.
	 * @param format format string
	 * @param args parameters to be added to final string
	 * @see String#format(String, Object...)
	 */
	public static void printlnf(String format, Object...args) {
		println(String.format(format, args));
	}
	
	/** 
	 * Prints a colored message in system console. A line break will be appended at the of the message.
	 * @param color color
	 * @param msg message to be printed
	 */
	public static void colorPrintln(ConsoleColor color, String msg) {
		println(getColorMessage(color, msg));
	}
	
	/**
	 * Prints a multi-colored message in system console. A line break will be appended at the end of the message.
	 * @param msg message to be printed
	 * @param colorToken color token in given message to be replaced by a color
	 * @param colors colors used in message
	 */
	public static void colorPrintln(String msg, String colorToken, ConsoleColor...colors) {
		println(getColorMessage(msg, colorToken, colors));
	}
	
	
	/** 
	 * Prints a message in system console.
	 * @param msg message to be printed.
	 */
	public static void print(String msg) {
		System.out.print(msg);
	}
	
	/**
	 * Prints a formatted message in system console.
	 * @param format format string
	 * @param args parameters to be added to final string
	 * @see String#format(String, Object...)
	 */
	public static void printf(String format, Object...args) {
		print(String.format(format, args));
	}
	
	/** 
	 * Prints a colored message in system console.
	 * @param color color
	 * @param msg message to be printed
	 */
	public static void colorPrint(ConsoleColor color, String msg) {
		print(getColorMessage(color, msg));
	}
	
	/**
	 * Prints a multi-colored message in system console.
	 * @param msg message to be printed
	 * @param colorToken color token in given message to be replaced by a color
	 * @param colors colors used in message
	 */
	public static void colorPrint(String msg, String colorToken, ConsoleColor...colors) {
		print(getColorMessage(msg, colorToken, colors));
	}
	// =========================================================================

	// INSTANCE SCOPE ==========================================================
	private ConsoleUtils() {}
	// =========================================================================
}
