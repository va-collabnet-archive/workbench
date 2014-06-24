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
package org.ihtsdo.mojo.maven.transform;

import java.io.IOException;
import java.io.Writer;

import org.ihtsdo.mojo.maven.I_ReadAndTransform;

public class SimpleExport extends AbstractExport {

    protected String[] columns;

    protected I_ReadAndTransform[] transformers;

    public void addTransformToSubclass(I_ReadAndTransform t) {
        if (transformers == null) {
            transformers = new I_ReadAndTransform[columns.length];
        }

        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(t.getName())) {
                transformers[i] = t;
                return;
            }
        }
    }

    public void writeRec() throws IOException {
        for (int i = 0; i < transformers.length; i++) {
            if (i != 0) {
                w.append(getOutputColumnDelimiter());
            }
            w.append(transformers[i].getLastTransform());
        }
        w.append(WINDOWS_LINE_TERMINATOR);
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public void writeColumns(Writer w) throws IOException {
        for (int i = 0; i < columns.length; i++) {
            if (i != 0) {
                w.append(getOutputColumnDelimiter());
            }
            w.append(columns[i]);
        }
        w.append(WINDOWS_LINE_TERMINATOR);
    }

    public void close() throws IOException {
        w.close();
    }

    protected void prepareForClose() {
        // Nothing to do
    }

}
