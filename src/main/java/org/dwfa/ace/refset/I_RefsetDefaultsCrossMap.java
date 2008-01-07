package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsCrossMap extends I_RefsetDefaultsCrossMapForRel {

    public I_GetConceptData getMapStatus();

    public void setMapStatus(I_GetConceptData mapStatus);

    public I_IntList getMapStatusPopupIds();

    public void setMapStatusPopupIds(I_IntList mapStatusPopupIds);

}