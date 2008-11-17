package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public final class FileNameExtractorWithDifferentExtentionsTest {

    private Table table;

    @Before
    public void setup() {
        table = Table.nullTable();
    }

    @Test
    public void shouldCreateATxtFileExtention() {

        FileNameExtractor extractor = new FileNameExtractorImpl(".txt");
        String fileName = extractor.extractFileName(table,
                new File("/somepath/arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT.txt"));
        assertThat(fileName, equalTo("arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-SNAPSHOT-1.txt"));
    }

    @Test
    public void shouldCreateAPropertiesFileExtention() {
        FileNameExtractor extractor = new FileNameExtractorImpl(".properties");
        String fileName = extractor.extractFileName(table,
                new File("/root/somepath/arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5.txt"));
        assertThat(fileName, equalTo("arf_sctid_concepts_au.gov.nehta.au-ct-release_1.5-1.properties"));
    }
}