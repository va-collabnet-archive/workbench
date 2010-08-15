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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.swing.SwingWorker;

public class SearchStringWorker extends SwingWorker<I_UpdateProgress> implements I_TrackContinuation {
    boolean continueWork = true;

    SearchPanel searchPanel;

    StopActionListener stopListener = new StopActionListener();

    CountDownLatch completeLatch;

    private String patternString;

    private int conceptCount;

    private Collection<I_DescriptionVersioned> regexMatches;
    private Collection<LuceneMatch> luceneMatches;

    private DescriptionsFromCollectionTableModel model;

    I_ConfigAceFrame config;

    private boolean lucene;

    private class MatchUpdator implements ActionListener {
        Timer updateTimer;

        public MatchUpdator() {
            super();
            updateTimer = new Timer(1000, this);
            updateTimer.start();
        }

        public void actionPerformed(ActionEvent e) {
            if (continueWork == false) {
                updateTimer.stop();
            }
            updateMatches();
        }

    }

    private class StopActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            continueWork = false;
            AceLog.getAppLog().info("Search canceled by user");
            searchPanel.setProgressInfo("cancelled by user");
            searchPanel.setProgressIndeterminate(false);
            searchPanel.setProgressValue(0);
            if (completeLatch != null) {
                while (completeLatch.getCount() > 0) {
                    completeLatch.countDown();
                }
            }
        }

    }

    private class RegexProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;

        boolean firstUpdate = true;

        public RegexProgressUpdator() {
            super();
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.dwfa.ace.search.I_UpdateProgress#actionPerformed(java.awt.event
         * .ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (continueWork) {
                if (firstUpdate) {
                    searchPanel.setProgressInfo("   Starting regex search   ");
                    if (conceptCount != Integer.MAX_VALUE) {
                        searchPanel.setProgressIndeterminate(false);
                        searchPanel.setProgressMaximum(conceptCount);
                        firstUpdate = false;
                    }
                }
                if (completeLatch != null) {
                    searchPanel.setProgressValue((int) (searchPanel.getProgressMaximum() - completeLatch.getCount()));
                }
                if (firstUpdate != true) {
                    updateProgress();
                    if (searchPanel.getProgressValue() == searchPanel.getProgressMaximum()) {
                        normalCompletion();
                    }
                }
            } else {
                updateProgress();
                updateTimer.stop();
            }
        }

        private void updateProgress() {
            String max = "" + searchPanel.getProgressMaximum();
            if (searchPanel.getProgressMaximum() == Integer.MAX_VALUE) {
                max = "unknown";
            }
            searchPanel.setProgressInfo("   " + regexMatches.size() + " descriptions out of " + max + " concepts   ");
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
                searchPanel.setProgressIndeterminate(false);
                searchPanel.setProgressMaximum(conceptCount);
                firstUpdate = false;
            }
            searchPanel.setProgressValue(0);
        }

    }

    public class LuceneProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;

        boolean firstUpdate = true;

        private Integer hits = null;

        public LuceneProgressUpdator() {
            super();
            updateTimer = new Timer(1000, this);
            updateTimer.start();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.dwfa.ace.search.I_UpdateProgress#actionPerformed(java.awt.event
         * .ActionEvent)
         */
        public void actionPerformed(ActionEvent e) {
            if (continueWork) {
                if (firstUpdate) {
                    if (hits == null) {
                        searchPanel.setProgressIndeterminate(true);
                    }
                    searchPanel.setProgressMaximum(conceptCount);
                    firstUpdate = false;
                }
                if (completeLatch != null) {
                    if (hits != null) {
                        searchPanel.setProgressIndeterminate(false);
                    }
                    searchPanel.setProgressMaximum(conceptCount);
                    searchPanel.setProgressValue((int) (searchPanel.getProgressMaximum() - completeLatch.getCount()));
                } else {
                    AceLog.getAppLog().info("completeLatch is null");
                }
                searchPanel.setProgressInfo(" " + luceneMatches.size() + " matches. ");
                if (hits != null && completeLatch.getCount() == 0) {
                    normalCompletion();
                }
            } else {
                updateTimer.stop();
            }
        }

        public void setIndeterminate(boolean value) {
            searchPanel.setProgressIndeterminate(value);
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
                searchPanel.setProgressIndeterminate(false);
                searchPanel.setProgressMaximum(conceptCount);
                firstUpdate = false;
            }
            searchPanel.setProgressValue(0);
            searchPanel.setProgressInfo(" " + luceneMatches.size() + " matches. Search complete. ");
        }

        public void setProgressInfo(String info) {
            searchPanel.setProgressInfo(info);
        }

        public void setHits(int hits) {
            this.hits = hits;
            conceptCount = hits;
            searchPanel.setProgressMaximum(hits);
        }

        public boolean continueWork() {
            return continueWork;
        }

    }

    public SearchStringWorker(SearchPanel searchPanel, DescriptionsFromCollectionTableModel model,
            String patternString, I_ConfigAceFrame config, boolean lucene) {
        super();
        this.config = new FrameConfigSnapshot(config);
        this.model = model;
        this.searchPanel = searchPanel;
        this.searchPanel.addStopActionListener(stopListener);
        this.searchPanel.setProgressInfo("   Searching lucene for " + patternString + "   ");
        this.searchPanel.setProgressIndeterminate(true);
        this.patternString = patternString;
        this.lucene = lucene;

    }

    public void updateMatches() {
        if (luceneMatches != null) {
            this.model.setLuceneMatches(luceneMatches);
        } else {
            this.model.setDescriptions(regexMatches);
        }

    }

    @Override
    protected I_UpdateProgress construct() throws Exception {
        if (patternString.length() < 2) {
            continueWork = false;
            throw new Exception("Search string to short: " + patternString);
        }
        I_UpdateProgress updater;
        if (lucene) {
            luceneMatches = Collections.synchronizedCollection(new TreeSet<LuceneMatch>());
            conceptCount = Integer.MAX_VALUE;
            AceLog.getAppLog().info("Desc count 2: " + conceptCount);
            updater = new LuceneProgressUpdator();
            completeLatch = new CountDownLatch(1);
            new MatchUpdator();
            completeLatch = ((I_Search) Terms.get()).searchLucene(this, patternString, luceneMatches, completeLatch,
                searchPanel.getExtraCriterion(), config, (LuceneProgressUpdator) updater);
        } else {
            regexMatches = new ConcurrentSkipListSet<I_DescriptionVersioned>(
                new DescriptionComparator());
            updater = new RegexProgressUpdator();
            try {
                completeLatch = new CountDownLatch(0);
                Pattern p = Pattern.compile(patternString);
                conceptCount = Integer.MAX_VALUE;
                conceptCount = Terms.get().getConceptCount();
                AceLog.getAppLog().info("Desc count 3: " + conceptCount);
                if (conceptCount > 0) {
                    completeLatch = new CountDownLatch(conceptCount);
                }
                new MatchUpdator();
                ((I_Search) Terms.get()).searchRegex(this, p, regexMatches, completeLatch, searchPanel.getExtraCriterion(),
                    config);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
                while (completeLatch.getCount() > 0) {
                    completeLatch.countDown();
                }
            }
        }
        completeLatch.await();
        updater.actionPerformed(null);
        return updater;
    }

    protected void finished() {
        try {
            if (continueWork) {
                continueWork = false;
                get();
            }
            updateMatches();
        } catch (InterruptedException e) {
            AceLog.getAppLog().alertAndLogException(e);
        } catch (Throwable e) {
            if (ACE.editMode) {
                AceLog.getAppLog().alertAndLogException(e);
            } else {
                AceLog.getAppLog().log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
        searchPanel.removeStopActionListener(stopListener);
        searchPanel.setShowProgress(false);
        searchPanel.setProgressValue(0);
    }

    public boolean continueWork() {
        return continueWork;
    }
}
