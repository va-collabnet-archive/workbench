package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.SynonymyType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.binding.snomed.TermAux;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class ToggleReviewStatusAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;
    I_ConfigAceFrame config;

    public ToggleReviewStatusAction(String actionName, DescFact fact, I_ConfigAceFrame config) {
        super(actionName);
        this.desc = fact.getComponent();
        this.config = config;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            I_AmPart componentVersion;
            config = Terms.get().getActiveAceFrameConfig();
            TerminologyBuilderBI tc = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            ViewCoordinate vc = config.getViewCoordinate();
            Collection<? extends RefexVersionBI<?>> annotations =
                    desc.getCurrentAnnotationMembers(vc, RefsetAux.SNOMED_REV_STATUS.getStrict(vc).getConceptNid());
            for(RefexVersionBI annot : annotations){
                if(RefexCnidVersionBI.class.isAssignableFrom(annot.getClass())){
                    RefexCnidVersionBI cAnnot = (RefexCnidVersionBI) annot;
                    int reviewStatusNid = cAnnot.getCnid1();
                    RefexCAB annotBp = null;
                    if(reviewStatusNid == TermAux.UNREVIEWED.getStrict(vc).getConceptNid()){
                        annotBp = cAnnot.makeBlueprint(vc);
                        annotBp.put(RefexProperty.CNID1,
                                TermAux.FINAL.getStrict(vc).getConceptNid());
                    }else{
                        annotBp = cAnnot.makeBlueprint(vc);
                        annotBp.put(RefexProperty.CNID1,
                                TermAux.UNREVIEWED.getStrict(vc).getConceptNid());
                    }
                    TerminologyBuilderBI builder = Ts.get().getTerminologyBuilder(config.getEditCoordinate(), vc);
                    builder.construct(annotBp);
                    Ts.get().addUncommitted(Ts.get().getConcept(desc.getConceptNid()));
                }
            }


        } catch (TerminologyException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (ContradictionException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }


    }
}
