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

import javax.swing.JFrame;

import org.dwfa.ace.log.AceLog;
import org.dwfa.util.io.FileIO;
import org.ihtsdo.db.bdb.concept.Concept;
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
	            
	            FileIO.recursiveDelete(new File("target/berkeley-db"));
	            Bdb.setUuidsToNidMap(uuidsNidMap);
	            
	            
			    fis = new FileInputStream(conceptsFile);
			    bis = new BufferedInputStream(fis);
			    DataInputStream in = new DataInputStream(bis);
			    int conceptsRead = 0;
	            while (fis.available() > 0) {
	            	conceptsRead++;
	            	EConcept eConcept = new EConcept(in);
			    	Concept newConcept = Concept.get(eConcept);
	    			if (conceptsRead % 10000 == 0) {
	    				System.out.print("\nconcepts: " + conceptsRead);
	    			}
	            		
			    }
	            System.out.println();
	            AceLog.getAppLog().info("\n\nconceptsRead: " + conceptsRead);
	            AceLog.getAppLog().info("\n\nFinished conceptRead");
	            AceLog.getAppLog().info("freeMemory: " + Runtime.getRuntime().freeMemory());
	            AceLog.getAppLog().info("maxMemory: " + Runtime.getRuntime().maxMemory());
	            AceLog.getAppLog().info("totalMemory: " + Runtime.getRuntime().totalMemory());
	            
	            Bdb.sync();
	            
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} 
        System.exit(0);
	}
}
