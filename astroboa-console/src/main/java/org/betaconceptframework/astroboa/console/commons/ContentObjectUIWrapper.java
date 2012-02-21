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

package org.betaconceptframework.astroboa.console.commons;



import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryProperty;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.TopicReferenceProperty;
import org.betaconceptframework.astroboa.api.model.definition.TopicReferencePropertyDefinition;
import org.betaconceptframework.astroboa.api.model.query.ContentAccessMode;
import org.betaconceptframework.astroboa.api.security.CmsRole;
import org.betaconceptframework.astroboa.api.service.ContentService;
import org.betaconceptframework.astroboa.api.service.DefinitionService;
import org.betaconceptframework.astroboa.console.security.LoggedInRepositoryUser;
import org.betaconceptframework.astroboa.security.CmsRoleAffiliationFactory;
import org.betaconceptframework.bean.AbstractBean;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.MicrosoftFileTextExtractor;
import org.betaconceptframework.utility.PdfToTextExtractor;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.security.Identity;
import org.springframework.web.context.support.ServletContextResource;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ContentObjectUIWrapper extends AbstractBean{
	
	private static final long serialVersionUID = 1L;
	
	// injected spring beans
	private PdfToTextExtractor pdfToTextExtractor;
	private MicrosoftFileTextExtractor microsoftFileTextExtractor;
	private ContentService contentService;
	private DefinitionService definitionService;
	private CMSUtilities cmsUtilities;
	
	// this bean is manually retrieved from seam context in constructor 
	private LoggedInRepositoryUser loggedInRepositoryUser;
	
	private Map<String, CmsProperty> cacheOfCheckedPropertiesForExistance = new HashMap<String, CmsProperty>(); 
	
	private ContentObject contentObject;
	private CmsPropertyProxy cmsPropertyProxy;
	private CmsPropertyDefinitionProxy cmsPropertyDefinitionProxy;
	
	// controls whether the content object details panel will be rendered during content object presentation in list view
	private boolean detailsRendered;
	
	// controls whether the user tags input panel will be rendered during content object presentation in list view
	private boolean userTagsInputFormRendered;
	
	private long textSearchRanking;
	
	private List<String> acceptedTaxonomiesForProfileSubject;
	
	public ContentObjectUIWrapper() {
		loggedInRepositoryUser = (LoggedInRepositoryUser) Contexts.getSessionContext().get("loggedInRepositoryUser");
	}
	
	public ContentObjectUIWrapper (ContentObject contentObject) {
		this.contentObject = contentObject;
		cmsPropertyProxy = new CmsPropertyProxy(contentObject);
		cmsPropertyDefinitionProxy = new CmsPropertyDefinitionProxy(contentObject);
		acceptedTaxonomiesForProfileSubject = null;
	}

	
	public String getContentObjectIcon() {
		// The Default Icon if we do not find a more appropriate one
		String contentObjectIconFilePath = "images/mime-type_icons/contentObject.png";
		
		/* 
		 * if the content object type is a file then we will find an appropriate icon according to the mime-type of the primaryBinaryChannel which
		 * holds the primary binary data of this file.
		 */
		//String contentObjectIconsBasePath = "/opt/apache-tomcat-5.5.20/webapps/Astroboa/";
		String mimeType = getMimeTypeOfFileTypeContentObject();
		if (mimeType != null) { // the mime type is retreived from the primaryBinaryChannel of "file-type" content objects" 
				String mimetypeIconFilePath = "images/mime-type_icons/" + mimeType + ".png";

				ServletContextResource mimeTypeIconResource = 
					new ServletContextResource((ServletContext)FacesContext.getCurrentInstance().getExternalContext().getContext(), mimetypeIconFilePath);
				if (mimeTypeIconResource.exists()) 
					return mimetypeIconFilePath;
				
				/*File iconFile = new File(contentObjectIconsBasePath + mimetypeIconFilePath);
				if (iconFile.exists()) 
					contentObjectIconFilePath = mimetypeIconFilePath;*/

		}
		else { // if it is not a "file-type" content object we check if an icon exists for its content type  
		
		}
		return	contentObjectIconFilePath;	
	}
	
	
	public boolean isFile() {
		if (getContentObject().getContentObjectType().equals("documentFile") ||
				getContentObject().getContentObjectType().equals("imageFile") ||
				getContentObject().getContentObjectType().equals("audioFile") ||
				getContentObject().getContentObjectType().equals("videoFile") ||
				getContentObject().getContentObjectType().equals("spreadsheetFile") ||
				getContentObject().getContentObjectType().equals("presentationFile")) 
			return true;
		else return false;
	}
	
	// returns the source File Name of the included file if the content Object is a file
	public String getSourceFileName() {
		if (isFile()) {
			BinaryProperty primaryBinaryChannelProperty = getBinaryProperty("primaryBinaryChannel");
			if (primaryBinaryChannelProperty != null)
			{	
				if (primaryBinaryChannelProperty.getPropertyDefinition().isMultiple())
				{
					if (primaryBinaryChannelProperty.getSimpleTypeValues().size() > 0)
						return primaryBinaryChannelProperty.getSimpleTypeValues().get(0).getSourceFilename();
				}
				else
					return primaryBinaryChannelProperty.getSimpleTypeValue().getSourceFilename();
			}
			
		}
		return null;
	}
	
	// returns the mimeType of the included file if the content Object is a file
	public String getMimeTypeOfFileTypeContentObject() {
		if (isFile()) {
			BinaryProperty binaryProperty = getBinaryProperty("primaryBinaryChannel");
			if (binaryProperty != null)
			{
				if (binaryProperty.getPropertyDefinition().isMultiple())
				{
					if (binaryProperty.getSimpleTypeValues().size() > 0)
						return binaryProperty.getSimpleTypeValues().get(0).getMimeType();
				}
				else
					binaryProperty.getSimpleTypeValue().getMimeType();
			}
		}
		return null;
	}
	
	public BinaryProperty getBinaryProperty(String binaryPropertyPath) {
		//Search only if property has not checked in before
		if (!cacheOfCheckedPropertiesForExistance.containsKey(binaryPropertyPath))
		{	

			if (getContentObject().getComplexCmsRootProperty().isChildPropertyDefined(binaryPropertyPath)) {
				BinaryProperty binaryProperty = (BinaryProperty) getContentObject().getCmsProperty(binaryPropertyPath);
				if (binaryProperty != null &&
						(binaryProperty.getPropertyDefinition().isMultiple() && CollectionUtils.isNotEmpty(binaryProperty.getSimpleTypeValues())) ||
						(!binaryProperty.getPropertyDefinition().isMultiple() && binaryProperty.getSimpleTypeValue() != null)
				)
					cacheOfCheckedPropertiesForExistance.put(binaryPropertyPath, binaryProperty);
				else
					return null;
			}
			else
				return null;
		}
		
		return (BinaryProperty)cacheOfCheckedPropertiesForExistance.get(binaryPropertyPath);
	}
	
	
	/* find if there is a thumbnail for the content object */
	public boolean isThumbnailAvailable() {
		return getBinaryProperty("thumbnail") != null;
	}
	
	/**
	 * The method returns the content of the thumbnail channel and is targeted for use by the UI to show a thumbnail for each content object.
	 * BE AWARE! The method assumes that the thumbnail binary channel exists. The calling code should first check the thumbnail existence using 
	 * <isThumnailAvailable> method which also ensures that binary content is loaded. If thumbnail property does not exist a null pointer exception will occur.
	 * If thumbnail property exists but has no value then null will be returned.
	 * @return
	 */
	public byte[] getThumbnailContent() {
		//We expected that thumbnail property is a single value property but checking just in case
		BinaryProperty thumbnailProperty = getBinaryProperty("thumbnail");
		if (thumbnailProperty.getPropertyDefinition().isMultiple())
		{
			if (thumbnailProperty.getSimpleTypeValues().size() > 0)
				return thumbnailProperty.getSimpleTypeValues().get(0).getContent();
		}
		else
			return thumbnailProperty.getSimpleTypeValue().getContent();
		return null;
	}
	
	/**
	 * The method returns the source filename of the thumbnail channel and is targeted for use by the UI to show a thumbnail for each content object.
	 * BE AWARE! The method assumes that the thumbnail binary channel exists. The calling code should first check the thumbnail existence using 
	 * the <binaryChannelExists> method or the <isThumnailAvailable> method which also ensures that binary content is loaded. 
	 * If thumbnail does not exist a null pointer exception will occur 
	 * @return
	 */
	public String getThumbnailFilename() {
		//We expected that thumbnail property is a single value property but checking just in case
		BinaryProperty thumbnailProperty = getBinaryProperty("thumbnail");
		if (thumbnailProperty.getPropertyDefinition().isMultiple())
		{
			if (thumbnailProperty.getSimpleTypeValues().size() > 0)
				return thumbnailProperty.getSimpleTypeValues().get(0).getSourceFilename();
		}
		else
			return thumbnailProperty.getSimpleTypeValue().getSourceFilename();
		
		return null;
			
	}
	
	/**
	 * The method returns the mimeType of the thumbnail channel and is targeted for use by the UI to show a thumbnail for each content object.
	 * BE AWARE! The method assumes that the thumbnail binary channel exists. The calling code should first check the thumbnail existence using 
	 * the <binaryChannelExists> method or the <isThumnailAvailable> method which also ensures that binary content is loaded. 
	 * If thumbnail does not exist a null pointer exception will occur 
	 * @return
	 */
	public String getThumbnailMimeType() {
		//We expected that thumbnail property is a single value property but checking just in case
		BinaryProperty thumbnailProperty = getBinaryProperty("thumbnail");
		if (thumbnailProperty.getPropertyDefinition().isMultiple())
		{
			if (thumbnailProperty.getSimpleTypeValues().size() > 0)
				return thumbnailProperty.getSimpleTypeValues().get(0).getMimeType();
		}
		else
			return thumbnailProperty.getSimpleTypeValue().getMimeType();
		return null;
		
	}
	
	/**
	 * The method creates an output stream for a byte array containing the data of a binary channel
	 * The output stream is used by a UI object (a4j:mediaOutput) to either render or create a link to the binary data 
	 * @param out
	 * @param data
	 * @throws IOException
	 */
	public void binaryDataOutput(OutputStream out, Object data) throws IOException{
		//out.write((byte[]) data);
		out.write(getThumbnailContent());
		out.close();
	}


	
	
	public boolean isDocumentFile() {
		if (getContentObject().getContentObjectType().equals("documentFile")) 
			return true;
		else 
			return false;
	}
	
	public boolean isPlainTextContainedInBinaryData() {
		String mimeType = getMimeTypeOfFileTypeContentObject();
		if (mimeType == null)
			return false;
		if (mimeType.equals("text/plain") || mimeType.equals("text/html"))
			return true; 
		else 
			return false;
	}
	
	
	public boolean isPdfFile() {
		String mimeType = getMimeTypeOfFileTypeContentObject();
		if (mimeType == null)
			return false;
		if (mimeType.equals("application/pdf") || mimeType.equals("application/x-pdf"))
			return true;
		else
			return false;
	}
	
	public boolean isWordFile() {
		String mimeType = getMimeTypeOfFileTypeContentObject();
		if (mimeType == null)
			return false;
		if (mimeType.equals("application/msword"))
			return true;
		else
			return false;
	}
	
	public boolean isPlainTextExtractionFromBinaryDataPossible() {
		
		if (isPdfFile() || isWordFile())
			return true;
		else
			return false;
	}
	
	public boolean isPlainTextPreviewOfBinaryDataAvailable() {
		if (isDocumentFile() && (isPlainTextContainedInBinaryData() || isPlainTextExtractionFromBinaryDataPossible())) //it has a binary data channel which contains plainText or the binary data can be converted to text
			return true;
		else 
			return false;
	}
	
	public String getPlainTextPreviewOfBinaryData() {
		if (isDocumentFile()) {
			if (isPlainTextContainedInBinaryData()) {
				try {
					return new String(getPrimaryBinaryChannelData(), "utf-8");
				}
				catch (Exception e) {
					return null;
				}
			}	
			else if (isPdfFile()){
				try {
					return pdfToTextExtractor.extractPdfToText(getPrimaryBinaryChannelData(), 1, 0, false, false, null);
				}
				catch (Exception e) {
					getLogger().error("Error while converting pdf file to text" , e);
					return null;
				}
			}
			else if (isWordFile()) {
				try {
					return microsoftFileTextExtractor.extractTextFromWordFile(getPrimaryBinaryChannelData());
				}
				catch (Exception e) {
					getLogger().error("Error while converting word file to text" , e);
					return null;
				}
			}
			else
				return null;
		}
		else
			return null;
	}
	
	
	/* returns the binary data of the binary channel named "primaryBinaryChannel". 
	 * This is the channel name in the image/video/audio/textBased content types which holds the binary data of the file stored in these content types
	 * These content types may also contain secondary binary channels such a thumbnail, preview or alternative encodings of the primary channel
	 */ 
	public byte[] getPrimaryBinaryChannelData() {
		BinaryProperty thumbnailProperty = getBinaryProperty("primaryBinaryChannel");
		if (thumbnailProperty.getPropertyDefinition().isMultiple())
		{
			if (thumbnailProperty.getSimpleTypeValues().size() > 0)
				return thumbnailProperty.getSimpleTypeValues().get(0).getContent();
		}
		else
			return thumbnailProperty.getSimpleTypeValue().getContent();
		return null;
	}
	
	
	public String getContentObjectOwnerName() {
		return getContentObject().getOwner().getLabel();
		
		// Getting the owner name from the label of the repository user may result in a different name than the name stored in the identity db. 
		// The Repository User corresponding to a Person in identity db as well as Repository User "label" property" which holds 
		// the person name is created upon first login of the user and is also updated at any other login.  
		// Therefore cases exist where the label may be different than the display name of the user if the user identity data are kept in an external IDP and so they are 
		// changed elsewhere than the Astroboa console (if the astroboa provided idp is utilized then user data can be updated inside astroboa and Repository label is synchronized).
		// We originally have used calls to the identity api in order to retrieve the name always from the identity db and possibly correct the label if we find differences.
		// However this is very costly when searching and rendering many content objects in the webui.
		// So we revert back to return the label and we may provide a utility which synchronizes the identity db with the repository user labels.
		// This utility will be offered by the webui and run by the repository administrator. It could be also scheduled to run periodically.
	}
	
	
	public List<Topic> getSystemTags() {
		
		TopicReferenceProperty profileSubjectProperty = ((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject"));
		if (profileSubjectProperty.getSimpleTypeValues() != null) {
			return profileSubjectProperty.getSimpleTypeValues();
		}
		
		return null;
		
	}
	
	public List<Topic> getUserTagsAddedByOwner() {
		List<Topic> userTagsAddedByOwner = null;
		TopicReferenceProperty profileSubjectProperty = ((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject"));
		if (profileSubjectProperty.getSimpleTypeValues() != null) {
			String contentObjectOwnerId =  getContentObject().getOwner().getId();
			userTagsAddedByOwner = new ArrayList<Topic>();
			for (Topic topic : profileSubjectProperty.getSimpleTypeValues()) {
				if (topic.getTaxonomy().getName().equals(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME) && topic.getOwner().getId().equals(contentObjectOwnerId))
					userTagsAddedByOwner.add(topic);
			}
		}
		return userTagsAddedByOwner;
	}
	
	public List<Topic> getTagsAddedByLoggedInUser() {
		List<Topic> tagsAddedByLoggedInUser = null;
		TopicReferenceProperty profileSubjectProperty = ((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject"));
		if (profileSubjectProperty.getSimpleTypeValues() != null) {
			tagsAddedByLoggedInUser = new ArrayList<Topic>();
			String loggedInUserId = getLoggedInRepositoryUserId();

			for (Topic topic: (List<Topic>) profileSubjectProperty.getSimpleTypeValues()) {
				if (topic.getTaxonomy().getName().equals(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME) && topic.getOwner().getId().equals(loggedInUserId))
					tagsAddedByLoggedInUser.add(topic);
			}
		}
		return tagsAddedByLoggedInUser;
	}
	
	public String getLoggedInUserId() {
		return Identity.instance().getPrincipal().getName();
	}
	
	
	private String getLoggedInRepositoryUserId() {
		return loggedInRepositoryUser.getRepositoryUser().getId();
	}
	
	
	/*
	 * return all user tags which are not added by the owner of this object and not added by the current logged in user
	 */
	public List<Topic> getUserTagsAddedByOthers() {
		List<Topic> othersTags = null;
		TopicReferenceProperty profileSubjectProperty = ((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject"));
		if (profileSubjectProperty.getSimpleTypeValues() != null) {
			othersTags = new ArrayList<Topic>();
			String contentObjectOwnerUUID =  getContentObject().getOwner().getId();

			String loggedInUserId = getLoggedInRepositoryUserId();

			for (Topic topic :profileSubjectProperty.getSimpleTypeValues()) {
				if (topic.getTaxonomy().getName().equals(Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME) && (! topic.getOwner().getId().equals(contentObjectOwnerUUID)) && (! topic.getOwner().getId().equals(loggedInUserId)))
					othersTags.add(topic);
			}
		}
		return othersTags;
	}
	
	
	public void removeTopicFromContentObjectSubject(String topicId) throws Exception {
		// We examine the  Subject of the currently edited content object which is a List of Topics to find which topic in Content Object Subject we should remove
		TopicReferenceProperty profileSubjectProperty = ((TopicReferenceProperty)contentObject.getCmsProperty("profile.subject"));
		if (profileSubjectProperty.getSimpleTypeValues() != null) {
			boolean topicFound = false;
			String topicIdOrLabel;
			int indexOfTopicInContentObjSubject = 0;
			for (Topic existingTopic : profileSubjectProperty.getSimpleTypeValues()) {
				if (existingTopic.getId() == null) // it is a new user Tag. We will use the topic label in order to locate it
					topicIdOrLabel = existingTopic.getLocalizedLabelForCurrentLocale();
				else topicIdOrLabel = existingTopic.getId();
				if (!topicIdOrLabel.equals(topicId)) 
					++indexOfTopicInContentObjSubject;
				else {
					topicFound = true;
					break;
				}
			}
			if (topicFound)
				profileSubjectProperty.removeSimpleTypeValue(indexOfTopicInContentObjSubject);
			else {
				getLogger().error("Topic is not present in Content Object Subject and thus cannot be removed");
				throw new Exception("Topic is not present in Content Object Subject and thus cannot be removed");
			}
		}
	}
	
	
	public boolean isLoggedInUserIdOrLoggedInUserRolesInAuthorizationList(List<String> authorizationList) {
		
		// If loggedIn  user is the Administrator then she is allowed
		if (Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN)))
		{
			return true;
		}
		
		// if loggedIn User is Owner  she is allowed
		if(getContentObject().getOwner() != null && 
				StringUtils.equals(getLoggedInRepositoryUserId(), getContentObject().getOwner().getId()))
		{
			//Special case if content object has no id check if user has role cms_editor
			if (getContentObject().getId() != null)
			{
				return true;
			}
			else
			{
				//User wants to access a new content object. Regardless of whether
				//access is related to tag, update, read or delete, the following rules must apply
				
				String contentObjectType = getContentObject().getContentObjectType();
				
				if (StringUtils.isBlank(contentObjectType))
				{
					logger.warn("User {} has been denied access to a content object whose type is not defined", getLoggedInUserId());
					return false;
				}
				
				//1. If content object type is one of built in types
				//    portalObject, portalSectionObject, dynamicContentAreaObject and scheduledContentAreaObject
				//   then role ROLE_CMS_PORTAL_EDITOR must be assigned to user
				if ("portalObject".equals(contentObjectType) ||
						"portalSectionObject".equals(contentObjectType) ||
						"dynamicContentAreaObject".equals(contentObjectType) ||
						"scheduledContentAreaObject".equals(contentObjectType))
				{
					return Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_PORTAL_EDITOR));
				}
				else
				{
					//All other types require that user must have role ROLE_CMS_EDITOR
					return Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_EDITOR));
					
				}
				
				
			}
		}

		//If list is empty the access is allowed
		if (CollectionUtils.isEmpty(authorizationList))
		{
			return true;
		}
		

		// if access is ALL users then logged in user is allowed too
		//Value ALL can be anywhere
		if(authorizationList.contains(ContentAccessMode.ALL.toString()))
				return true;
		
		// if access is NONE user then logged in user is not allowed
		//Value NONE can be anywhere
		if (authorizationList.contains(ContentAccessMode.NONE.toString()))
				return false;
		
		// if none of the above is true then only selected users / groups are allowed access.
		//lets check if the logged in user or her groups are in the access list
		String loggedInUserId = getLoggedInUserId();
		
		// the authorization list may have both usernames and user roles.
		for (String userIdOrRole : authorizationList) {
			
			if (userIdOrRole.equals(loggedInUserId)){
				return true;
			}
			else if (Identity.instance().hasRole(userIdOrRole)){
				return true; 
			}
		}
		
		return false;
	}
	
	// A user can publish if she has the relevant role and additionally has the rights to update a content object
	// A pre-save groovy script can be used in Astroboa Console to automatically add a publisher to access right list of objects that she may latter publish
	public boolean isLoggedInUserAuthorizedToPublishAndUnpublishContentObject() {
		if (Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_CMS_WEB_SITE_PUBLISHER)) && 
				isLoggedInUserIdOrLoggedInUserRolesInAuthorizationList(((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).getSimpleTypeValues())) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	public boolean isLoggedInUserAuthorizedToUpdateContentObject() {
		// There is a special case that the user is allowed to update her profile
		// This case happens when the user tries to update an object that is type is a personType or a subclass of personType and the username of the logged in user is 
		// equal to the user name stored in the "personAuthentication.username" property of the object
		
			return isLoggedInUsersPersonProfile() || isLoggedInUserIdOrLoggedInUserRolesInAuthorizationList(((StringProperty)contentObject.getCmsProperty("accessibility.canBeUpdatedBy")).getSimpleTypeValues());
	}
	
	// Check if object type is "personType" or a subclass of personType (i.e the "personType" defined in Astroboa internal schemas) and the username of the logged in user is 
	// equal to the user name stored in the "personAuthentication.username" property of the object
	// i.e. check if this object holds the data of a person and that person happens to be the logged in user
	public boolean isLoggedInUsersPersonProfile() {
		if (contentObject.getTypeDefinition().isTypeOf("personType") && 
				StringUtils.equals(contentObject.getTypeDefinition().getQualifiedName().getNamespaceURI(), "http://www.betaconceptframework.org/schema/astroboa/identity/person")) {
			String usernameStoredInObject = ((StringProperty)contentObject.getCmsProperty("personAuthentication.username")).getSimpleTypeValue();
			if (usernameStoredInObject != null &&
				usernameStoredInObject.equals(loggedInRepositoryUser.getIdentity())) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}
	
	public boolean isLoggedInUserAuthorizedToTagContentObject() {
		return isLoggedInUserIdOrLoggedInUserRolesInAuthorizationList(((StringProperty)contentObject.getCmsProperty("accessibility.canBeTaggedBy")).getSimpleTypeValues());
	}
	
	public boolean isLoggedInUserAuthorizedToDeleteContentObject() {
		boolean isUserAuthorized = false;

		if (!contentObject.getComplexCmsRootProperty().isChildPropertyDefined("accessibility")) {
			isUserAuthorized = true;
		} else
			isUserAuthorized = isLoggedInUserIdOrLoggedInUserRolesInAuthorizationList(((StringProperty)contentObject.getCmsProperty("accessibility.canBeDeletedBy")).getSimpleTypeValues());
		
		//SPECIAL CASE
		if ("personObject".equals(contentObject.getContentObjectType())){
			//In case this contentObject is of type personObject and currently connected user
			//is represented by this type then disable link
			StringProperty userNameProperty = (StringProperty) contentObject.getCmsProperty("personAuthentication.username");
			if (userNameProperty != null && userNameProperty.hasValues() && 
					userNameProperty.getSimpleTypeValue().equals(loggedInRepositoryUser.getIdentity())){
				return false;
			}
		}
		return isUserAuthorized;
	}
	
	
	public boolean isLoggedInUserAuthorizedToEditAccessRights() {
		// if logged in user is the Administrator then she is allowed
		if (Identity.instance().hasRole(CmsRoleAffiliationFactory.INSTANCE.getCmsRoleAffiliationForActiveRepository(CmsRole.ROLE_ADMIN)))
			return true;
		
		// loggedInUser is Owner so she is allowed. ONLY the OWNER is allowed to edit Access Rights
		if(getLoggedInRepositoryUserId().equals(getContentObject().getOwner().getId()))
			return true;
		else return false;
	}
	
	
	public ContentObject getContentObject() {
		return contentObject;
	}


	public void setContentObject(ContentObject contentObject) {
		this.contentObject = contentObject;
	}


	public boolean isDetailsRendered() {
		return detailsRendered;
	}


	public void setDetailsRendered(boolean detailsRendered) {
		this.detailsRendered = detailsRendered;
	}
	
	public void toggleDetailsRendered() {
		if (isDetailsRendered()) setDetailsRendered(false);
		else setDetailsRendered(true);
	}
	
	public void toggleUserTagsInputFormRendered() {
		if (isUserTagsInputFormRendered())
			setUserTagsInputFormRendered(false);
		else
			setUserTagsInputFormRendered(true);
	}

	
	// we need this in order to determine if we can show the image inline
	public boolean isJPGorPNGorGIFImageFile(){
		if (contentObject.getContentObjectType().equals("fileResourceObject") && 
				((BinaryProperty)contentObject.getCmsProperty("content")).hasValues() &&  
				((BinaryProperty)contentObject.getCmsProperty("content")).getSimpleTypeValue().getMimeType() != null) {
			
			String mimeType = ((BinaryProperty)contentObject.getCmsProperty("content")).getSimpleTypeValue().getMimeType();
			if (mimeType.equals("image/jpeg") || 
				mimeType.equals("image/png") || 
				mimeType.equals("image/x-png") ||
				mimeType.equals("image/gif")) {
				
				return true;
			}
			else {
				return false;
			}
		}
		
		return false; 
		
	}
	

	public List<String> getAcceptedTaxonomiesForProfileSubject(){
		if (acceptedTaxonomiesForProfileSubject ==null){
			if (getContentObject() != null &&
					getContentObject().getComplexCmsRootProperty() != null && 
					getContentObject().getComplexCmsRootProperty().isChildPropertyDefined("profile.subject")){
				
				//Property profile.subject is defined. Get its definition
				//In most cases it should be attached to content object's type definition
				TopicReferencePropertyDefinition profileSubjectDefinition = null;
				
				if (getContentObject().getComplexCmsRootProperty().getPropertyDefinition() != null){
					profileSubjectDefinition = (TopicReferencePropertyDefinition) getContentObject().getComplexCmsRootProperty().getPropertyDefinition().getChildCmsPropertyDefinition("profile.subject"); 
				}
				
				if (profileSubjectDefinition == null){
					//Look in aspect definitions
					if (!MapUtils.isEmpty(getContentObject().getComplexCmsRootProperty().getAspectDefinitions()) && 
							getContentObject().getComplexCmsRootProperty().getAspectDefinitions().containsKey("profile")){
						profileSubjectDefinition = (TopicReferencePropertyDefinition) getContentObject().getComplexCmsRootProperty().getAspectDefinitions().get("profile").getChildCmsPropertyDefinition("subject");
					}
				}
				
				if (profileSubjectDefinition == null){
					logger.error("'profile.subject' definition was not found neither in type definition nor in aspect definitions of edited content object "+ getContentObject().getId() + " "+ 
							" of type "+ getContentObject().getContentObjectType() + " Accepted taxonomies for property 'profile.subject' will be set the empty list");
				}
				else{
					acceptedTaxonomiesForProfileSubject = profileSubjectDefinition.getAcceptedTaxonomies();
				}
			}
			
		}
		
		return acceptedTaxonomiesForProfileSubject;
	}
	public long getTextSearchRanking() {
		return textSearchRanking;
	}


	public void setTextSearchRanking(long textSearchRanking) {
		this.textSearchRanking = textSearchRanking;
	}


	public boolean isUserTagsInputFormRendered() {
		return userTagsInputFormRendered;
	}


	public void setUserTagsInputFormRendered(boolean userTagsInputFormRendered) {
		this.userTagsInputFormRendered = userTagsInputFormRendered;
	}

	public CmsPropertyProxy getContentObjectProperty()
	{
		return cmsPropertyProxy;
	}
	
	public CmsPropertyDefinitionProxy getContentObjectPropertyDefined()
	{
		return cmsPropertyDefinitionProxy;
	}

	public void setCmsUtilities(CMSUtilities cmsUtilities) {
		this.cmsUtilities = cmsUtilities;
	}


	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}


	public void setLoggedInRepositoryUser(
			LoggedInRepositoryUser loggedInRepositoryUser) {
		this.loggedInRepositoryUser = loggedInRepositoryUser;
	}


	public void setMicrosoftFileTextExtractor(
			MicrosoftFileTextExtractor microsoftFileTextExtractor) {
		this.microsoftFileTextExtractor = microsoftFileTextExtractor;
	}


	public void setPdfToTextExtractor(PdfToTextExtractor pdfToTextExtractor) {
		this.pdfToTextExtractor = pdfToTextExtractor;
	}

	public void setCmsPropertyProxy(
			CmsPropertyProxy contentObjectPropertyProxy) {
		this.cmsPropertyProxy = contentObjectPropertyProxy;
	}

	public void setDefinitionService(DefinitionService definitionService) {
		this.definitionService = definitionService;
	}

	public void setCmsPropertyDefinitionProxy(
			CmsPropertyDefinitionProxy cmsPropertyDefinitionProxy) {
		this.cmsPropertyDefinitionProxy = cmsPropertyDefinitionProxy;
	}

	public String getContentObjectTypeForCurrentLocale(){
		
		String localizedLabelForContentObjectType = null;
		if (contentObject.getTypeDefinition() != null){
			localizedLabelForContentObjectType =  contentObject.getTypeDefinition().getDisplayName().getAvailableLocalizedLabel(JSFUtilities.getLocaleAsString());
		}
		
		return localizedLabelForContentObjectType;
	}
	
	public String getLocalizedMessageForEnabledWorklfow(){
		if (contentObject.getComplexCmsRootProperty().isChildPropertyDefined("workflow.managedThroughWorkflow")){
			StringProperty managedWorkFlowProperty = (StringProperty) contentObject.getCmsProperty("workflow.managedThroughWorkflow");
			
			if (managedWorkFlowProperty != null && managedWorkFlowProperty.getSimpleTypeValue() != null){
				try{
					return JSFUtilities.getLocalizedMessage("workflow."+managedWorkFlowProperty.getSimpleTypeValue(), null);
				}
				catch (Exception e){
					logger.warn("No localized label found for workflow "+ managedWorkFlowProperty.getSimpleTypeValue(), e);
					return "";
				}
			}
		}
		
		return "";
	}
}
