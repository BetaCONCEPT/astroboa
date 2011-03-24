/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.console.scripting;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * Created on Feb 28, 2010
 * 
 * This class provides a set of function calls available
 * to groovy scripts running inside Astroboa Console
 * 
 * The most important functions are those that enable the script to
 * append messages to an output console or log messages
 */
public class ScriptEngineUtilityAPI {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private StringBuilder scriptConsoleBuffer = new StringBuilder();
	
	
	
	// Method to show Groovy related errors/warnings in a dialog window.
	public void appendWarningToConsole(String message) {
		scriptConsoleBuffer.append("+++WARNING+++: " + message);
	}

	// Method to show Groovy related output messages in a dialog window.
	public void appendMessageToConsole(String message) {
		if (scriptConsoleBuffer == null) {
			scriptConsoleBuffer = new StringBuilder();
		}
		scriptConsoleBuffer.append(message);
	}
	
	public String getScriptConsoleBuffer() {
		return scriptConsoleBuffer != null? scriptConsoleBuffer.toString() : "";
	}
	
	// Return the contents of Console Buffer as String and then empty the buffer
	// It is meant to be used when regularly polling the buffer and want to show only the new content in each poll
	public String readAndEmptyConsoleBuffer() {
		String consoleBufferAsString = scriptConsoleBuffer != null? scriptConsoleBuffer.toString() : "";
		scriptConsoleBuffer = new StringBuilder();
		return consoleBufferAsString;
	}
	
	public void emptyConsoleBuffer() {
		scriptConsoleBuffer = new StringBuilder();
	}
	
	public Process exec(String command, File inDir) {
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(command, null, inDir);
		} catch (Exception e) {
			appendWarningToConsole(e.toString());
		}
		return proc;
	}

	// create a process to run a shell command
	public Process exec(String command) {
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			appendWarningToConsole(e.toString());
		}
		return proc;
	}
	
	public Logger getLogger() {
		return logger;
	}
}
