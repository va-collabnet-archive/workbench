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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class InputFileFilterImpl implements InputFileFilter {

    private final FileMatcher fileMatcher;

    public InputFileFilterImpl(final FileMatcher fileMatcher) {
        this.fileMatcher = fileMatcher;
    }

    public List<File> filter(final File inputDir, final List<String> filters) {
        List<File> matchedFiles = new ArrayList<File>();

        String[] files = inputDir.list();

        for (String file : files) {
            if (fileMatcher.match(file, filters)) {
                matchedFiles.add(new File(file));
            }
        }

        return matchedFiles;
    }
}
