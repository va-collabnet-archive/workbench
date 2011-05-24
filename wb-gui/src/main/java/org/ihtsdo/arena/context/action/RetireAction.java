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
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.arena.spec.Refsets;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.AnalogBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidAnalogBI;
import org.ihtsdo.tk.api.refex.type_cnid.RefexCnidVersionBI;
import org.ihtsdo.tk.drools.facts.ComponentFact;

public class RetireAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    ComponentVersionBI component;
    AnalogBI analog;

    public RetireAction(String actionName, ComponentFact<ComponentVersionBI> fact) {
        super(actionName);
        this.component = fact.getComponent();
        /*
        putValue(LARGE_ICON_KEY,
        new ImageIcon(BatchMonitor.class.getResource("/24x24/plain/delete2.png")));
         */
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (I_AmPart.class.isAssignableFrom(component.getClass())) {
                I_AmPart componentVersion = (I_AmPart) component;
                I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
                for (PathBI ep : config.getEditingPathSet()) {
                    analog = componentVersion.makeAnalog(
                            ArchitectonicAuxiliary.Concept.RETIRED.localize().getNid(),
                            config.getDbConfig().getUserConcept().getNid(),
                            ep.getConceptNid(),
                            Long.MAX_VALUE);
                }
                ComponentVersionBI newComponent = (ComponentVersionBI) analog;
                if(DescriptionVersionBI.class.isAssignableFrom(component.getClass())){
                	makeNotAcceptable(newComponent);
                }
                I_GetConceptData concept = Terms.get().getConceptForNid(newComponent.getNid());
                Terms.get().addUncommitted(concept);
            }
        } catch (TerminologyException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        } catch (IOException e1) {
            AceLog.getAppLog().alertAndLogException(e1);
        }
    }
    
    private void makeNotAcceptable(ComponentVersionBI newComponent){
    	DescriptionVersionBI desc = (DescriptionVersionBI) newComponent;
    	try{
    		I_ConfigAceFrame config = Terms.get().getActiveAceFrameConfig();
    		I_AmPart componentVersion;
    		ViewCoordinate vc = config.getViewCoordinate();
        	Collection<? extends RefexChronicleBI> refexes = desc.getCurrentRefexes(vc);
        	for(RefexChronicleBI refex : refexes){
        		int refexNid = refex.getCollectionNid();
        		int usNid = Ts.get().getNidForUuids(Refsets.EN_US_LANG.getLenient().getPrimUuid());
        		int gbNid = Ts.get().getNidForUuids(Refsets.EN_GB_LANG.getLenient().getPrimUuid());
        		if(refexNid == gbNid || refexNid == usNid){
        			componentVersion = (I_AmPart) refex;
            		AnalogBI analog = null;
            		for (PathBI ep : config.getEditingPathSet()) {
                        analog = componentVersion.makeAnalog(
                                ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid(),
                                config.getDbConfig().getUserConcept().getNid(),
                                ep.getConceptNid(),
                                Long.MAX_VALUE);
                    }
            		RefexVersionBI<?> newRefex = (RefexVersionBI<?>) analog;
            		//test member type
                		if (RefexCnidVersionBI.class.isAssignableFrom(newRefex.getClass())){
                			RefexCnidVersionBI rcv = (RefexCnidVersionBI) newRefex;
                			RefexCnidAnalogBI rca = (RefexCnidAnalogBI) rcv;
                			
                			rca.setCnid1(Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));

                			I_GetConceptData concept = Terms.get().getConceptForNid(newRefex.getNid());
                            Terms.get().addUncommitted(concept);
                			}else{
		                		throw new UnsupportedOperationException("Can't convert: RefexCnidVersionBI");
		                	}
        		}
        	}
    	}catch(IOException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
    	}catch (TerminologyException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (PropertyVetoException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		}
    }
}
