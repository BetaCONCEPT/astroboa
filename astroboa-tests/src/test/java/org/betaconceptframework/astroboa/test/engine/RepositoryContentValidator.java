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
package org.betaconceptframework.astroboa.test.engine;

import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.CmsApiConstants;
import org.betaconceptframework.astroboa.api.model.CmsProperty;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ComplexCmsProperty;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.SimpleCmsProperty;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * Compares content between two repositories
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryContentValidator {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public void compareContentObjects(ContentObject source, ContentObject target, boolean compareAspectList) throws Throwable{
		try{
			

			String sourceSystemName = source.getSystemName();
			String targetSystemName = target.getSystemName();

			compareBasicContentObjectProperties(source, target,
					sourceSystemName, targetSystemName);

			List<String> sourceAspects = source.getComplexCmsRootProperty().getAspects();
			List<String> targetAspects = target.getComplexCmsRootProperty().getAspects();

			if (compareAspectList){
				assertListSizes(sourceAspects, targetAspects, sourceSystemName +" aspect ");
			}

			compareRepositoryUsers(source.getOwner(), target.getOwner(), false, false);

			compareComplexCmsProperty(source.getComplexCmsRootProperty(), target.getComplexCmsRootProperty());
			
		}
		catch(Throwable e){
			logger.error("Source\n"+source.xml(true)+ "\nTarget\n"+target.xml(true));
			throw e;
		}

	}

	private void compareBasicContentObjectProperties(ContentObject source,
			ContentObject target, String sourceSystemName,
			String targetSystemName) {
		
		compareCmsRepositoryEntityProperties(source, target, true, sourceSystemName, targetSystemName);

		Assert.assertEquals(source.getSystemName(), target.getSystemName(), "Invalid systemNames, source: "+ sourceSystemName + " target : "+targetSystemName);
		Assert.assertEquals(source.getContentObjectType(), target.getContentObjectType(), "Invalid content types, source: "+ sourceSystemName + " target : "+targetSystemName);
	}

	private void compareComplexCmsProperty(
			ComplexCmsProperty sourceComplexCmsProperty,
			ComplexCmsProperty targetComplexCmsProperty){
		
		String sourcePropertyFullPath = sourceComplexCmsProperty.getFullPath();
		String targetPropertyFullPath = targetComplexCmsProperty.getFullPath();

		compareBasicProperties(sourceComplexCmsProperty, targetComplexCmsProperty, sourcePropertyFullPath, targetPropertyFullPath);

		Map<String, List<CmsProperty<?,?>>> sourceChildProperties = sourceComplexCmsProperty.getChildProperties();

		Map<String, List<CmsProperty<?,?>>> targetChildProperties = new HashMap<String, List<CmsProperty<?,?>>>();
		targetChildProperties.putAll(targetComplexCmsProperty.getChildProperties());

		for (String property : sourceChildProperties.keySet()){
			
			try{
			List<CmsProperty<?, ?>> sourceProperties = sourceChildProperties.get(property);

			List<CmsProperty<?, ?>> targetProperties = targetChildProperties.get(property);

			Assert.assertNotNull(targetProperties, "Property "+ property + " was not imported in complex property "+ sourcePropertyFullPath);

			assertListSizes(sourceProperties, targetProperties, "Property "+ property);

			for (int i=0; i<sourceProperties.size(); i++){
				CmsProperty<?, ?> sourceProperty = sourceProperties.get(i);

				CmsProperty<?, ?> targetProperty = targetProperties.get(i);

				if (sourceProperty.getValueType() == ValueType.Complex){
					compareComplexCmsProperty((ComplexCmsProperty)sourceProperty, (ComplexCmsProperty)targetProperty);
				}
				else{
					compareSimpleCmsProperty((SimpleCmsProperty)sourceProperty, (SimpleCmsProperty)targetProperty);
				}
			}

			targetChildProperties.remove(property);
			}
			catch(Throwable t){
				throw new CmsException("Property path "+ sourcePropertyFullPath+"."+property, t);
			}

		}

	}

	private void compareSimpleCmsProperty(SimpleCmsProperty sourceProperty,	SimpleCmsProperty targetProperty){
		String sourceSystemName = sourceProperty.getFullPath();
		String targetSystemName = targetProperty.getFullPath();

		compareBasicProperties(sourceProperty, targetProperty, sourceSystemName, targetSystemName);

		if (sourceProperty.getPropertyDefinition().isMultiple()){
			List sourceValues = sourceProperty.getSimpleTypeValues();
			List targetValues = targetProperty.getSimpleTypeValues();

			assertListSizes(sourceValues, targetValues, sourceSystemName+ " values ");

			for (int i=0; i<sourceValues.size();i++){
				compareValues(sourceValues.get(i), targetValues.get(i), sourceProperty.getValueType(), sourceProperty.getFullPath());
			}
		}
		else{
			if (sourceProperty.hasNoValues()){
				Assert.assertTrue(targetProperty.hasNoValues(), "Source property "+ sourceProperty.getFullPath() + " has no values but target property does");
			}
			else{
				compareValues(sourceProperty.getSimpleTypeValue(), targetProperty.getSimpleTypeValue(), sourceProperty.getValueType(), sourceProperty.getFullPath());
			}

		}

	}

	private void compareValues(Object source, Object target, ValueType valueType, String propertyPath){
		if (source == null){
			Assert.assertNull(target, "Value for property "+propertyPath+ " is null in source but not null in target");
		}


		switch (valueType) {
		case Binary:

			BinaryChannel sourceBinary = (BinaryChannel) source;
			BinaryChannel targetBinary = (BinaryChannel) target;

			Assert.assertEquals(sourceBinary.getName(), targetBinary.getName(), "Different binary channel names for property "+ propertyPath);
			Assert.assertEquals(sourceBinary.getEncoding(), targetBinary.getEncoding(), "Different binary channel encoding for property "+ propertyPath);
			Assert.assertEquals(sourceBinary.getMimeType(), targetBinary.getMimeType(), "Different binary channel mime types for property "+ propertyPath);
			Assert.assertEquals(sourceBinary.getSourceFilename(), targetBinary.getSourceFilename(), "Different binary channel source file name for property "+ propertyPath);
			Assert.assertEquals(DateUtils.format(sourceBinary.getModified(), "dd/MM/yyyy HH:mm:ss.SSS"), 
					DateUtils.format(targetBinary.getModified(), "dd/MM/yyyy HH:mm:ss.SSS"), 
					"Different binary channel modification date for property "+ propertyPath);


			break;
		case TopicReference :

			Topic sourceTopic = (Topic) source;
			Topic targetTopic = (Topic) target;

			//Compare specific properties
			final String sourceTopicName = sourceTopic.getName();
			final String targetTopicName = targetTopic.getName();

			compareBasicTopicProperties(sourceTopic, targetTopic, sourceTopicName,	targetTopicName, true);

			break;

		case ObjectReference :

			ContentObject sourceContentObject = (ContentObject) source;
			ContentObject targetContentObject = (ContentObject) target;

			String sourceSystemName = sourceContentObject.getSystemName();
			String targetSystemName = targetContentObject.getSystemName();

			compareBasicContentObjectProperties(sourceContentObject, targetContentObject,sourceSystemName, targetSystemName);

			break;

		case Date :
			
			Calendar sourceCalendar = (Calendar) source;
			Calendar targetCalendar = (Calendar) target;
			
			Assert.assertEquals(DateUtils.format(sourceCalendar, "dd/MM/yyyy HH:mm:ss.sss"), DateUtils.format(targetCalendar, "dd/MM/yyyy HH:mm:ss.sss"),
					"Different values, source : "+
					formatValue(source)+
					", target : "+ formatValue(target)+ 
					" for property "+ propertyPath);
			break;

		default:
			Assert.assertEquals(source, target, "Different values, source : "+
					formatValue(source)+
					", target : "+ formatValue(target)+ 
					" for property "+ propertyPath);
			break;
		}
	}

	private String formatValue(Object value) {
		return value instanceof Calendar ? 
				DateUtils.format((Calendar)value) :
					value.toString();

	}

	private void compareBasicProperties(
			CmsProperty sourceCmsProperty,
			CmsProperty targetCmsProperty,
			String sourceSystemName, String targetSystemName){
		compareCmsRepositoryEntityProperties(sourceCmsProperty, targetCmsProperty, true, sourceSystemName, targetSystemName);

		Assert.assertEquals(sourceCmsProperty.getFullPath(), targetCmsProperty.getFullPath(), "Invalid full paths, source: "+ sourceSystemName + " target : "+targetSystemName);
		Assert.assertEquals(sourceCmsProperty.getPath(), targetCmsProperty.getPath(), "Invalid paths, source: "+ sourceSystemName + " target : "+targetSystemName);
		Assert.assertEquals(sourceCmsProperty.getName(), targetCmsProperty.getName(), "Invalid names, source: "+ sourceSystemName + " target : "+targetSystemName);
		Assert.assertEquals(sourceCmsProperty.getValueType(), targetCmsProperty.getValueType(), "Invalid valueTypes, source: "+ sourceSystemName + " target : "+targetSystemName);
	}

	public void compareTaxonomyLists(List<Taxonomy> sourceTaxonomies,
			List<Taxonomy> targetTaxonomies) 
	{
		assertListSizes(sourceTaxonomies, targetTaxonomies, "Taxonomy");

		Taxonomy sourceSubjectTaxonomy = null;

		//create a map for target list
		Map<String, Taxonomy> targetTaxonomiesMap = createMap(targetTaxonomies);

		for (Taxonomy sourceTaxonomy : sourceTaxonomies)
		{

			if (Taxonomy.SUBJECT_TAXONOMY_NAME.equals(sourceTaxonomy.getName()))
			{
				//Subject Taxonomies have different ids. They will be compared later
				sourceSubjectTaxonomy = sourceTaxonomy;
			}
			else
			{
				Assert.assertTrue(targetTaxonomiesMap.containsKey(sourceTaxonomy.getId()), "Taxonomy "+sourceTaxonomy.getName() + " was not imported to clone repository");

				compareTaxonomies(sourceTaxonomy, targetTaxonomiesMap.get(sourceTaxonomy.getId()), true, true);

				targetTaxonomiesMap.remove(sourceTaxonomy.getId());
			}
		}

		//Finally what's left to compare are system users
		compareTaxonomies(sourceSubjectTaxonomy, targetTaxonomiesMap.values().iterator().next(), false, true);

	}

	public void compareRepositoryUserList(List<RepositoryUser> sourceRepositoryUsers,
			List<RepositoryUser> targetRepositoryUsers) {

		assertListSizes(sourceRepositoryUsers, targetRepositoryUsers, "Repository User");

		RepositoryUser sourceSystemUser = null;

		//create a map for target list
		Map<String, RepositoryUser> targetUsersMap = createMap(targetRepositoryUsers);

		for (RepositoryUser sourceUser : sourceRepositoryUsers){

			if (CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID.equals(sourceUser.getExternalId())){
				//SYSTEM USERs have different ids. They will be compared later
				sourceSystemUser = sourceUser;
			}
			else{
				Assert.assertTrue(targetUsersMap.containsKey(sourceUser.getId()), "Repository User "+sourceUser.getExternalId() + " was not imported to clone repository "+ targetUsersMap.keySet().toString());

				compareRepositoryUsers(sourceUser, targetUsersMap.get(sourceUser.getId()), true, true);

				targetUsersMap.remove(sourceUser.getId());
			}
		}

		//Finally what's left to compare are system users
		compareRepositoryUsers(sourceSystemUser, targetUsersMap.values().iterator().next(),  true, true);

	}

	public void compareRepositoryUsers(RepositoryUser sourceUser,
			RepositoryUser targetUser,  
			boolean compareFolksonomy, boolean compareSpaces) {

		if (sourceUser == null){
			Assert.assertNull(targetUser, "Source user is null but target is not");
			return ;
		}
		else{
			Assert.assertNotNull(targetUser, "Source user is "+sourceUser.getExternalId()+" but target user is null");
		}
		
		final String sourceExternalId = sourceUser.getExternalId();
		final String targetExternalId = targetUser.getExternalId();

		boolean systemUsers = CmsApiConstants.SYSTEM_REPOSITORY_USER_EXTRENAL_ID.equals(sourceExternalId);

		compareCmsRepositoryEntityProperties(sourceUser, targetUser, ! systemUsers, sourceExternalId, targetExternalId);

		Assert.assertEquals(sourceUser.getExternalId(), targetUser.getExternalId(), "Invalid externalId , source: "+ sourceExternalId + " target : "+targetExternalId);

		Assert.assertEquals(sourceUser.getLabel(), targetUser.getLabel(), "Invalid label , source: "+ sourceExternalId + " target : "+targetExternalId);

		if (compareFolksonomy){
			compareTaxonomies(sourceUser.getFolksonomy(), targetUser.getFolksonomy(), ! systemUsers, true);
		}

		if (compareSpaces){
			compareSpaces(sourceUser.getSpace(), targetUser.getSpace(), true, true, true, ! systemUsers, true);
		}
	}

	public void compareTaxonomies(Taxonomy source, Taxonomy target, boolean compareIdentifiers, boolean compareRootTopics){

		final String sourceTaxonomyName = source.getName();
		final String targetTaxonomyName = target.getName();

		compareCmsRepositoryEntityProperties(source, target, compareIdentifiers, sourceTaxonomyName, targetTaxonomyName);

		Assert.assertEquals(source.getName(), target.getName(), "Invalid names , source: "+ sourceTaxonomyName + " target : "+targetTaxonomyName);

		compareLocalization(source, target);

		if (compareRootTopics && source.getNumberOfRootTopics() > 0){
			Assert.assertEquals(source.getNumberOfRootTopics(), target.getNumberOfRootTopics(), "Invalid number of root topics , source: "+ sourceTaxonomyName 
					+ "\n "+ source.xml(true)+ " target : "+targetTaxonomyName+ "\n "+ target.xml(true));

			compareTopicList(source.getRootTopics(), target.getRootTopics(), true, false, compareIdentifiers);
		}


	}

	public void compareTopicList(List<Topic> source, List<Topic> target, boolean compareChildTopics, boolean compareTaxonomies, boolean compareIdentifiers){
		assertListSizes(source, target, "Topic ");

		Map<String, Topic> targetTopicsMap = createMap(target);

		for (Topic sourceTopic : source){

			Assert.assertTrue(targetTopicsMap.containsKey(sourceTopic.getId()) || targetTopicsMap.containsKey(sourceTopic.getName()), "Topic "+sourceTopic.getName() + " was not found in target topic map");

			if (targetTopicsMap.get(sourceTopic.getId()) != null){
				compareTopics(sourceTopic, targetTopicsMap.get(sourceTopic.getId()), compareChildTopics, true, compareTaxonomies, true, compareIdentifiers);
			}
			else if (targetTopicsMap.get(sourceTopic.getName()) != null){
				compareTopics(sourceTopic, targetTopicsMap.get(sourceTopic.getName()), compareChildTopics, true, compareTaxonomies, true, compareIdentifiers);
			}

			targetTopicsMap.remove(sourceTopic.getId());
		}

	}

	public void compareSpaceList(List<Space> source, List<Space> target, boolean compareChildSpace, boolean compareIdentifiers, boolean compareParents){
		assertListSizes(source, target, "Space ");

		Map<String, Space> targetSpacesMap = createMap(target);

		for (Space sourceSpace : source){

			Assert.assertTrue(targetSpacesMap.containsKey(sourceSpace.getId()) || targetSpacesMap.containsKey(sourceSpace.getName()), "Space "+sourceSpace.getName() + " was not found in target space map");

			if (targetSpacesMap.get(sourceSpace.getId()) != null){
				compareSpaces(sourceSpace, targetSpacesMap.get(sourceSpace.getId()), compareChildSpace, true, true, compareIdentifiers, compareParents);
			}
			else if (targetSpacesMap.get(sourceSpace.getName()) != null){
				compareSpaces(sourceSpace, targetSpacesMap.get(sourceSpace.getName()), compareChildSpace, true, true, compareIdentifiers, compareParents);
			}

			targetSpacesMap.remove(sourceSpace.getId());
		}

	}

	public void compareTopics(Topic sourceTopic, Topic targetTopic, boolean compareChildTopics, boolean compareOrders) {
		
		compareTopics(sourceTopic, targetTopic, compareChildTopics, compareOrders, true, true, true);
		
	}
	
	public void compareTopics(Topic sourceTopic, Topic targetTopic, boolean compareChildTopics, boolean compareOrders, boolean compareTaxonomies, boolean compareParents, boolean compareIdentifiers) {

		final String sourceTopicName = sourceTopic.getName();
		final String targetTopicName = targetTopic.getName();

		compareBasicTopicProperties(sourceTopic, targetTopic, sourceTopicName,
				targetTopicName, compareIdentifiers);

		if (compareOrders){
			Assert.assertEquals(sourceTopic.getOrder(), targetTopic.getOrder(), "Invalid orders , source: "+ sourceTopicName + " target : "+targetTopicName);
		}

		compareRepositoryUsers(sourceTopic.getOwner(), targetTopic.getOwner(),false, false);


		if (compareChildTopics){
			Assert.assertEquals(sourceTopic.getNumberOfChildren(), targetTopic.getNumberOfChildren(), "Invalid number of child topics , source: "+ sourceTopicName + " target : "+targetTopicName);

			if (sourceTopic.getNumberOfChildren() > 0){
				compareTopicList(sourceTopic.getChildren(), targetTopic.getChildren(), compareChildTopics, false, compareIdentifiers);
			}
		}

		if (compareTaxonomies){
			compareTaxonomies(sourceTopic.getTaxonomy(), targetTopic.getTaxonomy(), 
				! StringUtils.equals(sourceTopic.getTaxonomy().getName(), Taxonomy.SUBJECT_TAXONOMY_NAME), 
				false);
		}

		if (compareParents && sourceTopic.getParent() != null){
			compareTopics(sourceTopic.getParent(), targetTopic.getParent(), false, false, false, false, compareIdentifiers);
		}

	}

	private void compareBasicTopicProperties(Topic sourceTopic,
			Topic targetTopic, final String sourceTopicName,
			final String targetTopicName, boolean compareIdentifiers) {
		compareCmsRepositoryEntityProperties(sourceTopic, targetTopic, compareIdentifiers, sourceTopicName, targetTopicName);

		Assert.assertEquals(sourceTopic.getName(), targetTopic.getName(), "Invalid names , source: "+ sourceTopicName + " target : "+targetTopicName);
		
		compareLocalization(sourceTopic, targetTopic);
	}

	public void compareSpaces(Space sourceSpace, Space targetSpace, boolean compareChildSpace, boolean compareOrders, boolean compareOwners, boolean compareIdentifiers, boolean compareParents) {

		final String sourceSpaceName = sourceSpace.getName();
		final String targetSpaceName = targetSpace.getName();

		compareCmsRepositoryEntityProperties(sourceSpace, targetSpace, compareIdentifiers,sourceSpaceName, targetSpaceName);

		Assert.assertEquals(sourceSpace.getName(), targetSpace.getName(), "Invalid names , source: "+ sourceSpaceName + " target : "+targetSpaceName);

		if (compareOwners){
			compareRepositoryUsers(sourceSpace.getOwner(), targetSpace.getOwner(),false, false);
		}

		if (compareOrders){
			Assert.assertEquals(sourceSpace.getOrder(), targetSpace.getOrder(), "Invalid orders , source: "+ sourceSpaceName + " target : "+targetSpaceName);
		}

		compareLocalization(sourceSpace, targetSpace);



		if (compareChildSpace){
			Assert.assertEquals(sourceSpace.getNumberOfChildren(), targetSpace.getNumberOfChildren(), "Invalid number of child spaces , source: "+ sourceSpaceName + " target : "+targetSpaceName);
			
			if (sourceSpace.getNumberOfChildren() > 0){
				compareSpaceList(sourceSpace.getChildren(), targetSpace.getChildren(), compareChildSpace,compareIdentifiers, false);
			}
		}

		if (compareParents){
			if (sourceSpace.getParent() != null){

				Assert.assertNotNull(targetSpace.getParent(), "Target space has no parent, source: "+ sourceSpaceName + " target : "+targetSpaceName+ " , Parent of source : "+ sourceSpace.getParent().getName());

				compareSpaces(sourceSpace.getParent(), targetSpace.getParent(), false, false, false,compareIdentifiers, compareParents);
			}
		}

	}

	private void compareLocalization(Localization source, Localization target) {

		assertListSizes(source.getLocalizedLabels().values(), target.getLocalizedLabels().values(), "Localized Labels ");

		for (Entry<String, String> sourceLocLabelEntry : source.getLocalizedLabels().entrySet()){
			String targetLocalizedLabel = target.getLocalizedLabelForLocale(sourceLocLabelEntry.getKey());

			Assert.assertNotNull(targetLocalizedLabel, "Could not find localized label for locale "+ sourceLocLabelEntry.getKey() +" in target object");
			
			Assert.assertEquals(targetLocalizedLabel.trim(), sourceLocLabelEntry.getValue().trim(), "Localized label "+ sourceLocLabelEntry.getValue()+ " for locale "+ sourceLocLabelEntry.getKey() +" was not found at target object");
		}
	}

	private void compareCmsRepositoryEntityProperties(
			CmsRepositoryEntity sourceEntity, CmsRepositoryEntity targetEntity,
			boolean compareIdentifiers, String sourceSystemName, String targetSystemName) {

		if (compareIdentifiers){
			Assert.assertEquals(sourceEntity.getId(), targetEntity.getId(), "Invalid identifiers, source: "+ sourceSystemName + " target : "+targetSystemName);
		}
	}

	private <T extends CmsRepositoryEntity> Map<String, T> createMap(List<T> list){
		Map<String, T> map = new HashMap<String, T>();
		for (T item : list){
			if (item.getId() != null){
				map.put(item.getId(), item);
			}
			else{
				if (item instanceof Topic){
					map.put(((Topic)item).getName(), item);
				}
				else if (item instanceof Space){
					map.put(((Space)item).getName(), item);
				}
			}
		}

		return map;
	}

	private void assertListSizes(Collection sourceList,	Collection targetList, String messagePrefix){

		//Lists must have the same size
		Assert.assertTrue(sourceList != null && targetList != null && 
				sourceList.size() == targetList.size(), messagePrefix +" collections do not have the same size. \nSource "+sourceList +
				"\nTarget "+targetList);
	}

	public void compareContentObjectOutcome(
			CmsOutcome<ContentObject> sourceOutcome,
			CmsOutcome<ContentObject> targetOutcome) throws Throwable{

		final List<ContentObject> sourceResults = sourceOutcome.getResults();

		final List<ContentObject> targetResults = targetOutcome.getResults();

		assertListSizes(sourceResults, targetResults, "ContentObject outcome ");


		for (int i=0; i<sourceResults.size(); i++){
			compareContentObjects(sourceResults.get(i), targetResults.get(i), true);
		}
	}


}
