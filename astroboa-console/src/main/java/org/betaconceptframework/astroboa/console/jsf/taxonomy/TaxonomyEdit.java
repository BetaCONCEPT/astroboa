/**
 * Copyright (C) 2005-2007 BetaCONCEPT LP.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
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
package org.betaconceptframework.astroboa.console.jsf.taxonomy;


import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.service.TaxonomyService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.jsf.DynamicUIAreaPageComponent;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.LocaleSelector;

/**
 * Backing bean for page responsible to edit taxonomies and topics
 * @author Savvas Triantafyllou (striantafillou@betaconcept.gr)
 *
 */
@Name("taxonomyEdit")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TaxonomyEdit extends AbstractUIBean {
	
	
	private static final long serialVersionUID = 1L;
	
	private TaxonomyService taxonomyService;
	private TopicService topicService;
	private PageController pageController;
	private CMSUtilities cmsUtilities;
	
	@In(create=true)
	private TagEdit tagEdit;
	
	@In(create=true)
	private TaxonomyEditor taxonomyEditor;
	private RepositoryUser systemRepositoryUser;
	
	private String selectedTaxonomyId;
	private String selectedTopicId;
	
	public String openTaxonomyEditPage_UIAction(){

		//Reset taxonomy tree
		Events.instance().raiseEvent(SeamEventNames.NEW_TAXONOMY_TREE);

		return pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TAXONOMY_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	private Taxonomy findSelectedTaxonomy() { 
		if (StringUtils.isBlank(selectedTaxonomyId)){
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return null;
		}
	
		try {
			return taxonomyService.getTaxonomy(selectedTaxonomyId, ResourceRepresentationType.TAXONOMY_INSTANCE, FetchLevel.ENTITY, false);
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			logger.error("An error occured while searching for taxonomy by taxonomyId", e);
			return null;
		}
	}
	
	private Topic findSelectedTopic() { 
		if (StringUtils.isBlank(selectedTopicId)){
			JSFUtilities.addMessage(null, "topic.isNull", null, FacesMessage.SEVERITY_WARN);
			return null;
		}
	
		try {
			return topicService.getTopic(selectedTopicId, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY, false);
		}
		catch (Exception e) {
			JSFUtilities.addMessage(null, "topic.isNull", null, FacesMessage.SEVERITY_WARN);
			logger.error("An error occured while searching for topic by topicId", e);
			return null;
		}
	}
	
	public void addNewTopicToTopic(Topic topic){
		
		if (topic == null){
			JSFUtilities.addMessage(null, "topic.isNull", null,  FacesMessage.SEVERITY_WARN);
			return;
		}
		
		tagEdit.createNewEditedTag(topic.getTaxonomy() ,topic);
		
		//Change topic owner to be SYSTEM
		tagEdit.getEditedTag().setOwner(getSystemRepositoryUser());
		
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TOPIC_EDITOR.getDynamicUIAreaPageComponent());
		
	}
	
	public void addNewTopicToTopic(){
		
		Topic topic = findSelectedTopic();
		
		if (topic == null) {
			return;
		}
		
		tagEdit.createNewEditedTag(topic.getTaxonomy() ,topic);
		
		//Change topic owner to be SYSTEM
		tagEdit.getEditedTag().setOwner(getSystemRepositoryUser());
		
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TOPIC_EDITOR.getDynamicUIAreaPageComponent());
		
	}
	
	private RepositoryUser getSystemRepositoryUser() {
		if (systemRepositoryUser == null){
			systemRepositoryUser = cmsUtilities.findRepositoryUserByUserId(CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID, JSFUtilities.getLocaleAsString());
			
			if (systemRepositoryUser == null){
				throw new CmsException("Could not find SYSTEM repository user");
			}
		}
		
		return systemRepositoryUser;
	}

	public void deleteTaxonomy(Taxonomy taxonomy){
		
		if (taxonomy == null){
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		try{
			
			taxonomyService.deleteTaxonomyTree(taxonomy.getId());
			
			JSFUtilities.addMessage(null, "taxonomy.cascadingDelete.succesful", null, FacesMessage.SEVERITY_INFO);
			
			Events.instance().raiseEvent(SeamEventNames.TAXONOMY_DELETED, taxonomy.getId());
			
			taxonomyEditor.reset();
		}
		catch(Exception e){
			JSFUtilities.addMessage(null, "taxonomy.cascadingDelete.error", null, FacesMessage.SEVERITY_ERROR);
			logger.error("",e);
			return;
		}
	}
	
	public void deleteTaxonomy(){

		if (StringUtils.isBlank(selectedTaxonomyId)){
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		try{
			
			taxonomyService.deleteTaxonomyTree(selectedTaxonomyId);
			
			JSFUtilities.addMessage(null, "taxonomy.cascadingDelete.succesful", null, FacesMessage.SEVERITY_INFO);
			
			Events.instance().raiseEvent(SeamEventNames.TAXONOMY_DELETED, selectedTaxonomyId);
			
			taxonomyEditor.reset();
		}
		catch(Exception e){
			JSFUtilities.addMessage(null, "taxonomy.cascadingDelete.error", null, FacesMessage.SEVERITY_ERROR);
			logger.error("",e);
			return;
		}
	}
	
	
	public void deleteTopic(Topic topic){
		
		if (topic == null){
			JSFUtilities.addMessage(null, "topic.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		try{
			topicService.deleteTopicTree(topic.getId());
			JSFUtilities.addMessage(null, "topic.cascadingDelete.succesful", new String[] {topic.getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString())}, FacesMessage.SEVERITY_INFO);
	
			if (topic.getParent() != null)
				Events.instance().raiseEvent(SeamEventNames.RELOAD_TOPIC_TREE_NODE, topic.getParent().getId());
			else
				Events.instance().raiseEvent(SeamEventNames.RELOAD_TAXONOMY_TREE_NODE, topic.getTaxonomy().getId());
			
			
		}
		catch(Exception e){
			logger.error("",e);
			JSFUtilities.addMessage(null, "topic.cascadingDelete.error", null, FacesMessage.SEVERITY_ERROR);
			return;
		}
		finally{
			
			tagEdit.reset();
			taxonomyEditor.reset();
		}
	}
	
	public void deleteTopic(){
		
		Topic topic = findSelectedTopic();
		
		if (topic == null) {
			return;
		}
		
		try{
			topicService.deleteTopicTree(topic.getId());
			JSFUtilities.addMessage(null, "topic.cascadingDelete.succesful", new String[] {topic.getAvailableLocalizedLabel(LocaleSelector.instance().getLocaleString())}, FacesMessage.SEVERITY_INFO);
	
			if (topic.getParent() != null)
				Events.instance().raiseEvent(SeamEventNames.RELOAD_TOPIC_TREE_NODE, topic.getParent().getId());
			else
				Events.instance().raiseEvent(SeamEventNames.RELOAD_TAXONOMY_TREE_NODE, topic.getTaxonomy().getId());
			
			
		}
		catch(Exception e){
			logger.error("",e);
			JSFUtilities.addMessage(null, "topic.cascadingDelete.error", null, FacesMessage.SEVERITY_ERROR);
			return;
		}
		finally{
			
			tagEdit.reset();
			taxonomyEditor.reset();
		}
	}
	
	public void addNewTopicToTaxonomy(Taxonomy taxonomy){
		
		if (taxonomy == null){
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		tagEdit.createNewEditedTag(taxonomy, null);
		
		//Change topic owner to be SYSTEM
		tagEdit.getEditedTag().setOwner(getSystemRepositoryUser());
		
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TOPIC_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	public void addNewTopicToTaxonomy(){
		
		Taxonomy taxonomy = findSelectedTaxonomy();
		
		if (taxonomy == null) {
			return;
		}
		
		tagEdit.createNewEditedTag(taxonomy, null);
		
		//Change topic owner to be SYSTEM
		tagEdit.getEditedTag().setOwner(getSystemRepositoryUser());
		
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TOPIC_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	public void addNewTaxonomy() {
		//Reset taxonomy tree
		Events.instance().raiseEvent(SeamEventNames.NEW_TAXONOMY_TREE);
		
		taxonomyEditor.addNewTaxonomy();
		tagEdit.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TAXONOMY_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	public void editTaxonomy(Taxonomy taxonomy) {
		
		if (taxonomy == null){
			JSFUtilities.addMessage(null, "taxonomy.isNull", null, FacesMessage.SEVERITY_WARN);
			return;
		}
		
		taxonomyEditor.editTaxonomy(taxonomy);
		tagEdit.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TAXONOMY_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	public void editTaxonomy() {
		
		Taxonomy taxonomy = findSelectedTaxonomy();
		
		if (taxonomy == null) {
			return;
		}
		
		taxonomyEditor.editTaxonomy(taxonomy);
		tagEdit.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TAXONOMY_EDITOR.getDynamicUIAreaPageComponent());
	}


	public void editTopic(Topic topic) {
		
		if (topic == null){
			JSFUtilities.addMessage(null, "topic.isNull", null,  FacesMessage.SEVERITY_WARN);
			return;
		}
		
		tagEdit.editTag_UIAction(topic);
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TOPIC_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	public void editTopic() {
		
		Topic topic = findSelectedTopic();
		
		if (topic == null) {
			return;
		}
		
		tagEdit.editTag_UIAction(topic);
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TOPIC_EDITOR.getDynamicUIAreaPageComponent());
	}
	
	public void addTag_UIAction() {
		tagEdit.addTag_UIAction();
		taxonomyEditor.reset();
		
		pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.TAG_EDITOR.getDynamicUIAreaPageComponent());
	}
	

	public void setTaxonomyService(TaxonomyService taxonomyService) {
		this.taxonomyService = taxonomyService;
	}

	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}
	
	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}

	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}

	public void setSelectedTaxonomyId(String selectedTaxonomyId) {
		this.selectedTaxonomyId = selectedTaxonomyId;
	}

	public void setSelectedTopicId(String selectedTopicId) {
		this.selectedTopicId = selectedTopicId;
	}
	
}
