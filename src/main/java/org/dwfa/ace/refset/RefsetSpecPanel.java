package org.dwfa.ace.refset;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import org.dwfa.ace.table.JTableWithDragImage;
import org.dwfa.ace.table.refset.ExtTableRenderer;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;

public class RefsetSpecPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	RefsetSpecEditor editor;

	public RefsetSpecPanel(ACE ace) throws Exception {
		super(new GridBagLayout());
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setOneTouchExpandable(true);
		TermTreeHelper treeHelper = new TermTreeHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig()));
		editor = new RefsetSpecEditor(ace, treeHelper);
		split.setTopComponent(editor.getContentPanel());
		
		ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", treeHelper);
		ace.getAceFrameConfig().addPropertyChangeListener("commit", treeHelper);
		editor.getLabel().addTermChangeListener(treeHelper);
		
		JTabbedPane bottomTabs = new JTabbedPane();
		bottomTabs.addTab("hierarchical view", treeHelper.getHierarchyPanel());
			
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
		// getDescTuple(I_IntList treeDescPreferenceList, I_ConfigAceFrame config)
		column1.setReadMethod(I_GetConceptData.class.getMethod("getDescTuple", I_IntList.class, I_ConfigAceFrame.class));
		List<Object> parameters = new ArrayList<Object>();
		parameters.add(ace.getAceFrameConfig().getTableDescPreferenceList());
		parameters.add(ace.getAceFrameConfig());
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

		ReflexiveRefsetTableModel refsetTableModel = new ReflexiveRefsetTableModel(editor,
    			columns.toArray(new ReflexiveRefsetFieldData[columns.size()]));
		ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", refsetTableModel);
		ace.getAceFrameConfig().addPropertyChangeListener("commit", refsetTableModel);
		editor.getLabel().addTermChangeListener(refsetTableModel);
		
		refsetTableModel.setComponentId(Integer.MIN_VALUE);
		refsetTableModel.getRowCount();
		TableSorter sortingTable = new TableSorter(refsetTableModel);

		JTableWithDragImage refsetTable = new JTableWithDragImage(sortingTable);
		refsetTable.getColumnModel().getColumn(0).setIdentifier(column1);
		refsetTable.getColumnModel().getColumn(1).setIdentifier(column2);
		
		ExtTableRenderer renderer = new ExtTableRenderer();
		refsetTable.setDefaultRenderer(StringWithExtTuple.class, renderer);
		refsetTable.setDefaultRenderer(Number.class, renderer);
		refsetTable.setDefaultRenderer(Boolean.class, renderer);
		refsetTable.setDefaultRenderer(Integer.class, renderer);
		refsetTable.setDefaultRenderer(Double.class, renderer);
		refsetTable.setDefaultRenderer(String.class, renderer);

		
		bottomTabs.addTab("table view", new JScrollPane(refsetTable));
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

	public I_GetConceptData getRefsetSpecInSpecEditor() {
		return editor.getRefsetSpecInSpecEditor();
	}
}
