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
package org.dwfa.ace.api.ebr;


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
