
/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
 */
package org.ihtsdo.taxonomy;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.dnd.TerminologyTransferHandler;
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.arena.conceptview.ConceptViewRenderer;
import org.ihtsdo.taxonomy.nodes.RootNode;
import org.ihtsdo.taxonomy.nodes.TaxonomyNode;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidList;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;

/**
 *
 * @author kec
 */
public class TaxonomyHelper extends TermChangeListener implements PropertyChangeListener {
   private static ImageIcon statedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/graph_edge.png"));
   private static ImageIcon inferredView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/chrystal_ball.png"));
   private static ImageIcon inferredThenStatedView =
      new ImageIcon(ConceptViewRenderer.class.getResource("/16x16/plain/inferred-then-stated.png"));

   //~--- fields --------------------------------------------------------------

   private I_ConfigAceFrame     aceFrameConfig;
   private ActivityPanel        activity;
   private RelAssertionType     assertionType;
   private TaxonomyModel        model;
   private TaxonomyNodeRenderer renderer;
   private JButton              statedInferredButton;
   private TaxonomyTree         tree;

   //~--- constructors --------------------------------------------------------

   public TaxonomyHelper(I_ConfigAceFrame config) {
      super();
      this.aceFrameConfig = config;
      this.assertionType  = config.getRelAssertionType();
      Ts.get().addTermChangeListener(this);
   }

   //~--- methods -------------------------------------------------------------

   public synchronized void addMouseListener(MouseListener ml) {
      tree.addMouseListener(ml);
   }

   public void addTreeSelectionListener(TreeSelectionListener tsl) {
      tree.addTreeSelectionListener(tsl);
   }

   @Override
   public void changeNotify(long sequence, Set<Integer> changedXrefs, Set<Integer> changedComponents) {
      if (AceLog.getAppLog().isLoggable(Level.INFO)) {
         AceLog.getAppLog().info("Term change. Sequence: " + sequence + " changedXrefs: " + changedXrefs
                                 + " changedComponents: " + changedComponents);
      }
   }

   protected void collapseTree(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TaxonomyNode node = handleCollapse(evt, aceFrameConfig);
   }

   protected void expandTree(TreeExpansionEvent evt) {
      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();
   }

   private TaxonomyNode handleCollapse(TreeExpansionEvent evt, I_ConfigAceFrame aceFrameConfig) {
      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();

      return node;
   }

   @Override
   public void propertyChange(PropertyChangeEvent evt) {
      updateHierarchyView(evt.getPropertyName());
   }

   public void removeTreeSelectionListener(TreeSelectionListener tsl) {
      tree.removeTreeSelectionListener(tsl);
   }

   protected void treeSelectionChanged(TreeSelectionEvent evt) {
      TaxonomyNode node = (TaxonomyNode) evt.getPath().getLastPathComponent();

      if ((node != null) &&!(node instanceof RootNode)) {
         try {
            aceFrameConfig.setHierarchySelection((I_GetConceptData) Ts.get().getConcept(node.getCnid()));
         } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      } else {
         aceFrameConfig.setHierarchySelection(null);
      }
   }

   public void updateHierarchyView(String propChangeName) {
      RootNode root       = model.getRoot();
      int      childCount = root.getChildren().size();

      for (int i = 0; i < childCount; i++) {
         TaxonomyNode       childNode = (TaxonomyNode) model.getChild(root, i);
         TreePath           tp        = new TreePath(NodePath.getTreePath(model, childNode));
         tree.collapseRow(i);
         tree.collapsePath(tp);
      }
   }

   //~--- get methods ---------------------------------------------------------

   public RelAssertionType getAssertionType() {
      return assertionType;
   }

   public JScrollPane getHierarchyPanel() throws IOException, Exception {
      if (tree != null) {
         for (TreeExpansionListener tel : tree.getTreeExpansionListeners()) {
            tree.removeTreeExpansionListener(tel);
         }

         for (TreeSelectionListener tsl : tree.getTreeSelectionListeners()) {
            tree.removeTreeSelectionListener(tsl);
         }

         for (TreeWillExpandListener twel : tree.getTreeWillExpandListeners()) {
            tree.removeTreeWillExpandListener(twel);
         }
      }

      tree     = new TaxonomyTree(aceFrameConfig, this);
      renderer = new TaxonomyNodeRenderer(aceFrameConfig, this);
      model    = new TaxonomyModel(aceFrameConfig.getViewCoordinate(),
                                   new NidList(aceFrameConfig.getRoots().getSetValues()), renderer, tree);
      tree.putClientProperty("JTree.lineStyle", "None");
      tree.setLargeModel(true);

      // tree.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
      tree.setTransferHandler(new TerminologyTransferHandler(tree));

      // tree.setDragEnabled(true);
      ToolTipManager.sharedInstance().registerComponent(tree);
      tree.setCellRenderer(renderer);
      tree.setRootVisible(false);
      tree.setShowsRootHandles(true);
      model.addTreeWillExpandListener(tree);
      tree.addTreeSelectionListener(new TreeSelectionListener() {
         @Override
         public void valueChanged(TreeSelectionEvent evt) {
            treeSelectionChanged(evt);
         }
      });

      JScrollPane treeView = new JScrollPane(tree);

      treeView.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

      // treeView.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
      tree.setScroller(treeView);

      for (int i = 0; i < tree.getRowCount(); i++) {
         TreePath     path = tree.getPathForRow(i);
         TaxonomyNode node = (TaxonomyNode) path.getLastPathComponent();
      }

      statedInferredButton = new JButton(new AbstractAction("", statedView) {
         @Override
         public void actionPerformed(ActionEvent e) {
            switch (assertionType) {
            case INFERRED :
               assertionType = RelAssertionType.INFERRED_THEN_STATED;
               statedInferredButton.setIcon(inferredThenStatedView);
               statedInferredButton.setToolTipText("showing inferred then stated, toggle to show stated...");
               updateHierarchyView("changed from stated to inferred then stated");

               break;

            case INFERRED_THEN_STATED :
               assertionType = RelAssertionType.STATED;
               statedInferredButton.setIcon(statedView);
               statedInferredButton.setToolTipText("showing stated, toggle to show inferred...");
               updateHierarchyView("changed from inferred to stated");

               break;

            case STATED :
               assertionType = RelAssertionType.INFERRED;
               statedInferredButton.setIcon(inferredView);
               statedInferredButton.setToolTipText(
                   "showing inferred, toggle to show inferred then stated...");
               updateHierarchyView("changed from stated to inferred");

               break;
            }
         }
      });

      switch (assertionType) {
      case INFERRED :
         statedInferredButton.setIcon(inferredView);
         statedInferredButton.setToolTipText("showing inferred, toggle to show inferred then stated...");

         break;

      case INFERRED_THEN_STATED :
         statedInferredButton.setIcon(inferredThenStatedView);
         statedInferredButton.setToolTipText("showing inferred then stated, toggle to show stated...");

         break;

      case STATED :
         statedInferredButton.setIcon(inferredView);
         statedInferredButton.setToolTipText("showing stated, toggle to show inferred...");

         break;
      }

      statedInferredButton.setSelected(true);
      statedInferredButton.setPreferredSize(new Dimension(20, 16));
      statedInferredButton.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      statedInferredButton.setOpaque(false);
      statedInferredButton.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

      JPanel             buttonPanel = new JPanel(new GridBagLayout());
      GridBagConstraints c           = new GridBagConstraints();

      c.anchor  = GridBagConstraints.WEST;
      c.gridx   = 0;
      c.gridy   = 0;
      c.fill    = GridBagConstraints.NONE;
      c.weightx = 0;
      c.weighty = 0;
      buttonPanel.setOpaque(false);
      buttonPanel.add(statedInferredButton, c);
      treeView.setColumnHeaderView(buttonPanel);
      c.gridx++;
      c.weightx = 1;

      JLabel view = new JLabel(aceFrameConfig.getViewPositionSetReadOnly().toString());

      buttonPanel.add(view, c);

      return treeView;
   }

   NodeFactory getNodeFactory() {
      return model.nodeFactory;
   }

   public ConcurrentHashMap<Long, TaxonomyNode> getNodeMap() {
      return model.getNodeMap();
   }

   public TaxonomyNodeRenderer getRenderer() {
      return renderer;
   }

   public TaxonomyTree getTree() {
      return tree;
   }

   public ActivityPanel getTreeActivityPanel() {
      return activity;
   }

   public ViewCoordinate getViewCoordinate() {
      return this.aceFrameConfig.getViewCoordinate();
   }

   //~--- set methods ---------------------------------------------------------

   public void setTreeActivityPanel(ActivityPanel activity) {
      this.activity = activity;
   }
}
