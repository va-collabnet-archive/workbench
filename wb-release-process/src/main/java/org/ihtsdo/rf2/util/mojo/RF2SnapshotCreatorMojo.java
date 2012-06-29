package org.ihtsdo.rf2.util.mojo;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.mojo.ReleaseConfigMojo;
import org.ihtsdo.rf2.postexport.FileSorter;
import org.ihtsdo.rf2.postexport.SnapshotGenerator;



/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal rf2-snapshot-creator
 * 
 */
public class RF2SnapshotCreatorMojo extends ReleaseConfigMojo {

	/**
	 * Location of the build directory.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File targetDirectory;

	/**
	 * sorted file
	 * 
	 * @parameter
	 * @required
	 */
	private String inputFile;

	/**
	 * snapshot Date
	 * 
	 * @parameter
	 * @required
	 */
	private String snapshotDate;
	/**
	 * snapshot columns index
	 * 
	 * @parameter
	 * @required
	 */
	private String snapshotColumnIx;

	/**
	 * effective Time Column
	 * 
	 * @parameter
	 * @required
	 */
	private String effectiveTimeColumn;

	/**
	 *  output File. (output in this mojo)
	 * 
	 * @parameter
	 * @required
	 */
	private String outputFile;

	/**
	 * full columns indexes
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<String> fullColumnIxs;
	/**
	 * columns to filter
	 * 
	 * @parameter
	 * @required
	 */

	private ArrayList<String> columnFilterIxs;

	/**
	 * column filters
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList <String> columnFilterValues;

	public void execute() throws MojoExecutionException {	

		try {

			File folderTmp=new File(targetDirectory.getAbsolutePath() + "/tmpSorter" );
			if (!folderTmp.exists()){
				folderTmp.mkdir();
			}else{
				//TODO empty folder needed?
			}
			File inFile=new File (inputFile);
			File sortedfile=new File(inFile.getParentFile(), "srt_" + inFile.getName());

			int[] colIxs=new int[fullColumnIxs.size()];
			for (int i=0;i<fullColumnIxs.size();i++){
				colIxs[i]=Integer.parseInt(fullColumnIxs.get(i));
			}
			FileSorter fsc=new FileSorter(inFile, sortedfile, folderTmp, colIxs);
			fsc.execute();
			fsc=null;
			System.gc(); 

			int snapshotIx=Integer.parseInt(snapshotColumnIx);
			File snapshotSortedfile=new File(outputFile);

			Integer[] colFter=new Integer[columnFilterIxs.size()];
			for (int i=0;i<columnFilterIxs.size();i++){
				colFter[i]=Integer.parseInt(columnFilterIxs.get(i));
			}
			String[] colFterValue=new String[columnFilterValues.size()];
			for (int i=0;i<columnFilterValues.size();i++){
				colFterValue[i]=columnFilterValues.get(i);
			}
			int effTimeColumn=Integer.parseInt(effectiveTimeColumn);
			
			SnapshotGenerator sg=new SnapshotGenerator(sortedfile, snapshotDate,  snapshotIx, effTimeColumn, snapshotSortedfile, colFter, colFterValue);
			sg.execute();
			sg=null;
			System.gc();
			
			if (folderTmp.exists()){
				folderTmp.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}