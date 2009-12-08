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
package org.dwfa.vodb.process;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ThinExtByRefPartConceptInt;

public class ProcessMemberTaskConceptInt extends ProcessMemberTask {

    private static ProcessMemberTaskConceptInt[] taskArray;
    private static Exception processException;
    protected static Semaphore semaphore = new Semaphore(TASK_SIZE, true);

    private UUID conceptUuid;
    private int intExt;

    ProcessMemberTaskConceptInt(int arrayIndex) {
        super(arrayIndex);
    }

    protected EXT_TYPE getRefsetType() {
        return EXT_TYPE.CON_INT;
    }

    protected void reset(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid, int version,
            int memberId, UUID conceptUuid, int intExt) {
        resetCore(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId);
        this.conceptUuid = conceptUuid;
        this.intExt = intExt;
    }

    protected ThinExtByRefPartConceptInt makeNewPart() throws Exception {
        ThinExtByRefPartConceptInt part = new ThinExtByRefPartConceptInt();
        int conceptId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) conceptUuid,
            ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        part.setConceptId(conceptId);
        part.setIntValue(intExt);
        return part;
    }

    public static void acquire(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid, int version,
            int memberId, UUID conceptUuid, int intExt) throws Exception {
        check();
        semaphore.acquire();

        if (taskArray == null) {
            taskArray = new ProcessMemberTaskConceptInt[TASK_SIZE + 2];
            for (int i = 0; i < taskArray.length; i++) {
                taskArray[i] = new ProcessMemberTaskConceptInt(i);
            }
        }
        boolean foundUsableTask = false;
        for (ProcessMemberTaskConceptInt task : taskArray) {
            if (task.isUsable()) {
                task.reset(refsetUuid, statusUuid, componentUuid, pathUuid, version, memberId, conceptUuid, intExt);
                ProcessAceFormatSources.executors.submit(task);
                foundUsableTask = true;
                break;
            }
        }
        if (foundUsableTask == false) {
            throw new Exception("Acquired semaphore, but could not find usable task...");
        }
    }

    public Exception getProcessException() {
        return processException;
    }

    public void setProcessException(Exception processException) {
        ProcessMemberTaskConceptInt.processException = processException;
    }

    public ProcessMemberTaskConceptInt[] getTaskArray() {
        return taskArray;
    }

    public static void check() throws Exception {
        if (processException != null) {
            throw processException;
        }
    }

    @Override
    protected Semaphore getSemaphore() {
        return semaphore;
    }

}
