package org.ihtsdo.db.standalone;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.I_ProcessConceptData;
import org.ihtsdo.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.concept.component.description.DescriptionBinder;
import org.ihtsdo.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.id.NidCNidMapBdb;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.thread.NamedThreadFactory;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.description.TkDescriptionRevision;

public class BinaryLoad {
	public static void main(String[] args) {
        try {
        	long startTime = System.currentTimeMillis();
			if (true) {
			    File conceptsFile = new File("/Users/kec/Documents/workspace/bdb/Export Files/eConcepts.jbin");

			    
			    int numIdsRead = 0;
			    int numUuidsRead = 0;
	            AceLog.getAppLog().info("Starting populateHashMap\n\n");
	            Runtime.getRuntime().gc();
	            AceLog.getAppLog().info("freeMemory: " + Runtime.getRuntime().freeMemory());
	            AceLog.getAppLog().info("maxMemory: " + Runtime.getRuntime().maxMemory());
	            AceLog.getAppLog().info("totalMemory: " + Runtime.getRuntime().totalMemory());

	            FileIO.recursiveDelete(new File("target/berkeley-db"));
	        	Bdb.setup("target/berkeley-db");

	            
			    FileInputStream fis = new FileInputStream(conceptsFile);
			    BufferedInputStream bis = new BufferedInputStream(fis);
			    DataInputStream in = new DataInputStream(bis);
			    try {
		            while (true) {
		            	conceptsRead.incrementAndGet();
		            	EConcept eConcept = new EConcept(in);
		            	if (eConcept.getConceptAttributes().getUuids().contains(
		            		UUID.fromString("181e45e8-b05a-33da-8b52-7027cbee6856")) ||
		            		eConcept.getConceptAttributes().primordialUuid.getMostSignificantBits() ==
		            			1737903371905020890L) {
		            		System.out.println("Found it...");
		            	}
		            	for (TkDescription d: eConcept.getDescriptions()) {
		            		if (d.revisions != null) {
		            			if (d.getText().startsWith("concept retired")) {
		            				System.out.println("Found by desc: " + eConcept);
		            			}
			            		for (TkDescriptionRevision dv: d.getRevisions()) {
			            			if (dv.getText().startsWith("concept retired")) {
			            				System.out.println("Found by desc: " + eConcept);
			            			}
			            		}
		            		}
		            	}
		            	
		            	if (conceptsRead.get() > 649896) {
	        				System.out.println("count: " + conceptsRead.get());
	        				System.out.println("Found by count: " + eConcept);
		            	}
		            	//I_ProcessEConcept conceptConverter = converters.take(); 
		            	//conceptConverter.setEConcept(eConcept);
		            	//executors.execute(conceptConverter);
		    			if (conceptsRead.get() % 10000 == 0) {
		    				System.out.println("concepts: " + conceptsRead);
		    			}	
				    }
			    } catch (EOFException ex)  {
			    	in.close();
			    }
				System.out.println("concepts read: " + conceptsRead.get());
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
	            Bdb.getConceptDb().iterateConceptDataInSequence(new ValidateNidCidMap());
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
	
	static ExecutorService executors = Executors.newCachedThreadPool(new NamedThreadFactory(new ThreadGroup("binary load"), 
			"binary load "));
    static LinkedBlockingQueue<I_ProcessEConcept> converters = new LinkedBlockingQueue<I_ProcessEConcept>();
    private static int runtimeConverterSize = Runtime.getRuntime().availableProcessors() * 2;;
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

    private static class ValidateNidCidMap implements I_ProcessConceptData {
    	
    	NidCNidMapBdb nidCnidMap = Bdb.getNidCNidMap();
		@Override
		public void processConceptData(Concept concept) throws Exception {
			
			int cNid = concept.getNid();
			int testCNid = nidCnidMap.getCNid(cNid);
			test(cNid, testCNid);
			Collection<Integer> nids = concept.getAllNids();
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
		@Override
		public boolean continueWork() {
			return true;
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
