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
package org.betaconceptframework.astroboa.engine.jcr.util;


import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.model.impl.BinaryChannelImpl;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.item.JcrBuiltInItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class BinaryChannelUtils {

	private final Logger logger = LoggerFactory.getLogger(BinaryChannelUtils.class);
	
	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	private final static Pattern Astroboa_Resource_Api_Pattern = Pattern.compile("http://.*?/resource-api/.*");
	
	public  void populateBinaryChannelToNode(BinaryChannel binaryChannel, Node binaryParentNode, Session session, SaveMode saveMode, Context context, boolean binaryContentIsNew) throws Exception{
		if (binaryChannel == null){
			throw new CmsException("No BinaryChannel to populate");
		}

		if (binaryParentNode == null){
			throw new CmsException("No parent node of binary channel is provided");
		}

		final String name = binaryChannel.getName();
		if (name == null){
			throw new CmsException("BinaryChannel name not found");
		}

		try {
			Node binaryChannelNode;
			if (binaryChannel.getId() != null){
				binaryChannelNode = context.retrieveNodeForBinaryChannel(binaryChannel.getId()); 

				if (binaryChannelNode == null){
					//User has specified an id for binary channel
					binaryChannelNode = createNewBinaryChannelNode(binaryChannel, binaryParentNode, name, true);
				}

				//Check that binaryChannelNode has the proper parent
				if (!binaryChannelNode.getParent().getUUID().equals(binaryParentNode.getUUID())){
					throw new CmsException("Binary channel node "+ binaryChannelNode.getPath() + " does not belong to parent node "+ binaryParentNode.getPath());
				}
			}
			else{
				binaryChannelNode = createNewBinaryChannelNode(binaryChannel,binaryParentNode, name, false);
			}

			//Get content from external location if necessary
			loadContentFromExternalLocation(context, binaryChannel);
				

			populateNodeWithBinary(saveMode, binaryChannel, binaryChannelNode, session, binaryContentIsNew);

		} catch (RepositoryException e) {
			throw new CmsException(e);
		} 
	}

	public void loadContentFromExternalLocation(Context context, BinaryChannel binaryChannel) {
		
		String externalLocationOfTheContent = ((BinaryChannelImpl)binaryChannel).getExternalLocationOfTheContent();

		if (! binaryChannel.isNewContentLoaded() && StringUtils.isNotBlank(externalLocationOfTheContent)){

			//Check context first
			byte[] content = context.getBinaryContentForKey(externalLocationOfTheContent);
			
			//try to download
			if (content == null){
				InputStream inputStream = null;
				try {
					URL urlResource = new URL(externalLocationOfTheContent);
					
					if (Astroboa_Resource_Api_Pattern.matcher(externalLocationOfTheContent).matches() && 
							context.getImportConfiguration() != null && 
							context.getImportConfiguration().credentialsOfUserWhoHasAccessToBinaryContent() != null && 
							context.getImportConfiguration().credentialsOfUserWhoHasAccessToBinaryContent().getUsername() != null && 
							context.getImportConfiguration().credentialsOfUserWhoHasAccessToBinaryContent().getPassword() != null){

						String username = context.getImportConfiguration().credentialsOfUserWhoHasAccessToBinaryContent().getUsername();
						String password = context.getImportConfiguration().credentialsOfUserWhoHasAccessToBinaryContent().getPassword();
						
						URLConnection uc = urlResource.openConnection();
						uc.setRequestProperty ("Authorization", "Basic " + new String(Base64.encodeBase64(((username + ":" + password).getBytes()))));
						inputStream = (InputStream)uc.getInputStream();

					}
					else{
						inputStream = urlResource.openStream();
					}
					
					binaryChannel.setContent(IOUtils.toByteArray(inputStream));
	
				} catch (Throwable e) {
					//Log exception but continue with unmarshaling
					//BinaryChannle will be created without content
					logger.warn("Invalid external location "+externalLocationOfTheContent+ " of the content for binary channel "+ binaryChannel.getName(), e);
				}
				finally{
					if (inputStream != null){
						IOUtils.closeQuietly(inputStream);
					}
				}
			}
			else{
				binaryChannel.setContent(content);
			}
		}
	}

	private Node createNewBinaryChannelNode(BinaryChannel binaryChannel,
			Node binaryParentNode, final String name, boolean useProvidedId)
			throws RepositoryException {
		
		Node binaryChannelNode = JcrNodeUtils.addBinaryChannelNode(binaryParentNode, name);

		cmsRepositoryEntityUtils.createCmsIdentifier(binaryChannelNode, binaryChannel, useProvidedId);
		
		return binaryChannelNode;
	}

	private  void populateNodeWithBinary(SaveMode saveMode,BinaryChannel binaryChannel, Node binaryChannelNode, Session session, boolean binaryContentIsNew) throws Exception {

		ValueFactory valueFactory = session.getValueFactory();

		JcrNodeUtils.addSimpleProperty(saveMode, binaryChannelNode, CmsBuiltInItem.SourceFileName, binaryChannel.getSourceFilename(), valueFactory, ValueType.String);

		JcrNodeUtils.addSimpleProperty(saveMode, binaryChannelNode, 
				CmsBuiltInItem.Name, binaryChannel.getName(),
				valueFactory, ValueType.String);

		JcrNodeUtils.addSimpleProperty(saveMode, binaryChannelNode, CmsBuiltInItem.Size, binaryChannel.getSize(), valueFactory,ValueType.Long);

		populateBinaryData(saveMode, binaryChannel, binaryChannelNode, session,binaryContentIsNew);

	}

	private void populateBinaryData(SaveMode saveMode, BinaryChannel binaryChannel, Node binaryChannelNode,Session session, boolean binaryContentIsNew) throws Exception  {
		ValueFactory valueFactory = session.getValueFactory();

		/*Node binaryDataNode;
		if (binaryChannelNode.hasNode(CmsBuiltInItem.BinaryData.getJcrName()))
			binaryDataNode = binaryChannelNode.getNode(CmsBuiltInItem.BinaryData.getJcrName());
		else
			binaryDataNode = binaryChannelNode.addNode(CmsBuiltInItem.BinaryData.getJcrName(), JcrBuiltInItem.NtResource.getJcrName());
		 */
		
		JcrNodeUtils.addSimpleProperty(saveMode, binaryChannelNode, JcrBuiltInItem.JcrEncoding, binaryChannel.getEncoding(), valueFactory, ValueType.String);

		JcrNodeUtils.addSimpleProperty(saveMode, binaryChannelNode, JcrBuiltInItem.JcrMimeType, binaryChannel.getMimeType(), valueFactory, ValueType.String);

		//Update binary data only if value is new
		if (binaryContentIsNew){
			JcrNodeUtils.addBinaryProperty(saveMode, binaryChannelNode, JcrBuiltInItem.JcrData, binaryChannel.getContent(), valueFactory);

			//Create new path for binary content file
			createPathForBinaryContent(binaryChannelNode, binaryChannel, session);
		}

		JcrNodeUtils.addSimpleProperty(saveMode, binaryChannelNode, JcrBuiltInItem.JcrLastModified, binaryChannel.getModified(), valueFactory,ValueType.Date);
	}

	public void deleteBinaryChannelNode(Node contentNode, String binaryChannelName) throws RepositoryException {
		if (contentNode != null && contentNode.hasNode(binaryChannelName))
		{
			NodeIterator binaryDataNodes = contentNode.getNodes(binaryChannelName);

			while (binaryDataNodes.hasNext()){
				Node nextNode = binaryDataNodes.nextNode();
				nextNode.remove();
			}
		}
	}

	public Map<String, Node> partitionBinaryChannelNodeWithTheSameNamePerId(Node contentObjectNode, String binaryChannelName) throws RepositoryException {
		Map<String, Node> binaryChannelNodes = new HashMap<String, Node>();
		if (contentObjectNode != null && contentObjectNode.hasNodes()){
			NodeIterator contentObjectChildNodes = contentObjectNode.getNodes(binaryChannelName);
			while (contentObjectChildNodes.hasNext()){
				Node child = contentObjectChildNodes.nextNode();
				if (child.isNodeType(CmsBuiltInItem.BinaryChannel.getJcrName()) && child.hasProperty(CmsBuiltInItem.CmsIdentifier.getJcrName()))
					binaryChannelNodes.put(cmsRepositoryEntityUtils.getCmsIdentifier(child), child);
			}
		}

		return binaryChannelNodes;
	}

	/**
	 * Path is created according to Jackrabbit 1.4 DataStore implementation
	 * If storing blob files is changed this method should be updated accordingly
	 * @param binaryDataNode
	 * @param binaryChannel
	 * @param session
	 * @throws Exception
	 * @throws PathNotFoundException
	 * @throws RepositoryException
	 */
	public void createPathForBinaryContent(Node binaryDataNode,
			BinaryChannel binaryChannel, Session session) throws Exception,
			PathNotFoundException, RepositoryException {

		try{
			
			JackrabbitDependentUtils.createPathForBinaryContent(binaryDataNode, binaryChannel, session);
			
		}
		catch (Exception e){
			logger.error("Problem during binary value file path calculation ", e);
		}

	}

	public void addRepositoryIdToBinaryChannel(String binaryChannelNameOrPath,
			BinaryChannel binaryChannel) throws RepositoryException {
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForActiveClient();
		if (repositoryContext==null|| repositoryContext.getCmsRepository()==null||
				StringUtils.isBlank(repositoryContext.getCmsRepository().getId())){
			logger.warn("Could not find connected repostiory id while creating path for binary channel "+ binaryChannelNameOrPath);
		}
		else{
			((BinaryChannelImpl)binaryChannel).setRepositoryId(repositoryContext.getCmsRepository().getId());
		}
	}
}
