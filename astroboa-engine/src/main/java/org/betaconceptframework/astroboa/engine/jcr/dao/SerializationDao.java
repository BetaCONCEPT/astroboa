/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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


import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.api.model.io.FetchLevel;
import org.betaconceptframework.astroboa.api.model.io.SerializationConfiguration;
import org.betaconceptframework.astroboa.api.model.io.SerializationReport;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.api.model.query.criteria.ContentObjectCriteria;
import org.betaconceptframework.astroboa.context.AstroboaClientContext;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.PrototypeFactory;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean;
import org.betaconceptframework.astroboa.engine.jcr.io.SerializationBean.CmsEntityType;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryHandler;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsQueryResult;
import org.betaconceptframework.astroboa.engine.jcr.query.CmsScoreNodeIteratorUsingJcrRangeIterator;
import org.betaconceptframework.astroboa.model.impl.io.SerializationReportImpl;
import org.betaconceptframework.astroboa.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class SerializationDao {

	@Autowired
	private CmsQueryHandler cmsQueryHandler;
	
	@Autowired
	private SerializationBean serializationBean;
	
	//Use this method only if you want to use SerializationBean in a newly
	//created Thread
	@Autowired
	public PrototypeFactory prototypeFactory;
	
	//TODO : The use of the ExecutorService must be reviewed. 
	private	ExecutorService executorService = Executors.newCachedThreadPool();
	
	public long serializeSearchResults(Session session, CmsCriteria cmsCriteria, OutputStream os, FetchLevel fetchLevel, 
			SerializationConfiguration serializationConfiguration) throws Exception{

		CmsQueryResult cmsQueryResult = null;
		
		NodeIterator nodeIterator = null;
		
		 if (cmsCriteria instanceof ContentObjectCriteria){
			 
			 cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, cmsCriteria, false);
			 
			 nodeIterator = 
				new CmsScoreNodeIteratorUsingJcrRangeIterator(cmsQueryResult.getRowIterator());
		}
		else{
			cmsQueryResult = cmsQueryHandler.getNodesFromXPathQuery(session, cmsCriteria, true);
			nodeIterator = cmsQueryResult.getNodeIterator();
		}

		 serializationBean.serializeNodesAsResourceCollection(nodeIterator, os, cmsCriteria, fetchLevel, serializationConfiguration, cmsQueryResult.getTotalRowCount());
		 
		 return nodeIterator.getSize();

	}

	public void serializeCmsRepositoryEntity(Node nodeRepresentingEntity, OutputStream os, CmsEntityType entityTypeToSerialize, 
			List<String> propertyPathsWhoseValuesAreIncludedInTheSerialization, FetchLevel fetchLevel, boolean nodeRepresentsRootElement, 
			SerializationConfiguration serializationConfiguration) throws Exception{

		serializationBean.serializeNode(nodeRepresentingEntity, os, entityTypeToSerialize, serializationConfiguration, propertyPathsWhoseValuesAreIncludedInTheSerialization, fetchLevel, nodeRepresentsRootElement);
		
	}


	public Future<SerializationReport> serializeAllInstancesOfEntity(final CmsEntityType entityTypeToSerialize, final SerializationConfiguration serializationConfiguration) {
		
		Calendar now = Calendar.getInstance();
		
		final String serializationPath = DateUtils.format(now, "yyyy/MM/dd");
		
		final AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		final String filename = createFilename(now, entityTypeToSerialize, clientContext);
		
		final SerializationBean serializationBean = prototypeFactory.newSerializationBean();
		
		final SerializationReport serializationReport = new SerializationReportImpl(filename+".zip", serializationPath);
		
		Future<SerializationReport> future = executorService.submit(new Callable<SerializationReport>() {

			@Override
			public SerializationReport call() throws Exception {
				serializationBean.serialize(entityTypeToSerialize, serializationPath, filename, clientContext, serializationReport, serializationConfiguration);
				return serializationReport;
			}

		});
		
		
		return future;
	}
	
	public Future<SerializationReport> serializeObjectsUsingCriteria(final ContentObjectCriteria contentObjectCriteria, final SerializationConfiguration serializationConfiguration) {
		
		Calendar now = Calendar.getInstance();
		
		final String serializationPath = DateUtils.format(now, "yyyy/MM/dd");
		
		final AstroboaClientContext clientContext = AstroboaClientContextHolder.getActiveClientContext();
		
		final String filename = createFilename(now, CmsEntityType.OBJECT, clientContext);
		
		final SerializationBean serializationBean = prototypeFactory.newSerializationBean();
		
		final SerializationReport serializationReport = new SerializationReportImpl(filename+".zip", serializationPath);
		

		Future<SerializationReport> future = executorService.submit(new Callable<SerializationReport>() {

			@Override
			public SerializationReport call() throws Exception {
				
				serializationBean.serializeObjectsUsingCriteria(contentObjectCriteria, serializationPath, filename, clientContext, serializationReport, serializationConfiguration);
				
				return serializationReport;
			}

		});
		
		
		return future;
	}
	
	public  String createFilename(Calendar date, CmsEntityType entityTypeToSerialize, AstroboaClientContext clientContext){

		if (clientContext == null)
		{
			throw new CmsException("Astroboa client context is not provided. Serialization failed");
		}

		
		/*	Build serialization file path
		 * 
		 *  yyyy/MM/dd/<repositoryId>-{repositoryUsers|taxonomies|objects|organizationSpace-}yyyyMMddHHmmssSSS
		 *  
		 */
		
		String repositoryId = clientContext.getRepositoryContext().getCmsRepository().getId();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(repositoryId);
		
		if (entityTypeToSerialize != null)
		{
			switch (entityTypeToSerialize) {
			case OBJECT:
				sb.append("-objects");
				break;
			case ORGANIZATION_SPACE:
				sb.append("-organizationSpace");
				break;
			case REPOSITORY_USER:
				sb.append("-repositoryUsers");
				break;
			case TAXONOMY:
				sb.append("-taxonomies");
				break;
			default:
				break;
			}
		}
		
		sb.append("-");
		sb.append(DateUtils.format(date, "yyyyMMddHHmmssSSS"));
		
		return sb.toString();
		
	}
}
