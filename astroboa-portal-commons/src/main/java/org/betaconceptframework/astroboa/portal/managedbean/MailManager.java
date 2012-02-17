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
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.Date;

import javax.activation.FileTypeMap;
import javax.mail.MessagingException;

import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class MailManager {
	
	private JavaMailSender portalMailSender;
	
	public void sendMail(String mailFrom, String mailTo, String mailSubject, String mailText) throws MessagingException {
		
		MimeMessageHelper mailMessage = new MimeMessageHelper(getPortalMailSender().createMimeMessage(), "UTF-8");
		FileTypeMap fileTypeMap = new ConfigurableMimeFileTypeMap();
		mailMessage.setFileTypeMap(fileTypeMap);
		
		mailMessage.setFrom(mailFrom);
		
		mailMessage.setSentDate(new Date(System.currentTimeMillis()));
		mailMessage.setSubject(mailSubject);
		mailMessage.setText(mailText);
				
		mailMessage.setTo(mailTo);
		getPortalMailSender().send(mailMessage.getMimeMessage());
	}

	public JavaMailSender getPortalMailSender() {
		return portalMailSender;
	}

	public void setPortalMailSender(JavaMailSender portalMailSender) {
		this.portalMailSender = portalMailSender;
	}

}
