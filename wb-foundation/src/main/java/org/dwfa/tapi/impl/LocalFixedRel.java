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
package org.dwfa.tapi.impl;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class LocalFixedRel implements I_RelateConceptsLocally {

    private int nid, c1nid, relTypeNid, c2nid, characteristicNid, refinabilityNid, relGrp;

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            int uidCount = in.readInt();
            Collection<UUID> uids = new ArrayList<UUID>(uidCount);
            for (int i = 0; i < uidCount; i++) {
                long msb = in.readLong();
                long lsb = in.readLong();
                uids.add(new UUID(msb, lsb));
            }
            nid = LocalFixedTerminology.getStore().getNid(uids);
        } catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    private Object readResolve() throws ObjectStreamException {
        try {
            return LocalFixedTerminology.getStore().getRel(nid);
        } catch (Exception e) {
            ObjectStreamException oes = new InvalidObjectException(e.getMessage());
            oes.initCause(e);
            throw oes;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            Collection<UUID> uids = LocalFixedTerminology.getStore().getUids(nid);
            out.writeInt(uids.size());
            for (UUID uid : uids) {
                out.writeLong(uid.getMostSignificantBits());
                out.writeLong(uid.getLeastSignificantBits());
            }
        } catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    public LocalFixedRel() {
        super();
    }

    public LocalFixedRel(int nid, int c1nid, int relTypeNid, int c2nid, int characteristicNid, int refinabilityNid,
            int relGrp) {
        super();
        this.nid = nid;
        this.c1nid = c1nid;
        this.relTypeNid = relTypeNid;
        this.c2nid = c2nid;
        this.characteristicNid = characteristicNid;
        this.refinabilityNid = refinabilityNid;
        this.relGrp = relGrp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Relate#getC1()
     */
    public LocalFixedConcept getC1() {
        return LocalFixedConcept.get(c1nid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Relate#getC2()
     */
    public LocalFixedConcept getC2() {
        return LocalFixedConcept.get(c2nid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Relate#getCharacteristic()
     */
    public I_ConceptualizeLocally getCharacteristic() {
        return LocalFixedConcept.get(characteristicNid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Relate#getRefinability()
     */
    public I_ConceptualizeLocally getRefinability() {
        return LocalFixedConcept.get(refinabilityNid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Relate#getRelGrp()
     */
    public int getRelGrp() {
        return relGrp;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Relate#getRelType()
     */
    public I_ConceptualizeLocally getRelType() {
        return LocalFixedConcept.get(relTypeNid);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (I_RelateConceptsLocally.class.isAssignableFrom(obj.getClass())) {
            I_RelateConceptsLocally another = (I_RelateConceptsLocally) obj;
            return nid == another.getNid();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return nid;
    }

    public Collection<UUID> getUids() throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(nid);
    }

    public boolean isUniversal() {
        return false;
    }

    public I_RelateConceptsUniversally universalize() throws IOException, TerminologyException {
        I_StoreLocalFixedTerminology sourceServer = LocalFixedTerminology.getStore();
        return new UniversalFixedRel(sourceServer.getUids(nid), sourceServer.getUids(c1nid),
            sourceServer.getUids(relTypeNid), sourceServer.getUids(c2nid), sourceServer.getUids(characteristicNid),
            sourceServer.getUids(refinabilityNid), relGrp);
    }

    public I_ManifestLocally getExtension(I_ConceptualizeLocally extensionType) throws IOException,
            TerminologyException {
        return LocalFixedTerminology.getStore().getExtension(this, extensionType);
    }

    public int getNid() {
        return nid;
    }

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(this.getClass().getSimpleName());
        buff.append(": ");
        buff.append(nid);
        try {
            buff.append(" ");
            buff.append(this.getC1().toString());
            buff.append(" ");
            buff.append(this.getRelType().toString());
            buff.append(" ");
            buff.append(this.getC2().toString());
        } catch (Exception e1) {
            e1.printStackTrace();
            return this.getClass().getSimpleName() + "nid: " + nid + " c1nid: " + c1nid + " relTypeNid: " + relTypeNid
                + " c2nid: " + c2nid + " characteristicNid: " + characteristicNid + " refinabilityNid: "
                + refinabilityNid + " relGrp: " + relGrp;
        }
        return buff.toString();
    }

}
