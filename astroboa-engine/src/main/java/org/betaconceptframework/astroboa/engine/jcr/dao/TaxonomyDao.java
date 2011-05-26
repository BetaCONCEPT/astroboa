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
package org.betaconceptframework.astroboa.engine.jcr.dao;


import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.XMLChar;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.ResourceRepresentationType;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;
import org.betaconceptframework.astroboa.api.model.query.criteria.TaxonomyCriteria;
import org.betaconceptframework.astroboa.engine.jcr.io.ImportMode;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.renderer.TaxonomyRenderer;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsLocalizationUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.CmsRepositoryEntityUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.engine.jcr.util.TopicUtils;
import org.betaconceptframework.astroboa.model.factory.CmsCriteriaFactory;
import org.betaconceptframework.astroboa.model.impl.SaveMode;
import org.betaconceptframework.astroboa.model.impl.item.CmsBuiltInItem;
import org.betaconceptframework.astroboa.model.impl.query.CmsOutcomeImpl;
import org.betaconceptframework.astroboa.model.impl.query.xpath.XPathUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class TaxonomyDao extends JcrDaoSupport{

	private final Logger logger = LoggerFactory.getLogger(TaxonomyDao.class);

	@Autowired
	private TaxonomyRenderer taxonomyRenderer;

	@Autowired
	private CmsLocalizationUtils cmsLocalizationUtils;

	@Autowired
	private TopicUtils topicUtils;

	@Autowired
	private TopicDao topicDao;

	@Autowired
	private CmsRepositoryEntityUtils cmsRepositoryEntityUtils;

	@Autowired
	private SerializationDao serializationDao;
	
	@Autowired
	private ImportDao importDao;

	@Autowired
	private CmsQueryHandler cmsQueryHandler;


	public boolean deleteTaxonomy(String taxonomyIdOrName) {
		if (StringUtils.isBlank(taxonomyIdOrName))
			throw new CmsException("Undefined taxonomy id or name");

		Session session = null;

		Context context = null;
		try {
			
			session = getSession();


			//Retrieve taxonomy node with provided identifier
			//Node taxonomyNode =	cmsRepositoryEntityUtils.retrieveUniqueNodeForCmsRepositoryEntityId(session, taxonomyId);

			Node taxonomyNode = getTaxonomyNodeByIdOrName(session, taxonomyIdOrName);
			if (taxonomyNode == null){
				throw new CmsException("Could not find taxonomy with id or name "+taxonomyIdOrName+ " in order to delete it");
			}

			//Check taxonomy node found is a custom taxonomy and not a built in folksonomy
			boolean taxonomyIsAReservedTaxonomy = isTaxonomyNameAReservedName(taxonomyNode.getName());

			if (taxonomyIsAReservedTaxonomy){
				throw new CmsException("Taxonomy "+ taxonomyNode.getName()+ " is a reserved taxonomy and cannot be deleted");
			}

			//Taxonomies parent node
			Node taxonomyRootNode = JcrNodeUtils.getTaxonomyRootNode(session);

			//Its parent must be taxonomy root node
			if (! taxonomyNode.getParent().getPath().equals(taxonomyRootNode.getPath())){
				throw new CmsException("Taxonomy "+ taxonomyNode.getName()+ " does not correspond to a valid taxonomy");
			}

			context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, session);
			
			//Delete each root topic
			NodeIterator rootTopics = taxonomyNode.getNodes(CmsBuiltInItem.Topic.getJcrName());
			while (rootTopics.hasNext()){
				topicUtils.removeTopicJcrNode(rootTopics.nextNode(), session, false, context);
			}

			taxonomyNode.remove();

			session.save();
			return true;
		}
		catch (Throwable e) {
			throw new CmsException(e);
		}
		finally{
			if (context != null){
				context.dispose();
				context = null;
			}
		}
	}

	private boolean isTaxonomyNameAReservedName(String taxonomyName){
		//Check if name is a reserved name
		return Taxonomy.SUBJECT_TAXONOMY_NAME.equalsIgnoreCase(taxonomyName) ||
		Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME.equalsIgnoreCase(taxonomyName) ; 
	}

	public Taxonomy saveTaxonomy(Object taxonomySource) {

		if (taxonomySource == null){
			throw new CmsException("Cannot save an empty Taxonomy !");
		}
		
		if (taxonomySource instanceof String){
			//Use importer to unmarshal String to Taxonomy
			//and to save it as well.
			//What is happened is that importDao will create a Taxonomy
			//and will pass it here again to save it. 
			return importDao.importTaxonomy((String)taxonomySource, ImportMode.SAVE_ENTITY_TREE);
		}
		
		if (! (taxonomySource instanceof Taxonomy)){
			throw new CmsException("Expecting either String or Taxonomy and not "+taxonomySource.getClass().getName());
		}
		
		Taxonomy taxonomy = (Taxonomy) taxonomySource;

		if (StringUtils.isBlank(taxonomy.getName())){
			throw new CmsException("Undefined taxonomy name");
		}
		
		//Check if this taxonomy is built in and change its id
		changeIdentifierIfTaxonomyIsBuiltIn(taxonomy, Taxonomy.SUBJECT_TAXONOMY_NAME);

		String taxonomyName = taxonomy.getName();

		boolean taxonomyNameIsAReservedName = isTaxonomyNameAReservedName(taxonomyName);


		//Check taxonomy name
		if (!taxonomyNameIsAReservedName){
			if (! XMLChar.isValidNCName(taxonomyName)){
				throw new CmsException("Taxonomy name "+ taxonomyName+" is not a valid XML name.Check XML Namespaces recommendation [4]");
			}
			else if (! cmsRepositoryEntityUtils.isValidSystemName(taxonomyName)){
				throw new CmsException("Taxonomy name '"+taxonomyName+"' is not valid. It should match pattern "+CmsConstants.SYSTEM_NAME_REG_EXP);
			} 
		}

		Session session = null;

		SaveMode saveMode = null;
		try {

			//Check if taxonomy is new
			saveMode = StringUtils.isBlank(taxonomy.getId())? SaveMode.INSERT : SaveMode.UPDATE_ALL;

			session = getSession();

			//Retrieve taxonomy Jcr root node
			Node taxonomyRootJcrNode = JcrNodeUtils.getTaxonomyRootNode(session);

			Node taxonomyJcrNode = null;

			switch (saveMode) {
			case INSERT:

				throwExceptionIfTaxonomyNameExists(taxonomy,taxonomyRootJcrNode);

				//
				if (taxonomyNameIsAReservedName){
					throw new CmsException("Cannot save new taxonomy with reserved name "+ taxonomy.getName());
				}
				//Add node
				taxonomyJcrNode = taxonomyRootJcrNode.addNode(taxonomyName, CmsBuiltInItem.Taxonomy.getJcrName());

				cmsRepositoryEntityUtils.createCmsIdentifier(taxonomyJcrNode, taxonomy, false);

				break;
			case UPDATE_ALL:

				if (Taxonomy.SUBJECT_TAXONOMY_NAME.equalsIgnoreCase(taxonomyName)){
					taxonomyJcrNode = taxonomyRootJcrNode.getNode(Taxonomy.SUBJECT_TAXONOMY_NAME);
				}
				else{
					taxonomyJcrNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForCmsRepositoryEntity(session, taxonomy);
				}

				if (taxonomyJcrNode == null){
					if (taxonomyNameIsAReservedName){
						throw new CmsException("Try to save reserved taxonomy "+taxonomyName + " with invalid identifier "+taxonomy.getId());
					}

					//A new Taxonomy for this repository. Use provided id to create a new taxonomy Node

					//Check if taxonomy name already exists
					//That means that the same taxonomy exists with different identifier
					if (taxonomyRootJcrNode.hasNode(taxonomyName)){
						if (cmsRepositoryEntityUtils.hasCmsIdentifier(taxonomyRootJcrNode.getNode(taxonomyName))){
							String taxonomyJcrNodeIdentifier = cmsRepositoryEntityUtils.getCmsIdentifier(taxonomyRootJcrNode.getNode(taxonomyName));
							
							if (StringUtils.equals(taxonomyJcrNodeIdentifier, taxonomy.getId()) ){
									 logger.warn("Although internal query returned no results and JCR node with id "+taxonomy.getId() + " " +
									 		" a jcr node with the same name with taxonomy ("+taxonomyName+") and the same identifier ("+
									 		taxonomy.getId()+") was found. This JCR node will be used a taxonomy JCR node");
									 
									 taxonomyJcrNode = taxonomyRootJcrNode.getNode(taxonomyName);
							}
							else
							{
								throw new CmsException("Taxonomy "+ taxonomyName + " already exists with different identifier "+
										taxonomyJcrNodeIdentifier +" than the one provided "+ taxonomy.getId());
							}
						}
						else{
							throw new CmsException("Taxonomy "+ taxonomyName + " already exists with no identifier "+
										" where as taxonomy to be saved already has one "+ taxonomy.getId());
						}
					}
					else{

						taxonomyJcrNode = taxonomyRootJcrNode.addNode(taxonomyName, CmsBuiltInItem.Taxonomy.getJcrName());
						
						cmsRepositoryEntityUtils.createCmsIdentifier(taxonomyJcrNode, taxonomy, true);
					}

				}

				//Parent node of taxonomy jcr must either be RepositoryUser
				//or TaxonomyRoot
				if (taxonomyJcrNode.getParent() == null || 
						( !taxonomyJcrNode.getParent().isNodeType(CmsBuiltInItem.RepositoryUser.getJcrName()) && 
								!taxonomyJcrNode.getParent().isNodeType(CmsBuiltInItem.TaxonomyRoot.getJcrName())))
				{
					throw new CmsException("Taxonomy "+ taxonomyName+ " does not correspond to a valid taxonomy jcr node "+ taxonomyJcrNode.getPath());
				}

				//Update Name only if taxonomy is not the same with previous name
				//If name is different than the one existed check its uniqueness
				//If valid rename taxonomy node
				if (!taxonomyJcrNode.getName().equals(taxonomyName)){
					//Check that existing name is not subject's taxonomy
					if (isTaxonomyNameAReservedName(taxonomyJcrNode.getName())){
						throw new CmsException("Renamimg "+taxonomyJcrNode.getName()+" is prohibited");
					}

					throwExceptionIfTaxonomyNameExists(taxonomy,taxonomyRootJcrNode);

					//Move taxonomyNode (i.e change its name) only if that name is not astroboa system taxonomy name
					if (!taxonomyNameIsAReservedName){
						session.move(taxonomyJcrNode.getPath(), taxonomyRootJcrNode.getPath()+CmsConstants.FORWARD_SLASH+taxonomyName);
					}
				}


				break;

			default:
				break;
			}

			//Update localized labels
			if (taxonomyJcrNode != null){
				cmsLocalizationUtils.updateCmsLocalization(taxonomy, taxonomyJcrNode);

				cmsRepositoryEntityUtils.setSystemProperties(taxonomyJcrNode, taxonomy);
			}
			
			//in cases where taxonomy is a new one and contains root topics save them as well
			if (SaveMode.INSERT == saveMode){
				saveRootTopics(taxonomy);
			}

			session.save();

			return taxonomy;
		}
		catch(CmsException e){
			throw e;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}

	}

	private void throwExceptionIfTaxonomyNameExists(Taxonomy taxonomy,
			Node taxonomyRootNode) throws RepositoryException {
		if (Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME.equals(taxonomy.getName()) || 
				taxonomyRootNode.hasNode(taxonomy.getName())){
			throw new CmsException("Taxonomy name "+ taxonomy.getName() + " already exists");
		}
	}

	public  Node getTaxonomyJcrNode(Session session, String taxonomyName, boolean throwExceptionIfNotFound) throws  RepositoryException {
		
		final String relativeTaxonomyPath = XPathUtils.getRelativeTaxonomyPath(taxonomyName, false);
		
		try{
			return session.getRootNode().getNode(relativeTaxonomyPath);
		}
		catch(Exception e){
			if (throwExceptionIfNotFound){
				if (! (e instanceof RepositoryException)){
					throw new RepositoryException(e);
				}
				else{
					throw (RepositoryException)e;
				}
			}
			
			return null;
		}
		
	}

	private void changeIdentifierIfTaxonomyIsBuiltIn(Taxonomy taxonomyToBeSaved, String taxonomyName) {
		if (taxonomyName.equals(taxonomyToBeSaved.getName())){
			try{
				Node taxonomyRootNode = JcrNodeUtils.getTaxonomyRootNode(getSession());

				//It may be the case the built in taxonomy does not yet exist
				if (taxonomyRootNode.hasNode(taxonomyName)){
					Node taxonomyNode = taxonomyRootNode.getNode(taxonomyName);

					if (cmsRepositoryEntityUtils.hasCmsIdentifier(taxonomyNode)){
						String subjectTaxonomyIdentifier = cmsRepositoryEntityUtils.getCmsIdentifier(taxonomyNode);

						taxonomyToBeSaved.setId(subjectTaxonomyIdentifier);
					}
					else {
						throw new CmsException("Could not locate identifier for "+taxonomyName+" in node "+
								taxonomyNode.getPath());
					}
				}
			}catch(Exception e){
				throw new CmsException(e);
			}
		}
	}
	
	public Taxonomy saveTaxonomyTree(Taxonomy taxonomyToBeSaved) {
		
		taxonomyToBeSaved = saveTaxonomy(taxonomyToBeSaved);

		saveRootTopics(taxonomyToBeSaved);
		
		return taxonomyToBeSaved;
	}

	private void saveRootTopics(Taxonomy taxonomyToBeSaved) {
		//Save its root topics
		if (taxonomyToBeSaved.isRootTopicsLoaded()){
			
			List<Topic> rootTopics = taxonomyToBeSaved.getRootTopics();
			if (CollectionUtils.isNotEmpty(rootTopics)){

				Context context = new Context(cmsRepositoryEntityUtils, cmsQueryHandler, getSession());
				for (Topic rootTopic : rootTopics){
					//Set saved taxonomy in case
					if (rootTopic.getTaxonomy() == null 
						||	rootTopic.getTaxonomy().getId() == null
						||  rootTopic.getTaxonomy().getName() == null  
						|| taxonomyToBeSaved.getName().equals(rootTopic.getTaxonomy().getName())){
						rootTopic.setTaxonomy(taxonomyToBeSaved);
					}
					rootTopic = topicDao.saveTopic(rootTopic, context);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getTaxonomy(String taxonomyIdOrName, ResourceRepresentationType<T> taxonomyOutput, FetchLevel fetchLevel, boolean prettyPrint) {
		
		if (StringUtils.isBlank(taxonomyIdOrName)){
			return null;
		}

		if (! CmsConstants.UUIDPattern.matcher(taxonomyIdOrName).matches() &&
				Taxonomy.REPOSITORY_USER_FOLKSONOMY_NAME.equals(taxonomyIdOrName)){
			throw new CmsException("Use RepositoryUser.getFolksonomy() to retrieve a folskonomy");
		}

		ByteArrayOutputStream os = null;
		
		try {

			Session session = getSession();
			
			Node taxonomyNode = getTaxonomyNodeByIdOrName(session, taxonomyIdOrName);

			if (taxonomyNode == null){
				return null;
			}
			
			if (taxonomyOutput == null || ResourceRepresentationType.TAXONOMY_INSTANCE.equals(taxonomyOutput)|| 
					ResourceRepresentationType.TAXONOMY_LIST.equals(taxonomyOutput)){
				

				Taxonomy taxonomy = taxonomyRenderer.renderTaxonomy(taxonomyNode, null, cmsRepositoryEntityFactoryForActiveClient.newTaxonomy());

				//Pre fetch children or tree
				if (fetchLevel != null){
					
					switch (fetchLevel) {
					case ENTITY_AND_CHILDREN:
						//This way lazy loading will be enabled
						taxonomy.getRootTopics();
						break;
					case FULL:
						loadAllChildren(taxonomy.getRootTopics());
						break;
					default:
						break;
					}
					
				}
				
				//Return appropriate type
				if (taxonomyOutput == null || ResourceRepresentationType.TAXONOMY_INSTANCE.equals(taxonomyOutput) ){
					return (T) taxonomy;
				}
				else{
					//Return type is CmsOutcome.
					CmsOutcome<Taxonomy> outcome = new CmsOutcomeImpl<Taxonomy>(1, 0, 1);
					outcome.getResults().add(taxonomy);
					
					return (T) outcome;
				}
				
			}
			else if (ResourceRepresentationType.XML.equals(taxonomyOutput)|| 
					ResourceRepresentationType.JSON.equals(taxonomyOutput)){

				String taxonomy = null;
				
				os = new ByteArrayOutputStream();

				serializationDao.serializeCmsRepositoryEntity(taxonomyNode, os, taxonomyOutput, CmsEntityType.TAXONOMY, null, fetchLevel, true, false,prettyPrint);

				taxonomy = new String(os.toByteArray(), "UTF-8");

				return (T) taxonomy;
			}
			else{
				throw new CmsException("Invalid resource representation type for taxonomy "+taxonomyOutput);
			}


		}catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}
	}

	private void loadAllChildren(List<Topic> children) {
		
		if (children != null && ! children.isEmpty()){
			for (Topic child : children){
				loadAllChildren(child.getChildren());
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	public <T> T serializeAllTaxonomies(ResourceRepresentationType<T> taxonomyOutput, FetchLevel fetchLevel, boolean prettyPrint) {
		
		ByteArrayOutputStream os = null;
		
		try {

			Session session = getSession();
			
			if (taxonomyOutput == null){
				taxonomyOutput = (ResourceRepresentationType<T>) ResourceRepresentationType.TAXONOMY_LIST;
			}
			
			if (ResourceRepresentationType.TAXONOMY_INSTANCE.equals(taxonomyOutput)|| 
					ResourceRepresentationType.TAXONOMY_LIST.equals(taxonomyOutput)){
				
				Node taxonomyRootNode = JcrNodeUtils.getTaxonomyRootNode(session);
				
				if (! taxonomyRootNode.hasNodes()){
					if (ResourceRepresentationType.TAXONOMY_INSTANCE.equals(taxonomyOutput)){
						return null;
					}
					else {
						return (T) new CmsOutcomeImpl<Taxonomy>(0, 0, 0);
					}
				}
				
				NodeIterator taxonomyNodes = taxonomyRootNode.getNodes();
				
				if (taxonomyNodes.getSize() > 1 && ResourceRepresentationType.TAXONOMY_INSTANCE.equals(taxonomyOutput)){
					throw new CmsException("Invalid taxonomy output. Found more than one taxonomies. Cannot return a single Taxonomy instance");
				}
				
				CmsOutcome<Taxonomy> outcome = new CmsOutcomeImpl<Taxonomy>(taxonomyNodes.getSize(), 0, (int)taxonomyNodes.getSize());
				
				while (taxonomyNodes.hasNext()){
					
					Node taxonomyNode = taxonomyNodes.nextNode();
					
					Taxonomy taxonomy = taxonomyRenderer.renderTaxonomy(taxonomyNode, null, cmsRepositoryEntityFactoryForActiveClient.newTaxonomy());
					
					if (fetchLevel != null){
						switch (fetchLevel) {
						case ENTITY_AND_CHILDREN:
							//This way lazy loading will be enabled
							taxonomy.getRootTopics();
							break;
						case FULL:
							loadAllChildren(taxonomy.getRootTopics());
							break;
						default:
							break;
						}
					}
					outcome.getResults().add(taxonomy);
				}

				return (T) outcome;
				
			}
			else if (ResourceRepresentationType.XML.equals(taxonomyOutput)|| 
					ResourceRepresentationType.JSON.equals(taxonomyOutput)){

				os = new ByteArrayOutputStream();
			
				TaxonomyCriteria taxonomyCriteria = CmsCriteriaFactory.newTaxonomyCriteria();
				taxonomyCriteria.getRenderProperties().prettyPrint(prettyPrint);
				
				serializationDao.serializeSearchResults(session, taxonomyCriteria, os, fetchLevel, taxonomyOutput, false);
			
				return (T) new String(os.toByteArray(), "UTF-8");
			}
			else{
				throw new CmsException("Invalid resource representation type for taxonomy search results "+taxonomyOutput);
			}

			
		}
		catch(RepositoryException ex){
			throw new CmsException(ex);
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
		finally{
			IOUtils.closeQuietly(os);
		}
	}

	public Node getTaxonomyNodeByIdOrName(Session session, String taxonomyIdOrName){
		try{
			Node taxonomyNode = null;
			
			if (CmsConstants.UUIDPattern.matcher(taxonomyIdOrName).matches()){
				taxonomyNode = cmsRepositoryEntityUtils.retrieveUniqueNodeForTaxonomy(session, taxonomyIdOrName);

				if (taxonomyNode != null){
					return taxonomyNode;
				}
			}
			else{
				return getTaxonomyJcrNode(session, taxonomyIdOrName, false);
			}
			
			return null;
		}
		catch (Exception e) {
			throw new CmsException(e);
		}
	}

}
