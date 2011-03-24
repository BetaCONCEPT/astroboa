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
package org.betaconceptframework.astroboa.api.model.io;

import java.io.Serializable;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.definition.CmsDefinition;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;

/**
 * Represents all the possible representation types supported by Astroboa.
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ResourceRepresentationType<T> implements Serializable{

	/**
	 * XML representation
	 */
	public final static ResourceRepresentationType<String> XML = new ResourceRepresentationType<String>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -5045796116365559528L;

		@Override
		public String getTypeAsString() {
			return "XML";
		}
	};
	
	/**
	 * JSON representation
	 */
	public final static ResourceRepresentationType<String> JSON = new ResourceRepresentationType<String>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 3036275479370950737L;

		@Override
		public String getTypeAsString() {
			return "JSON";
		}
	};
	
	/**
	 * XSD representation
	 */
	public final static ResourceRepresentationType<String> XSD = new ResourceRepresentationType<String>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = -5045796116365559528L;

		@Override
		public String getTypeAsString() {
			return "XSD";
		}
	};
	
	public final static ResourceRepresentationType<ContentObject> CONTENT_OBJECT_INSTANCE = new ResourceRepresentationType<ContentObject>(){

		/**
		 * 
		 */
		private static final long serialVersionUID = 2361376909457226060L;

		@Override
		public String getTypeAsString() {
			return "CONTENT_OBJECT_INSTANCE";
		}
	};
	public final static ResourceRepresentationType<CmsOutcome<ContentObject>> CONTENT_OBJECT_LIST = new ResourceRepresentationType<CmsOutcome<ContentObject>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 7546478796459390965L;

		@Override
		public String getTypeAsString() {
			return "CONTENT_OBJECT_LIST";
		}
	};
	
	public final static ResourceRepresentationType<CmsDefinition> DEFINITION_INSTANCE = new ResourceRepresentationType<CmsDefinition>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -6241746452875452207L;

		@Override
		public String getTypeAsString() {
			return "DEFINITION_INSTANCE";
		}
	};
	
	public final static ResourceRepresentationType<CmsOutcome<CmsDefinition>> DEFINITION_LIST = new ResourceRepresentationType<CmsOutcome<CmsDefinition>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -9198333423274605819L;

		@Override
		public String getTypeAsString() {
			return "DEFINITION_LIST";
		}
	};
	
	public final static ResourceRepresentationType<Space> SPACE_INSTANCE = new ResourceRepresentationType<Space>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -9142563659642934197L;

		@Override
		public String getTypeAsString() {
			return "SPACE_INSTANCE";
		}
	};
	
	public final static ResourceRepresentationType<CmsOutcome<Space>> SPACE_LIST = new ResourceRepresentationType<CmsOutcome<Space>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = -4741029001511863296L;

		@Override
		public String getTypeAsString() {
			return "SPACE_LIST";
		}
	};
	
	public final static ResourceRepresentationType<Taxonomy> TAXONOMY_INSTANCE = new ResourceRepresentationType<Taxonomy>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 7157078749869223283L;

		@Override
		public String getTypeAsString() {
			return "TAXONOMY_INSTANCE";
		}
	};
	public final static ResourceRepresentationType<CmsOutcome<Taxonomy>> TAXONOMY_LIST = new ResourceRepresentationType<CmsOutcome<Taxonomy>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 9003209669474432048L;

		@Override
		public String getTypeAsString() {
			return "TAXONOMY_LIST";
		}
	};
	
	public final static ResourceRepresentationType<Topic> TOPIC_INSTANCE = new ResourceRepresentationType<Topic>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 8208657372604248660L;

		@Override
		public String getTypeAsString() {
			return "TOPIC_INSTANCE";
		}
	};
	public final static ResourceRepresentationType<CmsOutcome<Topic>> TOPIC_LIST = new ResourceRepresentationType<CmsOutcome<Topic>>(){
		/**
		 * 
		 */
		private static final long serialVersionUID = 7415736574428968765L;

		@Override
		public String getTypeAsString() {
			return "TOPIC_LIST";
		}
	};
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2880924484049218304L;

	public String getTypeAsString(){return "";}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getTypeAsString() == null) ? 0 : getTypeAsString().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ResourceRepresentationType<?> other = (ResourceRepresentationType<?>) obj;
		if (getTypeAsString() == null) {
			if (other.getTypeAsString() != null)
				return false;
		} else if (!getTypeAsString().equals(other.getTypeAsString()))
			return false;
		return true;
	}

}
