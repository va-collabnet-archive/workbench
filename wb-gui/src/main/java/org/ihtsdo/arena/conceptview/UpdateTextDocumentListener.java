/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.ihtsdo.arena.conceptview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConceptAttributeTuple;
import org.dwfa.ace.api.I_ConceptAttributeVersioned;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.tapi.TerminologyException;
import org.ihtsdo.arena.spec.AcceptabilityType;
import org.ihtsdo.tk.Ts;
import org.ihtsdo.tk.api.ComponentVersionBI;
import org.ihtsdo.tk.api.ContraditionException;
import org.ihtsdo.tk.api.TerminologyConstructorBI;
import org.ihtsdo.tk.api.blueprint.InvalidCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB;
import org.ihtsdo.tk.api.blueprint.RefexCAB.RefexProperty;
import org.ihtsdo.tk.api.description.DescriptionAnalogBI;
import org.ihtsdo.tk.api.description.DescriptionVersionBI;
import org.ihtsdo.tk.api.refex.RefexChronicleBI;
import org.ihtsdo.tk.api.refex.RefexVersionBI;
import org.ihtsdo.tk.dto.concept.component.refset.TK_REFSET_TYPE;
import org.ihtsdo.tk.example.binding.WbDescType;
import org.ihtsdo.tk.helper.TerminologyHelperDrools;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.ihtsdo.tk.api.ComponentChroncileBI;

/**
 *
 * @author kec
 */
public class UpdateTextDocumentListener implements DocumentListener, ActionListener {
      
      FixedWidthJEditorPane editorPane;
      DescriptionAnalogBI desc;
      Timer t;
      I_GetConceptData c;
      boolean update = false;
      I_ConfigAceFrame config;;
      
      public UpdateTextDocumentListener(FixedWidthJEditorPane editorPane, 
              DescriptionAnalogBI desc) throws TerminologyException, IOException {
         super();
         this.editorPane = editorPane;
         this.desc = desc;
         t = new Timer(10000, this);
         t.start();
         c = Terms.get().getConcept(desc.getConceptNid());
         
      }
      
      @Override
      public void insertUpdate(DocumentEvent e) {
         update = true;
      }
      
      @Override
      public void removeUpdate(DocumentEvent e) {
         update = true;
      }
      
      @Override
      public void changedUpdate(DocumentEvent e) {
         update = true;
      }
      
      @Override
      public void actionPerformed(ActionEvent e) {
         try{
        	 config = Terms.get().getActiveAceFrameConfig();  
       	  if (update) { //create new
       		  
                update = false;
                 
                Collection<? extends RefexChronicleBI<?>> refexes =  desc.getCurrentAnnotations(config.getViewCoordinate());
                //check for FSN or Pref term
                int type = desc.getTypeNid();
      			int pref = WbDescType.PREFERRED.getLenient().getNid();
      			int fsn = WbDescType.FULLY_SPECIFIED.getLenient().getNid();
      			
      		// do syn Update
            	if(refexes.isEmpty()){ //check for previous changes
            		if (type == fsn){
            			doFsnUpdate();
            		}else{
            			doSynUpdate();
            		}
                	
                }else {
               	   for(RefexChronicleBI<?> descRefex : refexes){
               		   if(descRefex.isUncommitted()){ 
               			List<? extends I_ExtendByRef> extensions = Terms.get().getAllExtensionsForComponent(desc.getNid(), true);
               			  for(I_ExtendByRef ext : extensions){
                  			   Terms.get().forget(ext); 
               			  }
                   	   } 
               	   	}
	               	if (type == fsn){
	        			doFsnUpdate();
	        		}else{
	        			doSynUpdate();
	        		}
                  }
               }
         } catch (IOException ex) {
  			AceLog.getAppLog().alertAndLogException(ex);
  		 } catch (TerminologyException ex) {
  			AceLog.getAppLog().alertAndLogException(ex);
		} catch (PropertyVetoException ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} catch (InvalidCAB ex) {
			AceLog.getAppLog().alertAndLogException(ex);
		} 
		}
      
      private void doFsnUpdate() throws PropertyVetoException, IOException, InvalidCAB {
              TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
  	                config.getViewCoordinate());
              TerminologyHelperDrools th = new TerminologyHelperDrools();

              desc.setText(editorPane.extractText());
              
              if (th.loadProperties()) {
            	   if(th.checkTermSpelling(editorPane.extractText(), "en-gb") && th.checkTermSpelling(editorPane.extractText(), "en-us")){
                	   RefexCAB refexSpecUs = new RefexCAB(
          	                    TK_REFSET_TYPE.CID,
          	                    desc.getNid(),
          	                    Ts.get().getNidForUuids(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")));
          	           refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
          	           RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);
          	           
	          	       RefexCAB refexSpecGb = new RefexCAB(
	       	                    TK_REFSET_TYPE.CID,
	       	                    desc.getNid(),
	       	                    Ts.get().getNidForUuids(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")));
	       	           refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
	       	           RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);
	       	           
	       	           I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
	       	           Ts.get().addUncommitted(refexGb);
	       	           I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
	       	           Ts.get().addUncommitted(refexUs);
                   }if (th.checkTermSpelling(editorPane.extractText(), "en-us")) {
                	   RefexCAB refexSpecUs = new RefexCAB(
       	                    TK_REFSET_TYPE.CID,
       	                    desc.getNid(),
       	                    Ts.get().getNidForUuids(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")));
       	            	refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
       	            	RefexChronicleBI<?> newRefex = tc.construct(refexSpecUs);
       	            	I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
       	            	Ts.get().addUncommitted(refex);
                   }else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) {
                	   RefexCAB refexSpecGb = new RefexCAB(
          	                    TK_REFSET_TYPE.CID,
          	                    desc.getNid(),
          	                    Ts.get().getNidForUuids(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")));
          	           refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.PREF.getLenient().getPrimUuid()));
          	           RefexChronicleBI<?> newRefex = tc.construct(refexSpecGb);
          	           I_GetConceptData refex = Terms.get().getConceptForNid(newRefex.getNid());
          	           Ts.get().addUncommitted(refex);
                   }
                   }
      }
      
      private void doSynUpdate() throws PropertyVetoException, IOException, InvalidCAB {
          TerminologyConstructorBI tc = Ts.get().getTerminologyConstructor(config.getEditCoordinate(),
	                config.getViewCoordinate());
          TerminologyHelperDrools th = new TerminologyHelperDrools();

          desc.setText(editorPane.extractText());
          
          if (th.loadProperties()) {
        	   if(th.checkTermSpelling(editorPane.extractText(), "en-gb") && th.checkTermSpelling(editorPane.extractText(), "en-us")){
            	   RefexCAB refexSpecUs = new RefexCAB(
      	                    TK_REFSET_TYPE.CID,
      	                    desc.getNid(),
      	                    Ts.get().getNidForUuids(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")));
      	           refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
      	           RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);
      	           
          	       RefexCAB refexSpecGb = new RefexCAB(
       	                    TK_REFSET_TYPE.CID,
       	                    desc.getNid(),
       	                    Ts.get().getNidForUuids(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")));
       	           refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
       	           RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);
       	           
       	           I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getNid());
       	           Ts.get().addUncommitted(refexGb);
       	           I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getNid());
       	           Ts.get().addUncommitted(refexUs);
               }if (th.checkTermSpelling(editorPane.extractText(), "en-us")) {
            	   RefexCAB refexSpecUs = new RefexCAB(
     	                    TK_REFSET_TYPE.CID,
     	                    desc.getNid(),
     	                    Ts.get().getNidForUuids(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")));
     	           refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
     	           RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);
     	           
         	       RefexCAB refexSpecGb = new RefexCAB(
      	                    TK_REFSET_TYPE.CID,
      	                    desc.getNid(),
      	                    Ts.get().getNidForUuids(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")));
      	           refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
      	           RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);
    	           
    	           I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
    	           Ts.get().addUncommitted(refexGb);
    	           I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
    	           Ts.get().addUncommitted(refexUs);
               }else if (th.checkTermSpelling(editorPane.extractText(), "en-gb")) {
            	   RefexCAB refexSpecUs = new RefexCAB(
     	                    TK_REFSET_TYPE.CID,
     	                    desc.getNid(),
     	                    Ts.get().getNidForUuids(UUID.fromString("29bf812c-7a77-595d-8b12-ea37c473a5e6")));
     	           refexSpecUs.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
     	           RefexChronicleBI<?> newRefexUs = tc.construct(refexSpecUs);
     	           
         	       RefexCAB refexSpecGb = new RefexCAB(
      	                    TK_REFSET_TYPE.CID,
      	                    desc.getNid(),
      	                    Ts.get().getNidForUuids(UUID.fromString("a0982f18-ec51-56d2-a8b1-6ff8964813dd")));
      	           refexSpecGb.put(RefexProperty.CNID1, Ts.get().getNidForUuids(AcceptabilityType.ACCEPTABLE.getLenient().getPrimUuid()));
      	           RefexChronicleBI<?> newRefexGb = tc.construct(refexSpecGb);
      	           
      	           I_GetConceptData refexGb = Terms.get().getConceptForNid(newRefexGb.getConceptNid());
      	           Ts.get().addUncommitted(refexGb);
      	           I_GetConceptData refexUs = Terms.get().getConceptForNid(newRefexUs.getConceptNid());
      	           Ts.get().addUncommitted(refexUs);
               }
          }         
      }
      
   }