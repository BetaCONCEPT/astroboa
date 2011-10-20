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

/**
 * Configuration for serialization process. 
 * 
 * <p>
 * SerializationConfiguration allows users to control how to export content
 * from an Astroboa repository.   
 * </p>
 * 
 * <p>
 * In order to build a configuration, one must specify the type 
 * of the entity whose instances will be serialized and then, she must 
 * use the available methods in order to provide values to one or more 
 * parameters. Finally, method <code>build</code> must be called to complete 
 * configuration creation.
 *  
 * For example, configuration for serializing objects can be declared like this:
 * 
 * <pre>
 * SerializationConfiguration serializationConfiguration = SerializationConfiguration.object()
		.prettyPrint(false)
		.representationType(ResourceRepresentationType.XML)
		.serializeBinaryContent(false)
		.build(); 
 * </pre>
 *  
 * </p> * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class SerializationConfiguration implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3390980528511007459L;
	/**
 	 * <code>true</code> to export binary content, i.e. serialize binary properties, 
	 * <code>false</code> otherwise. In both cases, a URL which serves the binary content is always provided.
	 */
	private final boolean serializeBinaryContent;
	
	/**
	 * @param output Representation output, one of XML, JSON. Default is {@link ResourceRepresentationType#XML}
	*/
	private final ResourceRepresentationType resourceRepresentationType;
	
	/**
	 *	<code>true</code> to enable pretty printer functionality such as 
	 * adding indentation and linefeeds in order to make output more human readable, <code>false<code> otherwise
	 */
	private final boolean prettyPrint;
	
	public static class Configuration implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 532970162615043958L;
		
		protected boolean serializeBinaryContent;
		protected ResourceRepresentationType resourceRepresentationType;
		protected boolean prettyPrint;

		private Configuration(){
			
		}
		
		public SerializationConfiguration build(){
			return new SerializationConfiguration(this);
		}
	}

	
	public static class RepositorySerializationConf extends Configuration{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1221535674455256525L;

		private RepositorySerializationConf(){
			super();
			this.serializeBinaryContent = false;
			this.resourceRepresentationType = ResourceRepresentationType.XML;
			this.prettyPrint = false;
		}
		
		public RepositorySerializationConf serializeBinaryContent(boolean serializeBinaryContent){
			this.serializeBinaryContent = serializeBinaryContent;
			return this;
		}

		public RepositorySerializationConf representationType(ResourceRepresentationType resourceRepresentationType){
			this.resourceRepresentationType = resourceRepresentationType;
			return this;
		}
		
		public RepositorySerializationConf prettyPrint(boolean prettyPrint){
			this.prettyPrint = prettyPrint;
			return this;
		}
		
	}
	
	public static class ObjectSerializationConf extends Configuration {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 5436598040383270426L;

		private ObjectSerializationConf(){
			super();
			this.serializeBinaryContent = false;
			this.resourceRepresentationType = ResourceRepresentationType.XML;
			this.prettyPrint = false;
		}
		
		public ObjectSerializationConf serializeBinaryContent(boolean serializeBinaryContent){
			this.serializeBinaryContent = serializeBinaryContent;
			return this;
		}
		
		public ObjectSerializationConf representationType(ResourceRepresentationType resourceRepresentationType){
			this.resourceRepresentationType = resourceRepresentationType;
			return this;
		} 

		public ObjectSerializationConf prettyPrint(boolean prettyPrint){
			this.prettyPrint = prettyPrint;
			return this;
		}

	}
	
	public static class TaxonomySerializationConf extends Configuration {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2045598736182265047L;

		private TaxonomySerializationConf(){
			super();
			this.resourceRepresentationType = ResourceRepresentationType.XML;
			this.prettyPrint = false;
		}
		
		public TaxonomySerializationConf representationType(ResourceRepresentationType resourceRepresentationType){
			this.resourceRepresentationType = resourceRepresentationType;
			return this;
		} 

		public TaxonomySerializationConf prettyPrint(boolean prettyPrint){
			this.prettyPrint = prettyPrint;
			return this;
		}
	}
	
	public static class TopicSerializationConf extends Configuration {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -269132925031999286L;

		private TopicSerializationConf(){
			super();
			this.resourceRepresentationType = ResourceRepresentationType.XML;
			this.prettyPrint = false;
		}
		
		public TopicSerializationConf representationType(ResourceRepresentationType resourceRepresentationType){
			this.resourceRepresentationType = resourceRepresentationType;
			return this;
		} 

		public TopicSerializationConf prettyPrint(boolean prettyPrint){
			this.prettyPrint = prettyPrint;
			return this;
		}
	}

	public static class SpaceSerializationConf extends Configuration {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8353729215893642781L;

		private SpaceSerializationConf(){
			super();
			this.resourceRepresentationType = ResourceRepresentationType.XML;
			this.prettyPrint = false;
		}
		
		public SpaceSerializationConf representationType(ResourceRepresentationType resourceRepresentationType){
			this.resourceRepresentationType = resourceRepresentationType;
			return this;
		} 

		public SpaceSerializationConf prettyPrint(boolean prettyPrint){
			this.prettyPrint = prettyPrint;
			return this;
		}
	}
	
	public static class RepositoryUserSerializationConf extends Configuration {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8353729215893642781L;

		private RepositoryUserSerializationConf(){
			super();
			this.resourceRepresentationType = ResourceRepresentationType.XML;
			this.prettyPrint = false;
		}
		
		public RepositoryUserSerializationConf representationType(ResourceRepresentationType resourceRepresentationType){
			this.resourceRepresentationType = resourceRepresentationType;
			return this;
		} 

		public RepositoryUserSerializationConf prettyPrint(boolean prettyPrint){
			this.prettyPrint = prettyPrint;
			return this;
		}
	}
	
	private SerializationConfiguration(Configuration builder) {
		this.serializeBinaryContent = builder.serializeBinaryContent;
		this.resourceRepresentationType = builder.resourceRepresentationType;
		this.prettyPrint = builder.prettyPrint;
	}

	
	public static ObjectSerializationConf object(){
		return new ObjectSerializationConf();
	}
	
	public static RepositorySerializationConf repository(){
		return new RepositorySerializationConf();
	}

	public static TaxonomySerializationConf taxonomy(){
		return new TaxonomySerializationConf();
	}

	public static TopicSerializationConf topic(){
		return new TopicSerializationConf();
	}

	public static SpaceSerializationConf space(){
		return new SpaceSerializationConf();
	}

	public static RepositoryUserSerializationConf repositoryUser(){
		return new RepositoryUserSerializationConf();
	}

	public boolean serializeBinaryContent(){
		return serializeBinaryContent;
	}
	
	public ResourceRepresentationType getResourceRepresentationType(){
		return resourceRepresentationType;
	}
	
	public boolean prettyPrint(){
		return prettyPrint;
	}
	
	public boolean isXMLRepresentationTypeEnabled(){
		return resourceRepresentationType != null && ResourceRepresentationType.XML.equals(resourceRepresentationType);
	}
	
	public boolean isJSONRepresentationTypeEnabled(){
		return resourceRepresentationType != null && ResourceRepresentationType.JSON.equals(resourceRepresentationType);
	}
	
}
