/*
 * Created on Mar 28, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.data;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.tasks.editor.ListEditor;

/**
 * @author kec
 *
 */
public class SetDataContainer extends DataContainer implements ListModel {

    /**
     * 
     */
    private static final long serialVersionUID = -5412672664184605560L;

    /**
     * @param id
     * @param description
     * @param data
     * @param elementClass
     */
    public SetDataContainer() {
        super(-1, "Set #", new ArrayListModel<Set<?>>(), Object.class);
    }
    
    private ArrayList<?> getList() throws IOException, ClassNotFoundException {
        return (ArrayList<?>) this.getData();
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        try {
            return this.getList().size();
        } catch (Exception e) {
            //No exceptions should be thrown for lists, since they are native objects. 
            throw new RuntimeException(e);
        } 
    }

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
         try {
             return this.getList().get(index);
        } catch (Exception e) {
            //No exceptions should be thrown for lists, since they are native objects. 
            throw new RuntimeException(e);
        } 
   }

    /**
     * @see javax.swing.ListModel#addListDataListener(javax.swing.event.ListDataListener)
     */
    public void addListDataListener(ListDataListener l) {
        throw new UnsupportedOperationException();
        
    }

    /**
     * @see javax.swing.ListModel#removeListDataListener(javax.swing.event.ListDataListener)
     */
    public void removeListDataListener(ListDataListener l) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException {
        return new BpaDragAndDropBean(prefix, dndComponent, allowDrop, allowDrag, Object.class);
    }
    public JComponent getEditor() throws SecurityException, NoSuchMethodException, IOException, ClassNotFoundException {
        JList list = new JList();
        list.setModel((ListModel) this.getData());
        JScrollPane scroller = new JScrollPane(list);
        scroller.setMinimumSize(new Dimension(150, 100));
        scroller.setPreferredSize(new Dimension(150, 100));

        list.setBorder(BorderFactory.createLoweredBevelBorder());
        return scroller;
    }
    
    /**
     * @see org.dwfa.bpa.process.I_ContainData#getEditorClass()
     */
    public Class<?> getEditorClass() {
        return ListEditor.class;
    }
    /**
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @see org.dwfa.bpa.process.I_ContainData#getReadMethod()
     */
    public Method getReadMethod() throws SecurityException, NoSuchMethodException {
        return this.getClass().getMethod("getData", new Class[] {});
    }
    /**
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @see org.dwfa.bpa.process.I_ContainData#getWriteMethod()
     */
    public Method getWriteMethod() throws SecurityException, NoSuchMethodException {
        return this.getClass().getMethod("setData", new Class[] { Serializable.class });
    }

}
