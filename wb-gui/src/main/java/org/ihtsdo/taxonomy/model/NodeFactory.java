
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.taxonomy.model;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.helper.thread.NamedThreadFactory;
import org.ihtsdo.taxonomy.CancelableBI;
import org.ihtsdo.taxonomy.TaxonomyNodeRenderer;
import org.ihtsdo.taxonomy.nodes.InternalNode;
import org.ihtsdo.taxonomy.nodes.InternalNodeMultiParent;
import org.ihtsdo.taxonomy.nodes.LeafNode;
import org.ihtsdo.taxonomy.nodes.LeafNodeMultiParent;
import org.ihtsdo.taxonomy.nodes.NodeComparator;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

/**
 *
 * @author kec
 */
public class NodeFactory {
    
    private static final ThreadGroup nodeFactoryThreadGroup = new ThreadGroup("NodeFactory ");
    public static ExecutorService taxonomyExecutors =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() + 1),
            new NamedThreadFactory(nodeFactoryThreadGroup, "Taxonomy "));
    public static ExecutorService pathExpanderExecutors =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() + 1),
            new NamedThreadFactory(nodeFactoryThreadGroup, "PathExpander "));
    public static ExecutorService childFinderExecutors =
            Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() + 1),
            new NamedThreadFactory(nodeFactoryThreadGroup, "ChildFinder "));
    //~--- fields --------------------------------------------------------------
    private ConcurrentSkipListMap<Long, MakeChildNodesWorker> childWorkerMap = new ConcurrentSkipListMap<Long, MakeChildNodesWorker>();
    private ChildNodeFilterBI childNodeFilter;
    private NodeExpansionListener expansionListener;
    private TaxonomyModel model;
    private NodeComparator nodeComparator;
    private TaxonomyNodeRenderer renderer;
    protected JTree tree;

    //~--- constructors --------------------------------------------------------
    public NodeFactory(TaxonomyModel model, TaxonomyNodeRenderer renderer, JTree tree,
            ChildNodeFilterBI childNodeFilter) {
        this.model = model;
        this.renderer = renderer;
        this.tree = tree;
        this.childNodeFilter = childNodeFilter;
        this.nodeComparator = new NodeComparator(model.nodeStore); 
        expansionListener = new NodeExpansionListener(tree);
    }

    //~--- methods -------------------------------------------------------------
    public void addNodeExpansionListener(JTree tree) {
        tree.addTreeExpansionListener(expansionListener);
        tree.addTreeWillExpandListener(expansionListener);
    }
    
    public void removeNodeExpansionListener(JTree tree) {
        tree.removeTreeExpansionListener(expansionListener);
        tree.removeTreeWillExpandListener(expansionListener);
    }
    
    public static void close() {
        try {
            pathExpanderExecutors.shutdown();
            pathExpanderExecutors.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            taxonomyExecutors.shutdown();
            taxonomyExecutors.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            childFinderExecutors.shutdown();
            childFinderExecutors.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void collapseNode(TaxonomyNode node) {
        CollapseHandler ch = new CollapseHandler(NodePath.getTreePath(model, node));
        
        FutureHelper.addFuture(taxonomyExecutors.submit(ch));
    }
    
    public CountDownLatch makeChildNodes(TaxonomyNode parentNode) throws IOException, Exception {
        return makeChildNodes(parentNode, null);
    }
    public CountDownLatch makeChildNodes(TaxonomyNode parentNode, List<SwingWorker> workers) throws IOException, Exception {
        
        if (parentNode == null || parentNode.isSecondaryParentNode()) {
            return new CountDownLatch(0);
        }
        
        MakeChildNodesWorker mcnw = childWorkerMap.get(parentNode.nodeId);
        
        if (mcnw == null) {
            mcnw = new MakeChildNodesWorker(parentNode, childNodeFilter, workers);
            childWorkerMap.put(parentNode.nodeId, mcnw);
            FutureHelper.addFuture(taxonomyExecutors.submit(mcnw));
        }
        
        return mcnw.latch;
    }
    
     
    public TaxonomyNode makeNode(int nid, TaxonomyNode parentNode) throws IOException, Exception {
        if (model.nodeStore.containsKey(TaxonomyModel.getNodeId(nid, parentNode.getConceptNid()))) {
            return model.nodeStore.get(TaxonomyModel.getNodeId(nid, parentNode.getConceptNid()));
        }
        
        ConceptVersionBI nodeConcept = model.ts.getConceptVersion(nid);
        TaxonomyNode node = makeNodeFromScratch(nodeConcept, parentNode);
        
        parentNode.addChild(node);
        
        return node;
    }
    
    public TaxonomyNode makeNode(ConceptVersionBI nodeConcept, int parentCnid, TaxonomyNode parentNode)
            throws ContradictionException, IOException {
        if (model.ts.getPossibleChildren(nodeConcept.getNid()).length == 0) {
            boolean multiParent = false;
            
            for (RelationshipVersionBI isaRel : nodeConcept.getRelationshipsOutgoingActiveIsa()) {
                if (isaRel == null || parentNode == null) {
                    continue;
                } else if (isaRel.getTargetNid() != parentNode.getConceptNid()) {
                    multiParent = true;
                    
                    break;
                }
            }
            
            if (multiParent) {
                LeafNodeMultiParent node = new LeafNodeMultiParent(nodeConcept.getNid(), parentCnid,
                        parentNode.nodeId);
                
                renderer.setupTaxonomyNode(node, nodeConcept);
                model.nodeStore.add(node);
                
                return node;
            } else {
                LeafNode node = new LeafNode(nodeConcept.getNid(), parentCnid, parentNode.nodeId);
                
                renderer.setupTaxonomyNode(node, nodeConcept);
                model.nodeStore.add(node);
                
                return node;
            }
        }
        
        InternalNodeMultiParent node = new InternalNodeMultiParent(nodeConcept.getNid(), parentCnid,
                parentNode.nodeId, nodeComparator);
        
        node.setIsLeaf(nodeConcept.isLeaf());
        setExtraParents(nodeConcept, node);
        renderer.setupTaxonomyNode(node, nodeConcept);
        model.nodeStore.add(node);

        // makeChildNodes(node);
        return node;
    }
    
    private TaxonomyNode makeNodeFromScratch(ConceptVersionBI nodeConcept, TaxonomyNode parentNode)
            throws Exception, IOException, ContradictionException {
        Long nodeId = TaxonomyModel.getNodeId(nodeConcept.getNid(), parentNode.getConceptNid());
        TaxonomyNode existingNode = model.nodeStore.get(nodeId);
        
        if (existingNode != null) {
            return existingNode;
        }
        
        return makeNode(nodeConcept, parentNode.getConceptNid(), parentNode);
    }
    
    public void removeDescendents(TaxonomyNode parent) {
        if (!parent.isLeaf()) {
            boolean noCycle = true;
            ((InternalNode) parent).setChildrenAreSet(false);
            for (Long nodeId : parent.getChildren()) {
                if(childWorkerMap.containsKey(nodeId)){
                    MakeChildNodesWorker worker = childWorkerMap.remove(nodeId);
                    if (worker != null) {
                        worker.canceled = true;
                    }
                }
                
                TaxonomyNode childNode = model.nodeStore.get(nodeId);
                
                if(childNode.getChildren().contains(parent.nodeId)){
                    noCycle = false;
                }
                if(noCycle){
                    if ((childNode != null) && (childNode.nodeId != parent.nodeId)) {
                        removeDescendents(childNode);
                        model.nodeStore.remove(nodeId);
                    }
                }
            }
            if(!noCycle){
                TreePath pathToNode = NodePath.getTreePath(model, parent);
                if(!model.getNodeFactory().getTree().isExpanded(pathToNode)){
                    ((InternalNode) parent).clearChildren();
                }
            }
            if(noCycle){
                ((InternalNode) parent).clearChildren();
            }
        }
    }
    
    void unLink() {
        tree.removeTreeExpansionListener(expansionListener);
        tree.removeTreeWillExpandListener(expansionListener);
    }

    //~--- get methods ---------------------------------------------------------
    public NodeComparator getNodeComparator() {
        return nodeComparator;
    }
    
    public JTree getTree() {
        return tree;
    }

    //~--- set methods ---------------------------------------------------------
    private void setExtraParents(ConceptVersionBI nodeConcept, TaxonomyNode node)
            throws ContradictionException, IOException {
        if (node.getParentNid() != Integer.MAX_VALUE) {    // test if root
            for (ConceptVersionBI parent : nodeConcept.getRelationshipsOutgoingTargetConceptsActiveIsa()) {
                if (parent.getNid() != node.getParentNid()) {
                    node.setHasExtraParents(true);
                    
                    return;
                }
            }
        }
    }

    private Map<TreePath, List<SwingWorker>> expansionWorkers = new HashMap<TreePath, List<SwingWorker>>();
    public void addNodeExpansionWorker(TreePath pathToExpand, SwingWorker nextSegmentWorker) {
        if (!expansionWorkers.containsKey(pathToExpand)) {
            expansionWorkers.put(pathToExpand, new ArrayList<SwingWorker>());
        }
        expansionWorkers.get(pathToExpand).add(nextSegmentWorker);
    }

    //~--- inner classes -------------------------------------------------------
    private class ChildFinder implements ProcessUnfetchedConceptDataBI, Callable<Object> {
        
        LinkedBlockingQueue<TaxonomyNode> childNodes = new LinkedBlockingQueue<TaxonomyNode>();
        ChildNodeFilterBI childFilter;
        IdentifierSet dataSet;
        ConceptVersionBI parent;
        TaxonomyNode parentNode;
        CancelableBI worker;

        //~--- constructors -----------------------------------------------------
        public ChildFinder(ConceptVersionBI parent, TaxonomyNode parentNode, CancelableBI worker,
                ChildNodeFilterBI childFilter)
                throws IOException, ContradictionException {
            this.worker = worker;
            this.parent = parent;
            this.parentNode = parentNode;
            this.dataSet = (IdentifierSet) Terms.get().getEmptyIdSet();
            this.childFilter = childFilter;
            
            for (int cnid : Ts.get().getPossibleChildren(parentNode.getConceptNid(), model.ts.getViewCoordinate())) {
                this.dataSet.setMember(cnid);
            }
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public Object call() throws Exception {
            try {
                if (dataSet.cardinality() < 24) {
                    I_IterateIds itr = dataSet.iterator();
                    
                    while (itr.next()) {
                        processPossibleChild(model.ts.getConceptVersion(itr.nid()));
                    }
                } else {
                    Ts.get().iterateConceptDataInParallel(this);
                }
                
                return null;
            } finally {
                dataSet = null;
                childNodes.put(parentNode);
            }
        }
        
        @Override
        public boolean continueWork() {
            return !worker.isCanceled();
        }
        
        private void processPossibleChild(ConceptVersionBI possibleChild) throws Exception {
            if (possibleChild.isChildOf(parent)) {
                if ((childFilter == null) || childFilter.pass(parent, possibleChild)) {
                    TaxonomyNode childNode = makeNodeFromScratch(possibleChild, parentNode);
                    if (parentNode.isLeaf()) {
                        TaxonomyNode oldParent = parentNode;
                        parentNode = new InternalNode(parentNode.getConceptNid(), parentNode.getParentNid(),
                                parentNode.parentNodeId, nodeComparator);
                        model.nodeStore.remove(oldParent.getNodeId());
                        model.nodeStore.add(parentNode);
                        renderer.setupTaxonomyNode(parentNode, parent);
                    }
                    if (parentNode.addChild(childNode) && childNode != null) {
                        childNodes.put(childNode);
                    }
                }
            }
        }
        
        @Override
        public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
            if (dataSet.isMember(cNid)) {
                ConceptVersionBI possibleChild = fetcher.fetch(model.ts.getViewCoordinate());
                
                processPossibleChild(possibleChild);
            }
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public NidBitSetBI getNidSet() throws IOException {
            return dataSet;
        }
    }
    
    private class CollapseHandler extends SwingWorker<TreePath, Object> {
        
        TreePath path;

        //~--- constructors -----------------------------------------------------
        public CollapseHandler(TreePath path) {
            this.path = path;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        protected TreePath doInBackground() throws Exception {
            TaxonomyNode collapsingNode = (TaxonomyNode) path.getLastPathComponent();
            TaxonomyNode latestCollapsingNode = model.nodeStore.get(collapsingNode.nodeId);
            
            if (latestCollapsingNode == null) {
                latestCollapsingNode = collapsingNode;
            }
            
            for (Long nodeId : latestCollapsingNode.getChildren()) {
                TaxonomyNode childNode = model.nodeStore.get(nodeId);
                
                if ((childNode != null) && !childNode.getChildren().isEmpty()) {
                    removeDescendents(childNode);
                }
            }
            
            return NodePath.getTreePath(model, latestCollapsingNode);
        }
        
        @Override
        protected void done() {
            try {
                model.treeStructureChanged(get());
            } catch (Throwable ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }
    
    private class MakeChildNodesWorker extends SwingWorker<Object, List<TaxonomyNode>>
            implements CancelableBI {
        
        Set<TaxonomyNode> childNodes = new HashSet<TaxonomyNode>();
        boolean canceled = false;
        CountDownLatch latch = new CountDownLatch(1);
        ChildNodeFilterBI childFilter;
        TaxonomyNode parentNode;
        TreePath path;
        List<SwingWorker> endActions;

        //~--- constructors -----------------------------------------------------
        public MakeChildNodesWorker(TaxonomyNode parentNode, ChildNodeFilterBI childFilter, List<SwingWorker> endActions) {
            this.parentNode = parentNode;
            this.childFilter = childFilter;
            this.path = NodePath.getTreePath(model, parentNode);
            this.endActions = endActions;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        protected Object doInBackground() throws Exception {
            try {
                ConceptVersionBI parent = model.ts.getConceptVersion(parentNode.getConceptNid());
                ChildFinder dataFinder = new ChildFinder(parent, parentNode, this, childFilter);
                Future<Object> finderFuture = childFinderExecutors.submit(dataFinder);
                long lastPublish = System.currentTimeMillis();
                ArrayList<TaxonomyNode> nodesToPublish = new ArrayList<TaxonomyNode>();
                
                if (canceled) {
                    return null;
                }
                
                TaxonomyNode childNode = dataFinder.childNodes.take();
                
                while (childNode.getNodeId() != parentNode.nodeId) {
                    nodesToPublish.add(childNode);
                    
                    long time = System.currentTimeMillis();
                    long timeSincePublish = time - lastPublish;
                    
                    if (timeSincePublish > 250) {
                        lastPublish = time;
                        publish(nodesToPublish);
                        nodesToPublish = new ArrayList<TaxonomyNode>(nodesToPublish.size());
                    }
                    
                    if (canceled) {
                        return null;
                    }
                    
                    childNode = dataFinder.childNodes.take();
                }
                
                if (nodesToPublish.size() > 0) {
                    publish(nodesToPublish);
                }
                
                finderFuture.get();
                
                if (parentNode instanceof InternalNode) {
                    ((InternalNode) parentNode).setChildrenAreSet(true);
                }
                
                model.nodeStore.add(parentNode.getFinalNode());
                
                return null;
            } finally {
                childWorkerMap.remove(parentNode.nodeId, this);
                latch.countDown();
            }
        }
        
        @Override
        protected void done() {
            try {
                get();
                if (this.endActions != null) {
                    for (SwingWorker w: this.endActions) {
                        w.execute();
                    }
                }
            } catch (Throwable ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
        
        @Override
        protected void process(List<List<TaxonomyNode>> chunks) {
            if (tree.isExpanded(path)) {
                model.treeStructureChanged(path);
            }
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public boolean isCanceled() {
            return canceled;
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setCanceled(boolean canceled) {
            this.canceled = canceled;
            latch.countDown();
        }
    }
    
    protected class NodeExpansionListener implements TreeWillExpandListener, TreeExpansionListener {
        
        JTree tree;

        //~--- constructors -----------------------------------------------------
        public NodeExpansionListener(JTree tree) {
            this.tree = tree;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public void treeCollapsed(TreeExpansionEvent event) {
            CollapseHandler ch = new CollapseHandler(event.getPath());
            
            FutureHelper.addFuture(taxonomyExecutors.submit(ch));
        }
        
        @Override
        public void treeExpanded(TreeExpansionEvent event) {
            //System.out.println("expanded: " + event.getPath());
        }
        
        @Override
        public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
            // System.out.println("will collapse: " + event.getPath());
        }
        
        
        
        @Override
        public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
            TreePath path = event.getPath();
            List<SwingWorker> workers = expansionWorkers.remove(path);
            TaxonomyNode expansionNode = (TaxonomyNode) path.getLastPathComponent();
            TaxonomyNode latestExpansionNode = model.nodeStore.get(expansionNode.nodeId);
            
            if (!childWorkerMap.containsKey(expansionNode.nodeId)) {
                try {
                    makeChildNodes(latestExpansionNode, workers);
                } catch (Exception ex) {
                    AceLog.getAppLog().alertAndLogException(ex);
                }
            } else {
                MakeChildNodesWorker worker = childWorkerMap.get(expansionNode.nodeId);
                
                if (worker.canceled) {
                    childWorkerMap.remove(expansionNode.nodeId);
                    
                    try {
                        makeChildNodes(latestExpansionNode, workers);
                    } catch (Exception ex) {
                        AceLog.getAppLog().alertAndLogException(ex);
                    }
                }
            }
        }
    }
}
