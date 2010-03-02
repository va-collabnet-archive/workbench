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
package org.dwfa.ace.utypes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.UUID;

public class UniversalAceExtByRefPartTemplateForRel extends UniversalAceExtByRefPart {

    Collection<UUID> valueTypeUid;
    int cardinality;
    Collection<UUID> semanticStatusUid;
    int browseAttributeOrder;
    int browseValueOrder;
    int notesScreenOrder;
    Collection<UUID> attributeDisplayStatusUid;
    Collection<UUID> characteristicStatusUid;

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(valueTypeUid);
        out.writeInt(cardinality);
        out.writeObject(semanticStatusUid);
        out.writeInt(browseAttributeOrder);
        out.writeInt(browseValueOrder);
        out.writeInt(notesScreenOrder);
        out.writeObject(attributeDisplayStatusUid);
        out.writeObject(characteristicStatusUid);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            valueTypeUid = (Collection<UUID>) in.readObject();
            cardinality = in.readInt();
            semanticStatusUid = (Collection<UUID>) in.readObject();
            browseAttributeOrder = in.readInt();
            browseValueOrder = in.readInt();
            notesScreenOrder = in.readInt();
            attributeDisplayStatusUid = (Collection<UUID>) in.readObject();
            characteristicStatusUid = (Collection<UUID>) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public Collection<UUID> getValueTypeUid() {
        return valueTypeUid;
    }

    public void setValueTypeUid(Collection<UUID> valueTypeUid) {
        this.valueTypeUid = valueTypeUid;
    }

    public int getCardinality() {
        return cardinality;
    }

    public void setCardinality(int cardinality) {
        this.cardinality = cardinality;
    }

    public Collection<UUID> getSemanticStatusUid() {
        return semanticStatusUid;
    }

    public void setSemanticStatusUid(Collection<UUID> semanticStatusUid) {
        this.semanticStatusUid = semanticStatusUid;
    }

    public int getBrowseAttributeOrder() {
        return browseAttributeOrder;
    }

    public void setBrowseAttributeOrder(int browseAttributeOrder) {
        this.browseAttributeOrder = browseAttributeOrder;
    }

    public int getBrowseValueOrder() {
        return browseValueOrder;
    }

    public void setBrowseValueOrder(int browseValueOrder) {
        this.browseValueOrder = browseValueOrder;
    }

    public int getNotesScreenOrder() {
        return notesScreenOrder;
    }

    public void setNotesScreenOrder(int notesScreenOrder) {
        this.notesScreenOrder = notesScreenOrder;
    }

    public Collection<UUID> getAttributeDisplayStatusUid() {
        return attributeDisplayStatusUid;
    }

    public void setAttributeDisplayStatusUid(Collection<UUID> attributeDisplayStatusUid) {
        this.attributeDisplayStatusUid = attributeDisplayStatusUid;
    }

    public Collection<UUID> getCharacteristicStatusUid() {
        return characteristicStatusUid;
    }

    public void setCharacteristicStatusUid(Collection<UUID> characteristicStatusUid) {
        this.characteristicStatusUid = characteristicStatusUid;
    }

}
