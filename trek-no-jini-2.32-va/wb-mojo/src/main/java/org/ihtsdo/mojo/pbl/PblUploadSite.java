package org.ihtsdo.mojo.pbl;

import java.io.IOException;
import java.util.Arrays;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal calls pbl.py to upload the site directory to the CuBIT project build
 * library.
 * 
 * @goal pbl-upload-site
 */

public class PblUploadSite extends AbstractMojo {

	/**
	 * CuBIT project to upload to.
	 * 
	 * @parameter
	 * @required
	 */
	private String cubitProject;

	/**
	 * Site root in the PBL to upload to.
	 * 
	 * @parameter
	 * @required
	 */
	private String siteRoot;

	/**
	 * Visibility is {pub|priv}. Default is pub
	 * 
	 * @parameter
	 */
	private String visibility = "pub";

	/**
	 * Delete before upload. Default is false;
	 * 
	 * @parameter
	 */
	private boolean deleteBeforeUpload = false;
	
	/**
	 * Provide verbose output on progress of upload. Default is false.
	 * @parameter
	 */
	private boolean verbose = false;

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			String[] command = new String[] {"pbl.py", "delete", "--force", "--project",
					cubitProject, "-t", visibility, "--remotepath", siteRoot};
			if (deleteBeforeUpload) {
				Executor.executeCommand(command, getLog());
			}
			if (verbose) {
				command = new String[] {"pbl.py", "upload", "-v", "--project",
						cubitProject, "--force", "-t", visibility, "-r", "/" + siteRoot,
						"-d", "\"Site upload\"", "target/site"};
			} else {
				command = new String[] {"pbl.py", "upload", "--project",
						cubitProject, "--force", "-t", visibility, "-r", "/" + siteRoot,
						"-d", "\"Site upload\"", "target/site"};
			}
			getLog().info(Arrays.asList(command).toString());
			int result = Executor.executeCommand(command, getLog());
			if (result != 0) {
				throw new MojoExecutionException("Unexpected return value: "
						+ result);
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (InterruptedException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}
}
