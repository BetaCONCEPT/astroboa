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

package org.betaconceptframework.astroboa.lucene.analyzer;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class GreekEnglishAnalyzer extends Analyzer{
	 private GreekAnalyzer greek = new GreekAnalyzer();
	 
	 public GreekEnglishAnalyzer(){
		 
	 }
	 
	 public TokenStream tokenStream(String fieldName, Reader reader) {
//	  Filters greek
	 TokenStream tokens = greek.tokenStream(fieldName, reader);

//	  Filters with standard analyzer
	 tokens = new StandardFilter(tokens);
	 tokens = new LowerCaseFilter(tokens);
	 tokens = new StopFilter(tokens, StopAnalyzer.ENGLISH_STOP_WORDS);

	 return tokens;
	 
	 }
	  
}
