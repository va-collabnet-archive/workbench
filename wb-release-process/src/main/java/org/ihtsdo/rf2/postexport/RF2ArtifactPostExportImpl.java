package org.ihtsdo.rf2.postexport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.ihtsdo.rf2.identifier.mojo.RefSetParam;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;


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

	private ArrayList<RefSetParam> refsetData;

	private String exportFolder;


	public RF2ArtifactPostExportImpl(FILE_TYPE fType, File rf2FullFolder,
			File rf2OutputFolder, File buildDirectory,
			String previousReleaseDate, String releaseDate,
			ArrayList<RefSetParam> refsetData,String fileExtension,
			String languageCode,String namespace,String exportFolder) throws IOException {
		this( fType,  rf2FullFolder,
				null,  rf2OutputFolder,  buildDirectory,
				previousReleaseDate,  releaseDate);
		this.exportFolder=exportFolder;
		this.fileExtension="." + fileExtension;
		this.refsetData=refsetData;
		if (languageCode!=null && !languageCode.equals("")){
			this.langCode="-" + languageCode;
		}else{
			this.langCode="";
		}
		this.namespace=namespace;
	}

	public RF2ArtifactPostExportImpl(FILE_TYPE fType, File rf2FullFolder,
			File rf2Exported,File rf2OutputFolder, File buildDirectory,
			String previousReleaseDate, String releaseDate,
			String fileExtension,
			String languageCode,String namespace) throws IOException {
		this( fType,  rf2FullFolder,
				rf2Exported,  rf2OutputFolder,  buildDirectory,
				previousReleaseDate,  releaseDate);
		this.fileExtension="." + fileExtension;
		if (languageCode!=null && !languageCode.equals("")){
			this.langCode="-" + languageCode;
		}else{
			this.langCode="";
		}
		this.namespace=namespace;
	}

	public RF2ArtifactPostExportImpl(FILE_TYPE fType, File rf2FullFolder,
			File rf2Exported, File rf2OutputFolder, File buildDirectory,
			String previousReleaseDate, String releaseDate) throws IOException {
		super();
		this.fileExtension=".txt";
		this.langCode="-en";
		this.namespace="INT";
		this.fType = fType;
		//		this.rf2FullFolder = new File(rf2FullFolder.getAbsolutePath() + "/org/ihtsdo/rf2");
		this.rf2FullFolder = new File(rf2FullFolder.getAbsolutePath() );
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
			rf2OutputFolder.mkdirs();
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
	}
	public void process() throws Exception{
		File previousFile=null;
		File sortedExportedfile=null;
		if (refsetData!=null){
			for (RefSetParam refsetParam:refsetData){
				rf2Exported=new File(exportFolder, refsetParam.refsetFileName + releaseDate + fileExtension);
				sortedExportedfile=new File(sortedfolderTmp,"exp_" + refsetParam.refsetFileName + releaseDate + fileExtension);

				fullFinalFile=new File(rf2FullOutputFolder.getAbsolutePath(),  refsetParam.refsetFileName + "Full" + langCode + "_" + namespace + "_" + releaseDate + fileExtension);
				deltaFinalFile=new File(rf2DeltaOutputFolder.getAbsolutePath(),  refsetParam.refsetFileName + "Delta" + langCode + "_" + namespace + "_" + releaseDate + fileExtension);
				snapshotFinalFile=new File(rf2SnapshotOutputFolder.getAbsolutePath(),  refsetParam.refsetFileName + "Snapshot" + langCode + "_" + namespace + "_" + releaseDate + fileExtension);
				previousFile=getPreviousFileByCustomName(rf2FullFolder,refsetParam.refsetFileName.toLowerCase());
				postProcess( previousFile, sortedExportedfile);
			}
			return;
		}
		fullFinalFile=getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), fType,releaseDate);
		deltaFinalFile=getDeltaOutputFile(rf2DeltaOutputFolder.getAbsolutePath(), fType,releaseDate);
		snapshotFinalFile=getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(), fType,releaseDate);
		previousFile=getPreviousFile(rf2FullFolder.getAbsolutePath(),fType);
		sortedExportedfile=new File(sortedfolderTmp,"exp_" + rf2Exported.getName());
		postProcess( previousFile, sortedExportedfile);
	}


	private void postProcess(File previousFile,File sortedExportedfile) throws Exception{
		File sortedPreviousfile=new File(sortedfolderTmp,"pre_" + previousFile.getName());

		FileSorter fsc=new FileSorter(previousFile, sortedPreviousfile, sortTmpfolderSortedTmp, fType.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();

		fsc=new FileSorter(rf2Exported, sortedExportedfile, sortTmpfolderSortedTmp, fType.getColumnIndexes());
		fsc.execute();
		fsc=null;
		System.gc();

		File snapshotSortedPreviousfile=new File(snapshotfolderTmp,"pre_" + previousFile.getName());
		SnapshotGeneratorMultiColumn sg=new SnapshotGeneratorMultiColumn(sortedPreviousfile, previousReleaseDate, fType.getSnapshotIndex(), fType.getEffectiveTimeColIndex(), snapshotSortedPreviousfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		File snapshotSortedExportedfile=new File(snapshotfolderTmp,"exp_" + rf2Exported.getName());
		sg=new SnapshotGeneratorMultiColumn(sortedExportedfile, releaseDate, fType.getSnapshotIndex(), fType.getEffectiveTimeColIndex(), snapshotSortedExportedfile, null, null);
		sg.execute();
		sg=null;
		System.gc();

		if (fType==FILE_TYPE.RF2_ATTRIBUTE_VALUE){
			ConsolidateInactRefsetSnapshotAndDelta  cis=new ConsolidateInactRefsetSnapshotAndDelta(fType,snapshotSortedPreviousfile,snapshotSortedExportedfile,snapshotFinalFile,deltaFinalFile,releaseDate);
			cis.execute();
			cis=null;
			System.gc();
		}else if (fType==FILE_TYPE.RF2_RELATIONSHIP || fType==FILE_TYPE.RF2_ISA_RETIRED){
			ConsolidateInfRelsSnapshotAndDelta  cis=new ConsolidateInfRelsSnapshotAndDelta(fType,snapshotSortedPreviousfile,snapshotSortedExportedfile,snapshotFinalFile,deltaFinalFile,releaseDate);
			cis.execute();
			cis=null;
			System.gc();
		}else if (fType==FILE_TYPE.RF2_ASSOCIATION || fType==FILE_TYPE.RF2_SIMPLE || fType==FILE_TYPE.RF2_SIMPLE_MAP ){
			ConsolidateSnapshotAndDeltaRefset cs=new ConsolidateSnapshotAndDeltaRefset(fType,snapshotSortedPreviousfile,snapshotSortedExportedfile,snapshotFinalFile,deltaFinalFile,releaseDate);
			cs.execute();
			cs=null;
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
