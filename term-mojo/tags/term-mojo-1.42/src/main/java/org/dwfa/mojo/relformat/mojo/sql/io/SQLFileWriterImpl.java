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
package org.dwfa.mojo.relformat.mojo.sql.io;

import org.dwfa.mojo.relformat.mojo.sql.FileNameExtractor;
import org.dwfa.mojo.relformat.mojo.sql.converter.LineToSQLConverter;
import org.dwfa.mojo.relformat.mojo.sql.io.util.Directory;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;
import org.dwfa.mojo.relformat.mojo.sql.parser.Table;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public final class SQLFileWriterImpl implements SQLFileWriter {

    private final FileNameExtractor fileNameExtractor;
    private final FileUtil fileUtil;

    public SQLFileWriterImpl(final FileNameExtractor fileNameExtractor,
            final FileUtil fileUtil) {
        this.fileNameExtractor = fileNameExtractor;
        this.fileUtil = fileUtil;
    }

    public void writer(final File file, final Table table,
            final String outputDirectory,
            final LineToSQLConverter lineToSQLConverter) {
        BufferedReader reader = null;
        PrintWriter writer = null;

        try {
            fileUtil.createDirectoriesIfNeeded(new Directory(outputDirectory));
            reader = openReader(file);
            writer = openWriter(table, file, outputDirectory);

            String line = reader.readLine();//skip the header line.

            while ((line = reader.readLine()) != null) {
                writer.println(lineToSQLConverter.convert(table, line));
            }
        } catch (Exception e) {
            throw new SQLFileWriterException(e);
        } finally {
            close(reader, writer);
        }
    }

    private PrintWriter openWriter(final Table table, final File file,
            final String outputDirectory) throws FileNotFoundException {
        String fileName = resolveFileName(table, file, outputDirectory);
        return new PrintWriter(fileName);
    }

    private BufferedReader openReader(final File file)
            throws FileNotFoundException {
        return new BufferedReader(new FileReader(file));
    }

    private String resolveFileName(final Table table, final File file,
            final String outputDirectory) {
        return fileUtil.createPath(outputDirectory, fileNameExtractor
            .extractFileName(table, file));
    }

    private void close(final BufferedReader reader, final PrintWriter writer) {
        fileUtil.closeSilently(reader);
        fileUtil.closeSilently(writer);
    }
}
