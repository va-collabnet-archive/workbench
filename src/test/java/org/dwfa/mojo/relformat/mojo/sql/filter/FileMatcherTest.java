package org.dwfa.mojo.relformat.mojo.sql.filter;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public final class FileMatcherTest {

    private FileMatcher matcher;

    @Before
    public void setup() {
        matcher = new FileMatcherImpl();
    }

    @Test
    public void shouldFilterAMatchedFile() {
        String fileName = "current_relationships_au.gov.nehta.au-ct-release_1.5-SNAPSHOT.txt";
        boolean matched = matcher.match(fileName, Arrays.asList("current_(.)*\\.txt"));
        assertThat(matched, equalTo(true));
    }
    
    @Test
    public void shouldFilterAcrossMultipleFilters() {
        String fileName = "current_relationships_au.gov.nehta.au-ct-release_1.5-SNAPSHOT.txt";
        List<String> filters = Arrays.asList("xyz", "(.)*\\.txt");

        boolean matched = matcher.match(fileName, filters);
        assertThat(matched, equalTo(true));
    }

    @Test
    public void shouldNotFilterAnUnmatchedFile() {
        String fileName = "HelloWorld.txt";
        boolean matched = matcher.match(fileName, Arrays.asList("Concept(.)*\\.txt"));
        assertThat(matched, equalTo(false));
    }
}
