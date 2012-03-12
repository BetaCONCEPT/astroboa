/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.context.JcrContext;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * Created on Jun 28, 2009
 */
@Name("scriptEngine")
@Scope(ScopeType.PAGE)
public class ScriptEngine {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private AstroboaClient astroboaClient;
	
	private PageController pageController;
	
	private String SCRIPTS_HOME_DIR = "astroboa-scripts";
	
	// holds utility functions exposed to running script
	private ScriptEngineUtilityAPI utilityAPI = new ScriptEngineUtilityAPI();
	
	@In(required=false)
	@Out(required=false)
	private String scriptSourceCode;

	@Out(required=false)
	private File loadedScriptFile;
	
	@In(required=false)
	@Out(required=false)
	private String currentScriptFileName;

	private JcrContext astroboaJcrContext;
	
	
	public void addNewScript() {
		currentScriptFileName="myNewScript.groovy";
		loadedScriptFile = null;
		scriptSourceCode = null;
		
		pageController.loadPageComponentInDynamicUIArea("/WEB-INF/pageComponents/scriptEngine/scriptEngine.xhtml");
	}
	
	public void loadScriptFromFileSystem(File scriptFile) {
		if (scriptFile.canRead()) {
			StringBuilder scriptContent = new StringBuilder();

			try {
				
				FileInputStream fis = new FileInputStream(scriptFile);
				InputStreamReader in = new InputStreamReader(fis, "UTF-8");
				BufferedReader input =  new BufferedReader(in);				
				try {
					String line = null;
					/*
					 * readLine is a bit quirky :
					 * it returns the content of a line MINUS the newline.
					 * it returns null only for the END of the stream.
					 * it returns an empty String if two newlines appear in a row.
					 */
					while (( line = input.readLine()) != null){
						scriptContent.append(line);
						scriptContent.append(System.getProperty("line.separator"));
					}
				}
				finally {
					input.close();
				}
			}
			catch (IOException e){
				logger.warn("Script content cannot be loaded.", e);
				utilityAPI.appendWarningToConsole("Script content cannot be loaded. IOException was thrown while loading. Message is:\n"
						+ e.getMessage()
						+ "\n" + prepareStackTrace(e));
			}

			scriptSourceCode = scriptContent.toString();
			loadedScriptFile = scriptFile;
			currentScriptFileName = scriptFile.getName();
			
			pageController.loadPageComponentInDynamicUIArea("/WEB-INF/pageComponents/scriptEngine/scriptEngine.xhtml");
		}

	}
	
	public String getConfirmationMessageForScriptSave() {
		File scriptFile = new File(System.getProperty("jboss.server.config.dir").substring(5) + SCRIPTS_HOME_DIR + File.separator + currentScriptFileName );
		if (scriptFile.exists()) {
			return "The file already exists. Are you sure you want to update it? If not press cancel and choose another file name.";
		}
		else {
			return "A new file will be created with the name:" + currentScriptFileName;
		}
	}
	
	public void saveScriptToFileSystem() {
		File scriptFile = new File(System.getProperty("jboss.server.config.dir").substring(5) + SCRIPTS_HOME_DIR + File.separator + currentScriptFileName );

		try {
			FileOutputStream fos = new FileOutputStream(scriptFile);
			OutputStreamWriter out = new OutputStreamWriter(fos, "UTF-8");
			BufferedWriter outWriter =  new BufferedWriter(out);
			try {
				outWriter.write(scriptSourceCode);
				utilityAPI.appendMessageToConsole(
						"Script has been saved.");
				// reread the script list
				Contexts.getPageContext().set("availableScripts", getAvailableScripts());
				// the currently loaded file should be the saved file
				loadedScriptFile = scriptFile;
			}
			finally {
				outWriter.close();
			}

		}
		catch (FileNotFoundException fileNotFoundException) {
			utilityAPI.appendWarningToConsole(
					"The requested file for saving cannot be found");
		}
		catch (UnsupportedEncodingException unsupportedEncodingException) {
			utilityAPI.appendWarningToConsole(
					"The requested file encoding (UTF-8) is not supported");
		}
		catch (IOException e) {
			utilityAPI.appendWarningToConsole("IOException was thrown while saving the script. Message is:\n"
					+ e.getMessage()
					+ "\n" + prepareStackTrace(e));
		}
				
	}
	
	@Factory(value="availableScripts", scope=ScopeType.PAGE)
	public List<File> getAvailableScripts() {
		
		List<File> scriptFileList = new ArrayList<File>();
		
		if (System.getProperty("jboss.server.config.dir") != null){
			File scriptHomeDir = new File(System.getProperty("jboss.server.config.dir").substring(5) + SCRIPTS_HOME_DIR + File.separator);
			if (scriptHomeDir.isDirectory()) {
				FileFilter groovySriptFileFilter = new FileFilter() {
					@Override
					public boolean accept(File file) {
						
						return file.getName().endsWith("groovy");
					}

				};
				
				scriptFileList = Arrays.asList(scriptHomeDir.listFiles(groovySriptFileFilter));
			}
		}
		return scriptFileList;
	}
	
	// The script will be retrieved from a specific file path inside the jboss conf directory.
	public void runScriptWithScriptEngine(String scriptFileName) {
		// reset console buffer
		utilityAPI.emptyConsoleBuffer();
		Binding binding;  // The 'binding' makes instances of the application objects available as 'variables' in the script
		URL[] roots;    // A list of directories to search for Groovy scripts (think of it as a PATH).
		
		URI scriptsAbsolutePath = null;
		if (System.getProperty("jboss.server.config.dir") != null){
			//We expect to find the scripts in JBOSS-HOME/server/default/conf/astroboa-scripts directory
			try {
				scriptsAbsolutePath = new URI(System.getProperty("jboss.server.config.dir")+SCRIPTS_HOME_DIR+File.separator);
				roots = new URL[]{scriptsAbsolutePath.toURL()};   // The root list is filled with the locations to be searched for the script
			}
			catch (URISyntaxException e) {
				logger.warn("A script path cannot be configured for the script engine. The script engine cannot run");
				utilityAPI.appendWarningToConsole("URISyntaxException was thrown in starting Groovy engine. Message is:\n"
						+ e.getMessage()
						+ "\n" + prepareStackTrace(e));
				return;
			}
			catch (MalformedURLException e) {
				logger.warn("A script path cannot be configured for the script engine. The script engine cannot run");
				utilityAPI.appendWarningToConsole("MalformedURLException was thrown in starting Groovy engine. Message is:\n"
						+ e.getMessage()
						+ "\n" + prepareStackTrace(e));
				return;
			}
			
		}
		else {
			logger.warn("A script path cannot be configured for the script engine. The script engine cannot run");
			utilityAPI.appendWarningToConsole("A script path cannot be configured for the script engine. The script engine cannot run");
			return;
		}
		
		
		Binding  scriptenv = new Binding();    // A new Binding is created ...
		
		// ... and filled with two 'variables': 
		// an instance of astroboaClient already logged in 
		// to the current repository as the current user
		scriptenv.setVariable("astroboaClient", astroboaClient);
		 // and an instance of the ScriptEngineUtilityAPI class as a utilities API provider for the running script.
		scriptenv.setVariable("utilityAPI", utilityAPI);
		
		//Expose Jcr context
		if (astroboaJcrContext == null)
		{
			
			try {
				  InitialContext ctx = new InitialContext();
				  
				  ApplicationContext springManagedRepositoryServicesContext = (ApplicationContext) ctx.lookup("astroboa.engine.context");
				  
				  if (springManagedRepositoryServicesContext != null)
				  {
					  astroboaJcrContext  = (JcrContext) springManagedRepositoryServicesContext.getBean("jcrContext");
				  }
				  
				} catch (Exception e) {
				  e.printStackTrace ();
				}

		}
		
		scriptenv.setVariable("astroboaJcrContext", astroboaJcrContext);
		
		binding = scriptenv;
		
		GroovyScriptEngine gse = null;
		
		gse = new GroovyScriptEngine(roots);   // instantiate  the script engine ...
		
		if (gse != null) {
			try {
				gse.run(scriptFileName, binding);      // ... and running the specified script
			} 
			catch (ResourceException re) {
				logger.warn("ResourceException in calling groovy script '" + scriptsAbsolutePath.getPath() + scriptFileName, re);
				utilityAPI.appendWarningToConsole("ResourceException in calling groovy script '" + scriptsAbsolutePath.getPath() + scriptFileName +
						"' Message is:\n" +re.getMessage()
						+ "\n" + prepareStackTrace(re));

			} 
			catch (ScriptException se) {
				logger.warn("ScriptException in calling groovy script '" + scriptsAbsolutePath.getPath() + scriptFileName, se);
				utilityAPI.appendWarningToConsole("ScriptException in calling groovy script '" + scriptsAbsolutePath.getPath() + scriptFileName +
						"' Message is:\n" +se.getMessage()
						+ "\n" + prepareStackTrace(se));
			}
			catch (Exception e) {
				logger.warn("Exception was thrown in calling groovy script '" + scriptsAbsolutePath.getPath() + scriptFileName, e);
				utilityAPI.appendWarningToConsole("ScriptException in calling groovy script '" + scriptsAbsolutePath.getPath() + scriptFileName +
						"' Message is:\n" + e.getMessage()
						+ "\n" + prepareStackTrace(e));
			}
		}

	}
	
	
	// The script is retrieved from astroboa repository. The scriptObjectName is the system name of a content object of type "scriptObject" which contains the script
	public void runScriptWithClassLoader(String scriptObjectName) {
		
		ContentObject scriptObject = null;
		ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria("scriptObject");
		contentObjectCriteria.addSystemNameEqualsCriterion(scriptObjectName);
		
		CmsOutcome<ContentObject> cmsOutcome = astroboaClient.getContentService().searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		if (cmsOutcome.getCount() > 0) {
			scriptObject = cmsOutcome.getResults().get(0);
		}
		else {
			logger.warn("No Script Object exists with systen name: " + scriptObjectName);
			utilityAPI.appendWarningToConsole("No Script Object exists with systen name:" + scriptObjectName);
			return;
		}
		
		if (((StringProperty)scriptObject.getCmsProperty("body")).getSimpleTypeValue() == null) {
			logger.warn("Script Object: " + scriptObjectName + " contains an empty script");
			utilityAPI.appendWarningToConsole("Script Object: " + scriptObjectName + " contains an empty script");
			return;
		}
		
		ClassLoader parentClassLoader = getClass().getClassLoader();
		GroovyClassLoader groovyClassLoader = new GroovyClassLoader(parentClassLoader);

		if (groovyClassLoader != null) {
			Class scriptClass = groovyClassLoader.parseClass(((StringProperty)scriptObject.getCmsProperty("body")).getSimpleTypeValue());
			try {
				GroovyObject groovyObject = (GroovyObject) scriptClass.newInstance();
				Object[] args = {};
				groovyObject.invokeMethod("run", args);
			}
			catch (Exception e) {
				logger.warn("Script Class could not be created", e);
				utilityAPI.appendWarningToConsole("Script Class could not be created");
			}
		}
		else {
			logger.warn("Script Engine could not be created");
			utilityAPI.appendWarningToConsole("Script Engine could not be created");
		}

	}



	// prepare a stack trace to be shown in an output window
	private String prepareStackTrace(Exception e) {
		Throwable exc = e;
		StringBuffer output = new StringBuffer();
		collectTraces(exc, output);
		if (exc.getCause() != null) {
			exc = exc.getCause();
			output.append("caused by::\n");
			output.append(exc.getMessage());
			output.append("\n");
			collectTraces(exc, output);
		}
		return output.toString();
	}

	private void collectTraces(Throwable e, StringBuffer output) {
		StackTraceElement[] trace = e.getStackTrace();
		for (int i=0; i < trace.length; i++) {
			output.append(trace[i].toString());
			output.append("\n");
		}
	}
	
	public ScriptEngineUtilityAPI getUtilityAPI() {
		return utilityAPI;
	}
	
}

