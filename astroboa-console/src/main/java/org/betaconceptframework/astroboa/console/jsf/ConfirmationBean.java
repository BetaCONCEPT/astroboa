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
package org.betaconceptframework.astroboa.console.jsf;

import java.lang.reflect.Method;

import javax.faces.application.FacesMessage;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This bean is used along with WEB-INF/pageComponents/confirmationDialog.xhtml
 * 
 * in order to successfully pop up a confirmation dialog box
 * and upon confirmation to execute proper action.
 *
 * There is also the possibility to execute a post action as well, regardless if user has clicked on OK
 * or Cancel button. That is you may choose to execute one action if user clicks OK and right after that to execute
 * another action.
 *  
 * In order to activate this utility, one must take the following steps :
 * 
 * 1. Include in your page, facelet WEB-INF/pageComponents/confirmationDialog.xhtml
 * 
 * 2. On the component that will trigger the action (in this example an a4j:commandLink)
 * user must specify attribute action with this bean method initiateConfirmation with the following parameters
 * 
 *  	a. The bean instance which contains the method that will be called upon confirmation
 *      b. The name of the method to be executed
 *      c. A customized label to be displayed in the confirmation dialog box
 *      d. A comma delimited string containing one or more component ids to be re rendered once action is completed, or null value
 *      e. The arguments needed by the method to be executed separated by comma, or null if none is required
 *      f. A javascript function to run when the "ok" action and the page re-rendering have finished. If none is provided then by default
 *      a function that hides the dialogue is run. If you provide a function you do not need to take care hiding the dialog. This will be taken care
 *      automatically
 *      
 * 2.5 If you need to execute a post action as well you need the above parameters plus the following parameters for the post action
 * 		
 * 		a. The bean instance which contains the method that will be called after confirmation action has been performed or after user has clicked Cancel
 *      b. The name of the method to be executed 
 *      c. An array of the arguments needed by the method to be executed, or null if none is required
 *      
 * 3. On the component that will trigger the action, user must specify attribute oncomplete in order for the panel to be displayed
 * 
 * 4. On the component that will trigger the action, user must specify the reRender attribute with the value 
 * "confirmationDialogPanel" in order for the panel to display the proper messages.
 * 
 *  For example a click to the following link will create an instance of this bean and store
 *  all necessary objects needed to complete action if confirmation is granted.
 *  When user confirms the action, method 'permanentlyRemoveSelectedContentObject_UIAction' 
 *  is executed with the proper arguments and upon successful execution components 
 *  'ajaxDynamicAreaPanel, topicTree, cmsTree, clipboardTable' are reRendered.
 * <pre>
 * <a4j:commandLink
		action="#{confirmationBean.initiateConfirmation('dialog.confirmation.delete.question',
				contentObjectList, 
				'permanentlyRemoveSelectedContentObject_UIAction',
				'My Custom Message', 
				'ajaxDynamicAreaPanel, topicTree, cmsTree, clipboardTable',
				contentObjectUIWrapper.contentObject.id,contentObjectUIWrapper.contentObject.contentObjectType,contentObjectUIWrapper.contentObjectProperty['profile.created'].simpleTypeValue)}"
		 		oncomplete="openConfirmationDialog();"
		 reRender="confirmationDialogPanel">
			Link label
	</a4j:commandLink>
 * </pre>
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@Name("confirmationBean")
@Scope(ScopeType.PAGE)
public class ConfirmationBean {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Object beanWhoseMethodWillBeCalledOnClickOKButton;

	private String methodNameToBeExecutedOnClickOKButton;

	private Object[] argumentsOfMethodToBeExecutedOnClickOKButton;

	private Object beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton;

	private String methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton;

	private Object[] argumentsOfMethodToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton;

	private String customizedConfirmationMessage;
	
	private String commaDelimitedReRenderComponentIds;

	private String messageKey;
	
	private String oncompleteJsFunction = "closeConfirmationDialog();";
	
	public void proceedWithAction_UIAction(){


		if (beanWhoseMethodWillBeCalledOnClickOKButton != null){

			if (StringUtils.isBlank(methodNameToBeExecutedOnClickOKButton)){
				logger.warn("No method name is available for bean {}.Action cannot procceed", beanWhoseMethodWillBeCalledOnClickOKButton.getClass().getName());
				actionFail();
				return;
			}

			//Find bean method
			Method beanMethod = retrieveMethod(beanWhoseMethodWillBeCalledOnClickOKButton,methodNameToBeExecutedOnClickOKButton);

			if (beanMethod == null){
				logger.warn("No method found for name {} in bean {}.Action cannot procceed",
						methodNameToBeExecutedOnClickOKButton,
						beanWhoseMethodWillBeCalledOnClickOKButton.getClass().getName());
				actionFail();
				return;
			}

			
			//Invoke method
			try{
				
				beanMethod.invoke(beanWhoseMethodWillBeCalledOnClickOKButton, argumentsOfMethodToBeExecutedOnClickOKButton);
				
				//all went well
				//Do not clear component ids to be rendered
				//as they are used after this method is completed
				proceedWithLastAction(false);
				
			}
			catch(Exception e){
				logger.error("Exception while invoking method "+methodNameToBeExecutedOnClickOKButton
						+" in bean "+beanWhoseMethodWillBeCalledOnClickOKButton.getClass().getName(),
						e);
				actionFail();
			}

		}
		else{
			logger.warn("No bean is available. Action cannot procceed");
			actionFail();
		}
	}
	
	private void proceedWithLastAction(boolean clearReRenderIds){

		if (beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton == null)
		{
			clear(clearReRenderIds);
			return;
		}

		if (StringUtils.isBlank(methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton)){
			logger.warn("No method name is available for bean {}.Action cannot procceed", beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton.getClass().getName());
			actionFail();
			return;
		}

		//Find bean method
		Method beanMethod = retrieveMethod(beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton,methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton);

		if (beanMethod == null){
			logger.warn("No method found for name {} in bean {}.Action cannot procceed",
					methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton,
					beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton.getClass().getName());
			actionFail();
			return;
		}


		//Invoke method
		try{

			beanMethod.invoke(beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton, argumentsOfMethodToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton);

			clear(clearReRenderIds);
		}
		catch(Exception e){
			logger.error("Exception while invoking method "+methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton
					+" in bean "+beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton.getClass().getName(),
					e);
			actionFail();
		}

	}


	private void actionFail() {
		JSFUtilities.addMessage(null,
				"application.unknown.error.message",
				null,
				FacesMessage.SEVERITY_WARN);
		clear(true);
	}

	
	public void clear_UIAction() {
		proceedWithLastAction(true);
	}
	
	private void clear(boolean clearComponentIdsToBeRendered) {
		beanWhoseMethodWillBeCalledOnClickOKButton=null;
		beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton=null;
		methodNameToBeExecutedOnClickOKButton=null;
		methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton=null;
		argumentsOfMethodToBeExecutedOnClickOKButton = null;
		argumentsOfMethodToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton = null;
		customizedConfirmationMessage = null;
		messageKey=null;

		if (clearComponentIdsToBeRendered){
			commaDelimitedReRenderComponentIds = null;
		}
	}

	public void initiateConfirmation(
			String messageKey, 
			Object beanWhoseMethodWillBeCalledOnClickOKButton, 
			String methodNameToBeExceutedOnClickOKButton,
			String customizedConfirmationMessage, 
			String reRenderComponentIds,
			String oncompleteJsFunction,
			Object... argumentsOfMethodToBeExecutedOnClickOKButton){
		
		initiateConfirmationWithActionOnOKAndFinalAction(messageKey, 
				beanWhoseMethodWillBeCalledOnClickOKButton, methodNameToBeExceutedOnClickOKButton, argumentsOfMethodToBeExecutedOnClickOKButton, 
				null, null, null, 
				customizedConfirmationMessage, 
				reRenderComponentIds,
				oncompleteJsFunction);
	}
	
	public void initiateConfirmationWithActionOnOKAndFinalAction(
			String messageKey, Object beanWhoseMethodWillBeCalledOnClickOKButton, 
			String methodNameToBeExceutedOnClickOKButton,
			Object[] argumentsOfMethodToBeExecutedOnClickOKButton,
			Object beanWhoseMethodWillBeCalledOnClickCancelButton, 
			String methodNameToBeExceutedOnClickCancelButton,
			Object[] argumentsOfMethodToBeExecutedOnClickCancelButton,
			String customizedConfirmationMessage, 
			String reRenderComponentIds, 
			String oncompleteJsFunction){
		
		this.messageKey = messageKey;
		this.beanWhoseMethodWillBeCalledOnClickOKButton = beanWhoseMethodWillBeCalledOnClickOKButton;
		this.methodNameToBeExecutedOnClickOKButton = methodNameToBeExceutedOnClickOKButton;
		this.argumentsOfMethodToBeExecutedOnClickOKButton =argumentsOfMethodToBeExecutedOnClickOKButton;
		this.beanWhoseMethodWillBeCalledAfterMethodFromOKButtonOrActionFromCancelButton = beanWhoseMethodWillBeCalledOnClickCancelButton;
		this.methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton = methodNameToBeExceutedOnClickCancelButton;
		this.argumentsOfMethodToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton =argumentsOfMethodToBeExecutedOnClickCancelButton;
		this.customizedConfirmationMessage = customizedConfirmationMessage;
		if (StringUtils.isNotBlank(oncompleteJsFunction)) {
			this.oncompleteJsFunction = oncompleteJsFunction + "; " + this.oncompleteJsFunction;
		}
		
		if (StringUtils.isNotBlank(reRenderComponentIds))
		{
			this.commaDelimitedReRenderComponentIds = reRenderComponentIds;
		}
		else
		{
			this.commaDelimitedReRenderComponentIds = null;
		}
	}
	
	
	/**
	 * @return the customizedConfirmationMessage
	 */
	public String getCustomizedConfirmationMessage() {
		return customizedConfirmationMessage;
	}

	

	/**
	 * @return the commaDelimitedReRenderComponentIds
	 */
	public String getCommaDelimitedReRenderComponentIds() {
		return commaDelimitedReRenderComponentIds;
	}


	/**
	 * @return the messageKey
	 */
	public String getMessageKey() {
		return messageKey;
	}
	
	public Object[] getEmptyArgumentArray() {
		return new Object[0];
	}


	public Object[] getArrayForCommaDelimitedObjects(Object... objects )
	{
		if (objects == null || objects.length == 0)
		{
			return getEmptyArgumentArray();
		}
		
		return objects;
	}
	private Method retrieveMethod(Object bean, String methodName) {
		Method[] beanMethods = bean.getClass().getMethods();


		for (Method beanMethod: beanMethods){

			if (methodName.equals(beanMethod.getName())){
				return beanMethod;
			}
		}

		return null;
	}

	public String getMethodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton() {
		return methodNameToBeExecutedAfterMethodFromOKButtonOrActionFromCancelButton;
	}

	public String getOncompleteJsFunction() {
		return oncompleteJsFunction;
	}
}
