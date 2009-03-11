/*
 * Created on Jun 9, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.queue.bpa.tasks.failsafe;

import java.io.Serializable;

import net.jini.core.lookup.ServiceID;

import org.dwfa.bpa.process.EntryID;
import org.dwfa.bpa.process.ProcessID;

public class QueueEntryData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String origin;
    private ServiceID queueID;
    private ProcessID processID;
    private EntryID entryID;
    /**
     * @param origin
     * @param queueid
     * @param processid
     */
    public QueueEntryData(String origin, ServiceID queueid, ProcessID processid, EntryID entryID) {
        super();
        this.origin = origin;
        queueID = queueid;
        processID = processid;
        this.entryID = entryID;
    }
    
    public QueueEntryData() {
        super();
    }
    /**
     * @return Returns the origin.
     */
    public String getOrigin() {
        return origin;
    }
    /**
     * @return Returns the processID.
     */
    public ProcessID getProcessID() {
        return processID;
    }
    /**
     * @return Returns the queueID.
     */
    public ServiceID getQueueID() {
        return queueID;
    }
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "origin: " + this.origin + "\nqueueID: " + this.queueID + "\nprocessID: " + this.processID + "\nentryID: " + this.entryID;
    }
    
    /**
     * @return Returns the entryID.
     */
    public EntryID getEntryID() {
        return entryID;
    }
}
