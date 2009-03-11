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
public class ProcessID implements Serializable, Comparable {
    private static final long serialVersionUID = 1;

    private UUID id;

    
    /**
     * @param id
     */
    public ProcessID(UUID id) {
        super();
        this.id = id;
    }
    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj) {
        ProcessID another = (ProcessID) obj;
        return this.id.toString().compareTo(another.id.toString());
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        ProcessID another = (ProcessID) obj;
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
    
    public UUID getUuid() {
        return this.id;
    }
}
