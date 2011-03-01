package org.ihtsdo.db.bdb;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ihtsdo.concept.component.refset.RefsetMember;
import org.ihtsdo.concept.component.refset.RefsetMemberFactory;
import org.ihtsdo.concept.component.refset.RefsetRevision;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;

import org.ihtsdo.tk.api.amend.InvalidAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec;
import org.ihtsdo.tk.api.amend.RefexAmendmentSpec.RefexProperty;
import org.ihtsdo.tk.api.amend.TerminologyAmendmentBI;
import org.ihtsdo.tk.api.coordinate.EditCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;

public class BdbAmender implements TerminologyAmendmentBI {

    EditCoordinate ec;
    ViewCoordinate vc;

    public BdbAmender(EditCoordinate ec, ViewCoordinate vc) {
        this.ec = ec;
        this.vc = vc;
    }

    @Override
    public RefexChronicleBI<?> amend(RefexAmendmentSpec res)
            throws IOException, InvalidAmendmentSpec {
        RefsetMember<?,?> refex = getRefex(res);
        if (refex != null) {
            return updateRefex(refex, res);
        } 
        return createRefex(res);
    }

    private RefexChronicleBI<?> updateRefex(RefsetMember<?, ?> refex, 
            RefexAmendmentSpec res) throws InvalidAmendmentSpec {
        for (int pathNid : ec.getEditPaths()) {
            RefsetRevision refexRevision = 
                    refex.makeAnalog(res.getInt(RefexProperty.STATUS_NID), 
                    ec.getAuthorNid(), pathNid, Long.MAX_VALUE);
            try {
                res.setPropertiesExceptSap(refexRevision);
            } catch (PropertyVetoException ex) {
                throw new InvalidAmendmentSpec("Refex: " + refex + 
                        "\n\nRefexAmendmentSpec: " + res, ex);
            }
        }
        return refex;
    }

    private RefsetMember<?,?> getRefex(RefexAmendmentSpec res)
            throws InvalidAmendmentSpec, IOException {
        if (Ts.get().hasUuid(res.getMemberUUID())) {
            ComponentChroncileBI<?> component = 
                    Ts.get().getComponent(res.getMemberUUID());
            if (res.getMemberType() == 
                    TK_REFSET_TYPE.classToType(component.getClass())) {
                return (RefsetMember<?,?>) component;
            } else {
                throw new InvalidAmendmentSpec(
                        "Component exists of different type: " + 
                        component + "\n\nRefexAmendmentSpec: " + res);
            }
        }
        return null;
    }

    @Override
    public RefexChronicleBI<?> amendIfNotCurrent(RefexAmendmentSpec res)
            throws IOException, InvalidAmendmentSpec {
        RefsetMember<?,?> refex = getRefex(res);
        if (refex != null) {
            boolean current = false;
            for (RefexVersionBI refexv: refex.getVersions(vc)) {
                if (res.validate(refexv)) {
                    current = true;
                    break;
                }
            }
            if (current) {
                return refex;
            }
            return updateRefex(refex, res);
        }

        return createRefex(res);
    }

    private RefexChronicleBI<?> createRefex(RefexAmendmentSpec res)
            throws IOException, InvalidAmendmentSpec {
        return RefsetMemberFactory.create(res, ec);
    }
}
