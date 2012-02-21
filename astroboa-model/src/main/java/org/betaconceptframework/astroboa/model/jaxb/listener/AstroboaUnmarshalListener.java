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
package org.betaconceptframework.astroboa.model.jaxb.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller.Listener;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.impl.CmsRepositoryEntityImpl;
import org.betaconceptframework.astroboa.model.jaxb.type.AstroboaEntityType;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaUnmarshalListener extends Listener{

	private Map<String, CmsRepositoryEntity> context = new HashMap<String, CmsRepositoryEntity>(); 

	@Override
	public void afterUnmarshal(Object target, Object parent) {

		//Provide values for XMLTransient variables which are necessary
		if (target != null){
			if (target instanceof Taxonomy){

				//In case root topics have been unmarshalled
				//update number of root topics
				if (((Taxonomy)target).isRootTopicsLoaded()){
					List<Topic> rootTopics = ((Taxonomy)target).getRootTopics();
					
					((Taxonomy)target).setNumberOfRootTopics(rootTopics.size());
					
					//Fix taxonomy
					for (Topic rootTopic : rootTopics){
						rootTopic.setTaxonomy((Taxonomy)target);
					}
					
				}
			}
			else if (target instanceof Topic){

				//In case children have been unmarshalled
				//update number of children
				final Topic topic = (Topic)target;
				
				if (((Topic)target).isChildrenLoaded()){

					List<Topic> children = ((Topic)target).getChildren();
					
					topic.setNumberOfChildren((children.size()));
				}
				
				boolean taxonomyHasBeenSetToTree = false;
				
				if (parent != null){
					if (parent instanceof Taxonomy){
						topic.setParent(null);
						topic.setTaxonomy((Taxonomy)parent);
						taxonomyHasBeenSetToTree = true;
					}
					else if (parent instanceof Topic && topic.getParent() == null){
						topic.setParent((Topic)parent);
						topic.setTaxonomy(((Topic)parent).getTaxonomy());
						taxonomyHasBeenSetToTree = true;
					}
				}
				
				if (! taxonomyHasBeenSetToTree && topic.getTaxonomy() != null && 
						((Topic)target).isChildrenLoaded()){
					for (Topic child : ((Topic)target).getChildren()){
						child.setTaxonomy(topic.getTaxonomy());
					}
				}
			}
			else if (target instanceof Space){

				final Space space = (Space)target;
				
				//In case children have been unmarshalled
				//update number of children
				if (((Space)target).isChildrenLoaded()){
					((Space)target).setNumberOfChildren((((Space)target).getChildren().size()));
				}
				
				if (parent != null){
					if (parent instanceof Space && space.getParent() == null){
						space.setParent((Space)parent);
					}
				}
			}

		}
	}

	@Override
	public void beforeUnmarshal(Object target, Object parent) {

		//Register parent to context if not already there.
		//This is an (ugly?) way of registering mainly CmsRepositoryEntities
		//which have been created by JAXB and not by any Adapter
		if (parent != null){

			if (parent instanceof CmsRepositoryEntity){ 
					
				if (!(parent instanceof AstroboaEntityType)){
					registerCmsRepositoryEntityToContext((CmsRepositoryEntity) parent);
				}
				
				//Add authentication token if not there
				((CmsRepositoryEntityImpl) parent).setAuthenticationToken(
						CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().getAuthenticationToken());
			}
			
			

		}


		//pass unmarshalling context to target
		if (target != null){
			if (target instanceof AstroboaEntityType){
				((AstroboaEntityType)target).setAstroboaUnmarshalListener(this);
			}
			
			if (target instanceof CmsRepositoryEntity){
				//Add authentication token if not there
				((CmsRepositoryEntityImpl) target).setAuthenticationToken(
						CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().getAuthenticationToken());
			}
		}
	}

	public CmsRepositoryEntity getCmsRepositoryEntityFromContext(String key) {

		if (key == null){
			return null;
		}

		if (context == null || ! context.containsKey(key)){
			return null;
		}

		return context.get(key);
	}

	public void registerCmsRepositoryEntityToContext(CmsRepositoryEntity cmsRepositoryEntity){
		if (context != null && cmsRepositoryEntity != null){ 
				
			if (cmsRepositoryEntity.getId() != null){
				
				if (contextDoesNotContainValue(cmsRepositoryEntity.getId())){
					context.put(cmsRepositoryEntity.getId(), cmsRepositoryEntity);
				}
			}
			else{
				//If entity is of type Topic or Taxonomy or Space or ContentObject
				//or RepositoryUser then it is safe to use their name (and externalId or systemName)
				//as the key instead of id
				if (cmsRepositoryEntity instanceof Taxonomy){
					final String name = ((Taxonomy)cmsRepositoryEntity).getName();
					if (name != null && contextDoesNotContainValue(name)){
						context.put(((Taxonomy)cmsRepositoryEntity).getName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof ContentObject){
					final String systemName = ((ContentObject)cmsRepositoryEntity).getSystemName();
					if (systemName != null && contextDoesNotContainValue(systemName)){
						context.put(((ContentObject)cmsRepositoryEntity).getSystemName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof Topic){
					final String name = ((Topic)cmsRepositoryEntity).getName();
					if (name != null && contextDoesNotContainValue(name)){
							context.put(name, cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof Space){
					final String name = ((Space)cmsRepositoryEntity).getName();
					if (name != null && contextDoesNotContainValue(name)){
						context.put(((Space)cmsRepositoryEntity).getName(), cmsRepositoryEntity);
					}
				}
				else if (cmsRepositoryEntity instanceof RepositoryUser){
					final String externalId = ((RepositoryUser)cmsRepositoryEntity).getExternalId();
					if (externalId != null && contextDoesNotContainValue(externalId)){
						context.put(((RepositoryUser)cmsRepositoryEntity).getExternalId(), cmsRepositoryEntity);
					}
				}
				
			}
		}
	}


	private boolean contextDoesNotContainValue(String key) {
		return key != null && ! context.containsKey(key);
	}

	public void clearContext(){
		context.clear();
	}

}
