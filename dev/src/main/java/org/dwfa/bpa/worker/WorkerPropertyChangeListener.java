/*
 * Created on Mar 8, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.worker;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dwfa.bpa.process.I_Work;

public class WorkerPropertyChangeListener implements PropertyChangeListener {
    I_Work worker;
    /**
     * @param worker
     */
    public WorkerPropertyChangeListener(I_Work worker) {
        super();
        this.worker = worker;
    }
    public void propertyChange(PropertyChangeEvent evt) {
        worker.getLogger().info("property change: " + evt.getPropertyName() + 
                " old:" + evt.getOldValue() + 
                " new:" + evt.getNewValue());
        
    }
    
}