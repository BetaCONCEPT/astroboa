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

package org.betaconceptframework.astroboa.api.model;

import java.io.InputStream;
import java.util.Calendar;

import org.betaconceptframework.astroboa.api.model.definition.BinaryPropertyDefinition;

/**
 * Represents a resource in content repository.
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface BinaryChannel extends CmsRepositoryEntity {

	public enum ContentDispositionType{
		ATTACHMENT, 
		INLINE
	}
	
	public enum CropPolicy{
		top, 
		center,
		bottom
	}
	
	/**
	 * Returns the name of binary channel.
	 * 
	 * @return BiinaryChannel name.
	 */
	String getName();

	/**
	 * Sets the name of binary channel.
	 * 
	 * BinaryChannel name should be the same with the 
	 * {@link BinaryPropertyDefinition definition} of property
	 * which contains it as a value.
	 * 
	 * @param name
	 *            BinaryChannel name.
	 */
	void setName(String name);

	/**
	 * Returns the filename for binary channel.
	 * 
	 * @return BinaryChannel's filename.
	 */
	String getSourceFilename();

	/**
	 * Sets the filename for binary channel.
	 * 
	 * @param name
	 *            BinaryChannel's filename.
	 */
	void setSourceFilename(String name);
	
	/**
	 * Returns the Binary Channel filename suffix.
	 * 
	 * @return BinaryChannel filename suffix.
	 */
	String getSourceFilenameSuffix();

	/**
	 * Returns MIME type (text/plain, image/gif, application/postscript,
	 * video/mpeg, etc) of resource's content.
	 * 
	 * @return BinaryChannel MIME type.
	 */
	String getMimeType();

	/**
	 * Sets MIME type (text/plain, image/gif, application/postscript,
	 * video/mpeg, etc) of resource's content.
	 *
	 * <p>
	 * MIME type value must be a valid MIME type because if content is indexable,
	 * this value is used to initiate a mechanism to index the contents of the resource.
	 * 
	 * MIME types that will trigger full text search indexing are
	 * 
	 * <ul>
	 *  <li>text/html
	 *  <li>text/plain
	 *  <li>application/vnd.ms-excel
	 *  <li>application/vnd.ms-powerpoint
     *  <li>application/mspowerpoint
     *  <li>application/vnd.ms-word
     *  <li>application/msword
     *  <li>application/vnd.oasis.opendocument.database
     *  <li>application/vnd.oasis.opendocument.formula
     *  <li>application/vnd.oasis.opendocument.graphics
     *  <li>application/vnd.oasis.opendocument.presentation
     *  <li>application/vnd.oasis.opendocument.spreadsheet
     *  <li>application/vnd.oasis.opendocument.text
     *  <li>application/pdf
     *  <li>application/rtf
     *  <li>text/rtf
     *  <li>text/xml
     *  <li>application/xml
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * Although supported MIME types cover a wide range of available 
	 * MIME types, there could be a case where a resource is indexable but 
	 * its MIME type is not supported. In this case, try to choose the closest supported MIME type.
	 * If this is not possible, save binary channel with the unsupported MIME type but
	 * its content will not be indexed.
	 * </p>
	 * 
	 * 
	 * @param mimeType
	 *            BinaryChannel MIME type.
	 */
	void setMimeType(String mimeType);

	/**
	 * Returns actual resource's content.
	 * 
	 * @return Actual resource's content.
	 */
	byte[] getContent();

	/**
	 * Sets actual resource's content.
	 * 
	 * @param content
	 *            Actual resource's content.
	 */
	void setContent(byte[] content);

	/**
	 * Helper method used mainly if a lazy loading mechanism is enabled. If
	 * implementation choose to lazy load binary channel actual content, this
	 * method checks if content is loaded without triggering the lazy loading
	 * mechanism.
	 * 
	 * @return <code>true</code> if content are loaded, <code>false</code>
	 *         otherwise.
	 */
	boolean isNewContentLoaded();

	/**
	 * Returns actual resource's content.
	 * 
	 * @return Actual resource's content as a stream.
	 */
	InputStream getContentAsStream();

	/**
	 * Returns content size in bytes.
	 * 
	 * @return Content size in bytes;
	 */
	long getSize();

	/**
	 * Sets content size in bytes.
	 * 
	 * @param size
	 *            Content size in bytes;
	 */
	void setSize(long size);

	/**
	 * Calculate binary data size in KBytes and return it as
	 * {@link java.lang.String}.
	 * 
	 * @return Content size in KBytes as a {@link java.lang.String}.
	 */
	String getCalculatedSize();

	/**
	 * Returns content's encoding.
	 * 
	 * @return Content's encoding.
	 */
	String getEncoding();

	/**
	 * Sets content's encoding.
	 * 
	 * @param encoding
	 *            Content's encoding.
	 */
	void setEncoding(String encoding);

	/**
	 * Returns the date when binary channel was last modified.
	 * 
	 * @return Last modification date.
	 */
	Calendar getModified();

	/**
	 * Sets the date when binary channel was last modified.
	 * 
	 * @param modified
	 *            Last modification date.
	 */
	void setModified(Calendar modified);

	/**
	 * Returns the filesystem path of
	 * binary channel's actual content, relative to repository home directory ,
	 * if content is stored in filesystem.
	 * 
	 * <p>
	 * Relative file system path in combination with repository home directory,
	 * allows direct access to the binary content through the filesystem
	 * where actual content is stored.
	 * </p>
	 * <p>
	 * The utilized JCR Repository implementation (Jackrabbit) allows to store
	 * binary data either in the DB or to filesystem. The adopted method in
	 * Astroboa is to store binary data in the filesystem in order to increase
	 * performance and allow easy access to big binary files. Acquiring relative
	 * file system path, the developer can manipulate the binary data without
	 * the need to load them into java memory, which is ideal for presenting big
	 * images or streaming audio/video.
	 * </p>
	 * 
	 * <p>
	 * This method is used only within the context of applications where
	 * repository home directory is available.
	 * </p>
	 * 
	 * @return BinaryChannel's actual content filesystem path relative of
	 *         repository home directory.
	 */
	String getRelativeFileSystemPath();
	
	/**
	 * Returns an absolute URL which can be used for retrieval of this binary channel content directly through the file system.
	 * Use this method if you want to generate resource URLs that overcome the Astroboa engine 
	 * and retrieve the binary content directly from the file system file where it is stored.
	 * This kind of binary content retrieval may provide a slight better performance (e.g. for web sites with high traffic) than accessing
	 * binary content through the Astroboa engine.
	 * 
	 * The method can generate valid URLs only if your repository blobs are stored in the file system. 
	 * Be also aware that this URL is insecure. Use it only for public content.
	 * Additionally this type of URL may not be supported in newer versions of Astroboa.
	 * 
	 * In general we recommend to avoid using this method for generating the URLs to your binary content. 
	 * There is an alternative method in this class that generates RESTful API URLs for retrieving binary content through the Astroboa engine 
	 * (i.e. through the content object that holds the binary property).
	 * Prefer the alternative {@link #getContentApiURL() getContentApiURL()} method since it generates secure URLs 
	 * (i.e. the content object may not allow access to the binary content by the user that uses the URL) 
	 * that are complaint with the "RESTful" API of the Astroboa Resource Framework
	 * 
	 * The URLs generated by this method are comprised of a fixed path to a filter that provides direct access to the stored binary content 
	 * <code>http://server/resource-api/f/binaryChannel</code>
	 * 
	 * followed by binary channel direct file access info. 
	 * 
	 * The URL is absolute, i.e. it is prefixed by http:// plus the host name of the server which hosts the content repository. 
	 * 
	 * The server name is configured inside the "astroboa-conf.xml" file that holds the repositories configurations 
	 * 
	 * @return
	 * 		Absolute URL which serves the binary channel content directly through the file system file where it is stored
	 */
	@Deprecated
	String getDirectURL();
	
	/**
	 * Same as {@link #getDirectURL() getDirectURL()} but it returns a relative URL instead of an absolute. 
	 * The URL can be used for retrieval of this binary channel content directly through the file system.
	 * Use this method if you want to generate resource URLs that overcome the Astroboa engine 
	 * and retrieve the binary content directly from the file system file where it is stored.
	 * This kind of binary content retrieval may provide a slight better performance (e.g. for web sites with high traffic) than accessing
	 * binary content through the Astroboa engine.
	 * 
	 * The method can generate valid URLs only if your repository blobs are stored in the file system. 
	 * Be also aware that this URL is insecure. Use it only for public content.
	 * Additionally this type of URL may not be supported in newer versions of Astroboa.
	 * 
	 * In general we recommend to avoid using this method for generating the URLs to your binary content. 
	 * There is an alternative method in this class that generates RESTful API URLs for retrieving binary content through the Astroboa engine 
	 * (i.e. through the content object that holds the binary property).
	 * Prefer the alternative {@link #getRelativeContentApiURL() getRelativeContentApiURL()} method since it generates secure URLs 
	 * (i.e. the content object may not allow access to the binary content by the user that uses the URL) 
	 * that are complaint with the "RESTful" API of the Astroboa Resource Framework
	 *
	 * 
	 * The URLs generated by this method are comprised of a fixed path to a filter that provides direct access to the stored binary content 
	 * <code>/resource-api/f/binaryChannel</code>
	 * 
	 * followed by binary channel direct file access info.
	 * 
	 * The URL is not absolute, i.e. it is NOT prefixed by http:// plus the host name of the server which hosts the content repository. 
	 * 
	 * This URL is useful if it is used in content applications running in the same server with the content repository.
	 * 
	 * The absolute URL is available through {@link #getDirectURL() getDirectURL()}
	 * 
	 * @return
	 * 		Relative URL which serves the binary channel content directly through the file system file where it is stored
	 */
	@Deprecated
	String getRelativeDirectURL();
	
	/**
	 * Same as {@link #getRelativeDirectURL() getRelativeDirectURL()} but it also provides same extra parameters in order to 
	 * allow the generation of a customized relative URL.
	 * The method parameters are used in order to append into the generated URL some extra information about the required content disposition type or 
	 * in the case of image resources to get back the images resized by the server (i.e. provide the required width and height).
	 * 
	 * Prefer the alternative {@link #getCustomizedRelativeContentApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType) getCustomizedRelativeContentApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType)} method since it generates secure URLs 
	 * (i.e. the content object may not allow access to the binary content by the user that uses the URL) 
	 * that are complaint with the "RESTful" API of the Astroboa Resource Framework
	 * 
	 * @param width
	 * @param height
	 * @param contentDispositionType
	 * @return
	 * 	Customized relative URL which serves the binary channel content directly through the file system file where it is stored
	 */
	@Deprecated
	String getCustomizedRelativeDirectURL(Integer width, Integer height, ContentDispositionType contentDispositionType);
	
	/**
	 * Same as {@link #getDirectURL() getDirectURL()} but it also provides same extra parameters in order to 
	 * allow the generation of a customized absolute URL.
	 * The method parameters are used in order to append into the generated URL some extra information about the required content disposition type or 
	 * in the case of image resources to get back the images resized by the server (i.e. provide the required width and height).
	 * 
	 * * Prefer the alternative {@link #getCustomizedContentApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType) getCustomizedContentApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType)} method since it generates secure URLs 
	 * (i.e. the content object may not allow access to the binary content by the user that uses the URL) 
	 * that are complaint with the "RESTful" API of the Astroboa Resource Framework
	 * 
	 * @param width
	 * @param height
	 * @param contentDispositionType
	 * @return
	 * 		Customized absolute URL which serves the binary channel content directly through the file system file where it is stored
	 */
	@Deprecated
	String getCustomizedDirectURL(Integer width, Integer height, ContentDispositionType contentDispositionType);

	/**
	 * Returns the URL of the server that serves the contents of this binary channel i.e. "http://" followed by the server
	 * fully qualified domain name or ip address.
	 * 
	 * The Server URL can used (in combination with {@link #getRestfulApiBasePath()}) to 
	 * synthesize an absolute RESTful API URL which serves the binary channel contents. 
	 * Consider using the {@link #getContentApiURL() or #getCustomizedContentApiURL(Integer, Integer, ContentDispositionType)} that automatically synthesize the absolute RESTful API URL.
	 *   
	 * @return
	 * 	The URL of the Astroboa server which serves the binary content. The URL is trimmed and if a trailing slash exists it is removed 
	 */
	String getServerURL();
	
	/**
	 * Returns the base path of the RESTful API which serves this binary resource i.e. the path that follows the Server URL provided by {@link #getServerURL()}. 
	 * 
	 * The path can used (in combination with {@link #getServerURL()} if absolute URLs are required) in order to
	 * determine the base URL for accessing the Astroboa RESful API and
	 * retrieve the binary file stored in the binary channel.
	 * 
	 * Consider using the {@link #getContentApiURL() or #getRelativeContentApiURL() or #getCustomizedContentApiURL(Integer, Integer, ContentDispositionType) or #getCustomizedRelativeContentApiURL(Integer, Integer, ContentDispositionType)} 
	 * that automatically synthesize the absolute/relative RESTful API URLs.
	 * 
	 * @return The base path (relative to the Astroboa repository server URL) under which the RESTful API is accessible (for retrieving the binary channel content). The path is trimmed, if a trailing slash exists it is removed, and if no leading slash exists it is added
	 */
	public String getRestfulApiBasePath();
	
	/**
	 * Returns an absolute RESTful API URL which can be used for secure retrieval of this binary channel content.
	 *  
	 * <p>
	 * Prefer this method over the similar one {@link #getDirectURL() getDirectURL()}
	 * The URLs generated by this method differ from URLs constructed by method {@link #getDirectURL() getDirectURL()} in two ways:
	 * They are complaint with the RESTful API provided by Astroboa. 
	 * They are secure since they are served through the Astroboa engine and not directly from the file system file where binary content is stored.
	 * {@link #getDirectURL()} locates the file system file where the binary content is stored and generates URLs that get the binary content right from the located file system path.
	 * This method generates URLs which use the Astroboa RESTful API to access a resource property. 
	 * In our case the generated RESTful API URL requests this binary channel content from its {@link ContentObject}/{@link BinaryProperty} container. 
	 * If the GET request for this URL does not contain the appropriate credentials for accessing the content object or if the content object is not public then access will not be allowed.
	 * </p>
	 * 
	 * <p>
	 * The generated URL follows the general Astroboa RESTful API URL pattern for accessing content object properties
	 * <code>http://server/resource-api/{repository-id}/objects/{contentObjectId}/{binaryChannelPath}</code>
	 * The {binaryChannelPath} is the path to the binary property that contains the binary channel possibly followed by a value index in brackets if the
	 * binary property contains multiple binary channels.
	 * 
	 * The URL is absolute, i.e. it is prefixed by http:// plus the host name of the server which hosts the content repository. 
	 * 
	 * The server name is configured inside the "astroboa-conf.xml" file that holds the repositories configurations 
	 * </p>
	 * 
	 * @deprecated Use {@link #buildResourceApiURL(Integer, Integer, Double, CropPolicy, ContentDispositionType, boolean, boolean)} instead
	 * @return
	 * 		The absolute Astroboa RESTful API URL which serves the binary content of this binary channel
	 */
	String getContentApiURL();
	
	/**
	 * Same as {@link #getContentApiURL()} but it returns a relative RESTful API URL instead of an absolute one.
	 * 
	 * <p>
	 * Prefer this method over the similar one {@link #getRelativeDirectURL() getRelativeDirectURL()}
	 * </p>
	 * 
	 * The URL is not absolute, i.e. it is NOT prefixed by http:// plus the host name of the server which hosts the content repository. 
	 * 
	 * This URL is useful if it is used in content applications running in the same server with the content repository.
	 * 
	 * The absolute URL is available through {@link #getContentApiURL()}
	 * 
	 * @deprecated Use {@link #buildResourceApiURL(Integer, Integer, Double, CropPolicy, ContentDispositionType, boolean, boolean)} instead
	 * 
	 * @return
	 * 		The relative Astroboa RESTful API URL which serves the binary content of this binary channel
	 */
	String getRelativeContentApiURL();
	
	/**
	 * Same as {@link #getContentApiURL() getContentApiURL()} but it also provides same extra parameters in order to 
	 * allow the generation of a customized absolute URL.
	 * The method parameters are used in order to append into the generated URL some extra information (as query parameters) about the required content disposition type or 
	 * in the case of image resources for getting back the images resized by the server (i.e. provide the required width and height).
	 * 
	 * <p>
	 * Prefer this method over the similar one {@link #getCustomizedDirectURL(Integer, Integer, ContentDispositionType) getCustomizedDirectURL(Integer, Integer, ContentDispositionType)}
	 * </p>
	 * 
	 * @param width Desired width 
	 * @param height Desired height
	 * @param contentDispositionType  <code>attachment</code> or <code>inline</code> (default value)
	 * 
	 * 
	 * @deprecated Use {@link #buildResourceApiURL(Integer, Integer, Double, CropPolicy, ContentDispositionType, boolean, boolean)} instead
	 * 
	 * @return 
	 * 		A customized absolute Astroboa RESTful API URL which serves the binary content of this binary channel
	 */
	String getCustomizedContentApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType);
	
	/**
	 * Same as {@link #getRelativeContentApiURL() getRelativeContentApiURL} but it also provides same extra parameters in order to 
	 * allow the generation of a customized relative URL.
	 * The method parameters are used in order to append into the generated URL some extra information (as query parameters) about the required content disposition type or 
	 * in the case of image resources for getting back the images resized by the server (i.e. provide the required width and height).
	 * 
	 * <p>
	 * Prefer this method over the similar one {@link #getCustomizedRelativeDirectURL(Integer, Integer, ContentDispositionType), getCustomizedRelativeDirectURL(Integer, Integer, ContentDispositionType)}
	 * </p>
	 * 
	 * 
	 * @param width Desired width 
	 * @param height Desired height
	 * @param contentDispositionType  <code>attachment</code> or <code>inline</code> (default value)	 
	 *
	 * @deprecated Use {@link #buildResourceApiURL(Integer, Integer, Double, CropPolicy, ContentDispositionType, boolean, boolean)} instead
	 * 
	 * @return
	 * 		A customized relative Astroboa RESTful API URL which serves the binary content of this binary channel
	 */
	String getCustomizedRelativeContentApiURL(Integer width, Integer height, ContentDispositionType contentDispositionType);
	
	/**
	 * Checks whether actual content exists either in the specified system path or
	 * if not in the provided (if any) content URL
	 * 
	 * 
	 * @return <code>true</code> if actual content exists, <code>false</code> otherwise
	 */
	boolean contentExists();
	
	/**
	 * Utility method to buid a Resource API URL that retreives the binary channel content
	 * If <code>friendlyUrl</code> is true the the content object system name is used for the generation of the URL.
	 * Otherwise the object id will be used to generate a permanent URL. Friendly URLs are not permanent because the system name 
	 * of the object may change. So if you require a permanent URL for other people to link to your resource use permanent URLs.
	 *   
	 * @param width if binary channel contains a JPG,PNG or GIF image use this parameter to resize the image
	 * @param height if binary channel contains a JPG,PNG or GIF image use this parameter to resize the image
	 * @param aspectRatio if binary channel contains a JPG,PNG or GIF image use this parameter to specify a new ratio for width / height. 
	 * Be aware that if you specify width, height and aspectRatio at the same time then the aspectRatio will be ignored.
	 * @param cropPolicy <code>top</code> (default value), <code>center</code>, <code>bottom</code>. Use it to specify where to crop from when aspect ratio is changed
	 * @param contentDispositionType contentDispositionType  <code>attachment</code> or <code>inline</code> (default value)
	 * @param friendlyUrl Specify whether you require a SEO / User friendly URL or a permanent URL. Permanent URLs are good for sharing while friendly URLs are better for Web Sites.
	 * @param relative Specify whether you want the URL to be absolute or not , i.e. whether the URL will be prefixed by http:// plus the host name of the server which hosts the content repository or not.
	 * @return
	 * 		A Resource API (RESTful) URL that retreives the binary channel content from the repository 
	 */
	String buildResourceApiURL(Integer width, Integer height, Double aspectRatio, CropPolicy cropPolicy, ContentDispositionType contentDispositionType, boolean friendlyUrl, boolean relative);


}
