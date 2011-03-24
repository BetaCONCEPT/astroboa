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

package org.betaconceptframework.astroboa.engine.definition.xsom;

import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import com.sun.xml.xsom.parser.AnnotationContext;
import com.sun.xml.xsom.parser.AnnotationParser;


/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsAnnotationParser extends AnnotationParser{

	private CmsAnnotationHandler cmsAnnotationHandler;
	private CmsAnnotation cmsannotation ; 

	
	public CmsAnnotationParser() {
		super();
		cmsAnnotationHandler = new CmsAnnotationHandler();
	}

	@Override
	public ContentHandler getContentHandler(AnnotationContext arg0, String arg1, ErrorHandler arg2, EntityResolver arg3) {
		cmsannotation = cmsAnnotationHandler.getCmsAnnotation();

		return cmsAnnotationHandler;
	}

	@Override
	public Object getResult(Object arg0) {

		return cmsannotation;
	}

	public void setCmsAnnotationHandler(CmsAnnotationHandler cmsAnnotationHandler) {
		this.cmsAnnotationHandler = cmsAnnotationHandler;
	}
	
	
}
