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
package org.dwfa.ace.task.developer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dwfa.ace.api.I_ConfigAceFrame;
import org.dwfa.ace.api.I_GetConceptData;
import org.dwfa.ace.api.I_IntSet;
import org.dwfa.ace.api.I_RelTuple;
import org.dwfa.ace.api.I_TermFactory;
import org.dwfa.ace.api.I_Transact;
import org.dwfa.ace.api.Terms;
import org.dwfa.ace.api.ebr.I_ExtendByRef;
import org.dwfa.ace.api.ebr.I_ExtendByRefPart;
import org.dwfa.ace.log.AceLog;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.cement.ArchitectonicAuxiliary;
import org.dwfa.tapi.TerminologyException;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = { @Spec(directory = "tasks/ide/developer", type = BeanType.TASK_BEAN) })
public class TestGetUncommittedRefset extends AbstractTask {

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
    }// End method writeObject

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == dataVersion) {

        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }// End method readObject

    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        // Nothing to do...
    }// End method complete

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {

        try {
            I_TermFactory termFactory = Terms.get();
            I_ConfigAceFrame config = termFactory.getActiveAceFrameConfig();
            StringBuffer msg = new StringBuffer();
            msg.append("\n");
            // TEST 1
            I_GetConceptData userInfo = termFactory.getConcept(ArchitectonicAuxiliary.Concept.USER_INFO.getUids());

            List<I_GetConceptData> uncommitted = new ArrayList<I_GetConceptData>();

            listNumOfRelRefsetMembers(termFactory, config, userInfo,
                "  Number of user info extensions BEFORE COMMIT: ", msg);
            for (I_Transact transact : termFactory.getUncommitted()) {
                if (I_GetConceptData.class.isAssignableFrom(transact.getClass())) {
                    uncommitted.add((I_GetConceptData) transact);
                    listNumOfRelRefsetMembers(termFactory, config, (I_GetConceptData) transact,
                        "  Number of uncommitted concept extensions BEFORE COMMIT: ", msg);
                }
            }

            termFactory.commit();

            // TEST 2
            listNumOfRelRefsetMembers(termFactory, config, userInfo, "  Number of user info extensions AFTER COMMIT: ",
                msg);

            for (I_GetConceptData concept : uncommitted) {
                listNumOfRelRefsetMembers(termFactory, config, concept,
                    "  Number of uncommitted concept extensions AFTER COMMIT: ", msg);
            }

            AceLog.getEditLog().info(msg.toString());
            return Condition.CONTINUE;
        } catch (IOException e) {
            throw new TaskFailedException(e);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }

    }// End method evaluate

    private void listNumOfRelRefsetMembers(I_TermFactory termFactory, I_ConfigAceFrame config,
            I_GetConceptData userInfo, String comment, StringBuffer msg) throws IOException, TerminologyException {
        I_IntSet allowedTypes = termFactory.newIntSet();
        for (int destRelNid : config.getDestRelTypes().getSetValues()) {
            allowedTypes.add(destRelNid);
        }
        allowedTypes.add(ArchitectonicAuxiliary.Concept.IS_A_REL.localize().getNid());
        List<? extends I_RelTuple> srcRelTuples = userInfo.getSourceRelTuples(config.getAllowedStatus(), allowedTypes,
            config.getViewPositionSetReadOnly(), config.getPrecedence(), config.getConflictResolutionStrategy());
        for (I_RelTuple relTuple : srcRelTuples) {
            List<? extends I_ExtendByRef> extensions = termFactory.getAllExtensionsForComponent(relTuple.getRelId(),
                true);
            msg.append(comment + extensions.size() + "\n");
            for (I_ExtendByRef ext : extensions) {
                for (I_ExtendByRefPart part : ext.getMutableParts()) {
                    if (part.getVersion() == Integer.MAX_VALUE) {
                        msg.append("\n     Uncommitted extension: " + ext + "\n");
                        break;
                    }
                }
            }
        }
    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }// End method getConditions

    public int[] getDataContainerIds() {
        return new int[] {};
    }

}// End class CreateRefsetMembersetPair
