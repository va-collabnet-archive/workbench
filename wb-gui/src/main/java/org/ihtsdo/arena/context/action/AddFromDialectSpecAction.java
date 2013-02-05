package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.UUID;
import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.IdDirective;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.binding.snomed.Language;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.DescriptionSpec;

public class AddFromDialectSpecAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ConceptVersionBI concept;
    SpecFact<?> spec;
    LANG_CODE dialect;
    DescriptionSpec descSpec;
    I_ConfigAceFrame config;
    UUID dialectUuid;
    DescriptionChronicleBI newDesc;
    RefexChronicleBI<?> newRefex;

    public AddFromDialectSpecAction(String actionName,
            ConceptFact concept, SpecFact<?> spec, LANG_CODE dialect, I_ConfigAceFrame config) throws IOException {
        super(actionName);
        this.concept = concept.getConcept();
        this.spec = spec;
        this.dialect = dialect;
        this.config = config;

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

        try {
            int type = Ts.get().getNidForUuids(descSpec.getDescTypeSpec().getLenient().getPrimUuid());
            int syn = SnomedMetadataRfx.getDES_SYNONYM_NID();
            int fsn = SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID();
            int usRefexNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
            int gbRefexNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
            int refexNid = 0;
            if (dialect.equals(LANG_CODE.EN_GB)) {
                dialectUuid = Language.EN_UK.getLenient().getPrimUuid();
                refexNid = gbRefexNid;
            } else if (dialect.equals(LANG_CODE.EN_US)) {
                dialectUuid = Language.EN_US.getLenient().getPrimUuid();
                refexNid = usRefexNid;
            }
            
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());

            if (type == syn) {
                DescCAB descSpecPref = new DescCAB(
                        concept.getNid(),
                        SnomedMetadataRfx.getDES_SYNONYM_NID(),
                        LANG_CODE.EN,
                        descSpec.getDescText(),
                        false, IdDirective.GENERATE_HASH);
                newDesc = tc.constructIfNotCurrent(descSpecPref);
                if (dialect.equals(LANG_CODE.EN_GB)) {
                    RefexCAB refexSpecPrefGb = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        descSpecPref.getComponentNid(),
                        gbRefexNid, IdDirective.GENERATE_HASH);
                    refexSpecPrefGb.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
                    newRefex = tc.constructIfNotCurrent(refexSpecPrefGb);
                } else {
                    RefexCAB refexSpecPrefUs = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        descSpecPref.getComponentNid(),
                        usRefexNid, IdDirective.GENERATE_HASH);
                    refexSpecPrefUs.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_ACCEPTABLE_NID());
                    newRefex = tc.constructIfNotCurrent(refexSpecPrefUs);
                }
            } else if (type == fsn) {
                DescCAB descSpecFsn = new DescCAB(
                        concept.getNid(),
                        SnomedMetadataRfx.getDES_FULL_SPECIFIED_NAME_NID(),
                        dialect,
                        descSpec.getDescText(),
                        false, IdDirective.GENERATE_HASH);
                RefexCAB refexSpecFsn = new RefexCAB(
                        TK_REFSET_TYPE.CID,
                        descSpecFsn.getComponentNid(),
                        refexNid, IdDirective.GENERATE_HASH);
                refexSpecFsn.put(RefexProperty.CNID1, SnomedMetadataRfx.getDESC_PREFERRED_NID());
                newDesc = tc.constructIfNotCurrent(descSpecFsn);
                newRefex = tc.constructIfNotCurrent(refexSpecFsn);
            }
            newDesc.addAnnotation(newRefex);
            ConceptChronicleBI concept = Ts.get().getConceptForNid(newDesc.getConceptNid());
            Ts.get().addUncommitted(concept);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (ContradictionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
    }
}
