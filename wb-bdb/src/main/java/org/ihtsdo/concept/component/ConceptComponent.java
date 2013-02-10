package org.ihtsdo.concept.component;

//~--- non-JDK imports --------------------------------------------------------
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

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
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.ace.utypes.UniversalAceIdentification;
import org.dwfa.ace.utypes.UniversalAceIdentificationPart;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
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
import org.ihtsdo.db.bdb.computer.version.VersionComputer;
import org.ihtsdo.db.util.NidPairForRefset;
import org.ihtsdo.helper.time.TimeHelper;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentBI;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.Precedence;
import org.ihtsdo.tk.api.TerminologySnapshotDI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.id.IdBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierLong;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierString;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifierUuid;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.hash.Hashcode;

//~--- JDK imports ------------------------------------------------------------

import java.beans.PropertyVetoException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dwfa.util.id.Type3UuidFactory;
import org.dwfa.util.id.Type5UuidFactory;
import org.ihtsdo.db.change.ChangeNotifier;

public abstract class ConceptComponent<R extends Revision<R, C>, C extends ConceptComponent<R, C>>
        implements I_AmTermComponent, I_AmPart<R>, I_AmTuple<R>, I_Identify, IdBI, I_IdPart, I_IdVersion,
        I_HandleFutureStatusAtPositionSetup {

    private static AtomicBoolean fixAlert = new AtomicBoolean(false);
    private static AnnotationWriter annotationWriter = new AnnotationWriter();
    //~--- fields --------------------------------------------------------------
    protected ArrayList<IdentifierVersion> additionalIdVersions;
    public ConcurrentSkipListSet<RefsetMember<?, ?>> annotations;
    public int enclosingConceptNid;
    private ArrayList<IdVersion> idVersions;
    public int nid;
    protected long primordialLsb;
    /**
     * primordial: first created or developed
     *
     */
    protected long primordialMsb;
    /**
     * primordial: first created or developed Sap = status, author, position; position = path, time;
     */
    public int primordialSapNid;
    public RevisionSet<R, C> revisions;

    //~--- constructors --------------------------------------------------------
    public ConceptComponent() {
        Bdb.gVersion.incrementAndGet();
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

            if (fixAlert.compareAndSet(true, false)) {
                AceLog.getAppLog().alertAndLogException(new Exception("a. Datafix warning. See log for details."));
                System.out.println("a-Datafix: cNid " + cNid + " "
                        + Bdb.getUuidsToNidMap().getUuidsForNid(cNid) + " incorrect for: "
                        + this.nid + " " + Bdb.getUuidsToNidMap().getUuidsForNid(this.nid)
                        + " should have been: " + this.enclosingConceptNid
                        + Bdb.getUuidsToNidMap().getUuidsForNid(this.enclosingConceptNid));
            }

        }

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
            if (fixAlert.compareAndSet(true, false)) {
                AceLog.getAppLog().alertAndLogException(new Exception("b. Datafix warning. See log for details."));
                System.out.println("b-Datafix: cNid " + cNid + " "
                        + Bdb.getUuidsToNidMap().getUuidsForNid(cNid) + " incorrect for: "
                        + this.nid + " " + Bdb.getUuidsToNidMap().getUuidsForNid(this.nid)
                        + " should have been: " + this.enclosingConceptNid
                        + Bdb.getUuidsToNidMap().getUuidsForNid(this.enclosingConceptNid));
            }
        }

        this.primordialSapNid = Bdb.getSapNid(eComponent);
        assert primordialSapNid > 0 : " Processing nid: " + enclosingConceptNid;
        this.primordialMsb = eComponent.getPrimordialComponentUuid().getMostSignificantBits();
        this.primordialLsb = eComponent.getPrimordialComponentUuid().getLeastSignificantBits();
        convertId(eComponent.additionalIds);
        assert nid != Integer.MAX_VALUE : "Processing nid: " + enclosingConceptNid;

        if (eComponent.getAnnotations() != null) {
            this.annotations = new ConcurrentSkipListSet<RefsetMember<?, ?>>();

            for (TkRefsetAbstractMember<?> eAnnot : eComponent.getAnnotations()) {
                RefsetMember<?, ?> annot = RefsetMemberFactory.create(eAnnot, enclosingConceptNid);

                this.annotations.add(annot);
                ChangeNotifier.touchRefexRC(annot.getReferencedComponentNid());
            }
        }
    }

    public ConceptComponent<R, C> merge(C another, boolean notify) {
        Set<Integer> versionSapNids = getVersionSapNids();

        // merge versions
        for (ConceptComponent<R, C>.Version v : another.getVersions()) {
            if ((v.getSapNid() != -1) && !versionSapNids.contains(v.getSapNid())) {
                if (notify) {
                    addRevision((R) v.getRevision());
                } else {
                    addRevision((R) v.getRevision(), false);
                }

            }
        }

        Set<Integer> identifierSapNids = getIdSapNids();

        // merge identifiers
        if (another.additionalIdVersions != null) {
            if (this.additionalIdVersions == null) {
                this.additionalIdVersions = another.additionalIdVersions;
            } else {
                for (IdentifierVersion idv : another.additionalIdVersions) {
                    if ((idv.getSapNid() != -1) && !identifierSapNids.contains(idv.getSapNid())) {
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
                HashMap<Integer, RefsetMember<?, ?>> anotherAnnotationMap = new HashMap<Integer, RefsetMember<?, ?>>();

                for (RefexChronicleBI annotation : another.annotations) {
                    anotherAnnotationMap.put(annotation.getNid(), (RefsetMember<?, ?>) annotation);
                }

                for (RefsetMember annotation : this.annotations) {
                    RefsetMember<?, ?> anotherAnnotation = anotherAnnotationMap.remove(annotation.getNid());

                    if (anotherAnnotation != null) {
                        for (RefsetMember.Version annotationVersion : anotherAnnotation.getVersions()) {
                            if ((annotationVersion.getSapNid() != -1)
                                    && !annotationSapNids.contains(annotationVersion.getSapNid())) {
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

    //~--- enums ---------------------------------------------------------------
    public enum IDENTIFIER_PART_TYPES {

        LONG(1), STRING(2), UUID(3);
        private int partTypeId;

        //~--- constructors -----------------------------------------------------
        IDENTIFIER_PART_TYPES(int partTypeId) {
            this.partTypeId = partTypeId;
        }

        //~--- methods ----------------------------------------------------------
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

        public void writeType(TupleOutput output) {
            output.writeByte(partTypeId);
        }

        //~--- get methods ------------------------------------------------------
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
    }

    //~--- methods -------------------------------------------------------------
    @SuppressWarnings("rawtypes")
    @Override
    public boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
        if (annotations == null) {
            annotations = new ConcurrentSkipListSet<RefsetMember<?, ?>>(new Comparator<RefexChronicleBI>() {

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

    abstract protected void addComponentNids(Set<Integer> allNids);

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

    public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate ec, long time) {
        IdentifierVersionLong v = null;

        for (int path : ec.getEditPaths()) {
            v = new IdentifierVersionLong(statusNid, ec.getAuthorNid(), path, time);
        }

        v.setAuthorityNid(authorityNid);
        v.setDenotation(longId);

        return addIdVersion(v);
    }

    @Override
    public boolean addLongId(Long longId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionLong v = new IdentifierVersionLong(statusNid, Terms.get().getAuthorNid(), pathNid,
                time);

        v.setAuthorityNid(authorityNid);
        v.setDenotation(longId);

        return addIdVersion(v);
    }

    @Override
    public boolean addMutableIdPart(I_IdPart srcId) {
        return addIdVersion((IdentifierVersion) srcId);
    }

    public final boolean addMutablePart(R version) {
        return addRevision(version);
    }

    public static void addNidToBuffer(Appendable buf, int nidToConvert) {
        try {
            if ((nidToConvert != 0) && Terms.get().hasConcept(nidToConvert)) {
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

    @SuppressWarnings("unchecked")
    public final boolean addRevision(R r) {
        return addRevision(r, true);
    }

    @SuppressWarnings("unchecked")
    public final boolean addRevision(R r, boolean notify) {
        assert r != null;

        boolean returnValue;
        Concept c = getEnclosingConcept();

        assert c != null : "Can't find concept for: " + r;

        if (revisions == null) {
            revisions = new RevisionSet(primordialSapNid);
            returnValue = revisions.add(r);
        } else {
            returnValue = revisions.add(r);
        }

        r.primordialComponent = (C) this;
        if (notify) {
            c.modified();
        }
        clearVersions();
        if (notify) {
            addRevisionHook(returnValue, r);
        }
        return returnValue;
    }

    protected void addRevisionHook(boolean returnValue, R r) {
    }

    public final boolean addRevisionNoRedundancyCheck(R r) {
        return addRevision(r);
    }

    @Override
    public boolean addStringId(String stringId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionString v = new IdentifierVersionString(statusNid, Terms.get().getAuthorNid(), pathNid,
                time);

        v.setAuthorityNid(authorityNid);
        v.setDenotation(stringId);

        return addIdVersion(v);
    }

    public boolean addStringId(String stringId, int authorityNid, int statusNid, int authorNid, int pathNid,
            long time) {
        IdentifierVersionString v = new IdentifierVersionString(statusNid, authorNid, pathNid, time);

        v.setAuthorityNid(authorityNid);
        v.setDenotation(stringId);

        return addIdVersion(v);
    }

    public static void addTextToBuffer(Appendable buf, int nidToConvert) {
        try {
            if ((nidToConvert != 0) && Terms.get().hasConcept(nidToConvert)) {
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

    @Override
    public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int pathNid, long time) {
        IdentifierVersionUuid v = new IdentifierVersionUuid(statusNid, Terms.get().getAuthorNid(), pathNid,
                time);

        v.setAuthorityNid(authorityNid);
        v.setDenotation(uuidId);

        return addIdVersion(v);
    }

    public boolean addUuidId(UUID uuidId, int authorityNid, int statusNid, int authorNid, int pathNid,
            long time) {
        IdentifierVersionUuid v = new IdentifierVersionUuid(statusNid, authorNid, pathNid, time);

        v.setAuthorityNid(authorityNid);
        v.setDenotation(uuidId);

        return addIdVersion(v);
    }

    protected String assertionString() {
        try {
            return Ts.get().getConcept(enclosingConceptNid).toLongString();
        } catch (IOException ex) {
            Logger.getLogger(ConceptComponent.class.getName()).log(Level.SEVERE, null, ex);
        }

        return toString();
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
                }
            }

            for (IdentifierVersion idv : toRemove) {
                additionalIdVersions.remove(idv);
                idv.setStatusAtPositionNid(-1);
            }
        }

        if (revisions != null) {

            for (R r : revisions) {
                if (r.getTime() == Long.MAX_VALUE) {
                    revisions.remove(r);
                    r.sapNid = -1;
                }
            }
        }

        if (annotations != null) {

            for (RefsetMember<?, ?> a : annotations) {
                a.clearVersions();

                if (a.getTime() == Long.MAX_VALUE) {
                    annotations.remove(a);
                    a.setStatusAtPositionNid(-1);
                } else if (a.revisions != null) {
                    for (RefsetRevision rv : a.revisions) {

                        if (rv.getTime() == Long.MAX_VALUE) {
                            a.revisions.remove(rv);
                            rv.sapNid = -1;
                        }
                    }
                }
            }
        }
    }

    protected void clearAnnotationVersions() {
        if (annotations != null) {
            for (RefsetMember<?, ?> rm : annotations) {
                rm.clearVersions();
            }
        }
    }

    public abstract void clearVersions();

    public boolean conceptComponentFieldsEqual(ConceptComponent<R, C> another) {
        if (this.nid != another.nid) {
            return false;
        }

        if (this.primordialSapNid != another.primordialSapNid) {
            return false;
        }

        if (this.primordialLsb != another.primordialLsb) {
            return false;
        }

        if (this.primordialMsb != another.primordialMsb) {
            return false;
        }

        if ((this.additionalIdVersions != null) && (another.additionalIdVersions == null)) {
            return false;
        }

        if ((this.additionalIdVersions == null) && (another.additionalIdVersions != null)) {
            return false;
        }

        if (this.additionalIdVersions != null) {
            if (this.additionalIdVersions.equals(another.additionalIdVersions) == false) {
                return false;
            }
        }

        if ((this.revisions != null) && (another.revisions == null)) {
            return false;
        }

        if ((this.revisions == null) && (another.revisions != null)) {
            return false;
        }

        if (this.revisions != null) {
            if (this.revisions.equals(another.revisions) == false) {
                return false;
            }
        }

        return true;
    }

    public boolean containsSapt(int sapt) {
        if (primordialSapNid == sapt) {
            return true;
        }

        if (revisions != null) {
            for (Revision r : revisions) {
                if (r.sapNid == sapt) {
                    return true;
                }
            }
        }

        return false;
    }

    public final void convertId(List<TkIdentifier> list) {
        if ((list == null) || list.isEmpty()) {
            return;
        }

        additionalIdVersions = new ArrayList<IdentifierVersion>(list.size());

        for (TkIdentifier idv : list) {
            Object denotation = idv.getDenotation();

            switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                case LONG:
                    IdentifierVersionLong idvl = new IdentifierVersionLong((TkIdentifierLong) idv);
                    additionalIdVersions.add(idvl);
                    Bdb.getUuidsToNidMap().put(Type3UuidFactory.fromSNOMED(idvl.getDenotation()), nid);
                    break;

                case STRING:
                    IdentifierVersionString idvs = new IdentifierVersionString((TkIdentifierString) idv);
                    additionalIdVersions.add(idvs);
                    Bdb.getUuidsToNidMap().put(Type3UuidFactory.fromSNOMED(idvs.getDenotation()), nid);

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

    @Override
    public final I_IdPart duplicateIdPart() {
        throw new UnsupportedOperationException();
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
    public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2, Boolean compareAuthoring) {
        List<? extends Version> versions1 = getVersions(vc1);
        List<? extends Version> versions2 = getVersions(vc2);

        if (versions1.size() != versions2.size()) {
            return false;
        } else if (versions1.size() == 1 && versions2.size() == 1) {
            for (Version v1 : versions1) {
                for (Version v2 : versions2) {
                    if (v1 == v2) {
                        return true;
                    }

                    if (v1.getStatusNid() != v2.getStatusNid()) {
                        return false;
                    }

                    if (compareAuthoring) {
                        if (v1.getAuthorNid() != v2.getAuthorNid()) {
                            return false;
                        }

                        if (v1.getPathNid() != v2.getPathNid()) {
                            return false;
                        }
                    }

                    if (v1.getTime() != v2.getTime()) {
                        return false;
                    }

                    if (v1.fieldsEqual(v2)) {
                        return false;
                    }
                }
            }
        } else {
            int foundCount = 0;
            for (Version v1 : versions1) {
                for (Version v2 : versions2) {
                    if (v1 == v2) {
                        foundCount++;
                    } else if (v1.getStatusNid() != v2.getStatusNid()) {
                        continue;
                    } else if (v1.getTime() != v2.getTime()) {
                        continue;
                    } else if (compareAuthoring
                            && (v1.getAuthorNid() != v2.getAuthorNid())) {
                        continue;
                    } else if (compareAuthoring
                            && (v1.getPathNid() != v2.getPathNid())) {
                        continue;
                    } else if (v1.fieldsEqual(v2)) {
                        foundCount++;
                    }
                }
            }
            if (foundCount != versions1.size()) {
                return false;
            }
        }
        return true;
    }

    public abstract boolean fieldsEqual(ConceptComponent<R, C> another);

    @Override
    public int hashCode() {
        return Hashcode.compute(new int[]{nid, primordialSapNid});
    }

    public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
        boolean changed = false;
        List<? extends Version> versions = this.getVersions(vc.getVcWithAllStatusValues());

        if (ec.getEditPaths().length != 1) {
            throw new IOException("Edit paths != 1: " + ec.getEditPaths().length + " " + Arrays.asList(ec));
        }

        int pathNid = ec.getEditPaths()[0];
        if (versions.size() == 1) {
            for (Version cv : versions) {
                if (!cv.isBaselineGeneration() && (cv.getPathNid() != pathNid)
                        && (cv.getTime() != Long.MAX_VALUE)) {
                    changed = true;
                    cv.makeAnalog(cv.getStatusNid(), ec.getAuthorNid(), pathNid, Long.MAX_VALUE);
                }
            }
        } else if (versions.size() > 1) {
            List<? extends Version> resolution = vc.getContradictionManager().resolveVersions(versions);

            if (versions.size() > 0) {
                for (Version cv : resolution) {
                    cv.makeAnalog(cv.getStatusNid(), ec.getAuthorNid(), pathNid, Long.MAX_VALUE);
                    changed = true;
                }
            }
        }

        // don't adjudicate ids
        // annotations
        if (annotations != null) {
            for (RefsetMember<?, ?> a : annotations) {
                boolean annotationChanged = a.makeAdjudicationAnalogs(ec, vc);
                changed = changed || annotationChanged;
            }
        }

        return changed;
    }

    @Override
    public final I_IdPart makeIdAnalog(int statusNid, int authorNid, int pathNid, long time) {
        throw new UnsupportedOperationException();
    }

    public ConceptComponent<R, C> merge(C another) throws IOException {

        return merge(another, true);
    }

    /**
     * Call when data has changed, so concept updates it's version.
     */
    protected void modified() {
        try {
            if (enclosingConceptNid != Integer.MIN_VALUE) {
                if ((Bdb.getNidCNidMap() != null) && Bdb.getNidCNidMap().hasConcept(enclosingConceptNid)) {
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
    public boolean promote(I_TestComponent test, I_Position viewPosition, PathSetReadOnly pomotionPaths,
            NidSetBI allowedStatus, Precedence precedence)
            throws IOException, TerminologyException {
        if (test.result(this, viewPosition, pomotionPaths, allowedStatus, precedence)) {
            return promote(viewPosition, pomotionPaths, allowedStatus, precedence);
        }

        return false;
    }

    private void readAnnotationsFromBdb(TupleInput input) {
        annotations = annotationWriter.entryToObject(input, enclosingConceptNid, nid);
    }

    public final void readComponentFromBdb(TupleInput input) {
        this.nid = input.readInt();
        this.primordialMsb = input.readLong();
        this.primordialLsb = input.readLong();
        this.primordialSapNid = input.readInt();
        assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;;
        readIdentifierFromBdb(input);
        readAnnotationsFromBdb(input);
        readFromBdb(input);
    }

    public abstract void readFromBdb(TupleInput input);

    private void readIdentifierFromBdb(TupleInput input) {

        // nid, list size, and conceptNid are read already by the binder...
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

    public final void resetUncommitted(int statusNid, int authorNid, int pathNid) {
        if (getTime() != Long.MIN_VALUE) {
            throw new UnsupportedOperationException("Cannot resetUncommitted if time != Long.MIN_VALUE");
        }

        this.primordialSapNid = Bdb.getSapNid(statusNid, authorNid, pathNid, Long.MAX_VALUE);
        assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
        this.getEnclosingConcept().setIsCanceled(false);
        this.clearVersions();
    }

    @Override
    public boolean sapIsInRange(int min, int max) {
        if ((primordialSapNid >= min) && (primordialSapNid <= max)) {
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

        if (additionalIdVersions != null) {
            for (IdentifierVersion id : additionalIdVersions) {
                if (id.sapIsInRange(min, max)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("nid:");
        buf.append(nid);
        buf.append(" pUuid:");
        buf.append(new UUID(primordialMsb, primordialLsb));
        buf.append(" sap: ");

        if (primordialSapNid == Integer.MIN_VALUE) {
            buf.append("Integer.MIN_VALUE");
        } else {
            buf.append(primordialSapNid);
        }

        if (primordialSapNid > 0) {
            try {
                buf.append(" status:");
                ConceptComponent.addNidToBuffer(buf, getStatusNid());
                buf.append(" author:");
                ConceptComponent.addNidToBuffer(buf, getAuthorNid());
                buf.append(" path:");
                ConceptComponent.addNidToBuffer(buf, getPathNid());
                UUID authorUuid = Ts.get().getConceptForNid(getAuthorNid()).getPrimUuid();
                String stringToHash = authorUuid.toString()
                        + Long.toString(getTime());
                UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID,
                        stringToHash);
                buf.append(" authTime: ");
                buf.append(type5Uuid);
                buf.append(" tm: ");
                buf.append(TimeHelper.formatDate(getTime()));
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

    @Override
    public abstract String toUserString();

    @Override
    public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
        return toUserString();
    }

    /**
     * Test method to check to see if two objects are equal in all respects.
     *
     * @param another
     * @return either a zero length String, or a String containing a description of the validation failures.
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public String validate(ConceptComponent<?, ?> another) throws IOException {
        assert another != null;

        StringBuilder buf = new StringBuilder();
        String validationResults = null;

        if (this.nid != another.nid) {
            buf.append("\tConceptComponent.nid not equal: \n" + "\t\tthis.nid = ").append(this.nid).append("\n"
                    + "\t\tanother.nid = ").append(another.nid).append("\n");
        }

        if (this.primordialSapNid != another.primordialSapNid) {
            buf.append("\tConceptComponent.primordialSapNid not equal: \n"
                    + "\t\tthis.primordialSapNid = ").append(this.primordialSapNid).append("\n"
                    + "\t\tanother.primordialSapNid = ").append(another.primordialSapNid).append("\n");
        }

        if (this.primordialMsb != another.primordialMsb) {
            buf.append("\tConceptComponent.primordialMsb not equal: \n"
                    + "\t\tthis.primordialMsb = ").append(this.primordialMsb).append("\n"
                    + "\t\tanother.primordialMsb = ").append(another.primordialMsb).append("\n");
        }

        if (this.primordialLsb != another.primordialLsb) {
            buf.append("\tConceptComponent.primordialLsb not equal: \n"
                    + "\t\tthis.primordialLsb = ").append(this.primordialLsb).append("\n"
                    + "\t\tanother.primordialLsb = ").append(another.primordialLsb).append("\n");
        }

        if (this.additionalIdVersions != null) {
            if (this.additionalIdVersions.equals(another.additionalIdVersions) == false) {
                buf.append(
                        "\tConceptComponent.additionalIdentifierParts not equal: \n"
                        + "\t\tthis.additionalIdentifierParts = ").append(this.additionalIdVersions).append(
                        "\n" + "\t\tanother.additionalIdentifierParts = ").append(
                        another.additionalIdVersions).append("\n");
            }
        }

        if (this.revisions != null) {
            if (this.revisions.equals(another.revisions) == false) {
                if (this.revisions.size() != another.revisions.size()) {
                    buf.append("\trevision.size() not equal");
                } else {
                    Iterator<R> thisRevItr = this.revisions.iterator();
                    Iterator<R> anotherRevItr = (Iterator<R>) another.revisions.iterator();

                    while (thisRevItr.hasNext()) {
                        R thisRevision = thisRevItr.next();
                        R anotherRevision = anotherRevItr.next();

                        validationResults = thisRevision.validate(anotherRevision);

                        if (validationResults.length() != 0) {
                            buf.append("\tRevision[").append(thisRevision).append(", ").append(
                                    anotherRevision).append("] not equal: \n");
                            buf.append(validationResults);
                        }
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

    public final int versionCount() {
        if (revisions == null) {
            return 1;
        }

        return revisions.size() + 1;
    }

    private void writeAnnotationsToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        annotationWriter.objectToEntry(annotations, output, maxReadOnlyStatusAtPositionNid);
    }

    public final void writeComponentToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        assert nid != 0;
        assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;;
        assert primordialSapNid != Integer.MAX_VALUE;
        output.writeInt(nid);
        output.writeLong(primordialMsb);
        output.writeLong(primordialLsb);
        output.writeInt(primordialSapNid);
        writeIdentifierToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeAnnotationsToBdb(output, maxReadOnlyStatusAtPositionNid);
        writeToBdb(output, maxReadOnlyStatusAtPositionNid);
    }

    private void writeIdentifierToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid) {
        List<IdentifierVersion> partsToWrite = new ArrayList<IdentifierVersion>();

        if (additionalIdVersions != null) {
            for (IdentifierVersion p : additionalIdVersions) {
                if ((p.getSapNid() > maxReadOnlyStatusAtPositionNid) && (p.getTime() != Long.MIN_VALUE)) {
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

    public abstract void writeToBdb(TupleOutput output, int maxReadOnlyStatusAtPositionNid);

    //~--- get methods ---------------------------------------------------------
    public ArrayList<IdentifierVersion> getAdditionalIdentifierParts() {
        return additionalIdVersions;
    }

    @Override
    public Collection<? extends IdBI> getAdditionalIds() {
        return getAdditionalIdentifierParts();
    }

    @Override
    public Collection<? extends IdBI> getAllIds() {
        return getIdVersions();
    }

    @Override
    public Set<Integer> getAllNidsForId() throws IOException {
        HashSet<Integer> allNids = new HashSet<Integer>();

        allNids.add(nid);
        allNids.add(getStatusNid());
        allNids.add(getAuthorNid());
        allNids.add(getPathNid());

        return allNids;
    }

    @Override
    public Set<Integer> getAllNidsForVersion() throws IOException {
        HashSet<Integer> allNids = new HashSet<Integer>();

        allNids.add(nid);
        allNids.add(getStatusNid());
        allNids.add(getAuthorNid());
        allNids.add(getPathNid());
        addComponentNids(allNids);

        return allNids;
    }

    public Set<Integer> getAllSapNids() throws IOException {
        return getComponentSapNids();
    }

    public Set<Integer> getAnnotationSapNids() {
        int size = 0;

        if (annotations != null) {
            size = size + annotations.size();
        }

        HashSet<Integer> sapNids = new HashSet<Integer>(size);

        if (annotations != null) {
            for (RefexChronicleBI<?> annotation : annotations) {
                for (RefexVersionBI<?> av : annotation.getVersions()) {
                    int sapNid = av.getSapNid();
                    if (sapNid > 0) {
                        sapNids.add(sapNid);
                    }
                }
            }
        }

        return sapNids;
    }

    public Set<Integer> getAnnotationNidsForSaps(Set<Integer> sapNids) {
        int size = 0;

        if (annotations != null) {
            size = size + annotations.size();
        }

        HashSet<Integer> annotNids = new HashSet<Integer>(size);

        if (annotations != null) {
            for (RefexChronicleBI<?> annotation : annotations) {
                for (RefexVersionBI<?> av : annotation.getVersions()) {
                    if (sapNids.contains(av.getSapNid())) {
                        annotNids.add(av.getNid());
                    }
                }
            }
        }

        return annotNids;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
        if (annotations == null) {
            return Collections.unmodifiableCollection(new ArrayList<RefexChronicleBI<?>>());
        }

        return Collections.unmodifiableCollection(annotations);
    }

    public ConcurrentSkipListSet<? extends RefexChronicleBI<?>> getAnnotationsMod() {
        return annotations;
    }

    @Override
    public int getAuthorNid() {
        return Bdb.getSapDb().getAuthorNid(primordialSapNid);
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
    public ComponentChroncileBI getChronicle() {
        return (ComponentChroncileBI) this;
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

    public Set<Integer> getComponentNidsForSaps(Set<Integer> sapNids) throws IOException {
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

        HashSet<Integer> componentNids = new HashSet<Integer>(size);

        componentNids.addAll(getVersionNidsForSaps(sapNids));
        componentNids.addAll(getIdNidsForSaps(sapNids));
        componentNids.addAll(getAnnotationNidsForSaps(sapNids));

        return componentNids;
    }

    @Override
    public int getConceptNid() {
        return enclosingConceptNid;
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz)
            throws IOException {
        if (annotations == null) {
            return Collections.unmodifiableCollection(new ArrayList<RefexVersionBI<?>>());
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
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz,
            int refexNid)
            throws IOException {
        Collection<RefexVersionBI<?>> returnValues = new ArrayList<RefexVersionBI<?>>();

        if (annotations != null) {
            for (RefexChronicleBI<?> refex : annotations) {
                if (refex.getCollectionNid() == refexNid) {
                    for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                        returnValues.add(version);
                    }
                }
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz)
            throws IOException {
        return getCurrentAnnotationMembers(xyz);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz, int refexNid)
            throws IOException {
        return getCurrentAnnotationMembers(xyz, refexNid);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexMembers(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes(refsetNid);
        List<RefexVersionBI<?>> returnValues =
                new ArrayList<RefexVersionBI<?>>(refexes.size());

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz) throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes();
        List<RefexVersionBI<?>> returnValues =
                new ArrayList<RefexVersionBI<?>>(refexes.size());

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                returnValues.add(version);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
            throws IOException {
        return getCurrentRefexMembers(xyz, refsetNid);
    }

    @Override
    public final Object getDenotation() {
        return new UUID(primordialMsb, primordialLsb);
    }

    public Object getDenotation(int authorityNid) throws IOException, TerminologyException {
        if (getAuthorityNid() == authorityNid) {
            return new UUID(primordialMsb, primordialLsb);
        }

        for (I_IdPart id : getMutableIdParts()) {
            if (id.getAuthorityNid() == authorityNid) {
                return id.getDenotation();
            }
        }

        return null;
    }

    public Concept getEnclosingConcept() {
        try {
            return Concept.get(enclosingConceptNid);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public I_Identify getFixedPart() {
        return this;
    }

    public Set<Integer> getIdSapNids() {
        int size = 1;

        if (additionalIdVersions != null) {
            size = size + additionalIdVersions.size();
        }

        HashSet<Integer> sapNids = new HashSet<Integer>(size);

        assert primordialSapNid != 0;
        sapNids.add(primordialSapNid);

        if (additionalIdVersions != null) {
            for (IdentifierVersion id : additionalIdVersions) {
                int sapNid = id.getSapNid();
                if (sapNid > 0) {
                    sapNids.add(id.getSapNid());
                }
            }
        }

        return sapNids;
    }

    public Set<Integer> getIdNidsForSaps(Set<Integer> sapNids) {
        int size = 1;

        if (additionalIdVersions != null) {
            size = size + additionalIdVersions.size();
        }

        HashSet<Integer> componentNids = new HashSet<Integer>(size);
        if (additionalIdVersions != null) {
            for (IdentifierVersion id : additionalIdVersions) {
                if (sapNids.contains(id.getSapNid())) {
                    componentNids.add(this.nid);
                }
            }
        }

        return componentNids;
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
    public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz) throws IOException {
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
    public final I_IdPart getMutableIdPart() {
        return this;
    }

    /*
     * Below methods have confusing naming, and should be considered for deprecation...
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

    public final int getMutablePartCount() {
        return revisions.size();
    }

    public final List<Version> getMutableParts(boolean returnConflictResolvedLatestState)
            throws TerminologyException, IOException {
        throw new UnsupportedOperationException("use getVersions()");
    }

    @Override
    public final int getNid() {
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
    public final int getPathId() {
        return Bdb.getSapDb().getPathNid(primordialSapNid);
    }

    @Override
    public final int getPathNid() {
        return Bdb.getSapDb().getPathNid(primordialSapNid);
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
    public UUID getPrimUuid() {
        return new UUID(primordialMsb, primordialLsb);
    }

    protected int getPrimordialStatusAtPositionNid() {
        return primordialSapNid;
    }

    @Override
    public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
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
    public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);
        List<RefexChronicleBI<?>> returnValues = new ArrayList<RefexChronicleBI<?>>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<Integer>();

        if ((pairs != null) && !pairs.isEmpty()) {
            for (NidPairForRefset pair : pairs) {
                RefexChronicleBI<?> ext = (RefexChronicleBI<?>) Bdb.getComponent(pair.getMemberNid());

                if ((ext != null) && !addedMembers.contains(ext.getNid()) && 
                        ext.getPrimordialVersion().getSapNid() != -1) {
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
                if (addedMembers.contains(annotation.getNid()) == false  && 
                        annotation.getPrimordialVersion().getSapNid() != -1) {
                    returnValues.add(annotation);
                    addedMembers.add(annotation.getNid());
                }
            }
        }
                
        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    @Deprecated
    public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
        return getRefexMembers(refsetNid);
    }

    public Set<Integer> getRefsetMemberSapNids() throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);

        if ((pairs == null) || pairs.isEmpty()) {
            return new HashSet<Integer>(0);
        }

        HashSet<Integer> returnValues = new HashSet<Integer>(pairs.size());

        for (NidPairForRefset pair : pairs) {
            RefexChronicleBI<?> ext = (RefexChronicleBI<?>) Bdb.getComponent(pair.getMemberNid());

            if (ext != null) {
                for (RefexVersionBI<?> refexV : ext.getVersions()) {
                    returnValues.add(refexV.getSapNid());
                }

                returnValues.addAll(((ConceptComponent) ext).getRefsetMemberSapNids());
            }
        }

        return returnValues;
    }

    public Collection<? extends RefexChronicleBI<?>> getRefsetMembers() throws IOException {
        List<NidPairForRefset> pairs = Bdb.getRefsetPairs(nid);

        if ((pairs == null) || pairs.isEmpty()) {
            return new ArrayList<RefexChronicleBI<?>>(0);
        }

        List<RefexChronicleBI<?>> returnValues = new ArrayList<RefexChronicleBI<?>>(pairs.size());
        HashSet<Integer> addedMembers = new HashSet<Integer>();

        for (NidPairForRefset pair : pairs) {
            RefexChronicleBI<?> ext = (RefexChronicleBI<?>) Bdb.getComponent(pair.getMemberNid());

            if ((ext != null) && !addedMembers.contains(ext.getNid())) {
                addedMembers.add(ext.getNid());
                returnValues.add(ext);
            }
        }

        return Collections.unmodifiableCollection(returnValues);
    }

    @Override
    public int getSapNid() {
        return primordialSapNid;
    }

    @Deprecated
    public int getStatus() {
        return getStatusId();
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
    public final List<UUID> getUUIDs() {
        List<UUID> returnValues = new ArrayList<UUID>();

        returnValues.add(new UUID(primordialMsb, primordialLsb));

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

    private static List<UUID> getUuids(int conceptNid) throws IOException {
        return Bdb.getConceptDb().getUuidsForConcept(conceptNid);
    }

    protected abstract ArrayIntList getVariableVersionNids();

    @Override
    @Deprecated
    public final int getVersion() {
        return ThinVersionHelper.convert(getTime());
    }

    public HashMap<Integer, ConceptComponent<R, C>.Version> getVersionSapMap() {
        int size = 1;

        if (revisions != null) {
            size = size + revisions.size();
        }

        HashMap<Integer, ConceptComponent<R, C>.Version> sapMap = new HashMap<Integer, ConceptComponent<R, C>.Version>(size);

        for (Version v : getVersions()) {
            sapMap.put(v.getSapNid(), v);
        }

        return sapMap;
    }

    public Set<Integer> getVersionSapNids() {
        int size = 1;

        if (revisions != null) {
            size = size + revisions.size();
        }

        HashSet<Integer> sapNids = new HashSet<Integer>(size);

        assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
        sapNids.add(primordialSapNid);

        if (revisions != null) {
            for (R r : revisions) {
                if (r.sapNid > 0) {
                    sapNids.add(r.sapNid);
                }
            }
        }

        return sapNids;
    }

    public Set<Integer> getVersionNidsForSaps(Set<Integer> sapNids) {
        int size = 1;

        if (revisions != null) {
            size = size + revisions.size();
        }

        HashSet<Integer> componentNids = new HashSet<Integer>(size);

        if (revisions != null) {
            for (R r : revisions) {
                if (sapNids.contains(r.sapNid)) {
                    componentNids.add(r.getNid());
                }
            }
        }

        return componentNids;
    }

    public abstract List<? extends Version> getVersions();

    public abstract List<? extends Version> getVersions(ViewCoordinate c);

    @Override
    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet) {
        return getVisibleIds(viewpointSet, new int[]{});
    }

    @Override
    public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet, int... authorityNids) {
        List<I_IdPart> visibleIdParts = new ArrayList<I_IdPart>();
        VersionComputer versionComputer = new VersionComputer();

        visibleIdParts.addAll(versionComputer.getSpecifiedIdParts(viewpointSet, getMutableIdParts(),
                authorityNids));

        return visibleIdParts;
    }

    @Override
    public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        Collection<? extends RefexChronicleBI<?>> members = getCurrentAnnotationMembers(xyz, refsetNid);

        for (RefexChronicleBI<?> refex : members) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
        Collection<? extends RefexChronicleBI<?>> refexes = getRefexes(refsetNid);

        for (RefexChronicleBI<?> refex : refexes) {
            for (RefexVersionBI<?> version : refex.getVersions(xyz)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasMutableIdPart(I_IdPart newPart) {
        return additionalIdVersions.contains(newPart);
    }

    public final boolean hasRevision(R r) {
        if (revisions == null) {
            return false;
        }

        return revisions.contains(r);
    }

    @Override
    public boolean isActive(NidSetBI allowedStatusNids) {
        return allowedStatusNids.contains(getStatusNid());
    }

    @Override
    public boolean isActive(ViewCoordinate vc) {
        return isActive(vc.getAllowedStatusNids());
    }

    @Override
    public boolean isBaselineGeneration() {
        return primordialSapNid <= Bdb.getSapDb().getReadOnlyMax();
    }

    public static boolean isCanceled(TupleInput input) {
        int nid = input.readInt();
        int primordialSapNid = input.readInt();

        return primordialSapNid == -1;
    }

    /*
     * (non-Javadoc) @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup #isSetup()
     */
    @Override
    public boolean isSetup() {
        return primordialSapNid != Integer.MAX_VALUE;
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

    //~--- set methods ---------------------------------------------------------
    @Override
    public void setAuthorNid(int authorNid) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (authorNid != getAuthorNid()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusNid(), authorNid, getPathNid(), Long.MAX_VALUE);
            assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;;
            modified();
        }
    }

    @Override
    public final void setAuthorityNid(int sourceNid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setDenotation(Object sourceId) {
        throw new UnsupportedOperationException();
    }

    public final void setNid(int nid) throws PropertyVetoException {
        if ((this.getSapNid() != Integer.MAX_VALUE) && (this.getTime() != Long.MAX_VALUE) && (this.nid != nid)
                && (this.nid != Integer.MAX_VALUE)) {
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
            this.primordialSapNid = Bdb.getSapNid(getStatusId(), Terms.get().getAuthorNid(), pathId,
                    Long.MAX_VALUE);
            assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
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
            this.primordialSapNid = Bdb.getSapNid(getStatusNid(), Terms.get().getAuthorNid(), pathId,
                    Long.MAX_VALUE);
            assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
            modified();
        }
    }

    public void setPrimordialUuid(UUID pUuid) {
        this.primordialMsb = pUuid.getMostSignificantBits();
        this.primordialLsb = pUuid.getLeastSignificantBits();
    }

    @Deprecated
    public void setStatus(int idStatus) {
        setStatusId(idStatus);
    }

    /*
     * (non-Javadoc) @see org.ihtsdo.db.bdb.concept.component.I_HandleDeferredStatusAtPositionSetup
     * #setStatusAtPositionNid(int)
     */
    @Override
    public void setStatusAtPositionNid(int sapNid) {
        this.primordialSapNid = sapNid;
        assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
        modified();
    }

    @Deprecated
    @Override
    public final void setStatusId(int statusId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (statusId != this.getStatusId()) {
            this.primordialSapNid = Bdb.getSapNid(statusId, Terms.get().getAuthorNid(), getPathId(),
                    Long.MAX_VALUE);
            assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

    @Override
    public final void setStatusNid(int statusId) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (statusId != this.getStatusNid()) {
            this.primordialSapNid = Bdb.getSapNid(statusId, Terms.get().getAuthorNid(), getPathNid(),
                    Long.MAX_VALUE);
            assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

    @Override
    public final void setTime(long time) {
        if (getTime() != Long.MAX_VALUE) {
            throw new UnsupportedOperationException(
                    "Cannot change status if time != Long.MAX_VALUE; Use makeAnalog instead.");
        }

        if (time != getTime()) {
            this.primordialSapNid = Bdb.getSapNid(getStatusNid(), Terms.get().getAuthorNid(), getPathNid(),
                    time);
            assert primordialSapNid != 0 : "Processing nid: " + enclosingConceptNid;
        }
    }

    @Override
    public final void setVersion(int version) {
        throw new UnsupportedOperationException("Use makeAnalog instead.");
    }

    //~--- inner classes -------------------------------------------------------
    public class IdVersion implements I_IdVersion, I_IdPart {

        protected int index = -1;

        //~--- constructors -----------------------------------------------------
        public IdVersion() {
            super();
        }

        public IdVersion(int index) {
            super();
            this.index = index;
        }

        //~--- methods ----------------------------------------------------------
        @Override
        public I_IdPart duplicateIdPart() {
            throw new UnsupportedOperationException("Use makeAnalog instead");
        }

        public I_IdPart makeIdAnalog() {

            // if (index >= 0) {
            // return additionalIdentifierParts.get(index).makeIdAnalog(statusNid, pathNid, time);
            // }
            // return new IdVersion(IdVersion.this, statusNid, pathNid, time, IdVersion.this);
            return ConceptComponent.this.makeIdAnalog(getStatusNid(), Terms.get().getAuthorNid(), getPathNid(),
                    getTime());
        }

        @Override
        public I_IdPart makeIdAnalog(int statusNid, int authorNid, int pathNid, long time) {

            // if (index >= 0) {
            // return additionalIdentifierParts.get(index).makeIdAnalog(statusNid, pathNid, time);
            // }
            // return new IdVersion(IdVersion.this, statusNid, pathNid, time, IdVersion.this);
            return ConceptComponent.this.makeIdAnalog(statusNid, authorNid, pathNid, time);
        }

        //~--- get methods ------------------------------------------------------
        @Override
        public Set<Integer> getAllNidsForId() throws IOException {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getAllNidsForId();
            }

            return getAllNidsForId();
        }

        @Override
        public int getAuthorNid() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getAuthorNid();
            }

            return Bdb.getSapDb().getAuthorNid(primordialSapNid);
        }

        @Override
        public int getAuthorityNid() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getAuthorityNid();
            }

            return ConceptComponent.this.getAuthorityNid();
        }

        @Override
        public Object getDenotation() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getDenotation();
            }

            return ConceptComponent.this.getDenotation();
        }

        @Override
        public I_IdPart getMutableIdPart() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return additionalIdVersions.get(index);
            }

            return this;
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
        public int getPathId() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getPathNid();
            }

            return Bdb.getSapDb().getPathNid(primordialSapNid);
        }

        @Override
        public int getPathNid() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getPathNid();
            }

            return Bdb.getSapDb().getPathNid(primordialSapNid);
        }

        @Override
        public int getSapNid() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return additionalIdVersions.get(index).getSapNid();
            }

            return primordialSapNid;
        }

        @Override
        @Deprecated
        public int getStatusId() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getStatusNid();
            }

            return Bdb.getSapDb().getStatusNid(primordialSapNid);
        }

        @Override
        public int getStatusNid() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getStatusNid();
            }

            return Bdb.getSapDb().getStatusNid(primordialSapNid);
        }

        @Override
        public long getTime() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getTime();
            }

            return Bdb.getSapDb().getTime(primordialSapNid);
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
        public int getVersion() {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                return getMutableIdPart().getVersion();
            }

            return Bdb.getSapDb().getVersion(primordialSapNid);
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setAuthorityNid(int sourceNid) {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                getMutableIdPart().setAuthorityNid(sourceNid);
            }

            // ConceptComponent.this.setAuthorityNid(sourceNid);
        }

        @Override
        public void setDenotation(Object sourceId) {
            if ((index >= 0) && (additionalIdVersions != null) && (index < additionalIdVersions.size())) {
                getMutableIdPart().setDenotation(sourceId);
            }

            // ConceptComponent.this.setDenotation(sourceId);
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

    public abstract class Version implements I_AmTuple<R>, I_Identify, ComponentVersionBI {

        protected ComponentVersionBI cv;

        //~--- constructors -----------------------------------------------------
        public Version(ComponentVersionBI cv) {
            super();
            this.cv = cv;
        }

        //~--- methods ----------------------------------------------------------
        public Concept getEnclosingConcept() {
            return ConceptComponent.this.getEnclosingConcept();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean addAnnotation(RefexChronicleBI annotation) throws IOException {
            return ConceptComponent.this.addAnnotation(annotation);
        }

        public boolean addLongId(Long longId, int authorityNid, int statusNid, EditCoordinate ec, long time) {
            return ConceptComponent.this.addLongId(longId, authorityNid, statusNid, ec, time);
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
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (Version.class.isAssignableFrom(obj.getClass())) {
                Version another = (Version) obj;

                if ((this.getNid() == another.getNid()) && (this.getSapNid() == another.getSapNid())) {
                    return true;
                }
            }

            return false;
        }

        public abstract boolean fieldsEqual(ConceptComponent<R, C>.Version another);

        @Override
        public boolean versionsEqual(ViewCoordinate vc1, ViewCoordinate vc2,
                Boolean compareAuthoring) {
            return ConceptComponent.this.versionsEqual(vc1, vc2, compareAuthoring);
        }

        @Override
        public int hashCode() {
            return Hashcode.compute(new int[]{this.getSapNid(), nid});
        }

        public boolean makeAdjudicationAnalogs(EditCoordinate ec, ViewCoordinate vc) throws IOException {
            return ConceptComponent.this.makeAdjudicationAnalogs(ec, vc);
        }

        @Override
        public boolean promote(PositionBI viewPosition, PathSetReadOnly pomotionPaths, NidSetBI allowedStatus,
                Precedence precedence)
                throws IOException, TerminologyException {
            return ConceptComponent.this.promote(viewPosition, pomotionPaths, allowedStatus, precedence);
        }

        @Override
        public boolean promote(I_TestComponent test, I_Position viewPosition, PathSetReadOnly pomotionPaths,
                NidSetBI allowedStatus, Precedence precedence)
                throws IOException, TerminologyException {
            if (test.result(this, viewPosition, pomotionPaths, allowedStatus, precedence)) {
                return promote(viewPosition, pomotionPaths, allowedStatus, precedence);
            }

            return false;
        }

        @Override
        public boolean sapIsInRange(int min, int max) {
            return cv.sapIsInRange(min, max);
        }

        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            try {
                buf.append("Version: ");
                buf.append(cv.toString());
                UUID authorUuid = Ts.get().getConceptForNid(getAuthorNid()).getPrimUuid();
                String stringToHash = authorUuid.toString()
                        + Long.toString(getTime());
                UUID type5Uuid = Type5UuidFactory.get(Type5UuidFactory.AUTHOR_TIME_ID,
                        stringToHash);
                buf.append(" authTime: ");
                buf.append(type5Uuid);
            } catch (Throwable e) {
                buf.append(" !!! Error computing author time hash !!! ");
                buf.append(e.getLocalizedMessage());
            }
            return buf.toString();
        }

        @Override
        public String toUserString() {
            return cv.toUserString();
        }

        @Override
        public String toUserString(TerminologySnapshotDI snapshot) throws IOException, ContradictionException {
            return cv.toUserString(snapshot);
        }

        //~--- get methods ------------------------------------------------------
        public List<IdentifierVersion> getAdditionalIdentifierParts() {
            if (additionalIdVersions == null) {
                return Collections.unmodifiableList(new ArrayList<IdentifierVersion>());
            }

            return Collections.unmodifiableList(additionalIdVersions);
        }

        @Override
        public Collection<? extends IdBI> getAdditionalIds() {
            return ConceptComponent.this.getAdditionalIds();
        }

        @Override
        public Collection<? extends IdBI> getAllIds() {
            return ConceptComponent.this.getIdVersions();
        }

        @Override
        public Set<Integer> getAllNidsForVersion() throws IOException {
            return cv.getAllNidsForVersion();
        }

        public Set<Integer> getAllSapNids() throws IOException {
            return ConceptComponent.this.getAllSapNids();
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getAnnotations() {
            return ConceptComponent.this.getAnnotations();
        }

        @Override
        public int getAuthorNid() {
            return cv.getAuthorNid();
        }

        @Override
        public ComponentChroncileBI getChronicle() {
            return ConceptComponent.this.getChronicle();
        }

        @Override
        public int getConceptNid() {
            return enclosingConceptNid;
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz)
                throws IOException {
            return ConceptComponent.this.getCurrentAnnotationMembers(xyz);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentAnnotationMembers(ViewCoordinate xyz,
                int refexNid)
                throws IOException {
            return ConceptComponent.this.getCurrentAnnotationMembers(xyz, refexNid);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz)
                throws IOException {
            return getCurrentAnnotationMembers(xyz);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentAnnotations(ViewCoordinate xyz, int refexNid)
                throws IOException {
            return getCurrentAnnotationMembers(xyz, refexNid);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentRefexMembers(ViewCoordinate xyz, int refsetNid)
                throws IOException {
            return ConceptComponent.this.getCurrentRefexMembers(xyz, refsetNid);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz)
                throws IOException {
            return ConceptComponent.this.getCurrentRefexes(xyz);
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getCurrentRefexes(ViewCoordinate xyz, int refsetNid)
                throws IOException {
            return ConceptComponent.this.getCurrentRefexMembers(xyz, refsetNid);
        }

        @Override
        public I_AmTermComponent getFixedPart() {
            return ConceptComponent.this;
        }

        @Override
        public List<? extends I_IdVersion> getIdVersions() {
            return ConceptComponent.this.getIdVersions();
        }

        @Override
        public Collection<? extends RefexVersionBI<?>> getInactiveRefexes(ViewCoordinate xyz)
                throws IOException {
            return ConceptComponent.this.getInactiveRefexes(xyz);
        }

        @Override
        public List<? extends I_IdPart> getMutableIdParts() {
            return ConceptComponent.this.getMutableIdParts();
        }

        @Override
        public I_AmPart getMutablePart() {
            return (I_AmPart) cv;
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

        @Override
        @Deprecated
        public int getPathId() {
            return cv.getPathNid();
        }

        @Override
        public int getPathNid() {
            return cv.getPathNid();
        }

        @Override
        public PositionBI getPosition() throws IOException {
            return cv.getPosition();
        }

        public Set<PositionBI> getPositions() throws IOException {
            return ConceptComponent.this.getPositions();
        }

        @Override
        public UUID getPrimUuid() {
            return new UUID(primordialMsb, primordialLsb);
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexMembers(int refsetNid) throws IOException {
            return ConceptComponent.this.getRefexMembers(refsetNid);
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexes() throws IOException {
            return ConceptComponent.this.getRefexes();
        }

        @Override
        public Collection<? extends RefexChronicleBI<?>> getRefexes(int refsetNid) throws IOException {
            return ConceptComponent.this.getRefexes(refsetNid);
        }

        public R getRevision() {
            if (cv == ConceptComponent.this) {
                return makeAnalog(getStatusNid(), getAuthorNid(), getPathNid(), getTime());
            }

            return (R) cv;
        }

        @Override
        public int getSapNid() {
            return cv.getSapNid();
        }

        @Override
        @Deprecated
        public int getStatusId() {
            return cv.getStatusNid();
        }

        @Override
        public int getStatusNid() {
            return cv.getStatusNid();
        }

        @Override
        public long getTime() {
            return cv.getTime();
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

        public abstract ArrayIntList getVariableVersionNids();

        @Override
        @Deprecated
        public int getVersion() {
            return Bdb.getSapDb().getVersion(cv.getSapNid());
        }

        @Override
        public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet) {
            return ConceptComponent.this.getVisibleIds(viewpointSet);
        }

        @Override
        public List<? extends I_IdPart> getVisibleIds(PositionSet viewpointSet, int... authorityNids) {
            return ConceptComponent.this.getVisibleIds(viewpointSet, authorityNids);
        }

        @Override
        public boolean hasCurrentAnnotationMember(ViewCoordinate xyz, int refsetNid) throws IOException {
            return ConceptComponent.this.hasCurrentAnnotationMember(xyz, refsetNid);
        }

        @Override
        public boolean hasCurrentRefexMember(ViewCoordinate xyz, int refsetNid) throws IOException {
            return ConceptComponent.this.hasCurrentRefexMember(xyz, refsetNid);
        }

        @Override
        public boolean hasExtensions() throws IOException {
            return ConceptComponent.this.hasExtensions();
        }

        @Override
        public boolean hasMutableIdPart(I_IdPart newPart) {
            return ConceptComponent.this.hasMutableIdPart(newPart);
        }

        @Override
        public boolean isActive(NidSetBI allowedStatusNids) throws IOException {
            return cv.isActive(allowedStatusNids);
        }

        @Override
        public boolean isActive(ViewCoordinate vc) throws IOException {
            return isActive(vc.getAllowedStatusNids());
        }

        @Override
        public boolean isBaselineGeneration() {
            return cv.isBaselineGeneration();
        }

        @Override
        public boolean isUncommitted() {
            return getTime() == Long.MAX_VALUE;
        }

        //~--- set methods ------------------------------------------------------
        @Override
        public void setAuthorNid(int authorNid) throws PropertyVetoException {
            ((AnalogBI) cv).setAuthorNid(authorNid);
        }

        public final void setNid(int nid) throws PropertyVetoException {
            ((AnalogBI) cv).setNid(nid);
        }

        @Override
        @Deprecated
        public void setPathId(int pathId) throws PropertyVetoException {
            ((AnalogBI) cv).setPathNid(pathId);
        }

        @Override
        public void setPathNid(int pathId) throws PropertyVetoException {
            ((AnalogBI) cv).setPathNid(pathId);
        }

        @Override
        @Deprecated
        public void setStatusId(int statusNid) throws PropertyVetoException {
            setStatusNid(statusNid);
        }

        @Override
        public void setStatusNid(int statusNid) throws PropertyVetoException {
            ((AnalogBI) cv).setStatusNid(statusNid);
        }

        @Override
        public void setTime(long time) throws PropertyVetoException {
            ((AnalogBI) cv).setTime(time);
        }
    }
}
