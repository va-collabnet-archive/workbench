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

import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.task.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// TODO:This has to be integration tested.
public final class WriterFactoryImpl implements WriterFactory {

    private static final String CONCEPT_ID_HEADER = "Concept ID";
    private static final String PREFERRED_TERM_HEADER = "PREFERRED_TERM";
    private static final String CONCEPTS_WITH_NO_DESCS_KEY = "_CONCEPTS_WITH_NO_DESCS_KEY_";
    private static final String NO_DESCRIPTIONS_FILE_NAME = "Concepts with no descriptions.txt";

    private final File outputDirectory;
    private final Logger logger;
    private final Map<String, ExportWriter> fileMap;
    private final I_TermFactory termFactory;
    private final RefsetUtil refsetUtil;
    private final String lineSepator;

    public WriterFactoryImpl(final File outputDirectory, final Logger logger, final I_TermFactory termFactory,
            final RefsetUtil refsetUtil) {
        this.termFactory = termFactory;
        this.refsetUtil = refsetUtil;
        this.outputDirectory = validate(outputDirectory);
        this.logger = logger;
        fileMap = new HashMap<String, ExportWriter>();
        lineSepator = System.getProperty("line.separator");
    }

    public DescriptionWriter createDescriptionFile(final String refsetName) throws Exception {
        if (!fileMap.containsKey(refsetName)) {
            DescriptionWriter writer = createDescriptionExportWriter(refsetName + ".refset.text");
            fileMap.put(refsetName, writer);
            addHeader(writer);
        }

        return (DescriptionWriter) fileMap.get(refsetName);
    }

    public NoDescriptionWriter createNoDescriptionFile() throws Exception {
        if (!fileMap.containsKey(CONCEPTS_WITH_NO_DESCS_KEY)) {
            NoDescriptionWriter writer = createNoDescriptionExportWriter(NO_DESCRIPTIONS_FILE_NAME);
            fileMap.put(CONCEPTS_WITH_NO_DESCS_KEY, writer);
        }

        return (NoDescriptionWriter) fileMap.get(CONCEPTS_WITH_NO_DESCS_KEY);
    }

    public void closeFiles() throws Exception {
        for (ExportWriter exportWriter : fileMap.values()) {
            exportWriter.close();
        }
    }

    private void addHeader(final ExportWriter writer) throws Exception {
        writer.append(CONCEPT_ID_HEADER);
        writer.append("\t");
        writer.append(PREFERRED_TERM_HEADER);
    }

    private DescriptionWriter createDescriptionExportWriter(final String fileName) throws IOException {
        File outputFile = prepareFile(fileName);
        return new DescriptionWriterImpl(new BufferedWriter(new FileWriter(outputFile)), termFactory, refsetUtil,
            lineSepator);
    }

    private NoDescriptionWriter createNoDescriptionExportWriter(final String fileName) throws IOException {
        File outputFile = prepareFile(fileName);
        return new NoDescriptionWriterImpl(new BufferedWriter(new FileWriter(outputFile)), lineSepator, logger);
    }

    private File prepareFile(final String fileName) throws IOException {
        File outputFile = new File(outputDirectory, fileName);
        if (outputFile.getParentFile().mkdirs()) {
            logger.logInfo("making directory - " + outputFile.getParentFile());
        }

        // create the file if it's not there.
        outputFile.createNewFile();
        return outputFile;
    }

    private File validate(final File outputDirectory) {
        if (outputDirectory == null) {
            throw new InvalidOutputDirectoryException("The output directory supplied is null.");
        }

        return outputDirectory;
    }
}
