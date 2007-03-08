package org.dwfa.vodb.jar;

import java.util.concurrent.Callable;

import com.sleepycat.je.DatabaseEntry;

public abstract class TermCounter implements Callable<Object> {

	int count = 0;
	protected boolean canceled = false;

	
	public int getCount() {
		return count;
	}
	public DatabaseEntry getDataEntry() {
		DatabaseEntry data = new DatabaseEntry(); 
		data.setPartial(0, 0, true);
		return data;
	}

	public DatabaseEntry getKeyEntry() {
		DatabaseEntry key = new DatabaseEntry(); 
		key.setPartial(0, 0, true);
		return key;
	}
	public void cancel() {
		canceled = true;
	}
	
}
