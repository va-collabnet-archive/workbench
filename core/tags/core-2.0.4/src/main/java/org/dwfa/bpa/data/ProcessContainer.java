/**
 * Copyright (c) 2009 International Health Terminology Standards Development
 * Organisation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Created on Feb 20, 2006
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
    public Class getEditorClass() {
        return ProcessReadOnly.class;
    }

    
}
