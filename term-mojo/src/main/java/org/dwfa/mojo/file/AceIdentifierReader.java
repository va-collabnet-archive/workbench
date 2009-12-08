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
package org.dwfa.mojo.file;

import java.io.File;
import java.util.logging.Logger;

import org.dwfa.ace.file.IterableFileReader;
import org.dwfa.tapi.TerminologyRuntimeException;

/**
 * Read ace identifier file rows into AceIdentifierRow
 * 
 * @author Ean Dungey
 */
public class AceIdentifierReader extends IterableFileReader<AceIdentifierRow> {
    private AceIdentifierReaderData data = new AceIdentifierReaderData(
        Logger.getLogger(AceIdentifierReader.class.getName()));

    /**
     * Create the reader for the file.
     * 
     * @param file File to read.
     */
    public AceIdentifierReader(File file) {
        setSourceFile(file);
    }

    /**
     * @see org.dwfa.ace.file.IterableFileReader#processLine(java.lang.String)
     * @throws TerminologyRuntimeException if the row is invalid.
     * @return AceIdentifierRow object.
     */
    @Override
    protected AceIdentifierRow processLine(String line) {
        AceIdentifierRow aceIdentifierRow;

        try {
            aceIdentifierRow = new AceIdentifierRow(line);
        } catch (IndexOutOfBoundsException ex) {
            data.logger.info("Invalid file format");
            throw new TerminologyRuntimeException("Invalid file format");
        } catch (Exception ex) {
            data.logger.info("Cannot process line:" + ex);
            throw new TerminologyRuntimeException(ex);
        }

        return aceIdentifierRow;
    }
}
