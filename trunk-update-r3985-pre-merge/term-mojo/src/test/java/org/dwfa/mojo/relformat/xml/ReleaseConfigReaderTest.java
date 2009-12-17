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
package org.dwfa.mojo.relformat.xml;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;

public class ReleaseConfigReaderTest {

    private ReleaseConfigReader reader;
    private static final String NAME_1 = "ARF-SCTID-CONCEPT";
    private static final String SCHEMA_1 = "create table blah (...)";
    private static final String NAME_2 = "CURRENT-CONCEPT";
    private static final String SCHEMA_2 = "create table blue (...)";

    @Before
    public void setup() {
        reader = new ReleaseConfigReaderImpl();
    }

    @Test
    public void shouldReturnAReleaseConfigFile() {
        String configContent = new ReleaseConfigBuilder().createReleaseFormat()
            .addName(NAME_1)
            .addSchema(SCHEMA_1)
            .addReleaseFormat()
            .createReleaseFormat()
            .addName(NAME_2)
            .addSchema(SCHEMA_2)
            .addReleaseFormat()
            .build();

        ReleaseConfig config = reader.reader(new ByteArrayInputStream(configContent.getBytes()));
        assertThat(config.getReleaseFormats().size(), equalTo(2));

        expectReleaseFormat1(config.getReleaseFormats().get(0));
        expectReleaseFormat2(config.getReleaseFormats().get(1));
    }

    private void expectReleaseFormat1(final ReleaseFormat releaseFormat) {
        expectCommonReleaseFormat(releaseFormat, NAME_1, SCHEMA_1);
    }

    private void expectReleaseFormat2(final ReleaseFormat releaseFormat) {
        expectCommonReleaseFormat(releaseFormat, NAME_2, SCHEMA_2);
    }

    private void expectCommonReleaseFormat(final ReleaseFormat releaseFormat, final String name, final String schema) {
        assertThat(releaseFormat.getType(), equalTo(name));
        assertThat(releaseFormat.getSchema(), equalTo(schema));
    }
}
