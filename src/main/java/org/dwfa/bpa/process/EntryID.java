/*
 * Created on Jun 11, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
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