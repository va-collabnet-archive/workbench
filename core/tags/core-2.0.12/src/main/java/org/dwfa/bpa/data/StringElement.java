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
package org.dwfa.bpa.data;

import java.awt.Dimension;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.dwfa.bpa.dnd.BpaDragAndDropBean;
import org.dwfa.bpa.dnd.I_DoDragAndDrop;
import org.dwfa.bpa.dnd.I_SupportDragAndDrop;
import org.dwfa.bpa.gui.UpdateFieldDocumentListener;
import org.dwfa.bpa.gui.glue.PropertyListenerGlue;
import org.dwfa.bpa.process.I_ContainData;
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;


/**
 * @author kec
 *
 */
public class StringElement extends DataContainer {


    /**
     * 
     */
    private static final long serialVersionUID = 9014638636545880787L;

    /**
	 * @param id
	 * @param description
	 * @param data
	 * @param elementClass
	 */
	public StringElement() {
		super(-1, "String Element #", null, String.class);
	}

    @Override
    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException {
        return new BpaDragAndDropBean(prefix, dndComponent, allowDrop, allowDrag, String.class);
    }
    
    public JComponent getEditor() throws SecurityException, NoSuchMethodException, IOException, ClassNotFoundException {
        JTextArea textField = new JTextArea(1, 10);
        textField.setLineWrap(true);
        textField.setText((String) this.getData());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
                new UpdateFieldDocumentListener(I_ContainData.class
                        .getDeclaredMethod("setData",
                                new Class[] { Serializable.class }),
                        this, textField, logger));
        JScrollPane scroller = new JScrollPane(textField);
        scroller.setMinimumSize(new Dimension(150, 60));
        scroller.setPreferredSize(new Dimension(150, 60));
        this.addPropertyChangeListener("data",
                new PropertyListenerGlue("setText",
                        String.class, textField));
        return scroller;
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
    public Class<JTextFieldEditor> getEditorClass() {
        return JTextFieldEditor.class;
    }
}
