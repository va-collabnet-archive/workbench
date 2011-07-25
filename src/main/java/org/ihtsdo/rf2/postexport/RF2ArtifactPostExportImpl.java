package org.ihtsdo.rf2.postexport;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;


public class RF2ArtifactPostExportImpl extends RF2ArtifactPostExportAbst{

	FILE_TYPE fType;

	private File rf2FullFolder;
	private File rf2Exported;
	private File rf2FullOutputFolder;
	private File rf2DeltaOutputFolder;
	private File rf2SnapshotOutputFolder;
	private File folderTmp;
	private String previousReleaseDate;
	private String releaseDate;

	private File sortedfolderTmp;

	private File snapshotfolderTmp;

	private File snapshotFinalFile;

	private File deltaFinalFile;

	private File fullFinalFile;

	private File sortTmpfolderSortedTmp;
	
	public RF2ArtifactPostExportImpl(FILE_TYPE fType, File rf2FullFolder,
			File rf2Exported, File rf2OutputFolder, File buildDirectory,
			String previousReleaseDate, String releaseDate) throws IOException {
		super();
		this.fType = fType;
		this.rf2FullFolder = new File(rf2FullFolder.getAbsolutePath() + "/org/ihtsdo/rf2");
		this.rf2Exported = rf2Exported;
		this.previousReleaseDate = previousReleaseDate;
		this.releaseDate = releaseDate;
		
		folderTmp=new File(buildDirectory.getAbsolutePath() + "/" + getTmpPostExport() );
		if (!folderTmp.exists()){
			folderTmp.mkdir();
		}else{
			//TODO empty folder needed?
		}
		sortedfolderTmp=new File(folderTmp.getAbsolutePath() + "/" + getTmpSort());
		if (!sortedfolderTmp.exists()){
			sortedfolderTmp.mkdir();
		}else{
			//TODO empty folder needed?
		}
		sortTmpfolderSortedTmp=new File(sortedfolderTmp.getAbsolutePath() + "/" + getTmpTmpSort());
		if (!sortTmpfolderSortedTmp.exists()){
			sortTmpfolderSortedTmp.mkdir();
		}else{
			//TODO empty folder needed?getTmpTmpSort
		}
		snapshotfolderTmp=new File(folderTmp.getAbsolutePath() + "/" + getTmpSnapShot() );
		if (!snapshotfolderTmp.exists()){
			snapshotfolderTmp.mkdir();
		}else{
			//TODO empty folder needed?
		}
		if (!rf2OutputFolder.exists()){
			rf2OutputFolder.mkdir();
		}else{
			//TODO empty folder needed?
		}

		rf2FullOutputFolder=new File(rf2OutputFolder.getAbsolutePath() + "/" + getFullOutputFolder() );
		if (!rf2FullOutputFolder.exists()){
			rf2FullOutputFolder.mkdir();
		}else{
			//TODO empty folder needed?
		}
		rf2DeltaOutputFolder=new File(rf2OutputFolder.getAbsolutePath() + "/" + getDeltaOutputFolder() );
		if (!rf2DeltaOutputFolder.exists()){
			rf2DeltaOutputFolder.mkdir();
		}else{
			//TODO empty folder needed?
		}
		rf2SnapshotOutputFolder=new File(rf2OutputFolder.getAbsolutePath() + "/" + getSnapshotOutputFolder() );
		if (!rf2SnapshotOutputFolder.exists()){
			rf2SnapshotOutputFolder.mkdir();
		}else{
			//TODO empty folder needed?
		}
		fullFinalFile=getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), fType,releaseDate);
		deltaFinalFile=getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), fType,releaseDate, previousReleaseDate);
		snapshotFinalFile=getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), fType,releaseDate);
	}
	public void postProcess() throws Exception{
		File previousFile=getPreviousFile(rf2FullFolder.getAbsolutePath(),fType);
		File sortedPreviousfile=new File(sortedfolderTmp,"pre_" + previousFile.getName());
		File filterPreviousfile=new File(sortedfolderTmp,"ftr_" + previousFile.getName());
		
		ValueAnalyzer vAnl=new ValueAnalyzer(ValueAnalyzer.OPERATOR.LOWER, releaseDate);
		CommonUtils.FilterFile(previousFile, filterPreviousfile, 1, vAnl);
		
		FileSorter fsc=new FileSorter(filterPreviousfile, sortedPreviousfile, sortTmpfolderSortedTmp, fType.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		
		filterPreviousfile.delete();
		
		File sortedExportedfile=new File(sortedfolderTmp,"exp_" + rf2Exported.getName());
		fsc=new FileSorter(rf2Exported, sortedExportedfile, sortTmpfolderSortedTmp, fType.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();
		

		File snapshotSortedPreviousfile=new File(snapshotfolderTmp,"pre_" + previousFile.getName());
		SnapshotGenerator sg=new SnapshotGenerator(sortedPreviousfile, previousReleaseDate, fType.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File snapshotSortedExportedfile=new File(snapshotfolderTmp,"exp_" + rf2Exported.getName());
		sg=new SnapshotGenerator(sortedExportedfile, releaseDate, fType.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		if (fType==FILE_TYPE.RF2_ATTRIBUTE_VALUE){
			ConsolidateInactRefsetSnapshotAndDelta  cis=new ConsolidateInactRefsetSnapshotAndDelta(fType,snapshotSortedPreviousfile,snapshotSortedExportedfile,snapshotFinalFile,deltaFinalFile,releaseDate);
			cis.execute();
			cis=null;
			System.gc();
		}else{
			ConsolidateSnapshotAndDelta cs=new ConsolidateSnapshotAndDelta(fType,snapshotSortedPreviousfile,snapshotSortedExportedfile,snapshotFinalFile,deltaFinalFile,releaseDate);
			cs.execute();
			cs=null;
			System.gc();
		}
		
		HashSet<File> hFile=new HashSet<File>();
		hFile.add(sortedPreviousfile);
		hFile.add(deltaFinalFile);

		CommonUtils.MergeFile(hFile,  fullFinalFile);
		System.gc();
		
	}
	
}
