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
	public final static String BETA_CONCEPT_CMS_URI = BETA_CONCEPT_URI + "/astroboa";
	/**
	 * BetaConcept CMS namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_PREFIX = BETA_CONCEPT_PREFIX + "cms";

	/**
	 * BetaConcept CMS schema namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_SCHEMA_URI = BETA_CONCEPT_URI + "/schema/astroboa";
	
	/**
	 * BetaConcept CMS schema api namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_API_URI = BETA_CONCEPT_CMS_SCHEMA_URI + "/api";

	/**
	 * BetaConcept CMS api namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_API_PREFIX = BETA_CONCEPT_CMS_PREFIX+ "api";

	/**
	 * BetaConcept CMS schema model namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_MODEL_DEFINITION_URI = BETA_CONCEPT_CMS_SCHEMA_URI + "/model";
	/**
	 * BetaConcept CMS model definition namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_MODEL_DEFINITION_PREFIX = BETA_CONCEPT_CMS_PREFIX+ "model";

	/**
	 * BetaConcept CMS schema admin namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_ADMIN_DEFINITION_URI = BETA_CONCEPT_CMS_SCHEMA_URI + "/admin";
	/**
	 * BetaConcept CMS admin definition namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_ADMIN_DEFINITION_PREFIX = BETA_CONCEPT_CMS_PREFIX+ "admin";
	
	/**
	 * BetaConcept CMS schema web namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_WEB_DEFINITION_URI = BETA_CONCEPT_CMS_SCHEMA_URI + "/web";
	/**
	 * BetaConcept CMS web definition namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_WEB_DEFINITION_PREFIX = BETA_CONCEPT_CMS_PREFIX+ "web";
	
	/**
	 * BetaConcept CMS schema utilTypes namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_UTILTYPES_DEFINITION_URI = BETA_CONCEPT_CMS_SCHEMA_URI + "/utilTypes";
	/**
	 * BetaConcept CMS utilTypes definition namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_UTILTYPES_DEFINITION_PREFIX = BETA_CONCEPT_CMS_PREFIX+ "utilTypes";
	
	/**
	 * Namespace URI for BetaConcept CMS mixin JCR node types.
	 */
	public final static String BETA_CONCEPT_CMS_MIX_URI = BETA_CONCEPT_CMS_URI + "/mix";
	/**
	 * Namespace prefix for BetaConcept CMS mixin JCR node types.
	 */
	public final static String BETA_CONCEPT_CMS_MIX_PREFIX = BETA_CONCEPT_CMS_PREFIX + "mix";
	
	/**
	 * BetaConcept CMS schema webservice namespace URI.
	 */
	public final static String BETA_CONCEPT_CMS_WEBSERVICE_URI = BETA_CONCEPT_CMS_SCHEMA_URI + "/webservice";

	/**
	 * BetaConcept CMS model webservice namespace prefix.
	 */
	public final static String BETA_CONCEPT_CMS_WEBSERVICE_PREFIX = BETA_CONCEPT_CMS_PREFIX+ "web";

}
