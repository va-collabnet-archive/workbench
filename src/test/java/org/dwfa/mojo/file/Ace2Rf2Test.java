package org.dwfa.mojo.file;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class Ace2Rf2Test {
    private static Ace2Rf2 ace2Rf2;

    @BeforeClass
    public static void setup(){
        ace2Rf2 = new Ace2Rf2();
        ace2Rf2.setSourceDirectory("src/test/resources/org/dwfa/mojo/file");
        ace2Rf2.setBuildDirectory("src/test/resources/org/dwfa/mojo/file");

        ace2Rf2.setIdAceFile("src/test/resources/org/dwfa/mojo/file/ids.txt");
        ace2Rf2.setConceptAceFile("src/test/resources/org/dwfa/mojo/file/concepts.txt");
        ace2Rf2.setDescriptionAceFile("src/test/resources/org/dwfa/mojo/file/descriptions.txt");
        ace2Rf2.setRelationshipAceFile("src/test/resources/org/dwfa/mojo/file/relationships.txt");

        ace2Rf2.setIdentifierRf2File("src/test/resources/org/dwfa/mojo/file/ids.rf2.txt");
        ace2Rf2.setConceptRf2File("src/test/resources/org/dwfa/mojo/file/concepts.rf2.txt");
        ace2Rf2.setDescriptionRf2File("src/test/resources/org/dwfa/mojo/file/descriptions.rf2.txt");
        ace2Rf2.setRelationshipRf2File("src/test/resources/org/dwfa/mojo/file/relationships.rf2.txt");
    }

    @Test
    public void testAce2Rf2() throws MojoExecutionException, MojoFailureException {
        ace2Rf2.execute();
    }
}
