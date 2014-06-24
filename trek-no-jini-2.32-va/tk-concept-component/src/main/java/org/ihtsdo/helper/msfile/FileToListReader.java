/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.msfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * Reads an file and converts into a list of strings with each element representing one line in the file.
 */
public class FileToListReader {
    private static ArrayList<String> fileList = null;
    private static Lock initLock = new ReentrantLock();
    private static final int BOM_SIZE = 4;

    /**
     * Imports each line of the
     * <code>file</code> into a list of
     * <code>Strings</code> with one element per file line. Checks if the file
     * has a BOM and reads accordingly.
     *
     * @param memberFile the OWL file to import
     * @throws IOException signals that an I/O exception has occurred
     */
    private static void lazyInit(File file)
            throws IOException {
        initLock.lock();
        byte bom[] = new byte[BOM_SIZE];
        String encoding;
        int unread;
        PushbackInputStream pushbackStream = new PushbackInputStream(new FileInputStream(file), BOM_SIZE);
        int n = pushbackStream.read(bom, 0, bom.length);
        // Read ahead four bytes and check for BOM marks.
        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            unread = n - 3;
        } else {
            unread = n;
        }
        // Unread bytes if necessary and skip BOM marks.
        if (unread > 0) {
            pushbackStream.unread(bom, (n - unread), unread);
        } else if (unread < -1) {
            pushbackStream.unread(bom, 0, 0);
        }

        InputStreamReader isr =
                new InputStreamReader(pushbackStream, "UTF-8");
        HashMap msFileSetMap = new HashMap<String, Set<String>>();
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        fileList = new ArrayList<String>();
        while (line != null) {
            fileList.add(line);
            line = reader.readLine();
        }
        initLock.unlock();
    }

    /**
     * Gets a list of strings representing the given
     * <code>file</code>.
     *
     * @param file the text file with one entry per line
     * @return the list of of strings where each element represents one line of
     * the description file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static ArrayList<String> getFileList(File file) throws IOException {
        lazyInit(file);
        return fileList;
    }
}
