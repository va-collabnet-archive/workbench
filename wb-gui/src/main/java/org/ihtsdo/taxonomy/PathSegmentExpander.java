
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.log.AceLog;

import org.ihtsdo.taxonomy.nodes.TaxonomyNode;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;
import org.dwfa.ace.ACE;

/**
 *
 * @author kec
 */
public class PathSegmentExpander extends SwingWorker<Integer, Object> {
   NodeFactory factory;
   int         index;
   TreePath    path;

   //~--- constructors --------------------------------------------------------

   public PathSegmentExpander(NodeFactory factory, TreePath path, int index) {
      this.path    = path;
      this.index   = index;
      this.factory = factory;
   }

   //~--- methods -------------------------------------------------------------

   @Override
   protected Integer doInBackground() throws Exception {
      TaxonomyNode   nodeToExpand = (TaxonomyNode) path.getPath()[index];
      CountDownLatch latch        = factory.makeChildNodes(nodeToExpand);

      latch.await();

      return index + 1;
   }

   @Override
   protected void done() {
      try {
         int newIndex = get();

         if (newIndex < path.getPathCount()) {
            PathSegmentExpander nextSegmentExpander = new PathSegmentExpander(factory, path, newIndex);
            ACE.threadPool.submit(nextSegmentExpander);
            factory.tree.expandPath(path.getParentPath());
         } else {
            int row = factory.tree.getRowForPath(path);

            row = Math.min(row + 15, factory.tree.getRowCount());
            factory.tree.scrollRowToVisible(row);
            factory.tree.expandPath(path.getParentPath());
            factory.tree.setSelectionPath(path);
            factory.tree.scrollPathToVisible(path);
         }
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }
}
