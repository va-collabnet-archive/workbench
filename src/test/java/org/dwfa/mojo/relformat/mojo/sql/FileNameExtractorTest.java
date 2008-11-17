package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public final class FileNameExtractorTest {

    private FileNameExtractor extractor;
    private Table table;


    @Before
    public void setup() {
        extractor = new FileNameExtractorImpl(".sql");
        table = Table.nullTable();
    }

    @Test
    public void shouldExtractAConceptFileName() {
        String fileName = extractor.extractFileName(table,
                new File("/somepath/arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT.txt"));
        assertThat(fileName, equalTo("arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT-1.sql"));
    }
    
    @Test
    public void shouldExtractARefsetFileName() {
        String fileName = extractor.extractFileName(table,
                new File("c:/anotherpath/refsets/SCTID_Path version reference set_SNOMED Clinical Terms Australian " +
                        "Extension_20081031T000000Z.string.refset"));
        assertThat(fileName, equalTo("SCTID_Path version reference set_SNOMED Clinical Terms Australian " +
                "Extension_20081031T000000Z.string-1.sql"));
    }
    @Test
    public void shouldAddACountToEachFileName() {
        String fileName = extractor.extractFileName(table,
                new File("c:/anotherpath/refsets/SCTID_Path version reference set_SNOMED Clinical Terms Australian " +
                        "Extension_20081031T000000Z.string.refset"));
        assertThat(fileName, equalTo("SCTID_Path version reference set_SNOMED Clinical Terms " +
                "Australian Extension_20081031T000000Z.string-1.sql"));

        fileName = extractor.extractFileName(table,
                new File("c:/refsets/UUID_Result test name member reference set_SNOMED Clinical Terms " +
                        "Australian Extension_20081031T000000Z.concept.refset"));
        assertThat(fileName, equalTo("UUID_Result test name member reference set_SNOMED Clinical Terms " +
                "Australian Extension_20081031T000000Z.concept-2.sql"));
    }    
}
