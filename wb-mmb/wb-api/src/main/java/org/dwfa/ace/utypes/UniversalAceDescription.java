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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.tapi.TerminologyException;

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
    
    public UniversalAceDescription(I_DescriptionVersioned desc) throws IOException, TerminologyException {
        super();
        UniversalAceDescription universal = new UniversalAceDescription(
        		Terms.get().getUids(desc.getDescId()), Terms.get().getUids(desc.getConceptId()),
        		desc.versionCount());
        for (I_DescriptionPart part : desc.getVersions(null)) {
            UniversalAceDescriptionPart universalPart = new UniversalAceDescriptionPart();
            universalPart.setInitialCaseSignificant(part.isInitialCaseSignificant());
            universalPart.setLang(part.getLang());
            universalPart.setPathId(Terms.get().getUids(part.getPathId()));
            universalPart.setStatusId(Terms.get().getUids(part.getStatusId()));
            universalPart.setText(part.getText());
            universalPart.setTypeId(Terms.get().getUids(part.getTypeId()));
            universalPart.setTime(part.getTime());
            universal.addVersion(universalPart);
        }
    }


    // START: ADDED TO IMPLEMENT JAVABEANS SPEC
    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public UniversalAceDescription() {
        super();
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setDescId(Collection<UUID> descId) {
        this.descId = descId;
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setConceptId(Collection<UUID> conceptId) {
        this.conceptId = conceptId;
    }

    /**
     * DO NOT USE THIS METHOD.
     * 
     * This method has been included to meet the JavaBeans specification,
     * however it should not be used as it allows access to attributes that
     * should not be modifiable and weakens the interface. The method has been
     * added as a convenience to allow JavaBeans tools access via introspection
     * but is not intended for general use by developers.
     * 
     * @deprecated
     */
    public void setVersions(List<UniversalAceDescriptionPart> versions) {
        this.versions = versions;
    }

    // END: ADDED TO IMPLEMENT JAVABEANS SPEC

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_DescriptionVersioned#addVersion(org.dwfa.vodb.types
     * .I_DescriptionPart)
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

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getVersions()
     */
    public List<UniversalAceDescriptionPart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#versionCount()
     */
    public int versionCount() {
        return versions.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_DescriptionVersioned#getConceptId()
     */
    public Collection<UUID> getConceptId() {
        return conceptId;
    }

    /*
     * (non-Javadoc)
     * 
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
        if (versions != null) {
            for (UniversalAceDescriptionPart part : versions) {
                buff.append("     ");
                buff.append(part.toString());
                buff.append("\n");
            }
        } else {
        	buff.append("###  mutableParts is null   ###");
        }

        return buff.toString();
    }

	public List<UniversalAceDescriptionPart> getMutableParts() {
		return versions;
	}

}
