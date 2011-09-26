/**
 * Copyright (c) 2011 International Health Terminology Standards Development
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



package org.ihtsdo.tree;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.api.concept.ConceptVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultTreeModel;

/**
 *
 * @author kec
 */
public class ExpandTaxonomyNodeWorker extends SwingWorker<Long, TaxonomyNode> {
   private static int     workerCount   = 0;
   private static boolean logTimingInfo = false;

   //~--- fields --------------------------------------------------------------

   private String                   progressMessageLower = "counting ";
   private int                      workerId             = workerCount++;
   private String                   workerIdStr          = " [" + workerId + "]";
   private String                   progressMessageUpper = "Expanding node " + workerIdStr;
   private boolean                  continueWork         = true;
   private boolean                  canceled             = false;
   private CountDownLatch           completeLatch;
   private ConceptVersionBI         cv;
   private DefaultTreeModel         model;
   private TaxonomyNode             node;
   private ExpandTaxonomyNodeWorker parentWorker;
   private TaxonomyNodeRenderer     renderer;

   //~--- constructors --------------------------------------------------------

   public ExpandTaxonomyNodeWorker(ConceptVersionBI cv, DefaultTreeModel model, TaxonomyNode node,
                                   ExpandTaxonomyNodeWorker parentWorker, TaxonomyNodeRenderer renderer) {
      this.cv           = cv;
      this.model        = model;
      this.node         = node;
      this.parentWorker = parentWorker;
      this.renderer     = renderer;
   }

   //~--- methods -------------------------------------------------------------

   /**
    *
    * @return elapsed time in ms.
    * @throws Exception
    */
   @Override
   protected Long doInBackground() throws Exception {
      long startTime = System.currentTimeMillis();

      renderer.setupTaxonomyNode(node, cv);

      if (parentWorker != null) {
         parentWorker.publish(node);
         parentWorker.completeLatch.countDown();
      }

      if (node.isExpanded()) {
         Collection<? extends ConceptVersionBI> children = cv.getRelsIncomingOriginsActiveIsa();
         TaxonomyNode childNode = null;
         // TODO make this work. 
         for (ConceptVersionBI child : children) {
            ExpandTaxonomyNodeWorker worker = new ExpandTaxonomyNodeWorker(child, 
                    this.model, childNode, this, this.renderer);         
         
         }

         completeLatch = new CountDownLatch(children.size());
         completeLatch.await();
      }

      return System.currentTimeMillis() - startTime;
   }

   @Override
   protected void done() {
      super.done();
   }

   @Override
   protected void process(List<TaxonomyNode> childen) {
      super.process(childen);
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
         @Override
         public void run() {
            node.removeAllChildren();
            model.setAsksAllowsChildren(false);
            model.nodeStructureChanged(node);
            model.setAsksAllowsChildren(true);
         }
      });
   }
}
