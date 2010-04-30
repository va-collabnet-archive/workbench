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
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.ihtsdo.mojo.maven.I_ReadAndTransform;
import org.dwfa.util.text.StringToWord;

public abstract class ProcessWords extends AbstractExport {

    private String[] columns;

    protected I_ReadAndTransform[] transformers;

    private Set<String> words = new TreeSet<String>();
    private HashMap<String, Integer> counts = new HashMap<String, Integer>();

    public void writeColumns(Writer w) throws IOException {
        w.write("WORD");
        w.write(getOutputColumnDelimiter());
        w.write("COUNT");
        w.write(WINDOWS_LINE_TERMINATOR);

    }

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
            String stringField = transformers[i].getLastTransform();
            StringTokenizer st = new StringTokenizer(stringField, StringToWord.WORD_DELIMITERS);
            // Split your words.
            while (st.hasMoreTokens()) {
                String t = st.nextToken();
                processToken(t);
            }
        }
    }

    protected abstract void processToken(String t) throws IOException;

    protected void addWord(String t) {
        words.add(t);
        if (counts.containsKey(t)) {
            counts.put(t, (Integer) counts.get(t) + 1);
        } else {
            counts.put(t, 1);
        }
    }

    protected void prepareForClose() throws IOException {
        for (Object word : words) {
            w.write(word.toString());
            w.write(getOutputColumnDelimiter());
            w.write(counts.get(word).toString());
            w.write(WINDOWS_LINE_TERMINATOR);
        }
    }

}
