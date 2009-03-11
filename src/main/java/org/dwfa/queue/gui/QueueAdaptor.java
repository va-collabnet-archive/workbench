/*
 * Created on Jan 21, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.queue.gui;

import net.jini.core.lookup.ServiceID;

import org.dwfa.bpa.process.I_QueueProcesses;

public class QueueAdaptor implements Comparable {
	I_QueueProcesses queue;

	String queueName;

	ServiceID id;

	/**
	 * @param queue
	 * @param queueName
	 * @param id
	 */
	public QueueAdaptor(I_QueueProcesses queue, String queueName,
			ServiceID id) {
		super();
		this.queue = queue;
		this.queueName = queueName;
		this.id = id;
	}

	public String toString() {
		return this.queueName;
	}

    public int compareTo(Object o) {
        QueueAdaptor qa2 = (QueueAdaptor) o;
        return queueName.compareTo(qa2.queueName);
    }
}