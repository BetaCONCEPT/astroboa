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
package org.betaconceptframework.astroboa.test.engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.activation.MimetypesFileTypeMap;
import javax.jcr.Session;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.log4j.Level;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.DoubleProperty;
import org.betaconceptframework.astroboa.api.model.LongProperty;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration.PersistMode;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.RepositoryUserCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedAccessException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.definition.ContentDefinitionConfiguration;
import org.betaconceptframework.astroboa.engine.definition.RepositoryEntityResolver;
import org.betaconceptframework.astroboa.engine.jcr.dao.ImportDao;
import org.betaconceptframework.astroboa.engine.jcr.dao.SerializationDao;
import org.betaconceptframework.astroboa.engine.jcr.identitystore.jackrabbit.JackrabbitIdentityStoreDao;
import org.betaconceptframework.astroboa.engine.jcr.io.ContentSourceExtractor;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.jaxb.CmsEntitySerialization;
import org.betaconceptframework.astroboa.test.AbstractAstroboaTest;
import org.betaconceptframework.astroboa.test.AstroboaTestContext;
import org.betaconceptframework.astroboa.test.TestConstants;
import org.betaconceptframework.astroboa.test.log.TestLogPolicy;
import org.betaconceptframework.astroboa.test.util.JAXBTestUtils;
import org.betaconceptframework.astroboa.test.util.JAXBValidationUtils;
import org.betaconceptframework.astroboa.test.util.TestUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.testng.Assert;
import org.testng.annotations.AfterTest;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class AbstractRepositoryTest extends AbstractAstroboaTest{

	protected CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	protected TestCmsDao testCmsDao;  
	private Session session;
	
	protected File logo;
	protected File logo2;
	
	protected Random random = new Random(10000);
	
	protected final String TEST_CONTENT_TYPE = "test";
	protected final String EXTENDED_TEST_CONTENT_TYPE = "extendedTest";
	protected final String DIRECT_EXTENDED_TEST_CONTENT_TYPE = "extendedTestDirectlyUsingType";
	
	protected JAXBValidationUtils jaxbValidationUtils;
	protected SerializationDao serializationDao;
	protected ImportDao importDao;
	protected RepositoryEntityResolver repositoryEntityResolver;
	protected ContentSourceExtractor contentSourceExtractor;
	protected RepositoryContentValidator repositoryContentValidator;
	
	protected boolean prettyPrint = false;
	
	private DatatypeFactory df ;
	
	//The value of this property is provided in pom.xml
	//by maven-surefire-plugin configuration properties
	private String astroboaVersion = null;
	
	protected void preSetup() throws Exception{
		
		System.setProperty("java.security.auth.login.config",new ClassPathResource("astroboa-test-jaas.config").getURL().toString());
		
		astroboaVersion = System.getProperty("astroboaVersion");
		
		TestLogPolicy.setLevelForLogger(Level.ERROR, ContentDefinitionConfiguration.class.getName());
		TestLogPolicy.setLevelForLogger(Level.ERROR, CmsEntitySerialization.class.getName());
		TestLogPolicy.setLevelForLogger(Level.ERROR, JackrabbitIdentityStoreDao.class.getName());
		
		//If debug is enabled for this test then pretty print is enabled as well.
		prettyPrint = LoggerFactory.getLogger("org.betaconceptframework.astroboa.engine.AbstractRepositoryTest").isDebugEnabled();
		
		if (df == null){
			try {
				df = DatatypeFactory.newInstance();
			} catch (DatatypeConfigurationException e) {
				throw new CmsException(e);
			}
		}
	}

	protected void postSetup() throws Exception {
		cmsRepositoryEntityFactory = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory();
		testCmsDao = new TestCmsDao();
		contentSourceExtractor = new ContentSourceExtractor();
		repositoryContentValidator = new RepositoryContentValidator();
		
		serializationDao = AstroboaTestContext.INSTANCE.getBean(SerializationDao.class, "serializationDao");
		importDao = AstroboaTestContext.INSTANCE.getBean(ImportDao.class, "importDao");
		
		repositoryEntityResolver = AstroboaTestContext.INSTANCE.getBean(RepositoryEntityResolver.class, "repositoryEntityResolver");

		copyFilesToUnmanagedDataStore();
		
		createUser(TestConstants.TEST_USER_NAME);
		
		TestLogPolicy.setDefaultLevelForLogger(ContentDefinitionConfiguration.class.getName());
		TestLogPolicy.setDefaultLevelForLogger(CmsEntitySerialization.class.getName());
		TestLogPolicy.setDefaultLevelForLogger(JackrabbitIdentityStoreDao.class.getName());
		
		jaxbValidationUtils = new JAXBValidationUtils(repositoryEntityResolver,astroboaVersion);
		
	}
	
	protected String createUser(String username) throws IOException {
		
		loginToRepositoryRepresentingIdentityStoreAsSystem();
		
		String personUUID = null;
		if (userHasNotBeenCreated(username)){
			String testUserXml = FileUtils.readFileToString(new ClassPathResource("person_test.xml").getFile());

			testUserXml = StringUtils.replace(testUserXml, "SYSTEM_USER_ID", repositoryUserService.getSystemRepositoryUser().getId());

			testUserXml = StringUtils.replace(testUserXml, "USERNAME", username);
			
			ImportConfiguration configuration = ImportConfiguration.object()
					.persist(PersistMode.PERSIST_ENTITY_TREE)
					.version(false)
					.updateLastModificationTime(true)
					.build();

			ContentObject testUser = importDao.importContentObject(testUserXml, configuration);
			logger.debug("Created "+testUser.getContentObjectType() + " with id "+ 
					testUser.getId() + " and system name "+testUser.getSystemName() +" representing user "+username + " in " +
							" repository "+AstroboaClientContextHolder.getActiveRepositoryId());

			personUUID = testUser.getId();
		}
		
		loginToTestRepositoryAsSystem();

		if (repositoryUserService.getRepositoryUser(username) == null){
			RepositoryUser repUser = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newRepositoryUser();
			repUser.setExternalId(username);
			repUser.setLabel(username);
			
			repositoryUserService.save(repUser);
		}
		
		return personUUID;
	}


	protected void deleteUser(String username){

		String systemName = "IDENTITY_STORE_"+username;
		ContentObject person = contentService.getContentObject(systemName, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY, 
				CacheRegion.NONE, null, false);
		
		Assert.assertNotNull(person, "Person Object with system name "+systemName + " was not found");
		
		contentService.deleteContentObject(person.getId());

	}
	
	private boolean userHasNotBeenCreated(String username) {
		
		return ! identityStore.userExists(username);
		
	}

	private void copyFilesToUnmanagedDataStore() throws IOException {

		logo = new ClassPathResource("logo.png").getFile();
		logo2 = new ClassPathResource("logo2.png").getFile();

		File unmanagedDataStore = new ClassPathResource("/repository/UnmanagedDataStore").getFile();

		FileUtils.copyFileToDirectory(logo, unmanagedDataStore);
		FileUtils.copyFileToDirectory(logo2, unmanagedDataStore);
	}
	
	
	protected BinaryChannel loadManagedBinaryChannel(File resource, String binaryChannelName) throws IOException {
		BinaryChannel image = cmsRepositoryEntityFactory.newBinaryChannel();
		image.setName(binaryChannelName);
		//image.setSize(resource.length());
		image.setContent(FileUtils.readFileToByteArray(resource));

		image.setSourceFilename(resource.getName());

		image.setMimeType(new MimetypesFileTypeMap().getContentType(resource));

		Calendar lastModifiedDate = Calendar.getInstance();
		lastModifiedDate.setTimeInMillis(resource.lastModified());

		image.setModified(lastModifiedDate);

		return image;
	}
	
	protected BinaryChannel loadUnManagedBinaryChannel(String resourceRelativePath, String binaryChannelName) throws IOException {
		BinaryChannel image = cmsRepositoryEntityFactory.newUnmanagedBinaryChannel(resourceRelativePath);

		image.setName(binaryChannelName);
		
		return image;
	}
	
	protected Session getSession(){
		if (session == null){
			session = testCmsDao.getSession();
		}
		
		return session;
	}
	
	@AfterTest
	public void disableSecurity(){

		loginToTestRepositoryAsSystem();
		
		if (session != null){
			session.logout();
			session = null;
		}
	}




	/* (non-Javadoc)
	 * @see org.betaconceptframework.astroboa.test.AbstractAstroboaTest#login()
	 */
	
	protected void loginToRepositoryRepresentingIdentityStoreAsSystem() {
		
		loginToRepository(TestConstants.TEST_IDENTITY_STORE_REPOSITORY_ID,
				IdentityPrincipal.SYSTEM, "betaconcept", false);
		
	}

	protected ContentObject createContentObject(RepositoryUser systemUser, String systemName) {
		return createContentObjectForType(TEST_CONTENT_TYPE, systemUser, systemName);
	}
	
	protected ContentObject createContentObjectForType(String contentType, RepositoryUser systemUser, String systemName) {

		ContentObject contentObject = cmsRepositoryEntityFactory.newObjectForType(contentType);
		contentObject.setOwner(systemUser);
		contentObject.setSystemName(TestUtils.createValidSystemName(systemName));
		
		contentObject.getCmsProperty("accessibility.canBeReadBy");
		contentObject.getCmsProperty("accessibility.canBeUpdatedBy");
		contentObject.getCmsProperty("accessibility.canBeDeletedBy");
		((StringProperty)contentObject.getCmsProperty("accessibility.canBeTaggedBy")).addSimpleTypeValue("NONE");

		String title = systemName;
		
		((StringProperty)contentObject.getCmsProperty("profile.title")).setSimpleTypeValue(title);
		((StringProperty)contentObject.getCmsProperty("profile.language")).addSimpleTypeValue("en");
		((CalendarProperty)contentObject.getCmsProperty("profile.modified")).setSimpleTypeValue(Calendar.getInstance());

		return contentObject;
	}	
	
	protected void removeRoleFromActiveSubject(String role)
	{
		
		if (AstroboaClientContextHolder.getActiveSecurityContext().hasRole(role)){
			boolean roleRemoved  = AstroboaClientContextHolder.getActiveSecurityContext().removeRole(role);
		
			if (! roleRemoved){
				logger.warn("Role {} was not removed from subject {}",
					role, AstroboaClientContextHolder.getActiveSecurityContext().getSubject());
			}
		}
	}
	
	protected void addRoleToActiveSubject(String role){
		
		boolean roleAdded  = AstroboaClientContextHolder.getActiveSecurityContext().addRole(role);

		if (! roleAdded){
			logger.warn("Role {} was not added to subject {}",role, AstroboaClientContextHolder.getActiveSecurityContext().getSubject());
		}
	}

	protected void assertCmsUnauthorizedAccessExceptionIsThrownWithCorrectMessage(CmsException e, String expectedMessage, boolean startsWith, String methodName){

		Throwable t = e;
		
		while (t != null && ! (t instanceof CmsUnauthorizedAccessException)){
			t =  t.getCause();
		}

		if (t == null || ! (t instanceof CmsUnauthorizedAccessException)){
			throw new CmsException("Invalid exception thrown in method "+methodName+" Expected CmsUnauthorizedAccessException", e);
		}
		
		//CmsUnauthorizedAccessException is thrown. Check message is correct
		
		if (startsWith){
			Assert.assertTrue(t.getMessage() != null && 
					t.getMessage().startsWith(expectedMessage), 
					"Invalid exception thrown in method "+methodName+" "+ t.getMessage());
		}
		else{
			Assert.assertEquals(expectedMessage, t.getMessage(), 
				"Invalid exception thrown in method "+methodName+" "+ e.getMessage());
		}
		
	}

	//Simply generate JSON to check if an exception is thrown
	protected void validateJson(CmsRepositoryEntity cmsRepositoryEntity){
		//Just generate JSON to see if process throws an exception
		logger.debug("Created JSON :\n"+ cmsRepositoryEntity.json(prettyPrint));

	}
	
	protected void validateJsonCmsOutcome(ContentObjectCriteria contentObjectCriteria){
		//Just generate JSON to see if process throws an exception
		logger.debug("Created JSON :\n"+ contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.JSON));
		
	}
	
	protected List<String> getTestContentTypes(){
		return Arrays.asList(TEST_CONTENT_TYPE, EXTENDED_TEST_CONTENT_TYPE);
	}

	protected Future<SerializationReport> serializeAllEntitiesOfType(final CmsEntityType entityTypeToBeSerialized, final SerializationConfiguration serializationConfiguration) {
		
		return serializationDao.serializeAllInstancesOfEntity(entityTypeToBeSerialized, serializationConfiguration);
	}
	
	@Override
	protected void preCleanup() {
			
	}

	@Override
	protected void postCleanup() {
		
		
	}
	
	protected void serializeUsingJCR(CmsEntityType cmsEntity) {
		
		long start = System.currentTimeMillis();
		
		String repositoryHomeDir = AstroboaClientContextHolder.getActiveClientContext().getRepositoryContext().getCmsRepository().getRepositoryHomeDirectory();
	
		File serializationHomeDir = new File(
				repositoryHomeDir+File.separator+
				CmsConstants.SERIALIZATION_DIR_NAME);
	
		File zipFile = new File(serializationHomeDir, "document"+DateUtils.format(Calendar.getInstance(), "ddMMyyyyHHmmss.sss")+".zip");
		
	
		OutputStream out = null;
		ZipArchiveOutputStream os = null; 
	
		try{
			
			if (!zipFile.exists())
			{
				FileUtils.touch(zipFile);
			}
			
			out = new FileOutputStream(zipFile); 
			os = (ZipArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream("zip", out);

			os.setFallbackToUTF8(true);
			
			//Serialize all repository using JCR
			os.putArchiveEntry(new ZipArchiveEntry("document-view.xml"));
				
			final Session session = getSession();
				
			switch (cmsEntity) {
			case OBJECT:
				session.exportDocumentView(JcrNodeUtils.getContentObjectRootNode(session).getPath(), os, false, false);
				break;
			case REPOSITORY_USER:
				session.exportDocumentView(JcrNodeUtils.getRepositoryUserRootNode(session).getPath(), os, false, false);
				break;
			case TAXONOMY:
				session.exportDocumentView(JcrNodeUtils.getTaxonomyRootNode(session).getPath(), os, false, false);
				break;
			case ORGANIZATION_SPACE:
				session.exportDocumentView(JcrNodeUtils.getOrganizationSpaceNode(session).getPath(), os, false, false);
				break;
			case REPOSITORY:
				session.exportDocumentView(JcrNodeUtils.getCMSSystemNode(session).getPath(), os, false, false);
				break;

			default:
				break;
			}
				
			os.closeArchiveEntry();
			
			
			os.finish(); 
			os.close(); 

		}
		catch(Exception e)
		{
			throw new CmsException(e);
		}
		finally
		{
			if (out != null)
			{
				IOUtils.closeQuietly(out);
			}
			
			if (os != null)
			{
				IOUtils.closeQuietly(os);
			}
			long serialzationDuration = System.currentTimeMillis() - start;
			
			logger.debug("Export entities using JCR finished in {} ", 
					DurationFormatUtils.formatDurationHMS(serialzationDuration) );
		}
	}	

	protected void assertIOOfEntity(CmsEntityType cmsEntityTypeToBeSerialized, ImportConfiguration configuration, SerializationConfiguration serializationConfiguration) throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		SerializationReport serializationReport = exportEntity(cmsEntityTypeToBeSerialized, serializationConfiguration);
		
		URI serializationURI = retrieveURΙOfExportOutcome(serializationReport);
		
		validateExportXml(serializationURI);
		
		ImportReport importReport = importExportedEntityToCloneRepository(serializationURI, configuration);

		validateImport(importReport, serializationURI, cmsEntityTypeToBeSerialized);
		
		loginToTestRepositoryAsSystem();
		
	}

	protected void assertIOOfObjectsUsingCriteria(ContentObjectCriteria contentObjectCriteria, ImportConfiguration configuration, SerializationConfiguration serializationConfiguration) throws Throwable{
		
		loginToTestRepositoryAsSystem();
		
		SerializationReport serializationReport = exportObjects(contentObjectCriteria, serializationConfiguration);
		
		URI serializationURI = retrieveURΙOfExportOutcome(serializationReport);
		
		validateExportXml(serializationURI);
		
		ImportReport importReport = importExportedEntityToCloneRepository(serializationURI, configuration);

		validateImport(importReport, serializationURI, CmsEntityType.OBJECT);
		validateImport(importReport, serializationURI, CmsEntityType.TAXONOMY);
		validateImport(importReport, serializationURI, CmsEntityType.TOPIC);
		
		loginToTestRepositoryAsSystem();
		
	}

	
	protected void validateImport(ImportReport importReport, URI exportURI, CmsEntityType cmsEntityToExport) throws Throwable {
		
		try{
			Assert.assertTrue(importReport.getErrors().isEmpty(), "Import failed \n "+importReport.getErrors());
			
			assertCloneRepositoryContainsExactlyTheSameInstancesForEntity(cmsEntityToExport);
			
		}
		catch(Throwable e){
			logExportXml(exportURI);
			throw e;
		}
	}

	private void assertCloneRepositoryContainsExactlyTheSameInstancesForEntity(CmsEntityType cmsEntityToExport) throws Throwable {
		
		switch (cmsEntityToExport) {
		case REPOSITORY_USER:
			assertCloneRepositoryContainsExactlyTheSameRepositoryUsers();
			break;
		case ORGANIZATION_SPACE:
			assertCloneRepositoryContainsExactlyTheSameOrganizationSpace();
			break;
		case  TAXONOMY:
			assertCloneRepositoryContainsExactlyTheSameTaxonomies();
			break;
		case  SPACE:
			assertCloneRepositoryContainsExactlyTheSameSpaces();
			break;
		case  OBJECT:
			assertCloneRepositoryContainsExactlyTheSameContentObjects();
			break;
		case REPOSITORY:
			assertCloneRepositoryContainsExactlyTheSameRepositoryUsers();
			assertCloneRepositoryContainsExactlyTheSameOrganizationSpace();
			assertCloneRepositoryContainsExactlyTheSameTaxonomies();
			assertCloneRepositoryContainsExactlyTheSameContentObjects();
			assertCloneRepositoryContainsExactlyTheSameSpaces();
			break;

		default:
			break;
		}
		
	}
	
	private void assertCloneRepositoryContainsExactlyTheSameContentObjects() throws Throwable {
		ContentObjectCriteria newContentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		newContentObjectCriteria.doNotCacheResults();
		newContentObjectCriteria.getRenderProperties().renderAllContentObjectProperties(true);
		newContentObjectCriteria.addOrderProperty("profile.title", Order.descending);
		
		loginToTestRepositoryAsSystem();
		CmsOutcome<ContentObject> sourceOutcome = contentService.searchContentObjects(newContentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		loginToCloneRepositoryAsSystem();
		CmsOutcome<ContentObject> targetOutcome = contentService.searchContentObjects(newContentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
		
		repositoryContentValidator.compareContentObjectOutcome(sourceOutcome, targetOutcome);

		
	}

	private void assertCloneRepositoryContainsExactlyTheSameSpaces() {
		SpaceCriteria newSpaceCriteria = CmsCriteriaFactory.newSpaceCriteria();
		newSpaceCriteria.doNotCacheResults();
		newSpaceCriteria.getRenderProperties().renderAllContentObjectProperties(true);
		
		loginToTestRepositoryAsSystem();
		newSpaceCriteria.addAncestorSpaceIdEqualsCriterion(spaceService.getOrganizationSpace().getId());
		CmsOutcome<Space> sourceSpaces = spaceService.searchSpaces(newSpaceCriteria, ResourceRepresentationType.SPACE_LIST);
		
		loginToCloneRepositoryAsSystem();
		newSpaceCriteria.reset();
		newSpaceCriteria.doNotCacheResults();
		newSpaceCriteria.getRenderProperties().renderAllContentObjectProperties(true);
		newSpaceCriteria.addAncestorSpaceIdEqualsCriterion(spaceService.getOrganizationSpace().getId());
		CmsOutcome<Space> targetSpaces = spaceService.searchSpaces(newSpaceCriteria, ResourceRepresentationType.SPACE_LIST);
		
		repositoryContentValidator.compareSpaceList(sourceSpaces.getResults(), targetSpaces.getResults(), true,false, false);

		
	}

	private void assertCloneRepositoryContainsExactlyTheSameRepositoryUsers() {
		
		RepositoryUserCriteria newRepositoryUserCriteria = CmsCriteriaFactory.newRepositoryUserCriteria();
		newRepositoryUserCriteria.doNotCacheResults();
		newRepositoryUserCriteria.getRenderProperties().renderAllContentObjectProperties(true);
		
		loginToTestRepositoryAsSystem();
		List<RepositoryUser> sourceRepositoryUsers = repositoryUserService.searchRepositoryUsers(newRepositoryUserCriteria);
		
		loginToCloneRepositoryAsSystem();
		List<RepositoryUser> targetRepositoryUsers = repositoryUserService.searchRepositoryUsers(newRepositoryUserCriteria);
		
		repositoryContentValidator.compareRepositoryUserList(sourceRepositoryUsers, targetRepositoryUsers);
		
		
	}
	
	private void assertCloneRepositoryContainsExactlyTheSameTaxonomies() {
		
		loginToTestRepositoryAsSystem();
		CmsOutcome<Taxonomy> sourceTaxonomies = taxonomyService.getAllTaxonomies(ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.FULL, false);
		
		loginToCloneRepositoryAsSystem();
		CmsOutcome<Taxonomy> targetTaxonomies = taxonomyService.getAllTaxonomies(ResourceRepresentationType.TAXONOMY_LIST, FetchLevel.FULL, false);
		
		repositoryContentValidator.compareTaxonomyLists(sourceTaxonomies.getResults(), targetTaxonomies.getResults());
		
		
	}
	
	private void assertCloneRepositoryContainsExactlyTheSameOrganizationSpace() {
		
		loginToTestRepositoryAsSystem();
		Space sourceOrganizationSpace = spaceService.getOrganizationSpace();
		
		loginToCloneRepositoryAsSystem();
		Space targetOrganizationSpace = spaceService.getOrganizationSpace();
		
		repositoryContentValidator.compareSpaces(sourceOrganizationSpace, targetOrganizationSpace, true, true, true,false, true);
		
		
	}

	protected void validateExportXml(URI exportURI) throws Exception {
		
		InputStream sourceInputStream = null;
		
		try{
			
			logExportXml(exportURI);
			
			sourceInputStream = contentSourceExtractor.extractStream(exportURI);

			jaxbValidationUtils.validateUsingSAX(sourceInputStream);

		}
		catch(Exception e){
			logExportXml(exportURI);
			throw e;
		}
		finally{
			IOUtils.closeQuietly(sourceInputStream);
		}
	}

	private void logExportXml(URI exportURI) throws IOException, Exception{
		
		final String xml = IOUtils.toString(contentSourceExtractor.extractStream(exportURI));
		
		try{
			logger.debug(TestUtils.prettyPrintXml(xml));
		}
		catch(Exception e)
		{
			logger.error(xml, "While pretty printing");
			throw e;
		}
	}

	protected ImportReport importExportedEntityToCloneRepository(URI exportURI, ImportConfiguration configuration) throws InterruptedException, ExecutionException {
		
		loginToCloneRepositoryAsSystem();
		
		long timeStart = System.currentTimeMillis();
		
		Future<ImportReport> importReportFuture = importDao.importRepositoryContentFromURI(exportURI, configuration);
		
		while (! importReportFuture.isDone()){
			final long timePassed = System.currentTimeMillis() - timeStart;
			Assert.assertTrue(timePassed < (5 * 60 * 1000), "Import does not seem to have finished.");
			
		}
		
		return importReportFuture.get();
	}

	protected URI retrieveURΙOfExportOutcome(SerializationReport exportReport)
			throws MalformedURLException {
		File exportXml = new File(exportReport.getAbsolutePath());

		return exportXml.toURI();
	}

	protected SerializationReport exportEntity(CmsEntityType cmsEntityToExport, SerializationConfiguration serializationConfiguration) throws InterruptedException, ExecutionException {
		long timeStart = System.currentTimeMillis();

		Future<SerializationReport> exportReportFuture  = serializationDao.serializeAllInstancesOfEntity(cmsEntityToExport, serializationConfiguration);
		
		//Wait until export is finished
		while (! exportReportFuture.isDone()){
			final long timePassed = System.currentTimeMillis() - timeStart;

			Assert.assertTrue(timePassed < (5 * 60 * 1000), "Export does not seem to have finished. Time passed "+timePassed +" ms");
		}
		
		
		return exportReportFuture.get();
	}	

	protected SerializationReport exportObjects(ContentObjectCriteria contentObjectCriteria, SerializationConfiguration serializationConfiguration) throws InterruptedException, ExecutionException {
		long timeStart = System.currentTimeMillis();

		Future<SerializationReport> exportReportFuture  = serializationDao.serializeObjectsUsingCriteria(contentObjectCriteria, serializationConfiguration);
		
		//Wait until export is finished
		while (! exportReportFuture.isDone()){
			final long timePassed = System.currentTimeMillis() - timeStart;

			Assert.assertTrue(timePassed < (5 * 60 * 1000), "Export does not seem to have finished. Time passed "+timePassed +" ms");
		}
		
		
		return exportReportFuture.get();
	}	

	protected ContentObject	createContentObjectAndPopulateAllProperties(RepositoryUser systemUser, String systemName){
		return createContentObjectAndPopulateAllPropertiesForType(TEST_CONTENT_TYPE, systemUser, systemName);	
	}
	
	protected ContentObject createContentObjectAndPopulateAllPropertiesForType(String contentType, RepositoryUser systemUser, String systemName){
		ContentObject contentObject = createContentObjectForType(contentType, systemUser, systemName);
		
		try{
			//Fill content object with all possible types of properties
			for (CmsPropertyPath cmsPropertyPath : CmsPropertyPath.values()){
				addValueForProperty(contentObject, cmsPropertyPath.getPeriodDelimitedPath());
			}
		}
		catch(Exception e){
			throw new CmsException(e);
		}
		
		return contentObject;
		
	}
	
	protected void addValueForProperty(ContentObject contentObject, String propertyPath) throws Exception{
		CmsProperty<?, ?> cmsProperty = contentObject.getCmsProperty(propertyPath);
		
		boolean multiple = cmsProperty.getPropertyDefinition().isMultiple();
		
		switch (cmsProperty.getValueType()) {
		case Binary:
			provideValueForSimplePropertyWithValueRange((BinaryProperty)cmsProperty, multiple, 
					Arrays.asList(loadManagedBinaryChannel(logo, cmsProperty.getName()), loadManagedBinaryChannel(logo2, cmsProperty.getName())));
			break;
		case ObjectReference:
			
			ContentObjectCriteria contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			contentObjectCriteria.doNotCacheResults();
			contentObjectCriteria.setOffsetAndLimit(0, 2);
			
			if (contentObject.getId()!=null){
				contentObjectCriteria.addIdNotEqualsCriterion(contentObject.getId());
			}
			
			CmsOutcome<ContentObject> outcome = contentService.searchContentObjects(contentObjectCriteria, ResourceRepresentationType.CONTENT_OBJECT_LIST);
			
			if (outcome.getCount() > 0){
				List<ContentObject> results = new ArrayList<ContentObject>();
				for (ContentObject co: outcome.getResults()){
					results.add(co);
				}
				
				provideValueForSimplePropertyWithValueRange((ObjectReferenceProperty)cmsProperty, multiple, results);
			}
			
			break;
		case Date:
			provideValueForSimplePropertyWithValueRange((CalendarProperty)cmsProperty, multiple, 
					Arrays.asList(Calendar.getInstance(), Calendar.getInstance()));

			break;
		case Double:
			provideValueForSimplePropertyWithValueRange((DoubleProperty)cmsProperty, multiple, Arrays.asList((double)1.6, (double)1.7));
			break;
		case Long:
			provideValueForSimplePropertyWithValueRange((LongProperty)cmsProperty, multiple, Arrays.asList((long)1.23, (long)1.34));
			break;
		case Boolean:
			provideValueForSimplePropertyWithValueRange((BooleanProperty)cmsProperty, multiple, Arrays.asList(false, true));
			break;
		case String:
			provideValueForSimplePropertyWithValueRange((StringProperty)cmsProperty, multiple, Arrays.asList("Test<b>Value</b>","TestValue"));
			break;
		case TopicReference:
			
			TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
			topicCriteria.doNotCacheResults();
			topicCriteria.setOffsetAndLimit(0, 2);
			
			CmsOutcome<Topic> topics = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);
			
			if (topics.getCount() > 0){
				provideValueForSimplePropertyWithValueRange((TopicReferenceProperty)cmsProperty, multiple, topics.getResults());
			}
			
			break;

		default:
			break;
		}
		
	}

	private <T> void provideValueForSimplePropertyWithValueRange(SimpleCmsProperty<T, ?, ?> cmsProperty, boolean multiple, 
			List<T> alternativeValues){
		Map<T, Localization> valueRange = ((SimpleCmsProperty<T, ?, ?>)cmsProperty).getPropertyDefinition().getValueEnumeration();
		if (valueRange != null && ! valueRange.isEmpty()){
			final Iterator<T> valueRangeIterator = valueRange.keySet().iterator();
			
			((SimpleCmsProperty<T, ?, ?>)cmsProperty).addSimpleTypeValue(valueRangeIterator.next());
			
			if (multiple){
				if (valueRangeIterator.hasNext()){
					((SimpleCmsProperty<T, ?, ?>)cmsProperty).addSimpleTypeValue(valueRangeIterator.next());
				}
				else{
					((SimpleCmsProperty<T, ?, ?>)cmsProperty).addSimpleTypeValue(alternativeValues.get(0));
				}
			}
		}
		else{
			((SimpleCmsProperty<T, ?, ?>)cmsProperty).addSimpleTypeValue(alternativeValues.get(0));
			
			if (multiple){
				((SimpleCmsProperty<T, ?, ?>)cmsProperty).addSimpleTypeValue(alternativeValues.get(0));
			}
		}
	}

	protected Topic createRootTopicForSubjectTaxonomy(String topicName) {
		
		Topic topic = JAXBTestUtils.createTopic(topicName, 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		topic.setTaxonomy(getSubjectTaxonomy());
		
		topic = topicService.save(topic);
		markTopicForRemoval(topic);

		return topic;
	}
	
	protected Topic createTopic(String topicName, Topic parentTopic) {
		
		Topic topic = JAXBTestUtils.createTopic(topicName, 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newTopic(),
				getSystemUser());
		topic.setTaxonomy(getSubjectTaxonomy());
		
		parentTopic.addChild(topic);
		
		topic = topicService.save(topic);
		markTopicForRemoval(topic);

		return topic;
	}
	
	protected Space createRootSpaceForOrganizationSpace(String spaceName) {
		
		Space space = JAXBTestUtils.createSpace(spaceName, 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		space.setParent(spaceService.getOrganizationSpace());
		
		space = spaceService.save(space);
		markSpaceForRemoval(space);

		return space;
	}
	
	protected Space createSpace(String spaceName, Space parentSpace) {
		
		Space space = JAXBTestUtils.createSpace(spaceName, 
				CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newSpace(),
				getSystemUser());
		
		parentSpace.addChild(space);
		
		space = spaceService.save(space);

		return space;
	}

	protected String generateExpectedValueForOutputFormat(String attributeName, String attributeValue, ResourceRepresentationType<?>  resourceRepresentationType){
		
		if (resourceRepresentationType.equals(ResourceRepresentationType.XML)){
			return attributeName+"=\""+attributeValue+"\"";
		}
		else if (resourceRepresentationType.equals(ResourceRepresentationType.JSON)){
			return "\""+attributeName+"\":\""+attributeValue+"\"";
		}
		else{
			return attributeName+"\""+attributeValue+"\"";
		}
	}
	
	protected Space getOrganizationSpace(){
		return spaceService.getOrganizationSpace();
	}
	
	protected void logTimeElapsed(String message, long startTime, String... args){
		logger.debug(message, new Object[]{ 
				DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - startTime), args
		});
	}
	
	protected String removeWhitespacesIfNecessary(CmsCriteria cmsCriteria, String xmlOrJson){
		
		if (xmlOrJson != null && cmsCriteria.getRenderProperties().isPrettyPrintEnabled()){
			return StringUtils.deleteWhitespace(xmlOrJson);
		}
		
		return xmlOrJson;
	}
	
	protected String removeWhitespacesIfNecessary(String xmlOrJson){
		
		if (xmlOrJson != null && prettyPrint){
			return StringUtils.deleteWhitespace(xmlOrJson);
		}
		
		return xmlOrJson;
	}

	protected String convertCalendarToXMLFormat(Calendar calendar, boolean dateTimePattern){
		if (dateTimePattern){
			GregorianCalendar gregCalendar = new GregorianCalendar(calendar.getTimeZone());
			gregCalendar.setTimeInMillis(calendar.getTimeInMillis());

			return df.newXMLGregorianCalendar(gregCalendar).toXMLFormat();
		}
		else{
			return df.newXMLGregorianCalendarDate(
					calendar.get( Calendar.YEAR ),
					calendar.get( Calendar.MONTH )+1,  	// Calendar.MONTH is zero based, XSD Date datatype's month field starts
					//	with JANUARY as 1.
					calendar.get( Calendar.DAY_OF_MONTH ),
					DatatypeConstants.FIELD_UNDEFINED ).toXMLFormat();
		}
	}

}
