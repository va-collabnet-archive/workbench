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
package org.dwfa.mojo.file.ace;

import java.io.File;
import java.io.IOException;

import org.dwfa.ace.file.GenericFileWriter;
import org.dwfa.tapi.TerminologyException;

/**
 * File writer for RF2 identifier rows.
 *
 * @author Ean Dungey
 */
public class AceIdentifierWriter extends GenericFileWriter<AceIdentifierRow> {

    public AceIdentifierWriter(File conceptFile) throws IOException {
        newLineChars = "\r\n";

        open(conceptFile, false);
    }

    /**
     * @see org.dwfa.ace.file.GenericFileWriter#serialize(java.lang.Object)
     */
    @Override
    protected String serialize(AceIdentifierRow conceptRow) throws IOException,
            TerminologyException {
        return conceptRow.toString();
    }
}
