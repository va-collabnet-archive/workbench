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
package org.dwfa.ace.refset;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;

public interface I_RefsetDefaultsTemplateForRel extends I_RefsetDefaults {

    public I_GetConceptData getValueType();

    public void setValueType(I_GetConceptData valueType);

    public I_IntList getValueTypePopupIds();

    public void setValueTypePopupIds(I_IntList valueTypePopupIds);

    public int getCardinality();

    public void setCardinality(int cardinality);

    public Integer[] getCardinalityPopupItems();

    public void setCardinalityPopupItems(Integer[] cardinalityPopupItems);

    public I_GetConceptData getSemanticStatus();

    public void setSemanticStatus(I_GetConceptData semanticStatus);

    public I_IntList getSemanticStatusPopupIds();

    public void setSemanticStatusPopupIds(I_IntList semanticStatusPopupIds);

    public int getBrowseAttributeOrder();

    public void setBrowseAttributeOrder(int browseAttributeOrder);

    public Integer[] getBrowseAttributeOrderPopupItems();

    public void setBrowseAttributeOrderPopupItems(Integer[] browseAttributeOrderPopupItems);

    public int getBrowseValueOrder();

    public void setBrowseValueOrder(int browseValueOrder);

    public Integer[] getBrowseValueOrderPopupItems();

    public void setBrowseValueOrderPopupItems(Integer[] browseValueOrderPopupItems);

    public int getNotesScreenOrder();

    public void setNotesScreenOrder(int notesScreenOrder);

    public Integer[] getNotesScreenOrderPopupItems();

    public void setNotesScreenOrderPopupItems(Integer[] notesScreenOrderPopupItems);

    public I_GetConceptData getAttributeDisplayStatus();

    public void setAttributeDisplayStatus(I_GetConceptData attributeDisplayStatus);

    public I_IntList getAttributeDisplayStatusPopupIds();

    public void setAttributeDisplayStatusPopupIds(I_IntList attributeDisplayStatusPopupIds);

    public I_GetConceptData getCharacteristicStatus();

    public void setCharacteristicStatus(I_GetConceptData characteristicStatus);

    public I_IntList getCharacteristicStatusPopupIds();

    public void setCharacteristicStatusPopupIds(I_IntList characteristicStatusPopupIds);

}
