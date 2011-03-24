/*
 * Copyright (C) 2005-2011 BetaCONCEPT LP.
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
package org.betaconceptframework.astroboa.console.jsf.edit;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class XSDSchemaContainer {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private File schemaFile;
	
	private String schema;

	public String getSchema() {
		return schema;
	}

	public boolean save() {
		try {
			if (schemaFile != null){
				FileUtils.writeStringToFile(schemaFile, schema, "UTF-8");
			}
		} catch (IOException e) {
			logger.error("",e);
			return false;
		}
		
		return true;
		
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public File getSchemaFile() {
		return schemaFile;
	}

	public void setSchemaFile(File schemaFile) {
		this.schemaFile = schemaFile;
	}
}