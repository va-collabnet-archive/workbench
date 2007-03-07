/*
 * Created on Mar 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa;

import org.dwfa.bpa.process.Condition;

/**
 * @author kec
 *  
 */
public class Branch implements I_Branch {
    
    private static final long serialVersionUID = 8984276903055679737L;

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		I_Branch another = (I_Branch) obj;
		return (this.condition.equals(another.getCondition()) && (this.destinationId == another
				.getDestinationId()));
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return this.destinationId;
	}

	private Condition condition;

	private int destinationId;

	/**
	 * @param condition
	 * @param destinationId
	 */
	public Branch(Condition condition, int destinationId) {
		super();
		this.condition = condition;
		this.destinationId = destinationId;
	}
    
    public Branch() {
        super();
    }

	/**
	 * @see org.dwfa.bpa.I_Branch#getCondition()
	 */
	public Condition getCondition() {
		return condition;
	}

	/**
	 * @see org.dwfa.bpa.I_Branch#getDestinationId()
	 */
	public int getDestinationId() {
		return destinationId;
	}
	public String toString() {
     return this.condition + " -> " + this.destinationId;   
    }

    /**
     * @param condition The condition to set.
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * @param destinationId The destinationId to set.
     */
    public void setDestinationId(int destinationId) {
        this.destinationId = destinationId;
    }
}