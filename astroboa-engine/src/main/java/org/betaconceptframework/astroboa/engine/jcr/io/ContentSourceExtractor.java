/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
 *
 * This file is part of Astroboa.
 *
 * Astroboa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Astroboa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Astroboa.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.betaconceptframework.astroboa.engine.jcr.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ContentSourceExtractor {

	private File tmpZip;
	
	private ZipFile zipFile;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public InputStream extractStream(URL contentSource) throws Exception
	{
		dispose();
		
		String filename = contentSource.getFile();
		
		if (StringUtils.isBlank(filename))
		{
			throw new Exception("No file name from URL "+ contentSource.toString());
		}
		
		
		if (filename.endsWith(".zip"))
		{
			tmpZip = File.createTempFile("rep",".zip");
			
			FileUtils.copyURLToFile(contentSource, tmpZip);
			
			zipFile = new ZipFile(tmpZip);
			
			Enumeration entries = zipFile.getEntries();
			
			while(entries.hasMoreElements())
			{
				ZipArchiveEntry entry  = (ZipArchiveEntry) entries.nextElement();
				
				if (entry.getName() != null && entry.getName().endsWith(".xml"))
				{
					return zipFile.getInputStream(entry);
				}
			}
			
			return null;
		}
		else if (filename.endsWith(".xml"))
		{
			return contentSource.openStream();
		}
		else
		{
			throw new Exception("Unsupported file extension "+ filename);
		}
	}
	
	public void dispose()
	{
		if (zipFile != null)
		{
			try {
				zipFile.close();
			} catch (IOException e) {
				logger.error("",e);
			}
		}
		
		if (tmpZip != null)
		{
			try {
				tmpZip.delete();
			} catch (Exception e) {
				logger.error("",e);
			}
		}
	}
}
