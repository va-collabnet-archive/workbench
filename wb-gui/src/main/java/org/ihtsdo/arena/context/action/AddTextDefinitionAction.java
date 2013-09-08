package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.helper.dialect.UnsupportedDialectOrLanguage;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refex.TK_REFEX_TYPE;

public class AddTextDefinitionAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    I_ConfigAceFrame config;
    DescriptionVersionBI desc;

    public AddTextDefinitionAction(String actionName,
            DescFact desc, I_ConfigAceFrame config) {
        super(actionName);
        this.config = config;
        this.desc = desc.getComponent();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            DescriptionCAB descBp = new DescriptionCAB(desc.getConceptNid(), 
                    SnomedMetadataRf2.DEFINITION_RF2.getLenient().getConceptNid(),
                    LANG_CODE.getLangCode(desc.getLang()),
                    "Clone of " + desc.getText(),
                    true);
            descBp.setComponentUuidNoRecompute(UUID.randomUUID());
            DescriptionChronicleBI dc = builder.construct(descBp);
            RefexCAB refexBp = null;
            for(RefexVersionBI refex : desc.getAnnotationsActive(config.getViewCoordinate())){
                if(refex.getRefexNid() == SnomedMetadataRfx.getGB_DIALECT_REFEX_NID()){
                    refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                    dc.getNid(), 
                    SnomedMetadataRfx.getGB_DIALECT_REFEX_NID());
                    refexBp.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
                    RefexChronicleBI<?> newRefex = builder.construct(refexBp);
                }else if(refex.getRefexNid() == SnomedMetadataRfx.getUS_DIALECT_REFEX_NID()){
                    refexBp = new RefexCAB(TK_REFEX_TYPE.CID,
                    dc.getNid(), 
                    SnomedMetadataRfx.getUS_DIALECT_REFEX_NID());
                    refexBp.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
                    RefexChronicleBI<?> newRefex = builder.construct(refexBp);
                }
            }
            if(refexBp == null){
                throw new UnsupportedDialectOrLanguage("Language refset not supported. Currently supporting EN-US and EN-GB ");
            }
            
            ConceptVersionBI cv = Ts.get().getConceptVersion(config.getViewCoordinate(), desc.getConceptNid());
            Ts.get().addUncommitted(cv);

        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (InvalidCAB e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (ContradictionException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (UnsupportedDialectOrLanguage e1){
            AceLog.getAppLog().alertAndLogException(e1);
        }

    }
}
