package org.dwfa.ace.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.AceLog;
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.swing.SwingWorker;

public class ExpandNodeSwingWorker extends SwingWorker<Object> implements
		ActionListener {

	private static int workerCount = 0;

	private int workerId = workerCount++;

	private static Logger logger = Logger.getLogger(ExpandNodeSwingWorker.class
			.getName());

	private class StopActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			lowerProgressMessage = "cancelled by user";
			stop();
		}

	}

	boolean hideWorkerId = true;

	String workerIdStr = " [" + workerId + "]";

	String upperProgressMessage = "Expanding node " + workerIdStr;

	String lowerProgressMessage = "counting ";

	private class ProgressUpdator implements I_UpdateProgress {
		Timer updateTimer;

		ActivityPanel activity;

		private boolean addToViewer;

		public ProgressUpdator(boolean addToViewer) {
			super();
			this.addToViewer = addToViewer;
			activity = new ActivityPanel(addToViewer, addToViewer);
			updateTimer = new Timer(300, this);
			updateTimer.start();
		}

		public void actionPerformed(ActionEvent e) {
			if (addToViewer) {
				addToViewer = false;
				ActivityViewer.addActivity(activity);
			}
			if (lowerProgressMessage.startsWith("counting")) {
				activity.setProgressInfoLower(lowerProgressMessage
						+ " continueWork:" + continueWork + " "
						+ activity.nextSpinner());
			}
			activity.setIndeterminate(maxChildren == -1);
			if ((completeLatch != null) && (!canceled)) {
				int processed = (int) (maxChildren - completeLatch.getCount());
				activity.setValue(processed);
				activity.setMaximum(maxChildren);
				activity.setProgressInfoLower(lowerProgressMessage + processed
						+ "/" + maxChildren + " " + activity.nextSpinner());
			} else {
				activity.setProgressInfoLower(lowerProgressMessage);
			}
			activity.setProgressInfoUpper(upperProgressMessage);
			if (!continueWork) {
				activity.complete();
				updateTimer.stop();
				if (lowerProgressMessage.startsWith("counting")) {
					activity.setProgressInfoLower(lowerProgressMessage
							+ " continueWork:" + continueWork + " "
							+ activity.nextSpinner());
				}
			}
		}

	}

	private class ChildrenUpdator implements ActionListener {
		
		private int allowableSticks = 20;
		
		Timer updateTimer;

		boolean inProgress;

		private Long lastCheck = Long.MAX_VALUE;
		
		private int stuckCount = 0;

		public ChildrenUpdator() {
			super();
			updateTimer = new Timer(1000, this);
			updateTimer.start();
		}

		public void actionPerformed(ActionEvent e) {
			if (completeLatch != null) {
				if (lastCheck == completeLatch.getCount()) {
					stuckCount++;
					if (logger.isLoggable(Level.FINE)) {
						logger.fine("ChildrenUpdator stuck at: " + lastCheck + " (" + stuckCount + ")");
					}
					if (stuckCount > allowableSticks) {
						if (logger.isLoggable(Level.INFO)) {
							logger.info("ChildrenUpdator stuck count exceeds allowable.");
						}
						lowerProgressMessage = "stopped because ChildrenUpdator stuck at: " + lastCheck + " (" + stuckCount + ") ";
						stop();
					}
				} else {
					if (checkContinueWork("checking in Children Updator")) {
						if (!inProgress) {
							lastCheck = completeLatch.getCount();
							stuckCount = 0;
							inProgress = true;
							updateChildrenInNode();
							inProgress = false;
						}
					} else {
						updateTimer.stop();
					}
				}
			} else {
				updateTimer.stop();
			}
		}
	}

	private class AddChildWorker implements Runnable {
		int conceptId;

		public AddChildWorker(int conceptId) {
			super();
			this.conceptId = conceptId;
		}

		public void run() {
			try {
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("ExpandNodeSwingWorker " + workerId
							+ " AddChildWorker: " + conceptId + " starting");
				}
				DefaultMutableTreeNode child = null;
				if (checkContinueWork("checking in add child worker")) {
					I_GetConceptData cb = ConceptBeanForTree.get(conceptId, 0,
							false);
					cb.getInitialText();
					boolean leaf = false;
					if ((acePanel.getAceFrameConfig().getDestRelTypes()
							.getSetValues().length == 0)
							&& (acePanel.getAceFrameConfig()
									.getSourceRelTypes().getSetValues().length == 0)) {
						leaf = cb.isLeaf(null, false);
					} else {
						leaf = cb.isLeaf(acePanel.getAceFrameConfig(), false);
					}

					child = new DefaultMutableTreeNode(cb, !leaf);
					sortedNodes.add(child);
					completeLatch.countDown();
				}
			} catch (Exception ex) {
				AceLog.getAppLog().alertAndLogException(ex);
			}
			if (logger.isLoggable(Level.FINEST)) {
				logger.finest("ExpandNodeSwingWorker " + workerId
						+ " AddChildWorker: " + conceptId + " finished");
			}
		}

	}

	private class MakeSrcChildWorkers implements Runnable {
		public void run() {
			for (I_RelTuple r : destRels) {
				ACE.threadPool.execute(new AddChildWorker(r.getC1Id()));
			}
		}

	}

	private class MakeDestChildWorkers implements Runnable {
		public void run() {
			for (I_RelTuple r : srcRels) {
				ACE.threadPool.execute(new AddChildWorker(r.getC2Id()));
			}
		}

	}

	DefaultTreeModel model;

	DefaultMutableTreeNode node;

	Comparator<I_GetConceptData> conceptBeanComparator;

	Boolean continueWork = true;

	boolean canceled = false;

	int maxChildren = -1;

	CountDownLatch completeLatch;

	List<I_RelTuple> destRels;

	List<I_RelTuple> srcRels;

	SortedSet<DefaultMutableTreeNode> sortedNodes;

	StopActionListener stopListener = new StopActionListener();

	private ACE acePanel;

	private JTreeWithDragImage tree;

	@Override
	protected I_UpdateProgress construct() throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				ProgressUpdator progressUpdator = new ProgressUpdator(false);
				acePanel.setTreeActivityPanel(progressUpdator.activity);
				progressUpdator.activity
						.addActionListener(ExpandNodeSwingWorker.this);
			}
		});
		upperProgressMessage = "Construct " + node + workerIdStr;
		I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
		I_IntSet allowedStatus = acePanel.getAceFrameConfig().getAllowedStatus();
		I_IntSet destRelTypes = acePanel.getAceFrameConfig().getDestRelTypes();
		I_IntSet sourceRelTypes = acePanel.getAceFrameConfig()
				.getSourceRelTypes();
		Set<I_Position> positions = acePanel.getAceFrameConfig()
				.getViewPositionSet();

		if ((destRelTypes.getSetValues().length == 0)
				&& (sourceRelTypes.getSetValues().length == 0)) {
			allowedStatus = null;
			destRelTypes = null;
			sourceRelTypes = null;
			positions = null;
		}
		// allowedStatus = null;

		lowerProgressMessage = "getting destination rels ";
		destRels = cb.getDestRelTuples(allowedStatus, destRelTypes, positions,
				false);
		lowerProgressMessage = "getting source rels ";
		srcRels = cb.getSourceRelTuples(allowedStatus, sourceRelTypes,
				positions, false);
		maxChildren = destRels.size() + srcRels.size();
		completeLatch = new CountDownLatch(maxChildren);
		lowerProgressMessage = "fetching ";
		ACE.threadPool.execute(new MakeSrcChildWorkers());
		ACE.threadPool.execute(new MakeDestChildWorkers());
		new ChildrenUpdator();
		if (checkContinueWork("checking in construct")) {
			completeLatch.await();
		}
		return null;
	}

	/**
	 * Executes on the AWT Event dispatch thread.
	 */
	protected void finished() {
		try {
			get();
			if (!canceled) {
				upperProgressMessage = "Finishing " + node + workerIdStr;
				if (continueWork) {
					updateChildrenInNode();
					upperProgressMessage = "Expansion complete for " + node
							+ workerIdStr;
					completeLatch = null;
					lowerProgressMessage = "Fetched " + node.getChildCount()
							+ " children";
					if (node.getChildCount() != maxChildren) {
						upperProgressMessage = "<html><font color=red>Warning for "
								+ node
								+ " expected children = "
								+ maxChildren
								+ " actual: "
								+ node.getChildCount()
								+ workerIdStr;
					}
					stopWorkAndRemove("worker finished");
					expandIfInList();
				}
			}
		} catch (InterruptedException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (ExecutionException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("ExpandNodeSwingWorker " + workerId + " finished.");
		}
		tree.workerFinished(this);
	}

	private void updateChildrenInNode() {
		List<DefaultMutableTreeNode> sortedList = new ArrayList<DefaultMutableTreeNode>(
				sortedNodes);
		node.removeAllChildren();
		for (DefaultMutableTreeNode child : sortedList) {
			node.add(child);
		}
		model.nodeStructureChanged(node);
	}

	private void expandIfInList() {
		for (DefaultMutableTreeNode child : sortedNodes) {
			TreePath tp = new TreePath(child.getPath());
			I_GetConceptData cb = (I_GetConceptData) child.getUserObject();

			if (acePanel.getAceFrameConfig().getChildrenExpandedNodes()
					.contains(cb.getConceptId())) {

				DefaultMutableTreeNode ancestor = (DefaultMutableTreeNode) child
						.getParent();
				while (ancestor != null) {
					I_GetConceptData parentBean = (I_GetConceptData) ancestor
							.getUserObject();
					if (parentBean != null) {
						if (parentBean.getConceptId() == cb.getConceptId()) {
							System.out
									.println(" Auto expand stopped. Found cycle.");
							return;
						}
					}
					ancestor = (DefaultMutableTreeNode) ancestor.getParent();
				}

				if (tree.isExpanded(tp) == false) {
					tree.expandPath(tp);
				} else {
					AceLog.getAppLog().info(" Already expanded");
				}
			}

		}
	}

	private static class NodeComparator implements
			Comparator<DefaultMutableTreeNode> {
		Comparator<I_GetConceptData> comparator;

		public NodeComparator(Comparator<I_GetConceptData> comparator) {
			super();
			this.comparator = comparator;
		}

		public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
			return comparator.compare((I_GetConceptData) o1.getUserObject(),
					(I_GetConceptData) o2.getUserObject());
		}

	}

	public ExpandNodeSwingWorker(DefaultTreeModel model, JTreeWithDragImage tree,
			DefaultMutableTreeNode node,
			Comparator<I_GetConceptData> conceptBeanComparator, ACE acePanel) {
		super();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("ExpandNodeSwingWorker " + workerId + " starting.");
		}
		this.model = model;
		this.tree = tree;
		if (hideWorkerId) {
			workerIdStr = "";
		}
		ProgressUpdator progressUpdator = new ProgressUpdator(true);
		progressUpdator.activity.addActionListener(this);
		this.node = node;
		this.acePanel = acePanel;
		this.conceptBeanComparator = conceptBeanComparator;
		sortedNodes = Collections
				.synchronizedSortedSet(new TreeSet<DefaultMutableTreeNode>(
						new NodeComparator(conceptBeanComparator)));
		upperProgressMessage = "Expanding " + node + workerIdStr;
	}

	public void stopWork(String message) {
		if (continueWork) {
			continueWork = false;
			canceled = true;
			lowerProgressMessage = "<html><font color=blue>Action programatically stopped: "
					+ message;
		}
		if (completeLatch != null) {
			while (completeLatch.getCount() > 0) {
				completeLatch.countDown();
			}
		}
	}

	private void stopWorkAndRemove(String message) {
		continueWork = false;
		TreeIdPath idPath = new TreeIdPath(node.getPath());
		acePanel.removeExpansionWorker(idPath, this, message);
		if (completeLatch != null) {
			while (completeLatch.getCount() > 0) {
				completeLatch.countDown();
			}
		}
	}

	private boolean checkContinueWork(String message) {
		if (!continueWork) {
			stopWorkAndRemove(message);
		}
		return continueWork;
	}

	public void actionPerformed(ActionEvent e) {
		continueWork = false;
		canceled = true;
		lowerProgressMessage = "<html><font color=red>User stopped worker";
	}

	private void stop() {
		continueWork = false;
		if (completeLatch != null) {
			while (completeLatch.getCount() > 0) {
				completeLatch.countDown();
			}
		}
		/*
		 * To avoid having JTree re-expand the root node, we disable
		 * ask-allows-children when we notify JTree about the new node
		 * structure.
		 */
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				node.removeAllChildren();
				model.setAsksAllowsChildren(false);
				model.nodeStructureChanged(node);
				model.setAsksAllowsChildren(true);
			}
			
		});
	}

}
