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
package org.betaconceptframework.astroboa.model.impl.query.xpath;

import java.util.Random;
import java.util.regex.Pattern;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public final class EscapeTextUtil {

	private final static int randomNumber = new Random().nextInt(100000);
	
	private final static Expression[] expressions = new Expression[]{
			new Expression("\\*", "jcrany"+randomNumber),
			new Expression("\"", "jcrphrase"+randomNumber),
			new Expression("'", "jcrsingle"+randomNumber, "''"),
			new Expression(" AND ", " jcrand "+randomNumber),
			new Expression(" OR ", " jcror "+randomNumber),
			new Expression("-", "jcrdash"+randomNumber),
			new Expression("_", "jcrunderscore"+randomNumber),
			new Expression("\\+", "jcrplus"+randomNumber, "\\\\\\+"),
			new Expression("!", "jcrexclamation"+randomNumber, "\\\\!"),
			new Expression("\\(", "jcrleftparenthesis"+randomNumber, "\\\\("),
			new Expression("\\)", "jcrrightparenthesis"+randomNumber, "\\\\)"),
			new Expression("\\[", "jcrleftsquarebracket"+randomNumber, "\\\\["),
			new Expression("\\]", "jcrrightbracket"+randomNumber, "\\\\]"),
			new Expression("\\{", "jcrleftcurlybracket"+randomNumber, "\\\\{"),
			new Expression("\\}", "jcrrightcurlybracket"+randomNumber, "\\\\}"),
			new Expression("\\^", "jcrcaret"+randomNumber, "\\\\^"),
			new Expression("~", "jcrswungdash"+randomNumber, "\\\\~"),
			new Expression("\\?", "jcrquestionmark"+randomNumber, "\\\\?"),
			new Expression(":", "jcrcolon"+randomNumber, "\\\\:"),
			new Expression("\\\\", "jcrbackslash"+randomNumber, "\\\\\\\\")
			};
	
	public static String escape(String text){
		
		for (Expression expression : expressions){
			text = expression.escape(text);
		}

		return text;
	}

	public static String unescape(String text) {
		for (Expression expression : expressions){
			text = expression.unescape(text);
		}

		return text;
	}
	
	
	private static class Expression{

		private Pattern patternUsedToEscapeExpression;
		private Pattern patternUsedToUnEscapeExpression;
		
		private String temporaryReplacement;
		
		private String finalReplacement;
		
		Expression(String expressionToEscape, String temporaryReplacement, String finalReplacement){
			
			patternUsedToEscapeExpression = Pattern.compile(expressionToEscape);
			patternUsedToUnEscapeExpression = Pattern.compile(temporaryReplacement);
			
			this.temporaryReplacement = temporaryReplacement;
			this.finalReplacement = finalReplacement;
			
			if (finalReplacement == null){
				this.finalReplacement = expressionToEscape;
			}
		}
		
		Expression(String expressionToEscape, String temporaryReplacement){
			this(expressionToEscape, temporaryReplacement, expressionToEscape);
		}
		
		public String escape(String text){
			return patternUsedToEscapeExpression.matcher(text).replaceAll(temporaryReplacement);
		}
		
		public String unescape(String text){
			return patternUsedToUnEscapeExpression.matcher(text).replaceAll(finalReplacement);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Expression [finalReplacement=");
			builder.append(finalReplacement);
			builder.append(", patternUsedToEscapeExpression=");
			builder.append(patternUsedToEscapeExpression);
			builder.append(", patternUsedToUnEscapeExpression=");
			builder.append(patternUsedToUnEscapeExpression);
			builder.append(", temporaryReplacement=");
			builder.append(temporaryReplacement);
			builder.append("]");
			return builder.toString();
		}

		
	}
}
