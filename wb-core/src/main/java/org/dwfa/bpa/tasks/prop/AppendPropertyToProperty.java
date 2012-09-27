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
package org.dwfa.bpa.tasks.prop;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/property tasks", type = BeanType.TASK_BEAN) })
public class AppendPropertyToProperty extends AbstractTask {

    private String destinationPropName = "";
    private String seperatorText = "";
    private String sourcePropName = "";

    private static final long serialVersionUID = 1;
    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(destinationPropName);
        out.writeObject(seperatorText);
        out.writeObject(sourcePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            if (objDataVersion >= 1) {
                // Read version 1 data fields
            	destinationPropName = (String) in.readObject();
            	seperatorText = (String) in.readObject();
            	sourcePropName = (String) in.readObject();
            }
            // Initialize transient properties...
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }
    }

    public AppendPropertyToProperty() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            String origStr = (String) process.getProperty(destinationPropName).toString();
            String appendStr = (String) process.getProperty(sourcePropName).toString();
            String newStr = origStr + seperatorText + appendStr;
            process.setProperty(destinationPropName, newStr);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getDestinationPropName() {
		return destinationPropName;
	}

	public void setDestinationPropName(String destinationPropName) {
		this.destinationPropName = destinationPropName;
	}

	public String getSeperatorText() {
		return seperatorText;
	}

	public void setSeperatorText(String seperatorText) {
		this.seperatorText = seperatorText;
	}

	public String getSourcePropName() {
		return sourcePropName;
	}

	public void setSourcePropName(String sourcePropName) {
		this.sourcePropName = sourcePropName;
	}

}
