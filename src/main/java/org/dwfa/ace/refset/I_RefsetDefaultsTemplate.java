package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsTemplate extends I_RefsetDefaultsTemplateForRel {

    public I_GetConceptData getAttribute();

    public void setAttribute(I_GetConceptData attribute);

    public I_IntList getAttributePopupIds();

    public void setAttributePopupIds(I_IntList attributePopupIds);

}