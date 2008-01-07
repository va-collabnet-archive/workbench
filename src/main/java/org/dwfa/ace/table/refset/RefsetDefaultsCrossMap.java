package org.dwfa.ace.table.refset;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaultsCrossMap;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.ConceptBean;
import org.dwfa.vodb.types.IntList;

public class RefsetDefaultsCrossMap extends RefsetDefaultsCrossMapForRel implements I_RefsetDefaultsCrossMap {


    public RefsetDefaultsCrossMap() throws TerminologyException, IOException {
        super();
        mapStatus = ConceptBean.get(RefsetAuxiliary.Concept.UNMAPPABLE_MAP_STATUS.getUids());
        mapStatusPopupIds.add(mapStatus.getConceptId());
        mapStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.EXACT_MAP_STATUS.getUids()).getConceptId());
        mapStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.GENERAL_MAP_STATUS.getUids()).getConceptId());
        mapStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.DEFAULT_MAP_STATUS.getUids()).getConceptId());
        mapStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.REQUIRES_CHECKING_MAP_STATUS.getUids()).getConceptId());
        mapStatusPopupIds.add(ConceptBean.get(RefsetAuxiliary.Concept.ALTERNATIVE_MAP_STATUS.getUids()).getConceptId());
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;
    
    private I_GetConceptData mapStatus;
    private I_IntList mapStatusPopupIds = new IntList();


    private void writeObject(ObjectOutputStream out) throws IOException {
       out.writeInt(dataVersion);
       out.writeObject(mapStatus.getUids());
       IntList.writeIntList(out, mapStatusPopupIds);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
       int objDataVersion = in.readInt();
       if (objDataVersion == dataVersion) {
           mapStatus = readConcept(in);
           mapStatusPopupIds = IntList.readIntListIgnoreMapErrors(in);
       } else {
          throw new IOException("Can't handle dataversion: " + objDataVersion);
       }
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMap#getMapStatus()
     */
    public I_GetConceptData getMapStatus() {
        return mapStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMap#setMapStatus(org.dwfa.ace.api.I_GetConceptData)
     */
    public void setMapStatus(I_GetConceptData mapStatus) {
        this.mapStatus = mapStatus;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMap#getMapStatusPopupIds()
     */
    public I_IntList getMapStatusPopupIds() {
        return mapStatusPopupIds;
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.table.refset.I_RefsetDefaultsCrossMap#setMapStatusPopupIds(org.dwfa.ace.api.I_IntList)
     */
    public void setMapStatusPopupIds(I_IntList mapStatusPopupIds) {
        this.mapStatusPopupIds = mapStatusPopupIds;
    }

}
