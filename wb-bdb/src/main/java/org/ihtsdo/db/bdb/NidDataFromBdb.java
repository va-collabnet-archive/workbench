package org.ihtsdo.db.bdb;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.thread.NamedThreadFactory;

import com.sleepycat.bind.tuple.TupleInput;

public class NidDataFromBdb implements I_GetNidData {

	public static enum REF_TYPE {SOFT, WEAK};
	
	private static REF_TYPE refType = REF_TYPE.SOFT;

	private Future<byte[]> readOnlyFuture;
	private Future<byte[]> readWriteFuture;
	
	private int nid;
	private Reference<byte[]> readOnlyBytes;
	private byte[] readWriteBytes;
	private static ThreadGroup nidDataThreadGroup  = new ThreadGroup("nid data threads");

	private static ExecutorService executorPool;
	
	static {
		resetExecutorPool();
	}

	public static void resetExecutorPool() {
		executorPool = Executors.newFixedThreadPool(
                        Math.min(6, Runtime.getRuntime().availableProcessors() + 1),
                        new NamedThreadFactory(nidDataThreadGroup,
				"Nid data service"));
	}
	
	public static void close() {
	    if (executorPool != null) {
	        AceLog.getAppLog().info("Shutting down NidDataFromBdb executor pool.");
	        executorPool.shutdown();
	        AceLog.getAppLog().info("Awaiting termination of NidDataFromBdb executor pool.");
	        try {
	            executorPool.awaitTermination(90, TimeUnit.MINUTES);
	        } catch (InterruptedException e) {
	            AceLog.getAppLog().warning(e.toString());
	        }
            AceLog.getAppLog().info("Termination NidDataFromBdb executor pool.");
	    }
        executorPool = null;
	}

	public NidDataFromBdb(int nid) {
		super();
		this.nid = nid;
		
		readOnlyFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadOnly()));
		readWriteFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadWrite()));
	}
	
	public synchronized void reset() {
		readWriteBytes = null;
		readWriteFuture = null;
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadOnlyBytes()
	 */
	public synchronized byte[] getReadOnlyBytes() throws IOException {
		if (readOnlyBytes == null) {
		    if (readOnlyFuture == null) {
		        readOnlyFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadOnly()));
		    }
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
				readOnlyFuture = null;
				return bytes;
			} catch (InterruptedException e) {
				throw new IOException(e);
			} catch (ExecutionException e) {
				throw new IOException(e);
			}
		}
		byte[] bytes = readOnlyBytes.get();
		if (bytes != null) {
		    return bytes;
		}
		readOnlyBytes = null;
		return getReadOnlyBytes();
	}

	/* (non-Javadoc)
	 * @see org.ihtsdo.db.bdb.I_GetNidData#getReadWriteBytes()
	 */
	public  synchronized byte[] getReadWriteBytes() throws IOException {
		if (readWriteBytes == null) {
            if (readWriteFuture == null) {
                readWriteFuture = executorPool.submit(new GetNidData(nid, Bdb.getConceptDb().getReadWrite()));
                return getReadWriteBytes();
            }
			try {
				readWriteBytes = readWriteFuture.get();
				readWriteFuture = null;
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
