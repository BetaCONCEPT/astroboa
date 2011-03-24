/**
 * 
 */
package org.betaconceptframework.astroboa.portal.managedbean;

import java.util.concurrent.ConcurrentHashMap;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 */

@AutoCreate
@Name("viewCountAggregator")
@Scope(ScopeType.APPLICATION)
public class ViewCountAggregator {
	
	private ConcurrentHashMap<String, Integer> contentObjectViewCount = new ConcurrentHashMap<String, Integer>(100);
	
	
	public ConcurrentHashMap<String, Integer> getContentObjectViewCount() {
		return contentObjectViewCount;
	}


	public void increaseContentObjectViewCounter(String contentObjectId) {
		Integer oldVal, newVal;
		boolean success = false;
		
	    do {
	      oldVal = contentObjectViewCount.get(contentObjectId);
	      newVal = (oldVal == null) ? 1 : (oldVal + 1);
	      if (oldVal == null) {
	    	  success = (contentObjectViewCount.putIfAbsent(contentObjectId, newVal) == null)? true : false;
	      }
	      else {
	    	  success = contentObjectViewCount.replace(contentObjectId, oldVal, newVal);
	      }
	      
	    } while (!success);
	  

	}
	
	
}
