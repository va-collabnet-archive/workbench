package org.dwfa.mojo.relformat.mojo.directimport;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class HyphenatedTableNameExtractorTest {
    
    private TableNameExtractor extractor;

    @Before
    public void setup() {
        extractor = new HyphenatedTableNameExtractor();
    }
    
    @Test
    public void shouldExtractACapitalisedTableName() {
        assertThat(extractor.extract("arf_uuid_identifiers-arf_uuid_identifiers_au.gov.nehta.au-ct-release.derb"), 
                equalTo("ARF_UUID_IDENTIFIERS"));
    }
}
