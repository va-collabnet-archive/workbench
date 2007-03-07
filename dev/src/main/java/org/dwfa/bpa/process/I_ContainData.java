/*
 * Created on Mar 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.process;

import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.swing.JComponent;

import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;

/**
 * A generic container for holding data within a business process. 
 * @author kec
 *  
 */
public interface I_ContainData extends I_ManageVetoableProperties, I_ManageProperties, Serializable {
	/**
	 * @return identifier for this data container. 
	 */
	public int getId();

	/**
	 * @param id identifier for this data container
	 * @throws PropertyVetoException Thrown if the identifier has already been set. 
	 */
	public void setId(int id) throws PropertyVetoException;
    
    /**
     * @return The class of element in this data container. If this container holds
     * a collection, the class of elements within the collection. 
     */
    public Class getElementClass();
    
    /**
     * @return True if this container holds a collection. 
     */
    public boolean isCollection();

	/**
	 * @return Description of this data container. 
	 */
	public String getDescription();

	/**
	 * @param description description of this data container. 
	 * @throws PropertyVetoException Thrown if the description is not unique within the process. 
	 */
	public void setDescription(String description) throws PropertyVetoException;

	/**
	 * @return The data from this container. 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public Serializable getData() throws Exception;
    /**
     * @param data Set the data in this container. 
     * @throws IOException 
     */
    public void setData(Serializable data) throws IOException;
    
	/**
	 * @param bounds Coordinates to layout this data container in a GUI. 
	 */
	public void setDataContainerBounds(Rectangle bounds);

	/**
	 * @return Coordinates to layout this data container in a GUI. 
	 */
	public Rectangle getDataContainerBounds();

    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException;

    public Transferable getTransferable() throws ClassNotFoundException;
    
    public JComponent getEditor() throws Exception;
    public Class getEditorClass();
    public Method getReadMethod() throws SecurityException, NoSuchMethodException;
    public Method getWriteMethod() throws SecurityException, NoSuchMethodException;

}