package org.betaconceptframework.astroboa.admintools;

import groovy.sql.Sql

import javax.naming.Context
import javax.naming.InitialContext

import org.jboss.seam.ScopeType
import org.jboss.seam.annotations.In
import org.jboss.seam.annotations.Name
import org.jboss.seam.annotations.Scope
import org.jboss.seam.faces.FacesMessages
import org.jboss.seam.international.StatusMessage.Severity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Scope(ScopeType.EVENT)
@Name("repositoryManager")
public class RepositoryManager {
	
	private final String SYSTEM_PROPERTY_KEY_FOR_JBOSS_HOME_PATH = 'jboss.home.dir'
	private final String JBOSS_PATH = System.getProperty(SYSTEM_PROPERTY_KEY_FOR_JBOSS_HOME_PATH)
	
	private final String DEFAULT_JBOSS_USER = 'jboss'
	private final String RELATIVE_JBOSS_SERVER_DIR = File.separator + 'server'
	private final String RELATIVE_JBOSS_DEPLOY_DIR = RELATIVE_JBOSS_SERVER_DIR + File.separator +  'default' + File.separator + 'deploy'
	private final String RELATIVE_JBOSS_CONF_DIR = RELATIVE_JBOSS_SERVER_DIR + File.separator +  'default' + File.separator + 'conf'
	
	// the path in file system where jcr repositories are stored (indexes, worspace config, custom schema files, etc)
	private final String DEFAULT_REPOSITORY_PATH = File.separator + 'opt' + File.separator + 'Astroboa-Repositories'
	
	
	private final String JACKRABBIT_JCA_CURRENT_VERSION = '1.5.7'
	// the jackrabbit datasource template is stored inside the war in WEB-INF/classes so its path is relative to classpath
	private final String PATH_TO_JACKRABBIT_DATASOURCE_TEMPLATE_FILE = File.separator + 'repository-templates' + File.separator + 'jackrabbit-template-ds.xml'
	
	private final String REPOSITORY_DB_DATASOURCE_TEMPLATE_FILE_NAME = 'astroboa-default-db-ds.xml'
	private final String PATH_TO_REPOSITORY_DB_DATASOURCE_TEMPLATE_FILE = JBOSS_PATH + RELATIVE_JBOSS_DEPLOY_DIR + File.separator + REPOSITORY_DB_DATASOURCE_TEMPLATE_FILE_NAME
	
	@In
	private String repositoryInternalName;
	
	@In
	private String repositoryEnglishLabel;
	
	@In
	private String serverIPOrFullyQualifiedName;
	
	@In
	private String systemUserSecretKey;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void createNewRepository() {
		
		if (! createRepositoryDB()) {
			return;
		}
		
		if (! createDBDataSource()) {
			dropRepositoryDB()
			return;
		}
		
		if (! createJackrabbitDataSource()) {
			dropRepositoryDB()
			return;
		}
	
	}
	
	private boolean createRepositoryDB() {
		try {
			Context initCtx = new InitialContext();

			def dataSource = initCtx.lookup('java:jdbc/postgresAdministration');

			if (dataSource != null) {

				def postgres = new Sql(dataSource);
				postgres.execute ("CREATE DATABASE " + repositoryInternalName + " ENCODING 'UTF8' ");
				postgres.close();
				FacesMessages.instance().add(Severity.INFO, "The database '{0}' has been created. This database will store the repository data", repositoryInternalName)
				return true;
			}

		}
		catch (Exception e) {
			logger.error("Could not create the repository database",e);
			FacesMessages.instance().add(Severity.WARN, "The New Repository has not been created because the underlying Database could not be created", null);
			return false;
		}
	}
	
	private boolean dropRepositoryDB() {
		try {
			Context initCtx = new InitialContext();

			def dataSource = initCtx.lookup('java:jdbc/postgresAdministration');

			if (dataSource != null) {

				def postgres = new Sql(dataSource);
				postgres.execute ("DROP DATABASE " + repositoryInternalName);
				postgres.close();
				FacesMessages.instance().add(Severity.INFO, "The database '{0}' has been droped.", repositoryInternalName)
				return true;
			}

		}
		catch (Exception e) {
			logger.error("Could not drop the repository database",e);
			FacesMessages.instance().add(Severity.WARN, "A repository database has been created on the proccess of repository creation but due to some error it cannot be removed. Please remove it manually. The name of the database to be removed is {0}. If you try again to create the repository you cannot not use the same repository internal name before you manually remove the database", repositoryInternalName);
			return false;
		}
	}
	
	private boolean createDBDataSource() {
		try {
			
			def dbDataSourceTemplateAsXmlNode = 
				new XmlParser().parse(new File(PATH_TO_REPOSITORY_DB_DATASOURCE_TEMPLATE_FILE))
				
			def dataSourceJndiName = dbDataSourceTemplateAsXmlNode.'xa-datasource'.'jndi-name'[0]
			dataSourceJndiName.value = 'jdbc/' + repositoryInternalName
			
			def dataBaseName = dbDataSourceTemplateAsXmlNode.'xa-datasource'.'xa-datasource-property'.find(){it.'@name' == 'DatabaseName'}
			dataBaseName.value = repositoryInternalName
			
			def pathToDbDatasourceFileForNewRepository = JBOSS_PATH + RELATIVE_JBOSS_DEPLOY_DIR + File.separator + 'astroboa-' + repositoryInternalName + 'db-ds.xml'
			def writer = new BufferedWriter(new FileWriter(pathToDbDatasourceFileForNewRepository))
			writer.write '''<?xml version="1.0" encoding="UTF-8"?> \n'''
			new XmlNodePrinter(new PrintWriter(writer)).print(dbDataSourceTemplateAsXmlNode)
			FacesMessages.instance().add(Severity.INFO, "A datasource for the repository database has been created at {0}", pathToDbDatasourceFileForNewRepository)
			
		}
		catch (Exception e) {
			logger.error("Could not create the datasource for the repository database", e);
			FacesMessages.instance().add(Severity.WARN, "The New Repository has not been created because it was not possible to create the datasource for the repository database",null);
			return false;
		}
		
	}
	
	private boolean createJackrabbitDataSource() {
		try {
			
			def jackrabbitDataSourceTemplateAsXmlNode =
				new XmlParser().parse(this.getClass().getClassLoader().getResourceAsStream(PATH_TO_JACKRABBIT_DATASOURCE_TEMPLATE_FILE))
				
			def dataSourceJndiName = jackrabbitDataSourceTemplateAsXmlNode.'tx-connection-factory'.'jndi-name'[0]
			dataSourceJndiName.value = 'jcr/' + repositoryInternalName
			
			def rarName = jackrabbitDataSourceTemplateAsXmlNode.'tx-connection-factory'.'rar-name'[0]
			rarName.value = 'jackrabbit-jca-' + JACKRABBIT_JCA_CURRENT_VERSION + '.rar'
			
			def repositoryHomeDir = jackrabbitDataSourceTemplateAsXmlNode.'tx-connection-factory'.'config-property'.find(){it.'@name' == 'homeDir'}
			repositoryHomeDir.value = DEFAULT_REPOSITORY_PATH + File.separator + repositoryInternalName + File.separator + 'repository'
			
			def repositoryConfigFile = jackrabbitDataSourceTemplateAsXmlNode.'tx-connection-factory'.'config-property'.find(){it.'@name' == 'configFile'}
			repositoryConfigFile.value = DEFAULT_REPOSITORY_PATH + File.separator + repositoryInternalName + File.separator + 'repository.xml'
			
			def pathTojackrabbitDatasourceFileForNewRepository = JBOSS_PATH + RELATIVE_JBOSS_DEPLOY_DIR + File.separator + 'astroboa-jackrabbit-' + repositoryInternalName + '-ds.xml'
			def writer = new BufferedWriter(new FileWriter(pathTojackrabbitDatasourceFileForNewRepository))
			writer.write '''<?xml version="1.0" encoding="UTF-8"?> \n'''
			new XmlNodePrinter(new PrintWriter(writer)).print(pathTojackrabbitDatasourceFileForNewRepository)
			
			FacesMessages.instance().add(Severity.INFO, "A jackrabbit datasource for the repository has been created at {0}", pathTojackrabbitDatasourceFileForNewRepository)
			
		}
		catch (Exception e) {
			logger.error("Could not create the jackrabbit datasource for the repository", e);
			FacesMessages.instance().add(Severity.WARN, "The New Repository has not been created because it was not possible to create the jackrabbit datasource for the repository",null);
			return false;
		}
		
	}

}