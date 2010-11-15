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
package org.dwfa.ace.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
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
import org.dwfa.ace.I_UpdateProgress;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_FilterTaxonomyRels;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.PositionSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.swing.SwingWorker;
import org.dwfa.tapi.ComputationCanceled;
import org.ihtsdo.time.TimeUtil;

public class ExpandNodeSwingWorker extends SwingWorker<Object> implements ActionListener {

    private static int workerCount = 0;
    private static boolean logTimingInfo = false;
    private int workerId = workerCount++;
    private static Logger logger = Logger.getLogger(ExpandNodeSwingWorker.class.getName());

    private class StopActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            lowerProgressMessage = "cancelled by user";
            stop();
        }
    }
    boolean hideWorkerId = true;
    boolean removed = false;
    String workerIdStr = " [" + workerId + "]";
    String upperProgressMessage = "Expanding node " + workerIdStr;
    String lowerProgressMessage = "counting ";

    private class ProgressUpdator implements I_UpdateProgress {

        Timer updateTimer;
        long start = System.currentTimeMillis();
        ActivityPanel activity;

        public ProgressUpdator(boolean addToViewer) {
            super();
            activity = new ActivityPanel(config, true);
            if (addToViewer) {
                addToViewer = false;
                try {
                    ActivityViewer.addActivity(activity);
                } catch (Exception e1) {
                    AceLog.getAppLog().alertAndLogException(e1);
                }
            }
            updateTimer = new Timer(500, this);
            updateTimer.start();
        }

        public void actionPerformed(ActionEvent e) {
            if (lowerProgressMessage.startsWith("counting")) {
                activity.setProgressInfoLower(lowerProgressMessage + " continueWork:" + continueWork + " "
                        + activity.nextSpinner());
            }
            activity.setIndeterminate(maxChildren == -1);
            if ((completeLatch != null) && (!canceled)) {
                int processed = (int) (maxChildren - completeLatch.getCount());
                activity.setValue(processed);
                activity.setMaximum(maxChildren);
                activity.setProgressInfoLower(lowerProgressMessage + processed + "/" + maxChildren + " "
                        + activity.nextSpinner());
            } else {
                activity.setProgressInfoLower(lowerProgressMessage);
            }
            activity.setProgressInfoUpper(upperProgressMessage);
            if (!continueWork) {
                try {
                    activity.complete();
                } catch (ComputationCanceled e1) {
                    //;
                }
                updateTimer.stop();
                boolean alwaysRemove = true;
                if (alwaysRemove) {
                    activity.removeActivityFromViewer();
                } else {
                    if (System.currentTimeMillis() - start < 1000) {
                        activity.removeActivityFromViewer();
                    } else if (lowerProgressMessage.contains("Action programatically stopped")) {
                        activity.removeActivityFromViewer();
                    }
                    if (lowerProgressMessage.startsWith("counting")) {
                        activity.setProgressInfoLower(lowerProgressMessage + " continueWork:" + continueWork + " "
                                + activity.nextSpinner());
                    }
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
                        lowerProgressMessage = "stopped because ChildrenUpdator stuck at: " + lastCheck + " ("
                                + stuckCount + ") ";
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
        int relId;

        public AddChildWorker(int conceptId, int relId) {
            super();
            this.conceptId = conceptId;
            this.relId = relId;
        }

        public void run() {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("ExpandNodeSwingWorker " + workerId + " AddChildWorker: " + conceptId + " starting");
                }
                DefaultMutableTreeNode child = null;
                if (checkContinueWork("checking in add child worker")) {
                    if (Terms.get().hasConcept(conceptId)) {
                        I_GetConceptData cb = ConceptBeanForTree.get(conceptId, relId, 0, false,
                                ExpandNodeSwingWorker.this.config);
                        boolean leaf = cb.isLeaf(config, false);
                        ;
                        child = new DefaultMutableTreeNode(cb, !leaf);
                        sortedNodes.add(child);
                    }
                    completeLatch.countDown();
                }
            } catch (Throwable ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("ExpandNodeSwingWorker " + workerId + " AddChildWorker: " + conceptId + " finished");
            }
        }
    }

    private class MakeSrcChildWorkers implements Runnable {

        public void run() {
            for (I_RelTuple r : destRels) {
                try {
                    if (Terms.get().hasConcept(r.getC2Id())) {
                        ACE.threadPool.execute(new AddChildWorker(r.getC1Id(), r.getRelId()));
                    }
                } catch (Throwable e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
    }

    private class MakeDestChildWorkers implements Runnable {

        public void run() {
            for (I_RelTuple r : srcRels) {
                try {
                    if (Terms.get().hasConcept(r.getC2Id())) {
                        ACE.threadPool.execute(new AddChildWorker(r.getC2Id(), r.getRelId()));
                    }
                } catch (Throwable e) {
                    AceLog.getAppLog().alertAndLogException(e);
                }
            }
        }
    }
    DefaultTreeModel model;
    DefaultMutableTreeNode node;
    Comparator<I_GetConceptDataForTree> conceptBeanComparator;
    Boolean continueWork = true;

    public Boolean getContinueWork() {
        return continueWork;
    }
    boolean canceled = false;
    int maxChildren = -1;
    CountDownLatch completeLatch;
    List<? extends I_RelTuple> destRels;
    List<? extends I_RelTuple> srcRels;
    SortedSet<DefaultMutableTreeNode> sortedNodes;
    StopActionListener stopListener = new StopActionListener();
    private TermTreeHelper treeHelper;
    private JTreeWithDragImage tree;

    @Override
    protected I_UpdateProgress construct() throws Exception {
        upperProgressMessage = "Expanding for " + node + workerIdStr;
        I_GetConceptData cb = (I_GetConceptData) node.getUserObject();
        I_IntSet allowedStatus = this.config.getAllowedStatus();
        I_IntSet destRelTypes = this.config.getDestRelTypes();
        I_IntSet sourceRelTypes = this.config.getSourceRelTypes();
        PositionSetReadOnly positions = this.config.getViewPositionSetReadOnly();

        if ((destRelTypes.getSetValues().length == 0) && (sourceRelTypes.getSetValues().length == 0)) {
            allowedStatus = null;
            destRelTypes = null;
            sourceRelTypes = null;
            positions = null;
        }

        lowerProgressMessage = "getting destination rels ";
        
        destRels = cb.getDestRelTuples(allowedStatus, destRelTypes, positions,
                config.getPrecedence(), config.getConflictResolutionStrategy(),
                config.getClassifierConcept().getNid(), config.getRelAssertionType());
        lowerProgressMessage = "getting source rels ";
        srcRels = cb.getSourceRelTuples(allowedStatus, sourceRelTypes, positions,
                config.getPrecedence(), config.getConflictResolutionStrategy(),
                config.getClassifierConcept().getNid(), config.getRelAssertionType());
        for (I_FilterTaxonomyRels taxonomyFilter : config.getTaxonomyRelFilterList()) {
            taxonomyFilter.filter(cb, srcRels, destRels, config);
        }
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
        long elapsedTime = Long.MIN_VALUE;
        try {
            get();
            elapsedTime = System.currentTimeMillis() - expansionStart;
            if (!canceled) {
                upperProgressMessage = "Finishing " + node + workerIdStr;
                if (continueWork) {
                    updateChildrenInNode();
                    upperProgressMessage = "Expansion complete for " + node + workerIdStr;
                    completeLatch = null;
                    lowerProgressMessage = "Fetched " + node.getChildCount() + " children in "
                            + TimeUtil.getElapsedTimeString(elapsedTime);
                    if (node.getChildCount() != maxChildren) {
                        upperProgressMessage = "<html><font color=red>Warning for " + node + " expected children = "
                                + maxChildren + " actual: " + node.getChildCount() + workerIdStr;
                    }
                    stopWorkAndRemove("worker finished");
                    expandIfInList();
                }
            } else {
                stopWorkAndRemove("worker canceled");
            }
        } catch (InterruptedException ex) {
            stopWorkAndRemove("worker interrupted");
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (ExecutionException ex) {
            stopWorkAndRemove("worker threw exception");
            AceLog.getAppLog().alertAndLogException(ex);
        }
        if (elapsedTime == Long.MIN_VALUE) {
            elapsedTime = System.currentTimeMillis() - expansionStart;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ExpandNodeSwingWorker " + workerId + " for " + node + " finished in " + TimeUtil.getElapsedTimeString(elapsedTime));
        } else if (logTimingInfo) {
            logger.info("ExpandNodeSwingWorker " + workerId + " for " + node + " finished in " + TimeUtil.getElapsedTimeString(elapsedTime));
        }
        tree.workerFinished(this);
        workers.remove(node.getUserObject());

    }

    private void updateChildrenInNode() {
        List<DefaultMutableTreeNode> sortedList = new ArrayList<DefaultMutableTreeNode>(sortedNodes);
        node.removeAllChildren();
        for (DefaultMutableTreeNode child : sortedList) {
            node.add(child);
        }
        model.nodeStructureChanged(node);
    }

    private void expandIfInList() {

        TreePath thisPath = new TreePath(node.getPath());
        if (tree.isExpanded(thisPath) == false) {
            tree.expandPath(thisPath);
        }

        for (DefaultMutableTreeNode child : sortedNodes) {
            TreePath childPath = new TreePath(child.getPath());
            I_GetConceptData cb = (I_GetConceptData) child.getUserObject();

            if (config.getChildrenExpandedNodes().contains(cb.getConceptNid())) {

                DefaultMutableTreeNode ancestor = (DefaultMutableTreeNode) child.getParent();
                while (ancestor != null) {
                    I_GetConceptData parentBean = (I_GetConceptData) ancestor.getUserObject();
                    if (parentBean != null) {
                        if (parentBean.getConceptNid() == cb.getConceptNid()) {
                            AceLog.getAppLog().warning("###\n### Auto expand stopped. Found cycle.\n###");
                            return;
                        }
                    }
                    ancestor = (DefaultMutableTreeNode) ancestor.getParent();
                }

                if (tree.isExpanded(childPath) == false) {
                    tree.expandPath(childPath);
                } else {
                    AceLog.getAppLog().info(" Already expanded");
                }
            }

        }
    }

    private static class NodeComparator implements Comparator<DefaultMutableTreeNode> {

        Comparator<I_GetConceptDataForTree> comparator;

        public NodeComparator(Comparator<I_GetConceptDataForTree> comparator) {
            super();
            this.comparator = comparator;
        }

        public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
            return comparator.compare((I_GetConceptDataForTree) o1.getUserObject(),
                    (I_GetConceptDataForTree) o2.getUserObject());
        }
    }
    private long expansionStart;
    private I_ConfigAceFrame config;
    private static Map<Object, ExpandNodeSwingWorker> workers = new TreeMap<Object, ExpandNodeSwingWorker>();

    public ExpandNodeSwingWorker(DefaultTreeModel model, JTreeWithDragImage tree, DefaultMutableTreeNode node,
            Comparator<I_GetConceptDataForTree> conceptBeanComparator, TermTreeHelper acePanel, I_ConfigAceFrame config) {
        super();
        if (workers.containsKey(node.getUserObject())) {
            ExpandNodeSwingWorker oldWorker = workers.get(node.getUserObject());
            oldWorker.stopWork("canceled 1");
            workers.put(node.getUserObject(), this);
        }
        expansionStart = System.currentTimeMillis();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ExpandNodeSwingWorker " + workerId + " starting.");
        }
        this.model = model;
        this.tree = tree;
        if (hideWorkerId) {
            workerIdStr = "";
        }
        this.node = node;
        this.treeHelper = acePanel;
        this.config = config;
        this.conceptBeanComparator = conceptBeanComparator;
        sortedNodes = Collections.synchronizedSortedSet(new TreeSet<DefaultMutableTreeNode>(new NodeComparator(
                conceptBeanComparator)));
        upperProgressMessage = "Expanding " + node + workerIdStr;
        ProgressUpdator progressUpdator = new ProgressUpdator(true);
        progressUpdator.activity.addRefreshActionListener(this);
        treeHelper.setTreeActivityPanel(progressUpdator.activity);
    }

    public void stopWork(String message) {
        if (continueWork) {
            continueWork = false;
            canceled = true;
            lowerProgressMessage = "<html><font color=blue>Action programatically stopped: " + message;
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
        treeHelper.removeExpansionWorker(idPath, this, message);
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

    public static boolean getLogTimingInfo() {
        return logTimingInfo;
    }

    public static void setLogTimingInfo(boolean logTimingInfo) {
        ExpandNodeSwingWorker.logTimingInfo = logTimingInfo;
    }
}
