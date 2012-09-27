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
package org.ihtsdo.mojo.mojo.file.mappingfile;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MappingFileGeneratorTest {

    private static final String BASE_RESOURCE_DIR = "target/generated-resources/mappingFiles/";
    private MappingFileGenerator generator;

    private File conceptsInputFile = new File(BASE_RESOURCE_DIR
        + "Uuid_sct_concepts_au.gov.nehta.amt.standalone_1.6.txt");
    private File descriptionsInputFile = new File(BASE_RESOURCE_DIR
        + "Uuid_sct_descriptions_au.gov.nehta.amt.standalone_1.6.txt");
    private File relationshipsInputFile = new File(BASE_RESOURCE_DIR
        + "Uuid_sct_relationships_au.gov.nehta.amt.standalone_1.6.txt");

    private File conceptsResultFile = new File(BASE_RESOURCE_DIR + "NEHTA-AMT-CONCEPT-sct-map-rw.txt");
    private File descriptionsResultFile = new File(BASE_RESOURCE_DIR + "NEHTA-AMT-DESCRIPTION-sct-map-rw.txt");
    private File relationshipsResultFile = new File(BASE_RESOURCE_DIR + "NEHTA-AMT-RELATIONSHIP-sct-map-rw.txt");

    private File appendTest = new File(BASE_RESOURCE_DIR + "appendTest.txt");

    private File legacyConceptIds = new File(BASE_RESOURCE_DIR + "legacyConceptIds.txt");

    @Before
    public void setup() {
        generator = new MappingFileGenerator();
    }

    @Test
    @Ignore
    public void testConceptsFile() throws MojoExecutionException, MojoFailureException, IOException {
        File outputFile = new File(BASE_RESOURCE_DIR + "mappingFileGeneratorTest-concepts-outfile.txt");
        generator.setInputFile(conceptsInputFile);
        generator.setOutputFile(outputFile);
        generator.setDate("2007-10-19 00:00:00");
        generator.execute();

        Collection<String> expectedExtraTestFileLines = new ArrayList<String>();
        expectedExtraTestFileLines.add("46bccdc4-8fb6-11db-b606-0800200c9a66+116680003");

        Collection<String> expectedExtraResultSetLines = new HashSet<String>();
        BufferedReader resultFileReader = new BufferedReader(new FileReader(legacyConceptIds));
        String line;
        int expectedlineCount = 0;
        while ((line = resultFileReader.readLine()) != null) {
            expectedlineCount++;
            expectedExtraResultSetLines.add(line);
        }
        resultFileReader.close();

        validateFilesEquivalent(outputFile, conceptsResultFile, expectedExtraTestFileLines, expectedExtraResultSetLines);
    }

    @Test
    @Ignore
    public void testConceptsFileAppend() throws MojoExecutionException, MojoFailureException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(appendTest));
        File outputFile = new File(appendTest.getAbsoluteFile() + ".appended_output_file.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, false));

        String line;
        while ((line = reader.readLine()) != null) {
            writer.write(line);
            writer.newLine();
        }

        reader.close();
        writer.close();

        generator.setInputFile(conceptsInputFile);
        generator.setOutputFile(outputFile);
        generator.setDate("2007-10-19 00:00:00");
        generator.append = true;
        generator.execute();

        Collection<String> expectedExtraTestFileLines = new ArrayList<String>();
        expectedExtraTestFileLines.add("46bccdc4-8fb6-11db-b606-0800200c9a66+116680003");

        validateFilesEquivalent(outputFile, conceptsResultFile, expectedExtraTestFileLines, new HashSet<String>());
    }

    @Test
    @Ignore
    public void testDescriptionsFile() throws MojoExecutionException, MojoFailureException, IOException {
        File outputFile = new File(BASE_RESOURCE_DIR + "mappingFileGeneratorTest-descriptions-outfile.txt");
        generator.setInputFile(descriptionsInputFile);
        generator.setOutputFile(outputFile);
        generator.setDate("2007-10-19 00:00:00");
        generator.execute();

        validateFilesEquivalent(outputFile, descriptionsResultFile);
    }

    @Test
    @Ignore
    public void testRelationshipsFile() throws MojoExecutionException, MojoFailureException, IOException {
        File outputFile = new File(BASE_RESOURCE_DIR + "mappingFileGeneratorTest-relationships-outfile.txt");
        generator.setInputFile(relationshipsInputFile);
        generator.setOutputFile(outputFile);
        generator.setDate("2007-10-19 00:00:00");
        generator.execute();

        validateFilesEquivalent(outputFile, relationshipsResultFile);
    }

    private void validateFilesEquivalent(File testFile, File expectedResult) throws IOException {
        validateFilesEquivalent(testFile, expectedResult, new ArrayList<String>(), new HashSet<String>());
    }

    /**
     * asserts that every file in the test file is in the result file and vice
     * versa
     * 
     * @throws IOException
     */
    private void validateFilesEquivalent(File testFile, File expectedResult,
            Collection<String> expectedExtraTestFileLines, Collection<String> expectedExtraResultSetLines)
            throws IOException {
        BufferedReader resultFileReader = new BufferedReader(new FileReader(expectedResult));
        HashSet<String> resultSet = new HashSet<String>();
        String line;
        int expectedlineCount = 0;
        while ((line = resultFileReader.readLine()) != null) {
            expectedlineCount++;
            resultSet.add(line + "+" + removeDate(resultFileReader.readLine()));
        }
        resultFileReader.close();

        assertEquals("result set set should be the same as the number of read lines", expectedlineCount,
            resultSet.size());

        BufferedReader testFileReader = new BufferedReader(new FileReader(testFile));
        int actualLineCount = 0;
        ArrayList<String> extraLines = new ArrayList<String>();
        while ((line = testFileReader.readLine()) != null) {
            actualLineCount++;
            line += "+" + removeDate(testFileReader.readLine());
            if (!resultSet.contains(line)) {
                extraLines.add(line);
            } else {
                resultSet.remove(line);
            }
        }
        testFileReader.close();

        assertEquals("test file contains " + extraLines.size() + " extra lines not in expected result", extraLines,
            expectedExtraTestFileLines);

        assertEquals("expected result file contains " + resultSet.size() + " extra lines not in test file", resultSet,
            expectedExtraResultSetLines);

    }

    private String removeDate(String fileLine) {

        return fileLine.split("\t")[0];
    }
}
