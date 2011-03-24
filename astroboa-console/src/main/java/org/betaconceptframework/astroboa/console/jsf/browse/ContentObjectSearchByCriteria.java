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
package org.betaconceptframework.astroboa.console.jsf.browse;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.TopicPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.model.query.Order;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.api.service.TopicService;
import org.betaconceptframework.astroboa.console.commons.CMSUtilities;
import org.betaconceptframework.astroboa.console.commons.ContentObjectStatefulSearchService;
import org.betaconceptframework.astroboa.console.commons.ContentObjectUIWrapperFactory;
import org.betaconceptframework.astroboa.console.commons.DublicateRepositoryUserExternalIdException;
import org.betaconceptframework.astroboa.console.jsf.ContentObjectList;
import org.betaconceptframework.astroboa.console.jsf.DynamicUIAreaPageComponent;
import org.betaconceptframework.astroboa.console.jsf.PageController;
import org.betaconceptframework.astroboa.console.jsf.SearchResultsFilterAndOrdering;
import org.betaconceptframework.astroboa.console.jsf.UIComponentBinding;
import org.betaconceptframework.astroboa.console.jsf.clipboard.Clipboard;
import org.betaconceptframework.astroboa.console.jsf.edit.XMLChar;
import org.betaconceptframework.astroboa.console.jsf.richfaces.LazyLoadingCmsDefinitionTreeNodeRichFaces;
import org.betaconceptframework.astroboa.console.jsf.search.ContentObjectListOfTypeQuery;
import org.betaconceptframework.astroboa.console.jsf.search.CriterionWrapper;
import org.betaconceptframework.astroboa.console.jsf.search.InvalidInputForCmsPropertyTypeException;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.AbstractUIBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelectionIndex;
import org.jboss.seam.core.Events;
import org.richfaces.event.DropEvent;

@Name("contentObjectSearchByCriteria")
@Scope(ScopeType.CONVERSATION)
/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectSearchByCriteria extends AbstractUIBean{

	private static final long serialVersionUID = 1L;
	
	//Statically Injected Beans
	private ContentObjectStatefulSearchService contentObjectStatefulSearchService;
	private ContentObjectList contentObjectList;
	private SearchResultsFilterAndOrdering searchResultsFilterAndOrdering;

	private CMSUtilities cmsUtilities;
	private PageController pageController;
	private TopicService topicService;
	private ContentService contentService;
	private ContentObjectUIWrapperFactory contentObjectUIWrapperFactory;
	private LoggedInRepositoryUser loggedInRepositoryUser;

	// Dynamically Injected Beans
	@In(required=false)
	UIComponentBinding uiComponentBinding;

	// this object holds all the search criteria upon which every searching/browsing for content objects in the repository is done
	ContentObjectCriteria contentObjectCriteria;

	@DataModel
	private List<CriterionWrapper> criterionWrappers;

	@DataModelSelectionIndex(value="criterionWrappers")
	private Integer selectedCriterionWrapperIndex;
	private DefinitionService definitionService;

	private boolean moveToClipboardUponSuccessfullSave;
	
	@In(create=true)
	ContentObjectListOfTypeQuery contentObjectListOfTypeQuery;
	
	//Specifies which tabs is active on Advanced Search Page
	private String activeTab;
	
	@In(create=true)
	private Clipboard clipboard;
	
	private CmsRepositoryEntityFactory cmsRepositoryEntityFactory;
	
	public void setTopicService(TopicService topicService) {
		this.topicService = topicService;
	}

	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

	public void setContentObjectUIWrapperFactory(
			ContentObjectUIWrapperFactory contentObjectUIWrapperFactory) {
		this.contentObjectUIWrapperFactory = contentObjectUIWrapperFactory;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	
	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}


	public void deleteCriterionWrapper_UIAction(){

		if (selectedCriterionWrapperIndex != null){
			if (criterionWrappers != null){
				criterionWrappers.remove(selectedCriterionWrapperIndex.intValue());
			}
		}

	}
	public void addCriterionWrapper(){
		if (criterionWrappers ==null)
			criterionWrappers = new ArrayList<CriterionWrapper>();

		criterionWrappers.add(new CriterionWrapper(topicService, contentService, contentObjectUIWrapperFactory, definitionService));
	}



	@Factory("criterionWrappers")
	public void initializeCriteriaWrappers(){
		criterionWrappers = new ArrayList<CriterionWrapper>();
	}

	public String initialize_UIAction(){

		activeTab = "criteriaTab";
		
		criterionWrappers = null;

		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		if (uiComponentBinding != null)
			uiComponentBinding.resetContentObjectTableScrollerComponent();

		clearResults();

		if (contentObjectListOfTypeQuery != null){
			contentObjectListOfTypeQuery.clear();
		}
		resetSearchResultsFilterAndOrdering();

		//Reset tree
		Events.instance().raiseEvent(SeamEventNames.NEW_CMS_DEFINITION_TREE_FOR_SEARCH,"");

		// set dynamic area to content object list presentation
		return pageController.loadPageComponentInDynamicUIArea(DynamicUIAreaPageComponent.ADVANCED_SEARCH.getDynamicUIAreaPageComponent());
	}

	private void resetSearchResultsFilterAndOrdering() {
		if (searchResultsFilterAndOrdering != null){
			searchResultsFilterAndOrdering.setSearchedText(null);
			searchResultsFilterAndOrdering.setSelectedContentObjectType(null);
			searchResultsFilterAndOrdering.setSelectedFromDate(null);
			searchResultsFilterAndOrdering.setSelectedOwnerFilter(null);
			searchResultsFilterAndOrdering.setSelectedToDate(null);
			searchResultsFilterAndOrdering.setSelectedContentObjectIdentifier(null);
			searchResultsFilterAndOrdering.setQueryName(null);
			searchResultsFilterAndOrdering.setQueryLocalizedLabel(null);
			searchResultsFilterAndOrdering.setQueryTitle(null);
			searchResultsFilterAndOrdering.setSelectedContentObjectSystemName(null);
			searchResultsFilterAndOrdering.setSelectedSearchModeFilter(SearchMode.SEARCH_ALL_NON_SYSTEM_BUILTIN_ENTITIES);
		}

	}

	public void saveCriteria_UIAction(){
		String queryName = searchResultsFilterAndOrdering.getQueryName();
		if (StringUtils.isBlank(queryName) || ! XMLChar.isValidNCName(queryName)){
			JSFUtilities.addMessage(null,"Δώστε ένα όνομα με λατινικούς χαρακτήρες χωρίς κενά στα κριτήρια της αναζήτησης για την αποθήκευσή τους", FacesMessage.SEVERITY_WARN);
			return;
		}

		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		// turn all user selections into search criteria
		try{
			setCriteriaFromUserSelection();
		}
		catch(InvalidInputForCmsPropertyTypeException e){
			JSFUtilities.addMessage(null,e.getMessage(), FacesMessage.SEVERITY_WARN);
			return ;
		}

		/* we set the result set size so that the fist 100 objects are returned.
		 * We do this search to get the number of matched content objects and fill the first page of results. 
		 */
		//contentObjectCriteria.getResultRowRange().setRange(0,99);
		contentObjectCriteria.setOffsetAndLimit(0,99);
		contentObjectCriteria.doNotCacheResults();

		// set required ordering only if no other order property has been specified
		setOrderToCriteria();


		//Create contentObject
		ContentObject queryContentObject = cmsRepositoryEntityFactory.newContentObjectForType("queryObject", JSFUtilities.getLocaleAsString());

		try{
			queryContentObject.setOwner(loggedInRepositoryUser.getRepositoryUser());
			
			String queryTitle =  StringUtils.isBlank(searchResultsFilterAndOrdering.getQueryTitle()) ? queryName : searchResultsFilterAndOrdering.getQueryTitle();
			String queryLocalizedLabel = StringUtils.isBlank(searchResultsFilterAndOrdering.getQueryLocalizedLabel()) ? queryName : searchResultsFilterAndOrdering.getQueryLocalizedLabel();
			
			//Add basic properties
			queryContentObject.setSystemName(queryName);

			Calendar today = GregorianCalendar.getInstance(JSFUtilities.getLocale());
			((CalendarProperty)queryContentObject.getCmsProperty("profile.created")).setSimpleTypeValue(today);
			((CalendarProperty)queryContentObject.getCmsProperty("profile.modified")).setSimpleTypeValue(today);

			((StringProperty)queryContentObject.getCmsProperty("profile.title")).addSimpleTypeValue(queryTitle);
			((StringProperty)queryContentObject.getCmsProperty("profile.language")).addSimpleTypeValue(JSFUtilities.getLocaleAsString());
			
			
			//Add localized label for query
			//By default English localized Label is querySystemName
			//If English is the locale and a localized label has been provided
			//then the default value will be replaced
			((StringProperty)queryContentObject.getCmsProperty("localizedLabels.en")).addSimpleTypeValue(queryName);
			
			//Check that its locale is supported
			if (queryContentObject.getComplexCmsRootProperty().isChildPropertyDefined("localizedLabels."+JSFUtilities.getLocaleAsString())){
				((StringProperty)queryContentObject.getCmsProperty("localizedLabels."+JSFUtilities.getLocaleAsString())).setSimpleTypeValue(queryLocalizedLabel);
			}
			
			// set the accessibility of the query object
			// The default will be applied, that is read by all, write / delete by None, Tagged by all
			// Load Accessibility properties
			StringProperty canBeReadBy = (StringProperty)queryContentObject.getCmsProperty("accessibility.canBeReadBy"); 
			StringProperty canBeUpdatedBy = (StringProperty)queryContentObject.getCmsProperty("accessibility.canBeUpdatedBy"); 
			StringProperty canBeDeletedBy = (StringProperty)queryContentObject.getCmsProperty("accessibility.canBeDeletedBy"); 
			StringProperty canBeTaggedBy = (StringProperty) queryContentObject.getCmsProperty("accessibility.canBeTaggedBy");
			canBeReadBy.addSimpleTypeValue(ContentAccessMode.ALL.toString());
			canBeUpdatedBy.addSimpleTypeValue(ContentAccessMode.NONE.toString());
			canBeDeletedBy.addSimpleTypeValue(ContentAccessMode.NONE.toString());
			canBeTaggedBy.addSimpleTypeValue(ContentAccessMode.ALL.toString());
			
			//This cast is necessary since ContentObjectCriteria interface does not extends Serializable
			String queryString = contentObjectCriteria.getXPathQuery();
			
			((StringProperty)queryContentObject.getCmsProperty("queryString")).addSimpleTypeValue(queryString);

			queryContentObject = contentService.saveContentObject(queryContentObject, false);

			JSFUtilities.addMessage(null,"Τα κριτήρια αποθηκεύτηκαν. Μπορείτε να εμπλουτίσετε με περισσότερα στοιχεία το καινούριο αντικείμενο, " +
					"επιλέγοντας επεξεργασία αντικειμένου. (Αντικείμενα περιεχομένου > "+
					queryContentObject.getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString())+" > "
					+today.get(Calendar.YEAR)+ " > "+(today.get(Calendar.MONTH)+1)+ " > "+
					today.get(Calendar.DAY_OF_MONTH)+ ")", FacesMessage.SEVERITY_INFO);

			Events.instance().raiseEvent(SeamEventNames.CONTENT_OBJECT_ADDED, new Object[]{
					queryContentObject.getContentObjectType(), queryContentObject.getId(), 
					((CalendarProperty)queryContentObject.getCmsProperty("profile.created")).getSimpleTypeValue()});

			if (isMoveToClipboardUponSuccessfullSave()){
				clipboard.copyContentObjectToClipboard_UIAction(
						queryContentObject.getId(),
						queryContentObject.getSystemName(), 
						queryTitle,
						queryContentObject.getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()),
						queryContentObject.getContentObjectType());
			}

		}
		catch(Exception e){
			logger.error("", e);
			JSFUtilities.addMessage(null,"Η αποθήκευση των κριτηρίων απέτυχε", FacesMessage.SEVERITY_WARN);
			return ;
		}

	}


	public String searchForContentWithPagedResultsUsingQueryContentObject_UIACtion(String queryString){
		
		if (StringUtils.isBlank(queryString)){
			JSFUtilities.addMessage(null,"Δεν υπάρχει η ερωταπόκριση" ,
					FacesMessage.SEVERITY_WARN);
			clearResults();
			return null;
		}
		
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		contentObjectCriteria.setXPathQuery(queryString);
		
		contentObjectCriteria.setOffsetAndLimit(0,99);
		
		// now we are ready to run the query
		int resultSetSize = 0;
		try {
			resultSetSize = contentObjectStatefulSearchService
			.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), 100);

			if (resultSetSize > 0) {

				contentObjectList
				.setContentObjectListHeaderMessage(
						JSFUtilities.getParameterisedStringI18n("content.search.contentObjectListHeaderMessageForSearchByCriteria",
								new String[]{String.valueOf(resultSetSize)}));
				
				contentObjectList.setLabelForFileGeneratedWhenExportingListToXml(JSFUtilities.getLocalizedMessage("advancedSearch.search.panel.header", null));

				if (uiComponentBinding != null){
					uiComponentBinding.resetContentObjectTableScrollerComponent();
				}

			}
			else{
				JSFUtilities.addMessage(null,"application.unknown.error.message",
						new String[] { "Δεν Βρέθηκαν αντικείμενα που πληρούν τα κριτήρια που θέσατε" },
						FacesMessage.SEVERITY_WARN);
				clearResults();
			}
		} catch (Exception e) {
			getLogger().error("Error while loading content objects ", e);
			JSFUtilities.addMessage(null, "content.search.contentObjectRetrievalError", null,FacesMessage.SEVERITY_ERROR);
			clearResults();
		}

		// set dynamic area to content object list presentation
		pageController.setDynamicUIAreaCurrentPageComponent(DynamicUIAreaPageComponent.ADVANCED_SEARCH.getDynamicUIAreaPageComponent());

		return null;
	}
	
	// search with the criteria supplied by the user in the search form
	public String searchForContentWithPagedResults_UIAction() {


		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();

		// turn all user selections into search criteria
		try{
			setCriteriaFromUserSelection();
		}
		catch(InvalidInputForCmsPropertyTypeException e){
			JSFUtilities.addMessage(null,e.getMessage(), FacesMessage.SEVERITY_ERROR);
			clearResults();
			return null;
		}

		/* we set the result set size so that the fist 100 objects are returned.
		 * We do this search to get the number of matched content objects and fill the first page of results. 
		 */
		//contentObjectCriteria.getResultRowRange().setRange(0,99);
		contentObjectCriteria.setOffsetAndLimit(0,99);

		// set required ordering only if no other order property has been specified
		setOrderToCriteria();

		// now we are ready to run the query
		int resultSetSize = 0;
		try {
			resultSetSize = contentObjectStatefulSearchService
			.searchForContentWithPagedResults(contentObjectCriteria, true, JSFUtilities.getLocaleAsString(), 100);

			if (resultSetSize > 0) {

				contentObjectList
				.setContentObjectListHeaderMessage(
						JSFUtilities.getParameterisedStringI18n("content.search.contentObjectListHeaderMessageForSearchByCriteria",
								new String[]{String.valueOf(resultSetSize)}));
				
				contentObjectList.setLabelForFileGeneratedWhenExportingListToXml(JSFUtilities.getLocalizedMessage("advancedSearch.search.panel.header", null));

				if (uiComponentBinding != null){
					uiComponentBinding.resetContentObjectTableScrollerComponent();
				}

			}
			else{
				JSFUtilities.addMessage(null,"content.search.noContentObjectsRelatedToCriteria",
						null,
						FacesMessage.SEVERITY_INFO);
				clearResults();
			}
		} catch (Exception e) {
			getLogger().error("Error while loading content objects ", e);
			JSFUtilities.addMessage(null, "content.search.contentObjectRetrievalError", null,FacesMessage.SEVERITY_ERROR);
			clearResults();
		}

		// set dynamic area to content object list presentation
		pageController.setDynamicUIAreaCurrentPageComponent(DynamicUIAreaPageComponent.ADVANCED_SEARCH.getDynamicUIAreaPageComponent());

		return null;

	}

	private void setOrderToCriteria() {
		if (MapUtils.isEmpty(contentObjectCriteria.getOrderProperties())){
			if (searchResultsFilterAndOrdering.getSelectedResultsOrder() != null) {
				contentObjectCriteria.addOrderProperty(
						"profile.modified",
						Order.valueOf(searchResultsFilterAndOrdering.getSelectedResultsOrder()));
			}
			else {
				contentObjectCriteria.addOrderProperty(
						"profile.modified",
						Order.descending);
			}
		}
	}

	private void clearResults() {
		contentObjectList.resetViewAndStateBeforeNewContentSearchResultsPresentation();
		contentObjectStatefulSearchService.setSearchResultSetSize(0);
		contentObjectStatefulSearchService.setReturnedContentObjects(null);
	}


	private void setCriteriaFromUserSelection() throws InvalidInputForCmsPropertyTypeException {

		//set search mode
		if (searchResultsFilterAndOrdering.getSelectedSearchModeFilter() != null){
			contentObjectCriteria.setSearchMode(searchResultsFilterAndOrdering.getSelectedSearchModeFilter());
		}
		
		// set content type criteria by user selection
		if (StringUtils.isNotBlank(searchResultsFilterAndOrdering.getSelectedContentObjectType())) 
			contentObjectCriteria.addContentObjectTypeEqualsCriterion(searchResultsFilterAndOrdering.getSelectedContentObjectType());

		// set owner criteria by user selection
		setCriteriaFromOwnerSelection();

		// set date criteria by user selection
		setCriteriaFromDateSelection();


		//set content object identifier
		if (StringUtils.isNotBlank(searchResultsFilterAndOrdering.getSelectedContentObjectIdentifier())){
			contentObjectCriteria.addIdEqualsCriterion(searchResultsFilterAndOrdering.getSelectedContentObjectIdentifier());
		}


		//set content object system name
		if (StringUtils.isNotBlank(searchResultsFilterAndOrdering.getSelectedContentObjectSystemName())){
			contentObjectCriteria.addSystemNameContainsCriterion(searchResultsFilterAndOrdering.getSelectedContentObjectSystemName());
		}

		// set text search criteria by user selection
		if (StringUtils.isNotBlank(searchResultsFilterAndOrdering.getSearchedText())){
			contentObjectCriteria.addFullTextSearchCriterion(searchResultsFilterAndOrdering.getSearchedText());
		}

		//Used defined criteria
		if (CollectionUtils.isNotEmpty(criterionWrappers)){
			for (CriterionWrapper criterionWrapper: criterionWrappers){

				Criterion criterion = criterionWrapper.getCriterion();
				if (criterion != null){
					contentObjectCriteria.addCriterion(criterion);
				}

				//Check if it participates in ordering
				if (criterionWrapper.getOrder() != null && StringUtils.isNotBlank(criterionWrapper.getPropertyPath())){
					contentObjectCriteria.addOrderProperty(criterionWrapper.getPropertyPath(), criterionWrapper.getOrder());
				}

			}
		}

	}

	private void setCriteriaFromDateSelection() {
		Date selectedToDate = searchResultsFilterAndOrdering.getSelectedToDate();

		Calendar toDate = null;
		if (selectedToDate != null){
			toDate = adjustDate(selectedToDate);
		}

		Calendar fromDate = DateUtils.toCalendar(searchResultsFilterAndOrdering.getSelectedFromDate());

		if (toDate != null || fromDate != null){
			contentObjectCriteria.addCriterion(CriterionFactory.between("profile.modified", fromDate, toDate));
		}

	}

	private Calendar adjustDate(Date selectedDate) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(selectedDate);

		// Adjust toDate to accommodate the full day time
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.MILLISECOND, 999);

		return cal;
	}

	private void setCriteriaFromOwnerSelection() {
		RepositoryUser resultRepositoryUser;

		Integer selectedOwnerFilter = searchResultsFilterAndOrdering.getSelectedOwnerFilter();
		try {
			if (selectedOwnerFilter != null)
				if (selectedOwnerFilter == 1) { // user has selected to
					// search only for her
					// owned content objects
					resultRepositoryUser = cmsUtilities.findLoggedInRepositoryUser(JSFUtilities.getLocaleAsString());
					// we should always find ONLY ONE repository user with the
					// provided id. If dublicates are found the above method
					// will throw an exception
					if (resultRepositoryUser != null) {
						contentObjectCriteria.addOwnerIdEqualsCriterion(resultRepositoryUser.getId());
					} else { // user was not found reset filter to default
						// value
						JSFUtilities
						.addMessage(
								null,
								"The logged in user could not be retreived from repository. The user has not been registered in the Repository. User registration happens automatically upon first content publication. Please publish a content object in order to be registered and be able to use the filter. Until then the filter has been reset to its default value",
								FacesMessage.SEVERITY_WARN);

						searchResultsFilterAndOrdering.setSelectedOwnerFilter(3);
					}
				}
// The new implementation astroboa implementation is IDM agnostic. The astroboa module may be used or any other external Identity Management Module.
// The interpretation of Groups / Roles should be different when an external identity system is utilized.
// We should define a way to recognize which user Groups / Roles are relevant with the cms.
// Otherwise there is a possibility of high performance penalty if the algorithm tries to calculate all the possible members of user groups and then
// try to find which of these users are owners of content in the repository.
// For example if the user belongs to a group which is irrelevant with the content repository and the group contains 2000 members then the algorithm will create criteria
// that contain 2.000 user ids none of which has put any content in the repository.
// If astroboa provided IDM module is utilized all the user groups are relevant to content authoring but for a huge LDAP store utilized across an organization 
// this may not be the case
// So at this version searching by user group will be disabled and revisited in the next version
//				else if (selectedOwnerFilter == 2) {
//					/*
//					 * user has selected to search only for content objects
//					 * owned by her group. So get userIds of users that belong
//					 * to the same group(s) as the currently logged in user and
//					 * are cms users who may have published to the repository
//					 * (have the role ROLE_CMS_EDITOR)
//					 */
//					List<Long> samePersonGroupMembers = personService
//					.loadSamePersonGroupMembersWithSpecifiedRole(
//							personId, "ROLE_CMS_VIEWER");
//
//					/*
//					 * now for each of these same group editors we will find the
//					 * relevant Repository User in order to populate the filter
//					 * if we do not find a relevant Repository user it means
//					 * that this user is a potencial CMS Repository Editor
//					 * according to her role but she has never published
//					 * anything. So we do not include her in the filter
//					 */
//					if (samePersonGroupMembers != null) {
//						List<String> ownerIds = new ArrayList<String>();
//						for (Long personIdInGroup : samePersonGroupMembers) {
//							resultRepositoryUser = cmsUtilities.findRepositoryUserByUserId(personIdInGroup.toString(), JSFUtilities.getLocaleAsString());
//							// we should always find ONLY ONE repository user
//							// with the provided id. If dublicates are found the
//							// above method will throw an exception
//							if (resultRepositoryUser != null)
//								ownerIds.add(resultRepositoryUser.getId());
//
//							/*
//							 * if no repository user was found who has as
//							 * External Id the person id there is no problem. It
//							 * means that the user with this id has never
//							 * published anything in the repository A user is
//							 * registered as a repository user if she publishes
//							 * content or is creator / contributor of content.
//							 * So in this case we need to do nothing (i.e. we do
//							 * not include the user in the UUIDs filter list
//							 */
//						}
//
//						if (CollectionUtils.isNotEmpty(ownerIds))
//							contentObjectCriteria.addOwnerIdsEqualsAnyCriterion(ownerIds);
//
//					} else {// no members found in user groups. This is a
//						// potencial problem since at least the user herself
//						// should be in cms user groups
//						// setOwnerUUIDsFilterList(null);
//						JSFUtilities
//						.addMessage(
//								null,
//								"content.search.contentObjectRetrievalError",
//								new String[] { "No users belonging to the same group(s) as the logged in user found. This is a potencial problem since at least your account should be in your cms user groups. Please report this to help desk." },
//								FacesMessage.SEVERITY_WARN);
//					}
//
//				}

			/*
			 * we do not the following if filter value is 3, if not selected we
			 * assume is 3 else if (getSelectedOwnerFilter() == 3) { // user has
			 * selected to search in all the repository - the default
			 * //setOwnerUUIDsFilterList(null); } else { // we do not support
			 * any other value - keep the default (3) and generate a message
			 * //setOwnerUUIDsFilterList(null); // generate the
			 * appropriate message JSFUtilities.addMessage(null,
			 * "application.unknown.error.message", new String[] { "The selected value
			 * for filtering by owner: " + getSelectedOwnerFilter() + " is not
			 * supported. Only the values 1(only mine), 2(only my group), 3(any
			 * owner) are allowed. This is possibly a problem of the User
			 * Interface that allowed such a value to be set. Consult Help Desk" },
			 * FacesMessage.SEVERITY_INFO);
			 */
		}  catch (DublicateRepositoryUserExternalIdException e) { // OOPS!! we
			// found
			// more than
			// one user
			// with the
			// same id.
			// Some
			// problem
			// with the
			// repository
			// exists
			JSFUtilities
			.addMessage(
					null,
					"More than one users with the id of the logged in user have be retreived from repository. There is a potencial problem with the repository. Please report this to the help desk. In the meanwhile the filter has been reset to its default",
					FacesMessage.SEVERITY_WARN);

			searchResultsFilterAndOrdering.setSelectedOwnerFilter(3);

		} catch (CmsException e) { // some problem occured at the repository
			logger.error("One or more repository users could not be retreived from the repository. An error occured.",
					e);
			// while retreiving a repository user
			JSFUtilities
			.addMessage(
					null,
					"One or more repository users could not be retreived from the repository.",
					FacesMessage.SEVERITY_WARN);

			searchResultsFilterAndOrdering.setSelectedOwnerFilter(3); // reset to default filter to allow the
			// user to proceed working despite the
			// error
		} catch (Exception e) {
			logger.error("The users belonging to the same group(s) as the logged in user could not be retreived from crm or the repository. An error occured.",e);
			JSFUtilities
			.addMessage(
					null,
					"The users belonging to the same group(s) as the logged in user could not be retreived from crm or the repository. An error occured.",
							FacesMessage.SEVERITY_WARN);
			searchResultsFilterAndOrdering.setSelectedOwnerFilter(3); // reset to default filter to allow the
			// user to proceed working despite the
			// error
		}
	}

	public void generateDefinitionTree_UIAction(ValueChangeEvent vce){
		String selectedContentType = (String) vce.getNewValue();

		Events.instance().raiseEvent(SeamEventNames.NEW_CMS_DEFINITION_TREE_FOR_SEARCH,selectedContentType);

		initializeCriteriaWrappers();

	}

	public SearchResultsFilterAndOrdering getSearchResultsFilterAndOrdering() {
		return searchResultsFilterAndOrdering;
	}


	public void setSearchResultsFilterAndOrdering(
			SearchResultsFilterAndOrdering searchResultsFilterAndOrdering) {
		this.searchResultsFilterAndOrdering = searchResultsFilterAndOrdering;
	}


	public void setContentObjectStatefulSearchService(
			ContentObjectStatefulSearchService contentObjectStatefulSearchService) {
		this.contentObjectStatefulSearchService = contentObjectStatefulSearchService;
	}


	public void setContentObjectList(
			ContentObjectList contentObjectList) {
		this.contentObjectList = contentObjectList;
	}


	public void setContentObjectCriteria(ContentObjectCriteria contentObjectCriteria) {
		this.contentObjectCriteria = contentObjectCriteria;
	}

	public void createACriterionWrapperFromDraggedDefinition_Listener(DropEvent dropEvent){
		LazyLoadingCmsDefinitionTreeNodeRichFaces draggedDefinitionNode = (LazyLoadingCmsDefinitionTreeNodeRichFaces)dropEvent.getDragValue();

		addCriterionWrapper();

		//Get the last criterion wrapper
		CriterionWrapper criterionWrapper = criterionWrappers.get(criterionWrappers.size()-1);

		String outcome = addDefinitionToCriterionWrapper(draggedDefinitionNode, criterionWrapper);

		if ("error".equals(outcome)){
			//remove last added criterion wrapper
			criterionWrappers.remove(criterionWrappers.size()-1);
		}

	}

	public void addDraggedDefinitionToCriterion_Listener(DropEvent dropEvent){
		LazyLoadingCmsDefinitionTreeNodeRichFaces draggedDefinitionNode = (LazyLoadingCmsDefinitionTreeNodeRichFaces)dropEvent.getDragValue();

		CriterionWrapper criterionWrapper = (CriterionWrapper)dropEvent.getDropValue();

		addDefinitionToCriterionWrapper(draggedDefinitionNode, criterionWrapper);
	}

	private String addDefinitionToCriterionWrapper(LazyLoadingCmsDefinitionTreeNodeRichFaces draggedDefinitionNode, CriterionWrapper criterionWrapper){
		try{


			if (draggedDefinitionNode == null || draggedDefinitionNode.getCmsDefinition() == null || 
					( ! (draggedDefinitionNode.getCmsDefinition() instanceof SimpleCmsPropertyDefinition) &&
							! (draggedDefinitionNode.getCmsDefinition() instanceof ComplexCmsPropertyDefinition))){
				JSFUtilities.addMessage(null, "Δεν επιλέχθηκε κανένα στοιχείο" , FacesMessage.SEVERITY_WARN);
				return "error";
			}


			if (criterionWrapper == null){
				JSFUtilities.addMessage(null, "Δεν υπάρχει κριτήριο ", FacesMessage.SEVERITY_ERROR);
				return "error";
			}

			criterionWrapper.clear();

			String defintionPath = draggedDefinitionNode.getDefinitionPathForQuery();

			if (StringUtils.isNotBlank(defintionPath)){
				//Remove from full path content type name
				//Replace administrativeMetaDataType, accessibilityType, webPublicationType, statisticType, workflowType
				//with profile, accessibility, webPublication, statistic, workflow respectively 
				//because they are now built in properties
				//and they cannot be attached to a content object.
				//this is due to a bug of web console and this is only a temporary
				//solution until a new advanced search console will be implemented
				if (defintionPath.startsWith("administrativeMetadataType")){
					defintionPath = defintionPath.replace("administrativeMetadataType", "profile");
				}
				else if (defintionPath.startsWith("accessibilityType")){
					defintionPath = defintionPath.replace("accessibilityType", "accessibility");
				}
				else if (defintionPath.startsWith("webPublicationType")){
					defintionPath = defintionPath.replace("webPublicationType", "webPublication");
				}
				else if (defintionPath.startsWith("statisticType")){
					defintionPath = defintionPath.replace("statisticType", "statistic");
				}
				else if (defintionPath.startsWith("workflowType")){
					defintionPath = defintionPath.replace("workflowType", "workflow");
				}
				
				criterionWrapper.setPropertyPath(defintionPath);
				criterionWrapper.setPropertyValueType(draggedDefinitionNode.getCmsDefinition().getValueType());

				criterionWrapper.setPropertyLocalizedLabel(draggedDefinitionNode.getDescription());
				
				if (draggedDefinitionNode.getCmsDefinition() instanceof TopicPropertyDefinition){
					criterionWrapper.setAcceptedTaxonomies(((TopicPropertyDefinition)draggedDefinitionNode.getCmsDefinition()).getAcceptedTaxonomies());
				}
				else if (draggedDefinitionNode.getCmsDefinition() instanceof ContentObjectPropertyDefinition){
					criterionWrapper.setAcceptedContentTypes(((ContentObjectPropertyDefinition)draggedDefinitionNode.getCmsDefinition()).getExpandedAcceptedContentTypes());
				}

			}
			else{

				JSFUtilities.addMessage(null, "Δεν υπάρχει κριτήριο ", FacesMessage.SEVERITY_ERROR);
				return "error";
			}

			return "success";

		}
		catch(Exception e){
			JSFUtilities.addMessage(null, "Το στοιχείο δεν προστέθηκε στο κριτήριο", FacesMessage.SEVERITY_ERROR);
			getLogger().error("PropertyPath could not be attached to criterion",e);
			return "error";
		}

	}


	public void setPageController(PageController pageController) {
		this.pageController = pageController;
	}

	public boolean isMoveToClipboardUponSuccessfullSave() {
		return moveToClipboardUponSuccessfullSave;
	}

	public void setMoveToClipboardUponSuccessfullSave(
			boolean moveToClipboardUponSuccessfullSave) {
		this.moveToClipboardUponSuccessfullSave = moveToClipboardUponSuccessfullSave;
	}

	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}

	public String getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(String activeTab) {
		this.activeTab = activeTab;
	}

	public void setCmsRepositoryEntityFactory(
			CmsRepositoryEntityFactory cmsRepositoryEntityFactory) {
		this.cmsRepositoryEntityFactory = cmsRepositoryEntityFactory;
	}

}
