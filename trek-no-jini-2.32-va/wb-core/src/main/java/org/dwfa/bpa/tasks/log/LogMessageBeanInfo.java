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
 * Created on Jun 1, 2005
 */
package org.dwfa.bpa.tasks.log;

import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

import org.dwfa.bpa.tasks.editor.JTextFieldEditor;
import org.dwfa.bpa.tasks.editor.LevelEditor;

/**
 * @author kec
 * 
 */
public class LogMessageBeanInfo extends SimpleBeanInfo {

    /**
     * 
     */
    public LogMessageBeanInfo() {
        super();
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor level = new PropertyDescriptor("level", LogMessage.class);
            level.setBound(true);
            level.setPropertyEditorClass(LevelEditor.class);
            level.setDisplayName("level");
            level.setShortDescription("Logging level for the message.");

            PropertyDescriptor log = new PropertyDescriptor("log", LogMessage.class);
            log.setBound(true);
            log.setPropertyEditorClass(JTextFieldEditor.class);
            log.setDisplayName("log");
            log.setShortDescription("Log upon which to write the message.");

            PropertyDescriptor message = new PropertyDescriptor("message", LogMessage.class);
            message.setBound(true);
            message.setPropertyEditorClass(JTextFieldEditor.class);
            message.setDisplayName("log message");
            message.setShortDescription("A message to write to the worker log.");

            PropertyDescriptor rv[] = { message, log, level };
            return rv;
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * @see java.beans.BeanInfo#getBeanDescriptor()
     */
    public BeanDescriptor getBeanDescriptor() {
        BeanDescriptor bd = new BeanDescriptor(LogMessageOnWorkerLog.class);
        bd.setDisplayName("<html><font color='green'><center>Log on Specified Log");
        return bd;
    }
}
