package org.dwfa.vodb;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.dwfa.ace.api.LocalVersionedTerminology;
import org.dwfa.ace.api.ebr.I_ThinExtByRefVersioned;
import org.dwfa.vodb.bind.ThinExtBinder;
import org.dwfa.vodb.bind.ThinExtBinder.EXT_TYPE;
import org.dwfa.vodb.types.ThinExtByRefPart;

public abstract class ProcessMemberTask implements Runnable {

    protected static int TASK_SIZE = 40;
    protected static int sequence = 0;
    /**
     * 
     */
    protected int arrayIndex;
    protected int taskSequence;
    protected boolean usable = true;
    protected UUID refsetUuid;
    protected UUID statusUuid;
    protected UUID componentUuid;
    protected UUID pathUuid;
    protected int version;
    protected int memberId;
    
    ProcessMemberTask(int arrayIndex) {
        super();
        this.arrayIndex = arrayIndex;
    }


    public boolean isUsable() {
        return usable;
    }


    protected abstract Exception getProcessException();
    protected abstract void setProcessException(Exception processException);
    protected abstract ThinExtByRefPart makeNewPart() throws Exception;
    protected abstract ProcessMemberTask[] getTaskArray();
    protected abstract Semaphore getSemaphore();
    
    public void run() {
        if (getProcessException() != null) {
            getSemaphore().release();
            return;
        }
        boolean ready = true;
        for (ProcessMemberTask task: getTaskArray()) {
            if (task.usable == false && 
                    task.componentUuid == this.componentUuid &&
                    task.arrayIndex != this.arrayIndex &&
                    task.taskSequence < this.taskSequence) {
                ready = false;
            }
        }

        if (getProcessException() != null) {
            getSemaphore().release();
            return;
        }
        if (ready) {
            try {
                int refsetId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) refsetUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
                int statusId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) statusUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
                int componentId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) componentUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
                int pathId = ProcessAceFormatSourcesBerkeley.map.getIntId((UUID) pathUuid, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);
                int typeId = ThinExtBinder.getExtensionTypeNid(getRefsetType(), ProcessAceFormatSourcesBerkeley.map, ProcessAceFormatSourcesBerkeley.aceAuxPath, version);

                VodbEnv tf = (VodbEnv) LocalVersionedTerminology.get();
                I_ThinExtByRefVersioned ext;
                ThinExtByRefPart part = makeNewPart();

                if (tf.hasExtension(memberId)) {
                    ext = tf.getExtension(memberId);
                    for (Object version: ext.getVersions()) {
                        if (version.getClass().equals(part.getClass()) == false) {
                            throw new Exception("Extension classes do not match: " + version.getClass().getName() + " "
                                                + part.getClass());
                        }
                    }
                } else {
                    ext = tf.newExtension(refsetId, memberId, componentId, typeId);
                }
                
                part.setPathId(pathId);
                part.setStatus(statusId);
                part.setVersion(version);
                ext.addVersion(part);
                
                
                tf.writeExt(ext);
                usable = true;
                getSemaphore().release();
            } catch (Exception ex) {
                setProcessException(ex);
                getSemaphore().release();
            }
        } else {
            ProcessAceFormatSources.executors.submit(this);
        }
    }


    protected abstract EXT_TYPE getRefsetType();

    protected void resetCore(UUID refsetUuid, UUID statusUuid, UUID componentUuid, UUID pathUuid,
        int version, int memberId) {
        this.usable = false;
        this.taskSequence = ProcessMemberTask.sequence++;
        this.refsetUuid = refsetUuid;
        this.statusUuid = statusUuid;
        this.componentUuid = componentUuid;
        this.pathUuid = pathUuid;
        this.version = version;
        this.memberId = memberId;
    }



}
