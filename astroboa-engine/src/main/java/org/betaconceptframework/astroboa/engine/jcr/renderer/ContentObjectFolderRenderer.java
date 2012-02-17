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
package org.betaconceptframework.astroboa.engine.jcr.renderer;


import java.util.Locale;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO9075;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder;
import org.betaconceptframework.astroboa.api.model.ContentObjectFolder.Type;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.engine.jcr.dao.ContentDefinitionDao;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.model.impl.ContentObjectFolderImpl;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.CmsUtils;
import org.betaconceptframework.astroboa.util.TreeDepth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ContentObjectFolderRenderer {

	@Autowired
	private ContentDefinitionDao contentDefinitionDao;
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;
	
	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	
	private final Logger logger = LoggerFactory.getLogger(ContentObjectFolderRenderer.class);
	
	public  ContentObjectFolder render(Session session, Node contentObjectFolderNode, int depth, boolean renderContentObjectIds, String locale) throws Exception {

		ContentObjectFolder contentObjectFolder = new ContentObjectFolderImpl();

		contentObjectFolder.setId(contentObjectFolderNode.getUUID());
		contentObjectFolder.setType(renderType(contentObjectFolderNode));
		((ContentObjectFolderImpl)contentObjectFolder).setFullPath(getFullPath(contentObjectFolderNode, contentObjectFolder.getType()));

		final String name = contentObjectFolderNode.getName();
		if (contentObjectFolder.getType() == ContentObjectFolder.Type.CONTENT_TYPE){
			//Extract type from node name
			String typeName = StringUtils.remove(name,CmsConstants.TYPE_FOLDER);

			contentObjectFolder.setName(typeName);
			
			//Search for definition for this type and get localized label
			ContentObjectTypeDefinition typeDefinition = contentDefinitionDao.getContentObjectTypeDefinition(typeName);
			
			if (typeDefinition == null){
				logger.warn("Unable to render localized label for content object folder {} for type {}",name, typeName);
				contentObjectFolder.setLocalizedLabelForCurrentLocale(name);
			}
			else{
				if (StringUtils.isBlank(locale)){
					locale = Locale.ENGLISH.toString();
				}
				
				contentObjectFolder.setLocalizedLabelForCurrentLocale(typeDefinition.getDisplayName().getLocalizedLabelForLocale(locale));
			}
		}
		else{
			String label = name;
			
			if ((contentObjectFolder.getType() == ContentObjectFolder.Type.DAY ||
					contentObjectFolder.getType() == ContentObjectFolder.Type.HOUR ||
					contentObjectFolder.getType() == ContentObjectFolder.Type.MINUTE ||
					contentObjectFolder.getType() == ContentObjectFolder.Type.SECOND) &&
				label != null && label.length() == 1){
				label = "0"+label;
			}
			
			contentObjectFolder.setLocalizedLabelForCurrentLocale(name);
			contentObjectFolder.setName(name);
		}
			
		
		final int nextDepth = CmsUtils.getNextDepth(depth);
		
		if (contentObjectFolder.getType() == ContentObjectFolder.Type.CONTENT_TYPE ||
				contentObjectFolder.getType() == ContentObjectFolder.Type.YEAR ||
				contentObjectFolder.getType() == ContentObjectFolder.Type.MONTH ||
				contentObjectFolder.getType() == ContentObjectFolder.Type.HOUR ||
				contentObjectFolder.getType() == ContentObjectFolder.Type.MINUTE ){
			
			contentObjectFolder.setNumberOfContentObjects(0);
			
			if (depth != TreeDepth.ZERO.asInt()){
				//Render children
				NodeIterator subFolders = contentObjectFolderNode.getNodes();
				while (subFolders.hasNext()){
					Node subFolderNode = subFolders.nextNode();
					
					if (getFolderType(subFolderNode) != null)
						contentObjectFolder.addSubFolder(render(session, subFolderNode, nextDepth,renderContentObjectIds,locale));
				}
			}
		}
		else if (contentObjectFolder.getType() == ContentObjectFolder.Type.DAY || contentObjectFolder.getType() == ContentObjectFolder.Type.SECOND){
			
			//Backwards compatibility. DAY may contain content object nodes as well thus we need to make a query in order
			//to retrieve all content object child nodes
			
			//Perform a query to load all nodes representing content object nodes
			String contentObjectFolderPathCriterion = ISO9075.encodePath(contentObjectFolderNode.getPath()).replaceFirst("/", "");
			
			String contentObjectNodeQuery = XPathUtils.createXPathSelect(contentObjectFolderPathCriterion, null, CmsBuiltInItem.StructuredContentObject, true);

			//If we do not want to render content object ids then we are only interested in their number
			int limit = renderContentObjectIds? -1: 0;
			
			CmsQueryResult contentObjectNodes = cmsQueryHandler.getNodesFromXPathQuery(session, contentObjectNodeQuery, 0, limit);
			
			contentObjectFolder.setNumberOfContentObjects(contentObjectNodes.getTotalRowCount());
			
			if (renderContentObjectIds){
				//Iterate over the nodes in order to retrieve content object ids
				NodeIterator childNodes = contentObjectNodes.getNodeIterator();
				while (childNodes.hasNext()){
					
					Node childNode = childNodes.nextNode();
					contentObjectFolder.addContentObjectId(cmsRepositoryEntityUtils.getCmsIdentifier(childNode));
				}
			}
			
			//Finally, if folder is a DAY folder and depth is > 0
			//then load its subfolders
			if (contentObjectFolder.getType() == ContentObjectFolder.Type.DAY && depth != TreeDepth.ZERO.asInt()){
				
				//We need to make a query to distinguish nodes, as DAY folder node may have content object nodes (version 2.x.x)
				//or hour folder nodes (version 3.x.x). In cases where both type of nodes exists then repository has been migrated from version 
				//2.x.x to 3.x.x
				String hourFolderNodeQuery = XPathUtils.createXPathSelect(contentObjectFolderPathCriterion, null, CmsBuiltInItem.GenericHourFolder, true);
				
				CmsQueryResult hourFolderNodes = cmsQueryHandler.getNodesFromXPathQuery(session, hourFolderNodeQuery, 0, -1);
				
				NodeIterator subFolderNodes = hourFolderNodes.getNodeIterator();
				
				while (subFolderNodes.hasNext()){
					contentObjectFolder.addSubFolder(render(session, subFolderNodes.nextNode(), nextDepth,renderContentObjectIds,locale));
				}
			}
			
		}
		

		return contentObjectFolder;

	}

	private String getFullPath(Node contentObjectFolderNode, Type type) throws RepositoryException {

		if (type == null){
			return "";
		}

		switch (type) {
		case CONTENT_TYPE:
		case YEAR:
			return contentObjectFolderNode.getName();
		case MONTH:
		case DAY:
		case HOUR:
		case MINUTE:
		case SECOND:
			//locate contentTypeFolder folder
			Node contentTypeFolder = contentObjectFolderNode.getParent();
			
			while (contentTypeFolder!= null && ! contentTypeFolder.isNodeType(CmsBuiltInItem.GenericContentTypeFolder.getJcrName())){
				contentTypeFolder = contentTypeFolder.getParent();
			}
			
			if (contentTypeFolder == null){
				return contentObjectFolderNode.getName();
			}
			
			String path = StringUtils.remove(contentObjectFolderNode.getPath(), contentTypeFolder.getPath());
			
			if (path != null && path.startsWith("/")){
				return StringUtils.removeStart(path, "/");
			}
			
			return path;
			
		default:
			break;
		}


		
		if (Type.MONTH == type){
			return getMonthName(contentObjectFolderNode)+"/"+contentObjectFolderNode.getName();
		}
		else if (Type.DAY == type){
			return getDayPath(contentObjectFolderNode)+"/"+	getMonthName(contentObjectFolderNode)+"/"+contentObjectFolderNode.getName();
		}
		else if (Type.HOUR == type){
			return getHourPath(contentObjectFolderNode)+"/"+ getDayPath(contentObjectFolderNode)+"/"+ 
			getMonthName(contentObjectFolderNode)+"/"+contentObjectFolderNode.getName();
		}
		else if (Type.MINUTE == type){
			return getMinutePath(contentObjectFolderNode)+"/"+ 
			getHourPath(contentObjectFolderNode)+"/"+ getDayPath(contentObjectFolderNode)+"/"+ 
			getMonthName(contentObjectFolderNode)+"/"+contentObjectFolderNode.getName();
		}
		else if (Type.SECOND == type){
			return contentObjectFolderNode.getParent().getParent().getParent().getParent().getParent().getName()+"/"+
			getMinutePath(contentObjectFolderNode)+"/"+
			getHourPath(contentObjectFolderNode)+"/"+ getDayPath(contentObjectFolderNode)+"/"+ 
			getMonthName(contentObjectFolderNode)+"/"+contentObjectFolderNode.getName();
		}

		// Type.YEAR == type || Type.CONTENT_TYPE == type
		return contentObjectFolderNode.getName();
	}

	private String getMinutePath(Node contentObjectFolderNode)
			throws RepositoryException, ItemNotFoundException,
			AccessDeniedException {
		return contentObjectFolderNode.getParent().getParent().getParent().getParent().getName();
	}

	private String getHourPath(Node contentObjectFolderNode)
			throws RepositoryException, ItemNotFoundException,
			AccessDeniedException {
		return contentObjectFolderNode.getParent().getParent().getParent().getName();
	}

	private String getDayPath(Node contentObjectFolderNode)
			throws RepositoryException, ItemNotFoundException,
			AccessDeniedException {
		return contentObjectFolderNode.getParent().getParent().getName();
	}

	private String getMonthName(Node contentObjectFolderNode)
			throws RepositoryException, ItemNotFoundException,
			AccessDeniedException {
		return contentObjectFolderNode.getParent().getName();
	}

	private  Type getFolderType(Node folderNode) throws RepositoryException {

		if (folderNode.isNodeType(CmsBuiltInItem.GenericContentTypeFolder.getJcrName())){
			return ContentObjectFolder.Type.CONTENT_TYPE;
		}
		else if (folderNode.isNodeType(CmsBuiltInItem.GenericYearFolder.getJcrName())){
			return ContentObjectFolder.Type.YEAR;
		}
		else if (folderNode.isNodeType(CmsBuiltInItem.GenericMonthFolder.getJcrName())){
			return ContentObjectFolder.Type.MONTH;
		}
		else if (folderNode.isNodeType(CmsBuiltInItem.GenericDayFolder.getJcrName())){
			return ContentObjectFolder.Type.DAY;
		}
		else if (folderNode.isNodeType(CmsBuiltInItem.GenericHourFolder.getJcrName())){
			return ContentObjectFolder.Type.HOUR;
		}
		else if (folderNode.isNodeType(CmsBuiltInItem.GenericMinuteFolder.getJcrName())){
			return ContentObjectFolder.Type.MINUTE;
		}
		else if (folderNode.isNodeType(CmsBuiltInItem.GenericSecondFolder.getJcrName())){
			return ContentObjectFolder.Type.SECOND;
		}
		else
			return null;
	}

	private  Type renderType(Node folderNode) throws Exception{

		Type folderType = getFolderType(folderNode);
		if (folderType == null)
			throw new Exception("Unknown content object folder type "+ folderNode.getPath());
		else
			return folderType;
	}
}
