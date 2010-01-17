package org.ihtsdo.db.standalone;

import java.awt.FileDialog;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.NidCNidMapBdb;
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_ProcessConceptData;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionBinder;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.etypes.EConcept;

public class BinaryLoad {
	public static void main(String[] args) {
        try {
        	long startTime = System.currentTimeMillis();
			FileDialog fd = new FileDialog(new JFrame(), "Select ids file", FileDialog.LOAD);
			fd.setDirectory(System.getProperty("user.dir"));
			
			//fd.setVisible(true); // Display dialog and wait for response
			//if (fd.getFile() != null) {
			if (true) {
			    //File idsFile = new File(fd.getDirectory(), fd.getFile());
			    File idsFile = new File("/Users/kec/Documents/workspace/bdb/Export Files/uuids.jbin");
			    System.out.println(idsFile);
			    FileInputStream fis = new FileInputStream(idsFile);
			    BufferedInputStream bis = new BufferedInputStream(fis);
			    DataInputStream idsDis = new DataInputStream(bis);
			    
			    File metaFile = new File(idsFile.getParent(), "exportData.xml");
			    File conceptsFile = new File(idsFile.getParent(), "eConcepts.jbin");
			    Properties dataProps = new Properties();
			    dataProps.loadFromXML(new FileInputStream(metaFile));
			    
			    int numIdsRead = 0;
			    int numUuidsRead = 0;
	            AceLog.getAppLog().info("Starting populateHashMap\n\n");
	            Runtime.getRuntime().gc();
	            AceLog.getAppLog().info("freeMemory: " + Runtime.getRuntime().freeMemory());
	            AceLog.getAppLog().info("maxMemory: " + Runtime.getRuntime().maxMemory());
	            AceLog.getAppLog().info("totalMemory: " + Runtime.getRuntime().totalMemory());

	            FileIO.recursiveDelete(new File("target/berkeley-db"));
	        	Bdb.setup();

	        	boolean preload = false;
	        	if (preload) {
		        	preloadIdentifiers(idsFile, numIdsRead, numUuidsRead);
	        	}
	            
	            
			    fis = new FileInputStream(conceptsFile);
			    bis = new BufferedInputStream(fis);
			    DataInputStream in = new DataInputStream(bis);
			    
	            while (fis.available() > 0) {
	            	conceptsRead.incrementAndGet();
	            	EConcept eConcept = new EConcept(in);
	            	I_ProcessEConcept conceptConverter = converters.take(); 
	            	conceptConverter.setEConcept(eConcept);
	            	executors.execute(conceptConverter);
	    			if (conceptsRead.get() % 10000 == 0) {
	    				System.out.println("concepts: " + conceptsRead);
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
	            
	            System.out.println();
	            AceLog.getAppLog().info("\n\nconceptsRead: " + conceptsRead);
	            AceLog.getAppLog().info("\n\nconceptsProcessed: " + conceptsProcessed);
	            AceLog.getAppLog().info("\n\nFinished conceptRead");
	            AceLog.getAppLog().info("freeMemory: " + Runtime.getRuntime().freeMemory());
	            AceLog.getAppLog().info("maxMemory: " + Runtime.getRuntime().maxMemory());
	            AceLog.getAppLog().info("totalMemory: " + Runtime.getRuntime().totalMemory());
	            
	            AceLog.getAppLog().info("finished load, start sync");    
	            AceLog.getAppLog().info("Concept count: " + Bdb.getConceptDb().getCount());    
	            AceLog.getAppLog().info("Concept attributes encountered: " + ConceptAttributesBinder.encountered +
	            		" written: " + ConceptAttributesBinder.written);    
	            AceLog.getAppLog().info("Descriptions encountered: " + DescriptionBinder.encountered +
	            		" written: " + DescriptionBinder.written);    
	            AceLog.getAppLog().info("Relationships encountered: " + RelationshipBinder.encountered +
	            		" written: " + RelationshipBinder.written);    
	            AceLog.getAppLog().info("Reset members encountered: " + RefsetMemberBinder.encountered +
	            		" written: " + RefsetMemberBinder.written);    
	            
	            AceLog.getAppLog().info("Starting ValidateNidCidMap");	            
	            Bdb.getConceptDb().iterateConceptData(new ValidateNidCidMap());
	            AceLog.getAppLog().info("Finished ValidateNidCidMap");
	            AceLog.getAppLog().info("Starting db sync.");
	            Bdb.sync();
	            AceLog.getAppLog().info("Finished db sync, starting close.");
	            Bdb.close();
	            AceLog.getAppLog().info("db closed");
	            AceLog.getAppLog().info("elapsed time: " + (System.currentTimeMillis() - startTime));
	            
	            FileIO.recursiveDelete(new File("target/berkeley-db/read-only"));
	            File dirToMove = new File("target/berkeley-db/mutable");
	            dirToMove.renameTo(new File("target/berkeley-db/read-only"));
	        	Bdb.setup();
	            
			    fis = new FileInputStream(conceptsFile);
			    bis = new BufferedInputStream(fis);
			    in = new DataInputStream(bis);
			    
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


	private static void preloadIdentifiers(File idsFile, int numIdsRead,
			int numUuidsRead) throws FileNotFoundException, IOException {
		FileInputStream fis;
		BufferedInputStream bis;
		DataInputStream idsDis;
		fis = new FileInputStream(idsFile);
		bis = new BufferedInputStream(fis);
		idsDis = new DataInputStream(bis);

		

		Collection<UUID> uuids = new ArrayList<UUID>(5);
		while (idsDis.available() > 0) {
			numIdsRead++;
			int uuidCount = idsDis.readInt();
			for (int i = 0; i < uuidCount; i++) {
				uuids.add(new UUID(idsDis.readLong(), idsDis.readLong()));
				numUuidsRead++;
			}
			Bdb.uuidsToNid(uuids);
			uuids.clear();
			if (numIdsRead % 100000 == 0) {
				System.out.print("\nids: " + numIdsRead);
			}
		}
		System.out.println();
		idsDis.close();
		Runtime.getRuntime().gc();
		AceLog.getAppLog().info("\n\nFinished populateHashMap");
		AceLog.getAppLog().info("freeMemory: " + Runtime.getRuntime().freeMemory());
		AceLog.getAppLog().info("maxMemory: " + Runtime.getRuntime().maxMemory());
		AceLog.getAppLog().info("totalMemory: " + Runtime.getRuntime().totalMemory());
		AceLog.getAppLog().info("\nconverterSize: " + converterSize);
	}
	
	static AtomicInteger conceptsRead = new AtomicInteger();
	static AtomicInteger conceptsProcessed = new AtomicInteger();
	
	static ExecutorService executors = Executors.newCachedThreadPool();
    static LinkedBlockingQueue<I_ProcessEConcept> converters = new LinkedBlockingQueue<I_ProcessEConcept>();
    private static int runtimeConverterSize = Runtime.getRuntime().availableProcessors() * 2;;
    private static int converterSize = 1;
    static {
        for (int i = 0; i < converterSize; i++) {
        	try {
				converters.put(new ConvertConcept());
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
        }
    }

    private static class ValidateNidCidMap implements I_ProcessConceptData {
    	
    	NidCNidMapBdb nidCnidMap = Bdb.getNidCNidMap();
		@Override
		public void processConceptData(Concept concept) throws Exception {
			
			int cNid = concept.getNid();
			int testCNid = nidCnidMap.getCNid(cNid);
			test(cNid, testCNid);
			int[] nids = concept.getAllNids();
			for (int nid: nids) {
				testCNid = nidCnidMap.getCNid(nid);
				test(cNid, testCNid);
			}
		}
		private void test(int cNid, int testCNid) throws Exception {
			if (testCNid != cNid) {
				AceLog.getAppLog().severe("Failure in nid cid map. cNid: " + cNid + " testCNid: " + testCNid);
				throw new Exception("Failure in nid cid map");
			}
		}
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
