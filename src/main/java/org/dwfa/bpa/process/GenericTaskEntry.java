/*
 * Created on Apr 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace05;

/**
 * @author kec
 *
 */
public class GenericTaskEntry implements Entry {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String name;
    /**
     * 
     */
    public GenericTaskEntry() {
        super();
    }
    
    public Entry execute(I_Work worker, JavaSpace05 space) throws TaskFailedException {
        throw new UnsupportedOperationException("GenericTaskEntry.execute() not implemented");
    }
    
    public long resultLeaseTime() {
        return 1000 * 10 * 60; // 10 minutes
    }

	/**
	 * @return
	 */
	public Object getName() {
        if (name == null) {
         return this.getClass().getName();   
        }
		return name;
	}

}
