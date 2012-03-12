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
package org.betaconceptframework.astroboa.test.util;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.SpaceCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JcrUtils {
	
	public static String dumpNode(Node node, int depth) throws RepositoryException {

		StringBuffer buffer = new StringBuffer();

		String tabs = "\n"+ getTabs(depth);

		buffer.append(tabs+node.getPath());

		buffer.append(tabs+node.getName());

		dumpNode(node, buffer, depth);
		
		NodeDefinition definition = node.getDefinition();

		if (definition != null)

		{

			NodeType declaringNodeType = definition.getDeclaringNodeType();

			if (declaringNodeType != null)

				buffer.append(tabs+"Declaring Node Type\t " +declaringNodeType.getName());

		}

		



		NodeType primaryNodeType = node.getPrimaryNodeType();



		if (dumpSuperTypes(primaryNodeType.getSupertypes(), buffer, depth))

		{

//			Version History 

			VersionHistory versionHistory = node.getVersionHistory();

			if (versionHistory != null )

			{

				buffer.append(tabs+"Versions");

				

				VersionIterator allVers = versionHistory.getAllVersions();

				while (allVers.hasNext())

				{

					Version version = allVers.nextVersion();

					buffer.append(tabs+ version.getName());

					NodeIterator it = version.getNodes("jcr:frozenNode");

					while (it.hasNext())

					{	

						dumpNode(it.nextNode(), buffer, depth);

					}

				}

			}

			else

				dumpNode(node, buffer, depth);

		}

		else

			dumpNode(node, buffer, depth);

		

		return buffer.toString();

		

	}
	
	private static boolean dumpSuperTypes(NodeType[] superTypes, StringBuffer buffer, int depth) {



		boolean isVersionnable = false;

		String tabs = "\n"+getTabs(depth)+"\t";

		

		if (superTypes != null && superTypes.length > 0)

		{

			buffer.append(tabs+"Super Types");

			for (NodeType superType: superTypes)

			{

				buffer.append(tabs+superType.getName());

				if (superType.getName().equals(JcrBuiltInItem.MixVersionable.getJcrName()))

					isVersionnable = true;

			}

		}

		else 

			buffer.append(tabs+"No Super Types");

		

		return isVersionnable;

	}

	public static String getTabs(int depth) {

		String tab = "";

		for (int j=0; j<depth; j++)

			tab  = tab.concat("\t");

		

		return tab;

	}




	private static void dumpNode(Node node, StringBuffer buffer, int depth) throws RepositoryException, ValueFormatException {

		String tabs = "\n"+getTabs(depth);

		PropertyIterator properties = node.getProperties();

		while (properties.hasNext()) {

			Property property = properties.nextProperty();

			buffer.append(tabs+property.getName() + "=");

			if (property.getDefinition().isMultiple()) {

				Value[] values = property.getValues();

				for (int i = 0; i < values.length; i++) {

					if (i > 0) {

						buffer.append(",");

					}

					buffer.append(values[i].getString());

				}

			}

			else {

				if (property.getType() != PropertyType.BINARY)

					buffer.append(property.getString());

			}

		}

		

		NodeIterator nodes = node.getNodes();

		if (nodes.getSize() == 0)

			buffer.append(tabs+"No Children");

		else

		{

			buffer.append(tabs+"Chidren");

			while (nodes.hasNext()) {

				Node child = nodes.nextNode();

				buffer.append(tabs+dumpNode(child, ++depth));

			}

		}

	}




	public static Node retrieveUniqueNodeForContentObject(Session session, String contetnObjectId) throws Exception{
		
		ContentObjectCriteria criteria = CmsCriteriaFactory.newContentObjectCriteria();
		
		criteria.addIdEqualsCriterion(contetnObjectId);
		
		String xpathQuery = criteria.getXPathQuery();

		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpathQuery,  Query.XPATH);

		QueryResult queryResultForAllNodes = query.execute();
		
		return uniqueNode(queryResultForAllNodes);
	}


	public static Node uniqueNode(QueryResult cmsQueryResult) throws RepositoryException
	{
		if (cmsQueryResult == null || cmsQueryResult.getNodes() == null	)
			return null;
		
		long size = cmsQueryResult.getNodes().getSize();
		
		if (size <= 0)
			return null;
		
		if (size > 1)
			throw new CmsException("Not unique node");
		
		try {
			return cmsQueryResult.getNodes().nextNode();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
			
	}


	public static Node retrieveUniqueNodeForTopic(Session session,
			String referencedEntityId) throws Exception {
		TopicCriteria criteria = CmsCriteriaFactory.newTopicCriteria();
		
		criteria.addIdEqualsCriterion(referencedEntityId);
		
		String xpathQuery = criteria.getXPathQuery();

		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpathQuery,  Query.XPATH);

		QueryResult queryResultForAllNodes = query.execute();
		
		return uniqueNode(queryResultForAllNodes);
	}
	
	public static Node getTaxonomyJcrNode(Node topicNode) throws RepositoryException
	{
		Node taxonomyNode = topicNode;
		
		if (! taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))
		{
			//Try to find contentObjectNode
				taxonomyNode = topicNode.getParent();
				while (!taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))
					taxonomyNode = taxonomyNode.getParent();
				
		}
		
		//Just in case
		if (!taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))
			throw new ItemNotFoundException("Unable to find taxonomy for topic "+ topicNode.getPath());
		
		return taxonomyNode;
	}


	public static String getCmsIdentifier(Node node) throws Exception {
		return node.getProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()).getString();
	}


	public static Node retrieveUniqueNodeForSpace(Session session,
			String spaceId) throws Exception {
		SpaceCriteria criteria = CmsCriteriaFactory.newSpaceCriteria();
		
		criteria.addIdEqualsCriterion(spaceId);
		
		String xpathQuery = criteria.getXPathQuery();

		QueryManager queryManager = session.getWorkspace().getQueryManager();
		Query query = queryManager.createQuery(xpathQuery,  Query.XPATH);

		QueryResult queryResultForAllNodes = query.execute();
		
		return uniqueNode(queryResultForAllNodes);
	}
}
