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

package org.betaconceptframework.astroboa.model.impl.item;

import org.betaconceptframework.astroboa.model.impl.ItemQName;

/**
 * Qualified names representing JCR API items.
 *
 * @author Gregory Chomatas (gchomatas@betaconcept.com)
 * @author Savvas Triantafyllou (striantafyllou@betaconcept.com)
 * 
 */
public enum JcrBuiltInItem implements ItemQName{

		// JCR Node Type Names
		NtBase(JcrNamespaceConstants.NT_PREFIX , JcrNamespaceConstants.NT_URL,"base"),
		NtUnstructured(JcrNamespaceConstants.NT_PREFIX , JcrNamespaceConstants.NT_URL,"unstructured"),
		NtResource(JcrNamespaceConstants.NT_PREFIX , JcrNamespaceConstants.NT_URL,"resource"),
		NtVersionLabels(JcrNamespaceConstants.NT_PREFIX , JcrNamespaceConstants.NT_URL,"VersionLabels"),
		NtVersionHistory(JcrNamespaceConstants.NT_PREFIX , JcrNamespaceConstants.NT_URL,"versionHistory"),
		NtVersionedChild(JcrNamespaceConstants.NT_PREFIX , JcrNamespaceConstants.NT_URL,"versionedChild"),
		NtFrozenNode(JcrNamespaceConstants.NT_PREFIX, JcrNamespaceConstants.NT_URL,"frozenNode"),
		MixVersionable(JcrNamespaceConstants.MIX_PREFIX , JcrNamespaceConstants.MIX_URL,"versionable"),
		MixReferenceable(JcrNamespaceConstants.MIX_PREFIX , JcrNamespaceConstants.MIX_URL,"referenceable"),
		MixLockable(JcrNamespaceConstants.MIX_PREFIX , JcrNamespaceConstants.MIX_URL,"lockable"),
		
		//JCR ContentObjectProperty types
		JcrRoot(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"root"),
		JcrUuid(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"uuid"),
		JcrPath(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"path"),
		JcrVersionableUuid(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"versionableUuid"),
		JcrVersionStorage(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"versionStorage"),
		JcrChildVersionHistory(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"childVersionHistory"),
		JcrContains(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"contains"),
		JcrLike(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"like"),
		JcrVersionLabels(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"VersionLabels"),
		JcrFrozenNode(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"frozenNode"),
		JcrFrozenUUID(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"frozenUuid"),
		JcrFrozenPrimaryType(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"frozenPrimaryType"),
		JcrRootVersion(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"rootVersion"),
		JcrScore(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"score"),
		JcrSystem(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"system"),
		JcrPrimaryType(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"primaryType"),
		JcrDeref(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"deref"),
		JcrMimeType(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"mimeType"),
		JcrEncoding(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"encoding"),
		JcrData(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"data"),
		JcrLastModified(JcrNamespaceConstants.JCR_PREFIX, JcrNamespaceConstants.JCR_URL,"lastModified"),
		
		//All QName
		All("", "", "*");
		
		private ItemQName jcrBuiltInItem;
		

		private JcrBuiltInItem(String prefix, String namespaceUrl, String localPart)
		{
			jcrBuiltInItem = new ItemQNameImpl(prefix, namespaceUrl, localPart);
			
		}
		
		public String getJcrName()
		{
			return jcrBuiltInItem.getJcrName();
		}
		
		public String getLocalPart() {
			return jcrBuiltInItem.getLocalPart();
		}

		public String getNamespaceURI() {
			return jcrBuiltInItem.getNamespaceURI();
		}

		public String getPrefix() {
			return jcrBuiltInItem.getPrefix();
		}

		public boolean equals(ItemQName otherItemQName) {
			return jcrBuiltInItem.equals(otherItemQName);
		}
		public boolean equalsTo(ItemQName otherItemQName) {
			return equals(otherItemQName);
		}


		
	}
