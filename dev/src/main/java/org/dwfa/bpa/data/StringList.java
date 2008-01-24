/*
 * Created on Mar 22, 2005
 *
 * Copyright 2005 by Informatics, Inc. 
 */
package org.dwfa.bpa.data;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.tasks.editor.ListEditor;


/**
 * @author kec
 *
 */
public class StringList extends DataContainer {


    /**
     * 
     */
    private static final long serialVersionUID = 1453626879291482237L;

    /**
	 * @param id
	 * @param description
	 * @param data
	 * @param elementClass
	 */
	public StringList() {
		super(-1, "String List #", new ArrayListModel<String>(), String.class);
	}
    @Override
    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException {
        return new BpaDragAndDropBean(prefix, dndComponent, allowDrop, allowDrag, String.class);
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
