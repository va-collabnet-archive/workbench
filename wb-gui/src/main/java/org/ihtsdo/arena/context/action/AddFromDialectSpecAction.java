package org.ihtsdo.arena.context.action;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.swing.AbstractAction;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.spec.ConceptSpec;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.DescCAB;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.drools.facts.ConceptFact;
import org.ihtsdo.tk.drools.facts.DescSpecFact;
import org.ihtsdo.tk.drools.facts.RelSpecFact;
import org.ihtsdo.tk.drools.facts.SpecFact;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.spec.DescriptionSpec;
import org.ihtsdo.tk.spec.RelSpec;
import org.ihtsdo.tk.example.binding.Language;
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
			UUID type = descSpec.getDescTypeSpec().get(concept.getViewCoordinate()).getPrimUuid();
			UUID syn = WbDescType.SYNONYM.getLenient().getPrimUuid();
			UUID fsn = WbDescType.FULLY_SPECIFIED.getLenient().getPrimUuid();
			if(dialect.equals("en-gb")){
				dialectUuid= UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd");
			}else if(dialect.equals("en-us")){
				dialectUuid = UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6");
			}
			TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
	                config.getViewCoordinate());

			if(type.equals(syn)){
				DescCAB descSpecPref = new DescCAB(
						concept.getPrimUuid(),
	                    WbDescType.SYNONYM.getLenient().getPrimUuid(),
	                    dialect,
	                    descSpec.getDescText(),
	                    false);
				RefexCAB refexSpecPrefGb = new RefexCAB(
	                    TK_REFSET_TYPE.CID,
	                    descSpecPref.getComponentNid(),
	                    Ts.get().getNidForUuids(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")));
	            RefexCAB refexSpecPrefUs = new RefexCAB(
	                    TK_REFSET_TYPE.CID,
	                    descSpecPref.getComponentNid(),
	                    Ts.get().getNidForUuids(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")));
	            if(dialect.equals("en-gb")){
	            	refexSpecPrefGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
	            	refexSpecPrefUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
	            }else {
	            	refexSpecPrefGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.NOT_ACCEPTABLE.getLenient().getPrimUuid()));
	            	refexSpecPrefUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
	            }
	            newDesc =  tc.construct(descSpecPref);
	            newRefex = tc.construct(refexSpecPrefGb);
	            newRefex = tc.construct(refexSpecPrefUs);
			}else if (type.equals(fsn)){
				DescCAB descSpecFsn = new DescCAB(
						concept.getPrimUuid(),
	                    WbDescType.FULLY_SPECIFIED.getLenient().getPrimUuid(),
	                    dialect,
	                    descSpec.getDescText(),
	                    false);
	            RefexCAB refexSpecFsn = new RefexCAB(
	                    TK_REFSET_TYPE.CID,
	                    descSpecFsn.getComponentNid(),
	                    Ts.get().getNidForUuids(dialectUuid));
	            refexSpecFsn.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
	            newDesc = tc.construct(descSpecFsn);
	            newRefex = tc.construct(refexSpecFsn);
			}
			I_GetConceptData desc = Terms.get().getConceptForNid(newDesc.getNid());
			I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
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
