
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.path;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.TaxonomyTree;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.taxonomy.model.NodePath;
import org.ihtsdo.taxonomy.model.TaxonomyModel;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.Collection;
import java.util.List;
import org.ihtsdo.tk.Ts;

/**
 *
 * @author kec
 */
public class PathExpander implements Runnable { 
   I_ConfigAceFrame config;
   ConceptVersionBI focus;
   TaxonomyModel    model;
   TaxonomyTree     tree;

   //~--- constructors --------------------------------------------------------

   public PathExpander(TaxonomyTree tree, I_ConfigAceFrame config, ConceptChronicleBI concept)
           throws IOException {
      this.tree   = tree;
      this.model  = (TaxonomyModel) tree.getModel();
      this.config = config;
      this.focus  = model.getTs().getConceptVersion(concept.getNid());
   }

   //~--- methods -------------------------------------------------------------

   @Override
   public void run() {
      try {
         Collection<List<Integer>> nidPaths     = focus.getNidPathsToRoot();
         List<Integer>             shortestPath = null;

         for (List<Integer> nidPath : nidPaths) {
            if (shortestPath == null) {
               shortestPath = nidPath;
            } else {
               if (shortestPath.size() > nidPath.size()) {
                  shortestPath = nidPath;
               }
            }
         }

         TaxonomyNode parent = model.getRoot();

         for (int i = shortestPath.size() - 1; i > -1; i--) {
            TaxonomyNode node = model.getNodeFactory().makeNode(shortestPath.get(i), parent);

            parent = node;
         }

         TaxonomyNode        focusNode = model.getNodeFactory().makeNode(
                 Ts.get().getConceptVersion(model.getTs().getViewCoordinate(),
                 focus.getNid()),
                 parent.getConceptNid(),
                 parent);
         PathSegmentExpander expander  = new PathSegmentExpander(tree.getNodeFactory(),
                                            NodePath.getTreePath(model, focusNode), 1);

         FutureHelper.addFuture(NodeFactory.pathExpanderExecutors.submit(expander));
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }
}
