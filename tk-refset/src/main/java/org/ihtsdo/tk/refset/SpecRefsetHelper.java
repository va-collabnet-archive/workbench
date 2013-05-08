package org.ihtsdo.tk.refset;

import java.util.Set;
import java.util.logging.Logger;

import org.dwfa.tapi.AllowDataCheckSuppression;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.TerminologyStoreDI;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

@AllowDataCheckSuppression
public class SpecRefsetHelper {

    protected Set<PathBI> editPaths;
    private Logger logger = Logger.getLogger(SpecRefsetHelper.class.getName());
    private EditCoordinate ec;
    protected ViewCoordinate vc;
    protected TerminologyStoreDI ts;
    private TerminologyBuilderBI builder;

    public SpecRefsetHelper(ViewCoordinate viewCoordinate, EditCoordinate editCoordinate) throws Exception {
        this.ec = editCoordinate;
        this.vc = viewCoordinate;
        this.ts = Ts.get();
        this.builder = ts.getTerminologyBuilder(editCoordinate, viewCoordinate);
    }

    public boolean newRefsetExtension(int refsetNid, int componentNid,
            int memberTypeNid) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, memberTypeNid);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    
    public boolean newConceptConceptRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int c2Nid)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    
    public boolean newStringRefsetExtension(int refsetNid, int componentNid,
            String extString) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.STR,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.STRING1, extString);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean newLongRefsetExtension(int refsetNid, int componentNid,
            long extLong) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.LONG,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.LONG1, extLong);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean newConceptStringRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, String extString)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_STR,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.STRING1, extString);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean newIntRefsetExtension(int refsetNid, int componentNid,
            int int1) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.INT,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.INTEGER1, int1);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean newConceptRefsetExtension(int refsetNid, int componentNid,
            int c1Nid) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean newConceptConceptConceptRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int c2Nid, int c3Nid)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID_CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        memberBp.put(RefexProperty.CNID3, c3Nid);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean newConceptConceptStringRefsetExtension(int refsetNid,
            int componentNid, int c1Nid, int c2Nid, String string1)
            throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID_CID_STR,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, c1Nid);
        memberBp.put(RefexProperty.CNID2, c2Nid);
        memberBp.put(RefexProperty.STRING1, string1);
        builder.constructIfNotCurrent(memberBp);
        return true;
    }

    public boolean retireRefsetExtension(int refsetNid, int componentNid, int memberTypeNid) throws Exception {
        RefexCAB memberBp = new RefexCAB(TK_REFEX_TYPE.CID,
                componentNid,
                refsetNid);
        memberBp.put(RefexProperty.CNID1, memberTypeNid);
        memberBp.setRetired();
        builder.constructIfNotCurrent(memberBp);
        return true;
    }
}
