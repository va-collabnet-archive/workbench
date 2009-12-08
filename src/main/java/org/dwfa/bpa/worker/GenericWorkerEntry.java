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
 * Created on Apr 18, 2005
 */
package org.dwfa.bpa.worker;

import java.util.UUID;

import net.jini.core.entry.Entry;

/**
 * @author kec
 * 
 */
public class GenericWorkerEntry implements Entry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String description;
    public UUID id;

    /**
     * @param description
     * @param id
     */
    public GenericWorkerEntry(String description, UUID id) {
        super();
        this.description = description;
        this.id = id;
    }

    /**
     * 
     */
    public GenericWorkerEntry() {
        super();
    }

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the id.
     */
    public UUID getId() {
        return id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.description + " (" + this.id + ")";
    }
}
