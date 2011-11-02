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

import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPartCid;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentChroncileBI;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ConceptFetcherBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.NidBitSetBI;
import org.ihtsdo.tk.api.ProcessUnfetchedConceptDataBI;
import org.ihtsdo.tk.api.concept.ConceptChronicleBI;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.tk.api.description.DescriptionChronicleBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.ihtsdo.tk.api.media.MediaChronicleBI;
import org.ihtsdo.tk.api.media.MediaVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.api.relationship.RelationshipChronicleBI;
import org.ihtsdo.tk.api.relationship.RelationshipVersionBI;
import org.ihtsdo.tk.dto.concept.component.description.TkDescription;
import org.ihtsdo.tk.dto.concept.component.media.TkMedia;
import org.ihtsdo.tk.dto.concept.component.refset.TkRefsetAbstractMember;
import org.ihtsdo.tk.dto.concept.component.relationship.TkRelationship;

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
   
   //~--- methods -------------------------------------------------------------
   private void addToNidList(ComponentChroncileBI component) throws IOException {
	   try{
		   if (component != null) {
			   ComponentVersionBI compBI= component.getPrimordialVersion();
			   Set<Integer>  d = compBI.getAllNidsForVersion();
			   for (int nid : d) {
	               if (Ts.get().getComponent(nid) == null) {
	            	   //System.out.println("Null component for : " + nid);
	            	   incorrectNids.add(nid);
	               }
	           }
		   }else{
			   incorrectNids.add(component.getNid());
		   }
	   } catch(NullPointerException ne){
		   // just ignore
		   //System.out.println("complete compnent is null");
	   }catch(Exception e){
		   // just ignore
	   }
	   
	}
   
   @Override
   public boolean continueWork() {
      return true;
   }

   private void processConcept(ConceptChronicleBI concept) throws IOException {
	   // concept attributtes
       addToNidList(concept.getConAttrs());

       // descriptions
       for (DescriptionChronicleBI desc : concept.getDescs()) {
    	   addToNidList(desc);
       }

       // relationships
       for (RelationshipChronicleBI rel : concept.getRelsOutgoing()) {
    	   addToNidList(rel);
       }

       // refset members
       if (!concept.isAnnotationStyleRefex()) {
           for (RefexChronicleBI refex : concept.getRefsetMembers()) {
        	   addToNidList(refex);
           }
       }
       
       // media
       for (MediaChronicleBI media : concept.getMedia()) {
    	   addToNidList(media);
       }

      
	   
           /*Collection<? extends ConceptVersionBI> conceptVersionBILst= concept.getVersions();
           if (conceptVersionBILst != null) {
		         for (ConceptVersionBI conceptVersionBI : conceptVersionBILst) {
				   try {
					 Collection<? extends DescriptionVersionBI> activeDescriptions = conceptVersionBI.getDescsActive();
					 //Collection<? extends DescriptionVersionBI> allDescriptions = (Collection<? extends DescriptionVersionBI>) conceptVersionBI.getDescs();
					 if (activeDescriptions != null) {
				         for (DescriptionVersionBI d : activeDescriptions) {
				            for (int nid : d.getAllNidsForVersion()) {
				               if (Ts.get().getComponent(nid) == null) {
				            	   System.out.println("Null component for desc: " + d);
				            	   incorrectNids.add(nid);
				               }
				            }				            
				         }
				      } 
						 
					 Collection<? extends RelationshipVersionBI> rels = conceptVersionBI.getRelsOutgoingActive();

				      if (rels != null) {
				         for (RelationshipVersionBI d : rels) {
				            for (int nid : d.getAllNidsForVersion()) {
				               if (Ts.get().getComponent(nid) == null){
				            	   System.out.println("Null component for Rel: " + d);
				            	   incorrectNids.add(nid);
				               }
				            }
				         }
				      }
					   
				      Collection<? extends RefexVersionBI<?>> activeRefsetMembers = conceptVersionBI.getRefsetMembersActive();
				     //Collection<? extends RefexVersionBI<?>> allRefsetMembers = (Collection<? extends RefexVersionBI<?>>) conceptVersionBI.getRefsetMembers();
						
				     if (activeRefsetMembers != null) {
				         for (RefexVersionBI d : activeRefsetMembers) {
				            for (int nid : d.getAllNidsForVersion()) {
				               if (Ts.get().getComponent(nid) == null){
			                     System.out.println("Null component for Refset: " + d);
			                     incorrectNids.add(nid);
				               }
				            }
				         }
				    }
					
					Collection<? extends MediaVersionBI> activeMedia = conceptVersionBI.getMediaActive();
					//Collection<? extends MediaVersionBI> allMedia = (Collection<? extends MediaVersionBI>) conceptVersionBI.getMedia();
				     if (activeMedia != null) {
				         for (MediaVersionBI d : activeMedia) {
				            for (int nid : d.getAllNidsForVersion()) {
				               if (Ts.get().getComponent(nid) == null) {
				            	   System.out.println("Null component for Media: " + d);
				            	   incorrectNids.add(nid);
				               }
				            }
				        }
				     }
				   } catch (Exception e) {
						e.printStackTrace();
						System.out.println(e.getMessage());
				   }
			   }
		   }*/
		   
		   // Original logic to check only for refset members
	   		try{
		     List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(concept.getNid(), true);
			     for (I_ExtendByRef extension : extensions) {
			    	I_ExtendByRefPartCid part = (I_ExtendByRefPartCid) extension.getMutableParts().get(0);
			     	try{
			  	    	UUID c1Uuid  = Terms.get().nidToUuid(part.getC1id());
				  	}catch(Exception e){
				  	    incorrectNids.add(part.getC1id());
				  	}
			     }
		   }catch(Exception e){
		    	//don't do anything , just ignore
		   }
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
