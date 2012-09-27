package org.ihtsdo.mojo.pbl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal to stage the generated site to a staging directory.
 * 
 * https://mgr.servers.aceworkspace.net/pbl/cubitci/pub/ace-mojo/site/index.html
 * 
 *  <cubitProject>cubitci</cubitProject>
 *  <siteRoot>ace-mojo</siteRoot>
 *  
 *  Will stage the files to ${stagingDirectory}/${cubitProject}/${visibility}/${siteRoot}
 * 
 * @goal pbl-stage-site
 */

public class PblStageSite  extends AbstractMojo {
	/**
	 * Staging directory
	 * 
	 * @parameter
	 * @required
	 */
	private String stagingDirectory;

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
	 * Site source to stage from.
	 * 
	 * @parameter default-value = "target/site";
	 */
	private String siteSource;
	/**
	 * Visibility is {pub|priv}. Default is pub
	 * 
	 * @parameter
	 */
	private String visibility = "pub";

	/**
	 * Array of parents...
	 * TODO figure out how to pass in an array of projects. 
	 * @parameter
	 */
	private String parentProject;

	/**
	 * Delete before stage. Default is true;
	 * 
	 * @parameter
	 */
	private boolean deleteBeforeStage = true;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			File stageTo = new File(new File(new File(stagingDirectory), cubitProject), visibility);
			getLog().info("stage 1: " + stageTo.getAbsolutePath());
			getLog().info(" parentProject: " + parentProject);
			if (parentProject != null) {
				stageTo = new File(stageTo, parentProject);
				getLog().info(" adding parent: " + parentProject);
			}
			getLog().info("stage 2: " + stageTo.getAbsolutePath());
			stageTo = new File(stageTo, siteRoot);
			getLog().info("stage 3: " + stageTo.getAbsolutePath());
			stageTo = new File(stageTo, "site");
			getLog().info("stage 4: " + stageTo.getAbsolutePath());
			if (deleteBeforeStage) {
				recursiveDelete(stageTo);
			}
			File stageSource = new File(siteSource);
			recursiveCopy(stageSource, stageTo, false);
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} 
	}

	public String getParentProject() {
		return parentProject;
	}

	public void setParentProject(String parentProject) {
		this.parentProject = parentProject;
	}

	
	public static void copyFile(File in, File out) throws IOException {
		FileChannel sourceChannel = new FileInputStream(in).getChannel();
		FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		// magic number for Windows, 64Mb - 32Kb)
		int maxCount = (64 * 1024 * 1024) - (32 * 1024);
		long size = sourceChannel.size();
		long position = 0;
		while (position < size) {
			position += sourceChannel.transferTo(position, maxCount, destinationChannel);
		} 
		sourceChannel.close();
		destinationChannel.close();
	}
	
	public static void recursiveCopy(File from, File to, boolean copyInvisibles) throws IOException {
		if (from.isDirectory()) {
			to.mkdirs();
			for (File f: from.listFiles()) {
				if (f.isHidden() == false || 
                    ((copyInvisibles == true) && (f.getName().endsWith(".DS_Store") == false))) {
					File childTo = new File(to, f.getName());
					recursiveCopy(f, childTo, copyInvisibles);
				}
			}
		} else {
			copyFile(from, to);
		}
	}
	
    public static void recursiveDelete(File from) throws IOException {
		if (from.isDirectory()) {
			for (File f: from.listFiles()) {
				recursiveDelete(f);
			}
		} 
		from.delete();
	}
    
}
