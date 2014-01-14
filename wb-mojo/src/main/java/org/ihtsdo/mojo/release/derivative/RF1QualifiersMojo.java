package org.ihtsdo.mojo.release.derivative;

import java.io.File;
import java.util.HashSet;

import org.ihtsdo.rf2.postexport.CommonUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.ihtsdo.rf2.derivatives.factory.RF2QualifierFactory;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.FileFilterAndSorter;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.util.Config;
import org.ihtsdo.rf2.util.ExportUtil;
import org.ihtsdo.rf2.util.JAXBUtil;
import org.ihtsdo.rf2.util.RedundantRowsFix;

/**
 * @author Alejandro Rodriguez
 * 
 * @goal create-rf1-qualifiers
 * @requiresDependencyResolution compile
 */

public class RF1QualifiersMojo extends AbstractMojo {

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
	 * Location of the exportFoler.
	 * 
	 * @parameter
	 * @required
	 */
	private String exportFolder;	
	
	
	// for accessing the web service
	/**
	 * endpointURL
	 * 
	 * @parameter
	 * 
	 */
	private String endpointURL;
	
	/**
	 * username
	 * 
	 * @parameter
	 * 
	 */
	private String username;
	
	/**
	 * password
	 * 
	 * @parameter
	 * 
	 */
	private String password;
	
	
	/**
	 * namespaceId
	 * 
	 * @parameter default-value="0"
	 * 
	 */
	private String namespaceId;
	
	/**
	 * partitionId
	 * 
	 * @parameter default-value="00"
	 * 
	 */
	private String partitionId;
	
	/**
	 * executionId
	 * 
	 * @parameter default-value="Daily-build"
	 * 
	 */
	private String executionId;
	
	/**
	 * moduleId
	 * 
	 * @parameter default-value="Core Concept Component"
	 * 
	 */
	private String moduleId;
	
	/**
	 * releaseId
	 * 
	 * @parameter 
	 * 
	 */
	private String releaseId;

	/**
	 * currentInferRels
	 * 
	 * @parameter 
	 * 
	 */
	private File currentInferRels;

	/**
	 * previousQualIds
	 * 
	 * @parameter 
	 * 
	 */
	private File previousQualIds;

	/**
	 * qualStartStop
	 * 
	 * @parameter 
	 * 
	 */
	private File qualStartStop;

	/**
	 * SCTID_Code_map
	 * 
	 * @parameter 
	 * 
	 */
	private File sctid_Code_map;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			Config config;
			
			 config = JAXBUtil.getConfig("/org/ihtsdo/rf2/config/rf1Qualifier.xml");
				
			// set all the values passed via mojo
			config.setOutputFolderName(exportFolder);
			
			config.setReleaseDate(releaseDate);
			
			config.setFlushCount(10000);
			config.setInvokeDroolRules("false");
			config.setFileExtension("txt");

			//Below Parameters are necessary for ID-Generation
			config.setNamespaceId(namespaceId);
			config.setPartitionId(partitionId);
			config.setExecutionId(executionId);
			config.setModuleId(moduleId);
			config.setReleaseId(releaseId);
			config.setUsername(username);
			config.setPassword(password);
			config.setEndPoint(endpointURL);
			
			// initialize meta hierarchy
			ExportUtil.init();
			
			ExportUtil.setCurrentInferRelsFile(currentInferRels);
			ExportUtil.setPreviousQualIdsFile(previousQualIds);
			ExportUtil.setQualStartStopFile(qualStartStop);
			ExportUtil.setSCTID_Code_MapFile(sctid_Code_map);

//			RF2QualifierFactory factory = new RF2QualifierFactory(config);
//			factory.export();
//			

			String outputFolderName = config.getOutputFolderName();

			File folder = new File(outputFolderName);

			if (!folder.exists())
				folder.mkdir();

			String exportFileName = config.getExportFileName();

			exportFileName += config.getReleaseDate() + "." + config.getFileExtension();

			File qualFile = new File(outputFolderName , exportFileName);
			File sortedQualfile=new File(qualFile.getParent(),"Sort_" + qualFile.getName());
			
			File tmpFol=new File (outputFolderName ,"sortTmp");
			if (!tmpFol.exists()){
				tmpFol.mkdirs();
			}
			FileSorter fsc=new FileSorter(qualFile, sortedQualfile, tmpFol, new int[]{1,2,3,5});
			fsc.execute();
			fsc=null;
			System.gc();
			RedundantRowsFix rrf=new RedundantRowsFix(sortedQualfile,null, new int[]{1,2,3}, new Integer[]{}, sortedQualfile);
			rrf.Fix();
			rrf=null;
			System.gc();
			
			File inferFile=new File(currentInferRels.getParent(),"Infer_"  + currentInferRels.getName());
						
//			FileFilterAndSorter ffs=new FileFilterAndSorter(currentInferRels, inferFile, tmpFol, new int[]{0}, new Integer[]{4}, new String[]{"0"});
//			ffs.execute();
//			ffs=null;
			
			File histFile=new File(currentInferRels.getParent(),"Hist_"  + currentInferRels.getName());
//		    ffs=new FileFilterAndSorter(currentInferRels, histFile, tmpFol, new int[]{0}, new Integer[]{4}, new String[]{"2"});
//		    ffs.execute();
//			ffs=null;
		    File addFile=new File(currentInferRels.getParent(),"Add_"  + currentInferRels.getName());
//		    ffs=new FileFilterAndSorter(currentInferRels, addFile, tmpFol, new int[]{0}, new Integer[]{4}, new String[]{"3"});
//		    ffs.execute();
//			ffs=null;
			System.gc();
			
		    HashSet<File> hFile=new HashSet<File>();
		    hFile.add(addFile);
		    hFile.add(histFile);
		    hFile.add(sortedQualfile);
		    hFile.add(inferFile);
		    

			File finalRf1File=new File(qualFile.getParent(),"Final_" + qualFile.getName());
		    CommonUtils.MergeFile(hFile, finalRf1File);
		    
		} catch (Exception e) {
			e.printStackTrace();
			e.getMessage();
			throw new MojoExecutionException(e.getMessage());
		}
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}

	public String getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(String releaseDate) {
		this.releaseDate = releaseDate;
	}

	public String getExportFolder() {
		return exportFolder;
	}

	public void setExportFolder(String exportFolder) {
		this.exportFolder = exportFolder;
	}
}
