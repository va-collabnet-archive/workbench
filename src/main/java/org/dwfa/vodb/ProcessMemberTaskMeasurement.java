package org.dwfa.vodb;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ThinExtByRefPartMeasurement;

public class ProcessMemberTaskMeasurement extends ProcessMemberTask {
    
    private static ProcessMemberTaskMeasurement[] taskArray;
    private static Exception processException;
    protected static Semaphore semaphore = new Semaphore(TASK_SIZE, true);
    
    private double measurementValue;
    private UUID unitsOfMeasureUuid;

    ProcessMemberTaskMeasurement(int arrayIndex) {
        super(arrayIndex);
    }
    
    
    protected EXT_TYPE getRefsetType() {
        return EXT_TYPE.MEASUREMENT;
    }

    protected void reset(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, double measurementValue, UUID unitsOfMeasureUuid) {
        resetCore(refsetUuid, statusUuid, componentUuid, pathUuid,
              version, memberId);
        this.measurementValue = measurementValue;
        this.unitsOfMeasureUuid = unitsOfMeasureUuid;
    }

    protected ThinExtByRefPartMeasurement makeNewPart() throws Exception {
        ThinExtByRefPartMeasurement part = new ThinExtByRefPartMeasurement();
        int unitsOfMeasureId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) unitsOfMeasureUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
        part.setUnitsOfMeasureId(unitsOfMeasureId);
        part.setMeasurementValue(measurementValue);
        return part;
    }

    public static void acquire(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId, double measurementValue, UUID unitsOfMeasureUuid) throws Exception {
        check();
        semaphore.acquire();
        
        if (taskArray == null) {
            taskArray = new ProcessMemberTaskMeasurement[TASK_SIZE + 2]; 
            for (int i = 0; i < taskArray.length; i++) {
                taskArray[i] = new ProcessMemberTaskMeasurement(i);
            }
        }
        boolean foundUsableTask = false;
        for (ProcessMemberTaskMeasurement task: taskArray) {
            if (task.isUsable()) {
                task.reset(refsetUuid, statusUuid, componentUuid, pathUuid,
                           version, memberId, measurementValue, unitsOfMeasureUuid);
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
        ProcessMemberTaskMeasurement.processException = processException;
    }

    public ProcessMemberTaskMeasurement[] getTaskArray() {
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