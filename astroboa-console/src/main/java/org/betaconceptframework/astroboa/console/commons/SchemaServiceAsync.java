package org.betaconceptframework.astroboa.console.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.definition.TopicPropertyDefinition;
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
		
		propertyDescription[1] = definitionService
			.getCmsPropertyDefinition(fullPropertyPath)
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
			List<String> allowedTaxonomyNames = ((TopicPropertyDefinition) definitionService.getCmsPropertyDefinition(fullPropertyPath))
			.getAcceptedTaxonomies();

			if (CollectionUtils.isNotEmpty(allowedTaxonomyNames)) {

				List<String> localizedLabels = new ArrayList<String>();
				//Load localized Labels for all taxonomies
				for (String allowedTaxonomyName : allowedTaxonomyNames){
					Taxonomy allowedTaxonomy = taxonomyService.getTaxonomy(allowedTaxonomyName, null);

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
	
	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}
}
