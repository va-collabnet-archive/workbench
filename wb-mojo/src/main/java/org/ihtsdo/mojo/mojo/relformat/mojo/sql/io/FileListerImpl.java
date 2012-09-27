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
package org.ihtsdo.mojo.mojo.relformat.mojo.sql.io;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.filter.FileMatcher;

public final class FileListerImpl implements FileLister {

    private final FileMatcher fileMatcher;

    public FileListerImpl(final FileMatcher fileMatcher) {
        this.fileMatcher = fileMatcher;
    }

    public List<File> list(final File inputFile, final List<String> filters, final List<String> excludes) {
        File[] files = getFiles(inputFile);
        List<File> resolvedFiles = new ArrayList<File>();

        for (File file : files) {
            if (file.isDirectory()) {
                if (isValid(file, excludes)) {
                    resolvedFiles.addAll(list(file, filters, excludes));
                }
                continue;
            }

            if (fileMatcher.match(file.getName(), filters)) {
                resolvedFiles.add(file);
            }
        }

        return resolvedFiles;
    }

    private File[] getFiles(final File inputFile) {
        File[] files = inputFile.listFiles();
        return (files == null) ? new File[0] : files;
    }

    private boolean isValid(final File file, final List<String> excludes) {
        return !fileMatcher.shouldExclude(file.getName(), excludes);
    }
}
