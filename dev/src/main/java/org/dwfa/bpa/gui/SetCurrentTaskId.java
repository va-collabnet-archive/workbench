/*
 * Created on Mar 21, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.dwfa.bpa.process.I_EncodeBusinessProcess;


/**
 * @author kec
 *
 */
public class SetCurrentTaskId implements PropertyChangeListener {

	I_EncodeBusinessProcess process;
	/**
	 * @param process
	 */
	public SetCurrentTaskId(I_EncodeBusinessProcess process) {
		super();
		this.process = process;
	}
	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		int currentTaskId = ((Integer) evt.getNewValue()).intValue();
		this.process.setCurrentTaskId(currentTaskId);
	}
}
