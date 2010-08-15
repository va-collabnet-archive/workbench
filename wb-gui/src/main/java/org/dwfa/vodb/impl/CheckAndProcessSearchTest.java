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
package org.dwfa.vodb.impl;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;

public class CheckAndProcessSearchTest implements Runnable {

	I_RepresentIdSet matches;

    I_GetConceptData conceptToTest;

    List<I_TestSearchResults> checkList;

    I_ConfigAceFrame config;

    Semaphore checkSemaphore;

    CountDownLatch conceptLatch;

    public CheckAndProcessSearchTest(CountDownLatch conceptLatch, Semaphore checkSemaphore,
    		I_RepresentIdSet matches, I_GetConceptData conceptToTest, List<I_TestSearchResults> checkList,
            I_ConfigAceFrame config) {
        super();
        this.matches = matches;
        this.conceptToTest = conceptToTest;
        this.checkList = checkList;
        this.config = config;
        this.checkSemaphore = checkSemaphore;
        this.conceptLatch = conceptLatch;
    }

    public void run() {
        if (checkList == null || checkList.size() == 0) {
                matches.setMember(conceptToTest.getConceptNid());
        } else {
            try {
                boolean failed = false;
                for (I_TestSearchResults test : checkList) {
                    if (test.test(conceptToTest, config) == false) {
                        failed = true;
                        break;
                    }
                }

                if (failed == false) {
                    matches.setMember(conceptToTest.getConceptNid());
                }
            } catch (TaskFailedException e) {
                if (ACE.editMode) {
                    AceLog.getAppLog().alertAndLogException(e);
                } else {
                    AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
                }
            }
        }
        checkSemaphore.release();
        conceptLatch.countDown();
    }

}
