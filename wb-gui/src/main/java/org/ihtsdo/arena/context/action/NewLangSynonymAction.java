package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.IdDirective;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class NewLangSynonymAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptSpec refex;
    I_ConfigAceFrame config;
    LANG_CODE lang;
    DescriptionVersionBI desc;

    public NewLangSynonymAction(String actionName, ConceptSpec refex, LANG_CODE lang,
            DescFact desc, I_ConfigAceFrame config) {
        super(actionName);
        this.refex = refex;
        this.config = config;
        this.lang = lang;
        this.desc = desc.getComponent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            int prefNid = SnomedMetadataRfx.getDESC_PREFERRED_NID();
            DescriptionCAB descBp = new DescriptionCAB(desc.getConceptNid(), 
                    SnomedMetadataRfx.getDES_SYNONYM_NID(),
                    lang,
                    "Clone of " + desc.getText(),
                    false, 
                    IdDirective.GENERATE_HASH);
            descBp.setComponentUuidNoRecompute(UUID.randomUUID());
            DescriptionChronicleBI dc = builder.construct(descBp);
            DescriptionVersionBI dv = dc.getVersion(config.getViewCoordinate());
            RefexCAB refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                    dv.getNid(), 
                    refex.getStrict(config.getViewCoordinate()).getConceptNid(),
                    IdDirective.GENERATE_HASH);
            refexBp.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
            RefexChronicleBI<?> newRefex = builder.construct(refexBp);
            ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(), desc.getConceptNid());
            Ts.get().addUncommitted(cv);

        } catch (IOException | InvalidCAB | ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
}
