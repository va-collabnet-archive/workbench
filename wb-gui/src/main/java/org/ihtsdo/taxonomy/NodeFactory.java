
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.helper.thread.NamedThreadFactory;
import org.ihtsdo.taxonomy.nodes.InternalNode;
import org.ihtsdo.taxonomy.nodes.InternalNodeMultiParent;
import org.ihtsdo.taxonomy.nodes.LeafNode;
import org.ihtsdo.taxonomy.nodes.LeafNodeMultiParent;
import org.ihtsdo.taxonomy.nodes.NodeComparator;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class NodeFactory {
   public static ExecutorService taxonomyExecutors =
      Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() + 1),
                                   new NamedThreadFactory(new ThreadGroup("NodeFactory "), "Taxonomy "));

   //~--- fields --------------------------------------------------------------

   private ConcurrentSkipListMap<Long, MakeChildNodesWorker> childWorkerMap = new ConcurrentSkipListMap<Long,
                                                                                 MakeChildNodesWorker>();
   private TaxonomyModel        model;
   private NodeComparator       nodeComparator;
   private TaxonomyNodeRenderer renderer;
   protected JTree              tree;

   //~--- constructors --------------------------------------------------------

   public NodeFactory(TaxonomyModel model, TaxonomyNodeRenderer renderer, JTree tree) {
      this.model          = model;
      this.renderer       = renderer;
      this.tree           = tree;
      this.nodeComparator = new NodeComparator(model.nodeMap);
   }

   //~--- methods -------------------------------------------------------------

   public void addNodeExpansionListener(JTree tree) {
      NodeExpansionListener l = new NodeExpansionListener(tree);

      tree.addTreeExpansionListener(l);
      tree.addTreeWillExpandListener(l);
   }

   public void collapseNode(TaxonomyNode node) {
      CollapseHandler ch = new CollapseHandler(NodePath.getTreePath(model, node));

      taxonomyExecutors.submit(ch);
   }

   public CountDownLatch makeChildNodes(TaxonomyNode parentNode) throws IOException, Exception {
      if (parentNode.isSecondaryParentNode()) {
         return new CountDownLatch(0);
      }

      MakeChildNodesWorker mcnw = childWorkerMap.get(parentNode.nodeId);

      if (mcnw == null) {
         mcnw = new MakeChildNodesWorker(parentNode);
         childWorkerMap.put(parentNode.nodeId, mcnw);
         taxonomyExecutors.submit(mcnw);
      }

      return mcnw.latch;
   }

   public TaxonomyNode makeNode(int nid, TaxonomyNode parentNode) throws IOException, Exception {
      if (model.nodeMap.containsKey(TaxonomyModel.getNodeId(nid, parentNode.getCnid()))) {
         return model.nodeMap.get(TaxonomyModel.getNodeId(nid, parentNode.getCnid()));
      }

      ConceptVersionBI nodeConcept = model.ts.getConceptVersion(nid);
      TaxonomyNode     node        = makeNodeFromScratch(nodeConcept, parentNode);

      parentNode.addChild(node);

      return node;
   }

   private TaxonomyNode makeNodeFromScratch(ConceptVersionBI nodeConcept, TaxonomyNode parentNode)
           throws Exception, IOException, ContraditionException {
      Long         nodeId       = TaxonomyModel.getNodeId(nodeConcept.getNid(), parentNode.getCnid());
      TaxonomyNode existingNode = model.nodeMap.get(nodeId);

      if (existingNode != null) {
         return existingNode;
      }

      if (model.ts.getPossibleChildren(nodeConcept.getNid()).length == 0) {
         boolean multiParent = false;

         for (RelationshipVersionBI isaRel : nodeConcept.getRelsOutgoingActiveIsa()) {
            if (isaRel.getDestinationNid() != parentNode.getCnid()) {
               multiParent = true;

               break;
            }
         }

         if (multiParent) {
            LeafNodeMultiParent node = new LeafNodeMultiParent(nodeConcept.getNid(), parentNode.getCnid(),
                                          parentNode.nodeId);

            renderer.setupTaxonomyNode(node, nodeConcept);
            model.nodeMap.put(node.nodeId, node);

            return node;
         } else {
            LeafNode node = new LeafNode(nodeConcept.getNid(), parentNode.getCnid(), parentNode.nodeId);

            renderer.setupTaxonomyNode(node, nodeConcept);
            model.nodeMap.put(node.nodeId, node);

            return node;
         }
      }

      InternalNodeMultiParent node = new InternalNodeMultiParent(nodeConcept.getNid(), parentNode.getCnid(),
                                        parentNode.nodeId, nodeComparator);

      node.setIsLeaf(nodeConcept.isLeaf());
      setExtraParents(nodeConcept, node);
      renderer.setupTaxonomyNode(node, nodeConcept);
      model.nodeMap.put(node.nodeId, node);

      // makeChildNodes(node);
      return node;
   }

   public void removeDescendents(TaxonomyNode parent) {
      if (!parent.isLeaf()) {
         ((InternalNode) parent).setChildrenAreSet(false);

         for (Long nodeId : parent.getChildren()) {
            MakeChildNodesWorker worker = childWorkerMap.remove(nodeId);

            if (worker != null) {
               worker.canceled = true;
            }

            TaxonomyNode childNode = model.nodeMap.get(nodeId);

            if ((childNode != null) && (childNode.nodeId != parent.nodeId)) {
               removeDescendents(childNode);
               model.nodeMap.remove(nodeId);
            }
         }
         ((InternalNode) parent).clearChildren();
      }
   }

   //~--- get methods ---------------------------------------------------------

   public NodeComparator getNodeComparator() {
      return nodeComparator;
   }

   //~--- set methods ---------------------------------------------------------

   private void setExtraParents(ConceptVersionBI nodeConcept, TaxonomyNode node)
           throws ContraditionException, IOException {
      if (node.getParentNid() != Integer.MAX_VALUE) {    // test if root
         for (ConceptVersionBI parent : nodeConcept.getRelsOutgoingDestinationsActiveIsa()) {
            if (parent.getNid() != node.getParentNid()) {
               node.setHasExtraParents(true);

               return;
            }
         }
      }
   }

   //~--- inner classes -------------------------------------------------------

   private class ChildFinder implements ProcessUnfetchedConceptDataBI, Callable<Object> {
      LinkedBlockingQueue<TaxonomyNode> childNodes = new LinkedBlockingQueue<TaxonomyNode>();
      IdentifierSet                     dataSet;
      ConceptVersionBI                  parent;
      TaxonomyNode                      parentNode;
      CancelableBI                      worker;

      //~--- constructors -----------------------------------------------------

      public ChildFinder(ConceptVersionBI parent, TaxonomyNode parentNode, CancelableBI worker)
              throws IOException {
         this.worker     = worker;
         this.parent     = parent;
         this.parentNode = parentNode;
         dataSet         = new IdentifierSet();

         for (int cnid : Ts.get().getPossibleChildren(parentNode.getCnid(), model.ts.getViewCoordinate())) {
            dataSet.setMember(cnid);
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
            TaxonomyNode childNode = makeNodeFromScratch(possibleChild, parentNode);

            if (parentNode.addChild(childNode)) {
               childNodes.put(childNode);
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
         TaxonomyNode collapsingNode       = (TaxonomyNode) path.getLastPathComponent();
         TaxonomyNode latestCollapsingNode = model.nodeMap.get(collapsingNode.nodeId);

         if (latestCollapsingNode == null) {
            latestCollapsingNode = collapsingNode;
         }

         for (Long nodeId : latestCollapsingNode.getChildren()) {
            TaxonomyNode childNode = model.nodeMap.get(nodeId);

            if ((childNode != null) &&!childNode.getChildren().isEmpty()) {
               removeDescendents(childNode);
            }
         }

         return NodePath.getTreePath(model, latestCollapsingNode);
      }

      @Override
      protected void done() {
         try {
            model.treeStructureChanged(get());
         } catch (InterruptedException ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ExecutionException ex) {
            Logger.getLogger(NodeFactory.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }


   private class MakeChildNodesWorker extends SwingWorker<Object, List<TaxonomyNode>>
           implements CancelableBI {
      Set<TaxonomyNode> childNodes = new HashSet<TaxonomyNode>();
      boolean           canceled   = false;
      CountDownLatch    latch      = new CountDownLatch(1);
      TaxonomyNode      parentNode;
      TreePath          path;

      //~--- constructors -----------------------------------------------------

      public MakeChildNodesWorker(TaxonomyNode parentNode) {
         this.parentNode = parentNode;
         this.path       = NodePath.getTreePath(model, parentNode);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      protected Object doInBackground() throws Exception {
         try {
            ConceptVersionBI        parent         = model.ts.getConceptVersion(parentNode.getCnid());
            ChildFinder             dataFinder     = new ChildFinder(parent, parentNode, this);
            Future<Object>          finderFuture   = taxonomyExecutors.submit(dataFinder);
            long                    lastPublish    = System.currentTimeMillis();
            ArrayList<TaxonomyNode> nodesToPublish = new ArrayList<TaxonomyNode>();

            if (canceled) {
               return null;
            }

            TaxonomyNode childNode = dataFinder.childNodes.take();

            while (childNode.getNodeId() != parentNode.nodeId) {
               nodesToPublish.add(childNode);

               long time             = System.currentTimeMillis();
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
            parentNode.childrenAreSet();
            model.nodeMap.put(parentNode.nodeId, parentNode.getFinalNode());

            return null;
         } finally {
            childWorkerMap.remove(parentNode.nodeId, this);
         }
      }

      @Override
      protected void done() {
         try {
            get();
         } catch (InterruptedException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } catch (ExecutionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } finally {
            childWorkerMap.remove(parentNode.nodeId, this);
            latch.countDown();
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

         taxonomyExecutors.submit(ch);
      }

      @Override
      public void treeExpanded(TreeExpansionEvent event) {

         // System.out.println("expanded: " + event.getPath());
      }

      @Override
      public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

         // System.out.println("will collapse: " + event.getPath());
      }

      @Override
      public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
         TreePath     path                = event.getPath();
         TaxonomyNode expansionNode       = (TaxonomyNode) path.getLastPathComponent();
         TaxonomyNode latestExpansionNode = model.nodeMap.get(expansionNode.nodeId);

         if (!childWorkerMap.containsKey(expansionNode.nodeId)) {
            try {
               makeChildNodes(latestExpansionNode);
            } catch (Exception ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         } else {
            MakeChildNodesWorker worker = childWorkerMap.get(expansionNode.nodeId);

            if (worker.canceled) {
               childWorkerMap.remove(expansionNode.nodeId);

               try {
                  makeChildNodes(latestExpansionNode);
               } catch (Exception ex) {
                  AceLog.getAppLog().alertAndLogException(ex);
               }
            }
         }
      }
   }
}
