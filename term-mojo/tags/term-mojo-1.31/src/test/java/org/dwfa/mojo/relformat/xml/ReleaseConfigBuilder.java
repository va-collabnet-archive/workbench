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

public class ReleaseConfigBuilder {

    private static final String START_CONFIG = "<ReleaseConfig>";
    private static final String END_CONFIG = "</ReleaseConfig>";

    private static final String START_RELEASE_FORMAT = "<ReleaseFormat>";
    private static final String END_RELEASE_FORMAT = "</ReleaseFormat>";

    private static final String START_NAME = "<Type>";
    private static final String END_NAME = "</Type>";

    private static final String START_SCHEMA = "<Schema>";
    private static final String END_SCHEMA = "</Schema>";

    private final StringBuilder builder;

    public ReleaseConfigBuilder() {
        builder = new StringBuilder();
        builder.append(START_CONFIG);
    }

    public ReleaseConfigBuilder createReleaseFormat() {
        builder.append(START_RELEASE_FORMAT);
        return this;
    }

    public ReleaseConfigBuilder addName(final String name) {
        builder.append(START_NAME).append(name).append(END_NAME);
        return this;
    }

    public ReleaseConfigBuilder addSchema(final String schema) {
        builder.append(START_SCHEMA).append(schema).append(END_SCHEMA);
        return this;
    }

    public ReleaseConfigBuilder addReleaseFormat() {
        builder.append(END_RELEASE_FORMAT);
        return this;
    }

    public String build() {
        return builder.append(END_CONFIG).toString();
    }
}
