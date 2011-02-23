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
            updateTimer = new Timer(1000, this);
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

    @Override
    protected I_UpdateProgress construct() throws Exception 
    {

    	I_UpdateProgress updater = new WfHxProgressUpdator();
    	searcher = new WorkflowHistoryRefsetSearcher();

    	int totalWfCount = searcher.getTotalMemberCount();

//        completeLatch = new CountDownLatch(totalWfCount);
        
        new MatchUpdator();
        
        
        wfHistorySearchResults = searcher.searchForWFHistory(wfSearchPanel.getExtraCriterion(), wfInProgress, completedWF, searchPreviousReleases, timestampBefore, timestampAfter);
//        while (completeLatch.getCount() > 0)
//        	completeLatch.countDown();
        
//        completeLatch.await();
        updater.actionPerformed(null);
            
        return updater;
        
    
/*	  
    	I_UpdateProgress updater = new WfHxProgressUpdator();
		wfHistorySearchResults = new TreeSet<WorkflowHistoryJavaBean>(WorkflowHistoryRefset.createWfHxJavaBeanComparer());
	
	    //completeLatch = new CountDownLatch(1);
	
	    new MatchUpdator();
	
	    //completeLatch = 
	        ((I_Search) Terms.get()).searchWfHx(this, wfHistorySearchResults, //completeLatch,
	                wfSearchPanel.getExtraCriterion(), config, (WfHxProgressUpdator) updater);
		
	    return updater;
*/
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
                	int searchSize = searcher.getTotalMemberCount();

                	wfSearchPanel.setProgressInfo("   Starting Workflow History search   ");
                    if (searchSize > 0) {
                    	wfSearchPanel.setProgressIndeterminate(false);
                        wfSearchPanel.setProgressMaximum(searchSize);
                        firstUpdate = false;
                    }
                }
//                if (completeLatch != null) {
//                	wfSearchPanel.setProgressValue((int) (wfSearchPanel.getProgressMaximum() - completeLatch.getCount()));
//                }
                if (firstUpdate != true) {
                    updateProgress();
                    if (wfSearchPanel.getProgressValue() == wfSearchPanel.getProgressMaximum()) {
                        normalCompletion();
                    }
                }
            } else {
                updateProgress();
                updateTimer.stop();
            }
        }

        private void updateProgress() {
            String max = "" + wfSearchPanel.getProgressMaximum();
            if (wfSearchPanel.getProgressMaximum() == Integer.MAX_VALUE) {
                max = "unknown";
            }
            
            if (wfHistorySearchResults != null)
                wfSearchPanel.setProgressInfo(" " + wfHistorySearchResults.size() + " matches. Search complete. ");
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
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
    
       /*

    public class WfHxProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;

        boolean firstUpdate = true;

        private Integer hits = null;

        public WfHxProgressUpdator() {
            super();
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }

        *
         * (non-Javadoc)
         * 
         * @see
         * org.dwfa.ace.search.I_UpdateProgress#actionPerformed(java.awt.event
         * .ActionEvent)
         *
        public void actionPerformed(ActionEvent e) {
            if (continueWork) 
            {
                if (firstUpdate) 
                {
                    if (hits == null) 
                    	wfSearchPanel.setProgressIndeterminate(true);
                    
                    wfSearchPanel.setProgressMaximum(searchSize);
                    firstUpdate = false;
                }
                *
                if (completeLatch != null) {
                    if (hits != null)
                    	wfSearchPanel.setProgressIndeterminate(false);
                 
                    wfSearchPanel.setProgressMaximum(searchSize);
                    wfSearchPanel.setProgressValue((int) (wfSearchPanel.getProgressMaximum() - completeLatch.getCount()));
                } else 
                    AceLog.getAppLog().info("completeLatch is null");                
                *
                wfSearchPanel.setProgressInfo(" " + wfHistorySearchResults.size() + " matches. ");
                
                if (hits != null )//&& completeLatch.getCount() == 0) 
                    normalCompletion();
            } else {
                updateTimer.stop();
            }
        }

        public void setIndeterminate(boolean value) {
        	wfSearchPanel.setProgressIndeterminate(value);
        }

               
        public void setProgressInfo(String info) {
        	wfSearchPanel.setProgressInfo(info);
        }

        public void setHits(int hits) {
            this.hits = hits;
            searchSize = hits;
            wfSearchPanel.setProgressMaximum(hits);
        }

        public boolean continueWork() {
            return continueWork;
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
                wfSearchPanel.setProgressIndeterminate(false);
                wfSearchPanel.setProgressMaximum(searchSize);
                firstUpdate = false;
            }
            wfSearchPanel.setProgressValue(0);
            wfSearchPanel.setProgressInfo(" " + wfHistorySearchResults.size() + " matches. Search complete. ");
        }

    }
*/

}

