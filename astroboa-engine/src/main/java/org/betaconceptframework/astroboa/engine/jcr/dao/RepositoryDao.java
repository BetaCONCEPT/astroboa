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
package org.betaconceptframework.astroboa.engine.jcr.dao;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.security.AstroboaCredentials;
import org.betaconceptframework.astroboa.api.security.AstroboaPrincipalName;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.api.security.RepositoryUserIdPrincipal;
import org.betaconceptframework.astroboa.api.security.exception.CmsInvalidPasswordException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginAccountExpiredException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginAccountLockedException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidCredentialsException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidPermanentKeyException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginInvalidUsernameException;
import org.betaconceptframework.astroboa.api.security.exception.CmsLoginPasswordExpiredException;
import org.betaconceptframework.astroboa.api.security.exception.CmsUnauthorizedRepositoryUseException;
import org.betaconceptframework.astroboa.api.security.management.IdentityStoreContextHolder;
import org.betaconceptframework.astroboa.api.service.RepositoryUserService;
import org.betaconceptframework.astroboa.cache.region.DefinitionCacheRegion;
import org.betaconceptframework.astroboa.configuration.LocalizationType.Label;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.configuration.RepositoryType;
import org.betaconceptframework.astroboa.configuration.SecurityType.PermanentUserKeyList.PermanentUserKey;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.CmsRepositoryImpl;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.context.SecurityContext;
import org.betaconceptframework.astroboa.engine.definition.ContentDefinitionConfiguration;
import org.betaconceptframework.astroboa.engine.jcr.initialization.CmsRepositoryInitializationManager;
import org.betaconceptframework.astroboa.engine.jcr.util.JackrabbitDependentUtils;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.security.CmsGroup;
import org.betaconceptframework.astroboa.security.CmsPrincipal;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.astroboa.security.CredentialsCallbackHandler;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springmodules.jcr.JcrSessionFactory;
import org.springmodules.jcr.SessionFactory;
import org.springmodules.jcr.SessionFactoryUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryDao implements ApplicationListener{

	@Autowired
	private SimpleCredentials betaConceptCredentials;

	@Autowired
	private CmsRepositoryInitializationManager cmsRepositoryInitializationManager;

	@Autowired
	private ContentDefinitionConfiguration contentDefinitionConfiguration;

	@Autowired
	//Used mainly when initializing IdentityStore repository
	private LazyLoader lazyLoader;

	@Autowired
	//Bound to service so that a new Transaction is created
	//as this is used when RepositoryUserPrincipal must be added to subject
	private RepositoryUserService repositoryUserService;
	
	@Autowired
	private DefinitionCacheRegion definitionCacheRegion;
	
	
	@Autowired
	private ConsistencyCheckerDao consistencyCheckerDao;

	private  final Logger logger = LoggerFactory.getLogger(RepositoryDao.class);

	private Map<String, CmsRepository> repositories = new HashMap<String, CmsRepository>();

	private Map<String, SessionFactory> jcrSessionFactoriesPerRepository = new HashMap<String, SessionFactory>();


	private void loadRepositoryFromConfiguration(String repositoryId)  {

		if (!RepositoryRegistry.INSTANCE.isRepositoryRegistered(repositoryId)){
			logger.warn("Found no configuration for repository "+repositoryId);
			return;
		}

		try{
			RepositoryType repositoryConfiguration = RepositoryRegistry.INSTANCE.getRepositoryConfiguration(repositoryId);

			String currentJcrRepositoryJndiName = repositoryConfiguration.getJcrRepositoryJndiName();
			String serverAliasURL = repositoryConfiguration.getServerAliasURL();
			String restfulApiBasePath = repositoryConfiguration.getRestfulApiBasePath();

			String repositoryServerURL = StringUtils.isBlank(serverAliasURL) ? RepositoryRegistry.INSTANCE.getDefaultServerURL() : serverAliasURL;
			String repositoryApplicationPolicyName = StringUtils.isBlank(repositoryConfiguration.getJaasApplicationPolicyName())? 
					RepositoryRegistry.INSTANCE.getDefaultJaasApplicationPolicyName(): 
						repositoryConfiguration.getJaasApplicationPolicyName();
			
			String externalIdentityStoreJndiName = repositoryConfiguration.getExternalIdentityStoreJndiName();

			String repositoryIdentityStoreId = null;

			if (StringUtils.isBlank(externalIdentityStoreJndiName)){
						repositoryIdentityStoreId =  (repositoryConfiguration.getIdentityStoreRepositoryId() == null )? 
								RepositoryRegistry.INSTANCE.getDefaultIdentityStoreId(): 
									repositoryConfiguration.getIdentityStoreRepositoryId();
			
								if (StringUtils.isBlank(repositoryIdentityStoreId)){
									//Define the repository to be identity store for it self
									repositoryIdentityStoreId = repositoryId;
								}
			}

			//Localized Labels
			HashMap<String, String> localizedLabels = new HashMap<String, String>();
			if (repositoryConfiguration.getLocalization() != null){
				List<Label> localizedLabelList = repositoryConfiguration.getLocalization().getLabel();
				for (Label localizedLabel : localizedLabelList){
					localizedLabels.put(localizedLabel.getLang(), localizedLabel.getValue());
				}
			}

			//Create JcrSessionFactory
			SessionFactory jcrSessionFactory = createJcrSessionFactory(currentJcrRepositoryJndiName);

			//This is the only wat to retrieve repositoy home dir.
			//Open a session, create one if none exists and retrieve the Repository object 
			//related to that session.
			Session session = SessionFactoryUtils.getSession(jcrSessionFactory, true);
			Repository repository = session.getRepository();
			String repositoryHomeDirPath = JackrabbitDependentUtils.getRepositoryHomeDir(repository);
			session.logout();
	
			CmsRepository cmsRepository = new CmsRepositoryImpl(repositoryId, localizedLabels,
					repositoryHomeDirPath, 
					repositoryServerURL,
					restfulApiBasePath,
					repositoryApplicationPolicyName, 
					repositoryIdentityStoreId, 
					externalIdentityStoreJndiName, 
					repositoryConfiguration.getSecurity().getSecretUserKeyList().getAdministratorSecretKey().getUserid());

			//Add values to maps
			repositories.put(repositoryId, cmsRepository);
			jcrSessionFactoriesPerRepository.put(repositoryId, jcrSessionFactory);
			
			if (fullReloadRepository(repositoryId, repositoryConfiguration, repository)){
				
				//Initialize repository and load definition to cache
				SecurityContext securityContext = new SecurityContext(repositoryId, null, 30, null);
				RepositoryContext repositoryContext = new RepositoryContext(cmsRepository, securityContext);
				AstroboaClientContextHolder.registerClientContext(new AstroboaClientContext(repositoryContext, new LazyLoader(null, null, null, null)), true);
				
				try{
					cmsRepositoryInitializationManager.initialize(cmsRepository);
						
					contentDefinitionConfiguration.loadDefinitionToCache();
		
					if (repositoryConfiguration.isCheckConsistency() || RepositoryRegistry.INSTANCE.isConsistencyCheckEnabled()){
						consistencyCheckerDao.performReferentialIntegrityCheck();
					}
				}
				catch(CmsException e){
					repositories.remove(repositoryId);
					jcrSessionFactoriesPerRepository.remove(repositoryId);
					AstroboaClientContextHolder.clearContext();
					throw e;
				}
				
				AstroboaClientContextHolder.clearContext();
	
				//	Create unmanaged datastore if not there
				try{
					File repositoryHomeDir = new File(cmsRepository.getRepositoryHomeDirectory());
	
					if (repositoryHomeDir.exists()){
						File unmanagedDataStoreDirectory = new File(repositoryHomeDir, CmsConstants.UNMANAGED_DATASTORE_DIR_NAME);
	
						if (!unmanagedDataStoreDirectory.exists()){
							if (!unmanagedDataStoreDirectory.mkdir()){
								throw new Exception("Could not create UnmanagedDataStore directory. File.mkdir() returned false");
							}
	
							logger.debug("Created UnmanagedDataStore directory for repository {} in path {}",cmsRepository.getId(),	unmanagedDataStoreDirectory.getAbsolutePath());
						}
					}
				}
				catch(Error e){
					logger.warn("Could not create UnmanagedDataStore directory", e);
				}
			}
		}
		catch(Exception e){
			logger.error("",e);
			return;
		}
	}


	private SessionFactory createJcrSessionFactory(String jcrRepositoryJndiName) throws Exception {

		InitialContext context = new InitialContext();

		Repository jcrRepository = (Repository)context.lookup(jcrRepositoryJndiName);

		//Create JcrSessionFactory For default workspace
		JcrSessionFactory jcrSessionFactory = new JcrSessionFactory();
		jcrSessionFactory.setCredentials(betaConceptCredentials);
		jcrSessionFactory.setRepository((Repository)jcrRepository);
		//Call this method to force further initialization process
		jcrSessionFactory.afterPropertiesSet();

		return jcrSessionFactory;
	}


	public List<CmsRepository> getAvailableCmsRepositories() {

		deployRepositoriesFoundInTheRepositoryRegistry();

		return new ArrayList<CmsRepository>(repositories.values());
	}


	private void deployRepositoriesFoundInTheRepositoryRegistry() {

		if (RepositoryRegistry.INSTANCE.configurationHasChanged() || repositories.isEmpty()){

			RepositoryRegistry.INSTANCE.loadRepositoryConfigurations();
			/*
			 * Key is the the JNDI name for the JCR Repository, value is the repository id
			 */
			Map<String,String> jcrRepositoryJndiNamesPerRepository = new HashMap<String, String>();
			/*
			 * Key is the the JNDI name for the Database, value is the repository id
			 */
			Map<String,String> databaseJndiNamesPerRepository = new HashMap<String,String>();

			Set<String> repositoryIdsToBeUndeployed = new HashSet<String>();

			for (Map.Entry<String, RepositoryType> repositoryConfigurationEntry : RepositoryRegistry.INSTANCE.getConfigurationsPerRepositoryId().entrySet()){

				String repositoryId = repositoryConfigurationEntry.getKey();
				RepositoryType repositoryConfiguration = repositoryConfigurationEntry.getValue();

				String currentJcrRepositoryJndiName = repositoryConfiguration.getJcrRepositoryJndiName();
				String currentDBJndiName = repositoryConfiguration.getDbJndiName();

				boolean initializeRepository = true;

				//Check that repository corresponds to 
				if (jcrRepositoryJndiNamesPerRepository.containsKey(currentJcrRepositoryJndiName)){
					logger.warn("Jcr Repository JNDI name '{}' already defined for repository '{}'. Repository {} will not be loaded.", 
							new Object[]{currentJcrRepositoryJndiName, jcrRepositoryJndiNamesPerRepository.get(currentJcrRepositoryJndiName), repositoryId});
					repositoryIdsToBeUndeployed.add(repositoryId);
					initializeRepository = false;
				}

				if (databaseJndiNamesPerRepository.containsKey(currentDBJndiName)){
					logger.warn("Astroboa Database JNDI name '{}' already defined for repository '{}'. Repository {} will not be loaded.", 
							new Object[]{currentDBJndiName, databaseJndiNamesPerRepository.get(currentDBJndiName), repositoryId});
					repositoryIdsToBeUndeployed.add(repositoryId);
					initializeRepository = false;
				}

				//For the moment initialize only the repositories which have not yet been initialized
				if (initializeRepository){
					loadRepositoryFromConfiguration(repositoryId);
				}
			}

			//Mark any repository which does not exist in the configuration as to be undeployed
			//This step is meaningful only when the configuration file is updated
			for (String deployedRepositoryId : repositories.keySet()){
				if (!RepositoryRegistry.INSTANCE.isRepositoryRegistered(deployedRepositoryId)){
					logger.warn("Repository {} does not belong to the configuration and therefore will be undeployed", deployedRepositoryId);
					repositoryIdsToBeUndeployed.add(deployedRepositoryId);
				}
			}

			//Undeploy any repository whose configuration is invalid
			for (String repositoyIdToBeUndeployed : repositoryIdsToBeUndeployed){
				repositories.remove(repositoyIdToBeUndeployed);
				jcrSessionFactoriesPerRepository.remove(repositoyIdToBeUndeployed);
				
				//Clear caches
				definitionCacheRegion.clearCacheForRepository(repositoyIdToBeUndeployed);
				
				logger.warn("Successfully undeployed repository {}", repositoryIdsToBeUndeployed);
			}

			//Detect any obvious cycles between repository and identity store repositories
			detectCycleBetweenRepositoryAndIdentityStoreRepositories();

			//Second pass to initialize any repository which is an identity store for another repository
			for (CmsRepository cmsRepository : repositories.values()){
					initializeIdentityStoreForRepository(cmsRepository);
			}
		}
	}


	private boolean fullReloadRepository(String repositoryId,	RepositoryType repositoryConfiguration, Repository repository) {
		
		//Reload repository if repository has not been loaded
		if (! repositories.containsKey(repositoryId)){
			return true;
		}
		
		//Check if cache manager settings have changed
		if (JackrabbitDependentUtils.cacheManagerSettingsHaveChanged(repositoryConfiguration, repository)){
			return true;
		}
		
		return false;
	}


	private void initializeIdentityStoreForRepository(
			CmsRepository cmsRepository) {
		
		if (StringUtils.isBlank(cmsRepository.getExternalIdentityStoreJNDIName())){
			String identityStoreRepositoryId = cmsRepository.getIdentityStoreRepositoryId();

			if (StringUtils.isBlank(identityStoreRepositoryId)){
				throw new CmsException("No external IdentityStore JNDI has been provided nor an identity store repository id for repository "+ cmsRepository.getId());
			}

			if (!repositories.containsKey(identityStoreRepositoryId)){
				throw new CmsException("Found no repository with id "+identityStoreRepositoryId+".Cannot initialize identity store for repository "+ cmsRepository.getId());
			}

			CmsRepository cmsRepositoryIdentityStore = repositories.get(identityStoreRepositoryId);
			Subject subject = new Subject();
			subject.getPrincipals().add(new IdentityPrincipal(IdentityPrincipal.SYSTEM));

			Group rolesPrincipal = new CmsGroup(AstroboaPrincipalName.Roles.toString());

			for (CmsRole cmsRole : CmsRole.values()){
				rolesPrincipal.addMember(new CmsPrincipal(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForRepository(cmsRole, identityStoreRepositoryId)));
			}

			subject.getPrincipals().add(rolesPrincipal);

			SecurityContext securityContext = new SecurityContext(identityStoreRepositoryId, 
					subject, 30, null);


			RepositoryContext repositoryContext = new RepositoryContext(cmsRepositoryIdentityStore, securityContext);
			AstroboaClientContextHolder.registerClientContext(new AstroboaClientContext(repositoryContext, 
					lazyLoader), true);
			cmsRepositoryInitializationManager.initializeIdentityStore(cmsRepository.getId(), cmsRepositoryIdentityStore);
			AstroboaClientContextHolder.clearContext();
		}
	}


	/*
	 *  A repository may refer to an external Identity Store or to another repository.
	 *  
	 *  At the latter case, the repository that serves as an identity store MUST
	 *  have ITSELF as an identity store. 
	 *  
	 */
	private void detectCycleBetweenRepositoryAndIdentityStoreRepositories() {

		List<String> repositoryIdsToBeRemoved = new ArrayList<String>();

		for (CmsRepository cmsRepository : repositories.values()){

			if (StringUtils.isNotBlank(cmsRepository.getExternalIdentityStoreJNDIName())){
				//We are only interested in repositories which refer to 
				//another repository as identity store
				continue;
			}
			else{

				if (cmsRepository.getIdentityStoreRepositoryId()==null){
					throw new CmsException("No external IdentityStore JNDI has been provided nor an identity store repository id for repository "+ cmsRepository.getId());
				}
				else{

					if (!StringUtils.equals(cmsRepository.getId(), cmsRepository.getIdentityStoreRepositoryId())){

						//Repository refers to another repository for identity store.
						//Check that this repository refers to itself as identity store
						CmsRepository identityStoreRepository = repositories.get(cmsRepository.getIdentityStoreRepositoryId());

						if (StringUtils.isNotBlank(identityStoreRepository.getExternalIdentityStoreJNDIName())){
							logger.warn("Repository "+ cmsRepository.getId() + " refers to repository "+ 
									identityStoreRepository.getId() +" to be its identity store, but the latter refers to an external identity store " +
							" which is not accepted.Both repositoties will be removed");

							repositoryIdsToBeRemoved.add(cmsRepository.getId());
							repositoryIdsToBeRemoved.add(identityStoreRepository.getId());
						}
						else {
							if (!StringUtils.equals(identityStoreRepository.getId(), identityStoreRepository.getIdentityStoreRepositoryId())){
								logger.warn("Repository "+ cmsRepository.getId() + " refers to repository "+ 
										identityStoreRepository.getId() +" to be its identity store, but the latter refers to another repository for" +
										" identity store ("+identityStoreRepository.getIdentityStoreRepositoryId()+") and not its self" +
								" which is not accepted.Both repositoties will be removed");

								repositoryIdsToBeRemoved.add(cmsRepository.getId());
								repositoryIdsToBeRemoved.add(identityStoreRepository.getId());
							}


						}

					}
				}
			}
		}


		for (String repositoyToBeRemoved : repositoryIdsToBeRemoved){
				repositories.remove(repositoyToBeRemoved);
				jcrSessionFactoriesPerRepository.remove(repositoyToBeRemoved);
		}
		
	}

	public String login(String repositoryId,  AstroboaCredentials credentials) {
		return login(repositoryId, credentials, null, null);
	}

	private boolean isPermanentKeyValidForUser(String repositoryId, String permanentKey, String userid) {

		if (StringUtils.isBlank(repositoryId) || StringUtils.isBlank(permanentKey) || StringUtils.isBlank(userid)){
			return false;
		}

		if (!RepositoryRegistry.INSTANCE.isRepositoryRegistered(repositoryId)){
			return false;
		}

		RepositoryType repositoryConfiguration = RepositoryRegistry.INSTANCE.getRepositoryConfiguration(repositoryId);

		//If no trusted key have been registered to configuration then all keys are accepted
		if (repositoryConfiguration.getSecurity() == null || repositoryConfiguration.getSecurity().getPermanentUserKeyList() == null ||
				CollectionUtils.isEmpty(repositoryConfiguration.getSecurity().getPermanentUserKeyList().getPermanentUserKey())){
			return false;
		}


		List<PermanentUserKey> permanentUserKeys = repositoryConfiguration.getSecurity().getPermanentUserKeyList().getPermanentUserKey();

		for (PermanentUserKey permanentUserKey : permanentUserKeys){

			if (StringUtils.equals(permanentUserKey.getKey(), permanentKey)){ 

				List<String> userIds = Arrays.asList(StringUtils.split(permanentUserKey.getUserid(), ","));

				return userIds.contains("*") || userIds.contains(userid);

			}
		}

		return false;
	}


	private SecurityContext authenticate(AstroboaCredentials credentials, String jaasApplicationPolicyName, String repositoryId, int currentAuthenticationTokenTimeout, String permanentKey	) {

		SecurityContext securityContext = null;

		Subject subject = null;
		try {


			CredentialsCallbackHandler callbackHandler = null;
			if (credentials != null){
				callbackHandler = new CredentialsCallbackHandler(credentials);
			}

			IdentityStoreContextHolder.setActiveRepositoryId(repositoryId);

			LoginContext loginContext = new LoginContext(jaasApplicationPolicyName, callbackHandler);

			loginContext.login();

			subject = loginContext.getSubject();


		}
		catch(AccountNotFoundException e){
			throw new CmsLoginInvalidUsernameException(e);
		}
		catch(FailedLoginException e){
			throw new CmsInvalidPasswordException(e);
		}
		catch(AccountLockedException e){
			throw new CmsLoginAccountLockedException(e);
		}
		catch(AccountExpiredException e){
			throw new CmsLoginAccountExpiredException(e);
		}
		catch(CredentialNotFoundException e){
			throw new CmsInvalidPasswordException(e);
		}
		catch(CredentialExpiredException e){
			throw new CmsLoginPasswordExpiredException(e);
		}
		catch (LoginException e) {
			throw new CmsException(e);
		}
		catch(CmsException e){
			throw e;
		}
		catch(Throwable t){
			throw new CmsException(t);
		}
		finally{
			IdentityStoreContextHolder.clear();
		}

		authorizeSubject(subject, repositoryId);

		try{
			String authenticationToken = createAuthenticationToken(subject,	jaasApplicationPolicyName, repositoryId, permanentKey);

			securityContext = new SecurityContext(authenticationToken, subject, currentAuthenticationTokenTimeout, 
					getAvailableRepositoryIds());

			if (logger.isDebugEnabled()){
				logger.debug("Successfull authentication: Token {} , Subject {}  for Thread {}" 
						, new Object[]{authenticationToken, subject, Thread.currentThread()});
			}

			return securityContext;
		}
		catch (NoSuchAlgorithmException e) {
			throw new CmsException(e);
		}


	}

	private List<String> getAvailableRepositoryIds() {
		return new ArrayList<String>(RepositoryRegistry.INSTANCE.getConfigurationsPerRepositoryId().keySet());
	}


	private String createAuthenticationToken(Subject subject,
			String jaasApplicationPolicyName, String repositoryId, String permanentKey)
	throws NoSuchAlgorithmException {

		//create authentication token
		StringBuilder stringToDigest = new StringBuilder(repositoryId);

		//Token contains the value provided in the UserPrincipal 
		//provided in Subject. If none is found throw an exception
		if (subject == null){
			throw new CmsException("No subject was found after login");
		}

		String identity = retrieveIdentityFromSubject(subject);

		stringToDigest.append(identity);

		if (StringUtils.isBlank(permanentKey)){
			stringToDigest.append(DateUtils.format(Calendar.getInstance()))
			.append(jaasApplicationPolicyName)
			.append(UUID.randomUUID());
		}
		else{

			if (StringUtils.isNotBlank(permanentKey) && ! isPermanentKeyValidForUser(repositoryId,permanentKey, identity)){
				throw new CmsLoginInvalidPermanentKeyException("Invalid permanent key "+ permanentKey + " for user "+ identity + " in repository "+ repositoryId);
			}

			stringToDigest.append(permanentKey); 
		}

		//According to documentation in Base64 class
		//Encoding is done as defined in RFC 2045 - 6.8. Base64 Content-Transfer-Encoding from RFC 2045 Multipurpose Internet Mail Extensions (MIME) Part One: Format of Internet Message Bodies
		//Since this character set contains '+' and '/' chars , these characters must be replaced by '_' and '-' accordingly
		//so that this token can be used in URLs if necessary. Since decoding process is not an issue the above chars can  be used
		//although they are not defined in the 64-character set.
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
		return new String(Base64.encodeBase64(messageDigest.digest(stringToDigest.toString().getBytes()))).replaceAll("\\+", "_").replaceAll("/", "-");
	}


	private String retrieveIdentityFromSubject(Subject subject) {

		if (subject == null || CollectionUtils.isEmpty(subject.getPrincipals(IdentityPrincipal.class))){
			throw new CmsException("Could not find identity principal in subject "+subject+" Unable to create authentication token");
		}

		//Retrieve the first one. Normally it should not have more than one
		return subject.getPrincipals(IdentityPrincipal.class).iterator().next().getName();
	}

	private int retrieveAuthenticationTokenTimeoutForRepository(String repositoryId) {

		RepositoryType repositoryConfigurationEntry = RepositoryRegistry.INSTANCE.getRepositoryConfiguration(repositoryId);

		if (repositoryConfigurationEntry == null){
			throw new CmsException("Could not find configuration entry for repository "+ repositoryId);
		}

		return repositoryConfigurationEntry.getAuthenticationTokenTimeout() == null ? RepositoryRegistry.INSTANCE.getDefaultAuthenticationTokenTimeout() : 
			repositoryConfigurationEntry.getAuthenticationTokenTimeout();
	}


	public SessionFactory getJcrSessionFactoryForAssociatedRepository() {

		String associatedRepositoryId = getAssociatedRepositoryId();

		if (! jcrSessionFactoriesPerRepository.containsKey(associatedRepositoryId)){
			if (!repositories.containsKey(associatedRepositoryId)){
				loadRepositoryFromConfiguration(associatedRepositoryId);

				if (! jcrSessionFactoriesPerRepository.containsKey(associatedRepositoryId)){
					throw new CmsException("Found no jcr session factory for associated repository "+ associatedRepositoryId);
				}
			}
			else{
				throw new CmsException("Found no jcr session factory for associated repository "+ associatedRepositoryId);
			}
		}

		return jcrSessionFactoriesPerRepository.get(associatedRepositoryId);
	}


	private String getAssociatedRepositoryId() {
		String associatedRepositoryId = AstroboaClientContextHolder.getActiveRepositoryId();

		if (associatedRepositoryId == null){
			throw new CmsException("Found no repository context");
		}

		return associatedRepositoryId;
	}


	public boolean isRepositoryAvailable(String repositoryId) {

		deployRepositoriesFoundInTheRepositoryRegistry();

		return StringUtils.isNotBlank(repositoryId) && repositories.containsKey(repositoryId);
	}


	public CmsRepository getCurrentConnectedRepository() {
		return AstroboaClientContextHolder.getActiveClientContext() != null ?
				( AstroboaClientContextHolder.getActiveClientContext().getRepositoryContext() != null ?
						AstroboaClientContextHolder.getActiveClientContext().getRepositoryContext().getCmsRepository() : null) : null;
	}


	public CmsRepository getCmsRepository(String repositoryId) {
		if (StringUtils.isBlank(repositoryId)){
			return null;
		}

		if (!repositories.containsKey(repositoryId)){
			deployRepositoriesFoundInTheRepositoryRegistry();
		}

		return repositories.get(repositoryId);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {

		//Need to initialize all repositories defined in configuration xml
		//after Spring Context has been instantiated
		if (event instanceof ContextRefreshedEvent) {
			deployRepositoriesFoundInTheRepositoryRegistry();
		}
	}


	private void addRepositoryUserIdPrincipalToSubject(Subject subject,
			String identity) {

		//Must provide a principal which will hold the RepositoryUserId
		//No need to check if user is ANONYMOUS
		if (StringUtils.isNotBlank(identity) && ! StringUtils.equals(identity, IdentityPrincipal.ANONYMOUS)){

			RepositoryUser repositoryUser = repositoryUserService.getRepositoryUser(identity);

			if (repositoryUser != null && repositoryUser.getId() != null){
				subject.getPrincipals().add(new RepositoryUserIdPrincipal(repositoryUser.getId()));
			}

		}
	}

	/**	
	 *	Subject authorization at this level is restricted only to authorize user
	 *	whether she can or cannot login to the specified repository.
	 *	Our default policy is a PERMIT REPOSITORY policy, meaning that an authenticated user
	 *	has access to REPOSITORY available repositories defined within a Astroboa Server.
	 *
	 *	In cases where an authenticated user has access to a subset of available repositories
	 *	then a {@link Group} named after "AuthorizedRepositories" must exist 
	 *  among {@link Subject} principals. 
	 *  
	 *  If so, only and only if the specified repository exists inside this list,
	 *  the user will be authorized to use Astroboa services for that repository.
	 */
	private void authorizeSubject(Subject subject, String repositoryId) {

		if (subject == null){
			throw new CmsException("No subject provided ");
		}

		//In case authenticated
		Set<Principal> principals = subject.getPrincipals();

		if (CollectionUtils.isNotEmpty(principals)){

			for (Principal principal : principals){

				if (principal instanceof Group && 
						AstroboaPrincipalName.AuthorizedRepositories.toString().equals(principal.getName()) ){

					//Found authorized repositories
					boolean userIsAuthorizedToAccessRepository = false;

					for (Enumeration<? extends Principal> authorizedRepositories = ((Group)principal).members(); authorizedRepositories.hasMoreElements();){
						Principal authorizedRepository = authorizedRepositories.nextElement();

						if (StringUtils.equals(authorizedRepository.getName(), repositoryId)){
							userIsAuthorizedToAccessRepository = true;
							break;
						}
					}

					if (!userIsAuthorizedToAccessRepository){
						throw new CmsUnauthorizedRepositoryUseException(repositoryId);
					}
				}
			}
		}

	}


	public String login(String repositoryId, AstroboaCredentials credentials, String permanentKey, String key)
	{

		if (StringUtils.isBlank(repositoryId)){
			throw new CmsException("Null or empty repository id '"+ repositoryId+"'");
		}

		//In case configuration has changed, load any changes prior to login
		deployRepositoriesFoundInTheRepositoryRegistry();
		
		CmsRepository cmsRepositoryToBeConnected = repositories.get(repositoryId);

		if (cmsRepositoryToBeConnected == null){	
			throw new CmsException("Repository with id "+ repositoryId+ " was not deployed.");
		}

		String username = credentials != null ? credentials.getUsername()  : null;

		if (username == null)
		{
			throw new CmsLoginInvalidUsernameException("No username provided");
		}


		if (StringUtils.isNotBlank(key) && ! RepositoryRegistry.INSTANCE.isSecretKeyValidForUser(repositoryId, username, key))
		{
			logger.warn("User {} tried to login with invalid secret key '{}' to repository {}", new Object[]{username, key, repositoryId});
			throw new CmsLoginInvalidCredentialsException();
		}

		char[] password = credentials != null ? credentials.getPassword() : null;

		AstroboaCredentials credentialsToSendToJAAS = new AstroboaCredentials(username, 
				password, 
				cmsRepositoryToBeConnected.getIdentityStoreRepositoryId(),
				cmsRepositoryToBeConnected.getExternalIdentityStoreJNDIName(),
				repositoryId, 
				key);

		//Authenticate user using JAAS application security domain
		//and create security context
		int currentAuthenticationTokenTimeout = retrieveAuthenticationTokenTimeoutForRepository(repositoryId);

		SecurityContext securityContext = authenticate(credentialsToSendToJAAS,	 
				cmsRepositoryToBeConnected.getApplicationPolicyName(),repositoryId, currentAuthenticationTokenTimeout, permanentKey);

		//Create a new AstroboaClientContext
		RepositoryContext repositoryContext = new RepositoryContext(cmsRepositoryToBeConnected, securityContext);

		AstroboaClientContext clientContext = new AstroboaClientContext(repositoryContext, lazyLoader);

		AstroboaClientContextHolder.registerClientContext(clientContext, true);

		//Need context to be registered first
		addRepositoryUserIdPrincipalToSubject(securityContext.getSubject(), securityContext.getIdentity());

		if (logger.isDebugEnabled()){
			logger.debug("Sucessfully logged in to repository {} and register ClientContext {} to Thread {}", new Object[]{repositoryId, clientContext, Thread.currentThread().getName()});
		}

		return securityContext.getAuthenticationToken();

	}

	public String loginAsAnonymous(String repositoryId, String permanentKey) {
		return login(repositoryId, IdentityPrincipal.ANONYMOUS, null, permanentKey);
	}


	public String login(String repositoryId, String username, String key,
			String permanentKey) {

		AstroboaCredentials credentials = new AstroboaCredentials(username);

		return login(repositoryId, credentials, permanentKey, key);
	}


	public String loginAsAdministrator(String repositoryId, String key,
			String permanentKey) {

		String administratorUsername = RepositoryRegistry.INSTANCE.retrieveAdminUsernameForRepository(repositoryId);

		if (StringUtils.isBlank(administratorUsername))
		{
			throw new CmsLoginInvalidUsernameException("No administrator username found");
		}

		return login(repositoryId, administratorUsername, key, permanentKey);
	}

	public String login(String repositoryId, Subject subject, String permanentKey) {

		if (StringUtils.isBlank(repositoryId) || ! RepositoryRegistry.INSTANCE.isRepositoryRegistered(repositoryId)){
			throw new CmsException("Invalid repository id "+ repositoryId);
		}

		//In case configuration has changed, load any changes prior to login
		deployRepositoriesFoundInTheRepositoryRegistry();

		if (!repositories.containsKey(repositoryId)){
			throw new CmsException("Repository id '"+repositoryId + "' found in configuration but could not be loaded");
		}

		authorizeSubject(subject, repositoryId);

		CmsRepository cmsRepositoryToBeConnected = repositories.get(repositoryId);

		int currentAuthenticationTokenTimeout = retrieveAuthenticationTokenTimeoutForRepository(repositoryId);

		String authenticationToken;
		try {
			authenticationToken = createAuthenticationToken(subject,cmsRepositoryToBeConnected.getApplicationPolicyName(), repositoryId, permanentKey);
		} catch (NoSuchAlgorithmException e) {
			throw new CmsException(e);
		}

		SecurityContext securityContext = new SecurityContext(authenticationToken, subject, currentAuthenticationTokenTimeout,
				getAvailableRepositoryIds());


		//Create a new AstroboaClientContext
		RepositoryContext repositoryContext = new RepositoryContext(cmsRepositoryToBeConnected, securityContext);

		AstroboaClientContext clientContext = new AstroboaClientContext(repositoryContext, lazyLoader);

		AstroboaClientContextHolder.registerClientContext(clientContext, true);

		addRepositoryUserIdPrincipalToSubject(subject, securityContext.getIdentity());

		if (logger.isDebugEnabled()){
			logger.debug("Sucessfully logged in to repository {} and register ClientContext {} to Thread {}", new Object[]{repositoryId, clientContext, Thread.currentThread().getName()});
		}

		return securityContext.getAuthenticationToken();
	}

}