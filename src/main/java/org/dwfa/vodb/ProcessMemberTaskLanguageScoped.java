package org.dwfa.vodb;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ThinExtByRefPartLanguageScoped;

public class ProcessMemberTaskLanguageScoped extends ProcessMemberTask {
    
    private static ProcessMemberTaskLanguageScoped[] taskArray;
    private static Exception processException;
    protected static Semaphore semaphore = new Semaphore(TASK_SIZE, true);
    
    private UUID acceptabilityUuid;
    private UUID correctnessUuid;
    private UUID degreeOfSynonymyUuid;
    private UUID scopeUuid;
    private int priority;
    private UUID tagUuid;

    ProcessMemberTaskLanguageScoped(int arrayIndex) {
        super(arrayIndex);
    }
    
    
    protected EXT_TYPE getRefsetType() {
        return EXT_TYPE.SCOPED_LANGUAGE;
    }

    protected void reset(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, UUID acceptabilityUuid, UUID correctnessUuid,
        UUID degreeOfSynonymyUuid, UUID scopeUuid, int priority, UUID tagUuid) {
        resetCore(refsetUuid, statusUuid, componentUuid, pathUuid,
              version, memberId);
        this.acceptabilityUuid = acceptabilityUuid;
        this.correctnessUuid = correctnessUuid;
        this.degreeOfSynonymyUuid = degreeOfSynonymyUuid;
        this.scopeUuid = scopeUuid;
        this.priority = priority;
        this.tagUuid = tagUuid;
    }


    protected ThinExtByRefPartLanguageScoped makeNewPart() throws Exception {
        ThinExtByRefPartLanguageScoped part = new ThinExtByRefPartLanguageScoped();
        int acceptabilityId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) acceptabilityUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        int correctnessId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) correctnessUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        int degreeOfSynonymyId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) degreeOfSynonymyUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        int scopeId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) scopeUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        int tagId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) tagUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);

        part.setAcceptabilityId(acceptabilityId);
        part.setCorrectnessId(correctnessId);
        part.setDegreeOfSynonymyId(degreeOfSynonymyId);
        part.setScopeId(scopeId);
        part.setPriority(priority);
        part.setTagId(tagId);
        return part;
    }

    public static void acquire(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, UUID acceptabilityUuid, UUID correctnessUuid,
        UUID degreeOfSynonymyUuid, UUID scopeUuid, int priority, UUID tagUuid) throws Exception {
        check();
        semaphore.acquire();
        
        if (taskArray == null) {
            taskArray = new ProcessMemberTaskLanguageScoped[TASK_SIZE + 2]; 
            for (int i = 0; i < taskArray.length; i++) {
                taskArray[i] = new ProcessMemberTaskLanguageScoped(i);
            }
        }
        boolean foundUsableTask = false;
        for (ProcessMemberTaskLanguageScoped task: taskArray) {
            if (task.isUsable()) {
                task.reset(refsetUuid, statusUuid, componentUuid, pathUuid,
                           version, memberId, acceptabilityUuid, correctnessUuid,
                           degreeOfSynonymyUuid, scopeUuid, priority, tagUuid);
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
        ProcessMemberTaskLanguageScoped.processException = processException;
    }

    public ProcessMemberTaskLanguageScoped[] getTaskArray() {
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