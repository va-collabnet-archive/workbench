package org.ihtsdo.rf2.identifier.mojo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.identifier.impl.RF2RelsIDRetrieveImpl;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.postexport.AuxiliaryFilesRetrieve;
import org.ihtsdo.rf2.postexport.CommonUtils;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2FileRetrieve;
import org.ihtsdo.rf2.postexport.SnapshotGeneratorMultiColumn;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-relationships-id-reassign-previous-not-released
 * 
 */
public class RF2PostInferredRelationshipIDRetrieveMojo extends ReleaseConfigMojo {

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
	 * Location of the previous Id not released file. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String previousIdNotReleasedFile;
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

		File previousNotReleasedFile;
		try {
			previousNotReleasedFile= new File(previousIdNotReleasedFile);

			if (!previousNotReleasedFile.exists()){
				previousNotReleasedFile.createNewFile();
			}
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

			File sortedPreviousfile=new File(sortedfolderTmp,"pre_" + previousNotReleasedFile.getName());
			FileSorter fsc=new FileSorter(previousNotReleasedFile, sortedPreviousfile, sortTmpfolderSortedTmp, FILE_TYPE.RF2_RELATIONSHIP.getColumnIndexes());
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
			File snapshotSortedPreviousfile=new File(snapshotfolderTmp,"pre_" + previousNotReleasedFile.getName());
			SnapshotGeneratorMultiColumn sg=new SnapshotGeneratorMultiColumn(sortedPreviousfile, releaseDate, FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
			sg.execute();
			sg=null;
			System.gc();

			File snapshotSortedExportedfile=new File(snapshotfolderTmp,"exp_" + exportedRelationshipFile.getName());
			sg=new SnapshotGeneratorMultiColumn(sortedExportedfile, releaseDate, FILE_TYPE.RF2_RELATIONSHIP.getSnapshotIndex(), 1, snapshotSortedExportedfile, null, null);
			sg.execute();
			sg=null;
			System.gc();

			File sortedSnapPreviousfile=new File(snapshotfolderTmp,"sortSnappre_" + previousNotReleasedFile.getName());	
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
			
			//split input pre assigned Ids process file in assigned and not assigned
			File assignedIdExportedfile=new File(snapshotfolderTmp,"AssigId_" + exportedRelationshipFile.getName());
			
			FileOutputStream fosu = new FileOutputStream( assignedIdExportedfile);
			OutputStreamWriter oswu = new OutputStreamWriter(fosu,"UTF-8");
			BufferedWriter bwu = new BufferedWriter(oswu);
			
			File uuidExportedFile=new File(snapshotfolderTmp,"Uuid_" + exportedRelationshipFile.getName());
			
			FileOutputStream fosd = new FileOutputStream( uuidExportedFile);
			OutputStreamWriter oswd = new OutputStreamWriter(fosd,"UTF-8");
			BufferedWriter bwd = new BufferedWriter(oswd);

			FileInputStream fis = new FileInputStream(sortedSnapExportedfile	);
			InputStreamReader isr = new InputStreamReader(fis,"UTF-8");
			BufferedReader brE = new BufferedReader(isr);

			String header =brE.readLine();
			
			bwu.append(header);
			bwu.append("\r\n");
			bwd.append(header);
			bwd.append("\r\n");
			
			String[] splitted;
			String line;
			HashSet<String> idExistent=new HashSet<String>();
			while ((line=brE.readLine())!=null){
				splitted=line.split("\t",-1);
				if (splitted[0].indexOf("-")>-1 || splitted[0].equals("")){
					bwd.append(line);
					bwd.append("\r\n");
				}else{
					bwu.append(line);
					bwu.append("\r\n");
					idExistent.add(splitted[0]);
				}
			}
			brE.close();
			bwd.close();
			bwu.close();
			System.gc();
			
			//filter input previous id not released to get not assigned yet in this process
			File prevNotReleasedIDFile=new File(snapshotfolderTmp,"NewIdNotAssig_" + sortedSnapPreviousfile.getName());
			
			fosd = new FileOutputStream( prevNotReleasedIDFile);
			oswd = new OutputStreamWriter(fosd,"UTF-8");
			bwd = new BufferedWriter(oswd);

			fis = new FileInputStream(sortedSnapPreviousfile	);
			isr = new InputStreamReader(fis,"UTF-8");
			brE = new BufferedReader(isr);

			header =brE.readLine();
			
			bwd.append(header);
			bwd.append("\r\n");
			
			while ((line=brE.readLine())!=null){
				splitted=line.split("\t",-1);
				if (!idExistent.contains(splitted[0])){
					bwd.append(line);
					bwd.append("\r\n");
				}
			}
			brE.close();
			bwd.close();
			
			
			File rf2OutputRelationshipsReassign=new File(exportedRelationshipFile.getParentFile().getAbsolutePath(),"outputRF2RelationshipPrevNRIdReassigned.txt");
			File outputUUIDsToAssign=new File(exportedRelationshipFile.getParentFile().getAbsolutePath(),"outputUUIDsToAssignPrevNotReleased.txt");
			File outputDifferences=new File(exportedRelationshipFile.getParentFile().getAbsolutePath(),"outputDifferencesPrevNotReleased.txt");

			RF2RelsIDRetrieveImpl rIdReassign=new RF2RelsIDRetrieveImpl(prevNotReleasedIDFile, uuidExportedFile,
					rf2OutputRelationshipsReassign, outputUUIDsToAssign, outputDifferences);

			rIdReassign.execute();
			rIdReassign=null;

			//build final relationships with id assigned from previous not released id
			if (rf2OutputRelationshipsReassign.exists()){
				String strOutput=exportedRelationshipFile.getAbsolutePath();
				exportedRelationshipFile.renameTo(new File(strOutput + ".prevToIdRetr"));
				
				HashSet<File> hFile=new HashSet<File>();
				hFile.add(rf2OutputRelationshipsReassign);
				hFile.add(assignedIdExportedfile);

				CommonUtils.MergeFile(hFile,  new File(strOutput));
				System.gc();
				
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

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