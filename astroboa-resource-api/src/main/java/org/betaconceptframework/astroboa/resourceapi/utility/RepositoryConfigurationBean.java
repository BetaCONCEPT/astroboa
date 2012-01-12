package org.betaconceptframework.astroboa.resourceapi.utility;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.betaconceptframework.astroboa.api.security.IdentityPrincipal;
import org.betaconceptframework.astroboa.configuration.Repositories;
import org.betaconceptframework.astroboa.configuration.RepositoryRegistry;
import org.betaconceptframework.astroboa.configuration.RepositoryType;
import org.betaconceptframework.astroboa.configuration.SecurityType.PermanentUserKeyList.PermanentUserKey;

public enum RepositoryConfigurationBean {
	
	INSTANCE; 
	
	private final Map<String,String> anonymousPermanentKeyPerRepository;

	private RepositoryConfigurationBean() {
		
		Map<String, String> permanentUserKeyMap = new HashMap<String, String>();
		
		Repositories Repositories = RepositoryRegistry.INSTANCE.getRepositories();
		
		for(RepositoryType RepositoryType : Repositories.getRepository()) {
			if(
				RepositoryType.getSecurity() != null && 
				RepositoryType.getSecurity().getPermanentUserKeyList() != null &&
				CollectionUtils.isNotEmpty(RepositoryType.getSecurity().getPermanentUserKeyList().getPermanentUserKey())) {
				for (PermanentUserKey permanentUserKey : RepositoryType.getSecurity().getPermanentUserKeyList().getPermanentUserKey()) {
					if (permanentUserKey.getUserid().equals(IdentityPrincipal.ANONYMOUS) || 
							permanentUserKey.getUserid().contains(IdentityPrincipal.ANONYMOUS)) {
						permanentUserKeyMap.put(RepositoryType.getId(), permanentUserKey.getKey());
					}
				}
			}
		}
		
		anonymousPermanentKeyPerRepository = permanentUserKeyMap;
	}
	
	public Map<String,String> getAnonymousPermanentKeyPerRepository() {
		return anonymousPermanentKeyPerRepository;
	}
}
