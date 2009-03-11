/*
 * Created on Feb 28, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

public class StopThreadException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public StopThreadException() {
        super();
    }

    public StopThreadException(String message) {
        super(message);
    }

    public StopThreadException(String message, Throwable cause) {
        super(message, cause);
    }

    public StopThreadException(Throwable cause) {
        super(cause);
    }

}
