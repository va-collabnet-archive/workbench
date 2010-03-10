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
package org.ihtsdo.mojo.mojo.relformat.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.ihtsdo.mojo.mojo.relformat.exception.ExportDDLMojoException;

import com.thoughtworks.xstream.XStream;

public final class ReleaseConfigReaderImpl implements ReleaseConfigReader {

    private final XStream xStream;

    public ReleaseConfigReaderImpl() {
        xStream = new XStream();
    }

    public ReleaseConfig reader(final InputStream in) {
        xStream.processAnnotations(ReleaseConfig.class);
        ReleaseConfig config = (ReleaseConfig) xStream.fromXML(in);
        closeStream(in);

        return config;
    }

    public ReleaseConfig reader(final File file) {
        try {
            return reader(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new ExportDDLMojoException(e);
        }
    }

    private void closeStream(final InputStream in) {
        try {
            in.close();
        } catch (IOException e) {
            throw new ExportDDLMojoException(e);
        }
    }
}
