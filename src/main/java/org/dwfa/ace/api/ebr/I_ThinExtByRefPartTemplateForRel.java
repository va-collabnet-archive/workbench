package org.dwfa.ace.api.ebr;

import org.dwfa.ace.api.ebr.I_ThinExtByRefPart;

public interface I_ThinExtByRefPartTemplateForRel extends I_ThinExtByRefPart {

    public int getValueTypeId();

    public void setValueTypeId(int valueTypeId);

    public int getCardinality();

    public void setCardinality(int cardinality);

    public int getSemanticStatusId();

    public void setSemanticStatusId(int semanticStatusId);

    public int getBrowseAttributeOrder();

    public void setBrowseAttributeOrder(int browseAttributeOrder);

    public int getBrowseValueOrder();

    public void setBrowseValueOrder(int browseValueOrder);

    public int getNotesScreenOrder();

    public void setNotesScreenOrder(int notesScreenOrder);

    public int getAttributeDisplayStatusId();

    public void setAttributeDisplayStatusId(int attributeDisplayStatusId);

    public int getCharacteristicStatusId();

    public void setCharacteristicStatusId(int characteristicStatusId);

}