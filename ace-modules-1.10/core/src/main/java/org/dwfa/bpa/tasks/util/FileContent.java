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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;

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
     */
    public FileContent(File file) {
        try {
            filename = file.getName();

            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            int nextByte = fis.read();
            while (nextByte != -1) {
                baos.write(nextByte);
                nextByte = fis.read();
            }
            // set content
            contents = baos.toByteArray();

            baos.close();
            fis.close();

        } catch (Exception e) {
            // if an error occurs, empty the file contents
            contents = new byte[0];
        }
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
