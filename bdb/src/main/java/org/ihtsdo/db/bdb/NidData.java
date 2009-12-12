package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.Database;

public class NidData {

	public static enum REF_TYPE {SOFT, WEAK};
	
	private static REF_TYPE refType = REF_TYPE.SOFT;

	private Future<byte[]> readOnlyFuture;
	private Future<byte[]> readWriteFuture;
	
	private int nid;
	private Database readOnly;
	private Reference<byte[]> readOnlyBytes;
	private byte[] readWriteBytes;

	public NidData(int nid, Database readOnly, Database readWrite) {
		super();
		this.nid = nid;
		this.readOnly = readOnly;
		readOnlyFuture = Bdb.getExecutorPool().submit(new GetNidData(nid, readOnly));
		readWriteFuture = Bdb.getExecutorPool().submit(new GetNidData(nid, readWrite));
	}

	public byte[] getReadOnlyBytes() throws InterruptedException, ExecutionException, IOException {
		if (readOnlyBytes == null) {
			byte[] bytes = readOnlyFuture.get();
			switch (refType) {
			case SOFT:
				readOnlyBytes = new SoftReference<byte[]>(bytes);
				break;
			case WEAK:
				readOnlyBytes = new WeakReference<byte[]>(bytes);
				break;
				default:
					throw new RuntimeException("Don't know how to handle: " + refType);
			}
			return bytes;
		}
		byte[] bytes = readOnlyBytes.get();
		if (bytes == null) {
			GetNidData getter = new GetNidData(nid, readOnly);
			bytes = getter.call();
		}
		return bytes;
	}

	public byte[] getReadWriteBytes() throws InterruptedException, ExecutionException {
		if (readWriteBytes == null) {
			readWriteBytes = readWriteFuture.get();
		}
		return readWriteBytes;
	}
	
	public TupleInput getReadOnlyTupleInput() throws InterruptedException, ExecutionException, IOException {
		return new TupleInput(getReadOnlyBytes());
	}

	public TupleInput getReadWriteTupleInput() throws InterruptedException, ExecutionException {
		return new TupleInput(getReadWriteBytes());
	}

}
