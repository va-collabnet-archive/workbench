/*
 * Created on Mar 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.data;

import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Logger;

import org.dwfa.bpa.dnd.DataContainerTransferable;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.process.I_ContainData;


/**
 * @author kec
 *
 */
public abstract class DataContainer implements I_ContainData {

	private int id = -1;
    private String description;
    private Serializable data;
    private Rectangle bounds;
    private Class elementClass;

    private static final long serialVersionUID = 1;

    private static final int dataVersion = 1;

    private transient VetoableChangeSupport vetoSupport = new VetoableChangeSupport(this);
    private transient PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    protected static Logger logger = Logger.getLogger(DataContainer.class.getName());
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(dataVersion);
        out.writeInt(this.id);
        out.writeObject(this.description);
        out.writeObject(this.data);
        out.writeObject(this.bounds);
        out.writeObject(this.elementClass);
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        int objDataVersion = in.readInt();
        if (objDataVersion == 1) {
            this.id = in.readInt();            
            this.description = (String) in.readObject();
            this.data = (Serializable) in.readObject();
            this.bounds = (Rectangle) in.readObject();
            this.elementClass = (Class) in.readObject();
            this.vetoSupport = new VetoableChangeSupport(this);
            this.changeSupport = new PropertyChangeSupport(this);
       } else {
            throw new IOException("Can't handle dataversion: " + objDataVersion);   
        }

    }
	/**
	 * @param id
	 * @param description
	 * @param data
	 */
	public DataContainer(int id, String description, Serializable data, Class elementClass) {
		super();
		this.id = id;
		this.description = description;
		this.data = data;
        this.elementClass = elementClass;
	}
	/**
	 * @see org.dwfa.bpa.process.I_ContainData#getId()
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @see org.dwfa.bpa.process.I_ContainData#setId(int)
	 */
	public void setId(int id) throws PropertyVetoException {
        int oldId = this.id;
        this.vetoSupport.fireVetoableChange("id", oldId, id);
        this.id = id;
        this.changeSupport.firePropertyChange("id", oldId, this.id);
	}

	/**
	 * @see org.dwfa.bpa.process.I_ContainData#getDescription()
	 */
	public String getDescription() {
        if (this.getId() == -1) {
            if (this.description.endsWith("#")) {
                return this.description.substring(0, this.description.length() - 2);
            }
        }
        if (this.description.endsWith("#")) {
            return this.description + this.getId();
        }
		return description;
	}

	/**
	 * @see org.dwfa.bpa.process.I_ContainData#setDescription(java.lang.String)
	 */
	public void setDescription(String description) throws PropertyVetoException {
        String oldDescription = this.description;
        this.vetoSupport.fireVetoableChange("description", oldDescription, description);
        this.description = description;
        this.changeSupport.firePropertyChange("description", oldDescription, this.description);
	}

	/**
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 * @see org.dwfa.bpa.process.I_ContainData#getData()
	 */
	public Serializable getData() throws IOException, ClassNotFoundException {
		return data;
	}
    
    public void setData(Serializable data) throws IOException {
        Object oldData = this.data;
        this.data = data;
        this.changeSupport.firePropertyChange("data", oldData, this.data);
    }

	/**
	 * @see org.dwfa.bpa.process.I_ContainData#setDataContainerBounds(java.awt.Rectangle)
	 */
	public void setDataContainerBounds(Rectangle bounds) {
        Rectangle oldBounds = this.bounds;
        this.bounds = bounds;
        this.changeSupport.firePropertyChange("bounds", oldBounds, this.bounds);
	}

	/**
	 * @see org.dwfa.bpa.process.I_ContainData#getDataContainerBounds()
	 */
	public Rectangle getDataContainerBounds() {
		return bounds;
	}

	/**
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 */
	public void addPropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(propertyName, listener);
	}
	/**
	 * @return
	 */
	public PropertyChangeListener[] getPropertyChangeListeners() {
		return changeSupport.getPropertyChangeListeners();
	}
	/**
	 * @param propertyName
	 * @return
	 */
	public PropertyChangeListener[] getPropertyChangeListeners(
			String propertyName) {
		return changeSupport.getPropertyChangeListeners(propertyName);
	}
	/**
	 * @param listener
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 */
	public void removePropertyChangeListener(String propertyName,
			PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(propertyName, listener);
	}
	/**
	 * @param listener
	 */
	public void addVetoableChangeListener(VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 */
	public void addVetoableChangeListener(String propertyName,
			VetoableChangeListener listener) {
		vetoSupport.addVetoableChangeListener(propertyName, listener);
	}
	/**
	 * @return
	 */
	public VetoableChangeListener[] getVetoableChangeListeners() {
		return vetoSupport.getVetoableChangeListeners();
	}
	/**
	 * @param propertyName
	 * @return
	 */
	public VetoableChangeListener[] getVetoableChangeListeners(
			String propertyName) {
		return vetoSupport.getVetoableChangeListeners(propertyName);
	}
	/**
	 * @param listener
	 */
	public void removeVetoableChangeListener(VetoableChangeListener listener) {
		vetoSupport.removeVetoableChangeListener(listener);
	}
	/**
	 * @param propertyName
	 * @param listener
	 */
	public void removeVetoableChangeListener(String propertyName,
			VetoableChangeListener listener) {
		vetoSupport.removeVetoableChangeListener(propertyName, listener);
	}
	/**
	 * @see org.dwfa.bpa.process.I_ContainData#getElementClass()
	 */
	public Class getElementClass() {
		return this.elementClass;
	}
	/**
	 * @see org.dwfa.bpa.process.I_ContainData#isCollection()
	 */
	public boolean isCollection() {
        if ((this.data == null) || ((Collection.class.isAssignableFrom(this.data.getClass())) == false)) {
            return false;
        }
		return true;
	}
    
    public String toString() {
        return this.description + " id: " + this.id + " class: " + this.elementClass + " data: " + data /* + " bounds: " + bounds */;
    }

    public abstract I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException;
    
    public final Transferable getTransferable() throws ClassNotFoundException {
        return new DataContainerTransferable(this);
    }
}
