/*
 * Copyright (c) 2010 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.qa.inheritance;

import java.beans.PropertyVetoException;
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
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.rules.RulesLibrary;
import org.ihtsdo.testmodel.DrRelationship;
import org.ihtsdo.tk.api.ContradictionException;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.RelAssertionType;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 * The Class RelationshipsDAO.
 */
public class RelationshipsDAO {

	/** The allowed dest rel types. */
	private I_IntSet allowedDestRelTypes;
	
	/** The allowed is a types. */
	private I_IntSet allowedIsATypes;
	
	/** The allowed status. */
	private I_IntSet allowedStatus;
	
	/** The config. */
	private I_ConfigAceFrame config;
	
	/** The term factory. */
	private I_TermFactory termFactory;
	
	/** The stated. */
	private I_GetConceptData stated;
	
	/** The model rel. */
	private I_IntSet modelRel;

	/** The set def char. */
	private Set<Integer> setDefChar;

	/**
	 * The Enum TEST_RESULTS.
	 */
	public enum TEST_RESULTS {/** The CONCEP t1_ ancestoro f_ concep t2. */
CONCEPT1_ANCESTOROF_CONCEPT2,/** The CONCEP t2_ ancestoro f_ concep t1. */
CONCEPT2_ANCESTOROF_CONCEPT1,/** The CONCEPT s_ dif f_ hierarchy. */
CONCEPTS_DIFF_HIERARCHY,
		
		/** The CONCEP t1_ subsu m_ concep t2. */
		CONCEPT1_SUBSUM_CONCEPT2,
/** The CONCEP t1_ equa l_ concep t2. */
CONCEPT1_EQUAL_CONCEPT2,
/** The CONCEP t2_ subsu m_ concep t1. */
CONCEPT2_SUBSUM_CONCEPT1,
/** The THER e_ i s_ n o_ subsum. */
THERE_IS_NO_SUBSUM,
		
		/** The ROL e1_ subsu m_ rol e2. */
		ROLE1_SUBSUM_ROLE2, 
 /** The ROL e2_ subsu m_ rol e1. */
 ROLE2_SUBSUM_ROLE1,
/** The ROL e1_ equa l_ rol e2. */
ROLE1_EQUAL_ROLE2,
/** The ROLE s_ crossover. */
ROLES_CROSSOVER,
		
		/** The ROLEGROU p1_ subsu m_ rolegrou p2. */
		ROLEGROUP1_SUBSUM_ROLEGROUP2,
/** The ROLEGROU p2_ subsu m_ rolegrou p1. */
ROLEGROUP2_SUBSUM_ROLEGROUP1,
/** The ROLEGROU p1_ equa l_ rolegrou p2. */
ROLEGROUP1_EQUAL_ROLEGROUP2,
/** The ROLEGROUP s_ crossover. */
ROLEGROUPS_CROSSOVER};


		/**
		 * Instantiates a new relationships dao.
		 *
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public RelationshipsDAO() throws IOException, TerminologyException{
			termFactory = Terms.get();
			allowedDestRelTypes =  termFactory.newIntSet();
			allowedStatus =  termFactory.newIntSet(); 
			config=Terms.get().getActiveAceFrameConfig();
			allowedIsATypes=config.getDestRelTypes();
			modelRel = RulesLibrary.getConceptModelRels();
			allowedDestRelTypes.addAll(allowedIsATypes.getSetValues());
			allowedDestRelTypes.addAll(modelRel.getSetValues());

			allowedStatus=config.getAllowedStatus();
			stated=termFactory.getConcept(SnomedMetadataRf2.STATED_RELATIONSHIP_RF2.getLenient().getNid());

			setDefChar=new HashSet<Integer>();
			setDefChar.add(stated.getConceptNid());

		}


		/**
		 * Gets the parents.
		 *
		 * @param concept the concept
		 * @return the parents
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public Set<? extends I_GetConceptData> getParents(I_GetConceptData concept) throws IOException, TerminologyException {
			Set<? extends I_GetConceptData> parents = new HashSet<I_GetConceptData>();
			parents =  concept.getSourceRelTargets(allowedStatus ,allowedIsATypes, getMockViewSet(config), config.getPrecedence(), config.getConflictResolutionStrategy());

			return parents;
		}

		/**
		 * Gets the stated is a rels.
		 *
		 * @param concept the concept
		 * @return the stated is a rels
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public List<? extends I_RelTuple> getStatedIsARels(I_GetConceptData concept) throws IOException, TerminologyException {

			return concept.getSourceRelTuples(allowedStatus, 
					allowedIsATypes, 
					getMockViewSet(config), config.getPrecedence(), 
					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
					RelAssertionType.STATED);
		}

		/**
		 * Gets the stated all rels.
		 *
		 * @param concept the concept
		 * @return the stated all rels
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public List<? extends I_RelTuple> getStatedAllRels(I_GetConceptData concept) throws IOException, TerminologyException {

			return concept.getSourceRelTuples(allowedStatus, 
					null, 
					getMockViewSet(config), config.getPrecedence(), 
					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
					RelAssertionType.STATED);
		}

		/**
		 * Gets the inferred rels.
		 *
		 * @param concept the concept
		 * @return the inferred rels
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public List<? extends I_RelTuple> getInferredRels(I_GetConceptData concept) throws IOException, TerminologyException {

			return concept.getSourceRelTuples(allowedStatus, 
					null, 
					getMockViewSet(config), config.getPrecedence(), 
					config.getConflictResolutionStrategy(), config.getClassifierConcept().getNid(), 
					RelAssertionType.INFERRED);
		}

		/**
		 * Gets the rel tuples.
		 *
		 * @param concept the concept
		 * @return the rel tuples
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public List<I_RelTuple> getRelTuples(I_GetConceptData concept) throws IOException, TerminologyException{
			List<I_RelTuple> result = new ArrayList<I_RelTuple>();
                        try {
                                InheritedRelationships inheritedRels = getInheritedRelationships(concept);
                                
				List<I_RelTuple[]> roleGroups = inheritedRels.getRoleGroups();
				int relGroup = 1;
				for (I_RelTuple[] i_RelTuples : roleGroups) {
					for (I_RelTuple i_RelTuple : i_RelTuples) {
						i_RelTuple.setGroup(relGroup);
						result.add(i_RelTuple);
					}
					relGroup++;
				}

				List<I_RelTuple> singles = inheritedRels.getSingleRoles();
				for (I_RelTuple i_RelTuple : singles) {
					i_RelTuple.setGroup(0);
					result.add(i_RelTuple);
				}
			} catch (PropertyVetoException e) {
				throw new TerminologyException(e);
			} catch (ContradictionException e) {
				throw new TerminologyException(e);
			}

			return result;

		}


		/**
		 * Gets the children.
		 *
		 * @param concept the concept
		 * @return the children
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public Set<? extends I_GetConceptData> getChildren(I_GetConceptData concept) throws IOException, TerminologyException {
			Set<? extends I_GetConceptData> children = new HashSet<I_GetConceptData>();

			children =  concept.getDestRelOrigins(allowedStatus, allowedIsATypes, getMockViewSet(config), config.getPrecedence(), config.getConflictResolutionStrategy());

			return children;
		}

		/**
		 * Subsumption concept test.
		 *
		 * @param concept1 the concept1
		 * @param concept2 the concept2
		 * @return the tES t_ results
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public TEST_RESULTS subsumptionConceptTest(I_GetConceptData concept1, I_GetConceptData concept2) throws IOException, TerminologyException, ContradictionException{
			if (concept1.getConceptNid()==concept2.getConceptNid()){
				return TEST_RESULTS.CONCEPT1_EQUAL_CONCEPT2;
			}
			if (concept1.isParentOf(concept2,allowedStatus, allowedIsATypes, getMockViewSet(config), config.getPrecedence(), config.getConflictResolutionStrategy())){
				return TEST_RESULTS.CONCEPT1_ANCESTOROF_CONCEPT2;
			}
			if (concept2.isParentOf(concept1,allowedStatus, allowedIsATypes, getMockViewSet(config), config.getPrecedence(), config.getConflictResolutionStrategy())){
				return TEST_RESULTS.CONCEPT2_ANCESTOROF_CONCEPT1;
			}
			return TEST_RESULTS.CONCEPTS_DIFF_HIERARCHY;

		}

		/**
		 * Subsumption role test.
		 *
		 * @param role1 the role1
		 * @param role2 the role2
		 * @return the tES t_ results
		 * @throws TerminologyException the terminology exception
		 * @throws IOException signals that an I/O exception has occurred.
		 */
		public TEST_RESULTS subsumptionRoleTest(I_RelTuple role1, I_RelTuple role2) throws TerminologyException, IOException, ContradictionException{
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
		
		/**
		 * Subsumption role group test.
		 *
		 * @param rolegroup1 the rolegroup1
		 * @param rolegroup2 the rolegroup2
		 * @return the tES t_ results
		 * @throws TerminologyException the terminology exception
		 * @throws IOException signals that an I/O exception has occurred.
		 */
		public TEST_RESULTS subsumptionRoleGroupTest(I_RelTuple[] rolegroup1, I_RelTuple[] rolegroup2) throws TerminologyException, IOException, ContradictionException{
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


		/**
		 * Role group1 sub sum to role group2.
		 *
		 * @param rolegroup1 the rolegroup1
		 * @param rolegroup2 the rolegroup2
		 * @return true, if successful
		 * @throws TerminologyException the terminology exception
		 * @throws IOException signals that an I/O exception has occurred.
		 */
		private boolean roleGroup1SubSumToRoleGroup2(
				I_RelTuple[] rolegroup1, I_RelTuple[] rolegroup2) throws
                        TerminologyException, IOException, ContradictionException {
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
		
		/**
		 * Gets the inherited relationships.
		 *
		 * @param concept the concept
		 * @return the inherited relationships
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public InheritedRelationships getInheritedRelationships (I_GetConceptData concept) throws
                        IOException, TerminologyException, ContradictionException{
			List<I_RelTuple[]> allRoleGroups=new ArrayList<I_RelTuple[]>();
			List<I_RelTuple> allSingleRoles=new ArrayList<I_RelTuple>();
			Set<I_GetConceptData> parents=new HashSet<I_GetConceptData>();
			getRecursiveDefiningAttributes(concept, allRoleGroups, allSingleRoles, parents);

			getMoreSpecificRoles(allRoleGroups, allSingleRoles);

			InheritedRelationships inheritedRelationships=new InheritedRelationships(allRoleGroups, allSingleRoles);

			return inheritedRelationships;

		}

		/**
		 * Gets the inherited relationships.
		 *
		 * @param parents the parents
		 * @return the inherited relationships
		 * @throws TerminologyException the terminology exception
		 * @throws IOException signals that an I/O exception has occurred.
		 */
		public InheritedRelationships getInheritedRelationships (Set<I_GetConceptData> parents) throws 
                        TerminologyException, IOException, ContradictionException{
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

		/**
		 * Gets the more specific roles.
		 *
		 * @param allRoleGroups the all role groups
		 * @param allSingleRoles the all single roles
		 * @return the more specific roles
		 * @throws TerminologyException the terminology exception
		 * @throws IOException signals that an I/O exception has occurred.
		 */
		private void getMoreSpecificRoles(List<I_RelTuple[]> allRoleGroups,
				List<I_RelTuple> allSingleRoles) throws TerminologyException, IOException, ContradictionException {

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


		/**
		 * Gets the recursive defining attributes.
		 *
		 * @param concept the concept
		 * @param allRoleGroups the all role groups
		 * @param allSingleRoles the all single roles
		 * @param parents the parents
		 * @return the recursive defining attributes
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		@SuppressWarnings("unchecked")
		private void getRecursiveDefiningAttributes(I_GetConceptData concept,
				List<I_RelTuple[]> allRoleGroups,List<I_RelTuple> allSingleRoles, Set<I_GetConceptData> parents) throws
                        IOException, TerminologyException, ContradictionException {

			List<I_RelTuple> relationships = (List<I_RelTuple>) concept.getSourceRelTuples(allowedStatus, allowedDestRelTypes, getMockViewSet(config),
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


		/**
		 * Checks if is defining char.
		 *
		 * @param characteristicId the characteristic id
		 * @return true, if is defining char
		 * @throws IOException signals that an I/O exception has occurred.
		 * @throws TerminologyException the terminology exception
		 */
		public boolean isDefiningChar(int characteristicId) throws IOException, TerminologyException, ContradictionException {

			if (setDefChar.contains(characteristicId)){
				return true;
			}
			I_GetConceptData charactConcept = termFactory.getConcept(characteristicId);
			if (stated.isParentOfOrEqualTo(charactConcept,allowedStatus, allowedIsATypes, getMockViewSet(config), config.getPrecedence(), config.getConflictResolutionStrategy())){
				setDefChar.add(characteristicId);
				return true;
			}				
			return false;
		}

		/**
		 * Gets the constraint normal form.
		 *
		 * @param conceptData the concept data
		 * @param factContextName the fact context name
		 * @return the constraint normal form
		 */
		public List<DrRelationship> getConstraintNormalForm(I_GetConceptData conceptData, String factContextName){

			List<DrRelationship> rels = new ArrayList<DrRelationship>();
			try {
				InheritedRelationships inhRel = getInheritedRelationships(conceptData);
				//Inherited single roles
				for (I_RelTuple relTuple:inhRel.getSingleRoles()){
					DrRelationship loopRel = new DrRelationship();
					loopRel.setModifierUuid("someUuid");
					loopRel.setAuthorUuid(termFactory.nidToUuid(relTuple.getAuthorNid()).toString());
					loopRel.setSourceUuid(termFactory.nidToUuid(relTuple.getSourceNid()).toString());
					loopRel.setTargetUuid(termFactory.nidToUuid(relTuple.getTargetNid()).toString());
					loopRel.setCharacteristicUuid(termFactory.nidToUuid(relTuple.getCharacteristicNid()).toString());
					loopRel.setPathUuid(termFactory.nidToUuid(relTuple.getPathNid()).toString());
					loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
					loopRel.setRelGroup(0);
					loopRel.setStatusUuid(termFactory.nidToUuid(relTuple.getStatusNid()).toString());
					loopRel.setTime(relTuple.getTime());
					loopRel.setTypeUuid(termFactory.nidToUuid(relTuple.getTypeNid()).toString());
					loopRel.setFactContextName(factContextName);
					rels.add(loopRel);
				}
				//Inherited grouped roles
				int groupNr=0;
				for (I_RelTuple[] relTuples:inhRel.getRoleGroups()){
					groupNr++;
					for (I_RelTuple relTuple:relTuples){
						DrRelationship loopRel = new DrRelationship();
						loopRel.setModifierUuid("someUuid");
						loopRel.setAuthorUuid(termFactory.nidToUuid(relTuple.getAuthorNid()).toString());
						loopRel.setSourceUuid(termFactory.nidToUuid(relTuple.getSourceNid()).toString());
						loopRel.setTargetUuid(termFactory.nidToUuid(relTuple.getTargetNid()).toString());
						loopRel.setCharacteristicUuid(termFactory.nidToUuid(relTuple.getCharacteristicNid()).toString());
						loopRel.setPathUuid(termFactory.nidToUuid(relTuple.getPathNid()).toString());
						loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
						loopRel.setRelGroup(groupNr);
						loopRel.setStatusUuid(termFactory.nidToUuid(relTuple.getStatusNid()).toString());
						loopRel.setTime(relTuple.getTime());
						loopRel.setTypeUuid(termFactory.nidToUuid(relTuple.getTypeNid()).toString());
						loopRel.setFactContextName(factContextName);
						rels.add(loopRel);
					}
				}
				//Is A's Stated
				List<I_RelTuple> relTuples=(List<I_RelTuple>) getStatedIsARels(conceptData);

				for (I_RelTuple relTuple:relTuples){
					DrRelationship loopRel = new DrRelationship();
					loopRel.setModifierUuid("someUuid");
					loopRel.setAuthorUuid(termFactory.nidToUuid(relTuple.getAuthorNid()).toString());
					loopRel.setSourceUuid(termFactory.nidToUuid(relTuple.getSourceNid()).toString());
					loopRel.setTargetUuid(termFactory.nidToUuid(relTuple.getTargetNid()).toString());
					loopRel.setCharacteristicUuid(termFactory.nidToUuid(relTuple.getCharacteristicNid()).toString());
					loopRel.setPathUuid(termFactory.nidToUuid(relTuple.getPathNid()).toString());
					loopRel.setPrimordialUuid(relTuple.getPrimUuid().toString());
					loopRel.setRelGroup(0);
					loopRel.setStatusUuid(termFactory.nidToUuid(relTuple.getStatusNid()).toString());
					loopRel.setTime(relTuple.getTime());
					loopRel.setTypeUuid(termFactory.nidToUuid(relTuple.getTypeNid()).toString());
					loopRel.setFactContextName(factContextName);
					rels.add(loopRel);
				}
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (ContradictionException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			return rels;

		}

		/**
		 * Gets the mock view set.
		 *
		 * @param config the config
		 * @return the mock view set
		 */
		private static PositionSet getMockViewSet(I_ConfigAceFrame config) {
			I_TermFactory termFactory = Terms.get();
			Set<PositionBI> viewPositions =  new HashSet<PositionBI>();
			try {
				for (PathBI loopPath : config.getEditingPathSet()) {
					PositionBI pos = termFactory.newPosition(loopPath, Long.MAX_VALUE);
					viewPositions.add(pos);
				}
			} catch (TerminologyException e) {
				AceLog.getAppLog().alertAndLogException(e);
			} catch (IOException e) {
				AceLog.getAppLog().alertAndLogException(e);
			}
			PositionSet mockViewSet = new PositionSet(viewPositions);
			return mockViewSet;
		}
}
