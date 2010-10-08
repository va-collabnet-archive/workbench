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
package org.dwfa.mojo.refset.members.export;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.task.util.Logger;

import java.io.Writer;

public final class NoDescriptionWriterImpl implements NoDescriptionWriter {

    private final Writer writer;
    private final Logger logger;
    private final String lineSeparator;

    public NoDescriptionWriterImpl(final Writer writer, final String lineSeparator, final Logger logger) {
        this.lineSeparator = lineSeparator;
        this.logger = logger;
        this.writer = writer;
    }

    public void write(final I_GetConceptData concept) throws Exception {
        String conceptUuids = concept.getUids().iterator().next().toString();
        logger.logWarn("Concept " + conceptUuids + " has no active preferred term");
        writer.append("Concept ").append(conceptUuids).append(" has no active preferred term");
        writer.append(lineSeparator);
    }

    public void close() throws Exception {
        writer.flush();
        writer.close();
    }

    public NoDescriptionWriter append(final String text) throws Exception {
        writer.append(text);
        return this;
    }
}
