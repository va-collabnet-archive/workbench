package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_RelPart;
import org.dwfa.ace.api.I_RelVersioned;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.cement.PrimordialId;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.WorkflowHistoryJavaBean;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = {
   @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN),
   @Spec(directory = "search/workflow", type = BeanType.TASK_BEAN)})
public class ModelerWorkflowHistory extends AbstractWorkflowHistorySearchTest {

   private static final long serialVersionUID = 1;
   private static final int dataVersion = 1;
   /**
    * Property name for the Modeler being searched.
    */
   private I_GetConceptData testModeler = null;

   private void writeObject(ObjectOutputStream out) throws IOException {
      out.writeInt(dataVersion);
      out.writeObject(this.testModeler);
   }

   private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
      int objDataVersion = in.readInt();
      if (objDataVersion == 1) 
      {
    	  Object obj = in.readObject();
    	  
          if (obj instanceof I_GetConceptData) {
        	  this.testModeler = (I_GetConceptData) obj;
          } else {
        	  this.testModeler = null;
          }
         
          if (this.testModeler == null) 
          {
            try 
            {
               I_GetConceptData leadModeler = WorkflowHelper.getLeadModeler();

               if (leadModeler != null) {
                  this.testModeler = WorkflowHelper.getLeadModeler();
               } else {
                  Iterator<String> itr = WorkflowHelper.getModelerKeySet().iterator();
                  if (itr.hasNext()) {
                     this.testModeler = WorkflowHelper.lookupModeler(itr.next());
                  }
               }
            } catch (Exception e) {
            	AceLog.getAppLog().log(Level.WARNING, "Error in initializing drop-down value", e);
            }
         }
      }

   }

   @Override
   public boolean test(SortedSet<WorkflowHistoryJavaBean> wfHistory)
           throws TaskFailedException {

      try {
         UUID testUUID = validateModeler(testModeler);

         if (testUUID == null) {
            return false;
         }

         for (WorkflowHistoryJavaBean wfHistoryItem : wfHistory) {
            if (wfHistoryItem.getModeler().equals(testUUID)) {
               return true;
            }
         }


         return false;

      } catch (Exception e) {
         throw new TaskFailedException("Couldn't read search Modeler!");
      }
   }

   public I_GetConceptData getTestModeler() {
      return testModeler;
   }

   public void setTestModeler(I_GetConceptData testModeler) {
      this.testModeler = testModeler;
   }

   private UUID validateModeler(I_GetConceptData mod) throws IOException, TerminologyException {
      I_GetConceptData cap = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.IHTSDO.getPrimoridalUid());

      final long pathNid = cap.getConceptAttributes().getPathNid();
      final long relTypeNid = Terms.get().getConcept(PrimordialId.IS_A_REL_ID.getUids()).getConceptNid();
      final long currentNid = Terms.get().getConcept(ArchitectonicAuxiliary.Concept.CURRENT.getUids()).getConceptNid();

      for (I_RelVersioned version : cap.getDestRels()) {
         List<? extends I_RelPart> parts = version.getMutableParts();
         for (I_RelPart relAttrPart : parts) {
            if ((relAttrPart.getPathNid() == pathNid)
                    && (relAttrPart.getTypeNid() == relTypeNid)
                    && (relAttrPart.getStatusNid() == currentNid)) {
               if (Terms.get().getConcept(version.getC1Id()).getInitialText().equalsIgnoreCase(mod.getInitialText())) {
                  return Terms.get().nidToUuid(version.getC1Id());
               }
            }
         }
      }

      return null;
   }
   

   public UUID getCurrentTestUUID() throws TaskFailedException {
       return testModeler.getPrimUuid();
   }

   @Override
	public int getTestType() {
		return hasModeler;
	}

	@Override
	public Object getTestValue() {
		return getTestModeler();
	}
}
