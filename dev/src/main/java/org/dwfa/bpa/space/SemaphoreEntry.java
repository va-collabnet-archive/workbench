/*
 * Created on Apr 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.space;

import net.jini.core.entry.Entry;

/**
 * @author kec
 * See page 77 of JavaSpaces Principles, Patterns, and Practice
 *
 */
public class SemaphoreEntry implements Entry {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String resource;
    /**
     * 
     */
    public SemaphoreEntry(String resource) {
        super();
        this.resource = resource;
    }
    
    public SemaphoreEntry() {
        super();
    }

    /**
     * @return Returns the resource.
     */
    public String getResource() {
        return resource;
    }
}
