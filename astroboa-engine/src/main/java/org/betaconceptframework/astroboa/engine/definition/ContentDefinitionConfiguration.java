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

package org.betaconceptframework.astroboa.engine.definition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.engine.definition.visitor.CmsDefinitionVisitor;
import org.betaconceptframework.astroboa.engine.definition.xsom.CmsEntityResolverForValidation;
import org.betaconceptframework.astroboa.engine.definition.xsom.CmsXsomParserFactory;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.CmsConstants.CmsMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.parser.XSOMParser;


/**
 * Class responsible to load Content Definition Configuration settings.
 * Content Definition configuration contains xml file for content definition information
 * and a properties file containing flags for action on content definition information.
 * For now content definition information is reloaded only if appropriate flag is set to true and
 * at the same time content definition file has been Modified
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentDefinitionConfiguration {

	private PropertiesConfiguration configuration;
	
	//Key is repository id
	private Map<String, List<FileConfiguration>> definitionFileConfigurations = new HashMap<String, List<FileConfiguration>>();
	
	private CmsXsomParserFactory cmsXsomParserFactory;
	private CmsDefinitionVisitor definitionVisitor;

	private  final Logger logger = LoggerFactory.getLogger(ContentDefinitionConfiguration.class);

	//Default value is production
	private CmsMode cmsMode;

	private List<String> builtinDefinitionSchemas;

	public void setBuiltinDefinitionSchemas(
			List<String> builtinDefinitionSchemas) {
		this.builtinDefinitionSchemas = builtinDefinitionSchemas;
	}

	public void setCmsXsomParserFactory(CmsXsomParserFactory cmsXsomParserFactory) {
		this.cmsXsomParserFactory = cmsXsomParserFactory;
	}

	public void setDefinitionVisitor(CmsDefinitionVisitor definitionVisitor) {
		this.definitionVisitor = definitionVisitor;
	}

	private void loadConfigurationFiles(CmsRepository associatedRepository, boolean logWarningIfNoXSDFileFound) {

		//Load BetaConcept Definition files from repository home directory
		//which exists in RepositoryContextImpl
		try{
			
			File[] schemaFiles = retrieveXmlSchemaFiles(associatedRepository,  logWarningIfNoXSDFileFound);

			
			//Create a file configuration for each xsd file
			//This is done in order to track any changes made at runtime to XSD
			//in order to reload definition
			definitionFileConfigurations.put(associatedRepository.getId(), new ArrayList<FileConfiguration>());

			if (ArrayUtils.isEmpty(schemaFiles) && logWarningIfNoXSDFileFound){
				logger.warn("Found no definition schema files for repository "+ associatedRepository);
			}
			else{
				for (File defFile: schemaFiles){
					try{
						logger.debug("Loading definition file {} for repository {}", defFile.getAbsolutePath(), associatedRepository.getId());

						XMLConfiguration fileConfiguration = new XMLConfiguration(defFile);
						fileConfiguration.setReloadingStrategy(new FileChangedReloadingStrategy());
						definitionFileConfigurations.get(associatedRepository.getId()).add(fileConfiguration);
					}
					catch(Exception e){
						logger.error("Error loading definition file "+defFile.getAbsolutePath()+" for repository "+
								 associatedRepository.getId()+"Most probably, it is not a valid XML file. All other definitions will be loaded", e);
						
						//Load an empty xml configuration
						XMLConfiguration fileConfiguration = new XMLConfiguration();
						definitionFileConfigurations.get(associatedRepository.getId()).add(fileConfiguration);
					}
				}
			}
		}
		catch (Throwable e)
		{
			throw new CmsException(e);
		}
	}

	private File[] retrieveXmlSchemaFiles(CmsRepository associatedRepository, boolean logWarningIfNoXSDFileFound) {
		if (associatedRepository == null || StringUtils.isBlank(associatedRepository.getRepositoryHomeDirectory())){
			throw new CmsException("Unable to locate repository home directory."+ 
					(associatedRepository == null? "No associated repository to current thread": "Undefined repository home dir for repository "+
							associatedRepository.getId()));
		}

		//Directory where definition xsd files are kept is defined from
		//repository home dir + directory name provided in content-definition.properties file
		String contentDefinitionSchemaPath = getDefinitionHomeDirPath(associatedRepository);

		File contentDefinitionSchemaDir = new File(contentDefinitionSchemaPath);

		//Check if directory exists
		if (!contentDefinitionSchemaDir.exists()){
			
			if (logWarningIfNoXSDFileFound){
				logger.warn("Unable to locate schema home directory {}. Only built in schemas will be loaded", contentDefinitionSchemaPath);
			}
			return new File[]{};
		}

		//Load all xsd files
		return contentDefinitionSchemaDir.listFiles(new XsdFilter());
	}

	private String getDefinitionHomeDirPath(CmsRepository associatedRepository) {
		String contentDefinitionSchemaPath = associatedRepository.getRepositoryHomeDirectory()+File.separator+
		configuration.getString(CmsConstants.BETACONCEPT_CONTENT_DEFINITION_SCHEMA_DIR, "");
		return contentDefinitionSchemaPath;
	}


	public void loadDefinitionToCache() throws Exception {

		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForActiveClient();

		if (repositoryContext == null || repositoryContext.getCmsRepository() == null){
			//No repository context found. Do nothing
			logger.warn("Unable to load definition files. No repository context found");
			return;
		}

		if (mustReloadDefinitionFiles(repositoryContext.getCmsRepository())){
			logger.debug("At least one definition file has been changed. Reloading takes place");
			refreshContentDefinition(repositoryContext.getCmsRepository());
		}

	}


	private boolean isCmsInProductionMode(){
		return CmsMode.production == cmsMode;
	}

	private void refreshContentDefinition(CmsRepository associatedRepository) throws Exception  {

		logger.debug("Reloading definition files");

		//Clear cache
		definitionVisitor.clear();

		try{

			if (definitionFileConfigurations == null){
				definitionFileConfigurations = new HashMap<String, List<FileConfiguration>>();
			}
			
			//This ensures that warning for empty XSD directory is issued only once
			 boolean logWarningIfNoXSDFileFound = definitionFileConfigurations.get(associatedRepository.getId())== null;
			 
			if (definitionFileConfigurations.containsKey(associatedRepository.getId())){
				//remove existing file configurations in order to reload REPOSITORY schemas
				//thus loading any new file added
				definitionFileConfigurations.remove(associatedRepository.getId());
			}

			loadConfigurationFiles(associatedRepository, logWarningIfNoXSDFileFound);

			List<FileConfiguration> repositoryDefinitionFileConfigurations = definitionFileConfigurations.get(associatedRepository.getId());

			XSOMParser xsomParser = createXsomParser();
			
			//Feed parser with user	defined schemas
			feedParserWithUserDefinedSchemas(xsomParser, repositoryDefinitionFileConfigurations);
			
			//Feed parser with builtin schemas
			feedParserWithBuiltInSchemas(xsomParser);

			//Load schemas to Definitions
			generateDefinitions(xsomParser);

		}
		catch(Exception e){
			if (definitionVisitor != null){
				definitionVisitor.clear();
			}
			
			//Something went wrong. At least load built in schemas.
			try{
				XSOMParser xsomParser = createXsomParser();

				//Feed parser with builtin schemas
				feedParserWithBuiltInSchemas(xsomParser);

				generateDefinitions(xsomParser);
			}
			catch(Exception e1){
				logger.error("Repository "+AstroboaClientContextHolder.getActiveRepositoryId()+" - Loading schemas to Astroboa Definitions failed.",e);
				logger.error("Repository "+AstroboaClientContextHolder.getActiveRepositoryId()+" - Loading built in only schemas as a fallback safe mechanism also failed." ,e1);

				if (definitionVisitor != null){
					definitionVisitor.clear();
				}

				throw e;
			}
				
			logger.warn("Repository "+AstroboaClientContextHolder.getActiveRepositoryId()+" - Loading schemas to Astroboa Definitions failed. Check error stack trace for more details. Neverthelss built in only schemas have been successfully loaded",e);
		}

	}

	private void generateDefinitions(XSOMParser xsomParser) throws SAXException, Exception {
		
		Map<String, XSSchema> schemas = new HashMap<String, XSSchema>();
		
		int index =1;
		
		final Collection<XSSchema> schemaSet = xsomParser.getResult().getSchemas();
		for (XSSchema schema: schemaSet) {
			final String targetNamespace = schema.getTargetNamespace();
			if (StringUtils.isBlank(targetNamespace)){
				//Put an index as a key
				schemas.put(String.valueOf(index++), schema);
			}
			else if (!schemas.containsKey(targetNamespace)){
				schemas.put(schema.getTargetNamespace(), schema);
			}
			schema.visit(definitionVisitor);
		}

		//Definition Visitor will put in cache all definitions
		//Definition cache will obtain repository context to 
		//correctly place definitions according to repository
		definitionVisitor.createContentDefintions();

	}

	private void feedParserWithBuiltInSchemas(XSOMParser xsomParser) {
		if (CollectionUtils.isNotEmpty(builtinDefinitionSchemas)){
			for (String builtinDefinitionSchema: builtinDefinitionSchemas){
				
				try{
					URL resource = this.getClass().getResource(builtinDefinitionSchema);
					
					//Do not parse astroboa-api.x.xsd.
					if (! builtinDefinitionSchema.contains(CmsConstants.ASTROBOA_API_SCHEMA_FILENAME)){
						xsomParser.parse(resource);
					}
					definitionVisitor.addXMLSchemaDefinitionForFileName(resource);
				}
				catch(Exception e){
					throw new CmsException("Parse error for definition file "+builtinDefinitionSchema, e);
				}
			}
		}
	}

	private void feedParserWithUserDefinedSchemas(XSOMParser xsomParser,
			List<FileConfiguration> repositoryDefinitionFileConfigurations) {
		
		List<String> absolutePathsOfFilesToExclude = new ArrayList<String>();
		
		boolean feedParser = true;
		
		while (feedParser){

			feedParser = false;
			
			//Create XSOM Parser
			if (xsomParser == null){
				xsomParser = createXsomParser();
			}
			
			for (FileConfiguration fileConf: repositoryDefinitionFileConfigurations){
				if (fileConf.getFile() == null){
					logger.warn("Found empty file configuration. This means that one of the XSD provided is not a valid xml. Parsing will continue for the rest of the xsds");
				}
				else {
					
					String absolutePath = fileConf.getFile().getAbsolutePath();
					
					if (! absolutePathsOfFilesToExclude.contains(absolutePath)){
					
						logger.debug("Reloadding and parsing file {}", absolutePath);

						try{
							fileConf.reload();
							xsomParser.parse(fileConf.getFile());
							definitionVisitor.addXMLSchemaDefinitionForFileName(FileUtils.readFileToByteArray(fileConf.getFile()),
									StringUtils.substringAfterLast(absolutePath, File.separator));
						}
						catch(Exception e){
							//Just issue a warning
							logger.warn("Parse error for definition file "+absolutePath+ " This file is excluded from building Astroboa Definitions", e);


							//we need to feed parser again since it sets an error flag to true 
							//and does not produce any schemas at all.
							feedParser = true;
							absolutePathsOfFilesToExclude.add(absolutePath);
							xsomParser = null;
							definitionVisitor.clear();
							break;
						}
					}
				}
			}
		}
		
	}

	private XSOMParser createXsomParser() {
		XSOMParser xsomParser;
		xsomParser = cmsXsomParserFactory.createXsomParser();
		return xsomParser;
	}


	public void setConfigurationFile(String configurationFile) {
		if (configurationFile != null)
			try {
				configuration = new PropertiesConfiguration(configurationFile);
				configuration.setReloadingStrategy(new FileChangedReloadingStrategy());

				final String cmsModeFromFile = configuration.getString(CmsConstants.CMS_MODE, CmsMode.production.toString());
				//Set CmsMode
				if (cmsModeFromFile != null)
					cmsMode = CmsMode.valueOf(cmsModeFromFile.toLowerCase());
				else
					cmsMode = CmsMode.production;

			} catch (Exception e) {
				logger.warn("Unable to load content definition properties file", e);
			} 
	}

	private boolean mustReloadDefinitionFiles(CmsRepository associatedRepository) {

		//Check reload flag with default value set to false
		if (isCmsInProductionMode()){
			if (definitionFileConfigurations != null && CollectionUtils.isNotEmpty(definitionFileConfigurations.get(associatedRepository.getId()))){
				return false;
			}
			else{
				return true;
			}
		}
		//Debug mode
		else if (configuration != null) 
		{ 


			final boolean reloadDefinitionFiles = configuration.getBoolean(CmsConstants.RELOAD_CONTENT_DEFINITION_FILE, false);

			//Reloading has been disabled but no definition files have ever been loaded
			//It may be the first time
			if (! reloadDefinitionFiles && (
					definitionFileConfigurations == null ||
					! definitionFileConfigurations.containsKey(associatedRepository.getId())
			)){
				return true;
			}

			if ( reloadDefinitionFiles && definitionFileConfigurations != null)
			{

				List<FileConfiguration> repositoryFileConfigurations = definitionFileConfigurations.get(associatedRepository.getId());

				if (repositoryFileConfigurations == null){
					return true;
				}
				
				//Check if any file has been added or removed
				//Get files that exist in definition directory
				File[] definitionSchemaFiles = retrieveXmlSchemaFiles(associatedRepository, false);

				//At least one definition was added or removed
				if (repositoryFileConfigurations.size() != definitionSchemaFiles.length){
					return true;
				}

				//Reload flag is set to true. Check that file is changed
				boolean atLeastOneFileChanged = false;
				for(FileConfiguration fileConf: repositoryFileConfigurations){
					if (fileConf.getReloadingStrategy().reloadingRequired()){
						fileConf.getReloadingStrategy().reloadingPerformed();
						logger.info("Definition file {} has been modified and will be reloaded for repository {}", fileConf.getFileName(), 
								associatedRepository.getId());
						atLeastOneFileChanged = true;
						break;
					}
				}

				return atLeastOneFileChanged;
				
			}

		}

		return false;
	}
	
	public boolean definitionFileForActiveRepositoryIsValid(String definitionToBeValidated, String definitionFileName) throws Exception{
		
		if (StringUtils.isBlank(definitionToBeValidated) || StringUtils.isBlank(definitionFileName)){
			return false;
		}
		
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForActiveClient();

		if (repositoryContext == null || repositoryContext.getCmsRepository() == null){
			//No repository context found. Do nothing
			logger.warn("Unable to validate definition files. No repository context found");
			return false;
		}

		final CmsRepository associatedRepository = repositoryContext.getCmsRepository();
		
		File[] existingDefinitionFiles = retrieveXmlSchemaFiles(associatedRepository, false); 
			
		CmsEntityResolverForValidation entityResolverForValidation = null;
		XSOMParser xsomParser = null;
		List<InputStream> openStreams = new ArrayList<InputStream>();
		
		try{
			entityResolverForValidation = createEntityResolverForValidation(existingDefinitionFiles, definitionToBeValidated, definitionFileName);
			
			//Feed parser with user defined schemas
			xsomParser = cmsXsomParserFactory.createXsomParserForValidation(entityResolverForValidation);

			//Feed parser with builtin schemas
			if (MapUtils.isNotEmpty(entityResolverForValidation.getDefinitionSources())){
				for (Entry<String,String> definitionSource: entityResolverForValidation.getDefinitionSources().entrySet()){
					
					if (! StringUtils.equals(CmsConstants.ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION, definitionSource.getKey())){
						try{
							InputStream openStream = IOUtils.toInputStream(definitionSource.getValue(), "UTF-8");
							openStreams.add(openStream);

							InputSource is = new InputSource(openStream);
							is.setSystemId(definitionSource.getKey());

							xsomParser.parse(is);
						}
						catch(Exception e){
							throw new CmsException("Parse error for definition "+definitionSource.getKey(), e);
						}
					}
				}
			}
			
			//Force parser to create XSD schemas. This way more errors can be detected
			xsomParser.getResult().getSchemas();
		
	}
	catch(Exception e){
		throw e;
	}
	finally{
		xsomParser = null;
		
		if (entityResolverForValidation != null){
			entityResolverForValidation.clearDefinitions();
		}
		
		if (! openStreams.isEmpty()){
			for (InputStream is : openStreams){
				IOUtils.closeQuietly(is);
			}
		}
	}

	return true;
		
	}

	private CmsEntityResolverForValidation createEntityResolverForValidation(File[] existingDefinitionFiles, String definitionToBeValidated, String definitionFileName) throws IOException {
		
		CmsEntityResolverForValidation entityResolver = new CmsEntityResolverForValidation();
		
		boolean definitionToBeValidatedHasBeenAdded = false;
		
		for (File existingDefinitionFile : existingDefinitionFiles){
			
			if (StringUtils.equals(definitionFileName, existingDefinitionFile.getName())){
				entityResolver.addDefinition(definitionFileName, definitionToBeValidated);
				definitionToBeValidatedHasBeenAdded = true;
			}
			else{
				entityResolver.addExternalDefinition(existingDefinitionFile);
			}
		}
		
		if (! definitionToBeValidatedHasBeenAdded){
			//New definition
			entityResolver.addDefinition(definitionFileName, definitionToBeValidated);
		}
		
		return entityResolver;
	}

}
