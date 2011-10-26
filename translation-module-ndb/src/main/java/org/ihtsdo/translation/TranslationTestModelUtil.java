package org.ihtsdo.translation;


public class TranslationTestModelUtil {
//	public static List<TerminologyComponent> convertContextualizedDescriptionsToTestModel(I_GetConceptData concept,
//			I_GetConceptData languageRefset) throws TerminologyException, IOException, Exception {
//		List<TerminologyComponent> convertedComponents = new ArrayList<TerminologyComponent>();
//		I_TermFactory tf = Terms.get();
//
//		List<org.ihtsdo.translation.ContextualizedDescription> workbenchContextualizedDescriptions =
//			LanguageUtil.getContextualizedDescriptions(concept.getConceptId(), languageRefset.getConceptId(), true);
//
//		for (org.ihtsdo.translation.ContextualizedDescription workbenchContextualizedDescription : 
//			workbenchContextualizedDescriptions) {
//			ContextualizedDescription testModelContextualizedDescription = new ContextualizedDescription();
//			testModelContextualizedDescription.setId(workbenchContextualizedDescription.getDescriptionVersioned().getUniversal().getDescId().iterator().next());
//			testModelContextualizedDescription.setConceptId(concept.getUids().iterator().next());
//			testModelContextualizedDescription.setText(workbenchContextualizedDescription.getText());
//			testModelContextualizedDescription.setLang(workbenchContextualizedDescription.getLang());
//			testModelContextualizedDescription.setLanguageRefsetId(languageRefset.getUids().iterator().next());
//			testModelContextualizedDescription.setTypeId(tf.getConcept(workbenchContextualizedDescription.getTypeId()).getUids().iterator().next());
//			testModelContextualizedDescription.setDescriptionStatusId(tf.getConcept(workbenchContextualizedDescription.getDescriptionStatusId()).getUids().iterator().next());
//			testModelContextualizedDescription.setInitialCaseSignificant(workbenchContextualizedDescription.isInitialCaseSignificant());
//
//			if (workbenchContextualizedDescription.getAcceptabilityId() != 0) {
//				testModelContextualizedDescription.setAcceptabilityId(tf.getConcept(workbenchContextualizedDescription.getAcceptabilityId()).getUids().iterator().next());
//			}
//			if (workbenchContextualizedDescription.getExtensionStatusId() != 0) {
//				testModelContextualizedDescription.setExtensionStatusId(tf.getConcept(workbenchContextualizedDescription.getExtensionStatusId()).getUids().iterator().next());
//			}
//
//			convertedComponents.add(testModelContextualizedDescription);
//		}
//
//		return convertedComponents;
//	}
//
//	public static List<TerminologyComponent> convertUncommittedContextualizedDescriptionsToTestModel(
//			I_GetConceptData concept, I_GetConceptData languageRefset) 
//			throws TerminologyException, IOException, Exception {
//		List<TerminologyComponent> convertedComponents = new ArrayList<TerminologyComponent>();
//		I_TermFactory tf = Terms.get();
//
//		List<org.ihtsdo.translation.ContextualizedDescription> workbenchContextualizedDescriptions =
//			LanguageUtil.getContextualizedDescriptions(concept.getConceptId(), languageRefset.getConceptId(), true);
//
//		for (org.ihtsdo.translation.ContextualizedDescription workbenchContextualizedDescription : 
//			workbenchContextualizedDescriptions) {
//			ContextualizedDescription testModelContextualizedDescription = new ContextualizedDescription();
//			testModelContextualizedDescription.setId(workbenchContextualizedDescription.getDescriptionVersioned().getUniversal().getDescId().iterator().next());
//			testModelContextualizedDescription.setConceptId(concept.getUids().iterator().next());
//			testModelContextualizedDescription.setText(workbenchContextualizedDescription.getText());
//			testModelContextualizedDescription.setLang(workbenchContextualizedDescription.getLang());
//			testModelContextualizedDescription.setLanguageRefsetId(languageRefset.getUids().iterator().next());
//			testModelContextualizedDescription.setTypeId(tf.getConcept(workbenchContextualizedDescription.getTypeId()).getUids().iterator().next());
//			testModelContextualizedDescription.setAcceptabilityId(tf.getConcept(workbenchContextualizedDescription.getAcceptabilityId()).getUids().iterator().next());
//			testModelContextualizedDescription.setDescriptionStatusId(tf.getConcept(workbenchContextualizedDescription.getDescriptionStatusId()).getUids().iterator().next());
//			testModelContextualizedDescription.setExtensionStatusId(tf.getConcept(workbenchContextualizedDescription.getExtensionStatusId()).getUids().iterator().next());
//			testModelContextualizedDescription.setInitialCaseSignificant(workbenchContextualizedDescription.isInitialCaseSignificant());
//
//			convertedComponents.add(testModelContextualizedDescription);
//		}
//
//		return convertedComponents;
//	}
}
