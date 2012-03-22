package org.ihtsdo.rf2.identifier.mojo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.constant.I_Constants;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.SnapshotGeneratorMultiColumn;
import org.ihtsdo.rf2.identifier.impl.RF2ICDOTargetsImpl;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.postexport.AuxiliaryFilesRetrieve;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.RF2ArtifactPostExportAbst.FILE_TYPE;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.JAXBUtil;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-ICDO-targets
 * 
 */
public class RF2ICDOTargetsMojo extends ReleaseConfigMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * release date. 20100731
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseDate;

	/**
	 * previuous release date. 20100731
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
	private String inputSnapshotSimpleMapFile;

	/**
	 * Location of the rf2 full. (input in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String rf2CompatibiliyPkgFolder;

	/**
	 * Location of the outputFolder. (output in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String outputFolder;

	// for accessing the web service
	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String endpointURL;

	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String username;

	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private String password;


	private String tmpPostExport="tmppostexport";	
	private String tmpSort="tmpsort";
	private String tmpTmpSort="tmp";
	private String tmpSnapShot="tmpsnapshot";
	private String endFile=".txt";
	private String fullSuffix="Full";

	public void execute() throws MojoExecutionException {	
		String previousFullFolder = rf2CompatibiliyPkgFolder ;

		File previousSimpleMapFullFile;
		try {
			previousSimpleMapFullFile = getPreviousFile(previousFullFolder);

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

			File exportedSimpleMapFile = new File(inputSnapshotSimpleMapFile );

			File sortTmpfolderSortedTmp=new File(sortedfolderTmp.getAbsolutePath() + "/" + getTmpTmpSort());
			if (!sortTmpfolderSortedTmp.exists()){
				sortTmpfolderSortedTmp.mkdir();
			}else{
				//TODO empty folder needed?getTmpTmpSort
			}

			File sortedPreviousfile=new File(sortedfolderTmp,"pre_" + previousSimpleMapFullFile.getName());
			FileSorter fsc=new FileSorter(previousSimpleMapFullFile, sortedPreviousfile, sortTmpfolderSortedTmp, FILE_TYPE.RF2_ICDO_TARGETS.getColumnIndexes());
			fsc.execute();
			fsc=null;
			System.gc();

			File sortedExportedfile=new File(sortedfolderTmp,"exp_" + exportedSimpleMapFile.getName());

			fsc=new FileSorter(exportedSimpleMapFile, sortedExportedfile, sortTmpfolderSortedTmp, new int[]{6,5,1});
			fsc.execute();
			fsc=null;
			System.gc();

			File snapshotfolderTmp=new File(folderTmp.getAbsolutePath() + "/" + getTmpSnapShot() );
			if (!snapshotfolderTmp.exists()){
				snapshotfolderTmp.mkdir();
			}else{
				//TODO empty folder needed?
			}
			File snapshotSortedPreviousfile=new File(snapshotfolderTmp,"pre_" + previousSimpleMapFullFile.getName());
			SnapshotGeneratorMultiColumn sg=new SnapshotGeneratorMultiColumn(sortedPreviousfile, previousReleaseDate, FILE_TYPE.RF2_ICDO_TARGETS.getSnapshotIndex(), 1, snapshotSortedPreviousfile, null, null);
			sg.execute();
			sg=null;
			System.gc();

			File snapshotSortedExportedfile=new File(snapshotfolderTmp,"exp_" + exportedSimpleMapFile.getName());
			SnapshotGeneratorMultiColumn sgm=new SnapshotGeneratorMultiColumn(sortedExportedfile, releaseDate, new int[]{6,5}, 1, snapshotSortedExportedfile, new Integer[]{4}, new String[]{I_Constants.ICDO_REFSET_ID});
			sgm.execute();
			sgm=null;
			System.gc();

			File rf2FullOutputFolder=new File(outputFolder );

			File finalFile=getFullOutputFile(rf2FullOutputFolder.getAbsolutePath(), FILE_TYPE.RF2_ICDO_TARGETS,releaseDate);

			Config config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/idGenerator.xml");
			// set all the values passed via mojo
			config.setFlushCount(10000);
			config.setUsername(username);
			config.setPassword(password);
			config.setEndPoint(endpointURL);
			RF2ICDOTargetsImpl rIdReassign=new RF2ICDOTargetsImpl(config,releaseDate,snapshotSortedPreviousfile, snapshotSortedExportedfile,
					rf2CompatibiliyPkgFolder, previousReleaseDate, targetDirectory, outputFolder, finalFile);

			rIdReassign.execute();
			rIdReassign=null;

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

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
	private File getPreviousFile(String rf2FullFolder) throws Exception {
		AuxiliaryFilesRetrieve AuxFileRetrieve=null;
		String retFile=null;
		AuxFileRetrieve = new AuxiliaryFilesRetrieve(rf2FullFolder);
		retFile=AuxFileRetrieve.getCrossMapICDOTgtAuxRF2File();

		if (retFile==null){
			return null;
		}
		return new File(retFile);
	}

	public String getTmpTmpSort() {
		return tmpTmpSort;
	}
}