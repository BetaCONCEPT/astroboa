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

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.model.jaxb.adapter.NumberOfChildrenAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.RepositoryUserAdapter;
import org.betaconceptframework.astroboa.model.jaxb.adapter.SpaceAdapter;
import org.betaconceptframework.astroboa.model.lazy.LazyLoader;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
@XmlRootElement(name="space")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "spaceType", propOrder = {
		"name",
		"order",
		"numberOfChildren",
	"owner",
	"parent",
    "children"
})
public class SpaceImpl  extends LocalizableEntityImpl implements Space, Serializable{
	
	/**
	 * 
	 */
	@XmlTransient
	private static final long serialVersionUID = -8652121215146718759L;

	@XmlTransient
	private int numberOfContentObjectReferences;
	
	@XmlTransient
	private List<String> contentObjectReferences ;
	
	@XmlAttribute
	private String name;
	
	/**
	 * Parent space
	 */
	@XmlElement(
			name="parentSpace"
			)
	@XmlJavaTypeAdapter(value=SpaceAdapter.class)
	private Space parent;

	/**
	 * Child spaces
	 */
	@XmlElementWrapper(name="childSpaces")
	@XmlElement(
			name="space",
			type=SpaceImpl.class
			)
	private List<Space> children;

	@XmlAttribute
	@XmlJavaTypeAdapter(type=int.class, value=NumberOfChildrenAdapter.class)
	private int numberOfChildren=-1;

	@XmlAttribute
	private Long order;

	@XmlElement
	@XmlJavaTypeAdapter(value=RepositoryUserAdapter.class)
	private RepositoryUser owner;

	@XmlTransient
	private int numberOfReferrerContentObjects = -1;

	@XmlTransient
	private List<String> referrerContentObjects ;

	public SpaceImpl(){
	}
	
	/**
	 * @return Returns the subSpaces.
	 */
	public List<Space> getChildren() {
		// Load children if not already there
		if (!isChildrenLoaded()) {
			
			//Continue only if there is an Id
			if (StringUtils.isNotBlank(getId() )) {
				getLazyLoader().lazyLoadSpaceChildren(this, authenticationToken);
			}
		}	
		return children;
	}

	/**
	 * @param subSpaces The subSpaces to set.
	 */
	public void setChildren(List<Space> subSpaces) {
		this.children = subSpaces;
		
		if (subSpaces == null){
			numberOfChildren = 0;
		}
		else{
			numberOfChildren = subSpaces.size();
		}

	}

	/**
	 * @return Returns the parent.
	 */
	public Space getParent() {
		return parent;
	}

	/**
	 * @param parent The parent to set.
	 */
	public void setParent(Space parent) {
		
		if (parent != null && (parent == this || 
				( StringUtils.equals(parent.getId(), this.getId()) && parent.getId() != null ) )
				){
			throw new CmsException("Space "+getId() + " : "+getName() + " cannot have itself as a parent");
		}

		this.parent = parent;
	}


	public void addChild(Space child)
	{
		if (child == null){
			return;
		}

		if (child != null && (child == this || 
				( StringUtils.equals(child.getId(), this.getId()) && child.getId() != null ) )
				){
			throw new CmsException("Space "+getId() + " : "+getName() + " cannot have itself as a child");
		}


		if (this.children == null){
			this.children = new ArrayList<Space>();
		}

		this.children.add(child);
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
		
		if (numberOfChildren < 0 ){
			return 0;
		}
		
		return numberOfChildren;
	}

	public void setNumberOfChildren(int numberOfChildren) {
		this.numberOfChildren = numberOfChildren;
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

	public int compareTo(Space anotherSpace) {
		if (this.getOrder() == null && anotherSpace.getOrder() == null)
			return 0;
		else if (this.getOrder() != null && anotherSpace.getOrder() == null)
			return 1;
		else if (this.getOrder() == null && anotherSpace.getOrder() != null)
			return -1;
		else {
			Long orderDifference = this.getOrder() - anotherSpace.getOrder();
			if (orderDifference > 0)
				return 1;
			else if (orderDifference < 0)
				return -1;
			else
				return 0;
		}
	}

	public int getNumberOfReferrerContentObjects() {
		if (!isNumberOfReferrerContentObjectsLoaded())
		{
			if (StringUtils.isNotBlank(getId())){
				getLazyLoader().lazyLoadNumberOfReferrerContentObjectsForSpace(this, authenticationToken);
			}

		}
		
		return numberOfReferrerContentObjects == -1 ? 0: numberOfReferrerContentObjects;
	}

	public List<String> getReferrerContentObjects() {
		if (!isReferrerContentObjectsLoaded())
		{
			if (StringUtils.isNotBlank(getId()))
			{
				getLazyLoader().lazyLoadReferrerContentObjectsForSpace(this, authenticationToken);
			}
		}
		return referrerContentObjects;
	}

	public void setNumberOfReferrerContentObjects(
			int numberOfReferrerContentObjects) {
		this.numberOfReferrerContentObjects = numberOfReferrerContentObjects;

	}

	public void setReferrerContentObjects(List<String> referrerContentObjects) {
		this.referrerContentObjects = referrerContentObjects;

	}

	public void addReferrerContentObject(String referrerContentObject) {
		if (referrerContentObject != null)
		{
			if (referrerContentObjects == null)
				referrerContentObjects = new ArrayList<String>();

			referrerContentObjects.add(referrerContentObject);
		}


	}

	public boolean isChildrenLoaded() {
		return children != null;
	}

	public boolean isNumberOfReferrerContentObjectsLoaded() {
		return numberOfReferrerContentObjects != -1;
	}

	public boolean isReferrerContentObjectsLoaded() {
		return referrerContentObjects != null;
	}

	public void addContentObjectReference(String contentObjectReference) {
		if (contentObjectReference != null)
		{
			if (contentObjectReferences == null)
				contentObjectReferences = new ArrayList<String>();
			
			contentObjectReferences.add(contentObjectReference);
		}
	}
	
	public List<String> getContentObjectReferences() {
		
		if (!isContentObjectReferencesLoaded()){
			if (StringUtils.isNotBlank(getId())){
				getLazyLoader().lazyLoadContentObjectReferences(this, authenticationToken);
			}
		}
		
		return contentObjectReferences;
	}

	public int getNumberOfContentObjectReferences() {
		return numberOfContentObjectReferences;
	}

	public void setNumberOfContentObjectReferences(int numberOfContentObjectReferences) {
		this.numberOfContentObjectReferences = numberOfContentObjectReferences;
		
	}

	public void setContentObjectReferences(List<String> contentObjectReferences) {
		this.contentObjectReferences = contentObjectReferences;
		
	}

	public boolean isContentObjectReferencesLoaded() {
		return contentObjectReferences != null;
	}
	
	private LazyLoader getLazyLoader() {
		return AstroboaClientContextHolder.getLazyLoaderForClient(authenticationToken);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SpaceImpl [");
		if (name != null)
			builder.append("name=").append(name);
		if (getId() != null)
			builder.append(", ").append("id=").append(getId());
		builder.append("]");
		return builder.toString();
	}

}
