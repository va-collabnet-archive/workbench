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
package org.dwfa.mojo.file;

/**
 * Used in the Ace2Rf2 mojo this pojo holds the details of an identifier file to
 * transform to RF2 format.
 */
public class IdentifierFile {

    String inputFileName;
    String outputFileName;
    boolean headerLine;

    public IdentifierFile(String inputFileName, String outputFileName, boolean headerLine) {
        this.inputFileName = inputFileName;
        this.outputFileName = outputFileName;
        this.headerLine = headerLine;
    }

    // needed by Maven
    public IdentifierFile() {
    }

    public String getInputFileName() {
        return inputFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public boolean isHeaderLine() {
        return headerLine;
    }

    public void setHeaderLine(boolean headerLine) {
        this.headerLine = headerLine;
    }

    public void setInputFileName(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }
}
