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
package org.betaconceptframework.astroboa.console.jsf.portal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;

import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.console.jsf.dashboard.ContentObjectListBean;
import org.betaconceptframework.astroboa.console.seam.SeamEventNames;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("scheduledForContentAreasContentObjectList")
@Scope(ScopeType.CONVERSATION)
public class ScheduledForContentAreasContentObjectList extends ContentObjectListBean{

	private Date requestedAppearanceDate;
	
	private Topic selectedContentArea;
	
	@Override
	protected List<ContentObject> orderResults(List<ContentObject> results) {
		return results;
	}
	
	public List<CmsProperty<?,?>> filterScheduledContentAreasForSelectedContentArea(ContentObject contentObject){
		
		List<CmsProperty<?,?>> filteredScheduledContentAreas = new ArrayList<CmsProperty<?,?>>();
		
		if (contentObject != null && selectedContentArea != null){
			List<CmsProperty<?, ?>> scheduledContentAreas = contentObject.getComplexCmsRootProperty().getChildPropertyList("scheduledContentAreaListType.scheduledContentArea");
			
			for (CmsProperty<?,?> scheduledContentArea :scheduledContentAreas){
				CmsProperty<?, ?> contentAreaProperty = ((ComplexCmsProperty<?, ?>)scheduledContentArea).getChildProperty("contentArea");
				
				if (contentAreaProperty!=null && ! ((TopicReferenceProperty)contentAreaProperty).hasNoValues() &&
						((TopicReferenceProperty)contentAreaProperty).getSimpleTypeValue().getId().equals(selectedContentArea.getId())){
					filteredScheduledContentAreas.add(scheduledContentArea);
				}
			}
		}
		
		return filteredScheduledContentAreas;
	}
	public boolean isRequestedAppearanceDateBetweenDates(Calendar startDate, Calendar endDate){
		
		if (requestedAppearanceDate == null || 
				( startDate == null && endDate == null)){
			return false;
		}
		
		Calendar requestedAppearanceCalendar = DateUtils.clearTimeFromCalendar(DateUtils.toCalendar(requestedAppearanceDate));
		
		if (endDate != null){
			return ! requestedAppearanceCalendar.before(startDate) && ! endDate.before(requestedAppearanceCalendar);
		}
		
		return startDate.equals(requestedAppearanceCalendar);
		
	}

	public void removeContentAreaFromContentObject_UIAction(ContentObject contentObject, String scheduledContentAreaPath){
		
		try{
			contentObject.removeCmsProperty(scheduledContentAreaPath);
			
			contentService.save(contentObject, false, true, null);
			
			JSFUtilities.addMessage(null, "Η προγραμματισμένη περιοχή από το αντικείμενο διαγράφηκε με επιτυχία",  FacesMessage.SEVERITY_INFO);
			
			Events.instance().raiseEvent(SeamEventNames.UPDATE_NO_OF_CONTENT_OBJECT_REFERRERS);
			
			findScheduledContentObjectsForContentArea_UIAction(selectedContentArea);
			
		} catch (Exception e) {
			logger.error("Error while removing scheduled content area",e);
			JSFUtilities.addMessage(null, "Η διαγραφή της προγραμματισμένης περιοχής από το αντικείμενο απέτυχε.",  FacesMessage.SEVERITY_ERROR);
		}
	}
	
	public void findScheduledContentObjectsForSelectedContentArea_UIAction(){
		findScheduledContentObjectsForContentArea_UIAction(selectedContentArea);
	}
	
	public void findScheduledContentObjectsForContentArea_UIAction(Topic contentArea) {
		
		if (contentArea == null){
			selectedContentArea = null;
			requestedAppearanceDate = null;
			return;
		}
		
		// reset search criteria to begin a new search
		contentObjectCriteria = null;
		contentObjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
		contentObjectCriteria.getRenderProperties().renderValuesForLocale(JSFUtilities.getLocaleAsString());
		
		
		try {
			selectedContentArea = contentArea;
			
			/*
			 * we are looking only for content objects scheduled for requested date
			 */
			Calendar requestedAppearanceCalendar = DateUtils.clearTimeFromCalendar(DateUtils.toCalendar(requestedAppearanceDate));
			
			if (requestedAppearanceCalendar == null){
				requestedAppearanceCalendar = DateUtils.clearTimeFromCalendar(Calendar.getInstance());
				requestedAppearanceDate = requestedAppearanceCalendar.getTime();
			}

			
			//This criteria looks for all content objects which have an ASPECT or a predefined complex type named 'scheduledContentAreaListType'
			//In case a user has defined a complex property of type 'scheduledContentAreaListType' but provided a name other than 'scheduledContentAreaListType'
			//e.g. <xs:element name="scheduledContentAreas" type="scheduledContentAreaListType:scheduledContentAreaListType"/>
			//this property will not participate in the results of the query.
			
			// we are searching only for content objects which have a complex property named 'scheduledContentAreaListType'
			// 
			//contentObjectCriteria.addTopicIdEqualsCriterion(contentArea.getId(), contentArea.getTaxonomy().getName());
			//contentObjectCriteria.addCriterion(CriterionFactory.equals("scheduledContentAreaListType.scheduledContentArea.contentArea", contentArea.getId()));

		
			
			//appearanceEndDate is not null AND appearanceEndDate >= requestedDate AND appearanceStartDate <= requestedDate 
			//Criterion appearanceEndDateExistsCriterion = CriterionFactory.isNotNull("scheduledContentAreaListType.scheduledContentArea.appearanceEndDate");
			//Criterion appearanceStartDateLessEqualsThanRequestedDateCriterion = CriterionFactory.lessThanOrEquals("scheduledContentAreaListType.scheduledContentArea.appearanceStartDate", requestedAppearanceCalendar);
			//Criterion appearanceEndDateGreaterEqualsThanRequestedDateCriterion = CriterionFactory.greaterThanOrEquals("scheduledContentAreaListType.scheduledContentArea.appearanceEndDate", requestedAppearanceCalendar);
			//Criterion requestedDateBetweenStartAndEndAppearanceDateCriterion = CriterionFactory.and(appearanceStartDateLessEqualsThanRequestedDateCriterion, appearanceEndDateGreaterEqualsThanRequestedDateCriterion);
			
			//Criterion oneCondition = CriterionFactory.and(appearanceEndDateExistsCriterion, requestedDateBetweenStartAndEndAppearanceDateCriterion);
			
			//appearanceEndDate is null AND appearanceStartDate == requestedDate
			//Criterion appearanceEndDateDoesNotExistCriterion = CriterionFactory.isNull("scheduledContentAreaListType.scheduledContentArea.appearanceEndDate");
			//Criterion appearanceStartDateEqualsRequestedDateCriterion = CriterionFactory.equals("scheduledContentAreaListType.scheduledContentArea.appearanceStartDate", requestedAppearanceCalendar);

			//Criterion secondCondition = CriterionFactory.and(appearanceEndDateDoesNotExistCriterion, appearanceStartDateEqualsRequestedDateCriterion);
			
			//contentObjectCriteria.addCriterion(CriterionFactory.or(oneCondition, secondCondition));
			
			//contentObjectCriteria.addOrderProperty("scheduledContentAreaListType.scheduledContentArea.appearanceDate", Order.descending);
			
			//Call parent to execute query and create paged list data model
			//searchForContentObjectWithPagedResults();
			
			
			/*
			 * Unfortunately the above criteria will not bring the desired results due to the nature of the XPATH query
			 * that is created by the criteria :
			 * 
			 * //element ( *,bccms:structuredContentObject )  [  
					(   scheduledContentAreaListType/scheduledContentArea/@contentArea = 'THE_SELECTED_CONTENT_AREA_ID'   and 
					  (  
						(  scheduledContentAreaListType/scheduledContentArea/@appearanceEndDate  and  
							(   scheduledContentAreaListType/scheduledContentArea/@appearanceStartDate <= xs:dateTime('REQUESTED_DATE')  
							   and   scheduledContentAreaListType/scheduledContentArea/@appearanceEndDate >= xs:dateTime('REQUESTED_DATE') 
							)
					    )
					    or
					    (  not(scheduledContentAreaListType/scheduledContentArea/@appearanceEndDate)  
					       and   scheduledContentAreaListType/scheduledContentArea/@appearanceStartDate = xs:dateTime('2008-10-08T00:00:00.000+03:00')   
					    )
					  )
					)
			 	]  
			 * 
			 * With the above query Astroboa repository will match any content object which has a complex property scheduledContentAreaListType/scheduledContentArea
			 * whose simple property @contentArea refers to the selected content area AND
			 * 
			 * (
			 *  (	this contentObject has a complex property scheduledContentAreaListType/scheduledContentArea whose simple property @appearanceEndDate exists AND
			 *   this contentObject has a complex property scheduledContentAreaListType/scheduledContentArea whose simple property @appearanceStartDate is <= requestedDate AND 
			 *   this contentObject has a complex property scheduledContentAreaListType/scheduledContentArea whose simple property @appearanceEndDate is >= requestedDate
			 *   )
			 *   OR 
			 *   (
			 *   	this contentObject has a complex property scheduledContentAreaListType/scheduledContentArea whose simple property @appearanceEndDate does not exists AND
			 *   	this contentObject has a complex property scheduledContentAreaListType/scheduledContentArea whose simple property @appearanceStartDate == requestedDate
			 *   )
			 * )
			 * 
			 * Notice that the above query does not declare that the SAME scheduledContentAreaListType/scheduledContentArea must match provided criteria.
			 * 
			 * The correct query is
			 * //element ( *,bccms:structuredContentObject )/scheduledContentAreaListType/scheduledContentArea  [  
					 @contentArea = 'THE_SELECTED_CONTENT_AREA_ID'   and 
					  (   
					     (   @appearanceEndDate  and  (   @appearanceStartDate <= xs:dateTime('REQUESTED_DATE')
					                               and   @appearanceEndDate >= xs:dateTime('REQUESTED_DATE')
												  )
					     ) 
					   or  (  not(@appearanceEndDate)  and @appearanceStartDate = xs:dateTime('REQUESTED_DATE') )
					  )
					]   
					
             * which is cannot be created using Astroboa Criteria API 					
			 */
			String query = "//element ( *,bccms:structuredContentObject )/scheduledContentAreaListType/scheduledContentArea  [" +  
					" @contentArea = '"+contentArea.getId()+"'   and " +
					"  ( "+  
					"     (   @appearanceEndDate  and  (   @appearanceStartDate <= xs:dateTime('"+XPathUtils.formatForQuery(requestedAppearanceCalendar)+"')"+
					"                               and   @appearanceEndDate >= xs:dateTime('"+XPathUtils.formatForQuery(requestedAppearanceCalendar)+"')"+
												  ")"+
					"     ) "+
					"   or  (  not(@appearanceEndDate)  and @appearanceStartDate = xs:dateTime('"+XPathUtils.formatForQuery(requestedAppearanceCalendar)+"') )"+
					"  )" +
					"] order by @appearanceStartDate asc, @order asc";  
			
			searchForContentObjectWithPagedResultsUsingQuery(query);
			
		} catch (Exception e) {
			logger.error("Error while loading content objects ",e);
			JSFUtilities.addMessage(null, "object.list.message.contentObjectRetrievalError", null, FacesMessage.SEVERITY_ERROR);
		}
		
	}

	public Date getRequestedAppearanceDate() {
		return requestedAppearanceDate;
	}

	public void setRequestedAppearanceDate(Date requestedAppearanceDate) {
		this.requestedAppearanceDate = requestedAppearanceDate;
	}

	public Topic getSelectedContentArea() {
		return selectedContentArea;
	}

	public void setSelectedContentArea(Topic selectedContentArea) {
		this.selectedContentArea = selectedContentArea;
	}
	

	
}
