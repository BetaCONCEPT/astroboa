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
package org.betaconceptframework.astroboa.test;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.betaconceptframework.astroboa.api.model.exception.CmsException;
import org.objectweb.jotm.Jotm;
import org.objectweb.transaction.jta.TMService;
import org.springframework.transaction.jta.JtaTransactionManager;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public class TestJtaTransactionManager extends JtaTransactionManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1096034511566275687L;

	private TMService jotm;

	
	public TestJtaTransactionManager() {
		super();
		try {
			this.jotm = new Jotm(true, false);
		} catch (NamingException e) {
			throw new CmsException(e);
		}

	}

	@Override
	public void afterPropertiesSet()  {
		super.afterPropertiesSet();
		
		//Register it self to JNDI
		InitialContext context;
		try {
			
			context = new InitialContext();
			
			//Finally add to JNDI Context TransactionManager
			//so that it is available to Jboss Tree Cache
			//Although in configuration file transaction manager is not defined
			//since Jotm is used
			//Spring autodetects that Jotm produces a UserTransaction which also 
			// implements Transactionmanager interface
			context.bind("java:/TransactionManager", getTransactionManager());
			context.bind("java:/UserTransaction", getUserTransaction());
			context.bind("java:comp/UserTransaction", getUserTransaction());

		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public UserTransaction getUserTransaction() {
		return jotm.getUserTransaction();
	}

	@Override
	public TransactionManager getTransactionManager() {
		return jotm.getTransactionManager();
	}
	
	

}
