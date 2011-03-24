/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.jcr.renderer;

import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.query.render.RenderProperties;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SimpleCmsPropertyRenderer {
	
	@Autowired
	private BinaryChannelRenderer binaryChannelRenderer;
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private TopicRenderer topicRenderer;
	@Autowired
	private RepositoryUserRenderer repositoryUserRenderer;
	@Autowired
	private ContentObjectRenderer contentObjectRenderer;

	
	public Object renderValue(String propertyName, ValueType definitionValueType, Value value, 
			Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session, String locale, RenderProperties renderProperties, Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions) throws RepositoryException {
		switch (definitionValueType) {
		case Boolean:
				return value.getBoolean();
				
		case String:	
				return value.getString();

		case Date:
			String dateLocale = locale;
			if (dateLocale == null){
				//TODO This must be changed. A way must be found to 
				//identify the correct locale for dates in case
				//locale is not available.
				//Perhaps the correct way would be to let
				//java.util.Calendar to get the default locale for this instance
				// of the Java Virtual Machine.
				//Greek locale is chosen as all applications developed so far
				//are inside Greek Domain.
				dateLocale = CmsConstants.LOCALE_GREEK;
			}

			return DateUtils.addLocaleToCalendar(value.getDate(), dateLocale);

		case Double:
			return value.getDouble();

		case Long:
			return value.getLong();

		case ContentObject:
			return renderContentObject(value,cachedCmsRepositoryEntities, cachedContentObjectTypeDefinitions, session, renderProperties);

		case RepositoryUser:
			return renderRepositoryUser(value, cachedCmsRepositoryEntities, session, renderProperties);

		case Space:
			return renderSpace();

		case Topic: 
			return renderTopic(value, session, cachedCmsRepositoryEntities, renderProperties);

		case Binary:
			return binaryChannelRenderer.renderUnmanagedBinaryChannel(propertyName,	value.getString());

		default:
			return null;
		}
	}
	
	private ContentObject renderContentObject(Value value, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities,
			Map<String, ContentObjectTypeDefinition> cachedContentObjectTypeDefinitions,
			Session session, RenderProperties renderProperties) throws RepositoryException {

		String contentObjectIdAsString = value.getString();  

		if (cachedCmsRepositoryEntities != null && cachedCmsRepositoryEntities.containsKey(contentObjectIdAsString))
		{
			return (ContentObject)cachedCmsRepositoryEntities.get(contentObjectIdAsString);
		}
		else
		{
			Node contentObjectNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForContentObject(session, contentObjectIdAsString);

			if (contentObjectNode == null)
			{
				throw new ItemNotFoundException("Content object with id "+ contentObjectIdAsString);
			}

			ContentObject contentObject = contentObjectRenderer.render(session, contentObjectNode, renderProperties, 
						cachedContentObjectTypeDefinitions, cachedCmsRepositoryEntities);

			if (cachedCmsRepositoryEntities != null)
			{
				cachedCmsRepositoryEntities.put(contentObjectIdAsString, contentObject);
			}
			
			return contentObject;
		}
	}

	private Space renderSpace() 
	{
		return null;
	}

	public RepositoryUser renderRepositoryUser(Value value, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, Session session, RenderProperties renderProperties) throws   RepositoryException {

		//			Do not render RepositoryUser if it has already been rendered
		String repUserIdAsString = value.getString();
		if (cachedCmsRepositoryEntities != null && cachedCmsRepositoryEntities.containsKey(repUserIdAsString))
		{
			return (RepositoryUser)cachedCmsRepositoryEntities.get(repUserIdAsString);
		}
		else
		{
			RepositoryUser repUser = repositoryUserRenderer.renderRepositoryUserNode(repUserIdAsString, renderProperties, session, cachedCmsRepositoryEntities);

			if (cachedCmsRepositoryEntities != null)
			{
				cachedCmsRepositoryEntities.put(repUserIdAsString, repUser);
			}

			return repUser;
		}

	}
	
	public Topic renderTopic(Value value, Session session, Map<String, CmsRepositoryEntity> cachedCmsRepositoryEntities, RenderProperties renderProperties) throws   RepositoryException {

		//Do not render Topic if it has already been rendered
		String topicIdAsString = value.getString();
		if (cachedCmsRepositoryEntities != null && cachedCmsRepositoryEntities.containsKey(topicIdAsString))
		{
			return (Topic) cachedCmsRepositoryEntities.get(topicIdAsString);
		}
		else
		{
			Topic topic = topicRenderer.renderTopic(topicIdAsString,renderProperties, session, cachedCmsRepositoryEntities);				

			if (cachedCmsRepositoryEntities != null)
			{
				cachedCmsRepositoryEntities.put(topicIdAsString, topic);
			}
			
			return topic;
		}
	}

}
