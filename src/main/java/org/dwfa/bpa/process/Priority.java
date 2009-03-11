/*
 * Created on Mar 25, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;


import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Priority to associate with a processes execution. 
 * Implementation of a typesafe enumeration. 
 * @author kec
 *  
 */
public class Priority implements Serializable, Comparable {
    private static final long serialVersionUID = 1;

    /**
     * <code>HIGHEST</code> Highest priority
     */
    public static final Priority HIGHEST = new Priority("Highest", 1);
    /**
     * <code>HIGH</code> high priority
     */
    public static final Priority HIGH = new Priority("High", 2);
    /**
     * <code>NORMAL</code> Normal priority
     */
    public static final Priority NORMAL = new Priority("Normal", 3);
    /**
     * <code>LOW</code> low priority 
     */
    public static final Priority LOW = new Priority("Low", 4);
    /**
     * <code>LOWEST</code> lowest priority
     */
    public static final Priority LOWEST = new Priority("Lowest", 5);

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return description;
    }
    public int compareTo(Object aObject) {
        return ordinal - ((Priority)aObject).ordinal;
    }

    private final int ordinal;

    /**
     *  
     */
    private final String description;
 
    private Priority(String description, int ordinal) {
        this.description = description;
        this.ordinal = ordinal;
    }

     public static final Priority[] values = { HIGHEST, HIGH, NORMAL, LOW, LOWEST };

    public static final List<Priority> VALUES = Collections.unmodifiableList(Arrays
            .asList(values));

    //Implement Serializable with these two items
    private Object readResolve() throws ObjectStreamException {
        return values[ordinal-1];
    }
    /**
     * @return
     */
    public String getXPriorityValue() {
        return Integer.toString(ordinal);
    }
}