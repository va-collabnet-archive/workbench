
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.taxonomy.path;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concurrent.future.FutureHelper;
import org.ihtsdo.taxonomy.model.NodeFactory;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.CountDownLatch;
import javax.swing.SwingUtilities;

import javax.swing.SwingWorker;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class PathSegmentExpander extends SwingWorker<Integer, Object> {

    NodeFactory factory;
    int index;
    TreePath path;
    TreePath pathToExpand;

    //~--- constructors --------------------------------------------------------
    public PathSegmentExpander(NodeFactory factory, TreePath path, int index) {
        this.path = path;
        this.index = index;
        this.factory = factory;
        int difference = path.getPathCount() - (index + 1);
        pathToExpand = path;
        for (int i = 0; i < difference; i++) {
            pathToExpand = pathToExpand.getParentPath();
        }

    }

    //~--- methods -------------------------------------------------------------
    @Override
    protected Integer doInBackground() throws Exception {
        TaxonomyNode nodeToExpand = (TaxonomyNode) path.getPath()[index];
        CountDownLatch latch = factory.makeChildNodes(nodeToExpand);

        latch.await();

        return index + 1;
    }

    @Override
    protected void done() {
        try {
            final int newIndex = get();
            if (newIndex < path.getPathCount()) {
                NextSegmentWorker worker = new NextSegmentWorker();
                factory.addNodeExpansionWorker(pathToExpand, worker);
                factory.getTree().expandPath(pathToExpand);
                worker.execute();
            } else {
                int row = factory.getTree().getRowForPath(path);

                try {
                    factory.getTree().scrollRowToVisible(row);
                    factory.getTree().expandPath(path.getParentPath());
                    factory.getTree().setSelectionPath(path);
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            factory.getTree().scrollPathToVisible(path);
                        }
                    });
                } catch (NullPointerException e) {
                    AceLog.getAppLog().info("Expander: " + e.getLocalizedMessage());
                }
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private class NextSegmentWorker extends SwingWorker<Object, Object> {

        @Override
        protected void done() {
            super.done();
            factory.getTree().scrollPathToVisible(pathToExpand);
            PathSegmentExpander nextSegmentExpander = new PathSegmentExpander(factory, path, index + 1);
            FutureHelper.addFuture(NodeFactory.pathExpanderExecutors.submit(nextSegmentExpander));
        }

        @Override
        protected Object doInBackground() throws Exception {
            return null;
        }
    }
}
