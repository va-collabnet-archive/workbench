package org.ihtsdo.concept.component;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_AmTuple;
import org.dwfa.ace.api.I_IdPart;
import org.dwfa.ace.api.I_IdVersion;
import org.dwfa.ace.api.I_Identify;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TestComponent;
import org.dwfa.ace.api.PathSetReadOnly;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.TimePathId;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.HashFunction;
import org.dwfa.vodb.bind.ThinVersionHelper;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.concept.component.identifier.IdentifierVersionLong;
import org.ihtsdo.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.concept.component.identifier.IdentifierVersionUuid;
import org.ihtsdo.concept.component.refset.AnnotationWriter;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.db.util.NidPairForRefset;
import org.ihtsdo.time.TimeUtil;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierString;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import java.util.Arrays;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.hash.Hashcode;

public abstract class ConceptComponent<R extends Revision<R, C>, C extends ConceptComponent<R, C>> implements
        I_AmTermComponent, I_AmPart<R>,
        I_AmTuple<R>, I_Identify, I_IdPart,
        I_IdVersion, I_HandleFutureStatusAtPositionSetup {

    public static void addTextToBuffer(Appendable buf, int nidToConvert) {
        try {
            if (nidToConvert != 0 && Terms.get().hasConcept(nidToConvert)) {
                buf.append(Ts.get().getConcept(nidToConvert).toString());
            } else {
                buf.append(Integer.toString(nidToConvert));
            }
        } catch (IOException e) {
            try {
                buf.append(e.getLocalizedMessage());
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
        }
    }

    public static void addNidToBuffer(Appendable buf, int nidToConvert) {
        try {
            if (nidToConvert != 0 && Terms.get().hasConcept(nidToConvert)) {
                buf.append("\"");
                buf.append(Ts.get().getConcept(nidToConvert).toString());
                buf.append("\" [");
                buf.append(Integer.toString(nidToConvert));
                buf.append("]");
            } else {
                buf.append(Integer.toString(nidToConvert));
            }
        } catch (IOException e) {
            try {
                buf.append(e.getLocalizedMessage());
            } catch (IOException ex) {
                AceLog.getAppLog().alertAndLogException(ex);
            }
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
                if (revisions.contains(e.getRevision())) {
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

    public abstract class Version
            implements I_AmTuple<R>, I_Identify, ComponentVersionBI {

        protected int index = -1;
        private boolean dup = false;

        public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
            return ConceptComponent.this.makeAdjudicationAnalogs(ec, vc);
        }

        @Override
        public ComponentChroncileBI getChronicle() {
            return ConceptComponent.this.getChronicle();
        }

        public Set<Integer> getAllSapNids() throws IOException {
            return ConceptComponent.this.getAllSapNids();
        }

        public boolean isUncommitted() {
            return getTime() == Long.MAX_VALUE;
        }

        public final void setNid(int nid) throws PropertyVetoException {
            if (index == -1) {
                ConceptComponent.this.setNid(nid);
            }
            throw new PropertyVetoException(null, null);
        }

        @Override
        public String toUserString(TerminologySnapshotDI snapshot)
                throws IOException, ContraditionException {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index).toUserString(snapshot);
            }
            return ConceptComponent.this.toUserString(snapshot);
        }

        @Override
        public boolean isBaselineGeneration() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index).isBaselineGeneration();
            }
            return ConceptComponent.this.isBaselineGeneration();
        }

        @Override
        public boolean isActive(NidSetBI allowedStatusNids) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index).isActive(allowedStatusNids);
            }
            return ConceptComponent.this.isActive(allowedStatusNids);
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexes()
                throws IOException {
            return ConceptComponent.this.getRefexes();
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid)
                throws IOException {
            return ConceptComponent.this.getRefexes(refsetNid);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz)
                throws IOException {
            return ConceptComponent.this.getCurrentRefexes(xyz);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
                throws IOException {
            return ConceptComponent.this.getCurrentRefexes(xyz, refsetNid);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(
                ViewCoordinate xyz) throws IOException {
            return ConceptComponent.this.getInactiveRefexes(xyz);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(
                ViewCoordinate xyz) throws IOException {
            return ConceptComponent.this.getCurrentAnnotations(xyz);
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
            return ConceptComponent.this.getAnnotations();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
            return ConceptComponent.this.addAnnotation(annotation);
        }

        @Override
        public boolean hasExtensions() throws IOException {
            return ConceptComponent.this.hasExtensions();
        }

        @Override
        public boolean addLongId(Long longId, int authorityNid, int statusNid, int pathNid, long time) {
            return ConceptComponent.this.addLongId(longId, authorityNid, statusNid, pathNid, time);
        }

        @Override
        public boolean addMutableIdPart(I_IdPart srcId) {
            return ConceptComponent.this.addMutableIdPart(srcId);
        }

        @Override
        public boolean addStringId(String stringId, int authorityNid, int statusNid, int pathNid, long time) {
            return ConceptComponent.this.addStringId(stringId, authorityNid, statusNid, pathNid, time);
        }

        @Override
        public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int pathNid, long time) {
            return ConceptComponent.this.addUuidId(uuidId, authorityNid, statusNid, pathNid, time);
        }

        @Override
        public List<? extends I_IdVersion> getIdVersions() {
            return ConceptComponent.this.getIdVersions();
        }

        @Override
        public List<? extends I_IdPart> getMutableIdParts() {
            return ConceptComponent.this.getMutableIdParts();
        }

        @Override
        public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet, int... authorityNids) {
            return ConceptComponent.this.getVisibleIds(viewpointSet, authorityNids);
        }

        @Override
        public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet) {
            return ConceptComponent.this.getVisibleIds(viewpointSet);
        }

        @Override
        @Deprecated
        public Set<TimePathId> getTimePathSet() {
            return ConceptComponent.this.getTimePathSet();
        }

        @Override
        public List<UUID> getUUIDs() {
            return ConceptComponent.this.getUUIDs();
        }

        @Override
        public UniversalAceIdentification getUniversalId() throws IOException, TerminologyException {
            return ConceptComponent.this.getUniversalId();
        }

        @Override
        public boolean hasMutableIdPart(I_IdPart newPart) {
            return ConceptComponent.this.hasMutableIdPart(newPart);
        }

        @Override
        public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
                Precedence precedence) throws IOException, TerminologyException {
            return ConceptComponent.this.promote(viewPosition, pomotionPaths, allowedStatus, precedence);
        }

        @Override
        public boolean promote(I_TestComponent test, I_Position viewPosition,
                PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
                Precedence precedence) throws IOException, TerminologyException {
            if (test.result(this, viewPosition, pomotionPaths, allowedStatus, precedence)) {
                return promote(viewPosition, pomotionPaths, allowedStatus, precedence);
            }
            return false;
        }

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
            return Hashcode.compute(new int[]{index, nid});
        }

        public Version() {
            super();
        }

        public Version(int index) {
            super();
            this.index = index;
        }

        @Override
        public int getConceptNid() {
            return enclosingConceptNid;
        }

        public R getRevision() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index);
            }
            return makeAnalog(getStatusNid(),
                    getAuthorNid(),
                    getPathNid(),
                    getTime());
        }

        @Override
        public I_AmPart getMutablePart() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index);
            }
            return this;
        }

        @Override
        public boolean sapIsInRange(int min, int max) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                CopyOnWriteArrayList<R> localRevisions = revisions;
                if (localRevisions != null && index < localRevisions.size()) {
                    return revisions.get(index).sapNid >= min
                            && revisions.get(index).sapNid <= max;
                }
                return false;
            }
            return ConceptComponent.this.sapIsInRange(min, max);
        }

        @Override
        public int getSapNid() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index).sapNid;
            }
            return primordialSapNid;
        }

        @Override
        public String toString() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return "Version: " + revisions.get(index).toString();
            }
            return "Version: " + ConceptComponent.this.toString();
        }

        @Override
        public String toUserString() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index).toUserString();
            }
            return ConceptComponent.this.toUserString();
        }

        public List<IdentifierVersion> getAdditionalIdentifierParts() {
            if (additionalIdVersions == null) {
                return Collections.unmodifiableList(new ArrayList<IdentifierVersion>());
            }
            return Collections.unmodifiableList(additionalIdVersions);
        }

        @Override
        @Deprecated
        public int getPathId() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return getMutablePart().getPathNid();
            }
            return Bdb.getSapDb().getPathNid(primordialSapNid);
        }

        @Override
        public int getPathNid() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return getMutablePart().getPathNid();
            }
            return Bdb.getSapDb().getPathNid(primordialSapNid);
        }

        @Override
        @Deprecated
        public int getStatusId() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return getMutablePart().getStatusNid();
            }
            return Bdb.getSapDb().getStatusNid(primordialSapNid);
        }

        @Override
        public int getStatusNid() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return getMutablePart().getStatusNid();
            }
            return Bdb.getSapDb().getStatusNid(primordialSapNid);
        }

        @Override
        public int getAuthorNid() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return getMutablePart().getAuthorNid();
            }
            return Bdb.getSapDb().getAuthorNid(primordialSapNid);
        }

        @Override
        public long getTime() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return getMutablePart().getTime();
            }
            return Bdb.getSapDb().getTime(primordialSapNid);
        }

        @Override
        public void setTime(long time) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                getMutablePart().setTime(time);
            }
            ConceptComponent.this.setTime(time);
        }

        @Override
        @Deprecated
        public int getVersion() {
            if (index >= 0 && revisions != null && index < revisions.size()) {
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
            resultList.add(getPathNid());
            resultList.add(getStatusNid());
            return resultList;
        }

        public abstract ArrayIntList getVariableVersionNids();

        @Override
        @Deprecated
        public void setPathId(int pathId) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                revisions.get(index).setPathNid(pathId);
            } else {
                ConceptComponent.this.setPathNid(pathId);
            }
        }

        @Override
        public void setPathNid(int pathId) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                revisions.get(index).setPathNid(pathId);
            } else {
                ConceptComponent.this.setPathNid(pathId);
            }
        }

        @Override
        @Deprecated
        public void setStatusId(int statusNid) {
            setStatusNid(statusNid);
        }

        @Override
        public void setStatusNid(int statusNid) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                revisions.get(index).setStatusNid(statusNid);
            } else {
                ConceptComponent.this.setStatusNid(statusNid);
            }
        }

        @Override
        public PositionBI getPosition() throws IOException {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                return revisions.get(index).getPosition();
            } else {
                return ConceptComponent.this.getPosition();
            }
        }

        public Set<PositionBI> getPositions() throws IOException {
            return ConceptComponent.this.getPositions();
        }

        @Override
        public void setAuthorNid(int authorNid) {
            if (index >= 0 && revisions != null && index < revisions.size()) {
                revisions.get(index).setAuthorNid(authorNid);
            } else {
                ConceptComponent.this.setAuthorNid(authorNid);
            }
        }

        @Override
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
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return additionalIdVersions.get(index).getSapNid();
            }
            return primordialSapNid;
        }

        @Override
        @Deprecated
        public int getPathId() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getPathId();
            }
            return Bdb.getSapDb().getPathNid(primordialSapNid);
        }

        @Override
        public int getPathNid() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getPathNid();
            }
            return Bdb.getSapDb().getPathNid(primordialSapNid);
        }

        @Override
        public int getAuthorNid() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getAuthorNid();
            }
            return Bdb.getSapDb().getAuthorNid(primordialSapNid);
        }

        @Override
        @Deprecated
        public int getStatusId() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getStatusId();
            }
            return Bdb.getSapDb().getStatusNid(primordialSapNid);
        }

        @Override
        public int getStatusNid() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getStatusNid();
            }
            return Bdb.getSapDb().getStatusNid(primordialSapNid);
        }

        @Override
        public long getTime() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getTime();
            }
            return Bdb.getSapDb().getTime(primordialSapNid);
        }

        @Override
        public int getVersion() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
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
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getAuthorityNid();
            }
            return ConceptComponent.this.getAuthorityNid();
        }

        @Override
        public Object getDenotation() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return getMutableIdPart().getDenotation();
            }
            return ConceptComponent.this.getDenotation();
        }

        @Override
        public I_IdPart getMutableIdPart() {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                return additionalIdVersions.get(index);
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
        public I_IdPart makeIdAnalog(int statusNid, int authorNid, int pathNid, long time) {

            // if (index >= 0) {
            // return additionalIdentifierParts.get(index).makeIdAnalog(statusNid, pathNid, time);
            // }
            // return new IdVersion(IdVersion.this, statusNid, pathNid, time, IdVersion.this);

            return ConceptComponent.this.makeIdAnalog(statusNid, authorNid, pathNid, time);
        }

        public I_IdPart makeIdAnalog() {

            // if (index >= 0) {
            // return additionalIdentifierParts.get(index).makeIdAnalog(statusNid, pathNid, time);
            // }
            // return new IdVersion(IdVersion.this, statusNid, pathNid, time, IdVersion.this);

            return ConceptComponent.this.makeIdAnalog(getStatusNid(),
                    Terms.get().getAuthorNid(),
                    getPathNid(), getTime());
        }

        @Override
        public void setAuthorityNid(int sourceNid) {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                getMutableIdPart().setAuthorityNid(sourceNid);
            }
            // ConceptComponent.this.setAuthorityNid(sourceNid);
        }

        @Override
        public void setDenotation(Object sourceId) {
            if (index >= 0 && additionalIdVersions != null && index < additionalIdVersions.size()) {
                getMutableIdPart().setDenotation(sourceId);
            }
            // ConceptComponent.this.setDenotation(sourceId);
        }

        public int getNid() {
            return nid;
        }

        @Override
        public final ArrayIntList getPartComponentNids() {
            ArrayIntList resultList = getVariableVersionNids();
            resultList.add(getPathNid());
            resultList.add(getStatusNid());
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
    public int primordialSapNid;
    /**
     * primordial: first created or developed
     *
     */
    public int primordialUNid;
    public CopyOnWriteArrayList<R> revisions;
    protected ArrayList<IdentifierVersion> additionalIdVersions;
    private ArrayList<IdVersion> idVersions;
    public ConcurrentSkipListSet<RefsetMember<?, ?>> annotations;

    @SuppressWarnings("rawtypes")
    @Override
    public boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
        if (annotations == null) {
            annotations = new ConcurrentSkipListSet<RefsetMember<?, ?>>(
                    new Comparator<RefexChronicleBI>() {

                        @Override
                        public int compare(RefexChronicleBI t, RefexChronicleBI t1) {
                            return t.getNid() - t1.getNid();
                        }
                    });
        }
        modified();
        Bdb.xrefAnnotation(annotation);
        return annotations.add((RefsetMember<?, ?>) annotation);
    }

    @Override
    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet,
            int... authorityNids) {
        List<I_IdPart> visibleIdParts = new ArrayList<I_IdPart>();
        VersionComputer versionComputer = new VersionComputer();
        visibleIdParts.addAll(versionComputer.getSpecifiedIdParts(viewpointSet, getMutableIdParts(), authorityNids));

        return visibleIdParts;
    }

    @Override
    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet) {
        return getVisibleIds(viewpointSet, new int[]{});
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
        if (annotations == null) {
            return Collections.unmodifiableCollection(
                    new ArrayList<RefexChronicleBI<?>>());
        }
        return Collections.unmodifiableCollection(annotations);
    }

    public ConcurrentSkipListSet<? extends RefexChronicleBI<?>> getAnnotationsMod() {
        return annotations;
    }

    /**
     * Call when data has changed, so concept updates it's version.
     */
    protected void modified() {
        try {
            if (enclosingConceptNid != Integer.MIN_VALUE) {
                if (Bdb.getNidCNidMap() != null && Bdb.getNidCNidMap().hasConcept(enclosingConceptNid)) {
                    Concept c = Bdb.getConcept(enclosingConceptNid);
                    if (c != null) {
                        c.modified();
                    }
                }
            } else {
                AceLog.getAppLog().warning("No enclosingConceptNid for: " + this);
            }
        } catch (IOException e) {
            AceLog.getAppLog().alertAndLogException(e);
        }
    }

    @Override
    public abstract String toUserString();

    @Override
    public String toUserString(TerminologySnapshotDI snapshot)
            throws IOException, ContraditionException {
        return toUserString();
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

    @Override
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
            try {
                buf.append(" status:");
                ConceptComponent.addNidToBuffer(buf, getStatusNid());
                buf.append(" author:");
                ConceptComponent.addNidToBuffer(buf, getAuthorNid());
                buf.append(" path:");
                ConceptComponent.addNidToBuffer(buf, getPathNid());
                buf.append(" tm: ");
                buf.append(TimeUtil.formatDate(getTime()));
                buf.append(" ");
                buf.append(getTime());
            } catch (Throwable e) {
                buf.append(" !!! Invalid sapNid. Cannot compute path, time, status. !!! ");
                buf.append(e.getLocalizedMessage());
            }
        } else {
            buf.append(" !!! Invalid sapNid. Cannot compute path, time, status. !!! ");
        }
        buf.append(" extraVersions: ");
        buf.append(revisions);
        buf.append(" xtraIds:");
        buf.append(additionalIdVersions);
        buf.append(" annotations:");
        buf.append(annotations);
        buf.append("};");
        return buf.toString();
    }

    public abstract List<? extends Version> getVersions();

    public Set<Integer> getAnnotationSapNids() {
        int size = 0;
        if (annotations != null) {
            size = size + annotations.size();
        }
        HashSet<Integer> sapNids = new HashSet<Integer>(size);
        if (annotations != null) {
            for (RefexChronicleBI<?> annotation : annotations) {
                for (RefexVersionBI<?> av : annotation.getVersions()) {
                    sapNids.add(av.getSapNid());
                }
            }
        }
        return sapNids;

    }

    public Set<Integer> getIdSapNids() {
        int size = 1;
        if (additionalIdVersions != null) {
            size = size + additionalIdVersions.size();
        }
        HashSet<Integer> sapNids = new HashSet<Integer>(size);
        sapNids.add(primordialSapNid);
        if (additionalIdVersions != null) {
            for (IdentifierVersion id : additionalIdVersions) {
                sapNids.add(id.getSapNid());
            }
        }
        return sapNids;
    }

    public HashMap<Integer, ConceptComponent<R, C>.Version> getVersionSapMap() {
        int size = 1;
        if (revisions != null) {
            size = size + revisions.size();
        }
        HashMap<Integer, ConceptComponent<R, C>.Version> sapMap =
                new HashMap<Integer, ConceptComponent<R, C>.Version>(size);
        for (Version v : getVersions()) {
            sapMap.put(v.getSapNid(), v);
        }
        return sapMap;
    }

    public static boolean isCanceled(TupleInput input) {
        int nid = input.readInt();
        int primordialSapNid = input.readInt();
        return primordialSapNid == -1;
    }

    protected ConceptComponent(int enclosingConceptNid, TupleInput input) throws IOException {
        super();
        this.enclosingConceptNid = enclosingConceptNid;
        readComponentFromBdb(input);
        int cNid = Bdb.getNidCNidMap().getCNid(nid);
        if (cNid == Integer.MAX_VALUE) {
            Bdb.getNidCNidMap().setCNidForNid(this.enclosingConceptNid, this.nid);
        } else if (cNid != this.enclosingConceptNid) {
            Bdb.getNidCNidMap().resetCidForNid(this.enclosingConceptNid, this.nid);
            AceLog.getAppLog().alertAndLogException(new Exception("Datafix warning. See log for details."));
            AceLog.getAppLog().warning("Datafix warning. cNid "
                    + cNid + " " + Bdb.getUuidsToNidMap().getUuidsForNid(cNid)
                    + "\nincorrect for: " + this.nid + " "
                    + Bdb.getUuidsToNidMap().getUuidsForNid(this.nid)
                    + "\nshould have been: " + this.enclosingConceptNid
                    + Bdb.getUuidsToNidMap().getUuidsForNid(this.enclosingConceptNid)
                    + "\nprocessing: " + this.toString());
        }

        assert this.primordialUNid != Integer.MIN_VALUE : "Processing nid: " + enclosingConceptNid;
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
    }

    // TODO move the EComponent constructors to a helper class or factory class...
    // So that the size of this class is kept limited ?
    protected ConceptComponent(TkComponent<?> eComponent, int enclosingConceptNid) throws IOException {
        super();
        assert eComponent != null;
        this.nid = Bdb.uuidToNid(eComponent.primordialUuid);
        assert this.nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
        this.enclosingConceptNid = enclosingConceptNid;
        int cNid = Bdb.getNidCNidMap().getCNid(nid);
        if (cNid == Integer.MAX_VALUE) {
            Bdb.getNidCNidMap().setCNidForNid(this.enclosingConceptNid, this.nid);
        } else if (cNid != this.enclosingConceptNid) {
            Bdb.getNidCNidMap().resetCidForNid(this.enclosingConceptNid, this.nid);
            AceLog.getAppLog().alertAndLogException(new Exception("Datafix warning. See log for details."));
            AceLog.getAppLog().warning("Datafix warning. cNid "
                    + cNid + " " + Bdb.getUuidsToNidMap().getUuidsForNid(cNid)
                    + "\nincorrect for: " + this.nid + " "
                    + Bdb.getUuidsToNidMap().getUuidsForNid(this.nid)
                    + "\nshould have been: " + this.enclosingConceptNid
                    + Bdb.getUuidsToNidMap().getUuidsForNid(this.enclosingConceptNid)
                    + "\nprocessing: " + this.toString());
        }
        this.primordialSapNid = Bdb.getSapNid(eComponent);
        this.primordialUNid = Bdb.getUuidsToNidMap().getUNid(eComponent.getPrimordialComponentUuid());
        convertId(eComponent.additionalIds);
        assert this.primordialUNid != Integer.MIN_VALUE : "Processing nid: " + enclosingConceptNid;
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
        if (eComponent.getAnnotations() != null) {
            this.annotations = new ConcurrentSkipListSet<RefsetMember<?, ?>>();
            for (TkRefsetAbstractMember<?> eAnnot : eComponent.getAnnotations()) {
                RefsetMember<?, ?> annot = RefsetMemberFactory.create(
                        eAnnot, enclosingConceptNid);
                this.annotations.add(annot);
            }
        }
    }

    public ConceptComponent() {
        Bdb.gVersion.incrementAndGet();
    }

    public final boolean readyToWrite() {
        assert nid != Integer.MAX_VALUE : assertionString();
        assert nid != 0 : assertionString();
        assert readyToWriteComponent();
        if (revisions != null) {
            for (R r : revisions) {
                assert r.readyToWrite();
            }
        }
        if (annotations != null) {
            for (RefsetMember<?, ?> m : annotations) {
                assert m.readyToWrite();
            }
        }
        if (additionalIdVersions != null) {
            for (IdentifierVersion idv : additionalIdVersions) {
                assert idv.readyToWrite();
            }
        }
        return true;
    }

    public abstract boolean readyToWriteComponent();

    public ConceptComponent<R, C> merge(C another) throws IOException {
        Set<Integer> versionSapNids = getVersionSapNids();

        // merge versions
        for (ConceptComponent<R, C>.Version v : another.getVersions()) {
            if (v.getSapNid() != -1 && !versionSapNids.contains(v.getSapNid())) {
                addRevision((R) v.getRevision());
            }
        }

        Set<Integer> identifierSapNids = getIdSapNids();
        // merge identifiers
        if (another.additionalIdVersions != null) {
            if (this.additionalIdVersions == null) {
                this.additionalIdVersions =
                        another.additionalIdVersions;
            } else {
                for (IdentifierVersion idv : another.additionalIdVersions) {
                    if (idv.getSapNid() != -1 && !identifierSapNids.contains(idv.getSapNid())) {
                        this.additionalIdVersions.add(idv);
                    }
                }
            }
        }


        Set<Integer> annotationSapNids = getAnnotationSapNids();
        // merge annotations
        if (another.annotations != null) {
            if (this.annotations == null) {
                this.annotations = another.annotations;
            } else {
                HashMap<Integer, RefsetMember<?, ?>> anotherAnnotationMap =
                        new HashMap<Integer, RefsetMember<?, ?>>();
                for (@SuppressWarnings("rawtypes") RefexChronicleBI annotation : another.annotations) {
                    anotherAnnotationMap.put(annotation.getNid(),
                            (RefsetMember<?, ?>) annotation);
                }

                for (@SuppressWarnings("rawtypes") RefsetMember annotation : this.annotations) {
                    RefsetMember<?, ?> anotherAnnotation =
                            anotherAnnotationMap.remove(annotation.getNid());
                    if (anotherAnnotation != null) {
                        for (@SuppressWarnings("rawtypes") RefsetMember.Version annotationVersion :
                                anotherAnnotation.getVersions()) {
                            if (annotationVersion.getSapNid() != -1 && !annotationSapNids.contains(
                                    annotationVersion.getSapNid())) {
                                annotation.addVersion((I_ExtendByRefPart) annotationVersion.getRevision());
                            }
                        }
                    }
                }

                this.annotations.addAll(anotherAnnotationMap.values());
            }
        }
        return this;
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
                if (revisions.isEmpty()) {
                    revisions = null;
                }
                Concept c = getEnclosingConcept();
                clearVersions();
                c.modified();
                BdbCommitManager.writeImmediate(c);
                return larger;
            }
            return dup2;
        }
    }

    public abstract void clearVersions();

    public Concept getEnclosingConcept() {
        try {
            return Concept.get(enclosingConceptNid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public final void convertId(List<TkIdentifier> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        additionalIdVersions = new ArrayList<IdentifierVersion>(list.size());
        for (TkIdentifier idv : list) {
            Object denotation = idv.getDenotation();
            switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                case LONG:
                    additionalIdVersions.add(new IdentifierVersionLong((TkIdentifierLong) idv));
                    break;
                case STRING:
                    additionalIdVersions.add(new IdentifierVersionString((TkIdentifierString) idv));
                    break;
                case UUID:
                    Bdb.getUuidsToNidMap().put((UUID) denotation, nid);
                    additionalIdVersions.add(new IdentifierVersionUuid((TkIdentifierUuid) idv));
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        idVersions = null;
    }

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup
     * #isSetup()
     */
    @Override
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
    @Override
    public void setStatusAtPositionNid(int sapNid) {
        this.primordialSapNid = sapNid;
        modified();
    }

    private void readIdentifierFromBdb(TupleInput input) {
        // nid, list size, and conceptNid are read already by the binder...
        primordialUNid = input.readInt();
        int listSize = input.readShort();
        if (listSize != 0) {
            additionalIdVersions = new ArrayList<IdentifierVersion>(listSize);
        }
        for (int i = 0; i < listSize; i++) {
            switch (IDENTIFIER_PART_TYPES.readType(input)) {
                case LONG:
                    IdentifierVersionLong idvl = new IdentifierVersionLong(input);
                    if (idvl.getTime() != Long.MIN_VALUE) {
                        additionalIdVersions.add(idvl);
                    }
                    break;
                case STRING:
                    IdentifierVersionString idvs = new IdentifierVersionString(input);
                    if (idvs.getTime() != Long.MIN_VALUE) {
                        additionalIdVersions.add(idvs);
                    }
                    break;
                case UUID:
                    IdentifierVersionUuid idvu = new IdentifierVersionUuid(input);
                    if (idvu.getTime() != Long.MIN_VALUE) {
                        additionalIdVersions.add(idvu);
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        idVersions = null;
    }
    private static AnnotationWriter annotationWriter = new AnnotationWriter();

    private void writeAnnotationsToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        annotationWriter.objectToEntry(annotations, output, maxReadOnlyStatusAtPositionNid);
    }

    private void readAnnotationsFromBdb(TupleInput input) {
        annotations = annotationWriter.entryToObject(input, enclosingConceptNid);
    }

    private void writeIdentifierToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        assert primordialSapNid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;
        assert primordialUNid != Integer.MIN_VALUE : "Processing nid: " + enclosingConceptNid;
        output.writeInt(primordialUNid);
        List<IdentifierVersion> partsToWrite = new ArrayList<IdentifierVersion>();
        if (additionalIdVersions != null) {
            for (IdentifierVersion p : additionalIdVersions) {
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
        if (additionalIdVersions == null) {
            additionalIdVersions = new ArrayList<IdentifierVersion>();
        }
        boolean returnValue = additionalIdVersions.add(srcId);
        idVersions = null;
        Concept c = getEnclosingConcept();
        c.modified();
        return returnValue;
    }

    @Override
    public final List<I_IdVersion> getIdVersions() {
        List<I_IdVersion> returnValues = new ArrayList<I_IdVersion>();
        if (additionalIdVersions != null) {
            returnValues.addAll(additionalIdVersions);
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
    public final List<UUID> getUUIDs() {
        List<UUID> returnValues = new ArrayList<UUID>();
        returnValues.add(Bdb.getUuidDb().getUuid(primordialUNid));
        if (additionalIdVersions != null) {
            for (IdentifierVersion idv : additionalIdVersions) {
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
        if (additionalIdVersions == null) {
            universal = new UniversalAceIdentification(1);
        } else {
            universal = new UniversalAceIdentification(additionalIdVersions.size() + 1);
        }
        UniversalAceIdentificationPart universalPart = new UniversalAceIdentificationPart();
        universalPart.setIdStatus(getUuids(getStatusNid()));
        universalPart.setPathId(getUuids(getPathNid()));
        universalPart.setSource(getUuids(getAuthorityNid()));
        universalPart.setSourceId(getDenotation());
        universalPart.setTime(getTime());
        universal.addVersion(universalPart);
        if (additionalIdVersions != null) {
            for (IdentifierVersion part : additionalIdVersions) {
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
        return additionalIdVersions.contains(newPart);
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

    @Override
    public final int getNid() {
        return nid;
    }

    public final void readComponentFromBdb(TupleInput input) {
        nid = input.readInt();
        primordialSapNid = input.readInt();
        readIdentifierFromBdb(input);
        readAnnotationsFromBdb(input);
        readFromBdb(input);
    }

    public final void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        assert nid != 0;
        assert primordialSapNid >= 0;
        assert primordialSapNid != Integer.MAX_VALUE;
        output.writeInt(nid);
        output.writeInt(primordialSapNid);
        writeIdentifierToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeAnnotationsToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeToBdb(output, maxReadOnlyStatusAtPositionNid);
    }

    public abstract void readFromBdb(TupleInput input);

    public abstract void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

    /*
     * Below methods have confusing naming, and should be considered for
     * deprecation...
     */
    @Override
    public final List<IdVersion> getMutableIdParts() {
        if (idVersions == null) {
            int count = 1;
            if (additionalIdVersions != null) {
                count = count + additionalIdVersions.size();
            }
            ArrayList<IdVersion> idvl = new ArrayList<IdVersion>(count);
            idvl.add(new IdVersion());
            if (additionalIdVersions != null) {
                for (int i = 0; i < additionalIdVersions.size(); i++) {
                    idvl.add(new IdVersion(i));
                }
            }
            idVersions = idvl;
        }
        return Collections.unmodifiableList(idVersions);
    }

    @SuppressWarnings("unchecked")
    public final boolean addRevision(R r) {
        assert r != null;
        boolean returnValue = false;
        Concept c = getEnclosingConcept();
        assert c != null : "Can't find concept for: " + r;
        if (revisions == null) {
            revisions = new CopyOnWriteArrayList<R>();
            returnValue = revisions.add(r);
        } else if (revisions.isEmpty()) {
            returnValue = revisions.add(r);
        } else if (revisions.get(revisions.size() - 1) != r && getVersionSapMap().containsKey(r.sapNid) == false) {
            assert revisions.get(revisions.size() - 1).equals(r) == false :
                    "last revision: " + revisions.get(revisions.size() - 1) + " new revision: " + r;
            returnValue = revisions.add(r); //maybe here...
        }
        r.primordialComponent = (C) this; // maybe here
        c.modified();
        clearVersions();
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

    @Deprecated
    @Override
    public final Set<TimePathId> getTimePathSet() {
        Set<TimePathId> set = new TreeSet<TimePathId>();
        set.add(new TimePathId(getVersion(), getPathNid()));
        if (revisions != null) {
            for (R p : revisions) {
                set.add(new TimePathId(p.getVersion(), p.getPathNid()));
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
    public int getAuthorNid() {
        return Bdb.getSapDb().getAuthorNid(primordialSapNid);
    }

    @Override
    public void setAuthorNid(int authorNid) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (authorNid != getPathNid()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusNid(), authorNid, getPathNid(), Long.MAX_VALUE);
            modified();
        }
    }

    @Override
    @Deprecated
    public final int getPathId() {
        return Bdb.getSapDb().getPathNid(primordialSapNid);
    }

    @Override
    public final int getPathNid() {
        return Bdb.getSapDb().getPathNid(primordialSapNid);
    }

    @Override
    @Deprecated
    public final int getStatusId() {
        return Bdb.getSapDb().getStatusNid(primordialSapNid);
    }

    @Override
    public final int getStatusNid() {
        return Bdb.getSapDb().getStatusNid(primordialSapNid);
    }

    @Override
    public final long getTime() {
        return Bdb.getSapDb().getTime(primordialSapNid);
    }

    @Override
    @Deprecated
    public final int getVersion() {
        return ThinVersionHelper.convert(getTime());
    }

    public final void setNid(int nid) throws PropertyVetoException {
        if (this.getSapNid() != Integer.MAX_VALUE
                && this.getTime() != Long.MAX_VALUE
                && this.nid != nid
                && this.nid != Integer.MAX_VALUE) {
            throw new PropertyVetoException("nid", null);
        }
        this.nid = nid;
    }

    @Override
    @Deprecated
    public final void setPathId(int pathId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (pathId != getPathId()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusId(),
                    Terms.get().getAuthorNid(),
                    pathId, Long.MAX_VALUE);
            modified();
        }
    }

    @Override
    public final void setPathNid(int pathId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (pathId != getPathNid()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusNid(),
                    Terms.get().getAuthorNid(),
                    pathId, Long.MAX_VALUE);
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
            this.primordialSapNid = Bdb.getSapNid(getStatusNid(),
                    Terms.get().getAuthorNid(),
                    getPathNid(), time);
        }
    }

    public final void resetUncommitted(int statusNid, int authorNid, int pathNid) {
        if (getTime() != Long.MIN_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot resetUncommitted if time != Long.MIN_VALUE");
        }
        this.primordialSapNid = Bdb.getSapNid(statusNid,
                authorNid,
                pathNid, Long.MAX_VALUE);
        this.getEnclosingConcept().setIsCanceled(false);
        this.clearVersions();
    }

    @Deprecated
    @Override
    public final void setStatusId(int statusId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (statusId != this.getStatusId()) {
            this.primordialSapNid = Bdb.getSapNid(statusId,
                    Terms.get().getAuthorNid(),
                    getPathId(), Long.MAX_VALUE);
        }
    }

    @Override
    public final void setStatusNid(int statusId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }
        if (statusId != this.getStatusNid()) {
            this.primordialSapNid = Bdb.getSapNid(statusId,
                    Terms.get().getAuthorNid(),
                    getPathNid(), Long.MAX_VALUE);
        }
    }

    @Override
    public final void setVersion(int version) {
        throw new UnsupportedOperationException("Use makeAnalog instead.");
    }

    @Override
    public final ArrayIntList getPartComponentNids() {
        ArrayIntList resultList = getVariableVersionNids();
        resultList.add(getPathNid());
        resultList.add(getStatusNid());
        return resultList;
    }

    protected abstract ArrayIntList getVariableVersionNids();

    @Override
    public final I_IdPart getMutableIdPart() {
        return this;
    }

    @Override
    public final I_IdPart duplicateIdPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final I_IdPart makeIdAnalog(int statusNid, int authorNid, int pathNid, long time) {
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
        if (this.additionalIdVersions != null && another.additionalIdVersions == null) {
            return false;
        }
        if (this.additionalIdVersions == null && another.additionalIdVersions != null) {
            return false;
        }
        if (this.additionalIdVersions != null) {
            if (this.additionalIdVersions.equals(another.additionalIdVersions) == false) {
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
        StringBuilder buf = new StringBuilder();
        String validationResults = null;

        if (this.nid != another.nid) {
            buf.append("\tConceptComponent.nid not equal: \n" + "\t\tthis.nid = ").append(this.nid).append("\n" + "\t\tanother.nid = ").append(another.nid).append("\n");
        }
        if (this.primordialSapNid != another.primordialSapNid) {
            buf.append("\tConceptComponent.primordialSapNid not equal: \n" + "\t\tthis.primordialSapNid = ").append(this.primordialSapNid).append("\n" + "\t\tanother.primordialSapNid = ").append(another.primordialSapNid).append("\n");
        }

        if (this.primordialUNid != another.primordialUNid) {
            buf.append("\tConceptComponent.primordialUNid not equal: \n" + "\t\tthis.primordialUNid = ").append(this.primordialUNid).append("\n" + "\t\tanother.primordialUNid = ").append(another.primordialUNid).append("\n");
        }

        if (this.additionalIdVersions != null) {
            if (this.additionalIdVersions.equals(another.additionalIdVersions) == false) {
                buf.append("\tConceptComponent.additionalIdentifierParts not equal: \n" + "\t\tthis.additionalIdentifierParts = ").append(this.additionalIdVersions).append("\n" + "\t\tanother.additionalIdentifierParts = ").append(another.additionalIdVersions).append("\n");
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
                            buf.append("\tRevision[").append(i).append("] not equal: \n");
                            buf.append(validationResults);
                        }
                    } else {
                        buf.append("\tConceptComponent.revision[").append(i).append("] not equal: \n");
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
        if (obj == null) {
            return false;
        }
        if (ConceptComponent.class.isAssignableFrom(obj.getClass())) {
            ConceptComponent<?, ?> another = (ConceptComponent<?, ?>) obj;
            return this.nid == another.nid;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return HashFunction.hashCode(new int[]{nid, primordialSapNid});
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
        if (additionalIdVersions != null) {
            for (IdentifierVersion idv : additionalIdVersions) {
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
        if (annotations != null) {
            for (RefexChronicleBI<?> r : annotations) {
                if (r.isUncommitted()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public UUID getPrimUuid() {
        return Bdb.getUuidDb().getUuid(primordialUNid);
    }

    public ArrayList<IdentifierVersion> getAdditionalIdentifierParts() {
        return additionalIdVersions;
    }

    @Override
    public boolean addStringId(String stringId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionString v = new IdentifierVersionString(statusNid, Terms.get().getAuthorNid(), pathNid, time);
        v.setAuthorityNid(authorityNid);
        v.setDenotation(stringId);
        return addIdVersion(v);
    }

    public boolean addStringId(String stringId, int authorityNid, int statusNid, int authorNid, int pathNid, long time) {
        IdentifierVersionString v = new IdentifierVersionString(statusNid, authorNid, pathNid, time);
        v.setAuthorityNid(authorityNid);
        v.setDenotation(stringId);
        return addIdVersion(v);
    }

    public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int authorNid, int pathNid, long time) {
        IdentifierVersionUuid v = new IdentifierVersionUuid(statusNid, authorNid, pathNid, time);
        v.setAuthorityNid(authorityNid);
        v.setDenotation(uuidId);
        return addIdVersion(v);
    }

    @Override
    public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionUuid v = new IdentifierVersionUuid(statusNid, Terms.get().getAuthorNid(), pathNid, time);
        v.setAuthorityNid(authorityNid);
        v.setDenotation(uuidId);
        return addIdVersion(v);
    }

    @Override
    public boolean addLongId(Long longId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionLong v = new IdentifierVersionLong(statusNid, Terms.get().getAuthorNid(), pathNid, time);
        v.setAuthorityNid(authorityNid);
        v.setDenotation(longId);
        return addIdVersion(v);
    }

    public boolean addLongId(Long longId, int authorityNid, int statusNid, int authorNid, int pathNid, long time) {
        IdentifierVersionLong v = new IdentifierVersionLong(statusNid, authorNid, pathNid, time);
        v.setAuthorityNid(authorityNid);
        v.setDenotation(longId);
        return addIdVersion(v);
    }

    public void cancel() {
        clearVersions();
        if (this.getTime() == Long.MAX_VALUE) {
            this.primordialSapNid = -1;
        }
        if (additionalIdVersions != null) {
            List<IdentifierVersion> toRemove = new ArrayList<IdentifierVersion>();
            for (IdentifierVersion idv : additionalIdVersions) {
                if (idv.getTime() == Long.MAX_VALUE) {
                    toRemove.add(idv);
                    idv.setTime(Long.MIN_VALUE);
                    idv.setStatusAtPositionNid(-1);
                }
            }
            if (toRemove.size() > 0) {
                for (IdentifierVersion idv : toRemove) {
                    additionalIdVersions.remove(idv);
                }
            }
        }
        if (revisions != null) {
            List<R> toRemove = new ArrayList<R>();
            for (R r : revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                    toRemove.add(r);
                }
            }
            if (toRemove.size() > 0) {
                for (R r : toRemove) {
                    revisions.remove(r);
                }
            }
        }

        if (annotations != null) {
            List<Object> toRemove = new ArrayList<Object>();
            for (Object a : annotations) {
                RefsetMember<? extends RefsetRevision<?, ?>, ? extends RefsetMember<?, ?>> rm = (RefsetMember<?, ?>) a;
                if (rm.getTime() == Long.MAX_VALUE) {
                    toRemove.add(a);
                } else if (rm.revisions != null) {
                    for (RefsetRevision rv : rm.revisions) {
                        List<RefsetRevision> revToRemove = new ArrayList<RefsetRevision>();
                        if (rv.getTime() == Long.MAX_VALUE) {
                            revToRemove.add(rv);
                        }
                        rm.revisions.removeAll(revToRemove);
                    }
                }
            }
            if (toRemove.size() > 0) {
                for (Object r : toRemove) {
                    annotations.remove((RefsetMember<?, ?>) r);
                }
            }
        }
    }

    @Override
    public boolean promote(I_TestComponent test, I_Position viewPosition,
            PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
            Precedence precedence) throws IOException, TerminologyException {
        if (test.result(this, viewPosition, pomotionPaths, allowedStatus, precedence)) {
            return promote(viewPosition, pomotionPaths, allowedStatus, precedence);
        }
        return false;
    }

    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    @Override
    public int getSapNid() {
        return primordialSapNid;
    }

    public Set<Integer> getAllSapNids() throws IOException {
        return getComponentSapNids();
    }

    public Set<Integer> getComponentSapNids() throws IOException {
        int size = 1;
        if (revisions != null) {
            size = size + revisions.size();
        }
        if (additionalIdVersions != null) {
            size = size + additionalIdVersions.size();
        }
        if (annotations != null) {
            size = size + annotations.size();
        }
        HashSet<Integer> sapNids = new HashSet<Integer>(size);

        sapNids.addAll(getVersionSapNids());
        sapNids.addAll(getIdSapNids());
        sapNids.addAll(getAnnotationSapNids());
        return sapNids;
    }

    public Set<Integer> getVersionSapNids() {
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

    public Set<Integer> getRefsetMemberSapNids()
            throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);
        if (pairs == null || pairs.isEmpty()) {
            return new HashSet<Integer>(0);
        }
        HashSet<Integer> returnValues = new HashSet<Integer>(pairs.size());
        for (NidPairForRefset pair : pairs) {
            RefexChronicleBI<?> ext =
                    (RefexChronicleBI<?>) Bdb.getComponent(pair.getMemberNid());
            if (ext != null) {
                for (RefexVersionBI<?> refexV : ext.getVersions()) {
                    returnValues.add(refexV.getSapNid());
                }
                returnValues.addAll(
                        ((ConceptComponent) ext).getRefsetMemberSapNids());
            }
        }
        return returnValues;
    }

    public Collection<? extends RefexChronicleBI<?>> getRefsetMembers()
            throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);
        if (pairs == null || pairs.isEmpty()) {
            return new ArrayList<RefexChronicleBI<?>>(0);
        }
        List<RefexChronicleBI<?>> returnValues = new ArrayList<RefexChronicleBI<?>>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<Integer>();
        for (NidPairForRefset pair : pairs) {
            RefexChronicleBI<?> ext = (RefexChronicleBI<?>) Bdb.getComponent(pair.getMemberNid());
            if (ext != null && !addedMembers.contains(ext.getNid())) {
                addedMembers.add(ext.getNid());
                returnValues.add(ext);
            }
        }
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes()
            throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);
        List<RefexChronicleBI<?>> returnValues = new ArrayList<RefexChronicleBI<?>>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<Integer>();
        if (pairs != null && !pairs.isEmpty()) {
            for (NidPairForRefset pair : pairs) {
                RefexChronicleBI<?> ext = (RefexChronicleBI<?>) Bdb.getComponent(pair.getMemberNid());
                if (ext != null && !addedMembers.contains(ext.getNid())) {
                    addedMembers.add(ext.getNid());
                    returnValues.add(ext);
                }
            }
        }

        ComponentBI component = this;
        if (component instanceof Concept) {
            component = ((Concept) component).getConceptAttributes();
        }
        ComponentChroncileBI<?> cc = (ComponentChroncileBI<?>) component;
        Collection<? extends RefexChronicleBI<?>> fetchedAnnotations = cc.getAnnotations();
        if (fetchedAnnotations != null) {
            for (RefexChronicleBI<?> annotation : fetchedAnnotations) {
                if (addedMembers.contains(annotation.getNid()) == false) {
                    returnValues.add(annotation);
                    addedMembers.add(annotation.getNid());
                }
            }
        }
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> r = getRefexes();
        List<RefexChronicleBI<?>> returnValues = new ArrayList<RefexChronicleBI<?>>(r.size());
        for (RefexChronicleBI<?> rcbi : r) {
            if (rcbi.getCollectionNid() == refsetNid) {
                returnValues.add(rcbi);
            }
        }
        return returnValues;
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes();
        List<RefexVersionBI<?>> returnValues = new ArrayList<RefexVersionBI<?>>(refexes.size());
        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz)
            throws IOException {
        Collection<? extends RefexVersionBI<?>> currentRefexes = new HashSet(getCurrentRefexes(xyz));

        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes();
        List<RefexVersionBI<?>> returnValues =
                new ArrayList<RefexVersionBI<?>>(refexes.size());

        ViewCoordinate allStatus = new ViewCoordinate(xyz);
        allStatus.setAllowedStatusNids(null);

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(allStatus)) {
                if (!currentRefexes.contains(version)) {
                    returnValues.add(version);
                }
            }
        }
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes(refsetNid);
        List<RefexVersionBI<?>> returnValues = new ArrayList<RefexVersionBI<?>>(refexes.size());
        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(
            ViewCoordinate xyz) throws IOException {
        if (annotations == null) {
            return Collections.unmodifiableCollection(
                    new ArrayList<RefexVersionBI<?>>());
        }
        Collection<RefexVersionBI<?>> returnValues = new ArrayList<RefexVersionBI<?>>();
        for (RefexChronicleBI<?> refex : annotations) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public PositionBI getPosition() throws IOException {
        return new Position(getTime(), Ts.get().getPath(getPathNid()));
    }

    public Set<PositionBI> getPositions() throws IOException {
        List<? extends Version> localVersions = getVersions();
        Set<PositionBI> positions = new HashSet<PositionBI>(localVersions.size());
        for (Version v : localVersions) {
            positions.add(v.getPosition());
        }
        return positions;
    }

    @Override
    public ComponentChroncileBI getChronicle() {
        return (ComponentChroncileBI) this;
    }

    @Override
    public boolean isActive(NidSetBI allowedStatusNids) {
        return allowedStatusNids.contains(getStatusNid());
    }

    @Override
    public boolean sapIsInRange(int min, int max) {
        if (primordialSapNid >= min
                && primordialSapNid <= max) {
            return true;
        }
        if (annotations != null) {
            for (RefexChronicleBI<?> a : annotations) {
                for (RefexVersionBI<?> av : a.getVersions()) {
                    if (av.sapIsInRange(min, max)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected String assertionString() {
        try {
            return Ts.get().getConcept(enclosingConceptNid).toLongString();
        } catch (IOException ex) {
            Logger.getLogger(ConceptComponent.class.getName()).log(Level.SEVERE, null, ex);
        }
        return toString();
    }

    @Override
    public boolean isBaselineGeneration() {
        return primordialSapNid <= Bdb.getSapDb().getReadOnlyMax();
    }

    public abstract List<? extends Version> getVersions(ViewCoordinate c);

    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        boolean changed = false;
        Collection<? extends Version> versions = this.getVersions(vc);
        if (ec.getEditPaths().length != 1) {
            throw new IOException("To many edit paths: " + ec);
        }
        int pathNid = ec.getEditPaths()[0];
        if (versions.size() == 1) {
            for (Version cv : versions) {
                if (!cv.isBaselineGeneration() && cv.getPathNid() != pathNid &&
                        cv.getTime() != Long.MAX_VALUE) {
                    changed = true;
                    cv.makeAnalog(cv.getStatusNid(), ec.getAuthorNid(),
                            pathNid, Long.MAX_VALUE);
                }
            }
        }

        // don't adjudicate ids

        // annotations
        if (annotations != null) {
            for (RefsetMember<?, ?> a : annotations) {
                changed = changed || a.makeAdjudicationAnalogs(ec, vc);
            }
        }
        return changed;
    }
}
