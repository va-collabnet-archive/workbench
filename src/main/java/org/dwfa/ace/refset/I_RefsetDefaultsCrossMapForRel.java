package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.refset.I_RefsetDefaults;

public interface I_RefsetDefaultsCrossMapForRel extends I_RefsetDefaults {

    public I_GetConceptData getRefineFlag();

    public void setRefineFlag(I_GetConceptData refineFlag);

    public I_IntList getRefineFlagPopupIds();

    public void setRefineFlagPopupIds(I_IntList refineFlagPopupIds);

    public I_GetConceptData getAdditionalCode();

    public void setAdditionalCode(I_GetConceptData additionalCode);

    public I_IntList getAdditionalCodePopupIds();

    public void setAdditionalCodePopupIds(I_IntList additionalCodePopupIds);

    public int getDefaultElementNo();

    public void setDefaultElementNo(int defaultElementNo);

    public Integer[] getElementNoPopupItems();

    public void setElementNoPopupItems(Integer[] elementNoPopupItems);

    public int getDefaultBlockNo();

    public void setDefaultBlockNo(int defaultBlockNo);

    public Integer[] getBlockNoPopupItems();

    public void setBlockNoPopupItems(Integer[] blockNoPopupItems);

}