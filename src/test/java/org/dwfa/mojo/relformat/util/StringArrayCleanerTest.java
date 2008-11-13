package org.dwfa.mojo.relformat.util;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public final class StringArrayCleanerTest {

    private StringArrayCleaner cleaner;

    @Before
    public void setup() {
        cleaner = new StringArrayCleanerImpl();
    }

    @Test
    public void shouldCleanAnArrayWithSpaces() {
        String[] array = {"1", "", "2", "3", ""};
        assertThat(cleaner.clean(array), equalTo(new String[] {"1", "2", "3"}));
    }

    @Test
    public void shouldCleanAnArrayWithPaddedElements() {
        String[] array = {"  1" , "2   ", "3", "  4  "};
        assertThat(cleaner.clean(array), equalTo(new String[] {"1", "2", "3", "4"}));
    }

    @Test
    public void shouldCleanAnArrayWithSpacesAndPadding() {
        String[] array = {"  1", "", " 2   ", "3", "     ", "   4      ", "5", "", "", "6   "};
        assertThat(cleaner.clean(array), equalTo(new String[] {"1", "2", "3", "4", "5", "6"}));
    }
}
