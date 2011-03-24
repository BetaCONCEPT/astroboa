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
package org.betaconceptframework.astroboa.resourceapi.utility;

import java.io.File;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.BinaryChannel.ContentDispositionType;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible to process urls representing the
 * content api for direct access to a binary channel.
 * 
 * 
 * Content API for direct access to a binary channel has the following two forms, 
 * depending on whether binary channel is managed or unmanaged.
 * 
 * For Managed binary channels
 * 
 * repository-id/datastore/<two-chars>/<two-chars>/<two-chars>/<40-chars>/<mime-type>/width/<3-digits>/height/<3-digits>/contentDispositionType/attachment|inline/filename.extension
 * 
 * and for unmanaged binary channels
 * 
 * repository-id/<one-or-more-chars-for-path>/width/<3-digits>/height/<3-digits>/contentDispositionType/attachment|inline/filename.extension
 * 
 * This class is able to extract necessary information for a given url according to one of these patterns
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class BinaryChannelFileAccessInfoProcessor {

	private Logger logger = LoggerFactory.getLogger(getClass());

	private String fileAccessInfoURL;

	private String repositoryId;

	private String fileName;

	private String mimeType;

	private String width;

	private String height;

	private String contentDispositionType;

	private String relativePathToStream;
	
	public BinaryChannelFileAccessInfoProcessor(String fileAccessInfoURL){
		this.fileAccessInfoURL = fileAccessInfoURL ;
		

	}
	
	
	public boolean processFileAccessInfo(){

		if (StringUtils.isNotBlank(fileAccessInfoURL)){
			
			//Apply both patterns as there is no (clean) way to know which one to use
			Matcher uriMatcher = ContentApiUtils.MANAGED_FILE_ACCESS_INFO_PATTERN.matcher(fileAccessInfoURL);
			
			if (uriMatcher.matches()){
			
				logGroups(uriMatcher);
				
				repositoryId = uriMatcher.group(1);

				fileName = uriMatcher.group(10);

				mimeType = uriMatcher.group(6);

				width = uriMatcher.group(7);

				height = uriMatcher.group(8);

				contentDispositionType = uriMatcher.group(9);

				if (StringUtils.isBlank(contentDispositionType)){
					contentDispositionType = ContentDispositionType.INLINE.toString().toLowerCase();
				}

				relativePathToStream = uriMatcher.group(2) + File.separator+
					uriMatcher.group(3) +File.separator+
					uriMatcher.group(4) +File.separator+
					uriMatcher.group(5);
				
				return true;
			}
			else{
				//Check second pattern
				uriMatcher = ContentApiUtils.UNMANAGED_FILE_ACCESS_INFO_PATTERN.matcher(fileAccessInfoURL);
				
				if (uriMatcher.matches()){
					
					logGroups(uriMatcher);
					
					repositoryId = uriMatcher.group(1);

					width = uriMatcher.group(3);

					height = uriMatcher.group(4);

					contentDispositionType = uriMatcher.group(5);

					if (StringUtils.isBlank(contentDispositionType)){
						contentDispositionType = ContentDispositionType.INLINE.toString().toLowerCase();
					}

					//Since it is unmanaged resource prefix relative path to UnmanagedDataStore directory
					relativePathToStream = CmsConstants.UNMANAGED_DATASTORE_DIR_NAME + File.separator+ uriMatcher.group(2);
					
					return true;
				}
			}
			
		}
		
		return false;
		
	}


	private void logGroups(Matcher uriMatcher) {

		if (logger.isDebugEnabled()){
			for (int j=1;j<=uriMatcher.groupCount(); j++){
				logger.debug("Group {} {}",j,uriMatcher.group(j));
			}
		}

		
	}


	/**
	 * @return the repositoryId
	 */
	public String getRepositoryId() {
		return repositoryId;
	}


	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}


	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}


	/**
	 * @return the width
	 */
	public String getWidth() {
		return width;
	}


	/**
	 * @return the height
	 */
	public String getHeight() {
		return height;
	}


	/**
	 * @return the contentDispositionType
	 */
	public String getContentDispositionType() {
		return contentDispositionType;
	}


	/**
	 * @return the relativePathToStream
	 */
	public String getRelativePathToStream() {
		return relativePathToStream;
	}
	
	
	
	

}
