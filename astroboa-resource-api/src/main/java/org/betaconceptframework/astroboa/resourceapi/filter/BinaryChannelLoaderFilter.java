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
package org.betaconceptframework.astroboa.resourceapi.filter;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepository;
import org.betaconceptframework.astroboa.api.service.RepositoryService;
import org.betaconceptframework.astroboa.resourceapi.utility.BinaryChannelFileAccessInfoProcessor;
import org.betaconceptframework.astroboa.resourceapi.utility.ContentApiUtils;
import org.betaconceptframework.utility.ImageUtils;
import org.betaconceptframework.utility.ImageUtils.ImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Filter responsible to serve binary channel contents from jcr repository.
 * 
 * It serves request url of name pattern 
 * 
 * <context-root>/f/binaryChannel/enc/<encryptedFileAccessInfo> or
 * <context-root>/f/binaryChannel/<repository-id>/<binary-channel-relative-system-path>/<mime-type>/<source-file-name>
 * 
 * In order to provide stream for binary channel it replaces <context-root>/binaryChannel/ with
 * absolute path of repository home directory. All available repository home directories are provided by {@link RepositoryService repositoryService}
 * during filter initialization.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class BinaryChannelLoaderFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(BinaryChannelLoaderFilter.class);

	private static final String BINARY_CHANNEL_FILTER_PREFIX = "/f/binaryChannel/";

	private static final String ENCRYTPION_PREFIX = "enc";

	private Map<String, String> repositoryHomeDirectoriesPerRepositoryId = new HashMap<String, String>();


	private RepositoryService repositoryService;

	public void setRepositoryService(RepositoryService repositoryService) {
		this.repositoryService = repositoryService;
	}

	public void destroy() {

	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		HttpServletResponse httpServletResponse = (HttpServletResponse) response;

		try{
			String fileAccessInfo = extractFileAccessInfo(httpServletRequest);

			if (StringUtils.isBlank(fileAccessInfo)){
				logger.warn("Invalid http request {} sent to binary channel filter", httpServletRequest.getRequestURI());
				httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else{
				logger.debug("Processing request {} in binary channel filter", fileAccessInfo);

				//Determine if file access info is encrypted or not
				if (fileAccessInfo.startsWith(ENCRYTPION_PREFIX)){

					fileAccessInfo = decryptRequest(StringUtils.substringAfter(fileAccessInfo, ENCRYTPION_PREFIX+"/"));
				}

				loadAndReturnContentOfBinaryChannel(fileAccessInfo, httpServletResponse);
			}

		}
		catch (Exception e){
			logger.error("", e);
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}

	private void loadAndReturnContentOfBinaryChannel(String fileAccessInfo, HttpServletResponse httpServletResponse) throws IOException {

		BinaryChannelFileAccessInfoProcessor fileAccessInfoProcessor = new BinaryChannelFileAccessInfoProcessor(fileAccessInfo);

		if (!fileAccessInfoProcessor.processFileAccessInfo()) {
			logger.warn("Invalid file access info "+ fileAccessInfo);
			httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
		else{


			String repositoryId = fileAccessInfoProcessor.getRepositoryId();

			String fileName = fileAccessInfoProcessor.getFileName();

			String mimeType = fileAccessInfoProcessor.getMimeType();

			String width = fileAccessInfoProcessor.getWidth();

			String height = fileAccessInfoProcessor.getHeight();

			String contentDispositionType = fileAccessInfoProcessor.getContentDispositionType();

			String relativePathToStream = fileAccessInfoProcessor.getRelativePathToStream();

			//Check that repository home directory exists
			if (MapUtils.isEmpty(repositoryHomeDirectoriesPerRepositoryId) || StringUtils.isBlank(repositoryId) || 
					! repositoryHomeDirectoriesPerRepositoryId.containsKey(repositoryId)){
				logger.error("No available home directory exists for repository "+ repositoryId);

				httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			else{
				logger.debug("Ready to serve binary channel : path {}, mime-type: {}, filename :{}",
						new Object[]{relativePathToStream, mimeType, fileName});

				File resource = null;
				try {
					//Load file
					resource = new File(repositoryHomeDirectoriesPerRepositoryId.get(repositoryId)+File.separator+relativePathToStream);

					if (!resource.exists()){
						logger.warn("Could not locate resource "+ repositoryHomeDirectoriesPerRepositoryId.get(repositoryId)+File.separator+relativePathToStream);
						httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
					else{

						//It may well be the case where filename and mime type are not provided
						//in file access info (especially if binary channel is unmanaged).
						//In this cases obtain filename and mime type from resource
						if (StringUtils.isBlank(fileName)){
							fileName = resource.getName();
						}
						
						if (StringUtils.isBlank(mimeType)){
							mimeType = new MimetypesFileTypeMap().getContentType(resource);
						}
						
						ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
						
						httpServletResponse.setDateHeader("Last-Modified", resource.lastModified());
						httpServletResponse.setContentType(mimeType);

						String processedFilename = org.betaconceptframework.utility.FilenameUtils.convertFilenameGreekCharactersToEnglishAndReplaceInvalidCharacters(fileName);

						//
						byte[] resourceByteArray = FileUtils.readFileToByteArray(resource);

						
						if (StringUtils.isNotBlank(mimeType) && mimeType.startsWith("image/")){

							resourceByteArray = resizeImageResource(resourceByteArray, mimeType, width, height);
							
							httpServletResponse.setHeader("Content-Disposition", contentDispositionType+";filename="
									+(width != null ? "W"+width : "")
									+(height != null ?"H"+height: "")
									+(width != null || height != null ? "-":"")
									+processedFilename);
							
						}
						else{
							//Resource is not an image. Set charset encoding 
							httpServletResponse.setCharacterEncoding("UTF-8");
						}

						if (! httpServletResponse.containsHeader("Content-Disposition")){
							httpServletResponse.setHeader("Content-Disposition", contentDispositionType+";filename="+processedFilename);
						}

						httpServletResponse.setHeader("ETag", ContentApiUtils.createETag(resource.lastModified(), resourceByteArray.length));
						httpServletResponse.setContentLength(resourceByteArray.length);
						
						try{
							IOUtils.write(resourceByteArray, servletOutputStream);
						
							servletOutputStream.flush();
						}
						catch(Exception e)
						{
							//Something went wrong while writing data to stream.
							//Just log to debug and not to warn
							logger.debug("", e);
							httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
						}
					}
				}
				catch (Exception e) {
					logger.error("", e);
					httpServletResponse.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}
	}

	private byte[] resizeImageResource(byte[] resourceByteArray, String mimeType, String width, String height) 
		throws IOException, InterruptedException {
		
		int imageWidth = 0;
		int imageHeight = 0;
		
		if (StringUtils.isNotBlank(width)){
			imageWidth = Integer.valueOf(width);
		}

		if (StringUtils.isNotBlank(height)){
			imageHeight = Integer.valueOf(height);
		}

		if (imageWidth != 0 && imageHeight != 0) {
			return ImageUtils.resize(resourceByteArray, ImageType.getImageTypeByMimeType(mimeType), imageWidth, imageHeight);
		}
		
		if (imageWidth != 0 && imageHeight == 0) {
			return ImageUtils.scaleToWidth(resourceByteArray, ImageType.getImageTypeByMimeType(mimeType), imageWidth);
		}
		
		if (imageWidth == 0 && imageHeight != 0) {
			return ImageUtils.scaleToHeight(resourceByteArray, ImageType.getImageTypeByMimeType(mimeType), imageHeight);
		}

		return resourceByteArray;
	}


	private String decryptRequest(String encryptedFileAccessInfo) {
		return encryptedFileAccessInfo;
	}

	private String extractFileAccessInfo(HttpServletRequest httpServletRequest) {
		//remove context-path and binaryChannel filter path from request
		return StringUtils.replace(httpServletRequest.getRequestURI(), httpServletRequest.getContextPath()+BINARY_CHANNEL_FILTER_PREFIX, "");
	}


	/*private void calculateAbsoluteFileSystemPathAndFilenameForBinaryData(String contextPath, 
			String requestURI, 
			String absoluteFileSystemPathToBinaryData, 
			String binaryDataFilenameAndSuffix) {

		String requiredURIRegularExpression = "^" +
			contextPath +
			BINARY_CHANNEL_PREFIX + 
			"([0-9abcdef]{2})" + // group 1
			"/" +
			"([0-9abcdef]{2})" + // group 2
			"/" +
			"([0-9abcdef]{2})" + // group 3
			"/" +
			"([0-9abcdef]{40})" + // group 4
			"/" +
			"([A-Za-z0-9_\\-]+)" + // group 5
			"\\." +
			"([A-Za-z0-9_\\-]{3})"; // group 6

		Pattern requiredURIPattern = Pattern.compile(requiredURIRegularExpression);
		Matcher uriMatcher = requiredURIPattern.matcher(requestURI);

		if (uriMatcher.matches()) {
			absoluteFileSystemPathToBinaryData = repositoryHomeDir + File.separator +
			uriMatcher.group(1) +
			uriMatcher.group(2) +
			uriMatcher.group(3) +
			uriMatcher.group(4);

			binaryDataFilenameAndSuffix = uriMatcher.group(5) + "." + uriMatcher.group(6);
		}

	}*/


	public void init(FilterConfig filterConfig) throws ServletException {

		List<CmsRepository> availableRepositories = repositoryService.getAvailableCmsRepositories();

		if (CollectionUtils.isNotEmpty(availableRepositories)){
			for (CmsRepository cmsRepository: availableRepositories){
				repositoryHomeDirectoriesPerRepositoryId.put(cmsRepository.getId(), cmsRepository.getRepositoryHomeDirectory());
			}
		}
		else{
			if (MapUtils.isEmpty(repositoryHomeDirectoriesPerRepositoryId))
				logger.warn("Found no repository to load its home directory.");
		}
	}

}
