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
package org.dwfa.ace.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dwfa.tapi.TerminologyException;

/**
 * Generic file writer that allows enables subclassing to create typed writers
 * that write
 * out objects each on a new line of a file.
 * Subclasses may override the <code>serialize</code> method to customise the
 * representation
 * of the objects in the file, the default is the objects <code>toString</code>
 * method.
 *
 * @author Dion
 *
 * @param <T>
 */
public class GenericFileWriter<T> {

    /**
     * The file to be written to
     */
    private File outputFile;
    
    /**
     * Buffered file writer.
     */
    private BufferedWriter writer;

    /**
     * New line chars defaults to line.separator.
     */
    protected String newLineChars = System.getProperty("line.separator");

    /**
     * Default column delimiter.
     */
    protected String COLUMN_DELIMITER = "\t";

    /**
     * Opens the specified file for writing.
     *
     * @param outputFile file to write to
     * @param append if true the file will be appended to,
     *            if false the file will be overwritten
     * @throws IOException
     */
    public void open(File outputFile, boolean append) throws IOException {
        this.outputFile = outputFile;
        writer = new BufferedWriter(new FileWriter(outputFile, append));
    }

    /**
     * Writes the specified list of objects to the file
     *
     * @param objectList
     * @throws IOException
     * @throws TerminologyException
     */
    public void write(List<T> objectList) throws IOException, TerminologyException {
        for (T object : objectList) {
            write(object);
        }
    }

    /**
     * Writes the specified object to the file
     *
     * @param object
     * @throws IOException
     * @throws TerminologyException
     */
    public void write(T object) throws IOException, TerminologyException {
        writer.append(serialize(object));
        writer.append(newLineChars);
    }

    /**
     * Format the object for output in the file - not that the representation
     * cannot contain newline characters.
     *
     * @param object
     * @return the object formatted for output in the file
     * @throws IOException
     * @throws TerminologyException
     */
    protected String serialize(T object) throws IOException, TerminologyException {
        return object.toString();
    }

    /**
     * Closes the writer once writing is complete
     *
     * @throws IOException
     */
    public void close() throws IOException {
        writer.flush();
        writer.close();
        writer = null;
    }

    /**
     * Write the header row.
     *
     * @param header String
     * @throws IOException on write error.
     */
    protected void writeHeader(String header) throws IOException {
        writer.append(header);
        writer.append(newLineChars);
    }
    
    public void abort() throws IOException {        
        close();
        outputFile.delete();
    }
}
