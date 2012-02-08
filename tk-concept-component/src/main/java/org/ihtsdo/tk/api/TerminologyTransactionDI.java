package org.ihtsdo.tk.api;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.IsaCoordinate;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;

public interface TerminologyTransactionDI {

   void addUncommitted(ConceptChronicleBI cc) throws IOException;

   void addUncommitted(ConceptVersionBI cv) throws IOException;

   void writeDirect(ConceptChronicleBI cc) throws IOException;

   void commit() throws IOException;

   void cancel() throws IOException;

   void commit(ConceptChronicleBI cc) throws IOException;

   void cancel(ConceptChronicleBI cc) throws IOException;

   void commit(ConceptVersionBI cv) throws IOException;

   void cancel(ConceptVersionBI cv) throws IOException;

   void addChangeSetGenerator(String key, ChangeSetGeneratorBI writer);

   void removeChangeSetGenerator(String key);

   ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
           File changeSetTempFileName,
           ChangeSetGenerationPolicy policy);

   Set<PositionBI> getPositionSet(Set<Integer> sapNids) throws IOException;

   Set<PathBI> getPathSetFromSapSet(Set<Integer> sapNids) throws IOException;

   Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException;
   
   PathBI getPath(int pathNid) throws IOException;
   
   void clearInferredIsaCache();
   
   void addInferredParents(ViewCoordinate vc, IsaCoordinate isac, int cnid, int[] parentNids) throws IOException;
   
   void setIsaCacheAsComplete(IsaCoordinate isac) throws IOException;
}
