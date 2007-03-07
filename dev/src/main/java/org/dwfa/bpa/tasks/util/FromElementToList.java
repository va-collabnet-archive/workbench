/*
 * Created on Mar 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.tasks.util;


import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.List;

import org.dwfa.util.bean.BeanList;
import org.dwfa.util.bean.BeanType;
import org.dwfa.util.bean.Spec;
import org.dwfa.bpa.process.Condition;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.process.I_Work;
import org.dwfa.bpa.process.TaskFailedException;
import org.dwfa.bpa.tasks.AbstractTask;


/**
 * @author kec
 *
 */
@BeanList(specs = 
{ @Spec(directory = "tasks/util", type = BeanType.TASK_BEAN)})
public class FromElementToList extends AbstractTask {

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;
    
    private int elementId = -1;
    private int listId = -1;

	/**
	 * @return Returns the elementId.
	 */
	public int getElementId() {
		return elementId;
	}
    public void setElementId(Integer elementId) {
        setElementId(elementId.intValue());
    }
    public void setElementId(int elementId) {
        int oldValue = this.elementId;
		this.elementId = elementId;
        this.firePropertyChange("elementId", oldValue, this.elementId);
	}
	/**
	 * @return Returns the listId.
	 */
	public int getListId() {
		return listId;
	}
    public void setListId(Integer listId) {
        setListId(listId.intValue());
    }
	/**
	 * @param listId The listId to set.
	 */
	public void setListId(int listId) {
        int oldValue = this.listId;
        this.listId = listId;
        this.firePropertyChange("listId", oldValue, this.listId);
	}
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(this.elementId);
        out.writeInt(this.listId);
     }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
        	    this.elementId = in.readInt();
            this.listId = in.readInt();
        } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
    /**
     * @param name
     */
    public FromElementToList() {
        super();
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#evaluate(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    @SuppressWarnings("unchecked")
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker)
            throws TaskFailedException {        
        I_ContainData listContainer = process.getDataContainer(this.listId);
        I_ContainData elementContainer = process.getDataContainer(this.elementId);
        List list;
        try {
            list = (List) listContainer.getData();
            Object element = elementContainer.getData();
            list.add(list.size(), element);
        } catch (Exception e) {
            throw new TaskFailedException(e);
        }
        return Condition.CONTINUE;
    }
    /**
     * @see org.dwfa.bpa.process.I_DefineTask#complete(org.dwfa.bpa.process.I_EncodeBusinessProcess, org.dwfa.bpa.process.I_Work)
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException {
        //Nothing to do
        
    }

    /**
     * @see org.dwfa.bpa.process.I_DefineTask#getConditions()
     */
    public Collection<Condition> getConditions() {
        return AbstractTask.CONTINUE_CONDITION;
    }

	/**
	 * @see org.dwfa.bpa.process.I_DefineTask#getDataContainerIds()
	 */
	public int[] getDataContainerIds() {
		return new int[] { this.elementId, this.listId };
	}

}
