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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.dwfa.tapi.I_ConceptualizeLocally;
import org.dwfa.tapi.I_ConceptualizeUniversally;
import org.dwfa.tapi.I_DescribeConceptLocally;
import org.dwfa.tapi.I_DescribeConceptUniversally;
import org.dwfa.tapi.I_ManifestUniversally;
import org.dwfa.tapi.I_RelateConceptsUniversally;
import org.dwfa.tapi.I_StoreLocalFixedTerminology;
import org.dwfa.tapi.I_StoreUniversalFixedTerminology;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.tapi.impl.LocalFixedConcept;
import org.dwfa.tapi.impl.LocalFixedTerminology;
import org.dwfa.tapi.impl.MemoryTermServer;
import org.dwfa.tapi.impl.UniversalFixedDescription;
import org.dwfa.tapi.impl.UniversalFixedRel;
import org.dwfa.util.id.Type3UuidFactory;

public class ArchitectonicAuxiliary implements I_AddToMemoryTermServer {
    public static final UUID NAME_SPACE = UUID.fromString("d0cb73c0-aaf7-11db-8294-0002a5d5c51b");

    private static String getArchitectonicText() {
        StringBuffer b = new StringBuffer();

        b.append("<html>The subject matter of <font color=blue>architectonic</font> is the structure of all human knowledge. ");
        b.append("The purpose of providing an architectonic scheme is to classify different types of knowledge and explain ");
        b.append("the relationships that exist between these classifications. Peirce's own architectonic system divides ");
        b.append("knowledge according to it status as a \"science\" and then explains the interrelation of these different ");
        b.append("scientific disciplines. His belief was that philosophy must be placed within this systematic account of ");
        b.append("knowledge as science. Peirce adopts his architectonic ambitions of structuring all knowledge, and ");
        b.append("organizing philosophy within it, from his great philosophical hero, Kant. This systematizing approach ");
        b.append("became crucial for Peirce in his later work. However, his belief in a structured philosophy related ");
        b.append("systematically to all other scientific disciplines was important to him throughout his philosophical ");
        b.append("life.");
        b.append("<center><img src='ace:1c4214ec-147a-11db-ac5d-0800200c9a66$f4d2fabc-7e96-3b3a-a348-ae867ba74029'></center><br>");
        b.append("Source:  <a href='http://www.iep.utm.edu/p/PeirceAr.htm'>The Internet Encyclopedia of Philosophy</a>");
        b.append("</html>");
        return b.toString();
    }


    public enum Concept implements I_ConceptEnumeration,I_ConceptualizeUniversally {

        ARCHITECTONIC_ROOT_CONCEPT("Terminology Auxiliary concept", getArchitectonicText(),
            new I_ConceptualizeUniversally[] { }),
        USER_ROLE("user role",
                   new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
             OWNER_ROLE("owner role",
                    new I_ConceptualizeUniversally[] { USER_ROLE }),
             AUTHOR_ROLE("author role",
                    new I_ConceptualizeUniversally[] { USER_ROLE }),
             REVIEWER_ROLE("reviewer role",
                    new I_ConceptualizeUniversally[] { USER_ROLE }),
             SME_ROLE("subject matter expert role", "SME role",
                    new I_ConceptualizeUniversally[] { USER_ROLE }),
             ADMIN_ROLE("administrator role", "admin role",
                    new I_ConceptualizeUniversally[] { USER_ROLE }),




        USER_PERMISSION("user permission",
                    new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
              ADD_USER("add user permission",
                    new I_ConceptualizeUniversally[] { USER_PERMISSION }),
              UNIVERSAL_EDIT("universal edit permission",
                    new I_ConceptualizeUniversally[] { USER_PERMISSION }),
              UNIVERSAL_VIEW("universal view permission",
                    new I_ConceptualizeUniversally[] { USER_PERMISSION }),
        USER("user",
              new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
              KEITH_CAMPBELL(new String[] {"Keith Campbell (Informatics, Inc.)", "KEC"}, null,
                      new I_ConceptualizeUniversally[] { USER }),
              SUSAN_CASTILLO(new String[] {"Susan Castillo (Informatics, Inc.)", "SC"}, null,
                      new I_ConceptualizeUniversally[] { USER }),
        TUPLE_TYPE("tuple type",
                    new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
                    ID_TUPLE(new String[] {"id tuple", "id tuple"}, null,
                            new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    CON_TUPLE(new String[] {"concept tuple", "concept tuple"}, null,
                                  new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    DESC_TUPLE(new String[] {"description tuple", "description tuple"}, null,
                                  new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    REL_TUPLE(new String[] {"relationship tuple", "relationship tuple"}, null,
                                  new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_CONCEPT_CONCEPT_TUPLE(new String[] {"concept concept ext tuple", "concept concept ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_CONCEPT_CONCEPT_CONCEPT_TUPLE(new String[] {"concept concept concept ext tuple", "concept concept concept ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_CONCEPT_CONCEPT_STRING_TUPLE(new String[] {"concept concept string ext tuple", "concept concept string ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_CONCEPT_STRING_TUPLE(new String[] {"concept string ext tuple", "concept string ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_CONCEPT_TUPLE(new String[] {"concept ext tuple", "concept ext tuple"}, null,
                                     new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_INT_TUPLE(new String[] {"int ext tuple", "int ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_BOOLEAN_TUPLE(new String[] {"boolean ext tuple", "boolean ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
                    EXT_STRING_TUPLE(new String[] {"string ext tuple", "string ext tuple"}, null,
                             new I_ConceptualizeUniversally[] { TUPLE_TYPE }),
        DEFINITION_TYPE("definition type",
               new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
               PRIMITIVE_DEFINITION(new String[] {"necessary but not sufficient", "primitive"}, null,
                             new I_ConceptualizeUniversally[] { DEFINITION_TYPE }),
               DEFINED_DEFINITION(new String[] {"necessary and sufficient definition", "defined"}, null,
                               new I_ConceptualizeUniversally[] { DEFINITION_TYPE }),
        IMAGE_TYPE("image type",
                new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            AUXILLARY_IMAGE("auxiliary image",
                    new I_ConceptualizeUniversally[] { IMAGE_TYPE }),
            VIEWER_IMAGE("viewer image",
                    new I_ConceptualizeUniversally[] { IMAGE_TYPE }),
      LANGUAGE_CONCEPT("language concept",
               new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
               CASE_SENSITIVITY(new String[] {"case sensitivity (language concept)","case sensitivity"}, null,
                       new I_ConceptualizeUniversally[] { LANGUAGE_CONCEPT }) ,
                       INITIAL_CHARACTER_NOT_CASE_SENSITIVE(new String[] {"initial character not case sensitive (case sensitivity)","ic changable"}, null,
                                                        new I_ConceptualizeUniversally[] { CASE_SENSITIVITY }) ,
                       ALL_CHARACTERS_CASE_SENSITIVE(new String[] {"all characters case sensitive (language concept)","unchangable"}, null,
                                                        new I_ConceptualizeUniversally[] { CASE_SENSITIVITY }) ,
               LANGUAGE_SPECIFICATION(new String[] {"language specification (language concept)","language"}, null,
                       new I_ConceptualizeUniversally[] { LANGUAGE_CONCEPT }) ,
                       LIT(new String[] {"Language-independent token (language concept)","Language-independent token"}, null,
  	                          new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
                       EN(new String[] {"English (language concept)","English"}, null,
 	                          new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
	                       EN_BZ(new String[] {"Belize English (language concept)","English-Belize"}, null,
	                          new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_TT(new String[] {"Trinidad English (language concept)","English-Trinidad"}, null,
	                          new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_AU(new String[] {"Australian English (language concept)","English-Australian"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_CA(new String[] {"Canadian English (language concept)","English-Canadian"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_US(new String[] {"United States English (language concept)","English-United States"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_GB(new String[] {"United Kingdom English (language concept)","English-United Kingdom"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_NZ(new String[] {"New Zealand English (language concept)","English-New Zealand"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_IE(new String[] {"Ireland English (language concept)","English-Ireland"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_ZA(new String[] {"South Africa English (language concept)","English-South Africa"}, null,
	                             new I_ConceptualizeUniversally[] { EN }) ,
	                       EN_JM(new String[] {"Jamica English (language concept)","English-Jamica"}, null,
	                                       new I_ConceptualizeUniversally[] { EN }) ,
                       ES(new String[] {"Spanish (language concept)","Spanish"}, null,
                    	 	    new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
                        	ES_AR(new String[] {"Spanish-Argentina (language concept)","Spanish-Argentina"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_BO(new String[] {"Spanish-Bolivia (language concept)","Spanish-Bolivia"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_CL(new String[] {"Spanish-Chile (language concept)","Spanish-Chile"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_CO(new String[] {"Spanish-Colombia (language concept)","Spanish-Colombia"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_CR(new String[] {"Spanish-Costa Rica (language concept)","Spanish-Costa Rica"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_DO(new String[] {"Spanish-Dominican Republic (language concept)","Spanish-Dominican Republic"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_EC(new String[] {"Spanish-Ecuador (language concept)","Spanish-Ecuador"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_ES(new String[] {"Spanish-Spain (language concept)","Spanish-Spain"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_GT(new String[] {"Spanish-Guatemala (language concept)","Spanish-Guatemala"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_HN(new String[] {"Spanish-Honduras (language concept)","Spanish-Honduras"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_MX(new String[] {"Spanish-Mexico (language concept)","Spanish-Mexico"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_NI(new String[] {"Spanish-Nicaragua (language concept)","Spanish-Nicaragua"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_PA(new String[] {"Spanish-Panama (language concept)","Spanish-Panama"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_PE(new String[] {"Spanish-Peru (language concept)","Spanish-Peru"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_PR(new String[] {"Spanish-Puerto Rico (language concept)","Spanish-Puerto Rico"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_PY(new String[] {"Spanish-Paraguay (language concept)","Spanish-Paraguay"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_SV(new String[] {"Spanish-El Salvador (language concept)","Spanish-El Salvador"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_UY(new String[] {"Spanish-Uruguay (language concept)","Spanish-Uruguay"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                        	ES_VE(new String[] {"Spanish-Venezuela (language concept)","Spanish-Venezuela"}, null,
                        	 	    new I_ConceptualizeUniversally[] { ES }) ,
                	 	   FR(new String[] {"French (language concept)","French"}, null,
                	 		 	    new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
	                	 		FR_BE(new String[] {"French-Belgium (language concept)","French-Belgium"}, null,
	                	 		 	    new I_ConceptualizeUniversally[] { FR }) ,
	                	 		FR_CA(new String[] {"French-Canadian (language concept)","French-Canadian"}, null,
	                	 		 	    new I_ConceptualizeUniversally[] { FR }) ,
	                	 		FR_FR(new String[] {"French-France (language concept)","French-France"}, null,
	                	 		 	    new I_ConceptualizeUniversally[] { FR }) ,
	                	 		FR_CH(new String[] {"French-Switzerland (language concept)","French-Switzerland"}, null,
	                	 		 	    new I_ConceptualizeUniversally[] { FR }) ,
	                	 		FR_LU(new String[] {"French-Luxembourg (language concept)","French-Luxembourg"}, null,
	                	 		 	    new I_ConceptualizeUniversally[] { FR }) ,
	                	 		FR_MC(new String[] {"French-Principality of Monaco (language concept)","French-Principality of Monaco"}, null,
	                	 		 	    new I_ConceptualizeUniversally[] { FR }) ,

                	 		DA(new String[] {"Danish (language concept)","Danish"}, null,
                	 		 	    new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
                    	 		DA_DK(new String[] {"Danish-Denmark (language concept)","Danish-Denmark"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { DA }) ,

                	 		SV(new String[] {"Swedish (language concept)","Swedish"}, null,
                	 		 	    new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
                    	 		SV_FI(new String[] {"Swedish-Finland (language concept)","Swedish-Finland"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { SV }) ,
                    	 		SV_SE(new String[] {"Swedish-Sweden (language concept)","Swedish-Sweden"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { SV }) ,

                	 		LT(new String[] {"Lithuanian (language concept)","Lithuanian"}, null,
                	 		 	    new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
                    	 		LT_LT(new String[] {"Lithuanian-Lithuania (language concept)","Lithuanian-Lithuania"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { LT }) ,

                	 		ZH(new String[] {"Chinese (language concept)","Chinese"}, null,
                	 		 	    new I_ConceptualizeUniversally[] { LANGUAGE_SPECIFICATION }) ,
                    	 		ZH_CN(new String[] {"Chinese-People's Republic of China (language concept)","Chinese-People's Republic of China"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,
                    	 		ZH_HK(new String[] {"Chinese-Hong Kong S.A.R. (language concept)","Chinese-Hong Kong S.A.R."}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,
                    	 		ZH_CHS(new String[] {"Chinese-Simplified (language concept)","Chinese-Simplified"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,
                    	 		ZH_CHT(new String[] {"Chinese-Traditional (language concept)","Chinese-Traditional"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,
                    	 		ZH_MO(new String[] {"Chinese-Macao S.A.R. (language concept)","Chinese-Macao S.A.R."}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,
                    	 		ZH_SG(new String[] {"Chinese-Singapore (language concept)","Chinese-Singapore"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,
                    	 		ZH_TW(new String[] {"Chinese-Taiwan (language concept)","Chinese-Taiwan"}, null,
                    	 		 	    new I_ConceptualizeUniversally[] { ZH }) ,


                       /*
                        */
               DESCRIPTION_FORM("description form",
                     new I_ConceptualizeUniversally[] { LANGUAGE_CONCEPT }),
                     UNNABREV_SINGULAR("unabbreviated singular form",
                           new I_ConceptualizeUniversally[] { DESCRIPTION_FORM }),
                     PLURAL_FORM("plural form",
                           new I_ConceptualizeUniversally[] { DESCRIPTION_FORM }),
                     INCOMPLETE_OR_ABBREVIATED("incomplete or abbreviated form",
                           new I_ConceptualizeUniversally[] { DESCRIPTION_FORM }),
                     SYMBOLIC_FORM("symbolic form",
                           new I_ConceptualizeUniversally[] { DESCRIPTION_FORM }),
               DEGREE_OF_SYNONOMY("degree of synonomy",
                     new I_ConceptualizeUniversally[] { LANGUAGE_CONCEPT }),
                     SYNONYMOUS("synonymous",
                           new I_ConceptualizeUniversally[] { DEGREE_OF_SYNONOMY, }),
                     NEAR_SYNONYMOUS("near synonymous (depending on context of use)",
                           new I_ConceptualizeUniversally[] { DEGREE_OF_SYNONOMY, }),
                     NON_SYNONYMOUS("non synonymous",
                           new I_ConceptualizeUniversally[] { DEGREE_OF_SYNONOMY, }),
               CORRECTNESS("correctness",
                     new I_ConceptualizeUniversally[] { LANGUAGE_CONCEPT }),
                     RECOMMENDED("recommended",
                           new I_ConceptualizeUniversally[] { CORRECTNESS, }),
                     INCORRECT("incorrect",
                           new I_ConceptualizeUniversally[] { CORRECTNESS, }),
               ACCEPTABILITY("acceptability",
                     new I_ConceptualizeUniversally[] { LANGUAGE_CONCEPT }),
                     NOT_SPECIFIED("not specified",
                           new I_ConceptualizeUniversally[] { ACCEPTABILITY, CORRECTNESS, DEGREE_OF_SYNONOMY}),
                     INVALID("invalid",
                           new I_ConceptualizeUniversally[] { ACCEPTABILITY, CORRECTNESS, DEGREE_OF_SYNONOMY}),
                     ACCEPTABLE("acceptable",
                           new I_ConceptualizeUniversally[] { ACCEPTABILITY, CORRECTNESS, }),
                     NOT_RECOMMENDED("not recommended",
                           new I_ConceptualizeUniversally[] { ACCEPTABILITY, CORRECTNESS, }),
                     NOT_ACCEPTABLE("not acceptable",
                           new I_ConceptualizeUniversally[] { ACCEPTABILITY }),
              DESCRIPTION_TYPE("description type",
                      new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
                  EXTERNAL_REFERENCE(new String[] {"external reference (description type)", "external reference"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_XHTML_DESC(new String[] {"read XHMTL desc (description type)", "read XHTML desc"}, null,
                                new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  READ_SYN_XHTML_DESC(new String[] {"read synonym XHMTL desc (description type)", "read synonym XHMTL desc"}, null,
                                new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_198_DESC(new String[] {"read 198 desc (description type)", "read 198 desc"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_60_DESC(new String[] {"read 60 desc (description type)", "read 60 desc"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_30_DESC(new String[] {"read 30 desc (description type)", "read 30 desc"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_SYN_198_DESC(new String[] {"synonym read 198 description (description type)", "synonym read 198 description"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_SYN_60_DESC(new String[] {"synonym read 60 description (description type)", "synonym read 60 description"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_SYN_30_DESC(new String[] {"synonym read 30 description (description type)", "synonym read 30 description"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),

                  READ_KEY_DESC(new String[] {"read key (description type)", "read key"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  READ_OPCS_20_DESC(new String[] {"read opcs code (description type)", "read opcs"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  READ_ICD10_20_DESC(new String[] {"read icd10 (description type)", "read icd10"}, null,
                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                        
                 ICD10_CODE_PLUS_RUBRIC_DESCRIPTION_TYPE(new String[] {"ICD10 Code plus Rubric", "ICD10 Code plus Rubric"}, null,
                                new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }), 
                 ICD10_EXCLUSION_DESCRIPTION_TYPE(new String[] {"ICD10 Exclusion", "ICD10 Exclusion"}, null,
                                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),                  
                 ICD10_INCLUSION_DESCRIPTION_TYPE(new String[] {"ICD10 Inclusion)", "ICD10 Inclusion"}, null,
                                                new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),  
                 ICD10_PREFERRED_DESCRIPTION_TYPE(new String[] {"ICD10 Preferred Description", "ICD10 Preferred"}, null,
                                                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }), 
                                                        
                 FULLY_SPECIFIED_DESCRIPTION_TYPE(PrimordialId.FULLY_SPECIFIED_DESCRIPTION_TYPE_ID,
                                                        new String[] {"fully specified name (description type)", "fully specified name"}, null,
                                                        new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  SYNONYM_DESCRIPTION_TYPE(new String[] {"synonym (description type)", "synonym"}, null,
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  UNSPECIFIED_DESCRIPTION_TYPE("unspecified  (description type)",
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  PREFERRED_DESCRIPTION_TYPE(PrimordialId.PREFERED_TERM_ID, new String[] {"preferred term (description type)", "preferred term"}, null,
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE,  ACCEPTABILITY}),
                  ENTRY_DESCRIPTION_TYPE("entry term (description type)",
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  XHTML_PREFERRED_DESC_TYPE("xhtml preferred (description type)",
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  XHTML_SYNONYM_DESC_TYPE("xhtml synonym (description type)",
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  XHTML_FULLY_SPECIFIED_DESC_TYPE("xhtml fully specified (description type)",
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  XHTML_DEF(PrimordialId.XHTML_DEF_ID, new String[] {"xhtml def (description type)"}, null,
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  EXTENSION_TABLE(PrimordialId.EXTENSION_TABLE_ID, new String[] {"extension table (description type)"}, null,
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
                  CHANGE_COMMENT("change comment (description type)",
                          new I_ConceptualizeUniversally[] { DESCRIPTION_TYPE }),
        RELATIONSHIP("relationship",
                new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            IS_A_REL(PrimordialId.IS_A_REL_ID, new String[] {"is a (relationship type)"}, null,
                    new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_SAME_AS_REL("is same as (relationship type)",
                     new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_ANALOG("is an analog of (relationship type)",
                             new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_READ_V2_V3_ANALOG("is a Read V2 analog of Read V3(relationship type)",
                       		new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_READ_V2_ICD10_ANALOG("is a Read V2 analog of ICD10(relationship type)",
                               		new I_ConceptualizeUniversally[] { RELATIONSHIP }),           		
            IS_READ_4B_V3_ANALOG("is a Read 4Byte analog of Read V3 (relationship type)",
                           new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_SNOMED_READ_V3_ANALOG("is a Read V3 analog of SNOMED(relationship type)",
                              		new I_ConceptualizeUniversally[] { RELATIONSHIP }),               
            IS_TERM_OF("is a Term of (relationship type)",
                                   new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_TERM_OF_SYN("is a Synonym Term of (relationship type)",
                        new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_READ_V3_TEMPLATE("is a Read V3 Template (relationship type)",
                        new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_ALIAS_OF("is alias of (relationship type)",
                     new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            IS_ICD10_SUBCLASS("ICD10 Subclass of",
                             new I_ConceptualizeUniversally[] { RELATIONSHIP }), 
            IS_ICD10_EXCLUSION("ICD10 exclusion relationship",
                                     new I_ConceptualizeUniversally[] { RELATIONSHIP }),              
            DUP_REL_TYPE("dup rel type (terminology constant)",
                    new I_ConceptualizeUniversally[] { RELATIONSHIP }),        
            IS_POT_DUP_REL("is-a potential duplicate rel (terminology constant)",
                    new I_ConceptualizeUniversally[] { DUP_REL_TYPE }),
            IS_NOT_A_DUP_REL("is NOT a dup rel (terminology constant)",
                    new I_ConceptualizeUniversally[] { DUP_REL_TYPE }),
            IS_A_DUP_REL("is-a dup rel (terminology constant)",
                    new I_ConceptualizeUniversally[] { DUP_REL_TYPE }),               
            GRANT_RELATIONSHIP("Grant relationship (user relationships)",
                    new I_ConceptualizeUniversally[] { RELATIONSHIP }),
                    GRANTED_ROLE("Granted role",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP }),
                    GRANTED_PERMISSION("Granted permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP }),



                    MODIFY_CONFIG_PERMISSION("Modify configuration settings permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, SME_ROLE, REVIEWER_ROLE, AUTHOR_ROLE, OWNER_ROLE }),
                    CREATE_DELETE_USERS_PERMISSION("Create and delete users permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    CREATE_NEW_PATH_PERMISSION("Create new path permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    IMPORT_DATA_PERMISSION("Import data permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    REFSET_BRANCH_PERMISSION("Refset branch permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    CREATE_WORKPACKAGE_PERMISSION("Create workpackage permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, OWNER_ROLE }),
                    PICK_UP_WORKPACKAGE_PERMISSION("Pick up workpackage permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, SME_ROLE, REVIEWER_ROLE, AUTHOR_ROLE }),
                    REQUEST_APPROVAL_PERMISSION("Request approval permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, AUTHOR_ROLE, OWNER_ROLE }),
                    SEND_FOR_REVIEW_PERMISSION("Send for review permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, OWNER_ROLE }),
                    ACCEPT_REFSET_PERMISSION("Assign accepted status to entire refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, OWNER_ROLE }),
                    RETURN_REVIEWED_REFSET_PERMISSION("Return reviewed refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, SME_ROLE, REVIEWER_ROLE }),
                    PUBLISH_REFSET_PERMISSION("Publish refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    EXPORT_REFSET_PERMISSION("Export refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, AUTHOR_ROLE, OWNER_ROLE }),
                    EXPORT_OTHER_REFSET_PERMISSION("Export other user refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, OWNER_ROLE }),
                    CREATE_NEW_REFSET_PERMISSION("Create new refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, AUTHOR_ROLE, OWNER_ROLE }),
                    MODIFY_REFSET_MEMBERS_PERMISSION("Modify refset members permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, AUTHOR_ROLE, OWNER_ROLE }),
                    COMMENT_REFSET_MEMBERS_PERMISSION("Comment on refset members permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, SME_ROLE, REVIEWER_ROLE}),
                    MODIFY_REFSET_METADATA_PERMISSION("Modify existing refset metadata permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, AUTHOR_ROLE, OWNER_ROLE }),
                    EDIT_REFSET("Edit refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, AUTHOR_ROLE }),
                    IMPORT_BRANCHED_REFSET_PERMISSION("Import refset from another branch permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    DEPRECATE_REFSET_PERMISSION("Deprecate refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),
                    UPVERSION_REFSET_PERMISSION("Upversion refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE, OWNER_ROLE }),
                    EXECUTE_OTHER_REFSET_PERMISSION("Execute other user refset permission",
                          new I_ConceptualizeUniversally[] { GRANT_RELATIONSHIP, ADMIN_ROLE }),

            ALLOWED_QUALIFIER_REL("allowed qualifier",
                    new I_ConceptualizeUniversally[] { RELATIONSHIP }),
            MAPPING_REL("mapping relationship",
                    new I_ConceptualizeUniversally[] { RELATIONSHIP }),
                CLASSIFIED_SPECIFIC_REIMB_REL("classified, specific for reimbursement",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
                CLASSIFIED_NOT_SPECIFIC_REIMB_REL("classified, NOT specific for reimbursement",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
                NOT_VALID_AS_PRIMARY_REL("not valid as primary",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
                NOT_VALID_WITHOUT_ADDITIONAL_CODES_REL("not valid without additional codes",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
                PC_NEEDED_SPECIFIC_REIMB_REL("patient characteristics needed to classify, specific for reimbursement",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
                PC_NEEDED_SPECIFIC_NOT_REIMB_REL("patient characteristics needed to classify, NOT specific for reimbursement",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
                NO_MAPPING_REL("no mapping possible",
                        new I_ConceptualizeUniversally[] { MAPPING_REL }),
        CHARACTERISTIC_TYPE("characteristic type",
                new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            DEFINING_CHARACTERISTIC(PrimordialId.DEFINING_CHARACTERISTIC_ID,
                    new String[] {"defining (characteristic type)"}, null,
                    new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
                 STATED_RELATIONSHIP(PrimordialId.STATED_CHARACTERISTIC_ID,
                		 new String[] {"stated (defining characteristic type)"}, null,
                		 new I_ConceptualizeUniversally[] { DEFINING_CHARACTERISTIC }),
                 INFERRED_RELATIONSHIP(PrimordialId.INFERRED_CHARACTERISTIC_ID,
                		 new String[] {"inferred (defining characteristic type)"}, null,
                		 new I_ConceptualizeUniversally[] { DEFINING_CHARACTERISTIC }),
                         STATED_AND_INFERRED_RELATIONSHIP("stated & inferred (defining characteristic type)",
                         		new I_ConceptualizeUniversally[] { INFERRED_RELATIONSHIP,  STATED_RELATIONSHIP}),
                         STATED_AND_SUBSUMED_RELATIONSHIP("subsumed (defining characteristic type)",
                                 new I_ConceptualizeUniversally[] { INFERRED_RELATIONSHIP,  STATED_RELATIONSHIP }),
            QUALIFIER_CHARACTERISTIC("qualifier (characteristic type)",
                    new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
            HISTORICAL_CHARACTERISTIC("historical (characteristic type)",
                    new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
            ADDITIONAL_CHARACTERISTIC("additional (characteristic type)",
                    new I_ConceptualizeUniversally[] { CHARACTERISTIC_TYPE }),
        RELATIONSHIP_REFINABILITY("relationship refinability",
                new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            NOT_REFINABLE(PrimordialId.NOT_REFINABLE_ID, new String[] {"not refinable (refinability type)"}, null,
                    new I_ConceptualizeUniversally[] { RELATIONSHIP_REFINABILITY }),
            OPTIONAL_REFINABILITY("optional (refinability type)",
                    new I_ConceptualizeUniversally[] { RELATIONSHIP_REFINABILITY }),
            MANDATORY_REFINABILITY("mandatory (refinability type)",
                    new I_ConceptualizeUniversally[] { RELATIONSHIP_REFINABILITY }),
        STATUS("status (status type)",
                new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            ACTIVE("active (active status type)",
                    new I_ConceptualizeUniversally[] { STATUS }),
                    IN_DEVELOPMENT("in_development (active status type)",
                        new I_ConceptualizeUniversally[] { ACTIVE }),
                    NOT_EDITABLE("not_editable (active status type)",
                        new I_ConceptualizeUniversally[] { ACTIVE }),
                    IN_REVIEW("in_review (active status type)",
                        new I_ConceptualizeUniversally[] { ACTIVE }),
                    COMPLETED("completed (active status type)",
                        new I_ConceptualizeUniversally[] { ACTIVE }),
                    PENDING_MOVE("pending move (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    CONCEPT_RETIRED("concept retired (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    LIMITED("limited (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    CURRENT(PrimordialId.CURRENT_ID, new String[] { "current (active status type)" }, null,
                                    new I_ConceptualizeUniversally[] { ACTIVE }),
                    FLAGGED_FOR_REVIEW("flagged (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    FLAGGED_FOR_DUAL_REVIEW("flagged for dual review (active status type)",
                                    new I_ConceptualizeUniversally[] { ACTIVE }),
                    DUAL_REVIEWED("dual reviewed (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    RESOLVED_IN_DUAL("resolved in dual (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    ADJUDICATED("adjudicated (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    READY_TO_PROMOTE("ready to promote (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    PROMOTED("promoted (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),

                    OPTIONAL("optional (active status type)",
                             new I_ConceptualizeUniversally[] { ACTIVE }),
                    DEVELOPMENTAL("developmental (active status type)",
                                     new I_ConceptualizeUniversally[] { ACTIVE }),
                    EXPERIMENTAL("experimental (active status type)",
                                             new I_ConceptualizeUniversally[] { ACTIVE }),
                    FROM_SNOMED("from snomed (active status type)",
                                                     new I_ConceptualizeUniversally[] { ACTIVE }),


                            REASSIGNED("reassigned (active status type)",
                     new I_ConceptualizeUniversally[] { ACTIVE }),
                     DUAL_REVIEWED_AND_REASSIGNED("dual reviewed and reassigned (active status type)",
                           new I_ConceptualizeUniversally[] { REASSIGNED }),
                     RESOLVED_IN_DUAL_AND_REASSIGNED("resolved in dual and reassigned (active status type)",
                           new I_ConceptualizeUniversally[] { REASSIGNED }),
                    PROCESSED("processed (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                     DUAL_REVIEWED_AND_PROCESSED("dual reviewed and processed (active status type)",
                           new I_ConceptualizeUniversally[] { PROCESSED }),
                     RESOLVED_IN_DUAL_AND_PROCESSED("resolved in dual and processed (active status type)",
                           new I_ConceptualizeUniversally[] { PROCESSED }),
                     ADJUDICATED_AND_PROCESSED("adjudicated and processed (active status type)",
                           new I_ConceptualizeUniversally[] { PROCESSED }),
                    DUPLICATE_PENDING_RETIREMENT("duplicate-pending retirement (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    INTERNAL_USE_ONLY("internal use only (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    DO_NOT_EDIT_INTERNAL_USE("do not edit, for internal use (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    DO_NOT_EDIT_FOR_RELEASE("do not edit, for release (active status type)",
                            new I_ConceptualizeUniversally[] { ACTIVE }),
                    FLAGGED_POTENTIAL_DUPLICATE("dup flag (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    FLAGGED_POTENTIAL_REL_ERROR("rel err flag (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    CURRENT_UNREVIEWED("unreviewed (active status type)",
                                new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    CURRENT_TEMP_INTERNAL_USE("temp, internal use only (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    FLAGGED_POTENTIAL_DESC_STYLE_ERROR("desc style flag (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    UNREVIEWED_NEW_ADDITION("unreviewed new addition (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    UNREVIEWED_NEW_DELETION("unreviewed new deletion (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    REVIEWED_APPROVED_ADDITION("reviewed and approved addition (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    REVIEWED_NOT_APPROVED_ADDITION("reviewed and not approved addition (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    REVIEWED_APPROVED_DELETION("reviewed and approved deletion (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
                    REVIEWED_NOT_APPROVED_DELETION("reviewed and not approved deletion (active status type)",
                            new I_ConceptualizeUniversally[] { FLAGGED_FOR_REVIEW }),
            INACTIVE("inactive (inactive status type)",
                    new I_ConceptualizeUniversally[] { STATUS }),
                    CONFLICTING("conflicting (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    NOT_YET_CREATED("not yet created (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    RETIRED("retired (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    RETIRED_MISSPELLED("retired-misspelled (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    DUPLICATE("duplicate (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    OUTDATED("outdated (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    AMBIGUOUS("ambiguous (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    ERRONEOUS("erroneous (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    INAPPROPRIATE("inappropriate (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    IMPLIED_RELATIONSHIP("implied (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    MOVED_ELSEWHERE("moved elsewhere (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
                    EXTINCT("extinct (inactive status type)",
                            new I_ConceptualizeUniversally[] { INACTIVE }),
            CONSTANT("constant (status type)",
                    new I_ConceptualizeUniversally[] { STATUS }),
        USER_INFO("user info",
            new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            USER_NAME("username",
                    new I_ConceptualizeUniversally[] { USER_INFO, DESCRIPTION_TYPE }),
            USER_INBOX("user inbox",
                    new I_ConceptualizeUniversally[] { USER_INFO, DESCRIPTION_TYPE }),
        PATH("path",
                new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            RELEASE("release",
                    new I_ConceptualizeUniversally[] { PATH }),
                SNOMED_CORE("SNOMED Core",
                        new I_ConceptualizeUniversally[] { RELEASE }),
                        SNOMED_20060731("SNOMED 2006-07-31",
                                new I_ConceptualizeUniversally[] { SNOMED_CORE }),
                        SNOMED_20060131("SNOMED 2006-01-31",
                                new I_ConceptualizeUniversally[] { SNOMED_CORE }),
                        SNOMED_20050731("SNOMED 2005-07-31",
                                new I_ConceptualizeUniversally[] { SNOMED_CORE }),
                        SNOMED_20050131("SNOMED 2005-01-31",
                                new I_ConceptualizeUniversally[] { SNOMED_CORE }),
                ARCHITECTONIC_BRANCH(PrimordialId.ACE_AUXILIARY_ID, new String[] {"Workbench Auxiliary"}, null,
                        new I_ConceptualizeUniversally[] { PATH }),
        TEST("test",
               new I_ConceptualizeUniversally[] { PATH }),
        DEVELOPMENT("development",
                new I_ConceptualizeUniversally[] { PATH }),
            
    ID_SOURCE("identifier source",
            new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
        SNOMED_INT_ID("SNOMED integer id",
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        SNOMED_T3_UUID("SNOMED Type 3 UUID",
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        SNOMED_RT_ID("SNOMED RT Id",
                        new I_ConceptualizeUniversally[] { ID_SOURCE }),
        CTV3_ID("CTV3_ID",
              new I_ConceptualizeUniversally[] { ID_SOURCE }),
        CTV3_TERM_ID("CTV3_TERM_ID",
                      new I_ConceptualizeUniversally[] { ID_SOURCE }),      
        CTV2_ID("CTV2_ID",
              new I_ConceptualizeUniversally[] { ID_SOURCE }),
        CTV2_TERM_ID("CTV2_TERM_ID",
                      new I_ConceptualizeUniversally[] { ID_SOURCE }),      
        CT4B_ID("CT4Byte_ID",
              new I_ConceptualizeUniversally[] { ID_SOURCE }),
        UNSPECIFIED_UUID(PrimordialId.ACE_AUX_ENCODING_ID, new String[] {"generated UUID"}, null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        T5_FROM_DATA_UUID("Data Generated Type 5 UUID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        OID("OID", null,
              new I_ConceptualizeUniversally[] { ID_SOURCE }),
        UNSPECIFIED_STRING("unspecified string", null,
              new I_ConceptualizeUniversally[] { ID_SOURCE }),
        ICD_9("ICD-9 ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        ICD_10("ICD-10 ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        LOINC("LOINC ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        RX_NORM("RX Norm ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        /*
        READ_V3_TERM("Read V3 Term ID", null,
        		new I_ConceptualizeUniversally[] { ID_SOURCE }),
        READ_V3_TERM_SYN("Read V3 Term Synonym ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),

        READ_V2_TERM("Read V2 Term ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        READ_V2_TERM_SYN("Read V2 Term Synonym ID", null,
                 new I_ConceptualizeUniversally[] { ID_SOURCE }),
            */
                READ_V3("Read V3 ID", null,
                        new I_ConceptualizeUniversally[] { ID_SOURCE }),
                READ_V2("Read V2 ID", null,
                        new I_ConceptualizeUniversally[] { ID_SOURCE }),
                READ_4B("Read 4Byte ID", null,
                        new I_ConceptualizeUniversally[] { ID_SOURCE }),
        CPT("CPT ID", null,
                new I_ConceptualizeUniversally[] { ID_SOURCE }),
        PROJECTS_ROOT_HIERARCHY("Terminology Project Auxiliary concept",
                new I_ConceptualizeUniversally[] { }),
            PROJECTS_ROOT("projects",
                        new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            TRANSLATION_PROJECTS_ROOT("translation projects",
            		new I_ConceptualizeUniversally[] { PROJECTS_ROOT }),
            PROJECT_EXTENSION_REFSET("project extension refset",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            WORKSETS_ROOT("worksets",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            WORKSET_EXTENSION_REFSET("workset extension refset",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            WORKLISTS_ROOT("worklists",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            WORKLISTS_EXTENSION_REFSET("worklists extension refset",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            PARTITIONS_ROOT("partitions",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            PARTITIONS_SCHEMES_ROOT("partition schemes",
            		new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            PARTITIONS_EXTENSION_REFSET("partitions extension refset",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            LINGUISTIC_GUIDELINES_ROOT("linguistic guidelines",
                    new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
            PROJECT_STATUS_ROOT("project status",
            		new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
        		WORKLIST_ITEM_ASSIGNED_STATUS("worklist item assigned",
        				new I_ConceptualizeUniversally[] { PROJECT_STATUS_ROOT }),
    			WORKLIST_ITEM_DELIVERED_STATUS("worklist item delivered",
    					new I_ConceptualizeUniversally[] { PROJECT_STATUS_ROOT }),
    			WORKLIST_ITEM_TRANSLATED_STATUS("worklist item translated",
    					new I_ConceptualizeUniversally[] { PROJECT_STATUS_ROOT }),
    			WORKLIST_ITEM_REVIEWED_STATUS("worklist item reviewed",
    					new I_ConceptualizeUniversally[] { PROJECT_STATUS_ROOT }),
    			WORKLIST_ITEM_REJECTED_STATUS("worklist item rejected",
    					new I_ConceptualizeUniversally[] { PROJECT_STATUS_ROOT }),
    			WORKLIST_ITEM_APPROVED_FOR_PUBLICATION_STATUS("worklist item approved for publication",
    					new I_ConceptualizeUniversally[] { PROJECT_STATUS_ROOT }),
            PROJECT_ATTRIBUTES_ROOT("project manager attributes",
            		new I_ConceptualizeUniversally[] { PROJECTS_ROOT_HIERARCHY }),
    	        INCLUDES_FROM_ATTRIBUTE("includes concepts from",
    	                new I_ConceptualizeUniversally[] { PROJECT_ATTRIBUTES_ROOT }),
    	        HAS_EXCLUSION_REFSET_ATTRIBUTE("has exclusion refset",
    	        		new I_ConceptualizeUniversally[] { PROJECT_ATTRIBUTES_ROOT }),
    			HAS_COMMON_REFSET_ATTRIBUTE("has common refset",
    					new I_ConceptualizeUniversally[] { PROJECT_ATTRIBUTES_ROOT }),
    			HAS_SOURCE_REFSET_ATTRIBUTE("has source refset",
    					new I_ConceptualizeUniversally[] { PROJECT_ATTRIBUTES_ROOT }),
    			HAS_LANGUAGE_SOURCE_REFSET_ATTRIBUTE("has language source refset",
    					new I_ConceptualizeUniversally[] { PROJECT_ATTRIBUTES_ROOT }),
    			HAS_LANGUAGE_TARGET_REFSET_ATTRIBUTE("has language target refset",
    					new I_ConceptualizeUniversally[] { PROJECT_ATTRIBUTES_ROOT }),
    	    PROJECT_MANAGER_ROLE("project manager role",
    	    		new I_ConceptualizeUniversally[] { USER_ROLE }),
    	    		PROJECT_CREATE_PERMISSION("project create permission",
    	    	    		new I_ConceptualizeUniversally[] { PROJECT_MANAGER_ROLE }),
    	    		PROJECT_VIEW_PERMISSION("project view permission",
    	    				new I_ConceptualizeUniversally[] { PROJECT_MANAGER_ROLE }),
    				PROJECT_UPDATE_PERMISSION("project update permission",
    						new I_ConceptualizeUniversally[] { PROJECT_MANAGER_ROLE }),
    				PROJECT_DELETE_PERMISSION("project delete permission",
    						new I_ConceptualizeUniversally[] { PROJECT_MANAGER_ROLE }),
    				VIEW_PROJECT_REPORTS_PERMISSION("view project reports permission",
    	    	    		new I_ConceptualizeUniversally[] { PROJECT_MANAGER_ROLE }),
    		WORKSET_MANAGER_ROLE("workset manager role",
    	    		new I_ConceptualizeUniversally[] { USER_ROLE }),
    	    		WORKSET_CREATE_PERMISSION("workset create permission",
    	    	    		new I_ConceptualizeUniversally[] { WORKSET_MANAGER_ROLE }),
    	    		WORKSET_VIEW_PERMISSION("workset view permission",
    	    				new I_ConceptualizeUniversally[] { WORKSET_MANAGER_ROLE }),
    				WORKSET_UPDATE_PERMISSION("workset update permission",
    						new I_ConceptualizeUniversally[] { WORKSET_MANAGER_ROLE }),
    				WORKSET_DELETE_PERMISSION("workset delete permission",
    						new I_ConceptualizeUniversally[] { WORKSET_MANAGER_ROLE }),
    				WORKSET_SYNC_PERMISSION("workset sync permission",
    						new I_ConceptualizeUniversally[] { WORKSET_MANAGER_ROLE }),
    				WORKSET_PROMOTION_PERMISSION("workset promotion permission",
    						new I_ConceptualizeUniversally[] { WORKSET_MANAGER_ROLE }),
    		PARTITIONING_MANAGER_ROLE("partitioning manager role",
    	    		new I_ConceptualizeUniversally[] { USER_ROLE }),
    	    		PARTITION_CREATE_PERMISSION("partition create permission",
    	    	    		new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    	    		PARTITION_VIEW_PERMISSION("partition view permission",
    	    				new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				PARTITION_UPDATE_PERMISSION("partition update permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				PARTITION_DELETE_PERMISSION("partition delete permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				WORKLIST_CREATE_PERMISSION("worklist create permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				WORKLIST_VIEW_PERMISSION("worklist view permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				WORKLIST_UPDATE_PERMISSION("worklist update permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				WORKLIST_DELETE_PERMISSION("worklist delete permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
    				WORKLIST_DELIVER_PERMISSION("worklist deliver permission",
    						new I_ConceptualizeUniversally[] { PARTITIONING_MANAGER_ROLE }),
		TRANSLATOR_ROLE("translator role",
	    		new I_ConceptualizeUniversally[] { USER_ROLE }),
		TRANSLATION_REVIEWER_ONE_ROLE("translation reviewer one role",
				new I_ConceptualizeUniversally[] { USER_ROLE }),
		TRANSLATION_REVIEWER_TWO_ROLE("translation reviewer two role",
				new I_ConceptualizeUniversally[] { USER_ROLE }),
		TRANSLATION_SME_TRANSLATION_ROLE("translation sme role",
				new I_ConceptualizeUniversally[] { USER_ROLE }),
		TRANSLATION_EDITORIAL_BOARD_ROLE("translation editorial board role",
				new I_ConceptualizeUniversally[] { USER_ROLE }),
		SNOMED_LANGUAGE_ES_PATH("spanish language path",
                new I_ConceptualizeUniversally[] { RELEASE }),
        SNOMED_LANGUAGE_SE_PATH("swedish language path",
                new I_ConceptualizeUniversally[] { RELEASE }),
    FILE_LINK_CATEGORY("file link category",
            new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
      BUSINESS_PROCESS_CATEGORY("business process",
            new I_ConceptualizeUniversally[] { FILE_LINK_CATEGORY }),
            TRANSLATION_BUSINESS_PROCESS_CATEGORY("translation business process",
            		new I_ConceptualizeUniversally[] { BUSINESS_PROCESS_CATEGORY }),
	SEMTAGS_ROOT("semantic tags",
            new I_ConceptualizeUniversally[] { ARCHITECTONIC_ROOT_CONCEPT }),
            SEMTAG_ADMINISTRATIVE_CONCEPT("administrative concept",
                    new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
            SEMTAG_ASSESMENT_SCALE("assessment scale",
            		new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
    		SEMTAG_ATTRIBUTE("attribute",
    				new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_BODY_STRUCTURE("body structure",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_CELL_STRUCTURE("cell structure",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_CELL("cell",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_CONTEXT_DEPENDANT_CATEGORY("context-dependent category",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_DISORDER("disorder",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_ENVIRONMENT_LOCATION("environment / location",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_ENVIRONMENT("environment",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_ETHNIC_GROUP("ethnic group",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_EVENT("event",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_FINDING("finding",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
			SEMTAG_GEOGRAPHIC_LOCATION("geographic location",
					new I_ConceptualizeUniversally[] { SEMTAGS_ROOT }),
					
       
        ;
        private ArrayList<UUID> conceptUids = new ArrayList<UUID>();

        public String[] parents_S;
        public String[] descriptions_S;

        private Boolean primitive = true;

        private UniversalFixedRel[] rels;

        private UniversalFixedDescription[] descriptions;

        private static PrimordialId[] descTypeOrder;

        private I_ConceptualizeLocally local;

		public String[] getParents_S(){
			return parents_S;
		}

		public String[] getDescriptions_S(){
			return descriptions_S;
		}

      private Concept(String descriptionString, I_ConceptualizeUniversally[] parents) {
         this(new String[] {descriptionString}, null, parents);
      }
      private Concept(String descriptionString, String defString, I_ConceptualizeUniversally[] parents) {
         this(new String[] {descriptionString}, defString, parents);
      }
        // PrimordialId
        private Concept(String[] descriptionStrings, String defString, I_ConceptualizeUniversally[] parents) {
            this.conceptUids.add(Type3UuidFactory.fromEnum(this));
            init(descriptionStrings, defString, parents);
        }
        private Concept(PrimordialId id, String[] descriptionStrings, String defString, I_ConceptualizeUniversally[] parents) {
            this.conceptUids = new ArrayList<UUID>(id.getUids());
            init(descriptionStrings, defString, parents);
        }
        private void init(String[] descriptionStrings, String defString, I_ConceptualizeUniversally[] parents) {
            try {
            	if (descriptionStrings.length==1) {
            		if (descriptionStrings[0].indexOf("(")!=-1) {
            			String[] newDescriptionStrings = new String[2];
                		newDescriptionStrings[0] = descriptionStrings[0].trim();
                		newDescriptionStrings[1] = descriptionStrings[0].substring(0,descriptionStrings[0].indexOf("(")).trim();
            			descriptionStrings = newDescriptionStrings;
            		}
            	}
            if(parents.length > 0){
            parents_S = new String[parents.length];
            for(int i=0;i<parents.length ; i++)
            {
              parents_S[i] = parents[i].toString();
            }
            }
            if(descriptionStrings.length > 0){
            	descriptions_S = descriptionStrings;
            }



                this.rels = makeRels(this, parents);
                this.descriptions = makeDescriptions(this, descriptionStrings, defString);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        public UniversalFixedRel[] makeRels(I_ConceptualizeUniversally source, I_ConceptualizeUniversally[] parents) throws Exception {
            UniversalFixedRel[] rels = new UniversalFixedRel[parents.length];
            int i = 0;
            for (I_ConceptualizeUniversally p: parents) {
                int relGrp = 0;
                int parentIndex = i++;
                rels[parentIndex] = new UniversalFixedRel(Type3UuidFactory.forRel(source.getUids(),
                        PrimordialId.IS_A_REL_ID.getUids(), p.getUids()),
                        source.getUids(),
                        PrimordialId.IS_A_REL_ID.getUids(), p.getUids(),
                        PrimordialId.STATED_CHARACTERISTIC_ID.getUids(),
                        PrimordialId.NOT_REFINABLE_ID.getUids(), relGrp);
            }
            return rels;
        }

        public UniversalFixedDescription[] makeDescriptions(I_ConceptualizeUniversally source, String[] descriptionStrings, String defString) throws Exception {
         if (descriptionStrings.length == 1) {
            String[] newDescriptionArray = new String[2];
            newDescriptionArray[0] = descriptionStrings[0];
            newDescriptionArray[1] = descriptionStrings[0];
            descriptionStrings = newDescriptionArray;
         }
            if (descTypeOrder == null) {
                descTypeOrder = new PrimordialId[] {
                        PrimordialId.FULLY_SPECIFIED_DESCRIPTION_TYPE_ID,
                        PrimordialId.PREFERED_TERM_ID };
            }
         UniversalFixedDescription[] descriptions;
         if (defString == null) {
            descriptions = new UniversalFixedDescription[descriptionStrings.length];
         } else {
            descriptions = new UniversalFixedDescription[descriptionStrings.length + 1];
         }
            int i = 0;
            boolean initialCapSig = true;
            String langCode = "en";
            for (String descText: descriptionStrings) {
                if (descText != null) {
                    descriptions[i] = new UniversalFixedDescription(Type3UuidFactory.forDesc(source.getUids(), descTypeOrder[i].getUids(), descText),
                            PrimordialId.CURRENT_ID.getUids(),
                            source.getUids(),
                            initialCapSig, descTypeOrder[i].getUids(), descText,
                            langCode);
                }
                i++;
            }
         if (defString != null) {
            descriptions[i] = new UniversalFixedDescription(Type3UuidFactory.forDesc(source.getUids(), PrimordialId.XHTML_DEF_ID.getUids(), defString),
                  PrimordialId.CURRENT_ID.getUids(),
                  source.getUids(),
                  initialCapSig, PrimordialId.XHTML_DEF_ID.getUids(), defString,
                  langCode);
         }
            return descriptions;
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


        public I_DescribeConceptUniversally getDescription(List<I_ConceptualizeUniversally> typePriorityList, I_StoreUniversalFixedTerminology termStore)  {
            throw new UnsupportedOperationException();
        }

        public Collection<I_DescribeConceptUniversally> getDescriptions(I_StoreUniversalFixedTerminology server)  {
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



        public I_ConceptualizeLocally localize() throws IOException, TerminologyException {
        	if (local == null) {
        		local = LocalFixedConcept.get(getUids(), primitive);
        	}
            return local;
        }
        public Collection<I_RelateConceptsUniversally> getDestRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }
        public Collection<I_RelateConceptsUniversally> getSourceRels(Collection<I_ConceptualizeUniversally> types, I_StoreUniversalFixedTerminology termStore) {
            throw new UnsupportedOperationException();
        }

		@Override
		public UUID getPrimoridalUid() throws IOException, TerminologyException {
			return conceptUids.get(0);
		}
    }

    public void addToMemoryTermServer(MemoryTermServer server) throws Exception {
        server.addRoot(Concept.ARCHITECTONIC_ROOT_CONCEPT);
        for (Concept c: Concept.values()) {
            server.add(c);
            for (I_DescribeConceptUniversally d: c.descriptions) {
                server.add(d);
            }
            for (I_RelateConceptsUniversally r: c.rels) {
                server.add(r);
            }
        }
    }

    /**
     * Values
     * <li>0 Unspecified This may be assigned as either a Preferred Term or
     * Synonym by a I_Describe Subset for a language, dialect or realm.
     * <li>1 Preferred This is the Preferred Term for the associated I_Concept.
     * <li>2 Synonym This is a Synonym for the associated I_Concept.
     * <li>3 FullySpecifiedName This is the FullySpecifiedName for the
     * associated I_Concept.
     *
     * @param type
     * @return
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws RemoteException
     */
    public static I_ConceptualizeUniversally getSnomedDescriptionType(int type) {
        switch (type) {
        case 0:
            return ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE;
        case 1:
            return ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE;
        case 2:
            return ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE;
        case 3:
            return ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE;
        }
        return ArchitectonicAuxiliary.Concept.DESCRIPTION_TYPE;

    }

    /**
     * Values
     * <li>0 Unspecified This may be assigned as either a Preferred Term or
     * Synonym by a I_Describe Subset for a language, dialect or realm.
     * <li>1 Preferred This is the Preferred Term for the associated I_Concept.
     * <li>2 Synonym This is a Synonym for the associated I_Concept.
     * <li>3 FullySpecifiedName This is the FullySpecifiedName for the
     * associated I_Concept.
     *
     * @param uuids
     * @return int value representing the enumerated type
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws RemoteException
     */
    public static int getSnomedDescriptionTypeId(Collection<UUID> uuids){
        if (containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.UNSPECIFIED_DESCRIPTION_TYPE.getUids()))
            return 0;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.getUids()))
            return 1;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.SYNONYM_DESCRIPTION_TYPE.getUids()))
            return 2;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()))
            return 3;

        return 0;
    }

    /**
     * Values
     * <li>0 CURRENT.
     * <li>1 NONCURRENT.
     * <li>2 DUPLICATE.
     * <li>3 OUTDATED.
     * <li>5 ERRONEOUS.
     * <li>6 LIMITED.
     * <li>7 INAPPROPRIATE.
     * <li>8 CONCEPTNONCURRENT
     * <li>10 MOVEDELSEWHERE
     * <li>11 PENDINGMOVE
     *
     * @param uuids
     * @return int value representing the enumerated type
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws RemoteException
     */
    public static int getSnomedDescriptionStatusId(Collection<UUID> uuids){
        if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.CURRENT.getUids()))
            return 0;
        else if (containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()))
            return 0;
        else if (containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.DO_NOT_EDIT_FOR_RELEASE.getUids()))
            return 0;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.RETIRED.getUids()))
            return 1;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.DUPLICATE.getUids()))
            return 2;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.OUTDATED.getUids()))
            return 3;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ERRONEOUS.getUids()))
            return 5;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.LIMITED.getUids()))
            return 6;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.INAPPROPRIATE.getUids()))
            return 7;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.getUids()))
            return 8;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.getUids()))
            return 10;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()))
            return 11;

        return -1;
    }

    private static boolean containsUuidElement(Collection<UUID> parentUuids, Collection<UUID> childUuids){

        Iterator<UUID> parentIt = parentUuids.iterator();
        Iterator<UUID> childIt = childUuids.iterator();

        while(parentIt.hasNext()){
            parentIt.next();
            while(childIt.hasNext()){
                if(parentUuids.contains(childIt.next())){
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Values
     * <li>0 CURRENT.
     * <li>1 RETIRED.
     * <li>2 DUPLICATE.
     * <li>3 OUTDATED.
     * <li>4 AMBIGUOS
     * <li>5 ERRONEOUS.
     * <li>6 LIMITED.
     * <li>7 INAPPROPRIATE.
     * <li>8 CONCEPT_RETIRED.
     * <li>10 MOVEDELSEWHERE
     * <li>11 PENDINGMOVE
     *
     * @param uuids
     * @return int value representing the enumerated type
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws RemoteException
     */
    public static int getSnomedConceptStatusId(Collection<UUID> uuids){
      if (containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.CURRENT.getUids()))
         return 0;
      else if (containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()))
         return 0;
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.RETIRED.getUids()))
            return 1;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.DUPLICATE.getUids()))
            return 2;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.OUTDATED.getUids()))
            return 3;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.AMBIGUOUS.getUids()))
            return 4;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ERRONEOUS.getUids()))
            return 5;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.LIMITED.getUids()))
            return 6;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.INAPPROPRIATE.getUids()))
            return 7;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED.getUids()))
            return 8;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE.getUids()))
            return 10;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.PENDING_MOVE.getUids()))
            return 11;

        return -1;
    }

    /**
     * Values:
     * <li>0 Defining This relationship represents a defining characteristic of
     * the sourceId concept. Hierarchical relationships (e.g. ISA and
     * PART-OF) are also regarded as defining relationships Example: Site =
     * Liver is a defining characteristic of Liver biopsy.
     * <li>1 Qualifier This relationship represents an optional qualifying
     * characteristic. Example: Revision status = Conversion from other type
     * of arthroplasty is a possible qualification of Hip replacement
     * <li>2 Historical This is used to relate an inactive concept to another
     * concept. Example: The Same As relationship connects an inactive concept
     * with the concept it duplicated. Only used in the Historical Relationships
     * File.
     * <li>3 Additional This relationship represents a context specific
     * characteristic. This is used to convey characteristics of a concept that
     * apply at a particular time within a particular organization but which are
     * not intrinsic to the concept. Example: Prescription Only Medicine is a
     * context specific characteristic of the I_Concept Amoxycillin 250mg
     * capsule. It is true currently in the UK but is not true in some other
     * countries.
     *
     * @param type
     * @return
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws RemoteException
     */
    public static I_ConceptualizeUniversally getSnomedCharacteristicType(int type)  {
        switch (type) {
        case 0:
            return ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC;
        case 1:
            return ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC;
        case 2:
            return ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC;
        case 3:
            return ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC;
        }
        return ArchitectonicAuxiliary.Concept.CHARACTERISTIC_TYPE;
    }

    public static int getSnomedCharacteristicTypeId(Collection<UUID> uuids){
        if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.DEFINING_CHARACTERISTIC.getUids()))
            return 0;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.STATED_RELATIONSHIP.getUids()))
         return 0;
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.QUALIFIER_CHARACTERISTIC.getUids()))
            return 1;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.HISTORICAL_CHARACTERISTIC.getUids()))
            return 2;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ADDITIONAL_CHARACTERISTIC.getUids()))
            return 3;

        return -1;
    }

    /**
     * An indication of whether it is possible to refine the target concept when
     * this I_Relate is used as a template for clinical data entry.
     * <p>
     * <p>
     * Values
     * <li>0 Not refinable Not refinable.
     * <li>1 Optional May be refined by selecting subtypes.
     * <li>2 Mandatory Must be refined by selecting a subtype.
     *
     * @param type
     * @return
     * @throws IdentifierIsNotNativeException
     * @throws QueryException
     * @throws RemoteException
     */
    public static I_ConceptualizeUniversally getSnomedRefinabilityType(int type)  {
        switch (type) {
        case 0:
            return ArchitectonicAuxiliary.Concept.NOT_REFINABLE;
        case 1:
            return ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY;
        case 2:
            return ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY;
        }
        return ArchitectonicAuxiliary.Concept.RELATIONSHIP_REFINABILITY;
    }

    public static int getSnomedRefinabilityTypeId(Collection<UUID> uuids){
        if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.NOT_REFINABLE.getUids()))
            return 0;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.OPTIONAL_REFINABILITY.getUids()))
            return 1;
        else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.MANDATORY_REFINABILITY.getUids()))
            return 2;

        return -1;
    }

       /**
         * Values
         * <li>-2 Conflicting
         * <li>-1 Not-yet created.
         * <li>0 Current The Description and its associated Concept are in
         * current use.
         * <li>1 Non-Current The Description has been withdrawn without a
         * specified reason.
         * <li>2 Duplicate The Description has been withdrawn from current use
         * because it duplicates another description containing the same term
         * (or a very similar term) associated with the same Concept.
         * <li>3 Outdated The Description has been withdrawn from current use
         * because this Term is no longer in general clinical use as a label for
         * the associated Concept.
         * <li>4 Ambiguous The Concept has been withdrawn from current use
         * because it is inherently ambiguous. These concepts are considered
         * inactive.
         * <li>5 Erroneous The Description has been withdrawn as the Term
         * contains errors.
         * <li>6 Limited The Description is a valid Description of a Concept
         * which has limited status (i.e. the Concept has ConceptStatus = 6).
         * <li>7 Inappropriate The Description has been withdrawn as the Term
         * should not refer to this concept.
         * <li>8 Concept noncurrent The Description is a valid Description of a
         * Concept which has been made non-current (i.e. the Concept has
         * ConceptStatus 1, 2, 3, 4, 5, or 10).
         * <li>9 Implied Relationship withdrawn but is implied by other active
         * Relationships.
         * <li>10 Moved elsewhere The Description has been moved to an
         * extension, to a different extension, or to the core. A reference will
         * indicate the namespace to which the description has been moved.
         * <li>11 Pending move The Description will be moved to an extension,
         * to a different extension, or to the core. A reference will indicate
         * the namespace to which the description has been moved when the
         * recipient organization confirms the move (Future Use).
         *
         * @param statusCode
         * @return
         */
    public static I_ConceptualizeUniversally getStatusFromId(int statusCode) {
        switch (statusCode) {
        case -2:
            return ArchitectonicAuxiliary.Concept.CONFLICTING;
        case -1:
            return ArchitectonicAuxiliary.Concept.NOT_YET_CREATED;
        case 0:
            return ArchitectonicAuxiliary.Concept.CURRENT;
        case 1:
            return ArchitectonicAuxiliary.Concept.RETIRED;
        case 2:
            return ArchitectonicAuxiliary.Concept.DUPLICATE;
        case 3:
            return ArchitectonicAuxiliary.Concept.OUTDATED;
        case 4:
            return ArchitectonicAuxiliary.Concept.AMBIGUOUS;
        case 5:
            return ArchitectonicAuxiliary.Concept.ERRONEOUS;
        case 6:
            return ArchitectonicAuxiliary.Concept.LIMITED;
        case 7:
            return ArchitectonicAuxiliary.Concept.INAPPROPRIATE;
        case 8:
            return ArchitectonicAuxiliary.Concept.CONCEPT_RETIRED;
        case 9:
            return ArchitectonicAuxiliary.Concept.IMPLIED_RELATIONSHIP;
        case 10:
            return ArchitectonicAuxiliary.Concept.MOVED_ELSEWHERE;
        case 11:
            return ArchitectonicAuxiliary.Concept.PENDING_MOVE;

        }
        throw new IllegalArgumentException("Unknown status code: " + statusCode);
    }

    public static void main(String[] args) throws Exception {
        try {
            File directory = new File(args[0]);
            directory.mkdirs();
            File conceptFile = new File(directory, "concepts.txt");
            File descFile = new File(directory, "descriptions.txt");
            File relFile = new File(directory, "relationships.txt");
            File rootsFile = new File(directory, "roots.txt");
            File extTypeFile = new File(directory, "extensions.txt");
            File altIdFile = new File(directory, "alt_ids.txt");

            MemoryTermServer mts = new MemoryTermServer();
            LocalFixedTerminology.setStore(mts);
            mts.setGenerateIds(true);
            ArchitectonicAuxiliary aa = new ArchitectonicAuxiliary();
            aa.addToMemoryTermServer(mts);
            DocumentAuxiliary da = new DocumentAuxiliary();
            da.addToMemoryTermServer(mts);
            RefsetAuxiliary rsa = new RefsetAuxiliary();
            rsa.addToMemoryTermServer(mts);

            HL7 hl7 = new HL7();
            hl7.addToMemoryTermServer(mts);

            QueueType queueType = new QueueType();
            queueType.addToMemoryTermServer(mts);

            mts.setGenerateIds(false);

               Writer altIdWriter = new FileWriter(altIdFile);

               Writer conceptWriter = new FileWriter(conceptFile);
            mts.writeConcepts(conceptWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.SNOMED);
            conceptWriter.close();

            Writer descWriter = new FileWriter(descFile);
            mts.writeDescriptions(descWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.SNOMED);
            descWriter.close();

            Writer relWriter = new FileWriter(relFile);
            mts.writeRelationships(relWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.SNOMED);
            relWriter.close();

            Writer rootsWriter = new FileWriter(rootsFile);
            mts.writeRoots(rootsWriter, MemoryTermServer.FILE_FORMAT.SNOMED);
            rootsWriter.close();

            Writer extensionTypeWriter = new FileWriter(extTypeFile);
            mts.writeExtensionTypes(extensionTypeWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.SNOMED);
            extensionTypeWriter.close();

            I_ConceptualizeLocally[] descTypeOrder = new I_ConceptualizeLocally[] {
                    mts.getConcept(mts.getNid(ArchitectonicAuxiliary.Concept.EXTENSION_TABLE.getUids())) };
            List<I_ConceptualizeLocally> descTypePriorityList = Arrays.asList(descTypeOrder);

            for (I_ConceptualizeLocally extensionType: mts.getExtensionTypes()) {
                I_DescribeConceptLocally typeDesc = extensionType.getDescription(descTypePriorityList);
                File extensionFile = new File(directory, typeDesc.getText() + ".txt");
                Writer extensionWriter = new FileWriter(extensionFile);
                mts.writeExtension(extensionType, extensionWriter, altIdWriter, MemoryTermServer.FILE_FORMAT.SNOMED);
                extensionWriter.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static List<I_ConceptualizeUniversally> preferredDescPrefList;
    public static List<I_ConceptualizeUniversally> getUniversalPreferredDescPrefList() {
        if (preferredDescPrefList == null) {
               preferredDescPrefList = new ArrayList<I_ConceptualizeUniversally>();

               preferredDescPrefList.add(Concept.XHTML_PREFERRED_DESC_TYPE);
            preferredDescPrefList.add(Concept.PREFERRED_DESCRIPTION_TYPE);
            preferredDescPrefList.add(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE);
             preferredDescPrefList.add(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE);
             preferredDescPrefList.add(Concept.SYNONYM_DESCRIPTION_TYPE);
             preferredDescPrefList.add(Concept.ENTRY_DESCRIPTION_TYPE);
        }
        return preferredDescPrefList;
    }
    public static List<I_ConceptualizeLocally> getLocalPreferredDescPrefList() throws IOException, TerminologyException {
        List<I_ConceptualizeLocally> localList = new ArrayList<I_ConceptualizeLocally>();
        for (I_ConceptualizeUniversally uc: getUniversalPreferredDescPrefList()) {
            localList.add(uc.localize());
        }
        return localList;
    }

    private static List<I_ConceptualizeUniversally> toStringDescPrefList;
    public static List<I_ConceptualizeUniversally> getToStringDescPrefList() {
        if (toStringDescPrefList == null) {
            toStringDescPrefList = new ArrayList<I_ConceptualizeUniversally>();
            toStringDescPrefList.add(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE);
            toStringDescPrefList.add(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE);
            toStringDescPrefList.add(Concept.PREFERRED_DESCRIPTION_TYPE);
            toStringDescPrefList.add(Concept.XHTML_PREFERRED_DESC_TYPE);
        }
        return toStringDescPrefList;
    }

    private static List<I_ConceptualizeUniversally> fullySpecifiedDescPrefList;
    public static List<I_ConceptualizeUniversally> getFullySpecifiedDescPrefList() {
        if (fullySpecifiedDescPrefList == null) {
            fullySpecifiedDescPrefList = new ArrayList<I_ConceptualizeUniversally>();
            fullySpecifiedDescPrefList.add(Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE);
            fullySpecifiedDescPrefList.add(Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE);
            fullySpecifiedDescPrefList.add(Concept.XHTML_PREFERRED_DESC_TYPE);
            fullySpecifiedDescPrefList.add(Concept.PREFERRED_DESCRIPTION_TYPE);
        }
        return fullySpecifiedDescPrefList;
    }

    private static Map<I_StoreLocalFixedTerminology, List<I_ConceptualizeLocally>> localToStringDescPrefListMap = new HashMap<I_StoreLocalFixedTerminology, List<I_ConceptualizeLocally>>();
    public static List<I_ConceptualizeLocally> getLocalToStringDescPrefList(I_StoreLocalFixedTerminology termServer) throws Exception {
        if (localToStringDescPrefListMap.get(termServer) == null) {
            List<I_ConceptualizeLocally> localList = new ArrayList<I_ConceptualizeLocally>();
            for (I_ConceptualizeUniversally uc: getToStringDescPrefList()) {
                localList.add(uc.localize());
            }
            localToStringDescPrefListMap.put(termServer, localList);
        }
        return localToStringDescPrefListMap.get(termServer);
    }

    private static List<I_ConceptualizeLocally> localFullySpecifiedDescPrefList;
    ;
    public static List<I_ConceptualizeLocally> getLocalFullySpecifiedDescPrefList() throws Exception {
        if (localFullySpecifiedDescPrefList == null) {
            localFullySpecifiedDescPrefList = new ArrayList<I_ConceptualizeLocally>();
            for (I_ConceptualizeUniversally uc: getFullySpecifiedDescPrefList()) {
                localFullySpecifiedDescPrefList.add(uc.localize());
            }
        }
        return localFullySpecifiedDescPrefList;
    }

    public enum LANG_CODE {
    	EN, EN_AU, EN_BZ, EN_CA, EN_GB, EN_IE, EN_JM, EN_NZ,
    	EN_TT, EN_US, EN_ZA, LIT, ES, ES_AR, ES_BO, ES_CL, ES_CO, ES_CR,
    	ES_DO, ES_EC, ES_ES, ES_GT, ES_HN, EX_MX, ES_NI, ES_PA, ES_PE,
    	ES_ER, ES_PY, ES_SV, ES_UY, ES_VE, FR, FR_BE, FR_CA, FR_FR, FR_CH,
    	FR_LU, FR_MC, DA, DA_DK, SV, SV_FI, SV_SE, LT, LT_LT, ZH,
        ZH_CN, ZH_HK, ZH_CHS, ZH_CHT, ZH_MO, ZH_SG, ZH_TW;
    }

    public static I_ConceptualizeUniversally getLanguageConcept(String langCode) {
    	String normalizedLangCode = langCode.toUpperCase().replace('-', '_');
    	switch (LANG_CODE.valueOf(normalizedLangCode)) {
    	case DA:
            return ArchitectonicAuxiliary.Concept.DA;
    	case DA_DK:
            return ArchitectonicAuxiliary.Concept.DA_DK;
    	case EN:
            return ArchitectonicAuxiliary.Concept.EN;
    	case EN_AU:
            return ArchitectonicAuxiliary.Concept.EN_AU;
    	case EN_BZ:
            return ArchitectonicAuxiliary.Concept.EN_BZ;
    	case EN_CA:
            return ArchitectonicAuxiliary.Concept.EN_CA;
    	case EN_GB:
            return ArchitectonicAuxiliary.Concept.EN_GB;
    	case EN_IE:
            return ArchitectonicAuxiliary.Concept.EN_IE;
    	case EN_JM:
            return ArchitectonicAuxiliary.Concept.EN_JM;
    	case EN_NZ:
            return ArchitectonicAuxiliary.Concept.EN_NZ;
    	case EN_TT:
            return ArchitectonicAuxiliary.Concept.EN_TT;
    	case EN_US:
            return ArchitectonicAuxiliary.Concept.EN_US;
    	case EN_ZA:
            return ArchitectonicAuxiliary.Concept.EN_ZA;
    	case ES:
            return ArchitectonicAuxiliary.Concept.ES;
    	case ES_AR:
            return ArchitectonicAuxiliary.Concept.ES_AR;
    	case ES_BO:
            return ArchitectonicAuxiliary.Concept.ES_BO;
    	case ES_CL:
            return ArchitectonicAuxiliary.Concept.ES_CL;
    	case ES_CO:
            return ArchitectonicAuxiliary.Concept.ES_CO;
    	case ES_CR:
            return ArchitectonicAuxiliary.Concept.ES_CR;
    	case ES_DO:
            return ArchitectonicAuxiliary.Concept.ES_DO;
    	case ES_EC:
            return ArchitectonicAuxiliary.Concept.ES_EC;
    	case ES_ES:
            return ArchitectonicAuxiliary.Concept.ES_ES;
     	case ES_GT:
            return ArchitectonicAuxiliary.Concept.ES_GT;
    	case ES_HN:
            return ArchitectonicAuxiliary.Concept.ES_HN;
    	case ES_NI:
            return ArchitectonicAuxiliary.Concept.ES_NI;
    	case ES_PA:
            return ArchitectonicAuxiliary.Concept.ES_PA;
    	case ES_PE:
            return ArchitectonicAuxiliary.Concept.ES_PE;
    	case ES_PY:
            return ArchitectonicAuxiliary.Concept.ES_PY;
    	case ES_SV:
            return ArchitectonicAuxiliary.Concept.ES_SV;
    	case ES_UY:
            return ArchitectonicAuxiliary.Concept.ES_UY;
    	case ES_VE:
            return ArchitectonicAuxiliary.Concept.ES_VE;
    	case FR:
            return ArchitectonicAuxiliary.Concept.FR;
    	case FR_BE:
            return ArchitectonicAuxiliary.Concept.FR_BE;
    	case FR_CA:
            return ArchitectonicAuxiliary.Concept.FR_CA;
    	case FR_CH:
            return ArchitectonicAuxiliary.Concept.FR_CH;
    	case FR_FR:
            return ArchitectonicAuxiliary.Concept.FR_FR;
    	case FR_LU:
            return ArchitectonicAuxiliary.Concept.FR_LU;
    	case FR_MC:
            return ArchitectonicAuxiliary.Concept.FR_MC;
    	case LIT:
            return ArchitectonicAuxiliary.Concept.LIT;
    	case LT:
            return ArchitectonicAuxiliary.Concept.LT;
    	case LT_LT:
            return ArchitectonicAuxiliary.Concept.LT_LT;
    	case SV:
            return ArchitectonicAuxiliary.Concept.SV;
    	case SV_FI:
            return ArchitectonicAuxiliary.Concept.SV_FI;
    	case SV_SE:
            return ArchitectonicAuxiliary.Concept.SV_SE;
    	case ZH:
            return ArchitectonicAuxiliary.Concept.ZH;
    	case ZH_CHS:
            return ArchitectonicAuxiliary.Concept.ZH_CHS;
    	case ZH_CHT:
            return ArchitectonicAuxiliary.Concept.ZH_CHT;
    	case ZH_CN:
            return ArchitectonicAuxiliary.Concept.ZH_CN;
    	case ZH_HK:
            return ArchitectonicAuxiliary.Concept.ZH_HK;
    	case ZH_MO:
            return ArchitectonicAuxiliary.Concept.ZH_MO;
    	case ZH_SG:
            return ArchitectonicAuxiliary.Concept.ZH_SG;
    	case ZH_TW:
            return ArchitectonicAuxiliary.Concept.ZH_TW;
    	}
        throw new NoSuchElementException("UNK: " + langCode);

    }
    public static String getLanguageCode(Collection<UUID> uuids) throws NoSuchElementException {
      if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN.getUids()))
          return LANG_CODE.EN.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_AU.getUids()))
        return LANG_CODE.EN_AU.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_BZ.getUids()))
        return LANG_CODE.EN_BZ.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_CA.getUids()))
        return LANG_CODE.EN_CA.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_GB.getUids()))
        return LANG_CODE.EN_GB.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_IE.getUids()))
        return LANG_CODE.EN_IE.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_JM.getUids()))
        return LANG_CODE.EN_JM.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_NZ.getUids()))
        return LANG_CODE.EN_NZ.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_TT.getUids()))
        return LANG_CODE.EN_TT.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_US.getUids()))
        return LANG_CODE.EN_US.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.EN_ZA.getUids()))
          return LANG_CODE.EN_ZA.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.LIT.getUids()))
          return LANG_CODE.LIT.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES.getUids()))
          return LANG_CODE.ES.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_AR.getUids()))
          return LANG_CODE.ES_AR.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_BO.getUids()))
          return LANG_CODE.ES_BO.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_CL.getUids()))
          return LANG_CODE.ES_CL.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_CO.getUids()))
          return LANG_CODE.ES_CO.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_CR.getUids()))
          return LANG_CODE.ES_CR.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_DO.getUids()))
          return LANG_CODE.ES_DO.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_EC.getUids()))
          return LANG_CODE.ES_EC.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_ES.getUids()))
          return LANG_CODE.ES_ES.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_GT.getUids()))
          return LANG_CODE.ES_GT.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_HN.getUids()))
          return LANG_CODE.ES_HN.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_MX.getUids()))
          return LANG_CODE.EX_MX.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_NI.getUids()))
          return LANG_CODE.ES_NI.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_PA.getUids()))
          return LANG_CODE.ES_PA.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_PE.getUids()))
          return LANG_CODE.ES_PE.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_PR.getUids()))
          return LANG_CODE.ES_ER.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_PY.getUids()))
          return LANG_CODE.ES_PY.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_SV.getUids()))
          return LANG_CODE.ES_SV.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_UY.getUids()))
          return LANG_CODE.ES_UY.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ES_VE.getUids()))
          return LANG_CODE.ES_VE.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR.getUids()))
          return LANG_CODE.FR.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR_BE.getUids()))
          return LANG_CODE.FR_BE.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR_CA.getUids()))
          return LANG_CODE.FR_CA.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR_FR.getUids()))
          return LANG_CODE.FR_FR.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR_CH.getUids()))
          return LANG_CODE.FR_CH.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR_LU.getUids()))
          return LANG_CODE.FR_LU.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.FR_MC.getUids()))
          return LANG_CODE.FR_MC.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.DA.getUids()))
          return LANG_CODE.DA.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.DA_DK.getUids()))
          return LANG_CODE.DA_DK.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.SV.getUids()))
          return LANG_CODE.SV.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.SV_FI.getUids()))
          return LANG_CODE.SV_FI.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.SV_SE.getUids()))
          return LANG_CODE.SV_SE.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.LT.getUids()))
          return LANG_CODE.LT.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.LT_LT.getUids()))
          return LANG_CODE.LT_LT.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH.getUids()))
          return LANG_CODE.ZH.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_CN.getUids()))
          return LANG_CODE.ZH_CN.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_HK.getUids()))
          return LANG_CODE.ZH_HK.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_CHS.getUids()))
          return LANG_CODE.ZH_CHS.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_CHT.getUids()))
          return LANG_CODE.ZH_CHT.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_MO.getUids()))
          return LANG_CODE.ZH_MO.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_SG.getUids()))
          return LANG_CODE.ZH_SG.toString();
      else if(containsUuidElement(uuids, ArchitectonicAuxiliary.Concept.ZH_TW.getUids()))
          return LANG_CODE.ZH_TW.toString();
      throw new NoSuchElementException("UNK: " + uuids);
  }
}
