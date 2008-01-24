/*
 * Created on Feb 20, 2006
 *
 * Copyright 2006 by Informatics, Inc. 
 */
package org.dwfa.bpa.data;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.process.I_EncodeBusinessProcess;
import org.dwfa.bpa.tasks.editor.ProcessReadOnly;

public class ProcessContainer extends DataContainer {


    public ProcessContainer() {
        super(-1, "Process Container #", null, I_EncodeBusinessProcess.class);
    }
    /**
     * 
     */
    private static final long serialVersionUID = 971993342760292419L;

     @Override
    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException {
        return new BpaDragAndDropBean(prefix, dndComponent, allowDrop, allowDrag, I_EncodeBusinessProcess.class);
    }
    public JComponent getEditor() throws SecurityException, NoSuchMethodException, IOException, ClassNotFoundException {
        if (this.getData() == null) {
            return new JLabel("null");
        }
        I_EncodeBusinessProcess p = (I_EncodeBusinessProcess) this.getData();
        return new JLabel(p.getName());
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
    /**
     * @see org.dwfa.bpa.process.I_ContainData#getEditorClass()
     */
    public Class<ProcessReadOnly> getEditorClass() {
        return ProcessReadOnly.class;
    }

    
}
