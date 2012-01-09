/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.ihtsdo.ace.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;

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
import org.ihtsdo.tk.api.concept.ConceptVersionBI;
import org.ihtsdo.tk.api.coordinate.ViewCoordinate;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetSearcher;
import org.ihtsdo.workflow.refset.edcat.EditorCategoryRefsetWriter;
import org.ihtsdo.workflow.refset.utilities.WorkflowHelper;

@BeanList(specs = {
    @Spec(directory = "tasks/workflow", type = BeanType.TASK_BEAN)})
public class UpdateEditorCategoryRefset extends AbstractTask {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final int dataVersion = 1;

    private EditorCategoryRefsetSearcher searcher = null;
    private HashMap<String, ConceptVersionBI> modelers = null;
    
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
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        String line = "";
        I_TermFactory tf = Terms.get();
        BufferedReader inputFile = null;

        try {
            ViewCoordinate vc = tf.getActiveAceFrameConfig().getViewCoordinate();
            EditorCategoryRefsetWriter writer = new EditorCategoryRefsetWriter();
            try {
				// Used when building bundle
            	File f = new File("../../src/main/users/userPermissionRefset.txt");
            	inputFile = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
				// Used during excecution of application via MENU-TASKS-UPDATE_ED_CATEGORY_REFSET
            	File f= new File("workflow" + File.separatorChar + "userPermissionRefset.txt");
            	inputFile = new BufferedReader(new FileReader(f));
            }

            WorkflowHelper.updateModelers(vc);
            modelers = WorkflowHelper.getModelers();
        	searcher = new EditorCategoryRefsetSearcher();

            String headerLine = inputFile.readLine();
            while ((line = inputFile.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }

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

                ConceptVersionBI newCategory = WorkflowHelper.lookupEditorCategory(columns[2], vc);
                ConceptVersionBI oldCategory = identifyExistingEditorCategory(columns, vc);
                boolean addingRequired = true;
                
                if (oldCategory != null) {
                	if (!oldCategory.equals(newCategory)) {
		                writer.retireEditorCategory(modelers.get(columns[0]), columns[1], oldCategory);
	                } else {
	                	addingRequired = false;
	                }
                }
                
                if (addingRequired) {
	                writer.setEditor(modelers.get(columns[0]));
	                writer.setSemanticArea(columns[1]);

	                writer.setCategory(newCategory);
	                writer.addMember();
	            }
            }

            Terms.get().commit();
        } catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, line, e);
        }

        return Condition.CONTINUE;
    }

    private ConceptVersionBI identifyExistingEditorCategory(String[] columns, ViewCoordinate vc) {
    	try {
    		return searcher.searchForCategoryByModelerAndTag(modelers.get(columns[0]), columns[1], vc);
		} catch (Exception e) {
            AceLog.getAppLog().log(Level.WARNING, "Failed to identify existing categories for mod: " + columns[0] + " and semTag: " + columns[1], e);
		}

		return null;
	}

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[]{};
    }
}
