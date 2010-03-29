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
 * Created on Dec 22, 2004
 */
package org.dwfa.bpa.process;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Interface for tasks which are contained within business processes. Execution
 * of tasks
 * are done in two steps. First the evaluate method is called to do most of the
 * activity of
 * this task, then the complete method is called to perform any final functions
 * such as writing
 * the process to a serialized stream. The complete action cannot make any
 * changes to the
 * evaluate method must provide all the information necessary for the process to
 * determine the
 * next task before calling the complete method. This seperation is necessary so
 * that if the
 * task is serialized by a process during the complete step, the proper state
 * will be represented
 * when the process is deserialized.
 * 
 * @author kec
 * @model
 * 
 */
public interface I_DefineTask extends I_ManageProperties, I_ManageVetoableProperties, PropertyChangeListener,
        Serializable {
    /**
     * <code>NAME</code>Bound property name for name of this task.
     * 
     * @model
     */
    public static final String NAME = "name";
    /**
     * <code>ID</code> Bound property name for the identifer of this task.
     * 
     * @model
     */
    public static final String ID = "id";

    /**
     * @return Name for this task.
     * @model
     */
    public String getName();

    /**
     * @return identifer for this task.
     * @model
     */
    public int getId();

    /**
     * @param id Identifer for this task.
     * @throws PropertyVetoException Thrown if the identifer has already been
     *             set.
     *             Prevents the same task from being added twice.
     * @model
     */
    public void setId(int id) throws PropertyVetoException;

    /**
     * Part 1 of a two step command pattern for task execution.
     * 
     * @param process The process that contains this task.
     * @param worker The worker providing the thread of execution for this task.
     * @return The exit condition for this task.
     * @throws TaskFailedException
     * @model
     */
    public Condition evaluate(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException;

    /**
     * Part 2 of a two step command pattern for task execution.
     * 
     * @param process The process that contains this task.
     * @param worker The worker providing the thread of execution for this task.
     * @throws TaskFailedException
     * @model
     */
    public void complete(I_EncodeBusinessProcess process, I_Work worker) throws TaskFailedException;

    /**
     * @return The possible evaluation conditions for this task.
     */
    public Collection<Condition> getConditions();

    /**
     * @return The data container identifiers used by this task.
     * @model
     * @deprecated
     */
    public int[] getDataContainerIds();

    /**
     * Check if a message of the given level would actually be logged by this
     * logger.
     * This check is based on the Loggers effective level,
     * which may be inherited from its parent.
     * 
     * @param level a messaging log level
     * @return true if the given message level is currently being logged.
     * @model
     */
    public boolean isLoggable(Level level);

    /**
     * Provides a uniform logging strategy for logging messages by tasks.
     * 
     * @return A logger named by the I_DefineTask interface.
     * @model
     */
    public Logger getLogger();

    /**
     * @return
     * @throws IntrospectionException
     * @model
     * @model
     * @model
     * @model
     * @model
     */
    public BeanInfo getBeanInfo() throws IntrospectionException;

    /**
     * BeanInfo for all properties, even those considered "internal" only (for
     * processes that
     * choose not to externalize all their internal properties when
     * participating in a process as a task).
     * 
     * @return
     * @throws IntrospectionException
     */
    public BeanInfo getAllPropertiesBeanInfo() throws IntrospectionException;
}
