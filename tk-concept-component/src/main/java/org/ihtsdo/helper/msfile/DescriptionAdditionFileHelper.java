/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
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

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author AKF
 */
public class DescriptionAdditionFileHelper {

    private static ArrayList<String> descFileList = null; //make correct types
    private static Lock initLock = new ReentrantLock();

    private static void lazyInit(File memberFile)
            throws IOException {
        initLock.lock();
        InputStreamReader isr =
                new InputStreamReader(new FileInputStream(memberFile), "UTF-8");
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

    public static ArrayList<String> getDescFileList(File descFile) throws IOException {
            lazyInit(descFile);
        return descFileList;
    }
}
