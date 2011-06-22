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
package org.dwfa.ace.search.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.I_Search;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.swing.SwingWorker;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel;

public class SearchWfHxWorker extends SwingWorker<I_UpdateProgress> implements I_TrackContinuation {
    boolean continueWork = true;

    WorkflowHistorySearchPanel wfSearchPanel;

    StopActionListener stopListener = new StopActionListener();

    private Collection<LuceneMatch> luceneMatches;
    CountDownLatch completeLatch;

    private int matchedWorkflowCount;


    //CountDownLatch completeLatch;
    private int searchSize;

    private boolean wfInProgress = true;
    private boolean completedWF = false;

    private WorkflowHistoryTableModel model;

    I_ConfigAceFrame config;

    private class MatchUpdator implements ActionListener {
        Timer updateTimer;

        public MatchUpdator() {
            super();
            // TODO: Once have full history, increase the timer from 100 milliseconds
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }

        public void actionPerformed(ActionEvent e) {
            if (continueWork == false) {
                updateTimer.stop();
            }
            //updateMatches();
        }

    }

    private class StopActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            continueWork = false;
            AceLog.getAppLog().info("Search canceled by user");
            wfSearchPanel.setProgressInfo("cancelled by user");
            wfSearchPanel.setProgressIndeterminate(false);
            wfSearchPanel.setProgressValue(0);
          /*  if (completeLatch != null) {
                while (completeLatch.getCount() > 0) {
                    completeLatch.countDown();
                }
            }
            */
        }

    }
    
    
    public SearchWfHxWorker(WorkflowHistorySearchPanel wfSearchPanel, WorkflowHistoryTableModel model,
            I_ConfigAceFrame config, boolean wfIP, boolean wfCompleted) {
        super();
        this.config = new FrameConfigSnapshot(config);
        this.model = model;
        this.wfSearchPanel = wfSearchPanel;
        this.wfInProgress= wfIP;
        this.completedWF = wfCompleted;
        wfSearchPanel.addStopActionListener(stopListener);
    }

  
    public void updateMatches() {
 		this.model.setWfHxBeans(luceneMatches);
    }

    private boolean hasMatches() {
    	return this.model.hasMatches();
    }
    @Override
    protected I_UpdateProgress construct() throws Exception 
    {
        luceneMatches = Collections.synchronizedCollection(new TreeSet<LuceneMatch>());
        matchedWorkflowCount = Integer.MAX_VALUE;
        I_UpdateProgress updater = new LuceneWfHxProgressUpdator();
        completeLatch = new CountDownLatch(1);
        new MatchUpdator();
        completeLatch = ((I_Search) Terms.get()).searchWfHx(this, luceneMatches, completeLatch,
        							wfSearchPanel.getExtraCriterion(), config, (LuceneWfHxProgressUpdator) updater,
        							this.wfInProgress, this.completedWF);
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
            	AceLog.getAppLog().log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }
        wfSearchPanel.removeStopActionListener(stopListener);
        wfSearchPanel.setShowProgress(false);
        wfSearchPanel.setProgressValue(0);
    }

    public class LuceneWfHxProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;

        boolean firstUpdate = true;

        private Integer hits = null;

        public LuceneWfHxProgressUpdator() {
            super();
            // TODO: Once have full history, increase the timer from 100 milliseconds
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }

       
        public void actionPerformed(ActionEvent e) {
            if (continueWork) {
                if (firstUpdate) {
                    if (hits == null) {
                        wfSearchPanel.setProgressIndeterminate(true);
                    }
                    wfSearchPanel.setProgressMaximum(matchedWorkflowCount);
                    firstUpdate = false;
                }
                
                if (completeLatch != null) {
                    if (hits != null) {
                        wfSearchPanel.setProgressIndeterminate(false);
                    }
                    wfSearchPanel.setProgressMaximum(matchedWorkflowCount);
                    wfSearchPanel.setProgressValue((int) (wfSearchPanel.getProgressMaximum() - completeLatch.getCount()));
                } else {
                    AceLog.getAppLog().info("completeLatch is null");
                }
                
                if (luceneMatches.size() == 0) {
                	wfSearchPanel.setProgressInfo("Starting search.");
                } else {
                	wfSearchPanel.setProgressInfo(" " + luceneMatches.size() + " matches. ");
                }

                if (hits != null && completeLatch.getCount() == 0) {
                    normalCompletion();
                }
            } else {
                updateTimer.stop();
            }
        }

        public void setIndeterminate(boolean value) {
            wfSearchPanel.setProgressIndeterminate(value);
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
                wfSearchPanel.setProgressIndeterminate(false);
                wfSearchPanel.setProgressMaximum(matchedWorkflowCount);
                firstUpdate = false;
            }
            wfSearchPanel.setProgressValue(0);
            
            if (luceneMatches.size() == 0) {
            	wfSearchPanel.setProgressInfo("The search did not find any matches.  Try different criterion");
            } else {
            	wfSearchPanel.setProgressInfo("Search completed having found " + luceneMatches.size() + " matches.  Processing results for display.");
            }
        }

        public void setProgressInfo(String info) {
            wfSearchPanel.setProgressInfo(info);
        }

        public void setHits(int hits) {
            this.hits = hits;
            matchedWorkflowCount = hits;
            wfSearchPanel.setProgressMaximum(hits);
        }

        public boolean continueWork() {
            return continueWork;
        }

    }

	@Override
	public boolean continueWork() {
        return continueWork;
    }
}

