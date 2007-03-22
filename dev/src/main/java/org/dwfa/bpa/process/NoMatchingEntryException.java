/*
 * Created on Apr 4, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

public class NoMatchingEntryException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NoMatchingEntryException() {
        super();
    }

    public NoMatchingEntryException(String message) {
        super(message);
    }

    public NoMatchingEntryException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMatchingEntryException(Throwable cause) {
        super(cause);
    }

}
