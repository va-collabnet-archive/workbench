
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_IterateIds;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.taxonomy.model.NodePath;
import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.*;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;
import org.ihtsdo.taxonomy.nodes.InternalNodeMultiParent;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;

/**
 *
 * @author kec
 */
public class NodeUpdator extends SwingWorker<Object, PublishRecord> implements ProcessUnfetchedConceptDataBI {
   private ConcurrentHashMap<Integer, ConcurrentSkipListSet<UpdateNodeBI>> nodesToChange =
      new ConcurrentHashMap<Integer, ConcurrentSkipListSet<UpdateNodeBI>>();
   private final HashSet<Integer>     changedConcepts                   = new HashSet<Integer>();
   private final HashSet<Integer>     referencedConceptsOfChangedRefexs = new HashSet<Integer>();
   private AtomicInteger              updateNodeId                      = new AtomicInteger();
   private final IdentifierSet        conceptsToRetrieve;
   private CountDownLatch             latch;
   private final TaxonomyModel        model;
   private final HashSet<Integer>     noTaxonomyChange;
   private final TaxonomyNodeRenderer renderer;
   private long                       sequence;
   private final TerminologyStoreDI   ts;
   private final ViewCoordinate       vc;
   private String helperName;

   //~--- constructors --------------------------------------------------------

   public NodeUpdator(TaxonomyModel model, long sequence, Set<Integer> originsOfChangedRels,
                      Set<Integer> destinationsOfChangedRels,
                      Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents,
                      TaxonomyNodeRenderer renderer,
                      String helperName)
           throws IOException, ContradictionException {
      this.ts                 = Ts.get();
      this.model              = model;
      this.sequence           = sequence;
      this.conceptsToRetrieve = (IdentifierSet) ts.getEmptyNidSet();
      this.renderer           = renderer;
      this.vc                 = model.getTs().getViewCoordinate();
      this.helperName = helperName;
      
      for (Integer nid : changedComponents) {
         int cNid = ts.getConceptNidForNid(nid);

         this.changedConcepts.add(cNid);
                }

      for (Integer nid : referencedComponentsOfChangedRefexs) {
         int cNid = ts.getConceptNidForNid(nid);

         this.referencedConceptsOfChangedRefexs.add(cNid);
      }

      noTaxonomyChange = new HashSet(changedConcepts);
      noTaxonomyChange.addAll(referencedConceptsOfChangedRefexs);
      noTaxonomyChange.removeAll(destinationsOfChangedRels);
      noTaxonomyChange.removeAll(originsOfChangedRels);

      //
      for (Integer cNid : originsOfChangedRels) {
         this.conceptsToRetrieve.setMember(cNid);
      }

      for (Integer cNid : destinationsOfChangedRels) {
         this.conceptsToRetrieve.setMember(cNid);
      }

      HashSet<Integer> originsAndDestinationsChanged = new HashSet<Integer>(originsOfChangedRels);

      originsAndDestinationsChanged.retainAll(destinationsOfChangedRels);
      originsOfChangedRels.removeAll(originsAndDestinationsChanged);
      destinationsOfChangedRels.removeAll(originsAndDestinationsChanged);

      for (int cNid : noTaxonomyChange) {
         Collection<Long> nodeIds = model.getNodeStore().getNodeIdsForConcept(cNid);

         if (nodeIds != null) {
            for (Long nodeId : nodeIds) {
               TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

               if (currentNode != null && cNid != Integer.MAX_VALUE) {
                  if (!nodesToChange.containsKey(cNid)) {
                     nodesToChange.put(cNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                  }

                  this.conceptsToRetrieve.setMember(cNid);
                  nodesToChange.get(cNid).add(new NoTaxonomyChangeNodeUpdate(currentNode));
               }
            }
         }
      }

      for (int cNid : originsOfChangedRels) {
         Collection<Long> nodeIds = model.getNodeStore().getNodeIdsForConcept(cNid);

         if (nodeIds != null) {
            for (Long nodeId : nodeIds) {
               TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

               if (currentNode != null) {
                  ParentChangeNodeUpdate pcnu = new ParentChangeNodeUpdate(currentNode);

                  this.conceptsToRetrieve.setMember(cNid);

                  if (!nodesToChange.containsKey(cNid)) {
                     nodesToChange.put(cNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                  }

                  nodesToChange.get(cNid).add(pcnu);
               }
            }
         }
      }

      for (int cNid : destinationsOfChangedRels) {
         Collection<Long> nodeIds = model.getNodeStore().getNodeIdsForConcept(cNid);

         if (nodeIds != null) {
            for (Long nodeId : nodeIds) {
               TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

               if (currentNode != null) {
                  this.conceptsToRetrieve.setMember(cNid);

                  int[]                          possibleChildren     = ts.getPossibleChildren(cNid,
                                                                           renderer.getViewCoordinate());
                  ConcurrentSkipListSet<Integer> possibleChildrenCSLS = new ConcurrentSkipListSet<Integer>();

                  for (int pcNid : possibleChildren) {
                     possibleChildrenCSLS.add(pcNid);
                  }

                  ChildChangeNodeUpdate ccnu = new ChildChangeNodeUpdate(currentNode, possibleChildrenCSLS);

                  if (!nodesToChange.containsKey(cNid)) {
                     nodesToChange.put(cNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                  }

                  nodesToChange.get(cNid).add(ccnu);

                  for (int pcNid : possibleChildren) {
                     if (!nodesToChange.containsKey(pcNid)) {
                        nodesToChange.put(pcNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                     }

                     this.conceptsToRetrieve.setMember(pcNid);
                     nodesToChange.get(pcNid).add(ccnu);
                  }
               }
            }
         }
      }

      for (int cNid : originsAndDestinationsChanged) {
         Collection<Long> nodeIds = model.getNodeStore().getNodeIdsForConcept(cNid);

         if (nodeIds != null) {
            for (Long nodeId : nodeIds) {
               TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

               if (currentNode != null) {
                  this.conceptsToRetrieve.setMember(cNid);

                  int[]                          possibleChildren     = ts.getPossibleChildren(cNid,
                                                                           renderer.getViewCoordinate());
                  ConcurrentSkipListSet<Integer> possibleChildrenCSLS = new ConcurrentSkipListSet<>();

                  for (int pcNid : possibleChildren) {
                     possibleChildrenCSLS.add(pcNid);
                  }

                  ParentAndChildChangeNodeUpdate paccnu = new ParentAndChildChangeNodeUpdate(currentNode,
                                                             possibleChildrenCSLS);

                  if (!nodesToChange.containsKey(cNid)) {
                     nodesToChange.put(cNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                  }

                  nodesToChange.get(cNid).add(paccnu);

                  for (int pcNid : possibleChildren) {
                     if (!nodesToChange.containsKey(pcNid)) {
                        nodesToChange.put(pcNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                     }

                     nodesToChange.get(cNid).add(paccnu);
                  }
               }
            }
         }
      }

      latch = new CountDownLatch(conceptsToRetrieve.size());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public boolean continueWork() {
      return true;
   }

   @Override
   protected Object doInBackground() throws Exception {
      if (conceptsToRetrieve.size() > 100) {
         Ts.get().iterateConceptDataInParallel(this);
      } else {
         I_IterateIds          cnidItr = conceptsToRetrieve.iterator();
         TerminologySnapshotDI tSnap   = ts.getSnapshot(vc);

         while (cnidItr.next()) {
            processConcept(tSnap.getConceptForNid(cnidItr.nid()));
         }
      }

      latch.await();

      return true;
   }

   @Override
   protected void done() {
      try {
         get();
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }

   @Override
   protected void process(List<PublishRecord> chunks) {
      for (PublishRecord rec : chunks) {
         switch (rec.updateType) {
         case CHILD_CHANGE :
            model.treeStructureChanged(NodePath.getTreePath(model, rec.publishedNode));

            break;

         case EXTRA_PARENT_CHANGE :
            model.treeStructureChanged(NodePath.getTreePath(model,
                    model.getNodeStore().get(rec.publishedNode.getNodeId())));

            break;

         case NO_TAXONOMY_CHANGE :
            model.valueForPathChanged(NodePath.getTreePath(model, rec.publishedNode), rec.publishedNode);

            break;

         case EXTRA_PARENT_AND_CHILD_CHANGE :
            model.treeStructureChanged(NodePath.getTreePath(model, rec.publishedNode));
            model.treeStructureChanged(NodePath.getTreePath(model,
                    model.getNodeStore().get(rec.publishedNode.getNodeId())));

            break;

         default :
            throw new UnsupportedOperationException("Can't handle: " + rec.updateType);
         }
      }
   }

   private void processConcept(ConceptVersionBI concept) throws Exception {
      int cNid = concept.getNid();

      if (nodesToChange.containsKey(cNid)) {
         for (UpdateNodeBI un : nodesToChange.get(cNid)) {
            un.update(concept);
         }
      }

      latch.countDown();
   }

   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
      if (conceptsToRetrieve.isMember(cNid)) {
         processConcept(fetcher.fetch(vc));
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public NidBitSetBI getNidSet() throws IOException {
      return conceptsToRetrieve;
   }

   //~--- inner interfaces ----------------------------------------------------

   private static interface UpdateNodeBI {
      public void update(ConceptVersionBI cv);
   }


   //~--- inner classes -------------------------------------------------------

   private class ChildChangeNodeUpdate extends UpdateNode implements UpdateNodeBI {
      TaxonomyNode                        newNode  = null;
      ConcurrentSkipListSet<TaxonomyNode> children = new ConcurrentSkipListSet<>();
      TaxonomyNode                        currentNode;
      ConcurrentSkipListSet<Integer>      possibleChildren;

      //~--- constructors -----------------------------------------------------

      public ChildChangeNodeUpdate(TaxonomyNode currentNode,
                                   ConcurrentSkipListSet<Integer> possibleChildren) {
         this.currentNode      = currentNode;
         this.possibleChildren = possibleChildren;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void update(ConceptVersionBI cv) {
         try {
            if (cv.getNid() == currentNode.getConceptNid()) {
               newNode = model.getNodeFactory().makeNode(cv, currentNode.getParentNid(),
                       model.getNodeStore().get(currentNode.parentNodeId));
            } else {
               for (RelationshipVersionBI rel : cv.getRelationshipsOutgoingActiveIsa()) {
                  if (rel.getTargetNid() == currentNode.getConceptNid()) {
                     TaxonomyNode childNode = model.getNodeFactory().makeNode(cv, currentNode.getConceptNid(),
                                                 currentNode);

                     children.add(childNode);

                     break;
                  }
               }

               possibleChildren.remove(cv.getNid());
            }

            if (possibleChildren.isEmpty() && (newNode != null)) {
               for (TaxonomyNode child : children) {
                  newNode.addChild(child);
               }
               PublishRecord pr = new PublishRecord(newNode, PublishRecord.UpdateType.CHILD_CHANGE);

               publish(pr);
            }
         } catch (   ContradictionException | IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }


   private class NoTaxonomyChangeNodeUpdate extends UpdateNode implements UpdateNodeBI {
      TaxonomyNode currentNode;

      //~--- constructors -----------------------------------------------------

      public NoTaxonomyChangeNodeUpdate(TaxonomyNode currentNode) {
         this.currentNode = currentNode;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void update(ConceptVersionBI cv) {
         try {
            renderer.setupTaxonomyNode(currentNode, cv);

            PublishRecord pr = new PublishRecord(currentNode, PublishRecord.UpdateType.NO_TAXONOMY_CHANGE);

            publish(pr);
         } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }


   private class ParentAndChildChangeNodeUpdate extends UpdateNode implements UpdateNodeBI {
     TaxonomyNode                        newNode  = null;
      ConcurrentSkipListSet<TaxonomyNode> children = new ConcurrentSkipListSet<>();
      TaxonomyNode                        currentNode;
      ConcurrentSkipListSet<Integer>      possibleChildren;

      //~--- constructors -----------------------------------------------------

      public ParentAndChildChangeNodeUpdate(TaxonomyNode currentNode,
              ConcurrentSkipListSet<Integer> possibleChildren) {
         this.currentNode      = currentNode;
         this.possibleChildren = possibleChildren;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void update(ConceptVersionBI cv) {
         try {
            if (cv.getNid() == currentNode.getConceptNid()) {
               newNode = model.getNodeFactory().makeNode(cv, currentNode.getParentNid(),
                       model.getNodeStore().get(currentNode.parentNodeId));
            } else {
               for (RelationshipVersionBI rel : cv.getRelationshipsOutgoingActiveIsa()) {
                  if (rel.getTargetNid() == currentNode.getConceptNid()) {
                     TaxonomyNode childNode = model.getNodeFactory().makeNode(cv, currentNode.getConceptNid(),
                                                 currentNode);

                     currentNode.addChild(childNode);

                     break;
                  }
               }

               possibleChildren.remove(cv.getNid());
            }

            if (possibleChildren.isEmpty() && (newNode != null)) {
               for (TaxonomyNode child : children) {
                  newNode.addChild(child);
               }
               PublishRecord pr = new PublishRecord(newNode, PublishRecord.UpdateType.EXTRA_PARENT_AND_CHILD_CHANGE);

               publish(pr);
            }
         } catch (   ContradictionException | IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }


   private class ParentChangeNodeUpdate extends UpdateNode implements UpdateNodeBI {
      TaxonomyNode currentNode;

      //~--- constructors -----------------------------------------------------

      public ParentChangeNodeUpdate(TaxonomyNode currentNode) {
         this.currentNode = currentNode;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void update(ConceptVersionBI cv) {
         try {
            TaxonomyNode parentNode = model.getNodeStore().nodeMap.get(currentNode.parentNodeId);
            
            boolean cycleExists = false;
            for(Long nodeId : parentNode.getChildren()){
                if(nodeId.equals(currentNode.getNodeId()) && nodesToChange.containsKey(parentNode.getConceptNid())){
                     TerminologySnapshotDI tSnap   = ts.getSnapshot(vc);
                     processConcept(tSnap.getConceptForNid(parentNode.getConceptNid()));
                     cycleExists = true;
                }
            }
            if(parentNode.getChildren().isEmpty()){
                cycleExists = true;
            }
            parentNode = model.getNodeStore().nodeMap.get(currentNode.parentNodeId);
            Collection<Long> children = parentNode.getChildren();
            if(cycleExists){
                if(children.isEmpty()){
                    model.getNodeStore().remove(currentNode.nodeId);
                }else{
                    TaxonomyNode newNode = model.getNodeFactory().makeNode(cv, currentNode.getParentNid(),
                                      model.getNodeStore().get(currentNode.parentNodeId));
                    renderer.setupTaxonomyNode(newNode, cv);
                    TreePath pathToNode = NodePath.getTreePath(model, currentNode);
                    if(model.getNodeFactory().getTree().isExpanded(pathToNode)){
                        if (newNode instanceof InternalNodeMultiParent) {
                            InternalNodeMultiParent mp = (InternalNodeMultiParent) newNode;
                            model.getNodeFactory().makeChildNodes(mp);
                        }
                    }
                    PublishRecord pr = new PublishRecord(newNode, PublishRecord.UpdateType.EXTRA_PARENT_CHANGE);

                    publish(pr);
                }
            }else{
                TaxonomyNode newNode = model.getNodeFactory().makeNode(cv, currentNode.getParentNid(),
                                      model.getNodeStore().get(currentNode.parentNodeId));
                renderer.setupTaxonomyNode(newNode, cv);
                TreePath pathToNode = NodePath.getTreePath(model, currentNode);
                if(model.getNodeFactory().getTree().isExpanded(pathToNode)){
                     if (newNode instanceof InternalNodeMultiParent) {
                        InternalNodeMultiParent mp = (InternalNodeMultiParent) newNode;
                        model.getNodeFactory().makeChildNodes(mp);
                     }
                }
               
                PublishRecord pr = new PublishRecord(newNode, PublishRecord.UpdateType.EXTRA_PARENT_CHANGE);

                publish(pr);
            }
           
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }


   private abstract class UpdateNode implements Comparable<UpdateNode> {
      private int updateNodeId = NodeUpdator.this.updateNodeId.getAndIncrement();

      //~--- methods ----------------------------------------------------------

      @Override
      public int compareTo(UpdateNode o) {
         return this.updateNodeId - o.updateNodeId;
      }
   }
}
