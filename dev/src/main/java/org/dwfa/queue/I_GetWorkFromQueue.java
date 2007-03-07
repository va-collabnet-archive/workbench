/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue;

import org.dwfa.bpa.process.I_QueueProcesses;

/**
 * @author kec
 *
 */
public interface I_GetWorkFromQueue {

    public void queueContentsChanged();
    public void start(I_QueueProcesses queue);
}
