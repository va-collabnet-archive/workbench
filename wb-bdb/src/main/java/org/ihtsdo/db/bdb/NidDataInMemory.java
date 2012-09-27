package org.ihtsdo.db.bdb;

import java.io.IOException;

import com.sleepycat.bind.tuple.TupleInput;

public class NidDataInMemory implements I_GetNidData {

	private byte[] readOnlyBytes;
	private byte[] readWriteBytes;
	
	public NidDataInMemory(byte[] readOnlyBytes, byte[] readWriteBytes) {
		super();
		this.readOnlyBytes = readOnlyBytes.clone();
		this.readWriteBytes = readWriteBytes.clone();
	}

	@Override
	public byte[] getReadOnlyBytes()  {
		return readOnlyBytes;
	}

	@Override
	public TupleInput getReadOnlyTupleInput()  {
		return new TupleInput(getReadOnlyBytes());
	}

	@Override
	public byte[] getReadWriteBytes() {
		return readWriteBytes;
	}

	@Override
	public TupleInput getMutableTupleInput()  {
		return new TupleInput(getReadWriteBytes());
	}

	@Override
	public boolean isPrimordial() throws IOException {
		return readOnlyBytes != null || readWriteBytes != null;
	}

	public void reset() {
		// nothing to do...
	}

}
