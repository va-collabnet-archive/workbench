package org.ihtsdo.rf2.identifier.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import junit.framework.TestCase;

import org.ihtsdo.rf2.postexport.CommonUtils;
import org.ihtsdo.rf2.postexport.ConsolidateQualSnapshotAndDelta;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportImpl;
import org.ihtsdo.rf2.postexport.SnapshotGenerator;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;


public class TestRF2RelationshipIDRetrieve extends TestCase{

	
	public void xtestIDReassignProcess() {
		File sortTmpfolder=new File("/Users/ar/Downloads/testIDReassign/tmp");
		File previousFile=new File("/Users/ar/Documents/WS_Work/rf2-full-release-files/src/main/org/ihtsdo/rf2/Terminology/sct2_Relationship_Full_INT_20110131.txt");
		File exportedFile=new File("/Users/ar/Documents/WS_Work/exportedfiles/sct2_Relationship_Full_INT_20110731.txt");
		
		File sortedPreviousfile=new File("/Users/ar/Downloads/testIDReassign/sortpre_" + previousFile.getName());	
		FileSorter fsc=new FileSorter(previousFile, sortedPreviousfile, sortTmpfolder, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		
		
		File sortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/sortexp_" + previousFile.getName());
		fsc=new FileSorter(exportedFile, sortedExportedfile, sortTmpfolder, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		
		
		File snapshotSortedPreviousfile=new File("/Users/ar/Downloads/testIDReassign/snappre_" + previousFile.getName());
		SnapshotGenerator sg=new SnapshotGenerator(sortedPreviousfile, "20110131", FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File snapshotSortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/snapexp_" + previousFile.getName());
		sg=new SnapshotGenerator(sortedExportedfile, "20110731",FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
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
		
		
		File rf2OutputRelationships=new File("/Users/ar/Downloads/testIDReassign/outputRF2RelationshipReassigned.txt");
		File outputUUIDsToAssign=new File("/Users/ar/Downloads/testIDReassign/outputUUIDsToAssign.txt");
		File outputDifferences=new File("/Users/ar/Downloads/testIDReassign/outputDifferences.txt");
		
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
	public void xtestReassignQual(){
		File sortedSnapPreviousfile=new File("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/target/tmppostexport/tmpsnapshot/sortSnappre_sct2_Qualifier_Full_INT_20110731.txt");
		File sortedSnapExportedfile=new File("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/target/tmppostexport/tmpsnapshot/sortSnapexp_xsct2_Qualifiers_Full_INT_20120131.txt");
		File rf2OutputRelationships=new File("/Users/ar/Downloads/testIDReassign/outputRF2RelationshipReassigned.txt");
		File outputUUIDsToAssign=new File("/Users/ar/Downloads/testIDReassign/outputUUIDsToAssign.txt");
		File outputDifferences=new File("/Users/ar/Downloads/testIDReassign/outputDifferences.txt");
		
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
	
	public void xtestPostExport(){
		RF2ArtifactPostExportImpl rpi;
		try {
			rpi = new RF2ArtifactPostExportImpl(FILE_TYPE.RF2_QUALIFIER, new File("/Users/ar/Documents/WS_Work/rf2-rf1-conversion/xres2_Compatibility_Package_INT_20110731")
					,new File ("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/destination/tmp/sct2_Qualifier_Full_INT_20120131.txt"), new File ("/Users/ar/Downloads/testIDReassign"), new File ("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/target"),
					"20110731", "20120131");

			rpi.postProcess();
			rpi=null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.gc();
	}
	public void testQualsConsolidate(){

		File sortTmpfolder=new File("/Users/ar/Downloads/testIDReassign/tmp");
		File fullFinalFile=new File("/Users/ar/Downloads/testIDReassign/Full/sct2_Qualifier_Full_INT_20120131.txt");
		File deltaFinalFile=new File("/Users/ar/Downloads/testIDReassign/Delta/sct2_Qualifier_Delta_INT_20120131.txt");
		File snapshotFinalFile=new File("/Users/ar/Downloads/testIDReassign/Snapshot/sct2_Qualifier_Snapshot_INT_20120131.txt");
		File exportedFile=new File("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/destination/tmp/sct2_Qualifier_Full_INT_20120131.txt");
		
		File sortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/sortexp_" + exportedFile.getName());
		FileSorter fsc=new FileSorter(exportedFile, sortedExportedfile, sortTmpfolder, FILE_TYPE.RF2_QUALIFIER.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		

		File snapshotSortedExportedfile=new File("/Users/ar/Downloads/testIDReassign/snapexp_" + exportedFile.getName());
		SnapshotGenerator sg=new SnapshotGenerator(sortedExportedfile, "20120131",FILE_TYPE.RF2_QUALIFIER.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
		sg.execute();
		sg=null;
		System.gc();
		
		ConsolidateQualSnapshotAndDelta cq = new ConsolidateQualSnapshotAndDelta(FILE_TYPE.RF2_QUALIFIER,
				new File("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/target/tmppostexport/tmpsnapshot/pre_sct2_Qualifier_Full_INT_20110731.txt"),
				 snapshotSortedExportedfile,
				 snapshotFinalFile,  deltaFinalFile, "20120131");
		
		try {
			cq.execute();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		cq=null;
		System.gc();
		HashSet<File> hFile=new HashSet<File>();
		hFile.add(new File("/Users/ar/Documents/WS_Work/CAP_export-runner -LOCAL/target/tmppostexport/tmpsort/pre_sct2_Qualifier_Full_INT_20110731.txt"));
		hFile.add(deltaFinalFile);

		CommonUtils.MergeFile(hFile,  fullFinalFile);
		System.gc();
	}
	
}
