package org.betaconceptframework.astroboa.console.commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ObjectReferenceProperty;
import org.betaconceptframework.astroboa.api.model.StringProperty;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.betaconceptframework.astroboa.console.jsf.edit.ContentObjectPropertyWrapper;
import org.betaconceptframework.astroboa.console.jsf.edit.DialogPropertyWrapper;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("editServiceAsync")
@Scope(ScopeType.EVENT)
public class EditServiceAsync {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private AstroboaClient astroboaClient;
	
	@In
	private DialogPropertyWrapper dialogPropertyWrapper;
	
	@In
	private Map<String,String> messages;

	@WebRemote
	public String addObjectToPropertyValues(String objectName, boolean checkAcceptedContentObjectTypes){
		try {
			ContentObjectPropertyWrapper propertyWrapper = (ContentObjectPropertyWrapper) dialogPropertyWrapper.getCmsPropertyWrapper();
			ObjectReferenceProperty property = (ObjectReferenceProperty) propertyWrapper.getCmsProperty();
			
			// add the wrapper index to the list of wrappers that should be updated by the UI
			propertyWrapper.getComplexCmsPropertyEdit().setWrapperIndexesToUpdate(Collections.singleton(propertyWrapper.getWrapperIndex()));
			
			if (property != null && propertyWrapper.getCmsPropertyDefinition() != null && 
					objectName != null) {
				List<ContentObject> contentObjects= new ArrayList<ContentObject>();
				if (propertyWrapper.getCmsPropertyDefinition().isMultiple()){
					contentObjects = property.getSimpleTypeValues();
				}
				else{
					if (property.getSimpleTypeValue() != null)
						contentObjects.add(property.getSimpleTypeValue());
	
				}
				
				ContentObject object = astroboaClient.getContentService()
						.getContentObject(objectName, ResourceRepresentationType.CONTENT_OBJECT_INSTANCE, FetchLevel.ENTITY_AND_CHILDREN, null, Arrays.asList("profile.title"), false);
				
				if (object == null) {
					return "{\"status\": \"warning\", \"message\": \"" + messages.get("dialog.objectSelection.selectedObjectNotFoundInRepository") + "\"}";
				}
				
				String objectId = object.getId();
	
				// check if selected content object is already inserted
				boolean contentObjectExists = false;
				for (ContentObject contentObject : contentObjects) {
					if (contentObject.getId() != null && contentObject.getId().equals(objectId)) {
						contentObjectExists = true;
						break;
					}
				}
	
				if (!contentObjectExists) {
					//Check that content object type is accepted
					if (CollectionUtils.isNotEmpty(propertyWrapper.getAcceptedContentTypes()) && checkAcceptedContentObjectTypes){
						if (object.getContentObjectType() == null ||
								! propertyWrapper.getAcceptedContentTypes().contains(object.getContentObjectType())
						){
							return "{\"status\": \"warning\", \"message\": \"" + messages.get("dialog.objectSelection.notAcceptedObjectType") + "\"}";
						}
	
					}
	
					if (propertyWrapper.isMultiple()){
						property.addSimpleTypeValue(object);
						propertyWrapper.getSimpleCmsPropertyValueWrappers().clear();
					}
					else{
						//Now replace value
						property.setSimpleTypeValue(object);
					}
					
					return "{\"status\": \"success\", \"message\": \"'" + ((StringProperty)object.getCmsProperty("profile.title")).getSimpleTypeValue() + "' "  + messages.get("dialog.objectSelection.objectHasBeenAdded") +"\"}";
	
				}
				else {
					return "{\"status\": \"warning\", \"message\": \"'" + ((StringProperty)object.getCmsProperty("profile.title")).getSimpleTypeValue() + "' "  + messages.get("dialog.objectSelection.objectHasAlreadyBeenAdded") +"\"}";
				}
			}
		
			return "{\"status\": \"warning\", \"message\": \"'" + objectName + "' "  + messages.get("dialog.objectSelection.failedToAddObjectBecauseRequiredDataAreMissing") +"\"}";
		}
		catch (Exception e) {
			logger.error("An error occured while adding a content object reference to property values", e);
			return "{\"status\": \"warning\", \"message\": \"" + messages.get("dialog.objectSelection.failedToAddObjectAnErrorOccured") + "\"}";
		}
	}
	
}
