/**
 * 
 */
package org.dwfa.mojo.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Simple Mojo that ssentially greps a file and writes the result to another file.
 * Given a regular expression the Mojo can be configured to copy all matching
 * or non-matchine lines from the input file to the output file
 * 
 * @author Dion McMurtrie
 *
 * @goal filter-file
 */
public class FileFilterMojo extends AbstractMojo {
	
	/**
	 * File to be read and filtered using the supplied expression
	 * 
	 * @parameter
	 * @required
	 */
	File inputFile;
	
	/**
	 * File to write the filtering result to - NB if the file exists it will
	 * be overwritten
	 * 
	 * @parameter
	 * @required
	 */
	File outputFile;
	
	/**
	 * Regular expression used to match lines from the input file as a basis for
	 * filtering the file.
	 * 
	 * @parameter
	 * @required
	 */
	String expression;

	/**
	 * Indicates if matching lines from the input file should be copied to the output file.
	 * If true, only lines matching the expression will be copied to the output file, if
	 * false only lines not matching the expression will be copied to the output file.
	 * Default value is true.
	 * 
	 * @parameter
	 */
	boolean matchingLinesPreserved = true;
	
	/* (non-Javadoc)
	 * @see org.apache.maven.plugin.Mojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
			throw new MojoFailureException("The input file " + inputFile + " must exist, be a file and be readable");
		}
		
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(inputFile));
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
			
			String inputLine = reader.readLine();
			while (inputLine != null) {
				boolean lineMatches = inputLine.matches(expression);
				if (matchingLinesPreserved && lineMatches) {
					//configured to copy matching line and line matches
					writer.write(inputLine);
					writer.newLine();
				} else if (!matchingLinesPreserved && !lineMatches) {
					//configured to copy non-matching lines and line does not match
					writer.write(inputLine);
					writer.newLine();					
				}
				
				inputLine = reader.readLine();
			}
			
			reader.close();
			writer.close();
		} catch (IOException e) {
			throw new MojoExecutionException("Mojo failed due to IO failure, input file was " 
					+ inputFile + " output file was " + outputFile 
					+ " and the expression was " + expression, e);
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

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public boolean isMatchingLinesPreserved() {
		return matchingLinesPreserved;
	}

	public void setMatchingLinesPreserved(boolean matchingLinesPreserved) {
		this.matchingLinesPreserved = matchingLinesPreserved;
	}

}
