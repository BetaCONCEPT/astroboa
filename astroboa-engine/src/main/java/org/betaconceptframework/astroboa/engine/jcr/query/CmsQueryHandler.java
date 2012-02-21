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

package org.betaconceptframework.astroboa.engine.jcr.query;


import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.betaconceptframework.astroboa.api.model.query.criteria.CmsCriteria;
import org.betaconceptframework.astroboa.context.AstroboaClientContextHolder;
import org.betaconceptframework.astroboa.engine.jcr.util.JackrabbitDependentUtils;
import org.betaconceptframework.astroboa.util.CmsConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsQueryHandler  {

	private  final Logger logger = LoggerFactory.getLogger(CmsQueryHandler.class);

	public CmsQueryResult getNodesFromXPathQuery(Session session, String xpathQuery,int offset, int limit) throws  RepositoryException{
		return getNodesFromXPathQuery(session, xpathQuery, offset, limit, true);
	}
	
	public CmsQueryResult getNodesFromXPathQuery(Session session, String xpathQuery,int offset, int limit, boolean retrieveNodeIterator) throws  RepositoryException{
		return getNodesFromQuery(session, xpathQuery, offset, limit, Query.XPATH, retrieveNodeIterator);
	}
	
	private CmsQueryResult getNodesFromQuery(Session session, String xpathQuery, int offset, int limit, String queryFormat, boolean retrieveNodeIterator) throws  RepositoryException{
		try{
			QueryManager queryManager = session.getWorkspace().getQueryManager();

			//Create Query object
			if (StringUtils.isBlank(queryFormat) || ( ! Query.XPATH.equals(queryFormat) && !Query.SQL.equals(queryFormat)))
				queryFormat = Query.XPATH;
			
			Query query = queryManager.createQuery(xpathQuery, queryFormat);

			//Inform query about offset and limit
			setOffsetAndLimitToQuery(query, offset, limit);

			//Run query
			QueryResult queryResultForAllNodes = executeQuery(query);

			//Get total number of nodes.
			//It does not matter if any offset or limit are set, Jackrabbit knows
			//how many nodes match criteria
			int totalNumberOfNodesMatchingQueryCriteria = JackrabbitDependentUtils.getTotalNumberOfRowsForQueryResult(queryResultForAllNodes);
			
			if (logger.isDebugEnabled()){
				logger.debug("Found {} nodes matching criteria. Offset :{}, Limit : {}",
						new Object[]{totalNumberOfNodesMatchingQueryCriteria, offset,limit});
				
				JackrabbitDependentUtils.logCacheManagerSettings(logger, session.getRepository());
			}
			
			//Wrap results
			if (retrieveNodeIterator){
				return new CmsQueryResult(totalNumberOfNodesMatchingQueryCriteria, queryResultForAllNodes.getNodes());
			}
			else{
				return new CmsQueryResult(totalNumberOfNodesMatchingQueryCriteria, queryResultForAllNodes.getRows());
			}

		}
		catch(RepositoryException e){
			logger.error("Exception thrown by executing the following query {}",xpathQuery);
			throw e;
		}

	}


	private QueryResult executeQuery(Query query) throws RepositoryException {
		long start = System.currentTimeMillis();

		QueryResult allNodes = query.execute();

		if (logger.isDebugEnabled()){

			long queryExecutionTime = System.currentTimeMillis() - start;
			
			logger.debug("Repository {} : Executed in {} secs, query : {}",
					new Object[]{AstroboaClientContextHolder.getActiveRepositoryId(), 
					DurationFormatUtils.formatDuration(queryExecutionTime, "ss.SSSS"), 
					query.getStatement()});
		}
		
		return allNodes;

	}

	public  CmsQueryResult getNodesFromXPathQuery(Session session, CmsCriteria cmsCriteria, int offset, int limit) throws  RepositoryException{
		return getNodesFromXPathQuery(session, cmsCriteria, offset, limit, true);
	}
	
	public  CmsQueryResult getNodesFromXPathQuery(Session session, CmsCriteria cmsCriteria, int offset, int limit, boolean retrieveNodeIterator) throws  RepositoryException{
		
		String pathQuery = cmsCriteria.getXPathQuery();
		
		return getNodesFromXPathQuery(session, pathQuery, offset, limit, retrieveNodeIterator);
	}

	public CmsQueryResult getNodesFromXPathQuery(Session session,
			CmsCriteria cmsCriteria) throws RepositoryException {
		return getNodesFromXPathQuery(session, cmsCriteria, true);
	}
	
	public CmsQueryResult getNodesFromXPathQuery(Session session,
			CmsCriteria cmsCriteria, boolean retrieveNodeIterator) throws RepositoryException {
		return getNodesFromXPathQuery(session, cmsCriteria, cmsCriteria.getOffset(), cmsCriteria.getLimit(), retrieveNodeIterator);
	}

	public void setOffsetAndLimitToQuery(Query query, int offset, int limit) {

		query.setOffset(offset);
		
		if (limit > 0){
			query.setLimit(limit);
		}
		else if (limit == 0){
			//Jackrabbit uses limit only if this is greater than 0
			//In all other cases it fetches all results.
			//In Astroboa, however, limit 0 denotes that no result should be rendered, 
			//that is only result count is needed.
			//So in order to 'limit' Jackrabbit to bring the minimum results possible
			//we set the limit to 1. This way Jackrabbit will only bring one result
			//which is acceptable since Astroboa requires no result to be fetched.
			//Total result count is not affected at all by limit
			query.setLimit(1);
		}
	}


}
