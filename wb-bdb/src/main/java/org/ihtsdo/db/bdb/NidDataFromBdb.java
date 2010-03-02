package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.ihtsdo.thread.NamedThreadFactory;

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
	private static ThreadGroup nidDataThreadGroup  = 
		new ThreadGroup("nid data threads");

	private static ExecutorService executorPool;

	public NidDataFromBdb(int nid, Database readOnly, Database readWrite) {
		super();
		this.nid = nid;
		this.readOnly = readOnly;
		
		if (executorPool == null) {
			executorPool = Executors.newCachedThreadPool(new NamedThreadFactory(nidDataThreadGroup,
			"Nid data service"));
		}

		readOnlyFuture = executorPool.submit(new GetNidData(nid, readOnly));
		readWriteFuture = executorPool.submit(new GetNidData(nid, readWrite));
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadOnlyBytes()
	 */
	public synchronized byte[] getReadOnlyBytes() throws IOException {
		if (readOnlyBytes == null) {
			try {
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
			} catch (InterruptedException e) {
				throw new IOException(e);
			} catch (ExecutionException e) {
				throw new IOException(e);
			}
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
	public  synchronized byte[] getReadWriteBytes() throws IOException {
		if (readWriteBytes == null) {
			try {
				readWriteBytes = readWriteFuture.get();
			} catch (InterruptedException e) {
				throw new IOException(e);
			} catch (ExecutionException e) {
				throw new IOException(e);
			}
		}
		return readWriteBytes;
	}
	
	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadOnlyTupleInput()
	 */
	public  synchronized TupleInput getReadOnlyTupleInput() throws IOException {
		return new TupleInput(getReadOnlyBytes());
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadWriteTupleInput()
	 */
	public  synchronized TupleInput getMutableTupleInput() throws IOException {
		return new TupleInput(getReadWriteBytes());
	}

	@Override
	public boolean isPrimordial() throws IOException {
		return getReadOnlyBytes().length == 0 && getReadWriteBytes().length == 0;
	}

}
