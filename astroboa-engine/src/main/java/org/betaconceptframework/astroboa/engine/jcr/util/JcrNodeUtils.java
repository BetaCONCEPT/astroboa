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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.jcr.query.CalendarInfo;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class JcrNodeUtils  {
	
	private final static Logger logger = LoggerFactory.getLogger(JcrNodeUtils.class);
	
	/**
	 * Retrieve Content type's Folder Node
	 * Creates a new one if not found
	 * @param session
	 * @param type
	 * @return
	 * @throws RepositoryException 
	 * @throws RepositoryException 
	 * @throws 
	 */
	public static  Node retrieveOrCreateContentTypeFolderNode(Session session, String type) throws RepositoryException 
	{
		String typeFolderPath = type + CmsConstants.TYPE_FOLDER;
		
		Node contentObjectRootNode = getContentObjectRootNode(session);
		
		try {
			return contentObjectRootNode.getNode(typeFolderPath);
		}
		catch (PathNotFoundException pnf)
		{
			//Content Type Folder does not exist. Create one
			return contentObjectRootNode.addNode(typeFolderPath);
		}
		
	}
	
	/**
	 * 
	 * @param parent
	 * @param childName
	 * @return Child node found with specified childName, null othrewise
	 * @throws RepositoryException
	 */
	public static  Node selectNode(Node parent, String childName) throws RepositoryException
	{
		try{
			return parent.getNode(childName);
		}
		catch (PathNotFoundException pnfe)
		{
			return null;
		}
	}

	public static Node createContentObjectParentNode(Node typeNode, CalendarInfo calendarInfo) throws RepositoryException{
		
		Node secondNode = selectNode(typeNode, calendarInfo.getFullPath());
		
		if (secondNode != null){
			return secondNode;
		}
		
		//Obtain year
		final String year = calendarInfo.getYear();
		final String month = calendarInfo.getMonth();
		final String day = calendarInfo.getDay();
		final String hour = calendarInfo.getHour();
		final String minute = calendarInfo.getMinute();
		final String second = calendarInfo.getSecond();
		
		Node yearNode = selectNode(typeNode, year);
		if (yearNode == null){
			yearNode = createYearFolderNode(typeNode, year);
		}
		
		Node monthNode = selectNode(yearNode, month);
		if (monthNode == null){
			monthNode = createMonthFolderNode(yearNode, month);
		}
		
		Node dayNode = selectNode(monthNode, day);
		if (dayNode == null){
			dayNode = createDayFolderNode(monthNode, day);
		}
		
		Node hourNode = selectNode(dayNode, hour);
		if (hourNode == null){
			hourNode = createHourFolderNode(dayNode, hour);
		}

		Node minuteNode = selectNode(hourNode, minute);
		
		if (minuteNode == null){
			minuteNode = createMinuteFolderNode(hourNode, minute);
		}
		return createSecondFolderNode(minuteNode, second);
	}

	private static Node createSecondFolderNode(Node minuteNode, String minute) throws RepositoryException {
		if (minuteNode == null)
			throw new ItemNotFoundException("Minute node");
		
		return minuteNode.addNode(minute, CmsBuiltInItem.GenericSecondFolder.getJcrName());
	}

	private static Node createMinuteFolderNode(Node hourNode, String minute) throws RepositoryException {
		if (hourNode == null)
			throw new ItemNotFoundException("Hour node");
		
		return hourNode.addNode(minute, CmsBuiltInItem.GenericMinuteFolder.getJcrName());
	}

	private static Node createHourFolderNode(Node dayNode, String hour) throws RepositoryException {
		if (dayNode == null)
			throw new ItemNotFoundException("Day node");
		
		return dayNode.addNode(hour, CmsBuiltInItem.GenericHourFolder.getJcrName());
	}

	private static Node createDayFolderNode(Node monthNode, String day) throws RepositoryException {
		if (monthNode == null)
			throw new ItemNotFoundException("Month node");
		
		return monthNode.addNode(day, CmsBuiltInItem.GenericDayFolder.getJcrName());
	}

	private static Node createMonthFolderNode(Node yearNode, String month) throws  RepositoryException {
		if (yearNode == null)
			throw new ItemNotFoundException("Year ndoe");
		
		return yearNode.addNode(month, CmsBuiltInItem.GenericMonthFolder.getJcrName());
	}

	private static Node createYearFolderNode(Node typeNode, String year) throws RepositoryException {
		if (typeNode == null)
			throw new ItemNotFoundException("Type node");
		
		return typeNode.addNode(year, CmsBuiltInItem.GenericYearFolder.getJcrName());
	}

	public static Node uniqueNode(CmsQueryResult cmsQueryResult)
	{
		if (cmsQueryResult == null)
			return null;
		
		long size = cmsQueryResult.getTotalRowCount();
		
		if (size <= 0)
			return null;
		
		if (size > 1)
			throw new CmsException("Not unique node");
		
		try {
			return cmsQueryResult.getNodeIterator().nextNode();
		} catch (Exception e) {
			logger.error("CmsQueryResult has 1 total row count but node iterator has a problem ", e);
			return null;
		}
			
	}

	public static Node getRepositoryUserRootNode(Session session) throws RepositoryException
	{
		return getRootNode(session, CmsBuiltInItem.RepositoryUserRoot);
	}

	public static Node getContentObjectRootNode(Session session) throws RepositoryException
	{
		return getRootNode(session, CmsBuiltInItem.ContentObjectRoot);
	}
	
	public static Node getTaxonomyRootNode(Session session) throws RepositoryException
	{
		return getRootNode(session, CmsBuiltInItem.TaxonomyRoot);
	}
	
	public static Node getCMSSystemNode(Session session) throws RepositoryException
	{
		return session.getRootNode().getNode(CmsBuiltInItem.SYSTEM.getJcrName());
	}
	
	public static Node getNodeByNativeRepositoryIdentifier(Session session, String nativeRepositoryIdentifier) throws RepositoryException
	{
		return session.getNodeByUUID(nativeRepositoryIdentifier);
	}

	
	public static Node getRootNode(Session session, ItemQName rootItem) throws  RepositoryException
	{
		if (rootItem != null)
			return getCMSSystemNode(session).getNode(rootItem.getJcrName());
		
		return null;
	}
	
	public static void addMultiValueProperty(Node node, ItemQName multiValueProperty, SaveMode saveMode, List values, ValueType valueType, ValueFactory valueFactory) throws RepositoryException
	{
		Value[] newValues =  JcrValueUtils.convertListToValueArray(values, valueType, valueFactory);
		
		addMultiValueProperty(saveMode, node, multiValueProperty, newValues, valueFactory, valueType);
	}
	
	private static void addMultiValueProperty(SaveMode saveMode, Node node, ItemQName property, Value[] values, ValueFactory valueFactory, ValueType valueType) throws RepositoryException {
		if (! ArrayUtils.isEmpty(values)){
			
			//It may be the case that property contains a single value and not a single value array.
			//In this case JCR throws an exception. Thus we remove value first and then we add the array of values
			if (node.hasProperty(property.getJcrName()) && 
					node.getProperty(property.getJcrName()).getDefinition() != null && 
					! node.getProperty(property.getJcrName()).getDefinition().isMultiple()){
				
				if (values != null){
					if (values.length > 1){
						throw new CmsException("Cannot save more than one values "+ generateStringOutput(values)+
								" to single value property "+
							node.getProperty(property.getJcrName()).getPath()+
							".Probably this property used to be single valued and its definition change to multivalue "+
							" and now user tries to save more than one values. To correct this propblem "+
							" you should run a script which modifies existing property values from single value to a value Array");
					}
					else if (values.length == 1){
						addSimpleProperty(saveMode, node, property, JcrValueUtils.getObjectValue(values[0]), valueFactory, valueType);
					}
				}
				else{
					node.setProperty(property.getJcrName(), JcrValueUtils.getJcrNull());
				}
			}
			else{
				node.setProperty(property.getJcrName(), values);
			}
		}
		else if (saveMode == SaveMode.UPDATE){
			if (node.hasProperty(property.getJcrName()) && 
					node.getProperty(property.getJcrName()).getDefinition() != null && 
					! node.getProperty(property.getJcrName()).getDefinition().isMultiple()){
				removeProperty(node, property, false);
			}
			else{
				removeProperty(node, property, true);
			}
		}
	}

	private static String generateStringOutput(Value[] values) {
		StringBuilder sb = new StringBuilder();
		
		try{
			if (! ArrayUtils.isEmpty(values)){
				for (Value value : values){
					if (value != null){
						switch (value.getType()) {
						case PropertyType.BOOLEAN:
							sb.append(value.getBoolean());
							break;
						case PropertyType.DATE:
							sb.append(DateUtils.format(value.getDate()));
							break;
						case PropertyType.DOUBLE:
							sb.append(value.getDouble());
							break;
						case PropertyType.LONG:
							sb.append(value.getLong());
							break;
						case PropertyType.NAME:
						case PropertyType.PATH:
						case PropertyType.REFERENCE:
						case PropertyType.STRING:
							sb.append(value.getString());
							break;

						default:
							break;
						}
					}
					else{
						sb.append("NULL");
					}
					sb.append(" ");
				}
			}
		}
		catch(Exception e){
			logger.warn("While trying to log value array", e);
		}
		
		return sb.toString();
	}
	
	public static void addSimpleProperty(SaveMode saveMode, Node node, ItemQName property, Object value, ValueFactory valueFactory, ValueType valueType) throws  RepositoryException {
		if (value != null){
			JcrValueUtils.addValue(node, property, JcrValueUtils.getJcrValue(value, valueType, valueFactory), false);
			logger.debug("Added value {} for property {} in node {}", new Object[]{value, property.getJcrName(), node.getPath()});
		}
		else if (saveMode == SaveMode.UPDATE){
			removeProperty(node, property, false);
			logger.debug("Removed property {} from node {}", new Object[]{property.getJcrName(), node.getPath()});
		}
	}
	
	private static  void removeProperty(Node node, ItemQName property, boolean multiValue) throws     RepositoryException {
		if (property != null && node != null)
		{
			final String propertyName = property.getJcrName();
			if (node.hasProperty(propertyName))
				if (multiValue)
					node.setProperty(propertyName, JcrValueUtils.getJcrNullForMultiValue());
				else
					node.setProperty(propertyName, JcrValueUtils.getJcrNull());
		}
	}

	public static void addBinaryProperty(SaveMode saveMode, Node node, ItemQName propertyName, byte[] value, ValueFactory valueFactory) throws  RepositoryException {
		if (value != null)
			node.setProperty(propertyName.getJcrName(), JcrValueUtils.getJcrBinary(value, valueFactory));
		else if (saveMode == SaveMode.UPDATE)
			removeProperty(node, propertyName, false);
		
	}

	public static Node getTaxonomyJcrNode(Node topicNode, boolean throwExceptionIfNotFound) throws RepositoryException
	{
		Node taxonomyNode = topicNode;
		
		if (! taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName())){
			//Try to find  taxonomy node
				taxonomyNode = topicNode.getParent();
				while (taxonomyNode != null && !taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))
					taxonomyNode = taxonomyNode.getParent();
				
		}
		
		if (throwExceptionIfNotFound && 
				(taxonomyNode == null || !taxonomyNode.isNodeType(CmsBuiltInItem.Taxonomy.getJcrName()))){
			throw new ItemNotFoundException("Unable to find taxonomy for topic "+ topicNode.getPath());
		}
		
		return taxonomyNode;
	}
	
	public static Node getContentObjectNode(Node propertyContainerNode) throws RepositoryException
	{
		Node contentObjectNode = propertyContainerNode;
		
		if (! contentObjectNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()))
		{
			//Try to find contentObjectNode
				contentObjectNode = propertyContainerNode.getParent();
				while (!contentObjectNode.isNodeType(CmsBuiltInItem.StructuredContentObject.getJcrName()))
					contentObjectNode = contentObjectNode.getParent();
				
		}
		
		return contentObjectNode;
	}

	public static Node getOrganizationSpaceNode(Session session) throws RepositoryException {
		return getRootNode(session, CmsBuiltInItem.OrganizationSpace);
	}

	public static Node addContentObjectNode(Node dayNode, String type) throws RepositoryException {
		//Create node
		return dayNode.addNode(type, CmsBuiltInItem.StructuredContentObject.getJcrName());
		
	}

	public static Node addRepositoryUserNode(Session session) throws RepositoryException {
		Node repositoryUserNode = getRepositoryUserRootNode(session).addNode(CmsBuiltInItem.RepositoryUser.getJcrName(), CmsBuiltInItem.RepositoryUser.getJcrName());
		
		repositoryUserNode.addNode(CmsBuiltInItem.Folksonomy.getJcrName(), CmsBuiltInItem.Taxonomy.getJcrName());
		
		return repositoryUserNode;
	}

	public static Node addSpaceNode(Node parentNode, String nodeNeme) throws RepositoryException {
		
		return parentNode.addNode(nodeNeme, CmsBuiltInItem.Space.getJcrName());
	}
	
	public static Node addTopicNode(Node parentNode, String nodeNeme) throws RepositoryException {
		
		return parentNode.addNode(nodeNeme, CmsBuiltInItem.Topic.getJcrName());
	}

	public static Node addBinaryChannelNode(Node binaryParentNode, String name) throws  RepositoryException {
		return binaryParentNode.addNode(name, CmsBuiltInItem.BinaryChannel.getJcrName());
	}
	
	public static Node addNodeForComplexCmsProperty(Node parentComplexPropertyNode, String name) throws  RepositoryException{
		
		Node childComplexPropertyNode = parentComplexPropertyNode.addNode(name, JcrBuiltInItem.NtUnstructured.getJcrName());
		
		childComplexPropertyNode.addMixin(JcrBuiltInItem.MixReferenceable.getJcrName());
		
		return childComplexPropertyNode;

	}

	public static Node addLocalizationNode(Node cmsRepositoryEntityNode) throws RepositoryException {
		
		return cmsRepositoryEntityNode.addNode(CmsBuiltInItem.Localization.getJcrName(), JcrBuiltInItem.NtUnstructured.getJcrName());
		
	}
	
	public static String getYearMonthDayPathForContentObjectNode(Node contentObjectNode) throws RepositoryException{
		
		if (contentObjectNode == null || contentObjectNode.getParent() == null){
			return "";
		}
		
		//In  releases prior to 3 (2.x.x) content object nodes were stored
		//under path  contentTypeFolder/year/month/day
		//where as from release 3 and onwards content object nodes are stored under path
		//contentTypeFolder/year/month/day/hour/minute
		
		//This method takes under consideration both structures for compatibility reasons
		
		//Find content object type folder
		Node contentObjectTypeFolderNode = contentObjectNode.getParent();
		//Try to find contentObjectTypeFolder node
		while (contentObjectTypeFolderNode != null && ! contentObjectTypeFolderNode.isNodeType(CmsBuiltInItem.GenericContentTypeFolder.getJcrName())){
			contentObjectTypeFolderNode = contentObjectTypeFolderNode.getParent();
		}
		
		if (contentObjectTypeFolderNode == null){
			return "";
		}
		
		String contentObjectNodeParentPath = contentObjectNode.getParent().getPath();
		String contentObjectTypeFolderPath = contentObjectTypeFolderNode.getPath();
		String path = StringUtils.difference(contentObjectTypeFolderPath+"/", contentObjectNodeParentPath);
		
		logger.debug("CO Parent Path {} \n Type Folder Path {}\n Difference {}",
				new Object[]{contentObjectNodeParentPath, contentObjectTypeFolderPath, path});

		//Path has either the format  year/month/day or year/month/day/hour/minute
		int count = StringUtils.countMatches(path, "/");
		
		if (count == 2){
			return path;
		}
		else{
			//Must return the first two
			path = StringUtils.substringBeforeLast(path, "/");
			return  StringUtils.substringBeforeLast(path, "/");
		}
		
	}
}
