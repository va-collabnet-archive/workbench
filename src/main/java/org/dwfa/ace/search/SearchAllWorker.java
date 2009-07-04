package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.Timer;

import org.dwfa.ace.ACE;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.config.FrameConfigSnapshot;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.types.ThinDescVersionedComparator;

public class SearchAllWorker extends SwingWorker<I_UpdateProgress> implements
		I_TrackContinuation {
	boolean continueWork = true;

	SearchPanel searchPanel;

	StopActionListener stopListener = new StopActionListener();

	CountDownLatch completeLatch;

	private int conceptCount;

	private Collection<I_DescriptionVersioned> regexMatches;
	private Collection<LuceneMatch> luceneMatches;

	private DescriptionsFromCollectionTableModel model;

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
			updateMatches();
		}

	}

	private class StopActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			continueWork = false;
			AceLog.getAppLog().info("Search canceled by user");
			searchPanel.setProgressInfo("cancelled by user");
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
			updateTimer = new Timer(500, this);
			updateTimer.start();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.dwfa.ace.search.I_UpdateProgress#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (continueWork) {
				if (firstUpdate) {
					if (conceptCount == Integer.MAX_VALUE) {
						searchPanel.setProgressIndeterminate(true);
					} else {
						searchPanel.setProgressIndeterminate(false);
						searchPanel.setProgressMaximum(conceptCount);
						firstUpdate = false;
					}
				}
				if (completeLatch != null) {
					searchPanel.setProgressValue((int) (searchPanel
							.getProgressMaximum() - completeLatch.getCount()));
				}
				if (firstUpdate) {
					searchPanel
					.setProgressInfo("   processing...   ");
				} else {
					updateProgress();
				}
				if (searchPanel.getProgressValue() == searchPanel
						.getProgressMaximum()) {
					AceLog.getAppLog().info("Normal completion at: " + searchPanel.getProgressMaximum());
					normalCompletion();
				}
			} else {
				updateTimer.stop();
			}
		}

		public void normalCompletion() {
			updateTimer.stop();
			if (firstUpdate) {
				searchPanel.setProgressIndeterminate(false);
				searchPanel.setProgressMaximum(conceptCount);
				firstUpdate = false;
			}
			searchPanel.setProgressValue(conceptCount);
			updateProgress();
		}

		private void updateProgress() {
			searchPanel.setProgressInfo("   " +
					regexMatches.size() + " descriptions out of " + (searchPanel.getProgressMaximum() - completeLatch.getCount())
							+ " concepts   ");
		}
	}


	public SearchAllWorker(SearchPanel searchPanel,
			DescriptionsFromCollectionTableModel model,
			I_ConfigAceFrame config) {
		super();
		this.config =  new FrameConfigSnapshot(config);
		this.model = model;
		this.searchPanel = searchPanel;
		this.searchPanel.addStopActionListener(stopListener);
		this.searchPanel.setProgressInfo("   Searching all...  ");
		this.searchPanel.setProgressIndeterminate(true);

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
		regexMatches = Collections
				.synchronizedCollection(new TreeSet<I_DescriptionVersioned>(
						new ThinDescVersionedComparator()));
		conceptCount = Integer.MAX_VALUE;
		conceptCount = AceConfig.getVodb().getConceptCount();
		I_UpdateProgress updater;
		if (conceptCount != Integer.MIN_VALUE) {
			AceLog.getAppLog().info("Concept count sa: " + conceptCount);
			searchPanel.setProgressMaximum(conceptCount);
			completeLatch = new CountDownLatch(conceptCount);
			new MatchUpdator();
			updater = new RegexProgressUpdator();
			AceConfig.getVodb().searchRegex(this, null, regexMatches, completeLatch,
					searchPanel.getExtraCriterion(), config);

			completeLatch.await();
		} else {
			searchPanel.setProgressMaximum(0);
			completeLatch = new CountDownLatch(0);
			updater = new RegexProgressUpdator();
			completeLatch.await();
		}
		return updater;
	}

	protected void finished() {
		AceLog.getAppLog().info("Search a finished.");
		try {
			if (continueWork) {
				continueWork = false;
				get();
			}
			updateMatches();
		} catch (InterruptedException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ExecutionException e) {
			if (ACE.editMode) {
				AceLog.getAppLog().alertAndLogException(e);
			} else {
				AceLog.getAppLog()
						.log(Level.SEVERE, e.getLocalizedMessage(), e);
			}
		}
		searchPanel.removeStopActionListener(stopListener);
		searchPanel.setShowProgress(false);
		searchPanel.setProgressIndeterminate(false);
		searchPanel.setProgressMaximum(searchPanel.getProgressValue());
		searchPanel.setProgressValue(0);

	}

	public boolean continueWork() {
		return continueWork;
	}
}
