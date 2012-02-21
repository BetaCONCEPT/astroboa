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

package org.betaconceptframework.astroboa.engine.definition.xsom;

import org.springframework.beans.factory.annotation.Autowired;
import org.xml.sax.ErrorHandler;

import com.sun.xml.xsom.parser.AnnotationParserFactory;
import com.sun.xml.xsom.parser.XSOMParser;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsXsomParserFactory {

	@Autowired
	private AnnotationParserFactory annotationParserFactory;
	
	@Autowired
	private ErrorHandler errorHandler;
	
	@Autowired
	private EntityResolverForBuiltInSchemas entityResolver;

	public XSOMParser createXsomParser(){
		XSOMParser xsomParser = new XSOMParser();
		xsomParser.setAnnotationParser(annotationParserFactory);
		xsomParser.setErrorHandler(errorHandler);
		xsomParser.setEntityResolver(entityResolver);
		
		return xsomParser;
	}
	
	public XSOMParser createXsomParserForValidation(CmsEntityResolverForValidation entityResolverForValidation){
		XSOMParser xsomParser = new XSOMParser();
		xsomParser.setAnnotationParser(annotationParserFactory);
		xsomParser.setErrorHandler(errorHandler);
		
		if (entityResolverForValidation!=null){
			entityResolverForValidation.setBuiltInEntityResolver(entityResolver);
		}
		
		xsomParser.setEntityResolver(entityResolverForValidation);
		
		return xsomParser;
	}

}
