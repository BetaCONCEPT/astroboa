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

package org.betaconceptframework.astroboa.model.impl.query.criteria;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.query.Condition;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.SimpleCriterion.CaseMatching;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class LocalizationCriterionImpl implements LocalizationCriterion, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2161975004277338543L;
	private String locale;
	private List<String> localizedLabels;
	private QueryOperator queryOperator = QueryOperator.LIKE;
	private boolean ignoreCase = false;

	public String getXPath() {

		//Neither locale nor localized labels have been provided
		if (StringUtils.isBlank(locale) && CollectionUtils.isEmpty(localizedLabels))
			return "";

		SimpleCriterion criterion = new SimpleCriterionImpl();
		
		String localizationPropertyPath = getJcrPropertyPathForLocale(locale);
		
		criterion.setProperty(localizationPropertyPath);
		criterion.setOperator(queryOperator);    
		criterion.setInternalCondition(Condition.OR);
		
		if(ignoreCase){
			criterion.setCaseMatching(CaseMatching.LOWER_CASE);
		}

		if (CmsBuiltInItem.Localization.getJcrName().equals(localizationPropertyPath) && QueryOperator.CONTAINS == queryOperator){
			((SimpleCriterionImpl)criterion).propertyIsComplex();
		}
		
		if (CollectionUtils.isNotEmpty(localizedLabels)){
			for (String value : localizedLabels){
				//Trim value before insertion to criterion
				value = value.trim();
				
				criterion.addValue(value);
			}
		}
		
		//Property name of this criterion is CmsBuiltInItem.Localization.getJcrName()
		//which is a complex property
		if (CmsBuiltInItem.Localization.getJcrName().equals(criterion.getProperty())){
			((SimpleCriterionImpl)criterion).propertyIsComplex();
		}
		
		return criterion.getXPath();

	}

	public void addLocalizedLabel(String localizedLabel) {
		if (localizedLabels == null)
			localizedLabels = new ArrayList<String>();


		localizedLabels.add(localizedLabel);

	}

	public void setLocale(String locale) {
		this.locale = locale;

	}

	public void setLocalizedLabels(List<String> localizedLabels) {
		this.localizedLabels = localizedLabels;		
	}

	private String getJcrPropertyPathForLocale(String locale) {

		if (StringUtils.isNotBlank(locale)){
			return CmsBuiltInItem.Localization.getJcrName()+CmsConstants.PERIOD_DELIM+ ItemUtils.createNewBetaConceptItem(locale).getJcrName();
		}
		else{
			return CmsBuiltInItem.Localization.getJcrName();
		}

	}


	public void enableContainsQueryOperator() {
		setQueryOperator(QueryOperator.CONTAINS);
	}

	@Override
	public void ignoreCaseInLabels() {
		ignoreCase = true;
		
	}

	@Override
	public void setQueryOperator(QueryOperator queryOperator) {
		this.queryOperator = queryOperator;
		
		if (this.queryOperator == null){
			this.queryOperator = QueryOperator.LIKE;
		}
		
	}
}
