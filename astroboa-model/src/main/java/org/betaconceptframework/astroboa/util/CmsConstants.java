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

package org.betaconceptframework.astroboa.util;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;

import org.betaconceptframework.astroboa.api.model.BetaConceptNamespaceConstants;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public final class CmsConstants {  
	
	public final static String ASTROBOA_VERSION = loadVersion();
	
	public final static String ASTROBOA_CONFIGURATION_XSD_FILENAME = "astroboa-conf-"+CmsConstants.ASTROBOA_VERSION+".xsd";
	
	public final static String LOCALE_GREEK = "el";
	public final static String BETACONCEPT_CONTENT_DEFINITION_SCHEMA_DIR = "astroboa.schemata.home.dir";
	public final static String TYPE_FOLDER = "TypeFolder";
	
	/**
	 * Application resources keys
	 */
	public final static String RELOAD_CONTENT_DEFINITION_FILE = "content.definition.file.reload";
	public final static String CMS_MODE = "cms.mode";
	
	public final static String AT_CHAR = "@";
	
	public final static String AMPERSAND = "&";
	
	public final static String QUESTION_MARK = "?";
	
	public final static String EQUALS_SIGN = "=";
	
	public final static String FORWARD_SLASH = "/";
	public final static String DOUBLE_FORWARD_SLASH = FORWARD_SLASH+FORWARD_SLASH;

	public final static String LEFT_PARENTHESIS = "(";
	public final static String RIGHT_PARENTHESIS = ")";
	public static final String LEFT_BRACKET = "[";
	public static final String RIGHT_BRACKET = "]";
	public final static String EMPTY_SPACE = " ";
	public final static String LEFT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE = EMPTY_SPACE+LEFT_PARENTHESIS+EMPTY_SPACE;
	public final static String RIGHT_PARENTHESIS_WITH_LEADING_AND_TRAILING_SPACE = EMPTY_SPACE+RIGHT_PARENTHESIS+EMPTY_SPACE;
	public static final String LEFT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE = EMPTY_SPACE+LEFT_BRACKET+EMPTY_SPACE;
	public static final String RIGHT_BRACKET_WITH_LEADING_AND_TRAILING_SPACE = EMPTY_SPACE+RIGHT_BRACKET+EMPTY_SPACE;

	public final static String ORDER_BY = "order by";
	public final static String ANY_NAME = "*";
	public final static String COMMA = ",";
	public final static String LIKE = "%";
	public final static String NOT = "not";
	public final static String ALL_ATTRIBUTES = AT_CHAR+ANY_NAME;
	
	public final static String QNAME_PREFIX_SEPARATOR = ":";
	public final static String FN_LOWER_CASE = "fn:lower-case";
	public final static String FN_UPPER_CASE = "fn:upper-case";
	
	public enum CmsMode
	{
		debug, 
		production;
	}

	public static final String PERIOD_DELIM = ".";
	public static final String ANY_TAXONOMY = "anyTaxonomy";
	/*
	 *  According to http://www.w3.org/TR/xmlschema-2/#dateTime
	 * lexical Representation of xs:dateTime is
	 * '-'? yyyy '-' mm '-' dd 'T' hh ':' mm ':' ss ('.' s+)? (zzzzzz)?
	 * 
	 * where zzzzzz represents the time zone which must have the form of
	 * +/- hh:mm | 'Z' as described in 3.2.7.3 Timezones.
	 * 
	 * However if zzzzzz pattern is used in java.text.SimpleDateFormat, then time zone
	 * produced will be different than the expected format, as SimpleDateFormat translates
	 * 'zzzzzz' to time zone description, for example Eastern European Time than +03:00 .
	 * 
	 * Moreover SimpleDateFormat does not support formatting time zone according to ISO8601
	 * at all.
	 * 
	 * Nevertheless, date time pattern will contain one 'Z' as an indicator about TimeZone
	 * according to SimpleDateFormat which is the only specific class provided by Java.
	 * 
	 * Astroboa implementation guarantees that the generated string representations of all dates
	 * inside an XML will follow the format accepted by XML specification.
	 * 
	 */ 
	public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";	
	public static final String DATE_PATTERN = "yyyy-MM-dd";
	
	
	public final static String PORTAL_CONTENT_OBJECT_TYPE = "portalObject";
	public final static String PORTAL_SECTION_CONTENT_OBJECT_TYPE = "portalSectionObject";
	public final static String QUERY_CONTENT_OBJECT_TYPE = "queryObject";
	
	public final static String DYNAMIC_CONTENT_AREA_CONTENT_OBJECT_TYPE = "dynamicContentAreaObject";
	
	public final static String SCHEDULED_CONTENT_AREA_CONTENT_OBJECT_TYPE = "scheduledContentAreaObject";

	public final static String CONTENT_API_FOR_DEFINITION_SCHEMA = "/f/definitionSchema/repository";
	public final static String CONTENT_API_FOR_BINARY_CHANNEL = "/f/binaryChannel";
	
	public final static String ASTROBOA_MODEL_SCHEMA_FILENAME = "astroboa-model";
	public final static String ASTROBOA_API_SCHEMA_FILENAME = "astroboa-api";
	
	public static final String XML_SCHEMA_LOCATION = "http://www.w3.org/2001/03/xml.xsd";
	public static final String XML_SCHEMA_DTD_LOCATION = "http://www.w3.org/2001/03/XMLSchema.dtd";
	public static final String XML_DATATYPES_DTD_LOCATION  = "http://www.w3.org/2001/03/datatypes.dtd";
	
	public final static String REPOSITORY_ELEMENT_NAME = "repository";
	public final static String REPOSITORY_PREFIXED_NAME = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_PREFIX+":"+REPOSITORY_ELEMENT_NAME;
	public final static String REPOSITORY_ID_ATTRIBUTE_NAME = "id";
	public final static String REPOSITORY_SERIALIZATION_CREATION_DATE_ATTRIBUTE_NAME = "created";
	public final static String RESOURCE_RESPONSE = "resourceResponse";
	public final static String RESOURCE_RESPONSE_PREFIXED_NAME = BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_API_PREFIX+":"+RESOURCE_RESPONSE;
	public final static String RESOURCE_COLLECTION = "resourceCollection";
	public final static String RESOURCE = "resource";
	public final static String TOTAL_RESOURCE_COUNT = "totalResourceCount";
	public final static String OFFSET = "offset";
	public final static String LIMIT = "limit";
	public static final String ROOT_TOPICS = "rootTopics";
	public static final String CHILD_TOPICS = "childTopics";
	public static final String CHILD_SPACES = "childSpaces";
	public static final String CHILD_DEFINITIONS = "childDefinitions";
	public static final String SUPER_TYPES = "superTypes";
	public static final String OWNER_ELEMENT_NAME = "owner";
	public static final String URL_ATTRIBUTE_NAME = "url";
	public static final String NUMBER_OF_CHILDREN_ATTRIBUTE_NAME = "numberOfChildren";
	public static final String PARENT_TOPIC = "parentTopic";
	public static final String PARENT_SPACE = "parentSpace";
	public static final String LOCALIZED_LABEL_ELEMENT_NAME = "label";
	public static final String LANG_ATTRIBUTE_NAME = "lang";
	public static final String LANG_ATTRIBUTE_NAME_WITH_PREFIX = XMLConstants.XML_NS_PREFIX+CmsConstants.QNAME_PREFIX_SEPARATOR+CmsConstants.LANG_ATTRIBUTE_NAME;
	public static final String LAST_MODIFICATION_DATE_ATTRIBUTE_NAME = "lastModificationDate";
	public static final String CONTENT_ELEMENT_NAME = "content";
	public static final String REPOSITORY_USERS_ELEMENT_NAME ="repositoryUsers";
	public static final String TAXONOMIES_ELEMENT_NAME ="taxonomies";
	public static final String CONTENT_OBJECTS_ELEMENT_NAME ="contentObjects";
	public static final String TITLE_ELEMENT_NAME = "title";
	public static final String PROFILE_ELEMENT_NAME = "profile";
	public static final String ARRAY_OF_OBJECT_TYPE_ELEMENT_NAME = "arrayOfObjectTypes";
	public static final String OBJECT_TYPE_ELEMENT_NAME = "objectType";
	public static final String ARRAY_OF_PROPERTIES_ELEMENT_NAME = "arrayOfProperties";
	public static final String PROPERTY_ELEMENT_NAME = "property";
	//This flag is used when exporting content to JSON format to indicate whether exported json object should be 
	//exported as an array or not. This is mostly useful in cases where json object contains only one value
	//abut must be exported as an array which has a single item rather than a single value object.
	//This flag is the name of a fake attribute passed in JSONXmlStreamWriter (located in astroboa-model module)
	public static final String EXPORT_AS_AN_ARRAY_INSTRUCTION =BetaConceptNamespaceConstants.BETA_CONCEPT_CMS_PREFIX+"exportAsAnArrayInstruction";
	
	/**
	 * The actual value for ${version} will be provided during process-resources phase by Maven resource plugin.
	 * ${version} corresponds to the version of this module which is the same with Astroboa version
	 */
	public final static String ASTROBOA_MODEL_SCHEMA_FILENAME_WITH_VERSION = ASTROBOA_MODEL_SCHEMA_FILENAME+"-"+ASTROBOA_VERSION+".xsd";
	public final static String ASTROBOA_API_SCHEMA_FILENAME_WITH_VERSION = ASTROBOA_API_SCHEMA_FILENAME+"-"+ASTROBOA_VERSION+".xsd";

	public static enum ContentObjectStatus {
		authored,
		submitted,
		approved,
		rejected,
		temporarilyRejectedForReauthoring,
		scheduledForPublication,
		published,
		publishedAndArchived,
		staged,
		submittedByExternalUser,
		archived
	}

	public final static Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	public final static String SYSTEM_NAME_ACCEPTABLE_CHARACTERS = "A-Za-z0-9_\\-\\.:";
	public final static String SYSTEM_NAME_REG_EXP = "[" + SYSTEM_NAME_ACCEPTABLE_CHARACTERS + "]+";
	public final static Pattern SystemNamePattern = Pattern.compile(SYSTEM_NAME_REG_EXP);
	// the "%" is required because ":" is a reserved character and thus should be used in its URL encoded form in URIs.
	// In the mean time it seams that many modern browsers accept and transmit the character without the need 
	// to have it encoded. So there is a possibility to receive the character in both its normal and its URL Encoded form
	// "%3A"is the URL Encoded form of ":" but we do not explicitly put it in the regular expression due a mysterious behavior
	// of resteasy that renders any following regexp non functioning when the "%3A" is explicitly inserted in this regular expression 
	public final static String SYSTEM_NAME_REG_EXP_FOR_RESTEASY = "[A-Za-z0-9_\\-\\.:%]+";
	public final static Pattern SystemNamePatternForResteasy = Pattern.compile(SYSTEM_NAME_REG_EXP_FOR_RESTEASY);
	
	public final static String PROPERTY_PATH_REG_EXP = "[A-Za-z0-9_\\-]+(\\[[0-9]+\\])?(\\.[A-Za-z0-9_\\-]+(\\[[0-9]+\\])?)*";
	public final static Pattern PropertyPathPattern = Pattern.compile(PROPERTY_PATH_REG_EXP);
	public final static String PROPERTY_PATH_REG_EXP_FOR_RESTEASY = "[A-Za-z0-9_\\-]+(%5B[0-9]+%5D)?(\\.[A-Za-z0-9_\\-]+(%5B[0-9]+%5D)?)*";
	public final static Pattern PropertyPathPatternForResteasy = Pattern.compile(PROPERTY_PATH_REG_EXP_FOR_RESTEASY);
	
	public final static String UUID_REG_EXP = "[0-9abcdef]{8}\\-[0-9abcdef]{4}\\-[0-9abcdef]{4}\\-[0-9abcdef]{4}\\-[0-9abcdef]{12}";
	public final static Pattern UUIDPattern = Pattern.compile(UUID_REG_EXP);
	
	public final static String UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY = UUID_REG_EXP + "|" + SYSTEM_NAME_REG_EXP_FOR_RESTEASY;
	public final static Pattern UUIDorSystemNamePatternForResteasy = Pattern.compile(UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY);

	public final static String INDEX_REG_EXP = "[0-9]+";
	public final static String UUID_REG_EXP_OR_INDEX_REG_EXP = "(("+UUID_REG_EXP + ")|"+ INDEX_REG_EXP + ")";
	
	public final static String PROPERTY_PATH_WITH_ID_REG_EXP_FOR_RESTEASY = "[A-Za-z0-9_\\-]+(%5B"+UUID_REG_EXP_OR_INDEX_REG_EXP+"%5D)?(\\.[A-Za-z0-9_\\-]+(%5B"+UUID_REG_EXP_OR_INDEX_REG_EXP+"%5D)?)*";
	
	//public final static String PROPERTY_PATH_WITH_ID_REG_EXP_FOR_RESTEASY = "[A-Za-z0-9_\\-]+(%5B"+UUID_REG_EXP+"+%5D)?(\\.[A-Za-z0-9_\\-]+(%5B"+UUID_REG_EXP+"+%5D)?)*(\\.[A-Za-z0-9_\\-]+(%5B"+UUID_REG_EXP+ "|" + "[0-9]++%5D)?)";
	
	
//	public final static String TOPIC_PATH_WITH_UUIDS_OR_SYSTEM_NAMES_REG_EXP = UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "(/" + UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + ")*";
	public final static String TOPIC_PATH_WITH_UUIDS_OR_SYSTEM_NAMES_REG_EXP = UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "(/(" + UUID_OR_SYSTEM_NAME_REG_EXP_FOR_RESTEASY + "))*";
	
	public static final String UNMANAGED_DATASTORE_DIR_NAME = "UnmanagedDataStore";
	public static final String SERIALIZATION_DIR_NAME = "serializations";
	
	public static final String CONTENT_DISPOSITION_TYPE = "contentDispositionType";

	public static final String CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX = "@";
	public static final String TOPIC_REFERENCE_CRITERION_VALUE_PREFIX = "#";
	public final static String INCLUDE_CHILDREN_EXPRESSION = CmsConstants.FORWARD_SLASH+CmsConstants.ANY_NAME;

	
	private static String loadVersion() {
		ResourceBundle resourceBundle = ResourceBundle.getBundle("astroboa");
		
		String version = null;
		
		if (resourceBundle!=null){
			version = resourceBundle.getString("astroboa.version");
		}
		
		if (version == null || version.trim().isEmpty()){
			throw new CmsException("Could not load file astroboa.properties and therefore cannot retrieve Astroboa version");
		}
		
		return version;
	}
	
	
	public static final String RESOURCE_API_CONTENT_URI_PATH = CmsConstants.FORWARD_SLASH+"contentObject";
	public static final String RESOURCE_API_TAXONOMY_URI_PATH = CmsConstants.FORWARD_SLASH+"taxonomy";
	public static final String RESOURCE_API_TOPIC_URI_PATH = CmsConstants.FORWARD_SLASH+"topic";
	public static final String RESOURCE_API_BINARY_CHANNEL_URI_PATH = CmsConstants.FORWARD_SLASH+"binaryChannel";
	public static final String RESOURCE_API_MODEL_URI_PATH = CmsConstants.FORWARD_SLASH+"model";
	public static final String RESOURCE_API_ENCRYPTION_UTILITY_URI_PATH = CmsConstants.FORWARD_SLASH+"encrypt";
	
}
