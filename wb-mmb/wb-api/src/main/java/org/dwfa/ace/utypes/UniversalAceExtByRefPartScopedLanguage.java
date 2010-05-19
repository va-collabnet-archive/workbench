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

public class UniversalAceExtByRefPartScopedLanguage extends UniversalAceExtByRefPartLanguage {
    /**
    * 
    */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private Collection<UUID> scopeUids;
    private int priority;
    private Collection<UUID> tagUids;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(scopeUids);
        out.writeInt(priority);
        out.writeObject(tagUids);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            scopeUids = (Collection<UUID>) in.readObject();
            priority = in.readInt();
            tagUids = (Collection<UUID>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Collection<UUID> getScopeUids() {
        return scopeUids;
    }

    public void setScopeUids(Collection<UUID> scopeUids) {
        this.scopeUids = scopeUids;
    }

    public Collection<UUID> getTagUids() {
        return tagUids;
    }

    public void setTagUids(Collection<UUID> tagUids) {
        this.tagUids = tagUids;
    }
}
