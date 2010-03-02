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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.table.TableColumn;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.I_HostConceptPlugins.REFSET_TYPES;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.dwfa.ace.api.ebr.I_ExtendByRefVersion;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.CheckBoxCellRenderer;
import org.dwfa.ace.table.refset.CheckBoxHeaderRenderer;
import org.dwfa.ace.table.refset.ExtTableRenderer;
import org.dwfa.ace.table.refset.ReflexiveRefsetCommentTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetTableModel;
import org.dwfa.ace.table.refset.ReflexiveTableModel;
import org.dwfa.ace.table.refset.SelectableReflexiveTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.task.refset.spec.RefsetSpec;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.dwfa.vodb.types.Position;

public class RefsetSpecPanel extends JPanel {

    private class HistoryActionListener implements ActionListener {

        public void actionPerformed(ActionEvent arg0) {
            try {
                commentTableModel = setupCommentTable();
                setupRefsetMemberTable(commentTableModel);
            } catch (NoSuchMethodException e) {
                AceLog.getAppLog().alertAndLogException(e);
            } catch (Exception e) {
                AceLog.getAppLog().alertAndLogException(e);
            }
        }

    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private ReflexiveRefsetCommentTableModel commentTableModel;

    RefsetSpecEditor editor;

    private JTabbedPane bottomTabs;

    private I_ConfigAceFrame aceFrameConfig;

    private ReflexiveRefsetTableModel refsetTableModel;

    private static final String HIERARCHICAL_VIEW = "taxonomy";
    private static final String REFSET_AND_PARENT_ONLY_VIEW = "  tree  ";
    private static final String TABLE_VIEW = " members";
    private static final String COMMENT_VIEW = "comments";

    private Box verticalBox;
    private Box horizontalBox;

    private boolean showPromotionCheckBoxes = false;
    private JButton approveButton;
    private JButton disapproveButton;
    private JLabel filterLabel = new JLabel("Filter view:");
    private JComboBox filterComboBox;
    private JTableWithDragImage refsetTable;
    private TableColumn checkBoxColumn;
    private Set<Object> promotionTypes = new HashSet<Object>();
    private JCheckBox selectAllCheckBox;
    private CheckBoxHeaderRenderer checkBoxHeaderRenderer;
    private SelectAllCheckBoxListener selectAllCheckBoxListener;

    public RefsetSpecPanel(ACE ace) throws Exception {
        super(new GridBagLayout());
        aceFrameConfig = ace.getAceFrameConfig();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);

        TermTreeHelper hierarchicalTreeHelper =
                new TermTreeHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig(), new IntSet(), false), ace);

        TermTreeHelper refsetAndParentOnlyTreeHelper =
                new TermTreeHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig(), new IntSet(), true), ace);

        editor = new RefsetSpecEditor(ace, hierarchicalTreeHelper, refsetAndParentOnlyTreeHelper, this);
        split.setTopComponent(editor.getContentPanel());

        ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", refsetAndParentOnlyTreeHelper);
        ace.getAceFrameConfig().addPropertyChangeListener("commit", refsetAndParentOnlyTreeHelper);
        editor.getLabel().addTermChangeListener(refsetAndParentOnlyTreeHelper);

        ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", hierarchicalTreeHelper);
        ace.getAceFrameConfig().addPropertyChangeListener("commit", hierarchicalTreeHelper);
        editor.getLabel().addTermChangeListener(hierarchicalTreeHelper);

        bottomTabs = new JTabbedPane();
        bottomTabs.addTab(HIERARCHICAL_VIEW, hierarchicalTreeHelper.getHierarchyPanel());
        bottomTabs.addTab(REFSET_AND_PARENT_ONLY_VIEW, refsetAndParentOnlyTreeHelper.getHierarchyPanel());

        verticalBox = new Box(BoxLayout.Y_AXIS);
        bottomTabs.addTab(TABLE_VIEW, verticalBox);
        bottomTabs.addTab(COMMENT_VIEW, new JScrollPane());
        commentTableModel = setupCommentTable();
        setupRefsetMemberTable(commentTableModel);
        editor.getLabel().setTermComponent(editor.getTermComponent());
        editor.addHistoryActionListener(new HistoryActionListener());

        split.setBottomComponent(bottomTabs);
        split.setDividerLocation(200);

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.BOTH;

        add(editor.getTopPanel(), c);
        c.gridy++;
        c.weightx = 1.0;
        c.weighty = 1.0;
        add(split, c);
        refsetTableModel.propertyChange(null);
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
        TableSorter sorter = (TableSorter) commentTable.getModel();
        return (ReflexiveRefsetCommentTableModel) sorter.getTableModel();
    }

    protected static JTableWithDragImage createCommentTable(I_ConfigAceFrame aceFrameConfig, RefsetSpecEditor editor)
            throws NoSuchMethodException, Exception {
        List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
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
        column2.setColumnName("comment");
        column2.setCreationEditable(true);
        column2.setUpdateEditable(true);
        column2.setFieldClass(String.class);
        column2.setMin(5);
        column2.setPref(50);
        column2.setMax(150);
        column2.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
        column2.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getStringValue"));
        column2.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setStringValue", String.class));
        column2.setType(REFSET_FIELD_TYPE.STRING);
        columns.add(column2);

        ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();
        column3.setColumnName("status");
        column3.setCreationEditable(false);
        column3.setUpdateEditable(false);
        column3.setFieldClass(String.class);
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
            column4.setColumnName("version");
            column4.setCreationEditable(false);
            column4.setUpdateEditable(false);
            column4.setFieldClass(Number.class);
            column4.setMin(5);
            column4.setPref(150);
            column4.setMax(150);
            column4.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
            column4.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getVersion"));
            column4.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setVersion", int.class));
            column4.setType(REFSET_FIELD_TYPE.VERSION);
            columns.add(column4);

            ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();
            column5.setColumnName("path");
            column5.setCreationEditable(false);
            column5.setUpdateEditable(false);
            column5.setFieldClass(String.class);
            column5.setMin(5);
            column5.setPref(150);
            column5.setMax(150);
            column5.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
            column5.setReadMethod(REFSET_TYPES.STRING.getPartClass().getMethod("getPathId"));
            column5.setWriteMethod(REFSET_TYPES.STRING.getPartClass().getMethod("setPathId", int.class));
            column5.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
            columns.add(column5);
        }

        ReflexiveRefsetCommentTableModel commentTableModel =
                new ReflexiveRefsetCommentTableModel(editor, columns.toArray(new ReflexiveRefsetFieldData[columns
                    .size()]));
        aceFrameConfig.addPropertyChangeListener("viewPositions", commentTableModel);
        aceFrameConfig.addPropertyChangeListener("commit", commentTableModel);
        editor.getLabel().addTermChangeListener(commentTableModel);

        commentTableModel.setComponentId(Integer.MIN_VALUE);
        commentTableModel.getRowCount();
        TableSorter sortingTable = new TableSorter(commentTableModel);

        JTableWithDragImage commentTable = new JTableWithDragImage(sortingTable);
        commentTable.getColumnModel().getColumn(0).setIdentifier(column1);
        commentTable.getColumnModel().getColumn(1).setIdentifier(column2);

        sortingTable.setTableHeader(commentTable.getTableHeader());
        sortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");

        ExtTableRenderer renderer = new ExtTableRenderer();
        commentTable.setDefaultRenderer(StringWithExtTuple.class, renderer);
        commentTable.setDefaultRenderer(Number.class, renderer);
        commentTable.setDefaultRenderer(Boolean.class, renderer);
        commentTable.setDefaultRenderer(Integer.class, renderer);
        commentTable.setDefaultRenderer(Double.class, renderer);
        commentTable.setDefaultRenderer(String.class, renderer);
        return commentTable;
    }

    public void setupRefsetMemberTable(ReflexiveRefsetCommentTableModel commentTableModel)
            throws NoSuchMethodException, Exception {
        List<ReflexiveRefsetFieldData> columns = new ArrayList<ReflexiveRefsetFieldData>();
        ReflexiveRefsetFieldData column1 = new ReflexiveRefsetFieldData();
        column1.setColumnName("referenced component");
        column1.setCreationEditable(true);
        column1.setUpdateEditable(true);
        column1.setFieldClass(Number.class);
        column1.setMin(5);
        column1.setPref(150);
        column1.setMax(1000);
        column1.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.CONCEPT_COMPONENT);
        column1
            .setReadMethod(I_GetConceptData.class.getMethod("getDescTuple", I_IntList.class, I_ConfigAceFrame.class));
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
        column2.setFieldClass(Number.class);
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
        column3.setFieldClass(String.class);
        column3.setMin(5);
        column3.setPref(150);
        column3.setMax(150);
        column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
        column3.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getStatusId"));
        column3.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setStatusId", int.class));
        column3.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
        columns.add(column3);

        ReflexiveRefsetFieldData column4 = new ReflexiveRefsetFieldData();
        column4.setColumnName("promotion status");
        column4.setCreationEditable(false);
        column4.setUpdateEditable(false);
        column4.setFieldClass(String.class);
        column4.setMin(5);
        column4.setPref(150);
        column4.setMax(150);
        column4.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PROMOTION_REFSET_PART);
        column4.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getC1id"));
        column4.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setC1id", int.class));
        column4.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
        columns.add(column4);

        if (editor.getShowHistory()) {
            ReflexiveRefsetFieldData column5 = new ReflexiveRefsetFieldData();
            column5.setColumnName("version");
            column5.setCreationEditable(false);
            column5.setUpdateEditable(false);
            column5.setFieldClass(Number.class);
            column5.setMin(5);
            column5.setPref(150);
            column5.setMax(150);
            column5.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
            column5.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getVersion"));
            column5.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setVersion", int.class));
            column5.setType(REFSET_FIELD_TYPE.VERSION);
            columns.add(column5);

            ReflexiveRefsetFieldData column6 = new ReflexiveRefsetFieldData();
            column6.setColumnName("path");
            column6.setCreationEditable(false);
            column6.setUpdateEditable(false);
            column6.setFieldClass(String.class);
            column6.setMin(5);
            column6.setPref(150);
            column6.setMax(150);
            column6.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
            column6.setReadMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("getPathId"));
            column6.setWriteMethod(REFSET_TYPES.CONCEPT.getPartClass().getMethod("setPathId", int.class));
            column6.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
            columns.add(column6);
        }

        refsetTableModel =
                new SelectableReflexiveTableModel(editor, columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));

        aceFrameConfig.addPropertyChangeListener("viewPositions", refsetTableModel);
        aceFrameConfig.addPropertyChangeListener("commit", refsetTableModel);
        editor.getLabel().addTermChangeListener(refsetTableModel);

        refsetTableModel.setComponentId(Integer.MIN_VALUE);
        refsetTableModel.setPromotionFilterId(null);
        refsetTableModel.getRowCount();
        TableSorter sortingTable = new TableSorter(refsetTableModel);

        refsetTable = new JTableWithDragImage(sortingTable);
        refsetTable.getColumnModel().getColumn(0).setIdentifier(column1);
        refsetTable.getColumnModel().getColumn(1).setIdentifier(column2);

        // set renderer and editor for checkbox column
        // get the last column
        int columnIndex = refsetTable.getColumnModel().getColumnCount() - 1;

        CheckBoxCellRenderer checkBoxRenderer = new CheckBoxCellRenderer();
        sortingTable.setTableHeader(refsetTable.getTableHeader());
        sortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");

        selectAllCheckBoxListener = new SelectAllCheckBoxListener();
        checkBoxHeaderRenderer =
                new CheckBoxHeaderRenderer(selectAllCheckBoxListener, this, refsetTable.getTableHeader());
        // refsetTable.getTableHeader();

        checkBoxColumn = refsetTable.getColumnModel().getColumn(columnIndex);
        checkBoxColumn.setHeaderRenderer(checkBoxHeaderRenderer);
        checkBoxColumn.setResizable(false);

        checkBoxColumn.setMaxWidth(checkBoxHeaderRenderer.getPreferredWidth());
        checkBoxColumn.setMinWidth(checkBoxHeaderRenderer.getPreferredWidth());
        checkBoxColumn.setPreferredWidth(checkBoxHeaderRenderer.getPreferredWidth());
        checkBoxColumn.setCellRenderer(checkBoxRenderer);

        // hide column as default (should only be visible during promotions BP)
        refsetTable.getColumnModel().removeColumn(checkBoxColumn);
        refsetTable.addMouseListener(new MouseClickListener());

        ExtTableRenderer renderer = new ExtTableRenderer();
        refsetTable.setDefaultRenderer(StringWithExtTuple.class, renderer);
        refsetTable.setDefaultRenderer(Number.class, renderer);
        refsetTable.setDefaultRenderer(Boolean.class, renderer);
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

                filterLabel = new JLabel("Filter view:");
                promotionTypes = new HashSet<Object>();
                promotionTypes.add("All");
                promotionTypes.add(Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION.getUids()));
                promotionTypes.add(Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION.getUids()));
                promotionTypes.add(Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION.getUids()));
                promotionTypes.add(Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION.getUids()));
                promotionTypes.add(Terms.get().getConcept(
                    ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION.getUids()));
                promotionTypes.add(Terms.get().getConcept(
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

                break;
            }
        }
        List<ReflexiveTableModel> commentTableModels = new ArrayList<ReflexiveTableModel>();
        commentTableModels.add(editor.getCommentTableModel());
        commentTableModels.add(commentTableModel);
        refsetTable.addMouseListener(new MemberTablePopupListener(refsetTable, aceFrameConfig, commentTableModels));
    }

    public I_GetConceptData getRefsetInSpecEditor() {
        return (I_GetConceptData) editor.getTermComponent();
    }

    public I_ExtendByRef getSelectedRefsetClauseInSpecEditor() {
        return getSelectedRefsetClauseInSpecEditor();
    }

    public JTree getTreeInSpecEditor() {
        return editor.getTreeInSpecEditor();
    }

    public I_GetConceptData getRefsetSpecInSpecEditor() throws IOException, TerminologyException {
        return editor.getRefsetSpecInSpecEditor();
    }

    public void setRefsetInSpecEditor(I_GetConceptData refset) {
        editor.setTermComponent(refset);
    }

    public ReflexiveRefsetCommentTableModel getCommentTableModel() {
        return commentTableModel;
    }

    public RefsetSpecEditor getRefsetSpecEditor() {
        return editor;
    }

    private class DisapproveActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {

                I_TermFactory tf = Terms.get();

                int currentNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
                IntSet currentSet = new IntSet();
                currentSet.add(currentNid);
                Set<Integer> tupleMemberIds = refsetTableModel.getSelectedTuples();

                PathSetReadOnly promotionPath = new PathSetReadOnly(aceFrameConfig.getPromotionPathSet());
                for (Integer tupleMemberId : tupleMemberIds) {
                    I_ExtendByRef tupleVersioned = tf.getExtension(tupleMemberId);
                    for (I_ExtendByRef extForMember : tf.getAllExtensionsForComponent(tupleVersioned
                        .getComponentId())) {
                        RefsetSpec helper = new RefsetSpec(getRefsetSpecInSpecEditor());
                        int promotionRefsetId = helper.getPromotionRefsetConcept().getConceptId();
                        if (promotionRefsetId == extForMember.getRefsetId()) {
                            List<? extends I_ExtendByRefVersion> promotionTuples =
                                    extForMember.getTuples(aceFrameConfig.getAllowedStatus(), aceFrameConfig
                                        .getViewPositionSetReadOnly(), false, false);
                            if (promotionTuples.size() > 0) {
                                I_ExtendByRefPart promotionPart = promotionTuples.get(0).getMutablePart();
                                if (promotionPart instanceof I_ExtendByRefPartCid) {

                                    for (I_Path p : aceFrameConfig.getEditingPathSet()) {

                                        int approveId;

                                        I_ExtendByRefPartCid clone =
                                                (I_ExtendByRefPartCid) promotionPart.makeAnalog(promotionPart
                                                    .getStatusId(), p.getConceptId(), Long.MAX_VALUE);

                                        if (clone.getC1id() == ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION
                                            .localize().getNid()) {
                                            break;
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION
                                            .localize().getNid()) {
                                            break;
                                        } else {
                                            break;
                                        }

                                        clone.setC1id(approveId);
                                        extForMember.addVersion(clone);
                                        tf.addUncommittedNoChecks(extForMember);
                                        extForMember.promote(new Position(Integer.MAX_VALUE, p), promotionPath,
                                            currentSet);
                                        tf.addUncommittedNoChecks(extForMember);
                                    }
                                }
                            }
                        }
                    }
                }

                tf.commit();

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            refsetTableModel.clearSelectedTuples();
            selectAllCheckBox.setSelected(false);
            refsetTable.getTableHeader().repaint();
        }
    }

    private class ApproveActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            try {

                I_TermFactory tf = Terms.get();

                int currentNid = ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid();
                IntSet currentSet = new IntSet();
                currentSet.add(currentNid);
                Set<Integer> tupleMemberIds = refsetTableModel.getSelectedTuples();

                PathSetReadOnly promotionPath = new PathSetReadOnly(aceFrameConfig.getPromotionPathSet());
                for (Integer tupleMemberId : tupleMemberIds) {
                    I_ExtendByRef tupleVersioned = tf.getExtension(tupleMemberId);
                    for (I_ExtendByRef extForMember : tf.getAllExtensionsForComponent(tupleVersioned
                        .getComponentId())) {
                        RefsetSpec helper = new RefsetSpec(getRefsetSpecInSpecEditor());
                        int promotionRefsetId = helper.getPromotionRefsetConcept().getConceptId();
                        if (promotionRefsetId == extForMember.getRefsetId()) {
                            List<? extends I_ExtendByRefVersion> promotionTuples =
                                    extForMember.getTuples(aceFrameConfig.getAllowedStatus(), aceFrameConfig
                                        .getViewPositionSetReadOnly(), false, false);
                            if (promotionTuples.size() > 0) {
                                I_ExtendByRefPart promotionPart = promotionTuples.get(0).getMutablePart();
                                if (promotionPart instanceof I_ExtendByRefPartCid) {
                                    for (I_Path p : aceFrameConfig.getEditingPathSet()) {

                                        int approveId;

                                        I_ExtendByRefPartCid clone =
                                                (I_ExtendByRefPartCid) promotionPart.makeAnalog(promotionPart
                                                    .getStatusId(), p.getConceptId(), Long.MAX_VALUE);

                                        if (clone.getC1id() == ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_ADDITION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.UNREVIEWED_NEW_DELETION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION
                                            .localize().getNid()) {
                                            break;
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION
                                            .localize().getNid()) {
                                            break;
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_ADDITION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_ADDITION
                                                        .localize().getNid();
                                        } else if (clone.getC1id() == ArchitectonicAuxiliary.Concept.REVIEWED_NOT_APPROVED_DELETION
                                            .localize().getNid()) {
                                            approveId =
                                                    ArchitectonicAuxiliary.Concept.REVIEWED_APPROVED_DELETION
                                                        .localize().getNid();
                                        } else {
                                            break;
                                        }

                                        clone.setC1id(approveId);
                                        extForMember.addVersion(clone);
                                        tf.addUncommittedNoChecks(extForMember);
                                        extForMember.promote(new Position(Integer.MAX_VALUE, p), promotionPath,
                                            currentSet);
                                        tf.addUncommittedNoChecks(extForMember);
                                    }
                                }
                            }
                        }
                    }
                }

                tf.commit();

            } catch (Exception e1) {
                e1.printStackTrace();
            }

            refsetTableModel.clearSelectedTuples();
            selectAllCheckBox.setSelected(false);
            refsetTable.getTableHeader().repaint();
        }
    }

    public boolean getShowPromotionCheckBoxes() {
        return showPromotionCheckBoxes;
    }

    public void setShowPromotionCheckBoxes(Boolean show) {
        if (showPromotionCheckBoxes != show) {
            showPromotionCheckBoxes = show;
            refsetTableModel.setShowPromotionCheckBoxes(show);
            setShowButtons(show);
        }
    }

    public void setShowButtons(boolean show) {
        approveButton.setVisible(show);
        disapproveButton.setVisible(show);
        filterLabel.setVisible(show);
        filterComboBox.setVisible(show);

        if (show) {
            refsetTable.getColumnModel().addColumn(checkBoxColumn);
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
            horizontalBox.setMaximumSize(new Dimension(0, 0));
            horizontalBox.setMinimumSize(new Dimension(0, 0));
            horizontalBox.setPreferredSize(new Dimension(0, 0));
            if (refsetTable.getParent() != null) {
                refsetTable.getParent().validate();
            }
        }
    }

    private Integer getSelectedPromotionFilter() {
        Object o = filterComboBox.getSelectedItem();
        if (o == null) {
            return null;
        } else if (o.equals("All")) {
            return null;
        } else {
            I_GetConceptData concept = (I_GetConceptData) o;
            return concept.getConceptId();
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

    private class MouseClickListener extends MouseAdapter {
        public void mouseClicked(MouseEvent mouseEvent) {
            if (getShowPromotionCheckBoxes()) {
                int checkedCount = 0;
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
        }
    }

    public JCheckBox getRendererComponent() {
        return selectAllCheckBox;
    }

    public void setRendererComponent(JCheckBox rendererComponent) {
        this.selectAllCheckBox = rendererComponent;
    }
}