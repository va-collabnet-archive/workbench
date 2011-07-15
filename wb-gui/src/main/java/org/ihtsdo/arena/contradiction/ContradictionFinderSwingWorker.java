/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.contradiction;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_ShowActivity;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.list.TerminologyListModel;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.util.Stopwatch;
import org.ihtsdo.contradiction.ContradictionConceptProcessor;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */

public class ContradictionFinderSwingWorker 
	extends SwingWorker<Set<Integer>, Integer> {
    
	private TerminologyListModel conflicts;
    private final I_ShowActivity actvityPanel;
	private ViewCoordinate viewCoord;
	private ContradictionEditorFrame frame;
    ContradictionFinderStopActionListener stopListener = new ContradictionFinderStopActionListener();

    private int contradictionsFoundCount;
    
    private boolean continueWork;
	private CountDownLatch completeLatch;
	private ContradictionConceptProcessor ccp;
	private Locale locale;
	private double numberConceptsToProcess;

	/* ContradictionUpdator */
    private class ContradictionUpdator implements ActionListener {
        Timer updateTimer;
        
        public ContradictionUpdator() {
            super();
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }

        public void actionPerformed(ActionEvent e) {
            if (continueWork == false) {
                updateTimer.stop();
            }
//            updateMatches();
        }

    }

    /* ContradictionFinderStopActionListener */
	private class ContradictionFinderStopActionListener implements ActionListener {
	
		public void actionPerformed(ActionEvent e) {
	        continueWork = false;
	        AceLog.getAppLog().info("Search canceled by user");
            frame.setProgressInfo("Search canceled.  Ready to run again.");
	        frame.setProgressIndeterminate(false);
	        frame.setProgressValue(0);
	        frame.enableStopButton(false);
	        if (completeLatch != null) {
	            while (completeLatch.getCount() > 0) {
	                completeLatch.countDown();
	            }
	        }
	    }
	}

    /* LuceneWfHxProgressUpdator */
    public class ContradictionProgressUpdator implements I_UpdateProgress {
        Timer updateTimer;

        boolean firstUpdate = true;

        private Integer hits = null;

        public ContradictionProgressUpdator() {
            super();
            updateTimer = new Timer(100, this);
            updateTimer.start();
        }
       
        public void actionPerformed(ActionEvent e) {
            if (continueWork) {
            	// If running
                if (firstUpdate) {
                    if (hits == null) {
                        frame.setProgressIndeterminate(true);
                    }
                    frame.setProgressMaximum(contradictionsFoundCount);
                    firstUpdate = false;
                }
                
                if (completeLatch != null) {
                    if (hits != null) {
                        frame.setProgressIndeterminate(false);
                    }
                    frame.setProgressMaximum(contradictionsFoundCount);
                    frame.setProgressValue((int) (frame.getProgressMaximum() - completeLatch.getCount()));
                } else {
                    AceLog.getAppLog().info("completeLatch is null");
                }
                
                int numberFound = ccp.getNumberContradictionsFound().get();
                String percentageStr = new String(" with " + createPercentage() + " concepts processed");
                String progressStr;
                
                if (numberFound == 0) {
                	progressStr = "None found" + percentageStr;
                } else {
                	progressStr = numberFound + " found" + percentageStr;
                }
                
            	frame.setProgressInfo(progressStr);

                if (hits != null && completeLatch.getCount() == 0) {
                    normalCompletion();
                }
            } else {
                updateTimer.stop();
            }
        }

        private String createPercentage() {
            int numberProcessed = ccp.getNumberConceptsProcessed().get();								

            NumberFormat percentFormatter;
 
        	percentFormatter = NumberFormat.getPercentInstance(locale);
        	String percentOut = percentFormatter.format(new Double(numberProcessed / numberConceptsToProcess));
        	
        	return percentOut;
        }


		public void setIndeterminate(boolean value) {
            frame.setProgressIndeterminate(value);
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
                frame.setProgressIndeterminate(false);
                frame.setProgressMaximum(contradictionsFoundCount);
                firstUpdate = false;
            }
            frame.setProgressValue(0);
            
            int numberFound = ccp.getNumberContradictionsFound().get();
            if (numberFound == 0) {
            	frame.setProgressInfo("No Contradictions Detected");
            } else {
            	frame.setProgressInfo("Finished having detected" + numberFound + " Contradictions");
            }
        }

        public void setProgressInfo(String info) {
            frame.setProgressInfo(info);
        }

        public void setHits(int hits) {
            this.hits = hits;
            contradictionsFoundCount = hits;
            frame.setProgressMaximum(hits);
        }

        public boolean continueWork() {
            return continueWork;
        }

    }

    
    /*
     * ContradictionFinderSwingWorker Class Methods
     */
    public ContradictionFinderSwingWorker(ContradictionEditorFrame editorFrame, ViewCoordinate vc) {
        this.frame = editorFrame;
        this.conflicts =  (TerminologyListModel) frame.getBatchConceptList().getModel();
        this.viewCoord = vc;
        this.actvityPanel = Terms.get().newActivityPanel(true, null, "Identifying conflicts", true);

        //locale = frame.getActiveFrame().getLocale();
        locale = new Locale("en-us");
    }

    @Override
    protected Set<Integer> doInBackground() throws Exception {
    	
    	// Setup Listeners
        contradictionsFoundCount = Integer.MAX_VALUE;
        numberConceptsToProcess = Terms.get().getConceptCount();
        ContradictionProgressUpdator updator = new ContradictionProgressUpdator();

        frame.addStopActionListener(stopListener);
        frame.enableStopButton(true);
        frame.setProgressIndeterminate(true);
        frame.setProgressInfo("Starting the Contradiction Detector");

        completeLatch = new CountDownLatch(1);
        new ContradictionUpdator();

        // Create processor
        ccp = new ContradictionConceptProcessor(viewCoord, Ts.get().getAllConceptNids(), actvityPanel);

        // About to start
        Stopwatch timer = new Stopwatch();
        timer.start();
        updator.setIndeterminate(true);
        continueWork = true;

        // Iterate in Parallel
        Ts.get().iterateConceptDataInParallel(ccp);
      
        // Done, get results
        Set<Integer> returnSet = new HashSet<Integer>();
        returnSet.addAll(ccp.getResults().getConflictingNids());
        returnSet.addAll(ccp.getResults().getDuplicateNewNids());
        returnSet.addAll(ccp.getResults().getDuplicateEditNids());
        
        // Update Listeners
        continueWork = false;
        updator.setHits(returnSet.size());
        updator.setIndeterminate(false);
        completeLatch = new CountDownLatch(returnSet.size());

        frame.removeStopActionListener(stopListener);
        frame.enableStopButton(false);
        frame.setProgressValue(0);

        actvityPanel.complete();

        timer.stop();

        return returnSet;
    }


    @Override
    protected void done() {
        try {
            Set<Integer> conflictingNids = get();
            conflictingNids.removeAll(conflicts.getNidsInList());
            for (Integer cnid: conflictingNids) {
                conflicts.addElement((I_GetConceptData) 
                        Ts.get().getConcept(cnid));
            }
            
            if (continueWork) {
                continueWork = false;
                get();
            }
//            updateMatches();

            frame.removeStopActionListener(stopListener);
            frame.enableStopButton(false);
            frame.setProgressValue(0);

            
            int numberFound = ccp.getNumberContradictionsFound().get();
            if (numberFound == 0) {
            	frame.setProgressInfo("Finished with no contradictions detected");
            } else if (numberFound == 1) {
            	frame.setProgressInfo("Finished with " + numberFound + " contradiction detected");
            } else {
            	frame.setProgressInfo("Finished with " + numberFound + " contradictions detected");
            }

            actvityPanel.complete();
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
    
    public void updateMatches() {

    }


}
