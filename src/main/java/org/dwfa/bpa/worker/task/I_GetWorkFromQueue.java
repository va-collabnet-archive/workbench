/*
 * Created on Apr 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.worker.task;

import org.dwfa.bpa.process.I_PluginToWorker;
import org.dwfa.bpa.process.I_QueueProcesses;

/**
 * @author kec
 *
 */
public interface I_GetWorkFromQueue extends I_PluginToWorker {

    public void queueContentsChanged();
    
    public void start(I_QueueProcesses queue);
}
