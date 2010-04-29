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
package org.dwfa.mojo.memrefset.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.mojo.relformat.mojo.sql.filter.FileMatcherImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.FileLister;
import org.dwfa.mojo.relformat.mojo.sql.io.FileListerImpl;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtilImpl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * This plugin exports ddl statements (Schema) from a release format config
 * file.
 * 
 * @goal convert-member-refset-to-xml
 * @phase process-resources
 */
public final class MemberRefsetExportMojo extends AbstractMojo {

    /**
     * Directory from which the input files are read.
     * 
     * @parameter default-value=${project.build.directory}/member-refset
     * @required
     */
    private File sourceDirectory;

    /**
     * Directory to which output files are written.
     * 
     * @parameter 
     *            default-value=${project.build.directory}/transformed-member-refset
     * @required
     */
    private String outputDirectory;

    /**
     * Default extension of output files.
     * 
     * @parameter default-value=xml
     * @required
     */
    private String outputExtension;

    /**
     * Default extension of input files.
     * 
     * @parameter default-value=cmrscs
     * @required
     */
    private String inputExtension;

    private final FileLister fileLister = new FileListerImpl(new FileMatcherImpl());

    private final CmrscsReader cmrscsReader = new CmrscsReaderImpl();

    private FileUtil fileUtil = new FileUtilImpl();

    private final TextFileWriter resultWriter = new TextFileWriterImpl(fileUtil);

    private final CmrscsResultToXMLConverter resultToXMLConverter = new CmrscsResultToXMLConverterImpl();

    public void execute() throws MojoExecutionException, MojoFailureException {
        List<File> files = fileLister.list(sourceDirectory, getInputFiles(), Arrays.<String> asList());
        if (files.isEmpty()) {
            getLog().warn("No files to process in " + sourceDirectory);
        }

        for (File file : files) {
            try {
                String inputFileName = file.getCanonicalPath();
                String outputFileName = fileUtil.changeExtension(file.getName(), outputExtension);
                String xml = resultToXMLConverter.convert(cmrscsReader.read(inputFileName));
                resultWriter.write(fileUtil.createPath(outputDirectory, outputFileName), xml);
            } catch (Exception e) {
                getLog().warn("Failed to convert file: " + file, e);
            }
        }
    }

    private List<String> getInputFiles() {
        return Arrays.asList("(.)*\\." + inputExtension);
    }

}
