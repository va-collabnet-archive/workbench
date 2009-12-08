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

public class UniversalAceRelationship implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private Collection<UUID> relId;

    private Collection<UUID> componentOneId;

    private Collection<UUID> componentTwoId;

    private List<UniversalAceRelationshipPart> versions;

    public UniversalAceRelationship(Collection<UUID> relId, Collection<UUID> componentOneId,
            Collection<UUID> componentTwoId, int count) {
        super();
        this.relId = relId;
        this.componentOneId = componentOneId;
        this.componentTwoId = componentTwoId;
        this.versions = new ArrayList<UniversalAceRelationshipPart>(count);
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
    public UniversalAceRelationship() {
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
    public Collection<UUID> getComponentOneId() {
        return componentOneId;
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
    public void setComponentOneId(Collection<UUID> componentOneId) {
        this.componentOneId = componentOneId;
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
    public Collection<UUID> getComponentTwoId() {
        return componentTwoId;
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
    public void setComponentTwoId(Collection<UUID> componentTwoId) {
        this.componentTwoId = componentTwoId;
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
    public void setRelId(Collection<UUID> relId) {
        this.relId = relId;
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
    public void setVersions(List<UniversalAceRelationshipPart> versions) {
        this.versions = versions;
    }

    // END: ADDED TO IMPLEMENT JAVABEANS SPEC

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_RelVersioned#addVersion(org.dwfa.vodb.types.I_RelPart
     * )
     */
    public boolean addVersion(UniversalAceRelationshipPart rel) {
        int index = versions.size() - 1;
        if (index == -1) {
            return versions.add(rel);
        } else if (index >= 0) {
            return versions.add(rel);
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#getVersions()
     */
    public List<UniversalAceRelationshipPart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#versionCount()
     */
    public int versionCount() {
        return versions.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#getC1Id()
     */
    public Collection<UUID> getC1Id() {
        return componentOneId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#getC2Id()
     */
    public Collection<UUID> getC2Id() {
        return componentTwoId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#getRelId()
     */
    public Collection<UUID> getRelId() {
        return relId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_RelVersioned#setC2Id(int)
     */
    public void setC2Id(Collection<UUID> destId) {
        componentTwoId = destId;

    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName());
        buff.append(": ");
        buff.append(relId);
        buff.append(" componentOneId: ");
        buff.append(componentOneId);
        buff.append(" componentTwoId: ");
        buff.append(componentTwoId);
        buff.append("\n");
        for (UniversalAceRelationshipPart part : versions) {
            buff.append("     ");
            buff.append(part.toString());
            buff.append("\n");
        }

        return buff.toString();
    }

}
