package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TrackContinuation;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.ace.task.search.I_TestSearchResults;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.swing.SwingWorker;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class SearchRefsetWorker extends SwingWorker<I_UpdateProgress> implements I_TrackContinuation {
    boolean continueWork = true;

    SearchPanel searchPanel;

    StopActionListener stopListener = new StopActionListener();

    CountDownLatch completeLatch;

    private int extensionCount;

    private Collection<I_DescriptionVersioned> refsetMatches;

    private DescriptionsFromCollectionTableModel model;

    I_ConfigAceFrame config;

	private I_GetConceptData refsetConcept;

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
                    searchPanel.setProgressInfo("   Starting refset search   ");
                    if (extensionCount != Integer.MAX_VALUE) {
                        searchPanel.setProgressIndeterminate(false);
                        searchPanel.setProgressMaximum(extensionCount);
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
            searchPanel.setProgressInfo("   " + refsetMatches.size() + " matches out of " + max + " members   ");
        }

        public void normalCompletion() {
            updateTimer.stop();
            if (firstUpdate) {
                searchPanel.setProgressIndeterminate(false);
                searchPanel.setProgressMaximum(extensionCount);
                firstUpdate = false;
            }
            searchPanel.setProgressValue(0);
        }

    }

    public SearchRefsetWorker(SearchPanel searchPanel, DescriptionsFromCollectionTableModel model,
            I_GetConceptData refsetConcept, I_ConfigAceFrame config) {
        super();
        this.config = new FrameConfigSnapshot(config);
        this.model = model;
        this.searchPanel = searchPanel;
        this.searchPanel.addStopActionListener(stopListener);
        this.searchPanel.setProgressInfo("   Searching lucene for " + refsetConcept + "   ");
        this.searchPanel.setProgressIndeterminate(true);
        this.refsetConcept = refsetConcept;
    }

    public void updateMatches() {
        this.model.setDescriptions(refsetMatches);
    }

    @Override
    protected I_UpdateProgress construct() throws Exception {
        I_UpdateProgress updater;
            refsetMatches = new ConcurrentSkipListSet<I_DescriptionVersioned>(
                new DescriptionComparator());
            updater = new RegexProgressUpdator();
            boolean alertForUnsupportedRc = true;
            try {
                Collection<? extends I_ExtendByRef> extensions = refsetConcept.getExtensions();
                extensionCount = extensions.size();
                completeLatch = new CountDownLatch(extensionCount);
                new MatchUpdator();
                List<I_TestSearchResults> criterion = searchPanel.getExtraCriterion();
                int fsnNid = Ts.get().uuidsToNid(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids());
                nextExt: for (I_ExtendByRef ext: extensions) {
                	completeLatch.countDown();
                	List<? extends I_ExtendByRefVersion> versions = ext.getTuples(config.getAllowedStatus(), config.getViewPositionSetReadOnly(), 
                			config.getPrecedence(), config.getConflictResolutionStrategy());
                	for (I_ExtendByRefVersion extVer: versions) {
                		for (I_TestSearchResults test: criterion) {
                			if (!test.test(extVer, config)) {
                				continue nextExt;
                			}
                		}
                    	// passed the tests. Add to results. 
                		ComponentBI referencedComponent = Ts.get().getComponent(extVer.getComponentId());
                		if (ConceptChronicleBI.class.isAssignableFrom(referencedComponent.getClass())) {
                			ConceptChronicleBI conceptChr = (ConceptChronicleBI) referencedComponent;
                			for (DescriptionChronicleBI descChr: conceptChr.getDescs()) {
                    			for (DescriptionVersionBI descV: descChr.getVersions(config.getCoordinate())) {
                    				if (descV.getTypeNid() == fsnNid) {
                        				refsetMatches.add((I_DescriptionVersioned) descV);
                        				continue nextExt;
                    				}
                    			}
                			}
                		} else if (DescriptionChronicleBI.class.isAssignableFrom(referencedComponent.getClass())) {
                			DescriptionChronicleBI descChr = (DescriptionChronicleBI) referencedComponent;
            				refsetMatches.add((I_DescriptionVersioned) descChr);
                		} else {
                			if (alertForUnsupportedRc) {
                				alertForUnsupportedRc = false;
                				AceLog.getAppLog().alertAndLogException(
                						new Exception("Search does not support referenced component type: " +
                								referencedComponent));
                			}
                		}
                		
                	}
                }
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
                while (completeLatch.getCount() > 0) {
                    completeLatch.countDown();
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
