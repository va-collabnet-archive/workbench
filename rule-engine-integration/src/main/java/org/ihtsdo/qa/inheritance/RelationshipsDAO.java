package org.ihtsdo.qa.inheritance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.tk.api.RelAssertionType;

public class RelationshipsDAO {

	private I_IntSet allowedDestRelTypes;
	private I_IntSet allowedIsATypes;
	private I_IntSet allowedStatus;
	private I_ConfigAceFrame config;
	private I_TermFactory termFactory;
	private I_GetConceptData definingChar;

	private Set<Integer> setDefChar;
	public enum TEST_RESULTS {CONCEPT1_ANCESTOROF_CONCEPT2,CONCEPT2_ANCESTOROF_CONCEPT1,CONCEPTS_DIFF_HIERARCHY,
		CONCEPT1_SUBSUM_CONCEPT2,CONCEPT1_EQUAL_CONCEPT2,CONCEPT2_SUBSUM_CONCEPT1,THERE_IS_NO_SUBSUM,
		ROLE1_SUBSUM_ROLE2, ROLE2_SUBSUM_ROLE1,ROLE1_EQUAL_ROLE2,ROLES_CROSSOVER,
		ROLEGROUP1_SUBSUM_ROLEGROUP2,ROLEGROUP2_SUBSUM_ROLEGROUP1,ROLEGROUP1_EQUAL_ROLEGROUP2,ROLEGROUPS_CROSSOVER};


		public RelationshipsDAO() throws IOException, TerminologyException{
			termFactory = Terms.get();
			allowedDestRelTypes =  termFactory.newIntSet();
			allowedStatus =  termFactory.newIntSet(); 
			config=Terms.get().getActiveAceFrameConfig();
			//			allowedDestRelTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.IS_A_REL.getUids()));
			allowedIsATypes=config.getDestRelTypes();
			I_IntSet modelRel = RulesLibrary.getConceptModelRels();
			allowedDestRelTypes.addAll(allowedIsATypes.getSetValues());
			allowedDestRelTypes.addAll(modelRel.getSetValues());

			//			allowedDestRelTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
			allowedStatus=config.getAllowedStatus();
			definingChar=termFactory.getConcept(ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.localize().getNid());
			//inferred=ArchitectonicAuxiliary.Concept.INFERRED_RELATIONSHIP.localize().getNid();
			
			setDefChar=new HashSet<Integer>();
			setDefChar.add(definingChar.getConceptNid());
			
			//			allowedStatus.add(ArchitectonicAuxiliary.Concept.CURRENT.localize().getNid());
			//			allowedStatus.add(ArchitectonicAuxiliary.Concept.ACTIVE.localize().getNid());
			////			config.setAllowedStatus(allowedStatus);

		}


		public Set<? extends I_GetConceptData> getParents(I_GetConceptData concept) throws IOException, TerminologyException {
			Set<? extends I_GetConceptData> parents = new HashSet<I_GetConceptData>();
			parents =  concept.getSourceRelTargets(allowedStatus ,allowedIsATypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

			return parents;
		}

		public List<? extends I_RelTuple> getStatedIsARels(I_GetConceptData concept) throws IOException, TerminologyException {
		
			return concept.getSourceRelTuples(allowedStatus, 
					allowedIsATypes, 
					config.getViewPositionSetReadOnly(), config.getPrecedence(), 
					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
					RelAssertionType.STATED);
		}
		
		public Set<? extends I_GetConceptData> getChildren(I_GetConceptData concept) throws IOException, TerminologyException {
			Set<? extends I_GetConceptData> children = new HashSet<I_GetConceptData>();

			children =  concept.getDestRelOrigins(allowedStatus, allowedIsATypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());

			return children;
		}

		public TEST_RESULTS subsumptionConceptTest(I_GetConceptData concept1, I_GetConceptData concept2) throws IOException, TerminologyException{
			if (concept1.getConceptNid()==concept2.getConceptNid()){
				return TEST_RESULTS.CONCEPT1_EQUAL_CONCEPT2;
			}
			if (concept1.isParentOf(concept2,allowedStatus, allowedIsATypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())){
				return TEST_RESULTS.CONCEPT1_ANCESTOROF_CONCEPT2;
			}
			if (concept2.isParentOf(concept1,allowedStatus, allowedIsATypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())){
				return TEST_RESULTS.CONCEPT2_ANCESTOROF_CONCEPT1;
			}
			return TEST_RESULTS.CONCEPTS_DIFF_HIERARCHY;

		}

		public TEST_RESULTS subsumptionRoleTest(I_RelTuple role1, I_RelTuple role2) throws TerminologyException, IOException{
			TEST_RESULTS relTargetSubSum=subsumptionConceptTest(termFactory.getConcept( role1.getC2Id()),termFactory.getConcept(role2.getC2Id()));
			TEST_RESULTS relTypeSubsum;
			switch(relTargetSubSum){
			case CONCEPT1_ANCESTOROF_CONCEPT2:
				relTypeSubsum=subsumptionConceptTest(termFactory.getConcept( role1.getTypeNid()),termFactory.getConcept(role2.getTypeNid()));
				switch(relTypeSubsum){
				case CONCEPT1_ANCESTOROF_CONCEPT2:
				case CONCEPT1_EQUAL_CONCEPT2:
					return TEST_RESULTS.ROLE1_SUBSUM_ROLE2;
				case CONCEPT2_ANCESTOROF_CONCEPT1:
					return TEST_RESULTS.ROLES_CROSSOVER;
				case CONCEPTS_DIFF_HIERARCHY:
					return TEST_RESULTS.THERE_IS_NO_SUBSUM;
				}
				break;
			case CONCEPT1_EQUAL_CONCEPT2:
				relTypeSubsum=subsumptionConceptTest(termFactory.getConcept( role1.getTypeNid()),termFactory.getConcept(role2.getTypeNid()));
				switch(relTypeSubsum){
				case CONCEPT1_ANCESTOROF_CONCEPT2:
					return TEST_RESULTS.ROLE1_SUBSUM_ROLE2;
				case CONCEPT1_EQUAL_CONCEPT2:
					return TEST_RESULTS.ROLE1_EQUAL_ROLE2;
				case CONCEPT2_ANCESTOROF_CONCEPT1:
					return TEST_RESULTS.ROLE2_SUBSUM_ROLE1;
				case CONCEPTS_DIFF_HIERARCHY:
					return TEST_RESULTS.THERE_IS_NO_SUBSUM;
				}
				break;
			case CONCEPT2_ANCESTOROF_CONCEPT1:
				relTypeSubsum=subsumptionConceptTest(termFactory.getConcept( role1.getTypeNid()),termFactory.getConcept(role2.getTypeNid()));
				switch(relTypeSubsum){
				case CONCEPT1_ANCESTOROF_CONCEPT2:
					return TEST_RESULTS.ROLES_CROSSOVER;
				case CONCEPT1_EQUAL_CONCEPT2:
				case CONCEPT2_ANCESTOROF_CONCEPT1:
					return TEST_RESULTS.ROLE2_SUBSUM_ROLE1;
				case CONCEPTS_DIFF_HIERARCHY:
					return TEST_RESULTS.THERE_IS_NO_SUBSUM;
				}
				break;
			case CONCEPTS_DIFF_HIERARCHY:
				return TEST_RESULTS.THERE_IS_NO_SUBSUM;
			}
			return null;

		}
		public TEST_RESULTS subsumptionRoleGroupTest(I_RelTuple[] rolegroup1, I_RelTuple[] rolegroup2) throws TerminologyException, IOException{
			TEST_RESULTS roleTestResult ;
			boolean rolesCrossover=false;
			boolean equalRoles=false;
			boolean oneSubsTwoRole=false;
			boolean twoSubsOneRole=false;
			boolean thereIsNoSubsRole=false;
			boolean crossover=false;
			boolean equal=false;
			boolean oneSubsTwo=false;
			boolean twoSubsOne=false;
			I_RelTuple[] tupleToTestSwitch;

			Set<I_RelTuple> tupleTested=new HashSet<I_RelTuple>();
			for (I_RelTuple role1: rolegroup1){
				for (I_RelTuple role2:rolegroup2){
					roleTestResult = subsumptionRoleTest(role1, role2);
					switch(roleTestResult){
					case ROLES_CROSSOVER:
						crossover=true;
						break;
					case ROLE1_EQUAL_ROLE2:
						equal=true;
						tupleTested.add( role2);
						break;
					case ROLE1_SUBSUM_ROLE2:
						oneSubsTwo=true;
						break;
					case ROLE2_SUBSUM_ROLE1:
						twoSubsOne=true;
						tupleTested.add( role2);
						break;
					case THERE_IS_NO_SUBSUM:
						break;
					}
					if (oneSubsTwo){
						break;
					}
				}
				if (oneSubsTwo){
					oneSubsTwoRole=true;
					oneSubsTwo=false;
					crossover=false;
					equal=false;
					twoSubsOne=false;
				}else if (twoSubsOne){
					twoSubsOneRole=true;
					twoSubsOne=false;
					crossover=false;
					equal=false;
				}else if (equal){
					equalRoles=true;
					equal=false;
					crossover=false;
				}else if (crossover){
					rolesCrossover=true;
					crossover=false;
				}else{
					thereIsNoSubsRole=true;		
				}

			}
			if (oneSubsTwoRole && twoSubsOneRole){
				return TEST_RESULTS.ROLEGROUPS_CROSSOVER;
			}
			if (rolesCrossover){
				return TEST_RESULTS.ROLES_CROSSOVER;
			}
			if (thereIsNoSubsRole){
				return TEST_RESULTS.THERE_IS_NO_SUBSUM;
			}
			if (oneSubsTwoRole){
				return TEST_RESULTS.CONCEPT1_SUBSUM_CONCEPT2;
			}
			if (twoSubsOneRole){
				int tuplesTotest=rolegroup2.length-tupleTested.size();
				if (tuplesTotest>0){
					tupleToTestSwitch=new I_RelTuple[tuplesTotest];
					int index=0;
					for (I_RelTuple role2:rolegroup2){
						if (!tupleTested.contains(role2)){
							tupleToTestSwitch[index]=role2;
							index++;
						}
					}
					boolean shortResult=roleGroup1SubSumToRoleGroup2(tupleToTestSwitch,rolegroup1);
					if (!shortResult){
						return TEST_RESULTS.THERE_IS_NO_SUBSUM;
					}
				}
				return TEST_RESULTS.CONCEPT2_SUBSUM_CONCEPT1;
			}
			if (equalRoles){
				return TEST_RESULTS.ROLEGROUP1_EQUAL_ROLEGROUP2;
			}
			return null;

		}


		private boolean roleGroup1SubSumToRoleGroup2(
				I_RelTuple[] rolegroup1, I_RelTuple[] rolegroup2) throws TerminologyException, IOException {
			TEST_RESULTS roleTestResult ;
			boolean equal=false;
			boolean oneSubsTwo=false;
			boolean twoSubsOne=false;

			for (I_RelTuple role1: rolegroup1){
				for (I_RelTuple role2:rolegroup2){
					roleTestResult = subsumptionRoleTest(role1, role2);
					switch(roleTestResult){
					case ROLES_CROSSOVER:
						break;
					case ROLE1_EQUAL_ROLE2:
						equal=true;
						break;
					case ROLE1_SUBSUM_ROLE2:
						oneSubsTwo=true;
						break;
					case ROLE2_SUBSUM_ROLE1:
						twoSubsOne=true;
						break;
					case THERE_IS_NO_SUBSUM:
						break;
					}
					if (oneSubsTwo){
						break;
					}
				}
				if (oneSubsTwo){
					equal=false;
					oneSubsTwo=false;
					twoSubsOne=false;
				}else if (twoSubsOne){
					return false;
				}else if (equal){
					equal=false;
				}else {
					return false;		
				}

			}
			return true;

		}
		public InheritedRelationships getInheritedRelationships (I_GetConceptData concept) throws IOException, TerminologyException{
			List<I_RelTuple[]> allRoleGroups=new ArrayList<I_RelTuple[]>();
			List<I_RelTuple> allSingleRoles=new ArrayList<I_RelTuple>();
			Set<I_GetConceptData> parents=new HashSet<I_GetConceptData>();
			getRecursiveDefiningAttributes(concept, allRoleGroups, allSingleRoles, parents);

			getMoreSpecificRoles(allRoleGroups, allSingleRoles);

			InheritedRelationships inheritedRelationships=new InheritedRelationships(allRoleGroups, allSingleRoles);

			return inheritedRelationships;

		}

		public InheritedRelationships getInheritedRelationships (Set<I_GetConceptData> parents) throws TerminologyException, IOException{
			List<I_RelTuple[]> allRoleGroups=new ArrayList<I_RelTuple[]>();
			List<I_RelTuple> allSingleRoles=new ArrayList<I_RelTuple>();
			Set<I_GetConceptData> recParents=new HashSet<I_GetConceptData>();

			for (I_GetConceptData conceptParent:parents){
				recParents.add(conceptParent);
			}
			for (I_GetConceptData conceptParent:parents){
				getRecursiveDefiningAttributes(conceptParent, allRoleGroups, allSingleRoles, recParents);
			}
			getMoreSpecificRoles(allRoleGroups, allSingleRoles);

			InheritedRelationships inheritedRelationships=new InheritedRelationships(allRoleGroups, allSingleRoles);

			return inheritedRelationships;
		}

		private void getMoreSpecificRoles(List<I_RelTuple[]> allRoleGroups,
				List<I_RelTuple> allSingleRoles) throws TerminologyException, IOException {

			for (int i=allSingleRoles.size()-1;i>-1;i--){
				for (int j=allSingleRoles.size()-1;j>-1;j--){
					if (!allSingleRoles.get(i).equals(allSingleRoles.get(j))){
						TEST_RESULTS roleResult=subsumptionRoleTest(allSingleRoles.get(i),allSingleRoles.get(j));
						if (roleResult.equals(TEST_RESULTS.ROLE1_SUBSUM_ROLE2) || roleResult.equals(TEST_RESULTS.ROLE1_EQUAL_ROLE2)){
							allSingleRoles.remove(allSingleRoles.get(i));
							break;
						}		
						if (roleResult.equals(TEST_RESULTS.ROLE2_SUBSUM_ROLE1)){
							allSingleRoles.remove(allSingleRoles.get(j));
							if (j<i) i--;
						}		
					}
				}
			}
			
			for (int i=allSingleRoles.size()-1;i>-1;i--){
				I_RelTuple[] singleRole1=new I_RelTuple[]{allSingleRoles.get(i)};
				for (I_RelTuple[] roleGroup2:allRoleGroups){
					boolean subsumed= roleGroup1SubSumToRoleGroup2(singleRole1,roleGroup2);
					if (subsumed){
						allSingleRoles.remove(allSingleRoles.get(i));
						break;
					}
				}
			}

			for (int i=allRoleGroups.size()-1;i>-1;i--){
				for (int j=allRoleGroups.size()-1;j>-1;j--){
					if (!allRoleGroups.get(i).equals(allRoleGroups.get(j))){
						boolean subsumed= roleGroup1SubSumToRoleGroup2(allRoleGroups.get(i),allRoleGroups.get(j));
						if (subsumed){
							allRoleGroups.remove(allRoleGroups.get(i));
							break;
						}		
						subsumed= roleGroup1SubSumToRoleGroup2(allRoleGroups.get(j),allRoleGroups.get(i));
						if (subsumed){
							allRoleGroups.remove(allRoleGroups.get(j));
							if (j<i) i--;
						}		
					}
				}
			}
		}


		@SuppressWarnings("unchecked")
		private void getRecursiveDefiningAttributes(I_GetConceptData concept,
				List<I_RelTuple[]> allRoleGroups,List<I_RelTuple> allSingleRoles, Set<I_GetConceptData> parents) throws IOException, TerminologyException {

			List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(allowedStatus, allowedDestRelTypes, config.getViewPositionSetReadOnly(),
					config.getPrecedence(), config.getConflictResolutionStrategy(),config.getClassifierConcept().getNid(),RelAssertionType.STATED);

			HashMap<Integer,List<I_RelTuple>> mapGroup= new HashMap<Integer,List<I_RelTuple>>() ;
			int group=0;
			List<I_RelTuple> roleList=new ArrayList<I_RelTuple>();

			for (I_RelTuple relationship : relationships) {

				if ( allowedIsATypes.contains(relationship.getTypeNid()) ) {
					I_GetConceptData parent= termFactory.getConcept(relationship.getC2Id());
					if (!parents.contains(parent)){
						parents.add(parent);
						getRecursiveDefiningAttributes(parent, allRoleGroups, allSingleRoles, parents );
					}
				} else {
					if (isDefiningChar(relationship.getCharacteristicNid())){
						if (relationship.getGroup()==0){
							allSingleRoles.add( relationship);
						}else{
							group = relationship.getGroup();
							if (mapGroup.containsKey(group)){
								roleList=mapGroup.get(group);
							}
							else{
								roleList=new ArrayList<I_RelTuple>();
							}

							roleList.add(relationship);
							mapGroup.put(group, roleList);
						}
					}
				}
			}			
			for (Integer key:mapGroup.keySet()){
				List<I_RelTuple> lTuple=mapGroup.get(key);
				I_RelTuple[] arrRelTuple=new I_RelTuple[lTuple.size()];
				lTuple.toArray(arrRelTuple);
				allRoleGroups.add(arrRelTuple);
			}

		}


		public boolean isDefiningChar(int characteristicId) throws IOException, TerminologyException {
			
			if (setDefChar.contains(characteristicId)){
				return true;
			}
			I_GetConceptData charactConcept = termFactory.getConcept(characteristicId);
			if (definingChar.isParentOf(charactConcept,allowedStatus, allowedIsATypes, config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy())){
				setDefChar.add(characteristicId);
				return true;
			}				
			return false;
		}
}
