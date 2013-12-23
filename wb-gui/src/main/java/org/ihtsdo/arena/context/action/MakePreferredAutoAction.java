/*
 * Copyright 2013 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.lang.LANG_CODE;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.TerminologyBuilderBI;
import org.ihtsdo.tk.api.blueprint.DescriptionCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.binding.snomed.RefsetAux;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRfx;
import org.ihtsdo.tk.drools.facts.DescFact;


public class MakePreferredAutoAction extends AbstractAction{
    private static final long serialVersionUID = 1L;
    private DescriptionVersionBI desc;
    private LANG_CODE dialect;
    private I_ConfigAceFrame config;
    private ViewCoordinate vc;
    private int preferredNid;
    private int acceptibleNid;
    private TerminologyBuilderBI tb;
    private boolean notHandled;

    public MakePreferredAutoAction(String actionName, DescFact fact, LANG_CODE dialect, I_ConfigAceFrame config) {
        super(actionName);
        this.desc = fact.getComponent();
        this.dialect = dialect;
        this.config = config;
        vc = config.getViewCoordinate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            notHandled = true;
            tb = Ts.get().getTerminologyBuilder(config.getEditCoordinate(),
                    config.getViewCoordinate());
            ConceptVersionBI concept = desc.getEnclosingConcept().getVersion(vc);
            Collection<? extends DescriptionVersionBI> prefDesc = concept.getDescriptionsPreferredActive();
            
            preferredNid = SnomedMetadataRfx.getDESC_PREFERRED_NID();
            acceptibleNid = SnomedMetadataRfx.getDESC_ACCEPTABLE_NID();
            
            int usRefsetNid = SnomedMetadataRfx.getUS_DIALECT_REFEX_NID();
            int gbRefsetNid = SnomedMetadataRfx.getGB_DIALECT_REFEX_NID();
            
            if(dialect.equals(LANG_CODE.EN)){
//                SET DESCRIPTION TO BE PREFERRED, CHANGE OLD PRFERRED TO ACCEPTIBLE
                for(DescriptionVersionBI d : prefDesc){
                    if(d.hasRefexMemberActive(vc, gbRefsetNid) && d.hasRefexMemberActive(vc, usRefsetNid)){
                        handleDescriptions(desc, d, gbRefsetNid);
                        handleDescriptions(desc, d, usRefsetNid);
                    }
                }
                
            }else if(dialect.equals(LANG_CODE.EN_US)){
//                SET DESCRIPTION TO BE PREFERRED, CHANGE OLD US PREFERRED TO ACCEPTIBLE
                for(DescriptionVersionBI d : prefDesc){
                    if(d.hasRefexMemberActive(vc, usRefsetNid) && !d.hasRefexMemberActive(vc, gbRefsetNid)){
                        handleDescriptions(desc, d, usRefsetNid);
                    }
                }
                if(notHandled){
                    handleDescriptions(desc, null, usRefsetNid);
                }
                
            }else if(dialect.equals(LANG_CODE.EN_GB)){
//                SET DESCRIPTION TO BE PREFERRED, CHANGE OLD GB PREFERRED TO ACCEPTIBLE
                for(DescriptionVersionBI d : prefDesc){
                    if(d.hasRefexMemberActive(vc, gbRefsetNid) && !d.hasRefexMemberActive(vc, usRefsetNid)){
                        handleDescriptions(desc, d, gbRefsetNid);
                    }
                }
                if(notHandled){
                    handleDescriptions(desc, null, gbRefsetNid);
                }
            }
//DO THESE?
//            else if(dialect.equals(LANG_CODE.DA)){
//                int dkRefsetNid = RefsetAux.DA_REFEX.getLenient().getNid();
////                SET DESCRIPTION TO BE PREFERRED, CHANGE OLD DA PREFERRED TO ACCEPTIBLE
//                
//            }else if(dialect.equals(LANG_CODE.SV)){
//                int svRefsetNid = RefsetAux.SV_REFEX.getLenient().getNid();
////                SET DESCRIPTION TO BE PREFERRED, CHANGE OLD SV PREFERRED TO ACCEPTIBLE
//                
//            }else if(dialect.equals(LANG_CODE.NL)){
//                int nlRefsetNid = RefsetAux.NL_REFEX.getLenient().getNid();
////                SET DESCRIPTION TO BE PREFERRED, CHANGE OLD NL PREFERRED TO ACCEPTIBLE
//                
//            }
            else{
                throw new UnsupportedOperationException("Dialect not supported");
            }
            
            Ts.get().addUncommitted(concept);
        } catch (ContradictionException ex) {
             AceLog.getAppLog().alertAndLogException(ex);
        } catch (IOException ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        } catch (InvalidCAB ex) {
            AceLog.getAppLog().alertAndLogException(ex);
        }
         
    }
    
    private void handleDescriptions(DescriptionVersionBI newPreferred, DescriptionVersionBI oldPreferred, int refexNid) throws IOException,
            ContradictionException, InvalidCAB{
        notHandled = false;
        for(RefexVersionBI r : newPreferred.getAnnotationMembersActive(vc, refexNid)){
            RefexCAB bp = r.makeBlueprint(vc);
            bp.put(RefexCAB.RefexProperty.CNID1, preferredNid);
            bp.setMemberUuid(r.getPrimUuid());
            tb.construct(bp);
        }
        if (oldPreferred != null) {
            for (RefexVersionBI r : oldPreferred.getAnnotationMembersActive(vc, refexNid)) {
                RefexCAB bp = r.makeBlueprint(vc);
                bp.put(RefexCAB.RefexProperty.CNID1, acceptibleNid);
                bp.setMemberUuid(r.getPrimUuid());
                tb.construct(bp);
            }
        }
    }
}
