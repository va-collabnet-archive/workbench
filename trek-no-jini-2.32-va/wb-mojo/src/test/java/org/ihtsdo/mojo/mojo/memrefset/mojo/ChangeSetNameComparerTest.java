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
package org.ihtsdo.mojo.mojo.memrefset.mojo;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

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
        boolean result = comparer.containsPrefix("d6c668fd-4772-45b4-89e6-48fe5e91f659", Arrays.asList(
            "3fc2aecb-dca5-4729-9f51-82c5bf70529e.20090120T135334.xml",
            "bead0066-afb6-4b08-b5dd-2bea72ee5162.20090120T135334.cmrscs",
            "d6c668fd-4772-45b4-89e6-48fe5e91f659.20090120T135334.cmrscs",
            "41a7baec-d45c-4d05-9278-07f25b6f489d.20090120T135334.cmrscs"));
        assertThat(result, equalTo(true));
    }

    @Test
    public void shouldNotMatchWhenSuppliedWithMultipleIncorrectPrefixes() {
        boolean result = comparer.containsPrefix("cebdf3ed-1c16-4f12-a23c-33246ad92df0.20090120T135334.cmrscs",
            Arrays.asList("3fc2aecb-dca5-4729-9f51-82c5bf70529e.20090120T135334.xml",
                "bead0066-afb6-4b08-b5dd-2bea72ee5162.20090120T135334.cmrscs",
                "d6c668fd-4772-45b4-89e6-48fe5e91f659.20090120T135334.cmrscs",
                "41a7baec-d45c-4d05-9278-07f25b6f489d.20090120T135334.cmrscs"));
        assertThat(result, equalTo(false));
    }
}
