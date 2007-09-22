package org.dwfa.mojo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_DescriptionPart;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntList;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_ProcessConcepts;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;

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
    * Version from which to derive date. 
    * 
    * @parameter expression="${project.version}"
    */
   private String version;
   
   private String releaseDate;
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
    * @parameter expression="concepts.txt"
    */
   private String conceptDataFileName;

   /**
    * File name for relationship table data output file
    * 
    * @parameter expression="relationships.txt"
    */
   private String relationshipsDataFileName;

   /**
    * File name for description table data output file
    * 
    * @parameter expression="descriptions.txt"
    */
   private String descriptionsDataFileName;
   
   /**
    * File name for description table data output file
    * 
    * @parameter expression="errorLog.txt"
    */
   private String errorLogFileName;
   
   /**
    * Positions to export data.
    * 
    * @parameter
    * @required
    */
   private PositionDescriptor[] positionsForExport;

   /**
    * Status values to include in export
    * 
    * @parameter
    * @required
    */
   private ConceptDescriptor[] statusValuesForExport;

   /**
    * Set of allowed roots of which all concepts must be 
    * subtypes of to be exported
    * 
    * @parameter
    * @required
    */
   private ConceptDescriptor[] allowedRoots;

   /**
    * The set of relationship types used to compute the subtype relationship
    * 
    * @parameter
    * @required
    */   
   private ConceptDescriptor[] relTypesToAllowedRoot;
   
   private class PrepareConceptData implements I_ProcessConcepts {
      I_TermFactory termFactory;

      private ArrayList<String> conceptUuidDistributionDetails = new ArrayList<String>();

      private ArrayList<String> relationshipUuidDistributionDetails = new ArrayList<String>();

      private ArrayList<String> descriptionUuidDistributionDetails = new ArrayList<String>();

      private int totalConcepts = 0;

      private int conceptsMatched = 0;

      private int conceptsUnmatched = 0;
      
      private int conceptsSuppressed = 0;
      
      private int maxSuppressed = Integer.MAX_VALUE;

      private ArrayList<String> unmatchedConcepts = new ArrayList<String>();
      
      private Set<I_Position> positions;
      
      private I_IntSet allowedStatus;
      
      private Writer errorWriter;

	  private Set<I_GetConceptData> allowedRootsSet;

	  private I_IntSet relTypesToAllowedRootIntSet;
      
      public PrepareConceptData(Set<I_Position> positions, I_IntSet allowedStatus, Set<I_GetConceptData> allowedRootsSet, I_IntSet relTypesToAllowedRootIntSet, Writer errorWriter) {
         super();
         termFactory = LocalVersionedTerminology.get();
         this.positions = positions;
         this.allowedStatus = allowedStatus;
         this.errorWriter = errorWriter;
         this.allowedRootsSet = allowedRootsSet;
         this.relTypesToAllowedRootIntSet = relTypesToAllowedRootIntSet;
      }

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

      public void processConcept(I_GetConceptData concept) throws Exception {
         
         if (conceptsSuppressed > maxSuppressed) {
            return;
         }

         // I_IntSet allowedTypes = termFactory.newIntSet();
         // allowedTypes.add(termFactory.uuidToNative(ArchitectonicAuxiliary.Concept.FULLY_SPECIFIED_DESCRIPTION_TYPE.getUids()));

         totalConcepts++;
         
         boolean allowedRoot = false;
         for (I_GetConceptData root : allowedRootsSet) {
			if (root.isParentOf(concept, allowedStatus, relTypesToAllowedRootIntSet, positions, false) || root.equals(concept)) {
				allowedRoot = true;
				break;
			}
		 }
         
         if (allowedRoot) {
	         /*
	          * Get concept details
	          */
	         if (getUuidBasedConceptDetails(concept, allowedStatus)) {
	            getUuidBasedRelDetails(concept, allowedStatus, null);
	            /*
	             * Get Description details
	             */
	            getUuidBasedDescriptionDetails(concept, allowedStatus, null);
	         }
         } else {
            conceptsSuppressed++;
            getLog().info("Suppressing: " + concept);
         }

      }// End method processConcept

      public String toString() {
         return "prepareConceptData";
      }

      private boolean getUuidBasedConceptDetails(I_GetConceptData concept, I_IntSet allowedStatus) throws IOException, TerminologyException {

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
            errorWriter.append("\n\nnull desc for: " + concept.getUids() + " " + concept.getDescriptions());
            return false;
         } else {
            StringBuilder stringBuilder = new StringBuilder("");
            List<I_ConceptAttributeTuple> matches = concept.getConceptAttributeTuples(allowedStatus, positions);
            if (matches == null || matches.size() == 0) {
               return false;
            }
            conceptsMatched++;
            for (I_ConceptAttributeTuple attribTup : matches) {
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
               //createRecord(stringBuilder, new SimpleDateFormat(dateFormat).format(new Date(ThinVersionHelper.convert(attribTup.getVersion()))));
               //TODO Make the records compute dates from multiple versions...
               createRecord(stringBuilder, releaseDate);
               // End record
               createRecord(stringBuilder, System.getProperty("line.separator"));
            }// End while loop
            conceptUuidDistributionDetails.add(stringBuilder.toString());
         }// End method getUuidBasedConceptDetaiils
         return true;
      }

      private void getUuidBasedRelDetails(I_GetConceptData concept, I_IntSet allowedStatus,
            I_IntSet allowedTypes) throws IOException, TerminologyException {

         for (I_RelVersioned rel: concept.getSourceRels()) {
            for (I_RelPart part: rel.getVersions()) {
               I_Path path = termFactory.getPath(termFactory.getUids(part.getPathId()));
               I_Position partPos = termFactory.newPosition(path, part.getVersion());
               if (allowedStatus.contains(part.getStatusId())) {
                  boolean positionOk = false;
                  for (I_Position exportPos: positions) {
                     if (exportPos.isSubsequentOrEqualTo(partPos)) {
                        positionOk = true;
                        break;
                     }
                  }
                  if (positionOk) {
                     StringBuilder stringBuilder = new StringBuilder();
                     // Relationship ID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(rel.getRelId()).getUids()
                           .get(0));

                     // Concept status
                     createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedConceptStatusId(LocalVersionedTerminology
                           .get().getConcept(part.getStatusId()).getUids()));

                     // Concept Id 1 UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(rel.getC1Id()).getUids().get(0));

                     // Relationship type UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getRelTypeId()).getUids()
                           .get(0));

                     // Concept Id 2 UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(rel.getC2Id()).getUids().get(0));

                     // (Characteristict Type integer)
                     int snomedCharacter = ArchitectonicAuxiliary.getSnomedCharacteristicTypeId(LocalVersionedTerminology
                           .get().getConcept(part.getCharacteristicId()).getUids());
                     if (snomedCharacter == -1) {
                        errorWriter.append("\nNo characteristic mapping for: " + LocalVersionedTerminology
                           .get().getConcept(part.getCharacteristicId()));
                     }
                     createRecord(stringBuilder, snomedCharacter);

                     // Refinability integer
                     createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedRefinabilityTypeId(LocalVersionedTerminology
                           .get().getConcept(part.getRefinabilityId()).getUids()));

                     // Relationship Group
                     createRecord(stringBuilder, part.getGroup());

                     // Amt added
                     // Relationship UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(rel.getRelId()).getUids()
                           .get(0));

                     // Concept1 UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(rel.getC1Id()).getUids().get(0));

                     // Relationship type UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getRelTypeId()).getUids()
                           .get(0));

                     // Concept2 UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(rel.getC2Id()).getUids().get(0));

                     // Characteristic Type UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getCharacteristicId())
                           .getUids().get(0));

                     // Refinability UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getRefinabilityId())
                           .getUids().get(0));

                     // Relationship status UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getStatusId()).getUids()
                           .get(0));

                     // Effective Time
                     //createRecord(stringBuilder, new SimpleDateFormat(dateFormat).format(new Date(ThinVersionHelper
                     //      .convert(part.getVersion()))));

                     //TODO Make the records compute dates from multiple versions...
                     createRecord(stringBuilder, releaseDate);
                     createRecord(stringBuilder, System.getProperty("line.separator"));

                     relationshipUuidDistributionDetails.add(stringBuilder.toString());
                  }
               }
            }
         }
      }// End method getUuidBasedRelDetails

      private void getUuidBasedDescriptionDetails(I_GetConceptData concept, I_IntSet allowedStatus,
            I_IntSet allowedTypes) throws IOException, TerminologyException {
         for (I_DescriptionVersioned desc: concept.getDescriptions()) {
            for (I_DescriptionPart part: desc.getVersions()) {
               I_Path path = termFactory.getPath(termFactory.getUids(part.getPathId()));
               I_Position partPos = termFactory.newPosition(path, part.getVersion());
               if (allowedStatus.contains(part.getStatusId())) {
                  boolean positionOk = false;
                  for (I_Position exportPos: positions) {
                     if (exportPos.isSubsequentOrEqualTo(partPos)) {
                        positionOk = true;
                        break;
                     }
                  }
                  if (positionOk) {
                     StringBuilder stringBuilder = new StringBuilder("");
                     // Snomed core
                     // Description Id
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(desc.getDescId()).getUids()
                           .get(0));

                     // Description Status
                     createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedDescriptionStatusId(LocalVersionedTerminology
                           .get().getConcept(part.getStatusId()).getUids()));

                     // ConceptId
                     createRecord(stringBuilder, concept.getUids().get(0));

                     // Term
                     createRecord(stringBuilder, part.getText());

                     // Case sensitivity
                     createRecord(stringBuilder, part.getInitialCaseSignificant() ? 1 : 0);

                     // Initial Capital Status
                     createRecord(stringBuilder, part.getInitialCaseSignificant() ? 1 : 0);

                     // Description Type
                     createRecord(stringBuilder, ArchitectonicAuxiliary.getSnomedDescriptionTypeId(LocalVersionedTerminology
                           .get().getConcept(part.getTypeId()).getUids()));

                     // Language code
                     createRecord(stringBuilder, part.getLang());

                     // AMT added
                     // Description UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(desc.getDescId()).getUids()
                           .get(0));

                     // Description status UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getStatusId()).getUids()
                           .get(0));

                     // Description type UUID
                     createRecord(stringBuilder, LocalVersionedTerminology.get().getConcept(part.getTypeId()).getUids()
                           .get(0));

                     // ConceptId
                     createRecord(stringBuilder, concept.getUids().get(0));

                     // Effective time
                     //createRecord(stringBuilder, new SimpleDateFormat(dateFormat).format(new Date(ThinVersionHelper
                     //      .convert(part.getVersion()))));
                     //TODO Make the records compute dates from multiple versions...
                     createRecord(stringBuilder, releaseDate);

                     // End record
                     createRecord(stringBuilder, System.getProperty("line.separator"));

                     descriptionUuidDistributionDetails.add(stringBuilder.toString());
                  }
               }
            }
         }
      }// End method getUuidBasedDescriptionDetails

      public int getConceptsSuppressed() {
         return conceptsSuppressed;
      }

      public void setConceptsSuppressed(int conceptsSuppressed) {
         this.conceptsSuppressed = conceptsSuppressed;
      }

   }// End class CheckConceptStatus

   private void createRecord(StringBuilder stringBuilder, Object fieldData) {
      stringBuilder.append(fieldData);
      if (fieldData != System.getProperty("line.separator"))
         stringBuilder.append("\t");
   }

   private void writeToFile(File file, ArrayList<String> data) throws IOException {
      Iterator it = data.iterator();
      Writer writer = new BufferedWriter(new FileWriter(file));
      while (it.hasNext()) {
         writer.write((String) it.next());
      }
      writer.close();
   }// End method writeToFile

   public void execute() throws MojoExecutionException, MojoFailureException {
      try {
         releaseDate = version.split("-")[0] + " 00:00:00";
         
         I_TermFactory termFactory = LocalVersionedTerminology.get();
         HashSet<I_Position> positions = new HashSet<I_Position>(positionsForExport.length);
         for (PositionDescriptor pd: positionsForExport) {
            positions.add(pd.getPosition());
         }
         I_IntSet statusValues = termFactory.newIntSet();
         List<I_GetConceptData> statusValueList = new ArrayList<I_GetConceptData>();
         for (ConceptDescriptor status: statusValuesForExport) {
            I_GetConceptData statusConcept = status.getVerifiedConcept();
            statusValues.add(statusConcept.getConceptId());
            statusValueList.add(statusConcept);
         }
         I_IntSet relTypeIntSet = termFactory.newIntSet();
         List<I_GetConceptData> relTypes = new ArrayList<I_GetConceptData>();
         for (ConceptDescriptor relType: relTypesToAllowedRoot) {
            I_GetConceptData relTypeConcept = relType.getVerifiedConcept();
            relTypeIntSet.add(relTypeConcept.getConceptId());
            relTypes.add(relTypeConcept);
         }
         Set<I_GetConceptData> rootSet = new HashSet<I_GetConceptData>();
         for (ConceptDescriptor root: allowedRoots) {
            rootSet.add(root.getVerifiedConcept());
         }
         getLog().info(" processing concepts for positions: " + positions + " with status: " + statusValueList + ", rel types:" + relTypes + ", roots:" + rootSet);

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
         
         Writer errorWriter = new BufferedWriter(new FileWriter(outputDirectory + errorLogFileName));

         File conceptFile = new File(outputDirectory + conceptDataFileName);
         File relationshipFile = new File(outputDirectory + relationshipsDataFileName);
         File descriptionFile = new File(outputDirectory + descriptionsDataFileName);

         PrepareConceptData pcd = new PrepareConceptData(positions, statusValues, rootSet, relTypeIntSet, errorWriter);
         termFactory.iterateConcepts(pcd);

         writeToFile(conceptFile, pcd.getConceptDistDetails());
         writeToFile(relationshipFile, pcd.getRelationshipDistDetails());
         writeToFile(descriptionFile, pcd.getDescriptionDistDetails());

         errorWriter.close();

      } catch (Exception e) {
         throw new MojoExecutionException(e.getLocalizedMessage(), e);
      }

   }// End method execute

   public PositionDescriptor[] getPositionsForExport() {
      return positionsForExport;
   }

   public void setPositionsForExport(PositionDescriptor[] positionsForExport) {
      this.positionsForExport = positionsForExport;
   }

   public ConceptDescriptor[] getStatusValuesForExport() {
      return statusValuesForExport;
   }

   public void setStatusValuesForExport(ConceptDescriptor[] statusValuesForExport) {
      this.statusValuesForExport = statusValuesForExport;
   }

public ConceptDescriptor[] getAllowedRoots() {
	return allowedRoots;
}

public void setAllowedRoots(ConceptDescriptor[] allowedRoots) {
	this.allowedRoots = allowedRoots;
}

public ConceptDescriptor[] getRelTypesToAllowedRoot() {
	return relTypesToAllowedRoot;
}

public void setRelTypesToAllowedRoot(ConceptDescriptor[] relTypesToAllowedRoot) {
	this.relTypesToAllowedRoot = relTypesToAllowedRoot;
}

}// End class ExportDatabase
