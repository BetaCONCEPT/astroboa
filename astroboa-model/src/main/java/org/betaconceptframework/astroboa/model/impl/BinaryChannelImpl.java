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
package org.betaconceptframework.astroboa.model.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Calendar;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.context.RepositoryContext;
import org.betaconceptframework.astroboa.model.jaxb.adapter.BinaryChannelAdapter;
import org.betaconceptframework.astroboa.util.CmsConstants;

/**
 * Repository's resource content 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
@XmlJavaTypeAdapter(value=BinaryChannelAdapter.class)
public class BinaryChannelImpl extends CmsRepositoryEntityImpl implements BinaryChannel, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6485262251238864739L;

	private String name;

	private long size;

	private String encoding;

	private Calendar modified;

	/**
	 * Name of the content channel
	 */
	private String sourceFilename;

	/**
	 * MIME type  (audio, image, text, video, etc) of resource's content  
	 */
	private String mimeType;

	/**
	 * Actual resource's content
	 */
	private byte[] newContent;

	private String relativeFileSystemPath;

	private String absoluteBinaryChannelContentPath;

	private String repositoryId;

	private String contentObjectId;
	private String contentObjectSystemName;
	
	private String binaryPropertyPermanentPath;
	
	private boolean unmanaged = false;

	private boolean binaryPropertyIsMultiValued;

	/**
	 * @return Returns the content.
	 */
	public byte[] getContent() {

		if (newContent != null){
			/*
			 * this.newContent = content
			 * 
			 * produces the following bug report from FindBugs 
			 * Returning a reference to a mutable object value stored in one of the object's fields exposes the internal representation of the object.
			 * If instances are accessed by untrusted code, and unchecked changes to the mutable object would compromise security or other important 
			 * properties, you will need to do something different. Returning a new copy of the object is better approach in many situations.
			 * 
			 * The following approach would be more preferable
			 * 
			 *  byte[] clone = new byte[newContent.length];
				System.arraycopy(newContent, 0, clone, 0, newContent.length);
				return clone;
				
				However, we need to examine the performance issue rising from copying the byte array
				each time user uses method getContent(). Therefore for the moment we return the reference
				to the provided content.
			*/
			return newContent;
		}

		//Return content from file system
		InputStream inputStream = null;
		try {

			inputStream = getContentAsStream();

			if (inputStream != null){

				newContent = IOUtils.toByteArray(inputStream);
			}
			else
			{
				newContent = new byte[0];
			}
			
			return newContent;

		} catch (Exception e) {
			throw new CmsException(e);
		}
		finally
		{
			if (inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					throw new CmsException(e);
				}
		}
	}

	/**
	 * Content is provided only if path for the given binary channel is set
	 * @param content The content to set.
	 */
	public void setContent(byte[] content) {

		/*
		 * this.newContent = content
		 * 
		 * produces the following bug report from FindBugs 
		 * This code stores a reference to an externally mutable object into the internal representation of the object.
		 * If instances are accessed by untrusted code, and unchecked changes to the mutable object would compromise security 
		 * or other important properties,
		 * you will need to do something different. Storing a copy of the object is better approach in many situations
		 * 
		 * The following approach would be more preferable
			if (content != null){
				System.arraycopy(content, 0, newContent, 0, content.length);
			}
			else{
				newContent = null;
			}
			
			However, we need to examine the performance issue rising from copying the byte array
			each time user uses method setContent(). Therefore for the moment we create a reference
			to the provided content.
		*/

		newContent = content;

		if (newContent != null)
		{
			setSize(newContent.length);
		}
		else
		{
			setSize(0);
		}
		//Reset paths since a new content has been defined
		absoluteBinaryChannelContentPath = null;
		relativeFileSystemPath = null;
	}


	/**
	 * 	 * @return Returns the mimeType.
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @param mimeType The mimeType to set.
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * @return Returns the sourceFilename.
	 */
	public String getSourceFilename() {
		return sourceFilename;
	}

	/**
	 * @param sourceFilename The sourceFilename to set.
	 */
	public void setSourceFilename(String name) {
		this.sourceFilename = name;
	}
	
	/**
	 * @return Returns the sourceFilename suffix.
	 */
	public String getSourceFilenameSuffix() {
		return StringUtils.substringAfterLast(sourceFilename, ".");
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getCalculatedSize() {
		Long roundedSize = Math.round(getSize() / (double)1024);
		return roundedSize.toString() + "Kb";
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public Calendar getModified() {
		return modified;
	}

	public void setModified(Calendar modified) {
		this.modified = modified;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNewContentLoaded() {
		return newContent != null;
	}

	public String getRelativeFileSystemPath() {
		return relativeFileSystemPath;
	}

	public void setRelativeFileSystemPath(String fileSystemPath) {
		this.relativeFileSystemPath = fileSystemPath;

	}

	public InputStream getContentAsStream() {
		try {

			//return new content if any
			if (newContent != null)
				return new ByteArrayInputStream(newContent);

			//Look for file content only if id exists
			//or binary channel is unmanaged
			if (StringUtils.isNotBlank(getId()) || unmanaged ){
				//Return content from file system
				try{
					if (StringUtils.isNotBlank(absoluteBinaryChannelContentPath)){
						
						//Used for testing 
						if (absoluteBinaryChannelContentPath.contains("datastore/fs/Re/so/fsResource:")){
							//Assuming default workspace is used
							return new FileInputStream(absoluteBinaryChannelContentPath.replace("datastore/fs/Re/so/fsResource:", "workspaces/default/blobs"));
						}
						
						return new FileInputStream(absoluteBinaryChannelContentPath);
					}

					//Throw exception to try to load content from binary's url
					throw new Exception();
				}
				catch(Exception e){

					//File which contains content of binary channel does not exist
					//Try its url

					String binaryChannelContentURL = getDirectURL();

					URL contentUrl = new URL(binaryChannelContentURL);

					return contentUrl.openStream();

				}
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void setAbsoluteBinaryChannelContentPath(
			String absoluteBinaryChannelContentPath) {
		this.absoluteBinaryChannelContentPath = absoluteBinaryChannelContentPath;

	}

	private String buildDirectURL(Integer width, Integer height, ContentDispositionType contentDispositionType) {

		StringBuilder directURLBuilder = new StringBuilder();
		directURLBuilder.append((StringUtils.isBlank(repositoryId) ? "no-repository":repositoryId));

		//If binary channel is unmanaged then relae path must be url encoded
		if (unmanaged){
			try{
				directURLBuilder.append(CmsConstants.FORWARD_SLASH)
				.append(URLEncoder.encode(relativeFileSystemPath, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				directURLBuilder.append(CmsConstants.FORWARD_SLASH)
				.append(relativeFileSystemPath);
			}
		}
		else{
			directURLBuilder.append(CmsConstants.FORWARD_SLASH)
			.append(relativeFileSystemPath);
		}
		
		

		//Mime Type information is made available only in managed binary channels
		if (!unmanaged){
			directURLBuilder.append(CmsConstants.FORWARD_SLASH+mimeType);
		}

		if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/")){
			if (width != null && width > 0){
				directURLBuilder.append(CmsConstants.FORWARD_SLASH)
				.append("width")
				.append(CmsConstants.FORWARD_SLASH)
				.append(width);
			}

			if (height != null && height > 0){
				directURLBuilder.append(CmsConstants.FORWARD_SLASH)
				.append("height")
				.append(CmsConstants.FORWARD_SLASH)
				.append(height);
			}
		}

		if (contentDispositionType != null){
			directURLBuilder.append(CmsConstants.FORWARD_SLASH)
			.append(CmsConstants.CONTENT_DISPOSITION_TYPE)
			.append(CmsConstants.FORWARD_SLASH)
			.append(contentDispositionType.toString().toLowerCase());
		}


		//Source file name information is made available only in managed binary channels
		if (!unmanaged){
			try {

				directURLBuilder.append(CmsConstants.FORWARD_SLASH)
				.append(URLEncoder.encode(sourceFilename, "UTF-8"));

			} catch (UnsupportedEncodingException e) {

				directURLBuilder.append(CmsConstants.FORWARD_SLASH)
				.append(sourceFilename);
			}
		}

		return directURLBuilder.toString();
	}

	public void setRepositoryId(String repositoryId){
		this.repositoryId = repositoryId;
	}


	public String getServerURL() {
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForClient(authenticationToken);
		if (repositoryContext != null && repositoryContext.getCmsRepository() != null && 
				StringUtils.isNotBlank(repositoryContext.getCmsRepository().getServerURL())){
			String serverURL = repositoryContext.getCmsRepository().getServerURL().trim();
			
			return serverURL.endsWith("/")? serverURL.substring(0, serverURL.length()-1) : serverURL; 
		}

		return null;
	}
	
	public String getRestfulApiBasePath() {
		RepositoryContext repositoryContext = AstroboaClientContextHolder.getRepositoryContextForClient(authenticationToken);
		if (
			repositoryContext != null && 
			repositoryContext.getCmsRepository() != null && 
			StringUtils.isNotBlank(repositoryContext.getCmsRepository().getRestfulApiBasePath())) {
			String restfulApiBasePath = repositoryContext.getCmsRepository().getRestfulApiBasePath().trim();
			if (!restfulApiBasePath.startsWith("/")) {
				restfulApiBasePath = "/" + restfulApiBasePath;
			}
			 
			return restfulApiBasePath.endsWith("/")? restfulApiBasePath.substring(0, restfulApiBasePath.length()-1) : restfulApiBasePath;
		}

		return null;
	}

	public boolean contentExists() {
		if (newContent != null){
			return true;
		}

		if (StringUtils.isNotBlank(absoluteBinaryChannelContentPath)){
			return new File(absoluteBinaryChannelContentPath).exists();
		}

		return false;
	}

	public void setUnmanaged(boolean unmanaged){
		this.unmanaged = unmanaged;
	}


	@Override
	public String getRelativeDirectURL() {
		return getCustomizedRelativeDirectURL(null, null, null);
	}
	
	@Override
	public String getDirectURL() {
		return getCustomizedDirectURL(null, null, null);
	}

	@Override
	public String getCustomizedDirectURL(Integer width, Integer height,
			ContentDispositionType contentDispositionType) {
		if (getServerURL() != null){
			return getServerURL()+
			getCustomizedRelativeDirectURL(width, height, contentDispositionType);
		}

		return null;

	}
	
	@Override
	public String getCustomizedRelativeDirectURL(Integer width, Integer height,
			ContentDispositionType contentDispositionType) {
			return 
				getRestfulApiBasePath() +
				CmsConstants.CONTENT_API_FOR_BINARY_CHANNEL +
				CmsConstants.FORWARD_SLASH + 
				buildDirectURL(width, height, contentDispositionType);
	}

	@Override
	public String getContentApiURL() {
		return getCustomizedContentApiURL(null, null, null);
	}

	@Override
	public String getCustomizedContentApiURL(Integer width, Integer height,
			ContentDispositionType contentDispositionType) {
		
		return  buildResourceApiURL(width, height, null, null, contentDispositionType, true, false);
		
		/*if (getServerURL() != null){
			return getServerURL()+
				getCustomizedRelativeContentApiURL(width, height, contentDispositionType);
		}

		return null;*/
	}


	@Override
	public String getRelativeContentApiURL() {
		return getCustomizedRelativeContentApiURL(null, null, null);
	}

	@Override
	public String getCustomizedRelativeContentApiURL(Integer width,
			Integer height, ContentDispositionType contentDispositionType) {
		return  buildResourceApiURL(width, height, null, null, contentDispositionType, true, true);
		
		/*getRestfulApiBasePath() +
		CmsConstants.FORWARD_SLASH + 
		buildContetApiURL(width, height, contentDispositionType, false);*/
	}

	/*private String buildContetApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType, boolean checkForMultivalueProperty) {

		if (StringUtils.isBlank(binaryPropertyPath)){
			return "";
		}
		
		String contentObjectIdOrSystemNameToBeUsedInURL = StringUtils.isBlank(contentObjectSystemName) ? contentObjectId : contentObjectSystemName;
		
		if (StringUtils.isBlank(contentObjectIdOrSystemNameToBeUsedInURL)){
			return "";
		}
		
		// Astroboa RESTful API URL pattern for accessing the value of content object properties
		// http://server/resource-api/
		// <reposiotry-id>/contentObject/<contentObjectId>/<binaryChannelPropertyValuePath>
		// ?contentDispositionType=<contentDispositionType>&width=<width>&height=<height>
			
		StringBuilder contentApiURLBuilder = new StringBuilder();
		
		contentApiURLBuilder.append((StringUtils.isBlank(repositoryId) ? "no-repository":repositoryId))
			.append(CmsConstants.FORWARD_SLASH)
			.append("contentObject");

		contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH)
			.append(contentObjectIdOrSystemNameToBeUsedInURL);

		
		if (checkForMultivalueProperty && binaryPropertyIsMultiValued){
<<<<<<< .mine
			String binaryPropertyPathWithoutIndex = PropertyPath.removeLastIndexFromPath(binaryPropertyPath);
			
			contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH)
								.append(binaryPropertyPathWithoutIndex)
								.append(CmsConstants.FORWARD_SLASH)
								.append(getId());
=======
			contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH+binaryPropertyPermanentPath);
>>>>>>> .r5114
		}
		else{
			contentApiURLBuilder.append(CmsConstants.FORWARD_SLASH)
								.append(binaryPropertyPath);
		}
		
		boolean questionMarkHasBeenUsed = false;
		
		if (contentDispositionType !=null){
			contentApiURLBuilder
				.append(CmsConstants.QUESTION_MARK)
				.append("contentDispositionType")
				.append(CmsConstants.EQUALS_SIGN)
				.append(contentDispositionType.toString().toLowerCase());
			questionMarkHasBeenUsed = true;
		}
		
		if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/")){
			if (width != null && width > 0){
				contentApiURLBuilder
					.append(questionMarkHasBeenUsed ? CmsConstants.AMPERSAND : CmsConstants.QUESTION_MARK)
					.append("width")
					.append(CmsConstants.EQUALS_SIGN)
					.append(width);
				questionMarkHasBeenUsed = true;
			}
			
			if (height != null && height > 0){
				contentApiURLBuilder.append(questionMarkHasBeenUsed ? CmsConstants.AMPERSAND : CmsConstants.QUESTION_MARK)
					.append("height")
					.append(CmsConstants.EQUALS_SIGN)
					.append(height);
			}
		}

		return contentApiURLBuilder.toString();
	}
 */
	
	public String buildResourceApiURL(Integer width, Integer height, Double aspectRatio,  CropPolicy cropPolicy, ContentDispositionType contentDispositionType, boolean friendlyUrl, boolean relative) {

		if (StringUtils.isBlank(contentObjectId) || StringUtils.isBlank(binaryPropertyPermanentPath)){
			return "";
		}
		
		// Astroboa RESTful API URL pattern for accessing the value of content object properties
		// http://server/resource-api/
		// <reposiotry-id>/contentObject/<contentObjectId>/<binaryChannelPropertyValuePath>
		// ?contentDispositionType=<contentDispositionType>&width=<width>&height=<height>
			
		StringBuilder resourceApiURLBuilder = new StringBuilder();
		
		if (! relative){
			String serverURL = getServerURL();
			
			if (serverURL != null){
				resourceApiURLBuilder.append(serverURL);
			}
		}
		
		resourceApiURLBuilder.append(getRestfulApiBasePath()).append(CmsConstants.FORWARD_SLASH); 

		resourceApiURLBuilder.append((StringUtils.isBlank(repositoryId) ? "no-repository":repositoryId));
		resourceApiURLBuilder.append(CmsConstants.FORWARD_SLASH).append("contentObject");
		
		if (friendlyUrl) {
			resourceApiURLBuilder.append(CmsConstants.FORWARD_SLASH).append(contentObjectSystemName);
		}
		else {
			resourceApiURLBuilder.append(CmsConstants.FORWARD_SLASH).append(contentObjectId);
		}
		
		resourceApiURLBuilder.append(CmsConstants.FORWARD_SLASH).append(binaryPropertyPermanentPath);
		
		if (binaryPropertyIsMultiValued){
			resourceApiURLBuilder.append(CmsConstants.LEFT_BRACKET)
			.append(getId())
			.append(CmsConstants.RIGHT_BRACKET);
		}
		
		StringBuilder urlParametersBuilder = new StringBuilder();
		
		if (contentDispositionType !=null){
			urlParametersBuilder
				.append(CmsConstants.AMPERSAND)
				.append("contentDispositionType")
				.append(CmsConstants.EQUALS_SIGN)
				.append(contentDispositionType.toString().toLowerCase());
		}
		
		if (isJPGorPNGorGIFImage()){
			if (width != null && width > 0){
				urlParametersBuilder
					.append(CmsConstants.AMPERSAND)
					.append("width")
					.append(CmsConstants.EQUALS_SIGN)
					.append(width);
			}
			
			if (height != null && height > 0){
				urlParametersBuilder.append(CmsConstants.AMPERSAND)
					.append("height")
					.append(CmsConstants.EQUALS_SIGN)
					.append(height);
			}
			
			// we accept to set a new aspect ratio only if  
			if (aspectRatio != null && (width == null || height == null)) {
				urlParametersBuilder.append(CmsConstants.AMPERSAND)
					.append("aspectRatio")
					.append(CmsConstants.EQUALS_SIGN)
					.append(aspectRatio);
				
				if (cropPolicy != null) {
					urlParametersBuilder.append(CmsConstants.AMPERSAND)
					.append("cropPolicy")
					.append(CmsConstants.EQUALS_SIGN)
					.append(cropPolicy.toString());
				}
			}

		}
		
		if (urlParametersBuilder.length() > 0) {
			urlParametersBuilder.replace(0, 1, CmsConstants.QUESTION_MARK);
		}
		return resourceApiURLBuilder.append(urlParametersBuilder).toString();
	}
	
	// we need this in order to determine if width, height, aspectRatio and cropPolicy url parameters should be appended in the URL 
	private boolean isJPGorPNGorGIFImage(){
		return StringUtils.isNotBlank(mimeType) && 
		(mimeType.equals("image/jpeg") || 
				mimeType.equals("image/png") || 
				mimeType.equals("image/x-png") ||
				mimeType.equals("image/gif"));
	}
	
	public void setContentObjectId(String contentObjectId){
		this.contentObjectId = contentObjectId;
	}
	
	public void setContentObjectSystemName(String systemName){
		this.contentObjectSystemName = systemName;
	}
	
	/**
	 * 
	 */
	public void clean() {
		getContent();
		absoluteBinaryChannelContentPath = null;
		contentObjectId = null;
		contentObjectSystemName = null;
		relativeFileSystemPath = null;
		
		setId(null);
		
	}
	
	@Override
	public String getResourceApiURL(ResourceRepresentationType<?>  resourceRepresentationType, boolean relative, boolean friendlyUrl) {
		
		return buildResourceApiURL(null, null, null, null, null, friendlyUrl, relative);
		
	}

	public void binaryPropertyIsMultiValued() {
		binaryPropertyIsMultiValued = true;
		
	}

	public void setBinaryPropertyPermanentPath(String binaryPropertyPermanentPath) {
		this.binaryPropertyPermanentPath = binaryPropertyPermanentPath;
	}
	
	
}
