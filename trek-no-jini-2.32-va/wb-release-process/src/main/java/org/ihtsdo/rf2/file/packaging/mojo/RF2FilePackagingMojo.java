package org.ihtsdo.rf2.file.packaging.mojo;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.ihtsdo.rf2.file.packaging.Package;
import org.w3c.dom.Document;

/**
 * Goal which sorts and generates delta, snapshot.
 * 
 * @goal package
 * 
 * @phase install
 */
public class RF2FilePackagingMojo extends AbstractMojo {
	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of the delta snapshot folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String deltaSnapshotFolder;

	/**
	 * Location of the destination folder.
	 * 
	 * @parameter
	 * @required
	 */
	private String packagingFolder;

	/**
	 * Folders
	 * 
	 * @parameter
	 * @required
	 */
	private String folderLayoutXML;

	public void execute() throws MojoExecutionException {

		getLog().info("Running the RF2 File Packaging with the following ");
		getLog().info("Delta Snapshot Folder     :" + deltaSnapshotFolder);
		getLog().info("Packaging Folder :" + packagingFolder);
		getLog().info("Packaging folder structure XML :" + folderLayoutXML);
		// check if the release folder exists
		File rFile = new File(deltaSnapshotFolder);
		if (!rFile.exists())
			throw new MojoExecutionException("Delta Snapshot folder : " + deltaSnapshotFolder + " doesn't exist, exiting ..");

		// check if the release folder contains files
		String[] rFiles = rFile.list();
		if (rFiles.length == 0)
			throw new MojoExecutionException("Delta Snapshot folder : " + deltaSnapshotFolder + " is empty, exiting ..");

		File dFile = new File(packagingFolder);

		// check if the packaging folder exists delete it
		if (dFile.exists()) {
			getLog().info("Packaging folder : " + packagingFolder + " exists, deleting ...");
			if (!Delete.deleteDirectory(dFile))
				getLog().info("WARNING! Packaging folder : " + packagingFolder + " not deleted, folders/files may be wrong");
			else
				getLog().info("Packaging folder : " + packagingFolder + " deleted.");
		}

		// create the packaging if it doesn't exist
		if (!dFile.exists()) {
			getLog().info("Creating Packaging folder : " + packagingFolder + " ...");
			dFile.mkdirs();
			getLog().info("Packaging folder : " + packagingFolder + " created.");
		}

		getLog().info("Processing the packaging ... ");

		try {

			process();

		} catch (NullPointerException ne) {
			getLog().error(ne);
			// System.out.println("NullPointerException:- " + ne.getMessage());
		} catch (Exception e) {
			getLog().error(e);
			// System.out.println("Exception:- " + e.getMessage());
		}

		getLog().info("Done.");
	}

	public void process() throws Exception {

		Package p = new Package();

		Document dom = p.parseXmlFile(folderLayoutXML);
		p.parseDocument(dom, deltaSnapshotFolder, packagingFolder);

	}
}
