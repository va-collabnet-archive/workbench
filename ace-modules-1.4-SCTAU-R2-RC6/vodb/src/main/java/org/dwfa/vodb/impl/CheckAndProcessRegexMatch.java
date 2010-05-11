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
/**
 *
 */
package org.dwfa.vodb.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.regex.Pattern;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;

public class CheckAndProcessRegexMatch implements Runnable {
    Pattern p;

    Collection<I_DescriptionVersioned> matches;

    I_DescriptionVersioned descV;

    List<I_TestSearchResults> checkList;

    I_ConfigAceFrame config;

    private CountDownLatch descLatch;

    Semaphore checkSemaphore;

    public CheckAndProcessRegexMatch(CountDownLatch descLatch, Semaphore checkSemaphore, Pattern p,
            Collection<I_DescriptionVersioned> matches, I_DescriptionVersioned descV,
            List<I_TestSearchResults> checkList, I_ConfigAceFrame config) {
        super();
        this.p = p;
        this.matches = matches;
        this.descV = descV;
        this.checkList = checkList;
        this.config = config;
        this.descLatch = descLatch;
        this.checkSemaphore = checkSemaphore;
    }

    public void run() {
        if ((p == null) || (descV.matches(p))) {
            if (checkList == null || checkList.size() == 0) {
                matches.add(descV);
            } else {
                try {
                    boolean failed = false;
                    for (I_TestSearchResults test : checkList) {
                        if (test.test(descV, config) == false) {
                            failed = true;
                            break;
                        }
                    }

                    if (failed == false) {
                        matches.add(descV);
                    }
                } catch (TaskFailedException e) {
                    if (ACE.editMode) {
                        AceLog.getAppLog().alertAndLogException(e);
                    } else {
                        AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
                    }
                } finally {
                    this.descLatch.countDown();
                    checkSemaphore.release();
                }
            }
        }
    }

}
