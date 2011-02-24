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
import java.util.SortedSet;
import java.util.logging.Level;

import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.ihtsdo.ace.table.WorkflowHistoryTableModel;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetSearcher;

public class SearchWfHistoryStringWorker extends SwingWorker<I_UpdateProgress> implements I_TrackContinuation {
    boolean continueWork = true;

    WorkflowHistorySearchPanel wfSearchPanel;

    private WorkflowHistoryRefsetSearcher searcher = null;
    
    StopActionListener stopListener = new StopActionListener();

    private SortedSet<WorkflowHistoryJavaBean>  wfHistorySearchResults = null;
    
    //CountDownLatch completeLatch;

    private int searchSize;

    private boolean wfInProgress = true;
    private boolean completedWF = false;
    private boolean searchPreviousReleases = false;
    private String timestampBefore = null;
    private String timestampAfter = null;

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
    
    
    public SearchWfHistoryStringWorker(WorkflowHistorySearchPanel wfSearchPanel, WorkflowHistoryTableModel model,
            I_ConfigAceFrame config, boolean wfIP, boolean wfCompleted, boolean wfSearchPreviousReleases, String timestampBefore, String timestampAfter) {
        super();
        this.config = new FrameConfigSnapshot(config);
        this.model = model;
        this.wfSearchPanel = wfSearchPanel;
        this.wfInProgress= wfIP;
        this.completedWF = wfCompleted;
        this.searchPreviousReleases = wfSearchPreviousReleases;
        this.timestampBefore = timestampBefore;
        this.timestampAfter = timestampAfter;

		searcher = new WorkflowHistoryRefsetSearcher();
    }

  
    public void updateMatches() {
 		this.model.setWfHxBeans(wfHistorySearchResults);
    }

    private boolean hasMatches() {
    	return this.model.hasMatches();
    }
    @Override
    protected I_UpdateProgress construct() throws Exception 
    {
    	I_UpdateProgress updater = new WfHxProgressUpdator();
    	searcher = new WorkflowHistoryRefsetSearcher();

        new MatchUpdator();
        
        wfHistorySearchResults = searcher.searchForWFHistory(wfSearchPanel.getExtraCriterion(), wfInProgress, completedWF, searchPreviousReleases, timestampBefore, timestampAfter);

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

    public class WfHxProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;

        boolean firstUpdate = true;

        public WfHxProgressUpdator() {
            super();
            // TODO: Once have full history, increase the timer from 100 milliseconds
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }

       
        public void actionPerformed(ActionEvent e) {
            if (continueWork) 
            {
                if (firstUpdate) 
                {
                	int searchSize = searcher.getRefsetMembersCount();
                	wfSearchPanel.setProgressInfo("   Starting Workflow History search   ");

                	if (searchSize > 0) {
                    	wfSearchPanel.setProgressIndeterminate(true);
                        wfSearchPanel.setProgressMaximum(searchSize);
                        firstUpdate = false;
                    }
                }

                if (firstUpdate != true) {
                    updateProgress();
                }
            } else {
                updateProgress();
                updateTimer.stop();

                if (hasMatches())
                {
                	wfSearchPanel.setProgressIndeterminate(false);
                	model.clearResults();
                    normalCompletion();
                }
            }
        }

        private void updateProgress() {
            if (wfHistorySearchResults != null)
                wfSearchPanel.setProgressInfo(" Searching " + searcher.getRefsetMembersCount()+ " records.");
        }

        public void normalCompletion() {
            updateTimer.stop();
    
            if (firstUpdate) 
            {
                wfSearchPanel.setProgressIndeterminate(false);
                wfSearchPanel.setProgressMaximum(searchSize);
                firstUpdate = false;
            }
            
            wfSearchPanel.setProgressValue(0);
            wfSearchPanel.setProgressInfo(" " + wfHistorySearchResults.size() + " matches. Search complete. ");
        }
    }

	@Override
	public boolean continueWork() {
        return continueWork;
    }
}

