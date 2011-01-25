package org.ihtsdo.ace.task.contradiction;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_RepresentIdSet;
import org.dwfa.ace.api.IdentifierSet;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.ihtsdo.concept.Concept;
import org.ihtsdo.concept.ConflictIdentifier;
import org.ihtsdo.concept.I_ProcessUnfetchedConceptData;
import org.ihtsdo.concept.ParallelConceptIterator;
import org.ihtsdo.concept.ConflictIdentifier.CONTRADICTION_RESULT;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PositionBI;

public class ContradictionConceptProcessor implements I_ProcessUnfetchedConceptData {
	NidBitSetBI cNids = new IdentifierSet();
	private ConflictIdentifier detector = null;
	private ContradictionIdentificationResults results = null;

	public ContradictionConceptProcessor(PositionBI position) 
	{
			buildInvestigationSet();
			results = new ContradictionIdentificationResults();
			detector = new ConflictIdentifier(); 
	}

	@Override
 	public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fcfc) throws Exception
	{
		if (cNids.isMember(cNid)) {
			Concept c = (Concept) fcfc.fetch();
			CONTRADICTION_RESULT position = (detector.inConflict(c));

			if (position.equals(CONTRADICTION_RESULT.CONTRADICTION))
				results.addConflict(c.getConceptNid());
			else if (position.equals(CONTRADICTION_RESULT.UNREACHABLE))
				results.addUnreachable(c.getConceptNid());
			else if (position.equals(CONTRADICTION_RESULT.SINGLE))
				results.addSingle(c.getConceptNid());
			else
				results.addNoneConflicting(c.getConceptNid());
		}
	}
	
	private void buildInvestigationSet() {
		I_RepresentIdSet set = new IdentifierSet();

		try
		{
		// Death From Overwork
		// set.setMember(Terms.get().getConcept(UUID.fromString("5b319d0d-d320-34e7-b996-0a8a58d95186")).getConceptNid());
		// Sleep Deprived

		// *** Components ***
		// <NONE> -- Social Context
		//set.setMember(Terms.get().getConcept(UUID.fromString("b89db478-21d5-3e51-972d-6c900f0ec436")).getConceptNid());

	/*
		// Unit Test #1 (Organism)
		set.setMember(Terms.get().getConcept(UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072")).getConceptNid());
		// Unit Test #2 (Test Concept UT #2)
		set.setMember(Terms.get().getConcept(UUID.fromString("cd05f03c-f645-47d3-943e-b74787041f27")).getConceptNid());

 		// Unit Test #3 (Record Artifact)
 		// Unit Test #4 (Temporal Observable)
 		// Unit Test #5 (Vital Sign)
		set.setMember(Terms.get().getConcept(UUID.fromString("2724663a-65a5-381f-ba31-6c8b576b8948")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("55acf2a4-6a88-3bf9-938e-74a21604bb24")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("d1352349-640b-3609-8888-c1e5e59f0fce")).getConceptNid());
		
 		// Unit Test #6 (Calcium deposit observable)
 		// Unit Test #7 (Clinical Finding)
 		// Unit Test #8 (Environment or geographical location)
 		// Unit Test #9 (Event)
		set.setMember(Terms.get().getConcept(UUID.fromString("9dd0b6e5-5e07-33ed-bf77-6048d56a3712")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("1b90be6e-f625-391a-9e14-08dc8b84392c")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698")).getConceptNid());

 		// Unit Test #10 (Administrative statuses)
 		// Unit Test #11 (Adverse incident outcome categories)
 		// Unit Test #12 (Calculus finding)
 		// Unit Test #13 (Clinical history and observation findings)
 		// Unit Test #14 (Clinical stage finding)
 		// Unit Test #15 (Cyanosis)

 		set.setMember(Terms.get().getConcept(UUID.fromString("d851e4c9-2c1f-32ab-8648-0a8bcd086ce1")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("b807ab7f-e643-31dc-9143-4469e9bf9ce2")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("181c903d-aaa7-38eb-863b-dceeccf07cef")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("54db2917-84be-313b-acd5-defce77c0077")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("a9ed3709-70ef-3e27-9651-a8d034fccb28")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("d52fb8f7-095c-3f34-9cb0-db4102dc1803")).getConceptNid());

		
		// Unit Test #16 (Jesse FSN/Jesse Preferred Term)
		//Jaleh (mod 1 path): efac07fb-6f7b-4f4e-98a2-236d34f99389	
		//Phil  (mod 1 path): 61ed2f1f-a65d-424d-9c73-a7af8b008bc4	
	// 		set.setMember(Terms.get().getConcept(UUID.fromString("efac07fb-6f7b-4f4e-98a2-236d34f99389")).getConceptNid());
	//		set.setMember(Terms.get().getConcept(UUID.fromString("61ed2f1f-a65d-424d-9c73-a7af8b008bc4")).getConceptNid());

 		// Unit Test #17 (Action)
 		// Unit Test #18 (Additional dosage instructions)
 		// Unit Test #19 (Additional Values)
 		// Unit Test #20 (Agencies and Organizations)

		// Unit Test #21 (Alphanumeric)
 		// Unit Test #22 (Anatomic reference point)
 		// Unit Test #23 (Benefits, entitilements and rights)
 		// Unit Test #24 (Classification system)
 		
		// Unit Test #25 (Clinical specialty)
 		// Unit Test #26 (Context values)
 		// Unit Test #27 (Descriptor)
 		// Unit Test #28 (Dosing instruction fragment) 
		
 		set.setMember(Terms.get().getConcept(UUID.fromString("f611bc15-3455-30a9-9399-d3d8471656c1")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("338f208f-500f-3c5e-9021-6b3ea7ec8115")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("443a62c8-cb70-31a3-af52-e492a166ced0")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("2593eed5-8a7d-35a9-9675-c668d50c3069")).getConceptNid());
 		
 		set.setMember(Terms.get().getConcept(UUID.fromString("3d42c525-9877-3350-b284-a6b98436f832")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("2fbdc824-dc00-3990-8013-b8308bbf9147")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("2fc6ae32-30cd-30eb-ad45-9b3d45cdc74a")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("8451d166-2590-3f07-b525-fd7e43912bb9")).getConceptNid());
 		
 		set.setMember(Terms.get().getConcept(UUID.fromString("6586c20a-36e8-3227-a573-a43d871c65ae")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("e9f55abd-e523-36bb-92a9-2d8bda4c28b5")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("139a7f6e-ab14-3ffe-bcf1-a3a6d724e6d4")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("1f8eafc0-bba1-3072-9f7b-fe0c07073531")).getConceptNid());

 		
 		// Unit Test #29 (Biopsy sample)
 		// Unit Test #30 (Blood specimen)
 		// Unit Test #31 (Body substance sample)

 		// Unit Test #32 (Calculus specimen)
 		// Unit Test #33 (Cardiovascular sample)
 		// Unit Test #34 (Bone marrow specimen)
 		
 		// Unit Test #35 (Dermatological sample)
 		// Unit Test #36 (Device specimen) 
 		// Unit Test #37 (Drug Specimen) 

 		set.setMember(Terms.get().getConcept(UUID.fromString("874f040d-a221-3941-8624-daf089d2534e")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("180a16c5-2638-3d33-ae7e-f89ef919ca70")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("8a0132bc-8fdd-3ee5-a4f3-364527fc6bfa")).getConceptNid());
 		
 		set.setMember(Terms.get().getConcept(UUID.fromString("68c42256-0b82-353b-ba03-be2e6f237e5c")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("7234f923-802a-3213-ba3a-b1346d7bb939")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("fa8d17c9-7ee3-39b9-9128-5faa77e5d595")).getConceptNid());
 		
 		set.setMember(Terms.get().getConcept(UUID.fromString("37acbcab-6056-30d4-b81c-141b0cc00ea3")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("5882c90a-3601-3ef3-9ca1-b4480555e7ee")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("de90eca1-ac53-3d2c-8d1c-fb7d3d2baab2")).getConceptNid());


		// Unit Test #38 (Assessment Scales)
 		// Unit Test #39 (Local Cyanosis)
 		// Unit Test #40 (Symptoms ratings) 
 		// Unit Test #41 (Tumor staging) 
		
 		set.setMember(Terms.get().getConcept(UUID.fromString("9db11d3b-a3c9-3655-b785-435ca45d89ed")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("95970cf0-8ef7-3344-a305-d70db964d472")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("a8d6eb61-3d3f-34d6-a94c-d9ee3f7c99da")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("2ca36f27-7e05-3a30-9e85-cf8d4a61984e")).getConceptNid());

		// Unit Test #42 (Allergen class)
 		// Unit Test #43 (Aromatherapy agent)
 		// Unit Test #44 (Biological substance)
 		// Unit Test #45 (Body substance) 
		
 		set.setMember(Terms.get().getConcept(UUID.fromString("915d2929-10ee-3b9a-8a3b-8533bd6fb39d")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("4fb20057-688e-3495-b9d6-c157d1694440")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("30c078f7-814a-309a-92e2-f2fafccbdc9f")).getConceptNid());
 		set.setMember(Terms.get().getConcept(UUID.fromString("8f9f6413-8ed6-3b72-a630-cbb91b22a3a1")).getConceptNid());
	*/
 		
 		// Unit Test #46 (Insufficient Requesting Detail) 
 		// Unit Test #47 (Medical examinations/reports status) 
 		// Unit Test #48 (Medicolegal procedure status) 
 		// Unit Test #49 (Patient encounter status) 
		set.setMember(Terms.get().getConcept(UUID.fromString("31a4d933-a8b4-30ee-b534-feb70e4e05ba")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("b66b613f-c3a8-3bb7-a165-5b78a810060c")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("48160dd7-756c-3b29-b9c5-fb36f140db81")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("e4e1ad2b-3ba8-3956-8558-880e9058bef3")).getConceptNid());
	/*
		// *** REFSETS ***
		// Unit Test #1  (Death)
 		// Unit Test #2  
 		// Unit Test #3  (Accidental Death)
 		// Unit Test #4  (Death-Expected)
 		// Unit Test #5  (Death by ashphyxiation)
 		// Unit Test #6  (Death by electrocution)
 		// Unit Test #7  (Alejandro Lopez)
		set.setMember(Terms.get().getConcept(UUID.fromString("ba7e96f8-7cad-3157-8c95-0290395c13d2")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("c4d25f5f-1f63-37e9-8775-c851fe99cb87")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("21cdeb5d-f402-3124-9f8d-00c6176dc37f")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("935ca22f-7703-3370-9e9f-1025694f3f9b")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("5a27bef4-41e0-33e7-8da3-76450d82cbf5")).getConceptNid());
		set.setMember(Terms.get().getConcept(UUID.fromString("800e6651-a619-3edf-bb90-74ab279966c9")).getConceptNid());
	*/
		} catch (Exception e) {
			AceLog.getAppLog().log(Level.WARNING, "Error in intializing Contradiction Concept Processor", e);
		}

		setNidSet(set);
	}

	public ContradictionIdentificationResults getResults() {
		return results;
	}

	public void setNidSet(NidBitSetBI nids)  {
		this.cNids = nids;
	}
	
	public NidBitSetBI getNidSet() throws IOException {
		return cNids;
	}
	
	@Override
	public void setParallelConceptIterators(
			List<ParallelConceptIterator> pcis) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean continueWork() {
		return true;
	}
}
