package org.betaconceptframework.astroboa.console.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ObjectReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("schemaServiceAsync")
@Scope(ScopeType.EVENT)
public class SchemaServiceAsync {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private DefinitionService definitionService;
	private TaxonomyService taxonomyService;
	
	@In
	private Map<String,String> messages;

	@WebRemote
	public String[] getPropertyDescription(String fullPropertyPath) {
		String propertyDescription[] = new String[2];
		propertyDescription[0] = fullPropertyPath;
		
		CmsPropertyDefinition propertyDefinition = (CmsPropertyDefinition) definitionService
				.getCmsDefinition(fullPropertyPath, ResourceRepresentationType.DEFINITION_INSTANCE, false);
		
		propertyDescription[1] = propertyDefinition
			.getDescription()
			.getAvailableLocalizedLabel(LocaleSelector.instance()
			.getLocaleString());
		
		return propertyDescription;
	}
	
	@WebRemote
	public String[] getTopicPropertyAllowedTaxonomies(String fullPropertyPath) {
		String allowedTaxonomies[] = new String[2];
		allowedTaxonomies[0] = fullPropertyPath;
		
		try {
			List<String> allowedTaxonomyNames = 
					((TopicReferencePropertyDefinition) definitionService.getCmsDefinition(fullPropertyPath, ResourceRepresentationType.DEFINITION_INSTANCE, false))
					.getAcceptedTaxonomies();

			if (CollectionUtils.isNotEmpty(allowedTaxonomyNames)) {

				List<String> localizedLabels = new ArrayList<String>();
				//Load localized Labels for all taxonomies
				for (String allowedTaxonomyName : allowedTaxonomyNames){
					Taxonomy allowedTaxonomy = taxonomyService.getTaxonomy(allowedTaxonomyName, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);

					if (allowedTaxonomy == null){
						logger.warn("Try to load accepted taxonomy {} but was not found", allowedTaxonomyName);
						localizedLabels.add(allowedTaxonomyName);
					}
					else{
						localizedLabels.add(allowedTaxonomy.getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString())); 
					}
				}

				allowedTaxonomies[1] = 
					messages.get("content.object.edit.topic.selection.accepted.values.fromSpecificTaxonomies") + 
					"<br/>" +
					"<strong>" + StringUtils.join(localizedLabels, ",") + "</strong>";

			}
			else {
				allowedTaxonomies[1] = messages.get("content.object.edit.topic.selection.accepted.values.fromAllTaxonomies");
			}


			return allowedTaxonomies; 
		}
		catch (Exception e) {
			logger.error("An error occured while asynchronously retrieving the allowed taxonomies for a topic property", e);
			return allowedTaxonomies;
		}
	}
	
	@WebRemote
	public String[] getObjectRefPropertyAllowedObjectTypes(String fullPropertyPath) {
		String allowedObjectTypes[] = new String[2];
		allowedObjectTypes[0] = fullPropertyPath;
		
		try {
			List<String> allowedObjectTypeNames = 
					((ObjectReferencePropertyDefinition) definitionService.getCmsDefinition(fullPropertyPath, ResourceRepresentationType.DEFINITION_INSTANCE, false))
					.getAcceptedContentTypes();

			if (CollectionUtils.isNotEmpty(allowedObjectTypeNames)) {

				List<String> localizedLabels = new ArrayList<String>();
				//Load localized Labels for all taxonomies
				for (String allowedObjectTypeName : allowedObjectTypeNames){
					ContentObjectTypeDefinition objectTypeDefinition = 
							(ContentObjectTypeDefinition) definitionService.getCmsDefinition(allowedObjectTypeName, ResourceRepresentationType.DEFINITION_INSTANCE, false);

					if (objectTypeDefinition == null){
						logger.warn("Try to load definition for object type {} but was not found", allowedObjectTypeName);
						localizedLabels.add(allowedObjectTypeName);
					}
					else{
						localizedLabels.add(objectTypeDefinition.getDisplayName().getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString())); 
					}
				}

				allowedObjectTypes[1] = 
					messages.get("content.object.edit.content.object.selection.accepted.values.specificObjectTypes") + 
					"<br/>" +
					"<strong>" + StringUtils.join(localizedLabels, ",") + "</strong>";

			}
			else {
				allowedObjectTypes[1] = messages.get("content.object.edit.content.object.selection.accepted.values.allObjectTypes");
			}


			return allowedObjectTypes; 
		}
		catch (Exception e) {
			logger.error("An error occured while asynchronously retrieving the allowed object types for an object reference property", e);
			return allowedObjectTypes;
		}
	}
	
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}
}
