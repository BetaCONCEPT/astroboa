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
package org.betaconceptframework.astroboa.console.jsf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum DynamicUIAreaPageComponent {
	BINARY_CHANNEL_VIEWER("/WEB-INF/pageComponents/binaryChannelViewer.xhtml","console"),
	SHARED_SPACE_NAVIGATOR("/WEB-INF/pageComponents/space/sharedSpaceNavigator.xhtml", "console","sharedSpaceNavigation"),
	TAXONOMY_EDITOR("/WEB-INF/pageComponents/taxonomy/taxonomyEditor.xhtml","console", "taxonomyEdit", "tagEdit", "taxonomyEditor"),
	TOPIC_EDITOR("/WEB-INF/pageComponents/taxonomy/topicEditor.xhtml","console", "taxonomyEdit", "tagEdit"),
	TAG_EDITOR("/WEB-INF/pageComponents/taxonomy/tagEditor.xhtml","console","taxonomyEdit", "tagEdit"),
	OBJECT_TYPE_SELECTOR("/WEB-INF/pageComponents/contentObjectTypeSelector.xhtml","console", "contentTypeEditor"),
	ADVANCED_SEARCH("/WEB-INF/pageComponents/search/advancedSearch.xhtml","console"),
	PERSON_PROFILE_EDITOR("/WEB-INF/pageComponents/identity/personInfoEditor.xhtml" ,"console", "personInfoEditor"),
	SCRIPT_ENGINE("/WEB-INF/pageComponents/scriptEngine/scriptEngine.xhtml","console","scriptEngine"),
	REPOSITORY_EDITOR("/WEB-INF/pageComponents/admin/repositoryEditor.xhtml","console");

	private String dynamicUIAreaPageComponent;	
	private String[] relatedBackingBeanNames;
	
	//This must be exactly the same with <rule if-outcome=..> in pages.xml
	private String pageWhichContainsComponent;
	
	private static Map<String, DynamicUIAreaPageComponent> componentsPerPagePath = new HashMap<String, DynamicUIAreaPageComponent>();
	
	static {
		for (DynamicUIAreaPageComponent dynamiComponent : DynamicUIAreaPageComponent.values())
			componentsPerPagePath.put(dynamiComponent.getDynamicUIAreaPageComponent(), dynamiComponent);
	}
	
	private DynamicUIAreaPageComponent(String dynamicUIAreaPageComponent, String pageWhichContainsComponent, String...relatedBackingBeanNames) {
		this.dynamicUIAreaPageComponent = dynamicUIAreaPageComponent;
		this.relatedBackingBeanNames = relatedBackingBeanNames;
		this.pageWhichContainsComponent = pageWhichContainsComponent;
	}
	
	public String getDynamicUIAreaPageComponent() {
		return dynamicUIAreaPageComponent;
	}
	
	public String[] getRelatedBackingBeanNames() {
		return relatedBackingBeanNames;
	}
	
	public String getPageWhichContainsComponent() {
		return pageWhichContainsComponent;
	}

	/**
	 * Given the currently active page component inside the dynamic UI area, the method finds the inactive page components
	 * and returns their backing beans. We will need these backing beans in order to remove them from the conversation scope
	 * There is a possibility that one or more beans required by inactive pages components are also required by the current active page component.
	 * These beans should not be removed and thus we
	 * exclude from the returned list of backing beans those which are needed by the current page
	 * @param activePageComponent
	 * @return
	 */
	public static List<String> getBackingBeanNamesOfInactivePageComponents(DynamicUIAreaPageComponent activePageComponent) {
		List<String> backingBeanNamesOfInactivePageComponents = new ArrayList<String>();
		List<String> beanNamesRequiredToRemainActive = Arrays.asList(activePageComponent.relatedBackingBeanNames);
		
		for (DynamicUIAreaPageComponent dynamicUIAreaPageComponent : DynamicUIAreaPageComponent.values()) {
			if (!dynamicUIAreaPageComponent.equals(activePageComponent)) {
				// backing beans of inactive page components which are also not required by active page component should be reset 
				// to free memory and allow to later load new page components with fresh backing beans (we do not need the stale state to interfere with new loaded data)
				List<String> potentiallyInactiveBeanNames = Arrays.asList(dynamicUIAreaPageComponent.getRelatedBackingBeanNames());
				
				if (CollectionUtils.isNotEmpty(potentiallyInactiveBeanNames))
					backingBeanNamesOfInactivePageComponents.addAll(potentiallyInactiveBeanNames);
			}
		}
		
		// now we should remove from the list those beans that are required by the currently active page component
		backingBeanNamesOfInactivePageComponents.removeAll(beanNamesRequiredToRemainActive);
		
		return backingBeanNamesOfInactivePageComponents;
	}
	
	
	public static DynamicUIAreaPageComponent getDynamicUIAreaComponent(String pageComponentPath){
		return componentsPerPagePath.get(pageComponentPath);
	}
	
	
}
