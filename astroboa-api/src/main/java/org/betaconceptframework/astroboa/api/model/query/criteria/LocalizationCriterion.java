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

package org.betaconceptframework.astroboa.api.model.query.criteria;

import java.util.List;

import org.betaconceptframework.astroboa.api.model.LocalizableEntity;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.Localization;
import org.betaconceptframework.astroboa.api.model.query.QueryOperator;

/**
 * Localization criterion. 
 * 
 * <p>
 * Localization  criterion is used when users want to search for
 * {@link LocalizableEntity entities}, mainly {@link Topic topics},
 * {@link Space spaces} or {@link Taxonomy taxonomies} based on 
 * some constraints about their localized labels.
 * </p>
 * 
 * <p>
 * A localized label of a  {@link LocalizableEntity} is a
 * label for a specific locale (See more in {@link Localization}).
 * With this criterion, users can create various constraints on 
 * a localized label of an entity :
 *
 * <ul>
 * <li>Create a criterion to match all entities whose label for
 *  locale <code>en</code> is either <code>foo</code> or 
 *  <code>bar</code>.  
 *  
 *  <pre>
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.setLocale("en");
 *     localizationCritetion.addLocalizedLabel("foo");
 *     localizationCritetion.addLocalizedLabel("bar");
 *     localizationCritetion.setQueryOperator(QueryOperator.EQUALS);
 *  </pre>
 * </li>
 * 
 * <li>Create a criterion to match all entities which have any label for locale <code>en</code>
 *  <pre>
 *  
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.setLocale("en");
 *     localizationCritetion.setQueryOperator(QueryOperator.IS_NOT_NULL);
 *  </pre>
 * </li>
 *  
 * <li>Create a criterion to match all entities which have values <code>foo<code> or 
 * <code>bar</code> in any locale.
 * 
 *  <pre>
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.addLocalizedLabel("foo");
 *     localizationCritetion.addLocalizedLabel("bar");
 *     localizationCritetion.setQueryOperator(QueryOperator.EQUALS);
 *  </pre>
 *  </li>
 *  
 *  <li>Create a criterion to match all entities whose label for
 *  locale <code>en</code> contain the words <code>foo</code> or <code>far</code>.

 *  <pre>
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.setLocale("en");
 *     localizationCritetion.addLocalizedLabel("foo");
 *     localizationCritetion.addLocalizedLabel("bar");
 *     localizationCritetion.setQueryOperator(QueryOperator.CONTAINS);
 *  </pre>
 * </li>
 * 
 *  <li>Create a criterion to match all entities whose label in any locale
 *  start with <code>foo</code> or <code>bar</code>.  
 *  Make sure that you do add an <code>*</code> at then end of the label.
 *
 *  <pre>
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.addLocalizedLabel("foo*");
 *     localizationCritetion.addLocalizedLabel("bar*");
 *     localizationCritetion.setQueryOperator(QueryOperator.CONTAINS);
 *  </pre>
 * </li>
 *  <li>Create a criterion to match all entities whose label in locale <code>en</code>
 *  start with <code>foo</code> or <code>bar</code>.  
 *  Make sure that you do add an <code>%</code> at then end of the label.
 *
 *  <pre>
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.addLocalizedLabel("foo%");
 *     localizationCritetion.addLocalizedLabel("bar%");
 *     localizationCritetion.setQueryOperator(QueryOperator.LIKE);
 *  </pre>
 * </li>
 *  <li>Create a criterion to match all entities whose label in locale <code>en</code>
 *  start with <code>foo</code> or <code>BAR</code> but to ginore case.  
 *  Make sure that you do add an <code>%</code> at then end of the label.
 *
 *  <pre>
 *     LocalizationCriterion localizationCriterion = CriterionFactory.newLocalizationCriterion();
 *     localizationCritetion.addLocalizedLabel("foo%");
 *     localizationCritetion.addLocalizedLabel("BAR%");
 *     localizationCritetion.setQueryOperator(QueryOperator.LIKE);
 *     localizationCritetion.ignoreCaseInLabels();
 *  </pre>
 * </li>
 *  </ul>
 *  
 *  
 *  </p>
 *  
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface LocalizationCriterion extends Criterion {

	/**
	 * Sets locale restriction.
	 * 
	 * @param locale
	 *            Locale value as defined in {@link Localization}.
	 */
	void setLocale(String locale);

	/**
	 * Create a criterion with {@link QueryOperator#LIKE like} operator for
	 * localizedLabel.
	 * 
	 * @param localizedLabel
	 *            Localized label.
	 */
	void addLocalizedLabel(String localizedLabel);

	/**
	 * Create a criterion with {@link QueryOperator#LIKE like} operator for
	 * localizedLabels.
	 * 
	 * Created criterion will match values containing ANY of the 
	 * provided localized labels.
	 * 
	 * @param localizedLabels
	 *            List of localized labels.
	 */
	void setLocalizedLabels(List<String> localizedLabels);
	
	/**
	 * Enables {@link QueryOperator#CONTAINS contains} query operator.
	 * 
	 * @deprecated use {@link #setQueryOperator(QueryOperator) instead} <code>setQueryOperator(QueryOperator.CONTAINS)</code>
	 */
	@Deprecated
	void enableContainsQueryOperator();
	
	/**
	 * Specify the query operator to be used in this criterion.
	 * 
	 * Default query operator is {@link QueryOperator#LIKE like}.
	 * 
	 * @param queryOperator 
	 */
	void setQueryOperator(QueryOperator queryOperator);

	/**
	 * Use this method to instruct the criterion to ignore case in 
	 * all label values.  
	 */
	void ignoreCaseInLabels();
}
