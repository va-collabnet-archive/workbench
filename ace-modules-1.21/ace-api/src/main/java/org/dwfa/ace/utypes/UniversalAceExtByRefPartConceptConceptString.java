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
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartConceptConceptString extends UniversalAceExtByRefPart {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Collection<UUID> c1UuidCollection;
    private Collection<UUID> c2UuidCollection;
    private String str;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(c1UuidCollection);
        out.writeObject(c2UuidCollection);
        out.writeObject(str);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            c1UuidCollection = (Collection<UUID>) in.readObject();
            c2UuidCollection = (Collection<UUID>) in.readObject();
            str = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public Collection<UUID> getC1UuidCollection() {
        return c1UuidCollection;
    }

    public void setC1UuidCollection(Collection<UUID> uuidCollection) {
        c1UuidCollection = uuidCollection;
    }

    public Collection<UUID> getC2UuidCollection() {
        return c2UuidCollection;
    }

    public void setC2UuidCollection(Collection<UUID> uuidCollection) {
        c2UuidCollection = uuidCollection;
    }
}
