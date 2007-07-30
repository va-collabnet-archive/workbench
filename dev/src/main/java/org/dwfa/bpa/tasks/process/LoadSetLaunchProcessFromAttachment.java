package org.dwfa.bpa.tasks.process;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.Stack;
import java.util.logging.Level;

import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;
import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;

@BeanList(specs = 
{ @Spec(directory = "tasks/processes/start tasks", type = BeanType.TASK_BEAN)})
public class LoadSetLaunchProcessFromAttachment extends AbstractTask {

    private String processPropName;
    private String originator;
    private String destination;
    private String processName;
    private String processSubject;

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(processPropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            processPropName = (String) in.readObject();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

     @SuppressWarnings("unchecked")
	public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            // Load process...
        	MarshalledObject marshalledProcess = (MarshalledObject) process.readAttachement(processPropName);
            I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) marshalledProcess.get();
             
            if (processSubject != null) {
            	processToLaunch.setSubject(processSubject);
            }
            if (processName != null) {
            	processToLaunch.setName(processName);
            }
            if (destination != null) {
            	processToLaunch.setDestination(destination);
            }
            if (originator != null) {
            	processToLaunch.setOriginator(originator);
            }

            // Set properties...
            for (PropertyDescriptor propDesc : processToLaunch.getBeanInfo()
                    .getPropertyDescriptors()) {
                if (PropertyDescriptorWithTarget.class
                        .isAssignableFrom(propDesc.getClass())) {
                    try {
                        PropertyDescriptorWithTarget pdwt = (PropertyDescriptorWithTarget) propDesc;
                        Object value = process.readProperty(pdwt.getLabel());
                        propDesc.getWriteMethod().invoke(pdwt.getTarget(),
                                value);
                        if (worker.getLogger().isLoggable(Level.FINE)) {
                            worker.getLogger().fine(
                                    "Set " + pdwt.getLabel() + " to " + value);
                        }
                    } catch (Exception ex) {
                        worker.getLogger().warning(ex.getMessage());
                    }
                }
            }

            // Launch Process...
            Stack<I_EncodeBusinessProcess> ps = worker.getProcessStack();
            worker.setProcessStack(new Stack<I_EncodeBusinessProcess>());
            processToLaunch.execute(worker);
            worker.setProcessStack(ps);
            return Condition.CONTINUE;
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
    }

    public void complete(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {

    }

    public Collection<Condition> getConditions() {
        return CONTINUE_CONDITION;
    }

    public int[] getDataContainerIds() {
        return new int[] {};
    }

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getOriginator() {
		return originator;
	}

	public void setOriginator(String originator) {
		this.originator = originator;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessSubject() {
		return processSubject;
	}

	public void setProcessSubject(String processSubject) {
		this.processSubject = processSubject;
	}

	public String getProcessPropName() {
		return processPropName;
	}

	public void setProcessPropName(String processPropName) {
		this.processPropName = processPropName;
	}

}
