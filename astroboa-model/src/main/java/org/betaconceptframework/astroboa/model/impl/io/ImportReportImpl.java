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
package org.betaconceptframework.astroboa.model.impl.io;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.io.ImportReport;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportReportImpl implements ImportReport, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1162328248724092260L;

	private List<String> errors = new ArrayList<String>();
	
	private int contentObjectsImported;
	
	private int repositoryUsersImported;
	
	private int taxonomiesImported;
	
	private int spacesImported;
	
	private int topicsImported;

	@Override
	public int getNumberOfContentObjectsImported() {
		return contentObjectsImported;
	}

	@Override
	public int getNumberOfRepositoryUsersImported() {
		return repositoryUsersImported;
	}

	@Override
	public int getNumberOfTaxonomiesImported() {
		return taxonomiesImported;
	}

	@Override
	public int getNumberOfSpacesImported() {
		return spacesImported;
	}

	@Override
	public int getNumberOfTopicsImported() {
		return topicsImported;
	}
	
	public List<String> getErrors() {
		return errors;
	}

	public void addContentObjectsImported(int contentObjectsImported) {
		this.contentObjectsImported += contentObjectsImported;
	}

	public void addRepositoryUsersImported(int repositoryUsersImported) {
		this.repositoryUsersImported += repositoryUsersImported;
	}

	public void addTaxonomiesImported(int taxonomiesImported) {
		this.taxonomiesImported += taxonomiesImported;
	}
	
	public void addTopicsImported(int topicsImported) {
		this.topicsImported += topicsImported;
	}

	public int getTotalCountOfImportedEntities() {
		return contentObjectsImported+repositoryUsersImported+taxonomiesImported+spacesImported+topicsImported;
	}

	public void addSpacesImported(int spacesImported) {
		this.spacesImported += spacesImported;
		
	}

}
