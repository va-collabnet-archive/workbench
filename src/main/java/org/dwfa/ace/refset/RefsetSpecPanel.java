package org.dwfa.ace.refset;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData;
import org.dwfa.ace.table.refset.ReflexiveRefsetTableModel;
import org.dwfa.ace.table.refset.StringWithExtTuple;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.INVOKE_ON_OBJECT_TYPE;
import org.dwfa.ace.table.refset.ReflexiveRefsetFieldData.REFSET_FIELD_TYPE;
import org.dwfa.ace.tree.TermTreeHelper;
import org.dwfa.bpa.util.TableSorter;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;

public class RefsetSpecPanel extends JPanel {
	private class HistoryActionListener implements ActionListener {

		public void actionPerformed(ActionEvent arg0) {
			try {
				setupRefsetTable();
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
	
	RefsetSpecEditor editor;

	private JTabbedPane bottomTabs;

	private I_ConfigAceFrame aceFrameConfig;
	
	private static final String HIERARCHICAL_VIEW = "hierarchical view";
	private static final String TABLE_VIEW = "table view";

	public RefsetSpecPanel(ACE ace) throws Exception {
		super(new GridBagLayout());
		aceFrameConfig = ace.getAceFrameConfig();
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setOneTouchExpandable(true);
		TermTreeHelper treeHelper = new TermTreeHelper(new RefsetSpecFrameConfig(ace.getAceFrameConfig()));
		editor = new RefsetSpecEditor(ace, treeHelper);
		split.setTopComponent(editor.getContentPanel());
		
		ace.getAceFrameConfig().addPropertyChangeListener("viewPositions", treeHelper);
		ace.getAceFrameConfig().addPropertyChangeListener("commit", treeHelper);
		editor.getLabel().addTermChangeListener(treeHelper);
		
		bottomTabs = new JTabbedPane();
		bottomTabs.addTab(HIERARCHICAL_VIEW, treeHelper.getHierarchyPanel());
			
		bottomTabs.addTab(TABLE_VIEW, new JScrollPane());
    	setupRefsetTable();
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
		
	}

	public void setupRefsetTable()
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
		
		if (editor.getShowHistory()) {
        	ReflexiveRefsetFieldData column3 = new ReflexiveRefsetFieldData();
        	column3.setColumnName("version");
        	column3.setCreationEditable(false);
        	column3.setUpdateEditable(false);
        	column3.setFieldClass(Number.class);
        	column3.setMin(5);
        	column3.setPref(150);
        	column3.setMax(150);
        	column3.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
        	column3.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getVersion"));
        	column3.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setVersion", int.class));
        	column3.setType(REFSET_FIELD_TYPE.VERSION);
        	columns.add(column3);

        	ReflexiveRefsetFieldData column4 = new ReflexiveRefsetFieldData();
        	column4.setColumnName("path");
        	column4.setCreationEditable(false);
        	column4.setUpdateEditable(false);
        	column4.setFieldClass(String.class);
        	column4.setMin(5);
        	column4.setPref(150);
        	column4.setMax(150);
        	column4.setInvokeOnObjectType(INVOKE_ON_OBJECT_TYPE.PART);
        	column4.setReadMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("getPathId"));
        	column4.setWriteMethod(EXT_TYPE.CONCEPT.getPartClass().getMethod("setPathId", int.class));
        	column4.setType(REFSET_FIELD_TYPE.CONCEPT_IDENTIFIER);
        	columns.add(column4);			
		}

		ReflexiveRefsetTableModel refsetTableModel = new ReflexiveRefsetTableModel(editor,
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
		sortingTable.getTableHeader()
                .setToolTipText("Click to specify sorting; Control-Click to specify secondary sorting");

		
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
