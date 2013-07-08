/**
 * Copyright (c) 2012 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.tk.binding.snomed;

import java.util.UUID;

import org.ihtsdo.tk.spec.ConceptSpec;

/**
 * The Class TermAux contains
 * <code>ConceptSpec</code> representations of concepts from the Terminology
 * Auxiliary Hierarchy. These concepts are not SNOMED CT concepts.
 */
public class TermAux {

    /**
     * Represents the concept: auxiliary image.
     */
    public static ConceptSpec AUX_IMAGE =
            new ConceptSpec("auxiliary image",
            UUID.fromString("be72219e-cb1c-3f07-b8b0-ad1d3281090b"));
    /**
     * Represents the concept: is a.
     */
    public static ConceptSpec IS_A =
            new ConceptSpec("is a (relationship type)",
            UUID.fromString("46bccdc4-8fb6-11db-b606-0800200c9a66"));
    /**
     * Represents the concept: current.
     */
    public static ConceptSpec CURRENT =
            new ConceptSpec("current (active status type)",
            UUID.fromString("2faa9261-8fb2-11db-b606-0800200c9a66"));
    /**
     * Represents the concept: retired.
     */
    public static ConceptSpec RETIRED =
            new ConceptSpec("retired (inactive status type)",
            UUID.fromString("e1956e7b-08b4-3ad0-ab02-b411869f1c09"));
    /**
     * Represents the concept: inactive status.
     */
    public static ConceptSpec INACTIVE_STATUS =
            new ConceptSpec("inactive (inactive status type)",
            UUID.fromString("1464ec56-7118-3051-9d21-0f95c1a39080"));
    /**
     * Represents the concept: moved to.
     */
    public static ConceptSpec MOVED_TO =
            new ConceptSpec("moved elsewhere (inactive status type)",
            UUID.fromString("76367831-522f-3250-83a4-8609ab298436"));
    /**
     * Represents the concept: qualifier.
     */
    public static ConceptSpec REL_QUALIFIER_CHAR =
            new ConceptSpec("qualifier (characteristic type)",
            UUID.fromString("416ad0e4-b6bc-386c-900e-121c58b20f55"));
    /**
     * Represents the concept: historic.
     */
    public static ConceptSpec REL_HISTORIC =
            new ConceptSpec("historical (characteristic type)",
            UUID.fromString("1d054ca3-2b32-3004-b7af-2701276059d5"));
    /**
     * Represents the concept: stated.
     */
    public static ConceptSpec REL_STATED_CHAR =
            new ConceptSpec("stated (defining characteristic type)",
            UUID.fromString("3fde38f6-e079-3cdc-a819-eda3ec74732d"));
    /**
     * Represents the concept: defining.
     */
    public static ConceptSpec REL_INFERED_CHAR =
            new ConceptSpec("defining",
            UUID.fromString("a4c6bf72-8fb6-11db-b606-0800200c9a66"));
    /**
     * Represents the concept: not refinable.
     */
    public static ConceptSpec REL_NOT_REFINABLE =
            new ConceptSpec("not refinable (refinability type)",
            UUID.fromString("e4cde443-8fb6-11db-b606-0800200c9a66"));
    /**
     * Represents the concept: optionally refinable.
     */
    public static ConceptSpec REL_OPTIONALLY_REFINABLE =
            new ConceptSpec("optional (refinability type)",
            UUID.fromString("c3d997d3-b0a4-31f8-846f-03fa874f5479"));
    /**
     * Represents the concept: SNOMED integer id. This is the SCTID Authority.
     */
    public static ConceptSpec SCT_ID_AUTHORITY =
            new ConceptSpec("SNOMED integer id",
            UUID.fromString("0418a591-f75b-39ad-be2c-3ab849326da9"));
    /**
     * Represents the concept: Workbench Auxiliary Path.
     */
    public static ConceptSpec WB_AUX_PATH =
            new ConceptSpec("Workbench Auxiliary",
            UUID.fromString("2faa9260-8fb2-11db-b606-0800200c9a66"));
    /**
     * Represents the concept: Workbench Auxiliary Path.
     */
    public static ConceptSpec SNOMED_CORE_PATH =
            new ConceptSpec("SNOMED Core",
            UUID.fromString("8c230474-9f11-30ce-9cad-185a96fd03a2"));
    /**
     * Represents the concept: path.
     */
    public static ConceptSpec PATH =
            new ConceptSpec("path",
            UUID.fromString("4459d8cf-5a6f-3952-9458-6d64324b27b7"));
    /**
     * Represents the concept: unreviewed.
     */
    public static ConceptSpec UNREVIEWED =
            new ConceptSpec("unreviewed",
            UUID.fromString("854552b2-74b7-3f68-81fc-3211950d2ba9"));
    /**
     * Represents the concept: final.
     */
    public static ConceptSpec FINAL =
            new ConceptSpec("final",
            UUID.fromString("5f00d1a8-0daf-5eed-89ff-49731e1a4e86"));
    /**
     * Represents the concept: ready to promote.
     */
    public static ConceptSpec PROMOTE = 
            new ConceptSpec("ready to promote",
            UUID.fromString("9801e17e-480b-3794-b002-e7de1d2cbb68"));
    /**
     * Represents the concept: ready to promoted.
     */
    public static ConceptSpec PROMOTED = 
            new ConceptSpec("promoted",
            UUID.fromString("96ce7a06-61ae-3c83-aaab-57bef0f56333"));
    /**
     * Represents the concept: user.
     */
    public static ConceptSpec USER =
            new ConceptSpec("user",
            UUID.fromString("f7495b58-6630-3499-a44e-2052b5fcf06c"));
    
    /**
     * Represents the concept: classifier. This is the concept used for the author in the inferred view.
     */
    public static ConceptSpec CLASSIFIER_USER =
            new ConceptSpec("IHTSDO Classifier",
            UUID.fromString("7e87cc5b-e85f-3860-99eb-7a44f2b9e6f9"));
}
