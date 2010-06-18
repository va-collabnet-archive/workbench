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
 *
 * @goal concat-files
 */
public class ConcatFilesMojo extends AbstractMojo {

	/**
	 * Files to be concatenated
	 *
	 * @parameter
	 * @required
	 */
	File[] inputFiles;

	/**
	 * The resulting file - NB if the file exists it will be
	 * overwritten
	 *
	 * @parameter
	 * @required
	 */
	File outputFile;

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
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        if (tmpFile.exists()) {
            tmpFile.delete();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(tmpFile));
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Mojo failed due to IO failure", e);
        }

        for (File inputFile : inputFiles) {
            if (!inputFile.exists() || !inputFile.isFile() || !inputFile.canRead()) {
                throw new MojoFailureException("The input file " + inputFile
                        + " must exist, be a file and be readable");
            }

            try {
                BufferedReader reader = new BufferedReader(
                        new FileReader(inputFile));
                String inputLine = reader.readLine();
                while (inputLine != null) {
                    writeLine(writer, inputLine);
                    inputLine = reader.readLine();
                }
                reader.close();
            } catch (IOException e) {
                throw new MojoExecutionException(
                        "Mojo failed due to IO failure, input file was "
                                + inputFile + " output file was " + outputFile, e);
            }
        }

        try {
            writer.close();
            if (outputFile.exists()) {
                outputFile.delete();
            }
            tmpFile.renameTo(outputFile);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Mojo failed due to IO failure", e);
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

    public File[] getInputFiles() {
        return inputFiles;
    }

    public void setInputFiles(File[] inputFiles) {
        this.inputFiles = inputFiles;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public boolean isUseDosLineTermination() {
        return useDosLineTermination;
    }

    public void setUseDosLineTermination(boolean useDosLineTermination) {
        this.useDosLineTermination = useDosLineTermination;
    }
}