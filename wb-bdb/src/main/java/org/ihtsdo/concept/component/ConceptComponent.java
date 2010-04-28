package org.ihtsdo.concept.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.concept.component.identifier.IdentifierVersionLong;
import org.ihtsdo.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.concept.component.identifier.IdentifierVersionUuid;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.etypes.EComponent;
import org.ihtsdo.etypes.EIdentifier;
import org.ihtsdo.etypes.EIdentifierLong;
import org.ihtsdo.etypes.EIdentifierString;
import org.ihtsdo.etypes.EIdentifierUuid;
import org.ihtsdo.time.TimeUtil;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public abstract class ConceptComponent<R extends Revision<R, C>, C extends ConceptComponent<R, C>> implements
        I_AmTermComponent, I_AmPart, I_AmTuple, I_Identify, I_IdPart, I_IdVersion, I_HandleFutureStatusAtPositionSetup {

    public static void addNidToBuffer(StringBuffer buf, int nidToConvert) {
        try {
            if (nidToConvert != 0 && Terms.get().hasConcept(nidToConvert)) {
                buf.append("\"");
                buf.append(Terms.get().getConcept(nidToConvert).getInitialText());
                buf.append("\" [");
                buf.append(nidToConvert);
                buf.append("]");
            } else {
                buf.append(nidToConvert);
            }
        } catch (IOException e) {
            buf.append(e.getLocalizedMessage());
        } catch (TerminologyException e) {
            buf.append(e.getLocalizedMessage());
        }
    }

    private static List<UUID> getUuids(int conceptNid) throws IOException {
        return Bdb.getConceptDb().getUuidsForConcept(conceptNid);
    }

    protected class EditableVersionList<V extends Version> extends ArrayList<V> {

        /**
		 * 
		 */
        private static final long serialVersionUID = 1L;

        public EditableVersionList(Collection<V> c) {
            super(c);
        }

        @Override
        public void add(int index, V element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(V e) {
            if (Revision.class.isAssignableFrom(e.getClass())) {
                if (revisions == null) {
                    throw new RuntimeException(
                        "Use makeAnalog to generate revisions. They will be automatically added.");
                }
                if (revisions.contains(e)) {
                    return false;
                }
                throw new RuntimeException("Use makeAnalog to generate revisions. They will be automatically added.");
            }
            if (e.index == -1) {
                return false;
            }
            if (revisions == null) {
                throw new RuntimeException("Use makeAnalog to generate revisions. They will be automatically added.");
            }
            if (e.index < revisions.size()) {
                return false;
            }
            throw new RuntimeException("Use makeAnalog to generate revisions. They will be automatically added.");
        }

        @Override
        public boolean addAll(Collection<? extends V> c) {
            boolean addedAnything = false;
            for (V v : c) {
                if (add(v)) {
                    addedAnything = true;
                }
            }
            return addedAnything;
        }

        @Override
        public boolean addAll(int index, Collection<? extends V> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            // TODO Auto-generated method stub
            return super.remove(o);
        }

        @Override
        public V set(int index, V element) {
            throw new UnsupportedOperationException();
        }

    }

    public enum IDENTIFIER_PART_TYPES {
        LONG(1),
        STRING(2),
        UUID(3);

        private int partTypeId;

        IDENTIFIER_PART_TYPES(int partTypeId) {
            this.partTypeId = partTypeId;
        }

        public static IDENTIFIER_PART_TYPES getType(Class<?> denotationClass) {
            if (UUID.class.isAssignableFrom(denotationClass)) {
                return UUID;
            } else if (Long.class.isAssignableFrom(denotationClass)) {
                return LONG;
            } else if (String.class.isAssignableFrom(denotationClass)) {
                return STRING;
            }
            throw new UnsupportedOperationException(denotationClass.toString());
        }

        public void writeType(TupleOutput output) {
            output.writeByte(partTypeId);
        }

        public static IDENTIFIER_PART_TYPES readType(TupleInput input) {
            int partTypeId = input.readByte();
            switch (partTypeId) {
            case 1:
                return LONG;
            case 2:
                return STRING;
            case 3:
                return UUID;
            }
            throw new UnsupportedOperationException("partTypeId: " + partTypeId);
        }
    };

    public abstract class Version implements I_AmTuple {
        protected int index = -1;
        private boolean dup = false;

        @Override
        public boolean equals(Object obj) {
            if (Version.class.isAssignableFrom(obj.getClass())) {
                Version another = (Version) obj;
                if (this.getNid() == another.getNid() && this.index == another.index) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return index;
        }

        public Version() {
            super();
        }

        public Version(int index) {
            super();
            this.index = index;
        }

        public R getRevision() {
            if (index >= 0) {
                return revisions.get(index);
            }
            return null;
        }

        @Override
        public I_AmPart getMutablePart() {
            if (index >= 0) {
                return revisions.get(index);
            }
            return this;
        }

        public int getSapNid() {
            if (index >= 0) {
                return revisions.get(index).sapNid;
            }
            return primordialSapNid;
        }

        public String toString() {
            if (index >= 0) {
                return "Version: " + revisions.get(index).toString();
            }
            return "Version: " + ConceptComponent.this.toString();
        }

        public ArrayList<IdentifierVersion> getAdditionalIdentifierParts() {
            return additionalIdentifierParts;
        }

        @Override
        public int getPathId() {
            if (index >= 0) {
                return getMutablePart().getPathId();
            }
            return Bdb.getSapDb().getPathId(primordialSapNid);
        }

        @Override
        public int getStatusId() {
            if (index >= 0) {
                return getMutablePart().getStatusId();
            }
            return Bdb.getSapDb().getStatusId(primordialSapNid);
        }

        @Override
        public long getTime() {
            if (index >= 0) {
                return getMutablePart().getTime();
            }
            return Bdb.getSapDb().getTime(primordialSapNid);
        }

        @Override
        public void setTime(long time) {
            if (index >= 0) {
                getMutablePart().setTime(time);
            }
            ConceptComponent.this.setTime(time);
        }

        @Override
        public int getVersion() {
            if (index >= 0) {
                return getMutablePart().getVersion();
            }
            return Bdb.getSapDb().getVersion(primordialSapNid);
        }

        @Override
        public I_AmTermComponent getFixedPart() {
            return ConceptComponent.this;
        }

        @Override
        public int getNid() {
            return nid;
        }

        @Override
        public final ArrayIntList getPartComponentNids() {
            ArrayIntList resultList = getVariableVersionNids();
            resultList.add(getPathId());
            resultList.add(getStatusId());
            return resultList;
        }

        public abstract ArrayIntList getVariableVersionNids();

        @Override
        public void setPathId(int pathId) {
            if (index >= 0) {
                revisions.get(index).setPathId(pathId);
            } else {
                ConceptComponent.this.setPathId(pathId);
            }
        }

        @Override
        public void setStatusId(int statusId) {
            if (index >= 0) {
                revisions.get(index).setStatusId(statusId);
            } else {
                ConceptComponent.this.setStatusId(statusId);
            }
        }

        public UUID getPrimUuid() {
            return Bdb.getUuidDb().getUuid(primordialUNid);
        }

        public Version removeDuplicates(Version dup1, Version dup2) {
            return ConceptComponent.this.removeDuplicates(dup1, dup2);
        }
    }

    public class IdVersion implements I_IdVersion, I_IdPart {
        protected int index = -1;

        public IdVersion() {
            super();
        }

        public IdVersion(int index) {
            super();
            this.index = index;
        }

        public int getSapNid() {
            if (index >= 0) {
                return additionalIdentifierParts.get(index).getSapNid();
            }
            return primordialSapNid;
        }

        @Override
        public int getPathId() {
            if (index >= 0) {
                return getMutableIdPart().getPathId();
            }
            return Bdb.getSapDb().getPathId(primordialSapNid);
        }

        @Override
        public int getStatusId() {
            if (index >= 0) {
                return getMutableIdPart().getStatusId();
            }
            return Bdb.getSapDb().getStatusId(primordialSapNid);
        }

        @Override
        public long getTime() {
            if (index >= 0) {
                return getMutableIdPart().getTime();
            }
            return Bdb.getSapDb().getTime(primordialSapNid);
        }

        @Override
        public int getVersion() {
            if (index >= 0) {
                return getMutableIdPart().getVersion();
            }
            return Bdb.getSapDb().getVersion(primordialSapNid);
        }

        @Override
        public I_IdPart duplicateIdPart() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        @Override
        public int getAuthorityNid() {
            if (index >= 0) {
                return getMutableIdPart().getAuthorityNid();
            }
            return ConceptComponent.this.getAuthorityNid();
        }

        @Override
        public Object getDenotation() {
            if (index >= 0) {
                return getMutableIdPart().getDenotation();
            }
            return ConceptComponent.this.getDenotation();
        }

        @Override
        public I_Identify getFixedIdPart() {
            return ConceptComponent.this;
        }

        @Override
        public I_Identify getIdentifier() {
            return ConceptComponent.this;
        }

        @Override
        public I_IdPart getMutableIdPart() {
            if (index >= 0) {
                return additionalIdentifierParts.get(index);
            }
            return this;
        }

        @Override
        @Deprecated
        public Set<TimePathId> getTimePathSet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<UUID> getUUIDs() {
            return ConceptComponent.this.getUUIDs();
        }

        @Override
        public I_IdPart makeIdAnalog(int statusNid, int pathNid, long time) {

            // if (index >= 0) {
            // return additionalIdentifierParts.get(index).makeIdAnalog(statusNid, pathNid, time);
            // }
            // return new IdVersion(IdVersion.this, statusNid, pathNid, time, IdVersion.this);

            return ConceptComponent.this.makeIdAnalog(statusNid, pathNid, time);
        }

        @Override
        public void setAuthorityNid(int sourceNid) {
            if (index >= 0) {
                getMutableIdPart().setAuthorityNid(sourceNid);
            }
            // ConceptComponent.this.setAuthorityNid(sourceNid);
        }

        @Override
        public void setDenotation(Object sourceId) {
            if (index >= 0) {
                getMutableIdPart().setDenotation(sourceId);
            }
            // ConceptComponent.this.setDenotation(sourceId);
        }

        @Override
        public int getNid() {
            return nid;
        }

        @Override
        public final ArrayIntList getPartComponentNids() {
            ArrayIntList resultList = getVariableVersionNids();
            resultList.add(getPathId());
            resultList.add(getStatusId());
            return resultList;
        }

        @Override
        @Deprecated
        public void setPathId(int pathId) {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        @Override
        @Deprecated
        public void setStatusId(int statusId) {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        @Override
        @Deprecated
        public void setVersion(int version) {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }
    }

    public int nid;

    public int enclosingConceptNid;

    /**
     * primordial: first created or developed Sap = Status At Position
     */
    public int primordialSapNid = Integer.MAX_VALUE;
    /**
     * primordial: first created or developed
     * 
     */
    public int primordialUNid = Integer.MIN_VALUE;

    public CopyOnWriteArrayList<R> revisions;

    private ArrayList<IdentifierVersion> additionalIdentifierParts;

    private ArrayList<IdVersion> idVersions;
    
    /**
     * Call when data has changed, so concept updates it's version. 
     */
    protected void modified() {
        try {
            if (Bdb.getNidCNidMap() != null && Bdb.getNidCNidMap().hasConcept(enclosingConceptNid)) {
                Concept c = Bdb.getConcept(enclosingConceptNid);
                if (c != null) {
                    c.modified();
                }
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }
    
    public boolean removeRevision(R r) {
        boolean changed = false;
        if (revisions != null) {
            synchronized (revisions) {
                changed = revisions.remove(r);
                clearVersions();
            }
        }
        return changed;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("nid:");
        buf.append(nid);
        buf.append(" pUuid:");
        buf.append(Bdb.getUuidDb().getUuid(primordialUNid));
        buf.append(" sap: ");
        if (primordialSapNid == Integer.MIN_VALUE) {
            buf.append("Integer.MIN_VALUE");
        } else {
            buf.append(primordialSapNid);
        }
        if (primordialSapNid >= 0) {
            buf.append(" status:");
            ConceptComponent.addNidToBuffer(buf, getStatusId());
            buf.append(" path:");
            ConceptComponent.addNidToBuffer(buf, getPathId());
            buf.append(" tm: ");
            buf.append(TimeUtil.formatDate(getTime()));
        } else {
            buf.append(" !!! Invalid sapNid. Cannot compute path, time, status. !!! ");
        }
        buf.append(" xtraIds:");
        buf.append(additionalIdentifierParts);
        buf.append(" xtraVersions: ");
        buf.append(revisions);
        buf.append("};");
        return buf.toString();
    }

    protected abstract List<? extends Version> getVersions();

    public Set<Integer> getIdSapNids() {
        int size = 1;
        if (idVersions != null) {
            size = size + idVersions.size();
        }
        HashSet<Integer> sapNids = new HashSet<Integer>(size);
        sapNids.add(primordialSapNid);
        if (idVersions != null) {
            for (IdVersion id : idVersions) {
                sapNids.add(id.getSapNid());
            }
        }
        return sapNids;
    }

    public Set<Integer> getComponentSapNids() {
        int size = 1;
        if (revisions != null) {
            size = size + revisions.size();
        }
        HashSet<Integer> sapNids = new HashSet<Integer>(size);
        sapNids.add(primordialSapNid);
        if (revisions != null) {
            for (R r : revisions) {
                sapNids.add(r.sapNid);
            }
        }
        return sapNids;
    }

    public HashMap<Integer, ConceptComponent<?, ?>.Version> getSapMap() {
        int size = 1;
        if (revisions != null) {
            size = size + revisions.size();
        }
        HashMap<Integer, ConceptComponent<?, ?>.Version> sapMap =
                new HashMap<Integer, ConceptComponent<?, ?>.Version>(size);
        for (Version v : getVersions()) {
            sapMap.put(v.getSapNid(), v);
        }
        return sapMap;
    }

    protected ConceptComponent(Concept enclosingConcept, TupleInput input) throws IOException {
        super();
        assert enclosingConcept != null;
        this.enclosingConceptNid = enclosingConcept.getNid();
        readComponentFromBdb(input);
        Bdb.getNidCNidMap().setCidForNid(this.enclosingConceptNid, this.nid);
        assert this.primordialUNid != Integer.MIN_VALUE : "Processing nid: " + enclosingConcept.getNid();
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConcept.getNid();
        assert nid != Integer.MIN_VALUE : "Processing nid: " + enclosingConcept.getNid();
    }

    // TODO move the EComponent constructors to a helper class or factory
    // class...
    // So that the size of this class is kept limited ?
    protected ConceptComponent(EComponent<?> eComponent, Concept enclosingConcept) throws IOException {
        super();
        assert enclosingConcept != null;
        assert eComponent != null;
        this.nid = Bdb.uuidToNid(eComponent.primordialUuid);
        assert this.nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConcept.getNid();
        this.enclosingConceptNid = enclosingConcept.getNid();
        Bdb.getNidCNidMap().setCidForNid(this.enclosingConceptNid, this.nid);
        this.primordialSapNid = Bdb.getSapNid(eComponent);
        this.primordialUNid = Bdb.getUuidsToNidMap().getUNid(eComponent.getPrimordialComponentUuid());
        convertId(eComponent.additionalIds);
        assert this.primordialUNid != Integer.MIN_VALUE : "Processing nid: " + enclosingConcept.getNid();
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConcept.getNid();
        assert nid != Integer.MIN_VALUE : "Processing nid: " + enclosingConcept.getNid();
    }

    public ConceptComponent() {
        Bdb.gVersion.incrementAndGet();
    }

    @SuppressWarnings("unchecked")
    public void merge(C component) {
        Set<Integer> currentSapNids = getComponentSapNids();
        HashMap<Integer, ConceptComponent<?, ?>.Version> newSapMap = component.getSapMap();
        newSapMap.keySet().removeAll(currentSapNids);
        for (ConceptComponent<?, ?>.Version v : newSapMap.values()) {
            assert !currentSapNids.contains(v.getSapNid()) : "currentSapNids: " + currentSapNids + " v: " + v
                + " newSapMap: " + newSapMap;
            addRevision((R) v.makeAnalog(v.getStatusId(), v.getPathId(), v.getTime()));
        }

        currentSapNids = getIdSapNids();
    }

    public Version removeDuplicates(Version dup1, Version dup2) {
        synchronized (revisions) {
            if (dup1.dup) {
                return dup1;
            }
            if (dup2.dup) {
                return dup2;
            }
            if (dup1.index != dup2.index) {

                Version smaller = dup1;
                Version larger = dup2;
                if (dup1.index > dup2.index) {
                    smaller = dup2;
                    larger = dup1;
                }
                larger.dup = true;

                int indexToRemove = larger.index;
                larger.index = smaller.index;
                if (revisions.size() < indexToRemove) {
                    revisions.remove(indexToRemove);
                } else {
                    indexToRemove = revisions.indexOf(larger);
                    if (indexToRemove != dup1.index && indexToRemove >= 0) {
                        revisions.remove(indexToRemove);
                    }
                }
                if (revisions.size() == 0) {
                    revisions = null;
                }
                Concept c = getEnclosingConcept();
                clearVersions();
                c.modified();
                BdbCommitManager.addUncommittedNoChecks(c);
                return larger;
            }
            return dup2;
        }
    }

    protected abstract void clearVersions();

    public Concept getEnclosingConcept() {
        try {
            return Concept.get(enclosingConceptNid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void convertId(List<EIdentifier> list) {
        if (list == null || list.size() == 0) {
            return;
        }
        additionalIdentifierParts = new ArrayList<IdentifierVersion>(list.size());
        for (EIdentifier idv : list) {
            Object denotation = idv.getDenotation();
            switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
            case LONG:
                additionalIdentifierParts.add(new IdentifierVersionLong((EIdentifierLong) idv));
                break;
            case STRING:
                additionalIdentifierParts.add(new IdentifierVersionString((EIdentifierString) idv));
                break;
            case UUID:
                Bdb.getUuidsToNidMap().put((UUID) denotation, nid);
                additionalIdentifierParts.add(new IdentifierVersionUuid((EIdentifierUuid) idv));
                break;
            default:
                throw new UnsupportedOperationException();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup
     * #isSetup()
     */
    public boolean isSetup() {
        assert primordialUNid != Integer.MIN_VALUE;
        return primordialSapNid != Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup
     * #setStatusAtPositionNid(int)
     */
    public void setStatusAtPositionNid(int sapNid) {
        this.primordialSapNid = sapNid;
        modified();
    }

    private void readIdentifierFromBdb(TupleInput input) {
        // nid, list size, and conceptNid are read already by the binder...
        primordialUNid = input.readInt();
        int listSize = input.readShort();
        if (listSize != 0) {
            additionalIdentifierParts = new ArrayList<IdentifierVersion>(listSize);
        }
        for (int i = 0; i < listSize; i++) {
            switch (IDENTIFIER_PART_TYPES.readType(input)) {
            case LONG:
                IdentifierVersionLong idvl = new IdentifierVersionLong(input);
                if (idvl.getTime() != Long.MIN_VALUE) {
                    additionalIdentifierParts.add(idvl);
                }
                break;
            case STRING:
                IdentifierVersionString idvs = new IdentifierVersionString(input);
                if (idvs.getTime() != Long.MIN_VALUE) {
                    additionalIdentifierParts.add(idvs);
                }
                break;
            case UUID:
                IdentifierVersionUuid idvu = new IdentifierVersionUuid(input);
                if (idvu.getTime() != Long.MIN_VALUE) {
                    additionalIdentifierParts.add(idvu);
                }
                break;
            default:
                throw new UnsupportedOperationException();
            }
        }
    }

    private final void writeIdentifierToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        assert primordialSapNid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
        assert primordialUNid != Integer.MIN_VALUE : "Processing nid: " + enclosingConceptNid;
        output.writeInt(primordialUNid);
        List<IdentifierVersion> partsToWrite = new ArrayList<IdentifierVersion>();
        if (additionalIdentifierParts != null) {
            for (IdentifierVersion p : additionalIdentifierParts) {
                if (p.getSapNid() > maxReadOnlyStatusAtPositionNid && p.getTime() != Long.MIN_VALUE) {
                    partsToWrite.add(p);
                }
            }
        }
        // Start writing

        output.writeShort(partsToWrite.size());
        for (IdentifierVersion p : partsToWrite) {
            p.getType().writeType(output);
            p.writeIdPartToBdb(output);
        }
    }

    @Override
    public boolean addMutableIdPart(I_IdPart srcId) {
        return addIdVersion((IdentifierVersion) srcId);
    }

    public boolean addIdVersion(IdentifierVersion srcId) {
        if (additionalIdentifierParts == null) {
            additionalIdentifierParts = new ArrayList<IdentifierVersion>();
        }
        boolean returnValue = additionalIdentifierParts.add(srcId);
        Concept c = getEnclosingConcept();
        c.modified();
        return returnValue;
    }

    @Override
    public final List<I_IdVersion> getIdVersions() {
        List<I_IdVersion> returnValues = new ArrayList<I_IdVersion>();
        if (additionalIdentifierParts != null) {
            returnValues.addAll(additionalIdentifierParts);
        }
        returnValues.add(this);
        return Collections.unmodifiableList(returnValues);
    }

    @Override
    public final int getAuthorityNid() {
        try {
            return ArchitectonicAuxiliary.Concept.UNSPECIFIED_UUID.localize().getNid();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TerminologyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public final Object getDenotation() {
        return Bdb.getUuidDb().getUuid(primordialUNid);
    }

    @Override
    public final void setAuthorityNid(int sourceNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDenotation(Object sourceId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final I_Identify getIdentifier() {
        return this;
    }

    @Override
    public final List<UUID> getUUIDs() {
        List<UUID> returnValues = new ArrayList<UUID>();
        returnValues.add(Bdb.getUuidDb().getUuid(primordialUNid));
        if (additionalIdentifierParts != null) {
            for (IdentifierVersion idv : additionalIdentifierParts) {
                if (IdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
                    IdentifierVersionUuid uuidPart = (IdentifierVersionUuid) idv;
                    returnValues.add(uuidPart.getUuid());
                }
            }
        }
        return returnValues;
    }

    @Override
    public UniversalAceIdentification getUniversalId() throws IOException, TerminologyException {
        UniversalAceIdentification universal = new UniversalAceIdentification(1);
        if (additionalIdentifierParts == null) {
            universal = new UniversalAceIdentification(1);
        } else {
            universal = new UniversalAceIdentification(additionalIdentifierParts.size() + 1);
        }
        UniversalAceIdentificationPart universalPart = new UniversalAceIdentificationPart();
        universalPart.setIdStatus(getUuids(getStatusId()));
        universalPart.setPathId(getUuids(getPathId()));
        universalPart.setSource(getUuids(getAuthorityNid()));
        universalPart.setSourceId(getDenotation());
        universalPart.setTime(getTime());
        universal.addVersion(universalPart);
        if (additionalIdentifierParts != null) {
            for (IdentifierVersion part : additionalIdentifierParts) {
                universalPart = new UniversalAceIdentificationPart();
                universalPart.setIdStatus(getUuids(part.getStatusId()));
                universalPart.setPathId(getUuids(part.getPathId()));
                universalPart.setSource(getUuids(part.getAuthorityNid()));
                universalPart.setSourceId(part.getDenotation());
                universalPart.setTime(part.getTime());
                universal.addVersion(universalPart);
            }
        }
        return universal;
    }

    @Override
    public boolean hasMutableIdPart(I_IdPart newPart) {
        return additionalIdentifierParts.contains(newPart);
    }

    public final boolean addMutablePart(R version) {
        return addRevision(version);
    }

    public final List<Version> getMutableParts(boolean returnConflictResolvedLatestState) throws TerminologyException,
            IOException {
        throw new UnsupportedOperationException("use getVersions()");
    }

    public final int getMutablePartCount() {
        return revisions.size();
    }

    public final int getNid() {
        return nid;
    }

    public final void readComponentFromBdb(TupleInput input) {
        nid = input.readInt();
        primordialSapNid = input.readInt();
        readIdentifierFromBdb(input);
        readFromBdb(input);
    }

    public final void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        assert nid != 0;
        assert primordialSapNid >= 0;
        assert primordialSapNid != Integer.MAX_VALUE;
        output.writeInt(nid);
        output.writeInt(primordialSapNid);
        writeIdentifierToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeToBdb(output, maxReadOnlyStatusAtPositionNid);
    }

    public abstract void readFromBdb(TupleInput input);

    public abstract void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

    /*
     * Below methods have confusing naming, and should be considered for
     * deprecation...
     */
    public final List<IdVersion> getMutableIdParts() {
        if (idVersions == null) {
            int count = 1;
            if (additionalIdentifierParts != null) {
                count = count + additionalIdentifierParts.size();
            }
            idVersions = new ArrayList<IdVersion>(count);
            idVersions.add(new IdVersion());
        }
        if (additionalIdentifierParts != null) {
            for (int i = 0; i < additionalIdentifierParts.size(); i++) {
                idVersions.add(new IdVersion(i));
            }
        }
        return Collections.unmodifiableList(idVersions);
    }

    @SuppressWarnings("unchecked")
    public final boolean addRevision(R r) {
        boolean returnValue = false;
        Concept c = getEnclosingConcept();
        if (revisions == null) {
            revisions = new CopyOnWriteArrayList<R>();
            returnValue = revisions.add(r);
        } else if (revisions.size() == 0) {
            returnValue = revisions.add(r);
        } else if (revisions.get(revisions.size() - 1) != r) {
            returnValue = revisions.add(r);
        }
        r.primordialComponent = (C) this;
        c.modified();
        return returnValue;
    }

    public final boolean addRevisionNoRedundancyCheck(R r) {
        return addRevision(r);
    }

    public final boolean hasRevision(R r) {
        if (revisions == null) {
            return false;
        }
        return revisions.contains(r);
    }

    public final int versionCount() {
        if (revisions == null) {
            return 1;
        }
        return revisions.size() + 1;
    }

    public final Set<TimePathId> getTimePathSet() {
        Set<TimePathId> set = new TreeSet<TimePathId>();
        set.add(new TimePathId(getVersion(), getPathId()));
        if (revisions != null) {
            for (R p : revisions) {
                set.add(new TimePathId(p.getVersion(), p.getPathId()));
            }
        }
        return set;
    }

    @Override
    public I_Identify getFixedPart() {
        return this;
    }

    public Object getDenotation(int authorityNid) throws IOException, TerminologyException {
        if (getAuthorityNid() == authorityNid) {
            return Bdb.getUuidDb().getUuid(primordialUNid);
        }
        for (I_IdPart id : getMutableIdParts()) {
            if (id.getAuthorityNid() == authorityNid) {
                return id.getDenotation();
            }
        }
        return null;
    }

    @Override
    public final int getPathId() {
        return Bdb.getSapDb().getPathId(primordialSapNid);
    }

    @Override
    public final int getStatusId() {
        return Bdb.getSapDb().getStatusId(primordialSapNid);
    }

    @Override
    public final long getTime() {
        return Bdb.getSapDb().getTime(primordialSapNid);
    }

    @Override
    public final int getVersion() {
        return ThinVersionHelper.convert(getTime());
    }

    @Override
    public final void setPathId(int pathId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (pathId != getPathId()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusId(), pathId, Long.MAX_VALUE);
            modified();
        }
    }

    @Override
    public final void setTime(long time) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (time != getTime()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusId(), getPathId(), time);
            modified();
        }
    }

    @Override
    public final void setStatusId(int statusId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (statusId != this.getStatusId()) {
            this.primordialSapNid = Bdb.getSapNid(statusId, getPathId(), Long.MAX_VALUE);
            modified();
        }
    }

    // TODO elimiate "version" from the api since it has double meaning.
    @Override
    public final void setVersion(int version) {
        throw new UnsupportedOperationException("Use makeAnalog instead.");
    }

    public final ArrayIntList getPartComponentNids() {
        ArrayIntList resultList = getVariableVersionNids();
        resultList.add(getPathId());
        resultList.add(getStatusId());
        return resultList;
    }

    protected abstract ArrayIntList getVariableVersionNids();

    @Override
    public final I_Identify getFixedIdPart() {
        return this;
    }

    @Override
    public final I_IdPart getMutableIdPart() {
        return this;
    }

    @Override
    public final I_IdPart duplicateIdPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final I_IdPart makeIdAnalog(int statusNid, int pathNid, long time) {
        throw new UnsupportedOperationException();
    }

    protected int getPrimordialStatusAtPositionNid() {
        return primordialSapNid;
    }

    public abstract boolean fieldsEqual(ConceptComponent<R, C> another);

    public boolean conceptComponentFieldsEqual(ConceptComponent<R, C> another) {
        if (this.nid != another.nid) {
            return false;
        }
        if (this.primordialSapNid != another.primordialSapNid) {
            return false;
        }
        if (this.primordialUNid != another.primordialUNid) {
            return false;
        }
        if (this.additionalIdentifierParts != null && another.additionalIdentifierParts == null) {
            return false;
        }
        if (this.additionalIdentifierParts == null && another.additionalIdentifierParts != null) {
            return false;
        }
        if (this.additionalIdentifierParts != null) {
            if (this.additionalIdentifierParts.equals(another.additionalIdentifierParts) == false) {
                return false;
            }
        }
        if (this.revisions != null && another.revisions == null) {
            return false;
        }
        if (this.revisions == null && another.revisions != null) {
            return false;
        }
        if (this.revisions != null) {
            if (this.revisions.equals(another.revisions) == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     * 
     * @param another
     * @return either a zero length String, or a String containing a description
     *         of the validation failures.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public String validate(ConceptComponent<?, ?> another) throws IOException {
        assert another != null;
        StringBuffer buf = new StringBuffer();
        String validationResults = null;

        if (this.nid != another.nid) {
            buf.append("\tConceptComponent.nid not equal: \n" + "\t\tthis.nid = " + this.nid + "\n"
                + "\t\tanother.nid = " + another.nid + "\n");
        }
        if (this.primordialSapNid != another.primordialSapNid) {
            buf.append("\tConceptComponent.primordialSapNid not equal: \n" + "\t\tthis.primordialSapNid = "
                + this.primordialSapNid + "\n" + "\t\tanother.primordialSapNid = " + another.primordialSapNid + "\n");
        }

        if (this.primordialUNid != another.primordialUNid) {
            buf.append("\tConceptComponent.primordialUNid not equal: \n" + "\t\tthis.primordialUNid = "
                + this.primordialUNid + "\n" + "\t\tanother.primordialUNid = " + another.primordialUNid + "\n");
        }

        if (this.additionalIdentifierParts != null) {
            if (this.additionalIdentifierParts.equals(another.additionalIdentifierParts) == false) {
                buf.append("\tConceptComponent.additionalIdentifierParts not equal: \n"
                    + "\t\tthis.additionalIdentifierParts = " + this.additionalIdentifierParts + "\n"
                    + "\t\tanother.additionalIdentifierParts = " + another.additionalIdentifierParts + "\n");
            }
        }

        if (this.revisions != null) {
            if (this.revisions.equals(another.revisions) == false) {
                for (int i = 0; i < this.revisions.size(); i++) {
                    // make sure there are elements in both arrays to compare
                    if (another.revisions.size() > i) {
                        Revision<R, C> thisRevision = (Revision<R, C>) this.revisions.get(i);
                        Revision<R, C> anotherRevision = (Revision<R, C>) another.revisions.get(i);
                        validationResults = thisRevision.validate(anotherRevision);
                        if (validationResults.length() != 0) {
                            buf.append("\tRevision[" + i + "] not equal: \n");
                            buf.append(validationResults);
                        }
                    } else {
                        buf.append("\tConceptComponent.revision[" + i + "] not equal: \n");
                        buf.append("\t\tThere is no corresponding Revision in another to compare it to.\n");
                    }
                }
            }
        }

        if (buf.length() != 0) {
            // Add a sentinal mark to indicate we reach the top of the hierarchy
            buf.append("\t----------------------------\n");
        }
        return buf.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (ConceptComponent.class.isAssignableFrom(obj.getClass())) {
            ConceptComponent<?, ?> another = (ConceptComponent<?, ?>) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[] { nid, primordialSapNid });
    }

    @Deprecated
    public int getStatus() {
        return getStatusId();
    }

    @Deprecated
    public void setStatus(int idStatus) {
        setStatusId(idStatus);
    }

    public boolean isUncommitted() {
        if (this.getTime() == Long.MAX_VALUE) {
            return true;
        }
        if (additionalIdentifierParts != null) {
            for (IdentifierVersion idv : additionalIdentifierParts) {
                if (idv.getTime() == Long.MAX_VALUE) {
                    return true;
                }
            }
        }
        if (revisions != null) {
            for (R r : revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                    return true;
                }
            }
        }
        return false;
    }

    public UUID getPrimUuid() {
        return Bdb.getUuidDb().getUuid(primordialUNid);
    }

    public ArrayList<IdentifierVersion> getAdditionalIdentifierParts() {
        return additionalIdentifierParts;
    }

    public boolean addStringId(String stringId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionString v = new IdentifierVersionString();
        v.setAuthorityNid(authorityNid);
        v.setDenotation(stringId);
        return addIdVersion(v);
    }

    public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionUuid v = new IdentifierVersionUuid();
        v.setAuthorityNid(authorityNid);
        v.setDenotation(uuidId);
        return addIdVersion(v);
    }

    public boolean addLongId(Long longId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionLong v = new IdentifierVersionLong();
        v.setAuthorityNid(authorityNid);
        v.setDenotation(longId);
        return addIdVersion(v);
    }
}
