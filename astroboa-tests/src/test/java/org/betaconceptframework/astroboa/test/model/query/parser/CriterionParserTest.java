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
package org.betaconceptframework.astroboa.test.model.query.parser;

import org.betaconceptframework.astroboa.api.model.query.QueryOperator;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria.SearchMode;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.Criterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.LocalizationCriterion;
import org.betaconceptframework.astroboa.api.model.query.criteria.TopicCriteria;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.factory.CriterionFactory;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CriterionParserTest {

	
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Test  
	public void testParseExpressionToCriterionForTopicCriteria() throws Exception {  

		//Reserved property paths
		checkExpressionForTopicCriteria("name=\"1\"", CriterionFactory.equals(CmsBuiltInItem.Name.getJcrName(), "1"));
		checkExpressionForTopicCriteria("name!=\"1\"", CriterionFactory.notEquals(CmsBuiltInItem.Name.getJcrName(), "1"));
		checkExpressionForTopicCriteria("name CONTAINS \"1\"", CriterionFactory.contains(CmsBuiltInItem.Name.getJcrName(), "1"));
		checkExpressionForTopicCriteria("name CONTAINS \"1\"", CriterionFactory.createSimpleCriterion(CmsBuiltInItem.Name.getJcrName(), "1",QueryOperator.CONTAINS));
		checkExpressionForTopicCriteria("name%%\"1%\"", CriterionFactory.like(CmsBuiltInItem.Name.getJcrName(), "1%"));
		checkExpressionForTopicCriteria("name%%\"1\"", CriterionFactory.like(CmsBuiltInItem.Name.getJcrName(), "1"));
		
		checkExpressionForTopicCriteria("label=\"1\"", createLocalizationCriterion(null, "1", QueryOperator.EQUALS));
		checkExpressionForTopicCriteria("label!=\"1\"", createLocalizationCriterion(null, "1", QueryOperator.NOT_EQUALS));
		checkExpressionForTopicCriteria("label CONTAINS \"1\"", createLocalizationCriterion(null, "1", QueryOperator.CONTAINS));
		checkExpressionForTopicCriteria("label%%\"1%\"", createLocalizationCriterion(null, "1%", QueryOperator.LIKE));
		checkExpressionForTopicCriteria("label%%\"1\"", createLocalizationCriterion(null, "1", QueryOperator.LIKE));

		checkExpressionForTopicCriteria("label.en=\"1\"", createLocalizationCriterion("en", "1", QueryOperator.EQUALS));
		checkExpressionForTopicCriteria("label.en!=\"1\"", createLocalizationCriterion("en", "1", QueryOperator.NOT_EQUALS));
		checkExpressionForTopicCriteria("label.en CONTAINS \"1\"", createLocalizationCriterion("en", "1", QueryOperator.CONTAINS));
		checkExpressionForTopicCriteria("label.en%%\"1%\"", createLocalizationCriterion("en", "1%", QueryOperator.LIKE));
		checkExpressionForTopicCriteria("label.en%%\"1\"", createLocalizationCriterion("en", "1", QueryOperator.LIKE));

	}

	private LocalizationCriterion createLocalizationCriterion(String locale, String label, QueryOperator operator) {
		LocalizationCriterion locLabelCriterion = CriterionFactory.newLocalizationCriterion();
		locLabelCriterion.addLocalizedLabel(label);
		locLabelCriterion.setLocale(locale);
		locLabelCriterion.setQueryOperator(operator);
		return locLabelCriterion;
	}  

	@Test  
	public void testParseExpressionToCriterion() throws Exception {  

		String propertyPath1="title";
		String propertyPath2="profile.subject";
		String propertyPath3="profile.language";

		//Reserved property paths
		checkExpressionForContentObjectCriteria("contentTypeName=\"1\"", CriterionFactory.equals(CmsBuiltInItem.ContentObjectTypeName.getJcrName(), "1"));
		checkExpressionForContentObjectCriteria("id=\"1\"", CriterionFactory.equals(CmsBuiltInItem.CmsIdentifier.getJcrName(), "1"));
		checkExpressionForContentObjectCriteria("systemName=\"1\"", CriterionFactory.equals(CmsBuiltInItem.SystemName.getJcrName(), "1"));
		checkExpressionForContentObjectCriteria("systemName%%\"1\"", CriterionFactory.like(CmsBuiltInItem.SystemName.getJcrName(), "1"));
		checkTextSearchExpression("textSearched=\"1\"", "1");
		checkTextSearchExpression("textSearched=\"^\"", "^");
		checkTextSearchExpression("textSearched=\"{\"", "{");
		checkTextSearchExpression("textSearched=\"}\"", "}");
		checkTextSearchExpression("textSearched=\"]\"", "]");
		checkTextSearchExpression("textSearched=\"[\"", "[");
		checkTextSearchExpression("textSearched=\"?\"", "?");
		checkTextSearchExpression("textSearched=\":\"", ":");
		checkTextSearchExpression("textSearched=\"!\"", "!");
		checkTextSearchExpression("textSearched=\"-\"", "-");
		checkTextSearchExpression("textSearched=\"astroboa1\"", "astroboa1");
		checkTextSearchExpression("textSearched=\"astroboa^\"", "astroboa^");
		checkTextSearchExpression("textSearched=\"astroboa{\"", "astroboa{");
		checkTextSearchExpression("textSearched=\"astroboa}\"", "astroboa}");
		checkTextSearchExpression("textSearched=\"astroboa]\"", "astroboa]");
		checkTextSearchExpression("textSearched=\"astroboa[\"", "astroboa[");
		checkTextSearchExpression("textSearched=\"astroboa?\"", "astroboa?");
		checkTextSearchExpression("textSearched=\"astroboa:\"", "astroboa:");
		checkTextSearchExpression("textSearched=\"astroboa!\"", "astroboa!");
		checkTextSearchExpression("textSearched=\"astroboa-\"", "astroboa-");
		checkTextSearchExpression("textSearched=\"\"", ""); //Empty string no search expression
		
		checkTextSearchExpression("textSearched=\"astro*boa-\"", "astro*boa-");
		checkTextSearchExpression("textSearched=\"*astroboa-\"", "*astroboa-");
		checkTextSearchExpression("textSearched=\"astroboa -\"", "astroboa -");
		checkTextSearchExpression("textSearched=\"astroboa \"", "astroboa");
		checkTextSearchExpression("textSearched=\" astroboa\"", "astroboa");
		checkTextSearchExpression("textSearched=\"astro boa\"", "astro boa");
		checkTextSearchExpression("textSearched=\"ast*ro boa\"", "ast*ro boa");
		
		checkTextSearchExpressionWithAnotherExpression(propertyPath1+"=\"3\" AND textSearched=\"astroboa\"", 
				"astroboa", CriterionFactory.and(null, CriterionFactory.equals(propertyPath1, 3))); //Need to create a fake AND criterion to produce the necessary parenthesis

		checkTextSearchExpressionWithAnotherExpression(propertyPath1+"=\"3\" AND textSearched=\"astroboa OR API\"", 
				"astroboa OR API", CriterionFactory.and(null, CriterionFactory.equals(propertyPath1, 3))); //Need to create a fake AND criterion to produce the necessary parenthesis

		
		//Boolean Values 
		checkExpressionForContentObjectCriteria(propertyPath1+"=\"true\"", CriterionFactory.equals(propertyPath1, true));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"true\")", CriterionFactory.equals(propertyPath1, true));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"false\")", CriterionFactory.equals(propertyPath1, false));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"TRUE\")", CriterionFactory.equals(propertyPath1, true));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"FALSE\")", CriterionFactory.equals(propertyPath1, false));

		//Date Values
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"2009-04-14T19:21:51.000+03:00\")", CriterionFactory.equals(propertyPath1, DateUtils.fromString("2009-04-14T19:21:51.000+0300", "yyyy-MM-dd'T'HH:mm:ss.SSSZ")));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"2009-04-14T19:21:51+03:00\")", CriterionFactory.equals(propertyPath1, DateUtils.fromString("2009-04-14T19:21:51+0300", "yyyy-MM-dd'T'HH:mm:ssZ")));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"2009-04-14\")", CriterionFactory.equals(propertyPath1, DateUtils.fromString("2009-04-14", "yyyy-MM-dd")));

		//Topic values
		checkExpressionForContentObjectCriteria("("+propertyPath2+" IS_NULL)", CriterionFactory.newTopicReferenceCriterion("profile.subject", (String)null, QueryOperator.EQUALS, false));
		checkExpressionForContentObjectCriteria("("+propertyPath2+" IS_NOT_NULL)", CriterionFactory.newTopicReferenceCriterion("profile.subject", (String)null, QueryOperator.NOT_EQUALS, false));
		checkExpressionForContentObjectCriteria("("+propertyPath2+" IS_NULL)", CriterionFactory.newTopicReferenceCriterion("profile.subject", (String)null, QueryOperator.IS_NULL, false));
		checkExpressionForContentObjectCriteria("("+propertyPath2+" IS_NOT_NULL)", CriterionFactory.newTopicReferenceCriterion("profile.subject", (String)null, QueryOperator.IS_NOT_NULL, false));
		checkExpressionForContentObjectCriteria("("+propertyPath2+"=\"1234\")", CriterionFactory.newTopicReferenceCriterion("profile.subject", "1234", QueryOperator.EQUALS, false));
		
		checkExpressionForContentObjectCriteria("bccms:contentObjectType=\"portal\"", CriterionFactory.equals("bccms:contentObjectType", "portal"));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"3\")", CriterionFactory.equals(propertyPath1, "3"));
		checkExpressionForContentObjectCriteria(""+propertyPath1+" IS_NOT_NULL", CriterionFactory.isNotNull(propertyPath1));
		checkExpressionForContentObjectCriteria(""+propertyPath1+" IS_NULL", CriterionFactory.isNull(propertyPath1));

		//These two are invalid yet no parse exception is thrown and
		//criterion built is the right one
		checkExpressionForContentObjectCriteria(""+propertyPath1+" IS_NULL \"sasa\"", CriterionFactory.isNull(propertyPath1));
		checkExpressionForContentObjectCriteria(""+propertyPath1+" IS_NULLsasa", CriterionFactory.isNull(propertyPath1));

		Criterion andWithIsNull = CriterionFactory.and(CriterionFactory.isNull(propertyPath1), CriterionFactory.equals(propertyPath2, 3));

		checkExpressionForContentObjectCriteria(""+propertyPath1+" IS_NULL AND "+propertyPath2+"=\"3\"", andWithIsNull);
		checkExpressionForContentObjectCriteria("("+propertyPath1+" IS_NULL) AND ("+propertyPath2+"=\"3\")", andWithIsNull);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+" IS_NULL) AND ("+propertyPath2+"=\"3\") )", andWithIsNull);
		checkExpressionForContentObjectCriteria("( "+propertyPath1+" IS_NULL AND "+propertyPath2+"=\"3\" )", andWithIsNull);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+" IS_NULL) AND "+propertyPath2+"=\"3\" )", andWithIsNull);

		checkAndWithQueryOperator(propertyPath1, propertyPath2, QueryOperator.EQUALS);
		checkAndWithQueryOperator(propertyPath1, propertyPath2, QueryOperator.GREATER);
		checkAndWithQueryOperator(propertyPath1, propertyPath2, QueryOperator.GREATER_EQUAL);
		checkAndWithQueryOperator(propertyPath1, propertyPath2, QueryOperator.LESS);
		checkAndWithQueryOperator(propertyPath1, propertyPath2, QueryOperator.LESS_EQUAL);
		checkAndWithQueryOperator(propertyPath1, propertyPath2, QueryOperator.LIKE);

		Criterion and = CriterionFactory.and(CriterionFactory.equals(propertyPath1, 3), CriterionFactory.equals(propertyPath2, 3));
		Criterion andTwice = CriterionFactory.and(and, CriterionFactory.equals(propertyPath3, 4));
		checkExpressionForContentObjectCriteria(""+propertyPath1+"=\"3\" AND "+propertyPath2+"=\"3\" AND "+propertyPath3+"=\"4\"", andTwice);
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"3\") AND ("+propertyPath2+"=\"3\") AND ("+propertyPath3+"=\"4\")", andTwice);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\") AND ("+propertyPath2+"=\"3\") AND ("+propertyPath3+"=\"4\") )", andTwice);
		checkExpressionForContentObjectCriteria("( "+propertyPath1+"=\"3\" AND "+propertyPath2+"=\"3\" AND "+propertyPath3+"=\"4\" )", andTwice);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\" AND "+propertyPath2+"=\"3\") AND "+propertyPath3+"=\"4\" )", andTwice);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\" AND ("+propertyPath2+"=\"3\")) AND "+propertyPath3+"=\"4\" )", andTwice);

		Criterion or = CriterionFactory.or(CriterionFactory.equals(propertyPath1, 3), CriterionFactory.equals(propertyPath2, 3));

		checkExpressionForContentObjectCriteria(""+propertyPath1+"=\"3\" OR "+propertyPath2+"=\"3\"", or);
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"3\") OR ("+propertyPath2+"=\"3\")", or);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\") OR ("+propertyPath2+"=\"3\") )", or);
		checkExpressionForContentObjectCriteria("( "+propertyPath1+"=\"3\" OR "+propertyPath2+"=\"3\" )", or);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\") OR "+propertyPath2+"=\"3\" )", or);

		Criterion orTwice = CriterionFactory.or(or, CriterionFactory.equals(propertyPath3, 4));
		checkExpressionForContentObjectCriteria(""+propertyPath1+"=\"3\" OR "+propertyPath2+"=\"3\" OR "+propertyPath3+"=\"4\"", orTwice);
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"3\") OR ("+propertyPath2+"=\"3\") OR ("+propertyPath3+"=\"4\")", orTwice);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\") OR ("+propertyPath2+"=\"3\") OR ("+propertyPath3+"=\"4\") )", orTwice);
		checkExpressionForContentObjectCriteria("( "+propertyPath1+"=\"3\" OR "+propertyPath2+"=\"3\" OR "+propertyPath3+"=\"4\" )", orTwice);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\" OR "+propertyPath2+"=\"3\") OR "+propertyPath3+"=\"4\" )", orTwice);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+"=\"3\" OR ("+propertyPath2+"=\"3\")) OR "+propertyPath3+"=\"4\" )", orTwice);

		Criterion andWithOr = CriterionFactory.and(CriterionFactory.equals(propertyPath1, 3), CriterionFactory.or(CriterionFactory.equals(propertyPath2, 3), 
				CriterionFactory.equals(propertyPath3, 4)));
		checkExpressionForContentObjectCriteria(""+propertyPath1+"=\"3\" AND "+propertyPath2+"=\"3\" OR "+propertyPath3+"=\"4\"", andWithOr);

		Criterion andWithOr2 = CriterionFactory.or(CriterionFactory.and(CriterionFactory.equals(propertyPath1, 3),CriterionFactory.equals(propertyPath2, 3)), 
				CriterionFactory.equals(propertyPath3, 4));
		checkExpressionForContentObjectCriteria("("+propertyPath1+"=\"3\" AND "+propertyPath2+"=\"3\") OR "+propertyPath3+"=\"4\"", andWithOr2);

	}  

	private void checkAndWithQueryOperator(String propertyPath1, String propertyPath2, QueryOperator queryOperator) throws Exception{
		Criterion and = null;

		String operatorAsString = queryOperator.getOp();
		String value = "\"3\"";
		if (QueryOperator.LIKE == queryOperator){
			operatorAsString = "%%";
			value="3%";
		}

		switch (queryOperator) {
		case EQUALS:
			and = CriterionFactory.and(CriterionFactory.equals(propertyPath1, 3), CriterionFactory.equals(propertyPath2, 3));			
			break;
		case GREATER:
			and = CriterionFactory.and(CriterionFactory.greaterThan(propertyPath1, 3), CriterionFactory.greaterThan(propertyPath2, 3));
			break;
		case GREATER_EQUAL:
			and = CriterionFactory.and(CriterionFactory.greaterThanOrEquals(propertyPath1, 3), CriterionFactory.greaterThanOrEquals(propertyPath2, 3));
			break;
		case LESS:
			and = CriterionFactory.and(CriterionFactory.lessThan(propertyPath1, 3), CriterionFactory.lessThan(propertyPath2, 3));
			break;
		case LESS_EQUAL:
			and = CriterionFactory.and(CriterionFactory.lessThanOrEquals(propertyPath1, 3), CriterionFactory.lessThanOrEquals(propertyPath2, 3));
			break;
		case LIKE:
			and = CriterionFactory.and(CriterionFactory.like(propertyPath1, value), CriterionFactory.like(propertyPath2, value));
			value="\""+value+"\"";
			break;	


		default:
			break;
		}

		checkExpressionForContentObjectCriteria(""+propertyPath1+operatorAsString+value+" AND "+propertyPath2+operatorAsString+value, and);
		checkExpressionForContentObjectCriteria("("+propertyPath1+operatorAsString+value+") AND ("+propertyPath2+operatorAsString+value+")", and);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+operatorAsString+value+") AND ("+propertyPath2+operatorAsString+value+") )", and);
		checkExpressionForContentObjectCriteria("( "+propertyPath1+operatorAsString+value+" AND "+propertyPath2+operatorAsString+value+" )", and);
		checkExpressionForContentObjectCriteria("( ("+propertyPath1+operatorAsString+value+") AND "+propertyPath2+operatorAsString+value+" )", and);
	}


	private void checkExpressionForTopicCriteria(String expression, Criterion expectedCriterion) throws Exception {
		checkExpression(expression, expectedCriterion, false);
	}

	private void checkExpressionForContentObjectCriteria(String expression, Criterion expectedCriterion) throws Exception {
		checkExpression(expression, expectedCriterion, true);
	}

	private void checkExpression(String expression, Criterion expectedCriterion, boolean useContentObjectCriteria) throws Exception {

		try{
			CmsCriteria parserCmsCriteria = null;
			CmsCriteria expectedCmsCriteria = null;
			
			if (useContentObjectCriteria){
				parserCmsCriteria = CmsCriteriaFactory.newContentObjectCriteria();
				expectedCmsCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			}
			else{
				parserCmsCriteria = CmsCriteriaFactory.newTopicCriteria();
				expectedCmsCriteria = CmsCriteriaFactory.newTopicCriteria();
				
			}
			
			expectedCmsCriteria.addCriterion(expectedCriterion);
			
			if (useContentObjectCriteria){
				CriterionFactory.parse(expression, (ContentObjectCriteria) parserCmsCriteria);
			}
			else {
				CriterionFactory.parse(expression, (TopicCriteria) parserCmsCriteria);
			}
			
			assertCriterionEquals(parserCmsCriteria, expectedCmsCriteria);

			logger.info("Expression : "+ expression + " produced XPATH : "+ parserCmsCriteria.getXPathQuery());
		}
		catch (RuntimeException e){
			logger.error(expression);
			throw e;
		}
	}
	
	private void checkTextSearchExpression(String expression, String textSearch) throws Exception {

		try{
			ContentObjectCriteria parserContentOjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			CriterionFactory.parse(expression, parserContentOjectCriteria);
			
			ContentObjectCriteria expectedContentOjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			expectedContentOjectCriteria.addFullTextSearchCriterion(textSearch);
			
			assertCriterionEquals(parserContentOjectCriteria, expectedContentOjectCriteria);

			logger.info("Expression : "+ expression + " produced XPATH : "+ parserContentOjectCriteria.getXPathQuery());
		}
		catch (RuntimeException e){
			logger.error(expression);
			throw e;
		}
	}
	
	private void checkTextSearchExpressionWithAnotherExpression(String expression, String textSearch, Criterion additionalExpression) throws Exception {

		try{
			ContentObjectCriteria parserContentOjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			CriterionFactory.parse(expression, parserContentOjectCriteria);
			
			ContentObjectCriteria expectedContentOjectCriteria = CmsCriteriaFactory.newContentObjectCriteria();
			expectedContentOjectCriteria.addFullTextSearchCriterion(textSearch);
			expectedContentOjectCriteria.addCriterion(additionalExpression);
			
			assertCriterionEquals(parserContentOjectCriteria, expectedContentOjectCriteria);

			logger.info("Expression : "+ expression + " produced XPATH : "+ parserContentOjectCriteria.getXPathQuery());
		}
		catch (RuntimeException e){
			logger.error(expression);
			throw e;
		}
	}




	private void assertCriterionEquals(CmsCriteria parserCmsCriteria, CmsCriteria expectedCmsCriteria){

		Assert.assertNotNull(parserCmsCriteria, "No criteria provided by parser");
		Assert.assertNotNull(expectedCmsCriteria, "No criteria provided by user");

		Assert.assertEquals(parserCmsCriteria.getXPathQuery(), expectedCmsCriteria.getXPathQuery());
	}

}
