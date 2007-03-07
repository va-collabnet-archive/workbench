/*
 * Created on Apr 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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
