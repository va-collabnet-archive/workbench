package org.dwfa.mojo.file;

/**
 * Copyright (c) 2010 International Health Terminology Standards Development Organisation
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.file.util.FileUtil;

import java.io.File;

/**
 * Simple Mojo that performs a search and replace on contents of files within a
 * directory. The resultant files are written to the output directory. If the
 * output directory is the same as the input directory the input files will be
 * overwritten
 * 
 * @author Luke Swindale
 * 
 * @goal search-replace-files-in-directory
 */
public class DirectoryFileSearchReplaceMojo extends AbstractMojo {

	private static String TMP_TOKEN = ".tmp";

	/**
	 * Directory to be read using the supplied expression
	 * 
	 * @parameter
	 * @required
	 */
	File inputDirectory;

	/**
	 * Directory to write the result to - NB if any files file exists they will
	 * be overwritten
	 * 
	 * @parameter
	 * @required
	 */
	File outputDirectory;

	/**
	 * Expression used to match text from the input file
	 * 
	 * @parameter
	 * @required
	 */
	String search;

	/**
	 * Value used to replace the text matched on the search string
	 * 
	 * @parameter
	 * @required
	 */
	String replace;

	/**
	 * Indicates if the output file should use DOS line termination - defaults
	 * to true
	 * 
	 * @parameter
	 */
	boolean useDosLineTermination = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		File tmpDir = new File(outputDirectory.getAbsolutePath() + TMP_TOKEN);

		if (!inputDirectory.exists() || !inputDirectory.isDirectory()
				|| !inputDirectory.canRead()) {
			throw new MojoFailureException("The input directory "
					+ inputDirectory
					+ " must exist, be a directory and be readable");
		}

		if (tmpDir.exists()) {
            FileUtil.deleteDirectory(tmpDir);
		}
		tmpDir.mkdirs();

		FileSearchReplaceMojo fileSearchReplaceMojo = new FileSearchReplaceMojo();
		fileSearchReplaceMojo.setUseDosLineTermination(useDosLineTermination);

		for (File file : inputDirectory.listFiles()) {
			fileSearchReplaceMojo.setInputFile(file);
			fileSearchReplaceMojo
					.setOutputFile(new File(tmpDir, file.getName()));
			fileSearchReplaceMojo.setSearch(search);
			fileSearchReplaceMojo.setReplace(replace);
			fileSearchReplaceMojo.execute();            
		}

		if (outputDirectory.exists()) {
            FileUtil.deleteDirectory(outputDirectory);
		}        
		tmpDir.renameTo(outputDirectory);        
	}

	public File getInputDirectory() {
		return inputDirectory;
	}

	public void setInputDirectory(File inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}

	public String getReplace() {
		return replace;
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}

	public boolean isUseDosLineTermination() {
		return useDosLineTermination;
	}

	public void setUseDosLineTermination(boolean useDosLineTermination) {
		this.useDosLineTermination = useDosLineTermination;
	}
}