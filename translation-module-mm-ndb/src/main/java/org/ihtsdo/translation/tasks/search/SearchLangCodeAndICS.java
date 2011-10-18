/**
 * Copyright (c) 2010 International Health Terminology Standards Development
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
package org.ihtsdo.translation.tasks.search;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.dwfa.ace.api.I_AmTermComponent;
import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_DescriptionTuple;
import org.dwfa.ace.api.I_DescriptionVersioned;
import org.dwfa.ace.task.search.AbstractSearchTest;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

/**
 * The Class HasLanguageCode.
 */
@BeanList(specs = { @Spec(directory = "tasks/ide/search", type = BeanType.TASK_BEAN), 
        @Spec(directory = "search", type = BeanType.TASK_BEAN) })
public class SearchLangCodeAndICS extends AbstractSearchTest {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1;

    /** The Constant dataVersion. */
    private static final int dataVersion = 1;

    /** The language code. */
    private String languageCode = "en";
    
    private String ics = "false";

    /**
     * Write object.
     * 
     * @param out the out
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(this.languageCode);
        out.writeObject(this.ics);
     }

    /**
     * Read object.
     * 
     * @param in the in
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
                this.languageCode = (String) in.readObject();
                this.ics = (String) in.readObject();
         } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }
    }

    /* (non-Javadoc)
     * @see org.dwfa.ace.task.search.AbstractSearchTest#test(org.dwfa.ace.api.I_AmTermComponent, org.dwfa.ace.api.I_ConfigAceFrame)
     */
    @Override
    public boolean test(I_AmTermComponent component, I_ConfigAceFrame frameConfig) throws TaskFailedException {
        try {
        	I_DescriptionVersioned descToTest = null;
            if (I_DescriptionVersioned.class.isAssignableFrom(component.getClass())) {
                descToTest = (I_DescriptionVersioned) component;
            } else {
                return applyInversion(false);
            }
            
            List<? extends I_DescriptionTuple> lastTuple= descToTest.getTuples(frameConfig.getConflictResolutionStrategy());
            //System.out.println("**************** lasttuple size false: "+ lastTuple.size());
            
            lastTuple= descToTest.getTuples(frameConfig.getConflictResolutionStrategy());
            //System.out.println("**************** lasttuple size true: "+ lastTuple.size());
            
            boolean boolIcs = false;
            if (ics.equals("true")) {
            	boolIcs = true;
            }
            
            if (lastTuple.iterator().next().getLang().equals(languageCode) &&
            		lastTuple.iterator().next().isInitialCaseSignificant() == boolIcs) {
            	return applyInversion(true); 
            } 
            
            return applyInversion(false);
        } catch (TerminologyException e) {
            throw new TaskFailedException(e);
        } catch (IOException e) {
            throw new TaskFailedException(e);
        }
    }

	/**
	 * Gets the language code.
	 * 
	 * @return the language code
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * Sets the language code.
	 * 
	 * @param languageCode the new language code
	 */
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getIcs() {
		return ics;
	}

	public void setIcs(String ics) {
		this.ics = ics;
	}
	

}
