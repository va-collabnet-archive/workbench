/*
 * Created on Feb 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.io.IOException;

/**
 * @author kec
 *
 */
public interface MonitorableProcess {

    /**
     * Called to find out how much work needs
     * to be done.
     */
    public int getLengthOfTask();

    /**
     * Called  to find out how much has been done.
     * @throws IOException
     */
    public int getCurrent() throws IOException;

    public void stop() throws Exception;

    /**
     * Called to find out if the task has completed.
     */
    public boolean isDone();
    
    /**
     * A guarded wait mechanism. See section 3.2.3 in Concurrent Programming in Java, 2nd edition. 
     * @throws InterruptedException
     *
     */
    
    public void waitTillDone() throws InterruptedException;


}
