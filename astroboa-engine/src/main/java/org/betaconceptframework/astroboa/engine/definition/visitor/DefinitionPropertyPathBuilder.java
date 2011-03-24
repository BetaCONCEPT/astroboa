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
package org.betaconceptframework.astroboa.engine.definition.visitor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicPropertyDefinition;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.util.CmsConstants;


/**
 * TODO : Rename this class as its responsibilities are more than initially 
 * designed. Practically, its job is to visit all definition hierarchy in order to 
 * produce several helper constructs.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class DefinitionPropertyPathBuilder extends AbstractCmsPropertyDefinitionVisitor{

	private Map<String, Set<String>> topicPropertyPathsPerTaxonomy = new HashMap<String, Set<String>>();

	private Set<String> mutlivalueProperties = new HashSet<String>();
	
	//If root definition is complex then we need the full path to include
	private boolean rootDefinitionIsComplex;

	private Deque<String> parentComplexPropertyPath = new ArrayDeque<String>();
	
	private List<ContentObjectPropertyDefinition> contentObjectPropertyDefinitions = new ArrayList<ContentObjectPropertyDefinition>();

	
	public DefinitionPropertyPathBuilder(boolean rootDefinitionIsComplex){
		setVisitType(VisitType.Full);
		this.rootDefinitionIsComplex = rootDefinitionIsComplex;
	}

	@Override
	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition) {

	}
	
	@Override
	public void finishedChildDefinitionsVisit(LocalizableCmsDefinition parentDefinition) {
		super.finishedChildDefinitionsVisit(parentDefinition);
		
		String path = parentComplexPropertyPath.poll();
		
		logger.debug("Removing path {}", path);
		
	}
	
	@Override
	public void startChildDefinitionsVisit(LocalizableCmsDefinition parentDefinition) {
		
		super.startChildDefinitionsVisit(parentDefinition);

		if (parentDefinition instanceof ContentObjectTypeDefinition){
			parentComplexPropertyPath.clear();
		}
		else{
			if (rootDefinitionIsComplex){
				parentComplexPropertyPath.push(((CmsPropertyDefinition)parentDefinition).getFullPath());
			}
			else{
				parentComplexPropertyPath.push(((CmsPropertyDefinition)parentDefinition).getPath());
			}
		}
		
		logger.debug("Added parent path {}", parentComplexPropertyPath.peek());
		
	}

	@Override
	public void visitComplexPropertyDefinition(
			ComplexCmsPropertyDefinition complexPropertyDefinition) {

		if (complexPropertyDefinition.isMultiple()){
			addMultivalueProperty(complexPropertyDefinition.getName());
		}
		
	}

	private void addMultivalueProperty(String multivalueProperty){
		if (multivalueProperty != null && ! mutlivalueProperties.contains(multivalueProperty)){
			mutlivalueProperties.add(multivalueProperty);
		}
	}
	
	@Override
	public <T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {

		if (simplePropertyDefinition.isMultiple()){
			addMultivalueProperty(simplePropertyDefinition.getName());
		}
		
		switch (simplePropertyDefinition.getValueType()) {
		case ContentObject:
			contentObjectPropertyDefinitions.add((ContentObjectPropertyDefinition) simplePropertyDefinition);
			
			break;
		case Topic:
			List<String> acceptedTaxonomies = ((TopicPropertyDefinition)simplePropertyDefinition).getAcceptedTaxonomies();
			
			if (rootDefinitionIsComplex){
				addTopicPropertyPathForTaxonomies(simplePropertyDefinition.getFullPath(),acceptedTaxonomies);
			}
			else{
				addTopicPropertyPathForTaxonomies(simplePropertyDefinition.getPath(),acceptedTaxonomies);
			}

			break;
		default:
			break;
		}

	}

	private <T> void addTopicPropertyPathForTaxonomies(
			String topicPropertyPath, List<String> acceptedTaxonomies) {
		
		if (CollectionUtils.isEmpty(acceptedTaxonomies)){
			addTopicPropertyPathForTaxonomy(CmsConstants.ANY_TAXONOMY, topicPropertyPath);
		}
		else{
			for (String acceptedTaxonomy: acceptedTaxonomies){
				addTopicPropertyPathForTaxonomy(acceptedTaxonomy, topicPropertyPath);
			}
		}
	}


	private void addTopicPropertyPathForTaxonomy(String taxonomyName, String childDefinitionPath) {

		if (childDefinitionPath.startsWith("administrativeMetadataType") || 
				childDefinitionPath.startsWith("accessibilityType")){
			//Should not register topic property path if property belongs to administrativeMetadataType or accessibilityType
			logger.debug("Topic property path {} will be not added to topic property path cache", childDefinitionPath);
			return;
		}

		if (!topicPropertyPathsPerTaxonomy.containsKey(taxonomyName)){
			topicPropertyPathsPerTaxonomy.put(taxonomyName, new HashSet<String>());
		}

		topicPropertyPathsPerTaxonomy.get(taxonomyName).add(childDefinitionPath);
		
		logger.debug("Added topic property path {} to cache", childDefinitionPath);
	}

	public Map<String, Set<String>> getTopicPropertyPathsPerTaxonomy() {
		return topicPropertyPathsPerTaxonomy;
	}

	public Set<String> getMutlivalueProperties() {
		return mutlivalueProperties;
	}

	public List<ContentObjectPropertyDefinition> getContentObjectPropertyDefinitions() {
		return contentObjectPropertyDefinitions;
	}

}
