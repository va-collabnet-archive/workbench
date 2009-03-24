package org.dwfa.ace.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;

public class RestoreSelectionSwingWorker extends SwingWorker<Object> implements ActionListener {

	private JTreeWithDragImage tree;
	private Object lastPropagationId;
	private int horizValue;
	private int vertValue;
	private TreePath selelectionPath;
	
	public RestoreSelectionSwingWorker(JTreeWithDragImage tree,
			Object lastPropagationId, int horizValue, int vertValue,
			TreePath selelectionPath) {
		super();
		this.tree = tree;
		this.lastPropagationId = lastPropagationId;
		this.horizValue = horizValue;
		this.vertValue = vertValue;
		this.selelectionPath = selelectionPath;
	}

	public RestoreSelectionSwingWorker(RestoreSelectionSwingWorker other) {
		super();
		this.tree = other.tree;
		this.lastPropagationId = other.lastPropagationId;
		this.horizValue = other.horizValue;
		this.vertValue = other.vertValue;
		this.selelectionPath = other.selelectionPath;
	}

	@Override
	protected Object construct() throws Exception {
		return null;
	}

	@Override
	protected void finished() {
		try {
			get();
			if (lastPropagationId.equals(tree.getLastPropagationId())) {
				if (ACE.expansionWorkers.size() == 0) {
					AceLog.getAppLog().info("RestoreSelectionSwingWorker resetting selection: " + lastPropagationId);
					if (selelectionPath != null && selelectionPath.getPathCount() > 0) {
						Object[] nodesToMatch = selelectionPath.getPath();
						TreePath pathToSelect = new TreePath(tree.getModel().getRoot());
						for (int pathNode = 1; pathNode < nodesToMatch.length; pathNode++) {
							DefaultMutableTreeNode parent = (DefaultMutableTreeNode) pathToSelect.getLastPathComponent();
							I_GetConceptData nodeToMatchObject = (I_GetConceptData) ((DefaultMutableTreeNode)nodesToMatch[pathNode]).getUserObject();
							for (int childNode = 0; childNode < parent.getChildCount(); childNode++) {
								DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(childNode);
								I_GetConceptData childObject = (I_GetConceptData) child.getUserObject();
								if (nodeToMatchObject.getConceptId() == childObject.getConceptId()) {
									pathToSelect = pathToSelect.pathByAddingChild(child);
									break;
								}
							}
						}
						tree.getSelectionModel().setSelectionPath(pathToSelect);
					}
		        	JScrollPane scroller = tree.getScroller();
		        	scroller.getHorizontalScrollBar().setValue(horizValue);
		        	scroller.getVerticalScrollBar().setValue(vertValue);
				} else {
					ACE.treeExpandThread.execute(new RestoreSelectionSwingWorker(this));
					AceLog.getAppLog().info("Expansion workers: " + ACE.expansionWorkers.entrySet());
					AceLog.getAppLog().info("Adding back RestoreSelectionSwingWorker: " + lastPropagationId);
					ACE.removeStaleExpansionWorker(ACE.expansionWorkers.keySet().iterator().next());
				}
			} else {
				AceLog.getAppLog().info("RestoreSelectionSwingWorker ending secondary to inequal propigationId: " + lastPropagationId
						+ " " + tree.getLastPropagationId());
			}
		} catch (InterruptedException e) {
			AceLog.getAppLog().alertAndLogException(e);
		} catch (ExecutionException e) {
			AceLog.getAppLog().alertAndLogException(e);
		}
	}

	public void actionPerformed(ActionEvent e) {
		AceLog.getAppLog().info("RestoreSelectionSwingWorker timer thread finished: " + lastPropagationId);
		ACE.treeExpandThread.execute(this);
	}
}
