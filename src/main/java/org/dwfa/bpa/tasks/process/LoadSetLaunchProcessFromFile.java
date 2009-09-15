package org.dwfa.bpa.tasks.process;

import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
{ @Spec(directory = "tasks/start tasks", type = BeanType.TASK_BEAN)})

public class LoadSetLaunchProcessFromFile extends AbstractTask {

    private String inputFilePropName = "A: INPUT_FILE";
    private String originator;
    private String destination;
    private String processName;
    private String processSubject;

    private static final long serialVersionUID = 1L;

    private static final int dataVersion = 1;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(inputFilePropName);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
        	inputFilePropName = (String) in.readObject();
        	if (objDataVersion >= 2) {
        		
        	} 
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public LoadSetLaunchProcessFromFile() {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            // Load process...
			String fileName = (String) process.readProperty(inputFilePropName);
            ObjectInputStream inStream = new ObjectInputStream(new FileInputStream(fileName));
            I_EncodeBusinessProcess processToLaunch = (I_EncodeBusinessProcess) inStream.readObject();
            
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

	public String getInputFilePropName() {
		return inputFilePropName;
	}

	public void setInputFilePropName(String inputFilePropName) {
		this.inputFilePropName = inputFilePropName;
	}

}
