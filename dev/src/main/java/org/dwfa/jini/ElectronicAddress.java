/*
 * Created on Mar 24, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.jini;

import net.jini.entry.AbstractEntry;

/**
 * Simple Jini entry class that allows an in-box to advertise its address. 
 * @author kec
 *
 */
public class ElectronicAddress extends AbstractEntry {
	/**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String address;
    public ElectronicAddress() {
        super();
    }
	/**
	 * 
	 */
	public ElectronicAddress(String address) {
		super();
		this.address = address;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		return this.address.equals(other);
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.address.hashCode();
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.address;
	}
}
