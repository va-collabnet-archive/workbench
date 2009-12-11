/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dwfa.ace.utypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class UniversalAceDescription implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Collection<UUID> descId;

	private Collection<UUID> conceptId;

	private List<UniversalAceDescriptionPart> versions;

	public UniversalAceDescription(Collection<UUID> descId, Collection<UUID> conceptId, int count) {
		super();
		this.descId = descId;
		this.conceptId = conceptId;
		this.versions = new ArrayList<UniversalAceDescriptionPart>(count);
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#addVersion(org.dwfa.vodb.types.I_DescriptionPart)
	 */
	public boolean addVersion(UniversalAceDescriptionPart newPart) {
		int index = versions.size() - 1;
		if (index == -1) {
			return versions.add(newPart);
		} else if (index >= 0) {
			return versions.add(newPart);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#getVersions()
	 */
	public List<UniversalAceDescriptionPart> getVersions() {
		return versions;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#versionCount()
	 */
	public int versionCount() {
		return versions.size();
	}


	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#getConceptId()
	 */
	public Collection<UUID> getConceptId() {
		return conceptId;
	}

	/* (non-Javadoc)
	 * @see org.dwfa.vodb.types.I_DescriptionVersioned#getDescId()
	 */
	public Collection<UUID> getDescId() {
		return descId;
	}
	@Override
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(this.getClass().getSimpleName());
		buff.append(": ");
		buff.append(descId);
		buff.append(" conId:");
		buff.append(conceptId);
		buff.append("\n");
		for (UniversalAceDescriptionPart part : versions) {
			buff.append("     ");
			buff.append(part.toString());
			buff.append("\n");
		}

		return buff.toString();
	}


}
