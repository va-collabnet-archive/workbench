package org.ihtsdo.rf2.identifier.impl;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.SnapshotGeneratorMultiColumn;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;


public class TestRF2RetiredIsaRelationshipIDRetrieve extends TestCase{

	
	public void testIDReassignProcess() {
		File sortTmpfolder=new File("/Users/ar/Downloads/testIDReassign/tmp");
		File previousFile=new File("/Users/ar/Documents/WS_Work/rf2-rf1-conversion/xres2_Compatibility_Package_INT_20110731/xres2_RetiredIsaRelationship_Full_INT_20110731.txt");
		File exportedFile=new File("/Users/ar/Documents/WS_Work/rf2-rf1-conversion/xres2_Compatibility_Package_INT_20110731/xres2_RetiredIsaRelationship_Full_INT_20110731.txt");
		
		File sortedPreviousfile=new File("/Users/ar/Downloads/testIDReassign/sortpre_" + previousFile.getName());	
		FileSorter fsc=new FileSorter(previousFile, sortedPreviousfile, sortTmpfolder, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		
		
		File sortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/sortpre_" + previousFile.getName());
//		fsc=new FileSorter(exportedFile, sortedExportedfile, sortTmpfolder, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
//		fsc.execute();
//		fsc=null;
//		System.gc();
		
		
		File snapshotSortedPreviousfile=new File("/Users/ar/Downloads/testIDReassign/snappre_" + previousFile.getName());
		SnapshotGeneratorMultiColumn sg=new SnapshotGeneratorMultiColumn(sortedPreviousfile, "20110131", FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File snapshotSortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/snapexp_" + previousFile.getName());
		sg=new SnapshotGeneratorMultiColumn(sortedExportedfile, "20110731",FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File sortedSnapPreviousfile=new File("/Users/ar/Downloads/testIDReassign/sortSnappre_" + previousFile.getName());	
		fsc=new FileSorter(snapshotSortedPreviousfile, sortedSnapPreviousfile, sortTmpfolder,new int[]{4,7,5,2,1,6});
		fsc.execute();
		fsc=null;
		System.gc();
//		
		
		File sortedSnapExportedfile=new File("/Users/ar/Downloads/testIDReassign/sortSnapexp_" + previousFile.getName());
		fsc=new FileSorter(snapshotSortedExportedfile, sortedSnapExportedfile, sortTmpfolder, new int[]{4,7,5,2,1,6});
		fsc.execute();
		fsc=null;
		System.gc();
		
		
		File rf2OutputRelationships=new File("/Users/ar/Downloads/testIDReassign/outputRF2RetiredIsaRelationshipReassigned.txt");
		File outputUUIDsToAssign=new File("/Users/ar/Downloads/testIDReassign/outputRetiredIsaUUIDsToAssign.txt");
		File outputDifferences=new File("/Users/ar/Downloads/testIDReassign/outputRetiredIsaDifferences.txt");
		
		try {
			RF2RelsIDRetrieveImpl rIdReassign=new RF2RelsIDRetrieveImpl(sortedSnapPreviousfile, sortedSnapExportedfile,
					rf2OutputRelationships, outputUUIDsToAssign, outputDifferences);
			
			rIdReassign.execute();
			rIdReassign=null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.gc();
		
	}

	public void testRetStatedIsasIDReassignProcess() {
		File sortTmpfolder=new File("/Users/ar/Downloads/testIDReassign/tmp");
		File previousFile=new File("/Users/ar/Documents/WS_Work/rf2-rf1-conversion/xres2_Compatibility_Package_INT_20110731/xres2_RetiredStatedIsaRelationship_Full_INT_20110731.txt");
		File exportedFile=new File("/Users/ar/Documents/WS_Work/rf2-rf1-conversion/xres2_Compatibility_Package_INT_20110731/xres2_RetiredStatedIsaRelationship_Full_INT_20110731.txt");
		
		File sortedPreviousfile=new File("/Users/ar/Downloads/testIDReassign/sortpre_" + previousFile.getName());	
		FileSorter fsc=new FileSorter(previousFile, sortedPreviousfile, sortTmpfolder, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		
		
		File sortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/sortpre_" + previousFile.getName());
//		fsc=new FileSorter(exportedFile, sortedExportedfile, sortTmpfolder, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
//		fsc.execute();
//		fsc=null;
//		System.gc();
		
		
		File snapshotSortedPreviousfile=new File("/Users/ar/Downloads/testIDReassign/snappre_" + previousFile.getName());
		SnapshotGeneratorMultiColumn sg=new SnapshotGeneratorMultiColumn(sortedPreviousfile, "20110131", FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File snapshotSortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/snapexp_" + previousFile.getName());
		sg=new SnapshotGeneratorMultiColumn(sortedExportedfile, "20110731",FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File sortedSnapPreviousfile=new File("/Users/ar/Downloads/testIDReassign/sortSnappre_" + previousFile.getName());	
		fsc=new FileSorter(snapshotSortedPreviousfile, sortedSnapPreviousfile, sortTmpfolder,new int[]{4,7,5,2,1,6});
		fsc.execute();
		fsc=null;
		System.gc();
//		
		
		File sortedSnapExportedfile=new File("/Users/ar/Downloads/testIDReassign/sortSnapexp_" + previousFile.getName());
		fsc=new FileSorter(snapshotSortedExportedfile, sortedSnapExportedfile, sortTmpfolder, new int[]{4,7,5,2,1,6});
		fsc.execute();
		fsc=null;
		System.gc();
		
		
		File rf2OutputRelationships=new File("/Users/ar/Downloads/testIDReassign/outputRF2RetiredStatedIsaRelationshipReassigned.txt");
		File outputUUIDsToAssign=new File("/Users/ar/Downloads/testIDReassign/outputRetiredStatedIsaUUIDsToAssign.txt");
		File outputDifferences=new File("/Users/ar/Downloads/testIDReassign/outputRetiredStatedIsaDifferences.txt");
		
		try {
			RF2RelsIDRetrieveImpl rIdReassign=new RF2RelsIDRetrieveImpl(sortedSnapPreviousfile, sortedSnapExportedfile,
					rf2OutputRelationships, outputUUIDsToAssign, outputDifferences);
			
			rIdReassign.execute();
			rIdReassign=null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.gc();
		
	}
	
}
