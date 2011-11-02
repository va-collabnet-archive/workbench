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



package org.ihtsdo.helper.bdb;

//~--- non-JDK imports --------------------------------------------------------

import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;

/**
 *
 * @author kec
 */
public class NidUuidFinder implements ProcessUnfetchedConceptDataBI {
   private AtomicInteger       count        = new AtomicInteger(0);
   private AtomicInteger       dots         = new AtomicInteger(0);
   ConcurrentSkipListSet<Integer> allPrimNids = new ConcurrentSkipListSet<Integer>();
   ConcurrentSkipListSet<Integer> incorrectNids     = new ConcurrentSkipListSet<Integer>();
   File                           missingNidFile     = new File("incorrectNid.oos");
   
   private final NidBitSetBI   nidset;

   //~--- constant enums ------------------------------------------------------

   private enum PASS { PASS_ONE, PASS_TWO }

   //~--- constructors --------------------------------------------------------
   public NidUuidFinder() throws IOException, ClassNotFoundException {
      nidset = Ts.get().getAllConceptNids();
   }
   
   @Override
   public boolean continueWork() {
      return true;
   }

   private void processConcept(ConceptChronicleBI concept) throws IOException {
// Incomplete. 
//      try{
//	     Collection<? extends RefexChronicleBI> extensions = concept.getRefexes();
//	     for (RefexChronicleBI extension : extensions) {
//	    	I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) extension.getMutableParts().get(0);
//	     	try{
//	  	    	UUID c1Uuid  = Terms.get().nidToUuid(part.getC1id());
//		  	}catch(Exception e){
//		  	    incorrectNids.add(part.getC1id());
//		  	}
//	     }
//	     }catch(Exception e){
//	    	//don't do anything , just ignore
//	     }
   }

   @Override
   public void processUnfetchedConceptData(int cNid, ConceptFetcherBI fetcher) throws Exception {
      count.incrementAndGet();

      if (count.get() % 1000 == 0) {
         System.out.print(".");
         System.out.flush();
         dots.incrementAndGet();

         if (dots.get() > 80) {
            dots.set(0);
            System.out.println();
            System.out.print(count.get() + ": ");
         }
      }

      processConcept(fetcher.fetch());
   }

   public void writeIncorrectNidFile() throws IOException {
      count.set(0);
      FileOutputStream     fosNid = new FileOutputStream(missingNidFile);
      BufferedOutputStream bosNid = new BufferedOutputStream(fosNid);
      ObjectOutputStream   oosNid = new ObjectOutputStream(bosNid);

      try {
         oosNid.writeObject(incorrectNids);
      } finally {
    	  oosNid.close();
      }
   }

   //~--- get methods ---------------------------------------------------------

   @Override
   public NidBitSetBI getNidSet() throws IOException {
      return nidset;
   }
   
   
   public ConcurrentSkipListSet<Integer> getincorrectNids() {
	  return incorrectNids;
   } 
   
}
