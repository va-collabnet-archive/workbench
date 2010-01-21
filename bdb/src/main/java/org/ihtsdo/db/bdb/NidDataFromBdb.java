package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.je.Database;

public class NidDataFromBdb implements I_GetNidData {

	public static enum REF_TYPE {SOFT, WEAK};
	
	private static REF_TYPE refType = REF_TYPE.SOFT;

	private Future<byte[]> readOnlyFuture;
	private Future<byte[]> readWriteFuture;
	
	private int nid;
	private Database readOnly;
	private Reference<byte[]> readOnlyBytes;
	private byte[] readWriteBytes;

	private static ExecutorService executorPool;

	public NidDataFromBdb(int nid, Database readOnly, Database readWrite) {
		super();
		this.nid = nid;
		this.readOnly = readOnly;
		
		if (executorPool == null) {
			executorPool = Executors.newFixedThreadPool(Bdb.getExecutorPoolSize());
		}

		readOnlyFuture = executorPool.submit(new GetNidData(nid, readOnly));
		readWriteFuture = executorPool.submit(new GetNidData(nid, readWrite));
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadOnlyBytes()
	 */
	public synchronized byte[] getReadOnlyBytes() throws InterruptedException, ExecutionException, IOException {
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

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadWriteBytes()
	 */
	public  synchronized byte[] getReadWriteBytes() throws InterruptedException, ExecutionException {
		if (readWriteBytes == null) {
			readWriteBytes = readWriteFuture.get();
		}
		return readWriteBytes;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadOnlyTupleInput()
	 */
	public  synchronized TupleInput getReadOnlyTupleInput() throws InterruptedException, ExecutionException, IOException {
		return new TupleInput(getReadOnlyBytes());
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadWriteTupleInput()
	 */
	public  synchronized TupleInput getMutableTupleInput() throws InterruptedException, ExecutionException {
		return new TupleInput(getReadWriteBytes());
	}

}
