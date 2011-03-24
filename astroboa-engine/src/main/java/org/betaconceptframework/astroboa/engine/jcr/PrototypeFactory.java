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
package org.betaconceptframework.astroboa.engine.jcr;

import org.betaconceptframework.astroboa.engine.jcr.io.ImportBean;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean;
import org.betaconceptframework.astroboa.engine.jcr.util.PopulateComplexCmsProperty;
import org.betaconceptframework.astroboa.engine.jcr.util.PopulateContentObject;
import org.betaconceptframework.astroboa.engine.jcr.util.PopulateSimpleCmsProperty;
import org.betaconceptframework.astroboa.engine.jcr.visitor.ComplexCmsPropertyNodeRemovalVisitor;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public abstract class PrototypeFactory {

	//Lookup methods
	public abstract ComplexCmsPropertyNodeRemovalVisitor newComplexCmsPropertyNodeRemovalVisitor();
	public abstract PopulateContentObject newPopulateContentObject();
	public abstract PopulateComplexCmsProperty newPopulateComplexCmsProperty();
	public abstract PopulateSimpleCmsProperty newPopulateSimpleCmsProperty();
	public abstract SerializationBean newSerializationBean();
	public abstract ImportBean newImportBean();
}
