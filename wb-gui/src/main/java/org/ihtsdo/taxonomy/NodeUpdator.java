
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.SwingWorker;
import org.dwfa.ace.api.I_IterateIds;

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

   //~--- constructors --------------------------------------------------------

   public NodeUpdator(TaxonomyModel model, long sequence, Set<Integer> originsOfChangedRels,
                      Set<Integer> destinationsOfChangedRels,
                      Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents,
                      TaxonomyNodeRenderer renderer)
           throws IOException {
      this.ts                 = Ts.get();
      this.model              = model;
      this.sequence           = sequence;
      this.conceptsToRetrieve = (IdentifierSet) ts.getEmptyNidSet();
      this.renderer           = renderer;
      this.vc                 = renderer.getViewCoordinate();

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
         for (Long nodeId : model.getNodeStore().getNodeIdsForConcept(cNid)) {
            TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

            if (currentNode != null) {
               if (!nodesToChange.containsKey(cNid)) {
                  nodesToChange.put(cNid, new ConcurrentSkipListSet<UpdateNodeBI>());
               }

               this.conceptsToRetrieve.setMember(cNid);
               nodesToChange.get(cNid).add(new NoTaxonomyChangeNodeUpdate(currentNode));
            }
         }
      }

      for (int cNid : originsOfChangedRels) {
         for (Long nodeId : model.getNodeStore().getNodeIdsForConcept(cNid)) {
            TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

            if (currentNode != null) {
               ParentChangeNodeUpdate pcnu = new ParentChangeNodeUpdate();

               this.conceptsToRetrieve.setMember(cNid);

               if (!nodesToChange.containsKey(cNid)) {
                  nodesToChange.put(cNid, new ConcurrentSkipListSet<UpdateNodeBI>());
               }

               nodesToChange.get(cNid).add(pcnu);
            }
         }
      }

      for (int cNid : destinationsOfChangedRels) {
         for (Long nodeId : model.getNodeStore().getNodeIdsForConcept(cNid)) {
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

               for (int pcNid : possibleChildren) {
                  if (!nodesToChange.containsKey(pcNid)) {
                     nodesToChange.put(pcNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                  }

                  nodesToChange.get(cNid).add(ccnu);
               }
            }
         }
      }

      for (int cNid : originsAndDestinationsChanged) {
         for (Long nodeId : model.getNodeStore().getNodeIdsForConcept(cNid)) {
            TaxonomyNode currentNode = model.getNodeStore().nodeMap.get(nodeId);

            if (currentNode != null) {
               this.conceptsToRetrieve.setMember(cNid);

               int[]                          possibleChildren     = ts.getPossibleChildren(cNid,
                                                                        renderer.getViewCoordinate());
               ConcurrentSkipListSet<Integer> possibleChildrenCSLS = new ConcurrentSkipListSet<Integer>();

               for (int pcNid : possibleChildren) {
                  possibleChildrenCSLS.add(pcNid);
               }

               ParentAndChildChangeNodeUpdate paccnu = new ParentAndChildChangeNodeUpdate();

               for (int pcNid : possibleChildren) {
                  if (!nodesToChange.containsKey(pcNid)) {
                     nodesToChange.put(pcNid, new ConcurrentSkipListSet<UpdateNodeBI>());
                  }

                  nodesToChange.get(cNid).add(paccnu);
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
          I_IterateIds cnidItr = conceptsToRetrieve.iterator();
          TerminologySnapshotDI tSnap = ts.getSnapshot(vc);
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

   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
      if (conceptsToRetrieve.isMember(cNid)) {
            processConcept(fetcher.fetch(vc));
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
      TaxonomyNode                   currentNode;
      ConcurrentSkipListSet<Integer> possibleChildren;

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
            if (cv.getNid() == currentNode.getCnid()) {
               renderer.setupTaxonomyNode(currentNode, cv);

               // publish node somehow...
            } else {
               for (RelationshipVersionBI rel : cv.getRelsOutgoingActiveIsa()) {
                  if (rel.getDestinationNid() == currentNode.getCnid()) {
                     TaxonomyNode childNode = model.getNodeFactory().makeNode(cv, currentNode.getCnid(),
                                                 currentNode);

                     currentNode.addChild(childNode);

                     break;
                  }
               }

               possibleChildren.remove(cv.getNid());

               if (possibleChildren.isEmpty()) {

                  // publish node somehow...
               }
            }
         } catch (ContradictionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         } catch (IOException ex) {
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
      @Override
      public void update(ConceptVersionBI cv) {
         throw new UnsupportedOperationException("Not supported yet.");
      }
   }


   private class ParentChangeNodeUpdate extends UpdateNode implements UpdateNodeBI {
      @Override
      public void update(ConceptVersionBI cv) {
         throw new UnsupportedOperationException("Not supported yet.");
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
