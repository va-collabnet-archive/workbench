package org.dwfa.mojo.memrefset.mojo;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.Arrays;

public final class ChangeSetNameComparerTest {

    private ChangeSetNameComparer comparer;

    @Before
    public void setup() {
        comparer = new ChangeSetNameComparerImpl();
    }

    @Test
    public void shouldNotMatchAFileWithTheIncorrectPrefix() {
        boolean result = comparer.containsPrefix("024f8047-58b8-48b2-89d0-173dc5b40caf",
                Arrays.asList("3fc2aecb-dca5-4729-9f51-82c5bf70529e.20090120T135334.xml"));
        assertThat(result, equalTo(false));
    }

    @Test
    public void shouldMatchAFileWithTheCorrectPrefix() {
        boolean result = comparer.containsPrefix("3fc2aecb-dca5-4729-9f51-82c5bf70529e",
                Arrays.asList("3fc2aecb-dca5-4729-9f51-82c5bf70529e.20090120T135334.xml"));
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldMatchAFileWhenSuppliedWithMultiplePrefixes() {
        boolean result = comparer.containsPrefix("d6c668fd-4772-45b4-89e6-48fe5e91f659",
                Arrays.asList(  "3fc2aecb-dca5-4729-9f51-82c5bf70529e.20090120T135334.xml",
                                "bead0066-afb6-4b08-b5dd-2bea72ee5162.20090120T135334.cmrscs",
                                "d6c668fd-4772-45b4-89e6-48fe5e91f659.20090120T135334.cmrscs",
                                "41a7baec-d45c-4d05-9278-07f25b6f489d.20090120T135334.cmrscs"));
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldNotMatchWhenSuppliedWithMultipleIncorrectPrefixes() {
        boolean result = comparer.containsPrefix("cebdf3ed-1c16-4f12-a23c-33246ad92df0.20090120T135334.cmrscs",
                Arrays.asList(  "3fc2aecb-dca5-4729-9f51-82c5bf70529e.20090120T135334.xml",
                                "bead0066-afb6-4b08-b5dd-2bea72ee5162.20090120T135334.cmrscs",
                                "d6c668fd-4772-45b4-89e6-48fe5e91f659.20090120T135334.cmrscs",
                                "41a7baec-d45c-4d05-9278-07f25b6f489d.20090120T135334.cmrscs"));
        assertThat(result, equalTo(false));
    }
}
