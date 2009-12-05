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
package org.dwfa.mojo.relformat.xml;

import org.apache.maven.plugin.logging.Log;
import org.dwfa.mojo.relformat.exception.ExportDDLMojoException;
import org.dwfa.mojo.relformat.mojo.sql.io.util.FileUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public final class ConfigFileWriterImpl implements ConfigFileWriter {

    private final Log logger;
    private final FileUtil fileUtil;

    public ConfigFileWriterImpl(final Log logger, final FileUtil fileUtil) {
        this.logger = logger;
        this.fileUtil = fileUtil;
    }

    public void write(final String fileName, final String content) {
        OutputStream out = null;
        try {
            out = openStream(fileName);
            out.write(content.getBytes());
        } catch (Exception e) {
            throw new ExportDDLMojoException(e);
        } finally {
            closeStream(fileName, out);
        }
    }

    private OutputStream openStream(final String fileName) {
        File targetFile = new File(fileName);
        fileUtil.createDirectoriesIfNeeded(targetFile);

        try {
            return new BufferedOutputStream(new FileOutputStream(targetFile));
        } catch (FileNotFoundException e) {
            return logAndThrow("Could not open file:" + fileName, e);
        }
    }

    private void closeStream(final String fileName, final OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (Exception e) {
                logAndThrow("Could not close file: " + fileName, e);
            }
        }
    }

    private OutputStream logAndThrow(final String message, final Throwable cause) {
        logger.error(message, cause);
        throw new ExportDDLMojoException(message, cause);
    }
}
