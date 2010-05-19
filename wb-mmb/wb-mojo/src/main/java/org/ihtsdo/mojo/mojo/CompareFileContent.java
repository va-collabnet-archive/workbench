/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.mojo.mojo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * <h1>CompareFileContent</h1>
 * <p>
 * This class is used to compare the contents of two files. It is configurable
 * from a maven pom file<br>
 * by passing the two files to be compared as parameters. If the contents of the
 * files are found to be different<br>
 * the maven build will fail and a message logged.
 * </p>
 * 
 * @goal compare-files
 * 
 */
public class CompareFileContent extends AbstractMojo {

    /**
     * <h2>firstFile</h2> First file to use for comparison
     * 
     * @parameter
     * @required
     */
    private File firstFile;

    /**
     * <h2>secondFile</h2>
     * 
     * Second file to use for comparison
     * 
     * @parameter
     * @required
     */
    private File secondFile;

    /**
     * <h2>spec</h2> Configuration object which contains the relevant config
     * details.
     * 
     * <h3>Spec elements and valid values:</h3>
     * <ol>
     * <li>comparisonBase - the type of comparison to be performed over file
     * data. There are only two valid values BYTE or TEXT</li><br>
     * </br>
     * <li>delimeter - the delimeter used by the file to separate fields</br>
     * i.e. "\t" for tab delimeter</li><br>
     * </br>
     * <li>excludedFields - comma separated list of fields to be excluded from
     * comaprison. Zero based numeric value</br> i.e. "0,3,5"</li>
     * </ol>
     * 
     * @parameter
     * @required
     */
    private ComparisonSpec spec;

    /*
     * private parameters not passed from maven
     */
    private String containDiffsMsg = "The two files being compared contain differences in content.";
    private String fileNotFoundMsg = "One or both of the files to be compared do not exist!";
    private String typeMismatchMsg = "The two files are of diferent types. Ensure both are either files or directories.";
    private String fileInfoMsg = "";

    /*
     * Mojo execution method.
     * 
     * @see org.apache.maven.plugin.AbstractMojo#execute()
     */
    public void execute() throws MojoExecutionException {
        try {
            if (firstFile.exists() && secondFile.exists()) {
                if (firstFile.isDirectory() && secondFile.isDirectory()) {

                    if (!firstFile.isDirectory() || !secondFile.isDirectory())
                        throw new ComparisonFailure(typeMismatchMsg);
                    else
                        processDirectories(firstFile, secondFile);

                } else {
                    setFileInfoMsg(firstFile, secondFile);
                    compareFiles(firstFile, secondFile);
                }// End if/else
            }// End if
        } catch (ComparisonFailure e) {
            throw new MojoExecutionException(e.getMessage());
        }
    }// End method execute

    /*
     * This method is used to set a class scope variable value.
     * The variable is used to give an indication of which files failed
     * comparison.
     */
    private void setFileInfoMsg(File file1, File file2) {
        fileInfoMsg = "( file 1 >>" + file1.getName() + " : file 2 >>" + file2.getName() + ")";
    }

    /*
     * This method is used to determine the type of comparison required based on
     * the configuration.
     */
    private void compareFiles(File file1, File file2) throws MojoExecutionException {
        if (spec.getComparisonBase().equalsIgnoreCase("BYTE"))
            byteComparison(file1, file2);
        else
            textComparison(file1, file2);
    }// End method processFiles

    /*
     * This method performs a text based comparison of two given files.
     */
    private boolean textComparison(File file1, File file2) throws MojoExecutionException {
        try {

            if (file1.isDirectory() || file2.isDirectory())
                return false;

            BufferedReader f1Reader = new BufferedReader(new FileReader(file1));
            BufferedReader f2Reader = new BufferedReader(new FileReader(file2));

            String f1Line = f1Reader.readLine();
            String f2Line = f2Reader.readLine();

            List<String> excludedFields = new ArrayList<String>();

            if (spec.getExcludedFields() != null) {
                String[] Fields = spec.getExcludedFields().split(",");
                for (String field : Fields) {
                    excludedFields.add(field);
                }
            }

            while (f1Line != null || f2Line != null) {

                if ((f1Line == null && f2Line != null) || (f1Line != null && f2Line == null))
                    throw new ComparisonFailure(containDiffsMsg + fileInfoMsg);

                if (spec.getDelimeter() == null) {
                    if (!f1Line.equals(f2Line))
                        throw new ComparisonFailure(containDiffsMsg + fileInfoMsg);
                } else {
                    String[] f1Tokens = (f1Line == null) ? null : f1Line.split(spec.getDelimeter());
                    String[] f2Tokens = (f2Line == null) ? null : f2Line.split(spec.getDelimeter());

                    int tokenIndex = 0;
                    for (String token : f1Tokens) {
                        if (!excludedFields.contains(new Integer(tokenIndex).toString()))
                            if (!token.equalsIgnoreCase(f2Tokens[tokenIndex].toString()))
                                throw new ComparisonFailure(containDiffsMsg + fileInfoMsg);

                        tokenIndex++;
                    }// End for loop
                }// End if/else

                f1Line = f1Reader.readLine();
                f2Line = f2Reader.readLine();

            }// End while loop

            getLog().info("Contents of files are identicial.");

        } catch (ComparisonFailure e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        return true;
    }// End method textComparison

    /*
     * This method performs a byte based comparison of two given files.
     */
    private boolean byteComparison(File file1, File file2) throws MojoExecutionException {
        try {

            if (file1.isDirectory() || file2.isDirectory())
                return false;

            InputStream is1 = new FileInputStream(file1);
            InputStream is2 = new FileInputStream(file2);

            while (true) {
                int c1 = is1.read();
                int c2 = is2.read();
                if (c1 != c2) {
                    throw new ComparisonFailure(containDiffsMsg);
                }
                if (c1 < 0 || c2 < 0) {
                    break;
                }
            }

            getLog().info("Contents of files are identicial.");

            is1.close();
            is2.close();
        } catch (ComparisonFailure e) {
            throw new MojoExecutionException(e.getMessage());
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(fileNotFoundMsg);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        return true;
    }// End method byteComparison

    /*
     * This method iterates over the files in two given directories and executes
     * the comparison on files with identical names in each directory.
     */
    private void processDirectories(File directory1, File directory2) throws MojoExecutionException {
        HashMap<String, File> dir1Files = getFiles(directory1);
        HashMap<String, File> dir2Files = getFiles(directory2);

        for (String key : dir1Files.keySet()) {
            setFileInfoMsg(dir1Files.get(key), dir2Files.get(key));
            compareFiles(dir1Files.get(key), dir2Files.get(key));

        }// End for loop
    }// End method processDirectories

    /*
     * This method populates a HashMap with the files in a given directory.
     */
    private HashMap<String, File> getFiles(File directory) {
        HashMap<String, File> files = new HashMap<String, File>();
        for (File file : directory.listFiles()) {
            if (!file.isDirectory())
                files.put(file.getName(), file);
        }

        return files;
    }// End method getFiles

    /*
     * Custom exception so we can exit and notify of file comparison failure
     */
    private class ComparisonFailure extends Exception {
        public ComparisonFailure(String message) {
            super(message);
        }
    }// End class ComparisonFailure

}// End class CompareFileContent
