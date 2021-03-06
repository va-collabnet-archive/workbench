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
package org.ihtsdo.mojo.mojo.relformat.mojo.converter.builder;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.DerbyImportFileNameExtractor;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.FileNameExtractor;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.FileNameExtractorImpl;

public final class FileNameExtractorBuilder {

    private String extension;
    private boolean genericExporter = true;

    public FileNameExtractorBuilder withExtension(final String extension) {
        this.extension = extension;
        return this;
    }

    public FileNameExtractorBuilder withGenericExporter() {
        this.genericExporter = true;
        return this;
    }

    public FileNameExtractorBuilder withDerbyExporter() {
        this.genericExporter = false;
        return this;
    }

    public FileNameExtractor build() {
        if (genericExporter) {
            return new FileNameExtractorImpl(extension);
        }

        return new DerbyImportFileNameExtractor(new FileNameExtractorImpl(extension));

    }
}
