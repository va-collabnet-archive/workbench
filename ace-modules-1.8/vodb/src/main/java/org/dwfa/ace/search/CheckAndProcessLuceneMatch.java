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
package org.dwfa.ace.search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.apache.lucene.document.Document;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.SearchStringWorker.LuceneProgressUpdator;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.vodb.I_StoreDescriptions;
import org.dwfa.vodb.types.ThinDescVersioned;

import com.sleepycat.je.DatabaseException;

public class CheckAndProcessLuceneMatch implements Runnable {

    Collection<LuceneMatch> matches;

    List<I_TestSearchResults> checkList;

    I_ConfigAceFrame config;

    Document doc;

    private float score;

    private CountDownLatch hitLatch;

    private I_StoreDescriptions descStore;

    public CheckAndProcessLuceneMatch(CountDownLatch hitLatch, LuceneProgressUpdator updater, Document doc,
            float score, Collection<LuceneMatch> matches, List<I_TestSearchResults> checkList, I_ConfigAceFrame config,
            I_StoreDescriptions descStore) {
        super();
        this.doc = doc;
        this.score = score;
        this.matches = matches;
        this.checkList = checkList;
        this.config = config;
        this.hitLatch = hitLatch;
        this.descStore = descStore;
    }

    public void run() {
        if (hitLatch.getCount() > 0) {
            int nid = Integer.parseInt(doc.get("dnid"));
            int cnid = Integer.parseInt(doc.get("cnid"));
            try {
                ThinDescVersioned descV = (ThinDescVersioned) descStore.getDescription(nid, cnid);
                LuceneMatch match = new LuceneMatch(descV, score);
                if (checkList == null || checkList.size() == 0) {
                    matches.add(match);
                    if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                        AceLog.getAppLog().fine("processing match: " + descV + " new match size: " + matches.size());
                    }
                } else {
                    try {
                        boolean failed = false;
                        for (I_TestSearchResults test : checkList) {
                            if (test != null) {
                                if (test.test(descV, config) == false) {
                                    failed = true;
                                    break;
                                }
                            }
                        }

                        if (failed == false) {
                            matches.add(match);
                        }
                    } catch (TaskFailedException e) {
                        AceLog.getAppLog().alertAndLogException(e);
                    }
                }
            } catch (IOException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            } catch (DatabaseException e1) {
                AceLog.getAppLog().alertAndLogException(e1);
            }
            this.hitLatch.countDown();
            if (AceLog.getAppLog().isLoggable(Level.FINE)) {
                AceLog.getAppLog().fine("Hit latch: " + this.hitLatch.getCount());
            }
        }
    }
}
