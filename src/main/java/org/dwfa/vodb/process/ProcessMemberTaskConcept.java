/**
 * 
 */
package org.dwfa.vodb.process;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ThinExtByRefPartConcept;

class ProcessMemberTaskConcept extends ProcessMemberTask {
    
    private static ProcessMemberTaskConcept[] taskArray;
    private static Exception processException;
    private static Semaphore semaphore = new Semaphore(TASK_SIZE, true);
    
    private UUID conceptUuid;

    ProcessMemberTaskConcept(int arrayIndex) {
        super(arrayIndex);
    }
    
    protected EXT_TYPE getRefsetType() {
        return EXT_TYPE.CONCEPT;
    }

    protected void reset(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, UUID conceptUuid) {
        resetCore(refsetUuid, statusUuid, componentUuid, pathUuid,
              version, memberId);
        this.conceptUuid = conceptUuid;
    }


    protected ThinExtByRefPartConcept makeNewPart() throws Exception {
        ThinExtByRefPartConcept part = new ThinExtByRefPartConcept();
        int conceptId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) conceptUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        part.setConceptId(conceptId);
        return part;
    }

    public static void acquire(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, UUID conceptUuid) throws Exception {
        check();
        semaphore.acquire();
        
        if (taskArray == null) {
            taskArray = new ProcessMemberTaskConcept[TASK_SIZE + 2]; 
            for (int i = 0; i < taskArray.length; i++) {
                taskArray[i] = new ProcessMemberTaskConcept(i);
            }
        }
        boolean foundUsableTask = false;
        for (ProcessMemberTaskConcept task: taskArray) {
            if (task.isUsable()) {
                task.reset(refsetUuid, statusUuid, componentUuid, pathUuid,
                           version, memberId, conceptUuid);
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
        ProcessMemberTaskConcept.processException = processException;
    }

    public ProcessMemberTaskConcept[] getTaskArray() {
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