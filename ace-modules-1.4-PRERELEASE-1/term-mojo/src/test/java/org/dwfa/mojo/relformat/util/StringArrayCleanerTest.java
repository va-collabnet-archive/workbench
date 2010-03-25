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
        String[] array = { "1", "", "2", "3", "" };
        assertThat(cleaner.clean(array), equalTo(new String[] { "1", "2", "3" }));
    }

    @Test
    public void shouldCleanAnArrayWithPaddedElements() {
        String[] array = { "  1", "2   ", "3", "  4  " };
        assertThat(cleaner.clean(array), equalTo(new String[] { "1", "2", "3", "4" }));
    }

    @Test
    public void shouldCleanAnArrayWithSpacesAndPadding() {
        String[] array = { "  1", "", " 2   ", "3", "     ", "   4      ", "5", "", "", "6   " };
        assertThat(cleaner.clean(array), equalTo(new String[] { "1", "2", "3", "4", "5", "6" }));
    }
}
