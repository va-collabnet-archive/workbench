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
package org.ihtsdo.ace.table;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.table.StringWithTuple;
import org.dwfa.ace.table.ConceptAttributeTableModel.StringWithConceptTuple;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;

public class WorkflowHistoryTableModel extends DefaultTableModel {
	
    private static final long serialVersionUID = 1L;

 	    public enum WORKFLOW_FIELD { 
 	    	FSN("FSN", 5, 400, 600), EDITOR("Editor", 5, 150, 180), 
 	    	STATE("State", 5, 115, 150), TIMESTAMP("TimeStamp", 5, 75, 200);

 /*
  *  FSN("FSN", 5, 400, 600), 	  	STATE("State", 5, 115, 150), 	ACTION("Action", 5, 115, 150), 	  
  *	 EDITOR("Editor", 5, 150, 180), 	PATH("Path", 5, 115, 150), 		TIMESTAMP("TimeStamp", 5, 75, 200);
  */
 
  	        private int min;
 	        private int pref;
 	        private int max;
 	        private String columnName;

 	        private WORKFLOW_FIELD(String columnName, int min, int pref, int max) {
 	            this.columnName = columnName;
 	            this.min = min;
 	            this.pref = pref;
 	            this.max = max;
 	        }

 	        public String getColumnName() {
 	            return columnName;
 	        }

 	        public int getMax() {
 	            return max;
 	        }

 	        public int getMin() {
 	            return min;
 	        }

 	        public int getPref() {
 	            return pref;
 	        }
 	    }

 	    private WORKFLOW_FIELD[] columns;


// 	    private SmallProgressPanel progress = new SmallProgressPanel();

 	    private I_ConfigAceFrame config;
 	    private DateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

 	    public WorkflowHistoryTableModel(WORKFLOW_FIELD[] columns, I_ConfigAceFrame config) {
 	        super();
 	        this.columns = columns;
 	        this.config = config;
 	    }

 	    public final void setColumns(WORKFLOW_FIELD[] columns) {
 	        if (this.columns.length != columns.length) {
 	            this.columns = columns;
 	            fireTableStructureChanged();
 	            return;
 	        }
 	        for (int i = 0; i < columns.length; i++) {
 	            if (columns[i].equals(this.columns[i]) == false) {
 	                this.columns = columns;
 	                fireTableStructureChanged();
 	                return;
 	            }
 	        }
 	    }

 	    public int getColumnCount() {
 	        return columns.length;
 	    }

    public WorkflowHistoryTableModel(String[][] data, String[] columns) {
        super(data, columns);
        wfHistoryList = new ArrayList<WorkflowHistoryJavaBean>();
    }

    private String getPrefText(UUID id) throws IOException, TerminologyException {
        I_GetConceptData cb = Terms.get().getConcept(id);
        I_DescriptionTuple desc = cb.getDescTuple(config.getTableDescPreferenceList(), config);
        if (desc != null) {
            return desc.getText();
        }
        return cb.getInitialText() + " null pref desc";
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        try {
            if (rowIndex >= getRowCount() || rowIndex < 0 || columnIndex < 0 || columnIndex >= getColumnCount()) {
                return null;
            }
            WorkflowHistoryJavaBean bean = wfHistoryList.get(rowIndex);

//            boolean inConflict = config.getHighlightConflictsInComponentPanel()
//                && config.getConflictResolutionStrategy().isInConflict((I_DescriptionVersioned) desc.getFixedPart());

            if (bean == null) {
                return null;
            }

            switch (columns[columnIndex]) {
            case FSN:
            	I_GetConceptData concept = Terms.get().getConcept(bean.getConcept());
         	 	// Get Latest Version of the FSN
         	 	final String fsn =  getLatestFSNVersion(concept);
            	I_ConceptAttributeTuple tuple = (I_ConceptAttributeTuple) concept.getConceptAttributes().getTuples().get(0);
                return new WorkflowFSNWithConceptTuple(fsn, tuple, false);
/*
 *          case ACTION:
 *               return new WorkflowTextFieldEditor(getPrefText(bean.getAction()), false);
 */
            case STATE:
                return new WorkflowTextFieldEditor(getPrefText(bean.getState()), false);
            case EDITOR:
                return new WorkflowTextFieldEditor(getPrefText(bean.getModeler()), false);
/*
 *              case PATH:
 *           	I_GetConceptData path = Terms.get().getConcept(bean.getPath());
 *           	I_ConceptAttributeTuple pTuple = path.getConceptAttributes().getTuples().get(0);
 *               return new WorkflowFSNWithConceptTuple(getPrefText(bean.getPath()), pTuple, false);
 */
            case TIMESTAMP:
            	Date d = new Date(bean.getWorkflowTime());
            	String timeStamp = formatter.format(d);
            	return new WorkflowTextFieldEditor(timeStamp, false);
            }
               
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }
    
    private String getLatestFSNVersion(I_GetConceptData concept) {
		try {
			for (I_DescriptionVersioned<?> descv: concept.getDescriptions()) {
				for (I_DescriptionTuple p: descv.getTuples()) {
					if (p.getTypeNid() == Terms.get().getConcept(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()).getNid())
						return descv.getLastTuple().getText();
		   		}
	   		}
		} catch (Exception e) {
        	AceLog.getAppLog().log(Level.SEVERE, "Error in identifying current concept's FSN", e);
		}

		return "";
    }

    private ArrayList<WorkflowHistoryJavaBean> wfHistoryList = new ArrayList<WorkflowHistoryJavaBean>();
    
    public WorkflowHistoryJavaBean getBean(int rowIndex) {
        if (rowIndex < 0 || wfHistoryList == null || rowIndex == wfHistoryList.size()) {
            return null;
        }
        return wfHistoryList.get(rowIndex);
    }

    public int getRowCount() {
    	if (wfHistoryList == null)
    		return 0;
    	else
    		return wfHistoryList.size();
    }

    public String getColumnName(int col) {
        return columns[col].getColumnName();
    }

    public boolean isCellEditable(int row, int col) {
        if (ACE.editMode == false) {
            return false;
        }

        if (row < 0 || row >= getRowCount()) {
            return false;
        }

        if (AceLog.getAppLog().isLoggable(Level.FINER)) {
            AceLog.getAppLog().finer("Cell is editable: " + row + " " + col);
        }

        return true;
    }

    public void setValueAt(Object value, int row, int col) {
        try {
            WorkflowHistoryJavaBean bean = getBean(row);
            boolean changed = false;

            switch (columns[col]) {
                case FSN:
//                case ACTION:
                case STATE:
                case EDITOR:
//                case PATH:
                case TIMESTAMP:
                	break;
                }
                fireTableDataChanged();

                if (changed) {
                    AceLog.getAppLog().info("Description table changed");
                    //updateDataAlerts(row);
                    //Terms.get().addUncommitted(Terms.get().getConcept(desc.getConceptNid()));
               }
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    public Class<?> getColumnClass(int c) {
        switch (columns[c]) {
        case FSN:
            return WorkflowFSNWithConceptTuple.class;
           
//        case ACTION:
//            return WorkflowTextFieldEditor.class;
        case STATE:
            return WorkflowTextFieldEditor.class;
        case EDITOR:
            return WorkflowTextFieldEditor.class;
//        case PATH:
//            return WorkflowFSNWithConceptTuple.class;
        case TIMESTAMP:
            return WorkflowTextFieldEditor.class;
        }
        return String.class;
    }

//    public SmallProgressPanel getProgress() {
//        return progress;
//    }
//
//    public void setProgress(SmallProgressPanel progress) {
//        this.progress = progress;
//    }
    

    
    
    
    
    
    public void setWfHxBeans(SortedSet<WorkflowHistoryJavaBean> beans) {
    	wfHistoryList = new ArrayList<WorkflowHistoryJavaBean>(beans);
 		String data[][] = new String[wfHistoryList.size()][]; 
 		WorkflowHistoryJavaBean bean = null;
 		Iterator<WorkflowHistoryJavaBean> itr = wfHistoryList.iterator();
 		int i = 0;
 		int errorConceptCount = 0;
 		int a = 0;
 		while (itr.hasNext())
 		{ 
 			try {
 				
 				bean = (WorkflowHistoryJavaBean)itr.next();
		 	 	I_GetConceptData concept = Terms.get().getConcept(bean.getConcept());
		 	 	
 				final String workflowId =  bean.getWorkflowId().toString();
         	 	final String action = Terms.get().getConcept(bean.getAction()).getInitialText();
         	 	final String conceptId = String.valueOf(concept.getConceptNid());
         	 	final String modeler = Terms.get().getConcept(bean.getModeler()).getInitialText();
         	 	final String path = Terms.get().getConcept(bean.getPath()).getInitialText();
         	 	final String state = Terms.get().getConcept(bean.getState()).getInitialText();
         	 	final Long timeStamp =  bean.getEffectiveTime();
         	 	final Long refsetColumnTimeStamp = bean.getWorkflowTime();
         	 	final String fsn = bean.getFSN();

         	 	String d[] = new String[] {workflowId, fsn, action, conceptId, modeler, path, state, timeStamp.toString(), refsetColumnTimeStamp.toString()}; 
         	 	data[i] = d;         			}
 			catch (Exception e) {
 				errorConceptCount++;
 				data[i] = new String[] {"", "", "", "", "", "", "", "", "", ""};
 			}
 			
 			i++;
 		}
 		
 		this.dataVector = convertToVector(data);
 
    	SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableChanged(new TableModelEvent(WorkflowHistoryTableModel.this));
            }
        });
    }

    protected int getWfHistoryCount() 
    {
        return wfHistoryList.size();
    }

    public static class WorkflowTextFieldEditor extends CellTextString <WorkflowTextFieldEditor>
    {
        boolean wrapLines;

        public WorkflowTextFieldEditor(String cellText, boolean wrapLines) {
            super(cellText);
            this.wrapLines = wrapLines;
        }

        public boolean getWrapLines() {
            return wrapLines;
        }

        public void setWrapLines(boolean wrapLines) {
            this.wrapLines = wrapLines;
        }
    }

    public WORKFLOW_FIELD[] getColumnEnums() {
        return columns;
    }


/*
 *     private class UpdateDataAlertsTimerTask extends TimerTask {
        boolean active = true;
        final int row;

        public UpdateDataAlertsTimerTask(int row) {
            super();
            this.row = row;
        }

        @Override
        public void run() {
            if (active) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (active) {
                            try {
                                I_DescriptionTuple desc = getDescription(row);
                                if (desc != null) {
                                    Terms.get().addUncommitted(Terms.get().getConcept(desc.getConceptNid()));
                                }
                            } catch (IOException e) {
                                AceLog.getAppLog().alertAndLogException(e);
                            } catch (TerminologyException e) {
                                AceLog.getAppLog().alertAndLogException(e);
							}
                        }
                    }
                });
            }
        }

        public void setActive(boolean active) {
            this.active = active;
        }

    }

    UpdateDataAlertsTimerTask alertUpdater;

    private void updateDataAlerts(int row) {
        if (alertUpdater != null) {
            alertUpdater.setActive(false);
        }
        alertUpdater = new UpdateDataAlertsTimerTask(row);
        UpdateAlertsTimer.schedule(alertUpdater, 2000);

    }


 */

    public static class WorkflowFSNWithConceptTuple extends StringWithTuple<StringWithConceptTuple> {
        String cellText;

        I_ConceptAttributeTuple tuple;

        public WorkflowFSNWithConceptTuple(String cellText, I_ConceptAttributeTuple tuple, boolean inConflict) {
            super(cellText, inConflict);
            this.tuple = tuple;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.dwfa.ace.table.I_CellTextWithTuple#getTuple()
         */
        public I_ConceptAttributeTuple getTuple() {
            return tuple;
        }
    }
}
