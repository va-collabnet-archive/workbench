package org.dwfa.mojo.file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test class for Ace to RF2 mojo.
 *
 * @author ean dungey
 */
public class Ace2Rf2Test {
    private static final String RELATIONSHIPS_RF2_TXT = "target/relationships.rf2.txt";
    private static final String DESCRIPTIONS_RF2_TXT = "target/descriptions.rf2.txt";
    private static final String CONCEPTS_RF2_TXT = "target/concepts.rf2.txt";
    private static final String IDS_RF2_TXT = "target/ids.rf2.txt";
    private static final String SRC_BUILD_DIRECTORY = "src/test/resources/org/dwfa/mojo/file";
    private static final String ACE_RELATIONSHIPS_TXT = "src/test/resources/org/dwfa/mojo/file/relationships.txt";
    private static final String ACE_DESCRIPTIONS_TXT = "src/test/resources/org/dwfa/mojo/file/descriptions.txt";
    private static final String ACE_IDS_TXT = "src/test/resources/org/dwfa/mojo/file/ids.txt";
    private static final String ACE_CONCEPTS_TXT = "src/test/resources/org/dwfa/mojo/file/concepts.txt";
    private static Ace2Rf2 ace2Rf2;

    /**
     * Setup Ace2Rf2 object
     */
    @BeforeClass
    public static void setup(){
        ace2Rf2 = new Ace2Rf2();
        ace2Rf2.setSourceDirectory(SRC_BUILD_DIRECTORY);
        ace2Rf2.setBuildDirectory(SRC_BUILD_DIRECTORY);

        ace2Rf2.setIdAceFile(ACE_IDS_TXT);
        ace2Rf2.setConceptAceFile(ACE_CONCEPTS_TXT);
        ace2Rf2.setDescriptionAceFile(ACE_DESCRIPTIONS_TXT);
        ace2Rf2.setRelationshipAceFile(ACE_RELATIONSHIPS_TXT);

        ace2Rf2.setIdentifierRf2File(IDS_RF2_TXT);
        ace2Rf2.setConceptRf2File(CONCEPTS_RF2_TXT);
        ace2Rf2.setDescriptionRf2File(DESCRIPTIONS_RF2_TXT);
        ace2Rf2.setRelationshipRf2File(RELATIONSHIPS_RF2_TXT);
    }

    /**
     * Test to Ace2Rf2 transformAce2Rf2 mojo.
     *
     * Simply checks that all rows are converted to RF2 format.
     *
     * @throws MojoExecutionException test error
     * @throws MojoFailureException test error
     * @throws IOException test error
     */
    @Test
    public void testAce2Rf2() throws MojoExecutionException, MojoFailureException, IOException {
        ace2Rf2.execute();

        Assert.assertTrue("Should be the same number of rows in the ACE and RF2 files.",
            lineCount(ACE_IDS_TXT) == lineCount(IDS_RF2_TXT));

        Assert.assertTrue("Should be the same number of rows in the ACE and RF2 files.",
            lineCount(ACE_CONCEPTS_TXT) == lineCount(CONCEPTS_RF2_TXT));

        Assert.assertTrue("Should be the same number of rows in the ACE and RF2 files.",
            lineCount(ACE_DESCRIPTIONS_TXT) == lineCount(DESCRIPTIONS_RF2_TXT));

        Assert.assertTrue("Should be the same number of rows in the ACE and RF2 files.",
            lineCount(ACE_RELATIONSHIPS_TXT) == lineCount(RELATIONSHIPS_RF2_TXT));
    }

    /**
     * Count the number of lines in the text file.
     *
     * @param filename String
     * @return long number of lines
     * @throws IOException on files opening and read errors.
     */
    private long lineCount(String filename) throws IOException {
        BufferedReader reader;
        long lineCount = 0;

        reader = new BufferedReader( new FileReader( ACE_IDS_TXT ) );

        for (;reader.readLine() != null;lineCount++) {
        }

        return lineCount;
    }
}
