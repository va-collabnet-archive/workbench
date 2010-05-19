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
package org.ihtsdo.mojo.mojo.memrefset.mojo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.ihtsdo.mojo.mojo.relformat.mojo.sql.io.util.FileUtil;

public final class TextFileWriterImpl implements TextFileWriter {

    private final FileUtil fileUtil;

    public TextFileWriterImpl(final FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public void write(final String fileName, final String text) {
        Writer writer = openFile(fileName);
        write(writer, text);
        closeFile(writer);
    }

    private Writer openFile(final String fileName) {
        try {
            fileUtil.createDirectoriesIfNeeded(fileName);
            return new BufferedWriter(new FileWriter(fileName));
        } catch (IOException e) {
            throw new CmrscsResultWriterException(e);
        }
    }

    private void write(final Writer writer, final String content) {
        try {
            writer.write(content);
        } catch (IOException e) {
            throw new CmrscsResultWriterException(e);
        }
    }

    private void closeFile(final Writer writer) {
        try {
            writer.close();
        } catch (IOException e) {
            throw new CmrscsResultWriterException(e);
        }
    }
}
