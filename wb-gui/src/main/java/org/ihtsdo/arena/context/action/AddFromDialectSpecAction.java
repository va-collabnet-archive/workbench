package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.example.binding.WbDescType;

public class AddFromDialectSpecAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptVersionBI concept;
    SpecFact<?> spec;
    String dialect;
    DescriptionSpec descSpec;
    I_ConfigAceFrame config;
    UUID dialectUuid;
    DescriptionChronicleBI newDesc;
    RefexChronicleBI<?> newRefex;

    public AddFromDialectSpecAction(String actionName,
            ConceptFact concept, SpecFact<?> spec, String dialect) throws IOException {
        super(actionName);
        this.concept = concept.getConcept();
        this.spec = spec;
        this.dialect = dialect;

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (DescSpecFact.class.isAssignableFrom(spec.getClass())) {
                addDesc();
            } else {
                throw new Exception("Can't handle type: " + spec);
            }
        } catch (Exception ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }

    private void addDesc() throws TerminologyException, IOException {
        descSpec = ((DescSpecFact) spec).getDescSpec();
        config = Terms.get().getActiveAceFrameConfig();

        try {
            int type = Ts.get().getNidForUuids(descSpec.getDescTypeSpec().get(concept.getViewCoordinate()).getPrimUuid());
            int syn = SnomedMetadataRfx.getDES_SYNONYM_NID();
            int fsn = SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID();
            int usRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
            int gbRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
            int refexNid = 0;
            if (dialect.equals("en-gb")) {
                dialectUuid = Language.EN_UK.getLenient().getPrimUuid();
                refexNid = gbRefexNid;
            } else if (dialect.equals("en-us")) {
                dialectUuid = Language.EN_US.getLenient().getPrimUuid();
                refexNid = usRefexNid;
            }
            
            TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
                    config.getViewCoordinate());

            if (type == syn) {
                DescCAB descSpecPref = new DescCAB(
                        concept.getNid(),
                        SnomedMetadataRfx.getDES_SYNONYM_NID(),
                        dialect,
                        descSpec.getDescText(),
                        false);
                newDesc = tc.constructIfNotCurrent(descSpecPref);
                if (dialect.equals("en-gb")) {
                    RefexCAB refexSpecPrefGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        descSpecPref.getComponentNid(),
                        gbRefexNid);
                    refexSpecPrefGb.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
                    newRefex = tc.constructIfNotCurrent(refexSpecPrefGb);
                } else {
                    RefexCAB refexSpecPrefUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        descSpecPref.getComponentNid(),
                        usRefexNid);
                    refexSpecPrefUs.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
                    newRefex = tc.constructIfNotCurrent(refexSpecPrefUs);
                }
            } else if (type == fsn) {
                DescCAB descSpecFsn = new DescCAB(
                        concept.getNid(),
                        SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID(),
                        dialect,
                        descSpec.getDescText(),
                        false);
                RefexCAB refexSpecFsn = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        descSpecFsn.getComponentNid(),
                        refexNid);
                refexSpecFsn.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
                newDesc = tc.constructIfNotCurrent(descSpecFsn);
                newRefex = tc.constructIfNotCurrent(refexSpecFsn);
            }
            I_GetConceptData desc = Terms.get().getConceptForNid(newDesc.getConceptNid());
            I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getConceptNid());
            Ts.get().addUncommitted(desc);
            Ts.get().addUncommitted(refex);
        } catch (IOException ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        } catch (InvalidCAB ex) {
            // TODO Auto-generated catch block
            ex.printStackTrace();
        }
    }
}
