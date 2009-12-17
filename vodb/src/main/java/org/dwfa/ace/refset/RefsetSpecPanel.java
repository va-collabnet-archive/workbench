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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.ExtTableRenderer;
import org.dwfa.ace.table.refset.ReflexiveRefsetCommentTableModel;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetTableModel;
import org.dwfa.ace.table.refset.ReflexiveTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.IntSet;

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

    public RefsetSpecPanel(ACE ace) throws Exception {
        super(new GridBagLayout());
        aceFrameConfig = ace.getAceFrameConfig();
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setOneTouchExpandable(true);

        TermTreeHelper hierarchicalTreeHelper = new TermTreeHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig(),
            new IntSet(), false), ace);

        TermTreeHelper refsetAndParentOnlyTreeHelper = new TermTreeHelper(new RefsetSpecFrameConfig(
            ace.getAceFrameConfig(), new IntSet(), true), ace);

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

        bottomTabs.addTab(TABLE_VIEW, new JScrollPane());
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
        column2.setReadMethod(EXT_TYPE.STRING.getPartClass().getMethod("getStringValue"));
        column2.setWriteMethod(EXT_TYPE.STRING.getPartClass().getMethod("setStringValue", String.class));
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
        column3.setReadMethod(EXT_TYPE.STRING.getPartClass().getMethod("getStatusId"));
        column3.setWriteMethod(EXT_TYPE.STRING.getPartClass().getMethod("setStatusId", int.class));
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
            column4.setReadMethod(EXT_TYPE.STRING.getPartClass().getMethod("getVersion"));
            column4.setWriteMethod(EXT_TYPE.STRING.getPartClass().getMethod("setVersion", int.class));
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
            column5.setReadMethod(EXT_TYPE.STRING.getPartClass().getMethod("getPathId"));
            column5.setWriteMethod(EXT_TYPE.STRING.getPartClass().getMethod("setPathId", int.class));
            column5.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
            columns.add(column5);
        }

        ReflexiveRefsetCommentTableModel commentTableModel = new ReflexiveRefsetCommentTableModel(editor,
            columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
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
        column1.setColumnName("referenced concept");
        column1.setCreationEditable(true);
        column1.setUpdateEditable(true);
        column1.setFieldClass(Number.class);
        column1.setMin(5);
        column1.setPref(150);
        column1.setMax(1000);
        column1.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.CONCEPT_COMPONENT);
        column1.setReadMethod(I_GetConceptData.class.getMethod("getDescTuple", I_IntList.class, I_ConfigAceFrame.class));
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
        column2.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getConceptId"));
        column2.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setConceptId", int.class));
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
        column3.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getStatusId"));
        column3.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setStatusId", int.class));
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
        column4.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getC1id"));
        column4.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setC1id", int.class));
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
            column5.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getVersion"));
            column5.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setVersion", int.class));
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
            column6.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getPathId"));
            column6.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setPathId", int.class));
            column6.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
            columns.add(column6);
        }

        refsetTableModel = new ReflexiveRefsetTableModel(editor,
            columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
        aceFrameConfig.addPropertyChangeListener("viewPositions", refsetTableModel);
        aceFrameConfig.addPropertyChangeListener("commit", refsetTableModel);
        editor.getLabel().addTermChangeListener(refsetTableModel);

        refsetTableModel.setComponentId(Integer.MIN_VALUE);
        refsetTableModel.getRowCount();
        TableSorter sortingTable = new TableSorter(refsetTableModel);

        JTableWithDragImage refsetTable = new JTableWithDragImage(sortingTable);
        refsetTable.getColumnModel().getColumn(0).setIdentifier(column1);
        refsetTable.getColumnModel().getColumn(1).setIdentifier(column2);

        sortingTable.setTableHeader(refsetTable.getTableHeader());
        sortingTable.getTableHeader().setToolTipText(
            "Click to specify sorting; Control-Click to specify secondary sorting");

        ExtTableRenderer renderer = new ExtTableRenderer();
        refsetTable.setDefaultRenderer(StringWithExtTuple.class, renderer);
        refsetTable.setDefaultRenderer(Number.class, renderer);
        refsetTable.setDefaultRenderer(Boolean.class, renderer);
        refsetTable.setDefaultRenderer(Integer.class, renderer);
        refsetTable.setDefaultRenderer(Double.class, renderer);
        refsetTable.setDefaultRenderer(String.class, renderer);
        for (int i = 0; i < bottomTabs.getTabCount(); i++) {
            if (bottomTabs.getTitleAt(i).equals(TABLE_VIEW)) {
                JScrollPane tableScroller = (JScrollPane) bottomTabs.getComponentAt(i);
                tableScroller.setViewportView(refsetTable);
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

    public I_ThinExtByRefVersioned getSelectedRefsetClauseInSpecEditor() {
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
}
