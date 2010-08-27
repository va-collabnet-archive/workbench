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

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.TerminologyException;

public class LocalFixedDesc implements I_DescribeConceptLocally, Externalizable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static int dataVersion = 3;

    public static int defaultDataVersion = 2;

    private int nid;

    private int statusNid;

    private int conceptNid;

    private boolean initialCapSig;

    private int descTypeNid;

    private String text;

    private String langCode;

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        try {
            int version = defaultDataVersion;
            int versionOrCount = in.readInt();
            int uidCount = -1;
            if (versionOrCount < 0) {
                version = versionOrCount - Integer.MIN_VALUE;
                uidCount = in.readInt();
            } else {
                uidCount = versionOrCount;
            }

            Collection<UUID> duids = new ArrayList<UUID>(uidCount);
            for (int i = 0; i < uidCount; i++) {
                long msb = in.readLong();
                long lsb = in.readLong();
                duids.add(new UUID(msb, lsb));
            }
            nid = LocalFixedTerminology.getStore().getNid(duids);
            if (version >= 2) {
                uidCount = in.readInt();
                Collection<UUID> cuids = new ArrayList<UUID>(uidCount);
                for (int i = 0; i < uidCount; i++) {
                    long msb = in.readLong();
                    long lsb = in.readLong();
                    cuids.add(new UUID(msb, lsb));
                }
                conceptNid = LocalFixedTerminology.getStore().getNid(cuids);
            } else {
                conceptNid = Integer.MIN_VALUE;
            }
        } catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    private Object readResolve() throws ObjectStreamException {
        try {
            return LocalFixedTerminology.getStore().getDescription(nid, conceptNid);
        } catch (Exception e) {
            ObjectStreamException oes = new InvalidObjectException(e.getMessage());
            oes.initCause(e);
            throw oes;
        }
    }

    public static I_DescribeConceptLocally get(UUID descriptionUid, UUID conceptUid,
            I_StoreLocalFixedTerminology sourceServer) throws Exception {
        int dnid = sourceServer.getNid(descriptionUid);
        int cnid = sourceServer.getNid(conceptUid);
        return sourceServer.getDescription(dnid, cnid);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        try {
            out.writeInt(Integer.MIN_VALUE + dataVersion);
            Collection<UUID> uids = LocalFixedTerminology.getStore().getUids(nid);
            out.writeInt(uids.size());
            for (UUID uid : uids) {
                out.writeLong(uid.getMostSignificantBits());
                out.writeLong(uid.getLeastSignificantBits());
            }
            Collection<UUID> concUids = LocalFixedTerminology.getStore().getUids(conceptNid);
            out.writeInt(concUids.size());
            for (UUID uid : concUids) {
                out.writeLong(uid.getMostSignificantBits());
                out.writeLong(uid.getLeastSignificantBits());
            }
        } catch (Exception e) {
            IOException ioe = new IOException();
            ioe.initCause(e);
            throw ioe;
        }
    }

    public LocalFixedDesc() {
        super();
    }

    public LocalFixedDesc(int nid, int statusNid, int conceptNid, boolean initialCapSig, int descTypeNid, String text,
            String lanCode) {
        super();
        this.nid = nid;
        this.statusNid = statusNid;
        this.conceptNid = conceptNid;
        this.initialCapSig = initialCapSig;
        this.descTypeNid = descTypeNid;
        this.text = text;
        this.langCode = lanCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Describe#getConcept()
     */
    public I_ConceptualizeLocally getConcept() {
        return LocalFixedConcept.get(conceptNid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Describe#getDescType()
     */
    public I_ConceptualizeLocally getDescType() {
        return LocalFixedConcept.get(descTypeNid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Describe#isInitialCapSig()
     */
    public boolean isInitialCapSig() {
        return initialCapSig;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Describe#getLangCode()
     */
    public String getLangCode() {
        return langCode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Describe#getStatus()
     */
    public I_ConceptualizeLocally getStatus() {
        return LocalFixedConcept.get(statusNid);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_Describe#getText()
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return nid + ": " + text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (I_DescribeConceptLocally.class.isAssignableFrom(obj.getClass())) {
            I_DescribeConceptLocally another = (I_DescribeConceptLocally) obj;
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

    public I_DescribeConceptUniversally universalize() throws IOException, TerminologyException {
        return new UniversalFixedDescription(LocalFixedTerminology.getStore().getUids(nid),
            LocalFixedTerminology.getStore().getUids(statusNid), LocalFixedTerminology.getStore().getUids(conceptNid),
            initialCapSig, LocalFixedTerminology.getStore().getUids(descTypeNid), text, langCode);
    }

    public I_ManifestLocally getExtension(I_ConceptualizeLocally extensionType) throws IOException,
            TerminologyException {
        return LocalFixedTerminology.getStore().getExtension(this, extensionType);
    }

    public int getNid() {
        return nid;
    }

}
