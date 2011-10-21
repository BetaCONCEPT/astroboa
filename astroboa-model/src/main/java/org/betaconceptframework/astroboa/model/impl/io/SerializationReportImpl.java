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
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.util.CmsConstants;

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
	
	private int completedSerializedObjects;
	private int totalNumberOfObjectsToBeSerialized;
	
	private int completedSerializedRepositoryUsers;
	private int totalNumberOfRepositoryUsersToBeSerialized;
	
	private int completedSerializedTaxonomies;
	private int totalNumberOfTaxonomiesToBeSerialized;

	private int completedSerializedTopics;
	private int totalNumberOfTopicsToBeSerialized;

	private int completedSerializedSpaces;
	private int totalNumberOfSpacesToBeSerialized;

	private List<String> errors = new ArrayList<String>();


	
	public SerializationReportImpl(String filename, String filePath) {

		String repositoryHomeDir = AstroboaClientContextHolder.getActiveClientContext().getRepositoryContext().getCmsRepository().getRepositoryHomeDirectory();

		this.filename = filename;
		this.absolutePath = repositoryHomeDir+File.separator+ CmsConstants.SERIALIZATION_DIR_NAME+File.separator+filePath+File.separator+filename;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public String getAbsolutePath() {
		return absolutePath;
	}

	public int getCountOfCompletedSerializedEntities() {
		return completedSerializedObjects+completedSerializedTaxonomies+completedSerializedRepositoryUsers+completedSerializedSpaces+completedSerializedTopics;
	}

	public List<String> getErrors() {
		return errors;
	}

	@Override
	public int getNumberOfCompletedSerializedObjects() {
		return completedSerializedObjects;
	}

	@Override
	public int getNumberOfCompletedSerializedRepositoryUsers() {
		return completedSerializedRepositoryUsers;
	}

	@Override
	public int getNumberOfCompletedSerializedTaxonomies() {
		return completedSerializedTaxonomies;
	}
	
	@Override
	public int getNumberOfCompletedSerializedSpaces() {
		return completedSerializedSpaces;
	}

	public void increaseNumberOfCompletedSerializedObjects(int completedSerializedObjects) {
		this.completedSerializedObjects += completedSerializedObjects;
	}

	public void increaseNumberOfCompletedSerializedRepositoryUsers(int completedSerializedRepositoryUsers) {
		this.completedSerializedRepositoryUsers += completedSerializedRepositoryUsers;
	}

	public void increaseNumberOfCompletedSerializedTaxonomies(int completedSerializedTaxonomies) {
		this.completedSerializedTaxonomies += completedSerializedTaxonomies;
	}

	public void increaseNumberOfCompletedSerializedSpaces(int completedSerializedSpaces) {
		this.completedSerializedSpaces += completedSerializedSpaces;
	}
	
	public void increaseNumberOfCompletedSerializedTopics(int completedSerializedTopics) {
		this.completedSerializedTopics += completedSerializedTopics;
	}

	@Override
	public int getTotalNumberOfObjects() {
		return totalNumberOfObjectsToBeSerialized;
	}

	@Override
	public int getTotalNumberOfRepositoryUsers() {
		return totalNumberOfRepositoryUsersToBeSerialized;
	}

	@Override
	public int getTotalNumberOfTaxonomies() {
		return totalNumberOfTaxonomiesToBeSerialized;
	}

	@Override
	public int getTotalNumberOfSpaces() {
		return totalNumberOfSpacesToBeSerialized;
	}

	@Override
	public int getTotalNumberOfTopics() {
		return totalNumberOfTopicsToBeSerialized;
	}

	public void setTotalNumberOfObjectsToBeSerialized(
			int totalNumberOfObjectsToBeSerialized) {
		this.totalNumberOfObjectsToBeSerialized = totalNumberOfObjectsToBeSerialized;
	}

	public void setTotalNumberOfRepositoryUsersToBeSerialized(
			int totalNumberOfRepositoryUsersToBeSerialized) {
		this.totalNumberOfRepositoryUsersToBeSerialized = totalNumberOfRepositoryUsersToBeSerialized;
	}

	public void setTotalNumberOfTaxonomiesToBeSerialized(
			int totalNumberOfTaxonomiesToBeSerialized) {
		this.totalNumberOfTaxonomiesToBeSerialized = totalNumberOfTaxonomiesToBeSerialized;
	}

	public void setTotalNumberOfSpacesToBeSerialized(
			int totalNumberOfSpacesToBeSerialized) {
		this.totalNumberOfSpacesToBeSerialized = totalNumberOfSpacesToBeSerialized;
	}

	public void setTotalNumberOfTopicsToBeSerialized(int totalNumberOfTopicsToBeSerialized) {
		this.totalNumberOfTopicsToBeSerialized = totalNumberOfTopicsToBeSerialized;
		
	}

	@Override
	public int getNumberOfCompletedSerializedTopics() {
		return this.completedSerializedTopics;
	}


	
}
