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
package org.ihtsdo.mojo.maven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Goal which joins two data files with a given key. The first file is primary,
 * in that it is read in and establishes the hash map against which the second
 * file is joined.
 * 
 * @goal join
 * @phase generate-resources
 */
public class Join extends AbstractMojo {

    /**
     * @parameter
     * @required
     */
    private String inputFileOne;

    /**
     * Key column index starts at 0
     * 
     * @parameter
     * @required
     */
    private int fileOneKeyColumn;

    /**
     * @parameter
     * @required
     */
    private String inputFileTwo;

    /**
     * Key column index starts at 0
     * 
     * @parameter
     * @required
     */
    private int fileTwoKeyColumn;

    /**
     * @parameter
     * @required
     */
    private String outputFile;

    /**
     * @parameter
     */
    private String columnDelimiter = "\t";

    private static class ReadRecord {
        String[] file1;

        String[] file2;

        public String[] getFile1() {
            return file1;
        }

        public void setFile1(String[] file1) {
            this.file1 = file1;
        }

        public String[] getFile2() {
            return file2;
        }

        public void setFile2(String[] file2) {
            this.file2 = file2;
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        HashMap<String, ReadRecord> keyMap = new HashMap<String, ReadRecord>();
        try {
            BufferedReader fileOneReader = new BufferedReader(new FileReader(inputFileOne));
            try {
                while (true) {
                    String line = fileOneReader.readLine();
                    String[] fields = line.split(columnDelimiter);
                    if (keyMap.containsKey(fields[fileOneKeyColumn])) {
                        throw new MojoExecutionException("Duplicate key in inputFileOne: " + fields[fileOneKeyColumn]);
                    }
                    ReadRecord rec = new ReadRecord();
                    rec.setFile1(fields);
                    keyMap.put(fields[fileOneKeyColumn], rec);
                }
            } catch (EOFException eof) {
                fileOneReader.close();
            }
            BufferedReader fileTwoReader = new BufferedReader(new FileReader(inputFileTwo));
            try {
                while (true) {
                    String line = fileTwoReader.readLine();
                    String[] fields = line.split(columnDelimiter);
                    if (keyMap.containsKey(fields[fileTwoKeyColumn])) {
                        ReadRecord rec = keyMap.get(fields[fileTwoKeyColumn]);
                        rec.setFile2(fields);
                    }
                }
            } catch (EOFException eof) {
                fileTwoReader.close();
            }

            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));
            for (ReadRecord rec : keyMap.values()) {
                if (rec.getFile2() == null) {
                    throw new MojoExecutionException("No entry in file 2 for: " + Arrays.asList(rec.getFile1()));
                }
                for (String field : rec.getFile1()) {
                    outputWriter.append(field);
                    outputWriter.append(columnDelimiter);
                }
                for (int i = 0; i < rec.getFile2().length; i++) {
                    outputWriter.append(rec.getFile2()[i]);
                    if (i < rec.getFile2().length - 1) {
                        outputWriter.append(columnDelimiter);
                    }
                }
                outputWriter.append("\n");
            }
            outputWriter.close();
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    public String getColumnDelimiter() {
        return columnDelimiter;
    }

    public void setColumnDelimiter(String columnDelimiter) {
        this.columnDelimiter = columnDelimiter;
    }

    public int getFileOneKeyColumn() {
        return fileOneKeyColumn;
    }

    public void setFileOneKeyColumn(int fileOneKeyColumn) {
        this.fileOneKeyColumn = fileOneKeyColumn;
    }

    public int getFileTwoKeyColumn() {
        return fileTwoKeyColumn;
    }

    public void setFileTwoKeyColumn(int fileTwoKeyColumn) {
        this.fileTwoKeyColumn = fileTwoKeyColumn;
    }

    public String getInputFileOne() {
        return inputFileOne;
    }

    public void setInputFileOne(String inputFileOne) {
        this.inputFileOne = inputFileOne;
    }

    public String getInputFileTwo() {
        return inputFileTwo;
    }

    public void setInputFileTwo(String inputFileTwo) {
        this.inputFileTwo = inputFileTwo;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

}
