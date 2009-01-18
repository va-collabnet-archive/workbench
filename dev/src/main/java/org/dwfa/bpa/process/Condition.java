/*
 * Created on Mar 18, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;


/**
 * Exit conditions for tasks. 
 * @author kec
 *  
 */
public enum Condition  {

    CONTINUE("Continue", true),
    ITEM_CANCELED("Canceled", true),
    ITEM_SKIPPED("Skipped", true),
    ITEM_COMPLETE("Completed", true),
    TRUE("True", true),
    FALSE("False", true),
    STOP("Stop", true),
    PROCESS_COMPLETE("The End", false),
    STOP_THEN_REPEAT("Stop then Repeat", false),
    WAIT_FOR_WEB_FORM("Web Post", true),
    PREVIOUS("Previous", true);

	public String toString() {
		return description;
	}
	
    public boolean isBranchCondition() {
     return branchCondition;   
    }

	/**
	 *  
	 */
	private final String description;
    private final boolean branchCondition;

	private Condition(String description, boolean setBranch) {
		this.description = description;
        this.branchCondition = setBranch;
	}

    public static Condition getFromString(String  desc) {
        for (Condition c: Condition.values()) {
            if (c.description.equals(desc)) {
                return c;
            }
        }
        throw new IllegalArgumentException(desc);
    }
}