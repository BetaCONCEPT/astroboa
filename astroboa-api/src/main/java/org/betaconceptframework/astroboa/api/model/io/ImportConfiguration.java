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
import java.util.HashMap;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.ValueType;

/**
 * Contains configuration for import/deserialization process. 
 * 
 * <p>
 * ImportConfiguration allows users to control how to import content
 * to an Astroboa repository.
 * </p>
 * 
 * <p>
 * In order to build a configuration, one must specify the type 
 * of the entity whose instances will be imported and then, she must 
 * use the available methods in order to provide values to one or more 
 * parameters. Finally, method <code>build</code> must be called to complete 
 * configuration creation.
 *  
 * For example, configuration for importing objects can be declared like this:
 * 
 * <pre>
 * ImportConfiguration configuration = ImportConfiguration.object()
				.persist(PersistMode.PERSIST_MAIN_ENTITY)
				.version(false)
				.updateLastModificationTime(false)
				.build();
 * </pre>
 *  
 * </p>
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class ImportConfiguration implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2785751336766426516L;
	

	public enum PersistMode {
		
		/**
		 * Import content to all 
		 * corresponding entities but do not persist them.
		 */
		DO_NOT_PERSIST,
		
		/**
		 * Import content to all 
		 * corresponding entities and persist them.
		 */ 
		PERSIST_MAIN_ENTITY,
		
		/**
		 * Import content to all corresponding entities
		 * and persist not only the main entity but the whole
		 * entity tree. 
		 * Used mainly for Topic, Taxonomy and Space.
		 */ 
		PERSIST_ENTITY_TREE,
	}
	
	/**
	 * <code>true</code> to create a new version for content
	 * object, <code>false</code> otherwise. 
	 * It is used only when <code>save</code> is <code>true</code>
	 * and imported entity is a {@link ContentObject}.
	 */
	private final boolean version;
	
	/**
	 * Control whether to persist or not 
	 * imported content
	 */
	private final PersistMode persistMode;
	
	/**
	 * <code>true</code> to change last modification date of the imported entity,
	 * <code>false</code> otherwise.
	 * It is used only when <code>save</code> is <code>true</code>
	 * and imported entity is a {@link ContentObject}.
	 */
	private final boolean updateLastModificationTime;
	
	/**
	 * Define the behavior of the import process when the values of one or more properties 
	 * of the objects being imported, are references to other objects which do not exist in 
	 * the repository.
	 * 
	 * The default behavior is to stop the import process and to throw an exception, in order
	 * to notify the user for the non-existence of the objects.
	 *  
	 * Set this flag to <code>true</code> in order to suppress this behavior and to allow
	 * the import process to continue. In this case, a warning is issued, the missing
	 * reference is saved as the value of the property as if the referred object exists and the
	 * import process smoothly continues.
	 * 
	 */
	private final boolean saveMissingObjectReferences;
	
	/**
	 * Map containing the binary content of one or more properties of type {@link ValueType#Binary}. 
	 * The key of the map must match the value of the 'url' attribute of the property 
	 * in the XML/JSON representation of the content.
	 * It is used only when imported entity is a {@link ContentObject}.
	 */
	private final Map<String, byte[]> binaryContent;
	
	
	/** 
	 * User credentials to be used when downloading binary content
	 * from a provided URL in order to save it to a repository.
	 * If any credentials are provided then an authorization header will be
	 * created and it will be used upon request.  
	 */
	private final Credentials credentialsOfUserWhoHasAccessToBinaryContent;
	
	public static class Configuration implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8216577761965559480L;
		
		protected PersistMode persistMode;
		protected boolean version;
		protected boolean updateLastModificationTime;
		protected Map<String, byte[]> binaryContent;
		protected boolean saveMissingObjectReferences;
		protected Credentials credentialsOfUserWhoHasAccessToBinaryContent;
		
		private Configuration(){
			
		}
		
		public ImportConfiguration build(){
			return new ImportConfiguration(this);
		}
	}

	
	public static class TaxonomyImportConfiguration extends Configuration{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 6987813675753693566L;

		private TaxonomyImportConfiguration(){
			super();
		}
		
		public TaxonomyImportConfiguration persist(PersistMode persistMode){
			this.persistMode = persistMode;
			return this;
		}
	}

	public static class TopicImportConfiguration extends Configuration{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -8672051521906802611L;

		private TopicImportConfiguration(){
			super();
		}
		
		public TopicImportConfiguration persist(PersistMode persistMode){
			this.persistMode = persistMode;
			return this;
		}
	}

	public static class RepositoryUserImportConfiguration extends Configuration{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3327651249846102439L;

		private RepositoryUserImportConfiguration(){
			super();
		}
		
		public RepositoryUserImportConfiguration persist(PersistMode persistMode){
			this.persistMode = persistMode;
			return this;
		}
	}
	
	public static class SpaceImportConfiguration extends Configuration{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3119157151529381090L;

		private SpaceImportConfiguration(){
			super();
		}
		
		public SpaceImportConfiguration persist(PersistMode persistMode){
			this.persistMode = persistMode;
			return this;
		}
	}


	public static class RepositoryImportConfiguration extends Configuration{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -717092049149850366L;

		private RepositoryImportConfiguration(){
			super();
			this.persistMode = PersistMode.PERSIST_ENTITY_TREE;
			this.saveMissingObjectReferences = false;
		}
		
		public RepositoryImportConfiguration version(boolean version){
			this.version = version;
			return this;
		}
		
		public RepositoryImportConfiguration updateLastModificationTime(boolean updateLastModificationTime){
			this.updateLastModificationTime = updateLastModificationTime;
			return this;
		}
		
		public RepositoryImportConfiguration addBinaryContent(String binaryContentId, byte[] binaryContent){
			
			if (binaryContentId == null || binaryContentId.trim().length() == 0){
				return this;
			}
			
			if (this.binaryContent == null){
				this.binaryContent = new HashMap<String, byte[]>();
			}
			
			this.binaryContent.put(binaryContentId, binaryContent);
			
			
			return this;
		}

		public RepositoryImportConfiguration addBinaryContent(Map<String, byte[]> binaryContent){
			
			if (binaryContent == null || binaryContent.isEmpty()){
				return this;
			}
			
			if (this.binaryContent == null){
				this.binaryContent = new HashMap<String, byte[]>();
			}
			
			this.binaryContent.putAll(binaryContent);
			
			return this;
		}
		
		public RepositoryImportConfiguration saveMissingObjectReferences(boolean saveMissingObjectReferences){
			this.saveMissingObjectReferences = saveMissingObjectReferences;
			return this;
		}
		
		public RepositoryImportConfiguration credentialsOfUserWithAccessToBinaryContent(String username, String password){
			this.credentialsOfUserWhoHasAccessToBinaryContent = new Credentials(username, password);
			return this;
		}
		 
	}
	
	public static class ObjectImportConfiguration extends Configuration {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -1749977202658061221L;

		private ObjectImportConfiguration(){
			super();
			this.updateLastModificationTime = true; //Default value is true
		}
		
		public ObjectImportConfiguration version(boolean version){
			this.version = version;
			return this;
		}
		
		public ObjectImportConfiguration persist(PersistMode persistMode){
			this.persistMode = persistMode;
			return this;
		}

		public ObjectImportConfiguration updateLastModificationTime(boolean updateLastModificationTime){
			this.updateLastModificationTime = updateLastModificationTime;
			return this;
		}
		
		public ObjectImportConfiguration addBinaryContent(String binaryContentId, byte[] binaryContent){
			
			if (binaryContentId == null || binaryContentId.trim().length() == 0){
				return this;
			}
			
			if (this.binaryContent == null){
				this.binaryContent = new HashMap<String, byte[]>();
			}
			
			this.binaryContent.put(binaryContentId, binaryContent);
			
			
			return this;
		}

		public ObjectImportConfiguration addBinaryContent(Map<String, byte[]> binaryContent){
			
			if (binaryContent == null || binaryContent.isEmpty()){
				return this;
			}
			
			if (this.binaryContent == null){
				this.binaryContent = new HashMap<String, byte[]>();
			}
			
			this.binaryContent.putAll(binaryContent);
			
			return this;
		}
		
		public ObjectImportConfiguration saveMissingObjectReferences(boolean saveMissingObjectReferences){
			this.saveMissingObjectReferences = saveMissingObjectReferences;
			return this;
		}
		
		public ObjectImportConfiguration credentialsOfUserWithAccessToBinaryContent(String username, String password){
			this.credentialsOfUserWhoHasAccessToBinaryContent = new Credentials(username, password);
			return this;
		}


	}
	
	private ImportConfiguration(Configuration builder) {
		this.persistMode = builder.persistMode;
		this.binaryContent = builder.binaryContent;
		this.updateLastModificationTime = builder.updateLastModificationTime;
		this.version = builder.version;
		this.saveMissingObjectReferences = builder.saveMissingObjectReferences;
		this.credentialsOfUserWhoHasAccessToBinaryContent = builder.credentialsOfUserWhoHasAccessToBinaryContent;
	}

	
	public static ObjectImportConfiguration object(){
		return new ObjectImportConfiguration();
	}
	
	public static RepositoryImportConfiguration repository(){
		return new RepositoryImportConfiguration();
	}

	public static RepositoryUserImportConfiguration repositoryUser(){
		return new RepositoryUserImportConfiguration();
	}

	public static TaxonomyImportConfiguration taxonomy(){
		return new TaxonomyImportConfiguration();
	}
	
	public static TopicImportConfiguration topic(){
		return new TopicImportConfiguration();
	}
	
	public static SpaceImportConfiguration space(){
		return new SpaceImportConfiguration();
	}
	
	public PersistMode getPersistMode() {
		return persistMode;
	}

	public boolean isVersion() {
		return version;
	}

	public boolean isUpdateLastModificationTime() {
		return updateLastModificationTime;
	}

	public Map<String, byte[]> getBinaryContent() {
		return binaryContent;
	}

	public boolean saveMissingObjectReferences() {
		return saveMissingObjectReferences;
	}
	
	public Credentials credentialsOfUserWhoHasAccessToBinaryContent() {
		return credentialsOfUserWhoHasAccessToBinaryContent;
	}



	public static class Credentials implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2154087866791803405L;
		
		private final String username;
		private final String password;
		
		private Credentials(String username,String password){
			this.username = username;
			this.password = password;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
	}	
		
		
}
