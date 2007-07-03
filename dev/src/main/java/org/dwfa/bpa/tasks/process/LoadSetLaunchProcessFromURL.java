/*
 * Created on Apr 7, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.process;

import java.beans.PropertyDescriptor;
import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Stack;
import java.util.logging.Level;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.PropertyDescriptorWithTarget;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;

/**
 * This task:
 * <ol><li>Loads a process from a URL</li>
 *     <li>Sets all the external properties of the loaded process to the value of properties 
 *         of the same name and type from within the calling process.
 *     <li>Executes the process</li>
 * </ol>
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/processes/start tasks", type = BeanType.TASK_BEAN)})
public class LoadSetLaunchProcessFromURL extends AbstractTask {

    private URL processURL = new URL("http://www.informatics.com/hello.xml");
    private String originator;
    private String destination;
    private String processName;
    private String processSubject;

    private static final long serialVersionUID = 2;

    private static final int dataVersion = 2;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeObject(processURL);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion <= dataVersion) {
            processURL = (URL) in.readObject();
        	if (objDataVersion >= 2) {
        		
        	} 
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);
        }

    }

    public LoadSetLaunchProcessFromURL() throws MalformedURLException {
        super();
    }

    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {
        try {
            // Load process...
            I_EncodeBusinessProcess processToLaunch;
            if (processURL.toString().toLowerCase().endsWith(".xml")) {
                InputStream inStream;
                if (processURL.getProtocol().toLowerCase().startsWith("file")) {
                    inStream = new FileInputStream(processURL.getFile());
                } else {
                    inStream = process.getStreamFromURL(processURL);
                }
                XMLDecoder d = new XMLDecoder(new BufferedInputStream(inStream));
                processToLaunch = (I_EncodeBusinessProcess) d.readObject();
            } else {
                processToLaunch = (I_EncodeBusinessProcess) process
                        .getObjectFromURL(processURL);
            }
            
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

    /**
     * @return Returns the processURL.
     */
    public String getProcessURLString() {
        return processURL.toString();
    }

    /**
     * @param processURL
     *            The processURL to set.
     * @throws MalformedURLException
     */
    public void setProcessURLString(String processURLString)
            throws MalformedURLException {
        this.processURL = new URL(processURLString);
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

}
