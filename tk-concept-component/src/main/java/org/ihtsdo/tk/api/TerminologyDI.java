package org.ihtsdo.tk.api;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;

public interface TerminologyDI {

   void addUncommitted(ConceptChronicleBI conceptChronicle) throws IOException;

   void addUncommitted(ConceptVersionBI conceptVersion) throws IOException;
   
   void addUncommittedNoChecks(ConceptChronicleBI conceptChronicle) throws IOException;
   
   void addUncommittedNoChecks(ConceptVersionBI conceptVersion) throws IOException;
   
   /**
    * 
    * @param conceptChronicle
    * @throws IOException
    * @deprecated not in TK3
    */
   @Deprecated
   void writeDirect(ConceptChronicleBI conceptChronicle) throws IOException;

   void commit() throws IOException;

   void cancel() throws IOException;

   void commit(ConceptChronicleBI conceptChronicle) throws IOException;
   
   void commit(ConceptChronicleBI cc, ChangeSetPolicy changeSetPolicy) throws IOException;

   void cancel(ConceptChronicleBI conceptChronicle) throws IOException;

   void commit(ConceptVersionBI conceptVersion) throws IOException;

   void cancel(ConceptVersionBI conceptVersion) throws IOException;

   void addChangeSetGenerator(String key, ChangeSetGeneratorBI changeSetGenerator);

   void removeChangeSetGenerator(String key);

   ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
           File changeSetTempFileName,
           ChangeSetGenerationPolicy changeSetGenerationPolicy);

   Set<PositionBI> getPositionSet(Set<Integer> stampNids) throws IOException;

   Set<PathBI> getPathSetFromSapSet(Set<Integer> stampNids) throws IOException;

   Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException;
   
   PathBI getPath(int pathNid) throws IOException;
}
