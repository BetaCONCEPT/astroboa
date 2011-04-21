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
package org.betaconceptframework.astroboa.engine.jcr.visitor;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.definition.ComplexCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.definition.ContentObjectTypeDefinition;
import org.betaconceptframework.astroboa.api.model.definition.SimpleCmsPropertyDefinition;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.commons.visitor.AbstractCmsPropertyDefinitionVisitor;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.util.Context;
import org.betaconceptframework.astroboa.engine.jcr.util.EntityAssociationUpdateHelper;
import org.betaconceptframework.astroboa.engine.jcr.util.JcrNodeUtils;
import org.betaconceptframework.astroboa.model.impl.item.ItemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class ComplexCmsPropertyNodeRemovalVisitor extends AbstractCmsPropertyDefinitionVisitor{

	private final Logger logger = LoggerFactory.getLogger(ComplexCmsPropertyNodeRemovalVisitor.class);

	private Node parentNode;

	private List<Node> childrenNodesToBeRemoved = new ArrayList<Node>();

	private Node contentObjectNode;

	private Session session;

	@Autowired
	private PrototypeFactory prototypeFactory;
	
	@Autowired
	private CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao;

	private Context context;
	
	public ComplexCmsPropertyNodeRemovalVisitor(){
		setVisitType(VisitType.Children);
	}

	public void setParentNode(Node parentNode, Node contentObjectNode) throws RepositoryException {

		this.parentNode = parentNode;

		//Load Content Object Node
		if (contentObjectNode == null){
			this.contentObjectNode = JcrNodeUtils.getContentObjectNode(this.parentNode);
		}
		else{
			this.contentObjectNode = contentObjectNode;
		}

	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	public void setContext(Context context){
		this.context = context;
	}

	public void visit(ContentObjectTypeDefinition contentObjectTypeDefinition)  {

	}

	public void visitComplexPropertyDefinition(ComplexCmsPropertyDefinition complexPropertyDefinition) {
		if (CollectionUtils.isNotEmpty(childrenNodesToBeRemoved)){
			try{

				ComplexCmsPropertyNodeRemovalVisitor childNodeRemovalVisitor = prototypeFactory.newComplexCmsPropertyNodeRemovalVisitor();
				childNodeRemovalVisitor.setSession(session);


				for (Node childNode: childrenNodesToBeRemoved){
					
					if (logger.isDebugEnabled()){
						logger.debug("Clearing tree of child node {} of parent node {}", complexPropertyDefinition.getName(), childNode.getPath());
					}
					
					childNodeRemovalVisitor.setParentNode(childNode,contentObjectNode);

					childNodeRemovalVisitor.loadChildNodesToBeDeleted(complexPropertyDefinition.getName());

					complexPropertyDefinition.accept(childNodeRemovalVisitor);

				}
			}catch(Exception e){
				throw new CmsException(e);
			}
		}
	}

	public <T> void visitSimplePropertyDefinition(SimpleCmsPropertyDefinition<T> simplePropertyDefinition) {

		//Update values and references only if there are child nodes and simple property definition
		//is one of the following Cms Repository Entities
		if (CollectionUtils.isNotEmpty(childrenNodesToBeRemoved) && 
				(simplePropertyDefinition.getValueType() == ValueType.ObjectReference ||
						simplePropertyDefinition.getValueType() == ValueType.TopicReference )){

			EntityAssociationUpdateHelper<CmsRepositoryEntity> associationUpdateHelper = 
				new EntityAssociationUpdateHelper<CmsRepositoryEntity>(session,cmsRepositoryEntityAssociationDao, context);
			
			associationUpdateHelper.setReferrerCmsRepositoryEntityNode(contentObjectNode);
			associationUpdateHelper.setReferrerPropertyName(ItemUtils.createSimpleItem(simplePropertyDefinition.getName()));
			associationUpdateHelper.setReferrerPropertyNameMultivalue(simplePropertyDefinition.isMultiple());

			for (Node childNode: childrenNodesToBeRemoved)
			{
				try {
					if (childNode.hasProperty(simplePropertyDefinition.getName())){
						
						if (logger.isDebugEnabled()){
							logger.debug("Clearing values of property {} of node {}", simplePropertyDefinition.getName(), childNode.getPath());
						}

						associationUpdateHelper.setReferrerPropertyContainerNode(childNode);
						associationUpdateHelper.update();
					}
				} catch (RepositoryException e) {
					throw new CmsException(e);
				}
			}
		}

	}

	public void addChildNodeToBeDeleted(Node childNodeToBeDeleted) {
		childrenNodesToBeRemoved.clear();
		
		if (childNodeToBeDeleted != null){
			childrenNodesToBeRemoved.add(childNodeToBeDeleted);
		}
	}
	
	public void loadChildNodesToBeDeleted(String nameOfChildNodesToBeDeleted) {
		childrenNodesToBeRemoved.clear();

		//Load all nodes for this definition
		try {
			if (parentNode.hasNode(nameOfChildNodesToBeDeleted))
			{
				NodeIterator childNodeIterator = parentNode.getNodes(nameOfChildNodesToBeDeleted);
				while (childNodeIterator.hasNext())
					childrenNodesToBeRemoved.add(childNodeIterator.nextNode());
			}
		} catch (RepositoryException e) {
			throw new CmsException(e);
		}

	}


}
