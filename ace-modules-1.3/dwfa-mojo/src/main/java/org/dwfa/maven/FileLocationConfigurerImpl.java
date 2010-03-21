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
package org.dwfa.maven;

import java.io.File;

public final class FileLocationConfigurerImpl implements FileLocationConfigurer {

    private static final String SQL_SUBDIR = "sql";
    private static final char FORWARD_SLASH = '/';

    private File sqlSourceDir;
    private File sqlTargetDir;
    private File dbDir;

    public FileLocationConfigurerImpl(final File sourceDirectory, final File outputDirectory, final String dbName) {
        sqlSourceDir = new File(sourceDirectory.getParentFile(), SQL_SUBDIR);
        sqlTargetDir = new File(outputDirectory, SQL_SUBDIR);
        sqlTargetDir.mkdirs();
        dbDir = new File(outputDirectory, dbName.replace(FORWARD_SLASH, File.separatorChar));
    }

    public File getSqlSourceDir() {
        return sqlSourceDir;
    }

    public File getSqlTargetDir() {
        return sqlTargetDir;
    }

    public File getDbDir() {
        return dbDir;
    }
}
