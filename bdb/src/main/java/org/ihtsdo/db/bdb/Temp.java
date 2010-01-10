package org.ihtsdo.db.bdb;

import java.awt.FileDialog;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
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
import org.ihtsdo.db.bdb.concept.Concept;
import org.ihtsdo.db.bdb.concept.I_ProcessConceptData;
import org.ihtsdo.db.bdb.concept.component.attributes.ConceptAttributesBinder;
import org.ihtsdo.db.bdb.concept.component.description.DescriptionBinder;
import org.ihtsdo.db.bdb.concept.component.refset.RefsetMemberBinder;
import org.ihtsdo.db.bdb.concept.component.relationship.RelationshipBinder;
import org.ihtsdo.etypes.EConcept;

public class Temp {
	public static void main(String[] args) {
        try {
        	
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

			    fis = new FileInputStream(idsFile);
			    bis = new BufferedInputStream(fis);
			    idsDis = new DataInputStream(bis);

	            UuidsToNidMap uuidsNidMap = new UuidsToNidMap(0, numUuidsRead);
	            Collection<UUID> uuids = new ArrayList<UUID>(5);
	            while (idsDis.available() > 0) {
			    	numIdsRead++;
			    	int uuidCount = idsDis.readInt();
			    	for (int i = 0; i < uuidCount; i++) {
			    		uuids.add(new UUID(idsDis.readLong(), idsDis.readLong()));
			    		numUuidsRead++;
			    	}
			    	uuidsNidMap.uuidsToNidWithGeneration(uuids);
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
	                        
	            FileIO.recursiveDelete(new File("target/berkeley-db"));
	        	Bdb.setup();
	            Bdb.setUuidsToNidMap(uuidsNidMap);
	        	//ConceptComponentBinder binder = new ConceptComponentBinder(null);
	            
	            
			    fis = new FileInputStream(conceptsFile);
			    bis = new BufferedInputStream(fis);
			    DataInputStream in = new DataInputStream(bis);
			    
	            while (fis.available() > 0) {
	            	conceptsRead.incrementAndGet();
	            	EConcept eConcept = new EConcept(in);
	            	I_ProcessEConcept conceptConverter = converters.take(); 
	            	conceptConverter.setEConcept(eConcept);
	            	executors.execute(conceptConverter);
			    	//Concept newConcept = Concept.get(eConcept);
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
	            
	            // Generate NID->CID map
	            
	            
	            AceLog.getAppLog().info("Starting PopulateNidCidMap");
	            
	            Bdb.getConceptDb().iterateConceptData(new PopulateNidCidMap());
	            AceLog.getAppLog().info("Finished PopulateNidCidMap");
	            AceLog.getAppLog().info("Starting ValidateNidCidMap");
	            
	            Bdb.getConceptDb().iterateConceptData(new ValidateNidCidMap());
	            AceLog.getAppLog().info("Finished ValidateNidCidMap");
	            AceLog.getAppLog().info("Starting db sync.");
	            Bdb.sync();
	            AceLog.getAppLog().info("Finishing db sync.");
	            Bdb.close();
	            AceLog.getAppLog().info("db closed");
	            
			}
		} catch (Throwable e) {
			e.printStackTrace();
            AceLog.getAppLog().info("Concept count: " + Bdb.getConceptDb().getCount());    
		} 
        System.exit(0);
	}
	
	static AtomicInteger conceptsRead = new AtomicInteger();
	static AtomicInteger conceptsProcessed = new AtomicInteger();
	
	static ExecutorService executors = Executors.newCachedThreadPool();
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
    	
    	NidCNidMap nidCnidMap = Bdb.getNidCNidMap();
		@Override
		public void processConceptData(Concept concept) throws Exception {
			
			int cNid = concept.getNid();
			int[] nids = concept.getAllNids();
			for (int nid: nids) {
				int testCNid = nidCnidMap.getCNid(nid);
				if (testCNid != cNid) {
					AceLog.getAppLog().severe("Failure in nid cid map");
				}
			}
		}
    }
    
    private static class PopulateNidCidMap implements I_ProcessConceptData {
    	
    	NidCNidMap nidCnidMap = Bdb.getNidCNidMap();
		@Override
		public void processConceptData(Concept concept) throws Exception {
			
			int cNid = concept.getNid();
			int[] nids = concept.getAllNids();
			nidCnidMap.setCidForNid(cNid, cNid);
			for (int nid: nids) {
				nidCnidMap.setCidForNid(cNid, nid);
			}
		}
    }
    
     
	private static class ConvertConcept implements I_ProcessEConcept {
		Throwable exception = null;
		EConcept eConcept = null;
		Concept newConcept = null;
		@Override
		public void run() {
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
