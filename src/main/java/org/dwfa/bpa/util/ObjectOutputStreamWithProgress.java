/*
 * Created on Feb 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author kec
 *
 */
public class ObjectOutputStreamWithProgress extends ObjectOutputStream implements MonitorableProcess {
    private int lengthOfTask;
    private int current;
    private boolean done = false;
    private boolean canceled = false;
	/**
	 * @param out
	 * @throws java.io.IOException
	 */
	public ObjectOutputStreamWithProgress(OutputStream out) throws IOException {
		super(out);
        this.current = 0;
        this.lengthOfTask = Integer.MAX_VALUE;
	}

	/**
	 * @return Returns the current.
	 */
	public int getCurrent() {
		return current;
	}
	/**
	 * @param current The current to set.
	 */
	public void setCurrent(int current) {
		this.current = current;
	}
	/**
	 * @return Returns the done.
	 */
	public boolean isDone() {
		return done;
	}
	/**
	 * @param done The done to set.
	 */
	public synchronized void setDone(boolean done) {
		this.done = done;
        this.notifyAll();
	}
	/**
	 * @return Returns the lengthOfTask.
	 */
	public int getLengthOfTask() {
		return lengthOfTask;
	}
	/**
	 * @param lengthOfTask The lengthOfTask to set.
	 */
	public void setLengthOfTask(int lengthOfTask) {
		this.lengthOfTask = lengthOfTask;
	}

	/**
	 * @see org.dwfa.bpa.util.MonitorableProcess#stop()
	 */
	public void stop() {
		this.canceled = true;
		
	}
	/**
	 * @return Returns the canceled.
	 */
	public boolean isCanceled() {
		return canceled;
	}
	/**
	 * @param canceled The canceled to set.
	 */
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	/**
	 * @see org.dwfa.bpa.util.MonitorableProcess#waitTillDone()
	 */
	public synchronized void waitTillDone() throws InterruptedException {
        while (!this.isDone()) wait();
	}
}
