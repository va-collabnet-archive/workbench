/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.helper.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

// TODO: Auto-generated Javadoc
/**
 * The Class JarCreator.
 */
public class JarCreator {
    
    /**
     * Recursive add to zip.
     *
     * @param output the output
     * @param parent the parent
     * @param prefix the prefix
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void recursiveAddToZip(JarOutputStream output, File parent, String prefix) throws IOException {
        if (parent == null) {
            return;
        }
        for (File child : parent.listFiles()) {
            if (child.isDirectory()) {
                recursiveAddToZip(output, child, prefix);
            } else {
                addToZip(prefix, child, output, null);
            }
        }
    }

    /**
     * Adds the to zip.
     *
     * @param prefix the prefix
     * @param f the f
     * @param output the output
     * @param comment the comment
     * @throws IOException signals that an I/O exception has occurred
     */
    public static void addToZip(String prefix, File f, JarOutputStream output, String comment) throws IOException {
        ZipEntry entry = new ZipEntry(prefix + FileIO.getRelativePath(f));
        if (f.exists()) {
            entry.setSize(f.length());
            entry.setTime(f.lastModified());
        } else {
            entry.setSize(0);
            entry.setTime(System.currentTimeMillis());
        }
        entry.setComment(comment);
        output.putNextEntry(entry);
        if (f.exists()) {
            FileInputStream fis = new FileInputStream(f);
            byte[] buf = new byte[10240];
            for (int i = 0;; i++) {
                int len = fis.read(buf);
                if (len < 0)
                    break;
                output.write(buf, 0, len);
            }
        }
        output.closeEntry();
    }

    /**
     * Adds the to zip.
     *
     * @param theClass the the class
     * @param output the output
     * @throws IOException signals that an I/O exception has occurred
     * @throws ClassNotFoundException the class not found exception
     */
    public static void addToZip(Class<?> theClass, JarOutputStream output) throws IOException, ClassNotFoundException {
        String classFileName = theClass.getName().replace('.', '/') + ".class";
        ZipEntry entry = new ZipEntry(classFileName);
        output.putNextEntry(entry);

        URL classUrl = theClass.getResource("/" + classFileName);

        InputStream classInputStream = classUrl.openStream();
        int size = classInputStream.available();
        byte[] data = new byte[size];
        classInputStream.read(data, 0, size);
        output.write(data, 0, size);
        output.closeEntry();
    }

}
