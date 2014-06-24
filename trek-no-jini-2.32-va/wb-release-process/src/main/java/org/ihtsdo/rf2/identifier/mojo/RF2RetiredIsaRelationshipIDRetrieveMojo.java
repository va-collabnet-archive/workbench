package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.identifier.impl.RF2RelsIDRetrieveImpl;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.postexport.AuxiliaryFilesRetrieve;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2FileRetrieve;
import org.ihtsdo.rf2.postexport.SnapshotGeneratorMultiColumn;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-retired-isa-relationships-id-reassign
 * 
 */
public class RF2RetiredIsaRelationshipIDRetrieveMojo extends ReleaseConfigMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * release date. 
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseDate;

	/**
	 * previuous release date. 
	 * 
	 * @parameter
	 * @required
	 */
	private String previousReleaseDate;

	/**
	 * Location of the exportFoler. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String exportedSnapshotFile;

	/**
	 * Location of the rf2 full. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String rf2FullFolder;
	
	/**
	 * Location of the outputFolder. (output in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String outputFolder;
	
	private String tmpPostExport="tmppostexport";	
	private String tmpSort="tmpsort";
	private String tmpTmpSort="tmp";
	private String tmpSnapShot="tmpsnapshot";
	private String fullOutputFolder="Full";
	private String snapshotOutputFolder="Snapshot";
	private String endFile=".txt";
	private String fullSuffix="Full";
	private String snapshotSuffix="Snapshot";

	public void execute() throws MojoExecutionException {	
		String previousFullFolder = rf2FullFolder ;

		File previousRelationshipFullFile;
		try {
			previousRelationshipFullFile = getPreviousFile(previousFullFolder,FILE_TYPE.RF2_ISA_RETIRED);

			File folderTmp=new File(targetDirectory.getAbsolutePath() + "/" + getTmpPostExport() );
			if (!folderTmp.exists()){
				folderTmp.mkdir();
			}else{
				//TODO empty folder needed?
			}
			File sortedfolderTmp=new File(folderTmp.getAbsolutePath() + "/" + getTmpSort());
			if (!sortedfolderTmp.exists()){
				sortedfolderTmp.mkdir();
			}else{
				//TODO empty folder needed?
			}

		//	Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/relationship.xml");

			File exportedRelationshipFile = new File(exportedSnapshotFile );

			File sortTmpfolderSortedTmp=new File(sortedfolderTmp.getAbsolutePath() + "/" + getTmpTmpSort());
			if (!sortTmpfolderSortedTmp.exists()){
				sortTmpfolderSortedTmp.mkdir();
			}else{
				//TODO empty folder needed?getTmpTmpSort
			}

			File sortedPreviousfile=new File(sortedfolderTmp,"pre_" + previousRelationshipFullFile.getName());
			FileSorter fsc=new FileSorter(previousRelationshipFullFile, sortedPreviousfile, sortTmpfolderSortedTmp, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
			fsc.execute();
			fsc=null;
			System.gc();


			File sortedExportedfile=new File(sortedfolderTmp,"exp_" + exportedRelationshipFile.getName());

			fsc=new FileSorter(exportedRelationshipFile, sortedExportedfile, sortTmpfolderSortedTmp, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
			fsc.execute();
			fsc=null;
			System.gc();



			File snapshotfolderTmp=new File(folderTmp.getAbsolutePath() + "/" + getTmpSnapShot() );
			if (!snapshotfolderTmp.exists()){
				snapshotfolderTmp.mkdir();
			}else{
				//TODO empty folder needed?
			}
			File snapshotSortedPreviousfile=new File(snapshotfolderTmp,"pre_" + previousRelationshipFullFile.getName());
			SnapshotGeneratorMultiColumn sg=new SnapshotGeneratorMultiColumn(sortedPreviousfile, previousReleaseDate, FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
			sg.execute();
			sg=null;
			System.gc();


			File snapshotSortedExportedfile=new File(snapshotfolderTmp,"exp_" + exportedRelationshipFile.getName());
			sg=new SnapshotGeneratorMultiColumn(sortedExportedfile, releaseDate, FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
			sg.execute();
			sg=null;
			System.gc();


			File sortedSnapPreviousfile=new File(snapshotfolderTmp,"sortSnappre_" + previousRelationshipFullFile.getName());	
			fsc=new FileSorter(snapshotSortedPreviousfile, sortedSnapPreviousfile, sortTmpfolderSortedTmp,new int[]{4,7,5,2,1,6});
			fsc.execute();
			fsc=null;
			System.gc();
			//		

			File sortedSnapExportedfile=new File(snapshotfolderTmp,"sortSnapexp_" + exportedRelationshipFile.getName());
			fsc=new FileSorter(snapshotSortedExportedfile, sortedSnapExportedfile, sortTmpfolderSortedTmp, new int[]{4,7,5,2,1,6});
			fsc.execute();
			fsc=null;
			System.gc();


//			File rf2SnapshotOutputFolder=new File(outputFolder + "/" + getSnapshotOutputFolder() );
//			if (!rf2SnapshotOutputFolder.exists()){
//				rf2SnapshotOutputFolder.mkdir();
//			}else{
//				//TODO empty folder needed?
//			}
//			File rf2FullOutputFolder=new File(outputFolder+ "/" + getFullOutputFolder() );
//			File rf2OutputRelationships=getSnapshotOutputFile(rf2SnapshotOutputFolder.getAbsolutePath(),  FILE_TYPE.RF2_RELATIONSHIP,releaseDate);
//			File rf2OutputRelationships=getFullOutputFile( rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_RELATIONSHIP,releaseDate);
			File rf2OutputRelationshipsReassign=new File(exportedRelationshipFile.getParentFile().getAbsolutePath(),"outputRF2RetIsaRelationshipReassigned.txt");
			File outputUUIDsToAssign=new File(exportedRelationshipFile.getParentFile().getAbsolutePath(),"outputRetIsaUUIDsToAssign.txt");
			File outputDifferences=new File(exportedRelationshipFile.getParentFile().getAbsolutePath(),"outputRetIsaDifferences.txt");

			RF2RelsIDRetrieveImpl rIdReassign=new RF2RelsIDRetrieveImpl(sortedSnapPreviousfile, sortedSnapExportedfile,
					rf2OutputRelationshipsReassign, outputUUIDsToAssign, outputDifferences);

			rIdReassign.execute();
			rIdReassign=null;

			if (rf2OutputRelationshipsReassign.exists()){
				String strOutput=exportedRelationshipFile.getAbsolutePath();
				exportedRelationshipFile.renameTo(new File(strOutput + ".prevToIdRetr"));
				rf2OutputRelationshipsReassign.renameTo(new File(strOutput));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private FILE_TYPE getRelFileType(String relFileType) {
		if (relFileType.equalsIgnoreCase(FILE_TYPE.RF2_RELATIONSHIP.name())){
			return FILE_TYPE.RF2_RELATIONSHIP;
		}
		if (relFileType.equalsIgnoreCase(FILE_TYPE.RF2_STATED_RELATIONSHIP.name())){
			return FILE_TYPE.RF2_STATED_RELATIONSHIP;
		}
		if (relFileType.equalsIgnoreCase(FILE_TYPE.RF2_ISA_RETIRED.name())){
			return FILE_TYPE.RF2_ISA_RETIRED;
		}
		if (relFileType.equalsIgnoreCase(FILE_TYPE.RF2_STATED_ISA_RETIRED.name())){
			return FILE_TYPE.RF2_STATED_ISA_RETIRED;
		}
		return null;
	}

	public File getSnapshotOutputFile(String parentFolder,FILE_TYPE fType,String date){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",snapshotSuffix);
		retFile+="_" + date + endFile;
		return new File(parentFolder,retFile);
		
	}
	public File getFullOutputFile(String parentFolder,FILE_TYPE fType,String date){
		String retFile=fType.getFileName();
		if (retFile==null){
			return null;
		}
		retFile=retFile.replace("SUFFIX",fullSuffix);
		retFile+="_" + date + endFile;
		return new File(parentFolder,retFile);

	}
	public String getTmpSnapShot() {
		return tmpSnapShot;
	}
	public String getTmpPostExport() {
		return tmpPostExport;
	}
	public String getTmpSort() {
		return tmpSort;
	}
	public String getFullOutputFolder() {
		return fullOutputFolder;
	}
	private File getPreviousFile(String rf2FullFolder,FILE_TYPE fType) throws Exception {
		RF2FileRetrieve RF2fRetrieve=null;
		AuxiliaryFilesRetrieve AuxFileRetrieve=null;
		String retFile=null;
		switch (fType){
		case RF2_CONCEPT:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getConceptFile();
			break;
		case RF2_DESCRIPTION:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getDescriptionFile();
			break;
		case RF2_RELATIONSHIP:		
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getRelationshipFile();
			break; 
		case RF2_STATED_RELATIONSHIP:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getStatedRelationshipFile();
			break; 
		case RF2_IDENTIFIER:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getIdentifierFile();
			break; 
		case RF2_TEXTDEFINITION:		
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getTextDefinitionFile();
			break;
		case RF2_LANGUAGE_REFSET:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getLanguageFile();
			break; 
		case RF2_ATTRIBUTE_VALUE:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getAttributeValueFile();
			break;
		case RF2_SIMPLE_MAP:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getSimpleMapFile();
			break;
		case RF2_SIMPLE:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getRefsetSimpleFile();
			break;
		case RF2_ASSOCIATION:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getAssociationFile();
			break;
		case RF2_ICD9_MAP:
			RF2fRetrieve = new RF2FileRetrieve(rf2FullFolder);
			retFile=RF2fRetrieve.getICD9CrossMapFile();
			break;
		case RF2_QUALIFIER:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getQualifierFile();
			break; 
		case RF2_COMPATIBILITY_IDENTIFIER:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getAssociationAuxiliaryFile();
		case RF2_ISA_RETIRED:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getRelationshipAuxiliaryFile();
		case RF2_STATED_ISA_RETIRED:
			AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
			retFile=AuxFileRetrieve.getStatedRelationshipAuxiliaryFile();
		}
		if (retFile==null){
			return null;
		}
		return new File(retFile);
	}

	public String getSnapshotOutputFolder() {
		return snapshotOutputFolder;
	}
	public String getTmpTmpSort() {
		return tmpTmpSort;
	}
}