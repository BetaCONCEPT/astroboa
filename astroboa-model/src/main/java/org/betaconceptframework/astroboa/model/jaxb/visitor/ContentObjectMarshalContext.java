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
package org.betaconceptframework.astroboa.model.jaxb.visitor;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.commons.lang.BooleanUtils;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.jaxb.type.ComplexCmsPropertyType;
import org.betaconceptframework.astroboa.model.jaxb.type.ContentObjectType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectMarshalContext {

	final Logger logger = LoggerFactory.getLogger(getClass());
	
	private ContentObjectType contentObjectType;
	
	private Deque<CmsPropertyInfo> complexCmsPropertyInfos = new ArrayDeque<CmsPropertyInfo>();
	private Deque<ComplexCmsPropertyType> complexCmsPropertyTypes = new ArrayDeque<ComplexCmsPropertyType>();
	private Deque<Boolean> aspectsAreVisitedQueue = new ArrayDeque<Boolean>();
	
	public ContentObjectMarshalContext(ContentObjectType contentObjectType) 
	{

		if (contentObjectType == null){
			throw new CmsException("No JAXB element of type ContentObjectType is provided");
		}
		else{
			this.contentObjectType = contentObjectType;
		}
	
	}

	public void addComplexCmsPropertyInfoToQueue(CmsPropertyInfo complexCmsPropertyInfo) {
		
		complexCmsPropertyInfos.push(complexCmsPropertyInfo);
		
	}

	public ContentObjectType getContentObjectType() {
		return contentObjectType;
	}

	public void pushComplexCmsPropertyInfo(CmsPropertyInfo complexCmsPropertyInfo) {
		logger.debug("BEFORE PUSH ComplexCmsPropertyInfo queue size {} for Thread {}", complexCmsPropertyInfos.size(), Thread.currentThread().getName());

		complexCmsPropertyInfos.push(complexCmsPropertyInfo);
		
		logger.debug("AFTER PUSH ComplexCmsPropertyInfo queue size {} for Thread {}", complexCmsPropertyInfos.size(), Thread.currentThread().getName());

	}

	public void pushComplexCmsPropertyType(ComplexCmsPropertyType value) {
		logger.debug("BEFORE PUSH ComplexCmsPropertyType  queue size {} for Thread {}", complexCmsPropertyTypes.size(), Thread.currentThread().getName());

		complexCmsPropertyTypes.push(value);
		
		logger.debug("AFTER PUSH ComplexCmsPropertyType  queue size {} for Thread {}", complexCmsPropertyTypes.size(), Thread.currentThread().getName());
	}

	public String getContextInfo() {
		StringBuilder sb = new StringBuilder("\t\tCurrent parent complex cms property");
		
		if (complexCmsPropertyInfos.peekFirst() != null){
			sb.append(complexCmsPropertyInfos.peekFirst().getFullPath());
		}
		
		sb.append("\t\tCurrent parent type ");
		
		if (complexCmsPropertyTypes.size() > 0){
			if ( complexCmsPropertyTypes.peekFirst().getCmsIdentifier() != null){
				sb.append(complexCmsPropertyTypes.peekFirst().getCmsIdentifier());
			}
			else if (contentObjectType != null){
				sb.append(contentObjectType.getContentObjectTypeName());
			}
		}
		
		return sb.toString();
	}

	public void pushAspectsAreVisited(boolean b) {
		aspectsAreVisitedQueue.push(b);
		
	}

	public void pollComplexCmsPropertyInfo() {
		logger.debug("BEFORE POP ComplexCmsProperty queue size {} for Thread {}", complexCmsPropertyInfos.size(), Thread.currentThread().getName());

		complexCmsPropertyInfos.poll();
		
		logger.debug("AFTER POP ComplexCmsProperty queue size {} for Thread {}", complexCmsPropertyInfos.size(), Thread.currentThread().getName());

	}

	public void pollComplexCmsPropertyType() {
		logger.debug("BEFORE POP ComplexCmsPropertyType  queue size {} for Thread {}", complexCmsPropertyTypes.size(), Thread.currentThread().getName());

		complexCmsPropertyTypes.poll();
		
		logger.debug("AFTER POP ComplexCmsPropertyType  queue size {} for Thread {}", complexCmsPropertyTypes.size(), Thread.currentThread().getName());
	}

	public void pollAspectsVisited() {
		aspectsAreVisitedQueue.poll();
	}

	public CmsPropertyInfo getFirstComplexCmsPropertyInfo() {
		return complexCmsPropertyInfos.peekFirst();
	}

	public ComplexCmsPropertyType getFirstComplexCmsPropertyType() {
		return complexCmsPropertyTypes.peekFirst();
	}

	public Boolean aspectsAreVisited() {
		return BooleanUtils.isTrue(aspectsAreVisitedQueue.peekFirst());
	}
}
