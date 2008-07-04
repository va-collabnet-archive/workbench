package org.dwfa.mojo.refset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;
import org.dwfa.mojo.file.FileHandler;


/**
 * Imports the contents of refset files from a directory
 * 
 * @see https://mgr.cubit.aceworkspace.net/pbl/cubitci/pub/ace-mojo/site/dataimport.html
 * @goal load-refset-files-from-directory
 * @phase process-resources
 * @requiresDependencyResolution compile
 */
public class ImportRefsetFromDirectory extends AbstractMojo {
	
	/**
	 * Directory the files are to read from
	 * 
	 * @parameter
	 * @required
	 */
	File refsetDirectory;
	
	/**
	 * Indicates if the transactional ACE interface should be used - defaults to false
	 * 
	 * @parameter
	 */
	boolean transactional;

	/**
	 * Indicates if the files contain a header row or not. If true the first line of the file
	 * will be skipped. Default value is true.
	 * 
	 * @parameter
	 */
	boolean hasHeader = true;
	
	private FilenameFilter filenameFilter;

	/*
	 * Mojo execution method.
	 * @see org.apache.maven.plugin.AbstractMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		try {
			List<File> files = recursivelyGetFiles(refsetDirectory);
			
			for (File file : files) {
				FileHandler<I_ThinExtByRefPart> handler = RefsetType.getHandlerForFile(file);
				handler.setTransactional(transactional);
				handler.setSourceFile(file);
				handler.setHasHeader(hasHeader);
				
				for (I_ThinExtByRefPart thinExtByRefPart : handler) {
					getLog().info("Imported from file " + file + " extension part " + thinExtByRefPart);
				}
			}
		} catch (Exception e) {
			throw new MojoExecutionException("failed importing files from " + refsetDirectory, e);
		}
	}

	private List<File> recursivelyGetFiles(File directory) {
		if (filenameFilter == null) {
			filenameFilter = RefsetType.getFileNameFilter();
		}
		
		List<File> allFiles = new ArrayList<File>();;
		for (File file : directory.listFiles(filenameFilter)) {
			if (file.isDirectory()) {
				allFiles.addAll(recursivelyGetFiles(file));
			} else {
				allFiles.add(file);
			}
		}
		return allFiles;
	}
}