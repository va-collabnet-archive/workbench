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
package org.dwfa.mojo.relformat.mojo.converter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.relformat.mojo.converter.builder.FileNameExtractorBuilder;
import org.dwfa.mojo.relformat.mojo.converter.builder.LineToDerbyLineConverterBuilder;
import org.dwfa.mojo.relformat.mojo.sql.TableCache;
import org.dwfa.mojo.relformat.mojo.sql.builder.SQLFileWriterBuilder;
import org.dwfa.mojo.relformat.mojo.sql.builder.TableCacheBuilder;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverter;
import org.dwfa.mojo.relformat.mojo.sql.filter.FileMatcherImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.FileLister;
import org.dwfa.mojo.relformat.mojo.sql.io.FileListerImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.SQLFileWriter;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtilImpl;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;
import org.dwfa.mojo.relformat.xml.ReleaseConfigReader;
import org.dwfa.mojo.relformat.xml.ReleaseConfigReaderImpl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * This plugin processes files from au-ct-release and converts them into a
 * derby-edible format.
 * 
 * @goal convert-to-derby-format
 * @phase process-resources
 */
public final class ConvertToDerbyFormatMojo extends AbstractMojo {

    // Skip refsets-no-marked-parents by default.
    // We could make this into an attribute of Format such as List<String>
    // excludes, with a default value if need be.
    // The reason this has not been done is because we would then have to
    // specify it for all refset formats.
    private static final String REFSET_NO_MARKED_PARENTS = "refsets-no-marked-parents";

    /**
     * The loction of where the sql files are written.
     * 
     * @parameter expression="${project.build.directory}/sql"
     */
    private String outputDirectory;

    /**
     * The location from which the ReleaseFormat text files are read.
     * 
     * @parameter expression="${basedir}/src/main/resources"
     */
    private File inputDirectory;

    /**
     * Turns verbose logging on/off. You probably want this turned off, unless
     * you are chasing a bug.
     * 
     * @parameter default-value="false"
     */
    private boolean verbose;

    /**
     * Tells SQL Write to concatenate files into 1 file
     * 
     * @parameter default-value="false"
     */
    private boolean concatfiles;

    /**
     * The location of the ReleaseConfig.xml file. Mappings between release
     * format files and exported sql
     * files are done through the pom.xml and the ReleaseConfig.xml.
     * 
     * @parameter expression="${basedir}/src/main/resources/ReleaseConfig.xml"
     */
    private File releaseFileLocation;

    /**
     * The configuration for each ReleaseFormat that is to be exported to sql.
     * 
     * @parameter
     * @required
     */
    private List<Format> formats;

    private final FileUtil fileUtil = new FileUtilImpl();

    private final FileNameExtractorBuilder fileNameExtractorBuilder = new FileNameExtractorBuilder().withExtension(
        ".derb").withDerbyExporter();

    private final SQLFileWriter sqlFileWriter = new SQLFileWriterBuilder(fileUtil).withFileNameExtractor(
        fileNameExtractorBuilder).build();

    private final TableCache tableCache = new TableCacheBuilder().build();

    private final LineToSQLConverter lineToDerbyLineConverter = new LineToDerbyLineConverterBuilder().build();

    private final ReleaseConfigReader configReader = new ReleaseConfigReaderImpl();
    private FileLister fileLister = new FileListerImpl(new FileMatcherImpl());

    public void execute() throws MojoExecutionException, MojoFailureException {
        tableCache.cache(configReader.reader(releaseFileLocation));

        logInfo("inputDirectory", inputDirectory);
        logInfo("releaseFileLocation", releaseFileLocation);

        for (Format format : formats) {
            try {
                logInfo("processing format", format.getType());
                List<File> matchingFiles = fileLister.list(inputDirectory, format.getFilters(),
                    Arrays.asList(REFSET_NO_MARKED_PARENTS));

                for (File aFile : matchingFiles) {
                    logInfo("processing file", aFile);
                    String fileName = "";
                    if (format.getaddfilename()) fileName = aFile.getName();
                    sqlFileWriter.writer(aFile, getTable(format), outputDirectory, lineToDerbyLineConverter, concatfiles, fileName);
                }
            } catch (Exception e) {
                getLog().error(e); // if a format fails, log and keep going to
                                   // the next.
            }
        }
    }

    private Table getTable(final Format type) {
        return tableCache.getTable(type.getType());
    }

    private void logInfo(final String attribute, final Object message) {
        if (verbose)
            getLog().info(attribute + " :" + message);
    }
}
