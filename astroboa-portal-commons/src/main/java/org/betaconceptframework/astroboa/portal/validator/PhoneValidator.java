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
package org.betaconceptframework.astroboa.portal.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.ui.jsf.utility.JSFUtilities;
import org.betaconceptframework.utility.CommonRegularExpressions;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.faces.Validator;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */

@Name("phoneValidator")
@BypassInterceptors
@Validator(id="phoneValidator")
public class PhoneValidator implements javax.faces.validator.Validator {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		logger.info("PhoneValidator.validate called");
		try {
			if (!validPhone((String)value)) {
				String validatorMessage = JSFUtilities.getStringI18n("validator.phone");
				FacesMessage message = new FacesMessage();
				message.setDetail(validatorMessage);
				message.setSummary(validatorMessage);
				message.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(message);
			}
		} catch (ConverterException e) {
			e.printStackTrace();
			FacesMessage message = new FacesMessage();
			message.setDetail("Cannot convert value to string");
			message.setSummary("Cannot convert value to string");
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ConverterException(message);
		}
	}
	
	private boolean validPhone(String value) {
		return StringUtils.isBlank(value) || 
			CommonRegularExpressions.COMPILED_PHONE_PATTERN.matcher(value).matches();
	}
}

