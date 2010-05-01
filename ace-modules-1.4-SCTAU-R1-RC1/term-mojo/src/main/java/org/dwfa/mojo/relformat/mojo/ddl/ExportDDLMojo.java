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
package org.dwfa.mojo.relformat.mojo.ddl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtilImpl;
import org.dwfa.mojo.relformat.util.SystemPropertyReaderImpl;
import org.dwfa.mojo.relformat.xml.ConfigFileWriterImpl;
import org.dwfa.mojo.relformat.xml.ReleaseConfig;
import org.dwfa.mojo.relformat.xml.ReleaseConfigReader;
import org.dwfa.mojo.relformat.xml.ReleaseConfigReaderImpl;
import org.dwfa.mojo.relformat.xml.ReleaseFormat;

import java.io.File;

/**
 * This plugin exports ddl statements (Schema) from a release format config
 * file.
 * 
 * @goal export-release-format-ddl
 * @phase process-resources
 */
public final class ExportDDLMojo extends AbstractMojo {

    /**
     * The name of the file to export the ddl statements to.
     * 
     * @parameter default-value="${project.build.directory}/ddl/schema.sql"
     */
    private String exportFile;

    /**
     * The location of the ReleaseConfig.xml file.
     * 
     * @parameter expression="${basedir}/src/main/resources/ReleaseConfig.xml"
     */
    private File releaseFileLocation;

    private final ReleaseConfigReader configReader = new ReleaseConfigReaderImpl();
    private final String lineSeparator = new SystemPropertyReaderImpl().getLineSeparator();

    public void execute() throws MojoExecutionException, MojoFailureException {
        StringBuilder builder = new StringBuilder();
        ReleaseConfig config = configReader.reader(releaseFileLocation);

        for (ReleaseFormat releaseFormat : config.getReleaseFormats()) {
            builder.append(releaseFormat.getSchema().trim());
            builder.append(lineSeparator);
            builder.append(lineSeparator);
        }

        createConfigFileWriter().write(exportFile, builder.toString());
    }

    private ConfigFileWriterImpl createConfigFileWriter() {
        return new ConfigFileWriterImpl(getLog(), new FileUtilImpl());
    }
}
