package org.dwfa.ace.api.ebr;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;

public interface I_ThinExtByRefPartCrossmapForRel extends I_ThinExtByRefPart {

    public int getRefineFlagId();

    public void setRefineFlagId(int refineFlagId);

    public int getAdditionalCodeId();

    public void setAdditionalCodeId(int additionalCodeId);

    public int getElementNo();

    public void setElementNo(int elementNo);

    public int getBlockNo();

    public void setBlockNo(int blockNo);

}