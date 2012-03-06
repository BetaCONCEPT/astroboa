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
package org.betaconceptframework.astroboa.context;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;

/**
 * Provides basic information about a Astroboa repository
 * located in configuration file for all available repositories.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class CmsRepositoryImpl implements CmsRepository, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4791512450639949427L;
	
	private final String id;
	
	//Specifying implementation in order to 
	//support serialization
	private final HashMap<String, String> localizedLabels;
	private final String repositoryHomeDir;

	private final String serverURL;
	
	private final String restfulApiBasePath;
	
	private final String identityStoreId;

	private final String externalIdentityStoreJNDIName;
	
	//Administrator's user id as defined in astroboa-conf.xml
	private final String administratorUserId;

	
	public CmsRepositoryImpl(String id, HashMap<String, String> localizedLabels, String repositoryHomeDir, 
			String serverURL, String restfulApiBasePath, String identityStoreId, 
			String externalIdentityStoreJNDIName, 
			String administratorUserId){
		this.id = id;
		this.localizedLabels = localizedLabels;
		this.repositoryHomeDir = repositoryHomeDir;
		this.serverURL = serverURL;
		this.restfulApiBasePath = restfulApiBasePath;
		this.identityStoreId = identityStoreId;
		this.externalIdentityStoreJNDIName = externalIdentityStoreJNDIName;
		this.administratorUserId = administratorUserId;
		
	}


	/**
	 * Returns the id of the repository
	 * 
	 * @return Repository Id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the localized label for the given locale.
	 * 
	 * If a blank <code>locale</code> is provided the default value is
	 * {@link Locale#ENGLISH English} locale.
	 *  
	 * @param locale Locale value.
	 * 
	 * @return localized label for the specified <code>locale</code>
	 *  or null if none exists.
	 */
	public String getLocalizedLabelForLocale(String locale) {

		String defaultLocalizedLabel = "No available localized label";

		if (MapUtils.isEmpty(localizedLabels)){
			return defaultLocalizedLabel;
		}
		
		if (StringUtils.isBlank(locale)){
			if (!localizedLabels.containsKey(Locale.ENGLISH.toString())){
				return defaultLocalizedLabel;
			}
			else{
				return  localizedLabels.get(Locale.ENGLISH.toString());
			}
		}
		else{
			if (!localizedLabels.containsKey(locale)){
				return defaultLocalizedLabel;
			}
			else{
				return localizedLabels.get(locale);
			}
		}
	}
	
	/**
	 * Returns the full path of repository home directory.
	 * 
	 * @return Repository home directory
	 */
	public String getRepositoryHomeDirectory(){
		return repositoryHomeDir;
	}
	
	public String toString(){
		return "CmsRepository id: "+ id;
	}

	/**
	 * Returns the URL of the server that hosts this content repository i.e. "http://" followed by the server
	 * fully qualified domain name or ip address.
	 * 
	 * The URL is configured in the repositories configuration XML file
	 * 
	 * This URL is mainly used in combination with {@link CmsRepositoryImpl#restfulApiBasePath} in order to
	 * determine the base URL for accessing the Astroboa RESful API.
	 * For example it is utilized in {@link BinaryChannelImpl} methods that produce the RESTful API calls for
	 * retrieval of binary files stored in content object properties 
	 * 
	 * @return Astroboa repository server URL
	 */
	public String getServerURL() {
		return serverURL;
	}
	
	/**
	 * Returns the base path under which the RESTful API is accessible i.e. the path that follows the Server URL provided by {@link #getServerURL()}.
	 * The path is configured in the repositories configuration XML file and is set by default to "/resource-api" 
	 * which is the context path of the content-api war (the war that implements the RESTful API). 
	 * 
	 * The path is used in combination with {@link CmsRepositoryImpl#serverURL} in order to
	 * determine the base URL for accessing the Astroboa RESful API.
	 * For example it is utilized in {@link BinaryChannelImpl} methods that produce the RESTful API calls for
	 * retrieval of binary files stored in content object properties
	 * 
	 * @return The base path (relative to the Astroboa repository server URL) under which the RESTful API is accessible
	 */
	public String getRestfulApiBasePath() {
		return restfulApiBasePath;
	}


	public String getExternalIdentityStoreJNDIName() {
		return externalIdentityStoreJNDIName;
	}


	public String getIdentityStoreRepositoryId() {
		return identityStoreId;
	}


	public String getAdministratorUserId() {
		return administratorUserId;
	}
}
