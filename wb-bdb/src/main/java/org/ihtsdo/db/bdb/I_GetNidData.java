package org.ihtsdo.db.bdb;

import java.io.IOException;

import com.sleepycat.bind.tuple.TupleInput;

public interface I_GetNidData {

	public byte[] getReadOnlyBytes() throws IOException;

	public byte[] getReadWriteBytes() throws IOException;

	public TupleInput getReadOnlyTupleInput() throws IOException;

	public TupleInput getMutableTupleInput() throws IOException;

	public boolean isPrimordial() throws IOException;
	
	public void reset();

}