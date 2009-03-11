/*
 * Created on Jan 12, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;

public class TargetAndProcessForEditor {
    Object target;
    I_EncodeBusinessProcess process;
    /**
     * @param process
     * @param task
     */
    public TargetAndProcessForEditor(I_EncodeBusinessProcess process, Object task) {
        super();
         this.process = process;
        this.target = task;
    }
    /**
     * @return Returns the process.
     */
    public I_EncodeBusinessProcess getProcess() {
        return process;
    }
    /**
     * @return Returns the task.
     */
    public Object getTarget() {
        return target;
    }
}