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
import java.util.List;
import java.util.UUID;

public class UniversalAceIdentification implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    private List<UniversalAceIdentificationPart> versions;

    public UniversalAceIdentification(int count) {
        super();
        this.versions = new ArrayList<UniversalAceIdentificationPart>(count);
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
    public UniversalAceIdentification() {
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
    public void setVersions(List<UniversalAceIdentificationPart> versions) {
        this.versions = versions;
    }

    // END: ADDED TO IMPLEMENT JAVABEANS SPEC

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IdVersioned#getVersions()
     */
    public List<UniversalAceIdentificationPart> getVersions() {
        return versions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.vodb.types.I_IdVersioned#getUIDs()
     */
    public List<UUID> getUIDs() {
        List<UUID> uids = new ArrayList<UUID>(versions.size());
        for (UniversalAceIdentificationPart p : versions) {
            if (UUID.class.isAssignableFrom(p.getSourceId().getClass())) {
                uids.add((UUID) p.getSourceId());
            }
        }
        return uids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.dwfa.vodb.types.I_IdVersioned#addVersion(org.dwfa.vodb.types.I_IdPart
     * )
     */
    public boolean addVersion(UniversalAceIdentificationPart srcId) {
        int index = versions.size() - 1;
        if (index == -1) {
            return versions.add(srcId);
        } else if (index >= 0) {
            return versions.add(srcId);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName());
        buff.append(": ");
        buff.append("\n");
        for (UniversalAceIdentificationPart part : versions) {
            buff.append("     ");
            buff.append(part.toString());
            buff.append("\n");
        }

        return buff.toString();
    }

}
