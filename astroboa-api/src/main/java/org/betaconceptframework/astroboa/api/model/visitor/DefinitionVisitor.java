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

package org.betaconceptframework.astroboa.api.model.visitor;

import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.LocalizableCmsDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;

/**
 * Support of Visitor pattern on a {@link CmsDefinition definition}.
 * 
 * Contains visit methods for type definition, for complex property definition
 * and for simple property definitions.
 * 
 * Also provides methods for controlling visit to all levels of definition or to
 * its children only.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface DefinitionVisitor {

 	/**
	 * Visitor type enumeration.
	 */
	public enum VisitType {
		/**
		 * Visits definition recursively until no more child definitions are
		 * found.
		 */
		Full,
		/**
		 * Visits only definition's child definitions. This is applicable only 
		 * for {@link ContentObjectTypeDefinition type definitions} and
		 * {@link ComplexCmsPropertyDefinition complex  property definitions}.
		 * In {@link SimpleCmsPropertyDefinition simple property definitions}
		 * this type has no effect.
		 */
		Children,
		
		/**
		 * Visits current definition only and not its child definitions.
		 * This is applicable only 
		 * for {@link ContentObjectTypeDefinition type definitions} and
		 * {@link ComplexCmsPropertyDefinition complex  property definitions}.
		 * In {@link SimpleCmsPropertyDefinition simple property definitions}
		 * this type has no effect.
		 */
		Self
		
		
	}

	/**
	 * Sets the type of visitor. Default value is {@link VisitType#Full}.
	 * 
	 * @param visitType Visitor type.
	 */
	void setVisitType(VisitType visitType);
	/**
	 * Returns the type of visitor. Default value is {@link VisitType#Full}.
	 * 
	 * @return Visitor type.
	 */
	VisitType getVisitType();

	/**
	 * Visit a content object type definition.
	 * 
	 * @param contentObjectTypeDefinition
	 *            Content object type definition to visit.
	 */
	void visit(ContentObjectTypeDefinition contentObjectTypeDefinition);

	/**
	 * Visit a complex property definition.  
	 * 
	 * @param complexPropertyDefinition
	 *            Complex property definition to visit.
	 */
	void visitComplexPropertyDefinition(
			ComplexCmsPropertyDefinition complexPropertyDefinition);

	/**
	 * Visit a simple property definition.
	 * 
	 * @param <T> {@link SimpleCmsPropertyDefinition}.
	 * @param simplePropertyDefinition  
	 * 	Simple property definition to visit.
	 */
	<T> void visitSimplePropertyDefinition(
			SimpleCmsPropertyDefinition<T> simplePropertyDefinition);
	
	/**
	 * Inform that a visit to child definitions has started
	 * 
	 * @param parentDefinition Parent definition
	 */
	void startChildDefinitionsVisit(LocalizableCmsDefinition parentDefinition);
	
	/**
	 * Inform that a visit to child definitions has finished
	 * 
	 * @param parentDefinition Parent definition
	 */
	void finishedChildDefinitionsVisit(LocalizableCmsDefinition parentDefinition);

}
