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
package org.dwfa.ace.task.view;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_Path;
import org.dwfa.ace.api.I_Position;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.task.ProcessAttachmentKeys;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.AceDateFormat;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/view", type = BeanType.TASK_BEAN) })
public class SetViewPositionFromProperty extends AbstractTask {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 2;

    private static DateFormat dateParser = AceDateFormat.getDataInputDateFormat();

    private String viewPathConceptPropName = ProcessAttachmentKeys.VIEW_PATH_CONCEPT.getAttachmentKey();

    private String profilePropName = ProcessAttachmentKeys.WORKING_PROFILE.getAttachmentKey();

    private String positionStr = "latest";

    private boolean keepExistingViewPaths = false;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(viewPathConceptPropName);
        out.writeObject(profilePropName);
        out.writeObject(positionStr);
        out.writeObject(keepExistingViewPaths);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion > dataVersion) {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

        viewPathConceptPropName = (String) in.readObject();
        profilePropName = (String) in.readObject();
        positionStr = (String) in.readObject();

        if (objDataVersion > 1) {
            keepExistingViewPaths = (Boolean) in.readObject();
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...

    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        try {
            I_ConfigAceFrame profile = getProperty(process, I_ConfigAceFrame.class, profilePropName);
            I_GetConceptData viewPathConcept = getProperty(process, I_GetConceptData.class, viewPathConceptPropName);

            I_TermFactory tf = LocalVersionedTerminology.get();
            I_Path viewPath = tf.getPath(viewPathConcept.getUids());

            int version = Integer.MAX_VALUE;
            if (!positionStr.equalsIgnoreCase("latest")) {
                Date date = dateParser.parse(positionStr);
                version = tf.convertToThinVersion(date.getTime());
            }

            I_Position newPosition = tf.newPosition(viewPath, version);
            Set<I_Position> viewPositionSet = profile.getViewPositionSet();
            if (!keepExistingViewPaths) {
                viewPositionSet.clear();
            }
            viewPositionSet.add(newPosition);

            profile.fireUpdateHierarchyView();

        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

        return Condition.CONTINUE;
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

    public String getProfilePropName() {
        return profilePropName;
    }

    public void setProfilePropName(String profilePropName) {
        this.profilePropName = profilePropName;
    }

    public String getViewPathConceptPropName() {
        return viewPathConceptPropName;
    }

    public void setViewPathConceptPropName(String editPathEntry) {
        this.viewPathConceptPropName = editPathEntry;
    }

    public String getPositionStr() {
        return positionStr;
    }

    public void setPositionStr(String positionStr) {
        this.positionStr = positionStr;
    }

    public boolean isKeepExistingViewPaths() {
        return keepExistingViewPaths;
    }

    public void setKeepExistingViewPaths(boolean keepExistingViewPaths) {
        this.keepExistingViewPaths = keepExistingViewPaths;
    }

}
