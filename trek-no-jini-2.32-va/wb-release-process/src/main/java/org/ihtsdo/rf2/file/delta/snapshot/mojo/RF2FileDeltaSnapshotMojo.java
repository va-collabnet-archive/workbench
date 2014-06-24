package org.ihtsdo.rf2.file.delta.snapshot.mojo;

import java.io.File;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.DeltaGenerator;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.FileSorter;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.SnapshotGenerator;
import org.ihtsdo.rf2.file.delta.snapshot.tasks.SnapshotGeneratorMultiColumn;

/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal run
 * 
 * @phase install
 */
public class RF2FileDeltaSnapshotMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the release folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String releaseFolder;

	/**
	 * Location of the destination folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String destinationFolder;

	/**
	 * Snapshot date.
	 * 
	 * @parameter
	 * @required
	 */
	private String snapshotDate;

	/**
	 * Delta start date.
	 * 
	 * @parameter
	 * @required
	 */
	private String deltaStartDate;

	/**
	 * Delta end date.
	 * 
	 * @parameter
	 * @required
	 */
	private String deltaEndDate;

	/**
	 * Files
	 * 
	 * @parameter
	 * @required
	 */
	private ArrayList<RF2File> rf2Files;

	public void execute() throws MojoExecutionException {

		getLog().info("Running the RF2 File Convertion with the following ");
		getLog().info("Release Folder     :" + releaseFolder);
		getLog().info("Destination Folder :" + destinationFolder);
		getLog().info("Snapshot date      :" + snapshotDate);
		getLog().info("Delta start date    :" + deltaStartDate);
		getLog().info("Delta end date     :" + deltaEndDate);

		// check if the release folder exists
		File rFile = new File(releaseFolder);
		if (!rFile.exists())
			throw new MojoExecutionException("Release folder : " + destinationFolder + " doesn't exist, exiting ..");
		
		//check if the release folder contains files
		String[] rFiles = rFile.list();
		if ( rFiles.length == 0 )
			throw new MojoExecutionException("Release folder : " + destinationFolder + " is empty, exiting ..");
		
		// create the tmp folder if doesn't exist
		File t = new File("tmp");

		if (!t.exists()) {
			getLog().info("Creating tmp folder: " + t);
			t.mkdirs();
		}

		// create the destination folder if it doesn't exist
		File dFile = new File(destinationFolder);
		if (!dFile.exists()) {
			getLog().info("Destination folder : " + destinationFolder + " doesn't exist, creating ..");
			dFile.mkdirs();
		}

		getLog().info("Creating Delta and Snapshots ");

		// iterate through all the files specified in the pom
		for (int f = 0; f < rf2Files.size(); f++) {

			// initialize
			File file = new File(releaseFolder + File.separator + rf2Files.get(f).fileName);
			File sortedFile = new File(destinationFolder + File.separator + rf2Files.get(f).sortedFileName);
			File deltaFile = new File(destinationFolder + File.separator + rf2Files.get(f).deltaFileName);
			File snapshotFile = new File(destinationFolder + File.separator + rf2Files.get(f).snapshotFileName);
			File snapshotMultiColumnFile = new File(destinationFolder + File.separator + rf2Files.get(f).snapshotMultiColumnFileName);
			
			if (getLog().isDebugEnabled()) {
				getLog().info("File :" + f + " : " + rf2Files.get(f).fileName);

				ArrayList<String> sorts = rf2Files.get(f).sort.sortOrdinals;
				for (int s = 0; s < sorts.size(); s++) {
					getLog().info("Sort oridnal :" + sorts.get(s));
				}
				
				ArrayList<String> deltas = rf2Files.get(f).delta.deltaOrdinals;
				for (int d = 0; d < deltas.size(); d++) {
					getLog().info("Detla oridnal :" + deltas.get(d));
				}
				
				ArrayList<String> snapshots = rf2Files.get(f).snapshot.snapshotOrdinals;
				for (int sn = 0; sn < snapshots.size(); sn++) {
					getLog().info("Sort oridnal :" + snapshots.get(sn));
				} // end debug enables
				
			}

			// sort the file
			getLog().info("File :" + rf2Files.get(f).fileName);
			getLog().info("Sorting ...");
			FileSorter fs = new FileSorter(file, sortedFile, t, getInt(rf2Files.get(f).sort.sortOrdinals));
			fs.execute();
			fs = null;
			System.gc();

			// create delta
			getLog().info("Creating Delta ...");
			DeltaGenerator dl = new DeltaGenerator(sortedFile, deltaStartDate, deltaEndDate, getInt(rf2Files.get(f).delta.deltaOrdinals), rf2Files.get(f).sort.effectiveTimeOrdinal, deltaFile);
			dl.execute();
			dl = null;
			System.gc();
					
			// create snapshot
			if(snapshotFile.getName() != null && snapshotFile.getName().length() != 4 && snapshotFile.getName().contains("_")){
				getLog().info("Creating Snapshot ...");
				SnapshotGenerator sg = new SnapshotGenerator(sortedFile, snapshotDate, getInt(rf2Files.get(f).snapshot.snapshotOrdinals)[0], rf2Files.get(f).snapshot.effectiveTimeOrdinal, snapshotFile,null, null);
				sg.execute();
				sg = null;
				System.gc();
			}
			
			// create snapshot multicolumn
			if(snapshotMultiColumnFile.getName() != null && snapshotMultiColumnFile.getName().length() != 4 && snapshotMultiColumnFile.getName().contains("_")){
				getLog().info("Creating Snapshot using MultipleColumn...");
				SnapshotGeneratorMultiColumn stg=new SnapshotGeneratorMultiColumn(sortedFile, snapshotDate,getInt(rf2Files.get(f).snapshot.snapshotOrdinals), rf2Files.get(f).snapshot.effectiveTimeOrdinal, snapshotMultiColumnFile, null, null);
				stg.execute();
				stg = null;
				System.gc();
			}
		} // end the main files loop

		// cleanup and delete tje tmp folder
		getLog().info("Cleaning up ...");
		String tmpFiles[] = t.list();
		for (int d = 0; d < tmpFiles.length; d++) {
		File tFile = new File(t.getAbsolutePath()+File.separator+tmpFiles[d]);
		 tFile.delete();
		 }
		t.delete();

		getLog().info("Done.");
	}

	public static int[] getInt(ArrayList<String> integers) {
		int[] ret = new int[integers.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = Integer.parseInt(integers.get(i));
		}

		return ret;
	}	
}