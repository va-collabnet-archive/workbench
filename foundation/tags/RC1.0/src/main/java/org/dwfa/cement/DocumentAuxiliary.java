/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.dwfa.cement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ExtendUniversally;
import org.dwfa.tapi.I_ManifestLocally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.tapi.impl.UniversalFixedIntExtension;
import org.dwfa.tapi.impl.UniversalFixedRel;
import org.dwfa.util.id.Type3UuidFactory;

public class DocumentAuxiliary implements I_AddToMemoryTermServer {

	
	public enum Concept implements I_ConceptualizeUniversally {
	DOCUMENT_AUXILIARY(new String[] {"Document Auxiliary Concept"},  
					new I_ConceptualizeUniversally[] { },
					new int[] {0}), 
		DOCUMENT_SECTION(new String[] {"Document Section"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_AUXILIARY }, 
					new int[] {0}), 
			SERVICE(new String[] {"Service:",
					"<font color=#606060><em><b>Service:</b></em></font>"},
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {10}), 
			ID_AND_VITAL_STATS(new String[] {"ID and Vital Stats:", "<font color=#606060><em><b>ID and Vital Stats:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {20}), 
			INFORMANT(new String[] {"Informant:", "<font color=#606060><em><b>Informant:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {30}), 
			REASON_FOR_VISIT(new String[] {"Reason for Visit:", "<font color=#606060><em><b>Reason for Visit:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {40}), 
			CHIEF_COMPLAINTS(new String[] {"Chief Complaints:", "<font color=#606060><em><b>Chief Complaints:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {50}), 
			HISTORY_PRESENT_ILLNESS(new String[] {"History of Present Illness:", "<font color=#606060><em><b>History of Present Illness:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {60}), 
			PAST_HISTORY(new String[] {"Past History:", "<font color=#606060><em><b>Past History:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {70}), 
				PH_GENERAL_HEALTH(new String[] {"General Health:", "<font color=#606060>General Health:</font>"}, 
						new I_ConceptualizeUniversally[] { PAST_HISTORY } , 
						new int[] {0}), 
				PH_INFECTIOUS_DISEASES(new String[] {"Infectious Diseases:", "<font color=#606060>Infectious Diseases:</font>"}, 
						new I_ConceptualizeUniversally[] { PAST_HISTORY }, 
						new int[] {1}), 
				PH_OPERATIONS_AND_INJURIES(new String[] {"Operations and Injuries:", "<font color=#606060>Operations and Injuries:</font>"}, 
						new I_ConceptualizeUniversally[] { PAST_HISTORY }, 
						new int[] {2}), 
				PH_PREVIOUS_HOSPITALIZATIONS(new String[] {"Previous Hospitalizations", "<font color=#606060>Previous Hospitalizations</font>"}, 
						new I_ConceptualizeUniversally[] { PAST_HISTORY }, 
						new int[] {3}), 
				PH_REVIEW_OF_SYSTEMS(new String[] {"Review of Systems:", "<font color=#606060>Review of Systems:</font>"}, 
						new I_ConceptualizeUniversally[] { PAST_HISTORY }, 
						new int[] {4}), 
					ROS_INTEGUMENT(new String[] {"Integument:", "<font size=-1 color=#606060>Integument:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {0}), 
					ROS_LYMPH_NODES(new String[] {"Lymph Nodes:", "<font size=-1 color=#606060>Lymph Nodes:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {1}), 
					ROS_BONES_JOINTS_MUSCLES(new String[] {"Bones, Joints, and Muscles:", "<font size=-1 color=#606060>Bones, Joints, and Muscles:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {2}), 
					ROS_HEMATOPOIETIC_SYSTEM(new String[] {"Hematopoietic:", "<font size=-1 color=#606060>Hematopoietic:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {3}), 
					ROS_ENDOCRINE_SYSTEM(new String[] {"Endocrine:", "<font size=-1 color=#606060>Endocrine:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {4}), 
					ROS_ALLERGIC_AND_IMMUNOLOGICAL(new String[] {"Allergy and Immunology:", "<font size=-1 color=#606060>Allergy and Immunology:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {5}), 
					ROS_HEAD(new String[] {"Head:", "<font size=-1 color=#606060>Head:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {6}), 
					ROS_EYES(new String[] {"Eyes:", "<font size=-1 color=#606060>Eyes:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {7}), 
					ROS_EARS(new String[] {"Ears:", "<font size=-1 color=#606060>Ears:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {8}), 
					ROS_NOSE(new String[] {"Nose:", "<font size=-1 color=#606060>Nose:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {9}), 
					ROS_MOUTH(new String[] {"Mouth:", "<font size=-1 color=#606060>Mouth:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {10}), 
					ROS_THROAT(new String[] {"Throat:", "<font size=-1 color=#606060>Throat:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {11}), 
					ROS_NECK(new String[] {"Neck:", "<font size=-1 color=#606060>Neck:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {12}), 
					ROS_BREASTS(new String[] {"Breasts:", "<font size=-1 color=#606060>Breasts:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {13}), 
					ROS_RESPIRATORY_SYSTEM(new String[] {"Respiratory System:", "<font size=-1 color=#606060>Respiratory System:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {14}), 
					ROS_GENITOURINARY_SYSTEM(new String[] {"Genitourinary System:", "<font size=-1 color=#606060>Genitourinary System:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {15}), 
					ROS_NERVOUS_SYSTEM(new String[] {"Nervous System:", "<font size=-1 color=#606060>Nervous System:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {16}), 
					ROS_PSYCHIATRIC_HISTORY(new String[] {"Psychiatric:", "<font size=-1 color=#606060>Psychiatric:</font>"}, 
							new I_ConceptualizeUniversally[] { PH_REVIEW_OF_SYSTEMS }, 
							new int[] {17}), 
			SOCIAL_HISTORY(new String[] {"Social History:", "<font color=#606060><em><b>Social History:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {80}), 
			HABITS(new String[] {"Habits:", "<font color=#606060><em><b>Habits:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {90}), 
				H_DIET(new String[] {"Diet:", "<font color=#606060>Diet:</font>"}, 
						new I_ConceptualizeUniversally[] { HABITS }, 
						new int[] {0}), 
				H_EXERCISE(new String[] {"Exercise:", "<font color=#606060>Exercise:</font>"},
						new I_ConceptualizeUniversally[] {HABITS}, 
						new int[] {1}), 
				H_ALCOHOL(new String[] {"Alcohol:", "<font color=#606060>Alcohol:</font>"}, 
						new I_ConceptualizeUniversally[] {HABITS}, 
					new int[] {2}), 
				H_TOBACCO(new String[] {"Tobacco:", "<font color=#606060>Tobacco:</font>"}, 
						new I_ConceptualizeUniversally[] {HABITS}, 
						new int[] {3}), 
				H_SLEEP(new String[] {"Sleep:", "<font color=#606060>Sleep:</font>"}, 
						new I_ConceptualizeUniversally[] {HABITS},
						new int[] {4}), 
			FAMILY_HISTORY(new String[] {"Family History:", "<font color=#606060><em><b>Family History:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {100}), 
			PHYSICAL_EXAMINATION(new String[] {"Physical Examination:", "<font color=#606060><em><b>Physical Examination:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {110}), 
				PE_VITALS(new String[] {"Vital Signs:", "<font color=#606060>Vital Signs:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {0}), 
				PE_INTEGUMENT(new String[] {"Integument:", "<font color=#606060>Integument:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {1}), 
				PE_LYMPH_NODES(new String[] {"Lymph Nodes:", "<font color=#606060>Lymph Nodes:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {2}), 
				PE_BONES_JOINTS_MUSCLES(new String[] {"Bones, Joints, and Muscles:", "<font color=#606060>Bones, Joints, and Muscles:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {3}), 
				PE_HEMATOPOIETIC_SYSTEM(new String[] {"Hemapoietic System:", "<font color=#606060>Hemapoietic System:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {4}), 
				PE_ENDOCRINE_SYSTEM(new String[] {"Endocrine System:", "<font color=#606060>Endocrine System:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {5}), 
				PE_ALLERGIC_AND_IMMUNOLOGICAL(new String[] {"Allergy and Immunology:", "<font color=#606060>Allergy and Immunology:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {6}),
				PE_HEAD(new String[] {"Head:", "<font color=#606060>Head:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {7}), 
				PE_EYES(new String[] {"Eyes:", "<font color=#606060>Eyes:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {8}), 
						PE_VISUAL_ACUITY(new String[] {"Visual Acuity:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Visual Acuity:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {10}), 
						PE_REFRACTION(new String[] {"Refraction:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Refraction:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {20}), 
						PE_REFRACTION_ADD(new String[] {"Refraction add:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Refraction add:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {22}), 
						PE_IOP(new String[] {"Intraocular pressure:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Intraocular pressure:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {25}), 
						PE_EYE_EXTERNAL(new String[] {"External:", "<font color=#606060>&nbsp;&nbsp;&nbsp;External:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {30}), 
								PE_EYE_ORBIT(new String[] {"Orbit:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Orbit:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_EXTERNAL }, 
										new int[] {5}), 
								PE_EYE_EYELIDS(new String[] {"Eyelids:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Eyelids:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_EXTERNAL }, 
										new int[] {10}), 
								PE_EYE_LACRIMAL(new String[] {"Lacrimal:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Lacrimal:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_EXTERNAL }, 
										new int[] {20}), 
								PE_EYE_MOTILITY(new String[] {"Motility:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Motility:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_EXTERNAL }, 
										new int[] {30}), 
								PE_PUPILS(new String[] {"Pupils:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Pupils:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_EXTERNAL }, 
										new int[] {40}), 
						PE_VISUAL_FIELD(new String[] {"Visual Fields:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Visual Fields:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {40}), 
						PE_EYE_ANTERIOR_SEGMENT(new String[] {"Anterior Segment:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Anterior Segment:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {80}), 
								PE_CONJUNCTIVA(new String[] {"Conjunctiva:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Conjunctiva:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_ANTERIOR_SEGMENT }, 
										new int[] {5}), 
								PE_CORNEA_TEAR_FILM(new String[] {"Cornea & tear film:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Cornea & tear film:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_ANTERIOR_SEGMENT }, 
										new int[] {10}), 
								PE_ANTERIOR_CHAMBER(new String[] {"Anterior Chamber:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Anterior Chamber:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_ANTERIOR_SEGMENT }, 
										new int[] {20}), 
								PE_IRIS(new String[] {"Iris:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Iris:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_ANTERIOR_SEGMENT }, 
										new int[] {30}), 
								PE_LENS(new String[] {"Lens:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Lens:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_ANTERIOR_SEGMENT }, 
										new int[] {40}), 
						PE_EYE_POSTERIOR_SEGMENT(new String[] {"Posterior Segment:", "<font color=#606060>&nbsp;&nbsp;&nbsp;Posterior Segment:</font>"}, 
								new I_ConceptualizeUniversally[] { PE_EYES }, 
								new int[] {90}), 
								PE_DILATION(new String[] {"Dilation:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Dilation:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_POSTERIOR_SEGMENT }, 
										new int[] {5}), 
								PE_VITREOUS(new String[] {"Vitreous:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Vitreous:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_POSTERIOR_SEGMENT }, 
										new int[] {10}), 
								PE_DISC(new String[] {"Disc:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Disc:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_POSTERIOR_SEGMENT }, 
										new int[] {20}), 
								PE_MACULA(new String[] {"Macula:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Macula:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_POSTERIOR_SEGMENT }, 
										new int[] {30}), 
								PE_VESSELS(new String[] {"Vessels:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Vessels:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_POSTERIOR_SEGMENT }, 
										new int[] {40}), 
								PE_PERIPHERY(new String[] {"Periphery:", "<font color=#606060>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Periphery:</font>"}, 
										new I_ConceptualizeUniversally[] { PE_EYE_POSTERIOR_SEGMENT }, 
										new int[] {50}), 
				PE_EARS(new String[] {"Ears:", "<font color=#606060>Ears:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {9}), 
				PE_NOSE(new String[] {"Nose:", "<font color=#606060>Nose:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {10}), 
				PE_MOUTH(new String[] {"Mouth:", "<font color=#606060>Mouth:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {11}), 
				PE_THROAT(new String[] {"Throat:", "<font color=#606060>Throat:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {12}), 
				PE_NECK(new String[] {"Neck:", "<font color=#606060>Neck:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {13}), 
				PE_BREASTS(new String[] {"Breasts:", "<font color=#606060>Breasts:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {14}), 
				PE_RESPIRATORY_SYSTEM(new String[] {"Respiratory System:", "<font color=#606060>Respiratory System:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {15}), 
				PE_GENITOURINARY_SYSTEM(new String[] {"Genitourinary System:", "<font color=#606060>Genitourinary System:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {16}), 
				PE_NERVOUS_SYSTEM(new String[] {"Nervous System:", "<font color=#606060>Nervous System:</font>"}, 
						new I_ConceptualizeUniversally[] { PHYSICAL_EXAMINATION }, 
						new int[] {17}), 
			LABORATORY(new String[] {"Laboratory:", "<font color=#606060><em><b>Laboratory:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {120}), 
			RADIOLOGY(new String[] {"Radiology:", "<font color=#606060><em><b>Radiology:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {130}), 
			CONSULTATIONS(new String[] {"Consultations:", "<font color=#606060><em><b>Consultations:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {140}), 
				CO_SURGERY(new String[] {"Surgery Consultations:", "<font color=#606060>Surgery Consultations:</font>"}, 
						new I_ConceptualizeUniversally[] { CONSULTATIONS }, 
						new int[] {0}), 
				CO_DERM(new String[] {"Dermatoloty Consultations:", "<font color=#606060>Dermatoloty Consultations:</font>"}, 
						new I_ConceptualizeUniversally[] { CONSULTATIONS }, 
						new int[] {1}), 
				CO_EYE(new String[] {"Eye Consultations:", "<font color=#606060>Eye Consultations:</font>"}, 
						new I_ConceptualizeUniversally[] { CONSULTATIONS }, 
						new int[] {2}), 
			ASSESSMENT(new String[] {"Assessment:", "<font color=#606060><em><b>Assessment:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {150}), 
			INTERVENTIONS(new String[] {"Interventions:", "<font color=#606060><em><b>Interventions:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {155}), 
				EDUCATION(new String[] {"Education:", "<font color=#606060>Education:</font>"}, 
						new I_ConceptualizeUniversally[] { INTERVENTIONS }, 
						new int[] {10}), 
			PLAN(new String[] {"Plan:", "<font color=#606060><em><b>Plan:</b></em></font>"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_SECTION }, 
					new int[] {160}),
					
					
		ASSERTION_CONDITION(new String[] {"Assertion condition"},  
					new I_ConceptualizeUniversally[] { DOCUMENT_AUXILIARY }, 
					new int[] {10}), 
			UNKNOWN(new String[] {"Unknown"},  
					new I_ConceptualizeUniversally[] { ASSERTION_CONDITION }, 
					new int[] {20}), 
				NOT_ASKED(new String[] {"Not asked"},  
						new I_ConceptualizeUniversally[] { UNKNOWN }, 
						new int[] {30}), 
				SOURCE_DOES_NOT_KNOW(new String[] {"Source does not know"},  
						new I_ConceptualizeUniversally[] { UNKNOWN }, 
						new int[] {40}), 
				UNKNOWABLE_TEMPORARY(new String[] {"Temporarily unknowable"},  
						new I_ConceptualizeUniversally[] { UNKNOWN }, 
						new int[] {50}), 
				UNKNOWABLE_PERMANENT(new String[] {"Permanently unknowable"},  
						new I_ConceptualizeUniversally[] { UNKNOWN }, 
						new int[] {60}), 
			KNOWN(new String[] {"Known"},  
					new I_ConceptualizeUniversally[] { ASSERTION_CONDITION }, 
					new int[] {70}), 
				DEFAULT_KNOWN(new String[] {"Default value"},  
						new I_ConceptualizeUniversally[] { KNOWN }, 
						new int[] {80}), 
				CONFIRMED_KNOWN(new String[] {"Confirmed default"},  
						new I_ConceptualizeUniversally[] { KNOWN }, 
						new int[] {90}), 
				ORIGINAL_VALUE(new String[] {"Original value"},  
						new I_ConceptualizeUniversally[] { KNOWN }, 
						new int[] {100}), 
;

		private Collection<UUID> conceptUids = new ArrayList<UUID>();
		
		private Boolean primitive = true;
		
		private UniversalFixedRel[] rels;
		
		private UniversalFixedDescription[] descriptions;
						
		private Concept(String[] descriptionStrings, I_ConceptualizeUniversally[] parents,
				int[] relOrder) {
			this.conceptUids.add(Type3UuidFactory.fromEnum(this)); 
			try {
				this.rels = makeRels(this, parents);
				int i = 0;
				for (UniversalFixedRel r: rels) {
					int order = relOrder[i++];
					UniversalFixedIntExtension ext = new UniversalFixedIntExtension(Type3UuidFactory.forExtension(r, 
							RefsetAuxiliary.Concept.INT_EXTENSION, RefsetAuxiliary.Concept.DOCUMENT_SECTION_ORDER), order);
					HashMap<I_ConceptualizeUniversally, I_ExtendUniversally> extensionsForComponent = new HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>();
					extensionsForComponent.put(RefsetAuxiliary.Concept.DOCUMENT_SECTION_ORDER, ext);
					extensions.put(r, extensionsForComponent);
				}
				this.descriptions = makeDescriptions(this, descriptionStrings, descTypeOrder);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		public boolean isPrimitive(I_StoreUniversalFixedTerminology server) {
			return true;
		}


		public Collection<UUID> getUids() {
			return conceptUids;
		}

		public boolean isUniversal() {
			return true;
		}

		public I_ManifestLocally localize(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}


		public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}

		public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_ConceptualizeUniversally> getDestRelConcepts(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_ConceptualizeUniversally> getDestRelConcepts(
				Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_RelateConceptsUniversally> getDestRels(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_RelateConceptsUniversally> getSourceRels(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(I_StoreUniversalFixedTerminology server) {
			throw new UnsupportedOperationException();
		}



		public Collection<I_ConceptualizeUniversally> getSrcRelConcepts(
				Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}



		public I_ManifestUniversally getExtension(I_ConceptualizeUniversally extensionType, I_StoreUniversalFixedTerminology extensionServer) {
			throw new UnsupportedOperationException();
		}

		public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}
		public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
			throw new UnsupportedOperationException();
		}


		public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
			return LocalFixedConcept.get(getUids(), primitive);
		}
	}	
	
	private static HashMap<I_ManifestUniversally, HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>> extensions = new HashMap<I_ManifestUniversally, HashMap<I_ConceptualizeUniversally,I_ExtendUniversally>>();

	/* (non-Javadoc)
	 * @see org.dwfa.cement.I_AddToMemoryTermServer#addToMemoryTermServer(org.dwfa.cement.MemoryTermServer)
	 */
	public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
		server.addRoot(Concept.DOCUMENT_AUXILIARY);
		for (Concept s: Concept.values()) {
			server.add(s);
			for (I_DescribeConceptUniversally d: s.descriptions) {
				server.add(d);
			}
			for (I_RelateConceptsUniversally r: s.rels) {
				server.add(r);
			}
		}
		for (Map.Entry<I_ManifestUniversally, HashMap<I_ConceptualizeUniversally, I_ExtendUniversally>> componentAndExtensions: extensions.entrySet()) {
			for (Map.Entry<I_ConceptualizeUniversally, I_ExtendUniversally> extTypeAndExt: componentAndExtensions.getValue().entrySet()) {
				server.addExtension(componentAndExtensions.getKey().localize(), 
						(I_ConceptualizeLocally) extTypeAndExt.getKey().localize(), 
						extTypeAndExt.getValue().localize());
			}
		}
	}
	
	public static UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents) throws Exception {
		I_ConceptualizeUniversally[] relTypes = new I_ConceptualizeUniversally[parents.length];
		Arrays.fill(relTypes, ArchitectonicAuxiliary.Concept.IS_A_REL);
		return makeRels(source, parents, relTypes);
	}

	public static UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents, I_ConceptualizeUniversally[] relTypes) throws Exception {
		UniversalFixedRel[] rels = new UniversalFixedRel[parents.length];
		int i = 0;
		for (I_ConceptualizeUniversally p: parents) {
			int relGrp = 0;
			int parentIndex = i++;
			Collection<UUID> relUids = Type3UuidFactory.forRel(source.getUids(), 
					relTypes[parentIndex].getUids(), p.getUids());
			rels[parentIndex] = new UniversalFixedRel(relUids, 
					source.getUids(),
					relTypes[parentIndex].getUids(), p.getUids(),
					ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids(),
					ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids(), relGrp);
		}
		return rels;
	}
	static List<I_ConceptualizeLocally> localDescTypeOrder;
	public static List<I_ConceptualizeLocally> getDescTypeOrder() throws IOException, TerminologyException {
		if (localDescTypeOrder == null) {
			localDescTypeOrder = new ArrayList<I_ConceptualizeLocally>();
			localDescTypeOrder.add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize());
			localDescTypeOrder.add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize());
		}
		return localDescTypeOrder;
	}
	private static I_ConceptualizeUniversally[] descTypeOrder = { ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE, ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE};
	public static UniversalFixedDescription[] makeDescriptions(I_ConceptualizeUniversally source, String[] descriptionStrings, I_ConceptualizeUniversally[] descTypeOrder) throws Exception {
		UniversalFixedDescription[] descriptions = new UniversalFixedDescription[descriptionStrings.length];
		int i = 0;
		boolean initialCapSig = true;
		String langCode = "en";
		for (String descText: descriptionStrings) {
			if (descText != null) {
				descriptions[i] = new UniversalFixedDescription(Type3UuidFactory.forDesc(source.getUids(), descTypeOrder[i].getUids(), descText),
						ArchitectonicAuxiliary.Concept.CURRENT.getUids(), 
						source.getUids(),
						initialCapSig, descTypeOrder[i].getUids(), descText,
						langCode);
			}
			i++;
		}
		return descriptions;
	}

}
