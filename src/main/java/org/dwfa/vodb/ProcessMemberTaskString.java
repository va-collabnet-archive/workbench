package org.dwfa.vodb;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ThinExtByRefPartString;

public class ProcessMemberTaskString extends ProcessMemberTask {
    
    private static ProcessMemberTaskString[] taskArray;
    private static Exception processException;
    protected static Semaphore semaphore = new Semaphore(TASK_SIZE, true);
    
    private String strExt;

    ProcessMemberTaskString(int arrayIndex) {
        super(arrayIndex);
    }
    
    protected void reset(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, String strExt) {
        resetCore(refsetUuid, statusUuid, componentUuid, pathUuid,
              version, memberId);
        this.strExt = strExt;
    }

    
    protected EXT_TYPE getRefsetType() {
        return EXT_TYPE.STRING;
    }

    protected ThinExtByRefPartString makeNewPart() throws Exception {
        ThinExtByRefPartString part = new ThinExtByRefPartString();
        part.setStringValue(strExt);
        return part;
    }

    public static void acquire(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, String strExt) throws Exception {
        check();
        semaphore.acquire();
        
        if (taskArray == null) {
            taskArray = new ProcessMemberTaskString[TASK_SIZE + 2]; 
            for (int i = 0; i < taskArray.length; i++) {
                taskArray[i] = new ProcessMemberTaskString(i);
            }
        }
        boolean foundUsableTask = false;
        for (ProcessMemberTaskString task: taskArray) {
            if (task.isUsable()) {
                task.reset(refsetUuid, statusUuid, componentUuid, pathUuid,
                           version, memberId, strExt);
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
        ProcessMemberTaskString.processException = processException;
    }

    public ProcessMemberTaskString[] getTaskArray() {
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