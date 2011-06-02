package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_AmPart;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.arena.spec.SynonymyType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.drools.facts.DescFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.ConceptSpec;

public class SetCaseSensitivityAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    DescriptionVersionBI desc;
    boolean isMember;

    public SetCaseSensitivityAction(String actionName, DescFact fact, boolean isMember) {
        super(actionName);
        this.desc = fact.getComponent();
        this.isMember = isMember;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	I_ConfigAceFrame config;
		try {
			I_AmPart componentVersion;
			config = Terms.get().getActiveAceFrameConfig();
			TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
	                config.getViewCoordinate());
			ViewCoordinate vc = config.getViewCoordinate();
			
			if(currentSynonymy){
				Collection<? extends RefexChronicleBI> refexes = desc.getCurrentRefexes(vc);
				int synonymyCollectionNid = Ts.get().getNidForUuids(Refsets.DEGREE_OF_SYNONYMY.getLenient().getPrimUuid());
				int synonymyTypeNid;
				
				if(synonymy.equals(SynonymyType.NEAR_SYNONYMOUS)){
					synonymyTypeNid = Ts.get().getNidForUuids(SynonymyType.NEAR_SYNONYMOUS.getLenient().getPrimUuid());
				}else if(synonymy.equals(SynonymyType.NON_SYNONYMOUS)){
					synonymyTypeNid = Ts.get().getNidForUuids(SynonymyType.NON_SYNONYMOUS.getLenient().getPrimUuid());
				}else {
					synonymyTypeNid = Ts.get().getNidForUuids(SynonymyType.SYNONYM.getLenient().getPrimUuid());
				}
				
				if (refexes != null) {
	                for (RefexChronicleBI refex : refexes) {
	                	if (refex.getCollectionNid() == synonymyCollectionNid) {
	                		//make analog
	                		componentVersion = (I_AmPart) refex;
	                		AnalogBI analog = null;
	                		for (PathBI ep : config.getEditingPathSet()) {
	                            analog = componentVersion.makeAnalog(
	                                    ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
	                                    config.getDbConfig().getUserConcept().getNid(),
	                                    ep.getConceptNid(),
	                                    Long.MAX_VALUE);
	                            
		                		RefexVersionBI<?> newRefex = (RefexVersionBI<?>) analog;
		                		//test member type
		                		if (RefexCnidVersionBI.class.isAssignableFrom(newRefex.getClass())){
		                			RefexCnidVersionBI rcv = (RefexCnidVersionBI) newRefex;
		                			RefexCnidAnalogBI rca = (RefexCnidAnalogBI) rcv;
		                			
		                			rca.setCnid1(synonymyTypeNid);

		                			I_GetConceptData concept = Terms.get().getConceptForNid(newRefex.getNid());
		                            Terms.get().addUncommitted(concept);
		                		}else{
				                		throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
				                }
			                }
	                	}
	                }
	            }
			}else {
				if(synonymy.equals(SynonymyType.SYNONYM)){
					RefexCAB syn = new RefexCAB(
										TK_REFSET_TYPE.CID,
										desc.getNid(),
										Ts.get().getNidForUuids(degreeOfSynonymy));
					syn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.SYNONYM.getLenient().getPrimUuid()));
					RefexChronicleBI<?> newRefex = tc.construct(syn);
	   	           	I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
	   	           	Ts.get().addUncommitted(refex);
				}else if(synonymy.equals(SynonymyType.NEAR_SYNONYMOUS)){
					RefexCAB nearSyn = new RefexCAB(
							TK_REFSET_TYPE.CID,
							desc.getNid(),
							Ts.get().getNidForUuids(degreeOfSynonymy));
					nearSyn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.NEAR_SYNONYMOUS.getLenient().getPrimUuid()));
					RefexChronicleBI<?> newRefex = tc.construct(nearSyn);
	   	           	I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
	   	           	Ts.get().addUncommitted(refex);
				}else if(synonymy.equals(SynonymyType.NON_SYNONYMOUS)){
					RefexCAB notSyn = new RefexCAB(
							TK_REFSET_TYPE.CID,
							desc.getNid(),
							Ts.get().getNidForUuids(degreeOfSynonymy));
					notSyn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(SynonymyType.NON_SYNONYMOUS.getLenient().getPrimUuid()));
					RefexChronicleBI<?> newRefex = tc.construct(notSyn);
	   	           	I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
	   	           	Ts.get().addUncommitted(refex);
				}
				else{
					throw new UnsupportedOperationException("Synonymy not supported");
				}
			}
			
			
		} catch (TerminologyException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (IOException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (PropertyVetoException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (InvalidCAB ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
    	
       
    }
}
