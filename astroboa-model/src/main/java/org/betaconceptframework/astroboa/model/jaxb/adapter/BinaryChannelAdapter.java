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
package org.betaconceptframework.astroboa.model.jaxb.adapter;

import java.io.InputStream;
import java.net.URL;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.model.factory.CmsRepositoryEntityFactoryForActiveClient;
import org.betaconceptframework.astroboa.model.jaxb.type.BinaryChannelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class BinaryChannelAdapter extends XmlAdapter<BinaryChannelType,BinaryChannel>{

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private DatatypeFactory df ;
	
	private boolean marshallBinaryContent;
	
	public BinaryChannelAdapter() {
		this(false);
	}
	
	public BinaryChannelAdapter(boolean marshallBinaryContent) {
		
		this.marshallBinaryContent = marshallBinaryContent;
		
		try {
			df = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new CmsException(e);
		}
	}
	
	@Override
	public BinaryChannelType marshal(BinaryChannel binaryChannel) throws Exception {

		if (binaryChannel != null){
			BinaryChannelType binaryChannelType = new BinaryChannelType();
			
			binaryChannelType.setEncoding(binaryChannel.getEncoding());
			binaryChannelType.setId(binaryChannel.getId());
			
			if (binaryChannel.getModified() != null){
				GregorianCalendar gregCalendar = new GregorianCalendar(binaryChannel.getModified().getTimeZone());
				gregCalendar.setTimeInMillis(binaryChannel.getModified().getTimeInMillis());

				binaryChannelType.setLastModificationDate(df.newXMLGregorianCalendar(gregCalendar));
			}
			
			binaryChannelType.setMimeType(binaryChannel.getMimeType());
			binaryChannelType.setSourceFileName(binaryChannel.getSourceFilename());
			
			binaryChannelType.setUrl(binaryChannel.buildResourceApiURL(null, null, null, null, null, false, false));

			if (marshallBinaryContent){
				byte[] content = binaryChannel.getContent();
				
				if (content != null){
					//No need to encode since it is done automatically from JAXB
					//binaryChannelType.setContent(Base64.encodeBase64(content));
					binaryChannelType.setContent(content);
				}
			}

			return binaryChannelType;
			
		}
		
		return null;
	}

	@Override
	public BinaryChannel unmarshal(BinaryChannelType binaryChannelType) throws Exception {

		if (binaryChannelType != null){
			BinaryChannel binaryChannel = CmsRepositoryEntityFactoryForActiveClient.INSTANCE.getFactory().newBinaryChannel();
			
			binaryChannel.setEncoding(binaryChannelType.getEncoding());
			binaryChannel.setId(binaryChannelType.getId());
			
			if (binaryChannelType.getLastModificationDate() != null){
				binaryChannel.setModified(binaryChannelType.getLastModificationDate().toGregorianCalendar());
			}
			
			binaryChannel.setMimeType(binaryChannelType.getMimeType());
			binaryChannel.setSourceFilename(binaryChannelType.getSourceFileName());
			
			final byte[] content = binaryChannelType.getContent();
			
			if (content != null){
				binaryChannel.setContent(Base64.decodeBase64(content));
			}

			if (! binaryChannel.isNewContentLoaded()){
				//Try URL
				if (binaryChannelType.getUrl() != null){
					InputStream inputStream = null;
					try {
						URL urlResource = new URL(binaryChannelType.getUrl());
						
						inputStream = urlResource.openStream();
						
						binaryChannel.setContent(IOUtils.toByteArray(inputStream));

					} catch (Throwable e) {
						//Log exception but continue with unmarshalling
						//BinaryChannle will be created without content
						logger.warn("Invalid URL {} for binary channel {}", binaryChannelType.getUrl(),binaryChannel.getName() );
					}
					finally{
						if (inputStream != null){
							IOUtils.closeQuietly(inputStream);
						}
					}
				}
			}
			
			return binaryChannel;
			
		}
		return null;
	}

}
