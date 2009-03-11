/*
 * Created on Feb 12, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.util;

/**
 * @author kec
 *
 */
public class Stopwatch {
    private long startTime = -1;
    private long stopTime = -1;
    private boolean running = false;

	public Stopwatch start() {
		startTime = System.currentTimeMillis();
        running = true;
        return this;
    }
    public Stopwatch stop() {
     stopTime = System.currentTimeMillis();
     running = false;
     return this;
    }
    public long getElapsedTime() {
        if (startTime == -1) {
         return 0;   
        }
        if (running) {
         return System.currentTimeMillis() - startTime;   
        }
        return stopTime - startTime;
    }
    public Stopwatch reset() {
     startTime = -1;
     stopTime = -1;
     running = false;
     return this;
     
    }
}
