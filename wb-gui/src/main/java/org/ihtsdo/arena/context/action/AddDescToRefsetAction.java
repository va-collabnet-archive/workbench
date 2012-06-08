package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class AddDescToRefsetAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptSpec refex;
    I_ConfigAceFrame config;
    DescriptionVersionBI desc;

    public AddDescToRefsetAction(String actionName, ConceptSpec refex,
            DescFact desc, I_ConfigAceFrame config) {
        super(actionName);
        this.refex = refex;
        this.config = config;
        this.desc = desc.getComponent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            int prefNid = SnomedMetadataRfx.getDESC_PREFERRED_NID();
            RefexCAB refexBp = new RefexCAB(TK_REFSET_TYPE.CID,
                    desc.getNid(), 
                    refex.getStrict(config.getViewCoordinate()).getConceptNid());
            refexBp.put(RefexProperty.CNID1, TermAux.UNREVIEWED.getLenient().getNid());
            RefexChronicleBI<?> newRefex = builder.construct(refexBp);
            ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(), desc.getConceptNid());
            cv.addAnnotation(newRefex);
            Ts.get().addUncommitted(cv);

        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
}
