/**
 * Copyright (c) 2012 International Health Terminology Standards Development
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
package org.ihtsdo.tk.api;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.ihtsdo.tk.api.changeset.ChangeSetGenerationPolicy;
import org.ihtsdo.tk.api.changeset.ChangeSetGeneratorBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.cs.ChangeSetPolicy;
import org.ihtsdo.tk.api.search.ScoredComponentReference;

// TODO: Auto-generated Javadoc
/**
 * The Interface TerminologyDI.
 */
public interface TerminologyDI {
    
   /**
    * Do text search.
    *
    * @param query the query
    * @return the collection
    * @throws IOException Signals that an I/O exception has occurred.
    * @throws ParseException the parse exception
    */
   Collection<ScoredComponentReference> doTextSearch(String query) throws IOException, ParseException;;

   /**
    * Adds the uncommitted.
    *
    * @param conceptChronicle the concept chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void addUncommitted(ConceptChronicleBI conceptChronicle) throws IOException;

   /**
    * Adds the uncommitted.
    *
    * @param conceptVersion the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void addUncommitted(ConceptVersionBI conceptVersion) throws IOException;
   
   /**
    * Adds the uncommitted no checks.
    *
    * @param conceptChronicle the concept chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void addUncommittedNoChecks(ConceptChronicleBI conceptChronicle) throws IOException;
   
   /**
    * Adds the uncommitted no checks.
    *
    * @param conceptVersion the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void addUncommittedNoChecks(ConceptVersionBI conceptVersion) throws IOException;
   
   /**
    * Write direct.
    *
    * @param conceptChronicle the concept chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    * @deprecated not in TK3
    */
   @Deprecated
   void writeDirect(ConceptChronicleBI conceptChronicle) throws IOException;

   /**
    * Commit.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void commit() throws IOException;

   /**
    * Cancel.
    *
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void cancel() throws IOException;

   /**
    * Commit.
    *
    * @param conceptChronicle the concept chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void commit(ConceptChronicleBI conceptChronicle) throws IOException;
   
   /**
    * Commit.
    *
    * @param cc the cc
    * @param changeSetPolicy the change set policy
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void commit(ConceptChronicleBI cc, ChangeSetPolicy changeSetPolicy) throws IOException;

   /**
    * Cancel.
    *
    * @param conceptChronicle the concept chronicle
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void cancel(ConceptChronicleBI conceptChronicle) throws IOException;

   /**
    * Commit.
    *
    * @param conceptVersion the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void commit(ConceptVersionBI conceptVersion) throws IOException;

   /**
    * Cancel.
    *
    * @param conceptVersion the concept version
    * @throws IOException Signals that an I/O exception has occurred.
    */
   void cancel(ConceptVersionBI conceptVersion) throws IOException;

   /**
    * Adds the change set generator.
    *
    * @param key the key
    * @param changeSetGenerator the change set generator
    */
   void addChangeSetGenerator(String key, ChangeSetGeneratorBI changeSetGenerator);

   /**
    * Removes the change set generator.
    *
    * @param key the key
    */
   void removeChangeSetGenerator(String key);

   /**
    * Creates the dto change set generator.
    *
    * @param changeSetFileName the change set file name
    * @param changeSetTempFileName the change set temp file name
    * @param changeSetGenerationPolicy the change set generation policy
    * @return the change set generator bi
    */
   ChangeSetGeneratorBI createDtoChangeSetGenerator(File changeSetFileName,
           File changeSetTempFileName,
           ChangeSetGenerationPolicy changeSetGenerationPolicy);

   /**
    * Gets the position set.
    *
    * @param stampNids the stamp nids
    * @return the position set
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Set<PositionBI> getPositionSet(Set<Integer> stampNids) throws IOException;

   /**
    * Gets the path set from sap set.
    *
    * @param stampNids the stamp nids
    * @return the path set from sap set
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Set<PathBI> getPathSetFromSapSet(Set<Integer> stampNids) throws IOException;

   /**
    * Gets the path set from position set.
    *
    * @param positions the positions
    * @return the path set from position set
    * @throws IOException Signals that an I/O exception has occurred.
    */
   Set<PathBI> getPathSetFromPositionSet(Set<PositionBI> positions) throws IOException;
   
   /**
    * Gets the path.
    *
    * @param pathNid the path nid
    * @return the path
    * @throws IOException Signals that an I/O exception has occurred.
    */
   PathBI getPath(int pathNid) throws IOException;
   
     
   /**
    * Gets the native id from alternate id.
    *
    * @param authorityUuid the authority uuid
    * @param altId the alt id
    * @return the native id from alternate id
    * @throws IOException Signals that an I/O exception has occurred.
    */
   int getNidFromAlternateId(UUID authorityUuid, String altId) throws IOException;

}
