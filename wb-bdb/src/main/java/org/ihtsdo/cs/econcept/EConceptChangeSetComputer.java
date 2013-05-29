package org.ihtsdo.cs.econcept;

//~--- non-JDK imports --------------------------------------------------------
import org.dwfa.ace.log.AceLog;

import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
import org.ihtsdo.concept.component.ConceptComponent.IDENTIFIER_PART_TYPES;
import org.ihtsdo.concept.component.attributes.ConceptAttributes;
import org.ihtsdo.concept.component.description.Description;
import org.ihtsdo.concept.component.identifier.IdentifierVersion;
import org.ihtsdo.concept.component.identifier.IdentifierVersionLong;
import org.ihtsdo.concept.component.identifier.IdentifierVersionString;
import org.ihtsdo.concept.component.identifier.IdentifierVersionUuid;
import org.ihtsdo.concept.component.image.Image;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.relationship.Relationship;
import org.ihtsdo.cs.I_ComputeEConceptForChangeSet;
import org.ihtsdo.db.bdb.Bdb;
import org.ihtsdo.db.bdb.BdbCommitManager;
import org.ihtsdo.db.bdb.computer.ReferenceConcepts;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionRevision;
import org.ihtsdo.etypes.EIdentifierLong;
import org.ihtsdo.etypes.EIdentifierString;
import org.ihtsdo.etypes.EIdentifierUuid;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.EImageRevision;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision;
import org.ihtsdo.tk.api.NidSetBI;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.dto.concept.component.TkComponent;
import org.ihtsdo.tk.dto.concept.component.TkRevision;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributes;
import org.ihtsdo.tk.dto.concept.component.attribute.TkConceptAttributesRevision;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.identifier.TkIdentifier;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refex.TkRefexAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import org.ihtsdo.cs.ChangeSetWriterHandler;
import org.ihtsdo.db.change.ChangeNotifier;

public class EConceptChangeSetComputer implements I_ComputeEConceptForChangeSet {

    private int minSapNid = Integer.MIN_VALUE;
    private static final int maxSapNid = Integer.MAX_VALUE;
    private int classifier = ReferenceConcepts.SNOROCKET.getNid();
    private ChangeSetGenerationPolicy policy;

    //~--- constructors --------------------------------------------------------
    public EConceptChangeSetComputer(ChangeSetGenerationPolicy policy, NidSetBI commitSapNids) {
        super();
        this.policy = policy;

        switch (policy) {
            case COMPREHENSIVE:
                break;

            case INCREMENTAL:
                minSapNid = commitSapNids.getMin();
                break;

            case MUTABLE_ONLY:
                minSapNid = Bdb.getSapDb().getReadOnlyMax() + 1;
                break;

            default:
                throw new RuntimeException("Can't handle policy: " + policy);
        }

        assert minSapNid <= maxSapNid : "Min not <= max; min: " + minSapNid + " max: " + maxSapNid;
    }

    //~--- methods -------------------------------------------------------------
    private TkConceptAttributes processConceptAttributes(Concept c, AtomicBoolean changed) throws IOException {
        TkConceptAttributes eca = null;

        for (ConceptAttributes.Version v : c.getConAttrs().getTuples()) {
            if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                    && (v.getTime() != Long.MAX_VALUE)) {
                changed.set(true);

                ChangeNotifier.touch(c.getNid(), ChangeNotifier.Change.COMPONENT);
                if (eca == null) {
                    eca = new EConceptAttributes();
                    eca.setDefined(v.isDefined());
                    setupFirstVersion(eca, v);
                } else {
                    TkConceptAttributesRevision ecv = new TkConceptAttributesRevision();

                    ecv.setDefined(v.isDefined());
                    setupRevision(eca, v, ecv);
                }

            }
        }

        return eca;
    }

    private List<TkDescription> processDescriptions(Concept c, AtomicBoolean changed) throws IOException {
        List<TkDescription> eDescriptions = new ArrayList<TkDescription>(c.getDescs().size());

        for (Description d : c.getDescs()) {
            EDescription ecd = null;

            for (Description.Version v : d.getTuples()) {
                if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                        && (v.getTime() != Long.MAX_VALUE)) {
                    changed.set(true);


                    ChangeNotifier.touch(c.getNid(), ChangeNotifier.Change.COMPONENT);
                    if (ecd == null) {
                        ecd = new EDescription();
                        eDescriptions.add(ecd);
                        ecd.setConceptUuid(Bdb.getPrimUuidForConcept(v.getConceptNid()));
                        ecd.setInitialCaseSignificant(v.isInitialCaseSignificant());
                        ecd.setLang(v.getLang());
                        ecd.setText(v.getText());
                        ecd.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeNid()));
                        setupFirstVersion(ecd, v);
                    } else {
                        EDescriptionRevision ecv = new EDescriptionRevision();

                        ecv.setInitialCaseSignificant(v.isInitialCaseSignificant());
                        ecv.setLang(v.getLang());
                        ecv.setText(v.getText());
                        ecv.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeNid()));
                        setupRevision(ecd, v, ecv);
                    }

                }
            }
        }

        return eDescriptions;
    }

    private List<TkMedia> processMedia(Concept c, AtomicBoolean changed) throws IOException {
        List<TkMedia> eImages = new ArrayList<TkMedia>();

        for (Image img : c.getImages()) {
            EImage eImg = null;

            for (Image.Version v : img.getTuples()) {
                if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                        && (v.getTime() != Long.MAX_VALUE)) {

                    changed.set(true);
                    ChangeNotifier.touch(v.getNid(), ChangeNotifier.Change.COMPONENT);

                    if (eImg == null) {
                        eImg = new EImage();
                        eImages.add(eImg);
                        eImg.setConceptUuid(Bdb.getPrimUuidForConcept(v.getConceptNid()));
                        eImg.setFormat(v.getFormat());
                        eImg.setDataBytes(v.getImage());
                        eImg.setTextDescription(v.getTextDescription());
                        eImg.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeNid()));
                        setupFirstVersion(eImg, v);
                    } else {
                        EImageRevision eImgR = new EImageRevision();

                        eImgR.setTextDescription(v.getTextDescription());
                        eImgR.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeNid()));
                        setupRevision(eImg, v, eImgR);
                    }

                }
            }
        }

        return eImages;
    }

    private List<TkRefexAbstractMember<?>> processRefsetMembers(Concept c, AtomicBoolean changed)
            throws IOException {
        List<TkRefexAbstractMember<?>> eRefsetMembers =
                new ArrayList<TkRefexAbstractMember<?>>(c.getRefsetMembers().size());
        Collection<RefsetMember<?, ?>> membersToRemove = new ArrayList<RefsetMember<?, ?>>();

        for (RefsetMember<?, ?> member : c.getRefsetMembers()) {
            TkRefexAbstractMember<?> eMember = null;
            Concept concept = Bdb.getConceptForComponent(member.getReferencedComponentNid());

            if ((concept != null) && !concept.isCanceled()) {
                for (RefsetMember<?, ?>.Version v : member.getTuples()) {
                    if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                            && (v.getTime() != Long.MAX_VALUE)) {

                        changed.set(true);
                        ChangeNotifier.touch(v.getNid(), ChangeNotifier.Change.COMPONENT);
                        ChangeNotifier.touch(v.getReferencedComponentNid(), ChangeNotifier.Change.REFEX_XREF);

                        if (eMember == null) {
                            try {
                                eMember = v.getERefsetMember();

                                if (eMember != null) {
                                    eRefsetMembers.add(eMember);
                                    setupFirstVersion(eMember, v);
                                }
                            } catch (Exception e) {
                                AceLog.getAppLog().info("Failed in getting Concept for: " + member.toString() + " and " + v.toString());
                            }
                        } else {
                            TkRevision eRevision = v.getERefsetRevision();

                            setupRevision(eMember, v, eRevision);
                        }

                    }
                }
            } else {
                member.primordialSapNid = -1;
                membersToRemove.add(member);
            }
        }

        if (!membersToRemove.isEmpty()) {
            BdbCommitManager.writeImmediate(c);
        }

        return eRefsetMembers;
    }

    private List<TkRelationship> processRelationships(Concept c, AtomicBoolean changed) throws IOException {
        List<TkRelationship> rels = new ArrayList<TkRelationship>(c.getSourceRels().size());

        for (Relationship r : c.getSourceRels()) {
            TkRelationship ecr = null;

            for (Relationship.Version v : r.getTuples()) {
                if (v.stampIsInRange(minSapNid, maxSapNid) && (v.getTime() != Long.MIN_VALUE)
                        && (v.getTime() != Long.MAX_VALUE) && (v.getAuthorNid() != classifier)) {

                    try {
                        changed.set(true);
                        ChangeNotifier.touch(v.getNid(), ChangeNotifier.Change.COMPONENT);
                        ChangeNotifier.touch(v.getSourceNid(), ChangeNotifier.Change.REL_ORIGIN);
                        ChangeNotifier.touch(v.getTargetNid(), ChangeNotifier.Change.REL_XREF);

                        if (ecr == null) {
                            ecr = new ERelationship();
                            rels.add(ecr);
                            ecr.setC1Uuid(Bdb.getPrimUuidForConcept(v.getC1Id()));
                            ecr.setC2Uuid(Bdb.getPrimUuidForConcept(v.getC2Id()));
                            ecr.setCharacteristicUuid(Bdb.getPrimUuidForConcept(v.getCharacteristicId()));
                            ecr.setRefinabilityUuid(Bdb.getPrimUuidForConcept(v.getRefinabilityNid()));
                            ecr.setRelGroup(v.getGroup());
                            ecr.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeNid()));
                            setupFirstVersion(ecr, v);
                        } else {
                            ERelationshipRevision ecv = new ERelationshipRevision();

                            ecv.setCharacteristicUuid(Bdb.getPrimUuidForConcept(v.getCharacteristicId()));
                            ecv.setRefinabilityUuid(Bdb.getPrimUuidForConcept(v.getRefinabilityNid()));
                            ecv.setRelGroup(v.getGroup());
                            ecv.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeNid()));
                            setupRevision(ecr, v, ecv);
                        }
                    } catch (AssertionError e) {
                        AceLog.getAppLog().alertAndLogException(new Exception(e.getLocalizedMessage() + "\n\n"
                                + c.toLongString(), e));

                        throw e;
                    }
                }

            }
        }

        return rels;
    }

    @SuppressWarnings("unchecked")
    private void setupFirstVersion(TkComponent ec, ConceptComponent<?, ?>.Version v) throws IOException {
        ec.primordialUuid = v.getPrimUuid();
        ec.setPathUuid(Bdb.getPrimUuidForConcept(v.getPathNid()));
        ec.setStatusUuid(Bdb.getPrimUuidForConcept(v.getStatusNid()));
        ec.setAuthorUuid(Bdb.getPrimUuidForConcept(v.getAuthorNid()));
        ec.setModuleUuid(Bdb.getPrimUuidForConcept(v.getModuleNid()));
        ec.setTime(v.getTime());

        if (v.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : v.getAdditionalIdentifierParts()) {
                TkIdentifier eIdv = null;

                if ((idv.getStampNid() >= minSapNid) && (idv.getStampNid() <= maxSapNid)
                        && (v.getTime() != Long.MIN_VALUE) && (v.getTime() != Long.MAX_VALUE)) {
                    if (IdentifierVersionLong.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new EIdentifierLong();
                    } else if (IdentifierVersionString.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new EIdentifierString();
                    } else if (IdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new EIdentifierUuid();
                    }

                    eIdv.setDenotation(idv.getDenotation());
                    eIdv.setAuthorityUuid(Bdb.getPrimUuidForConcept(idv.getAuthorityNid()));
                    eIdv.setPathUuid(Bdb.getPrimUuidForConcept(idv.getPathNid()));
                    eIdv.setStatusUuid(Bdb.getPrimUuidForConcept(idv.getStatusNid()));
                    eIdv.setAuthorUuid(Bdb.getPrimUuidForConcept(idv.getAuthorNid()));
                    eIdv.setModuleUuid(Bdb.getPrimUuidForConcept(v.getModuleNid()));
                    eIdv.setTime(idv.getTime());
                }
            }
        }

        if (v.getAnnotations() != null) {
            HashMap<UUID, TkRefexAbstractMember<?>> annotationMap = new HashMap<UUID, TkRefexAbstractMember<?>>();

            if (ec.getAnnotations() != null) {
                for (TkRefexAbstractMember<?> member :
                        (Collection<TkRefexAbstractMember<?>>) ec.getAnnotations()) {
                    annotationMap.put(member.getPrimordialComponentUuid(), member);
                }
            }

            for (RefsetMember<?, ?> member : (Collection<RefsetMember<?, ?>>) v.getAnnotations()) {
                TkRefexAbstractMember<?> eMember = null;
                Concept concept =
                        Bdb.getConceptForComponent(member.getReferencedComponentNid());

                if ((concept != null) && !concept.isCanceled()) {
                    for (RefsetMember<?, ?>.Version mv : member.getVersions()) {
                        if (mv.stampIsInRange(minSapNid, maxSapNid) && (mv.getTime() != Long.MIN_VALUE)
                                && (mv.getTime() != Long.MAX_VALUE)) {

                            if (eMember == null) {
                                eMember = mv.getERefsetMember();

                                if (eMember != null) {
                                    if (ec.getAnnotations() == null) {
                                        ec.setAnnotations(new ArrayList());
                                    }

                                    ec.getAnnotations().add(eMember);
                                    setupFirstVersion(eMember, mv);
                                }
                            } else {
                                TkRevision eRevision = mv.getERefsetRevision();

                                setupRevision(eMember, mv, eRevision);
                            }
                        }

                    }
                }
            }
        }

        if (v.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : v.getAdditionalIdentifierParts()) {
                if (idv.sapIsInRange(minSapNid, maxSapNid)) {
                    if (ec.getAdditionalIdComponents() == null) {
                        ec.setAdditionalIdComponents(new ArrayList<TkIdentifier>());
                    }

                    Object denotation = idv.getDenotation();

                    switch (IDENTIFIER_PART_TYPES.getType(denotation.getClass())) {
                        case LONG:
                            ec.additionalIds.add(new EIdentifierLong(idv));

                            break;

                        case STRING:
                            ec.additionalIds.add(new EIdentifierString(idv));

                            break;

                        case UUID:
                            ec.additionalIds.add(new EIdentifierUuid(idv));

                            break;

                        default:
                            throw new UnsupportedOperationException();
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void setupRevision(TkComponent ec, ConceptComponent.Version v, TkRevision ev) throws IOException {
        if (ec.revisions == null) {
            ec.revisions = new ArrayList();
        }

        ev.setPathUuid(Bdb.getPrimUuidForConcept(v.getPathNid()));
        ev.setStatusUuid(Bdb.getPrimUuidForConcept(v.getStatusNid()));
        ev.setAuthorUuid(Bdb.getPrimUuidForConcept(v.getAuthorNid()));
        ev.setModuleUuid(Bdb.getPrimUuidForConcept(v.getModuleNid()));
        ev.setTime(v.getTime());
        ec.revisions.add(ev);
    }

    @Override
    public String toString() {
        return "EConceptChangeSetComputer: minSapNid: " + minSapNid + " maxSapNid: " + maxSapNid + " policy: "
                + policy;
    }

    //~--- get methods ---------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see
     * org.ihtsdo.cs.I_ComputeEConceptForChangeSet#getEConcept(org.ihtsdo.concept
     * .Concept)
     */
    @Override
    public EConcept getEConcept(final Concept c) throws IOException {
        EConcept ec = new EConcept();
        AtomicBoolean changed = new AtomicBoolean(false);
        ec.annotationIndexStyleRefex = c.isAnnotationIndex();
        ec.annotationStyleRefex = c.isAnnotationStyleRefex();
        ec.setPrimordialUuid(c.getPrimUuid());
        ec.setConceptAttributes(processConceptAttributes(c, changed));
        ec.setDescriptions(processDescriptions(c, changed));
        ec.setRelationships(processRelationships(c, changed));
        ec.setImages(processMedia(c, changed));

        if (!c.isAnnotationStyleRefex()) {
            ec.setRefsetMembers(processRefsetMembers(c, changed));
        }

        if (changed.get()) {
            if (ChangeSetWriterHandler.writeCommitRecord && ec.conceptAttributes == null) {
                ec.setConceptAttributes(new TkConceptAttributes(c.getConceptAttributes()));
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        AceLog.getEditLog().alertAndLogException(
                                new Exception("Missing commit record, added: " + 
                                c.toString()));
                    }
                });
            }
            return ec;
        }

        return null;
    }
}
