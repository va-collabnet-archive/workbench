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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.builder;

import org.ihtsdo.mojo.mojo.relformat.mojo.converter.builder.FileNameExtractorBuilder;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.io.SQLFileWriter;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.io.SQLFileWriterImpl;
import org.ihtsdo.mojo.mojo.relformat.mojo.sql.io.util.FileUtil;

public final class SQLFileWriterBuilder {

    private FileUtil fileUtil;
    private FileNameExtractorBuilder fileNameExtractorBuilder;

    public SQLFileWriterBuilder(final FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public SQLFileWriterBuilder withFileNameExtractor(final FileNameExtractorBuilder fileNameExtractorBuilder) {
        this.fileNameExtractorBuilder = fileNameExtractorBuilder;
        return this;
    }

    public SQLFileWriter build() {
        return new SQLFileWriterImpl(fileNameExtractorBuilder.build(), fileUtil);
    }
}
