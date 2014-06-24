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
package org.dwfa.bpa.tasks.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.dwfa.util.io.FileIO;

/**
 * Class to store file name and content, to be stored as an attachment.
 */
public class FileContent implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String filename;
    private byte[] contents;

    /**
     * Create a new FileContent object.
     * 
     * @param file The file to read in.
     * @throws IOException
     */
    public FileContent(File file) throws IOException {
        filename = file.getName();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream is = new BufferedInputStream(new FileInputStream(file));
        FileIO.copyFile(is, baos, true);
        // set content
        contents = baos.toByteArray();
        baos.close();
    }

    /**
     * Returns the filename.
     * 
     * @return The filename.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Returns the content of the file as an array of bytes.
     * 
     * @return The content of the file as an array of bytes.
     */
    public byte[] getContents() {
        return contents;
    }
}
