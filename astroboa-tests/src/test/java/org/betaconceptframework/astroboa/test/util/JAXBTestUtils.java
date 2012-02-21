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

import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.testng.Assert;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class JAXBTestUtils {

	public static Space createSpace(String spaceName, Space space, RepositoryUser repositoryUser) {

		//space.setId(UUID.randomUUID().toString());
		space.setName(spaceName);
		space.setOrder((long)234);

		space.addLocalizedLabel("en", spaceName);
		space.addLocalizedLabel("fr", spaceName);
		
		if (repositoryUser.getId() == null){
			space.setOwner(createRepositoryUser("spaceOwner", repositoryUser));
		}
		else{
			space.setOwner(repositoryUser);
		}

		return space;

	}

	public static Topic createTopic(String topicName, Topic topic, RepositoryUser repositoryUser) {

		//topic.setId(UUID.randomUUID().toString());
		topic.setName(topicName);
		topic.setAllowsReferrerContentObjects(true);
		topic.setOrder((long)123);
		
		if (repositoryUser.getId() == null)
		{
			topic.setOwner(createRepositoryUser("topicOwner", repositoryUser));
		}
		else{
			topic.setOwner(repositoryUser);
		}
		
		topic.addLocalizedLabel("en", topicName);
		topic.addLocalizedLabel("fr", topicName);

		return topic;
	}
	
	public static RepositoryUser createRepositoryUser(String externalId, RepositoryUser repositoryUser ) {

		repositoryUser.setExternalId(externalId);
		repositoryUser.setLabel(externalId);
		
		//repositoryUser.setId(UUID.randomUUID().toString());

		//repositoryUser.getFolksonomy().setId(UUID.randomUUID().toString());

		repositoryUser.getFolksonomy().addLocalizedLabel("en", "MyFolksonomy");
		repositoryUser.getFolksonomy().addLocalizedLabel("fr", "MyFolksonomy");
		
		repositoryUser.getSpace().setName("mySpace");
		repositoryUser.getSpace().addLocalizedLabel("en", "MySpace");
		
		//repositoryUser.getSpace().setId(UUID.randomUUID().toString());
		
		return repositoryUser;
	}


	public static Taxonomy createTaxonomy(String taxonomyName, Taxonomy taxonomy) {

		taxonomy.setName(taxonomyName);
		//taxonomy.setId(UUID.randomUUID().toString());

		
		taxonomy.addLocalizedLabel("en", taxonomyName);
		taxonomy.addLocalizedLabel("fr", taxonomyName);

		return taxonomy;
	}
	
	public static void assertSpacesAreTheSame(Space space, Space spaceFromUnMarshalling, boolean assertChildren) {

		Assert.assertEquals(spaceFromUnMarshalling.getId(), space.getId());
		Assert.assertEquals(spaceFromUnMarshalling.getName(), space.getName());
		Assert.assertEquals(spaceFromUnMarshalling.getOrder(), space.getOrder());

		assertLocalization(space, spaceFromUnMarshalling);

		if (assertChildren){
			spaceFromUnMarshalling.getChildren();
			space.getChildren();
			
			Assert.assertEquals(spaceFromUnMarshalling.getNumberOfChildren(), space.getNumberOfChildren());


			//Assert child spaces
			for (int i=0; i<space.getNumberOfChildren();i++){
				Space childSpaceFromUnmarshalling = spaceFromUnMarshalling.getChildren().get(i);

				Space manuallyCreatedChildSpace = space.getChildren().get(i);

				assertSpacesAreTheSame(childSpaceFromUnmarshalling, manuallyCreatedChildSpace, assertChildren);

				//Special case
				Assert.assertTrue(( manuallyCreatedChildSpace.getParent() != null && childSpaceFromUnmarshalling.getParent() != null ) ||
						(manuallyCreatedChildSpace.getParent() == null && childSpaceFromUnmarshalling.getParent() == null), 
						( childSpaceFromUnmarshalling.getParent() == null ? "Space created from unmarshalling does not contain parent while it should have.  " :
						"Space created manually does not contain parent while space created from unmarshalling has a parent !!!" )
				);

				if (manuallyCreatedChildSpace.getParent() != null){
					assertSpacesAreTheSame(manuallyCreatedChildSpace.getParent(), childSpaceFromUnmarshalling.getParent(), false);
				}
			}
		}
	}
	
	public static void assertRepositoryUsersAreTheSame(RepositoryUser manuallyCreatedRepositoryUser, RepositoryUser repositoryUserFromUnmarshaling) {

		Assert.assertEquals(repositoryUserFromUnmarshaling.getId(), manuallyCreatedRepositoryUser.getId());
		Assert.assertEquals(repositoryUserFromUnmarshaling.getExternalId(), manuallyCreatedRepositoryUser.getExternalId());

		assertSpacesAreTheSame(manuallyCreatedRepositoryUser.getSpace(), repositoryUserFromUnmarshaling.getSpace(), true);
		
		assertTaxonomyiesAreTheSame(manuallyCreatedRepositoryUser.getFolksonomy(), repositoryUserFromUnmarshaling.getFolksonomy(), true);
		
	}


	public static void assertTopicsAreTheSame(Topic topic, Topic topicFromUnMarshalling, boolean assertChildren, boolean assertTaxonomyRootTopics) {

		Assert.assertEquals(topicFromUnMarshalling.getId(), topic.getId());
		Assert.assertEquals(topicFromUnMarshalling.getName(), topic.getName());
		Assert.assertEquals(topicFromUnMarshalling.isAllowsReferrerContentObjects(), topic.isAllowsReferrerContentObjects());
		Assert.assertEquals(topicFromUnMarshalling.getOrder(), topic.getOrder());

		assertLocalization(topic, topicFromUnMarshalling);

		assertTaxonomyiesAreTheSame(topic.getTaxonomy(), topicFromUnMarshalling.getTaxonomy(), assertTaxonomyRootTopics);

		if (assertChildren){
			topicFromUnMarshalling.getChildren();
			topic.getChildren();
			Assert.assertEquals(topicFromUnMarshalling.getNumberOfChildren(), topic.getNumberOfChildren(), "Invalid number of children for "+topic.getName());


			//Assert child topics
			for (int i=0; i<topic.getNumberOfChildren();i++){
				Topic childTopicFromUnmarshalling = topicFromUnMarshalling.getChildren().get(i);

				Topic manuallyCreatedChildTopic = topic.getChildren().get(i);

				assertTopicsAreTheSame(childTopicFromUnmarshalling, manuallyCreatedChildTopic, assertChildren, assertTaxonomyRootTopics);

				//Special case
				Assert.assertTrue(( manuallyCreatedChildTopic.getParent() != null && childTopicFromUnmarshalling.getParent() != null ) ||
						(manuallyCreatedChildTopic.getParent() == null && childTopicFromUnmarshalling.getParent() == null), 
						( childTopicFromUnmarshalling.getParent() == null ? "Topic created from unmarshalling does not contain parent while it should have.  " :
						"Topic created manually does not contain parent while topic created from unmarshalling has a parent !!!" )
				);

				if (manuallyCreatedChildTopic.getParent() != null){
					assertTopicsAreTheSame(manuallyCreatedChildTopic.getParent(), childTopicFromUnmarshalling.getParent(), false, false);
				}
			}
		}
	}

	public static void assertTaxonomyiesAreTheSame(Taxonomy manuallyCreatedTaxonomy, Taxonomy taxonomyFromUnmarshaling, boolean assertRootTopics) {

		Assert.assertEquals(taxonomyFromUnmarshaling.getId(), manuallyCreatedTaxonomy.getId());
		Assert.assertEquals(taxonomyFromUnmarshaling.getName(), manuallyCreatedTaxonomy.getName());

		assertLocalization(manuallyCreatedTaxonomy, taxonomyFromUnmarshaling);

		if (assertRootTopics){
			taxonomyFromUnmarshaling.getRootTopics();
			manuallyCreatedTaxonomy.getRootTopics();
			Assert.assertEquals(taxonomyFromUnmarshaling.getNumberOfRootTopics(), manuallyCreatedTaxonomy.getNumberOfRootTopics());

			//Assert child topics
			for (int i=0; i<manuallyCreatedTaxonomy.getNumberOfRootTopics();i++){
				Topic childTopicFromUnmarshalling = taxonomyFromUnmarshaling.getRootTopics().get(i);

				Topic manuallyCreatedChildTopic = manuallyCreatedTaxonomy.getRootTopics().get(i);

				assertTopicsAreTheSame(childTopicFromUnmarshalling, manuallyCreatedChildTopic, true, false);

				//Special case
				Assert.assertTrue(( manuallyCreatedChildTopic.getParent() != null && childTopicFromUnmarshalling.getParent() != null ) ||
						(manuallyCreatedChildTopic.getParent() == null && childTopicFromUnmarshalling.getParent() == null), 
						( childTopicFromUnmarshalling.getParent() == null ? "Topic created from unmarshalling does not contain parent while it should have.  " :
						"Topic created manually does not contain parent while topic created from unmarshalling has a parent !!!" )
				);

				if (manuallyCreatedChildTopic.getParent() != null){
					assertTopicsAreTheSame(manuallyCreatedChildTopic.getParent(), childTopicFromUnmarshalling.getParent(), false, false);
				}
			}
		}

	}

	private static void assertLocalization(Localization manuallyCreatedLocalization,
			Localization localizationFromUnMarshalling) {
		Assert.assertTrue(( manuallyCreatedLocalization != null && localizationFromUnMarshalling != null ) ||
				(manuallyCreatedLocalization == null && localizationFromUnMarshalling == null), 
				( manuallyCreatedLocalization== null ? "Localization created from unmarshalling is not null while it should be null.  " :
				"Localization created manually is not null while localization created from unmarshalling is null." )
		);

		Assert.assertTrue(( manuallyCreatedLocalization.getLocalizedLabels() != null && localizationFromUnMarshalling.getLocalizedLabels() != null ) ||
				(manuallyCreatedLocalization.getLocalizedLabels() == null && localizationFromUnMarshalling.getLocalizedLabels() == null), 
				( manuallyCreatedLocalization.getLocalizedLabels() == null ? "Localization created from unmarshalling is not empty while it should be empty.  " :
				"Localization created manually is not empty while localization created from unmarshalling is empty." )
		);

		Assert.assertTrue(manuallyCreatedLocalization.getLocalizedLabels().size() == localizationFromUnMarshalling.getLocalizedLabels().size(), "Found different size of localized label maps");


		for (Entry<String, String> manuallyLocLabel : manuallyCreatedLocalization.getLocalizedLabels().entrySet()){

			String locLabelFromUnmarshalling = localizationFromUnMarshalling.getLocalizedLabelForLocale(manuallyLocLabel.getKey());

			Assert.assertTrue(locLabelFromUnmarshalling !=null, "No localized label for locale "+ manuallyLocLabel.getKey()+ " was found in localization created from unmarshalling");

			Assert.assertEquals(locLabelFromUnmarshalling, manuallyLocLabel.getValue(), "Localized labels for locale "+manuallyLocLabel.getKey() + " do not match.");
		}

	}
	
	public static void assertParentSpaceIsTheSameObjectAmongSpaceChildren(Space rootSpace) {

		if (rootSpace.isChildrenLoaded()){
			if (CollectionUtils.isNotEmpty(rootSpace.getChildren())){

				for (Space space : rootSpace.getChildren()){
					assertSpaceIsTheSame(rootSpace, space);
				}
			}
		}

	}

	private static void assertSpaceIsTheSame(Space parentSpace, Space space) {

		Assert.assertNotNull(space.getParent());

		Assert.assertSame(parentSpace, space.getParent());

		if (space.isChildrenLoaded() && CollectionUtils.isNotEmpty(space.getChildren())){

			for (Space childSpace : space.getChildren()){
				assertSpaceIsTheSame(space, childSpace);
			}
		}

	}

	public static void assertParentTopicAndTaxonomyAreTheSameObjectsAmongTopicChildren(Topic rootTopic) {


		if (rootTopic.isChildrenLoaded()){
			if (CollectionUtils.isNotEmpty(rootTopic.getChildren())){

				Taxonomy taxonomy = rootTopic.getTaxonomy();

				for (Topic topic : rootTopic.getChildren()){
					assertTopicAndTaxonomyAreTheSame(rootTopic, taxonomy, topic);
				}
			}
		}

	}

	private static void assertTopicAndTaxonomyAreTheSame(Topic parentTopic,
			Taxonomy taxonomy, Topic topic) {

		Assert.assertNotNull(topic.getParent());
		Assert.assertSame(parentTopic, topic.getParent());

		Assert.assertNotNull(topic.getTaxonomy());
		Assert.assertSame(taxonomy , topic.getTaxonomy());

		if (topic.isChildrenLoaded() && CollectionUtils.isNotEmpty(topic.getChildren())){

			for (Topic childTopic : topic.getChildren()){
				assertTopicAndTaxonomyAreTheSame(topic, taxonomy, childTopic);
			}
		}

	}

}
