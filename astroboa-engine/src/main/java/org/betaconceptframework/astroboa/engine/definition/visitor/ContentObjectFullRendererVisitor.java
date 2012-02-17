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
package org.betaconceptframework.astroboa.engine.definition.visitor;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.engine.model.lazy.local.LazyComplexCmsPropertyLoader;
import org.betaconceptframework.astroboa.model.impl.LazyCmsProperty;
import org.betaconceptframework.astroboa.model.impl.definition.ComplexCmsPropertyDefinitionImpl;

/**
 * This class is responsible to load properties of a content object
 * according to the provided definition.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectFullRendererVisitor extends AbstractCmsPropertyDefinitionVisitor{

	private LazyComplexCmsPropertyLoader lazyComplexCmsPropertyLoader;
	
	private Deque<ComplexCmsProperty<?,?>> parentComplexCmsPropertyQueue = new ArrayDeque<ComplexCmsProperty<?,?>>(); 
	private String contentObjectNodeUUID;
	private RenderProperties renderProperties;
	private Session session;
	private Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities;
	
	public ContentObjectFullRendererVisitor(ContentObject contentObject, LazyComplexCmsPropertyLoader lazyComplexCmsPropertyLoader, 
			String contentObjectNodeUUID, Session session, RenderProperties renderProperties, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities) {
		
		if (contentObject == null){
			throw new CmsException("Must specify a content object in order to render its properties");
		}
		
		if (lazyComplexCmsPropertyLoader == null){
			throw new CmsException("Must specify a lazy complex cms property loader");
		}
		
		setVisitType(VisitType.Self);
		
		this.contentObjectNodeUUID = contentObjectNodeUUID;
		this.session = session;
		this.renderProperties = renderProperties;
		this.cachedCmsRepositoryEntities = cachedCmsRepositoryEntities;
		this.lazyComplexCmsPropertyLoader = lazyComplexCmsPropertyLoader;
		
		parentComplexCmsPropertyQueue.push(contentObject.getComplexCmsRootProperty());
		
	}

	
	@Override
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {
		
		if (contentObjectTypeDefinition.hasCmsPropertyDefinitions()){
			for (CmsPropertyDefinition propertyDefinition: contentObjectTypeDefinition.getPropertyDefinitions().values()){
				propertyDefinition.accept(this);
			}
		}

	}

	@Override
	public void visitComplexPropertyDefinition(
			ComplexCmsPropertyDefinition complexPropertyDefinition) {
		
		renderCmsPropertiesForDefinition(complexPropertyDefinition);
	}


	private void renderCmsPropertiesForDefinition(
			CmsPropertyDefinition cmsPropertyDefinition) {
		
		ComplexCmsProperty<?, ?> parentProperty = parentComplexCmsPropertyQueue.getFirst();
		
		//No need to render a property if it has no value whatsoever.
		if (!parentProperty.hasValueForChildProperty(cmsPropertyDefinition.getName())){
			return;
		}
		
		List<CmsProperty<?, ?>> cmsProperties = lazyComplexCmsPropertyLoader.renderChildProperty(cmsPropertyDefinition, 
				((LazyCmsProperty)parentProperty).getPropertyContainerUUID(),
				contentObjectNodeUUID, renderProperties, session, 
				cachedCmsRepositoryEntities);
		
		
		if (CollectionUtils.isNotEmpty(cmsProperties)){
			
			for (CmsProperty<?,?> childProperty: cmsProperties){
				
				//No point in rendering complex properties with null identifier
				if (childProperty.getValueType() == ValueType.Complex && childProperty.getId() == null){
					continue;
				}
				
				//No point in rendering empty property which means that no data are stored in repository
				if (childProperty instanceof SimpleCmsProperty && ((SimpleCmsProperty)childProperty).hasNoValues() ){
					continue;
				}
				
				try {
					
					if (cmsPropertyDefinition instanceof ComplexCmsPropertyDefinition && 
							((ComplexCmsPropertyDefinitionImpl)cmsPropertyDefinition).isRecursive() && 
							((ComplexCmsProperty)childProperty).getId() == null){
						//Definition is recursive and created property has no identifier. 
						break;
					}
							
					((LazyCmsProperty)parentComplexCmsPropertyQueue.getFirst()).addNewProperty(childProperty.getName(), cmsPropertyDefinition, childProperty);
					
					
					if (cmsPropertyDefinition instanceof ComplexCmsPropertyDefinition &&  
							((ComplexCmsPropertyDefinition)cmsPropertyDefinition).hasChildCmsPropertyDefinitions()){

						Collection<CmsPropertyDefinition> childPropertyDefinitions = ((ComplexCmsPropertyDefinition)cmsPropertyDefinition).getChildCmsPropertyDefinitions().values();

						//Change current parent
						parentComplexCmsPropertyQueue.push((ComplexCmsProperty<?, ?>) childProperty);

						for (CmsPropertyDefinition childPropertyDefinition : childPropertyDefinitions){

							//Visit its children
							childPropertyDefinition.accept(this);
						}

						parentComplexCmsPropertyQueue.pop();

					}
				} catch (Exception e) {
					throw new CmsException(e);
				}
			}
		}
	}

	@Override
	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {

		renderCmsPropertiesForDefinition(simplePropertyDefinition);
		
	}

}
