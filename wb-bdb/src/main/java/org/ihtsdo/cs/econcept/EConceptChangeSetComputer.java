package org.ihtsdo.cs.econcept;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.dwfa.ace.api.cs.ChangeSetPolicy;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.types.IntSet;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.component.ConceptComponent;
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
import org.ihtsdo.db.util.NidPair;
import org.ihtsdo.etypes.EComponent;
import org.ihtsdo.etypes.EConcept;
import org.ihtsdo.etypes.EConceptAttributes;
import org.ihtsdo.etypes.EConceptAttributesRevision;
import org.ihtsdo.etypes.EDescription;
import org.ihtsdo.etypes.EDescriptionRevision;
import org.ihtsdo.etypes.EIdentifier;
import org.ihtsdo.etypes.EIdentifierLong;
import org.ihtsdo.etypes.EIdentifierString;
import org.ihtsdo.etypes.EIdentifierUuid;
import org.ihtsdo.etypes.EImage;
import org.ihtsdo.etypes.EImageRevision;
import org.ihtsdo.etypes.ERefsetMember;
import org.ihtsdo.etypes.ERelationship;
import org.ihtsdo.etypes.ERelationshipRevision;
import org.ihtsdo.etypes.ERevision;

public class EConceptChangeSetComputer implements I_ComputeEConceptForChangeSet {
    private int minSapNid = Integer.MIN_VALUE;
    private int maxSapNid = Integer.MAX_VALUE;
    private IntSet commitSapNids;
    private ChangeSetPolicy policy;
    private boolean processNidLists = true;

    public String toString() {
        return "EConceptChangeSetComputer: minSapNid: " + minSapNid + " maxSapNid: " + maxSapNid + " policy: " + policy;
    }

    public EConceptChangeSetComputer(ChangeSetPolicy policy, IntSet commitSapNids, boolean processNidLists) {
        super();
        this.policy = policy;
        this.processNidLists = processNidLists;
        switch (policy) {
        case COMPREHENSIVE:
            maxSapNid = commitSapNids.getMax();
            break;
        case INCREMENTAL:
            if (!commitSapNids.contiguous()) {
                this.commitSapNids = commitSapNids;
            }
            maxSapNid = commitSapNids.getMax();
            minSapNid = commitSapNids.getMin();
            break;
        case MUTABLE_ONLY:
            maxSapNid = Integer.MAX_VALUE;
            minSapNid = Bdb.getSapDb().getReadOnlyMax() + 1;
            break;
        default:
            throw new RuntimeException("Can't handle policy: " + policy);
        }
        assert minSapNid <= maxSapNid : "Min not <= max; min: " + minSapNid + " max: " + maxSapNid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ihtsdo.cs.I_ComputeEConceptForChangeSet#getEConcept(org.ihtsdo.concept
     * .Concept)
     */
    public EConcept getEConcept(Concept c) throws IOException {
        EConcept ec = new EConcept();
        AtomicBoolean changed = new AtomicBoolean(false);

        ec.setPrimordialUuid(c.getPrimUuid());
        ec.setConceptAttributes(processConceptAttributes(c, changed));
        ec.setDescriptions(processDescriptions(c, changed));
        ec.setRelationships(processRelationships(c, changed));
        ec.setImages(processImages(c, changed));
        ec.setRefsetMembers(processRefsetMembers(c, changed));

        if (processNidLists) {
            ec.setDestRelUuidTypeUuids(processNidNidList(c.getData().getDestRelNidTypeNidList(), changed));
            ec.setRefsetUuidMemberUuidForConcept(processNidNidList(c.getData().getRefsetNidMemberNidForConceptList(),
                changed));
            ec.setRefsetUuidMemberUuidForDescriptions(processNidNidList(c.getData()
                .getRefsetNidMemberNidForDescriptionsList(), changed));
            ec.setRefsetUuidMemberUuidForImages(processNidNidList(c.getData().getRefsetNidMemberNidForImagesList(), changed));
            ec.setRefsetUuidMemberUuidForRefsetMembers(processNidNidList(c.getData()
                .getRefsetNidMemberNidForRefsetMembersList(), changed));
            ec.setRefsetUuidMemberUuidForRels(processNidNidList(c.getData().getRefsetNidMemberNidForRelsList(), changed));
            return ec;
        }
        if (changed.get()) {
            return ec;
        }
        return null;
    }

    private List<ERefsetMember<?>> processRefsetMembers(Concept c, AtomicBoolean changed) throws IOException {
        List<ERefsetMember<?>> eRefsetMembers = new ArrayList<ERefsetMember<?>>(c.getRefsetMembers().size());
        for (RefsetMember<?, ?> member : c.getRefsetMembers()) {
            ERefsetMember<?> eMember = null;
            for (RefsetMember<?, ?>.Version v : member.getTuples()) {
                if (v.getSapNid() >= minSapNid && v.getSapNid() <= maxSapNid && v.getTime() != Long.MIN_VALUE) {
                    if (commitSapNids == null || commitSapNids.contains(v.getSapNid())) {
                        changed.set(true);
                        try {
                            if (eMember == null) {
                                eMember = v.getERefsetMember();
                                eRefsetMembers.add(eMember);
                                setupFirstVersion(eMember, v);
                            } else {
                                ERevision eRevision = v.getERefsetRevision();
                                setupRevision(eMember, v, eRevision);
                            }
                        } catch (TerminologyException e) {
                            throw new IOException(e);
                        }
                    }
                }
            }
        }
        return eRefsetMembers;
    }

    private List<UUID> processNidNidList(List<? extends NidPair> nidNidList, AtomicBoolean changed) throws IOException {
        List<UUID> uuidUuidList = new ArrayList<UUID>(nidNidList.size() * 2);
        for (NidPair pair : nidNidList) {
            int nid1 = pair.getNid1();
            int nid2 = pair.getNid2();
            Concept c1 = Bdb.getConceptForComponent(nid1);
            Concept c2 = Bdb.getConceptForComponent(nid2);
            if (c1 == null || c1.isCanceled() || c2 == null || c2.isCanceled()) {
                // nothing to do...
            } else {
                UUID uuid1 = Bdb.getPrimUuidForComponent(nid1);
                UUID uuid2 = Bdb.getPrimUuidForComponent(nid2);
                if (uuid1 != null && uuid2 != null) {
                    uuidUuidList.add(uuid1);
                    uuidUuidList.add(uuid2);
                } else {
                    if (uuid1 == null) {
                        AceLog.getAppLog().warning(
                            "---------------------------------------------" + "null primordial uuid for nid1: " + nid1
                                + " nid2: " + nid2 + " concept1: " + c1.toLongString() + " concept2: "
                                + c2.toLongString());
                    } else {
                        AceLog.getAppLog().warning(
                            "---------------------------------------------" + "null primordial uuid for nid2: " + nid2
                                + " nid1: " + nid1 + " concept1: " + c1.toLongString() + " concept2: "
                                + c2.toLongString());
                    }
                }
            }
        }
        return uuidUuidList;
    }

    private List<EImage> processImages(Concept c, AtomicBoolean changed) throws IOException {
        List<EImage> eImages = new ArrayList<EImage>();
        for (Image img : c.getImages()) {
            EImage eImg = null;
            for (Image.Version v : img.getTuples()) {
                if (v.getSapNid() >= minSapNid && v.getSapNid() <= maxSapNid && v.getTime() != Long.MIN_VALUE) {
                    if (commitSapNids == null || commitSapNids.contains(v.getSapNid())) {
                        changed.set(true);
                        if (eImg == null) {
                            eImg = new EImage();
                            eImages.add(eImg);
                            eImg.setConceptUuid(Bdb.getPrimUuidForConcept(v.getConceptId()));
                            eImg.setFormat(v.getFormat());
                            eImg.setImage(v.getImage());
                            eImg.setTextDescription(v.getTextDescription());
                            eImg.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeId()));
                            setupFirstVersion(eImg, v);
                        } else {
                            EImageRevision eImgR = new EImageRevision();
                            eImgR.setTextDescription(v.getTextDescription());
                            eImgR.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeId()));
                            setupRevision(eImg, v, eImgR);
                        }
                    }
                }
            }
        }
        return eImages;
    }

    private List<ERelationship> processRelationships(Concept c, AtomicBoolean changed) throws IOException {
        List<ERelationship> rels = new ArrayList<ERelationship>(c.getSourceRels().size());
        for (Relationship r : c.getSourceRels()) {
            ERelationship ecr = null;
            for (Relationship.Version v : r.getTuples()) {
                if (v.getSapNid() >= minSapNid && v.getSapNid() <= maxSapNid && v.getTime() != Long.MIN_VALUE) {
                    if (commitSapNids == null || commitSapNids.contains(v.getSapNid())) {
                        changed.set(true);
                        if (ecr == null) {
                            ecr = new ERelationship();
                            rels.add(ecr);
                            ecr.setC1Uuid(Bdb.getPrimUuidForConcept(v.getC1Id()));
                            ecr.setC2Uuid(Bdb.getPrimUuidForConcept(v.getC2Id()));
                            ecr.setCharacteristicUuid(Bdb.getPrimUuidForConcept(v.getCharacteristicId()));
                            ecr.setRefinabilityUuid(Bdb.getPrimUuidForConcept(v.getRefinabilityId()));
                            ecr.setRelGroup(v.getGroup());
                            ecr.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeId()));
                            setupFirstVersion(ecr, v);
                        } else {
                            ERelationshipRevision ecv = new ERelationshipRevision();
                            ecv.setCharacteristicUuid(Bdb.getPrimUuidForConcept(v.getCharacteristicId()));
                            ecv.setRefinabilityUuid(Bdb.getPrimUuidForConcept(v.getRefinabilityId()));
                            ecv.setRelGroup(v.getGroup());
                            ecv.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeId()));
                            setupRevision(ecr, v, ecv);
                        }
                    }
                }
            }
        }
        return rels;
    }

    private List<EDescription> processDescriptions(Concept c, AtomicBoolean changed) throws IOException {
        List<EDescription> eDescriptions = new ArrayList<EDescription>(c.getDescriptions().size());
        for (Description d : c.getDescriptions()) {
            EDescription ecd = null;
            for (Description.Version v : d.getTuples()) {
                if (v.getSapNid() >= minSapNid && v.getSapNid() <= maxSapNid && v.getTime() != Long.MIN_VALUE) {
                    changed.set(true);
                    if (commitSapNids == null || commitSapNids.contains(v.getSapNid())) {
                        if (ecd == null) {
                            ecd = new EDescription();
                            eDescriptions.add(ecd);
                            ecd.setConceptUuid(Bdb.getPrimUuidForConcept(v.getConceptId()));
                            ecd.setInitialCaseSignificant(v.isInitialCaseSignificant());
                            ecd.setLang(v.getLang());
                            ecd.setText(v.getText());
                            ecd.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeId()));
                            setupFirstVersion(ecd, v);
                        } else {
                            EDescriptionRevision ecv = new EDescriptionRevision();
                            ecv.setInitialCaseSignificant(v.isInitialCaseSignificant());
                            ecv.setLang(v.getLang());
                            ecv.setText(v.getText());
                            ecv.setTypeUuid(Bdb.getPrimUuidForConcept(v.getTypeId()));
                            setupRevision(ecd, v, ecv);
                        }
                    }
                }
            }
        }
        return eDescriptions;
    }

    private EConceptAttributes processConceptAttributes(Concept c, AtomicBoolean changed) throws IOException {
        EConceptAttributes eca = null;
        for (ConceptAttributes.Version v : c.getConceptAttributes().getTuples()) {
            if (v.getSapNid() >= minSapNid && v.getSapNid() <= maxSapNid && v.getTime() != Long.MIN_VALUE) {
                changed.set(true);
                if (commitSapNids == null || commitSapNids.contains(v.getSapNid())) {
                    if (eca == null) {
                        eca = new EConceptAttributes();
                        eca.setDefined(v.isDefined());
                        setupFirstVersion(eca, v);
                    } else {
                        EConceptAttributesRevision ecv = new EConceptAttributesRevision();
                        ecv.setDefined(v.isDefined());
                        setupRevision(eca, v, ecv);
                    }
                }
            }
        }
        return eca;
    }

    @SuppressWarnings("unchecked")
    private void setupRevision(EComponent ec, ConceptComponent.Version v, ERevision ev) throws IOException {
        if (ec.revisions == null) {
            ec.revisions = new ArrayList();
        }
        ev.setPathUuid(Bdb.getPrimUuidForConcept(v.getPathId()));
        ev.setStatusUuid(Bdb.getPrimUuidForConcept(v.getStatusId()));
        ev.setTime(v.getTime());
        ec.revisions.add(ev);
    }

    @SuppressWarnings("unchecked")
    private void setupFirstVersion(EComponent ec, ConceptComponent<?, ?>.Version v) throws IOException {
        ec.primordialUuid = v.getPrimUuid();
        ec.setPathUuid(Bdb.getPrimUuidForConcept(v.getPathId()));
        ec.setStatusUuid(Bdb.getPrimUuidForConcept(v.getStatusId()));
        ec.setTime(v.getTime());
        if (v.getAdditionalIdentifierParts() != null) {
            for (IdentifierVersion idv : v.getAdditionalIdentifierParts()) {
                EIdentifier eIdv = null;
                if (idv.getSapNid() >= minSapNid && idv.getSapNid() <= maxSapNid && v.getTime() != Long.MIN_VALUE) {
                    if (IdentifierVersionLong.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new EIdentifierLong();
                    } else if (IdentifierVersionString.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new EIdentifierString();
                    } else if (IdentifierVersionUuid.class.isAssignableFrom(idv.getClass())) {
                        eIdv = new EIdentifierUuid();
                    }
                    eIdv.setDenotation(idv.getDenotation());
                    eIdv.setAuthorityUuid(Bdb.getPrimUuidForConcept(idv.getAuthorityNid()));
                    eIdv.setPathUuid(Bdb.getPrimUuidForConcept(idv.getPathId()));
                    eIdv.setStatusUuid(Bdb.getPrimUuidForConcept(idv.getStatusId()));
                    eIdv.setTime(idv.getTime());
                }
            }
        }
    }
}
