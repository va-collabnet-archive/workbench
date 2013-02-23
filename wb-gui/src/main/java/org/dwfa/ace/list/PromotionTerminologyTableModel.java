/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dwfa.ace.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

/**
 *
 * @author akf
 */
public class PromotionTerminologyTableModel extends AbstractTableModel implements 
        ListDataListener {

    CopyOnWriteArrayList<ConceptChronicleBI> elements = new CopyOnWriteArrayList<>();
    HashSet<Integer> changedDesc = new HashSet<>();
    HashSet<Integer> changedStated = new HashSet<>();
    HashSet<Integer> changedInferred = new HashSet<>();
//    private ConceptCheckChangeListener listViewChangeListener = new ConceptCheckChangeListener();

    public enum MODEL_FIELD {

        CONCEPT("concepts"),
        DESC("changed description"),
        DL_STATED("DL change - stated"),
        DL_INFERRED("DL change - inferred"),
        SELECT("include");
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

    public PromotionTerminologyTableModel() {
        super();
    }

    public PromotionTerminologyTableModel(TerminologyList list, NidBitSetBI changedDescNids,
            NidBitSetBI changedStatedNids, NidBitSetBI changedInferredNids) throws IOException {
        super();
        this.list = list;
        this.list.getModel().addListDataListener(this);
        
        if (changedDescNids != null) {
            NidBitSetItrBI descItr = changedDescNids.iterator();
            while (descItr.next()) {
                int nid = descItr.nid();
                changedDesc.add(Ts.get().getConceptNidForNid(nid));
            }
        }

        if (changedStatedNids != null) {
            NidBitSetItrBI statedItr = changedStatedNids.iterator();
            while (statedItr.next()) {
                int nid = statedItr.nid();
                changedStated.add(Ts.get().getConceptNidForNid(nid));
            }
        }

        if (changedInferredNids != null) {
            NidBitSetItrBI infItr = changedInferredNids.iterator();
            while (infItr.next()) {
                int nid = infItr.nid();
                changedInferred.add(Ts.get().getConceptNidForNid(nid));
            }
        }
        
        kb = ConceptTemplates.getKb();
//        Ts.get().addTermChangeListener(listViewChangeListener);
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
            case DESC:
                int nid = elements.get(row).getConceptNid();
                if (changedDesc.contains(nid)) {
                    return "<html>&bull;";
                }
                return "";
            case DL_STATED:
                nid = elements.get(row).getConceptNid();
                if (changedStated.contains(nid)) {
                    return "<html>&bull;";
                }
                return "";
            case DL_INFERRED:
                nid = elements.get(row).getConceptNid();
                if (changedInferred.contains(nid)) {
                    return "<html>&bull;";
                }
                return "";
            case SELECT:
                //return a check box to select to include in promotion
                return"";
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
    
    

//    private class ConceptCheckChangeListener extends TermChangeListener {
//        @Override
//        public void changeNotify(long sequence, Set<Integer> originsOfChangedRels, Set<Integer> destinationsOfChangedRels, Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents, Set<Integer> changedComponentAlerts, Set<Integer> changedComponentTemplates, boolean fromClassification) {
//            int index = 0;
//            if (!changedComponentTemplates.isEmpty()) {
//                index = 0;
//                for (ConceptChronicleBI c : elements) {
//                    if (changedComponentTemplates.contains(c.getConceptNid())) {
//                        fireTableRowsUpdated(index, index);
//                    }
//                    index++;
//                }
//            }
//            index = 0;
//            if (!changedComponentAlerts.isEmpty()) {
//                for (ConceptChronicleBI c : elements) {
//                    if (changedComponentAlerts.contains(c.getConceptNid())) {
//                        fireTableRowsUpdated(index, index);
//                    }
//                    index++;
//                }
//            }
//
//        }
//    }

    public void setChangedDesc(NidBitSetBI changedDesc) throws IOException {
        NidBitSetItrBI itr = changedDesc.iterator();
        this.changedDesc.clear();
        while(itr.next()){
            this.changedDesc.add(Ts.get().getConceptNidForNid(itr.nid()));
        }
    }

    public void setChangedStated(NidBitSetBI changedStated) throws IOException {
        NidBitSetItrBI itr = changedStated.iterator();
        this.changedStated.clear();
        while(itr.next()){
            this.changedStated.add(Ts.get().getConceptNidForNid(itr.nid()));
        }
    }

    public void setChangedInferred(NidBitSetBI changedInferred) throws IOException {
        NidBitSetItrBI itr = changedInferred.iterator();
        this.changedInferred.clear();
        while(itr.next()){
            this.changedInferred.add(Ts.get().getConceptNidForNid(itr.nid()));
        }
    }
}
