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
/*
 * Created on Jun 11, 2005
 */
package org.dwfa.bpa.process;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author kec
 * 
 */
public class EntryID implements Serializable, Comparable<EntryID> {
    private static final long serialVersionUID = 1;

    private UUID id;

    /**
     * @param id
     */
    public EntryID(UUID id) {
        super();
        this.id = id;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(EntryID obj) {
        EntryID another = (EntryID) obj;
        return this.id.toString().compareTo(another.id.toString());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        EntryID another = (EntryID) obj;
        return this.id.equals(another.id);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.id.hashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.id.toString();
    }

}
