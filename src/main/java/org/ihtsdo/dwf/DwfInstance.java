/*
 * Copyright 2011 International Health Terminology Standards Development Organisation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ihtsdo.dwf;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A class that encapsulates an instance
 *
 * @author kec
 */
public abstract class DwfInstance implements Serializable {

   /**
    * Unique identifier of the process this object is an instance of.
    */
   private UUID processUuid;
   /**
    * Unique identifier for this instance of the process;
    */
   private UUID instanceUuid;
   /**
    * The drools flow language that defines the workflow of this instance.
    */
   private String dfl;
   /**
    * Instance specific data that this workflow instance requires.
    */
   private Map<String, Object> data;
   /**
    * Keys of the data that need to be loaded into working memory prior
    * executing the drools flow.
    */
   private Set<String> workingMemoryKeys;
   /**
    * Data elements whose toString() method should be called
    * to include their end-execution values in the execution log.
    */
   private Set<String> historyDataKeys;
   /**
    * XML execution log.
    *
    * TODO: should this be an object instead of XML?
    */
   private Set<String> executionLog;


   /**
    * TODO: should this be an enum?
    * Key for the deadline to display in the queue
    * The drools execution update this value.
    */
   public static final String deadlineKey = "org.ihtsdo.dwf.deadlineKey";
   /**
    * TODO: should this be an enum?
    * Key for the priority type to display in the queue
    * The drools execution update this value.
    */
   private static final String priorityKey = "org.ihtsdo.dwf.priorityKey";
   /**
    * TODO: should this be an enum?
    * Key for the origin to display in the queue
    * The drools execution update this value.
    */
   private static final String originKey = "org.ihtsdo.dwf.originKey";
   /**
    * TODO: should this be an enum?
    * Key for the destination to deliver to
    * The drools execution update this value.
    */
   private static final String destinationKey = "org.ihtsdo.dwf.destinationKey";
   /**
    * TODO: should this be an enum?
    * Key for the subject to display in the queue
    * The drools execution update this value.
    */
   private static final String subjectKey = "org.ihtsdo.dwf.subjectKey";
   /**
    * TODO: should this be an enum?
    * Key for the message type to display in the queue
    * The drools execution update this value.
    */
   private static final String messageKey = "org.ihtsdo.dwf.messageKey";

   public void execute(Object worker) throws Exception {
      this.evaluate(worker);
      this.complete(worker);
   }

   /**
    * Execution of this instance in the Drools engine on the client.
    * @param worker
    */
   public abstract void evaluate(Object worker);

   /**
    * Actions to be completed after state of instance is final (delivery, or
    * write to disk)
    *
    * @param worker
    */
   public abstract void complete(Object worker);
}
