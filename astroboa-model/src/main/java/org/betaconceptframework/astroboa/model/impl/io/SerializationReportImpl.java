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
package org.betaconceptframework.astroboa.model.impl.io;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.betaconceptframework.astroboa.api.model.io.SerializationReport;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SerializationReportImpl implements SerializationReport, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5790138562652996169L;
	
	private String filename;
	private String absolutePath;
	
	private boolean finished;
	
	private int objectsSerialized;
	
	private int repositoryUsersSerialized;
	
	private int taxonomiesSerialized;

	private int spacesSerialized;

	private List<String> errors = new ArrayList<String>();

	
	public SerializationReportImpl(String filename, String filePath) {
		this.filename = filename;
		this.absolutePath = filePath+File.separator+filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getAbsolutePath() {
		return absolutePath;
	}

	@Override
	public boolean hasSerializationFinished() {
		return finished;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public int getCountOfSerializedEntities() {
		return objectsSerialized+taxonomiesSerialized+repositoryUsersSerialized+spacesSerialized;
	}

	public List<String> getErrors() {
		return errors;
	}

	@Override
	public int getNumberOfObjectsSerialized() {
		return objectsSerialized;
	}

	@Override
	public int getNumberOfRepositoryUsersSerialized() {
		return repositoryUsersSerialized;
	}

	@Override
	public int getNumberOfTaxonomiesSerialized() {
		return taxonomiesSerialized;
	}
	
	@Override
	public int getNumberOfSpacesSerialized() {
		return spacesSerialized;
	}

	
	public void increaseNumberOfObjectsSerialized(int objectsSerialized) {
		this.objectsSerialized += objectsSerialized;
	}

	public void increaseRepositoryUsersSerialized(int repositoryUsersSerialized) {
		this.repositoryUsersSerialized += repositoryUsersSerialized;
	}

	public void increaseTaxonomiesSerialized(int taxonomiesSerialized) {
		this.taxonomiesSerialized += taxonomiesSerialized;
	}

	public void increaseSpacesSerialized(int spacesSerialized) {
		this.spacesSerialized += spacesSerialized;
		
	}


	
}
