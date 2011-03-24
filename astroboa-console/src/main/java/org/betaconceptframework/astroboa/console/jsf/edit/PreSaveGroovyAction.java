package org.betaconceptframework.astroboa.console.jsf.edit;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.client.AstroboaClient;
import org.slf4j.Logger;

public interface PreSaveGroovyAction {
	public void run(AstroboaClient astroboaClient, ContentObject contentObject, Logger logger);
}
