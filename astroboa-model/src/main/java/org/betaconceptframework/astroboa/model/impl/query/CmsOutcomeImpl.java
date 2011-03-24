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

package org.betaconceptframework.astroboa.model.impl.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.query.CmsOutcome;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class CmsOutcomeImpl<T> implements CmsOutcome<T>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4065252300099865391L;
	private long count;
	private List<T> results;
	private int offset;
	private int limit;

	private Map<String, Double> resultRanking = new HashMap<String, Double>();
	
	public CmsOutcomeImpl(long count, int offset, int limit) {
		super();
		this.count = count;
		this.offset = offset;
		this.limit = limit;
		results = new ArrayList<T>();
	}
	
	public long getCount() {
		return count;
	}
	public List<T> getResults() {
		return results;
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}
	
	//When security is enabled, it may be the case the the actual count
	//of results is smaller than the initial estimate which was made
	//prior to enforce security rules.
	//This method is used to correct that initial estimate
	public void setCount(long count){
		this.count = count;
	}
	
	public void addResult(T entity, double ranking){
		if (entity != null){
			results.add(entity);

			if (entity instanceof CmsRepositoryEntity){
				resultRanking.put(((CmsRepositoryEntity)entity).getId(), ranking);
			}
			else{
				resultRanking.put(entity.toString(), ranking);
			}
		}
	}
	
	public double getRanking(T entity){
		if (entity == null){
			return 0;
		}
		
		if (entity instanceof CmsRepositoryEntity){
			return resultRanking.get(((CmsRepositoryEntity)entity).getId());
		}
		else{
			return resultRanking.get(entity.toString());
		}
		
	}
	
}
