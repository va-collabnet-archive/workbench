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
package org.ihtsdo.helper.msfile;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// TODO: Auto-generated Javadoc
/**
 * The Class DescriptionAdditionFileHelper.
 *
 * @author AKF
 */
public class DescriptionAdditionFileHelper {

    /** The desc file list. */
    private static ArrayList<String> descFileList = null;
    
    /** The init lock. */
    private static Lock initLock = new ReentrantLock();
    
    /** The Constant BOM_SIZE. */
    private static final int BOM_SIZE = 4;

    /**
     * Lazy init.
     *
     * @param memberFile the member file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void lazyInit(File memberFile)
            throws IOException {
        initLock.lock();
        byte bom[] = new byte[BOM_SIZE];
        String encoding;
        int unread;
        PushbackInputStream pushbackStream = new PushbackInputStream(new FileInputStream(memberFile), BOM_SIZE);
        int n = pushbackStream.read(bom, 0, bom.length);
        // Read ahead four bytes and check for BOM marks.
        if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
            unread = n - 3;
        }else{
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
        descFileList = new ArrayList<String>();
        while (line != null) {
            descFileList.add(line);
            line = reader.readLine();
        }
        initLock.unlock();
    }

    /**
     * Gets the desc file list.
     *
     * @param descFile the desc file
     * @return the desc file list
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static ArrayList<String> getDescFileList(File descFile) throws IOException {
            lazyInit(descFile);
        return descFileList;
    }
}
