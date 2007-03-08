package org.dwfa.vodb.jar;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

import com.sleepycat.je.DatabaseEntry;

public abstract class TermWriter implements Callable<Object> {
	DataOutputStream dos;
	int count = 0;
	protected boolean canceled = false;

	public TermWriter(OutputStream outStream) {
		dos = new DataOutputStream(new BufferedOutputStream(outStream));
	}

	public DatabaseEntry getDataEntry() {
		return new DatabaseEntry(); 
	}

	public DatabaseEntry getKeyEntry() {
		return new DatabaseEntry();
	}

	public void close() throws IOException {
		dos.close();
	}

	public int getCount() {
		return count;
	}
	public void cancel() {
		canceled  = true;
	}

}
