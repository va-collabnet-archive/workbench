package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
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
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class AddDescriptionToRefexAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptSpec refex;
    I_ConfigAceFrame config;
    DescriptionVersionBI desc;
    ConceptSpec conceptValue;
    boolean randomUuid;

    public AddDescriptionToRefexAction(String actionName, ConceptSpec refex,
            ConceptSpec conceptValue,
            DescFact desc, I_ConfigAceFrame config,
            boolean randomUuid) {
        super(actionName);
        this.refex = refex;
        this.conceptValue = conceptValue;
        this.config = config;
        this.desc = desc.getComponent();
        this.randomUuid = randomUuid;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                    desc.getNid(), 
                    refex.getStrict(config.getViewCoordinate()).getConceptNid());
            refexBp.put(RefexProperty.CNID1, conceptValue.getLenient().getNid());
            if(randomUuid){
                refexBp.setMemberUuid(UUID.randomUUID());
            }
            RefexChronicleBI<?> newRefex = builder.construct(refexBp);
            ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(), desc.getConceptNid());
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
