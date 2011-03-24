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
package org.betaconceptframework.astroboa.portal.form;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BooleanProperty;
import org.betaconceptframework.astroboa.api.model.CalendarProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicProperty;
import org.betaconceptframework.astroboa.api.model.definition.CmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.portal.utility.CalendarUtils;
import org.betaconceptframework.astroboa.portal.utility.CmsUtils;
import org.betaconceptframework.astroboa.portal.utility.PortalStringConstants;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.faces.Renderer;
import org.jboss.seam.international.LocaleSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

public abstract class AbstractForm<T extends AstroboaClient> {
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	private Map<String, Object> formParams = new HashMap<String, Object>();
	
	/*
	 * This map can store values which do not correspond to content object properties, 
	 * like password confirmation input field. It is not used by form infrastructure.
	 * 
	 * It may be used from implementor as a container for any key, value pair
	 */
	private Map<String, Object> tempParams = new HashMap<String, Object>();
	
	@Out(required=false)
	protected ContentObject formContentObject = null;
	
	
	@In(value="#{captcha.challengeText}", required=false)
	private String challengeText;
	
	@In(value="#{captcha.response}", required=false)
	private String challengeResponse;
	
	@In(create=true) 
	protected Renderer renderer;
	
	@In
	protected LocaleSelector localeSelector;
	
	protected List<String> formSubmissionMessageList = new ArrayList<String>();
	
	private Boolean successfulFormSubmission;
	
	@In(create=true)
	protected CmsUtils cmsUtils;
	
	@In(create=true)
	protected CalendarUtils calendarUtils;
	
	private final static String STATUS_OF_USER_SUBMITTED_FORM = "submittedByExternalUser";
	private final static String WORKFLOW_FOR_USER_SUBMITTED_FORM = "webPublishing";
	
	public String submitForm() {
		formContentObject = null;
		
		try {
			formSubmissionMessageList = new ArrayList<String>();
			
			checkChallengeResponsePhase();
				
			formContentObject = getFormsRepositoryClient().getCmsRepositoryEntityFactory().newContentObjectForType(getFormType(), JSFUtilities.getLocaleAsString());
			
			applyDefaultValuesPhase(formParams, formContentObject);

			validateAndApplyFormValuesPhase(formParams, formContentObject);

			
			// run custom form post processing code before saving
			postValidateAndApplyValuesPhase(formParams, formContentObject);
			
			savePhase(formContentObject);
			
			postSavePhase(formParams, formContentObject);
			
			//return "/successfulFormSubmission.xhtml";
			
			successfulFormSubmission = true;
			return null;
			
		}
		catch (ChallengeValidationException e) {
			logger.info("An null or invalid form challenge response was provided");
			return null;
		}
		catch (FormValidationException e) {
			logger.warn("An error occured during validation", e);
				
			if (CollectionUtils.isEmpty(formSubmissionMessageList))
			{
				formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.unknown.error", null));
			}
			
			return null;
		}
		catch (Exception e) {
			logger.error("Failed to save user submitted form", e);
			formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.unknown.error", null));

			if (formContentObject != null) {
				formContentObject.setId(null);
			}
			return null;
		}
		
		
	}
	
	
	private void savePhase(ContentObject formContentObject) throws Exception {
		getFormsRepositoryClient().getContentService().save(formContentObject, false, true, null);
		formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.successful.submission", null));
		formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.successful.submission.key.message", new String[]{formContentObject.getId()}));
		formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.successful.submission.info.message", null));
	}
	
	private void checkChallengeResponsePhase() throws ChallengeValidationException{
		if (enableCheckForChallengeRespone())
		{
			if (StringUtils.isBlank(challengeResponse)) {
				//String message = "Δεν συμπληρώσατε τα γράμματα που εμφανίζονται στην εικόνα. Παρακαλώ συμπληρώστε τα γράμματα που βλέπετε στην εικόνα.";
				formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.empty.challenge.response", null));
				throw new ChallengeValidationException("Null Challenge Responce");
			}

			if (challengeText != null && challengeResponse != null && !challengeResponse.equals(challengeText)) {
				//String message = "Δεν συμπληρώσατε σωστά τα γράμματα που εμφανίζονται στην εικόνα. Παρακαλώ συμπληρώστε τα γράμματα που βλέπετε στην εικόνα.";
				formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.empty.challenge.response", null));
				throw new ChallengeValidationException("challengeText=" + challengeText + " ,challenge responce=" + challengeResponse + " - No Match");
			}
		}
		
	}
	
	private void validateAndApplyFormValuesPhase(Map<String,Object> formParams, ContentObject formContentObject) throws Exception {
		for (String formParameter : formParams.keySet()) {
			applyFormParameterToContentObjectProperty(formContentObject, formParams, formParameter);
		}
	}
	
	
	private void applyDefaultValuesPhase(Map<String,Object> formParams, ContentObject formContentObject) throws Exception {
		StringProperty statusProperty = (StringProperty) formContentObject.getCmsProperty("profile.contentObjectStatus");
		statusProperty.setSimpleTypeValue(STATUS_OF_USER_SUBMITTED_FORM);
		
		StringProperty workflowProperty = (StringProperty) formContentObject.getCmsProperty("workflow.managedThroughWorkflow");
		workflowProperty.setSimpleTypeValue(WORKFLOW_FOR_USER_SUBMITTED_FORM);

	//	StringProperty commentsProperty = (StringProperty) formContentObject.addChildCmsPropertyTemplate("internalComments");
	//	commentsProperty.setSimpleTypeValue("External Submitter Name: " + firstName + " " + lastName + "\n" + 
	//			"email: " + email + "\n" +
	//			"telephone: " + telephone + "\n" +
	//			"Organization Name: " + organizationName);
		
		//Form submitter is ALWAYS SYSTEM Repository User
		RepositoryUser formSubmitter = getFormsRepositoryClient().getRepositoryUserService().getSystemRepositoryUser();
		
		if (formSubmitter == null) {
			logger.error("Retrieved a null User for form submitter. Cannot proceed to save submitted event");
			formSubmissionMessageList.add(JSFUtilities.getLocalizedMessage("form.empty.challenge.response", null));
			throw new Exception("Retrieved a null User for form submitter. Cannot proceed to save submitted event");
		}
		
		formContentObject.setOwner(formSubmitter);
		
		Calendar now = GregorianCalendar.getInstance(JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
		CalendarProperty createdProperty = (CalendarProperty) formContentObject.getCmsProperty("profile.created");
		createdProperty.setSimpleTypeValue(now);
		CalendarProperty modifiedProperty = (CalendarProperty) formContentObject.getCmsProperty("profile.modified");
		modifiedProperty.setSimpleTypeValue(now);
		
		// supply a default title, language and author
		// these may be overwritten by formPostProcessing method
		StringProperty titleProperty = (StringProperty) formContentObject.getCmsProperty("profile.title");
		titleProperty.setSimpleTypeValue(formContentObject.getTypeDefinition().getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()) + " " + calendarUtils.convertDateToString(now.getTime(), PortalStringConstants.DATE_FORMAT_PATTERN));
		
		StringProperty languageProperty = (StringProperty) formContentObject.getCmsProperty("profile.language");
		languageProperty.addSimpleTypeValue(JSFUtilities.getLocaleAsString());
		
		StringProperty creatorProperty = (StringProperty) formContentObject.getCmsProperty("profile.creator");
		creatorProperty.addSimpleTypeValue("anonymous");
		
		applyAccessibilityPropertiesPhase(formContentObject);
	}
	
	private void applyFormParameterToContentObjectProperty(ContentObject formContentObject, Map<String, Object> formParameters, String formParameter) throws Exception, FormValidationException {
		
		CmsPropertyDefinition formParameterDefinition = 
			(CmsPropertyDefinition) getFormsRepositoryClient().getDefinitionService().getCmsDefinition(formContentObject.getContentObjectType()+"."+formParameter, ResourceRepresentationType.DEFINITION_INSTANCE);
		
		// check if field is defined
		if (formParameterDefinition == null) {
			String errorMessage = "For the provided form parameter: " + 
				formParameter + 
				", there is no corresponding property in the ContentObject Type:" + 
				formContentObject.getContentObjectType() + 
				"which models the form";
			throw new FormValidationException(errorMessage);
		}
		
		Object formParameterValue = formParameters.get(formParameter);
		
		String requiredFieldErrorMessage = "Το πεδίο " + "'" + formParameterDefinition.getDisplayName().getLocalizedLabelForLocale(JSFUtilities.getLocaleAsString()) + "'" +
			" είναι υποχρεωτικό. Παρακαλώ βάλτε τιμή";
		
		switch (formParameterDefinition.getValueType()) {
		case String:
		{
			// check if form value is of the appropriate type
			if (!(formParameterValue instanceof String)) {
				throw new FormValidationException("Invalid form field value type for property:" + formParameter + " A String value was expected");
			}
			
			// check if no value has been provided for a mandatory property
			if (formParameterDefinition.isMandatory() && StringUtils.isBlank((String) formParameterValue)) {
				formSubmissionMessageList.add(requiredFieldErrorMessage);
				throw new FormValidationException(requiredFieldErrorMessage);
			}
			
			StringProperty formParameterProperty = (StringProperty) formContentObject.getCmsProperty(formParameter);
			if (StringUtils.isBlank((String) formParameterValue)) {
				formParameterProperty.setSimpleTypeValue(null);
			}
			else {
				formParameterProperty.setSimpleTypeValue((String)formParameterValue);
			}
			break;
		}

		case Date:
		{
			// check if form value is of the appropriate type
			if (!(formParameterValue instanceof Date)) {
				throw new FormValidationException("Invalid form field value type for property:" + formParameter + " A Date value was expected");
			}
			
			// check if no value has been provided for a mandatory property
			if (formParameterDefinition.isMandatory() && formParameterValue == null) {
				formSubmissionMessageList.add(requiredFieldErrorMessage);
				throw new FormValidationException(requiredFieldErrorMessage);
			}
			
			CalendarProperty formParameterProperty = (CalendarProperty) formContentObject.getCmsProperty(formParameter);
			if (formParameterValue == null) {
				formParameterProperty.setSimpleTypeValue(null);
			}
			else {
				Calendar calendar = GregorianCalendar.getInstance(JSFUtilities.getTimeZone(), JSFUtilities.getLocale());
				calendar.setTime(((Date)formParameters.get(formParameter)));
				formParameterProperty.setSimpleTypeValue(calendar);
			}
			break;	
		}

		case Boolean:
		{
			// check if form value is of the appropriate type
			if (!(formParameterValue instanceof Boolean)) {
				throw new FormValidationException("Invalid form field value type for property:" + formParameter + " A Boolean value was expected");
			}
			
			// check if no value has been provided for a mandatory property
			if (formParameterDefinition.isMandatory() && formParameterValue == null) {
				formSubmissionMessageList.add(requiredFieldErrorMessage);
				throw new FormValidationException(requiredFieldErrorMessage);
			}
			
			BooleanProperty formParameterProperty = (BooleanProperty) formContentObject.getCmsProperty(formParameter);
			if (formParameterValue == null) {
				formParameterProperty.setSimpleTypeValue(null);
			}
			else {
				formParameterProperty.setSimpleTypeValue((Boolean)formParameters.get(formParameter));
			}
			break;
		}
		case Topic:
		{
			// check if form value is of the appropriate type
			// we expect a string which corresponds to the id of an existing topic
			if (!(formParameterValue instanceof String)) {
				throw new FormValidationException("Invalid form field value type for property:" + formParameter + " A string value corresponding to the id of an existing topic was expected");
			}
			
			// check if no value has been provided for a mandatory property
			if (formParameterDefinition.isMandatory() && StringUtils.isBlank((String) formParameterValue)) {
				formSubmissionMessageList.add(requiredFieldErrorMessage);
				throw new FormValidationException(requiredFieldErrorMessage);
			}
			
			
			
			TopicProperty formParameterProperty = (TopicProperty) formContentObject.getCmsProperty(formParameter);
			if (StringUtils.isBlank((String)formParameterValue)) {
				formParameterProperty.setSimpleTypeValue(null);
			}
			else {
				Topic retrievedTopic = getFormsRepositoryClient().getTopicService().getTopic((String) formParameterValue, ResourceRepresentationType.TOPIC_INSTANCE, FetchLevel.ENTITY);
				if (retrievedTopic == null) {
					throw new Exception("Not topic found for the provided topic id:" + formParameterValue);
				}
				
				formParameterProperty.setSimpleTypeValue(retrievedTopic);
			}
			break;
		}

		default:
			throw new Exception("Not supported value type:" + formParameterDefinition.getValueType() + " for parameter:" + formParameter);
		}
	}
	
	// run custom validation rules and do custom processing to formContentObject or formParams 
	public abstract void postValidateAndApplyValuesPhase(Map<String,Object> formParams, ContentObject formContentObject) throws Exception;
	
	// run additional actions after successful form saving, e.g. send email, start a workflow, etc. 
	public abstract void postSavePhase(Map<String,Object> formParams, ContentObject formContentObject) throws Exception;
	
	// called inside applyDefaultValuesPhase and permit custom accessibility setup
	protected abstract void applyAccessibilityPropertiesPhase(ContentObject formContentObject);
	
	//called inside checkChallengeResponsePhase and allow users to disable challenge response in case they do
	//not display captcha in their form
	protected abstract boolean enableCheckForChallengeRespone();
	
	public Map<String, Object> getFormParams() {
		return formParams;
	}

	
	public abstract String getFormType();
	
	protected abstract T getFormsRepositoryClient();
		

	public ContentObject getFormContentObject() {
		return formContentObject;
	}


	public List<String> getFormSubmissionMessageList() {
		return formSubmissionMessageList;
	}


	public Boolean getSuccessfulFormSubmission() {
		return successfulFormSubmission;
	}


	public void setSuccessfulFormSubmission(Boolean successfulFormSubmission) {
		this.successfulFormSubmission = successfulFormSubmission;
	}


	public Map<String, Object> getTempParams() {
		return tempParams;
	}

	
}

