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

package org.betaconceptframework.astroboa.model.impl;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.jaxb.adapter.NumberOfChildrenAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.RepositoryUserAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.TaxonomyAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.TopicAdapter;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlRootElement(name="topic")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "topicType", propOrder = {
		"name",
		"allowsReferrerContentObjects",
		"order",
		"numberOfChildren",
	"owner",
    "taxonomy",
    "parent",
    "children"
})
public class TopicImpl  extends LocalizableEntityImpl implements Topic, Serializable {

	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = 2829025801426584624L;
	
	@XmlAttribute
	private String name;
	
	
	/**
	 * Used in case parent needs to be lazy loaded
	 */
	@XmlTransient
	private String parentId;


	@XmlAttribute
	private boolean allowsReferrerContentObjects;
	
	//Quick and dirty way to know whether allowsReferrerContentObjects
	//has actually been set by the user or not
	//In the future this variable should be a Boolean.
	//For more details check java doc in CmsRepositoryEntityImpl.systemBuiltInEntity
	@XmlTransient
	private boolean allowsReferrerContentObjectsHasBeenSet;

	@XmlAttribute
	@XmlJavaTypeAdapter(type=int.class, value=NumberOfChildrenAdapter.class)
	private int numberOfChildren=-1;

	@XmlAttribute
	private Long order;

	@XmlElement
	@XmlJavaTypeAdapter(value=RepositoryUserAdapter.class)
	private RepositoryUser owner;

	@XmlTransient
	private int numberOfContentObjectIdsWhichReferToThisTopic = -1;

	@XmlTransient
	private List<String> contentObjectIdsWhichReferToThisTopic ;

	/**
	 * Parent topic
	 */
	@XmlElement(
			name="parentTopic"
	)
	@XmlJavaTypeAdapter(value=TopicAdapter.class)
	private Topic parent;
	/**
	 * Child Topics
	 */
	@XmlElementWrapper(name="childTopics")
	@XmlElement(
			name="topic",
			type=TopicImpl.class
	)
	private List<Topic> children;


	@XmlElement
	@XmlJavaTypeAdapter(value=TaxonomyAdapter.class)
	private Taxonomy taxonomy;
	
	public TopicImpl(){

	}
	
	/**
	 * @return Returns the subTopics.
	 */
	public List<Topic> getChildren() {
		// Load children if not already there
		if (!isChildrenLoaded()) {
			
			//Continue only if there is an Id
			if (StringUtils.isNotBlank(getId() )) {
				getLazyLoader().lazyLoadTopicChildren(this, authenticationToken);
			}
		}	
		return children;
	}

	/**
	 * @param subTopics The subTopics to set.
	 */
	public void setChildren(List<Topic> subTopics) {
		this.children = subTopics;
		
		if (subTopics == null){
			numberOfChildren = 0;
		}
		else{
			numberOfChildren = subTopics.size();
		}
	}

	/**
	 * @return Returns the parent.
	 */
	public Topic getParent() {
		
		if (parent == null && StringUtils.isNotBlank(parentId)){
			LazyLoader lazyLoader = getLazyLoader();
			if (lazyLoader != null){
				getLazyLoader().lazyLoadParentTopic(this, parentId, authenticationToken);
			}
		}
		
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(Topic parent) {
		
		if (parent != null && (parent == this || 
				( StringUtils.equals(parent.getId(), this.getId()) && parent.getId() != null )||  
				( StringUtils.equals(parent.getName(), this.getName()) && parent.getName() != null) )
				){
			throw new CmsException("Topic "+getId() + " : "+getName() + " cannot have itself as a parent");
		}

		this.parent = parent;
		
		if (parent != null && parent.getId() != null){
			this.parentId = parent.getId();
		}
		else{
			//Nullify parentId as well
			this.parentId = null;
		}
	}


	public void addChild(Topic child){
		if (child == null){
			return;
		}

		if (child != null && (child == this || 
				( StringUtils.equals(child.getId(), this.getId()) && child.getId() != null )||  
				( StringUtils.equals(child.getName(), this.getName()) && child.getName() != null) )
				){
			throw new CmsException("Topic "+getId() + " : "+getName() + " cannot have itself as a child");
		}
		
		if (children == null){
			children = new ArrayList<Topic>();
		}
		
		//add child only if it has not been added.
		if (!children.contains(child)){
			children.add(child);
		}
		
		child.setParent(this);
		
		numberOfChildren = children.size();
	}


	public RepositoryUser getOwner() {
		return owner;
	}

	public void setOwner(RepositoryUser owner) {
		this.owner = owner;
	}

	public int getNumberOfChildren() {
		
		if (numberOfChildren < 0 && children != null){
			numberOfChildren = children.size();
		}
		
		if (numberOfChildren < 0){
			return 0;
		}
		
		return numberOfChildren;
	}

	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
	}

	/**
	 * @return the taxonomy
	 */
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	/**
	 * @param taxonomy the taxonomy to set
	 */
	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
		
		//If there are children then children should have the same taxonomy
		if (CollectionUtils.isNotEmpty(children)){
			for (Topic child : children){
				
				Taxonomy childTaxonomy = child.getTaxonomy();
				
				//Both are null, thus they are the same so continue with the other children
				if (childTaxonomy == null && taxonomy == null){
					continue;
				}
				
				//One of them is null therefore change child's taxonomy 
				if ( childTaxonomy == null || taxonomy == null){
					child.setTaxonomy(taxonomy);
				}
				else{
					//They are both not null
					
					//If they are the same continue with the other children
					if (childTaxonomy == taxonomy){
						continue;
					}
					else {
						//If no ids are provided check with their names
						if (childTaxonomy.getId() == null && taxonomy.getId() == null){
							
							//Both have the same name so continue with the other children
							if (StringUtils.equals(childTaxonomy.getName(), taxonomy.getName())){
								continue;
							}
							else{
								//Their names differ, thus change child's taxonomy
								child.setTaxonomy(taxonomy);
							}
						}
						else{
							//One or both ids are not null. If they are the same, continue with the other children
							if (StringUtils.equals(childTaxonomy.getId(), taxonomy.getId())){
								continue;
							}
							else{
								//Different ids, change child's taxonomy
								child.setTaxonomy(taxonomy);
							}
						}
					}
				}
			}
		}
	}

	public Long getOrder() {
		return order;
	}

	public void setOrder(Long order) {
		this.order = order;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isAllowsReferrerContentObjects() {
		return allowsReferrerContentObjects;
	}

	public void setAllowsReferrerContentObjects(
			boolean allowsReferrerContentObjects) {
		allowsReferrerContentObjectsHasBeenSet = true;
		this.allowsReferrerContentObjects = allowsReferrerContentObjects;
	}



	public int getNumberOfReferrerContentObjects() {
		return getNumberOfContentObjectsWhichReferToThisTopic();
	}

	public List<String> getReferrerContentObjects() {
		return getContentObjectIdsWhichReferToThisTopic();
	}

	public void setNumberOfContentObjectIdsWhichReferToThisTopic(int numberOfContentObjectIdsWhichReferToThisTopic) {
		this.numberOfContentObjectIdsWhichReferToThisTopic = numberOfContentObjectIdsWhichReferToThisTopic;

	}

	public void setContentObjectIdsWhichReferToThisTopic(List<String> contentObjectIdsWhichReferToThisTopic) {
		this.contentObjectIdsWhichReferToThisTopic = contentObjectIdsWhichReferToThisTopic;

	}

	public void addContentObjectIdWhichReferToThisTopic(String contentObjectIdWhichReferToThisTopic) {
		if (contentObjectIdWhichReferToThisTopic != null){
			if (contentObjectIdsWhichReferToThisTopic == null)
				contentObjectIdsWhichReferToThisTopic = new ArrayList<String>();

			contentObjectIdsWhichReferToThisTopic.add(contentObjectIdWhichReferToThisTopic);
		}


	}

	public boolean isChildrenLoaded() {
		return children != null;
	}

	public boolean isNumberOfReferrerContentObjectsLoaded() {
		return isNumberOfContentObjectsWhichReferToThisTopicLoaded();
	}

	public boolean isReferrerContentObjectsLoaded() {
		return areContenObjectIdsWhichReferToThisTopicLoaded();
	}

	private LazyLoader getLazyLoader() {
		return AstroboaClientContextHolder.getLazyLoaderForClient(authenticationToken);
	}
	

	public void setParentId(String parentId){
		this.parentId = parentId;
	}

	public void detectCycle(String name) {
		
		if (name == null){
			if (parent != null){
				((TopicImpl)parent).detectCycle(this.name);
			}
		}
		else{
			if (StringUtils.equals(this.name, name)){
				throw new CmsException("Topic with name "+name+ " exists more than once in topic hierarchy");
			}
			else if (parent != null){
				((TopicImpl)parent).detectCycle(name);
			}
		}
		
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TopicImpl [");
		if (name != null)
			builder.append("name=").append(name);
		if (getId() != null)
			builder.append(", ").append("id=").append(getId());
		if (taxonomy != null)
			builder.append(", ").append("taxonomy=").append(taxonomy.toString());
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean areContenObjectIdsWhichReferToThisTopicLoaded() {
		return contentObjectIdsWhichReferToThisTopic != null; 
	}

	@Override
	public List<String> getContentObjectIdsWhichReferToThisTopic() {
		if (!areContenObjectIdsWhichReferToThisTopicLoaded()){
			if (StringUtils.isNotBlank(getId())){
				getLazyLoader().lazyLoadContentObjectIdsWhichReferToTopic(this, authenticationToken);
			}
		}
		return contentObjectIdsWhichReferToThisTopic;	
	}

	@Override
	public int getNumberOfContentObjectsWhichReferToThisTopic() {
		if (!isNumberOfContentObjectsWhichReferToThisTopicLoaded()){
			if (StringUtils.isNotBlank(getId())){
				getLazyLoader().lazyLoadNumberOfContentObjectIdsWhichReferToTopic(this, authenticationToken);
			}

		}
		
		return numberOfContentObjectIdsWhichReferToThisTopic == -1 ? 0: numberOfContentObjectIdsWhichReferToThisTopic;
	}

	@Override
	public boolean isNumberOfContentObjectsWhichReferToThisTopicLoaded() {
		return numberOfContentObjectIdsWhichReferToThisTopic != -1;
	}

	public boolean allowsReferrerContentObjectsHasBeenSet() {
		return allowsReferrerContentObjectsHasBeenSet;
	}
	

	
}
