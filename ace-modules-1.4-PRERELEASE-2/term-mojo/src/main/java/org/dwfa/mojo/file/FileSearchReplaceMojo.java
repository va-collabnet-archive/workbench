package org.dwfa.mojo.file;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

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
/**
 * Simple Mojo that performs a search and replace on contents of a file. The
 * resultant file is written to the output directory. If the output file is the
 * same as the input file the input file will be overwritten
 * 
 * @author Luke Swindale
 * 
 * @goal search-replace-file
 */
public class FileSearchReplaceMojo extends AbstractMojo {

	/**
	 * File to be read using the supplied expression
	 * 
	 * @parameter
	 * @required
	 */
	File inputFile;

	/**
	 * File to write the result to - NB if the file exists it will be
	 * overwritten
	 * 
	 * @parameter
	 * @required
	 */
	File outputFile;

	/**
	 * Expression used to match text from the input file
	 * 
	 * @parameter
	 * @required
	 */
	String search;

	/**
	 * Value used to replace text matched on the search criteria
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

	private static final String TMP_TOKEN = ".tmp";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {

		File tmpFile = new File(outputFile.getAbsolutePath() + TMP_TOKEN);

		if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
			throw new MojoFailureException("The input file " + inputFile
					+ " must exist, be a file and be readable");
		}

		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}

		if (tmpFile.exists()) {
			tmpFile.delete();
		}
		try {
			BufferedReader reader = new BufferedReader(
					new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile));

			String inputLine = reader.readLine();
			while (inputLine != null) {
				writeLine(writer, inputLine.replaceAll(search, replace));
				inputLine = reader.readLine();
			}

			reader.close();
			writer.close();

			if (outputFile.exists()) {
				outputFile.delete();
			}
			tmpFile.renameTo(outputFile);
		} catch (IOException e) {
			throw new MojoExecutionException(
					"Mojo failed due to IO failure, input file was "
							+ inputFile + " output file was " + outputFile
							+ " and the search string was " + search, e);
		}
	}

	private void writeLine(BufferedWriter writer, String inputLine)
			throws IOException {
		writer.write(inputLine);
		if (useDosLineTermination) {
			writer.write("\r\n");
		} else {
			writer.newLine();
		}
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(File outputFile) {
		this.outputFile = outputFile;
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
