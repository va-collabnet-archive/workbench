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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_RelateConceptsLocally;
import org.dwfa.tapi.NoMappingException;
import org.dwfa.tapi.TerminologyException;

public class LocalFixedConcept implements I_ConceptualizeLocally, Externalizable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private int nid;

    private transient Boolean primitive;

    private transient Collection<I_DescribeConceptLocally> descriptions;

    private transient Collection<I_RelateConceptsLocally> sourceRels;

    private transient Collection<I_RelateConceptsLocally> destRels;

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
        WeakReference<LocalFixedConcept> ref = cbeans.get(this);
        if (ref != null) {
            return ref.get();
        }
        cbeans.put(this, new WeakReference<LocalFixedConcept>(this));
        return this;
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

    public LocalFixedConcept() {
        super();
    }

    private LocalFixedConcept(int nid, Boolean primitive) {
        super();
        this.nid = nid;
        this.primitive = primitive;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (I_ConceptualizeLocally.class.isAssignableFrom(obj.getClass())) {
            I_ConceptualizeLocally another = (I_ConceptualizeLocally) obj;
            return nid == another.getNid();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return nid;
    }

    public static LocalFixedConcept get(int conceptNid) {
        return get(conceptNid, null);
    }

    private static WeakHashMap<LocalFixedConcept, WeakReference<LocalFixedConcept>> cbeans = new WeakHashMap<LocalFixedConcept, WeakReference<LocalFixedConcept>>();

    public static void purge() {
    	cbeans.clear();
    }
    
    public static LocalFixedConcept get(int conceptNid, Boolean primitive) {
        LocalFixedConcept cb = new LocalFixedConcept(conceptNid, primitive);
        WeakReference<LocalFixedConcept> ref = cbeans.get(cb);
        if (ref != null) {
            cb = ref.get();
        } else {
            cbeans.put(cb, new WeakReference<LocalFixedConcept>(cb));
        }
        if (primitive != null) {
            cb.primitive = primitive;
        }
        return cb;
    }

    public static Collection<I_ConceptualizeLocally> getConceptsForDescriptions(
            Collection<I_DescribeConceptLocally> descriptions) throws Exception {
        Set<I_ConceptualizeLocally> concepts = new HashSet<I_ConceptualizeLocally>();
        for (I_DescribeConceptLocally d : descriptions) {
            concepts.add(d.getConcept());
        }
        return concepts;
    }

    public static LocalFixedConcept get(UUID conceptUid) throws Exception {
        int nid = LocalFixedTerminology.getStore().getNid(conceptUid);
        return LocalFixedConcept.get(nid);
    }

    public static LocalFixedConcept get(Collection<UUID> uids) throws NoMappingException {
        if (LocalFixedTerminology.getStore() == null) {
            throw new NoMappingException("LocalFixedTerminology has not been initialized.");
        }
        for (UUID id : uids) {
            int nid;
            try {
                nid = LocalFixedTerminology.getStore().getNid(id);
                return LocalFixedConcept.get(nid);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new NoMappingException("Can't find: " + uids);
    }

    public static LocalFixedConcept get(Collection<UUID> uids, Boolean primitive) throws TerminologyException {
        LocalFixedConcept concept = get(uids);
        if (concept.primitive == null) {
            concept.primitive = primitive;
        } else {
            if (concept.primitive.equals(primitive) == false) {
                throw new TerminologyException("Primitive states do not agree for: " + uids + " provided: " + primitive
                    + " retrieved: " + concept.primitive);
            }
        }
        return concept;
    }

    public String toString() {
        StringBuffer buff = new StringBuffer();
        try {
            List<I_ConceptualizeLocally> prefList = ArchitectonicAuxiliary.getLocalToStringDescPrefList(LocalFixedTerminology.getStore());
            I_DescribeConceptLocally desc = getDescription(prefList);
            buff.append(" ");
            if (desc != null) {
                buff.append(desc.getText());
            } else {
                throw new Exception("desc is null");
            }
        } catch (Exception e1) {
            try {
                System.err.println("desc is null in LocalFixedConcept.toString(). " + getDescriptions());
                Collection<I_DescribeConceptLocally> desc = getDescriptions();
                for (I_DescribeConceptLocally d : desc) {
                    buff.append("\n     desc: ");
                    buff.append(d.toString());
                }
                buff.append("\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        buff.append(" ");
        buff.append(this.getClass().getSimpleName());
        buff.append(": ");
        buff.append(nid);
        return buff.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getDescriptions()
     */
    public Collection<I_DescribeConceptLocally> getDescriptions() throws IOException, TerminologyException {
        if (descriptions == null) {
            descriptions = LocalFixedTerminology.getStore().getDescriptionsForConcept(this);
        }
        return descriptions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getSourceRels()
     */
    public Collection<I_RelateConceptsLocally> getSourceRels() throws IOException, TerminologyException {
        if (sourceRels == null) {
            sourceRels = LocalFixedTerminology.getStore().getSourceRels(this);
        }
        return sourceRels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getDestRels()
     */
    public Collection<I_RelateConceptsLocally> getDestRels() throws IOException, TerminologyException {
        if (destRels == null) {
            destRels = LocalFixedTerminology.getStore().getDestRels(this);
        }
        return destRels;
    }

    public Collection<I_RelateConceptsLocally> getDestRels(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException {
        Collection<I_RelateConceptsLocally> destRelsOfType = new ArrayList<I_RelateConceptsLocally>();
        for (I_RelateConceptsLocally rel : getDestRels()) {
            if (types.contains(rel.getRelType())) {
                destRelsOfType.add(rel);
            }
        }
        return destRelsOfType;
    }

    public Collection<I_RelateConceptsLocally> getSourceRels(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException {
        Collection<I_RelateConceptsLocally> srcRelsOfType = new ArrayList<I_RelateConceptsLocally>();
        for (I_RelateConceptsLocally rel : getSourceRels()) {
            if (types.contains(rel.getRelType())) {
                srcRelsOfType.add(rel);
            }
        }
        return srcRelsOfType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getDestRelConcepts()
     */
    public Collection<I_ConceptualizeLocally> getDestRelConcepts() throws IOException, TerminologyException {
        Collection<I_ConceptualizeLocally> results = new ArrayList<I_ConceptualizeLocally>();
        for (I_RelateConceptsLocally r : getSourceRels()) {
            results.add(r.getC2());
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getDestRelConcepts(java.util.List)
     */
    public List<I_ConceptualizeLocally> getDestRelConcepts(Collection<I_ConceptualizeLocally> types)
            throws IOException, TerminologyException {
        List<I_ConceptualizeLocally> results = new ArrayList<I_ConceptualizeLocally>();
        for (I_RelateConceptsLocally r : getSourceRels()) {
            if (types.contains(r.getRelType())) {
                results.add(r.getC2());
            }
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getSrcRelConcepts()
     */
    public List<I_ConceptualizeLocally> getSrcRelConcepts() throws IOException, TerminologyException {
        List<I_ConceptualizeLocally> results = new ArrayList<I_ConceptualizeLocally>();
        for (I_RelateConceptsLocally r : getDestRels()) {
            results.add(r.getC1());
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#getSrcRelConcepts(java.util.List)
     */
    public List<I_ConceptualizeLocally> getSrcRelConcepts(Collection<I_ConceptualizeLocally> types) throws IOException,
            TerminologyException {
        List<I_ConceptualizeLocally> results = new ArrayList<I_ConceptualizeLocally>();
        for (I_RelateConceptsLocally r : getDestRels()) {
            if (types.contains(r.getRelType())) {
                results.add(r.getC1());
            }
        }
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.dwfa.tapi.I_AmConcept#isPrimitive()
     */
    public boolean isPrimitive() throws IOException, TerminologyException {
        if (primitive == null) {
            I_ConceptualizeLocally serverConcept = LocalFixedTerminology.getStore().getConcept(nid);
            primitive = serverConcept.isPrimitive();
        }
        return primitive;
    }

    public Collection<UUID> getUids() throws IOException, TerminologyException {
        return LocalFixedTerminology.getStore().getUids(nid);
    }

    public boolean isUniversal() {
        return false;
    }

    public I_ConceptualizeUniversally universalize() throws IOException, TerminologyException {
        return UniversalFixedConcept.get(LocalFixedTerminology.getStore().getUids(nid));
    }

    public I_ManifestLocally getExtension(I_ConceptualizeLocally extensionType) throws IOException,
            TerminologyException {
        return LocalFixedTerminology.getStore().getExtension(this, extensionType);
    }

    public int getNid() {
        return nid;
    }

    public I_DescribeConceptLocally getDescription(List<I_ConceptualizeLocally> typePriorityList) throws IOException,
            TerminologyException {
        for (I_ConceptualizeLocally descType : typePriorityList) {
            for (I_DescribeConceptLocally desc : getDescriptions()) {
                if (desc.getDescType().equals(descType)) {
                    return desc;
                }
            }
        }
        return null;
    }

}
