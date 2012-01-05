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
package org.betaconceptframework.astroboa.engine.jcr.renderer;


import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.engine.jcr.util.BinaryChannelUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrValueUtils;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class BinaryChannelRenderer extends AbstractRenderer  {

	@Autowired
	private CmsRepositoryEntityRenderer cmsRepositoryEntityRenderer;
	
	@Autowired
	private BinaryChannelUtils binaryChannelUtils;

	private final static String BINARY_NO_NAME = "BinaryNoName";
	
	public BinaryChannel renderUnmanagedBinaryChannel(String name, String relativePathToBinaryContent) throws RepositoryException {
		
		
		BinaryChannel binaryChannel  = cmsRepositoryEntityFactoryForActiveClient.newUnmanagedBinaryChannel(relativePathToBinaryContent);
		
		if (StringUtils.isBlank(name)){
			
			binaryChannel.setName(BINARY_NO_NAME);	
		}
		else{
			binaryChannel.setName(name);
		}
		
		

		return binaryChannel;
	}

	
	/**
	 * Create a content channel
	 * Node containing data may also contain properties for mime type AND/OR file name
	 * Real Content is loaded through proxy only when it is actually needed or if preload data is set to true
	 * @param binaryChannelNode
	 * @param preloadData 
	 * @param binaryDataPropertyName Name of property which contains binary data
	 * @return
	 * @throws RepositoryException 
	 * @throws 
	 * @throws ValueFormatException 
	 * @throws  
	 * @throws 
	 */
	public  BinaryChannel render(Node binaryChannelNode, boolean loadData) throws   RepositoryException  {

		BinaryChannel binaryChannel  = cmsRepositoryEntityFactoryForActiveClient.newBinaryChannel();
		
			cmsRepositoryEntityRenderer.renderCmsRepositoryEntityBasicAttributes(binaryChannelNode, binaryChannel);
			
			if (binaryChannelNode.hasProperty(CmsBuiltInItem.Name.getJcrName()))
				binaryChannel.setName(binaryChannelNode.getProperty(CmsBuiltInItem.Name.getJcrName()).getString());
			else
				binaryChannel.setName(BINARY_NO_NAME);
			
			if (binaryChannelNode.hasProperty(CmsBuiltInItem.Size.getJcrName()))
				binaryChannel.setSize(binaryChannelNode.getProperty(CmsBuiltInItem.Size.getJcrName()).getLong());

			if (binaryChannelNode.hasProperty(CmsBuiltInItem.SourceFileName.getJcrName()))
				binaryChannel.setSourceFilename(binaryChannelNode.getProperty(CmsBuiltInItem.SourceFileName.getJcrName()).getString());

			if (binaryChannelNode.hasProperty(JcrBuiltInItem.JcrMimeType.getJcrName()))
				binaryChannel.setMimeType(binaryChannelNode.getProperty(JcrBuiltInItem.JcrMimeType.getJcrName()).getString());


			if (binaryChannelNode.hasProperty(JcrBuiltInItem.JcrEncoding.getJcrName()))
				binaryChannel.setEncoding(binaryChannelNode.getProperty(JcrBuiltInItem.JcrEncoding.getJcrName()).getString());

			binaryChannel.setModified(DateUtils.addLocaleToCalendar(binaryChannelNode.getProperty(JcrBuiltInItem.JcrLastModified.getJcrName()).getDate(), CmsConstants.LOCALE_GREEK));

			if (loadData && binaryChannelNode.hasProperty(JcrBuiltInItem.JcrData.getJcrName()))
				binaryChannel.setContent((byte[])JcrValueUtils.getObjectValue(binaryChannelNode.getProperty(JcrBuiltInItem.JcrData.getJcrName()).getValue()));

			//Also set repository id to be available if file AccessInfo may be created
			binaryChannelUtils.addRepositoryIdToBinaryChannel(binaryChannelNode.getPath(), binaryChannel);

			return binaryChannel;
	}
	
	

}
