
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy.path;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.CountDownLatch;

import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

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

            FutureHelper.addFuture(NodeFactory.pathExpanderExecutors.submit(nextSegmentExpander));
            factory.getTree().expandPath(path.getParentPath());
         } else {
            int row = factory.getTree().getRowForPath(path);

            row = Math.min(row + 15, factory.getTree().getRowCount());
            factory.getTree().scrollRowToVisible(row);
            factory.getTree().expandPath(path.getParentPath());
            factory.getTree().setSelectionPath(path);
            factory.getTree().scrollPathToVisible(path);
         }
      } catch (Exception ex) {
         AceLog.getAppLog().alertAndLogException(ex);
      }
   }
}
