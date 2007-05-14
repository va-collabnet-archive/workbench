package org.dwfa.ace.search;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.Timer;

import org.dwfa.ace.AceLog;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.config.AceConfig;
import org.dwfa.ace.table.DescriptionsFromCollectionTableModel;
import org.dwfa.swing.SwingWorker;
import org.dwfa.vodb.types.ThinDescVersioned;
import org.dwfa.vodb.types.ThinDescVersionedComparator;

public class SearchStringWorker extends SwingWorker<I_UpdateProgress> implements I_TrackContinuation {
	boolean continueWork = true;

	SearchPanel searchPanel;

	StopActionListener stopListener = new StopActionListener();

	CountDownLatch completeLatch;

	private String patternString;

	private int descCount;

	private Collection<ThinDescVersioned> matches;

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
			updateTimer = new Timer(100, this);
			updateTimer.start();
		}

		/* (non-Javadoc)
		 * @see org.dwfa.ace.search.I_UpdateProgress#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (continueWork) {
				if (firstUpdate) {
					searchPanel.setProgressIndeterminate(false);
					searchPanel.setProgressMaximum(descCount);
					firstUpdate = false;
				}
				searchPanel
						.setProgressValue((int) (searchPanel.getProgressMaximum() - completeLatch
								.getCount()));
				searchPanel.setProgressInfo("   " +
						searchPanel.getProgressValue() + "/" + searchPanel.getProgressMaximum()
								+ " descriptions   ");
				if (searchPanel.getProgressValue() == searchPanel.getProgressMaximum()) {
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
				searchPanel.setProgressMaximum(descCount);
				firstUpdate = false;
			}
			searchPanel.setProgressValue(descCount);
			searchPanel.setProgressInfo(
					"   Searched " + searchPanel.getProgressMaximum() + " descriptions   ");
		}

	}
	public class LuceneProgressUpdator implements I_UpdateProgress {
		Timer updateTimer;

		boolean firstUpdate = true;

		private String info;

		public LuceneProgressUpdator() {
			super();
			updateTimer = new Timer(100, this);
			updateTimer.start();
		}

		/* (non-Javadoc)
		 * @see org.dwfa.ace.search.I_UpdateProgress#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			if (continueWork) {
				if (firstUpdate) {
					searchPanel.setProgressIndeterminate(true);
					searchPanel.setProgressMaximum(descCount);
					firstUpdate = false;
				}
				searchPanel
						.setProgressValue((int) (searchPanel.getProgressMaximum() - completeLatch
								.getCount()));
				searchPanel.setProgressInfo("   " + info +  ")");
				if (searchPanel.getProgressValue() == searchPanel.getProgressMaximum()) {
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
				searchPanel.setProgressMaximum(descCount);
				firstUpdate = false;
			}
			searchPanel.setProgressValue(descCount);
			searchPanel.setProgressInfo(
					"   Searched " + searchPanel.getProgressMaximum() + " descriptions   ");
		}
		
		public void setProgressInfo(String info) {
			this.info = info;
		}

	}

	public SearchStringWorker(SearchPanel searchPanel, 
			DescriptionsFromCollectionTableModel model, String patternString,
			I_ConfigAceFrame config, boolean lucene) {
		super();
		this.config = config;
		this.model = model;
		this.searchPanel = searchPanel;
		this.searchPanel.addStopActionListener(stopListener);
		this.searchPanel.setProgressInfo(
				"   Searching for " + patternString + "   ");
		this.searchPanel.setProgressIndeterminate(true);
		this.patternString = patternString;
		this.lucene = lucene;

	}

	public void updateMatches() {
		this.model.setDescriptions(matches);
		
	}

	@Override
	protected I_UpdateProgress construct() throws Exception {
		if (patternString.length() < 2) {
			continueWork = false;
			throw new Exception("Search string to short: " + patternString);
		}
		matches = Collections
				.synchronizedCollection(new TreeSet<ThinDescVersioned>(
						new ThinDescVersionedComparator()));
		I_UpdateProgress updater;
		if (lucene) {
			updater = new LuceneProgressUpdator();
			File luceneDir = new File("lucene");
			boolean makeIndex = (luceneDir.exists() == false);
			if (makeIndex) {
				descCount = AceConfig.vodb.countDescriptions();
				completeLatch = new CountDownLatch(descCount + 1);
			} else {
				completeLatch = new CountDownLatch(1);
			}
			new MatchUpdator();
			AceConfig.vodb.searchLucene(this, patternString, matches, completeLatch, 
					searchPanel.getRootConcept(), config, luceneDir, (LuceneProgressUpdator) updater);
		} else {
			updater = new RegexProgressUpdator();
			descCount = AceConfig.vodb.countDescriptions();
			completeLatch = new CountDownLatch(descCount);
			Pattern p = Pattern.compile(patternString);
			new MatchUpdator();
			AceConfig.vodb.searchRegex(this, p, matches, completeLatch, searchPanel.getRootConcept(), config);
			completeLatch.await();
		}
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
		} catch (ExecutionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
		searchPanel.removeStopActionListener(stopListener);
		searchPanel.setShowProgress(false);
		}

	public boolean continueWork() {
		return continueWork;
	}
}
