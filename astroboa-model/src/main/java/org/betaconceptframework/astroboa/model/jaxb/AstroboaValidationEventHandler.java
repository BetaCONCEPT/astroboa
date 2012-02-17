/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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
package org.betaconceptframework.astroboa.model.jaxb;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Used to control validation events. Just log any warnings.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class AstroboaValidationEventHandler implements ValidationEventHandler, ErrorHandler {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public boolean handleEvent(ValidationEvent arg0) {
		if (arg0 != null && arg0.getSeverity() == ValidationEvent.WARNING){
			logger.warn("", arg0.getLinkedException());
			return true;
		}
		
		return false;
	}

	@Override
	public void error(SAXParseException exception) throws SAXException {
		logger.error("",exception);
		throw exception;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		logger.error("",exception);
		throw exception;
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		logger.warn("",exception);
		throw exception;
	}
}
