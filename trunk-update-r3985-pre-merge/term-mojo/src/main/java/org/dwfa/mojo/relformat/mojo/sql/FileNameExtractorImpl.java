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
package org.dwfa.mojo.relformat.mojo.sql;

import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

import java.io.File;

public final class FileNameExtractorImpl implements FileNameExtractor {

    private int count = 1;
    private final String extention;

    public FileNameExtractorImpl(final String extention) {
        this.extention = extention;
    }

    public String extractFileName(final Table table, final File file) {
        String name = file.getName();

        return new StringBuilder().append(name.substring(0, name.lastIndexOf("."))).append("-").append(count++).append(
            extention).toString();
    }
}
