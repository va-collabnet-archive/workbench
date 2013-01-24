/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.helper.owl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import org.ihtsdo.tk.Ts;

/**
 * Represents a concept imported from an OWL file.
 */
public class OwlConcept {
    private String id;
    private String label;
    private String fsn;
    private Integer sctId;
    private Integer fmaid;
    private UUID conceptUuid;
    private ArrayList<String> parents = new ArrayList<>();
    
    public OwlConcept(String id) {
        this.id = id;
        this.label = null;
        this.fsn = null;
        this.sctId = null;
        this.fmaid = null;
        this.parents = new ArrayList<>();
    }
    
    public OwlConcept(String id, String label, String fsn, int sctId, int fmaid) {
        this.id = id;
        this.label = label;
        this.fsn = fsn;
        this.sctId = sctId;
        this.fmaid = fmaid;
        this.parents = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getFsn() {
        return fsn;
    }

    public Integer getSctId() {
        return sctId;
    }

    public int getFmaid() {
        return fmaid;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setFsn(String fsn) {
        this.fsn = fsn;
    }

    public void setSctId(int sctId) {
        this.sctId = sctId;
    }

    public void setFmaid(int fmaid) {
        this.fmaid = fmaid;
    }

    public void setConceptUuid(UUID conceptUuid) {
        this.conceptUuid = conceptUuid;
    }
    
    public void addParent(String parent){
        parents.add(parent);
    }
    
    public ArrayList<String> getParents(){
        return parents;
    }

    public UUID getConceptUuid() {
        return conceptUuid;
    }
    
    @Override
    public String toString(){
        String owlString = "ID: " + id + " LABEL: " + label + " FSN: " + fsn + " FMAID: " + fmaid + " SCTID: " + sctId + " PARENTS: " + parents;
        return owlString;
    }
}
