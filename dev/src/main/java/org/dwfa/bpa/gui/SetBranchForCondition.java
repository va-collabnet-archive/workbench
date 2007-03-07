/*
 * Created on Mar 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;


/**
 * @author kec
 *
 */
public class SetBranchForCondition implements PropertyChangeListener {

    
	private int originId;
    private Condition condition;
    private I_EncodeBusinessProcess process;
	/**
	 * @param originId
	 * @param condition
	 * @param process
	 */
	public SetBranchForCondition(int originId, Condition condition,
			I_EncodeBusinessProcess process) {
		super();
		this.originId = originId;
		this.condition = condition;
		this.process = process;
	}
	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
        int oldId = ((Integer) evt.getOldValue()).intValue();
        int newId = ((Integer) evt.getNewValue()).intValue();
        if (oldId != -1) {
            this.process.removeBranch(this.process.getTask(originId), this.process.getTask(oldId),  this.condition);
         }
        if (newId != -1) {
            this.process.addBranch(this.process.getTask(originId), this.process.getTask(newId), this.condition);
         }
	}

}
