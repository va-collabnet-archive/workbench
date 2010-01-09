package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import com.sleepycat.bind.tuple.TupleInput;

public interface I_GetNidData {

	public byte[] getReadOnlyBytes() throws InterruptedException,
			ExecutionException, IOException;

	public byte[] getReadWriteBytes() throws InterruptedException,
			ExecutionException;

	public TupleInput getReadOnlyTupleInput() throws InterruptedException,
			ExecutionException, IOException;

	public TupleInput getReadWriteTupleInput() throws InterruptedException,
			ExecutionException;

}