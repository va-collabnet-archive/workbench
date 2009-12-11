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
import org.dwfa.bpa.tasks.editor.JTextFieldEditor;


/**
 * @author kec
 *
 */
public class IntegerElement extends DataContainer {


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
	public IntegerElement() {
		super(-1, "Integer Element #", null, Integer.class);
	}
    @Override
    public I_SupportDragAndDrop getDragAndDropSupport(String prefix, I_DoDragAndDrop dndComponent,
            boolean allowDrop, boolean allowDrag) throws ClassNotFoundException {
        return new BpaDragAndDropBean(prefix, dndComponent, allowDrop, allowDrag, Integer.class);
    }
    public JComponent getEditor() throws SecurityException, NoSuchMethodException, IOException, ClassNotFoundException {
        JTextArea textField = new JTextArea(1, 10);
        textField.setLineWrap(false);
        textField.setText((String) this.getData());
        textField.setBorder(BorderFactory.createLoweredBevelBorder());
        textField.getDocument().addDocumentListener(
                new UpdateFieldDocumentListener(this.getWriteMethod(),
                        this, textField, logger));
        JScrollPane scroller = new JScrollPane(textField);
        scroller.setMinimumSize(new Dimension(150, 60));
        scroller.setPreferredSize(new Dimension(150, 60));
        this.addPropertyChangeListener("data",
                new PropertyListenerGlue("setText",
                        String.class, textField));
        return scroller;
    }
    
    public void setIntStr(String val) throws NumberFormatException, IOException {
        this.setData(new Integer(val.trim()));
    }
    public String getIntStr() throws IOException, ClassNotFoundException {
        if (this.getData() != null) {
            return this.getData().toString();
        } 
        return "";
    }
    /**
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @see org.dwfa.bpa.process.I_ContainData#getReadMethod()
     */
    public Method getReadMethod() throws SecurityException, NoSuchMethodException {
        return this.getClass().getMethod("getIntStr", new Class[] {});
    }
    /**
     * @throws NoSuchMethodException 
     * @throws SecurityException 
     * @see org.dwfa.bpa.process.I_ContainData#getWriteMethod()
     */
    public Method getWriteMethod() throws SecurityException, NoSuchMethodException {
        return this.getClass().getMethod("setIntStr", new Class[] { Serializable.class });
    }
    /**
     * @see org.dwfa.bpa.process.I_ContainData#getEditorClass()
     */
    public Class getEditorClass() {
        return JTextFieldEditor.class;
    }
    
    

}
