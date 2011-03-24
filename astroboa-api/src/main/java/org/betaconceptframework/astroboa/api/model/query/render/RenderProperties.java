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

package org.betaconceptframework.astroboa.api.model.query.render;

import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;

/**
 * Represents instructions on how to render {@link CmsRepositoryEntity entities}
 * which appear in query results.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface RenderProperties {
		
	/**
	 * Clear all render instructions.  
	 */
	void resetRenderInstructions();
	
	/**
	 * Adds a render instruction.
	 * @param renderInstruction A render instruction.
	 * @param value Render instruction value.
	 * 
	 * @deprecated Use specific methods for each render instruction
	 */
	void addRenderInstruction(RenderInstruction renderInstruction, 
			Object value);
	
	/**
	 * Returns all render instructions.
	 * 
	 * @return Render instructions.
	 */
	Map<RenderInstruction, Object> getRenderInstructions();
	
	/**
	 * Returns value provided for specified render instruction.
	 * 
	 * @param renderInstruction Render instruction.
	 * @return Value for render instruction of null if no render 
	 * instruction is provided.
	 * 
	 * @deprecated Use specific methods for each render instruction
	 */
	Object getValueForRenderInstruction(RenderInstruction renderInstruction);
	
	/**
	 * Control lazy loading mechanism when query results contain {@link ContentObject}s.
	 * 
	 * @param renderAllContentObjectProperties <code>true</code> will load all
	 * content object properties, <code>false</code> will enable lazy loading, 
	 * that is, a content object property will be available only when required. 
	 */
	void renderAllContentObjectProperties(boolean renderAllContentObjectProperties);
	
	/**
	 * Check if lazy loading has been disabled.
	 *  
	 * @return <code>true</code> if lazy loading is disabled, <code>false</code> otherwise
	 */
	boolean allContentObjectPropertiesAreRendered();
	
	/**
	 * Specify the version of a {@link ContentObject} to render. 
	 * 
	 * <p>
	 * <code>Blank</code> value (<code>null</code> or empty {@link String}) corresponds
	 * to latest version.
	 * </p>
	 * 
	 * @param versionName Version of content object to render, <code>null</code> indicates that 
	 * the latest version of a content object will be rendered, which is the default
	 */
	void renderVersionForContentObject(String versionName);
	
	/**
	 * Get the version which will be used to render {@link ContentObject}s.
	 * 
	 * <code>null</code> stands for latest version.
	 * 
	 * @return Version of {@link ContentObject}, or <code>null</code> for no version
	 */
	String getVersionUsedToRender();
	
	/**
	 * Control whether to render parent entity or not.
	 * 
	 * <p>
	 * When query results contain {@link Topic}s or {@link Space}s, sometimes it is not 
	 * required that their parent should be returned. 
	 * </p> 
	 * 
	 * <p>
	 * Default behavior is that  parent entity is rendered only when it is required. 
	 * </p>
	 * 
	 * @param renderParent <code>true</code> to render parent entity, <code>false</code> otherwise
	 * which is the default.
	 */
	void renderParentEntity(boolean renderParent);
	
	/**
	 * Check whether parent entity is rendered or not
	 * 
	 * @return <code>true</code> if parent entity is rendered, <code>false</code> otherwise
	 */
	boolean isParentEntityRendered();
	
	/**
	 * In a multilingual environment, a property may have
	 * more than one values. Use this method to instruct Astroboa
	 * to render values for the specified list of languages.
	 * 
	 * A blank list (<code>null</code> or empty) indicates that
	 * all values should be rendered.
	 * 
	 * @param locales Locale value as defined in {@link Localization}.
	 */
	void renderValuesForLocales(List<String> locales);
	
	/**
	 * Convenient method when only one language is needed.
	 * 
	 * It replaces any existing locale.
	 * 
	 * @param locale Locale value as defined in {@link Localization}.
	 */
	void renderValuesForLocale(String locale);
	
	/**
	 * Get the locales used when rendering multingual properties
	 * 
	 * @return Always a not null list.
	 */
	List<String> getLocalesUsedForRender();

	/**
	 * Convenient method to retrieve the specified locale.
	 * 
	 * Must be used when only one locale is provided.
	 * 
	 * @return The first locale found in locale list
	 */
	String getFirstLocaleUsedForRender();
	
	/**
	 * Instruct renderer to serialize content objects with the same element name
	 * instead of their content type name. 
	 * 
	 * <p>
	 * Useful only when XML serialization is enabled.
	 * </p>
	 * 
	 * @param serializeContentObjectsUsingTheSameNameAsCollectionItemName <code>true</code> to export content object collection with
	 * the same name, <code>false</code> otherwise.
	 */
	void serializeContentObjectsUsingTheSameNameAsCollectionItemName(boolean serializeContentObjectsUsingTheSameNameAsCollectionItemName);
	
	/**
	 * Check whether content object are serialized with the same element name
	 * 
	 * @return <code>true</code> if content object collection is serialized with
	 * the same name, <code>false</code> otherwise.
	 */
	boolean areSerializeContentObjectsUsingTheSameNameAsCollectionItemName();
	
	/**
	 * Instruct Astroboa to pretty print resource representation. 
	 * Applicable only when resource representation type is either XML or JSON.
	 * 
	 * @param enable <code>true</code> to pretty print XML or JSON, <code>false</code> otherwise
	 */
	void prettyPrint(boolean enable);
	
	/**
	 * Check whether pretty print is enabled or not
	 * 
	 * @return <code>true</code> if pretty print is enabled, <code>false</code> otherwise
	 */
	boolean isPrettyPrintEnabled();
}
