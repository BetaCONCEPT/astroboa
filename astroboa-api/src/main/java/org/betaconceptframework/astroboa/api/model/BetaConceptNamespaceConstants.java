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

package org.betaconceptframework.astroboa.api.model;

/**
 * BetaConcept namespace constants.  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class BetaConceptNamespaceConstants {

	/**
	 * BetaConcept namespace URI.
	 */
	public final static String BETA_CONCEPT_URI = "http://www.betaconceptframework.org";

	/**
	 * BetaConcept namespace prefix.
	 */
	public final static String BETA_CONCEPT_PREFIX = "bc";

	/**
	 * BetaConcept CMS namespace URI.
	 */
	public final static String ASTROBOA_URI = BETA_CONCEPT_URI + "/astroboa";
	/**
	 * BetaConcept CMS namespace prefix.
	 */
	public final static String ASTROBOA_PREFIX = BETA_CONCEPT_PREFIX + "cms";
	
	/**
	 * This will be the new prefix which will replace the above
	 * For now it is used only in any XML element which follow the 
	 * types defined in astroboa-api.xsd
	 */
	public final static String ASTROBOA_NEW_PREFIX = "astroboa";

	/**
	 * ASTROBOA schema namespace URI.
	 */
	public final static String ASTROBOA_SCHEMA_URI = BETA_CONCEPT_URI + "/schema/astroboa";
	
	/**
	 * ASTROBOA schema api namespace URI.
	 */
	public final static String ASTROBOA_API_URI = ASTROBOA_SCHEMA_URI + "/api";

	/**
	 * ASTROBOA api namespace prefix.
	 */
	public final static String ASTROBOA_API_PREFIX = ASTROBOA_NEW_PREFIX+ "-api";

	/**
	 * ASTROBOA schema model namespace URI.
	 */
	public final static String ASTROBOA_MODEL_DEFINITION_URI = ASTROBOA_SCHEMA_URI + "/model";
	
	/**
	 * ASTROBOA model definition namespace prefix.
	 */
	public final static String ASTROBOA_MODEL_DEFINITION_PREFIX = ASTROBOA_PREFIX+ "model";

	/**
	 * ASTROBOA schema admin namespace URI.
	 */
	public final static String ASTROBOA_ADMIN_DEFINITION_URI = ASTROBOA_SCHEMA_URI + "/admin";
	
	/**
	 * ASTROBOA admin definition namespace prefix.
	 */
	public final static String ASTROBOA_ADMIN_DEFINITION_PREFIX = ASTROBOA_PREFIX+ "admin";
	
	/**
	 * ASTROBOA schema web namespace URI.
	 */
	public final static String ASTROBOA_WEB_DEFINITION_URI = ASTROBOA_SCHEMA_URI + "/web";
	
	/**
	 * ASTROBOA web definition namespace prefix.
	 */
	public final static String ASTROBOA_WEB_DEFINITION_PREFIX = ASTROBOA_PREFIX+ "web";
	
	/**
	 * ASTROBOA schema utilTypes namespace URI.
	 */
	public final static String ASTROBOA_UTILTYPES_DEFINITION_URI = ASTROBOA_SCHEMA_URI + "/utilTypes";
	
	/**
	 * ASTROBOA utilTypes definition namespace prefix.
	 */
	public final static String ASTROBOA_UTILTYPES_DEFINITION_PREFIX = ASTROBOA_PREFIX+ "utilTypes";
	
	/**
	 * Namespace URI for BetaConcept CMS mixin JCR node types.
	 */
	public final static String ASTROBOA_MIX_URI = ASTROBOA_URI + "/mix";
	/**
	 * Namespace prefix for BetaConcept CMS mixin JCR node types.
	 */
	public final static String ASTROBOA_MIX_PREFIX = ASTROBOA_PREFIX + "mix";
	
	/**
	 * BetaConcept CMS schema webservice namespace URI.
	 */
	public final static String ASTROBOA_WEBSERVICE_URI = ASTROBOA_SCHEMA_URI + "/webservice";

	/**
	 * BetaConcept CMS model webservice namespace prefix.
	 */
	public final static String ASTROBOA_WEBSERVICE_PREFIX = ASTROBOA_PREFIX+ "web";

}
