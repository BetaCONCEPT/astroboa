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

package org.betaconceptframework.astroboa.api.service;


import java.net.URI;
import java.net.URL;
import java.util.concurrent.Future;

import org.betaconceptframework.astroboa.api.model.ContentObject;
import org.betaconceptframework.astroboa.api.model.RepositoryUser;
import org.betaconceptframework.astroboa.api.model.Space;
import org.betaconceptframework.astroboa.api.model.Taxonomy;
import org.betaconceptframework.astroboa.api.model.Topic;
import org.betaconceptframework.astroboa.api.model.io.ImportConfiguration;
import org.betaconceptframework.astroboa.api.model.io.ImportReport;

/**
 * Service providing methods for importing content into an Astroboa repository. 
 * 
 * <p>
 * Import source is expected to be in XML or JSON (not yet implemented) format.
 * It can either be a simple {@link String} or a {@link URL} whose content
 * will be opened and processed. In the later case, this services identifies
 * compressed (ZIP) files as well.
 * </p>
 * 
 * 
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public interface ImportService {

	/**
	 * Import content to Astroboa repository. 
	 * 
	 * <p>
	 * Import source can be any URL whose content is either XML or JSON or a ZIP file which contains XML or JSON files
	 * </p>
	 * 
	 * <p>
	 * Import procedure takes place in a separate Thread in order not to
	 * 	block current Thread. Import progress is depicted in {@link ImportReport}
	 * 	but it can be followed only by local connections. 
	 * 	</p>
	 * 
	 * @param contentSource Import source location. 
	 * @param configuration Import configuration.
	 * 
	 * @return A report about import progress. In local invocations of this method it is possible to follow import progress.
	 */
	Future<ImportReport> importRepositoryContentFromURI(URI contentSource, ImportConfiguration configuration);
	
	/**
	 * Import content to Astroboa repository. 
	 * 
	 * <p>
	 * Import source can be any String whose content is either XML or JSON.
	 * </p>
	 * 
	 * <p>
	 * Import procedure takes place in a separate Thread in order not to
	 * 	block current Thread. Import progress is depicted in {@link ImportReport}
	 * 	but it can be followed only by local connections. 
	 * 	</p>
	 * 
	 * @param contentSource Import source. 
	 * @param configuration Import configuration.
	 * 
	 * @return A report about import progress. In local invocations of this method it is possible to follow import progress.
	 */
	Future<ImportReport> importRepositoryContentFromString(String contentSource, ImportConfiguration configuration);
	
	/**
	 * Import content object to Astroboa repository from XML or JSON. 
	 * 
	 * <p>
	 * No separate Thread is created.
	 * </p>
	 * 
	 * <p>
	 * The same security rules with {@link ContentService#save(Object, boolean, boolean, String)}
	 * apply.
	 * </p>
	 * 
	 * @param contentSource Xml or JSON representation of a {@link ContentObject}.
	 * @param configuration Import configuration.
	 * 
	 * @return Imported {@link ContentObject}
	 */
	ContentObject importContentObject(String contentSource,ImportConfiguration configuration);
	
	/**
	 * Import repository user to Astroboa repository from XML or JSON. 
	 * 
	 * <p>
	 * No separate Thread is created.
	 * </p>
	 * 
	 * <p>
	 * If source contains {@link RepositoryUser#getFolksonomy() folksonomy} root topics xml
	 * and {@link RepositoryUser#getSpace() space} xml, then these are saved or updated as well
	 * </p>
	 * 
	 * @param repositoryUserSource Xml or JSON representation of a {@link RepositoryUser}.
	 * @param configuration Import configuration.
	 * 
	 * @return Newly created or updated RepositoryUser
	 */
	RepositoryUser importRepositoryUser(String repositoryUserSource, ImportConfiguration configuration);

	/**
	 * Import topic to Astroboa repository from XML or JSON. 
	 * 
	 * <p>
	 * No separate Thread is created.
	 * </p>
	 * 
	 * <p>
	 * If source contains topic tree rather than topic properties only the whole tree is saved
	 * </p>
	 * 
	 * @param topicSource Xml or JSON representation of a {@link Topic}.
	 * @param configuration Import configuration.
	 * 
	 * @return Newly created or updated Topic
	 */
	Topic importTopic(String topicSource, ImportConfiguration configuration);

	/**
	 * Import space to Astroboa repository from XML or JSON. 
	 * 
	 * <p>
	 * No separate Thread is created.
	 * </p>
	 * 
	 * <p>
	 * If source contains space tree rather than space properties only the whole tree is saved
	 * </p>
	 * 
	 * @param spaceSource Xml or JSON representation of a {@link Space}.
	 * @param configuration Import configuration.
	 * 
	 * @return Newly created or updated Space
	 */
	Space importSpace(String spaceSource, ImportConfiguration configuration);

	/**
	 * Import taxonomy to Astroboa repository from XML or JSON. 
	 * 
	 * <p>
	 * No separate Thread is created.
	 * </p>
	 * 
	 * <p>
	 * If source contains taxonomy tree rather than taxonomy properties only the whole tree is saved
	 * </p>
	 * 
	 * @param taxonomySource Xml or JSON representation of a {@link Taxonomy}.
	 * @param configuration Import configuration.
	 * 
	 * @return Newly created or updated Taxonomy
	 */
	Taxonomy importTaxonomy(String taxonomySource, ImportConfiguration configuration);

}
