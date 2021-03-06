/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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



package org.dwfa.ace.refset;

//~--- non-JDK imports --------------------------------------------------------

import org.dwfa.ace.ACE;
import org.dwfa.ace.activity.ActivityPanel;
import org.dwfa.ace.activity.ActivityViewer;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.CheckBoxCellRenderer;
import org.dwfa.ace.table.refset.CheckBoxHeaderRenderer;
import org.dwfa.ace.table.refset.ExtTableRenderer;
import org.dwfa.ace.table.refset.ReflexiveRefsetCommentTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetTableModel;
import org.dwfa.ace.table.refset.ReflexiveTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.tree.TreeMouseListener;
import org.dwfa.bpa.util.SortClickListener;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.ComputationCanceled;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;

import org.ihtsdo.taxonomy.TaxonomyHelper;
import org.ihtsdo.taxonomy.TaxonomyMouseListener;
import org.ihtsdo.taxonomy.model.childfilters.OrCompositeChildFilter;
import org.ihtsdo.taxonomy.model.childfilters.RefsetMemberChildFilter;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidListBI;
import org.ihtsdo.tk.api.PathBI;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.query.RefsetSpec;
import org.ihtsdo.util.swing.GuiUtil;

public class RefsetSpecPanel extends JPanel {
   private static final String COMMENT_VIEW                = "comments";
   private static final String HIERARCHICAL_VIEW           = "taxonomy";
   private static final String REFSET_AND_PARENT_ONLY_VIEW = "  tree  ";
   private static final String TABLE_VIEW                  = "members";

   /**
    *
    */
   private static final long                         serialVersionUID   = 1L;
   private static HashMap<Integer, PROMOTION_STATUS> promotionStatusMap = new HashMap<Integer,
                                                                             PROMOTION_STATUS>();

   //~--- fields --------------------------------------------------------------

   private boolean                          showPromotionCheckBoxes = false;
   private boolean                          showPromotionFilters    = false;
   private boolean                          showPromotionTab        = false;
   private Set<Object>                      promotionTypes          = new HashSet<Object>();
   private JLabel                           filterLabel             = new JLabel("Filter view:");
   private Box                              snomedIdPanel           = new Box(BoxLayout.Y_AXIS);
   private final ACE                        ace;
   private I_ConfigAceFrame                 aceFrameConfig;
   private JButton                          approveButton;
   private Box                              bottomPanelVerticalBox;
   private JTabbedPane                      bottomTabs;
   private TableColumn                      checkBoxColumn;
   private CheckBoxHeaderRenderer           checkBoxHeaderRenderer;
   private ReflexiveRefsetCommentTableModel commentTableModel;
   private JButton                          disapproveButton;
   private RefsetSpecEditor                 editor;
   private JComboBox                        filterComboBox;
   private TaxonomyHelper                   hierarchicalTreeHelper;
   private Box                              horizontalBox;
   private TaxonomyHelper                   refsetAndParentOnlyTreeHelper;
   private JTableWithDragImage              refsetTable;
   private ReflexiveRefsetTableModel        refsetTableModel;
   private JCheckBox                        selectAllCheckBox;
   private SelectAllCheckBoxListener        selectAllCheckBoxListener;
   private JSplitPane                       split;
   private Box                              verticalBox;

   //~--- constructors --------------------------------------------------------

   public RefsetSpecPanel(ACE ace) throws Exception {
      super(new GridBagLayout());
      this.ace       = ace;
      aceFrameConfig = ace.getAceFrameConfig();
      split          = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
      split.setOneTouchExpandable(true);
      hierarchicalTreeHelper = new TaxonomyHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig(),
              new IntSet(), false), "Refset hierarchy", null);
      refsetAndParentOnlyTreeHelper = new TaxonomyHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig(),
              new IntSet(), true), " Refset parent-only hierarchy", null);
      bottomPanelVerticalBox = new Box(BoxLayout.Y_AXIS);
      bottomTabs             = new JTabbedPane();
      bottomTabs.addTab(HIERARCHICAL_VIEW, hierarchicalTreeHelper.getHierarchyPanel());
      bottomTabs.addTab(REFSET_AND_PARENT_ONLY_VIEW, refsetAndParentOnlyTreeHelper.getHierarchyPanel());
      hierarchicalTreeHelper.addMouseListener(new TaxonomyMouseListener(hierarchicalTreeHelper));
      refsetAndParentOnlyTreeHelper.addMouseListener(new TaxonomyMouseListener(hierarchicalTreeHelper));
      hierarchicalTreeHelper.addMouseListener(new TreeMouseListener(ace));
      refsetAndParentOnlyTreeHelper.addMouseListener(new TreeMouseListener(ace));
      editor = new RefsetSpecEditor(ace, hierarchicalTreeHelper, refsetAndParentOnlyTreeHelper, this);
      split.setTopComponent(editor.getContentPanel());
      ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", refsetAndParentOnlyTreeHelper);
      ace.getAceFrameConfig().addPropertyChangeListener("commit", refsetAndParentOnlyTreeHelper);
      ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", hierarchicalTreeHelper);
      ace.getAceFrameConfig().addPropertyChangeListener("commit", hierarchicalTreeHelper);
      verticalBox = new Box(BoxLayout.Y_AXIS);
      bottomTabs.addTab(TABLE_VIEW, verticalBox);
      bottomTabs.addTab(COMMENT_VIEW, new JScrollPane());
      bottomTabs.addChangeListener(new TabChangeListener());
      commentTableModel = setupCommentTable();
      setupRefsetMemberTable(commentTableModel);
      I_AmTermComponent termComponent = editor.getTermComponent();
      editor.getLabel().setTermComponent(null);
      editor.getLabel().setTermComponent(termComponent);
      editor.addHistoryActionListener(new HistoryActionListener());
      editor.getLabel().addTermChangeListener(new PropertyChangeListener() {

           @Override
           public void propertyChange(PropertyChangeEvent evt) {
               refresh();
           }
       });
      bottomPanelVerticalBox.add(bottomTabs);
      refreshSnomedIdPanel();
      bottomPanelVerticalBox.add(snomedIdPanel);
      split.setBottomComponent(bottomPanelVerticalBox);
      split.setDividerLocation(200);

      GridBagConstraints c = new GridBagConstraints();

      c.anchor  = GridBagConstraints.NORTHWEST;
      c.gridx   = 0;
      c.gridy   = 0;
      c.weightx = 1.0;
      c.weighty = 0.0;
      c.fill    = GridBagConstraints.BOTH;
      add(editor.getTopPanel(), c);
      c.gridy++;
      c.weightx = 1.0;
      c.weighty = 1.0;
      add(split, c);
      refsetTableModel.propertyChange(null);
   }

   //~--- enums ---------------------------------------------------------------

   private enum PROMOTION_STATUS {
      REVIEWED_APPROVED_ADDITION(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION),
      REVIEWED_APPROVED_DELETION(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION),
      REVIEWED_NOT_APPROVED_ADDITION(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION),
      REVIEWED_NOT_APPROVED_DELETION(ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION),
      UNREVIEWED_NEW_ADDITION(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION),
      UNREVIEWED_NEW_DELETION(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION);

      private int nid;

      //~--- constructors -----------------------------------------------------

      private PROMOTION_STATUS(I_ConceptualizeUniversally c) {
         try {
            nid = c.localize().getNid();
            promotionStatusMap.put(nid, this);
         } catch (TerminologyException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }

      //~--- get methods ------------------------------------------------------

      public static PROMOTION_STATUS get(int nid) throws TerminologyException, IOException {
         return promotionStatusMap.get(nid);
      }

      public int getNid() {
         return nid;
      }
   }

   //~--- methods -------------------------------------------------------------

   protected JTableWithDragImage createCommentTable(I_ConfigAceFrame aceFrameConfig, RefsetSpecEditor editor)
           throws NoSuchMethodException, Exception {
      List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();

      columns.add(ReflexiveRefsetFieldData.getRowColumn());

      ReflexiveRefsetFieldData column1 = new ReflexiveRefsetFieldData();

      column1.setColumnName("referenced component");
      column1.setCreationEditable(true);
      column1.setUpdateEditable(true);
      column1.setFieldClass(Number.class);
      column1.setMin(5);
      column1.setPref(150);
      column1.setMax(1000);
      column1.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.COMPONENT);

      List<Object> parameters = new ArrayList<Object>();

      parameters.add(aceFrameConfig.getTableDescPreferenceList());
      parameters.add(aceFrameConfig);
      column1.setReadParamaters(parameters);
      column1.setType(REFSET_FIELD_TYPE.COMPONENT_IDENTIFIER);
      columns.add(column1);

      ReflexiveRefsetFieldData column2 = new ReflexiveRefsetFieldData();

      column2.setColumnName("comments");
      column2.setCreationEditable(true);
      column2.setUpdateEditable(true);
      column2.setFieldClass(StringWithExtTuple.class);
      column2.setMin(5);
      column2.setPref(300);
      column2.setMax(2000);
      column2.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
      column2.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getString1Value"));
      column2.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setString1Value", String.class));
      column2.setType(REFSET_FIELD_TYPE.STRING);
      columns.add(column2);

      ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();

      column3.setColumnName("status");
      column3.setCreationEditable(false);
      column3.setUpdateEditable(false);
      column3.setFieldClass(StringWithExtTuple.class);
      column3.setMin(5);
      column3.setPref(150);
      column3.setMax(150);
      column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
      column3.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getStatusId"));
      column3.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setStatusId", int.class));
      column3.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
      columns.add(column3);

      if (editor.getShowHistory()) {
         ReflexiveRefsetFieldData column4 = new ReflexiveRefsetFieldData();

         column4.setColumnName("time");
         column4.setCreationEditable(false);
         column4.setUpdateEditable(false);
         column4.setFieldClass(StringWithExtTuple.class);
         column4.setMin(5);
         column4.setPref(150);
         column4.setMax(150);
         column4.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
         column4.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getTime"));
         column4.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setTime", long.class));
         column4.setType(REFSET_FIELD_TYPE.TIME);
         columns.add(column4);

         ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();

         column5.setColumnName("path");
         column5.setCreationEditable(false);
         column5.setUpdateEditable(false);
         column5.setFieldClass(StringWithExtTuple.class);
         column5.setMin(5);
         column5.setPref(150);
         column5.setMax(450);
         column5.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
         column5.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getPathId"));
         column5.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setPathId", int.class));
         column5.setType(REFSET_FIELD_TYPE.COMPONENT_IDENTIFIER);
         columns.add(column5);
      }

      ReflexiveRefsetCommentTableModel modelForCommentTable =
         new ReflexiveRefsetCommentTableModel(editor,
            columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));

      aceFrameConfig.addPropertyChangeListener("viewPositions", modelForCommentTable);
      aceFrameConfig.addPropertyChangeListener("commit", modelForCommentTable);
      aceFrameConfig.addPropertyChangeListener("commitEnabled", modelForCommentTable);
      aceFrameConfig.addPropertyChangeListener("uncommitted", modelForCommentTable);
      editor.getLabel().addTermChangeListener(modelForCommentTable);

      int               componentId = Integer.MIN_VALUE;
      I_AmTermComponent component   = editor.getTermComponent();

      if (component != null) {
         componentId = component.getNid();
      }

      modelForCommentTable.setComponentId(componentId);
      modelForCommentTable.getRowCount();

      JTableWithDragImage commentTable = new JTableWithDragImage(modelForCommentTable);

      commentTable.getColumnModel().addColumnModelListener(new CommentTableColumnModelListener(commentTable));
      SortClickListener.setupSorter(commentTable);

      for (int i = 0; i < commentTable.getColumnModel().getColumnCount(); i++) {
         TableColumn column = commentTable.getColumnModel().getColumn(i);

         column.setIdentifier(columns.get(i));
         column.setPreferredWidth(columns.get(i).getPref());
         column.setMaxWidth(columns.get(i).getMax());
         column.setMinWidth(columns.get(i).getMin());
      }

      commentTable.getTableHeader().setToolTipText("Click to specify sorting");

      ExtTableRenderer renderer = new ExtTableRenderer();

      commentTable.setDefaultRenderer(StringWithExtTuple.class, renderer);
      commentTable.setDefaultRenderer(Number.class, renderer);
      commentTable.setDefaultRenderer(Boolean.class, renderer);
      commentTable.setDefaultRenderer(Integer.class, renderer);
      commentTable.setDefaultRenderer(Double.class, renderer);
      commentTable.setDefaultRenderer(String.class, renderer);

      return commentTable;
   }

   private boolean memberTabSelected() {
      int selectedTabIndex = bottomTabs.getSelectedIndex();

      if (bottomTabs.getTitleAt(selectedTabIndex).equals(TABLE_VIEW)) {
         return true;
      } else {
         return false;
      }
   }

   public void refresh() {
      editor.refresh();

      RefreshSwingWorker worker = new RefreshSwingWorker();

      ACE.threadPool.execute(worker);
   }

   private void refreshSnomedIdPanel() {
      if (snomedIdPanel == null) {
         snomedIdPanel = new Box(BoxLayout.Y_AXIS);
      }

      snomedIdPanel.removeAll();

      if (!memberTabSelected()) {
         return;
      }

      if (!hasSingleComponentRowSelected()) {
         return;
      }

      Box horizontalBoxLayout = new Box(BoxLayout.X_AXIS);
      int memberId      = getComponentIdOfCurrentSelection();

      horizontalBoxLayout.add(Box.createHorizontalStrut(5));

      JLabel snomedIdLabel = new JLabel("SNOMED ID of selected component: ");

      horizontalBoxLayout.add(snomedIdLabel);
      horizontalBoxLayout.add(Box.createHorizontalStrut(5));

      if (hasSnomedId(memberId)) {
         horizontalBoxLayout.add(new JLabel("" + getSnomedId(memberId)));
      } else {
         horizontalBoxLayout.add(new JLabel("Not available"));
      }

      horizontalBoxLayout.add(Box.createHorizontalGlue());
      snomedIdPanel.add(Box.createVerticalStrut(5));
      snomedIdPanel.add(horizontalBoxLayout);
      snomedIdPanel.add(Box.createVerticalStrut(5));
   }

   public ReflexiveRefsetCommentTableModel setupCommentTable() throws NoSuchMethodException, Exception {
      JTableWithDragImage commentTable = createCommentTable(aceFrameConfig, editor);

      for (int i = 0; i < bottomTabs.getTabCount(); i++) {
         if (bottomTabs.getTitleAt(i).equals(COMMENT_VIEW)) {
            JScrollPane tableScroller = (JScrollPane) bottomTabs.getComponentAt(i);

            tableScroller.setViewportView(commentTable);

            break;
         }
      }

      return (ReflexiveRefsetCommentTableModel) commentTable.getModel();
   }

   public void setupRefsetMemberTable(ReflexiveRefsetCommentTableModel commentTableModel)
           throws NoSuchMethodException, Exception {
      List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();

      columns.add(ReflexiveRefsetFieldData.getRowColumn());

      ReflexiveRefsetFieldData column1 = new ReflexiveRefsetFieldData();

      column1.setColumnName("referenced component");
      column1.setCreationEditable(true);
      column1.setUpdateEditable(true);
      column1.setFieldClass(StringWithExtTuple.class);
      column1.setMin(5);
      column1.setPref(150);
      column1.setMax(1000);
      column1.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.CONCEPT_COMPONENT);
      column1.setReadMethod(I_GetConceptData.class.getMethod("getDescTuple", NidListBI.class,
              I_ConfigAceFrame.class));

      List<Object> parameters = new ArrayList<Object>();

      parameters.add(aceFrameConfig.getTableDescPreferenceList());
      parameters.add(aceFrameConfig);
      column1.setReadParamaters(parameters);
      column1.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
      columns.add(column1);

      ReflexiveRefsetFieldData column2 = new ReflexiveRefsetFieldData();

      column2.setColumnName("member type");
      column2.setCreationEditable(true);
      column2.setUpdateEditable(true);
      column2.setFieldClass(StringWithExtTuple.class);
      column2.setMin(5);
      column2.setPref(50);
      column2.setMax(150);
      column2.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
      column2.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getC1id"));
      column2.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setC1id", int.class));
      column2.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
      columns.add(column2);

      ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();

      column3.setColumnName("member status");
      column3.setCreationEditable(false);
      column3.setUpdateEditable(false);
      column3.setFieldClass(StringWithExtTuple.class);
      column3.setMin(5);
      column3.setPref(150);
      column3.setMax(150);
      column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
      column3.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getStatusId"));
      column3.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setStatusId", int.class));
      column3.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
      columns.add(column3);

      if (editor.getShowHistory()) {
         ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();

         column5.setColumnName("time");
         column5.setCreationEditable(false);
         column5.setUpdateEditable(false);
         column5.setFieldClass(StringWithExtTuple.class);
         column5.setMin(5);
         column5.setPref(150);
         column5.setMax(150);
         column5.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
         column5.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getTime"));
         column5.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setTime", long.class));
         column5.setType(REFSET_FIELD_TYPE.TIME);
         columns.add(column5);

         ReflexiveRefsetFieldData column6 = new ReflexiveRefsetFieldData();

         column6.setColumnName("path");
         column6.setCreationEditable(false);
         column6.setUpdateEditable(false);
         column6.setFieldClass(StringWithExtTuple.class);
         column6.setMin(5);
         column6.setPref(150);
         column6.setMax(150);
         column6.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
         column6.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getPathId"));
         column6.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setPathId", int.class));
         column6.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
         columns.add(column6);
      }

      refsetTableModel = new ReflexiveRefsetTableModel(editor,
              columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
      aceFrameConfig.addPropertyChangeListener("viewPositions", refsetTableModel);
      aceFrameConfig.addPropertyChangeListener("commit", refsetTableModel);
      aceFrameConfig.addPropertyChangeListener("commitEnabled", commentTableModel);
      aceFrameConfig.addPropertyChangeListener("uncommitted", commentTableModel);
      editor.getLabel().addTermChangeListener(refsetTableModel);

      int               componentId = Integer.MIN_VALUE;
      I_AmTermComponent component   = editor.getTermComponent();

      if (component != null) {
         componentId = component.getNid();
      }

      refsetTableModel.setComponentId(componentId);
      refsetTableModel.setPromotionFilterId(null);
      refsetTableModel.getRowCount();
      refsetTable = new JTableWithDragImage(refsetTableModel);
      SortClickListener.setupSorter(refsetTable);
      refsetTable.setAutoCreateRowSorter(false);

      TableRowSorterWithOptionalCheckbox sorter = new TableRowSorterWithOptionalCheckbox(refsetTableModel);

      refsetTable.setRowSorter(sorter);
      refsetTable.getTableHeader().setToolTipText("Click to specify sorting");

      for (int i = 0; i < columns.size(); i++) {
         TableColumn column = refsetTable.getColumnModel().getColumn(i);

         column.setIdentifier(columns.get(i));
         column.setPreferredWidth(columns.get(i).getPref());
         column.setMaxWidth(columns.get(i).getMax());
         column.setMinWidth(columns.get(i).getMin());
         column.setResizable(true);
      }

      // set renderer and editor for checkbox column
      // get the last column
      CheckBoxCellRenderer checkBoxRenderer = new CheckBoxCellRenderer();

      selectAllCheckBoxListener = new SelectAllCheckBoxListener();
      checkBoxHeaderRenderer    = new CheckBoxHeaderRenderer(selectAllCheckBoxListener, this,
              refsetTable.getTableHeader());
      checkBoxColumn = new TableColumn();
      checkBoxColumn.setCellRenderer(checkBoxRenderer);
      checkBoxColumn.setResizable(false);
      checkBoxColumn.setHeaderRenderer(checkBoxHeaderRenderer);
      checkBoxColumn.setModelIndex(columns.size());
      checkBoxColumn.setMaxWidth(checkBoxHeaderRenderer.getPreferredWidth());
      checkBoxColumn.setMinWidth(checkBoxHeaderRenderer.getPreferredWidth());
      checkBoxColumn.setPreferredWidth(checkBoxHeaderRenderer.getPreferredWidth());

      // hide column as default (should only be visible during promotions BP)
      // refsetTable.getColumnModel().removeColumn(checkBoxColumn);
      refsetTable.addMouseListener(new MouseClickListener());

      // add listener for member row selection so that we can update the SNOMED ID if necessary
      ListSelectionModel selectionModel = refsetTable.getSelectionModel();

      selectionModel.addListSelectionListener(new MemberSelectionListener(refsetTable));

      ExtTableRenderer renderer = new ExtTableRenderer();

      refsetTable.setDefaultRenderer(StringWithExtTuple.class, renderer);
      refsetTable.setDefaultRenderer(Number.class, renderer);
      refsetTable.setDefaultRenderer(Boolean.class, checkBoxRenderer);
      refsetTable.setDefaultRenderer(Integer.class, renderer);
      refsetTable.setDefaultRenderer(Double.class, renderer);
      refsetTable.setDefaultRenderer(String.class, renderer);

      for (int i = 0; i < bottomTabs.getTabCount(); i++) {
         if (bottomTabs.getTitleAt(i).equals(TABLE_VIEW)) {
            horizontalBox = new Box(BoxLayout.X_AXIS);
            approveButton = new JButton("Approve selected");
            approveButton.addActionListener(new ApproveActionListener());
            disapproveButton = new JButton("Disapprove selected");
            disapproveButton.addActionListener(new DisapproveActionListener());
            approveButton.setPreferredSize(disapproveButton.getPreferredSize());
            filterLabel    = new JLabel("Filter view:");
            promotionTypes = new HashSet<Object>();
            promotionTypes.add("All");
            promotionTypes.add(
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids()));
            promotionTypes.add(
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids()));
            promotionTypes.add(
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.getUids()));
            promotionTypes.add(
                Terms.get().getConcept(ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.getUids()));
            promotionTypes.add(
                Terms.get().getConcept(
                   ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.getUids()));
            promotionTypes.add(
                Terms.get().getConcept(
                   ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION.getUids()));
            filterComboBox = new JComboBox(promotionTypes.toArray());
            filterComboBox.setSelectedItem("All");
            filterComboBox.addActionListener(new FilterSelectionActionListener());
            filterComboBox.setMaximumSize(filterComboBox.getPreferredSize());
            setShowButtons(showPromotionCheckBoxes);
            horizontalBox.add(Box.createHorizontalStrut(5));
            horizontalBox.add(approveButton);
            horizontalBox.add(Box.createHorizontalStrut(5));
            horizontalBox.add(disapproveButton);
            horizontalBox.add(Box.createHorizontalGlue());
            horizontalBox.add(filterLabel);
            horizontalBox.add(Box.createHorizontalStrut(5));
            horizontalBox.add(filterComboBox);
            horizontalBox.add(Box.createHorizontalStrut(5));

            if (verticalBox != null) {
               verticalBox.removeAll();
            }

            verticalBox.add(horizontalBox);
            verticalBox.add(Box.createVerticalGlue());

            JScrollPane scrollPane = new JScrollPane();

            scrollPane.setViewportView(refsetTable);
            verticalBox.add(scrollPane);
            verticalBox.add(Box.createVerticalGlue());
            GuiUtil.tickle(verticalBox);

            break;
         }
      }

      List<ReflexiveTableModel> commentTableModels = new ArrayList<ReflexiveTableModel>();

      commentTableModels.add(editor.getCommentTableModel());
      commentTableModels.add(commentTableModel);
      refsetTable.addMouseListener(new MemberTablePopupListener(refsetTable, aceFrameConfig,
              commentTableModels));
   }

   private void updateCheckBoxes() {
      int checkedCount        = 0;
      int checkBoxColumnIndex = refsetTable.getColumnModel().getColumnCount() - 1;

      selectAllCheckBox.removeItemListener(selectAllCheckBoxListener);

      boolean[] flags = new boolean[refsetTable.getRowCount()];

      for (int i = 0; i < refsetTable.getRowCount(); i++) {
         flags[i] = ((Boolean) refsetTable.getValueAt(i, checkBoxColumnIndex)).booleanValue();

         if (flags[i]) {
            checkedCount++;
         }
      }

      if (checkedCount == refsetTable.getRowCount()) {
         selectAllCheckBox.setSelected(true);
      }

      if (checkedCount != refsetTable.getRowCount()) {
         selectAllCheckBox.setSelected(false);
      }

      selectAllCheckBox.addItemListener(selectAllCheckBoxListener);
      refsetTable.getTableHeader().repaint();
   }

   //~--- get methods ---------------------------------------------------------

   public ReflexiveRefsetCommentTableModel getCommentTableModel() {
      return commentTableModel;
   }

   private int getComponentIdOfCurrentSelection() {
      int                rowIndex          = refsetTable.getSelectedRow();
      StringWithExtTuple selectedComponent = (StringWithExtTuple) refsetTable.getValueAt(rowIndex, 1);

      return selectedComponent.getId();
   }

   private PROMOTION_STATUS getNewStatusForApproval(PROMOTION_STATUS oldStatus) {
      if (oldStatus == null) {
         return null;
      }

      switch (oldStatus) {
      case REVIEWED_APPROVED_ADDITION :
         return null;

      case REVIEWED_APPROVED_DELETION :
         return null;

      case REVIEWED_NOT_APPROVED_ADDITION :
         return PROMOTION_STATUS.REVIEWED_APPROVED_ADDITION;

      case REVIEWED_NOT_APPROVED_DELETION :
         return PROMOTION_STATUS.REVIEWED_APPROVED_DELETION;

      case UNREVIEWED_NEW_ADDITION :
         return PROMOTION_STATUS.REVIEWED_APPROVED_ADDITION;

      case UNREVIEWED_NEW_DELETION :
         return PROMOTION_STATUS.REVIEWED_APPROVED_DELETION;

      default :
         throw new RuntimeException("Can't handle promotion status: " + oldStatus);
      }
   }

   private PROMOTION_STATUS getNewStatusForDisapproval(PROMOTION_STATUS oldStatus) {
      if (oldStatus == null) {
         return null;
      }

      switch (oldStatus) {
      case REVIEWED_APPROVED_ADDITION :
         return PROMOTION_STATUS.REVIEWED_NOT_APPROVED_ADDITION;

      case REVIEWED_APPROVED_DELETION :
         return PROMOTION_STATUS.REVIEWED_NOT_APPROVED_DELETION;

      case REVIEWED_NOT_APPROVED_ADDITION :
         return null;

      case REVIEWED_NOT_APPROVED_DELETION :
         return null;

      case UNREVIEWED_NEW_ADDITION :
         return PROMOTION_STATUS.REVIEWED_NOT_APPROVED_ADDITION;

      case UNREVIEWED_NEW_DELETION :
         return PROMOTION_STATUS.REVIEWED_NOT_APPROVED_DELETION;

      default :
         throw new RuntimeException("Can't handle promotion status: " + oldStatus);
      }
   }

   public I_GetConceptData getRefsetInSpecEditor() {
      return (I_GetConceptData) editor.getTermComponent();
   }

   public RefsetSpecEditor getRefsetSpecEditor() {
      return editor;
   }

   public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
      return editor.getRefsetSpecInSpecEditor();
   }

   public JTableWithDragImage getRefsetTable() {
      return refsetTable;
   }

   public JCheckBox getRendererComponent() {
      return selectAllCheckBox;
   }

   private Integer getSelectedPromotionFilter() {
      Object o = filterComboBox.getSelectedItem();

      if (o == null) {
         return null;
      } else if (o.equals("All")) {
         return null;
      } else {
         I_GetConceptData concept = (I_GetConceptData) o;

         return concept.getConceptNid();
      }
   }

   public I_ExtendByRef getSelectedRefsetClauseInSpecEditor() {
      return getSelectedRefsetClauseInSpecEditor();
   }

   public boolean getShowPromotionCheckBoxes() {
      return showPromotionCheckBoxes;
   }

   public Boolean getShowPromotionFilters() {
      return showPromotionFilters;
   }

   public Boolean getShowPromotionTab() {
      return showPromotionTab;
   }

   private String getSnomedId(int componentId) {
      try {
         I_Identify idVersioned     = Terms.get().getId(componentId);
         int        snomedIntegerId =
            Terms.get().getId(
                ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next()).getNid();
         List<? extends I_IdPart> parts      = idVersioned.getMutableIdParts();
         I_IdPart                 latestPart = null;

         for (I_IdPart part : parts) {
            if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
               if (part.getAuthorityNid() == snomedIntegerId) {
                  latestPart = part;
               }
            }
         }

         if (latestPart != null) {
            return latestPart.getDenotation().toString();
         } else {
            throw new IOException("No SNOMED ID available");
         }
      } catch (IOException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      return "unknown";
   }

   public JTree getTreeInSpecEditor() {
      return editor.getTreeInSpecEditor();
   }

   private boolean hasSingleComponentRowSelected() {
      if (refsetTable.getSelectedRowCount() == 1) {
         return true;
      } else {
         return false;
      }
   }

   private boolean hasSnomedId(int componentId) {
      try {
         I_Identify idVersioned     = Terms.get().getId(componentId);
         int        snomedIntegerId =
            Terms.get().getId(
                ArchitectonicAuxiliary.Concept.SNOMED_INT_ID.getUids().iterator().next()).getNid();
         I_IdPart latestPart = null;

         if (idVersioned != null) {
            List<? extends I_IdPart> parts = idVersioned.getMutableIdParts();

            for (I_IdPart part : parts) {
               if ((latestPart == null) || (part.getTime() >= latestPart.getTime())) {
                  if (part.getAuthorityNid() == snomedIntegerId) {
                     latestPart = part;
                  }
               }
            }
         }

         if (latestPart != null) {
            return true;
         } else {
            return false;
         }
      } catch (IOException e) {
         AceLog.getAppLog().alertAndLogException(e);
      }

      return false;
   }

   //~--- set methods ---------------------------------------------------------

   public void setCheckBoxRendererComponent(JCheckBox rendererComponent) {
      this.selectAllCheckBox = rendererComponent;
   }

   public void setRefsetInSpecEditor(I_GetConceptData refset) {
      editor.setTermComponent(refset);
   }

   public void setRefsetTable(JTableWithDragImage refsetTable) {
      this.refsetTable = refsetTable;
   }

   public void setShowButtons(boolean show) {
      approveButton.setVisible(show);
      disapproveButton.setVisible(show);
      filterLabel.setVisible(show);
      filterComboBox.setVisible(show);

      if (show) {
         refsetTable.getColumnModel().addColumn(checkBoxColumn);
         refsetTableModel.enableCheckBoxColumn(true);
         horizontalBox.setMaximumSize(null);
         horizontalBox.setMinimumSize(null);
         horizontalBox.setPreferredSize(null);

         for (int i = 0; i < bottomTabs.getTabCount(); i++) {
            if (bottomTabs.getTitleAt(i).equals(TABLE_VIEW)) {
               bottomTabs.setSelectedIndex(i);

               break;
            }
         }
      } else {
         refsetTable.getColumnModel().removeColumn(checkBoxColumn);
         refsetTableModel.enableCheckBoxColumn(false);
         horizontalBox.setMaximumSize(new Dimension(0, 0));
         horizontalBox.setMinimumSize(new Dimension(0, 0));
         horizontalBox.setPreferredSize(new Dimension(0, 0));

         if (refsetTable.getParent() != null) {
            refsetTable.getParent().validate();
         }
      }
   }

   public void setShowPromotionCheckBoxes(Boolean show) {
      if (showPromotionCheckBoxes != show) {
         showPromotionCheckBoxes = show;
         refsetTableModel.setShowPromotionCheckBoxes(show);
         setShowButtons(show);
      }
   }

   public void setShowPromotionFilters(Boolean show) {
      showPromotionFilters = show;
      filterLabel.setVisible(show);
      filterComboBox.setVisible(show);
      horizontalBox.setMaximumSize(null);
      horizontalBox.setMinimumSize(null);
      horizontalBox.setPreferredSize(null);
   }

   public void setShowPromotionTab(Boolean show) {
      if (show) {
         for (int i = 0; i < bottomTabs.getTabCount(); i++) {
            if (bottomTabs.getTitleAt(i).equals(TABLE_VIEW)) {
               bottomTabs.setSelectedIndex(i);

               break;
            }
         }
      } else {
         for (int i = 0; i < bottomTabs.getTabCount(); i++) {
            if (bottomTabs.getTitleAt(i).equals(HIERARCHICAL_VIEW)) {
               bottomTabs.setSelectedIndex(i);

               break;
            }
         }
      }

      horizontalBox.setMaximumSize(null);
      horizontalBox.setMinimumSize(null);
      horizontalBox.setPreferredSize(null);
   }

   //~--- inner classes -------------------------------------------------------

   private class ApproveActionListener implements ActionListener {
        @Override
      public void actionPerformed(ActionEvent e) {
         Set<Integer> tupleMemberIds = refsetTableModel.getSelectedTuples();

         try {
            new ProcessSelectionWorker(tupleMemberIds, true).execute();
         } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
         }
      }
   }


   private class CommentTableColumnModelListener implements TableColumnModelListener {
      JTableWithDragImage table;

      //~--- constructors -----------------------------------------------------

      public CommentTableColumnModelListener(JTableWithDragImage table) {
         super();
         this.table = table;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void columnAdded(TableColumnModelEvent arg0) {

         // TODO Auto-generated method stub
      }

      @Override
      public void columnMarginChanged(ChangeEvent arg0) {
         int rowCount    = table.getRowCount();
         int columnCount = table.getColumnCount();

         if ((table.getModel().getRowCount() == table.getRowCount())
                 && (table.getModel().getColumnCount() == table.getColumnCount())) {
            for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
               double rowPreferredHeight = 0;

               for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
                  Component comp = table.getCellRenderer(rowIndex,
                                      columnIndex).getTableCellRendererComponent(table,
                                         table.getModel().getValueAt(rowIndex, columnIndex), false, false,
                                         rowIndex, columnIndex);
                  int currentRowHeight = table.getRowHeight(rowIndex);
                  int currentRowWidth  = table.getColumnModel().getColumn(columnIndex).getWidth();

                  comp.setSize(currentRowWidth, currentRowHeight);

                  double dPreferredHeight = comp.getPreferredSize().getHeight();

                  if ((dPreferredHeight > rowPreferredHeight) && (rowIndex >= 0)
                          && (rowIndex < table.getRowCount())) {
                     rowPreferredHeight = dPreferredHeight;

                     // set the new row height based on the preferred height of the cell component
                     try {
                        table.setRowHeight(rowIndex, (int) dPreferredHeight);
                     } catch (IndexOutOfBoundsException e) {
                        AceLog.getAppLog().warning("columnMarginChanged ex: " + e.toString());
                     }
                  }
               }
            }
         }
      }

      @Override
      public void columnMoved(TableColumnModelEvent arg0) {

         // TODO Auto-generated method stub
      }

      @Override
      public void columnRemoved(TableColumnModelEvent arg0) {

         // TODO Auto-generated method stub
      }

      @Override
      public void columnSelectionChanged(ListSelectionEvent arg0) {

         // TODO Auto-generated method stub
      }
   }


   private class DisapproveActionListener implements ActionListener {
        @Override
      public void actionPerformed(ActionEvent e) {
         Set<Integer> tupleMemberIds = refsetTableModel.getSelectedTuples();

         try {
            new ProcessSelectionWorker(tupleMemberIds, false).execute();
         } catch (Exception e1) {
            AceLog.getAppLog().alertAndLogException(e1);
         }
      }
   }


   public class FilterSelectionActionListener implements ActionListener {
      @Override
      public void actionPerformed(ActionEvent arg0) {
         Integer selectedPromotionFilter = getSelectedPromotionFilter();

         refsetTableModel.setPromotionFilterId(selectedPromotionFilter);
         refsetTableModel.getRowCount();
         editor.getLabel().setTermComponent(editor.getTermComponent());
      }
   }


   private class HistoryActionListener implements ActionListener {
        @Override
      public void actionPerformed(ActionEvent arg0) {
         try {
            commentTableModel = setupCommentTable();
            setupRefsetMemberTable(commentTableModel);

            if ((refsetTable != null) && (refsetTable.getParent() != null)) {
                refsetTableModel.fireTableDataChanged();
                GuiUtil.tickle(refsetTable);
               refsetTable.getParent().validate();
            }
         } catch (NoSuchMethodException e) {
            AceLog.getAppLog().alertAndLogException(e);
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
   }


   public class MemberSelectionListener implements ListSelectionListener {
      JTableWithDragImage refsetTable;

      //~--- constructors -----------------------------------------------------

      public MemberSelectionListener(JTableWithDragImage refsetTable) {
         this.refsetTable = refsetTable;
      }

      //~--- methods ----------------------------------------------------------

      @Override
      public void valueChanged(ListSelectionEvent e) {
         if (e.getValueIsAdjusting()) {
            return;
         }

         refreshSnomedIdPanel();
         snomedIdPanel.repaint();

         Container parent = snomedIdPanel.getParent();

         while (parent != null) {
            parent.repaint();
            parent = parent.getParent();
         }
      }
   }


   private class MouseClickListener extends MouseAdapter {
        @Override
      public void mouseClicked(MouseEvent mouseEvent) {
         if (getShowPromotionCheckBoxes()) {
            updateCheckBoxes();
         }
      }
   }


   /**
    * @TODO modify implementation to report progress using the swing worker
    *       methods.
    * @author kec
    *
    */
   private class ProcessSelectionWorker extends SwingWorker<Boolean, Void> {
      int                   processed = 0;
      private long          startTime = System.currentTimeMillis();
      private ActivityPanel activity;
      boolean               forApproval;
      Set<Integer>          tupleMemberIds;

      //~--- constructors -----------------------------------------------------

      private ProcessSelectionWorker(Set<Integer> tupleMemberIds, boolean forApproval) throws Exception {
         super();
         this.tupleMemberIds = tupleMemberIds;
         this.forApproval    = forApproval;
         activity            = new ActivityPanel(aceFrameConfig, true);
         activity.setIndeterminate(true);
         activity.setMaximum(tupleMemberIds.size());
         activity.setProgressInfoUpper("Changing status of selected members");
         activity.setProgressInfoLower("Setting up...");
         activity.addRefreshActionListener(new ActionListener() {
                @Override
            public void actionPerformed(ActionEvent e) {
               cancel(false);
            }
         });
         ActivityViewer.addActivity(activity);
      }

      //~--- methods ----------------------------------------------------------

      @Override
      protected Boolean doInBackground() throws Exception {
         int    currentNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
         IntSet currentSet = new IntSet();

         currentSet.add(currentNid);

         Set<I_GetConceptData> refsetConcepts = new HashSet<I_GetConceptData>();

         activity.setIndeterminate(false);
         activity.setProgressInfoLower("Processing...");

         PathSetReadOnly promotionPath = new PathSetReadOnly(aceFrameConfig.getPromotionPathSet());

         for (Integer tupleMemberId : tupleMemberIds) {
            if (isCancelled()) {
               Terms.get().cancel();

               return false;
            }

            if (processed % 500 == 0) {
               activity.setValue(processed++);

               long   endTime      = System.currentTimeMillis();
               long   elapsed      = endTime - startTime;
               String elapsedStr   = TimeUtil.getElapsedTimeString(elapsed);
               String remainingStr = TimeUtil.getRemainingTimeString(processed, tupleMemberIds.size(),
                                        elapsed);

               activity.setProgressInfoLower("Elapsed: " + elapsedStr + ";  Remaining: " + remainingStr);
            }

            I_ExtendByRef tupleVersioned = Terms.get().getExtension(tupleMemberId);

            refsetConcepts.add(Terms.get().getConcept(tupleVersioned.getRefsetId()));

             for (I_ExtendByRef extForMember : Terms.get().getAllExtensionsForComponent(tupleMemberId)) {
                 RefsetSpec helper = new RefsetSpec(getRefsetSpecInSpecEditor(), aceFrameConfig.getViewCoordinate());
                 ConceptChronicleBI promotionRefsetConcept = helper.getPromotionRefsetConcept();
                 if (promotionRefsetConcept != null) {
                     int promotionRefsetId = helper.getPromotionRefsetConcept().getConceptNid();

                     if (promotionRefsetId == extForMember.getRefsetId()) {
                         List<? extends I_ExtendByRefVersion> promotionTuples =
                                 extForMember.getTuples(aceFrameConfig.getAllowedStatus(),
                                 aceFrameConfig.getViewPositionSetReadOnly(),
                                 aceFrameConfig.getPrecedence(),
                                 aceFrameConfig.getConflictResolutionStrategy());

                         if (promotionTuples.size() > 0) {
                             I_ExtendByRefPart promotionPart = promotionTuples.get(0).getMutablePart();

                             if (promotionPart instanceof I_ExtendByRefPartCid) {
                                 for (PathBI p : aceFrameConfig.getEditingPathSet()) {
                                     I_ExtendByRefPartCid partToPromote = (I_ExtendByRefPartCid) promotionPart;
                                     PROMOTION_STATUS oldStatus = PROMOTION_STATUS.get(partToPromote.getC1id());
                                     PROMOTION_STATUS newStatus;

                                     if (forApproval) {
                                         newStatus = getNewStatusForApproval(oldStatus);
                                     } else {
                                         newStatus = getNewStatusForDisapproval(oldStatus);
                                     }

                                     if (newStatus != null) {
                                         I_ExtendByRefPartCid analog = (I_ExtendByRefPartCid) partToPromote.makeAnalog(
                                                 promotionPart.getStatusNid(),
                                                 Long.MAX_VALUE,
                                                 aceFrameConfig.getEditCoordinate().getAuthorNid(),
                                                 aceFrameConfig.getEditCoordinate().getModuleNid(),
                                                 p.getConceptNid());

                                         analog.setC1id(newStatus.getNid());
                                         extForMember.addVersion(analog);
                                         extForMember.promote(new Position(Long.MAX_VALUE, p), promotionPath,
                                                 currentSet, aceFrameConfig.getPrecedence(), aceFrameConfig.getEditCoordinate().getAuthorNid());
                                     }
                                 }
                             }
                         }
                     }
                 }
            }
         }

         for (I_GetConceptData refset : refsetConcepts) {
            Terms.get().addUncommittedNoChecks(refset);
         }

         Terms.get().commit();

         return true;
      }

      @Override
      public void done() {
         long   elapsed    = System.currentTimeMillis() - startTime;
         String elapsedStr = TimeUtil.getElapsedTimeString(elapsed);

         if (!isCancelled()) {
            activity.setProgressInfoLower("Complete. Time: " + elapsedStr);
         } else {
            activity.setProgressInfoLower("Cancelled.");
         }

         try {
            activity.complete();
         } catch (ComputationCanceled e1) {

            // Nothing to do
         }

         refsetTableModel.clearSelectedTuples();
         selectAllCheckBox.setSelected(false);
         refsetTable.getTableHeader().repaint();

         try {
            get();
         } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
         }
      }
   }


   private class RefreshSwingWorker extends SwingWorker<Object, Object> {
      private JScrollPane refsetAndParentOnlyHierarchicalPanel;
      private JScrollPane refsetHierarcyPanel;

      //~--- methods ----------------------------------------------------------

      @Override
      protected Object doInBackground() throws Exception {
         hierarchicalTreeHelper = new TaxonomyHelper(new RefsetSpecFrameConfig(aceFrameConfig, new IntSet(),
                 false), "Refset hierarchy", null);
         refsetHierarcyPanel = hierarchicalTreeHelper.getHierarchyPanel();
         hierarchicalTreeHelper.addMouseListener(new TaxonomyMouseListener(hierarchicalTreeHelper));
         hierarchicalTreeHelper.addMouseListener(new TreeMouseListener(ace));

         OrCompositeChildFilter filter   = new OrCompositeChildFilter();
         RefsetSpecFrameConfig  rsConfig = new RefsetSpecFrameConfig(aceFrameConfig, new IntSet(), true);

         if (getRefsetInSpecEditor() != null) {
            try {
               for (int refsetNid : rsConfig.getRefsetsToShowInTaxonomy().getListArray()) {
                  filter.getFilterList().add(
                      new RefsetMemberChildFilter(
                          Ts.get().getConceptVersion(aceFrameConfig.getViewCoordinate(), refsetNid)));
               }
            } catch (IOException ex) {
               AceLog.getAppLog().alertAndLogException(ex);
            }
         }

         refsetAndParentOnlyTreeHelper = new TaxonomyHelper(rsConfig, " Refset parent-only hierarchy",
                 filter);
         refsetAndParentOnlyHierarchicalPanel = refsetAndParentOnlyTreeHelper.getHierarchyPanel();
         refsetAndParentOnlyTreeHelper.addMouseListener(
             new TaxonomyMouseListener(refsetAndParentOnlyTreeHelper));
         refsetAndParentOnlyTreeHelper.addMouseListener(new TreeMouseListener(ace));

         return null;
      }

      @Override
      protected void done() {
         try {
            get();
            bottomTabs.setComponentAt(0, refsetHierarcyPanel);
            bottomTabs.setComponentAt(1, refsetAndParentOnlyHierarchicalPanel);
         } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
         }
      }
   }


   private class SelectAllCheckBoxListener implements ItemListener {
      @Override
      public void itemStateChanged(ItemEvent e) {
         boolean selected = e.getStateChange() == ItemEvent.SELECTED;

         if (selected) {
            refsetTableModel.selectAllTuples();
         } else {
            refsetTableModel.clearSelectedTuples();
         }

         refsetTable.repaint();
      }
   }


   public class TabChangeListener implements ChangeListener {
      @Override
      public void stateChanged(ChangeEvent arg0) {
         refreshSnomedIdPanel();
         snomedIdPanel.repaint();
      }
   }
}
