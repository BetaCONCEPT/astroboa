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
package org.betaconceptframework.astroboa.console.jsf.edit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CacheRegion;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.console.commons.TopicComparator;
import org.betaconceptframework.astroboa.console.commons.TopicComparator.OrderByProperty;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("languageSelector")
@Scope(ScopeType.SESSION)
public class LanguageSelector {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Map<String, SelectItem> languagesAsSelectItems = new LinkedHashMap<String, SelectItem>();

	private TopicComparator topicComparator;

	@In
	private LocaleSelector localeSelector;

	private TopicService topicService;
	private TaxonomyService taxonomyService;

	public Collection<SelectItem> getLanguagesAsSelectItems(){

		try{
			if (languagesAsSelectItems.isEmpty()){
				//No select item found for locale.
				//Create select item list from language taxonomy
				List<Topic> languageTopics = retrieveAllLanguageTopics();

				if (CollectionUtils.isEmpty(languageTopics)){
					//No topics found for taxonomy. Load locale selector
					return localeSelector.getSupportedLocales(); 
				}
				else{

					logger.error("Creating select items");
					
					if (topicComparator == null){
						topicComparator = new TopicComparator("en", OrderByProperty.LABEL);
					}

					Collections.sort(languageTopics, topicComparator);

					for (Topic language : languageTopics){
						SelectItem selectItem = new SelectItem();
						selectItem.setLabel(language.getLocalizedLabelForLocale("en"));
						selectItem.setValue(language.getName());

						languagesAsSelectItems.put(language.getName(), selectItem);
					}
				}
			}
			return languagesAsSelectItems.values();
		}
		catch(Exception e){
			logger.error("",e);
			return localeSelector.getSupportedLocales();
		}
	}

	private List<Topic> retrieveAllLanguageTopics() {

		if (taxonomyService.getTaxonomy("language", ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false)==null){
			return null;
		}
		
		TopicCriteria topicCriteria = CmsCriteriaFactory.newTopicCriteria();
		topicCriteria.addTaxonomyNameEqualsCriterion("language");
		topicCriteria.setCacheable(CacheRegion.TEN_MINUTES);

		CmsOutcome<Topic> outcome = topicService.searchTopics(topicCriteria, ResourceRepresentationType.TOPIC_LIST);

		return outcome != null ? outcome.getResults() : null;
	}

	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}

	public void setTaxonomyService(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}
	
	
	public String getLabelForLanguage(String language){
		if (language ==null){
			return null;
		}
		
		if (languagesAsSelectItems.isEmpty()){
			getLanguagesAsSelectItems();
		}
		
		if (languagesAsSelectItems.isEmpty() || ! languagesAsSelectItems.containsKey(language)){
			return language;
		}
		
		return languagesAsSelectItems.get(language).getLabel();
	}
	
	public static LanguageSelector instance() {
		if ( !Contexts.isSessionContextActive() ) {
			throw new IllegalStateException("No active session context");
		}

		return (LanguageSelector) Component.getInstance(LanguageSelector.class, ScopeType.SESSION);
	}
}
