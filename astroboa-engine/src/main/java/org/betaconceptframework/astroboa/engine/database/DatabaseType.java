/*
 * Copyright (C) 2005-2012 BetaCONCEPT Limited
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
package org.betaconceptframework.astroboa.engine.database;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum DatabaseType {

	postgresql("PostgreSQL"),
	derby("Apache Derby"),
	hsql("HSQL Database Engine"),
	oracle("Oracle");

	private static Map<String, DatabaseType> databaseTypePerProductNames = new HashMap<String, DatabaseType>();
	
	private String productName;
	
	private DatabaseType(String databaseProductName) {
		this.productName = databaseProductName;
	}
	
	static{
		for (DatabaseType databaseType : DatabaseType.values()){
			databaseTypePerProductNames.put(databaseType.getProductName(),databaseType);	
		}
	}

	public String getProductName() {
		return productName;
	}
	
	public static String getDatabaseTypeForProductName(String productName){
		if (productName == null || ! databaseTypePerProductNames.containsKey(productName)){
			return null;
		}
		
		return databaseTypePerProductNames.get(productName).toString();
	}
	
	
	
}
