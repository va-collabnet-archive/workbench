package org.ihtsdo.tk.query.helper;

import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import org.dwfa.cement.RefsetAuxiliary;
import org.dwfa.tapi.AllowDataCheckSuppression;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

@AllowDataCheckSuppression
/**
 * The class RefsetHelper contains convenience methods for creating refset membership.
 */
public class RefsetHelper {

    protected Set<PathBI> editPaths;
    private Logger logger = Logger.getLogger(RefsetHelper.class.getName());
    private EditCoordinate ec;
    protected ViewCoordinate vc;
    protected TerminologyStoreDI ts;
    private TerminologyBuilderBI builder;

    public RefsetHelper(ViewCoordinate viewCoordinate, EditCoordinate editCoordinate) throws Exception {
        this.ec = editCoordinate;
        this.vc = viewCoordinate;
        this.ts = Ts.get();
        this.builder = ts.getTerminologyBuilder(editCoordinate, viewCoordinate);
    }

    public RefexChronicleBI newRefsetExtension(int refsetNid, int componentNid,
            int memberTypeNid) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, memberTypeNid);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    
    public RefexChronicleBI newConceptConceptRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int c2Nid)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }
    
    public RefexChronicleBI newConceptConceptRefsetExtension(UUID memberUuid, int refsetNid,
            int componentNid, int c1Nid, int c2Nid)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        memberBp.setMemberUuid(memberUuid);
        return builder.constructIfNotCurrent(memberBp);
    }

    
    public RefexChronicleBI newStringRefsetExtension(int refsetNid, int componentNid,
            String extString) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.STRING1, extString);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI newLongRefsetExtension(int refsetNid, int componentNid,
            long extLong) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.LONG,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.LONG1, extLong);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI newConceptStringRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, String extString)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_STR,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.STRING1, extString);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }
    
    public RefexChronicleBI newConceptIntRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int extInt)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_INT,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.INTEGER1, extInt);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI newIntRefsetExtension(int refsetNid, int componentNid,
            int int1) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.INT,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.INTEGER1, int1);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI newConceptRefsetExtension(int refsetNid, int componentNid,
            int c1Nid) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }
    
    public RefexChronicleBI newBooleanRefsetExtension(int refsetNid, int componentNid,
            boolean boolean1) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.BOOLEAN,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.BOOLEAN1, boolean1);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI newConceptConceptConceptRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int c2Nid, int c3Nid)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID_CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        memberBp.put(RefexProperty.CNID3, c3Nid);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI newConceptConceptStringRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int c2Nid, String string1)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID_STR,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        memberBp.put(RefexProperty.STRING1, string1);
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }

    public RefexChronicleBI retireRefsetExtension(int refsetNid, int componentNid, int memberTypeNid) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, memberTypeNid);
        memberBp.setRetired();
        memberBp.setMemberUuid(memberBp.computeMemberContentUuid());
        return builder.constructIfNotCurrent(memberBp);
    }
    
    public boolean hasPurpose(int refsetId, int purposeId) throws Exception {
            ConceptChronicleBI memberRefsetConcept = Ts.get().getConcept(refsetId);
            if (memberRefsetConcept == null) {
                return false;
            }
            for(ConceptVersionBI target : memberRefsetConcept.getVersion(vc).getRelationshipsOutgoingTargetConceptsActive(
                    RefsetAuxiliary.Concept.REFSET_PURPOSE_REL.localize().getNid())){
                if(target.getNid() == purposeId){
                    return true;
                }
            }
            return false;
    }
}
