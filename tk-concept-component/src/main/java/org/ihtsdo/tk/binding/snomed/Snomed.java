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
 * The Class Snomed contains
 * <code>ConceptSpec</code> representations of SNOMED hierarchy concepts. These
 * are used by the drools rules for automatons or contextual suggestions in the
 * Arena.
 */
public class Snomed {

    /**
     * Represents the SNOMED concept: is a.
     */
    public static ConceptSpec IS_A =
            new ConceptSpec("Is a (attribute)",
            UUID.fromString("c93a30b9-ba77-3adb-a9b8-4589c9f8fb25"));
    /**
     * Represents the SNOMED concept: body structure.
     */
    public static ConceptSpec BODY_STRUCTURE =
            new ConceptSpec("Body structures",
            UUID.fromString("4be3f62e-28d5-3bb4-a424-9aa7856a1790"));
    /**
     * Represents the SNOMED concept: finding site.
     */
    public static ConceptSpec FINDING_SITE =
            new ConceptSpec("Finding site (attribute)",
            UUID.fromString("3a6d919d-6c25-3aae-9bc3-983ead83a928"));
    /**
     * Represents the SNOMED concept: clinical finding.
     */
    public static ConceptSpec CLINICAL_FINDING =
            new ConceptSpec("Clinical finding (finding)",
            UUID.fromString("bd83b1dd-5a82-34fa-bb52-06f666420a1c"));
    /**
     * Represents the SNOMED concept: fully specified description type.
     */
    public static ConceptSpec FULLY_SPECIFIED_DESCRIPTION_TYPE =
            new ConceptSpec("RF1 fully specified name (description type)",
            UUID.fromString("5e1fe940-8faf-11db-b606-0800200c9a66"));
    /**
     * Represents the SNOMED concept: core namespace.
     */
    public static ConceptSpec CORE =
            new ConceptSpec("Core Namespace",
            UUID.fromString("d6bbe207-7b5c-3e32-a2a1-f9259a7260c1"));
    /**
     * Represents the SNOMED concept: Extension Namespace 1000000.
     */
    public static ConceptSpec EXTENSION_0 =
            new ConceptSpec("Extension Namespace 1000000",
            UUID.fromString("18388bfd-9fab-3581-9e22-cbae53725ef2"));
    /**
     * Represents the SNOMED concept: Extension Namespace 1000013.
     */
    public static ConceptSpec EXTENSION_13 =
            new ConceptSpec("Extension Namespace 1000013",
            UUID.fromString("bb57db0f-def7-3fb7-b7f2-89fa7710bffa"));
    /**
     * Represents the SNOMED concept: concept history attribute.
     */
    public static ConceptSpec CONCEPT_HISTORY_ATTRIB =
            new ConceptSpec("Concept history attribute",
            UUID.fromString("f323b5dd-1f97-3873-bcbc-3563663dda14"));
    /**
     * Represents the SNOMED concept: Pharmaceutical / biologic product.
     */
    public static ConceptSpec PRODUCT =
            new ConceptSpec("Pharmaceutical / biologic product (product)",
            UUID.fromString("5032532f-6b58-31f9-84c1-4a365dde4449"));
    /**
     * Represents the SNOMED concept: inactive concept.
     */
    public static ConceptSpec INACTIVE_CONCEPT =
            new ConceptSpec("Inactive concept (inactive concept)",
            UUID.fromString("f267fc6f-7c4d-3a79-9f17-88b82b42709a"));
    /**
     * Represents the SNOMED concept: SNOMED CT Core.
     */
    public static ConceptSpec CORE_MODULE =
            new ConceptSpec("SNOMED CT core",
            UUID.fromString("1b4f1ba5-b725-390f-8c3b-33ec7096bdca"));
    /**
     * Represents the SNOMED concept: Module. Used when a module associated with an edit is unknown.
     */
    public static ConceptSpec UNSPECIFIED_MODULE =
            new ConceptSpec("Module (core metadata concept)",
            UUID.fromString("40d1c869-b509-32f8-b735-836eac577a67"));
    //Concept Specs for context sensitive role relationships
    /**
     * Represents the SNOMED concept: access.
     */
    public static ConceptSpec ACCESS =
            new ConceptSpec("Access (attribute)",
            UUID.fromString("3f5a4b8c-923b-3df5-9362-67881b729394"));
    /**
     * Represents the SNOMED concept: approach.
     */
    public static ConceptSpec APPROACH =
            new ConceptSpec("Procedural approach (qualifier value)",
            UUID.fromString("2209583c-de0b-376d-9aa0-850c37240788"));
    /**
     * Represents the SNOMED concept: associated finding.
     */
    public static ConceptSpec ASSOCIATED_FINDING =
            new ConceptSpec("Associated finding (attribute)",
            UUID.fromString("b20b664d-2690-3092-a2ef-7f8013b2dad3"));
    /**
     * Represents the SNOMED concept: associated morphology.
     */
    public static ConceptSpec ASSOCIATED_MORPHOLOGY =
            new ConceptSpec("Associated morphology (attribute)",
            UUID.fromString("3161e31b-7d00-33d9-8cbd-9c33dc153aae"));
    /**
     * Represents the SNOMED concept: associated with.
     */
    public static ConceptSpec ASSOCIATED_WITH =
            new ConceptSpec("Associated with (attribute)",
            UUID.fromString("79e34041-f87c-3659-b033-41bdd35bd89e"));
    /**
     * Represents the SNOMED concept: after.
     */
    public static ConceptSpec ASSOCIATED_WITH_AFTER =
            new ConceptSpec("After (attribute)",
            UUID.fromString("fb6758e0-442c-3393-bb2e-ff536711cde7"));
    /**
     * Represents the SNOMED concept: causative agent.
     */
    public static ConceptSpec ASSOCIATED_WITH_AGENT =
            new ConceptSpec("Causative agent (attribute)",
            UUID.fromString("f770e2d8-91e6-3c55-91be-f794ee835265"));
    /**
     * Represents the SNOMED concept: due to.
     */
    public static ConceptSpec ASSOCIATED_WITH_DUE =
            new ConceptSpec("Due to (attribute)",
            UUID.fromString("6525dbf8-c839-3e45-a4bb-8bab7faf7cf9"));
    /**
     * Represents the SNOMED concept: clinical course.
     */
    public static ConceptSpec CLINICAL_COURSE =
            new ConceptSpec("Clinical course (attribute)",
            UUID.fromString("0d8a9cbb-e21e-3de7-9aad-8223c000849f"));
    /**
     * Represents the SNOMED concept: component.
     */
    public static ConceptSpec COMPONENT =
            new ConceptSpec("Component (attribute)",
            UUID.fromString("8f0696db-210d-37ab-8fe1-d4f949892ac4"));
    /**
     * Represents the SNOMED concept: direct substance.
     */
    public static ConceptSpec DIRECT_SUBSTANCE =
            new ConceptSpec("Direct substance (attribute)",
            UUID.fromString("49ee3912-abb7-325c-88ba-a98824b4c47d"));
    /**
     * Represents the SNOMED concept: environment.
     */
    public static ConceptSpec ENVIRONMENT =
            new ConceptSpec("Environment (environment)",
            UUID.fromString("da439d54-0823-3b47-abed-f9ba50791335"));
    /**
     * Represents the SNOMED concept: event.
     */
    public static ConceptSpec EVENT =
            new ConceptSpec("Event (event)",
            UUID.fromString("c7243365-510d-3e5f-82b3-7286b27d7698"));
    /**
     * Represents the SNOMED concept: finding context.
     */
    public static ConceptSpec FINDING_CONTEXT =
            new ConceptSpec("Finding context (attribute)",
            UUID.fromString("2dbbf50e-9e14-382d-80be-ec7a020cb436"));
    /**
     * Represents the SNOMED concept: finding informer.
     */
    public static ConceptSpec FINDING_INFORMER =
            new ConceptSpec("Finding informer (attribute)",
            UUID.fromString("4990c973-2c08-3972-93ed-3ce9cd4e1776"));
    /**
     * Represents the SNOMED concept: finding method.
     */
    public static ConceptSpec FINDING_METHOD =
            new ConceptSpec("Finding method (attribute)",
            UUID.fromString("ee283805-ec23-3e22-8bd0-c739f8cbdd7d"));
    /**
     * Represents the SNOMED concept: has active ingredient.
     */
    public static ConceptSpec HAS_ACTIVE_INGREDIENT =
            new ConceptSpec("Has active ingredient (attribute)",
            UUID.fromString("65bf3b7f-c854-36b5-81c3-4915461020a8"));
    /**
     * Represents the SNOMED concept: has definitional manifestation.
     */
    public static ConceptSpec HAS_DEFINITIONAL_MANIFESTATION =
            new ConceptSpec("Has definitional manifestation (attribute)",
            UUID.fromString("545df979-75ea-3f82-939a-565d032bcdad"));
    /**
     * Represents the SNOMED concept: has dose form.
     */
    public static ConceptSpec HAS_DOSE_FORM =
            new ConceptSpec("Has dose form (attribute)",
            UUID.fromString("072e7737-e22e-36b5-89d2-4815f0529c63"));
    /**
     * Represents the SNOMED concept: has focus.
     */
    public static ConceptSpec HAS_FOCUS =
            new ConceptSpec("Has focus (attribute)",
            UUID.fromString("b610d820-4486-3b5e-a2c1-9b66bc718c6d"));
    /**
     * Represents the SNOMED concept: has intent.
     */
    public static ConceptSpec HAS_INTENT =
            new ConceptSpec("Has intent (attribute)",
            UUID.fromString("4e504dc1-c971-3e20-a4f9-b86d0c0490af"));
    /**
     * Represents the SNOMED concept: has interpretation.
     */
    public static ConceptSpec HAS_INTERPRETATION =
            new ConceptSpec("Has interpretation (attribute)",
            UUID.fromString("993a598d-a95a-3235-813e-59252c975070"));
    /**
     * Represents the SNOMED concept: has specimen.
     */
    public static ConceptSpec HAS_SPECIMEN =
            new ConceptSpec("Has specimen (attribute)",
            UUID.fromString("5ce3e93b-8594-3d38-b410-b06039e63e3c"));
    /**
     * Represents the SNOMED concept: interprets.
     */
    public static ConceptSpec INTERPRETS =
            new ConceptSpec("Interprets (attribute)",
            UUID.fromString("75e0da0c-21ea-301f-a176-bf056788afe5"));
    /**
     * Represents the SNOMED concept: laterality.
     */
    public static ConceptSpec LATERALITY =
            new ConceptSpec("Laterality (attribute)",
            UUID.fromString("26ca4590-bbe5-327c-a40a-ba56dc86996b"));
    /**
     * Represents the SNOMED concept: link assertion.
     */
    public static ConceptSpec LINK_ASSERTION =
            new ConceptSpec("Link assertion (link assertion)",
            UUID.fromString("7f39edac-198d-366d-b8b9-4eab221ee144"));
    /**
     * Represents the SNOMED concept: measurement method.
     */
    public static ConceptSpec MEASUREMENT_METHOD =
            new ConceptSpec("Measurement method (attribute)",
            UUID.fromString("a6e4f659-a4b4-33b7-a75d-4a810167b32a"));
    /**
     * Represents the SNOMED concept: method.
     */
    public static ConceptSpec METHOD =
            new ConceptSpec("Method (attribute)",
            UUID.fromString("d0f9e3b1-29e4-399f-b129-36693ba4acbc"));
    /**
     * Represents the SNOMED concept: morphologic abnormality.
     */
    public static ConceptSpec MORPHOLOGIC_ABNORMALITY =
            new ConceptSpec("Morphologically abnormal structure (morphologic abnormality)",
            UUID.fromString("3d3c4a6a-98d6-3a7c-9e1b-7fabf61e5ca5"));
    /**
     * Represents the SNOMED concept: observable entity.
     */
    public static ConceptSpec OBSERVABLE_ENTITY =
            new ConceptSpec("Observable entity (observable entity)",
            UUID.fromString("d678e7a6-5562-3ff1-800e-ab070e329824"));
    /**
     * Represents the SNOMED concept: occurrence.
     */
    public static ConceptSpec OCCURRENCE =
            new ConceptSpec("Occurrence (attribute)",
            UUID.fromString("d99e2a70-243d-3bf2-967a-faee3265102b"));
    /**
     * Represents the SNOMED concept: organism.
     */
    public static ConceptSpec ORGANISM =
            new ConceptSpec("Organism (organism)",
            UUID.fromString("0bab48ac-3030-3568-93d8-aee0f63bf072"));
    /**
     * Represents the SNOMED concept: pathological process.
     */
    public static ConceptSpec PATHOLOGICAL_PROCESS =
            new ConceptSpec("Pathological process (attribute)",
            UUID.fromString("52542cae-017c-3fc4-bff0-97b7f620db28"));
    /**
     * Represents the SNOMED concept: person.
     */
    public static ConceptSpec PERSON =
            new ConceptSpec("Person (person)",
            UUID.fromString("37c4cc1d-b35c-3080-80ac-b5e3a14c8a4b"));
    /**
     * Represents the SNOMED concept: physical force.
     */
    public static ConceptSpec PHYSICAL_FORCE =
            new ConceptSpec("Physical force (physical force)",
            UUID.fromString("32213bf6-c073-3ce1-b0c7-9463e43af2f1"));
    /**
     * Represents the SNOMED concept: physical object.
     */
    public static ConceptSpec PHYSICAL_OBJECT =
            new ConceptSpec("Physical object (physical object)",
            UUID.fromString("72765109-6b53-3814-9b05-34ebddd16592"));
    /**
     * Represents the SNOMED concept: priority.
     */
    public static ConceptSpec PRIORITY =
            new ConceptSpec("Priority (attribute)",
            UUID.fromString("77d496f0-d56d-3ab1-b3c4-b58969ddd078"));
    /**
     * Represents the SNOMED concept: procedure.
     */
    public static ConceptSpec PROCEDURE =
            new ConceptSpec("Procedure (procedure)",
            UUID.fromString("bfbced4b-ad7d-30aa-ae5c-f848ccebd45b"));
    /**
     * Represents the SNOMED concept: procedure context.
     */
    public static ConceptSpec PROCEDURE_CONTEXT =
            new ConceptSpec("Procedure context (attribute)",
            UUID.fromString("6d2e9614-a93f-3835-b278-01650b17743a"));
    /**
     * Represents the SNOMED concept: procedure device.
     */
    public static ConceptSpec PROCEDURE_DEVICE =
            new ConceptSpec("Procedure device (attribute)",
            UUID.fromString("820447dc-ff12-3902-b752-6e5397d297ef"));
    /**
     * Represents the SNOMED concept: device direct.
     */
    public static ConceptSpec PROCEDURE_DEVICE_DIRECT =
            new ConceptSpec("Direct device (attribute)",
            UUID.fromString("102422d3-6b68-3d16-a756-1df791d91e7f"));
    /**
     * Represents the SNOMED concept: indirect device.
     */
    public static ConceptSpec PROCEDURE_INDIRECT_DEVICE =
            new ConceptSpec("Indirect device (attribute)",
            UUID.fromString("9f4020b4-9949-3448-b43a-f3f5b0d44e2b"));
    /**
     * Represents the SNOMED concept: using access device.
     */
    public static ConceptSpec PROCCEDURE_ACCESS_DEVICE =
            new ConceptSpec("Using access device (attribute)",
            UUID.fromString("857b607c-bed8-3432-b474-1a65e613f242"));
    /**
     * Represents the SNOMED concept: procedure using device.
     */
    public static ConceptSpec PROCEDURE_USING_DEVICE =
            new ConceptSpec("Using device (attribute)",
            UUID.fromString("7ee6ba00-b099-3c34-bfc0-6c9366ad9eae"));
    /**
     * Represents the SNOMED concept: procedure morphology.
     */
    public static ConceptSpec PROCEDURE_MORPHOLOGY =
            new ConceptSpec("Procedure morphology (attribute)",
            UUID.fromString("c6456f56-c088-34f5-a85d-39a5fcf62411"));
    /**
     * Represents the SNOMED concept: direct morphology.
     */
    public static ConceptSpec PROCEDURE_MORPHOLOGY_DIRECT =
            new ConceptSpec("Direct morphology (attribute)",
            UUID.fromString("f28dd2fb-7573-3c53-b42a-c8212c946738"));
    /**
     * Represents the SNOMED concept: indirect morphology.
     */
    public static ConceptSpec PROCEDURE_INDIRECT_MORPHOLOGY =
            new ConceptSpec("Indirect morphology (attribute)",
            UUID.fromString("f941f564-f7b1-3a4f-a2ed-f8e1787ee082"));
    /**
     * Represents the SNOMED concept: procedure site.
     */
    public static ConceptSpec PROCEDURE_SITE =
            new ConceptSpec("Procedure site (attribute)",
            UUID.fromString("78dd0334-4b9e-3c26-9266-356f8c5c43ed"));
    /**
     * Represents the SNOMED concept: procedure site direct.
     */
    public static ConceptSpec PROCEDURE_SITE_DIRECT =
            new ConceptSpec("Procedure site - Direct (attribute)",
            UUID.fromString("472df387-0193-300f-9184-85b59aa85416"));
    /**
     * Represents the SNOMED concept: procedure site indirect.
     */
    public static ConceptSpec PROCEDURE_SITE_INDIRECT =
            new ConceptSpec("Procedure site - Indirect (attribute)",
            UUID.fromString("ac38de9e-2c97-37ed-a3e2-365a87ba1730"));
    /**
     * Represents the SNOMED concept: property.
     */
    public static ConceptSpec PROPERTY =
            new ConceptSpec("Property (attribute)",
            UUID.fromString("066462e2-f926-35d5-884a-4e276dad4c2c"));
    /**
     * Represents the SNOMED concept: qualifier value.
     */
    public static ConceptSpec QUALIFIER_VALUE =
            new ConceptSpec("Qualifier value (qualifier value)",
            UUID.fromString("ed6a9820-ba24-3917-b1b2-151e9c5a7a8d"));
    /**
     * Represents the SNOMED concept: recipient category.
     */
    public static ConceptSpec RECIPIENT_CATEGORY =
            new ConceptSpec("Recipient category (attribute)",
            UUID.fromString("e4233cb6-6b8f-3ae5-85e5-dab691a81ecd"));
    /**
     * Represents the SNOMED concept: revision status.
     */
    public static ConceptSpec REVISION_STATUS =
            new ConceptSpec("Revision status (attribute)",
            UUID.fromString("7b15c5ab-ecf9-3dd4-932c-5e7ce2482ee4"));
    /**
     * Represents the SNOMED concept: route of administration.
     */
    public static ConceptSpec ROUTE_OF_ADMIN =
            new ConceptSpec("Route of administration (attribute)",
            UUID.fromString("ddbb95e5-aaf6-38f3-b400-dcfb1f85be91"));
    /**
     * Represents the SNOMED concept: scale type.
     */
    public static ConceptSpec SCALE_TYPE =
            new ConceptSpec("Scale type (attribute)",
            UUID.fromString("087afdd2-23cd-34c3-93a4-09088dfd480c"));
    /**
     * Represents the SNOMED concept: situation with explicit context.
     */
    public static ConceptSpec SITUATION_WITH_EXPLICIT_CONTEXT =
            new ConceptSpec("Situation with explicit context (situation)",
            UUID.fromString("27d03723-07c3-3de9-828b-76aa05a23438"));
    /**
     * Represents the SNOMED concept: social context.
     */
    public static ConceptSpec SOCIAL_CONTEXT =
            new ConceptSpec("Social context (social concept)",
            UUID.fromString("b89db478-21d5-3e51-972d-6c900f0ec436"));
    /**
     * Represents the SNOMED concept: specimen.
     */
    public static ConceptSpec SPECIMEN =
            new ConceptSpec("Specimen (specimen)",
            UUID.fromString("3680e12d-c14c-39cb-ac89-2ae1fa125d41"));
    /**
     * Represents the SNOMED concept: specimen procedure.
     */
    public static ConceptSpec SPECIMEN_PROCEDURE =
            new ConceptSpec("Specimen procedure (attribute)",
            UUID.fromString("e81aa5e5-fcf6-3329-994d-3154576ac90d"));
    /**
     * Represents the SNOMED concept: specimen source identity.
     */
    public static ConceptSpec SPECIMEN_SOURCE_ID =
            new ConceptSpec("Specimen source identity (attribute)",
            UUID.fromString("4ae2b18c-db93-339c-8a9f-35e027007bf5"));
    /**
     * Represents the SNOMED concept: specimen source morphology.
     */
    public static ConceptSpec SPECIMEN_SOURCE_MORPHOLOGY =
            new ConceptSpec("Specimen source morphology (attribute)",
            UUID.fromString("3dd1e927-005e-30ba-b3a4-0a67d538fefe"));
    /**
     * Represents the SNOMED concept: specimen source topography.
     */
    public static ConceptSpec SPECIMEN_SOURCE_TOPOGRAPHY =
            new ConceptSpec("Specimen source topography (attribute)",
            UUID.fromString("4aafafbc-b21f-30a6-b676-0224e6b001ab"));
    /**
     * Represents the SNOMED concept: specimen substance.
     */
    public static ConceptSpec SPECIMEN_SUBSTANCE =
            new ConceptSpec("Specimen substance (attribute)",
            UUID.fromString("500b618d-2896-36ff-a020-c3c988f816f1"));
    /**
     * Represents the SNOMED concept: subject relationship context.
     */
    public static ConceptSpec SUBJECT_REL_CONTEXT =
            new ConceptSpec("Subject relationship context (attribute)",
            UUID.fromString("cbd2a57c-a28d-3494-9193-2189f2b618a2"));
    /**
     * Represents the SNOMED concept: substance.
     */
    public static ConceptSpec SUBSTANCE =
            new ConceptSpec("Substance (substance)",
            UUID.fromString("95f41098-8391-3f5e-9d61-4b019f1de99d"));
    /**
     * Represents the SNOMED concept: temporal context.
     */
    public static ConceptSpec TEMPORAL_CONTEXT =
            new ConceptSpec("Temporal context (attribute)",
            UUID.fromString("2c6acb71-a375-30b4-952d-63916ed74084"));
    /**
     * Represents the SNOMED concept: time aspect.
     */
    public static ConceptSpec TIME_ASPECT =
            new ConceptSpec("Time aspect (attribute)",
            UUID.fromString("350adfa7-8fd5-3b95-91f2-8119b500a464"));
    /**
     * Represents the SNOMED concept: using energy.
     */
    public static ConceptSpec USING_ENERGY =
            new ConceptSpec("Using energy (attribute)",
            UUID.fromString("3050f9ea-e811-37f2-b132-ffa06afcfbbe"));
    /**
     * Represents the SNOMED concept: using substance.
     */
    public static ConceptSpec USING_SUBSTANCE =
            new ConceptSpec("Using substance (attribute)",
            UUID.fromString("996261c3-3c12-3f09-8f14-e30e85e9e70d"));
//    WAITING FOR THESE CONCEPTS TO BE IN INT DEV
    /**
     * Represents concept: quantity reference set.
     */
    public static ConceptSpec QUANTITY =
		new ConceptSpec("Quantity reference set",
		UUID.fromString("704ceee7-6d19-392f-a0e6-e28dfd444801"));
    /**
     * Represents concept: concentration reference set.
     */
    public static ConceptSpec CONCENTRATION =
		new ConceptSpec("Concentration reference set",
		UUID.fromString("29f8b0a9-daf8-3df8-bc46-710a76298c4c"));
    public static ConceptSpec SOME =
                new ConceptSpec("Some", 
                UUID.fromString("a526d5af-b083-3f20-86af-9a6ce17a8b72"));
    /**
     * Represents the module dependency refset.
     */
    public static ConceptSpec MODULE_DEPENDENCY = new ConceptSpec("Module dependency reference set (foundation metadata concept)",
                UUID.fromString("19076bfe-661f-39c2-860c-8706a37073b0"));
}
