package org.ihtsdo.db.standalone;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.thread.NamedThreadFactory;

public class SecondaryLoad {
	static AtomicInteger conceptsRead = new AtomicInteger();
	static AtomicInteger conceptsProcessed = new AtomicInteger();

	static ExecutorService executors = Executors.newCachedThreadPool(new NamedThreadFactory(new ThreadGroup("secondary load"), 
	"secondary load "));
    static LinkedBlockingQueue<I_ProcessEConcept> converters = new LinkedBlockingQueue<I_ProcessEConcept>();
    private static int runtimeConverterSize = Runtime.getRuntime().availableProcessors() * 2;
    private static int converterSize = runtimeConverterSize;
    static {
        for (int i = 0; i < converterSize; i++) {
        	try {
				converters.put(new ConvertConcept());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
        }
    }

    public static void main(String[] args) {
        try {
        	long startTime = System.currentTimeMillis();
			if (true) {

	        	Bdb.setup();
	            
			    File directory = new File("/Users/kec/Documents/workspace/bdb/Export Files");

			    File conceptsFile = new File(directory, "eConcepts.jbin");

			    FileInputStream fis = new FileInputStream(conceptsFile);
			    BufferedInputStream bis = new BufferedInputStream(fis);
			    DataInputStream in = new DataInputStream(bis);
			    
			    int conceptsReread = 0;
	            while (fis.available() > 0) {
	            	conceptsReread++;
	            	EConcept eConcept = new EConcept(in);
	            	Concept newConcept = Concept.get(eConcept);

	            	Bdb.getConceptDb().getConcept(newConcept.getConceptId());
	            
	    			if (conceptsReread % 10000 == 0) {
	    				System.out.println("concepts re-read: " + conceptsReread);
	    			}
			    }
	            // See if any exceptions in the last converters;
	            while (converters.isEmpty() == false) {
	            	I_ProcessEConcept conceptConverter = converters.take();
	            	conceptConverter.setEConcept(null);
	            }
	            
	            while (conceptsProcessed.get() < conceptsRead.get()) {
					Thread.sleep(1000);
	            }
	            
	            AceLog.getAppLog().info(" Finished re-read, starting close. Elapsed time: " + 
	            		(System.currentTimeMillis() - startTime));
	            Bdb.close();
	            AceLog.getAppLog().info("Closed. Elapsed time: " + 
	            		(System.currentTimeMillis() - startTime));
	            
	            
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} 
        System.exit(0);
	}

	private static class ConvertConcept implements I_ProcessEConcept {
		Throwable exception = null;
		EConcept eConcept = null;
		Concept newConcept = null;
    	NidCNidMapBdb nidCnidMap;
		@Override
		public void run() {
			if (nidCnidMap == null) {
				nidCnidMap = Bdb.getNidCNidMap();
			}
			try {
				newConcept = Concept.get(eConcept);
				Bdb.getConceptDb().writeConcept(newConcept);
				conceptsProcessed.incrementAndGet();
			} catch (Throwable e) {
				exception = e;
			}
			try {
				converters.put(this);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		/* (non-Javadoc)
		 * @see org.ihtsdo.db.bdb.I_ProcessEConcept#setEConcept(org.ihtsdo.etypes.EConcept)
		 */
		public void setEConcept(EConcept eConcept) throws Throwable {
			if (exception != null) {
				throw exception;
			}
			this.eConcept = eConcept;
		}
	}
}
