/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dwfa.ace.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.NidBitSetItrBI;
import org.ihtsdo.tk.api.TermChangeListener;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_nid.RefexNidVersionBI;

/**
 *
 * @author akf
 */
public class PromotionTerminologyTableModel extends AbstractTableModel implements 
        ListDataListener {

    CopyOnWriteArrayList<ConceptChronicleBI> elements = new CopyOnWriteArrayList<>();
    HashMap<Integer, Integer> changedDesc = new HashMap<>();
    HashMap<Integer, Integer> changedStated = new HashMap<>();
    HashMap<Integer, Integer> changedInferred = new HashMap<>();
    ViewCoordinate mergeVc;
    int mergePathNid;
    private ConceptCheckChangeListener tableChangeListener = new ConceptCheckChangeListener();
//    private ConceptCheckChangeListener listViewChangeListener = new ConceptCheckChangeListener();

    public enum MODEL_FIELD {

        CONCEPT("concepts"),
        DESC("changed description"),
        DL_STATED("DL change - stated"),
        DL_INFERRED("DL change - inferred"),
        SELECT("reviewed");
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
            NidBitSetBI changedStatedNids, NidBitSetBI changedInferredNids,
            ViewCoordinate mergeVc, int mergePathNid) throws IOException {
        super();
        this.list = list;
        this.list.getModel().addListDataListener(this);
        Ts.get().addTermChangeListener(tableChangeListener);
        this.mergeVc = mergeVc;
        this.mergePathNid = mergePathNid;
        
        if (changedDescNids != null) {
            NidBitSetItrBI descItr = changedDescNids.iterator();
            while (descItr.next()) {
                int nid = descItr.nid();
                changedDesc.put(Ts.get().getConceptNidForNid(nid), nid);
            }
        }

        if (changedStatedNids != null) {
            NidBitSetItrBI statedItr = changedStatedNids.iterator();
            while (statedItr.next()) {
                int nid = statedItr.nid();
                changedStated.put(Ts.get().getConceptNidForNid(nid), nid);
            }
        }

        if (changedInferredNids != null) {
            NidBitSetItrBI infItr = changedInferredNids.iterator();
            while (infItr.next()) {
                int nid = infItr.nid();
                changedInferred.put(Ts.get().getConceptNidForNid(nid), nid);
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
                if (changedDesc.containsKey(nid)) {
                    return "<html>&bull;";
                }
                return "";
            case DL_STATED:
                nid = elements.get(row).getConceptNid();
                if (changedStated.containsKey(nid)) {
                    return "<html>&bull;";
                }
                return "";
            case DL_INFERRED:
                nid = elements.get(row).getConceptNid();
                if (changedInferred.containsKey(nid)) {
                    return "<html>&bull;";
                }
                return "";
            case SELECT:
                nid = elements.get(row).getConceptNid();
                Collection<? extends RefexVersionBI<?>> refsetMembersActive = null;
                try {
                    refsetMembersActive = Ts.get().getConceptVersion(mergeVc, mergePathNid).getRefsetMembersActive();

                    Integer componentNid = null;
                    if (changedDesc.containsKey(nid)) {
                        componentNid = changedDesc.get(nid);
                    } else if (changedStated.containsKey(nid)) {
                        componentNid = changedStated.get(nid);
                    } else if (changedInferred.containsKey(nid)) {
                        componentNid = changedInferred.get(nid);
                    }
                    if (refsetMembersActive != null && componentNid != null) {
                        for (RefexVersionBI member : refsetMembersActive) {
                            if (member.getReferencedComponentNid() == componentNid) {
                                RefexNidVersionBI rn = (RefexNidVersionBI) member;
                                return Ts.get().getConcept(rn.getNid1()).toUserString();
                            }
                        }
                    }
                } catch (ContradictionException e) {
//TODO: do something  
                } catch (IOException e) {
//TODO: do something                 
                }
                
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
    
    

    private class ConceptCheckChangeListener extends TermChangeListener {
        @Override
        public void changeNotify(long sequence, Set<Integer> originsOfChangedRels, Set<Integer> destinationsOfChangedRels, Set<Integer> referencedComponentsOfChangedRefexs, Set<Integer> changedComponents, Set<Integer> changedComponentAlerts, Set<Integer> changedComponentTemplates, boolean fromClassification) {
            if(changedComponentAlerts.contains(mergePathNid)){
                fireTableDataChanged();
            }
        }
    }

    public void setChangedDesc(NidBitSetBI changedDesc) throws IOException {
        NidBitSetItrBI itr = changedDesc.iterator();
        this.changedDesc.clear();
        while(itr.next()){
            this.changedDesc.put(Ts.get().getConceptNidForNid(itr.nid()), itr.nid());
        }
    }

    public void setChangedStated(NidBitSetBI changedStated) throws IOException {
        NidBitSetItrBI itr = changedStated.iterator();
        this.changedStated.clear();
        while(itr.next()){
            this.changedStated.put(Ts.get().getConceptNidForNid(itr.nid()), itr.nid());
        }
    }

    public void setChangedInferred(NidBitSetBI changedInferred) throws IOException {
        NidBitSetItrBI itr = changedInferred.iterator();
        this.changedInferred.clear();
        while(itr.next()){
            this.changedInferred.put(Ts.get().getConceptNidForNid(itr.nid()), itr.nid());
        }
    }
}
