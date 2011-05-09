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
/**
 * 
 */
package org.dwfa.mojo.file.mappingfile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Generates an ACE SCTID mapping file from an AMT format concept, relationship or descriptions file.
 * 
 * TODO - add support for SNOMED and ARF formats
 * 
 * @author Dion McMurtrie
 * 
 * @goal generate-mapping-file
 * 
 */
public class MappingFileGenerator extends AbstractMojo {
    private static final String CONCEPTS_HEADER =
            "CONCEPTID	CONCEPTSTATUS	FULLYSPECIFIEDNAME	CTV3ID	SNOMEDID	ISPRIMITIVE	CONCEPTUUID	CONCEPTSTATUSUUID	EFFECTIVETIME";

    private static final String DESCRIPTIONS_HEADER =
            "DESCRIPTIONID	DESCRIPTIONSTATUS	CONCEPTID	TERM	INITIALCAPITALSTATUS	DESCRIPTIONTYPE	LANGUAGECODE	DESCRIPTIONUUID	DESCRIPTIONSTATUSUUID	DESCRIPTIONTYPEUUID	CONCEPTUUID	LANGUAGEUUID	CASESENSITIVITY	EFFECTIVETIME";

    private static final String RELATIONSHIPS_HEADER =
            "RELATIONSHIPID	CONCEPTID1	RELATIONSHIPTYPE	CONCEPTID2	CHARACTERISTICTYPE	REFINABILITY	RELATIONSHIPGROUP	RELATIONSHIPUUID	CONCEPTUUID1	RELATIONSHIPTYPEUUID	CONCEPTUUID2	CHARACTERISTICTYPEUUID	REFINABILITYUUID	RELATIONSHIPSTATUSUUID	EFFECTIVETIME";

    /**
     * The file to convert to a mapping file - must be a concepts, descriptions or relationships file
     * 
     * @parameter
     * @required
     */
    File inputFile;

    /**
     * The output mapping file - if it exists it will be overwritten!
     * 
     * @parameter
     * @required
     */
    File outputFile;

    /**
     * Indicates if data should be appended to the output file - default is not to overwrite
     * 
     * @parameter
     */
    boolean append = false;

    /**
     * Indicates if the generated mapping file is in "Legacy Map" format - default is not "Legacy Map" format
     * 
     * @parameter
     */
    boolean isLegacy = false;

    /**
     * Date to put in the mapping file - defaults to 2007-01-01 00:00:00
     * 
     * @parameter expression="2007-01-01 00:00:00"
     */
    String date;

    private BufferedReader reader;

    private BufferedWriter writer;

    public void execute() throws MojoExecutionException, MojoFailureException {
        openFiles();

        String header = getInputLine();

        if (header.equals(CONCEPTS_HEADER)) {
            processConceptsFile();
        } else if (header.equals(DESCRIPTIONS_HEADER) && !isLegacy) {
            processDescriptionsFile();
        } else if (header.equals(RELATIONSHIPS_HEADER) && !isLegacy) {
            processRelationshipsFile();
        } else {
            throw new MojoExecutionException("cannot process file " + inputFile
                + " file is not a concepts, description or relationships file");
        }
    }

    private void processRelationshipsFile() throws MojoExecutionException {
        writeTokenByIndex(0, 7);
    }

    private void processDescriptionsFile() throws MojoExecutionException {
        writeTokenByIndex(0, 7);
    }

    private void processConceptsFile() throws MojoExecutionException {
        if (isLegacy) {
            writeLegacyHeader();
            writeTokenByIndex(6, 2);
        } else {
            writeTokenByIndex(0, 6);
        }
    }

    private void writeLegacyHeader() throws MojoExecutionException {
        try {
            writer.write("CONCEPTUUID\tFULLYSPECIFIEDNAME");
            writer.newLine();
        } catch (IOException e) {
            throw new MojoExecutionException("failed writing to output file "
                + outputFile, e);
        }
    }

    private void writeTokenByIndex(int sctid, int uuid)
            throws MojoExecutionException {
        Pattern pattern = Pattern.compile("\\t");
        String[] tokens;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                tokens = pattern.split(line);
                if (isLegacy) {
                    writeToLegacyOutput(tokens[sctid], tokens[uuid]);
                } else {
                    writeToOutput(tokens[sctid], tokens[uuid]);
                }
            }
            writer.close();
        } catch (IOException e) {
            throw new MojoExecutionException("cannot read from input file "
                + inputFile, e);
        }
    }

    private void writeToOutput(String sctid, String uuid)
            throws MojoExecutionException {
        try {
            writer.write(uuid);
            writer.newLine();
            writer.write(sctid);
            writer.write("\t");
            writer.write(date);
            writer.newLine();
        } catch (IOException e) {
            throw new MojoExecutionException("failed writing to output file "
                + outputFile, e);
        }
    }

    private void writeToLegacyOutput(String uuid, String fsn)
            throws MojoExecutionException {
        try {
            writer.write(uuid);
            writer.write("\t");
            writer.write(fsn);
            writer.newLine();
        } catch (IOException e) {
            throw new MojoExecutionException("failed writing to output file "
                + outputFile, e);
        }
    }

    private String getInputLine() throws MojoExecutionException {
        try {
            String line = reader.readLine();

            if (line == null) {
                reader.close();
            }

            return line;
        } catch (IOException e) {
            throw new MojoExecutionException("failed reading input file "
                + inputFile, e);
        }
    }

    private void openFiles() throws MojoExecutionException {
        try {
            reader = new BufferedReader(new FileReader(inputFile));
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(
                "cannot execute - input file must exist, be readable and not be empty",
                e);
        }

        if (outputFile.exists()) {
            getLog().warn(
                "existing output file " + outputFile + " will be overwritten");
        }

        try {
            writer = new BufferedWriter(new FileWriter(outputFile, append));
        } catch (IOException e) {
            throw new MojoExecutionException(
                "cannot execute - input file must exist, be readable and not be empty",
                e);
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
