/**
 * Copyright (c) 2009 International Health Terminology Standards Development
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
package org.ihtsdo.ace.task;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;

import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefset;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = { @Spec(directory = "tasks/workflow", type = BeanType.TASK_BEAN) })
public class UpdateEditorCategoryRefset extends AbstractTask {
 
    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {
            //
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }
    
    
    
    
    /**
     * @TODO use a type 1 uuid generator instead of a random uuid...
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException 
    {
    	String line = "";
    	
    	 try {
             EditorCategoryRefset refset = new EditorCategoryRefset();
             I_TermFactory tf = Terms.get();

             HashMap<String, I_GetConceptData> modelers = new HashMap<String, I_GetConceptData>();
         	 refset = new EditorCategoryRefset();
             EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();
             File f= new File("workflow/userPermissionRefset.txt");

             Scanner scanner = new Scanner(f);

          	 WorkflowHelper.updateModelers();
             modelers = WorkflowHelper.getModelers();

             while (scanner.hasNextLine())
             {
             	line = scanner.nextLine();

             	String[] columns = line.split(",");
             	//Get rid of "User permission"
             	columns[0] = (String) columns[0].subSequence("User permission (".length(), columns[0].length());
             	//remove ")"
             	columns[2] = columns[2].trim();
             	columns[2] = columns[2].substring(0, columns[2].length() - 1);

             	int i = 0;
             	for (String c : columns) {
             		columns[i++] = c.split("=")[1].trim();
             	}

             	writer.setEditor(modelers.get(columns[0]));
             	writer.setSemanticArea(columns[1]);

             	writer.setCategory(WorkflowHelper.lookupEditorCategory(columns[2]));
             	writer.addMember();
             };

 	        tf.addUncommitted(refset.getRefsetConcept());
         } catch (Exception e) {
        	 AceLog.getAppLog().log(Level.WARNING, line, e);
 		}
        
        return Condition.CONTINUE;
    }

     public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}
