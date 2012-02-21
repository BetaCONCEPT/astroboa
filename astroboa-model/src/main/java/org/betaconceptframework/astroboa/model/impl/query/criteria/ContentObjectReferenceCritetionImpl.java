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
package org.betaconceptframework.astroboa.model.impl.query.criteria;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectReferenceCriterion;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentObjectReferenceCritetionImpl extends SimpleCriterionImpl implements ContentObjectReferenceCriterion,Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2784417619841150402L;

	@Override
	public void propertyIsComplex() {
		//Properties of these type are never complex
		//Do nothing
	}

	@Override
	public void setCaseMatching(CaseMatching caseMatching) {
		//Case matching does not play any role in this context
	}

	@Override
	public void setOperator(QueryOperator operator) {
		//Only QueryOperator.EQUALS and QueryOperator.NOT_EQUALS are allowed
		if (operator != null && QueryOperator.EQUALS != operator && QueryOperator.NOT_EQUALS != operator
				&& QueryOperator.IS_NULL != operator && QueryOperator.IS_NOT_NULL != operator){
			throw new CmsException("Invalid query operator "+operator + " for content object reference criterion");
		}
		
		super.setOperator(operator);
	}

	@Override
	public void addContentObjectAsAValue(ContentObject contentObjectReference) {
		if (contentObjectReference != null){
			if (StringUtils.isNotBlank(contentObjectReference.getId())){
				addValue(contentObjectReference.getId());
			}
			else if (StringUtils.isNotBlank(contentObjectReference.getSystemName())){
				addValue(CmsConstants.CONTENT_OBJECT_REFERENCE_CRITERION_VALUE_PREFIX+contentObjectReference.getSystemName());
			}
			else{
				LoggerFactory.getLogger(getClass()).warn("Content Object {} has neither an identifier nor a system name and therefore cannot be used as a criterion value ", contentObjectReference.toString());
			}
		}
		else{
			LoggerFactory.getLogger(getClass()).warn("Content Object is null and therefore cannot be used as a criterion value ");
		}
		
	}

	@Override
	public void addContentObjectsAsValues(
			List<ContentObject> contentObjectReferences) {
		
		if (CollectionUtils.isNotEmpty(contentObjectReferences)){
			for (ContentObject contentObjectReference: contentObjectReferences){
				addContentObjectAsAValue(contentObjectReference);
			}
		}
		
	}

	@Override
	public void addValue(Object value) {
		
		if (value != null && value instanceof ContentObject){
			addContentObjectAsAValue((ContentObject)value);
		}
		else{
			super.addValue(value);
		}
	}

	@Override
	public void setValues(List<Object> values) {
		if (values != null){
			for (Object value : values){
				addValue(value);
			}
		}
	}

}
