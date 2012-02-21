/**
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

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.contexts.Contexts;

/**
 * This class proxies all the methods of ContentObjectEdit class that load a content object for editing.
 * The purpose of proxing is that prior method call we want to enforce the removal of the old contentObjectEdit instance 
 * from conversation scope and the creation of a fresh instance. In this way we avoid having a stale state that interferes 
 * with the new object to be edited.
 * So all EL expressions in XHTML pages and all classes outside the package are enforced to call the public methods in this class instead of the 
 * protected methods in ContentObjectEdit Class. 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@Name("objectEditInit")
@Scope(ScopeType.CONVERSATION)
public class ObjectEditInit {
	
	
	public String editObject_UIAction(String objectId) {
		Contexts.getConversationContext().remove("contentObjectEdit");
		Contexts.getConversationContext().remove("complexCmsPropertyEdit");
		Contexts.getConversationContext().flush();
		ContentObjectEdit contentObjectEdit = (ContentObjectEdit) Component.getInstance(ContentObjectEdit.class, true);
		contentObjectEdit.setSelectedContentObjectIdentifier(objectId);
		return contentObjectEdit.editContentObject_UIAction();
	}
	
	public String editNewObject_UIAction(String objectType) {
		Contexts.getConversationContext().remove("contentObjectEdit");
		Contexts.getConversationContext().remove("complexCmsPropertyEdit");
		Contexts.getConversationContext().flush();
		ContentObjectEdit contentObjectEdit = (ContentObjectEdit) Component.getInstance(ContentObjectEdit.class, true);
		contentObjectEdit.setContentObjectTypeForNewObject(objectType);
		return contentObjectEdit.editContentObject_UIAction();
	}
	
	public String editNewObject_UIAction(String objectType, Space spaceToCopyNewObject) {
		Contexts.getConversationContext().remove("contentObjectEdit");
		Contexts.getConversationContext().remove("complexCmsPropertyEdit");
		Contexts.getConversationContext().flush();
		ContentObjectEdit contentObjectEdit = (ContentObjectEdit) Component.getInstance(ContentObjectEdit.class, true);
		contentObjectEdit.setContentObjectTypeForNewObject(objectType);
		contentObjectEdit.setSpaceToCopyNewObject(spaceToCopyNewObject);
		return contentObjectEdit.editContentObject_UIAction();
	}
	
	public String editObjectFromDraft_UIAction(ContentObject contentObject) {
		Contexts.getConversationContext().remove("contentObjectEdit");
		Contexts.getConversationContext().remove("complexCmsPropertyEdit");
		Contexts.getConversationContext().flush();
		ContentObjectEdit contentObjectEdit = (ContentObjectEdit) Component.getInstance(ContentObjectEdit.class, true);
		return contentObjectEdit.editContentObjectFromDraft_UIAction(contentObject);
	}
	
	public void removeContentObjectEditBean() {
		Contexts.getConversationContext().remove("contentObjectEdit");
		// we should also remove the bean that handles the edit of complex properties
		Contexts.getConversationContext().remove("complexCmsPropertyEdit");
		Contexts.getConversationContext().flush();
	}
	
}
