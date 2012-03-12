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
