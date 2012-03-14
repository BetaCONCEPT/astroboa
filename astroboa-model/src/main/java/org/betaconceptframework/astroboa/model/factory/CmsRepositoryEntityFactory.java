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

package org.betaconceptframework.astroboa.model.factory;


import java.io.File;
import java.util.Calendar;
import java.util.Locale;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;
import org.betaconceptframework.astroboa.model.impl.ComplexCmsRootPropertyImpl;
import org.betaconceptframework.astroboa.model.impl.ContentObjectImpl;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.RepositoryUserImpl;
import org.betaconceptframework.astroboa.model.impl.SpaceImpl;
import org.betaconceptframework.astroboa.model.impl.TaxonomyImpl;
import org.betaconceptframework.astroboa.model.impl.TopicImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexPropertyDefinitionHelper;
import org.betaconceptframework.astroboa.model.impl.definition.ContentObjectTypeDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.definition.LocalizableCmsDefinitionImpl;
import org.betaconceptframework.astroboa.model.impl.query.render.RenderPropertiesImpl;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * Provides methods for creating new content repository {@link CmsRepositoryEntity entities}.
 *
 *
 *  <p>
 * This is the entry point for creating repository entities like
 * {@link ContentObject content objects} or {@link Topic topics}.
 * </p>
 * 
 * <p>
 * The scope of this factory is to provide the developer with 
 * a repository entity, properly initialized with all underlying details specific 
 * to Astroboa implementation model.
 * </p>
 * 
 * All subclasses of this class must provide the authentication token needed by the entities
 * created
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public abstract class CmsRepositoryEntityFactory {

	/**
	 * Creates a new {@link Topic}.
	 * 
	 * @return A topic
	 */
	public Topic newTopic(){
		
		TopicImpl topic = new TopicImpl();
		topic.setAuthenticationToken(getAuthenticationToken());
		
		return topic;
		
	}
	
	/**
	 * Creates a new {@link Space}.
	 * @return A space
	 */
	public Space newSpace(){
		
		SpaceImpl space = new SpaceImpl();
		space.setAuthenticationToken(getAuthenticationToken());
		
		return space;
		
	}
	
	/**
	 * Creates a new {@link RepositoryUser}
	 * @return A repository user
	 */
	public RepositoryUser newRepositoryUser(){
		
		RepositoryUser repositoryUser = new RepositoryUserImpl();
		((RepositoryUserImpl)repositoryUser).setFolksonomy(newTaxonomy());
		((RepositoryUserImpl)repositoryUser).setSpace(newSpace());
		((RepositoryUserImpl)repositoryUser).setAuthenticationToken(getAuthenticationToken());
		
		return repositoryUser;
	}
	
	/**
	 * Creates a new {@link Taxonomy}
	 * @return A taxonomy
	 */
	public Taxonomy newTaxonomy(){
		
		TaxonomyImpl taxonomy = new TaxonomyImpl();
		taxonomy.setAuthenticationToken(getAuthenticationToken());
		
		return taxonomy;
		
	}
	
	/**
	 * Creates a new {@link BinaryChannel}
	 * @return A binary channel
	 */
	public BinaryChannel newBinaryChannel(){
		BinaryChannelImpl binaryChannel = new BinaryChannelImpl();
		binaryChannel.setAuthenticationToken(getAuthenticationToken());
		
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForClient(getAuthenticationToken());
		CmsRepository activeCmsRepository = repositoryContext != null ? repositoryContext.getCmsRepository() : null;
		
		if (activeCmsRepository == null || StringUtils.isBlank(activeCmsRepository.getRepositoryHomeDirectory())){
			throw new CmsException("Could not find repository id for authentication token "+ getAuthenticationToken()  );
		}
		
		binaryChannel.setRepositoryId(activeCmsRepository.getId());
		
		return binaryChannel;
	}

	
	/**
	 * Create a new {@link ContentObject content object}.
	 * 
	 * <p>
	 * Newly created instance will contain content object's {@link ContentObjectTypeDefinition definition} instance 
	 * and all necessary mechanism which allows users 
	 * to add and/or remove {@link CmsProperty properties} 
	 * to/from content object.
	 * </p>
	 * 
	 * <p>
	 * This is the entry point for creating new content objects instances and users
	 * are strongly advised to use ONLY this method when they want to create new content objects.
	 * </p>
	 * 
	 * @param contentType
	 *            Content object type name.
	 * 
	 * @return An {@link ContentObject instance } of content type.
	 */ 
	public ContentObject newObjectForType(String contentType){
		try{	
			LazyLoader lazyLoader = AstroboaClientContextHolder.getLazyLoaderForClient(getAuthenticationToken());
			
			if (lazyLoader == null){
					throw new CmsException("Could not find LazyLoader in thread");
			}
			else{
				lazyLoader.activateClientContextForAuthenticationToken(getAuthenticationToken());
			}
			
			ContentObjectTypeDefinition contentObjectTypeDefinition = (ContentObjectTypeDefinition) lazyLoader.getDefinitionService().getCmsDefinition(contentType, ResourceRepresentationType.DEFINITION_INSTANCE,false);

			if (contentObjectTypeDefinition == null){
				throw new CmsException("No content object definition for type "+ contentType + " for repository "+
						AstroboaClientContextHolder.getActiveRepositoryId());
			}

			ContentObject contentObject  = new ContentObjectImpl(); 
			ComplexCmsRootPropertyImpl complexCmsRootPropertyImpl = new ComplexCmsRootPropertyImpl();
			complexCmsRootPropertyImpl.setAuthenticationToken(getAuthenticationToken());
			
			((ContentObjectImpl)contentObject).setComplexCmsRootProperty(complexCmsRootPropertyImpl);
			((ContentObjectImpl)contentObject).setAuthenticationToken(getAuthenticationToken());
			
			RenderProperties renderProperties = new RenderPropertiesImpl();

			//	Type
			contentObject.setContentObjectType(contentObjectTypeDefinition.getName());

			//Render Type Definition
			((ContentObjectImpl)contentObject).setTypeDefinition(contentObjectTypeDefinition);

			Localization displayName = ((LocalizableCmsDefinitionImpl)contentObjectTypeDefinition).cloneDisplayName();
			
			Localization description = ((LocalizableCmsDefinitionImpl)contentObjectTypeDefinition).cloneDescription();
			
			ComplexPropertyDefinitionHelper complexPropertyDefinitionHelper = ((ContentObjectTypeDefinitionImpl)contentObjectTypeDefinition).getComplexPropertyDefinitionHelper();

			contentObject.getComplexCmsRootProperty().setPropertyDefinition(
					new ComplexCmsPropertyDefinitionImpl(contentObjectTypeDefinition.getQualifiedName(),
					description,displayName, false,false,true, null, null, null, null, complexPropertyDefinitionHelper, 
					((ContentObjectTypeDefinitionImpl)contentObjectTypeDefinition).getDefinitionFileURI(),  
					contentObjectTypeDefinition.getPropertyPathsWhoseValuesCanBeUsedAsALabel(), true,  
					null, contentObjectTypeDefinition.getName(), true));

			//Provide values that will be used for lazy rendering
			((LazyCmsProperty) contentObject.getComplexCmsRootProperty()).setRenderProperties(renderProperties);

			return contentObject;

		} catch (Exception e) {
			throw new CmsException(e);
		}
	}
	

	/**
	 * Creates a new unmanaged {@link BinaryChannel} from the provided path, 
	 * relative to UnmanagedDataStore directory which is located under repository home directory.
	 * 
	 * @return Resource path relative to UnmanagedDataStore directory
	 */
	public BinaryChannel newUnmanagedBinaryChannel(String relativePathToBinaryContent){
		BinaryChannelImpl binaryChannel = new BinaryChannelImpl();
		binaryChannel.setAuthenticationToken(getAuthenticationToken());
		binaryChannel.setRelativeFileSystemPath(relativePathToBinaryContent);
		binaryChannel.setUnmanaged(true);
		
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForClient(getAuthenticationToken());
		CmsRepository activeCmsRepository = repositoryContext != null ? repositoryContext.getCmsRepository() : null; 
		
		if (activeCmsRepository == null || StringUtils.isBlank(activeCmsRepository.getRepositoryHomeDirectory())){
			throw new CmsException("Could not find repository home directory."+
					(activeCmsRepository==null ? "Found no active repository" : "Active repository "+
					activeCmsRepository.getId() + " does not provide its home directory."));
		}
		
		String unmanagedDataStorePath = activeCmsRepository.getRepositoryHomeDirectory()+File.separator+CmsConstants.UNMANAGED_DATASTORE_DIR_NAME;
		
		String absoluteBinaryChanelContentPath = unmanagedDataStorePath+File.separator+relativePathToBinaryContent;
		
		((BinaryChannelImpl)binaryChannel).setAbsoluteBinaryChannelContentPath(absoluteBinaryChanelContentPath);
		
		File binaryFile = new File(absoluteBinaryChanelContentPath);
		
		if (binaryFile.exists()) {
		
			binaryChannel.setSize(binaryFile.length());
			
			binaryChannel.setSourceFilename(binaryFile.getName());
			
			binaryChannel.setMimeType(new MimetypesFileTypeMap().getContentType(binaryFile));
		
			//Encoding ???
			//binaryChannel.setEncoding();
			
			Calendar lastModifiedDate = Calendar.getInstance();
			lastModifiedDate.setTimeInMillis(binaryFile.lastModified());

			binaryChannel.setModified(lastModifiedDate);
			
		}
		
		((BinaryChannelImpl)binaryChannel).setRepositoryId(activeCmsRepository.getId());
		
		return binaryChannel;
	}
	
	public abstract String getAuthenticationToken();

}
