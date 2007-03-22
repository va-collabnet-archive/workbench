/*
 * Created on Feb 14, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author kec
 *  
 */
public class FilterInputStreamWithProgress extends FilterInputStream implements
MonitorableProcess {
	private int nread = 0;

	private int size = 0;
    
    private boolean done = false;
    
    private boolean stop = false;

	/**
	 * @param in
	 */
	public FilterInputStreamWithProgress(InputStream in) {
		super(in);
		try {
			size = in.available();
		} catch (IOException ioe) {
			size = 0;
		}
	}

	public int read() throws IOException {
        if (stop) {
            throw new IOException("Process canceled");   
           }
		int c = in.read();
		if (c >= 0) {
			nread++;
		}
		return c;
	}

	public int read(byte b[]) throws IOException {
        if (stop) {
            throw new IOException("Process canceled");   
           }
		int nr = in.read(b);
		if (nr > 0) {
			nread += nr;
		}
		return nr;
	}

	public int read(byte b[], int off, int len) throws IOException {
        if (stop) {
            throw new IOException("Process canceled");   
           }
		int nr = in.read(b, off, len);
		if (nr > 0) {
            nread += nr;
        }
		return nr;
	}
    public long skip(long n) throws IOException {
        if (stop) {
            throw new IOException("Process canceled");   
           }
        long nr = in.skip(n);
        if (nr > 0) {
            nread += nr;
        }
        return nr;
    }
    public void close() throws IOException {
        in.close();
        setDone(true);
    }
    /**
	 * 
	 */
	private synchronized void setDone(boolean done) {
		this.done = done;
        this.notifyAll();
	}

	public synchronized void reset() throws IOException {
        if (stop) {
         throw new IOException("Process canceled");   
        }
        in.reset();
        nread = size - in.available();
    }


	/**
	 * @see org.dwfa.bpa.util.TaskWithProgress#getLengthOfTask()
	 */
	public int getLengthOfTask() {
		
		return size;
	}

	/**
	 * @see org.dwfa.bpa.util.TaskWithProgress#getCurrent()
	 */
	public int getCurrent() {
		return nread;
	}

	/**
	 * @see org.dwfa.bpa.util.TaskWithProgress#stop()
	 */
	public void stop()  {
		this.stop = true;

	}

	/**
	 * @see org.dwfa.bpa.util.TaskWithProgress#isDone()
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * @see org.dwfa.bpa.util.MonitorableProcess#waitTillDone()
	 */
	public void waitTillDone() throws InterruptedException {
        while (!this.isDone()) wait();
		
	}


}