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

public class RefsetWriterParameterObjectImpl implements RefsetWriterParameterObject {
    private final ProgressLogger progressLogger;
    private final RefsetTextWriter refsetTextWriter;
    private final WriterFactory writerFactory;

    public RefsetWriterParameterObjectImpl(final ProgressLogger progressLogger,
            final RefsetTextWriter refsetTextWriter, final WriterFactory writerFactory) {
        this.progressLogger = progressLogger;
        this.refsetTextWriter = refsetTextWriter;
        this.writerFactory = writerFactory;
    }

    public ProgressLogger getProgressLogger() {
        return progressLogger;
    }

    public RefsetTextWriter getRefsetTextWriter() {
        return refsetTextWriter;
    }

    public WriterFactory getWriterFactory() {
        return writerFactory;
    }
}
