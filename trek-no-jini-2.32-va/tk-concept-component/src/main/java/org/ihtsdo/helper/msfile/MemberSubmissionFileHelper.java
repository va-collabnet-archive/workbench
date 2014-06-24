/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.helper.msfile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The Class MemberSubmissionFileHelper takes a text file specifying concepts
 * and loads the concepts into an
 * <code>Set</code> with one element for each line of the file. The description
 * file should have one concept per line. Designed to help import concepts from
 * IHTSDO members into SNOMED core.
 */
public class MemberSubmissionFileHelper {

    private static Map<String, Set<String>> msFileSetMap = null;
    private static Lock initLock = new ReentrantLock();

    /**
     * Imports the
     * <code>memberFile</code> into a map where the member country generating
     * the submission is associated with the submission. Each submission is a
     * set of
     * <code>Strings</code> with one element per file line.
     *
     * @param member the member submitting the file
     * @param memberFile the submission file
     * @throws IOException signals that an I/O exception has occurred
     */
    private static void lazyInit(String member, File memberFile)
            throws IOException {
        initLock.lock();
        InputStreamReader isr =
                new InputStreamReader(new FileInputStream(memberFile), "UTF-8");
        HashMap msFileSetMap = new HashMap<String, Set<String>>();
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        Set<String> msFileSet = new LinkedHashSet<String>();
        while (line != null) {
            msFileSet.add(line);
            line = reader.readLine();
        }
        msFileSetMap.put(member, msFileSet);
        MemberSubmissionFileHelper.msFileSetMap = msFileSetMap;
        initLock.unlock();
    }

    /**
     * Gets a set of strings representing the
     * <code>memberSubmissionFile</code>.
     *
     * @param member a string representing member responsible for the submission
     * @param memberSubmissionFile the member submission text file with one
     * concept per line
     * @return the set of strings where each element represents on line of the
     * member submission file
     * @throws IOException signals that an I/O exception has occurred
     */
    public static Set<String> getMsFileSet(String member, File memberSubmissionFile) throws IOException {
        lazyInit(member, memberSubmissionFile);
        Set<String> line = msFileSetMap.get(member);
        return line;
    }
}
