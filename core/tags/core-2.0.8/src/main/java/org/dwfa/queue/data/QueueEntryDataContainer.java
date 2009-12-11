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
 * Created on Feb 17, 2006
 */
package org.dwfa.queue.data;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.dwfa.bpa.data.DataContainer;
import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.gui.glue.PropertyListenerGlue;
import org.dwfa.bpa.tasks.editor.ToStringReadOnlyEditor;
import org.dwfa.queue.bpa.tasks.failsafe.QueueEntryData;

public class QueueEntryDataContainer extends DataContainer {


    /**
     * 
     */
    private static final long serialVersionUID = 971993342760292419L;

    /**
     * @param id
     * @param description
     * @param data
     * @param elementClass
     */
    public QueueEntryDataContainer() {
        super(-1, "Queue Entry #", null, QueueEntryData.class);
    }
    @Override
    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException {
        return new BpaDragAndDropBean(prefix, dndComponent, allowDrop, allowDrag, QueueEntryData.class);
    }
    /**
     * Returns an editor to display properly, but this container is not directly 
     * editable within the GUI. 
     * @throws ClassNotFoundException 
     * @throws IOException 
     * @see org.dwfa.bpa.process.I_ContainData#getEditor()
     */
    public JComponent getEditor() throws SecurityException, NoSuchMethodException, IOException, ClassNotFoundException {
        JLabel textField = new JLabel();
        if (this.getData() != null) {
            textField.setText((String) this.getData().toString());
        } else {
            textField.setText("null");
        }
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        JScrollPane scroller = new JScrollPane(textField);
        scroller.setMinimumSize(new Dimension(150, 60));
        scroller.setPreferredSize(new Dimension(150, 60));
        this.addPropertyChangeListener("data",
                new PropertyListenerGlue("setText",
                        String.class, textField));
        return scroller;
    }
    /**
     * @see org.dwfa.bpa.process.I_ContainData#getEditorClass()
     */
    public Class<?> getEditorClass() {
        return ToStringReadOnlyEditor.class;
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
