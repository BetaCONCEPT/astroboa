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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.portal.schedule.RepositoryResourceBundleMessagesScheduler;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Astroboa Resource Bundle.
 * 
 * <p>
 *   Astroboa Resource bundle is responsible to deliver locale-specific
 *   resources (mainly strings) stored in a taxonomy. 
 *   Each topic of this taxonomy represents a locale-specific resource 
 *   and topic's localized labels represent the values of this resource 
 *   for different locales. 
 * </p>
 * 
 * <p>
 *  Astroboa resource bundle expects to find a taxonomy whose name is comprised of the value of the property 
 *  {@link PortalStringConstants#PORTAL_SYSTEM_NAME} (located in portal.properties),
 *  suffixed by "-"+{@link PortalStringConstants#REPOSITORY_RESOURCE_BUNDLE_TAXONOMY_NAME_SUFFIX}.
 * </p>
 * 
 * <p>
 * 	All taxonomy's topic names and their localized labels are pre-loaded in order to 
 *  deliver the appropriate resources more efficiently. A scheduler is initialized as well, in 
 *  order to refresh the resources (topics and their labels). The period at which resources are refreshed
 *  is determined by the value of the property {@link PortalStringConstants#REPOSITORY_RESOURCE_BUNDLE_CACHE_REGION}
 *  (located in portal.properties file) and by default is {@link CacheRegion#ONE_DAY}. 
 * </p>
 * 
 * <p>
 * 	Note, that value for cache is 1 day ({@link CacheRegion#ONE_DAY} ), scheduler is scheduled to run at 3:00 a.m. every day
 * and if value for cache is no cache ({@link CacheRegion#NONE} ), scheduler is scheduled to run every minute.
 * </p>
 * 
 * <p>
 * 	Please not that all topics are loaded, regardless of whether they are only containers or not.
 * </p>
 * 
 * <p>
 * 	This approach offers a number of advantages in respect to the use of messages.properties files 
 * 
 *   <ul>
 *     <li>Portal's localized labels are stored in the repository where all other portal related content (templates, layouts, css, etc)
 *     is stored. This means that practically all portal related content can be managed through Astroboa</li>
 *     <li>In traditional approach, if you needed to change the contents of a messages.properties file you need to restart the
 *     application which is not the case with this setup. Just perform the change in the taxonomy and it will be available
 *     on the next scheduler execution</li>
 *     <li>You can organize resources in a tree like hierarchy in contrary to the flat hierarchy offered by the messages.properties</li>
 *   </ul>
 * </p>
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Scope(ScopeType.APPLICATION)  
@Name("repositoryResourceBundle")  
public class RepositoryResourceBundle extends ResourceBundle{

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@In(create=true)
	private RepositoryResourceBundleMessagesScheduler repositoryResourceBundleMessagesScheduler;

	private Map<String, Map<String, String>> resourcesPerKey = new HashMap<String, Map<String,String>>();

	public static RepositoryResourceBundle instance(){
		RepositoryResourceBundle repositoryResourceBundle = null;
		
		if (!Contexts.isApplicationContextActive()) {
			repositoryResourceBundle = new RepositoryResourceBundle();
		} else {
			repositoryResourceBundle = (RepositoryResourceBundle) Component.getInstance(RepositoryResourceBundle.class, ScopeType.APPLICATION);
		}
		
		if (repositoryResourceBundle != null){
			repositoryResourceBundle.initialize();
		}
		
		return repositoryResourceBundle;
	}
	
	private void initialize(){

		CacheRegion cacheRegion = CacheRegion.ONE_DAY;
		
		PropertiesConfiguration portalConfiguration = null;
		
		try{
			portalConfiguration = new PropertiesConfiguration("portal.properties");
			
			String cacheRegionValueInConfiguration =  portalConfiguration.getString(PortalStringConstants.REPOSITORY_RESOURCE_BUNDLE_CACHE_REGION);
			
			try{
				cacheRegion = CacheRegion.valueOf(cacheRegionValueInConfiguration);
			}
			catch(Exception e){
				logger.warn("Invalid value of "+PortalStringConstants.REPOSITORY_RESOURCE_BUNDLE_CACHE_REGION +" : "+ cacheRegionValueInConfiguration+
						" Cache Region will be set to "+CacheRegion.ONE_DAY);
				cacheRegion =CacheRegion.ONE_DAY;
			}
		}
		catch(Exception e){
			logger.error("",e);
			portalConfiguration=null;
		}
			
		//Activate scheduler to load resource bundles
		switch (cacheRegion) {
		case ONE_DAY:
			// Run in 3 in the morning every day
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), "0 0 3 * * ?");
			break;
		case NONE:
		case ONE_MINUTE:
			// Run every minute
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(1*60*1000));
			break;
		case FIVE_MINUTES:
			// Run every 5 minute
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(5*60*1000));
			break;
		case TEN_MINUTES:
			// Run every 10 minute
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(10*60*1000));
			break;
		case THIRTY_MINUTES:
			// Run every 30 minute
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(30*60*1000));
			break;
		case ONE_HOUR:
			// Run every hour
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(1*60*60*1000));
			break;
		case SIX_HOURS:
			// Run every 6 hours
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(6*60*60*1000));
			break;
		case TWELVE_HOURS:
			// Run every 12 hours
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), new Long(12*60*60*1000));
			break;
		default:
			// Run in 3 in the morning every day
			repositoryResourceBundleMessagesScheduler.loadRepositoryResourceBundle(new Date(), "0 0 3 * * ?");
			break;
		}
		
	}

	@Override
	public Enumeration<String> getKeys() {
		return Collections.enumeration(resourcesPerKey.keySet());
	}
	
	@Override
	protected Object handleGetObject(String key) {
		
		try{
			
			if (resourcesPerKey.isEmpty() || StringUtils.isBlank(key) || ! resourcesPerKey.containsKey(key)){
				return key;
			}
			
			//Retrieve active locale
			LocaleSelector localeSelector = LocaleSelector.instance();

			if ( localeSelector == null || ! resourcesPerKey.get(key).containsKey(localeSelector.getLocaleString())){
				return key;
			}
			
			return resourcesPerKey.get(key).get(localeSelector.getLocaleString());

		}
		catch(Exception e){
			logger.error("",e);
			return key;
		}
	}


	/**
	 * @param resourcesPerKey
	 */
	public void setResources(
			Map<String, Map<String, String>> resourcesPerKey) {
		if (resourcesPerKey == null){
			clearResources();
		}
		else{
			this.resourcesPerKey = resourcesPerKey;
		}
		
	}

	/**
	 * 
	 */
	public void clearResources() {
		if (this.resourcesPerKey != null){
			this.resourcesPerKey.clear();
		}
		
	}
}
