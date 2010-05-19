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
import java.util.HashSet;
import java.util.StringTokenizer;

import org.dwfa.util.text.StringToWord;
import org.ihtsdo.mojo.maven.I_ReadAndTransform;

public class LowerCaseWordIndex extends AbstractExport {

    private String descIdColumn;
    private I_ReadAndTransform descIdTransformer;
    private String conceptIdColumn;
    private I_ReadAndTransform conceptIdTransformer;
    private String[] textColumns;

    protected I_ReadAndTransform[] transformers;

    private int maxLength = 0;

    public void writeColumns(Writer w) throws IOException {
        w.write(descIdColumn);
        w.write(getOutputColumnDelimiter());
        w.write(conceptIdColumn);
        for (String column : textColumns) {
            w.write(getOutputColumnDelimiter());
            w.write(column);

        }
        w.write(WINDOWS_LINE_TERMINATOR);

    }

    public void addTransformToSubclass(I_ReadAndTransform t) {
        if (transformers == null) {
            transformers = new I_ReadAndTransform[textColumns.length];
        }

        if (conceptIdColumn.equals(t.getName())) {
            conceptIdTransformer = t;
        } else if (descIdColumn.equals(t.getName())) {
            descIdTransformer = t;
        } else
            for (int i = 0; i < textColumns.length; i++) {
                if (textColumns[i].equals(t.getName())) {
                    transformers[i] = t;
                    return;
                }
            }
    }

    public void writeRec() throws IOException {

        HashSet<String> words = new HashSet<String>();
        for (int i = 0; i < transformers.length; i++) {
            String stringField = transformers[i].getLastTransform();
            StringTokenizer st = new StringTokenizer(stringField, StringToWord.WORD_DELIMITERS);
            // Split your words.
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                maxLength = Math.max(maxLength, t.length());
                words.add(t.toLowerCase());
            }
        }

        for (Object word : words) {
            w.append(descIdTransformer.getLastTransform());
            w.append(getOutputColumnDelimiter());
            w.append(conceptIdTransformer.getLastTransform());
            w.append(getOutputColumnDelimiter());
            w.append(word.toString());
            w.append(WINDOWS_LINE_TERMINATOR);
        }

    }

    protected void prepareForClose() throws IOException {
        System.out.println("Max word length: " + maxLength);
    }

}
