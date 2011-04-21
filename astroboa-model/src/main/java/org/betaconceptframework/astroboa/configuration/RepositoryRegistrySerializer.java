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
package org.betaconceptframework.astroboa.configuration;

import java.util.List;

import org.betaconceptframework.astroboa.configuration.LocalizationType.Label;
import org.betaconceptframework.astroboa.util.AbstractSerializer;


/**
 * Astroboa Repository Registry Serializer.
 * 
 * It serializes configuration to XML or JSON
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class RepositoryRegistrySerializer extends AbstractSerializer{


	public RepositoryRegistrySerializer(boolean prettyPrint, boolean jsonOutput) {

		super(prettyPrint, jsonOutput);
		
	}

	public String serialize(){

		if (! outputisJSON()){
			startElement("astroboa", true, true);
		}
		
		writeAttribute("server", RepositoryRegistry.INSTANCE.getDefaultServerURL());
		
		if (! outputisJSON()){
			endElement("astroboa", true, false);	
		}
		
		exportRepositoryInfo(RepositoryRegistry.INSTANCE.getRepositories().getRepository());

		if (! outputisJSON()){
			endElement("astroboa", false, true);
		}
		
		return super.serialize();
	}


	private void exportRepositoryInfo(List<RepositoryType> repositories) {
			
		if (outputisJSON()){
			startArray("repository");
		}
		
		for (RepositoryType repository : repositories){
			
				startElement("repository", true, false);
				
				writeAttribute("id", repository.getId());
				
				if (! outputisJSON()){
					endElement("repository", true, false);	
				}
				
				if (repository.getLocalization() != null){
					addLocalization(repository.getLocalization().getLabel());
				}
				
				endElement("repository", false, true);

			}
		
		if (outputisJSON()){
			endArray();
		}

	}

	private void addLocalization(List<Label> labels) {
		
		startElement("label", true, true);

		for (Label label : labels){
			writeAttribute(label.getLang(), label.getValue());
		}
		
		endElement("label", true, true);	
		
	}
	
}
