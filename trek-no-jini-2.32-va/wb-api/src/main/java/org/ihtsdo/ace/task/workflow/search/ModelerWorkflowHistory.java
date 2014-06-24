package org.ihtsdo.ace.task.workflow.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.UUID;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.ace.task.gui.component.WorkflowConceptVersion;
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
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
   private WorkflowConceptVersion testModeler = null;

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
        	  this.testModeler = (WorkflowConceptVersion) obj;
          } else {
        	  this.testModeler = null;
          }
         
          if (this.testModeler == null) 
          {
            try 
            {
            	ViewCoordinate vc = Terms.get().getActiveAceFrameConfig().getViewCoordinate();
            	
            	ConceptVersionBI leadModeler = WorkflowHelper.getLeadModeler(vc);

            	if (leadModeler != null) {
            		this.testModeler = new WorkflowConceptVersion(WorkflowHelper.getLeadModeler(vc));
            	} else {
            		Iterator<String> itr = WorkflowHelper.getModelerKeySet().iterator();
            		if (itr.hasNext()) {
            			this.testModeler = new WorkflowConceptVersion(WorkflowHelper.lookupModeler(itr.next()));
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
         UUID testUUID = testModeler.getPrimUuid();

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

   public WorkflowConceptVersion getTestModeler() {
      return testModeler;
   }

   public void setTestModeler(WorkflowConceptVersion testModeler) {
      this.testModeler = testModeler;
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
