package org.dwfa.mojo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.vodb.bind.ThinVersionHelper;

/**
 *  *
 * <h1>ExportDatabase</h1>
 * <br>
 * <p>
 * The <code>ExportDatabase</code> class Exports database tables to flat
 * files.
 * </p>
 * <p>
 * </p>
 * 
 * 
 * <br>
 * <br>
 * 
 * @see <code>org.apache.maven.plugin.AbstractMojo</code>
 * @author PeterVawser
 * @goal exportdata
 */
public class ExportDatabase extends AbstractMojo {

   /**
    * Date format to use in output files
    * 
    * @parameter expression="yyyy.mm.dd hh:mm:ss"
    */
   private String dateFormat;

   /**
    * Location of the directory to output data files to.
    * 
    * @parameter expression="${project.build.directory}"
    * @required
    */
   private String outputDirectory;

   /**
    * File name for concept table data output file
    * 
    * @parameter expression="concepts_amt_standalone_r0_1_20070817.txt"
    */
   private String conceptDataFileName;

   /**
    * File name for relationship table data output file
    * 
    * @parameter expression="relationships_amt_standalone_r0_1_20070817.txt"
    */
   private String relationshipsDataFileName;

   /**
    * File name for description table data output file
    * 
    * @parameter expression="descriptions_amt_standalone_r0_1_20070817.txt"
    */
   private String descriptionsDataFileName;

   private class prepareConceptData implements I_ProcessConcepts {
      I_TermFactory termFactory;

      private ArrayList<String> conceptUuidDistributionDetails = new ArrayList<String>();

      private ArrayList<String> relationshipUuidDistributionDetails = new ArrayList<String>();

      private ArrayList<String> descriptionUuidDistributionDetails = new ArrayList<String>();

      private int totalConcepts = 0;

      private int conceptsMatched = 0;

      private int conceptsUnmatched = 0;

      private ArrayList<String> unmatchedConcepts = new ArrayList<String>();

      public ArrayList<String> getUnmatchedConcepts() {
         return unmatchedConcepts;
      }

      public int getTotals() {
         return totalConcepts;
      }

      public int getmatched() {
         return conceptsMatched;
      }

      public int getUnmatched() {
         return conceptsUnmatched;
      }

      public ArrayList<String> getConceptDistDetails() {
         return conceptUuidDistributionDetails;
      }

      public ArrayList<String> getRelationshipDistDetails() {
         return relationshipUuidDistributionDetails;
      }

      public ArrayList<String> getDescriptionDistDetails() {
         return descriptionUuidDistributionDetails;
      }

      public prepareConceptData() throws Exception {
         termFactory = LocalVersionedTerminology.get();

      }

      public void processConcept(I_GetConceptData concept) throws Exception {
         // I_Path architectonicPath = termFactory.getPath(
         // ArchitectonicAuxiliary.
         // Concept.ARCHITECTONIC_BRANCH.
         // getUids());
         //
         // I_Position latestOnArchitectonicPath = termFactory.newPosition(
         // architectonicPath,
         // Integer.MAX_VALUE);
         // Set<I_Position> positions = new HashSet<I_Position>();
         // positions.add(latestOnArchitectonicPath);

         I_IntSet allowedStatus = termFactory.newIntSet();
         allowedStatus.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT.getUids()));
         allowedStatus.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.CURRENT_UNREVIEWED.getUids()));

         // I_IntSet allowedTypes = termFactory.newIntSet();
         // allowedTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

         totalConcepts++;
         /*
          * Get concept details
          */
         if (getUuidBasedConceptDetails(concept, allowedStatus, null)) {
            /*
             * Get relationship details
             */
            List<I_RelTuple> relationshipTuples = concept.getSourceRelTuples(allowedStatus, null, null, false);
            getUuidBasedRelDetails(relationshipTuples);

            /*
             * Get Description details
             */
            getUuidBasedDescriptionDetails(concept, allowedStatus, null, null);
         }


      }// End method processConcept

      public String toString() {
         return "prepareConceptData";
      }

      private boolean getUuidBasedConceptDetails(I_GetConceptData concept, I_IntSet allowedStatus,
            Set<I_Position> positions) throws IOException, TerminologyException {

         // I_IntSet allowedTypes = termFactory.newIntSet();
         // allowedTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

         // if(!attribTuplesIt.hasNext() || !descTuplesIt.hasNext()){
         // conceptsUnmatched++;
         // while(descTuplesIt.hasNext()){
         // I_DescriptionTuple descTup = descTuplesIt.next();
         // unmatchedConcepts.add(descTup.getText());
         // }
         // descTuplesIt = descriptionTuples.iterator();
         // }

         I_IntList fsOrder = LocalVersionedTerminology.get().newIntList();

         fsOrder.add(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.localize().getNid());
         fsOrder.add(ArchitectonicAuxiliary.Concept.XHTML_FULLY_SPECIFIED_DESC_TYPE.localize().getNid());
         fsOrder.add(ArchitectonicAuxiliary.Concept.PREFERRED_DESCRIPTION_TYPE.localize().getNid());
         fsOrder.add(ArchitectonicAuxiliary.Concept.XHTML_PREFERRED_DESC_TYPE.localize().getNid());

         I_DescriptionTuple descForConceptFile = concept.getDescTuple(fsOrder, null, positions);
         if (descForConceptFile == null) {
            //
            System.out.println(" null desc for: " + concept.getUids() + " " + concept.getDescriptions());
            return false;
         } else {
            StringBuilder stringBuilder = new StringBuilder("");
            for (I_ConceptAttributeTuple attribTup : concept.getConceptAttributeTuples(allowedStatus, positions)) {
               conceptsMatched++;
               // Snomed core
               // ConceptId
               createRecord(stringBuilder, concept.getUids().get(0));

               // Concept status
               createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedConceptStatusId(LocalVersionedTerminology
                     .get().getConcept(attribTup.getConceptStatus()).getUids()));

               // Fully specified name
               createRecord(stringBuilder, descForConceptFile.getText());
               // createRecord(stringBuilder, descriptionTuples.get(0).getText()
               // );

               // CTV3ID... We ignore this for now.
               createRecord(stringBuilder, "null");

               // SNOMED 3 ID... We ignore this for now.
               createRecord(stringBuilder, "null");

               // IsPrimative value
               createRecord(stringBuilder, attribTup.isDefined() ? 0 : 1);

               // AMT added
               // Concept UUID
               createRecord(stringBuilder, concept.getUids().get(0));

               // ConceptStatusId
               createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(attribTup.getConceptStatus())
                     .getUids().get(0));

               // Effective time
               createRecord(stringBuilder, new SimpleDateFormat(dateFormat).format(new Date(ThinVersionHelper
                     .convert(attribTup.getVersion()))));
               // End record
               createRecord(stringBuilder, System.getProperty("line.separator"));
            }// End while loop
            conceptUuidDistributionDetails.add(stringBuilder.toString());
         }// End method getUuidBasedConceptDetaiils
         return true;
      }

      private void getUuidBasedRelDetails(List<I_RelTuple> relationshipTuples) throws IOException, TerminologyException {
         Iterator<I_RelTuple> relTuplesIt = relationshipTuples.iterator();
         StringBuilder stringBuilder = new StringBuilder();

         while (relTuplesIt.hasNext()) {
            I_RelTuple relTuple = relTuplesIt.next();
            LocalVersionedTerminology.get().getConcept(relTuple.getC1Id()).getUids();

            // Relationship ID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getRelId()).getUids()
                  .get(0));

            // Concept Id 1 UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getC1Id()).getUids().get(0));

            // Relationship type UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getRelTypeId()).getUids()
                  .get(0));

            // Concept Id 2 UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getC2Id()).getUids().get(0));

            // (Characteristict Type integer)
            createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(LocalVersionedTerminology
                  .get().getConcept(relTuple.getCharacteristicId()).getUids()));

            // Refinability integer
            createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedRefinabilityTypeId(LocalVersionedTerminology
                  .get().getConcept(relTuple.getRefinabilityId()).getUids()));

            // Relationship Group
            createRecord(stringBuilder, relTuple.getGroup());

            // Amt added
            // Relationship UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getRelId()).getUids()
                  .get(0));

            // Concept1 UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getC1Id()).getUids().get(0));

            // Relationship type UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getRelTypeId()).getUids()
                  .get(0));

            // Concept2 UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getC2Id()).getUids().get(0));

            // Characteristic Type UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getCharacteristicId())
                  .getUids().get(0));

            // Refinability UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getRefinabilityId())
                  .getUids().get(0));

            // Relationship status UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(relTuple.getStatusId()).getUids()
                  .get(0));

            // Effective Time
            createRecord(stringBuilder, new SimpleDateFormat(dateFormat).format(new Date(ThinVersionHelper
                  .convert(relTuple.getVersion()))));

            createRecord(stringBuilder, System.getProperty("line.separator"));

            relationshipUuidDistributionDetails.add(stringBuilder.toString());

         }// End while

         relationshipUuidDistributionDetails.add(stringBuilder.toString());
      }// End method getUuidBasedRelDetails

      private void getUuidBasedDescriptionDetails(I_GetConceptData concept, I_IntSet allowedStatus,
            I_IntSet allowedTypes, Set<I_Position> positions) throws IOException, TerminologyException {
         List<I_DescriptionTuple> descriptionTuples = concept.getDescriptionTuples(allowedStatus, allowedTypes,
               positions);
         Iterator<I_DescriptionTuple> descTuplesIt = descriptionTuples.iterator();
         StringBuilder stringBuilder = new StringBuilder("");

         while (descTuplesIt.hasNext()) {
            I_DescriptionTuple descTuple = descTuplesIt.next();

            // Snomed core
            // Description Id
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(descTuple.getDescId()).getUids()
                  .get(0));

            // Description Status
            createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedDescriptionStatusId(LocalVersionedTerminology
                  .get().getConcept(descTuple.getStatusId()).getUids()));

            // ConceptId
            createRecord(stringBuilder, concept.getUids().get(0));

            // Term
            createRecord(stringBuilder, descTuple.getText());

            // Case sensitivity
            createRecord(stringBuilder, descTuple.getInitialCaseSignificant() ? 1 : 0);

            // Initial Capital Status
            createRecord(stringBuilder, descTuple.getInitialCaseSignificant() ? 1 : 0);

            // Description Type
            createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedDescriptionTypeId(LocalVersionedTerminology
                  .get().getConcept(descTuple.getTypeId()).getUids()));

            // Language code
            createRecord(stringBuilder, descTuple.getLang());

            // AMT added
            // Description UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(descTuple.getDescId()).getUids()
                  .get(0));

            // Description status UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(descTuple.getStatusId()).getUids()
                  .get(0));

            // Description type UUID
            createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(descTuple.getStatusId()).getUids()
                  .get(0));

            // ConceptId
            createRecord(stringBuilder, concept.getUids().get(0));

            // Effective time
            createRecord(stringBuilder, new SimpleDateFormat(dateFormat).format(new Date(ThinVersionHelper
                  .convert(descTuple.getVersion()))));

            // End record
            createRecord(stringBuilder, System.getProperty("line.separator"));

            descriptionUuidDistributionDetails.add(stringBuilder.toString());
         }// End while loop
      }// End method getUuidBasedDescriptionDetails

   }// End class CheckConceptStatus

   private void createRecord(StringBuilder stringBuilder, Object fieldData) {
      stringBuilder.append(fieldData);
      if (fieldData != System.getProperty("line.separator"))
         stringBuilder.append("\t");
   }

   private void writeToFile(File file, ArrayList<String> data) throws IOException {
      Iterator it = data.iterator();
      FileWriter fileWriter = new FileWriter(file);
      while (it.hasNext()) {
         fileWriter.write((String) it.next());
      }

      fileWriter.close();

   }// End method writeToFile

   public void execute() throws MojoExecutionException, MojoFailureException {
      try {
         I_TermFactory termFactory = LocalVersionedTerminology.get();
         prepareConceptData pcd = new prepareConceptData();
         termFactory.iterateConcepts(pcd);

         // getLog().info("---------------------------------------");
         // getLog().info("--- total concepts == " + pcd.getTotals() +" ---");
         // getLog().info("--- matched concepts == "+ pcd.getmatched() +" ---");
         // getLog().info("--- unmatched concepts == "+ pcd.getUnmatched() +"
         // ---");
         // ArrayList<String> unmatched = pcd.getUnmatchedConcepts();
         // Iterator it = unmatched.iterator();
         // while(it.hasNext()){
         // String s = (String)it.next();
         // getLog().info("--- "+s+" ---");
         // }
         // getLog().info("---------------------------------------");

         File conceptFile = new File(outputDirectory + conceptDataFileName);
         File relationshipFile = new File(outputDirectory + relationshipsDataFileName);
         File descriptionFile = new File(outputDirectory + descriptionsDataFileName);

         writeToFile(conceptFile, pcd.getConceptDistDetails());
         writeToFile(relationshipFile, pcd.getRelationshipDistDetails());
         writeToFile(descriptionFile, pcd.getDescriptionDistDetails());

      } catch (Exception e) {
         throw new MojoExecutionException(e.getLocalizedMessage(), e);
      }

   }// End method execute

}// End class ExportDatabase
