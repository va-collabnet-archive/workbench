/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.ihtsdo.mojo.econcept;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.vodb.types.Position;
import org.ihtsdo.helper.export.ActiveOnlyExport;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.PathBI;
import org.ihtsdo.tk.api.PositionBI;
import org.ihtsdo.tk.api.PositionSet;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.binding.snomed.SnomedMetadataRf2;

/**
 *
 * @goal export-econcepts-latest-only
 *
 * @phase process-resources @requiresDependencyResolution compile
 */
public class ExportEConceptLatestOnly extends AbstractMojo {

   /**
    * Location of the build directory.
    *
    * @parameter default-value="${project.build.directory}"
    */
   private File outputDirectory;

   /**
    * Location of the build directory.
    *
    * @parameter default-value="eConcepts.jbin"
    */
   private String outputFileStr;
   
   /**
    * Location of the build directory.
    *
    * @parameter 
    * @required
    */
   private ConceptDescriptor exportPath;

   //~--- methods -------------------------------------------------------------

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
      File outputFile = new File(outputDirectory, outputFileStr);

      outputFile.getParentFile().mkdirs();

      FileOutputStream fos;

      try {
         fos = new FileOutputStream(outputFile);
      } catch (FileNotFoundException ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      }

      BufferedOutputStream bos = new BufferedOutputStream(fos);
      DataOutputStream     out = new DataOutputStream(bos);

      try {
         NidBitSetBI exclusionSet = Ts.get().getEmptyNidSet();

         exclusionSet.setMember(ArchitectonicAuxiliary.Concept.CTV3_ID.localize().getNid());
         exclusionSet.setMember(ArchitectonicAuxiliary.Concept.SNOMED_RT_ID.localize().getNid());

         Map<UUID, UUID> conversionMap = new HashMap<UUID, UUID>() {
            @Override
            public UUID get(Object o) {
               UUID returnUuid = super.get(o);

               if (returnUuid == null) {
                  returnUuid = (UUID) o;
               }

               return returnUuid;
            }
         };
         ViewCoordinate vc           = Ts.get().getMetadataViewCoordinate();
         ViewCoordinate conceptVc     = new ViewCoordinate(vc);
         conceptVc.getAllowedStatusNids().clear();

         PathBI         path         = Ts.get().getPath(exportPath.getVerifiedConcept().getNid());
         PositionBI     viewPosition = new Position(Long.MAX_VALUE, path);

         conceptVc.setPositionSet(new PositionSet(viewPosition));

         ViewCoordinate relVc     = new ViewCoordinate(vc);
         path         = Ts.get().getPath(exportPath.getVerifiedConcept().getNid());
         viewPosition = new Position(Long.MAX_VALUE, path);

         relVc.setPositionSet(new PositionSet(viewPosition));

         ViewCoordinate descVc     = new ViewCoordinate(vc);
         path         = Ts.get().getPath(exportPath.getVerifiedConcept().getNid());
         viewPosition = new Position(Long.MAX_VALUE, path);

         descVc.setPositionSet(new PositionSet(viewPosition));
         descVc.getAllowedStatusNids().add(Ts.get().getNidForUuids(SnomedMetadataRf2.CONCEPT_NON_CURRENT_RF2.getUuids()));
         ActiveOnlyExport exporter = new ActiveOnlyExport(conceptVc, descVc, relVc, 
                 exclusionSet, out, conversionMap);

         Ts.get().iterateConceptDataInSequence(exporter);
      } catch (Exception ex) {
         throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
      } finally {
         try {
            out.close();
         } catch (IOException ex) {
            throw new MojoExecutionException(ex.getLocalizedMessage(), ex);
         }
      }
   }

   //~--- get methods ---------------------------------------------------------

   public File getOutputDirectory() {
      return outputDirectory;
   }

   public String getOutputFileStr() {
      return outputFileStr;
   }

   //~--- set methods ---------------------------------------------------------

   public void setOutputDirectory(File outputDirectory) {
      this.outputDirectory = outputDirectory;
   }

   public void setOutputFileStr(String outputFileStr) {
      this.outputFileStr = outputFileStr;
   }
}
