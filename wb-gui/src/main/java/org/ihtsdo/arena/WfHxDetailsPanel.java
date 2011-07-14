package org.ihtsdo.arena;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.arena.conceptview.ConceptViewSettings;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.history.WorkflowHistoryRefsetReader;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

public class WfHxDetailsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	// Table-Based fields
	private static final String[] columnNames = {"Action", "State", "Modeler", "Timestamp"};
	private JTable table = null;
	private List<Boolean> newWfUidIndices;

	// Data Storage fields
	private I_GetConceptData currentConcept = null;
	private TreeSet<WorkflowHistoryJavaBean> conceptWfBeans;
	private long currentLatestTimestamp = Long.MIN_VALUE;


	public WfHxDetailsPanel(ConceptViewSettings settings) {
		super(new GridLayout(1,1));
		currentConcept = settings.getConcept();

		// Create Table
    	generateWfHxTable();
    	setupTablePanel();
    	
    	// Create & add Pane ScrollPane 
    	JScrollPane scrollPane = new JScrollPane(table);
    	scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	add(scrollPane);
	}

	private void setupTablePanel() {
    	TableColumn column = null;
    	table.setFillsViewportHeight(true);

    	// Setup Renderers
    	WfHxDetailsPanelRenderer renderer = new WfHxDetailsPanelRenderer(newWfUidIndices);
    	table.setDefaultRenderer(Object.class, renderer);  
    	table.getTableHeader().setDefaultRenderer(renderer);  

    	
    	/* Calculate Preferred tableWidth */
    	// Max width per column
    	int[] columnWidths = calculateTableWidth();
    	
    	// size of margin * (n+1 margins for n columns)
 		int marginWidth = table.getColumnModel().getColumnMargin() * (columnWidths.length + 1);

 		// Initialize with margin and 
 		int tableWidth = marginWidth;

    	for (int i = 0; i < table.getColumnCount(); i++) {
    		// Calculate tableWidth
        	tableWidth += columnWidths[i];

        	// Set Column's Preferred-Widths 
        	column = table.getColumnModel().getColumn(i);
	        column.setPreferredWidth((int)(columnWidths[i] * 1.25)); 
    	}
    	
    	
    	/* Calculate Preferred tableHeight */
    	// for # rows of data + header
    	int totalRows = getRowCount() + 1; 
    	
    	// size of rows
    	int rowHeight = table.getRowHeight() * totalRows;
    	
    	// size of margin *  ((n+1 margins for n data rows)  +  (1 for header's two margins)
    	int marginHeight = table.getRowMargin() * (totalRows + 2);
    	
    	// row Height + margin Height
    	int tableHeight = rowHeight + marginHeight;

    	
    	/* Set Panel Preferred-Size */
    	setBounds(0, 0, tableWidth, tableHeight);
	}

	private int[] calculateTableWidth() {
		TableColumnModel columns = table.getColumnModel();
 		TableModel data = table.getModel();
 		int counter = 0;
 		int[] columnWidths = new int[columns.getColumnCount()];

 		for (int column = 0; column < columns.getColumnCount(); column++) {
 			// In case header tag is longer than actual values
 			TableCellRenderer h = columns.getColumn(column).getHeaderRenderer();
 			if (h == null) {
 				h = table.getDefaultRenderer(Object.class);
 			}
 			
 			Component c = h.getTableCellRendererComponent(table, columns.getColumn(column).getHeaderValue(), false, false, -1, column);
 			int currentMaxWidth = (int)(c.getPreferredSize().width * 1.25);

 			for (int row = 0; row < table.getRowCount(); row++) {
 				TableCellRenderer r = table.getCellRenderer(row, column);
 				c = r.getTableCellRendererComponent(table, data.getValueAt(row, column), false, false, row, column);
 				currentMaxWidth = Math.max(currentMaxWidth, (int)(c.getPreferredSize().width * 1.25));
 			}	
 			
 			columnWidths[counter++] = currentMaxWidth;
 		}
 		
 		return columnWidths;
	}

	private void generateWfHxTable() {
        int counter = 0;
        boolean colorA = true;
        UUID currentWfUid = UUID.randomUUID();
        newWfUidIndices = new ArrayList<Boolean>();

        try {
			WorkflowHistoryRefsetReader reader = new WorkflowHistoryRefsetReader();

			conceptWfBeans = WorkflowHelper.getAllWorkflowHistory(currentConcept);
	        Object[][] data = new Object[conceptWfBeans.size()][];

			for (WorkflowHistoryJavaBean bean : conceptWfBeans) {
	        	String[] row = new String[4];
	        	
	        	String action = reader.processMetaForDisplay(Terms.get().getConcept(bean.getAction()));
	    		String state = reader.processMetaForDisplay(Terms.get().getConcept(bean.getState()));
	    		String modeler = Terms.get().getConcept(bean.getModeler()).getInitialText();
	    		String time = WorkflowHelper.format.format(new Date(bean.getWorkflowTime()));
	    		
	    		row[0] = action;
	    		row[1] = state;
	    		row[2] = modeler;
	    		row[3] = time;
	    		
	    		data[counter++] = row;
	        
	    		if (!bean.getWorkflowId().equals(currentWfUid)) {
	    			currentWfUid = bean.getWorkflowId();
	    			
	    			// new Wf Uid, flip color boolean
	    			colorA = !colorA;
	    		}

    			newWfUidIndices.add(colorA);
			}
	        
			// Create Table and set concept-specific fields
	        table = new JTable(data, columnNames);
	        currentLatestTimestamp = conceptWfBeans.last().getWorkflowTime();
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Cannot create WfHx Details Panel Table");
		}
	}

	public boolean isNewHtmlCodeRequired(I_GetConceptData arenaConcept) {
		boolean generateNewHtml = false;
		
		if (arenaConcept != null) {
			if (arenaConcept.getPrimUuid() != null) {
				// Proper Concept
				if (currentConcept == null || !arenaConcept.getPrimUuid().equals(currentConcept.getPrimUuid())) {
					generateNewHtml = true;
				}  
	
				try {
					WorkflowHistoryJavaBean latestBean = WorkflowHelper.getLatestWfHxJavaBeanForConcept(arenaConcept);
					
					if (latestBean != null) {
						// Greater means new, lesser means undo performed
						if (latestBean.getWorkflowTime() != currentLatestTimestamp) {
							generateNewHtml = true;
						}
					}
				} catch (Exception e) {
					AceLog.getAppLog().log(Level.WARNING, "Failure to identify latest WfHx for concept: " + arenaConcept);
				}
			} else {
				// Concept is not correct state (missing a PrimUid).
				// Therefore, generate new (blank) wfHx details, but do not 
				// assign the currentConcept nor currentLatestTimestamp.
				generateNewHtml = true;
			}
		}
		
		return generateNewHtml;
	}

	public int getRowCount() { 
		return conceptWfBeans.size();
	}
}
