/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dwfa.ace.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import org.dwfa.ace.api.I_GetConceptData;
import org.ihtsdo.arena.conceptview.ConceptTemplates;
import org.ihtsdo.arena.drools.EditPanelKb;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 *
 * @author akf
 */
public class TerminologyTableModel extends AbstractTableModel implements 
        ListDataListener {

    CopyOnWriteArrayList<ConceptChronicleBI> elements = new CopyOnWriteArrayList<ConceptChronicleBI>();
    private ConceptCheckChangeListener listViewChangeListener = new ConceptCheckChangeListener();

    public enum MODEL_FIELD {

        CONCEPT("concepts"),
        TEMPLATE("templates"),
        DATA_CHECK("data checks");
        private String columnName;

        private MODEL_FIELD(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }
    }
    private MODEL_FIELD[] columns;
    private TerminologyList list;
    private EditPanelKb kb;

    public TerminologyTableModel() {
        super();
    }

    public TerminologyTableModel(TerminologyList list) {
        super();
        this.list = list;
        this.list.getModel().addListDataListener(this);
        kb = ConceptTemplates.getKb();
        Ts.get().addTermChangeListener(listViewChangeListener);
        for (I_GetConceptData c : list.getTerminologyModel().elements) {
            elements.add((ConceptChronicleBI) c);
            kb.setConcept(c);
        }

        columns = MODEL_FIELD.values();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int column) {
        return columns[column].getColumnName();
    }

    @Override
    public int getRowCount() {
        return elements.size();
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount() || row < 0 || col < 0) {

            return null;
        }

        switch (columns[col]) {
            case CONCEPT:
                return elements.get(row);
            case TEMPLATE:
                int nid = elements.get(row).getConceptNid();
                boolean hasTemplates = false;
                if (ConceptTemplates.templates.containsKey(
                        nid)) {
                    hasTemplates = ConceptTemplates.templates.get(nid);
                    if (hasTemplates == true) {
                        return "<html>&bull;";
                    }
                    return "";
                }
                return "";
            case DATA_CHECK:
                nid = elements.get(row).getConceptNid();
                boolean hasChecks = false;
                if (ConceptTemplates.dataChecks.containsKey(
                        nid)) {
                    hasChecks = ConceptTemplates.dataChecks.get(nid);
                    if (hasChecks == true) {
                        return "<html>&bull;";
                    }
                    return "";
                }
                return "";
        }
        return null;
    }

    public TerminologyList getList() {
        return list;
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        elements.add((ConceptChronicleBI) value);
        list.dataModel.addElement((I_GetConceptData) value);
        fireTableRowsUpdated(row, row);
    }

    public ConceptChronicleBI getElementAt(int index) {
        return elements.get(index);
    }

    public int getSize() {
        return elements.size();
    }

    public void addElement(ConceptChronicleBI concept) {
        list.dataModel.addElement((I_GetConceptData) concept);
    }

    public void addElement(int rowIndex, ConceptChronicleBI concept) {
        list.dataModel.addElement(rowIndex, (I_GetConceptData) concept);
    }

    public ConceptChronicleBI removeElement(int rowIndex) {
        ConceptChronicleBI concept = elements.get(rowIndex);
        list.dataModel.removeElement(rowIndex);
        return concept;
    }

    public void clear() {
        list.dataModel.clear();
    }

    //for list
    @Override
    public void intervalAdded(ListDataEvent lde) {
        int index = lde.getIndex0();
        I_GetConceptData newElement = list.dataModel.getElementAt(index);
        if (!elements.contains((ConceptChronicleBI) newElement)) {
            elements.add(index, (ConceptChronicleBI) newElement);
            kb.setConcept((I_GetConceptData) newElement);
        }
        fireTableRowsInserted(index, index);
    }

    @Override
    public void intervalRemoved(ListDataEvent lde) {
        int index = lde.getIndex0();
        int index1 = lde.getIndex1();
        if (index1 == elements.size()) {
            elements.clear();
            fireTableDataChanged();
        } else {
            elements.remove(index);
            fireTableRowsDeleted(index, index);
        }
    }

    @Override
    public void contentsChanged(ListDataEvent lde) {
        List<I_GetConceptData> listElements = list.dataModel.getElements();
        elements.clear();
        for (I_GetConceptData c : listElements) {
            kb.setConcept(c);
            elements.add((ConceptChronicleBI) c);
        }
        fireTableDataChanged();
    }

    private class ConceptCheckChangeListener extends TermChangeListener {
        @Override
        public void changeNotify(long sequence, Set<Integer> originsOfChangedRels, Set<Integer> destinationsOfChangedRels, Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents, Set<Integer> changedComponentAlerts, Set<Integer> changedComponentTemplates, boolean fromClassification) {
            int index = 0;
            if (!changedComponentTemplates.isEmpty()) {
                index = 0;
                for (ConceptChronicleBI c : elements) {
                    if (changedComponentTemplates.contains(c.getConceptNid())) {
                        fireTableRowsUpdated(index, index);
                    }
                    index++;
                }
            }
            index = 0;
            if (!changedComponentAlerts.isEmpty()) {
                for (ConceptChronicleBI c : elements) {
                    if (changedComponentAlerts.contains(c.getConceptNid())) {
                        fireTableRowsUpdated(index, index);
                    }
                    index++;
                }
            }

        }
    }
}
