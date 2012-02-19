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

package org.betaconceptframework.astroboa.engine.jcr.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.ValueFormatException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.betaconceptframework.astroboa.api.model.CmsRepositoryEntity;
import org.betaconceptframework.astroboa.api.model.ValueType;
import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.betaconceptframework.astroboa.engine.database.dao.CmsRepositoryEntityAssociationDao;
import org.betaconceptframework.astroboa.model.impl.ItemQName;
import org.betaconceptframework.astroboa.model.impl.SaveMode;

/**
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 *
 */
public class  EntityAssociationUpdateHelper<T extends CmsRepositoryEntity> extends EntityAssociationHelperBase<T> {

	public EntityAssociationUpdateHelper(
			Session session,
			CmsRepositoryEntityAssociationDao cmsRepositoryEntityAssociationDao,
			Context context) {

		super(session, cmsRepositoryEntityAssociationDao, context);

	}


	private Node referrerCmsRepositoryEntityNode;
	private Node referrerPropertyContainerNode;
	private ItemQName referrerPropertyName;
	private boolean isReferrerPropertyNameMultivalue;

	private Node referrerNodeWhichContainsProperty;


	//New Ids to be added
	private List<T> valuesToBeAdded;

	//Existing values
	private List<String> existingValues =  new ArrayList<String>();

	public void setValuesToBeAdded(List<T> valuesToBeAdded) {
		this.valuesToBeAdded = valuesToBeAdded;
	}

	public void update() throws RepositoryException{
		//Initiliaze ids and other variables which will be used through this update
		initialise();

		loadExistingValues();

		//Values to be added will replace existing values
		//Before that we must isolate all values that
		//must be removed from associations
		//and all values that should be added to associations
		Map<String,T> newValuesMapToBeAdded = new LinkedHashMap<String, T>();

		List<String> listOfNewIdsToBeAdded = new ArrayList<String>();
		
		if (CollectionUtils.isNotEmpty(valuesToBeAdded)){

			for (T valueToAdd : valuesToBeAdded){

				String idToBeAdded = valueToAdd.getId();

				if (StringUtils.isBlank(idToBeAdded))
					throw new CmsException("Cms Repository Entity does not have an id" );

				newValuesMapToBeAdded.put(idToBeAdded, valueToAdd);
				
				listOfNewIdsToBeAdded.add(idToBeAdded);
			}
		}

		ValueFactory valueFactory = session.getValueFactory();

		//Replace values in JCR Node
		if (isReferrerPropertyNameMultivalue){
			JcrNodeUtils.addMultiValueProperty(referrerNodeWhichContainsProperty, 
					referrerPropertyName, SaveMode.UPDATE, listOfNewIdsToBeAdded, ValueType.String, valueFactory);	
		}
		else{
			if (newValuesMapToBeAdded != null && newValuesMapToBeAdded.size() >1){
				throw new CmsException("Must update single value property "+referrerPropertyName.getJcrName()+ " in node "+
						referrerNodeWhichContainsProperty.getPath()+ " but found more than one values "+
						newValuesMapToBeAdded.toString());
			}

			JcrNodeUtils.addSimpleProperty(SaveMode.UPDATE, 
					referrerNodeWhichContainsProperty, referrerPropertyName, 
					CollectionUtils.isEmpty(listOfNewIdsToBeAdded)? null : listOfNewIdsToBeAdded.get(0), //Expecting at most one value 
							valueFactory, ValueType.String);
		}

	}

	private void initialise() throws RepositoryException {
		referrerNodeWhichContainsProperty = ((referrerPropertyContainerNode == null)? referrerCmsRepositoryEntityNode : referrerPropertyContainerNode); 
	}

	private void loadExistingValues() throws ValueFormatException,
	RepositoryException, PathNotFoundException {

		if (existingValues != null){
			existingValues.clear();
		}
		else{
			existingValues = new ArrayList<String>();
		}

		List<Value> existingValues = new ArrayList<Value>();

		if (referrerNodeWhichContainsProperty.hasProperty(referrerPropertyName.getJcrName())){
			if (isReferrerPropertyNameMultivalue){
				existingValues = Arrays.asList(referrerNodeWhichContainsProperty.getProperty(referrerPropertyName.getJcrName()).getValues());
			}
			else{
				Value existingValue = referrerNodeWhichContainsProperty.getProperty(referrerPropertyName.getJcrName()).getValue();
				existingValues.add(existingValue);
			}

			if (CollectionUtils.isNotEmpty(existingValues)){
				for (Value existingValue: existingValues)
					this.existingValues.add(existingValue.getString());
			}
		}
	}


	public void setReferrerCmsRepositoryEntityNode(
			Node referrerCmsRepositoryEntityNode) {
		this.referrerCmsRepositoryEntityNode = referrerCmsRepositoryEntityNode;
	}


	public void setReferrerPropertyContainerNode(Node referrerPropertyContainerNode) {
		this.referrerPropertyContainerNode = referrerPropertyContainerNode;
	}


	public void setReferrerPropertyName(ItemQName referrerPropertyName) {
		this.referrerPropertyName = referrerPropertyName;
	}


	public void setReferrerPropertyNameMultivalue(
			boolean isReferrerPropertyNameMultivalue) {
		this.isReferrerPropertyNameMultivalue = isReferrerPropertyNameMultivalue;
	}

}

