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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.TimePathId;

public class UniversalIdList implements Serializable, I_Transact, I_AmChangeSetObject {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Set<UniversalAceIdentification> uncommittedIds = new HashSet<UniversalAceIdentification>();

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
    public void setUncommittedIds(Set<UniversalAceIdentification> uncommittedIds) {
        this.uncommittedIds = uncommittedIds;
    }

    // END: ADDED TO IMPLEMENT JAVABEANS SPEC

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(uncommittedIds);
    }

    public String toString() {
        return "UniversalIdList: " + "\n uncommittedIds: " + uncommittedIds;
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            uncommittedIds = (Set<UniversalAceIdentification>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public Set<UniversalAceIdentification> getUncommittedIds() {
        return uncommittedIds;
    }

    public void abort() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void commit(int version, Set<TimePathId> values) throws IOException {
        throw new UnsupportedOperationException();
    }
}
