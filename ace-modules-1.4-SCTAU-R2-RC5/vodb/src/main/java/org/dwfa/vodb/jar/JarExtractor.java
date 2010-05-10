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
package org.dwfa.vodb.jar;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.dwfa.ace.log.AceLog;

public class JarExtractor {
    public static void main(String[] args) {

        String pathSeperator = System.getProperty("path.separator");
        String path = System.getProperty("java.class.path");
        String[] parts = path.split(pathSeperator);
        /*
         * This assumes that the jar file to be extracted is the first
         * entry in the java.class.path. The extracted folder will be
         * named its own name with a " folder" extension, and will
         * be extracted into the same directory as the original jar
         * file resides in.
         */

        File destDir = new File(parts[0] + " folder");
        if (destDir.exists()) {
            for (int i = 0; destDir.exists(); i++) {
                destDir = new File(parts[0] + " folder " + i);
            }
        }
        destDir.mkdirs();
        try {
            JarFile jf = new JarFile(new File(parts[0]));
            for (Enumeration<JarEntry> e = jf.entries(); e.hasMoreElements();) {
                JarEntry je = e.nextElement();
                AceLog.getAppLog().info(
                    "Jar entry: " + je.getName() + " compressed: " + je.getCompressedSize() + " size: " + je.getSize()
                        + " time: " + new Date(je.getTime()) + " comment: " + je.getComment());
                java.io.File f = new java.io.File(destDir + java.io.File.separator + je.getName());

                if (je.isDirectory()) { // if its a directory, create it
                    f.mkdir();
                    continue;
                }
                java.io.InputStream is = jf.getInputStream(je); // get the input
                // stream
                f.getParentFile().mkdirs();
                java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                byte[] buffer = new byte[102400];
                while (is.available() > 0) { // write contents of 'is' to
                    // 'fos'
                    int bytesRead = is.read(buffer);
                    fos.write(buffer, 0, bytesRead);
                }
                fos.close();
                is.close();
                f.setLastModified(je.getTime());
            }
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }

        JOptionPane.showMessageDialog(new JFrame(), "Eggs aren't supposed to be green.");
        AceLog.getAppLog().info("[d] jar extractor exit...");
        System.exit(0);
    }
}
