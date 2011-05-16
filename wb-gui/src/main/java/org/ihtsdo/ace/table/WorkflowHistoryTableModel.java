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
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Level;

import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

import org.dwfa.ace.ACE;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.search.LuceneMatch;
import org.dwfa.ace.search.workflow.LuceneWfHxMatch;
import org.dwfa.ace.table.StringWithTuple;
import org.dwfa.ace.table.ConceptAttributeTableModel.StringWithConceptTuple;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lucene.WorkflowLuceneSearchResult;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

public class WorkflowHistoryTableModel extends DefaultTableModel {
	
    private static final long serialVersionUID = 1L;
 
	private Integer searchResults = null;

	// First is most recent and Last is the oldest
    private ArrayList<WorkflowLuceneSearchResult> wfHistoryList = new ArrayList<WorkflowLuceneSearchResult>();

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
        wfHistoryList = new ArrayList<WorkflowLuceneSearchResult>();
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
            WorkflowLuceneSearchResult result = wfHistoryList.get(rowIndex);

            if (result == null) {
                return null;
            }

            switch (columns[columnIndex]) {
            case FSN:
            	I_GetConceptData concept = Terms.get().getConcept(UUID.fromString(result.getConcept()));
            	
            	// Attribute Tuple for drag
            	I_ConceptAttributeTuple tuple = (I_ConceptAttributeTuple) concept.getConceptAttributes().getTuples().get(0);
         	 	
            	// Get Pref to display
            	I_DescriptionTuple tupleToDisplay = concept.getDescTuple(config.getTableDescPreferenceList(), config);
            	DescriptionVersionBI versionToDisplay = tupleToDisplay.getVersion(config.getViewCoordinate());
            	String pref = versionToDisplay.getText();
            	
            	return new WorkflowFSNWithConceptTuple(pref, tuple, false);
/*
 *          case ACTION:
 *               return new WorkflowTextFieldEditor(getPrefText(bean.getAction()), false);
 */
            case STATE:
                return new WorkflowTextFieldEditor(getPrefText(UUID.fromString(result.getState())), false);
            case EDITOR:
                return new WorkflowTextFieldEditor(getPrefText(UUID.fromString(result.getModeler())), false);
/*
 *              case PATH:
 *           	I_GetConceptData path = Terms.get().getConcept(bean.getPath());
 *           	I_ConceptAttributeTuple pTuple = path.getConceptAttributes().getTuples().get(0);
 *               return new WorkflowFSNWithConceptTuple(getPrefText(bean.getPath()), pTuple, false);
 */
            case TIMESTAMP:
            	Date d = new Date(result.getTime());
            	String timeStamp = formatter.format(d);
            	return new WorkflowTextFieldEditor(timeStamp, false);
            }
               
        } catch (Exception e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
        return null;
    }
    
    public WorkflowLuceneSearchResult getBean(int rowIndex) {
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

    public void setWfHxBeans(Collection<LuceneMatch> luceneMatches) {
    	wfHistoryList = new ArrayList<WorkflowLuceneSearchResult>();

    	for (LuceneMatch match : luceneMatches) {
    		WorkflowLuceneSearchResult addingSearchResult = ((LuceneWfHxMatch)match).getDisplayValues();
    		
    		if (wfHistoryList.size() == 0) {
        		wfHistoryList.add(((LuceneWfHxMatch)match).getDisplayValues());
    		} else if (addingSearchResult.compareTo(wfHistoryList.get(0)) > 0) {
    			// Older than all in list, so add at top
	    		wfHistoryList.add(0, ((LuceneWfHxMatch)match).getDisplayValues());
    		} else {
    			int idx = -1;
	    		for (int i = 0; i < wfHistoryList.size() && idx < 0; i++) {
	    			WorkflowLuceneSearchResult testingSearchResult = wfHistoryList.get(i);
	    			
	    			if (addingSearchResult.compareTo(testingSearchResult) > 0) {
	    				idx = i;
	    			}
	    		}
	    		
	    		if (idx < 0) {
	    			// Add to end of list
	    			idx = wfHistoryList.size();
	    		}
	    		
	    		wfHistoryList.add(idx, ((LuceneWfHxMatch)match).getDisplayValues());
	    	}
    	}
    	
 		String data[][] = new String[wfHistoryList.size()][]; 
    	WorkflowLuceneSearchResult result = null;
 		Iterator<WorkflowLuceneSearchResult> itr = wfHistoryList.iterator();
 		int i = 0;
 		int errorConceptCount = 0;
 		
 		searchResults = luceneMatches.size();
 		
 		while (itr.hasNext())
 		{ 
 			try {
 				
 				result = itr.next();
 				
 				final String workflowId = null;
         	 	final String action = null;
         	 	final String conceptId = null;
         	 	final String path = null;

         	 	final String state = result.getState();
         	 	final String modeler = result.getModeler();
         	 	final String timeStamp =  null;
         	 	final String fsn = result.getFsn();
         	 	final Long refsetColumnTimeStamp = result.getTime();

         	 	String d[] = new String[] {workflowId, fsn, action, conceptId, modeler, path, state, timeStamp, refsetColumnTimeStamp.toString()}; 
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

    public static class WorkflowFSNWithConceptTuple extends StringWithTuple<StringWithConceptTuple> {
        String cellText;

        I_ConceptAttributeTuple tuple;

        public WorkflowFSNWithConceptTuple(String cellText, I_ConceptAttributeTuple tuple, boolean inConflict) {
            super(cellText, inConflict);
            this.tuple = tuple;
        }

        public I_ConceptAttributeTuple getTuple() {
            return tuple;
        }
    }

	public void clearResults() {
		searchResults = null;
	}

	public boolean hasMatches() {
		return searchResults != null;
	}
}
